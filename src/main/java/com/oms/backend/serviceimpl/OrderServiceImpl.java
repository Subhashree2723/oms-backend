package com.oms.backend.serviceimpl;

import com.oms.backend.dto.OrderDtos.*;
import com.oms.backend.entity.*;
import com.oms.backend.exception.BadRequestException;
import com.oms.backend.exception.ResourceNotFoundException;
import com.oms.backend.repository.*;
import com.oms.backend.service.InvoiceService;
import com.oms.backend.service.NotificationService;
import com.oms.backend.service.OrderService;
import com.oms.backend.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final NotificationService notificationService;
    private final WhatsappService whatsappService;

    private OrderResponse toResponse(Orders order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = items.stream().map(i -> OrderItemResponse.builder()
                .productId(i.getProduct().getId())
                .productName(i.getProduct().getName())
                .quantity(i.getQuantity())
                .price(i.getPrice())
                .gstAmount(i.getGstAmount())
                .subtotal(i.getSubtotal())
                .build()).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getFullName())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .gstAmount(order.getGstAmount())
                .grandTotal(order.getGrandTotal())
                .orderDate(order.getOrderDate())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional
    public OrderResponse createOrder(String username, CreateOrderRequest request) {
        Customer customer = customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user: " + username));

        String orderNumber = "ORD" + System.currentTimeMillis();

        Orders order = Orders.builder()
                .orderNumber(orderNumber)
                .customer(customer)
                .status(Orders.OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .build();
        order = ordersRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal gstTotal = BigDecimal.ZERO;
        List<OrderItem> savedItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            if (product.getStockQty() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal lineBase = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            BigDecimal lineGst = lineBase.multiply(product.getGstPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineSubtotal = lineBase.add(lineGst);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .price(product.getPrice())
                    .gstAmount(lineGst)
                    .subtotal(lineSubtotal)
                    .build();
            savedItems.add(orderItemRepository.save(item));

            total = total.add(lineBase);
            gstTotal = gstTotal.add(lineGst);

            // reduce stock
            int newQty = product.getStockQty() - itemReq.getQuantity();
            product.setStockQty(newQty);
            productRepository.save(product);

            stockRepository.findByProductId(product.getId()).ifPresent(s -> {
                s.setQuantity(newQty);
                stockRepository.save(s);
            });

            stockHistoryRepository.save(StockHistory.builder()
                    .product(product)
                    .changeType("REMOVE")
                    .quantity(itemReq.getQuantity())
                    .previousQty(newQty + itemReq.getQuantity())
                    .newQty(newQty)
                    .remarks("Order " + orderNumber)
                    .build());
        }

        BigDecimal grandTotal = total.add(gstTotal);
        order.setTotalAmount(total);
        order.setGstAmount(gstTotal);
        order.setGrandTotal(grandTotal);
        order = ordersRepository.save(order);

        // create delivery record
        Delivery delivery = Delivery.builder()
                .order(order)
                .address(request.getDeliveryAddress())
                .status(Delivery.DeliveryStatus.PENDING)
                .build();
        deliveryRepository.save(delivery);

        // notify admin users via in-app notification + simulated WhatsApp
        String msg = "New order " + orderNumber + " placed by " + customer.getFullName()
                + " for a grand total of " + grandTotal;
        userRepository.findAll().stream()
                .filter(u -> u.getRole().getName().equals("ROLE_ADMIN"))
                .forEach(admin -> notificationService.notify(admin.getId(), "New Order Received", msg));

        whatsappService.sendOrderNotification(order.getId(), customer.getPhone(), msg);

        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, String status) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        Orders.OrderStatus newStatus;
        try {
            newStatus = Orders.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + status);
        }
        order.setStatus(newStatus);
        order = ordersRepository.save(order);

        // keep delivery status roughly in sync
        deliveryRepository.findByOrderId(orderId).ifPresent(d -> {
            switch (newStatus) {
                case OUT_FOR_DELIVERY -> d.setStatus(Delivery.DeliveryStatus.OUT_FOR_DELIVERY);
                case DELIVERED -> {
                    d.setStatus(Delivery.DeliveryStatus.DELIVERED);
                    d.setDeliveryDate(LocalDateTime.now());
                }
                default -> {}
            }
            deliveryRepository.save(d);
        });

        // auto-generate invoice once order is confirmed
        if (newStatus == Orders.OrderStatus.CONFIRMED) {
            invoiceService.generateInvoice(orderId);
        }

        // notify the customer
        User customerUser = order.getCustomer().getUser();
        notificationService.notify(customerUser.getId(), "Order Status Updated",
                "Your order " + order.getOrderNumber() + " is now " + newStatus);

        return toResponse(order);
    }

    @Override
    public OrderResponse getById(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getAll() {
        return ordersRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getByCustomer(String username) {
        Customer customer = customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user: " + username));
        return ordersRepository.findByCustomerId(customer.getId()).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getByStatus(String status) {
        Orders.OrderStatus s;
        try {
            s = Orders.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + status);
        }
        return ordersRepository.findByStatus(s).stream().map(this::toResponse).collect(Collectors.toList());
    }
}
