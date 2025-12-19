package com.webdev.bloggingsystem.dto;

import java.time.Instant;
import java.util.List;

public record BlogEntryResponseDto(
        int id,
        String author,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt,
        List<String> categories,
        int totalComments,
        boolean isPublic
) {}
