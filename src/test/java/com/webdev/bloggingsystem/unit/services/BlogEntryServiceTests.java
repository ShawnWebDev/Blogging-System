package com.webdev.bloggingsystem.unit.services;

import com.webdev.bloggingsystem.dto.BlogEntryResponseDto;
import com.webdev.bloggingsystem.dto.UserProfile;
import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.Category;
import com.webdev.bloggingsystem.exceptions.ResourceNotFoundException;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import com.webdev.bloggingsystem.repositories.BlogEntryRepo;
import com.webdev.bloggingsystem.repositories.CategoryRepo;
import com.webdev.bloggingsystem.repositories.CommentRepo;
import com.webdev.bloggingsystem.services.AuthService;
import com.webdev.bloggingsystem.services.BlogEntryServiceImpl;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// TODO : UNIT TEST ALL PATHS IN METHODS OF SERVICE LAYERS OF ALL ENTITIES!

@ExtendWith(MockitoExtension.class)
public class BlogEntryServiceTests {
    @Mock
    BlogEntryRepo blogEntryRepo;
    @Mock
    AppUserRepo appUserRepo;
    @Mock
    CategoryRepo categoryRepo;
    @Mock
    CommentRepo commentRepo;
    @Mock
    AuthService authService;
    @Mock
    Tuple mockedTuple;

    @InjectMocks
    BlogEntryServiceImpl blogEntryService;

    private static BlogEntry publicEntry;
    private static BlogEntry privateEntry;

    private static AppUser testAuthor;


    @BeforeAll
    static void setUp() {
        Set<Category> categories = new HashSet<>();
        Category category1 = new Category(
                "Test Category 1",
                "Testing mock for category 1."
        );
        category1.setId(1);
        categories.add(category1);
        Category category2 = new Category(
                "Test Category 2",
                "Testing mock for category 2."
        );
        category2.setId(2);
        categories.add(category2);

        testAuthor = new AppUser(
                "Test Author",
                "TestPassword",
                "email@email.com"
        );
        testAuthor.setId(1);
        testAuthor.setIsActive(true);

        publicEntry = new BlogEntry(
                testAuthor,
                "Test Entry",
                "Test Content",
                true,
                categories
        );
        publicEntry.setId(1);
        publicEntry.setAuthorId(1);

        privateEntry = new BlogEntry(
                testAuthor,
                "Test Entry",
                "Test Content",
                false,
                categories
        );
        privateEntry.setId(1);
        privateEntry.setAuthorId(1);
    }

    @Test
    void testGetBlogEntryById_publicByAuthor() {
        String principalName = "Test Author";
        UserProfile userProfile = new UserProfile(principalName, false);
        when(blogEntryRepo.findBlogEntryById(1)).thenReturn(Optional.of(publicEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        when(commentRepo.countCommentsByBlogEntryId(1)).thenReturn(mockedTuple);
        when(mockedTuple.get("commentCount", Long.class)).thenReturn(0L);

        BlogEntryResponseDto blogEntryResponseDto = blogEntryService.getBlogEntryById(1);

        BlogEntryResponseDto expectedBlogEntryResponse = new BlogEntryResponseDto(
                1,
                "Test Author",
                "Test Entry",
                "Test Content",
                null,
                null,
                List.of("Test Category 1", "Test Category 2"),
                0,
                true
        );

        assertEquals(expectedBlogEntryResponse, blogEntryResponseDto);
    }

    @Test
    void testGetBlogEntryById_publicByNonAuthor() {
        String principalName = "Test Non-Author";
        UserProfile userProfile = new UserProfile(principalName, false);
        when(blogEntryRepo.findBlogEntryById(1)).thenReturn(Optional.of(publicEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        when(commentRepo.countCommentsByBlogEntryId(1)).thenReturn(mockedTuple);
        when(mockedTuple.get("commentCount", Long.class)).thenReturn(0L);

        BlogEntryResponseDto blogEntryResponseDto = blogEntryService.getBlogEntryById(1);

        BlogEntryResponseDto expectedBlogEntryResponse = new BlogEntryResponseDto(
                1,
                "Test Author",
                "Test Entry",
                "Test Content",
                null,
                null,
                List.of("Test Category 1", "Test Category 2"),
                0,
                true
        );

        assertEquals(expectedBlogEntryResponse, blogEntryResponseDto);
    }

    @Test
    void testGetBlogEntryById_privateByAuthor() {
        String principalName = "Test Author";
        UserProfile userProfile = new UserProfile(principalName, false);
        when(blogEntryRepo.findBlogEntryById(1)).thenReturn(Optional.of(privateEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        when(commentRepo.countCommentsByBlogEntryId(1)).thenReturn(mockedTuple);
        when(mockedTuple.get("commentCount", Long.class)).thenReturn(0L);

        BlogEntryResponseDto blogEntryResponseDto = blogEntryService.getBlogEntryById(1);

        BlogEntryResponseDto expectedBlogEntryResponse = new BlogEntryResponseDto(
                1,
                "Test Author",
                "Test Entry",
                "Test Content",
                null,
                null,
                List.of("Test Category 1", "Test Category 2"),
                0,
                false
        );

        assertEquals(expectedBlogEntryResponse, blogEntryResponseDto);
    }

    @Test
    void testGetBlogEntryById_privateByNonAuthor() {
        String principalName = "Test Non-Author";
        UserProfile userProfile = new UserProfile(principalName, false);
        when(blogEntryRepo.findBlogEntryById(1)).thenReturn(Optional.of(privateEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                blogEntryService.getBlogEntryById(1));

        assertEquals("Entry not found with id 1", ex.getMessage());
    }

    //todo: continue unit tests for the rest of the methods starting at getAllBlogEntries()

}
