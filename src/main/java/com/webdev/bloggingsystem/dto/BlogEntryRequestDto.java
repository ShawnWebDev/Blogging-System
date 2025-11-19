package com.webdev.bloggingsystem.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record BlogEntryRequestDto(
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,
        @Size(min = 300, max = 65535, message = "Content must be between 300 and 65,535 characters")
        String content,
        @Size(min = 1, max = 4, message = "Post must have between 1 and 4 categories")
        List<String> categories,
        Boolean isPublic
) {}