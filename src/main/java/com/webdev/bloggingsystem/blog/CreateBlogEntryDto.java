package com.webdev.bloggingsystem.blog;


import com.webdev.bloggingsystem.errorHandling.MaxBytes;
import com.webdev.bloggingsystem.errorHandling.UniqueTitle;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private int[] categoryIds = new int[4];

    private List<BlogEntryContentBlockDto> contentBlocks = new ArrayList<>();

    public CreateBlogEntryDto() {}

    public static CreateBlogEntryDto create(BlogEntry blogEntry, int[] categoryIds,  List<BlogEntryContentBlockDto> contentBlocks) {
        CreateBlogEntryDto dto = new CreateBlogEntryDto();
        dto.id = blogEntry.getId() != null ? blogEntry.getId() : null;
        dto.title = blogEntry.getTitle();
        dto.description = blogEntry.getDescription();
        dto.thumbnailUrl = blogEntry.getThumbnailUrl();
        dto.thumbnailAlt = blogEntry.getThumbnailAlt();
        dto.categoryIds = categoryIds;
        dto.contentBlocks = contentBlocks;
        return dto;
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
    public List<BlogEntryContentBlockDto> getContentBlocks() {
        return contentBlocks;
    }
    public void setContentBlocks(List<BlogEntryContentBlockDto> contentBlocks) {
        this.contentBlocks = contentBlocks;
    }

    @Override
    public String toString() {
        return "CreateBlogEntryDto{" +
                "title= " + title +
                ", description= " + description +
                ", thumbnailUrl= " + thumbnailUrl +
                ", thumbnailAlt= " + thumbnailAlt +
                ", categories= " + Arrays.toString(categoryIds) +
                ", contentBlocks= " + contentBlocks +
                '}';
    }
}
