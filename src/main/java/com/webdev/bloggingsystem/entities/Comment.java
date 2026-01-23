package com.webdev.bloggingsystem.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "author_id", nullable = false)
    private Integer authorId;

    @Column(name = "post_id", nullable = false)
    private Integer blogEntryId;

    @Column(name = "parent_comment_id")
    private Integer parentCommentId;


    public Comment() {}

    public Comment(String comment, Integer authorId, Integer blogEntryId) {
        this.comment = comment;
        this.authorId = authorId;
        this.blogEntryId = blogEntryId;
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
