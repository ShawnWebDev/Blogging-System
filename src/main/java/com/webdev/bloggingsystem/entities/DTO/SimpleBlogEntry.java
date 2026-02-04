package com.webdev.bloggingsystem.entities.DTO;

import java.time.Instant;

public record SimpleBlogEntry(int id, String title, String description, Instant createdAt) {
}
