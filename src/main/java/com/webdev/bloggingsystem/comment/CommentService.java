package com.webdev.bloggingsystem.comment;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;
import com.webdev.bloggingsystem.user.AppUserDao;
import com.webdev.bloggingsystem.user.AuthorDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentDao commentDao;
    private final AppUserDao appUserDao;

    public CommentService(CommentDao commentDao, AppUserDao appUserDao) {
        this.commentDao = commentDao;
        this.appUserDao = appUserDao;
    }

    public List<Comment> getParentCommentsByPostId(Integer entryId) {
        return commentDao.getParentCommentsByPostId(entryId);
    }

    public List<Comment> getReplyCommentsByParentId(Integer parentId) {
        return commentDao.getReplyCommentsByParentId(parentId);
    }


    // todo : test insert and get by id.
    // returns comment for UI so not to fetch entire list again.
    public Comment saveComment(CreateCommentDto dto) {
        String username = getUsername();
        AuthorDto author = appUserDao.findAuthorByUsername(username)
                .orElseThrow(() -> new BlogEntryException("User with name " + username + " not found!"));

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
    public Comment updateComment() {}

    public void deleteComment(Integer commentId) {
        // soft delete setting "deleted by Username or ADMIN"
    }*/

    public static String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return auth.getName();
    }

}
