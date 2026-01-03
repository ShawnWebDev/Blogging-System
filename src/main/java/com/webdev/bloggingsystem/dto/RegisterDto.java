package com.webdev.bloggingsystem.dto;

import com.webdev.bloggingsystem.exceptions.MaxBytes;
import com.webdev.bloggingsystem.exceptions.UniqueUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record RegisterDto(
        @UniqueUsername
        @Pattern(regexp = "^(?=.+\\d)(?=.+[a-z])(?=.+[A-Z])(?!.+\\s).{6,}$",
                message = "Minimum requirements not met! Must have at least 6 characters with no spaces, 1 digit, 1 uppercase, and 1 lowercase")
        @MaxBytes(value = 32)
        String username,
        @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[.!?@#$%^&*+_-])(?!.+\\s).{8,}$",
                message = "Minimum requirements not met! Must have at least 8 characters with no spaces, 1 digit, 1 uppercase, 1 lowercase, and 1 symbol .!?@#$%^&*+_-")
        @MaxBytes(value = 72)
        String password,
        @Email
        @MaxBytes(value = 255)
        String email
) { }