package com.webdev.bloggingsystem.blog;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    public String createPost(CreateBlogEntryDto dto) {
        int bytes = this.getCurrentByteCount(dto.getContentBlocks());
        if (bytes > MAX_BYTES) {
            logger.error("Content block exceeds maximum allowed bytes!!!");
            throw new BlogEntryException("Content block exceeds maximum allowed bytes!!!");
        }
        String jsonContentString = mapper.writeValueAsString(sanitizeContentBlocks(dto.getContentBlocks()));
        logger.info("Create post json content string: {}", jsonContentString);

        //create entry from dto, save to db, get id/slug, save categories to join table with batchInsertJoins(Set, int)
        logger.info("Content block is being saved...");
        BlogEntry blogEntry = BlogEntry.createBlogEntry(
                dto.getTitle(),
                dto.getDescription(),
                jsonContentString,
                dto.getThumbnailUrl(),
                dto.getThumbnailAlt()
        );
        int blogId = blogEntryDao.insert(blogEntry);
        //remove 0 values from categoryIds array.
        Set<Integer> cleanedCategoryIds = new HashSet<>();
        for (int catId : dto.getCategoryIds()) {
            if (catId != 0) {
                cleanedCategoryIds.add(catId);
            }
        }
        int updatedJoinAmt = categoryDao.batchInsertJoins(cleanedCategoryIds, blogId);
        logger.info("Updated join amt has been saved. Rows created: {}", updatedJoinAmt);
        // return slug for location to direct after submit.
        return blogEntry.getSlug();
    }

    public FullBlogEntryDto readPostById(int id) {
        BlogEntry entry = blogEntryDao.findById(id)
                .orElseThrow(() -> new BlogEntryException("Entry not found with id: " + id));

        List<BlogEntryContentBlockDto> contentBlockList = mapper.readValue(entry.getContent(), new TypeReference<>() {});
        // get all values from converted content & category list
        return new FullBlogEntryDto(
                entry.getTitle(), entry.getSlug(), entry.getDescription(), entry.getCreatedAt(), entry.getUpdatedAt(),
                entry.getCategoryNames(), contentBlockList);
    }

    public FullBlogEntryDto readPostBySlug(String slug) {
        BlogEntry entry = blogEntryDao.findBySlug(slug)
                .orElseThrow(() -> new BlogEntryException("Entry not found: " + slug));

        List<BlogEntryContentBlockDto> contentBlockList = mapper.readValue(entry.getContent(), new TypeReference<>() {});
        // get all values from converted content & category list
        return new FullBlogEntryDto(
                entry.getTitle(), entry.getSlug(), entry.getDescription(), entry.getCreatedAt(), entry.getUpdatedAt(),
                entry.getCategoryNames(), contentBlockList);
    }

    public String updatePost(int id) {
        return "";
    }

    public String deletePost(int id) {
        return "";
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
