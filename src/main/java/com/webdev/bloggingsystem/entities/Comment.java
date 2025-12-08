package com.webdev.bloggingsystem.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "comments")
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "comment-with-author",
                attributeNodes = {
                        @NamedAttributeNode("author")
                }
        ),
        @NamedEntityGraph(
                name = "comment-with-author-and-blogEntry",
                attributeNodes = {
                        @NamedAttributeNode("author"),
                        @NamedAttributeNode(value = "blogEntry", subgraph = "entry-author")
                },
                subgraphs = {
                        @NamedSubgraph(
                                name = "entry-author",
                                attributeNodes = {
                                        @NamedAttributeNode("author")
                                }
                        )
                }
        )
})
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

    public AppUser getAuthor() {
        return author;
    }

    public void setAuthor(AppUser author) {
        this.author = author;
    }

    public BlogEntry getBlogEntry() {
        return blogEntry;
    }

    public void setBlogEntry(BlogEntry blogEntry) {
        this.blogEntry = blogEntry;
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

    // compares only id fields, returns false for entities not persisted - where this.id == null
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass() || this.getId() == null) return false;
        Comment comment = (Comment) o;
        return Objects.equals(this.getId(), comment.getId());
    }

    // sets hashcode for entities not persisted to 31 as temporary fallback
    @Override
    public int hashCode() {
        if (this.getId() == null) return 31;
        return this.getId().hashCode();
    }
}
