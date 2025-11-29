package com.webdev.bloggingsystem.dto;

import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.Category;

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
        int totalComments,
        boolean isPublic
) {
    public BlogEntryResponseDto(BlogEntry entry, int commentCount) {
        this(
            entry.getId(),
            entry.getAuthor().getUsername(),
            entry.getTitle(),
            entry.getContent(),
            entry.getCreatedAt(),
            entry.getUpdatedAt(),
            entry.getCategories().stream().map(Category::getCategoryName).toList(),
            commentCount,
            entry.isPublic()
        );
    }
}
