package com.oms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryDto {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    private String imageUrl;
    private Long parentId;
    /** Populated only on the "/top-level" endpoint response, for convenience. */
    private java.util.List<CategoryDto> subCategories;
}
