package com.webdev.bloggingsystem.errorHandling;

import com.webdev.bloggingsystem.user.AppUserDao;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {
    private final AppUserDao appUserDao;

    public UniqueUsernameValidator(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !appUserDao.existsByUsername(value);
    }
}
