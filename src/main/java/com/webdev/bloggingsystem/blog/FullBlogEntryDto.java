package com.webdev.bloggingsystem.blog;

import java.time.Instant;
import java.util.List;

public record FullBlogEntryDto(String title,
                               String slug,
                               String description,
                               Instant createdAt,
                               Instant updatedAt,
                               List<String> categories,
                               List<BlogEntryContentBlockDto> blocks) {
}
