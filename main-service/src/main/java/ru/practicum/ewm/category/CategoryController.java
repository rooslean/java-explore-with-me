package ru.practicum.ewm.category;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/categories")
    ResponseEntity<List<CategoryDto>> getAll(@RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(categoryService.findAll(from, size), HttpStatus.OK);
    }

    @GetMapping("/categories/{catId}")
    ResponseEntity<CategoryDto> getById(@PathVariable Long catId) {
        return new ResponseEntity<>(categoryService.findById(catId), HttpStatus.OK);
    }

    @PostMapping("/admin/categories")
    ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        return new ResponseEntity<>(categoryService.createCategory(categoryDto), HttpStatus.CREATED);
    }

    @PatchMapping("/admin/categories/{catId}")
    ResponseEntity<CategoryDto> updateCategory(@PathVariable Long catId, @RequestBody @Valid CategoryDto categoryDto) {
        return new ResponseEntity<>(categoryService.updateCategory(catId, categoryDto), HttpStatus.OK);
    }

    @DeleteMapping("/admin/categories/{catId}")
    ResponseEntity<Object> deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
