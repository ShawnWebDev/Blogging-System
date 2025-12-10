package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.CategoryResponseDto;
import com.webdev.bloggingsystem.entities.Category;
import com.webdev.bloggingsystem.repositories.CategoryRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepo categoryRepo;

    public CategoryServiceImpl(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        List<Category> categoryList = (List<Category>) categoryRepo.findAll();

        if (categoryList.isEmpty()) {
            return List.of();
        }

        List<CategoryResponseDto> categoryResponseDtoList = new ArrayList<>();
        for (Category category : categoryList) {
            CategoryResponseDto categoryDto = new CategoryResponseDto(
                    category.getId(),
                    category.getCategoryName(),
                    category.getDescription()
            );
            categoryResponseDtoList.add(categoryDto);
        }

        return categoryResponseDtoList;
    }


}
