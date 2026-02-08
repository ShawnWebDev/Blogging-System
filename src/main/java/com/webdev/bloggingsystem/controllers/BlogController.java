package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.entities.Category;
import com.webdev.bloggingsystem.entities.DTO.SimpleBlogEntry;
import com.webdev.bloggingsystem.repositories.BlogEntryDao;
import com.webdev.bloggingsystem.repositories.CategoryDao;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.List;

@Controller
@RequestMapping("/blog")
public class BlogController {

    private static final int PAGE_SIZE = 10;

    private final BlogEntryDao blogEntryDao;
    private final CategoryDao categoryDao;

    public BlogController(BlogEntryDao blogEntryDao, CategoryDao categoryDao) {
        this.blogEntryDao = blogEntryDao;
        this.categoryDao = categoryDao;
    }

    @GetMapping
    public View blog(Model model, HttpServletResponse response,
                     @RequestHeader(value = "HX-Request", required = false) boolean isHtmx,
                     @RequestParam(value = "pageNumber", defaultValue = "1", required = false) int pageNumber) {

        // will need count to show how many pages are available when I implement that part.
        // int count = blogEntryDao.count();
        List<SimpleBlogEntry> blogEntries = blogEntryDao.findAllSimple(pageNumber, PAGE_SIZE);
        List<Category> categories = categoryDao.findAll();

        response.setHeader("HX-Push-Url", "/blog");

        model.addAttribute("heading", "Welcome to my blog!");
        model.addAttribute("posts", blogEntries);
        model.addAttribute("categories", categories);

        if (isHtmx) {
            // for htmx request, only needs heading h1 and blog-main
            return FragmentsRendering.fragment("components/header-components::header").fragment("blog::blog-main").build();
        }
        // for refresh or direct to /blog, index contains heading, nav, and css/js, only needs blog-main
        return FragmentsRendering.fragment("index").fragment("blog::blog-main").build();
    }

    @GetMapping("/blogComponent/commentForm")
    public View commentForm() {
        return FragmentsRendering.fragment("components/comment-components::comment-form-enabled").build();
    }

    @GetMapping("/blogComponent/removeCommentForm")
    public View remove() {
        return FragmentsRendering.fragment("components/comment-components::comment-form-disabled").build();
    }

    @GetMapping("/blogComponent/posts")
    public View posts(Model model, @RequestParam("category") String categoryName,
                        @RequestParam(value = "pageNumber", defaultValue = "1", required = false) int pageNumber) {
        List<SimpleBlogEntry> sortedBlogEntries = blogEntryDao.findAllSimpleBlogEntriesToCategoryName(categoryName, pageNumber, PAGE_SIZE);
        model.addAttribute("posts", sortedBlogEntries);
        return FragmentsRendering.fragment("components/blog-components::all-posts").build();
    }




}
