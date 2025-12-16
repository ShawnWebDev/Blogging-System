package com.webdev.bloggingsystem.dto;

import com.webdev.bloggingsystem.exceptions.MaxBytes;
import jakarta.validation.constraints.NotBlank;

public record CommentRequestDto(
        @MaxBytes(4000)
        @NotBlank(message = "Input empty")
        String comment
) {
}
