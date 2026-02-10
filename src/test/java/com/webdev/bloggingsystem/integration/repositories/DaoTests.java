package com.webdev.bloggingsystem.integration.repositories;

import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.Category;
import com.webdev.bloggingsystem.entities.DTO.Author;
import com.webdev.bloggingsystem.entities.DTO.SimpleBlogEntry;
import com.webdev.bloggingsystem.repositories.AppUserDao;
import com.webdev.bloggingsystem.repositories.BlogEntryDao;
import com.webdev.bloggingsystem.repositories.CategoryDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@JdbcTest
@ActiveProfiles("test")
@Testcontainers
@Import({AppUserDao.class, BlogEntryDao.class, CategoryDao.class})
public class DaoTests {

    @Autowired
    private AppUserDao appUserDao;
    @Autowired
    private BlogEntryDao blogEntryDao;
    @Autowired
    private CategoryDao categoryDao;

    @Container
    @ServiceConnection
    static MariaDBContainer mariadbContainer = new MariaDBContainer("mariadb:lts-ubi9");

    @Test
    void getUserByUsername() {
        Optional<AppUser> user = appUserDao.findByUsername("TestAdmin");

        System.out.println(user);
        Assertions.assertTrue(user.isPresent());
        Assertions.assertEquals("TestAdmin", user.get().getUsername());
    }

    @Test
    void getAuthorById() {
        Optional<Author> author = appUserDao.findAuthorById(1);

        System.out.println(author);
        Assertions.assertTrue(author.isPresent());
        Assertions.assertEquals("TestAdmin", author.get().username());
    }

    @Test
    public void testFindById() {
        BlogEntry blogEntry = blogEntryDao.findById(1).orElse(null);
        Assertions.assertNotNull(blogEntry);
        Assertions.assertEquals(1, blogEntry.getId());
        Assertions.assertEquals("Test Post 1", blogEntry.getTitle());

        System.out.println("result: " + blogEntry);
    }

    @Test
    public void testFindByIdNotExist() {
        BlogEntry blogEntry = blogEntryDao.findById(99).orElse(null);
        Assertions.assertNull(blogEntry);

        System.out.println("result: " + blogEntry);
    }

    @Test
    public void testFindAllSimplePaginatedPage1() {
        List<SimpleBlogEntry> result = blogEntryDao.findAllSimple(1, 5);
        System.out.println("result: " + result);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(5, result.size());
    }

    @Test
    public void testFindAllSimplePaginatedPage2() {
        List<SimpleBlogEntry> result = blogEntryDao.findAllSimple(2, 5);
        System.out.println("result: " + result);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAllByCategory() {
        List<SimpleBlogEntry> result = blogEntryDao.findAllSimpleBlogEntriesToCategoryName("Test Category 3", 1, 5);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(3, result.getFirst().categories().size());

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
                ""
        );

        int postId = blogEntryDao.insert(blogEntry);
        BlogEntry blog = blogEntryDao.findById(postId).orElse(null);
        Assertions.assertNotNull(blog);
        Assertions.assertEquals("Test title", blog.getTitle());
        System.out.println("result: " + blog);

        int insertedCategories = categoryDao.batchInsertJoins(Set.of(1, 2, 3), postId);
        Assertions.assertEquals(3, insertedCategories);
    }

    @Test
    @Transactional
    public void insertBlogEntryWithNonExistentCategories() {
        BlogEntry blogEntry = BlogEntry.createBlogEntry(
                1,
                "Test title",
                "Test Description",
                "Test Content",
                ""
        );
        Set<Integer> set = Set.of(10, 20, 30);

        int postId = blogEntryDao.insert(blogEntry);
        Assertions.assertEquals(6, postId);

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> categoryDao.batchInsertJoins(set, postId));
    }

    @Test
    @Transactional
    public void testUpdateBlogEntry() {
        BlogEntry blogEntry = blogEntryDao.findById(1).orElse(null);
        Assertions.assertNotNull(blogEntry);
        System.out.println("slug before title update: " + blogEntry.getSlug());

        blogEntry.setContent("Updated Content Here...");
        blogEntry.setTitle("Updated Title");

        Assertions.assertEquals("updated-title", blogEntry.getSlug());

        Assertions.assertEquals(1, blogEntryDao.update(blogEntry));

        BlogEntry updatedBlogEntry = blogEntryDao.findById(1).orElse(null);
        System.out.println("result: " + updatedBlogEntry);
    }

    @Test
    @Transactional
    public void testUpdateNonExistentBlogEntry() {
        BlogEntry blogEntry = BlogEntry.createBlogEntry(
                1,
                "Fake title",
                "Fake Description",
                "Fake Content",
                ""
        );
        blogEntry.setId(99);
        Assertions.assertEquals(0, blogEntryDao.update(blogEntry));

        BlogEntry updatedBlogEntry = blogEntryDao.findById(99).orElse(null);
        Assertions.assertNull(updatedBlogEntry);
        System.out.println("result: " + updatedBlogEntry);
    }

    @Test
    @Transactional
    public void testDeleteBlogEntry() {
        BlogEntry blogEntry = blogEntryDao.findById(1).orElse(null);
        Assertions.assertNotNull(blogEntry);

        int deleted = blogEntryDao.deleteById(blogEntry.getId());
        Assertions.assertEquals(1, deleted);

        blogEntry = blogEntryDao.findById(1).orElse(null);
        Assertions.assertNull(blogEntry);
    }



}
