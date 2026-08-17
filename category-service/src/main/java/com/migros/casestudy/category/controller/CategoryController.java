package com.migros.casestudy.category.controller;

import com.migros.casestudy.category.dto.response.CategoryResponse;
import com.migros.casestudy.category.service.CategoryService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Sabit kategori lookup işlemleri")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Kategorileri listele")
    public ResponseEntity<List<CategoryResponse>> getAll() {
        List<CategoryResponse> response =
                categoryService.getAll();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Kategori getir", description = "Kategori kodunu büyük/küçük harf duyarsız şekilde arar.")
    public ResponseEntity<CategoryResponse> getByCode(
            @PathVariable String code
    ) {
        CategoryResponse response =
                categoryService.getByCode(code);

        return ResponseEntity.ok(response);
    }
}