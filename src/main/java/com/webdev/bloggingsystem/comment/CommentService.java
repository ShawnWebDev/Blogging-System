package com.webdev.bloggingsystem.comment;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;
import com.webdev.bloggingsystem.user.AppUserDao;
import com.webdev.bloggingsystem.user.AuthorDto;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CommentService {
    private final CommentDao commentDao;
    private final AppUserDao appUserDao;

    CommentService(CommentDao commentDao, AppUserDao appUserDao) {
        this.commentDao = commentDao;
        this.appUserDao = appUserDao;
    }

    List<Comment> getParentCommentsByPostId(Integer postId) {
        return commentDao.getParentCommentsByPostId(postId);
    }

    List<Comment> getReplyCommentsByParentId(Integer parentId, Integer postId) {
        return commentDao.getReplyCommentsByParentId(parentId, postId);
    }

    String getCommentContentByCommentId(Integer commentId) {
        return commentDao.getCommentContentByCommentId(commentId).orElse("");
    }

    // todo : integration tests.
    // returns comment for UI so not to fetch entire list again.
    Comment saveComment(CreateCommentDto dto, String username) {
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
        Integer commentId = dto.getCommentId();
        Comment commentToEdit = commentDao.getCommentById(commentId)
                .orElseThrow(() -> new BlogEntryException("Comment not found with id: " + commentId));

        this.validateAuthor(username, commentToEdit.getAuthor().id());
        commentToEdit.setContent(dto.content);

        // only updates content
        int isUpdated = commentDao.update(commentToEdit.getId(), commentToEdit.getContent());

        if (isUpdated == 1) {
            // if isUpdated, then comment exists, do not need to fetch again as content is updated with original object.
            return commentToEdit;
        } else  {
            throw new BlogEntryException("Comment not updated.");
        }
    }

    Comment deleteComment(Integer commentId) {
        // soft delete setting "deleted by Username or ADMIN"
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BlogEntryException("Please log in.");
        //these fields in auth cannot be null at this point. Security will filter non-authenticated user out.
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ADMIN"));

        Comment commentToEdit = commentDao.getCommentById(commentId)
                .orElseThrow(() -> new BlogEntryException("Comment not found with id: " + commentId));

        int isUpdated = 0;
        boolean isHardDelete = false;
        int id = commentToEdit.getId();
        if (!commentToEdit.getContent().equals("Comment deleted.") &&
                (isAdmin || this.validateAuthor(username, commentToEdit.getAuthor().id()))) {
            if (commentDao.hasReplies(id)) {
                isUpdated = commentDao.softDelete(id);
            } else {
                isUpdated = commentDao.hardDelete(id);
                isHardDelete = true;
            }
        }

        if (isUpdated == 1 && isHardDelete) {
            return null;
        } else if (isUpdated == 1) {
            return commentDao.getCommentById(commentToEdit.getId())
                    .orElseThrow(() -> new BlogEntryException("Comment not found with id: " + commentToEdit.getId()));
        } else  {
            throw new BlogEntryException("Comment not deleted.");
        }
    }

    String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

    boolean validateCommentInEntry(Integer commentId, Integer entryId) {
        return commentDao.existsCommentByIdInEntry(commentId, entryId);
    }

    boolean validateAuthor(String username, Integer commentAuthorId) {
        AuthorDto author = appUserDao.findAuthorByUsername(username)
                .orElseThrow(() -> new BlogEntryException("Please login to comment."));

        if (!commentAuthorId.equals(author.id())) {
            throw new BlogEntryException("Not the author of this comment.");
        }
        return true;
    }

}
