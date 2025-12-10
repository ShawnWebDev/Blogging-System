package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.CommentResponseDto;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface CommentService {
    List<CommentResponseDto> getAllRepliesByParentId(Integer parentId);
    CommentResponseDto getCommentById(Integer commentId);
    List<CommentResponseDto> getAllCommentsByUsername();
    List<CommentResponseDto> getAllTopLevelCommentsByBlogEntryId(Integer blogEntryId);
    Map.Entry<URI, CommentResponseDto> saveComment(String commentText, Integer postId, Integer parentId, UriComponentsBuilder ucb);
    CommentResponseDto updateComment(String newCommentText, Integer commentId);
    void deleteComment(Integer commentId);
}
