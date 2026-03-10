package com.webdev.bloggingsystem.blog;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BlogEntryServiceTest {
    @Mock
    BlogEntryDao blogEntryDao;
    @Mock
    CategoryDao categoryDao;
    @Mock
    ObjectMapper mapper;

    @InjectMocks
    BlogEntryService blogEntryService;

    //todo : create unit tests for BlogEntryService methods.

    @Test
    void testCreatePost() {
        CreateBlogEntryDto createBlogEntryDto = getCreateBlogEntryDto();
        when(mapper.writeValueAsString(createBlogEntryDto.getContentBlocks()))
                .thenReturn(mockedJson);
        when(blogEntryDao.insert(any(BlogEntry.class))).thenReturn(1);
        when(categoryDao.batchInsertJoins(anySet(), eq(1))).thenReturn(3);
        int result = blogEntryService.createPost(createBlogEntryDto);
        Assertions.assertEquals(6, result);
        verify(blogEntryDao, times(1)).insert(any(BlogEntry.class));
        // verify the empty values '0' are filtered out
        verify(categoryDao).batchInsertJoins(argThat(set -> set.size() == 3), eq(1));
    }

    @Test
    void testCreatePost_ContentTooLarge() {
        CreateBlogEntryDto createBlogEntryDto = new CreateBlogEntryDto();
        String tooLargeJson = "a".repeat(BlogEntryService.MAX_BYTES + 1);
        when(mapper.writeValueAsString(any())).thenReturn(tooLargeJson);
        Exception ex = Assertions.assertThrows(BlogEntryException.class, () ->
                blogEntryService.createPost(createBlogEntryDto));
        Assertions.assertEquals("Content block exceeds maximum allowed bytes!!!", ex.getMessage());
        verify(blogEntryDao, times(0)).insert(any(BlogEntry.class));
    }

    // todo : test these service methods.
    @Test
    void testEditPost() {
        CreateBlogEntryDto createBlogEntryDto = getCreateBlogEntryDto();
    }

    @Test
    void testDeletePost() {
        CreateBlogEntryDto createBlogEntryDto = getCreateBlogEntryDto();
    }

    // Helper method unit tests...
    @Test
    void testGetByteCount() {
        List<BlogEntryContentBlockDto> contentBlockDtoList = getCreateBlogEntryDto().getContentBlocks();

        int expectedByteCount = mockedJson.getBytes(StandardCharsets.UTF_8).length;
        when(mapper.writeValueAsString(any())).thenReturn(mockedJson);
        int actualByteCount = blogEntryService.getCurrentByteCount(contentBlockDtoList);

        Assertions.assertEquals(expectedByteCount, actualByteCount);
        verify(mapper, times(1)).writeValueAsString(any(List.class));
    }

    @Test
    void testSanitizeContentBlocks() {
        BlogEntryContentBlockDto textBlock = new BlogEntryContentBlockDto();
        textBlock.setType(BlockType.PARAGRAPH);
        textBlock.setText("   This is a paragraph.   ");

        BlogEntryContentBlockDto imageBlock = new BlogEntryContentBlockDto();
        imageBlock.setType(BlockType.IMAGE);
        imageBlock.setUrl("  http://myblog.com/photo.jpg  ");
        imageBlock.setAlt("  A cool photo.  ");

        List<BlogEntryContentBlockDto> dirtyBlocks = List.of(textBlock, imageBlock);
        List<BlogEntryContentBlockDto> sanitizedBlocks = BlogEntryService.sanitizeContentBlocks(dirtyBlocks);
        Assertions.assertEquals(2, sanitizedBlocks.size());
        Assertions.assertEquals("This is a paragraph.", sanitizedBlocks.getFirst().getText());
        Assertions.assertEquals("http://myblog.com/photo.jpg", sanitizedBlocks.get(1).getUrl());
        Assertions.assertEquals("A cool photo.", sanitizedBlocks.get(1).getAlt());
        Assertions.assertNull(sanitizedBlocks.getFirst().getUrl());
        Assertions.assertNull(sanitizedBlocks.getFirst().getAlt());
        Assertions.assertNull(sanitizedBlocks.get(1).getText());
    }


    private final static String mockedJson = "[{\"text\":\"Test Post Heading 1\",\"type\":\"HEADING\"},{\"text\":\"Test Post Paragraph 1\",\"type\":\"PARAGRAPH\"}]";

    private static CreateBlogEntryDto getCreateBlogEntryDto() {
        CreateBlogEntryDto createBlogEntryDto = new CreateBlogEntryDto();
        createBlogEntryDto.setTitle("Test Post Title");
        createBlogEntryDto.setDescription("Test Post Description");
        createBlogEntryDto.setThumbnailUrl("testUrl.com");
        createBlogEntryDto.setThumbnailAlt("Fake image alt text.");

        int[] categories = new int[] {1, 2, 3, 0};
        createBlogEntryDto.setCategoryIds(categories);

        List<BlogEntryContentBlockDto> contentBlocks = new ArrayList<>();
        BlogEntryContentBlockDto blogEntryContentHeading= new BlogEntryContentBlockDto();
        blogEntryContentHeading.setType(BlockType.HEADING);
        blogEntryContentHeading.setText("Test Post Heading 1");
        contentBlocks.add(blogEntryContentHeading);

        BlogEntryContentBlockDto blogEntryContentParagraph = new BlogEntryContentBlockDto();
        blogEntryContentParagraph.setType(BlockType.PARAGRAPH);
        blogEntryContentParagraph.setText("Test Post Paragraph 1");
        contentBlocks.add(blogEntryContentParagraph);

        createBlogEntryDto.setContentBlocks(contentBlocks);
        // Full 'contentBlocks' to JSON should be ->
        // [{"text":"Test Post Heading 1","type":"HEADING"},{"text":"Test Post Paragraph 1","type":"PARAGRAPH"}]
        // which is 101 bytes.

        return createBlogEntryDto;
    }


}
