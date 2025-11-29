package com.webdev.bloggingsystem.services;

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

    public CommentServiceImpl(CommentRepo commentRepo, AppUserRepo appUserRepo, BlogEntryRepo blogEntryRepo) {
        this.commentRepo = commentRepo;
        this.appUserRepo = appUserRepo;
        this.blogEntryRepo = blogEntryRepo;
    }

    @Override
    public List<CommentResponseDto> getAllRepliesByParentId(Integer parentId) {
        List<Comment> comments = commentRepo.findAllByParentCommentId(parentId);
        return this.getCommentResponseDtos(comments);
    }

    @Override
    public List<CommentResponseDto> getAllCommentsByUsername(String principalName) {
        List<Comment> comments = commentRepo.findAllByAuthorUsername(principalName);
        return this.getCommentResponseDtos(comments);
    }

    @Override
    public List<CommentResponseDto> getAllCommentsByBlogEntryId(Integer blogEntryId) {
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

        Comment comment = commentRepo.findCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

        Integer amount = commentRepo.countRepliesByParentCommentId(commentId);
        logger.debug("getCommentById: reply count: {}", amount);

        return mapRequestToDto(comment, amount);
    }

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
            parentComment = commentRepo.findCommentById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + parentId));
        }

        Comment savedComment = commentRepo.save(mapRequestToEntity(commentText, author, blogEntry, parentComment));

        return ucb.path("api/comments/comment/{commentId}").buildAndExpand(savedComment.getId()).toUri();
    }

    @Override
    public void updateComment(String newCommentText, Integer commentId, String principalName) {
        Comment commentToUpdate = commentRepo.findCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

        if (!commentToUpdate.getAuthor().getUsername().equals(principalName)) {
            throw new ResourceNotFoundException("Comment not found with id " + commentId);
        }
        commentToUpdate.setComment(newCommentText);
        commentRepo.save(commentToUpdate);
    }

    @Override
    public void deleteComment(Integer commentId, String principalName) {
        logger.debug("deleteComment: getting Comment with author and BlogEntry with author {}", principalName);
        Comment commentToDelete = commentRepo.findBlogEntryAndCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

        logger.debug("deleteComment: deleting comment {} with author {} from entry {} with author {}",
                commentToDelete, commentToDelete.getAuthor().getUsername(), commentToDelete.getBlogEntry(), commentToDelete.getBlogEntry().getAuthor().getUsername());

        boolean authorized = false;
        String commentText = "Comment Removed By ";
        if (commentToDelete.getAuthor().getUsername().equals(principalName)) {
            authorized = true;
            commentText += "Comment Author..";
        } else if (commentToDelete.getBlogEntry().getAuthor().getUsername().equals(principalName)) {
            authorized = true;
            commentText += "Blog Author..";
        }

        if (authorized) {
            commentToDelete.setComment(commentText);
            commentRepo.save(commentToDelete);
            return;
        }

        logger.debug("deleteComment: comment not found with Comment author or BlogEntry author {}", principalName);
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
                comment.getParentComment() == null ? null : comment.getParentComment().getId(),
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
                    mapRequestToDto(comment, replyCountMap.getOrDefault(comment.getId(), 0))
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

        if (!comments.isEmpty()) {
            responseDtos = mapCommentListToDtoList(comments, countRepliesByParentCommentIds);
            return responseDtos;
        }

        return List.of();
    }
}
