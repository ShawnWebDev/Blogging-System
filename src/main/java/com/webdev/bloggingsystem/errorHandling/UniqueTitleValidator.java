package com.webdev.bloggingsystem.errorHandling;

import com.webdev.bloggingsystem.blog.BlogEntryDao;
import com.webdev.bloggingsystem.blog.CreateBlogEntryDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueTitleValidator implements ConstraintValidator<UniqueTitle, CreateBlogEntryDto> {
    private Integer id;
    private String message;
    private final BlogEntryDao blogEntryDao;

    public UniqueTitleValidator(BlogEntryDao blogEntryDao) {
        this.blogEntryDao = blogEntryDao;
    }

    @Override
    public void initialize(UniqueTitle constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(CreateBlogEntryDto dto, ConstraintValidatorContext constraintValidatorContext) {
        if (blogEntryDao.existsByTitleAndNotId(dto.getTitle(), dto.getId())) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(this.message)
                    .addPropertyNode("title")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
