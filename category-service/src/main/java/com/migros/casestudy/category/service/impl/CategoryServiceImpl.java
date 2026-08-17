package com.migros.casestudy.category.service.impl;

import com.migros.casestudy.category.dto.response.CategoryResponse;
import com.migros.casestudy.category.entity.Category;
import com.migros.casestudy.category.exception.NotFoundException;
import com.migros.casestudy.category.mapper.CategoryMapper;
import com.migros.casestudy.category.repository.CategoryRepository;
import com.migros.casestudy.category.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getByCode(String code) {
        String normalizedCode = normalizeCode(code);

        Category category = categoryRepository
                .findByCode(normalizedCode)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Kategori bulunamadı: "
                                        + normalizedCode
                        )
                );

        return categoryMapper.toResponse(category);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new NotFoundException(
                    "Kategori kodu boş olamaz."
            );
        }

        return code.trim().toUpperCase();
    }
}