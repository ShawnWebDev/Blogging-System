package com.webdev.bloggingsystem.dto;

import java.time.Instant;

public record CommentResponseDto(
        int id,
        String comment,
        Instant createdAt,
        String author,
        Integer replyCount
) {}
