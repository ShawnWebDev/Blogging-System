package com.webdev.bloggingsystem.exceptions;

import com.webdev.bloggingsystem.repositories.BlogEntryRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueTitleValidator implements ConstraintValidator<UniqueTitle, String> {
    private String message;
    private final BlogEntryRepo blogEntryRepo;

    public UniqueTitleValidator(BlogEntryRepo blogEntryRepo) {
        this.blogEntryRepo = blogEntryRepo;
    }

    @Override
    public void initialize(UniqueTitle constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (blogEntryRepo.existsByTitle(s)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(this.message)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
