package com.webdev.bloggingsystem.entities.DTO;

import com.webdev.bloggingsystem.entities.BlockType;

public record BlogEntryContentBlock(BlockType type, String text, String url, String alt, String caption) {
}
