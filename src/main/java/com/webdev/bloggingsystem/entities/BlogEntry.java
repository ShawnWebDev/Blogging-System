package com.webdev.bloggingsystem.entities;

import java.time.Instant;

public class BlogEntry {
    private Integer id;

    private String title;

    private String description;

    private String content;

    private boolean isPublic;

    private Instant createdAt;

    private Instant updatedAt;

    private int authorId;

    public BlogEntry() {}

    public BlogEntry(int authorId, String title, String description, String content, boolean isPublic) {
        this.authorId = authorId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.isPublic = isPublic;
    }

    public static BlogEntry createBlogEntry(
            int authorId, String title, String description, String content, boolean isPublic) {
        BlogEntry blogEntry = new BlogEntry(authorId, title, description, content, isPublic);
        blogEntry.setCreatedAt(Instant.now());
        return blogEntry;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public boolean isPublic() {
        return isPublic;
    }
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    public int getAuthorId() {
        return authorId;
    }
    public void setAuthorId(int author) {
        this.authorId = author;
    }

    @Override
    public String toString() {
        return "BlogEntry{" +
                "id=" + id +
                ", title=" + title +
                ", description=" + description +
                ", content=" + content +
                ", isPublic=" + isPublic +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", authorId=" + authorId +
                '}';
    }
}
