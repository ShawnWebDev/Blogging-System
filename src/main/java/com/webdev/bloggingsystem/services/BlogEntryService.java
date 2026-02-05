package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.repositories.BlogEntryDao;
import com.webdev.bloggingsystem.repositories.CategoryDao;
import org.springframework.stereotype.Service;

@Service
public class BlogEntryService {
    private final BlogEntryDao blogEntryDao;
    private final CategoryDao categoryDao;

    public BlogEntryService(BlogEntryDao blogEntryDao, CategoryDao categoryDao) {
        this.blogEntryDao = blogEntryDao;
        this.categoryDao = categoryDao;
    }


}
