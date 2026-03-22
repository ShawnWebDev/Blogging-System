package com.webdev.bloggingsystem.blog;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final BlogService blogService;

    public CategoryController(BlogService blogEntryService) {
        this.blogService = blogEntryService;
    }


    @GetMapping
    public String editCategoriesView(Model model, HtmxRequest htmxRequest, HtmxResponse htmxResponse) {
        model.addAttribute("categories", blogService.findAllCategories());

        if (!htmxRequest.isHtmxRequest()) {
            model.addAttribute("fromBlog", true);
            return "edit-categories";
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/categories")) {
            htmxResponse.setPushUrl("/categories");
        }
        return "edit-categories::edit-categories";
    }

    @HxRequest
    @GetMapping("/edit")
    public String editCategoryFormView(Model model, @RequestParam Integer id) {
        model.addAttribute("category", blogService.findCategoryById(id));
        return "components/categories-components::edit-category-card";
    }

    @HxRequest
    @GetMapping("/cancelEdit")
    public String cancelEdit(Model model, @RequestParam Integer id) {
        model.addAttribute("category", blogService.findCategoryById(id));
        return "components/categories-components::category-card";
    }

    @HxRequest
    @PostMapping("/update")
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("category", category);
            return "components/categories-components::edit-category-card";
        }

        blogService.updateCategory(category);

        model.addAttribute("category", blogService.findCategoryById(category.getId()));
        return "components/categories-components::category-card";
    }

    @HxRequest
    @DeleteMapping("/delete")
    public String deleteCategory(Model model, @RequestParam Integer id) {
        blogService.deleteCategoryById(id);
        model.addAttribute("categories", blogService.findAllCategories());
        return "edit-categories::edit-categories";
    }

}
