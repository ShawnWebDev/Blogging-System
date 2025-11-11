package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Comment;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepo extends Repository<Comment, Integer> {
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.author WHERE c.parentComment.id = :id")
    List<Comment> findAllByParentCommentId(@Param("id") Integer parentId);
}
