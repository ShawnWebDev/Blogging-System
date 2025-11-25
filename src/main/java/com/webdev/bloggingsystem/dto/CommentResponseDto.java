package com.webdev.bloggingsystem.dto;

import java.time.Instant;

public record CommentResponseDto(
        int id,
        String comment,
        Instant createdAt,
        Instant updatedAt,
        String author,
        Integer replyCount
) {}
