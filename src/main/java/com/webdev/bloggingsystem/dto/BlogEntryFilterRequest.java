package com.webdev.bloggingsystem.dto;

public record BlogEntryFilterRequest(
        String categoryName,
        String afterDate,
        String beforeDate
){}