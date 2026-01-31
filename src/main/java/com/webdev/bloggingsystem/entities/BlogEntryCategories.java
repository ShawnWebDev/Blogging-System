package com.webdev.bloggingsystem.entities;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("posts_categories")
public record BlogEntryCategories(@Column("category_id") Integer categoryId) {
}