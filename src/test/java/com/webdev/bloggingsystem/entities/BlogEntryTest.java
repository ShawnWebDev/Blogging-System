package com.webdev.bloggingsystem.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BlogEntryTest {
    private BlogEntry blogEntry;
    private AppUser mockUser;
    private Category mockCategory;
    private Comment mockComment1;
    private Comment mockComment2;

    @BeforeEach
    public void setUp() {
        blogEntry = new BlogEntry();
        blogEntry.setId(1);

        mockUser = Mockito.mock(AppUser.class);
        mockCategory = Mockito.mock(Category.class);
        mockComment1 = Mockito.mock(Comment.class);
        mockComment2 = Mockito.mock(Comment.class);

        Mockito.when(mockUser.getId()).thenReturn(1);
        Mockito.when(mockCategory.getId()).thenReturn(1);
        Mockito.when(mockComment1.getId()).thenReturn(1);
        Mockito.when(mockComment2.getId()).thenReturn(2);
    }

    @Test
    @DisplayName("1. Constructor to ensure object is created.")
    public void testConstructor() {
        BlogEntry blogEntry = new BlogEntry(
                mockUser,
                "Test Blog Title",
                "Test Blog Content.",
                true,
                Set.of(mockCategory)
        );
        assertNotNull(blogEntry);
        assertEquals(BlogEntry.class, blogEntry.getClass(), "BlogEntry class should be created.");
        assertEquals("Test Blog Title", blogEntry.getTitle(), "title should be created.");
        assertEquals("Test Blog Content.", blogEntry.getContent(),  "content should be created.");
    }

    @Test
    @DisplayName("2. setAuthor and getAuthor.")
    public void testSetAuthor() {
        blogEntry.setAuthor(mockUser);
        assertEquals(mockUser, blogEntry.getAuthor(), "Author should be set with mockUser.");
    }

    @Test
    @DisplayName("3. add and remove from categories.")
    public void testAddCategory() {
        blogEntry.addCategory(mockCategory);
        assertEquals(1, blogEntry.getCategories().size(), "One Category should be added.");
        assertTrue(blogEntry.getCategories().contains(mockCategory));

        blogEntry.removeCategory(mockCategory);
        assertEquals(0, blogEntry.getCategories().size(), "One Category should be removed.");
        assertFalse(blogEntry.getCategories().contains(mockCategory), "Category should not be in Set.");
    }


    @Test
    @DisplayName("5. blogEntry entities are equal with same ID")
    public void testEquals() {
        BlogEntry testEntry = new BlogEntry();
        testEntry.setId(1);

        assertEquals(testEntry, blogEntry);
        assertEquals(blogEntry, testEntry);
    }

    @Test
    @DisplayName("6. blogEntry entities are not equal with null IDs")
    public void testNotEquals() {
        BlogEntry testEntry = new BlogEntry();
        blogEntry.setId(null);

        assertNotEquals(testEntry, blogEntry);
        assertNotEquals(blogEntry, testEntry);
    }

    @Test
    @DisplayName("7. null id should have hash of 31 and revert to id when persisted")
    public void testNullIdHashCode() {
        BlogEntry testEntry = new BlogEntry();
        assertEquals(31, testEntry.hashCode());

        testEntry.setId(1);
        assertEquals(1, testEntry.hashCode());
    }

    @Test
    @DisplayName("8. consistent hashcode to id")
    public void testConsistentHashcode() {
        BlogEntry testEntry = new BlogEntry();
        testEntry.setId(1);
        assertEquals(1, testEntry.hashCode());
        assertEquals(Integer.valueOf(1).hashCode(), testEntry.hashCode());
    }
}