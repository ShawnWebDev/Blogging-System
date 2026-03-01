package com.webdev.bloggingsystem.blog;

import java.time.Instant;
import java.util.List;

public record FullBlogEntryDto(int id,
                                String title,
                                String slug,
                                String description,
                                Instant createdAt,
                                Instant updatedAt,
                                List<String> categories,
                                List<BlogEntryContentBlockDto> blocks) {

    public static FullBlogEntryDto create(BlogEntry entry, List<BlogEntryContentBlockDto> blocks) {
        return new FullBlogEntryDto(
                entry.getId(), entry.getTitle(), entry.getSlug(), entry.getDescription(), entry.getCreatedAt(), entry.getUpdatedAt(),
                entry.getCategoryNames(), blocks);
    }
}
