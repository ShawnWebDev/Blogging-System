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
public class BlogService {
    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

    private final BlogEntryDao blogEntryDao;
    private final CategoryDao categoryDao;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    BlogService(BlogEntryDao blogEntryDao, CategoryDao categoryDao, Parser markdownParser, HtmlRenderer htmlRenderer) {
        this.blogEntryDao = blogEntryDao;
        this.categoryDao = categoryDao;
        this.markdownParser = markdownParser;
        this.htmlRenderer = htmlRenderer;
    }

    BlogEntry findBlogEntryById(int id) {
        return blogEntryDao.findById(id)
                .orElseThrow(() -> new BlogEntryException("Entry not found with id: " + id));
    }

    List<SimpleBlogEntryDto> findAllSimpleBlogEntries() {
        return blogEntryDao.findAllSimple();
    }

    List<SimpleBlogEntryDto> findAllSimpleBlogEntriesInProgress() {
        return blogEntryDao.findAllSimpleInProgress();
    }

    List<SimpleBlogEntryDto> findAllSimpleBlogEntriesToCategoryName(String categoryName) {
        return blogEntryDao.findAllSimpleBlogEntriesToCategoryName(categoryName);
    }

    List<Category> findAllCategories() {
        return categoryDao.findAll();
    }

    Category findCategoryById(Integer id) {
        return categoryDao.findCategoryById(id)
                .orElseThrow(() -> new BlogEntryException("Category with id " + id + " not found!")
        );
    }

    List<SimpleCategoryDto> findAllSimpleCategories() {
        return categoryDao.findAllNames();
    }

    String findCategoryDescriptionByName(String categoryName) {
        return categoryDao.findCategoryDescriptionByName(categoryName);
    }

    void batchInsertCategoryJoins(int[] categoryIds, int blogId) {
        //remove 0 values from dto.categoryIds array and batch insert ids
        categoryDao.batchInsertJoins(this.cleanCategoryIds(categoryIds), blogId);
    }

    int createCategory(Category category) {
        return categoryDao.insert(category);
    }

    void deleteCategoryById(int id) {
        categoryDao.deleteCategoryById(id);
    }

    @Transactional
    Object[] createPost(CreateBlogEntryDto dto) {
        //create entry from dto, save to db, get id/slug, save categories to join table with batchInsertJoins(Set<ids>, id)
        logger.info("Post is being saved...");
        BlogEntry blogEntry = this.buildBlogEntryFromDto(dto);

        int blogId = blogEntryDao.insert(blogEntry);
        this.batchInsertCategoryJoins(dto.getCategoryIds(), blogId);
        // return id and slug for location redirect after submit.
        return new Object[]{blogId, blogEntry.getSlug()};
    }

    BlogEntry readPost(int id) {
        BlogEntry entry = this.findBlogEntryById(id);
        if (entry.getInProgress() && this.isNotAdmin()) {
            throw new BlogEntryException("Entry is in progress and cannot be read!");
        }
        entry.setContent(this.renderMarkdown(entry.getContent()));
        return entry;
    }

    boolean isNotAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ||
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
    }

    @Transactional
    Object[] updatePost(CreateBlogEntryDto dto) {
        logger.info("Post is being updated...");
        int blogId = dto.getId();
        BlogEntry blogEntry = this.buildBlogEntryFromDto(dto);
        blogEntry.setId(blogId);

        int isUpdated = blogEntryDao.update(blogEntry);
        if (isUpdated == 0) {
            throw new BlogEntryException("Entry NOT updated with id: " + blogId);
        }
        categoryDao.deleteJoinedByBlogId(blogId);
        this.batchInsertCategoryJoins(dto.getCategoryIds(), blogId);
        // return id and slug for location redirect after submit.
        return new Object[]{blogId, blogEntry.getSlug()};
    }

    // database handles cascade delete of category join table's relations
    void deletePost(int id) {
        int deleted = blogEntryDao.deleteById(id);
        if (deleted == 0) {
            throw new BlogEntryException("Entry NOT deleted with id: " + id);
        }
    }

    void updateCategory(Category category) {
        logger.info("Category is being updated...");
        int categoryId = category.getId();
        Category categoryToUpdate = new Category(
                category.getCategoryName(),
                category.getDescription()
        );
        categoryToUpdate.setId(categoryId);
        int isUpdated = categoryDao.update(categoryToUpdate);
        if (isUpdated == 0) {
            throw new BlogEntryException("Category NOT updated with id: " + categoryId);
        }
    }

    Set<Integer> cleanCategoryIds(int[] categoryIds) {
        //remove 0 and possible duplicate values from categoryIds array.
        Set<Integer> cleanedCategoryIds = new HashSet<>();
        for (int catId : categoryIds) {
            if (catId != 0) {
                cleanedCategoryIds.add(catId);
            }
        }
        return cleanedCategoryIds;
    }

    BlogEntry buildBlogEntryFromDto(CreateBlogEntryDto dto) {
        logger.info("Building blog entry from dto {}", dto.toString());
        BlogEntry blogEntry = new BlogEntry(
                dto.getTitle(),
                dto.getDescription(),
                dto.getContent(),
                dto.getThumbnailUrl(),
                dto.getThumbnailAlt()
        );
        blogEntry.setSlug(dto.getTitle());
        blogEntry.setInProgress(dto.getInProgress());
        blogEntry.setCodeUrl(dto.getCodeUrl());
        blogEntry.setDemoUrl(dto.getDemoUrl());
        blogEntry.setPortfolio(dto.getIsPortfolio());
        return blogEntry;
    }

    String renderMarkdown(String content) {
        return htmlRenderer.render(markdownParser.parse(content));
    }

    CreateBlogEntryDto buildCreateDtoForEdit(int id) {
        BlogEntry post = this.findBlogEntryById(id);
        int[] categoryIds = new int[4];

        if (!post.getCategoryNames().isEmpty()) {
            List<Integer> categoryIdList = categoryDao.findAllIdsInNames(post.getCategoryNames());
            for (int i = 0; i < categoryIdList.size(); i++) {
                categoryIds[i] = categoryIdList.get(i);
            }
        }

        return new CreateBlogEntryDto(post.getId(), post.getSlug(), post.getTitle(), post.getDescription(),
                post.getThumbnailUrl(), post.getThumbnailAlt(), categoryIds, post.getContent(), post.getInProgress(),
                post.getCodeUrl(), post.getDemoUrl(), post.isPortfolio());
    }
}