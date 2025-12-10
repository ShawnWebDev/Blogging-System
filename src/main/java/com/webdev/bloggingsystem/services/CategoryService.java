package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> getAllCategories();
}
