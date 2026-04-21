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

    List<Comment> getParentCommentsByPostId(Integer entryId) {
        return commentDao.getParentCommentsByPostId(entryId);
    }

    List<Comment> getReplyCommentsByParentId(Integer parentId) {
        return commentDao.getReplyCommentsByParentId(parentId);
    }

    String getCommentContentByCommentId(Integer commentId) {
        return commentDao.getCommentContentByCommentId(commentId).orElse("");
    }

    // todo : integration tests.
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
        // Check comment is a reply and
        if (dto.getParentCommentId() != null) {
            comment.setParentCommentId(dto.getParentCommentId());
        }

        int savedId = commentDao.insert(comment);

        return commentDao.getCommentById(savedId)
                .orElseThrow(() -> new BlogEntryException("Comment not found with id: " + savedId));
    }

    Comment updateComment(CreateCommentDto dto) {
        String username = getUsername();
        AuthorDto author = appUserDao.findAuthorByUsername(username)
                .orElseThrow(() -> new BlogEntryException("Please login to comment."));

        Comment commentToEdit = commentDao.getCommentById(dto.commentId)
                .orElseThrow(() -> new BlogEntryException("Comment not found with id: " + dto.commentId));

        if (!commentToEdit.getAuthor().id().equals(author.id())) {
            throw new BlogEntryException("You are not the author of this comment.");
        }

        commentToEdit.setContent(dto.content);

        int isUpdated = commentDao.update(commentToEdit.getId(), commentToEdit.getContent());

        if (isUpdated == 1) {
            return commentDao.getCommentById(commentToEdit.getId())
                    .orElseThrow(() -> new BlogEntryException("Comment not found with id: " + commentToEdit.getId()));
        } else  {
            throw new BlogEntryException("Comment not updated.");
        }
    }

/*    public void deleteComment(Integer commentId) {
        // soft delete setting "deleted by Username or ADMIN"
    }*/

    String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

    String validateCommentInEntry(Integer commentId, Integer entryId) {
        if (!commentDao.existsCommentByIdInEntry(commentId, entryId)) {
            return "Ids do not match!";
        }
        return "";
    }

}
