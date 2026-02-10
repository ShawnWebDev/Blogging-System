package com.webdev.bloggingsystem.entities.DTO;

import java.time.Instant;
import java.util.List;

public record SimpleBlogEntry(
        int id, String title, String description, Instant createdAt, List<String> categories, String thumbnailUrl) {
}
