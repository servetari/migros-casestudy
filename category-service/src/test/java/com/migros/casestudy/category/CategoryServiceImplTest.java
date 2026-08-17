package com.migros.casestudy.category;

import com.migros.casestudy.category.dto.response.CategoryResponse;
import com.migros.casestudy.category.entity.Category;
import com.migros.casestudy.category.exception.NotFoundException;
import com.migros.casestudy.category.mapper.CategoryMapper;
import com.migros.casestudy.category.repository.CategoryRepository;
import com.migros.casestudy.category.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(
                categoryRepository,
                categoryMapper
        );
    }

    @Test
    void getAll_shouldReturnAllCategories() {
        Category fruitCategory =
                new Category("ME", "Meyve");

        Category fishCategory =
                new Category("BL", "Balık");

        CategoryResponse fruitResponse =
                new CategoryResponse("ME", "Meyve");

        CategoryResponse fishResponse =
                new CategoryResponse("BL", "Balık");

        when(categoryRepository.findAll())
                .thenReturn(
                        List.of(
                                fruitCategory,
                                fishCategory
                        )
                );

        when(categoryMapper.toResponse(fruitCategory))
                .thenReturn(fruitResponse);

        when(categoryMapper.toResponse(fishCategory))
                .thenReturn(fishResponse);

        List<CategoryResponse> result =
                categoryService.getAll();

        assertEquals(2, result.size());
        assertSame(fruitResponse, result.get(0));
        assertSame(fishResponse, result.get(1));

        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponse(fruitCategory);
        verify(categoryMapper).toResponse(fishCategory);
    }

    @Test
    void getAll_shouldReturnEmptyList_whenCategoryListIsEmpty() {
        when(categoryRepository.findAll())
                .thenReturn(List.of());

        List<CategoryResponse> result =
                categoryService.getAll();

        assertEquals(0, result.size());

        verify(categoryRepository).findAll();
    }

    @Test
    void getByCode_shouldReturnCategory_whenCategoryExists() {
        Category category =
                new Category("ME", "Meyve");

        CategoryResponse expectedResponse =
                new CategoryResponse("ME", "Meyve");

        when(categoryRepository.findByCode("ME"))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toResponse(category))
                .thenReturn(expectedResponse);

        CategoryResponse result =
                categoryService.getByCode("ME");

        assertSame(expectedResponse, result);
        assertEquals("ME", result.code());
        assertEquals("Meyve", result.name());

        verify(categoryRepository).findByCode("ME");
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void getByCode_shouldNormalizeCode() {
        Category category =
                new Category("ME", "Meyve");

        CategoryResponse expectedResponse =
                new CategoryResponse("ME", "Meyve");

        when(categoryRepository.findByCode("ME"))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toResponse(category))
                .thenReturn(expectedResponse);

        CategoryResponse result =
                categoryService.getByCode("  me  ");

        assertEquals("ME", result.code());
        assertEquals("Meyve", result.name());

        verify(categoryRepository).findByCode("ME");
    }

    @Test
    void getByCode_shouldThrowNotFoundException_whenCategoryDoesNotExist() {
        when(categoryRepository.findByCode("XX"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> categoryService.getByCode("XX")
        );

        verify(categoryRepository).findByCode("XX");
        verify(categoryMapper, never())
                .toResponse(
                        org.mockito.ArgumentMatchers.any(Category.class)
                );
    }

    @Test
    void getByCode_shouldThrowNotFoundException_whenCodeIsBlank() {
        assertThrows(
                NotFoundException.class,
                () -> categoryService.getByCode(" ")
        );

        verify(categoryRepository, never())
                .findByCode(
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    @Test
    void getByCode_shouldThrowNotFoundException_whenCodeIsNull() {
        assertThrows(
                NotFoundException.class,
                () -> categoryService.getByCode(null)
        );

        verify(categoryRepository, never())
                .findByCode(
                        org.mockito.ArgumentMatchers.anyString()
                );
    }
}