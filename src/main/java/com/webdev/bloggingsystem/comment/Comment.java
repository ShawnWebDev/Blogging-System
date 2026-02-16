package com.webdev.bloggingsystem.comment;

import java.time.Instant;

public class Comment {
    private Integer id;

    private String comment;

    private Instant createdAt;

    private Instant updatedAt;

    private int authorId;

    private int blogEntryId;

    private Integer parentCommentId;


    public Comment() {}

    public Comment(String comment, Integer authorId, Integer blogEntryId) {
        this.comment = comment;
        this.authorId = authorId;
        this.blogEntryId = blogEntryId;
        createdAt = Instant.now();
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
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
    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }
    public int getBlogEntryId() {
        return blogEntryId;
    }
    public void setBlogEntryId(Integer blogEntryId) {
        this.blogEntryId = blogEntryId;
    }
    public Integer getParentCommentId() {
        return parentCommentId;
    }
    public void setParentCommentId(Integer parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
