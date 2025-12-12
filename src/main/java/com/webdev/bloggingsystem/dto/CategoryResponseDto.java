package com.webdev.bloggingsystem.dto;

public record CategoryResponseDto(
        int id,
        String categoryName,
        String description,
        int postCount) {
}
