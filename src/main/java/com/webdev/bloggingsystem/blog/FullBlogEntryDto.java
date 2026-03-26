package com.webdev.bloggingsystem.blog;

import java.time.Instant;
import java.util.List;

public record FullBlogEntryDto(int id,
                                String title,
                                String slug,
                                String description,
                                String thumbnailUrl,
                                String thumbnailAlt,
                                Instant createdAt,
                                Instant updatedAt,
                                List<String> categories,
                                String contentHtml) {
}
