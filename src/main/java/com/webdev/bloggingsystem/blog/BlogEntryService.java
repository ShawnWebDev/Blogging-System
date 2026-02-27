package com.webdev.bloggingsystem.blog;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

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

    public void createPost(CreateBlogEntryDto dto) {
        int bytes = this.getCurrentByteCount(dto.getContentBlocks());
        if (bytes > MAX_BYTES) {
            logger.error("Content block exceeds maximum allowed bytes!!!");
            throw new BlogEntryException("Content block exceeds maximum allowed bytes!!!");
            // send error to ui
        }
        String jsonString = mapper.writeValueAsString(dto.getContentBlocks());
        logger.info("JSON String: {} ", jsonString);
        logger.info("Blocks List: {} ", mapper.readValue(jsonString, new TypeReference<List<BlogEntryContentBlockDto>>() {}));

        //save to db, get id, save to category join table with batchInsertJoins(Set, int)
        logger.info("Content block is being saved...");
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
                .filter(Objects::nonNull)
                .filter(block ->
                        (block.getType() != null) && (
                        (block.getText() != null && !block.getText().isBlank()) ||
                        (block.getUrl() != null && !block.getUrl().isBlank()))
                ).map(block -> {
                    // need to also check for null and blank in text and url because one could still be null or blank, the filter would only remove one.
                        if (block.getText() != null && !block.getText().isBlank()) block.setText(block.getText().trim());
                        if (block.getText() != null && !block.getUrl().isBlank()) block.setUrl(block.getUrl().trim());
                        if (block.getAlt() != null && !block.getAlt().isBlank()) block.setAlt(block.getAlt().trim());
                        if (block.getCaption() != null && !block.getCaption().isBlank()) block.setCaption(block.getCaption().trim());
                        return block;
                }
                ).toList();
    }

}
