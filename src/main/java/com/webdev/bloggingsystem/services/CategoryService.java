package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.CategoryRequestDto;
import com.webdev.bloggingsystem.dto.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> getAllCategories();
    CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto);
    CategoryResponseDto updateCategory(CategoryRequestDto categoryRequestDto, int id);
    void deleteCategory(int id);
}
