package com.webdev.bloggingsystem.comment;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.FragmentsRendering;


@Controller
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final static Logger logger = LoggerFactory.getLogger(CommentController.class);

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @HxRequest
    @GetMapping("/commentComponent/commentForm")
    public String commentForm(Model model, @RequestParam Integer entryId, @RequestParam(required = false) Integer parentCommentId) {
        model.addAttribute("entryId", entryId);
        model.addAttribute("commentDto", new CreateCommentDto());

        if (parentCommentId != null) {
            model.addAttribute("parentCommentId", parentCommentId);
        }

        return "components/comment-components::comment-form-enabled";
    }

    @HxRequest
    @GetMapping("/all/{entryId}")
    public String allParentComments(Model model, @PathVariable Integer entryId) {
        model.addAttribute("comments", commentService.getParentCommentsByPostId(entryId));

        return "components/comment-components::comment-container";
    }

    @HxRequest
    @GetMapping("/replies/{parentId}")
    public String replyComments(Model model, @PathVariable Integer parentId) {
        model.addAttribute("replyComments", commentService.getReplyCommentsByParentId(parentId));
        model.addAttribute("parentId", parentId);
        return "components/comment-components::comment-replies";
    }

    // todo: add POST endpoint for creating comments and replies,
    // todo: target comment-list or reply-(parent id) when adding new comment to necessary list
    @HxRequest
    @PostMapping("/createComment")
    public FragmentsRendering createComment(Model model,
                                            @Valid @ModelAttribute("commentDto") CreateCommentDto createCommentDto,
                                            BindingResult result) {

        logger.info("createComment called.. dto = {}", createCommentDto);

        model.addAttribute("entryId", createCommentDto.getEntryId());

        if (result.hasErrors()) {
            model.addAttribute("commentDto", createCommentDto);
            return FragmentsRendering
                    .fragment("components/comment-components::comment-form-enabled")
                    .build();
        }
        // for replies, target id: reply-(parent id)..
        /*
        if (createCommentDto.getParentCommentId() != null) {
            model.addAttribute("parentCommentId", createCommentDto.getParentCommentId());
            model.addAttribute("isReply", true);
        }*/

        // for top-level, target id: comment-list
        model.addAttribute("commentItem", commentService.saveComment(createCommentDto));
        model.addAttribute("isReply", false);
        model.addAttribute("commentDto", new CreateCommentDto());

        return FragmentsRendering
                .fragment("components/comment-components::comment-form-enabled")
                .fragment("components/comment-components::single-comment-oob")
                .build();
    }

    // todo: another POST endpoint for updating,
    // todo: add DELETE endpoint for soft delete of comment/reply,
    // todo: ensure only comment author or ADMIN can modify comments (using Principal)
    // todo: add these to template functionality

}
