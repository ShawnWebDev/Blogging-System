package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.CommentRequestDto;
import com.webdev.bloggingsystem.dto.CommentResponseDto;
import com.webdev.bloggingsystem.services.CommentService;

import jakarta.validation.Valid;
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
        return ResponseEntity.ok(commentService.getAllUsersComments());
    }

    @PostMapping("/posts/{blogEntryId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable Integer blogEntryId, @RequestParam(name = "parentId", required = false ) Integer parentCommentId,
            @RequestBody @Valid CommentRequestDto commentText, UriComponentsBuilder ucb)
    {
        // On 200 OK, front-end will handle page update without a refresh
        Map.Entry<URI, CommentResponseDto> commentResponseDtoEntry =
                commentService.saveComment(commentText.comment(), blogEntryId, parentCommentId, ucb);
        return ResponseEntity.status(201)
                .location(commentResponseDtoEntry.getKey()).body(commentResponseDtoEntry.getValue());
    }


    @PutMapping("/comments/comment/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Integer commentId,
            @RequestBody @Valid CommentRequestDto commentText)
    {
        // On 200 OK, front-end will handle page update without a refresh
        return ResponseEntity.ok(commentService.updateComment(commentText.comment(), commentId));
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
