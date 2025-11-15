package com.webdev.bloggingsystem.entities;

import java.time.Instant;
import java.util.List;

public record BlogEntryResponseDto(
        Integer id,
        String author,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt,
        List<String> categories,
        List<CommentResponseDto> comments
) {
    public BlogEntryResponseDto(BlogEntry entry, List<CommentResponseDto> comments) {
        this(
            entry.getId(),
            entry.getAuthor().getUsername(),
            entry.getTitle(),
            entry.getContent(),
            entry.getCreatedAt(),
            entry.getUpdatedAt(),
            entry.getCategories().stream().map(Category::getCategoryName).toList(),
            comments
        );
    }
}
