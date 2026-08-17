package com.migros.casestudy.category.mapper;

import com.migros.casestudy.category.dto.response.CategoryResponse;
import com.migros.casestudy.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.code(),
                category.name()
        );
    }
}