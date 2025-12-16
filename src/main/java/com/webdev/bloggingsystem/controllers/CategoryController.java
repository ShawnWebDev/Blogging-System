package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.CategoryRequestDto;
import com.webdev.bloggingsystem.dto.CategoryResponseDto;
import com.webdev.bloggingsystem.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // todo : set up input validation and return validation errors if needed on request dto in create and update methods
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody @Valid CategoryRequestDto categoryDto) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@RequestBody @Valid CategoryRequestDto categoryDto, @PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}