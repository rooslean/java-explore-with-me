package ru.practicum.ewm.category;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.ObjectNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = CategoryMapper.mapToCategory(categoryDto);
        categoryDto = CategoryMapper.mapToCategoryDto(categoryRepository.save(category));
        return categoryDto;
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(ObjectNotFoundException::new);
        category.setName(categoryDto.getName());
        categoryDto = CategoryMapper.mapToCategoryDto(categoryRepository.save(category));
        return categoryDto;
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryDto> findAll(int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        return categoryRepository.findAll(page)
                .getContent()
                .stream()
                .map(CategoryMapper::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(ObjectNotFoundException::new);
        return CategoryMapper.mapToCategoryDto(category);
    }
}
