package com.webdev.bloggingsystem.blog;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        model.addAttribute("postsHeading", "All Posts");    // used for dynamic heading of 'all-posts' fragment with category filter
        model.addAttribute("categories", categoryDao.findAll()); // Full Category with description
    }

    // called when requesting main blog template
    @GetMapping
    public FragmentsRendering blog(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest,
                         @RequestParam(value = "pageNumber", defaultValue = "1", required = false) int pageNumber) {

        // will need count to show how many pages are available when I implement that part.
        // int count = blogEntryDao.count();
        this.populateBlogDashboardModel(model, pageNumber);

        if (!htmxRequest.isHtmxRequest()) {
            // for refresh or direct to /blog, contains heading fragment with nav, css/js, and blog-main fragment with all-posts
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
                .fragment("blog::blog-main")
                .build();
    }

    // "/blog/{id}" only to be used by HTMX internal navigation
    @HxRequest
    @GetMapping("/{id}")
    public FragmentsRendering blogViewFromId(Model model, @PathVariable Integer id) {
        FullBlogEntryDto entryDto = blogEntryService.readPostById(id);
        model.addAttribute("entry", entryDto);
        model.addAttribute("test", "from id: " + id);

        return FragmentsRendering
                .fragment("single-post::single-post")
                .header("HX-Push-Url", "/blog/"+entryDto.slug())
                .build();
    }

    // "/blog/{slug}" to be used by external links, direct url, refresh
    @GetMapping("/{slug}")
    public FragmentsRendering blogViewFromSlug(Model model, @PathVariable String slug) {
        FullBlogEntryDto entryDto = blogEntryService.readPostBySlug(slug);
        model.addAttribute("entry", entryDto);
        model.addAttribute("test", "from slug: " + slug);

        return FragmentsRendering
                .fragment("single-post")
                .build();
    }

    // called when requesting blog entry input form template
    @GetMapping("/createPost")
    public FragmentsRendering createPostView(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        this.populateCreatePostModel(model, new CreateBlogEntryDto());
        if (!htmxRequest.isHtmxRequest()) {
            // for refresh or direct to /blog/createPost
            return FragmentsRendering
                    .fragment("create-post")
                    .build();
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/createPost")) {
            htmxResponse.setPushUrl("/blog/createPost");
        }

        return FragmentsRendering
                .fragment("create-post::create-post")
                .build();
    }

    // called when submitting a new blog entry
    @PostMapping("/createPost")
    public FragmentsRendering createPost(@Valid @ModelAttribute("post") CreateBlogEntryDto createBlogEntryDto,
                                         BindingResult result, Model model) {

        if (result.hasErrors()) {
            this.populateCreatePostModel(model, createBlogEntryDto);
            return FragmentsRendering
                    .fragment("create-post::create-post")
                    .build();
        }

        blogEntryService.createPost(createBlogEntryDto);

        this.populateBlogDashboardModel(model, 1);
        return FragmentsRendering
                .fragment("blog::blog-main")
                .header("HX-Push-Url", "/blog")
                .build();
    }


    // called when filtering posts by category
    @HxRequest
    @GetMapping("/blogComponent/posts")
    public FragmentsRendering posts(Model model,
                        @RequestParam(value = "category", defaultValue = "All", required = false) String categoryName,
                        @RequestParam(value = "pageNumber", defaultValue = "1", required = false) Integer pageNumber) {

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
