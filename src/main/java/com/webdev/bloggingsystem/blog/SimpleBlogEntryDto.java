package com.webdev.bloggingsystem.blog;

import java.time.Instant;
import java.util.List;

public record SimpleBlogEntryDto(
        int id,
        String slug,
        String title,
        String description,
        Instant createdAt,
        String thumbnailUrl,
        String thumbnailAlt) {
}
