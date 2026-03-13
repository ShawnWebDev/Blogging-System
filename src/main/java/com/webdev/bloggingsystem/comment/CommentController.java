package com.webdev.bloggingsystem.comment;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
public class CommentController {



    // ** move to comment controller **
    @HxRequest
    @GetMapping("/blogComponent/commentForm")
    public FragmentsRendering commentForm() {
        return FragmentsRendering
                .fragment("components/comment-components::comment-form-enabled")
                .build();
    }

    @HxRequest
    @GetMapping("/blogComponent/removeCommentForm")
    public FragmentsRendering removeCommentForm() {
        return FragmentsRendering
                .fragment("components/comment-components::comment-form-disabled")
                .build();
    }
}
