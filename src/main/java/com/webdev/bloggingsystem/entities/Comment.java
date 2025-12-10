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

    @Column(name = "author_id", nullable = false, insertable = false, updatable = false)
    private Integer authorId;

    @Column(name = "post_id", nullable = false, insertable = false, updatable = false)
    private Integer blogEntryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BlogEntry blogEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    public Comment() {}
    public Comment(String comment, AppUser author, BlogEntry blogEntry) {
        this.comment = comment;
        this.author = author;
        this.blogEntry = blogEntry;
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
    public int getBlogEntryId() {
        return blogEntryId;
    }
    public BlogEntry getBlogEntry() {
        return blogEntry;
    }
    public Comment getParentComment() {
        return parentComment;
    }
    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
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
