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

    @GetMapping("/comments/{parentId}")
    public ResponseEntity<List<CommentResponseDto>> getAllReplyCommentsByParentId(
            @PathVariable Integer parentId)
    {
        return ResponseEntity.ok(commentService.getAllRepliesByParentId(parentId));
    }

    @GetMapping("/comments/comment/{commentId}")
    public ResponseEntity<CommentResponseDto> getSingleCommentById(
            @PathVariable Integer commentId)
    {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Void> createComment(
            @PathVariable Integer postId, @RequestParam(name = "parentId", required = false ) Integer parentCommentId,
            @RequestBody @MaxBytes(4000) @NotBlank(message = "Input empty") String commentText,
            Principal principal, UriComponentsBuilder ucb)
    {
        // todo : should this send the URI of the Comment or of the Post that the comment is contained?
        // todo : should there be an anchor tag for comments to auto scroll to?
        return ResponseEntity.created(commentService.saveComment(commentText, postId, parentCommentId, principal.getName(), ucb))
                .build();
    }

    @PutMapping("/comments/comment/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Integer commentId,
            @RequestBody @MaxBytes(value = 4000) @NotBlank(message = "Input empty") String commentText,
            Principal principal)
    {
        commentService.updateComment(commentText, commentId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Integer commentId, Principal principal,
            @RequestParam(name = "isBlogEntryAuthor", required = false ) Boolean isBlogEntryAuthor)
    {
        commentService.deleteComment(commentId, principal.getName());
        return ResponseEntity.noContent().build();
    }


    //todo : implement and test admin endpoints
    /*
    @PutMapping("admin/comments/comment/{commentId}")

    @DeleteMapping("admin/comments/{commentId}")
    */
}
