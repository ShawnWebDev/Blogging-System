package com.webdev.bloggingsystem.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Table("blog_entries")
public class BlogEntry {
    @Id
    private Integer id;

    private String title;

    private String content;

    private boolean isPublic;

    private Instant createdAt;

    private Instant updatedAt;

    private int authorId;

    @MappedCollection(idColumn = "post_id")
    private Set<BlogEntryCategories> categoryIds;

    public BlogEntry() {}

    public BlogEntry(Integer authorId, String title, String content, boolean isPublic) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.isPublic = isPublic;
        categoryIds = new HashSet<>();
        createdAt = Instant.now();
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
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    public int getAuthorId() {
        return authorId;
    }
    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }
    public Set<BlogEntryCategories> getCategoryIds() {
        return categoryIds;
    }
    public void setCategoryIds(Set<BlogEntryCategories> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public void addCategory(Category category) {
        this.categoryIds.add(new BlogEntryCategories(category.getId()));
    }

    public void removeCategory(Category category) {
        if (this.categoryIds != null) {
            this.categoryIds.remove(new BlogEntryCategories(category.getId()));
        }
    }

    @Override
    public String toString() {
        return "BlogEntry{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", isPublic=" + isPublic +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
