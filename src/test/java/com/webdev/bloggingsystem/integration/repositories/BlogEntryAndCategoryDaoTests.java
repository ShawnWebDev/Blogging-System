package com.webdev.bloggingsystem.integration.repositories;

import com.webdev.bloggingsystem.blog.*;

import static org.junit.jupiter.api.Assertions.*;

import com.webdev.bloggingsystem.comment.Comment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.util.List;
import java.util.Set;

@JdbcTest
@ActiveProfiles("test")
@Testcontainers
@Import({BlogEntryDao.class, CategoryDao.class})
public class BlogEntryAndCategoryDaoTests {
    @Autowired
    private BlogEntryDao blogEntryDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private JdbcClient jdbc;

    @Container
    @ServiceConnection
    static MariaDBContainer mariadbContainer = new MariaDBContainer("mariadb:lts-ubi9");

    /* --- blog entry --- */

    @Test
    public void testFindById() {
        BlogEntry blogEntry = blogEntryDao.findById(1).orElse(null);
        assertNotNull(blogEntry);
        assertEquals("Test Post 1", blogEntry.getTitle());
        assertEquals(List.of("Test Category 1", "Test Category 2"), blogEntry.getCategoryNames());

        System.out.println("result: " + blogEntry);
    }

    @Test
    public void testFindByIdNotExist() {
        BlogEntry blogEntry = blogEntryDao.findById(99).orElse(null);
        assertNull(blogEntry);

        System.out.println("result: " + blogEntry);
    }

    @Test
    public void testFindAllSimpleByCategory() {
        // Test Category 3 - contains 3 Blog Entries, one is "In-progress" and should not be included.
        List<SimpleBlogEntryDto> result = blogEntryDao.findAllSimpleBlogEntriesToCategoryName("Test Category 3");

        assertEquals(2, result.size());
        for (SimpleBlogEntryDto blogEntry : result) {
            assertNotEquals("Test Post 6", blogEntry.title());
        }

        System.out.println("result: " + result);
    }

    @Test
    public void testFindAllSimple() {
        // 6 total Blog Entries, one "In-Progress" should not be included.
        List<SimpleBlogEntryDto> result = blogEntryDao.findAllSimple();

        assertEquals(5, result.size());
        for (SimpleBlogEntryDto blogEntry : result) {
            assertNotEquals("Test Post 6", blogEntry.title());
        }

        System.out.println("result: " + result);
    }

    @Test
    public void testFindInProgress() {
        List<SimpleBlogEntryDto> result = blogEntryDao.findAllSimpleInProgress();
        assertEquals(1, result.size());
        assertEquals("Test Post 6", result.getFirst().title());
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
        blogEntry.setInProgress(false);
        int postId = blogEntryDao.insert(blogEntry);
        System.out.println("postId: " + postId);
        int insertedCategories = categoryDao.batchInsertJoins(Set.of(1, 2, 3), postId);
        assertEquals(3, insertedCategories);

        BlogEntry blog = blogEntryDao.findById(postId).orElse(null);
        assertNotNull(blog);
        assertEquals("Test title", blog.getTitle());
        System.out.println("result: " + blog);
    }

    @Test
    @Transactional
    public void testUpdateBlogEntry() {
        BlogEntry blogEntry = blogEntryDao.findById(1).orElse(null);
        System.out.println("result: " + blogEntry);
        assertNotNull(blogEntry);
        System.out.println("slug before title update: " + blogEntry.getSlug());

        blogEntry.setContent("Updated Content Here...");
        blogEntry.setTitle("Updated Title");
        blogEntry.setSlug(blogEntry.getTitle());
        int isUpdated = blogEntryDao.update(blogEntry);
        blogEntry = blogEntryDao.findById(1).orElse(null);
        System.out.println("result after update: " + blogEntry);
        assertNotNull(blogEntry);
        // 1 == record updated
        assertEquals(1, isUpdated);
        assertEquals("Updated Title", blogEntry.getTitle());
        assertEquals("updated-title", blogEntry.getSlug());

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
        assertEquals(0, blogEntryDao.update(blogEntry));

        BlogEntry updatedBlogEntry = blogEntryDao.findById(99).orElse(null);
        assertNull(updatedBlogEntry);
    }

    @Test
    @Transactional
    public void testDeleteBlogEntry() {
        BlogEntry blogEntry = blogEntryDao.findById(1).orElse(null);
        assertNotNull(blogEntry);

        int deleted = blogEntryDao.deleteById(blogEntry.getId());
        assertEquals(1, deleted);

        blogEntry = blogEntryDao.findById(1).orElse(null);
        assertNull(blogEntry);

        //Ensure category joins and related comments are cascade deleted..
        List<Category> categoriesWithId = jdbc.sql("SELECT * FROM posts_categories WHERE post_id = 1").query(Category.class).list();
        assertEquals(0, categoriesWithId.size());

        List<Comment> commentsWithId = jdbc.sql("SELECT * FROM comments WHERE post_id = 1").query(Comment.class).list();
        assertEquals(0, commentsWithId.size());
    }

    @Test
    @Transactional
    public void testDeleteNonExistentBlogEntry() {
        int deleted = blogEntryDao.deleteById(99);
        assertEquals(0, deleted);

        BlogEntry blogEntry = blogEntryDao.findById(99).orElse(null);
        assertNull(blogEntry);
    }

    @Test
    public void testExistsByTitleAndNotIdUpdate() {
        // update case : id passed in, title gets added to form regardless (shouldn't change it in the first place as the slug will change with it),
        // using same DTO used to create post, don't need to mess with it.
        boolean exists = blogEntryDao.existsByTitleAndNotId("Test Post 1", 1);
        System.out.println("exists: " + exists);
        assertFalse(exists);
    }

    @Test
    public void testExistsByTitleAndNotIdTrue() {
        // create case : duplicate title, id is null.
        boolean exists = blogEntryDao.existsByTitleAndNotId("Test Post 1", null);
        System.out.println("exists: " + exists);
        assertTrue(exists);
    }

    @Test
    public void testExistsByTitleAndNotIdFalse() {
        // create case : unique title, id is null.
        boolean exists = blogEntryDao.existsByTitleAndNotId("Test Post 99", null);
        System.out.println("exists: " + exists);
        assertFalse(exists);
    }

    /* --- category --- */

    @Test
    public void testFindCategoryIdsInNames() {
        List<String> categoryNames = List.of("Test Category 1", "Test Category 2");
        List<Integer> categoryIds = categoryDao.findAllIdsInNames(categoryNames);
        assertEquals(categoryIds.size(), categoryNames.size());
        assertEquals(List.of(1, 2), categoryIds);

        System.out.println("result: " + categoryIds);
    }

    @Test
    public void testFindSimpleCategories() {
        List<SimpleCategoryDto> result = categoryDao.findAllNames();

        SimpleCategoryDto simpleCategoryDto_1 = new SimpleCategoryDto(1, "Test Category 1");
        SimpleCategoryDto simpleCategoryDto_2 = new SimpleCategoryDto(2, "Test Category 2");
        SimpleCategoryDto simpleCategoryDto_3 = new SimpleCategoryDto(3, "Test Category 3");
        assertEquals(List.of(simpleCategoryDto_1, simpleCategoryDto_2, simpleCategoryDto_3), result);
    }


}
