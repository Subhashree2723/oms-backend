package com.oms.backend.controller;

import com.oms.backend.entity.Delivery;
import com.oms.backend.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<List<Delivery>> getAll() {
        return ResponseEntity.ok(deliveryService.getAll());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Delivery> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getByOrderId(orderId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Delivery> updateStatus(@PathVariable Long orderId, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(deliveryService.updateStatus(orderId, body.get("status"), body.get("notes")));
    }
}
