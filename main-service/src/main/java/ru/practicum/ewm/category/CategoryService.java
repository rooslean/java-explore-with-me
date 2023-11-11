package ru.practicum.ewm.category;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto);

    void deleteCategory(Long categoryId);

    List<CategoryDto> findAll(int from, int size);

    CategoryDto findById(Long id);
}
