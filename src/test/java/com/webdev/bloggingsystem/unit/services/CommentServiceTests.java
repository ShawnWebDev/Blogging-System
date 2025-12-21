package com.webdev.bloggingsystem.unit.services;

import com.webdev.bloggingsystem.dto.CommentResponseDto;
import com.webdev.bloggingsystem.dto.UserProfile;
import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.Comment;
import com.webdev.bloggingsystem.exceptions.ResourceNotFoundException;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import com.webdev.bloggingsystem.repositories.BlogEntryRepo;
import com.webdev.bloggingsystem.repositories.CommentRepo;
import com.webdev.bloggingsystem.services.AuthService;
import com.webdev.bloggingsystem.services.CommentServiceImpl;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTests {
    @Mock
    CommentRepo commentRepo;
    @Mock
    AppUserRepo appUserRepo;
    @Mock
    BlogEntryRepo blogEntryRepo;
    @Mock
    AuthService authService;
    @Mock
    Tuple mockedTuple;
    @Mock
    UriComponentsBuilder mockUcb;

    @InjectMocks
    CommentServiceImpl commentService;

    private static AppUser appUser;
    private static BlogEntry blogEntry;
    private static BlogEntry privateBlogEntry;
    private static List<Comment> testComments;
    private static Comment testComment1;
    private static Comment testComment2;
    private static Comment testComment3;
    private static Comment testComment4;
    private static CommentResponseDto expectedResponse1;
    private static CommentResponseDto expectedResponse2;
    private static CommentResponseDto expectedResponse3;
    private static CommentResponseDto expectedResponse4;

    @BeforeAll
    static void setUp() {
        appUser = new AppUser(
                "Test User",
                "Test password",
                "testEmail@email.com"
        );
        appUser.setId(1);

        blogEntry = new BlogEntry(
                1,
                "Title",
                "Content",
                true,
                Set.of()
        );
        blogEntry.setId(1);
        blogEntry.setAuthorId(1);

        privateBlogEntry = new BlogEntry(
                1,
                "Private Title",
                "Private Content",
                false,
                Set.of()
        );
        privateBlogEntry.setId(2);
        privateBlogEntry.setAuthorId(1);

        testComments = new ArrayList<>();
        testComment1 = new Comment(
                "Test comment one..",
                1,
                1
        );
        testComment1.setId(1);
        testComment1.setAuthorId(1);

        testComment2 = new Comment(
                "Test comment two..",
                1,
                1
        );
        testComment2.setId(2);
        testComment2.setAuthorId(1);
        testComment2.setBlogEntryId(1);

        testComment3 = new Comment(
                "Test comment three..",
                1,
                1
        );
        testComment3.setId(3);
        testComment3.setAuthorId(1);

        testComments.add(testComment1);
        testComments.add(testComment2);
        testComments.add(testComment3);

        testComment4 = new Comment(
                "Test comment four..",
                1,
                1
        );
        testComment4.setId(4);
        testComment4.setAuthorId(1);

        expectedResponse1 = new CommentResponseDto(
                1,
                1,
                null,
                "Test comment one..",
                null,
                null,
                "Test User",
                0
        );
        expectedResponse2 = new CommentResponseDto(
                2,
                1,
                null,
                "Test comment two..",
                null,
                null,
                "Test User",
                0
        );
        expectedResponse3 = new CommentResponseDto(
                3,
                1,
                null,
                "Test comment three..",
                null,
                null,
                "Test User",
                0
        );
        expectedResponse4 = new CommentResponseDto(
                4,
                1,
                null,
                "Test comment four..",
                null,
                null,
                "Test User",
                2
        );
    }


    @Test
    public void testGetAllReplies_NoneFound() {
        when(commentRepo.findAllByParentCommentId(anyInt())).thenReturn(List.of());

        List<CommentResponseDto> result = commentService.getAllRepliesByParentId(1);
        System.out.println(result);
        assertEquals(List.of(), result);

        ArgumentCaptor<Integer> parentIdCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(commentRepo).findAllByParentCommentId(parentIdCaptor.capture());

        assertEquals(1, parentIdCaptor.getValue());
    }

    @Test
    public void testGetAllReplies_Found() {
        when(commentRepo.findAllByParentCommentId(anyInt())).thenReturn(testComments);
        when(appUserRepo.findUsernamesById(anySet())).thenReturn(List.of(mockedTuple));
        when(mockedTuple.get("userId", Integer.class)).thenReturn(1);
        when(mockedTuple.get("username", String.class)).thenReturn("Test User");

        List<CommentResponseDto> result = commentService.getAllRepliesByParentId(1);
        System.out.println(result);
        assertEquals(List.of(expectedResponse1,expectedResponse2, expectedResponse3), result);

        ArgumentCaptor<Integer> parentIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Set> usernamesCaptor = ArgumentCaptor.forClass(Set.class);
        verify(commentRepo).findAllByParentCommentId(parentIdCaptor.capture());
        verify(appUserRepo).findUsernamesById(usernamesCaptor.capture());

        assertEquals(1, parentIdCaptor.getValue());
        assertEquals(1, usernamesCaptor.getValue().size());
    }

    @Test
    public void testGetAllUsersComments_NotFound() {
        when(authService.getUserProfile()).thenReturn(
                new UserProfile("Non Poster", false)
        );
        when(appUserRepo.findIdByUsername(anyString())).thenReturn(Optional.of(1));
        when(commentRepo.findAllByAuthorId(1)).thenReturn(List.of());

        List<CommentResponseDto> result = commentService.getAllUsersComments();
        System.out.println(result);
        assertEquals(List.of(), result);

        ArgumentCaptor<String> userNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(appUserRepo).findIdByUsername(userNameCaptor.capture());
        verify(commentRepo).findAllByAuthorId(userIdCaptor.capture());

        assertEquals("Non Poster", userNameCaptor.getValue());
        assertEquals(1, userIdCaptor.getValue());
    }

    @Test
    public void testGetAllUsersComments_Found() {
        when(authService.getUserProfile()).thenReturn(
                new UserProfile("Test User", false)
        );
        when(appUserRepo.findIdByUsername(anyString())).thenReturn(Optional.of(1));
        when(commentRepo.findAllByAuthorId(1)).thenReturn(testComments);
        when(appUserRepo.findUsernamesById(anySet())).thenReturn(List.of(mockedTuple));
        when(mockedTuple.get("userId", Integer.class)).thenReturn(1);
        when(mockedTuple.get("username", String.class)).thenReturn("Test User");

        List<CommentResponseDto> result = commentService.getAllUsersComments();
        System.out.println(result);
        assertEquals(List.of(expectedResponse1,expectedResponse2, expectedResponse3), result);

        ArgumentCaptor<String> userNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(appUserRepo).findIdByUsername(userNameCaptor.capture());
        verify(commentRepo).findAllByAuthorId(userIdCaptor.capture());
    }

    @Test
    public void testGetAllTopLevelCommentsByBlogEntryId_NotFound() {
        when(commentRepo.fetchTopLevelCommentsByBlogEntryId(anyInt())).thenReturn(List.of());
        List<CommentResponseDto> result = commentService.getAllTopLevelCommentsByBlogEntryId(1);
        System.out.println(result);
        assertEquals(List.of(), result);
    }

    @Test
    public void testGetAllTopLevelCommentsByBlogEntryId_Found() {
        when(commentRepo.fetchTopLevelCommentsByBlogEntryId(anyInt())).thenReturn(testComments);
        when(appUserRepo.findUsernamesById(anySet())).thenReturn(List.of(mockedTuple));
        when(mockedTuple.get("userId", Integer.class)).thenReturn(1);
        when(mockedTuple.get("username", String.class)).thenReturn("Test User");
        List<CommentResponseDto> result = commentService.getAllTopLevelCommentsByBlogEntryId(1);
        System.out.println(result);
        assertEquals(List.of(expectedResponse1,expectedResponse2, expectedResponse3), result);
    }

    @Test
    public void testGetCommentById_NotFound() {
        when(commentRepo.findById(1)).thenReturn(Optional.empty());
        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                commentService.getCommentById(1));
        assertEquals("Comment not found with id " + 1, ex.getMessage());
    }

    @Test
    public void testGetCommentById_Found_noReplies() {
        when(commentRepo.findById(1)).thenReturn(Optional.of(testComment1));
        when(appUserRepo.findUsernameById(anyInt())).thenReturn(Optional.of("Test User"));
        when(commentRepo.countRepliesByParentCommentId(1)).thenReturn(null);

        CommentResponseDto result = commentService.getCommentById(1);
        System.out.println(result);
        assertEquals(expectedResponse1, result);

        ArgumentCaptor<Integer> commentIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> authorIdCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(commentRepo).findById(commentIdCaptor.capture());
        verify(appUserRepo).findUsernameById(authorIdCaptor.capture());
        verify(commentRepo).countRepliesByParentCommentId(commentIdCaptor.capture());
        assertEquals(1, commentIdCaptor.getValue());
        assertEquals(1, authorIdCaptor.getValue());
        assertEquals(testComment1.getAuthorId(), authorIdCaptor.getValue());
    }

    @Test
    public void testGetCommentById_Found_withReplies() {
        when(commentRepo.findById(1)).thenReturn(Optional.of(testComment4));
        when(appUserRepo.findUsernameById(anyInt())).thenReturn(Optional.of("Test User"));
        when(commentRepo.countRepliesByParentCommentId(1)).thenReturn(2);

        CommentResponseDto result = commentService.getCommentById(1);
        System.out.println(result);

        assertEquals(expectedResponse4, result);

        ArgumentCaptor<Integer> commentIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> authorIdCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(commentRepo).findById(commentIdCaptor.capture());
        verify(appUserRepo).findUsernameById(authorIdCaptor.capture());
        verify(commentRepo).countRepliesByParentCommentId(commentIdCaptor.capture());
        assertEquals(1, commentIdCaptor.getValue());
        assertEquals(1, authorIdCaptor.getValue());
        assertEquals(testComment1.getAuthorId(), authorIdCaptor.getValue());
    }

    @Test
    public void testSaveComment_toPublicEntry() {
        when(authService.getUserProfile()).thenReturn(
                new UserProfile("Test User", false)
        );
        when(appUserRepo.findIdByUsername(anyString())).thenReturn(Optional.of(1));
        when(blogEntryRepo.findById(anyInt())).thenReturn(Optional.of(blogEntry));
        when(commentRepo.save(any(Comment.class))).thenReturn(testComment1);
        when(mockUcb.path(anyString())).thenReturn(UriComponentsBuilder.fromPath("api/comments/comment/1"));

        Map.Entry<URI, CommentResponseDto> result = commentService.saveComment(
                "Test comment one..",
                1,
                null,
                mockUcb
        );
        System.out.println(result);

        assertEquals("api/comments/comment/1", result.getKey().toString());
        assertEquals(expectedResponse1, result.getValue());

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepo).save(commentCaptor.capture());
        assertEquals(testComment1.getComment(), commentCaptor.getValue().getComment());
    }

    @Test
    public void testSaveComment_toNotFoundEnty() {
        when(blogEntryRepo.findById(anyInt())).thenReturn(Optional.empty());
        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                commentService.saveComment("", 99, null, mockUcb));
        assertEquals("Entry not found with id " + 99, ex.getMessage());
    }

    @Test
    public void testSaveComment_toPrivateEntry() {
        when(blogEntryRepo.findById(anyInt())).thenReturn(Optional.of(privateBlogEntry));
        when(authService.getUserProfile()).thenReturn(new UserProfile("Test User", false));
        when(appUserRepo.findIdByUsername(anyString())).thenReturn(Optional.of(1));
        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                commentService.saveComment("To Private entry", 2, null, mockUcb));
        assertEquals("Entry not found with id " + 2, ex.getMessage());
    }

    @Test
    public void testUpdateComment_commentAuthor() {
        when(commentRepo.findById(anyInt())).thenReturn(Optional.of(testComment2));
        when(authService.getUserProfile()).thenReturn(
                new UserProfile("Test User", false));
        when(appUserRepo.findUsernameById(anyInt())).thenReturn(Optional.of("Test User"));
        when(commentRepo.countRepliesByParentCommentId(anyInt())).thenReturn(null);
        String newCommentText = "Updated comment";
        testComment2.setComment(newCommentText);
        when(commentRepo.save(any(Comment.class))).thenReturn(testComment2);
        CommentResponseDto result = commentService.updateComment(newCommentText, 2);
        System.out.println(result);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepo).save(commentCaptor.capture());
        assertEquals(result.comment(), commentCaptor.getValue().getComment());
        assertEquals(newCommentText, result.comment());

        //revert
        testComment2.setComment("Test comment two..");
    }

    @Test
    public void testUpdateComment_nonAuthor() {
        when(commentRepo.findById(anyInt())).thenReturn(Optional.of(testComment2));
        when(authService.getUserProfile()).thenReturn(
                new UserProfile("Not Author", false));
        when(appUserRepo.findUsernameById(anyInt())).thenReturn(Optional.of("Test User"));
        Exception ex = assertThrows(ResourceNotFoundException.class, () ->
                commentService.updateComment("To Private entry", 2));
        assertEquals("Comment not found with id " + 2, ex.getMessage());
    }

    @Test
    public void testDeleteComment_commentAuthor() {
        int authorId = testComment2.getAuthorId();

        when(commentRepo.findById(anyInt())).thenReturn(Optional.of(testComment2));
        when(authService.getUserProfile()).thenReturn(
                new UserProfile("Test User", false));
        when(appUserRepo.findIdByUsername(anyString())).thenReturn(Optional.of(authorId));
        when(blogEntryRepo.findAuthorIdByPostId(anyInt())).thenReturn(Optional.of(authorId));
        when(commentRepo.save(any(Comment.class))).thenReturn(testComment2);

        commentService.deleteComment(testComment2.getId());

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepo).save(commentCaptor.capture());
        assertEquals(testComment2.getComment(), commentCaptor.getValue().getComment());
        assertEquals("Comment Removed By Comment Author..", commentCaptor.getValue().getComment());
    }

    @Test
    public void testDeleteComment_blogEntryAuthor() {
        int authorId = 3;

        when(commentRepo.findById(anyInt())).thenReturn(Optional.of(testComment2));
        when(authService.getUserProfile()).thenReturn(
                new UserProfile("Test User", false));
        when(appUserRepo.findIdByUsername(anyString())).thenReturn(Optional.of(authorId));
        when(blogEntryRepo.findAuthorIdByPostId(anyInt())).thenReturn(Optional.of(authorId));
        when(commentRepo.save(any(Comment.class))).thenReturn(testComment2);

        commentService.deleteComment(testComment2.getId());

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepo).save(commentCaptor.capture());
        assertEquals(testComment2.getComment(), commentCaptor.getValue().getComment());
        assertEquals("Comment Removed By Blog Author..", commentCaptor.getValue().getComment());
    }

    @Test
    public void testDeleteComment_admin() {
        int authorId = testComment2.getAuthorId();

        when(commentRepo.findById(anyInt())).thenReturn(Optional.of(testComment2));
        when(authService.getUserProfile()).thenReturn(
                new UserProfile("Not Author", true));
        when(appUserRepo.findIdByUsername(anyString())).thenReturn(Optional.of(3));
        when(blogEntryRepo.findAuthorIdByPostId(anyInt())).thenReturn(Optional.of(authorId));
        when(commentRepo.save(any(Comment.class))).thenReturn(testComment2);

        commentService.deleteComment(testComment2.getId());

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepo).save(commentCaptor.capture());
        assertEquals(testComment2.getComment(), commentCaptor.getValue().getComment());
        assertEquals("Comment Removed By Admin..", commentCaptor.getValue().getComment());
    }

    //service methods will be further tested in CommentControllerTests : integration tests
}
