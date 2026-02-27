package com.webdev.bloggingsystem.comment;

import com.webdev.bloggingsystem.blog.BlogEntryDao;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentDao commentDao;
    private final BlogEntryDao blogEntryDao;

    public CommentService(CommentDao commentDao, BlogEntryDao blogEntryDao) {
        this.commentDao = commentDao;
        this.blogEntryDao = blogEntryDao;
    }


}
