package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.Comment;
import com.webdev.bloggingsystem.entities.CommentResponseDto;
import com.webdev.bloggingsystem.exceptions.ResourceNotFoundException;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import com.webdev.bloggingsystem.repositories.BlogEntryRepo;
import com.webdev.bloggingsystem.repositories.CommentRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private final static Logger logger  = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepo commentRepo;
    private final AppUserRepo appUserRepo;
    private final BlogEntryRepo blogEntryRepo;

    public CommentServiceImpl(CommentRepo commentRepo, AppUserRepo appUserRepo, BlogEntryRepo blogEntryRepo) {
        this.commentRepo = commentRepo;
        this.appUserRepo = appUserRepo;
        this.blogEntryRepo = blogEntryRepo;
    }

    @Override
    public List<CommentResponseDto> getAllRepliesByParentId(Integer parentId) {
        List<Comment> comments = commentRepo.findAllByParentCommentId(parentId);
        List<CommentResponseDto> responseDtos;
        if (!comments.isEmpty()) {
            responseDtos = new ArrayList<>();
            for (Comment comment : comments) {
                responseDtos.add(
                        this.mapRequestToDto(comment)
                );
            }
            logger.debug("getAllRepliesByParentId: responseDtos: {}", responseDtos);
            return responseDtos;
        }
        return List.of();
    }

    @Override
    public CommentResponseDto getCommentById(Integer commentId) {
        logger.debug("getCommentById: commentId: {}", commentId);

        Comment comment = commentRepo.getCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

        return this.mapRequestToDto(comment);
    }

    // todo: create validation logic, use before saving & updating.
    @Override
    public URI saveComment(String commentText, Integer postId, Integer parentId, String principalName,
                           UriComponentsBuilder ucb) {
        logger.debug("saveComment: getting author {}", principalName);
        AppUser author = appUserRepo.findByUsername(principalName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with name " + principalName));

        logger.debug("saveComment: getting blog entry {}", postId);
        BlogEntry blogEntry = blogEntryRepo.findSimpleBlogEntryById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + postId));

        Comment parentComment = null;
        if (parentId != null) {
            logger.debug("saveComment: getting parent comment {}", parentId);
            parentComment = commentRepo.getCommentById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + parentId));
        }

        Comment savedComment = commentRepo.save(this.mapRequestToEntity(commentText, author, blogEntry, parentComment));

        return ucb.path("api/comments/comment/{commentId}").buildAndExpand(savedComment.getId()).toUri();
    }

    @Override
    public void updateComment(String newCommentText, Integer commentId, String principalName) {
        // todo: !
    }

    private Comment mapRequestToEntity(String commentText, AppUser author, BlogEntry blogEntry, Comment parentComment) {
        Comment comment = new Comment(commentText, author, blogEntry);
        if (parentComment != null) {
            comment.setParentComment(parentComment);
        }
        return comment;
    }

    private CommentResponseDto mapRequestToDto(Comment comment) {
        return new CommentResponseDto(
                comment.getId(), comment.getComment(), comment.getCreatedAt(), comment.getAuthor().getUsername()
        );
    }
}
