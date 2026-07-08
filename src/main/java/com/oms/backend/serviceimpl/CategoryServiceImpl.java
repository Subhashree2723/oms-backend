package com.oms.backend.serviceimpl;

import com.oms.backend.dto.CategoryDto;
import com.oms.backend.entity.Category;
import com.oms.backend.exception.BadRequestException;
import com.oms.backend.exception.ResourceNotFoundException;
import com.oms.backend.repository.CategoryRepository;
import com.oms.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private CategoryDto toDto(Category c) {
        return CategoryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .parentId(c.getParentId())
                .build();
    }

    @Override
    public CategoryDto create(CategoryDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BadRequestException("Category already exists: " + dto.getName());
        }
        Category c = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .parentId(dto.getParentId())
                .build();
        return toDto(categoryRepository.save(c));
    }

    @Override
    public CategoryDto update(Long id, CategoryDto dto) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) {
            c.setImageUrl(dto.getImageUrl());
        }
        c.setParentId(dto.getParentId());
        return toDto(categoryRepository.save(c));
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDto getById(Long id) {
        return categoryRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Override
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getTopLevelWithSubCategories() {
        List<Category> topLevel = categoryRepository.findByParentIdIsNullOrderByIdAsc();
        return topLevel.stream().map(c -> {
            CategoryDto dto = toDto(c);
            List<CategoryDto> subs = categoryRepository.findByParentIdOrderByIdAsc(c.getId())
                    .stream().map(this::toDto).collect(Collectors.toList());
            dto.setSubCategories(subs);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getSubCategories(Long parentId) {
        return categoryRepository.findByParentIdOrderByIdAsc(parentId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }
}
