package com.webdev.bloggingsystem.projections;

import java.time.Instant;

public interface BlogEntrySummary {
    Integer getId();
    String getTitle();
    String getContent();
    boolean isPublic();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    String getAuthorUsername();
}
