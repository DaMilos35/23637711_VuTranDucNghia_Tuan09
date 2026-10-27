package com.example.eshopapi.service;

import com.example.eshopapi.dto.CategoryRequest;
import com.example.eshopapi.dto.CategoryResponse;
import com.example.eshopapi.dto.CategoryStatResponse;
import com.example.eshopapi.entity.Category;
import com.example.eshopapi.exception.ConflictException;
import com.example.eshopapi.exception.NotFoundException;
import com.example.eshopapi.repository.CategoryRepository;
import com.example.eshopapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục id = " + id));
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Tên danh mục đã tồn tại");
        }
        Category category = Category.builder()
                .name(request.name().trim())
                .description(request.description())
                .build();
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName(), saved.getDescription());
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục id = " + id));

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Tên danh mục đã tồn tại ở danh mục khác");
        }

        category.setName(request.name().trim());
        category.setDescription(request.description());
        Category updated = categoryRepository.save(category);
        return new CategoryResponse(updated.getId(), updated.getName(), updated.getDescription());
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy danh mục id = " + id);
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException("Không thể xoá danh mục đang có sản phẩm");
        }
        categoryRepository.deleteById(id);
    }

    // Nâng cao N3
    @Transactional(readOnly = true)
    public List<CategoryStatResponse> getCategoryStats() {
        return categoryRepository.getCategoryStats();
    }
}