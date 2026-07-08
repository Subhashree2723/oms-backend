package com.oms.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull
        private Long productId;
        @NotNull @Min(1)
        private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateOrderRequest {
        @NotEmpty
        private List<OrderItemRequest> items;
        @NotBlank
        private String deliveryAddress;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal gstAmount;
        private BigDecimal subtotal;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderResponse {
        private Long id;
        private String orderNumber;
        private Long customerId;
        private String customerName;
        private String status;
        private BigDecimal totalAmount;
        private BigDecimal gstAmount;
        private BigDecimal grandTotal;
        private LocalDateTime orderDate;
        private List<OrderItemResponse> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotBlank
        private String status;
    }
}
