package com.webdev.bloggingsystem.blog;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateBlogEntryDto {

    private String title;

    private String description;

    private String thumbnailUrl;

    private String thumbnailAlt;

    private int[] categoryIds = new int[4];

    private List<BlogEntryContentBlockDto> contentBlocks = new ArrayList<>();

    public CreateBlogEntryDto() {}

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
    public int[] getCategories() {
        return categoryIds;
    }
    public void setCategories(int[] categories) {
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
