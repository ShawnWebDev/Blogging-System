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
    public BlogEntryResponseDto(BlogEntry entry, boolean getComments) {
        Set<Category> categorySet = entry.getCategories();
        // only fetch comments if specified to maintain lazy load
        Set<Comment> commentSet;
        List<String> categories = new ArrayList<>(categorySet.size());
        List<CommentResponseDto> comments = null;

        if (getComments) {
            commentSet = entry.getComments();
            comments = new ArrayList<>(commentSet.size());

            for (Comment curr : commentSet) {
                comments.add(new CommentResponseDto(
                        curr.getId(),
                        curr.getComment(),
                        curr.getCreatedAt(),
                        curr.getAuthor().getUsername()
                ));
            }
        }

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
