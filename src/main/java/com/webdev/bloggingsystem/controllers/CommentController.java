package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.CommentResponseDto;
import com.webdev.bloggingsystem.exceptions.MaxBytes;
import com.webdev.bloggingsystem.services.CommentService;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.List;

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

    //todo : implement and test endpoints using admin role (UserProfile)

    @PostMapping("/posts/{blogEntryId}/comments")
    public ResponseEntity<Void> createComment(
            @PathVariable Integer blogEntryId, @RequestParam(name = "parentId", required = false ) Integer parentCommentId,
            @RequestBody @MaxBytes(4000) @NotBlank(message = "Input empty") String commentText,
            Principal principal, UriComponentsBuilder ucb)
    {
        // todo : return CREATED with DTO - Front-end will handle page update without a refresh
        return ResponseEntity.created(commentService.saveComment(commentText, blogEntryId, parentCommentId, ucb))
                .build();
    }

    @PutMapping("/comments/comment/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Integer commentId,
            @RequestBody @MaxBytes(value = 4000) @NotBlank(message = "Input empty") String commentText)
    {
        // todo : return OK with DTO - Front-end will handle page update without a refresh
        commentService.updateComment(commentText, commentId);
        return ResponseEntity.noContent().build();
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
