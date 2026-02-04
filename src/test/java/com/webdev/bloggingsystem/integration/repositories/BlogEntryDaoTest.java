package com.webdev.bloggingsystem.integration.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.Category;
import com.webdev.bloggingsystem.entities.DTO.SimpleBlogEntry;
import com.webdev.bloggingsystem.repositories.BlogEntryDao;

import com.webdev.bloggingsystem.repositories.CategoryDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


@Import({BlogEntryDao.class, CategoryDao.class})
public class BlogEntryDaoTest extends BaseRepoTest {

    @Autowired
    private BlogEntryDao blogEntryDao;
    @Autowired
    private CategoryDao categoryDao;


    @Test
    public void testFindById() {
        BlogEntry blogEntry = blogEntryDao.findById(1).orElse(null);
        Assertions.assertNotNull(blogEntry);
        Assertions.assertEquals(1, blogEntry.getId());
        Assertions.assertEquals("Test Public Post 1", blogEntry.getTitle());

        System.out.println("result: " + blogEntry);
    }

    @Test
    public void testFindByIdNotExist() {
        BlogEntry blogEntry = blogEntryDao.findById(99).orElse(null);
        Assertions.assertNull(blogEntry);

        System.out.println("result: " + blogEntry);
    }

    @Test
    public void testFindAll() {
        List<BlogEntry> result = blogEntryDao.findAllFull();
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(4, result.size());

        System.out.println("result: " + result);
    }

    @Test
    public void testFindAllSimple() {
        List<SimpleBlogEntry> result = blogEntryDao.findAllSimple();
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(4, result.size());

        System.out.println("result: " + result);
    }

    @Test
    @Transactional
    public void insertBlogEntryWithCategories() {
        BlogEntry blogEntry = BlogEntry.createBlogEntry(
            1,
            "Test title",
            "Test Description",
            "Test Content",
            true
        );
        Set<Integer> set = Set.of(1, 2, 3);

        int postId = blogEntryDao.insert(blogEntry);
        BlogEntry blog = blogEntryDao.findById(postId).orElse(null);
        Assertions.assertNotNull(blog);
        Assertions.assertEquals("Test title", blog.getTitle());
        System.out.println("result: " + blog);

        int insertedCategories = categoryDao.batchInsertJoins(set, postId);
        Assertions.assertEquals(3, insertedCategories);

        List<Category> entryCategories = categoryDao.findAllCategoriesToBlogId(postId);
        Assertions.assertEquals(3, entryCategories.size());
        System.out.println("found categories: " + entryCategories);
    }

    @Test
    @Transactional
    public void insertBlogEntryWithNonExistentCategories() {
        BlogEntry blogEntry = BlogEntry.createBlogEntry(
                1,
                "Test title",
                "Test Description",
                "Test Content",
                true
        );
        Set<Integer> set = Set.of(10, 20, 30);

        int postId = blogEntryDao.insert(blogEntry);
        Assertions.assertEquals(5, postId);

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> categoryDao.batchInsertJoins(set, postId));
    }




}
