package com.webdev.bloggingsystem.errorHandling;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class MaxBytesValidator implements ConstraintValidator<MaxBytes, String> {
    private int maxBytes;
    private String message;

    @Override
    public void initialize(MaxBytes constraintAnnotation) {
        this.maxBytes = constraintAnnotation.value();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        int byteCount = s.getBytes(StandardCharsets.UTF_8).length;

        if (byteCount > this.maxBytes) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    this.message + " " + byteCount + " / " + this.maxBytes + " Bytes Used").addConstraintViolation();

            return false;
        }

        return true;
    }
}
