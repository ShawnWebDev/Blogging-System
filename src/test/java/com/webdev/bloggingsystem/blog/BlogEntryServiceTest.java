package com.webdev.bloggingsystem.blog;

import com.webdev.bloggingsystem.errorHandling.BlogEntryException;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class BlogEntryServiceTest {
    @Mock
    BlogEntryDao blogEntryDao;
    @Mock
    CategoryDao categoryDao;
    @Mock
    Parser markdownParser;
    @Mock
    HtmlRenderer htmlRenderer;

    @InjectMocks
    BlogService blogEntryService;

    private BlogEntry inProgressEntry = new BlogEntry();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        inProgressEntry = new BlogEntry();
    }

    private static void setAuth(String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // unit tests for BlogService methods that are not just wiring DAO calls or building objects.

    @Test
    void testCleanCategoryIds_shouldRemoveZerosAndDuplicates() {
        int[] idArray = new int[]{1, 2, 2, 0};
        Set<Integer> categoryIdSet = BlogService.cleanCategoryIds(idArray);
        assertEquals(Set.of(1, 2), categoryIdSet);
    }

    @Test
    void testReadPostById_throwNotFound() {
        when(blogEntryDao.findById(anyInt())).thenReturn(Optional.empty());
        Exception ex = Assertions.assertThrows(BlogEntryException.class, () ->
                blogEntryService.readPostById(0));
        Assertions.assertEquals("Entry not found with id: 0", ex.getMessage());
    }

    @Test
    void testReadPostBySlug_throwNotFound() {
        when(blogEntryDao.findBySlug(anyString())).thenReturn(Optional.empty());
        Exception ex = Assertions.assertThrows(BlogEntryException.class, () ->
                blogEntryService.readPostBySlug(""));
        Assertions.assertEquals("Entry not found: ", ex.getMessage());
    }

    @Test
    void testReadPostById_throwInProgress() {
        setAuth("USER");
        inProgressEntry.setInProgress(true);
        when(blogEntryDao.findById(anyInt())).thenReturn(Optional.of(inProgressEntry));
        Exception ex = Assertions.assertThrows(BlogEntryException.class, () ->
                blogEntryService.readPostById(1));
        Assertions.assertEquals("Entry is in progress and cannot be read!", ex.getMessage());
    }

    @Test
    void testReadPostBySlug_throwInProgress() {
        setAuth("USER");
        inProgressEntry.setInProgress(true);
        when(blogEntryDao.findBySlug(anyString())).thenReturn(Optional.of(inProgressEntry));
        Exception ex = Assertions.assertThrows(BlogEntryException.class, () ->
                blogEntryService.readPostBySlug("some-slug"));
        Assertions.assertEquals("Entry is in progress and cannot be read!", ex.getMessage());
    }

    @Test
    void testReadPostById_allowInProgress() {
        setAuth("ADMIN");
        inProgressEntry.setInProgress(true);
        inProgressEntry.setId(1);
        when(blogEntryDao.findById(anyInt())).thenReturn(Optional.of(inProgressEntry));

        BlogEntry entry = blogEntryService.readPostById(1);

        assertEquals(1, entry.getId());
    }

    @Test
    void testReadPostBySlug_allowInProgress() {
        setAuth("ADMIN");
        inProgressEntry.setInProgress(true);
        inProgressEntry.setId(1);
        when(blogEntryDao.findBySlug(anyString())).thenReturn(Optional.of(inProgressEntry));

        BlogEntry entry = blogEntryService.readPostBySlug("some-slug");

        assertEquals(1, entry.getId());
    }

    @Test
    void testIsNotAdmin_isAdmin() {
        setAuth("ADMIN");
        boolean isAdmin = BlogService.isNotAdmin();
        Assertions.assertFalse(isAdmin);
    }

    @Test
    void testIsNotAdmin_isUser() {
        setAuth("USER");
        boolean isAdmin = BlogService.isNotAdmin();
        Assertions.assertTrue(isAdmin);
    }








}
