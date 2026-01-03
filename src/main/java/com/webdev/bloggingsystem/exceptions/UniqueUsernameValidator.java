package com.webdev.bloggingsystem.exceptions;

import com.webdev.bloggingsystem.repositories.AppUserRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {
    private final AppUserRepo appUserRepo;
    private String message;

    public UniqueUsernameValidator(AppUserRepo appUserRepo) {
        this.appUserRepo = appUserRepo;
    }

    @Override
    public void initialize(UniqueUsername constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (appUserRepo.existsByUsername(s)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(this.message)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
