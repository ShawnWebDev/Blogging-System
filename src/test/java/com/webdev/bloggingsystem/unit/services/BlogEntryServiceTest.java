package com.webdev.bloggingsystem.unit.services;

import com.webdev.bloggingsystem.blog.BlogEntryDao;
import com.webdev.bloggingsystem.blog.CategoryDao;
import com.webdev.bloggingsystem.blog.BlogEntryService;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BlogEntryServiceTest {
    @Mock
    BlogEntryDao blogEntryDao;
    @Mock
    CategoryDao categoryDao;
    @Mock
    ObjectMapper mapper;

    @InjectMocks
    BlogEntryService blogEntryService;

    //todo : create unit tests for BlogEntryService methods.



}
