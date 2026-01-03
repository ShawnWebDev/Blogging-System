package com.webdev.bloggingsystem.exceptions;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MaxBytesValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxBytes {

    int value();
    String message() default "Input length exceeded";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
