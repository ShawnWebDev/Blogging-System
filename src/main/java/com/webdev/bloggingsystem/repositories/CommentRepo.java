package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Comment;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepo extends CrudRepository<Comment, Integer> {
    List<Comment> findAllByParentCommentId(@Param("id") Integer parentId);

    List<Comment> findAllByAuthorId(Integer authorId);

    Integer countRepliesByParentCommentId(Integer parentCommentId);

    @Query("SELECT c FROM Comment c WHERE c.blogEntryId = :blogEntryId AND c.parentComment IS NULL")
    List<Comment> fetchTopLevelCommentsByBlogEntryId(@Param("blogEntryId") Integer blogEntryId);

    @Query("SELECT c.parentComment.id AS parentId, count(c) AS replyCount " +
            "FROM Comment c WHERE c.parentComment.id IN :parentIds GROUP BY c.parentComment.id")
    List<Tuple> countRepliesByParentCommentIds(@Param("parentIds") List<Integer> parentIds);

    @Query("SELECT c.blogEntry.id AS blogId, count(c) AS commentCount " +
            "FROM Comment c WHERE c.blogEntry.id IN :entryIds GROUP BY c.blogEntry.id")
    List<Tuple> countCommentsInBlogEntryIds(@Param("entryIds") List<Integer> entryIds);

    @Query("SELECT c.blogEntry.id AS blogId, count(c) AS commentCount " +
            "FROM Comment c WHERE c.blogEntry.id = :entryId GROUP BY c.blogEntry.id")
    Tuple countCommentsByBlogEntryId(@Param("entryId") Integer entryId);
}
