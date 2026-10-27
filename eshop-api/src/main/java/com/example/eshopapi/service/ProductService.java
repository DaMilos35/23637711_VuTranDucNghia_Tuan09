package com.example.eshopapi.service;

import com.example.eshopapi.dto.PageResponse;
import com.example.eshopapi.dto.ProductRequest;
import com.example.eshopapi.dto.ProductResponse;
import com.example.eshopapi.entity.Category;
import com.example.eshopapi.entity.Product;
import com.example.eshopapi.exception.ConflictException;
import com.example.eshopapi.exception.NotFoundException;
import com.example.eshopapi.repository.CategoryRepository;
import com.example.eshopapi.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> searchProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        String cleanKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Product> pageResult = productRepository.search(cleanKeyword, categoryId, minPrice, maxPrice, pageable);

        List<ProductResponse> content = pageResult.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategoryId(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Không tìm thấy danh mục id = " + categoryId);
        }
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm id = " + id));
        return mapToResponse(p);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục id = " + request.categoryId()));

        Product product = Product.builder()
                .name(request.name().trim())
                .description(request.description())
                .price(request.price())
                .quantity(request.quantity())
                .imageUrl(request.imageUrl())
                .category(category)
                .build();

        Product saved = productRepository.saveAndFlush(product);
        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm id = " + id));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục id = " + request.categoryId()));

        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);

        Product updated = productRepository.saveAndFlush(product);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy sản phẩm id = " + id);
        }
        productRepository.deleteById(id);
        entityManager.flush(); // Kích hoạt kiểm tra khoá ngoại ngay lập tức nếu có trong order_detail
    }

    // Nâng cao N1: Tăng/giảm tồn kho
    @Transactional
    public ProductResponse updateStock(Long id, int delta) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm id = " + id));

        int newQuantity = product.getQuantity() + delta;
        if (newQuantity < 0) {
            throw new ConflictException("Số lượng tồn kho sau khi thay đổi không được nhỏ hơn 0");
        }

        product.setQuantity(newQuantity);
        Product updated = productRepository.saveAndFlush(product);
        return mapToResponse(updated);
    }

    private ProductResponse mapToResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getQuantity(),
                p.getImageUrl(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.getCreatedAt()
        );
    }
}