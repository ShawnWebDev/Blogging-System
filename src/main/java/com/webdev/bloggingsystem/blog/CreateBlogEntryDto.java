package com.webdev.bloggingsystem.blog;

import com.webdev.bloggingsystem.errorHandling.MaxBytes;
import com.webdev.bloggingsystem.errorHandling.UniqueTitle;
import jakarta.validation.constraints.NotBlank;

import java.util.Arrays;

@UniqueTitle
public class CreateBlogEntryDto {
    private Integer id;
    @NotBlank
    @MaxBytes(value = 255)
    private String title;
    @NotBlank
    @MaxBytes(value = 500)
    private String description;
    @NotBlank
    @MaxBytes(value = 255)
    private String thumbnailUrl;
    @NotBlank
    @MaxBytes(value = 255)
    private String thumbnailAlt;
    @NotBlank
    @MaxBytes(value = 65535)
    private String content;

    private int[] categoryIds = new int[4];

    public CreateBlogEntryDto() {}

    public CreateBlogEntryDto(Integer id, String title, String description, String thumbnailUrl, String thumbnailAlt, int[] categoryIds, String content) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailAlt = thumbnailAlt;
        this.categoryIds = categoryIds;
        this.content = content;
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
    public int[] getCategoryIds() {
        return categoryIds;
    }
    public void setCategoryIds(int[] categories) {
        this.categoryIds = categories;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "CreateBlogEntryDto{" +
                "title= " + title +
                ", description= " + description +
                ", thumbnailUrl= " + thumbnailUrl +
                ", thumbnailAlt= " + thumbnailAlt +
                ", categories= " + Arrays.toString(categoryIds) +
                ", content " + content +
                '}';
    }
}
