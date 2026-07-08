package com.oms.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDto {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private Long categoryId;
    private String categoryName;
    private String description;
    @NotNull @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;
    @NotNull
    private BigDecimal gstPercent;
    private Integer stockQty;
    private String imageUrl;
    private Boolean active;
}
