package com.webdev.bloggingsystem.blog;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Configuration
public class MarkdownConfig {
    @Bean
    public Parser markdownParser() {
        return Parser.builder().build();
    }

    @Bean
    public HtmlRenderer renderer() {
        return HtmlRenderer.builder()
                .escapeHtml(true)
                .build();
    }
}
