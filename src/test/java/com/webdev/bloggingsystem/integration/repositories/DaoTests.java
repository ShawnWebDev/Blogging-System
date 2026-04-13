package com.webdev.bloggingsystem.integration.repositories;

import com.webdev.bloggingsystem.blog.*;
import com.webdev.bloggingsystem.comment.Comment;
import com.webdev.bloggingsystem.comment.CommentDao;
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
@Import({AppUserDao.class, BlogEntryDao.class, CategoryDao.class, CommentDao.class})
public class DaoTests {

    @Autowired
    private AppUserDao appUserDao;
    @Autowired
    private BlogEntryDao blogEntryDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private JdbcClient jdbc;

    @Container
    @ServiceConnection
    static MariaDBContainer mariadbContainer = new MariaDBContainer("mariadb:lts-ubi9");

    // ** AppUser **

    @Test
    void getUserByUsername() {
        Optional<AppUser> user = appUserDao.findByUsername("TestAdmin");

        System.out.println(user);
        Assertions.assertTrue(user.isPresent());
        Assertions.assertEquals("TestAdmin", user.get().getUsername());
    }

    @Test
    void getAuthorByUsername() {
        Optional<AuthorDto> author = appUserDao.findAuthorByUsername("TestAdmin");

        System.out.println(author);
        Assertions.assertTrue(author.isPresent());
        Assertions.assertEquals(1, author.get().id());
    }

    // ** BlogEntry **

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
    public void testFindAllByCategory() {
        List<SimpleBlogEntryDto> result = blogEntryDao.findAllSimpleBlogEntriesToCategoryName("Test Category 3");
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());

        System.out.println("result: " + result);
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
        Assertions.assertEquals(3, insertedCategories);

        BlogEntry blog = blogEntryDao.findById(postId).orElse(null);
        Assertions.assertNotNull(blog);
        Assertions.assertEquals("Test title", blog.getTitle());
        System.out.println("result: " + blog);
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
        blogEntry.setSlug(blogEntry.getTitle());
        int isUpdated = blogEntryDao.update(blogEntry);
        blogEntry = blogEntryDao.findById(1).orElse(null);
        System.out.println("result after update: " + blogEntry);
        Assertions.assertNotNull(blogEntry);
        // 1 == record updated
        Assertions.assertEquals(1, isUpdated);
        Assertions.assertEquals("Updated Title", blogEntry.getTitle());
        Assertions.assertEquals("updated-title", blogEntry.getSlug());

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

    // ** Category **

    @Test
    public void testFindCategoryIdsInNames() {
        List<String> categoryNames = List.of("Test Category 1", "Test Category 2");
        List<Integer> categoryIds = categoryDao.findAllIdsInNames(categoryNames);
        Assertions.assertEquals(categoryIds.size(), categoryNames.size());
        Assertions.assertEquals(List.of(1, 2), categoryIds);

        System.out.println("result: " + categoryIds);
    }

    @Test
    public void testFindSimpleCategories() {
        List<SimpleCategoryDto> result = categoryDao.findAllNames();
        Assertions.assertNotNull(result);
        SimpleCategoryDto simpleCategoryDto_1 = new SimpleCategoryDto(1, "Test Category 1");
        SimpleCategoryDto simpleCategoryDto_2 = new SimpleCategoryDto(2, "Test Category 2");
        SimpleCategoryDto simpleCategoryDto_3 = new SimpleCategoryDto(3, "Test Category 3");
        Assertions.assertEquals(List.of(simpleCategoryDto_1, simpleCategoryDto_2, simpleCategoryDto_3), result);
    }

    // ** Comment **

    @Test
    public void testFindAllParentCommentsById() {
        List<Comment> result = commentDao.getParentCommentsByPostId(1);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals("Test Comment on Test Post 1", result.getFirst().getContent());
        Assertions.assertEquals(2, result.getFirst().getReplyCount());

        System.out.println("result: " + result);
    }

    @Test
    public void testFindAllRepliesByParentId() {
        List<Comment> result = commentDao.getReplyCommentsByParentId(1);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Test Reply 1 to Comment 1 on Test Post 1", result.getFirst().getContent());
        Assertions.assertEquals(1, result.getFirst().getReplyCount());

        System.out.println("result: " + result);
    }

}
