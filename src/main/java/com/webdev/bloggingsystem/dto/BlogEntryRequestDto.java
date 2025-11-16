package com.webdev.bloggingsystem.dto;

import java.util.List;

public record BlogEntryRequestDto(
        String title,
        String content,
        List<String> categories,
        Boolean isPublic
) {}