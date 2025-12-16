package com.webdev.bloggingsystem.dto;

import com.webdev.bloggingsystem.exceptions.MaxBytes;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
        @Size(min = 1, message = "Minimum of 1 character")
        @MaxBytes(255)
        String categoryName,
        @Size(min = 1, message = "Minimum of 1 character")
        @MaxBytes(255)
        String description
) {
}
