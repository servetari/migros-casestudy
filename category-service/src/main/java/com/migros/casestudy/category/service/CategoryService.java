package com.migros.casestudy.category.service;

import com.migros.casestudy.category.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAll();

    CategoryResponse getByCode(String code);
}