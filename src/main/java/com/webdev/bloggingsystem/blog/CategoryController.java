package com.webdev.bloggingsystem.blog;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final BlogService blogService;

    public CategoryController(BlogService blogEntryService) {
        this.blogService = blogEntryService;
    }


    @GetMapping
    public FragmentsRendering editCategories(Model model, HtmxRequest htmxRequest, HtmxResponse htmxResponse) {
        model.addAttribute("categories", blogService.findAllCategories());

        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromBlog", true);
            return FragmentsRendering
                    .fragment("edit-categories")
                    .build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/categories")) {
            htmxResponse.setPushUrl("/categories");
        }
        return FragmentsRendering
                .fragment("edit-categories::edit-categories")
                .build();
    }
}
