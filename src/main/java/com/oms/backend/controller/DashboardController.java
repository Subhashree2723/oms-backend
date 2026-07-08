package com.oms.backend.controller;

import com.oms.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProductRepository productRepository;
    private final OrdersRepository ordersRepository;
    private final CustomerRepository customerRepository;
    private final CustomerIssueRepository customerIssueRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", productRepository.count());
        summary.put("totalOrders", ordersRepository.count());
        summary.put("totalCustomers", customerRepository.count());
        summary.put("openIssues", customerIssueRepository.findByStatus(
                com.oms.backend.entity.CustomerIssue.IssueStatus.OPEN).size());

        double revenue = ordersRepository.findAll().stream()
                .filter(o -> o.getStatus() == com.oms.backend.entity.Orders.OrderStatus.DELIVERED)
                .mapToDouble(o -> o.getGrandTotal().doubleValue())
                .sum();
        summary.put("totalRevenue", revenue);

        Map<String, Long> statusBreakdown = new HashMap<>();
        for (com.oms.backend.entity.Orders.OrderStatus s : com.oms.backend.entity.Orders.OrderStatus.values()) {
            statusBreakdown.put(s.name(), (long) ordersRepository.findByStatus(s).size());
        }
        summary.put("ordersByStatus", statusBreakdown);

        return ResponseEntity.ok(summary);
    }
}
