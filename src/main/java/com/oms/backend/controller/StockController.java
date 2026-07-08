package com.oms.backend.controller;

import com.oms.backend.dto.ApiResponse;
import com.oms.backend.dto.StockAdjustRequest;
import com.oms.backend.entity.StockHistory;
import com.oms.backend.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse> adjust(@Valid @RequestBody StockAdjustRequest request) {
        stockService.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Stock updated").build());
    }

    @GetMapping("/history/{productId}")
    public ResponseEntity<List<StockHistory>> history(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getHistory(productId));
    }
}
