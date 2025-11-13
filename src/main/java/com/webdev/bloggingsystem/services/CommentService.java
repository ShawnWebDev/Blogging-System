package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.entities.CommentResponseDto;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

public interface CommentService {
    List<CommentResponseDto> getAllRepliesByParentId(Integer parentId);
    CommentResponseDto getCommentById(Integer commentId);
    URI saveComment(String commentText, Integer postId, Integer parentId, String principalName, UriComponentsBuilder ucb);
    void updateComment(String newCommentText, Integer commentId, String principalName);
}
