package com.webdev.bloggingsystem.errorHandling;

import com.webdev.bloggingsystem.blog.Category;
import com.webdev.bloggingsystem.blog.CategoryDao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueCategoryValidator implements ConstraintValidator<UniqueCategoryName, Category> {
    private final CategoryDao categoryDao;

    public UniqueCategoryValidator(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public boolean isValid(Category category, ConstraintValidatorContext context) {
        if (categoryDao.existsByNameAndNotId(category.getCategoryName(), category.getId())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("categoryName")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
