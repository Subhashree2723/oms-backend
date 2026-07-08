package com.oms.backend.repository;

import com.oms.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Category> findByParentIdIsNullOrderByIdAsc();
    List<Category> findByParentIdOrderByIdAsc(Long parentId);
}
