package com.webdev.bloggingsystem.blog;

public record BlogEntryContentBlockDto(BlockType type, String text, String url, String alt, String caption) {
}
