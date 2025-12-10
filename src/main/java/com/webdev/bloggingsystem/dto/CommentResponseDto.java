package com.webdev.bloggingsystem.dto;

import java.time.Instant;

public record CommentResponseDto(
        int id,
        int blogEntryId,
        Integer parentId,
        String comment,
        Instant createdAt,
        Instant updatedAt,
        String author,
        int replyCount
) {}
