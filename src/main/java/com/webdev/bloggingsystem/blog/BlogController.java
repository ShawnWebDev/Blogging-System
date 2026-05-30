package com.webdev.bloggingsystem.blog;

import com.webdev.bloggingsystem.comment.CreateCommentDto;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Controller
@RequestMapping("/blog")
public class BlogController {
    private static final Logger logger = LoggerFactory.getLogger(BlogController.class);

    private final BlogService blogService;

    public BlogController(BlogService blogEntryService) {
        this.blogService = blogEntryService;
    }

    private void populateCreatePostModel(Model model, CreateBlogEntryDto post, boolean isEditing) {
        model.addAttribute("categoryList", blogService.findAllSimpleCategories());
        model.addAttribute("post", post);
        model.addAttribute("isEditing", isEditing);
        model.addAttribute("title", isEditing ? "Edit Post | Shawn Osborne" : "Create Post | Shawn Osborne");
    }

    private void populateBlogDashboardModel(Model model, String categoryName, String categoryDescription) {
        model.addAttribute("title", "Blog | Shawn Osborne");
        model.addAttribute("metaDesc", "A software engineer's personal blog where he talks about technology and life. By Shawn Osborne.");
        model.addAttribute("categoryName", categoryName);    // used for dynamic heading of 'all-posts' fragment with category filter
        model.addAttribute("categoryDesc", categoryDescription);
    }

    private void populateSinglePostModel(Model model, BlogEntry entry) {
        model.addAttribute("entry", entry);
        model.addAttribute("fromBlog", true);
        model.addAttribute("title", entry.getTitle());
        model.addAttribute("metaDesc", entry.getDescription());
        model.addAttribute("commentDto", new CreateCommentDto());
    }

    // called when requesting main blog dashboard
    @GetMapping
    public Object blogDashboardView(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        populateBlogDashboardModel(model, "All", "");
        model.addAttribute("posts", blogService.findAllSimpleBlogEntries());
        model.addAttribute("categories", blogService.findAllSimpleCategories());

        if (!htmxRequest.isHtmxRequest()) {
            // for refresh or direct to /blog, contains heading fragment with nav, css/js, and blog-main fragment with all-posts
            model.addAttribute("fromBlog", true);
            return "blog";
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

    // This is a separate endpoint for security, in progress posts should not be viewable by USER role
    @HxRequest
    @GetMapping("/blogComponent/posts/inProgress")
    public String inProgressPostsView(Model model) {
        populateBlogDashboardModel(model, "In Progress", "");
        model.addAttribute("posts", blogService.findAllSimpleBlogEntriesInProgress());

        return "components/blog-components::all-posts";
    }

    // called when filtering posts by category in main blog dashboard
    @HxRequest
    @GetMapping("/blogComponent/posts")
    public Object filteredPostsView(Model model, HtmxRequest htmxRequest, HtmxResponse htmxResponse,
                                    @RequestParam(value = "categoryName", defaultValue = "All", required = false) String categoryName) {

        List<SimpleBlogEntryDto> filteredBlogEntries;
        String categoryDescription;

        if (categoryName.equals("All")) {
            filteredBlogEntries = blogService.findAllSimpleBlogEntries();
            categoryDescription = "";
        } else {
            filteredBlogEntries = blogService.findAllSimpleBlogEntriesToCategoryName(categoryName);
            categoryDescription = blogService.findCategoryDescriptionByName(categoryName);
        }

        boolean isFromBlog = false;
        String url = htmxRequest.getCurrentUrl();
        try {
            // getPath() strips params to correctly send fragment if /blog, /blog?logout, or /blog?sessionExpired
            isFromBlog = url != null && new URI(url).getPath().equals("/blog");
        } catch (URISyntaxException e) {
            logger.warn("Incorrect referer header: {}. ** 'isFromBlog' is defaulted to false.", url);
        }

        populateBlogDashboardModel(model, categoryName, categoryDescription);
        model.addAttribute("posts", filteredBlogEntries);

        if (isFromBlog) {
            return "components/blog-components::all-posts";
        }

        // if from single-post page: reset url to /blog, fetch category list, and render blog dashboard with "posts" filter applied
        htmxResponse.setPushUrl("/blog");
        model.addAttribute("categories", blogService.findAllSimpleCategories());
        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("blog::blog-main")
                .build();
    }

    // to be used by HTMX navigation
    @HxRequest
    @GetMapping("/post/{id}/{slug}")
    public FragmentsRendering singlePostViewFragment(Model model, @PathVariable Integer id, @PathVariable String slug) {
        BlogEntry entry = blogService.readPost(id);
        this.populateSinglePostModel(model, entry);

        return FragmentsRendering
                .fragment("components/shared-head::head-title")
                .fragment("single-post::single-post")
                .header("HX-Push-Url", "/blog/post/" + id + "/" + entry.getSlug())
                .build();
    }

    // to be used by external links, direct url, refresh
    @GetMapping("/post/{id}/{slug}")
    public String singlePostViewFullPage(Model model, @PathVariable Integer id, @PathVariable String slug) {
        BlogEntry entry = blogService.readPost(id);
        this.populateSinglePostModel(model, entry);

        return "single-post";
    }

    // called when requesting blog entry input form template
    @GetMapping("/post/createPost")
    public String createPostView(Model model, HtmxResponse htmxResponse, HtmxRequest htmxRequest) {
        populateCreatePostModel(model, new CreateBlogEntryDto(), false);

        if (!htmxRequest.isHtmxRequest()) {
            // for refresh or direct to /blog/createPost
            return "create-post";
        }
        if (htmxRequest.getCurrentUrl() == null || !htmxRequest.getCurrentUrl().endsWith("/createPost")) {
            htmxResponse.setPushUrl("/blog/post/createPost");
        }

        return "create-post::create-post";
    }

    // called when submitting a new blog entry
    @PostMapping("/post/createPost")
    public Object createPost(@Valid @ModelAttribute("post") CreateBlogEntryDto createBlogEntryDto,
                              BindingResult result, Model model) {

        if (result.hasErrors()) {
            populateCreatePostModel(model, createBlogEntryDto, false);

            return "create-post::create-post";
        }

        Object[] entryRef = blogService.createPost(createBlogEntryDto);

        return ResponseEntity.ok()
                .header("HX-Location", "{\"path\":\"/blog/post/" + entryRef[0] + "/" + entryRef[1] +"\", \"target\":\"#main-content\", \"swap\":\"outerHTML\"}")
                .build();
    }

    // load input form fragment for edits
    @GetMapping("/post/editPost/{id}")
    public Object editPostView(Model model, @PathVariable Integer id,
                                       HtmxRequest htmxRequest) {
        CreateBlogEntryDto dto = blogService.buildCreateDtoForEdit(id);
        populateCreatePostModel(model, dto, true);

        if (!htmxRequest.isHtmxRequest()) {
            return "create-post";
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

            return "create-post::create-post";
        }

        Object[] entryRef = blogService.updatePost(createBlogEntryDto);

        return ResponseEntity.ok()
                .header("HX-Location", "{\"path\":\"/blog/post/" + entryRef[0] + "/" + entryRef[1] + "\", \"target\":\"#main-content\", \"swap\":\"outerHTML\"}")
                .build();
    }

    @HxRequest
    @DeleteMapping("/post/deletePost/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        logger.info("Deleting post with id: {}", id);
        blogService.deletePost(id);

        return ResponseEntity.ok()
                .header("HX-Location", "{\"path\":\"/blog\", \"target\":\"#main-content\", \"swap\":\"outerHTML\"}")
                .build();
    }

    @HxRequest
    @GetMapping("/postComponent/adminButtons")
    public String adminButtonsFragment(Model model, @RequestParam(value = "id") Integer id) {
        model.addAttribute("id", id);
        return "components/post-components::admin-buttons";
    }

    @HxRequest
    @PostMapping("/postComponent/validateSize")
    public String validateSizeFragment(Model model, @RequestParam("content") String content) {
        int count = content.length();
        int max = 65535; // for TEXT type in MariaDB Column 'blog_entries.content'

        model.addAttribute("size", count + " / " + max);

        return "components/post-components::size-count";
    }
}
