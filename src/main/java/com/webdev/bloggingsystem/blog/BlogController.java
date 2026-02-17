package com.webdev.bloggingsystem.blog;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public FragmentsRendering blog(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest,
                           @RequestParam(value = "pageNumber", defaultValue = "1", required = false) int pageNumber) {

        // will need count to show how many pages are available when I implement that part.
        // int count = blogEntryDao.count();
        List<SimpleBlogEntryDto> blogEntries = blogEntryDao.findAllSimple(pageNumber, PAGE_SIZE);
        List<Category> categories = categoryDao.findAll();
        // checks if request is from HTMX and the current address is not already set to /blog,
        // then pushes /blog into browser history and address bar.
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/blog")) {
            htmxResponse.setPushUrl("/blog");
        }

        model.addAttribute("heading", "Welcome to my blog!");
        model.addAttribute("posts", blogEntries);
        model.addAttribute("postsHeading", "All Posts");    // used for dynamic heading of 'all-posts' fragment with category filter
        model.addAttribute("categories", categories);

        if (htmxRequest.isHtmxRequest()) {
            // for htmx request, only needs heading h1 and blog-main with all-posts
            return FragmentsRendering
                    .fragment("components/header-components::simple-header")
                    .fragment("blog::blog-main")
                    .build();
        }
        // for refresh or direct to /blog, contains heading fragment with nav, css/js, and blog-main fragment with all-posts
        return FragmentsRendering
                .fragment("blog")
                .build();
    }

    @PostMapping("/createPost")
    public String createPost(Model model, @Valid @ModelAttribute("inventoryItem") FullBlogEntryDto fullBlogEntryDto) {
        // redirect or trigger load of created resource
        return "";
    }

    // ** HTMX ONLY REQUESTS **
    @HxRequest
    @GetMapping("/blogComponent/posts")
    public FragmentsRendering posts(Model model,
                        @RequestParam(value = "category", defaultValue = "All", required = false) String categoryName,
                        @RequestParam(value = "pageNumber", defaultValue = "1", required = false) int pageNumber) {

        List<SimpleBlogEntryDto> sortedBlogEntries;
        if (categoryName.equals("All")) {
            sortedBlogEntries = blogEntryDao.findAllSimple(pageNumber, PAGE_SIZE);
        } else {
            sortedBlogEntries = blogEntryDao.findAllSimpleBlogEntriesToCategoryName(categoryName, pageNumber, PAGE_SIZE);
        }
        model.addAttribute("posts", sortedBlogEntries);
        model.addAttribute("postsHeading", categoryName + " Posts");

        return FragmentsRendering
                .fragment("components/blog-components::all-posts")
                .build();
    }

    @HxRequest
    @GetMapping("/blogComponent/createPost")
    public FragmentsRendering createPostView(Model model) {
        return FragmentsRendering
                .fragment("components/post-components::create-post")
                .build();
    }

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
