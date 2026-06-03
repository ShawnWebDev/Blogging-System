package com.webdev.bloggingsystem.errorHandling;

import com.webdev.bloggingsystem.user.AppUserDao;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    private final AppUserDao appUserDao;

    public UniqueEmailValidator(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !appUserDao.existsByEmail(value);
    }
}
