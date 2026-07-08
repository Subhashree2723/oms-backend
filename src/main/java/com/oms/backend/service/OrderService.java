package com.oms.backend.service;

import com.oms.backend.dto.OrderDtos.*;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(String username, CreateOrderRequest request);
    OrderResponse updateStatus(Long orderId, String status);
    OrderResponse getById(Long orderId);
    List<OrderResponse> getAll();
    List<OrderResponse> getByCustomer(String username);
    List<OrderResponse> getByStatus(String status);
}
