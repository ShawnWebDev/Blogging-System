package com.webdev.bloggingsystem.comment;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;
import com.webdev.bloggingsystem.user.AppUserDao;
import com.webdev.bloggingsystem.user.AuthorDto;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentDao commentDao;
    private final AppUserDao appUserDao;

    CommentService(CommentDao commentDao, AppUserDao appUserDao) {
        this.commentDao = commentDao;
        this.appUserDao = appUserDao;
    }

    // todo : unit test

    List<Comment> getParentCommentsByPostId(Integer entryId) {
        return commentDao.getParentCommentsByPostId(entryId);
    }

    List<Comment> getReplyCommentsByParentId(Integer parentId) {
        return commentDao.getReplyCommentsByParentId(parentId);
    }

    // todo : integration test insert and get by id.
    // returns comment for UI so not to fetch entire list again.
    Comment saveComment(CreateCommentDto dto) {
        String username = getUsername();
        AuthorDto author = appUserDao.findAuthorByUsername(username)
                .orElseThrow(() -> new BlogEntryException("Please login to comment."));

        Comment comment = new Comment(
                dto.content,
                author,
                dto.entryId
        );
        if (dto.getParentCommentId() != null) {
            comment.setParentCommentId(dto.getParentCommentId());
        }
        int savedId = commentDao.insert(comment);

        return commentDao.getCommentById(savedId);
    }

/*
    Comment updateComment() {}

    public void deleteComment(Integer commentId) {
        // soft delete setting "deleted by Username or ADMIN"
    }*/

    static String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

}
