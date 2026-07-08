package com.oms.backend.service;

import com.oms.backend.dto.CategoryDto;
import java.util.List;

public interface CategoryService {
    CategoryDto create(CategoryDto dto);
    CategoryDto update(Long id, CategoryDto dto);
    void delete(Long id);
    CategoryDto getById(Long id);
    List<CategoryDto> getAll();
    /** Top-level (parentId == null) categories, each with its subCategories populated. */
    List<CategoryDto> getTopLevelWithSubCategories();
    List<CategoryDto> getSubCategories(Long parentId);
}
