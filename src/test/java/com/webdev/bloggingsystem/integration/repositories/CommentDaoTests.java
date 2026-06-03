package com.webdev.bloggingsystem.integration.repositories;

import com.webdev.bloggingsystem.comment.*;

import com.webdev.bloggingsystem.user.AuthorDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.util.List;

@JdbcTest
@ActiveProfiles("test")
@Testcontainers
@Import(CommentDao.class)
public class CommentDaoTests {
    @Autowired
    private CommentDao commentDao;

    @Container
    @ServiceConnection
    static MariaDBContainer mariadbContainer = new MariaDBContainer("mariadb:lts-ubi9");


    @Test
    public void testFindAllParentCommentsByPostId() {
        List<Comment> result = commentDao.getParentCommentsByPostId(1);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        // fetched in descending order by createdAt.
        Assertions.assertEquals("Test Comment on Test Post 1", result.getFirst().getContent());
        Assertions.assertEquals(2, result.getFirst().getReplyCount());

        System.out.println("result: " + result);
    }

    @Test
    public void testFindAllParentCommentsById_NonExistentId() {
        List<Comment> result = commentDao.getParentCommentsByPostId(99);

        Assertions.assertTrue(result.isEmpty());

        System.out.println("result: " + result);
    }

    @Test
    public void testFindAllRepliesByParentId() {
        List<Comment> result = commentDao.getReplyCommentsByParentId(1, 1);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        // fetched in descending order by createdAt.
        Assertions.assertEquals("Test Reply 2 to Comment 1 on Test Post 1", result.getFirst().getContent());
        Assertions.assertEquals(1, result.get(1).getReplyCount());

        System.out.println("result: " + result);
    }

    @Test
    public void testFindAllRepliesByParentId_NoReplies() {
        List<Comment> result = commentDao.getReplyCommentsByParentId(4,3);

        Assertions.assertTrue(result.isEmpty());

        System.out.println("result: " + result);
    }

    @Test
    public void testFindAllRepliesByParentId_NonExistentId() {
        List<Comment> result = commentDao.getReplyCommentsByParentId(99, 1);

        Assertions.assertTrue(result.isEmpty());

        System.out.println("result: " + result);
    }

    @Test
    public void testFindCommentById() {
        Comment result = commentDao.getCommentById(1).orElse(null);
        System.out.println("result: " + result);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Test Comment on Test Post 1", result.getContent());
        // reply count should be defaulted to 0 in single comment query as it's only used when inserting new comment.
        Assertions.assertEquals(0, result.getReplyCount());
    }

    @Test
    public void testFindCommentById_NonExistentId() {
        Comment result = commentDao.getCommentById(99).orElse(null);
        Assertions.assertNull(result);
    }

    @Test
    @Transactional
    void testInsertParentComment() {
        Comment comment = new Comment(
                "Test insert new parent comment.",
                new AuthorDto(2, "TestUser"),
                1
        );
        int insertedId = commentDao.insert(comment);

        Comment insertedComment = commentDao.getCommentById(insertedId).orElse(null);
        Assertions.assertNotNull(insertedComment);
        Assertions.assertEquals("Test insert new parent comment.", insertedComment.getContent());
        Assertions.assertEquals(0, insertedComment.getReplyCount());
        Assertions.assertEquals("TestUser", insertedComment.getAuthor().username());
    }

    @Test
    @Transactional
    void testInsertReplyComment() {
        Comment comment = new Comment(
                "Test insert new reply comment.",
                new AuthorDto(2, "TestUser"),
                1
        );
        comment.setParentCommentId(1);
        int insertedId = commentDao.insert(comment);
        //verify insert
        Comment insertedComment = commentDao.getCommentById(insertedId).orElse(null);
        Assertions.assertNotNull(insertedComment);
        Assertions.assertEquals("Test insert new reply comment.", insertedComment.getContent());
        Assertions.assertEquals(0, insertedComment.getReplyCount());
        Assertions.assertEquals("TestUser", insertedComment.getAuthor().username());
        //verify is counted in replies of parent comment
        List<Comment> parentCommentReplies = commentDao.getReplyCommentsByParentId(1, 1);
        Assertions.assertNotNull(parentCommentReplies);
        Assertions.assertEquals(3, parentCommentReplies.size());
        Assertions.assertEquals("Test insert new reply comment.", parentCommentReplies.getFirst().getContent());
    }

    @Test
    @Transactional
    void testUpdateComment() {
        Comment comment = commentDao.getCommentById(1).orElse(null);
        Assertions.assertNotNull(comment);
        String updatedContent = "***Updated comment.***";
        int isUpdated = commentDao.update(1, updatedContent);
        Assertions.assertEquals(1, isUpdated);
        Comment updatedComment = commentDao.getCommentById(1).orElse(null);
        Assertions.assertNotNull(updatedComment);
        Assertions.assertEquals(updatedContent, updatedComment.getContent());
    }

    @Test
    @Transactional
    void testSoftDeleteComment() {
        int softDeletedComment = commentDao.softDelete(1);
        Assertions.assertEquals(1, softDeletedComment);

        Comment comment = commentDao.getCommentById(1).orElse(null);
        Assertions.assertNotNull(comment);
        Assertions.assertEquals("Comment Deleted", comment.getContent());
        Assertions.assertTrue(comment.isDeleted());
    }

    @Test
    @Transactional
    void testHardDeleteComment() {
        int hardDeletedComment = commentDao.hardDelete(4);
        Assertions.assertEquals(1, hardDeletedComment);

        Comment comment = commentDao.getCommentById(4).orElse(null);
        Assertions.assertNull(comment);
    }
}
