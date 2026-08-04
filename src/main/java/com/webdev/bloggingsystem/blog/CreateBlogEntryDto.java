package com.webdev.bloggingsystem.blog;

import com.webdev.bloggingsystem.errorHandling.UniqueTitle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Arrays;

@UniqueTitle
public class CreateBlogEntryDto {
    private Integer id;
    private String slug;
    @NotBlank
    @Size(min = 2, max = 255)
    private String title;
    @NotBlank
    @Size(min = 2, max = 500)
    private String description;
    @Size(max = 255)
    private String thumbnailUrl;
    @Size(max = 255)
    private String thumbnailAlt;
    @Size(max = 65535)
    private String content;
    @Size(max = 255)
    private String codeUrl;
    @Size(max = 255)
    private String demoUrl;
    @Size(max = 255)
    private String articleUrl;

    private boolean inProgress;
    private boolean isPortfolio;

    private int[] categoryIds = new int[4];


    public CreateBlogEntryDto() {}

    public CreateBlogEntryDto(Integer id, String slug, String title, String description, String thumbnailUrl, String thumbnailAlt, int[] categoryIds, String content,
                              boolean inProgress, String codeUrl, String demoUrl, String articleUrl, boolean isPortfolio) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailAlt = thumbnailAlt;
        this.categoryIds = categoryIds;
        this.content = content;
        this.inProgress = inProgress;
        this.codeUrl = codeUrl;
        this.demoUrl = demoUrl;
        this.articleUrl = articleUrl;
        this.isPortfolio = isPortfolio;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getSlug() {
        return slug;
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
    public boolean getInProgress() {
        return inProgress;
    }
    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
    public boolean getIsPortfolio() {
        return isPortfolio;
    }
    public void setIsPortfolio(boolean isPortfolio) {
        this.isPortfolio = isPortfolio;
    }
    public String getCodeUrl() {
        return codeUrl;
    }
    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }
    public String getDemoUrl() {
        return demoUrl;
    }
    public void setDemoUrl(String demoUrl) {
        this.demoUrl = demoUrl;
    }
    public String getArticleUrl() {
        return articleUrl;
    }
    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
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
                ", inProgress= " + inProgress +
                ", isPortfolio= " + isPortfolio +
                '}';
    }
}
