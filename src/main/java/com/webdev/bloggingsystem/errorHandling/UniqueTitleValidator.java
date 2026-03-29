package com.webdev.bloggingsystem.errorHandling;

import com.webdev.bloggingsystem.blog.BlogEntryDao;
import com.webdev.bloggingsystem.blog.CreateBlogEntryDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueTitleValidator implements ConstraintValidator<UniqueTitle, CreateBlogEntryDto> {
    private final BlogEntryDao blogEntryDao;

    public UniqueTitleValidator(BlogEntryDao blogEntryDao) {
        this.blogEntryDao = blogEntryDao;
    }

    @Override
    public boolean isValid(CreateBlogEntryDto dto, ConstraintValidatorContext context) {
        if (blogEntryDao.existsByTitleAndNotId(dto.getTitle(), dto.getId())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("title")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
