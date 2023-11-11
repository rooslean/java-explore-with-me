package ru.practicum.ewm.category;

public class CategoryMapper {
    public static Category mapToCategory(CategoryDto categoryDto) {
        return Category.builder()
                .name(categoryDto.getName())
                .build();
    }

    public static CategoryDto mapToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
