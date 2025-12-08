package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.CommentResponseDto;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

public interface CommentService {
    List<CommentResponseDto> getAllRepliesByParentId(Integer parentId);
    CommentResponseDto getCommentById(Integer commentId);
    List<CommentResponseDto> getAllCommentsByUsername();
    List<CommentResponseDto> getAllTopLevelCommentsByBlogEntryId(Integer blogEntryId);
    URI saveComment(String commentText, Integer postId, Integer parentId, UriComponentsBuilder ucb);
    void updateComment(String newCommentText, Integer commentId);
    void deleteComment(Integer commentId);
}
