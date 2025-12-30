package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.UserProfile;
import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.Comment;
import com.webdev.bloggingsystem.dto.CommentResponseDto;
import com.webdev.bloggingsystem.exceptions.ResourceNotFoundException;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import com.webdev.bloggingsystem.repositories.BlogEntryRepo;
import com.webdev.bloggingsystem.repositories.CommentRepo;

import jakarta.transaction.Transactional;
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
    public List<CommentResponseDto> getAllRepliesByParentId(int parentId) {
        List<Comment> comments = commentRepo.findAllByParentCommentId(parentId);

        if (comments.isEmpty()) {
            return List.of();
        }
        return this.getCommentResponseDtos(comments);
    }

    @Override
    public List<CommentResponseDto> getAllUsersComments() {
        // security chain prevents unauthorized user from accessing the calling endpoint
        UserProfile userProfile = authService.getUserProfile();
        int authorId = this.getUserIdByUsernameOrThrowNotFound(userProfile.username());

        List<Comment> comments = commentRepo.findAllByAuthorId(authorId);
        if (comments.isEmpty()) {
            return List.of();
        }

        return this.getCommentResponseDtos(comments);
    }

    @Override
    public List<CommentResponseDto> getAllTopLevelCommentsByBlogEntryId(int blogEntryId) {
        List<Comment> comments = commentRepo.fetchTopLevelCommentsByBlogEntryId(blogEntryId);

        if (comments.isEmpty()) {
            return List.of();
        }

        return this.getCommentResponseDtos(comments);
    }

    @Override
    public CommentResponseDto getCommentById(int commentId) {
        Comment comment = this.getCommentByIdOrThrowNotFound(commentId);
        String authorName = this.getUsernameByIdOrThrowNotFound(comment.getAuthorId());

        Integer amount = commentRepo.countRepliesByParentCommentId(commentId);
        int replyCount = amount != null ? amount : 0;

        return mapRequestToDto(comment, authorName, replyCount);
    }

    @Transactional
    @Override
    public Map.Entry<URI, CommentResponseDto> saveComment(String commentText, int postId, Integer parentId, UriComponentsBuilder ucb) {
        BlogEntry blogEntry = blogEntryRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + postId));

        if (!blogEntry.isPublic()) {
            throw new ResourceNotFoundException("Entry not found with id " + postId);
        }
        //Comment parentComment = null;
        if (parentId != null && !commentRepo.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent comment not found with id " + parentId);
        }

        UserProfile userProfile = authService.getUserProfile();
        int authorId = this.getUserIdByUsernameOrThrowNotFound(userProfile.username());

        Comment savedComment = commentRepo.save(mapRequestToEntity(commentText, authorId, postId, parentId));
        URI uri = ucb.path("api/comments/comment/{commentId}").buildAndExpand(savedComment.getId()).toUri();

        return Map.entry(uri, mapRequestToDto(savedComment, userProfile.username(), 0));
    }

    @Transactional
    @Override
    public CommentResponseDto updateComment(String newCommentText, int commentId) {
        Comment commentToUpdate = this.getCommentByIdOrThrowNotFound(commentId);

        UserProfile userProfile = authService.getUserProfile();
        String authorUsername = this.getUsernameByIdOrThrowNotFound(commentToUpdate.getAuthorId());

        if (!userProfile.username().equals(authorUsername)) {
            throw new ResourceNotFoundException("Comment not found with id " + commentId);
        }
        Integer amount = commentRepo.countRepliesByParentCommentId(commentId);
        int replyCount = amount != null ? amount : 0;
        commentToUpdate.setComment(newCommentText);
        commentRepo.save(commentToUpdate);
        return mapRequestToDto(commentToUpdate, authorUsername, replyCount);
    }

    @Transactional
    @Override
    public void deleteComment(int commentId) {
        Comment commentToDelete = this.getCommentByIdOrThrowNotFound(commentId);

        UserProfile userProfile = authService.getUserProfile();
        int principalId = this.getUserIdByUsernameOrThrowNotFound(userProfile.username());
        int blogAuthorId = blogEntryRepo.findAuthorIdByPostId(commentToDelete.getBlogEntryId())
                .orElse(0);

        boolean authorized = false;
        String commentText = "Comment Removed By ";
        if (commentToDelete.getAuthorId() ==  principalId) {
            authorized = true;
            commentText += "Comment Author..";
        } else if (blogAuthorId == principalId) {
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

    private int getUserIdByUsernameOrThrowNotFound(String username) {
        return appUserRepo.findIdByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Username not found"));
    }

    private String getUsernameByIdOrThrowNotFound(int id) {
        return appUserRepo.findUsernameById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User id not found"));
    }

    private Comment getCommentByIdOrThrowNotFound(int commentId) {
        return commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));
    }

    private static Comment mapRequestToEntity(String commentText, int authorId, int blogEntryId, Integer parentCommentId) {
        Comment comment = new Comment(commentText, authorId, blogEntryId);
        if (parentCommentId != null) {
            comment.setParentCommentId(parentCommentId);
        }
        return comment;
    }

    private static CommentResponseDto mapRequestToDto(Comment comment, String authorName, int amount) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getBlogEntryId(),
                comment.getParentCommentId(),
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
                    mapRequestToDto(
                            comment,
                            authorIdsToNames.get(comment.getAuthorId()),
                            replyCountMap.getOrDefault(comment.getId(), 0)
                    )
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
        if (comments.isEmpty()) {
            return List.of();
        }

        List<Integer> commentIds = comments.stream().map(Comment::getId).toList();
        Set<Integer> authorIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Integer, Integer> countRepliesByParentCommentIds = this.mapRepliesCountToCommentIds(commentIds);
        Map<Integer, String> authorIdsToNames = this.mapAuthorNameToAuthorIds(authorIds);

        return mapCommentListToDtoList(comments, authorIdsToNames, countRepliesByParentCommentIds);
    }
}