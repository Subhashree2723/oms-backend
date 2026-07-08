package com.oms.backend.service;

import com.oms.backend.dto.ProductDto;
import java.util.List;

public interface ProductService {
    ProductDto create(ProductDto dto);
    ProductDto update(Long id, ProductDto dto);
    void delete(Long id);
    ProductDto getById(Long id);
    List<ProductDto> getAll();
    List<ProductDto> search(String keyword, Long categoryId);
    ProductDto uploadImage(Long id, String imageUrl);
}
