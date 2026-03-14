package com.webdev.bloggingsystem.blog;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BlogEntryService {
    public static final int MAX_BYTES = 65535; // for TEXT type in MariaDB Column 'blog_entries.content'
    private static final Logger logger = LoggerFactory.getLogger(BlogEntryService.class);

    private final BlogEntryDao blogEntryDao;
    private final CategoryDao categoryDao;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public BlogEntryService(BlogEntryDao blogEntryDao, CategoryDao categoryDao, Parser markdownParser, HtmlRenderer htmlRenderer) {
        this.blogEntryDao = blogEntryDao;
        this.categoryDao = categoryDao;
        this.markdownParser = markdownParser;
        this.htmlRenderer = htmlRenderer;
    }

    public List<SimpleBlogEntryDto> findAllSimpleBlogEntries(int pageNumber, int pageSize) {
        return blogEntryDao.findAllSimple(pageNumber, pageSize);
    }

    public List<SimpleBlogEntryDto> findAllSimpleBlogEntriesInProgress() {
        return blogEntryDao.findAllSimpleInProgress();
    }

    public List<SimpleBlogEntryDto> findAllSimpleBlogEntriesToCategoryName(String categoryName, int pageNumber, int pageSize) {
        return blogEntryDao.findAllSimpleBlogEntriesToCategoryName(categoryName, pageNumber, pageSize);
    }

    public List<Category> findAllCategories() {
        return categoryDao.findAll();
    }

    public List<SimpleCategoryDto> findAllSimpleCategories() {
        return categoryDao.findAllNames();
    }

    public String findCategoryDescriptionByName(String categoryName) {
        return categoryDao.findCategoryDescriptionByName(categoryName);
    }

    @Transactional
    public int createPost(CreateBlogEntryDto dto) {
        //create entry from dto, save to db, get id/slug, save categories to join table with batchInsertJoins(Set<ids>, id)
        logger.info("Content is being saved...");
        BlogEntry blogEntry = new BlogEntry(
                dto.getTitle(),
                dto.getDescription(),
                dto.getContent(),
                dto.getThumbnailUrl(),
                dto.getThumbnailAlt()
        );
        blogEntry.setSlug(dto.getTitle());
        blogEntry.setInProgress(dto.getInProgress());

        int blogId = blogEntryDao.insert(blogEntry);
        //remove 0 values from categoryIds array.
        Set<Integer> cleanedCategoryIds = cleanCategoryIds(dto.getCategoryIds());
        logger.info("Saving category relations... ");
        categoryDao.batchInsertJoins(cleanedCategoryIds, blogId);

        // return id for location redirect after submit.
        return blogId;
    }

    public FullBlogEntryDto readPostById(int id) {
        BlogEntry entry = blogEntryDao.findById(id)
                .orElseThrow(() -> new BlogEntryException("Entry not found with id: " + id));
        if (entry.getInProgress() && isNotAdmin()) {
            throw new BlogEntryException("Entry is in progress and cannot be read!");
        }
        return this.buildFullBlogEntryDto(entry);
    }

    public FullBlogEntryDto readPostBySlug(String slug) {
        BlogEntry entry = blogEntryDao.findBySlug(slug)
                .orElseThrow(() -> new BlogEntryException("Entry not found: " + slug));
        if (entry.getInProgress() && isNotAdmin()) {
            throw new BlogEntryException("Entry is in progress and cannot be read!");
        }
        return this.buildFullBlogEntryDto(entry);
    }

    private static boolean isNotAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ||
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
    }

    @Transactional
    public int updatePost(CreateBlogEntryDto dto) {
        logger.info("Content block is being updated...");
        int blogId = dto.getId();
        BlogEntry blogEntry = new BlogEntry(
                dto.getTitle(),
                dto.getDescription(),
                dto.getContent(),
                dto.getThumbnailUrl(),
                dto.getThumbnailAlt()
        );
        blogEntry.setId(blogId);
        blogEntry.setSlug(dto.getTitle());
        blogEntry.setInProgress(dto.getInProgress());
        int isUpdated = blogEntryDao.update(blogEntry);
        if (isUpdated == 0) {
            throw new BlogEntryException("Entry NOT updated with id: " + blogId);
        }
        categoryDao.deleteJoinedByBlogId(blogId);
        //remove 0 values from categoryIds array.
        Set<Integer> cleanedCategoryIds = cleanCategoryIds(dto.getCategoryIds());

        logger.info("Updating category relations... ");
        categoryDao.batchInsertJoins(cleanedCategoryIds, blogId);
        return blogEntry.getId();
    }

    @Transactional
    public void deletePost(int id) {
        int deleted = blogEntryDao.deleteById(id);
        if (deleted == 0) {
            throw new BlogEntryException("Entry NOT deleted with id: " + id);
        }
    }

    private String renderMarkdown(String content) {
        return htmlRenderer.render(markdownParser.parse(content));
    }

    private FullBlogEntryDto buildFullBlogEntryDto(BlogEntry entry) {
        return new FullBlogEntryDto(entry.getId(), entry.getTitle(), entry.getSlug(), entry.getDescription(),
                entry.getCreatedAt(), entry.getUpdatedAt(), entry.getCategoryNames(), this.renderMarkdown(entry.getContent()));
    }

    public CreateBlogEntryDto buildCreateDto(int id) {
        BlogEntry post = blogEntryDao.findById(id)
                .orElseThrow(() -> new BlogEntryException("Entry not found with id: " + id));
        List<Integer> categoryIdList = categoryDao.findAllIdsInNames(post.getCategoryNames());
        int[] categoryIds = new int[4];
        for (int i = 0; i < categoryIdList.size(); i++) {
            categoryIds[i] = categoryIdList.get(i);
        }

        return new CreateBlogEntryDto(post.getId(), post.getTitle(), post.getDescription(),
                post.getThumbnailUrl(), post.getThumbnailAlt(), categoryIds, post.getContent(), post.getInProgress());
    }

    private static Set<Integer> cleanCategoryIds(int[] categoryIds) {
        //remove 0 and possible duplicate values from categoryIds array.
        Set<Integer> cleanedCategoryIds = new HashSet<>();
        for (int catId : categoryIds) {
            if (catId != 0) {
                cleanedCategoryIds.add(catId);
            }
        }
        return cleanedCategoryIds;
    }
}