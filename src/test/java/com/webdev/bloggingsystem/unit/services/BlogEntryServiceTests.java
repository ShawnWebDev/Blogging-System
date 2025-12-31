package com.webdev.bloggingsystem.unit.services;

import com.webdev.bloggingsystem.dto.*;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    @Mock
    UriComponentsBuilder mockUcb;

    @InjectMocks
    BlogEntryServiceImpl blogEntryService;

    private static BlogEntry publicEntry;
    private static BlogEntry privateEntry;
    private static AppUser testAuthor;
    private static Set<Category> categories = new HashSet<>();
    private static Category category1;
    private static Category category2;
    private static BlogEntryResponseDto expectedPublicBlogEntryResponse;
    private static BlogEntryResponseDto expectedPrivateBlogEntryResponse;


    @BeforeAll
    static void setUp() {
        category1 = new Category(
                "Test Category 1",
                "Testing mock for category 1."
        );
        category1.setId(1);
        categories.add(category1);
        category2 = new Category(
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
                1,
                "Test Entry",
                "Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars.",
                true,
                new HashSet<>(Set.of(category1))
        );
        publicEntry.setId(1);
        publicEntry.setAuthorId(1);

        privateEntry = new BlogEntry(
                1,
                "Test Entry",
                "Test Content",
                false,
                categories
        );
        privateEntry.setId(2);
        privateEntry.setAuthorId(1);

        expectedPublicBlogEntryResponse = new BlogEntryResponseDto(
                1,
                "Test Author",
                "Test Entry",
                "Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars.",
                null,
                null,
                List.of("Test Category 1"),
                0,
                true
        );

        expectedPrivateBlogEntryResponse = new BlogEntryResponseDto(
                2,
                "Test Author",
                "Test Entry",
                "Test Content",
                null,
                null,
                List.of("Test Category 1", "Test Category 2"),
                0,
                false
        );
    }

    @Test
    void testGetBlogEntryById_publicByAuthor() {
        String principalName = "Test Author";
        int entryId = publicEntry.getId();
        UserProfile userProfile = new UserProfile(principalName, false);
        when(blogEntryRepo.findBlogEntryById(entryId)).thenReturn(Optional.of(publicEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        when(commentRepo.countCommentsByBlogEntryId(1)).thenReturn(mockedTuple);
        when(mockedTuple.get("commentCount", Long.class)).thenReturn(0L);

        BlogEntryResponseDto blogEntryResponseDto = blogEntryService.getBlogEntryById(entryId);

        assertEquals(expectedPublicBlogEntryResponse, blogEntryResponseDto);
    }

    @Test
    void testGetBlogEntryById_publicByNonAuthor() {
        String principalName = "Test Non-Author";
        int entryId = publicEntry.getId();
        UserProfile userProfile = new UserProfile(principalName, false);
        when(blogEntryRepo.findBlogEntryById(entryId)).thenReturn(Optional.of(publicEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        when(commentRepo.countCommentsByBlogEntryId(entryId)).thenReturn(mockedTuple);
        when(mockedTuple.get("commentCount", Long.class)).thenReturn(0L);

        BlogEntryResponseDto blogEntryResponseDto = blogEntryService.getBlogEntryById(entryId);

        assertEquals(expectedPublicBlogEntryResponse, blogEntryResponseDto);
    }

    @Test
    void testGetBlogEntryById_privateByAuthor() {
        String principalName = "Test Author";
        int entryId = privateEntry.getId();
        UserProfile userProfile = new UserProfile(principalName, false);
        when(blogEntryRepo.findBlogEntryById(entryId)).thenReturn(Optional.of(privateEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        when(commentRepo.countCommentsByBlogEntryId(entryId)).thenReturn(mockedTuple);
        when(mockedTuple.get("commentCount", Long.class)).thenReturn(0L);

        BlogEntryResponseDto blogEntryResponseDto = blogEntryService.getBlogEntryById(entryId);

        assertEquals(expectedPrivateBlogEntryResponse, blogEntryResponseDto);
    }

    @Test
    void testGetBlogEntryById_privateByNonAuthor() {
        String principalName = "Test Non-Author";
        int entryId = privateEntry.getId();
        UserProfile userProfile = new UserProfile(principalName, false);

        when(blogEntryRepo.findBlogEntryById(entryId)).thenReturn(Optional.of(privateEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                blogEntryService.getBlogEntryById(entryId));

        assertEquals("Entry not found with id " + entryId, ex.getMessage());
    }

    @Test
    void testGetAllBlogEntriesPageable() { // tested more in depth in ../integration/BlogEntryControllerTest
        BlogEntry entry1 = new BlogEntry(); entry1.setId(10); entry1.setAuthorId(1);
        BlogEntry entry2 = new BlogEntry(); entry2.setId(20); entry2.setAuthorId(2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogEntry> dummyPage = new PageImpl<>(List.of(entry1, entry2), pageable, 2);
        when(blogEntryRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(dummyPage);

        PaginatedBlogEntriesResponseDto pageDto = blogEntryService.getAllBlogEntries(
                pageable,
                new BlogEntryFilterRequest(null, null, null),
                false
        );

        System.out.println(pageDto);
        assertEquals(2, pageDto.entries().size());
    }

    @Test
    void testSaveBlogEntry() {
        BlogEntryRequestDto blogEntryRequestDto = new BlogEntryRequestDto(
                "Test Entry",
            "Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars.",
                List.of("Test Category 1"),
                true
        );
        when(authService.getUserProfile()).thenReturn(new UserProfile("Test Author", false));
        when(appUserRepo.findIdByUsername("Test Author")).thenReturn(Optional.of(1));
        when(categoryRepo.findByCategoryNameIn(List.of("Test Category 1"))).thenReturn(Set.of(category1));
        when(blogEntryRepo.save(any())).thenReturn(publicEntry);
        when(mockUcb.path(anyString())).thenReturn(UriComponentsBuilder.fromPath("api/posts/1"));
        Map.Entry<URI, BlogEntryResponseDto> actualBlogEntryResponse = blogEntryService.saveEntry(blogEntryRequestDto, mockUcb);

        assertEquals(expectedPublicBlogEntryResponse, actualBlogEntryResponse.getValue());
    }

    @Test
    void testSaveBlogEntryInvalidCategory() {
        BlogEntryRequestDto blogEntryRequestDto = new BlogEntryRequestDto(
                "Test Entry",
                "Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars,Test Content of 300 chars.",
                List.of("Test Category 99"),
                true
        );
        when(authService.getUserProfile()).thenReturn(new UserProfile("Test Author", false));
        when(appUserRepo.findIdByUsername("Test Author")).thenReturn(Optional.of(1));
        when(categoryRepo.findByCategoryNameIn(List.of("Test Category 99"))).thenReturn(Set.of());

        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                blogEntryService.saveEntry(blogEntryRequestDto, mockUcb));
        assertEquals("Categories not found: [Test Category 99]", ex.getMessage());
    }

    @Test
    void testUpdateEntryWithNewCategory() {
        UserProfile userProfile = new UserProfile(testAuthor.getUsername(), false);
        when(blogEntryRepo.findBlogEntryById(1)).thenReturn(Optional.of(publicEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        when(categoryRepo.findByCategoryNameIn(List.of("Test Category 1", "Test Category 2"))).thenReturn(categories);
        BlogEntryRequestDto blogEntryRequestDto = new BlogEntryRequestDto(
                null,
                null,
                List.of("Test Category 1", "Test Category 2"),
                null
        );

        when(blogEntryRepo.save(any())).thenReturn(publicEntry);

        BlogEntryResponseDto result = blogEntryService.updateEntryById(1, blogEntryRequestDto);
        ArgumentCaptor<BlogEntry> captor = ArgumentCaptor.forClass(BlogEntry.class);
        verify(blogEntryRepo).save(captor.capture());

        BlogEntry savedEntry = captor.getValue();
        assertEquals(2, savedEntry.getCategories().size());
        assertEquals(List.of("Test Category 1", "Test Category 2"), result.categories());

        //revert
        publicEntry.removeCategory(category2);
    }

    @Test
    void testUpdateEntryRemoveCategory() {
        UserProfile userProfile = new UserProfile(testAuthor.getUsername(), false);
        when(blogEntryRepo.findBlogEntryById(2)).thenReturn(Optional.of(privateEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        when(categoryRepo.findByCategoryNameIn(List.of("Test Category 1"))).thenReturn(Set.of(category1));
        BlogEntryRequestDto blogEntryRequestDto = new BlogEntryRequestDto(
                null,
                null,
                List.of("Test Category 1"),
                null
        );
        when(blogEntryRepo.save(any())).thenReturn(privateEntry);

        BlogEntryResponseDto result = blogEntryService.updateEntryById(2, blogEntryRequestDto);
        ArgumentCaptor<BlogEntry> captor = ArgumentCaptor.forClass(BlogEntry.class);
        verify(blogEntryRepo).save(captor.capture());

        BlogEntry savedEntry = captor.getValue();
        assertEquals(1, savedEntry.getCategories().size());
        assertEquals(List.of("Test Category 1"), result.categories());

        //revert
        privateEntry.addCategory(category2);
    }

    @Test
    void testDeleteEntryAsAuthor() {
        UserProfile userProfile = new UserProfile(testAuthor.getUsername(), false);
        when(blogEntryRepo.findBlogEntryById(1)).thenReturn(Optional.of(publicEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        blogEntryService.deleteEntryById(1);
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(blogEntryRepo).deleteBlogEntryById(captor.capture());
        assertEquals(1, captor.getValue());
    }

    @Test
    void testDeleteEntryAsAdmin() {
        UserProfile userProfile = new UserProfile("Test Admin", true);
        when(blogEntryRepo.findBlogEntryById(1)).thenReturn(Optional.of(publicEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        blogEntryService.deleteEntryById(1);
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(blogEntryRepo).deleteBlogEntryById(captor.capture());
        assertEquals(1, captor.getValue());
    }

    @Test
    void testDeleteEntryAsUnauthorized() {
        UserProfile userProfile = new UserProfile("Fake Author", false);
        when(blogEntryRepo.findBlogEntryById(1)).thenReturn(Optional.of(publicEntry));
        when(appUserRepo.findUsernameById(1)).thenReturn(Optional.of(testAuthor.getUsername()));
        when(authService.getUserProfile()).thenReturn(userProfile);
        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                blogEntryService.deleteEntryById(1));
        assertEquals("Entry not found with id 1", ex.getMessage());
    }

    // further tested in integration tests
}
