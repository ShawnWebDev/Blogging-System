package com.webdev.bloggingsystem.blog;

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
    BlogService blogEntryService;

    //todo : create unit tests for BlogEntryService methods.










}
