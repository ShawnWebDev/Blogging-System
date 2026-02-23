package com.webdev.bloggingsystem.blog;

import java.time.Instant;
import java.util.List;

public class BlogEntry {
    private Integer id;

    private String title;

    private String description;

    private String content;

    private Instant createdAt;

    private Instant updatedAt;

    private String slug;

    private String thumbnailUrl;

    private String thumbnailAlt;

    private List<String> categoryNames;

    public BlogEntry() {}

    public BlogEntry(String title, String description, String content, String thumbnailUrl, String thumbnailAlt) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailAlt = thumbnailAlt;
    }

    public BlogEntry(String title, String description, String content, Instant createdAt, Instant updatedAt, String slug, List<String> categoryNames) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.slug = slug;
        this.categoryNames = categoryNames;
    }

    public static BlogEntry createBlogEntry(String title, String description, String content, String thumbnailUrl, String thumbnailAlt) {
        BlogEntry blogEntry = new BlogEntry(title, description, content, thumbnailUrl, thumbnailAlt);
        blogEntry.setCreatedAt(Instant.now());
        blogEntry.setSlug(generateSlugFromTitle(title));
        return blogEntry;
    }

    private static String generateSlugFromTitle(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // replaces everything NOT a-z, 0-9, or space
                .trim() // remove outer whitespace
                .replaceAll("\\s+", "-"); // replace one or more space with a hyphen
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
        this.setSlug(generateSlugFromTitle(title)); // <-- for updating
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
    public String getSlug() {
        return slug;
    }
    public void setSlug(String slug) {
        this.slug = slug;
    }
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public String getThumbnailAlt() {
        return thumbnailAlt;
    }
    public void setThumbnailAlt(String thumbnailAlt) {
        this.thumbnailAlt = thumbnailAlt;
    }
    public List<String> getCategoryNames() {
        return categoryNames;
    }
    public void setCategoryNames(List<String> categoryNames) {
        this.categoryNames = categoryNames;
    }

    @Override
    public String toString() {
        return "BlogEntry{" +
                "id= " + id +
                ", slug= " + slug +
                ", title= " + title +
                ", thumbnailUrl= " + thumbnailUrl +
                ", thumbnailAlt= " + thumbnailAlt +
                ", description= " + description +
                ", content= " + content +
                ", createdAt= " + createdAt +
                ", updatedAt= " + updatedAt +
                ", categoryNames= " + categoryNames +
                '}';
    }
}
