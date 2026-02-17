package com.webdev.bloggingsystem.blog;

public class BlogEntryContentBlockDto {
    private BlockType type;
    private String text;
    private String url;
    private String alt;
    private String caption;

    public BlogEntryContentBlockDto() {}

    public BlockType getType() {
        return type;
    }
    public void setType(BlockType type) {
        this.type = type;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getAlt() {
        return alt;
    }
    public void setAlt(String alt) {
        this.alt = alt;
    }
    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return "BlogEntryContentBlockDto{" +
                "type=" + type +
                ", text= " + text +
                ", url= " + url +
                ", alt= " + alt +
                ", caption= " + caption +
                '}';
    }
}
