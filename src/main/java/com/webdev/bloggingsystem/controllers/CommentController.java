package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.CommentResponseDto;
import com.webdev.bloggingsystem.services.CommentService;

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
    public ResponseEntity<List<CommentResponseDto>> getAllReplyCommentsByParentId(@PathVariable Integer parentId) {
        return ResponseEntity.ok(commentService.getAllRepliesByParentId(parentId));
    }

    @GetMapping("/comments/comment/{commentId}")
    public ResponseEntity<CommentResponseDto> getSingleCommentById(@PathVariable Integer commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Void> createComment(@PathVariable Integer postId,
                                              @RequestParam(name = "parentId", required = false ) Integer parentCommentId,
                                              @RequestBody String commentText,
                                              Principal principal, UriComponentsBuilder ucb) {
        return ResponseEntity.created(commentService.saveComment(commentText, postId, parentCommentId, principal.getName(), ucb))
                .build();
    }

    @PutMapping("/comments/comment/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Integer commentId,
                                              @RequestBody String commentText, Principal principal) {
        commentService.updateComment(commentText, commentId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
