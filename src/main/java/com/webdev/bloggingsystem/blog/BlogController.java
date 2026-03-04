package com.webdev.bloggingsystem.blog;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.FragmentsRendering;

import java.util.List;

@Controller
@RequestMapping("/blog")
public class BlogController {
    private static final Logger logger = LoggerFactory.getLogger(BlogController.class);
    private static final int PAGE_SIZE = 10;

    private final BlogEntryDao blogEntryDao;
    private final CategoryDao categoryDao;
    private final BlogEntryService blogEntryService;

    public BlogController(BlogEntryDao blogEntryDao, CategoryDao categoryDao, BlogEntryService blogEntryService) {
        this.blogEntryDao = blogEntryDao;
        this.categoryDao = categoryDao;
        this.blogEntryService = blogEntryService;
    }

    private void populateCreatePostModel(Model model, CreateBlogEntryDto post) {
        model.addAttribute("categoryList", categoryDao.findAllNames()); // SimpleCategoryDto
        model.addAttribute("blockTypes", BlockType.values());
        model.addAttribute("post", post);
    }

    private void populateBlogDashboardModel(Model model, int pageNumber) {
        model.addAttribute("posts", blogEntryDao.findAllSimple(pageNumber, PAGE_SIZE));
        model.addAttribute("categoryName", "All");    // used for dynamic heading of 'all-posts' fragment with category filter
        model.addAttribute("categories", categoryDao.findAll()); // Full Category with description
    }

    // called when requesting main blog template
    @GetMapping
    public FragmentsRendering blog(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest,
                        @RequestParam(value = "pageNumber", defaultValue = "1", required = false) int pageNumber,
                        @RequestParam(required = false) String logout) {

        logger.info("get blog dashboard....");
        // will need count to show how many pages are available when I implement that part.
        // int count = blogEntryDao.count();
        this.populateBlogDashboardModel(model, pageNumber);
        if (logout != null) {
            model.addAttribute("logout", "You have logged out.");
        }

        if (!htmxRequest.isHtmxRequest()) {
            // for refresh or direct to /blog, contains heading fragment with nav, css/js, and blog-main fragment with all-posts
            model.addAttribute("fromBlog", true);
            return FragmentsRendering
                    .fragment("blog")
                    .build();
        }
        // checks if request is from HTMX and the current address is not already set to /blog,
        // then pushes /blog into browser history and address bar.
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/blog")) {
            htmxResponse.setPushUrl("/blog");
        }
        // for htmx request, only needs heading h1 and blog-main with all-posts
        return FragmentsRendering
                .fragment("blog::blog-title")
                .fragment("blog::blog-main")
                .build();
    }

    // "/blog/{id}" only to be used by HTMX internal navigation
    @HxRequest
    @GetMapping("/post/id/{id}")
    public FragmentsRendering blogViewFromId(Model model, @PathVariable Integer id) {
        FullBlogEntryDto entryDto = blogEntryService.readPostById(id);
        model.addAttribute("entry", entryDto);
        model.addAttribute("fromBlog", true);
        return FragmentsRendering
                .fragment("blog::blog-title")
                .fragment("single-post::single-post")
                .header("HX-Push-Url", "/blog/post/"+entryDto.slug())
                .build();
    }

    // "/blog/post/{slug}" to be used by external links, direct url, refresh
    @GetMapping("/post/{slug}")
    public FragmentsRendering blogViewFromSlug(Model model, @PathVariable String slug) {
        FullBlogEntryDto entryDto = blogEntryService.readPostBySlug(slug);
        model.addAttribute("entry", entryDto);
        model.addAttribute("fromBlog", true);
        return FragmentsRendering
                .fragment("single-post")
                .build();
    }

    // called when requesting blog entry input form template
    @GetMapping("/post/createPost")
    public FragmentsRendering createPostView(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        this.populateCreatePostModel(model, new CreateBlogEntryDto());
        if (!htmxRequest.isHtmxRequest()) {
            // for refresh or direct to /blog/createPost
            return FragmentsRendering
                    .fragment("create-post")
                    .build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/createPost")) {
            htmxResponse.setPushUrl("/blog/post/createPost");
        }

        return FragmentsRendering
                .fragment("create-post::create-post")
                .build();
    }

    // called when submitting a new blog entry
    @PostMapping("/post/createPost")
    public Object createPost(@Valid @ModelAttribute("post") CreateBlogEntryDto createBlogEntryDto,
                              BindingResult result, Model model) {

        if (result.hasErrors()) {
            this.populateCreatePostModel(model, createBlogEntryDto);
            return FragmentsRendering
                    .fragment("create-post::create-post")
                    .build();
        }

        String blogSlug = blogEntryService.createPost(createBlogEntryDto);

        return ResponseEntity.ok()
                .header("HX-Location", "/blog/post/"+blogSlug)
                .build();
    }

    // load input form fragment for edits
    @GetMapping("/post/editPost/{id}")
    public FragmentsRendering editPost(Model model, @PathVariable Integer id,
                                       HtmxRequest htmxRequest) {
        CreateBlogEntryDto dto = blogEntryService.buildCreateDto(id);

        this.populateCreatePostModel(model, dto);
        model.addAttribute("isEditing", true);

        if (!htmxRequest.isHtmxRequest()) {
            return FragmentsRendering
                    .fragment("create-post")
                    .build();
        }

        return FragmentsRendering
                .fragment("create-post::create-post")
                .header("HX-Push-Url", "/blog/post/editPost/"+id)
                .build();
    }

    @PostMapping("/post/editPost")
    public Object editPost(@Valid @ModelAttribute("post") CreateBlogEntryDto createBlogEntryDto,
                                       BindingResult result, Model model) {
        logger.info("edit post");
        if (result.hasErrors()) {
            this.populateCreatePostModel(model, createBlogEntryDto);
            return FragmentsRendering
                    .fragment("create-post::create-post")
                    .build();
        }
        String blogSlug = blogEntryService.updatePost(createBlogEntryDto);

        return ResponseEntity.ok()
                .header("HX-Location", "/blog/post/"+blogSlug)
                .build();
    }

    @HxRequest
    @DeleteMapping("/post/deletePost/{id}")
    public String deletePost(Model model, @PathVariable Integer id) {
        logger.info("Deleting post with id: {}", id);
        return "redirect:/blog";
    }


    // called when filtering posts by category
    @HxRequest
    @GetMapping("/blogComponent/posts")
    public FragmentsRendering posts(Model model,
                        @RequestParam(value = "categoryName", defaultValue = "All", required = false) String categoryName,
                        @RequestParam(value = "pageNumber", defaultValue = "1", required = false) Integer pageNumber) {

        List<SimpleBlogEntryDto> filteredBlogEntries;
        String categoryDescription = null;
        if (categoryName.equals("All")) {
            filteredBlogEntries = blogEntryDao.findAllSimple(pageNumber, PAGE_SIZE);
        } else {
            filteredBlogEntries = blogEntryDao.findAllSimpleBlogEntriesToCategoryName(categoryName, pageNumber, PAGE_SIZE);
            categoryDescription = categoryDao.findCategoryDescriptionByName(categoryName);
        }
        model.addAttribute("posts", filteredBlogEntries);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("categoryDesc", categoryDescription);

        return FragmentsRendering
                .fragment("components/blog-components::all-posts")
                .build();
    }

    @HxRequest
    @GetMapping("/postComponent/adminButtons")
    public FragmentsRendering adminButtons(Model model, @RequestParam(value = "id") Integer id) {
        model.addAttribute("id", id);
        return FragmentsRendering
                .fragment("components/post-components::admin-buttons")
                .build();
    }

    // called when adding "content block" to blog entry input form
    @HxRequest
    @GetMapping("/postComponent/addBlock")
    public FragmentsRendering addBlock(Model model,
                                       @RequestParam Integer index) {
        model.addAttribute("index", index);
        model.addAttribute("blockTypes", BlockType.values());
        model.addAttribute("block", new BlogEntryContentBlockDto());
        return FragmentsRendering
                .fragment("components/post-components::content-block")
                .build();
    }

    @HxRequest
    @PostMapping("/postComponent/validateSize")
    public FragmentsRendering validateSize(Model model, @ModelAttribute("post") CreateBlogEntryDto post) {
        int count = blogEntryService.getCurrentByteCount(post.getContentBlocks());
        model.addAttribute("byteCount", count + " / " + BlogEntryService.MAX_BYTES);

        return FragmentsRendering
                .fragment("components/post-components::byte-count")
                .build();
    }



}
