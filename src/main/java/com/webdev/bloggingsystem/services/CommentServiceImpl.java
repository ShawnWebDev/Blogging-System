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
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        if (comments.isEmpty()) {
            return List.of();
        } else {
            return this.getCommentResponseDtos(comments);
        }
    }

    @Override
    public List<CommentResponseDto> getAllCommentsByUsername() {
        // security chain prevents unauthorized user from accessing the calling endpoint
        UserProfile userProfile = authService.getUserProfile();
        Integer authorId = appUserRepo.findIdByUsername(userProfile.username());
        List<Comment> comments = commentRepo.findAllByAuthorId(authorId);
        if (comments.isEmpty()) {
            return List.of();
        } else {
            return this.getCommentResponseDtos(comments);
        }
    }

    @Override
    public List<CommentResponseDto> getAllTopLevelCommentsByBlogEntryId(Integer blogEntryId) {
        List<Comment> comments = commentRepo.fetchTopLevelCommentsByBlogEntryId(blogEntryId);
        if (comments.isEmpty()) {
            return List.of();
        } else {
            return this.getCommentResponseDtos(comments);
        }
    }

    @Override
    public CommentResponseDto getCommentById(Integer commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));
        String authorName = appUserRepo.findUsernameById(comment.getAuthorId());
        Integer amount = commentRepo.countRepliesByParentCommentId(commentId);

        return mapRequestToDto(comment, authorName, amount);
    }

    @Override
    public URI saveComment(String commentText, Integer postId, Integer parentId, UriComponentsBuilder ucb) {
        UserProfile userProfile = authService.getUserProfile();
        Integer authorId = appUserRepo.findIdByUsername(userProfile.username());
        AppUser authorRef = appUserRepo.getReferenceById(authorId);

        BlogEntry blogEntry = blogEntryRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + postId));

        Comment parentComment = null;
        if (parentId != null) {
            parentComment = commentRepo.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + parentId));
        }
        Comment savedComment = commentRepo.save(mapRequestToEntity(commentText, authorRef, blogEntry, parentComment));

        // todo : return saved entity via DTO
        return ucb.path("api/comments/comment/{commentId}").buildAndExpand(savedComment.getId()).toUri();
    }

    @Override
    public void updateComment(String newCommentText, Integer commentId) {
        UserProfile userProfile = authService.getUserProfile();
        Integer userId = appUserRepo.findIdByUsername(userProfile.username());

        Comment commentToUpdate = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

        if (commentToUpdate.getAuthorId() == userId) {
            commentToUpdate.setComment(newCommentText);
            commentRepo.save(commentToUpdate);
            // todo : return updated entity via DTO
            return;
        }

        throw new ResourceNotFoundException("Comment not found with id " + commentId);
    }

    @Override
    public void deleteComment(Integer commentId) {
        UserProfile userProfile = authService.getUserProfile();
        Integer authorId = appUserRepo.findIdByUsername(userProfile.username());

        Comment commentToDelete = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

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

        throw new ResourceNotFoundException("Comment not found with id " + commentId);
    }

    private static Comment mapRequestToEntity(String commentText, AppUser author, BlogEntry blogEntry, Comment parentComment) {
        Comment comment = new Comment(commentText, author, blogEntry);
        if (parentComment != null) {
            comment.setParentComment(parentComment);
        }
        return comment;
    }

    private static CommentResponseDto mapRequestToDto(Comment comment, String authorName, Integer amount) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getBlogEntry().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getComment(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                authorName,
                amount
        );
    }

    private static List<CommentResponseDto> mapCommentListToDtoList(
            List<Comment> comments, Map<Integer, String> authorIdsToNames, Map<Integer, Integer> replyCountMap)
    {
        List<CommentResponseDto> responseDtos = new ArrayList<>();
        for (Comment comment : comments) {
            responseDtos.add(
                    mapRequestToDto(comment, authorIdsToNames.get(comment.getAuthorId()), replyCountMap.get(comment.getId()))
            );
        }
        return responseDtos;
    }

    private Map<Integer, Integer> mapRepliesCountToCommentIds(List<Integer> commentIds) {
        return commentRepo.countRepliesByParentCommentIds(commentIds)
                .stream().collect(Collectors.toMap(
                        row -> row.get("parentId", Integer.class),
                        row -> row.get("replyCount", Long.class).intValue()
                ));
    }

    private Map<Integer, String> mapAuthorNameToAuthorIds(Set<Integer> authorIds) {
        return appUserRepo.findUsernamesById(authorIds)
                .stream().collect(Collectors.toMap(
                        row -> row.get("userId", Integer.class),
                        row -> row.get("username", String.class)
                ));
    }

    private List<CommentResponseDto> getCommentResponseDtos(List<Comment> comments) {
        List<Integer> commentIds = comments.stream().map(Comment::getId).toList();
        Set<Integer> authorIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Integer, Integer> countRepliesByParentCommentIds = this.mapRepliesCountToCommentIds(commentIds);
        Map<Integer, String> authorIdsToNames = this.mapAuthorNameToAuthorIds(authorIds);
        List<CommentResponseDto> responseDtos;
        if (comments.isEmpty()) {
            return List.of();
        }
        responseDtos = mapCommentListToDtoList(comments, authorIdsToNames, countRepliesByParentCommentIds);
        return responseDtos;
    }
}