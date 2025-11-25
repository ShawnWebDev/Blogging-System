package com.webdev.bloggingsystem.dto;

import com.webdev.bloggingsystem.exceptions.MaxBytes;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BlogEntryRequestDto(
        @Size(min = 4, message = "Minimum of 4 characters")
        @MaxBytes(255)
        String title,
        @Size(min = 1000, message = "Minimum of 1000 characters")
        @MaxBytes(value = 65535)
        String content,
        @Size(min = 1, max = 4, message = "Must have between 1 and 4 categories")
        List<String> categories,
        Boolean isPublic
) {}