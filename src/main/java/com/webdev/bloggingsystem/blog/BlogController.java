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

    private final BlogEntryService blogEntryService;

    public BlogController(BlogEntryService blogEntryService) {
        this.blogEntryService = blogEntryService;
    }

    private void populateCreatePostModel(Model model, CreateBlogEntryDto post, boolean isEditing) {
        model.addAttribute("categoryList", blogEntryService.findAllSimpleCategories());
        model.addAttribute("blockTypes", BlockType.values());
        model.addAttribute("post", post);
        model.addAttribute("isEditing", isEditing);
        model.addAttribute("title", isEditing ? "Edit Post | Shawn Osborne" : "Create Post | Shawn Osborne");
    }

    private void populateBlogDashboardModel(Model model, String categoryName, String categoryDescription) {
        model.addAttribute("title", "Blog | Shawn Osborne");
        model.addAttribute("categoryName", categoryName);    // used for dynamic heading of 'all-posts' fragment with category filter
        model.addAttribute("categoryDesc", categoryDescription);
    }

    private void populateSinglePostModel(Model model, FullBlogEntryDto entryDto) {
        model.addAttribute("entry", entryDto);
        model.addAttribute("fromBlog", true);
        model.addAttribute("title", entryDto.slug());
    }

    // called when requesting main blog dashboard
    @GetMapping
    public FragmentsRendering blog(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest,
                        @RequestParam(value = "pageNumber", defaultValue = "1", required = false) int pageNumber,
                        @RequestParam(required = false) String logout) {

        // will need count to show how many pages are available when I implement that part.
        // int count = blogEntryDao.count();
        populateBlogDashboardModel(model, "All", "Select a category to filter.");
        model.addAttribute("posts", blogEntryService.findAllSimpleBlogEntries(pageNumber, PAGE_SIZE));
        model.addAttribute("categories", blogEntryService.findAllCategories());

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

    // called when filtering posts by category in main blog dashboard
    @HxRequest
    @GetMapping("/blogComponent/posts")
    public FragmentsRendering posts(Model model,
                                    @RequestParam(value = "categoryName", defaultValue = "All", required = false) String categoryName,
                                    @RequestParam(value = "pageNumber", defaultValue = "1", required = false) Integer pageNumber) {

        List<SimpleBlogEntryDto> filteredBlogEntries;
        String categoryDescription;

        if (categoryName.equals("All")) {
            filteredBlogEntries = blogEntryService.findAllSimpleBlogEntries(pageNumber, PAGE_SIZE);
            categoryDescription = "Select a category to filter.";
        } else {
            filteredBlogEntries = blogEntryService.findAllSimpleBlogEntriesFiltered(categoryName, pageNumber, PAGE_SIZE);
            categoryDescription = blogEntryService.findCategoryDescriptionByName(categoryName);
        }

        populateBlogDashboardModel(model, categoryName, categoryDescription);
        model.addAttribute("posts", filteredBlogEntries);

        return FragmentsRendering
                .fragment("components/blog-components::all-posts")
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
        populateCreatePostModel(model, new CreateBlogEntryDto(), false);

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
            populateCreatePostModel(model, createBlogEntryDto, false);

            return FragmentsRendering
                    .fragment("create-post::create-post")
                    .build();
        }

        int blogId = blogEntryService.createPost(createBlogEntryDto);

        return ResponseEntity.ok()
                .header("HX-Location", "{\"path\":\"/blog/post/id/"+blogId+"\", \"target\":\"#main-content\", \"swap\":\"outerHTML\"}")
                .build();
    }

    // load input form fragment for edits
    @GetMapping("/post/editPost/{id}")
    public FragmentsRendering editPost(Model model, @PathVariable Integer id,
                                       HtmxRequest htmxRequest) {
        CreateBlogEntryDto dto = blogEntryService.buildCreateDto(id);
        populateCreatePostModel(model, dto, true);

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
            this.populateCreatePostModel(model, createBlogEntryDto, true);

            return FragmentsRendering
                    .fragment("create-post::create-post")
                    .build();
        }
        int blogId = blogEntryService.updatePost(createBlogEntryDto);

        return ResponseEntity.ok()
                .header("HX-Location", "{\"path\":\"/blog/post/id/"+blogId+"\", \"target\":\"#main-content\", \"swap\":\"outerHTML\"}")
                .build();
    }

    @HxRequest
    @DeleteMapping("/post/deletePost/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        logger.info("Deleting post with id: {}", id);
        blogEntryService.deletePost(id);

        return ResponseEntity.ok()
                .header("HX-Location", "{\"path\":\"/blog\", \"target\":\"#main-content\", \"swap\":\"outerHTML\"}")
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
