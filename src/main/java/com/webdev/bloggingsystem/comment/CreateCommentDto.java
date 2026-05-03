package com.webdev.bloggingsystem.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateCommentDto {
    @NotBlank
    @Size(min = 1, max = 500)
    String content;
    @NotNull
    Integer entryId;

    Integer commentId;

    Integer parentCommentId;

    public CreateCommentDto() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getEntryId() {
        return entryId;
    }

    public void setEntryId(Integer entryId) {
        this.entryId = entryId;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Integer parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    @Override
    public String toString() {
        return "CreateCommentDto{" +
                "content= " + content +
                ", entryId= " + entryId +
                ", commentId= " + commentId +
                ", parentCommentId= " + parentCommentId +
                '}';
    }
}
