package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.CommentResponseDto;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface CommentService {
    List<CommentResponseDto> getAllRepliesByParentId(int parentId);
    CommentResponseDto getCommentById(int commentId);
    List<CommentResponseDto> getAllCommentsByUsername();
    List<CommentResponseDto> getAllTopLevelCommentsByBlogEntryId(int blogEntryId);
    Map.Entry<URI, CommentResponseDto> saveComment(String commentText, int postId, Integer parentId, UriComponentsBuilder ucb);
    CommentResponseDto updateComment(String newCommentText, int commentId);
    void deleteComment(int commentId);
}
