package com.webdev.bloggingsystem.entities;

import java.time.Instant;

public record CommentResponseDto(
        int id,
        String comment,
        Instant createdAt,
        String author,
        Integer replyCount
) {}
