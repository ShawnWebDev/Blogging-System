package com.webdev.bloggingsystem.entities.DTO;

import java.time.Instant;
import java.util.List;

public record BlogEntryDto(String title,
                           String description,
                           Instant createdAt,
                           Instant updatedAt,
                           List<String> categories,
                           List<BlogEntryContentBlock> blocks) {
}
