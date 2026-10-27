package com.example.eshopapi.controller;

import com.example.eshopapi.dto.PageResponse;
import com.example.eshopapi.dto.ProductRequest;
import com.example.eshopapi.dto.ProductResponse;
import com.example.eshopapi.dto.StockUpdateRequest;
import com.example.eshopapi.exception.BadRequestException;
import com.example.eshopapi.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "price", "quantity", "createdAt"
    );

    // P1: Tìm kiếm + phân trang + sắp xếp
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
    ) {
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Tham số page phải >= 0 và size phải > 0");
        }

        String sortField = sort[0].trim();
        // Nâng cao N2: Kiểm tra thuộc tính sort hợp lệ
        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            throw new BadRequestException("Thuộc tính sắp xếp không hợp lệ: " + sortField);
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (sort.length > 1) {
            String dirStr = sort[1].trim().toLowerCase();
            if ("desc".equals(dirStr)) {
                direction = Sort.Direction.DESC;
            } else if (!"asc".equals(dirStr)) {
                throw new BadRequestException("Hướng sắp xếp phải là asc hoặc desc");
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return ResponseEntity.ok(productService.searchProducts(keyword, categoryId, minPrice, maxPrice, pageable));
    }

    // P2: Chi tiết sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // P3: Thêm sản phẩm
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        URI location = URI.create("/api/products/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    // P4: Sửa toàn bộ thông tin sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // P5: Xoá sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Nâng cao N1: Tăng/giảm tồn kho
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable("id") Long id,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.updateStock(id, request.delta()));
    }
}