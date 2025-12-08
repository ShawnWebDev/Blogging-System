package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.UserProfile;
import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.Comment;
import com.webdev.bloggingsystem.dto.CommentResponseDto;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final static Logger logger  = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepo commentRepo;
    private final AppUserRepo appUserRepo;
    private final BlogEntryRepo blogEntryRepo;
    private final AuthService authService;

    public CommentServiceImpl(CommentRepo commentRepo, AppUserRepo appUserRepo, BlogEntryRepo blogEntryRepo,
                              AuthService authService) {
        this.commentRepo = commentRepo;
        this.appUserRepo = appUserRepo;
        this.blogEntryRepo = blogEntryRepo;
        this.authService = authService;
    }

    @Override
    public List<CommentResponseDto> getAllRepliesByParentId(Integer parentId) {
        List<Comment> comments = commentRepo.findAllByParentCommentId(parentId);
        return this.getCommentResponseDtos(comments);
    }

    @Override
    public List<CommentResponseDto> getAllCommentsByUsername() {
        // security chain prevents unauthorized user from accessing the calling endpoint
        UserProfile userProfile = authService.getUserProfile();
        List<Comment> comments = commentRepo.findAllByAuthorUsername(userProfile.username());
        return this.getCommentResponseDtos(comments);
    }

    @Override
    public List<CommentResponseDto> getAllTopLevelCommentsByBlogEntryId(Integer blogEntryId) {
        List<Comment> comments = commentRepo.fetchTopLevelCommentsByBlogEntryId(blogEntryId);
        List<Integer> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        logger.debug("getAllCommentsByBlogEntryId: comments {}", comments);
        if (comments.isEmpty()) {
            return List.of();
        } else {
            // maps reply count to the top-level comment ids
            Map<Integer, Integer> mapReplyCountToParentCommentIds = commentRepo.countRepliesByParentCommentIds(commentIds)
                    .stream().collect(Collectors.toMap(
                            row -> row.get("parentId", Integer.class),
                            row -> row.get("replyCount", Long.class).intValue()));

            // creates list of DTOs of parent comments with reply count
            return comments.stream()
                    .map(comment -> new CommentResponseDto(
                            comment.getId(),
                            comment.getBlogEntry().getId(),
                            comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                            comment.getComment(),
                            comment.getCreatedAt(),
                            comment.getUpdatedAt(),
                            comment.getAuthor().getUsername(),
                            mapReplyCountToParentCommentIds.getOrDefault(comment.getId(), 0)
                    )).toList();
        }
    }

    @Override
    public CommentResponseDto getCommentById(Integer commentId) {
        logger.debug("getCommentById: commentId: {}", commentId);

        // todo remove AppUser from query and get only user name from author id in separate query.
        Comment comment = commentRepo.findCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

        Integer amount = commentRepo.countRepliesByParentCommentId(commentId);
        logger.debug("getCommentById: reply count: {}", amount);

        return mapRequestToDto(comment, amount);
    }

    @Override
    public URI saveComment(String commentText, Integer postId, Integer parentId, UriComponentsBuilder ucb) {
        // must be authorized to access calling endpoint
        UserProfile userProfile = authService.getUserProfile();
        Integer authorId = appUserRepo.getIdByUsername(userProfile.username());
        logger.debug("saveComment: getting author {}", userProfile.username());
        AppUser authorRef = appUserRepo.getReferenceById(authorId);

        logger.debug("saveComment: getting blog entry {}", postId);
        BlogEntry blogEntry = blogEntryRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + postId));

        Comment parentComment = null;
        if (parentId != null) {
            logger.debug("saveComment: getting parent comment {}", parentId);
            parentComment = commentRepo.findCommentById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + parentId));
        }

        Comment savedComment = commentRepo.save(mapRequestToEntity(commentText, authorRef, blogEntry, parentComment));

        return ucb.path("api/comments/comment/{commentId}").buildAndExpand(savedComment.getId()).toUri();
    }

    @Override
    public void updateComment(String newCommentText, Integer commentId) {
        UserProfile userProfile = authService.getUserProfile();
        logger.debug("updateComment: getting author id {}", userProfile.username());
        Integer authorId = appUserRepo.getIdByUsername(userProfile.username());

        // todo remove AppUser from query and get only user name from author id in separate query.
        Comment commentToUpdate = commentRepo.findCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

        if (commentToUpdate.getAuthorId() != authorId) {
            throw new ResourceNotFoundException("Comment not found with id " + commentId);
        }

        commentToUpdate.setComment(newCommentText);
        commentRepo.save(commentToUpdate);
    }

    @Override
    public void deleteComment(Integer commentId) {
        UserProfile userProfile = authService.getUserProfile();
        logger.debug("deleteComment: getting author id {}", userProfile.username());
        Integer authorId = appUserRepo.getIdByUsername(userProfile.username());

        logger.debug("deleteComment: getting Comment with author and BlogEntry with author {}", userProfile.username());
        Comment commentToDelete = commentRepo.findBlogEntryAndCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

        logger.debug("deleteComment: deleting comment {} from entry {}",
                commentToDelete, commentToDelete.getBlogEntry()
        );

        boolean authorized = false;
        String commentText = "Comment Removed By ";
        if (commentToDelete.getAuthorId() ==  authorId) {
            authorized = true;
            commentText += "Comment Author..";
        } else if (commentToDelete.getBlogEntry().getAuthorId().equals(authorId)) {
            authorized = true;
            commentText += "Blog Author..";
        } else if (userProfile.isAdmin()) {
            authorized = true;
            commentText += "Admin..";
        }

        if (authorized) {
            commentToDelete.setComment(commentText);
            commentRepo.save(commentToDelete);
            return;
        }

        logger.debug("deleteComment: comment not found with Comment author or BlogEntry author {}", userProfile.username());
        throw new ResourceNotFoundException("Comment not found with id " + commentId);
    }

    private static Comment mapRequestToEntity(String commentText, AppUser author, BlogEntry blogEntry, Comment parentComment) {
        Comment comment = new Comment(commentText, author, blogEntry);
        if (parentComment != null) {
            comment.setParentComment(parentComment);
        }
        return comment;
    }

    private static CommentResponseDto mapRequestToDto(Comment comment, Integer amount) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getBlogEntry().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getComment(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getAuthor().getUsername(),
                amount
        );
    }

    private static List<CommentResponseDto> mapCommentListToDtoList(List<Comment> comments, Map<Integer, Integer> replyCountMap) {
        List<CommentResponseDto> responseDtos = new ArrayList<>();
        for (Comment comment : comments) {
            responseDtos.add(
                    mapRequestToDto(comment, replyCountMap.get(comment.getId()))
            );
        }
        return responseDtos;
    }

    private Map<Integer, Integer> mapRepliesCountToComments(List<Comment> comments) {
        List<Integer> commentIds = comments.stream().map(Comment::getId).toList();
        return commentRepo.countRepliesByParentCommentIds(commentIds)
                .stream().collect(Collectors.toMap(
                        row -> row.get("parentId", Integer.class),
                        row -> row.get("replyCount", Long.class).intValue()
                ));
    }

    private List<CommentResponseDto> getCommentResponseDtos(List<Comment> comments) {
        Map<Integer, Integer> countRepliesByParentCommentIds = this.mapRepliesCountToComments(comments);
        List<CommentResponseDto> responseDtos;
        if (comments.isEmpty()) {
            return List.of();
        }
        responseDtos = mapCommentListToDtoList(comments, countRepliesByParentCommentIds);
        return responseDtos;
    }
}