package com.migros.casestudy.category.repository;

import com.migros.casestudy.category.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    List<Category> findAll();

    Optional<Category> findByCode(String code);

    boolean existsByCode(String code);
}