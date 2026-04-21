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
    public String commentForm(Model model,
                              @RequestParam Integer entryId, @RequestParam(required = false) Integer parentCommentId) {
        CreateCommentDto dto = new CreateCommentDto();
        dto.setEntryId(entryId);

        if (parentCommentId != null) {
            dto.setParentCommentId(parentCommentId);
        }
        model.addAttribute("commentDto", dto);

        return "components/comment-components::comment-form-enabled";
    }

    @HxRequest
    @GetMapping("/commentComponent/editForm")
    public String editCommentForm(Model model,
                                  @RequestParam Integer entryId, @RequestParam Integer commentId) {
        String content = commentService.getCommentContentByCommentId(commentId);
        CreateCommentDto dto = new CreateCommentDto();
        dto.setContent(content);
        dto.setEntryId(entryId);
        dto.setCommentId(commentId);

        model.addAttribute("commentDto", dto);

        return "components/comment-components::edit-comment-form";
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

        return "components/comment-components::comment-replies";
    }

    @HxRequest
    @PostMapping("/createComment")
    public Object createComment(Model model,
                                @Valid @ModelAttribute("commentDto") CreateCommentDto commentDto, BindingResult result) {

        model.addAttribute("isReply", false);

        if (result.hasErrors()) {
            model.addAttribute("commentDto", commentDto);
            return "components/comment-components::comment-form-enabled";
        }

        model.addAttribute("commentItem", commentService.saveComment(commentDto));
        model.addAttribute("commentDto", new CreateCommentDto());

        return FragmentsRendering
                .fragment("components/comment-components::comment-form-enabled")
                .fragment("components/comment-components::single-comment-oob")
                .build();
    }

    @HxRequest
    @PostMapping("/createReply")
    public Object createReply(Model model,
                              @Valid @ModelAttribute("commentDto") CreateCommentDto commentDto, BindingResult result) {

        model.addAttribute("isReply", true);

        boolean hasErrors = false;
        String commentValidation = commentService.validateCommentInEntry(commentDto.parentCommentId, commentDto.entryId);

        if (commentService.getUsername() == null) {
            hasErrors = true;
            model.addAttribute("noAuth", true);
        }
        if (commentDto.parentCommentId != null && !commentValidation.isEmpty()) {
            hasErrors = true;
            model.addAttribute("noComment", commentValidation);
        }
        if (hasErrors || result.hasErrors()) {
            model.addAttribute("commentDto", commentDto);
            return "components/comment-components::comment-form-enabled";
        }

        model.addAttribute("commentItem", commentService.saveComment(commentDto));

        return FragmentsRendering
                .fragment("components/comment-components::single-reply-oob")
                .header("HX-Reswap", "delete")
                .build();
    }

    // todo: Error handling, author access validation? validate commentId with EntryId?
    @HxRequest
    @PostMapping("/editComment")
    public Object editComment(Model model,
                            @Valid @ModelAttribute("commentDto") CreateCommentDto commentDto, BindingResult result) {

        boolean hasErrors = false;
        String commentValidation = commentService.validateCommentInEntry(commentDto.commentId, commentDto.entryId);

        if (!commentValidation.isEmpty()) {
            hasErrors = true;
            model.addAttribute("noComment", commentValidation);
        }
        if (hasErrors || result.hasErrors()) {
            model.addAttribute("commentDto", commentDto);
            return "components/comment-components::edit-comment-form";
        }

        // return Comment from service to swap
        model.addAttribute("commentItem", commentService.updateComment(commentDto));

        // HX-Reswap to change the swap target in the form from "this" to "delete" on success
        return FragmentsRendering
                .fragment("components/comment-components::edit-single-comment-content-oob")
                .fragment("components/comment-components::edit-single-comment-time-oob")
                .header("HX-Reswap", "delete")
                .build();
    }

    // todo: add DELETE endpoint for soft delete of comment/reply,
    // todo: ensure only comment author or ADMIN can modify comments (using Principal)
    // todo: add these to template functionality

}
