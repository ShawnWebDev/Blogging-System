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

    private static void populateCreatePostModel(Model model, CreateBlogEntryDto post, List<SimpleCategoryDto> categoryList, boolean isEditing) {
        model.addAttribute("categoryList", categoryList); // SimpleCategoryDto
        model.addAttribute("blockTypes", BlockType.values());
        model.addAttribute("post", post);
        model.addAttribute("isEditing", isEditing);
    }

    private static void populateBlogDashboardModel(Model model, List<SimpleBlogEntryDto> posts, List<Category> categories, String categoryName, String categoryDescription) {
        model.addAttribute("title", "Blog | Shawn Osborne");
        model.addAttribute("posts", posts);
        model.addAttribute("categoryName", categoryName);    // used for dynamic heading of 'all-posts' fragment with category filter
        if (categories != null) model.addAttribute("categories", categories); // Full Category with description
        model.addAttribute("categoryDesc", categoryDescription);
    }

    private static void populateSinglePostModel(Model model, FullBlogEntryDto entryDto) {
        model.addAttribute("entry", entryDto);
        model.addAttribute("fromBlog", true);
        model.addAttribute("title", entryDto.slug());
    }

    // called when requesting main blog template
    @GetMapping
    public FragmentsRendering blog(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest,
                        @RequestParam(value = "pageNumber", defaultValue = "1", required = false) int pageNumber,
                        @RequestParam(required = false) String logout) {

        // will need count to show how many pages are available when I implement that part.
        // int count = blogEntryDao.count();
        List<SimpleBlogEntryDto> posts = blogEntryDao.findAllSimple(pageNumber, PAGE_SIZE);
        List<Category> categories = categoryDao.findAll();
        populateBlogDashboardModel(model, posts, categories, "All", "Select a category to filter.");

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
                .fragment("components/shared-head::head-title")
                .fragment("blog::blog-main")
                .build();
    }

    // "/blog/{id}" only to be used by HTMX internal navigation
    @HxRequest
    @GetMapping("/post/id/{id}")
    public FragmentsRendering blogViewFromId(Model model, @PathVariable Integer id) {
        FullBlogEntryDto entryDto = blogEntryService.readPostById(id);
        populateSinglePostModel(model, entryDto);

        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("single-post::single-post")
                .header("HX-Push-Url", "/blog/post/"+entryDto.slug())
                .build();
    }

    // "/blog/post/{slug}" to be used by external links, direct url, refresh
    @GetMapping("/post/{slug}")
    public FragmentsRendering blogViewFromSlug(Model model, @PathVariable String slug) {
        FullBlogEntryDto entryDto = blogEntryService.readPostBySlug(slug);
        populateSinglePostModel(model, entryDto);

        return FragmentsRendering
                .fragment("single-post")
                .build();
    }

    // called when requesting blog entry input form template
    @GetMapping("/post/createPost")
    public FragmentsRendering createPostView(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        List<SimpleCategoryDto> categoryDtos = categoryDao.findAllNames();
        boolean isEditing = false;
        populateCreatePostModel(model, new CreateBlogEntryDto(), categoryDtos, isEditing);

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
            List<SimpleCategoryDto> categoryDtos = categoryDao.findAllNames();
            boolean isEditing = false;
            populateCreatePostModel(model, createBlogEntryDto, categoryDtos, isEditing);

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
        List<SimpleCategoryDto> categoryDtos = categoryDao.findAllNames();
        boolean isEditing = true;
        populateCreatePostModel(model, dto, categoryDtos, isEditing);

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
        if (result.hasErrors()) {
            List<SimpleCategoryDto> categoryDtos = categoryDao.findAllNames();
            boolean isEditing = true;
            populateCreatePostModel(model, createBlogEntryDto, categoryDtos, isEditing);

            return FragmentsRendering
                    .fragment("create-post::create-post")
                    .build();
        }
        String blogSlug = blogEntryService.updatePost(createBlogEntryDto);

        return ResponseEntity.ok()
                .header("HX-Location", "/blog/post/"+blogSlug)
                .build();
    }

    // todo : finish this endpoint and delete service method.
    @HxRequest
    @DeleteMapping("/post/deletePost/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        logger.info("Deleting post with id: {}", id);

        return ResponseEntity.ok()
                .header("HX-Redirect", "/blog")
                .build();
    }

    // called when filtering posts by category
    @HxRequest
    @GetMapping("/blogComponent/posts")
    public FragmentsRendering posts(Model model,
                        @RequestParam(value = "categoryName", defaultValue = "All", required = false) String categoryName,
                        @RequestParam(value = "pageNumber", defaultValue = "1", required = false) Integer pageNumber) {

        List<SimpleBlogEntryDto> filteredBlogEntries;
        String categoryDescription;

        if (categoryName.equals("All")) {
            filteredBlogEntries = blogEntryDao.findAllSimple(pageNumber, PAGE_SIZE);
            categoryDescription = "Select a category to filter.";
        } else {
            filteredBlogEntries = blogEntryDao.findAllSimpleBlogEntriesToCategoryName(categoryName, pageNumber, PAGE_SIZE);
            categoryDescription = categoryDao.findCategoryDescriptionByName(categoryName);
        }

        populateBlogDashboardModel(model, filteredBlogEntries, null, categoryName, categoryDescription);

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
