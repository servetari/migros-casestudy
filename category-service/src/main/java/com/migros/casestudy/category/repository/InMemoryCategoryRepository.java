package com.migros.casestudy.category.repository;

import com.migros.casestudy.category.entity.Category;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryCategoryRepository
        implements CategoryRepository {

    private static final List<Category> CATEGORIES = List.of(
            new Category("BK", "Bakliyat"),
            new Category("ME", "Meyve"),
            new Category("ET", "Et"),
            new Category("IC", "İçecek"),
            new Category("BL", "Balık")
    );

    @Override
    public List<Category> findAll() {
        return CATEGORIES;
    }

    @Override
    public Optional<Category> findByCode(String code) {
        return CATEGORIES.stream()
                .filter(category ->
                        category.code().equalsIgnoreCase(code)
                )
                .findFirst();
    }

    @Override
    public boolean existsByCode(String code) {
        return CATEGORIES.stream()
                .anyMatch(category ->
                        category.code().equalsIgnoreCase(code)
                );
    }
}