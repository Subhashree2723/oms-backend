package com.oms.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StockAdjustRequest {
    @NotNull
    private Long productId;
    @NotNull
    private String changeType; // ADD, REMOVE, ADJUST
    @NotNull
    private Integer quantity;
    private String remarks;
}
