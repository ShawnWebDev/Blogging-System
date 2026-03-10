package com.webdev.bloggingsystem.integration.repositories;

import com.webdev.bloggingsystem.blog.*;
import com.webdev.bloggingsystem.user.AppUser;
import com.webdev.bloggingsystem.user.AuthorDto;
import com.webdev.bloggingsystem.user.AppUserDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
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
    @Autowired
    private JdbcClient jdbc;

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
        Optional<AuthorDto> author = appUserDao.findAuthorById(1);

        System.out.println(author);
        Assertions.assertTrue(author.isPresent());
        Assertions.assertEquals("TestAdmin", author.get().username());
    }

    @Test
    public void testFindById() {
        BlogEntry blogEntry = blogEntryDao.findById(1).orElse(null);
        Assertions.assertNotNull(blogEntry);
        Assertions.assertEquals("Test Post 1", blogEntry.getTitle());
        Assertions.assertEquals(List.of("Test Category 1", "Test Category 2"), blogEntry.getCategoryNames());

        System.out.println("result: " + blogEntry);
    }

    @Test
    public void testFindBySlug() {
        BlogEntry blogEntry = blogEntryDao.findBySlug("test-post-1").orElse(null);
        Assertions.assertNotNull(blogEntry);
        Assertions.assertEquals("Test Post 1", blogEntry.getTitle());
        Assertions.assertEquals(List.of("Test Category 1", "Test Category 2"), blogEntry.getCategoryNames());

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
        List<SimpleBlogEntryDto> result = blogEntryDao.findAllSimple(1, 5);
        System.out.println("result: " + result);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(5, result.size());
    }

    @Test
    public void testFindAllSimplePaginatedPage2() {
        List<SimpleBlogEntryDto> result = blogEntryDao.findAllSimple(2, 5);
        System.out.println("result: " + result);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAllByCategory() {
        List<SimpleBlogEntryDto> result = blogEntryDao.findAllSimpleBlogEntriesToCategoryName("Test Category 3", 1, 5);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(3, result.getFirst().categories().size());

        System.out.println("result: " + result);
    }

    @Test
    public void testFindCategoryIdsInNames() {
        List<String> categoryNames = List.of("Test Category 1", "Test Category 2");
        List<Integer> categoryIds = categoryDao.findAllIdsInNames(categoryNames);
        Assertions.assertEquals(categoryIds.size(), categoryNames.size());
        Assertions.assertEquals(List.of(1, 2), categoryIds);

        System.out.println("result: " + categoryIds);
    }


    @Test
    @Transactional
    public void insertBlogEntryWithCategories() {
        BlogEntry blogEntry = new BlogEntry(
            "Test title",
            "Test Description",
            "Test Content",
                "url",
                "alt"
        );
        blogEntry.setSlug(blogEntry.getTitle());
        int postId = blogEntryDao.insert(blogEntry);
        System.out.println("postId: " + postId);
        int insertedCategories = categoryDao.batchInsertJoins(Set.of(1, 2, 3), postId);
        Assertions.assertEquals(3, insertedCategories);

        BlogEntry blog = blogEntryDao.findById(postId).orElse(null);
        Assertions.assertNotNull(blog);
        Assertions.assertEquals("Test title", blog.getTitle());
        System.out.println("result: " + blog);


    }

    @Test
    @Transactional
    public void insertBlogEntryWithNonExistentCategories() {
        BlogEntry blogEntry = new BlogEntry(
                "Test title",
                "Test Description",
                "Test Content",
                "url",
                "alt"
        );
        blogEntry.setSlug(blogEntry.getTitle());
        Set<Integer> set = Set.of(10, 20, 30);

        int postId = blogEntryDao.insert(blogEntry);
        Assertions.assertEquals(6, postId);

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> categoryDao.batchInsertJoins(set, postId));
    }

    @Test
    @Transactional
    public void testUpdateBlogEntry() {
        BlogEntry blogEntry = blogEntryDao.findById(1).orElse(null);
        System.out.println("result: " + blogEntry);
        Assertions.assertNotNull(blogEntry);
        System.out.println("slug before title update: " + blogEntry.getSlug());

        blogEntry.setContent("Updated Content Here...");
        blogEntry.setTitle("Updated Title");

        Assertions.assertEquals("updated-title", blogEntry.getSlug());
        // 1 record updated..
        Assertions.assertEquals(1, blogEntryDao.update(blogEntry));

        BlogEntry updatedBlogEntry = blogEntryDao.findById(1).orElse(null);
        System.out.println("result: " + updatedBlogEntry);
    }

    @Test
    @Transactional
    public void testUpdateNonExistentBlogEntry() {
        BlogEntry blogEntry = new BlogEntry(
                "Fake title",
                "Fake Description",
                "Fake Content",
                "url",
                "alt"
        );
        blogEntry.setId(99);
        blogEntry.setSlug(blogEntry.getTitle());
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

        //Ensure category joins are cascade deleted..
        List<Category> categoriesWIthId = jdbc.sql("SELECT * FROM posts_categories WHERE post_id = 1").query(Category.class).list();
        Assertions.assertEquals(0, categoriesWIthId.size());
    }

    @Test
    @Transactional
    public void testDeleteNonExistentBlogEntry() {
        int deleted = blogEntryDao.deleteById(99);
        Assertions.assertEquals(0, deleted);

        BlogEntry blogEntry = blogEntryDao.findById(99).orElse(null);
        Assertions.assertNull(blogEntry);

        //Ensure category joins are cascade deleted..
        List<Category> categoriesWIthId = jdbc.sql("SELECT * FROM posts_categories WHERE post_id = 99").query(Category.class).list();
        Assertions.assertEquals(0, categoriesWIthId.size());
    }

    @Test
    public void testExistsByTitleAndNotIdUpdate() {
        // update case : id passed in.
        boolean exists = blogEntryDao.existsByTitleAndNotId("Test Post 1", 1);
        Assertions.assertFalse(exists);
    }

    @Test
    public void testExistsByTitleAndNotIdTrue() {
        // create case : duplicate title, id is null.
        boolean exists = blogEntryDao.existsByTitleAndNotId("Test Post 1", null);
        Assertions.assertTrue(exists);
    }

    @Test
    public void testExistsByTitleAndNotIdFalse() {
        // create case : unique title, id is null.
        boolean exists = blogEntryDao.existsByTitleAndNotId("Test Post 99", null);
        Assertions.assertFalse(exists);
    }




}
