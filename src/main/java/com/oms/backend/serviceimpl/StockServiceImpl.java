package com.oms.backend.serviceimpl;

import com.oms.backend.dto.StockAdjustRequest;
import com.oms.backend.entity.Product;
import com.oms.backend.entity.Stock;
import com.oms.backend.entity.StockHistory;
import com.oms.backend.exception.BadRequestException;
import com.oms.backend.exception.ResourceNotFoundException;
import com.oms.backend.repository.ProductRepository;
import com.oms.backend.repository.StockHistoryRepository;
import com.oms.backend.repository.StockRepository;
import com.oms.backend.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void adjustStock(StockAdjustRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        Stock stock = stockRepository.findByProductId(product.getId())
                .orElseGet(() -> Stock.builder().product(product).quantity(0).build());

        int previousQty = stock.getQuantity();
        int newQty;
        String type = request.getChangeType().toUpperCase();

        switch (type) {
            case "ADD" -> newQty = previousQty + request.getQuantity();
            case "REMOVE" -> {
                newQty = previousQty - request.getQuantity();
                if (newQty < 0) throw new BadRequestException("Insufficient stock to remove");
            }
            case "ADJUST" -> newQty = request.getQuantity();
            default -> throw new BadRequestException("Invalid change type: " + type);
        }

        stock.setQuantity(newQty);
        stock.setUpdatedAt(LocalDateTime.now());
        stockRepository.save(stock);

        product.setStockQty(newQty);
        productRepository.save(product);

        StockHistory history = StockHistory.builder()
                .product(product)
                .changeType(type)
                .quantity(request.getQuantity())
                .previousQty(previousQty)
                .newQty(newQty)
                .remarks(request.getRemarks())
                .build();
        stockHistoryRepository.save(history);
    }

    @Override
    public List<StockHistory> getHistory(Long productId) {
        return stockHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }
}
