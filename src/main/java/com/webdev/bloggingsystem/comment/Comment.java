package com.webdev.bloggingsystem.comment;

import com.webdev.bloggingsystem.user.AuthorDto;

import java.time.Instant;

public class Comment {
    private Integer id;

    private String content;

    private Instant createdAt;

    private Instant updatedAt;

    private AuthorDto author;

    private Integer blogEntryId;

    private Integer parentCommentId;

    private Integer replyCount;

    private boolean deleted;

    public Comment() {}

    public Comment(String content, AuthorDto author, Integer blogEntryId) {
        this.content = content.trim();
        this.author = author;
        this.blogEntryId = blogEntryId;
    }

    public Comment(int id, String content, Instant createdAt, Instant updatedAt, Integer blogEntryId, AuthorDto author, Integer replyCount, boolean deleted) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.blogEntryId = blogEntryId;
        this.author = author;
        this.replyCount = replyCount;
        this.deleted = deleted;
    }

    public Comment(int id, String content, Instant createdAt, Instant updatedAt, Integer blogEntryId, Integer parentCommentId, AuthorDto author, Integer replyCount, boolean deleted) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.blogEntryId = blogEntryId;
        this.parentCommentId = parentCommentId;
        this.author = author;
        this.replyCount = replyCount;
        this.deleted = deleted;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String comment) {
        this.content = comment;
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
    public AuthorDto getAuthor() {
        return author;
    }
    public void setAuthor(AuthorDto author) {
        this.author = author;
    }
    public Integer getBlogEntryId() {
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
    public Integer getReplyCount() {
        return replyCount;
    }
    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id= " + id +
                ", content= " + content +
                ", createdAt= " + createdAt +
                ", replyCount=" + replyCount +
                '}';
    }
}
