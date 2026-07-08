package com.oms.backend.service;

import com.oms.backend.dto.StockAdjustRequest;
import com.oms.backend.entity.StockHistory;
import java.util.List;

public interface StockService {
    void adjustStock(StockAdjustRequest request);
    List<StockHistory> getHistory(Long productId);
}
