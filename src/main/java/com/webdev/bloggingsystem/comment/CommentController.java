package com.webdev.bloggingsystem.comment;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.FragmentsRendering;


@Controller
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

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
    @GetMapping("/all")
    public String allParentComments(Model model, @RequestParam Integer entryId) {
        model.addAttribute("comments", commentService.getParentCommentsByPostId(entryId));

        return "components/comment-components::comment-container";
    }

    @HxRequest
    @GetMapping("/replies")
    public String replyComments(Model model, @RequestParam Integer parentId, @RequestParam Integer entryId) {
        model.addAttribute("replyComments", commentService.getReplyCommentsByParentId(parentId, entryId));
        model.addAttribute("parentId", parentId);
        return "components/comment-components::comment-replies";
    }

    @HxRequest
    @PostMapping("/createComment")
    public Object createComment(Model model,
                                @Valid @ModelAttribute("commentDto") CreateCommentDto commentDto, BindingResult result) {

        model.addAttribute("isReply", false);

        String username = commentService.getUsername();
        boolean hasErrors = false;

        if (username == null) {
            hasErrors = true;
            model.addAttribute("noAuth", true);
        }
        if (hasErrors || result.hasErrors()) {
            model.addAttribute("commentDto", commentDto);
            return "components/comment-components::comment-form-enabled";
        }

        Comment savedComment = commentService.saveComment(commentDto, username);

        CreateCommentDto freshDto = new CreateCommentDto();
        freshDto.setEntryId(savedComment.getBlogEntryId());

        model.addAttribute("commentItem", savedComment);
        model.addAttribute("commentDto", freshDto);

        return FragmentsRendering
                .fragment("components/comment-components::comment-form-enabled")
                .fragment("components/comment-components::single-comment-oob")
                .build();
    }
    // reply form is available without authenticating. So username check is needed.
    @HxRequest
    @PostMapping("/createReply")
    public Object createReply(Model model,
                              @Valid @ModelAttribute("commentDto") CreateCommentDto commentDto, BindingResult result) {

        model.addAttribute("isReply", true);

        boolean hasErrors = false;
        boolean commentValidation = commentService.validateCommentInEntry(commentDto.parentCommentId, commentDto.entryId);
        String username = commentService.getUsername();

        if (username == null) {
            hasErrors = true;
            model.addAttribute("noAuth", true);
        }
        if (commentDto.parentCommentId != null && !commentValidation) {
            hasErrors = true;
            model.addAttribute("noComment", "Ids do not match!");
        }
        if (hasErrors || result.hasErrors()) {
            model.addAttribute("commentDto", commentDto);
            return "components/comment-components::comment-form-enabled";
        }

        model.addAttribute("commentItem", commentService.saveComment(commentDto, username));

        return FragmentsRendering
                .fragment("components/comment-components::single-reply-oob")
                .header("HX-Reswap", "delete")
                .build();
    }

    @HxRequest
    @PostMapping("/editComment")
    public Object editComment(Model model,
                            @Valid @ModelAttribute("commentDto") CreateCommentDto commentDto, BindingResult result) {

        boolean hasErrors = false;
        boolean commentValidation = commentService.validateCommentInEntry(commentDto.commentId, commentDto.entryId);

        if (!commentValidation) {
            hasErrors = true;
            model.addAttribute("noComment", "Ids do not match!");
        }
        if (commentDto.getContent().equals("Comment deleted.")) {
            hasErrors = true;
            model.addAttribute("badEdit", "You must change the deleted comment.");
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

    @HxRequest
    @DeleteMapping("/delete")
    public Object deleteComment(Model model, @RequestParam Integer entryId, @RequestParam Integer commentId,
                                HtmxResponse htmxResponse) {
        boolean commentValidation = commentService.validateCommentInEntry(commentId, entryId);
        if (!commentValidation) {
            throw new BlogEntryException("Ids do not match!");
        };

        Comment deletedComment = commentService.deleteComment(commentId);
        if (deletedComment == null) {
            htmxResponse.setRetarget("#comment-" + commentId);
            htmxResponse.setReswap(HtmxReswap.delete());
            return ResponseEntity.ok().build();
        }

        model.addAttribute("commentItem", deletedComment);

        return FragmentsRendering
                .fragment("components/comment-components::edit-single-comment-content-oob")
                .fragment("components/comment-components::edit-single-comment-time-oob")
                .build();
        }

}
