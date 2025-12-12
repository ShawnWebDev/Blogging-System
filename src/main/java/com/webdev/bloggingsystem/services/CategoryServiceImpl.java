package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.CategoryRequestDto;
import com.webdev.bloggingsystem.dto.CategoryResponseDto;
import com.webdev.bloggingsystem.dto.UserProfile;
import com.webdev.bloggingsystem.entities.Category;
import com.webdev.bloggingsystem.exceptions.ResourceNotFoundException;
import com.webdev.bloggingsystem.repositories.CategoryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final static Logger logger  = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepo categoryRepo;
    private final AuthService authService;

    public CategoryServiceImpl(CategoryRepo categoryRepo, AuthService authService) {
        this.categoryRepo = categoryRepo;
        this.authService = authService;
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        List<Category> categoryList = (List<Category>) categoryRepo.findAll();

        if (categoryList.isEmpty()) {
            return List.of();
        }

        List<Integer> categoryIds = categoryList.stream().map(Category::getId).toList();
        Map<Integer, Integer> postToCategoryCountMap = this.mapCategoryIdToPostCount(categoryIds);
        List<CategoryResponseDto> categoryResponseDtoList = new ArrayList<>();
        for (Category category : categoryList) {
            CategoryResponseDto categoryDto = new CategoryResponseDto(
                    category.getId(),
                    category.getCategoryName(),
                    category.getDescription(),
                    postToCategoryCountMap.getOrDefault(category.getId(), 0)
            );
            categoryResponseDtoList.add(categoryDto);
        }

        return categoryResponseDtoList;
    }

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto) {
        this.checkAuthorization();
        Category category = new Category(
                categoryRequestDto.categoryName(),
                categoryRequestDto.description()
        );
        categoryRepo.save(category);

        return new CategoryResponseDto(
                category.getId(),
                category.getCategoryName(),
                category.getDescription(),
                0
        );
    }

    @Override
    public CategoryResponseDto updateCategory(CategoryRequestDto categoryRequestDto, int id) {
        this.checkAuthorization();
        Category categoryToUpdate = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        if (categoryRequestDto.categoryName() != null) {
            categoryToUpdate.setCategoryName(categoryRequestDto.categoryName());
        }
        if (categoryRequestDto.description() != null) {
            categoryToUpdate.setDescription(categoryRequestDto.description());
        }
        categoryRepo.save(categoryToUpdate);
        Integer postCount = categoryRepo.countPostsWithCategoryId(categoryToUpdate.getId());

        return new CategoryResponseDto(
                categoryToUpdate.getId(),
                categoryToUpdate.getCategoryName(),
                categoryToUpdate.getDescription(),
                postCount == null ? 0 : postCount
        );
    }

    @Override
    public void deleteCategory(int id) {
        this.checkAuthorization();
        Category categoryToDelete = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        categoryRepo.delete(categoryToDelete);
    }

    private void checkAuthorization() {
        // fail-safe, security filter should catch first
        UserProfile userProfile = authService.getUserProfile();
        logger.debug("checkAuthorization: got this far?");
        if (!userProfile.isAdmin()) {
            throw new InsufficientAuthenticationException("User is not Admin");
        }
    }

    private Map<Integer, Integer> mapCategoryIdToPostCount(List<Integer> categoryIds) {
        return categoryRepo.countPostsInCategoryIds(categoryIds)
                .stream().collect(Collectors.toMap(
                        row -> row.get("categoryId", Integer.class),
                        row -> row.get("postCount", Long.class).intValue()
                ));
    }
}
