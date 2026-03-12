package com.webdev.bloggingsystem.blog;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BlogEntryContentBlockDto {
    private BlockType type;
    private String text;
    private List<InlineSpanDto> paragraphSpans = new ArrayList<>();
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
    public List<InlineSpanDto> getParagraphSpans() {
        return paragraphSpans;
    }
    public void setParagraphSpans(List<InlineSpanDto> paragraphSpans) {
        this.paragraphSpans = paragraphSpans;
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
                ", text=" + text +
                ", paragraphSpans=" + paragraphSpans +
                ", url=" + url +
                ", alt=" + alt +
                ", caption=" + caption +
                '}';
    }
}
