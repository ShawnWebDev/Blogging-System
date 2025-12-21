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

    @Query("SELECT c FROM Comment c WHERE c.blogEntryId = :blogEntryId AND c.parentCommentId IS NULL")
    List<Comment> fetchTopLevelCommentsByBlogEntryId(@Param("blogEntryId") Integer blogEntryId);

    @Query("SELECT c.parentCommentId AS parentId, count(c) AS replyCount " +
            "FROM Comment c WHERE c.parentCommentId IN :parentIds GROUP BY c.parentCommentId")
    List<Tuple> countRepliesByParentCommentIds(@Param("parentIds") List<Integer> parentIds);

    @Query("SELECT c.blogEntryId AS blogId, count(c) AS commentCount " +
            "FROM Comment c WHERE c.blogEntryId IN :entryIds GROUP BY c.blogEntryId")
    List<Tuple> countCommentsInBlogEntryIds(@Param("entryIds") List<Integer> entryIds);

    @Query("SELECT c.blogEntryId AS blogId, count(c) AS commentCount " +
            "FROM Comment c WHERE c.blogEntryId = :entryId GROUP BY c.blogEntryId")
    Tuple countCommentsByBlogEntryId(@Param("entryId") Integer entryId);
}
