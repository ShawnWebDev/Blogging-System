package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.CommentResponseDto;
import com.webdev.bloggingsystem.exceptions.MaxBytes;
import com.webdev.bloggingsystem.services.CommentService;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/posts/{blogEntryId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getAllCommentsByBlogEntryId(@PathVariable Integer blogEntryId)
    {
        return ResponseEntity.ok(commentService.getAllTopLevelCommentsByBlogEntryId(blogEntryId));
    }

    @GetMapping("/comments/{parentId}")
    public ResponseEntity<List<CommentResponseDto>> getAllReplyCommentsByParentId(@PathVariable Integer parentId)
    {
        return ResponseEntity.ok(commentService.getAllRepliesByParentId(parentId));
    }

    @GetMapping("/comments/comment/{commentId}")
    public ResponseEntity<CommentResponseDto> getSingleCommentById(@PathVariable Integer commentId)
    {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @GetMapping("/comments/me")
    public ResponseEntity<List<CommentResponseDto>> getAllCommentsForUser()
    {
        return ResponseEntity.ok(commentService.getAllCommentsByUsername());
    }

    @PostMapping("/posts/{blogEntryId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Integer blogEntryId, @RequestParam(name = "parentId", required = false ) Integer parentCommentId,
            @RequestBody @MaxBytes(4000) @NotBlank(message = "Input empty") String commentText, UriComponentsBuilder ucb)
    {
        // On 200 OK, front-end will handle page update without a refresh
        Map.Entry<URI, CommentResponseDto> commentResponseDtoEntry =
                commentService.saveComment(commentText, blogEntryId, parentCommentId, ucb);
        return ResponseEntity.status(201)
                .location(commentResponseDtoEntry.getKey()).body(commentResponseDtoEntry.getValue());
    }

    @PutMapping("/comments/comment/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable Integer commentId,
            @RequestBody @MaxBytes(value = 4000) @NotBlank(message = "Input empty") String commentText)
    {
        // On 200 OK, front-end will handle page update without a refresh
        return ResponseEntity.ok(commentService.updateComment(commentText, commentId));
    }

    @DeleteMapping("/comments/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Integer commentId)
    {
        // on 204 NO CONTENT - Front-end will handle page update without a refresh
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
