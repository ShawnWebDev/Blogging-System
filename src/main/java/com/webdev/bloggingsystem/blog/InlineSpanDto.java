package com.webdev.bloggingsystem.blog;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class InlineSpanDto {
    private SpanType spanType;
    private String spanText;
    private String spanUrl;

    public InlineSpanDto() {}

    public SpanType getSpanType() {
        return spanType;
    }

    public void setSpanType(SpanType spanType) {
        this.spanType = spanType;
    }
    public String getSpanText() {
        return spanText;
    }
    public void setSpanText(String spanText) {
        this.spanText = spanText;
    }
    public String getSpanUrl() {
        return spanUrl;
    }
    public void setSpanUrl(String spanUrl) {
        this.spanUrl = spanUrl;
    }

    @Override
    public String toString() {
        return "InlineSpanDto{" +
                "spanType=" + spanType +
                ", spanText=" + spanText +
                ", spanUrl=" + spanUrl +
                '}';
    }
}
