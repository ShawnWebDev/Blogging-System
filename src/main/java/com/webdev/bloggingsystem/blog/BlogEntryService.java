package com.webdev.bloggingsystem.blog;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class BlogEntryService {
    public static final int MAX_BYTES = 65535; // for TEXT type in MariaDB Column 'blog_entries.content'
    private static final Logger logger = LoggerFactory.getLogger(BlogEntryService.class);

    private final BlogEntryDao blogEntryDao;
    private final CategoryDao categoryDao;
    private final ObjectMapper mapper;

    public BlogEntryService(BlogEntryDao blogEntryDao, CategoryDao categoryDao, ObjectMapper objectMapper) {
        this.blogEntryDao = blogEntryDao;
        this.categoryDao = categoryDao;
        this.mapper = objectMapper;
    }

    public List<SimpleBlogEntryDto> findAllSimpleBlogEntries(int pageNumber, int pageSize) {
        return blogEntryDao.findAllSimple(pageNumber, pageSize);
    }

    public List<SimpleBlogEntryDto> findAllSimpleBlogEntriesFiltered(String categoryName, int pageNumber, int pageSize) {
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
    public String createPost(CreateBlogEntryDto dto) {
        List<BlogEntryContentBlockDto> contentBlocks = dto.getContentBlocks();
        if (contentBlocks.isEmpty()) {
            throw new BlogEntryException("Content blocks are empty!");
        }

        String jsonContentString = mapper.writeValueAsString(sanitizeContentBlocks(dto.getContentBlocks()));
        checkCurrentByteCount(jsonContentString.getBytes().length);
        logger.info("Create post json content string: {}", jsonContentString);

        //create entry from dto, save to db, get id/slug, save categories to join table with batchInsertJoins(Set, int)
        logger.info("Content is being saved...");
        BlogEntry blogEntry = new BlogEntry(
                dto.getTitle(),
                dto.getDescription(),
                jsonContentString,
                dto.getThumbnailUrl(),
                dto.getThumbnailAlt()
        );
        blogEntry.setSlug(dto.getTitle());
        int blogId = blogEntryDao.insert(blogEntry);
        //remove 0 values from categoryIds array.
        Set<Integer> cleanedCategoryIds = cleanCategoryIds(dto.getCategoryIds());

        int updatedJoinAmt = categoryDao.batchInsertJoins(cleanedCategoryIds, blogId);
        logger.info("Updated join amt has been saved. Rows created: {}", updatedJoinAmt);
        // return slug for location to direct after submit.
        return blogEntry.getSlug();
    }

    public FullBlogEntryDto readPostById(int id) {
        BlogEntry entry = blogEntryDao.findById(id)
                .orElseThrow(() -> new BlogEntryException("Entry not found with id: " + id));
        return this.buildFullBlogEntryDto(entry);
    }

    public FullBlogEntryDto readPostBySlug(String slug) {
        BlogEntry entry = blogEntryDao.findBySlug(slug)
                .orElseThrow(() -> new BlogEntryException("Entry not found: " + slug));
        return this.buildFullBlogEntryDto(entry);
    }

    @Transactional
    public String updatePost(CreateBlogEntryDto dto) {
        String jsonContentString = mapper.writeValueAsString(sanitizeContentBlocks(dto.getContentBlocks()));
        checkCurrentByteCount(jsonContentString.getBytes().length);
        logger.info("Update post json content string: {}", jsonContentString);

        logger.info("Content block is being updated...");
        int blogId = dto.getId();
        BlogEntry blogEntry = new BlogEntry(
                dto.getTitle(),
                dto.getDescription(),
                jsonContentString,
                dto.getThumbnailUrl(),
                dto.getThumbnailAlt()
        );
        blogEntry.setId(blogId);
        blogEntry.setSlug(dto.getTitle());
        int isUpdated = blogEntryDao.update(blogEntry);
        if (isUpdated == 0) {
            throw new BlogEntryException("Entry not updated!");
        }
        categoryDao.deleteJoinedByBlogId(blogId);
        //remove 0 values from categoryIds array.
        Set<Integer> cleanedCategoryIds = cleanCategoryIds(dto.getCategoryIds());

        logger.info("Saving category relations... ");
        categoryDao.batchInsertJoins(cleanedCategoryIds, blogId);
        return blogEntry.getSlug();
    }

    @Transactional
    public String deletePost(int id) {
        // todo
        return "";
    }

    private FullBlogEntryDto buildFullBlogEntryDto(BlogEntry entry) {
        List<BlogEntryContentBlockDto> contentBlockList = mapper.readValue(entry.getContent(), new TypeReference<>() {});
        return new FullBlogEntryDto(entry.getId(), entry.getTitle(), entry.getSlug(), entry.getDescription(),
                entry.getCreatedAt(), entry.getUpdatedAt(), entry.getCategoryNames(), contentBlockList);
    }

    public CreateBlogEntryDto buildCreateDto(int id) {
        BlogEntry post = blogEntryDao.findById(id)
                .orElseThrow(() -> new BlogEntryException("Entry not found with id: " + id));
        List<Integer> categoryIdList = categoryDao.findAllIdsInNames(post.getCategoryNames());
        int[] categoryIds = new int[4];
        for (int i = 0; i < categoryIdList.size(); i++) {
            categoryIds[i] = categoryIdList.get(i);
        }
        List<BlogEntryContentBlockDto> contentBlockList = mapper.readValue(post.getContent(), new TypeReference<>() {});

        return new CreateBlogEntryDto(post.getId(), post.getTitle(), post.getDescription(),
                post.getThumbnailUrl(), post.getThumbnailAlt(), categoryIds, contentBlockList);
    }

    private static Set<Integer> cleanCategoryIds(int[] categoryIds) {
        //remove 0 values from categoryIds array.
        Set<Integer> cleanedCategoryIds = new HashSet<>();
        for (int catId : categoryIds) {
            if (catId != 0) {
                cleanedCategoryIds.add(catId);
            }
        }
        return cleanedCategoryIds;
    }

    private static void checkCurrentByteCount(int bytes) {
        if (bytes > MAX_BYTES) {
            logger.error("Content block exceeds maximum allowed bytes!!!");
            throw new BlogEntryException("Content block exceeds maximum allowed bytes!!!");
        }
    }

    public int getCurrentByteCount(List<BlogEntryContentBlockDto> contentBlocks) {
        return mapper.writeValueAsString(sanitizeContentBlocks(contentBlocks))
                .getBytes(StandardCharsets.UTF_8)
                .length;
    }

    // removes 'content blocks' that are null, have a null type or have null or blank text or url field
    // they have to have heading, paragraph, or code text or a url (for image) to be valid. - to be used in create and update
    static List<BlogEntryContentBlockDto> sanitizeContentBlocks(List<BlogEntryContentBlockDto> contentBlocks) {
        return contentBlocks.stream()
                .filter(Objects::nonNull) // remove null index / blocks due to deleting blocks in html form
                .filter(block ->
                        (block.getType() != null) && ( // every block needs a type
                        (block.getText() != null && !block.getText().isBlank()) ||
                        (block.getUrl() != null && !block.getUrl().isBlank())) // every block must have either text or url
                ).map(block -> {
                    // trim edge white space,
                    // need to also check for null and blank in text and url because one could still be null or blank, the filter would only remove either/or.
                        if (block.getText() != null && !block.getText().isBlank()) block.setText(block.getText().trim());
                        if (block.getUrl() != null && !block.getUrl().isBlank()) block.setUrl(block.getUrl().trim());
                        if (block.getAlt() != null && !block.getAlt().isBlank()) block.setAlt(block.getAlt().trim());
                        if (block.getCaption() != null && !block.getCaption().isBlank()) block.setCaption(block.getCaption().trim());
                        return block;
                }
                ).toList(); // unmodifiable list, it will not be changed after this, only persisted
    }
}