package com.webdev.bloggingsystem.errorHandling;

import com.webdev.bloggingsystem.blog.BlogEntryDao;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueTitleValidator implements ConstraintValidator<UniqueTitle, String> {
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
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (blogEntryDao.existsByTitle(s)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(this.message)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
