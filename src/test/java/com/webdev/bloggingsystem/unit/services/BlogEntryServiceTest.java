package com.webdev.bloggingsystem.unit.services;

import com.webdev.bloggingsystem.blog.BlogEntryDao;
import com.webdev.bloggingsystem.blog.CategoryDao;
import com.webdev.bloggingsystem.blog.BlogEntryService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BlogEntryServiceTest {
    @Mock
    BlogEntryDao blogEntryDao;
    @Mock
    CategoryDao categoryDao;

    @InjectMocks
    BlogEntryService blogEntryService;


}
