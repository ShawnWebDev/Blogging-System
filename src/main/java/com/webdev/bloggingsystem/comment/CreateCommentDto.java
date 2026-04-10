package com.webdev.bloggingsystem.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentDto {
    @NotBlank
    @Size(min = 1, max = 500)
    String content;

    Integer entryId;

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
                ", parentCommentId= " + parentCommentId +
                '}';
    }
}
