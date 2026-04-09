package com.webdev.bloggingsystem.comment;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentDao commentDao;

    public CommentService(CommentDao commentDao) {
        this.commentDao = commentDao;
    }

    public List<Comment> getParentCommentsByPostId(Integer entryId) {
        return commentDao.getParentCommentsByPostId(entryId);
    }

    public List<Comment> getReplyCommentsByParentId(Integer parentId) {
        return commentDao.getReplyCommentsByParentId(parentId);
    }


}
