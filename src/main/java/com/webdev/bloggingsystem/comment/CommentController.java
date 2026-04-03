package com.webdev.bloggingsystem.comment;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentDao commentDao;


    @HxRequest
    @GetMapping("/commentComponent/commentForm")
    public String commentForm() {
        return "components/comment-components::comment-form-enabled";
    }

    @GetMapping("/all/{entryId}")
    public String allParentComments(Model model, @PathVariable Integer entryId) {
        model.addAttribute("comments", commentDao.getParentCommentsByPostId(entryId));
        return "components/comment-components::comment-container";
    }

}
