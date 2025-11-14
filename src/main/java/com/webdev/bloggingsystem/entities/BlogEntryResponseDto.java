package com.webdev.bloggingsystem.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        Set<Category> categorySet = entry.getCategories();
        // only fetch comments if specified to maintain lazy load
        List<String> categories = new ArrayList<>(categorySet.size());

        for (Category curr : categorySet) {
            categories.add(curr.getCategoryName());
        }

        // "this" default constructor
        this(
            entry.getId(),
            entry.getAuthor().getUsername(),
            entry.getTitle(),
            entry.getContent(),
            entry.getCreatedAt(),
            entry.getUpdatedAt(),
            categories,
            comments
        );
    }
}
