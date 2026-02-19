package com.webdev.bloggingsystem.blog;

import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;

@Service
public class BlogEntryService {
    private final BlogEntryDao blogEntryDao;
    private final CategoryDao categoryDao;
    private final ObjectMapper mapper;

    public BlogEntryService(BlogEntryDao blogEntryDao, CategoryDao categoryDao, ObjectMapper objectMapper) {
        this.blogEntryDao = blogEntryDao;
        this.categoryDao = categoryDao;
        this.mapper = objectMapper;
    }

    public String createPost(CreateBlogEntryDto dto) {
        List<BlogEntryContentBlockDto> cleanedBlocks = removeEmptyContentBlocks(dto.getContentBlocks());
        String content = mapper.writeValueAsString(cleanedBlocks);

        return content;
    }

/*
    public FullBlogEntryDto readPost(int id) {
        BlogEntry entry = blogentryDao.findById(id);
        List<BlogEntryContentBlockDto> contentBlockList mapper.readValue(entry.content, new TypeReference<List<BlogEntryContentBlockDto>>() {});
        // get all values from converted content & category list
        return new FullBlogEntryDto();
    }*/


    static List<BlogEntryContentBlockDto> removeEmptyContentBlocks(List<BlogEntryContentBlockDto> contentBlocks) {
        return contentBlocks.stream()
                .filter(Objects::nonNull)
                .filter(block -> block.getType() != null)
                .filter(block ->
                        (block.getText() != null && !block.getText().isBlank()) ||
                        (block.getUrl() != null && !block.getUrl().isBlank())
                ).toList();
    }


}
