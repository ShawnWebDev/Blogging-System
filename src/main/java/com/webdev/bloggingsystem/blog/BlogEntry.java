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

    private boolean inProgress;

    //used in portfolio entry
    private String codeUrl;

    private String demoUrl;

    private boolean isPortfolio;

    private boolean hasArticle;

    public BlogEntry() {}

    public BlogEntry(String title, String description, String content, String thumbnailUrl, String thumbnailAlt) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailAlt = thumbnailAlt;
    }
    // used in DAO for result set in - fullBlogEntryExtractor()
    public BlogEntry(int id, String title, String description, String content, Instant createdAt, Instant updatedAt,
                     String slug, List<String> categoryNames, String thumbnailUrl, String thumbnailAlt, boolean inProgress,
                     String codeUrl, String demoUrl, boolean isPortfolio, boolean hasArticle) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.slug = slug;
        this.categoryNames = categoryNames;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailAlt = thumbnailAlt;
        this.inProgress = inProgress;
        this.codeUrl = codeUrl;
        this.demoUrl = demoUrl;
        this.isPortfolio = isPortfolio;
        this.hasArticle = hasArticle;
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
    public void setSlug(String title) {
        this.slug = generateSlugFromTitle(title);
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
    public boolean getInProgress() {
        return inProgress;
    }
    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
    public String getCodeUrl() {
        return codeUrl;
    }
    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }
    public String getDemoUrl() {
        return demoUrl;
    }
    public void setDemoUrl(String demoUrl) {
        this.demoUrl = demoUrl;
    }
    public boolean isPortfolio() {
        return isPortfolio;
    }
    public void setPortfolio(boolean portfolio) {
        isPortfolio = portfolio;
    }
    public boolean getHasArticle() {
        return hasArticle;
    }
    public void setHasArticle(boolean hasArticle) {
        this.hasArticle = hasArticle;
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
                ", inProgress= " + inProgress +
                ", isPortfolio= " + isPortfolio +
                '}';
    }
}
