package com.example.eshopapi.repository;

import com.example.eshopapi.dto.CategoryStatResponse;
import com.example.eshopapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("SELECT new com.example.eshopapi.dto.CategoryStatResponse(" +
            "c.id, c.name, COUNT(p.id), SUM(p.price * p.quantity)) " +
            "FROM Category c LEFT JOIN Product p ON p.category = c " +
            "GROUP BY c.id, c.name " +
            "ORDER BY c.id ASC")
    List<CategoryStatResponse> getCategoryStats();
}