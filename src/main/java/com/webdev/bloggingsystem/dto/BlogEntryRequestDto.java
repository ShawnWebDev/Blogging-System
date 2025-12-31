package com.webdev.bloggingsystem.dto;

import com.webdev.bloggingsystem.exceptions.MaxBytes;
import com.webdev.bloggingsystem.exceptions.UniqueTitle;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BlogEntryRequestDto(
        @Size(min = 4, message = "Minimum of 4 characters")
        @MaxBytes(255)
        @UniqueTitle
        String title,
        @Size(min = 300, message = "Minimum of 300 characters")
        @MaxBytes(value = 65535)
        String content,
        @Size(min = 1, max = 4, message = "Must have between 1 and 4 categories")
        List<String> categories,
        Boolean isPublic
) {}