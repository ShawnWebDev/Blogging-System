package com.webdev.bloggingsystem.integration.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.repositories.BlogEntryDao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Set;


@Import(BlogEntryDao.class)
public class BlogEntryDaoTest extends BaseRepoTest {

    @Autowired
    private BlogEntryDao blogEntryDao;

    @Test
    public void insertBlogEntry() {
        BlogEntry blogEntry = new BlogEntry(
            1,
            "Test title",
            "Test Content",
            true
        );
        Set<Integer> set = Set.of(1, 2, 3);

        int postId = blogEntryDao.insertBlogEntry(blogEntry, set);
    }


}
