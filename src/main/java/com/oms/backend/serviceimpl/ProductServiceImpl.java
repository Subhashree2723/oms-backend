package com.oms.backend.serviceimpl;

import com.oms.backend.dto.ProductDto;
import com.oms.backend.entity.Category;
import com.oms.backend.entity.Product;
import com.oms.backend.entity.Stock;
import com.oms.backend.exception.ResourceNotFoundException;
import com.oms.backend.repository.CategoryRepository;
import com.oms.backend.repository.ProductRepository;
import com.oms.backend.repository.StockRepository;
import com.oms.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;

    private ProductDto toDto(Product p) {
        return ProductDto.builder()
                .id(p.getId())
                .name(p.getName())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .gstPercent(p.getGstPercent())
                .stockQty(p.getStockQty())
                .imageUrl(p.getImageUrl())
                .active(p.getActive())
                .build();
    }

    @Override
    public ProductDto create(ProductDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));

        Product p = Product.builder()
                .name(dto.getName())
                .category(category)
                .description(dto.getDescription())
                .price(dto.getPrice())
                .gstPercent(dto.getGstPercent())
                .stockQty(dto.getStockQty() == null ? 0 : dto.getStockQty())
                .imageUrl(dto.getImageUrl())
                .active(dto.getActive() == null ? true : dto.getActive())
                .build();
        p = productRepository.save(p);

        // initialize stock row
        Stock stock = Stock.builder().product(p).quantity(p.getStockQty()).build();
        stockRepository.save(stock);

        return toDto(p);
    }

    @Override
    public ProductDto update(Long id, ProductDto dto) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));
            p.setCategory(category);
        }
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setGstPercent(dto.getGstPercent());
        if (dto.getActive() != null) p.setActive(dto.getActive());
        if (dto.getImageUrl() != null) p.setImageUrl(dto.getImageUrl());

        return toDto(productRepository.save(p));
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductDto getById(Long id) {
        return productRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Override
    public List<ProductDto> getAll() {
        return productRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> search(String keyword, Long categoryId) {
        return productRepository.search(keyword, categoryId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ProductDto uploadImage(Long id, String imageUrl) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        p.setImageUrl(imageUrl);
        return toDto(productRepository.save(p));
    }
}
