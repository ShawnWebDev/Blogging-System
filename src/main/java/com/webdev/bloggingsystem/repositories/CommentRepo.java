package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepo extends CrudRepository<Comment, Integer> {
}
