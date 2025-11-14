package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Comment;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepo extends CrudRepository<Comment, Integer> {
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.author WHERE c.parentComment.id = :id")
    List<Comment> findAllByParentCommentId(@Param("id") Integer parentId);

    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.author WHERE c.id = :id")
    Optional<Comment> findCommentById(Integer id);

    Integer countRepliesByParentCommentId(Integer parentCommentId);
}
