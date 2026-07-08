package com.oms.backend.controller;

import com.oms.backend.dto.OrderDtos.*;
import com.oms.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ---- Customer endpoints ----
    @PostMapping("/api/customer/orders")
    public ResponseEntity<OrderResponse> createOrder(Authentication auth, @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(auth.getName(), request));
    }

    @GetMapping("/api/customer/orders")
    public ResponseEntity<List<OrderResponse>> myOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getByCustomer(auth.getName()));
    }

    @GetMapping("/api/customer/orders/{id}")
    public ResponseEntity<OrderResponse> myOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    // ---- Admin endpoints ----
    @GetMapping("/api/admin/orders")
    public ResponseEntity<List<OrderResponse>> allOrders(@RequestParam(required = false) String status) {
        if (status != null) return ResponseEntity.ok(orderService.getByStatus(status));
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/api/admin/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PutMapping("/api/admin/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.getStatus()));
    }
}
