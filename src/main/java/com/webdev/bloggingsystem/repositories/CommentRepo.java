package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Comment;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepo extends CrudRepository<Comment, Integer> {
    @EntityGraph(value = "comment-with-author", type = EntityGraph.EntityGraphType.LOAD)
    List<Comment> findAllByParentCommentId(@Param("id") Integer parentId);

    @EntityGraph(value = "comment-with-author", type = EntityGraph.EntityGraphType.LOAD)
    List<Comment> findAllByBlogEntryId(Integer blogEntryId);

    @EntityGraph(value = "comment-with-author", type = EntityGraph.EntityGraphType.LOAD)
    List<Comment> findAllByAuthorUsername(String username);

    @EntityGraph(value = "comment-with-author", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Comment> findCommentById(Integer id);

    @EntityGraph(value = "comment-with-author-and-blogEntry", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Comment> findBlogEntryAndCommentById(Integer commentId);

    Integer countRepliesByParentCommentId(Integer parentCommentId);

    @Query("SELECT c.parentComment.id AS parentId, count(c) AS replyCount " +
            "FROM Comment c WHERE c.parentComment.id IN :parentIds GROUP BY c.parentComment.id")
    List<Tuple> countRepliesByParentCommentIds(@Param("parentIds") List<Integer> parentIds);

    @Query("SELECT c.blogEntry.id AS blogId, count(c) AS commentCount " +
            "FROM Comment c WHERE c.blogEntry.id IN :entryIds GROUP BY c.blogEntry.id")
    List<Tuple> countCommentsByBlogEntryIds(@Param("entryIds") List<Integer> entryIds);
}
