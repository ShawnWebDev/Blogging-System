package com.webdev.bloggingsystem.blog;

import java.time.Instant;

public record SimplePortfolioEntryDto (
        int id,
        String slug,
        String title,
        String description,
        Instant createdAt,
        String thumbnailUrl,
        String thumbnailAlt,
        String codeUrl,
        String demoUrl
) {
}
