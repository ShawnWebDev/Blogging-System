package com.webdev.bloggingsystem.integration;

import com.webdev.bloggingsystem.dto.CommentRequestDto;
import com.webdev.bloggingsystem.dto.CommentResponseDto;
import com.webdev.bloggingsystem.services.CommentService;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.webdev.bloggingsystem.dto.LoginDto;

import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CommentService commentService;

    private String testUserToken;

    private String getToken(String username) {
        LoginDto loginDto = new LoginDto(username, "TestPassword");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        return response.getBody();
    }

    @BeforeAll
    public void beforeAll() {
        this.testUserToken = this.getToken("TestUser");
    }

    @Test
    @DisplayName("1. should fetch all reply comments for parent comment and count replies to them")
    void fetchAllReplyComments() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/comments/1", HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        System.out.println("response: " + response);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        System.out.println("json: " + documentContext.jsonString());
        String reply_1  = documentContext.read("$[0].comment");
        String reply_2  = documentContext.read("$[1].comment");

        System.out.println("reply_1: " + reply_1 +  " reply_2: " + reply_2);
        assertEquals("Test Reply 1 to Comment 1 on Test Post 1", reply_1);
        assertEquals("Test Reply 2 to Comment 1 on Test Post 1", reply_2);
    }

    @Test
    @DisplayName("2. should create a comment and return it's uri.")
    @DirtiesContext
    void createCommentAndReturnUri() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        CommentRequestDto commentDto = new CommentRequestDto(
                "Test Comment #2 on Test Post 1"
        );

        HttpEntity<CommentRequestDto> request = new HttpEntity<>(commentDto, headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts/1/comments", HttpMethod.POST, request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Should return 201 Created");

        System.out.println("response: " + response);

        URI uri = response.getHeaders().getLocation();
        System.out.println(uri.toString());
        request = new HttpEntity<>(headers);
        response = restTemplate
                .exchange(uri, HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        String comment  = documentContext.read("$.comment");
        assertEquals(commentDto.comment(), comment);

        System.out.println("response: " + response);
        System.out.println("documentContext: " + documentContext.jsonString());
    }

    @Test
    @DisplayName("3. should create a reply comment and return it's uri.")
    @DirtiesContext
    void createReplyComment() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        CommentRequestDto commentDto = new CommentRequestDto(
                "Test Reply 3 to Comment 1 on Test Post 1"
        );

        HttpEntity<CommentRequestDto> request = new HttpEntity<>(commentDto, headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts/1/comments?parentId=1", HttpMethod.POST, request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Should return 201 Created");

        System.out.println("response: " + response);

        URI uri = response.getHeaders().getLocation();
        System.out.println(uri.toString());
        request = new HttpEntity<>(headers);
        response = restTemplate
                .exchange(uri, HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        String comment  = documentContext.read("$.comment");
        assertEquals(commentDto.comment(), comment);

        System.out.println("response: " + response);
        System.out.println("documentContext: " + documentContext.jsonString());
    }

    @Test
    @DisplayName("4. should fetch single comment by id.")
    void fetchSingleCommentById() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/comments/comment/1", HttpMethod.GET, request, String.class);
        System.out.println("response: " + response);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        String comment  = documentContext.read("$.comment");
        assertEquals("Test Comment on Test Post 1", comment);
    }

    @Test
    @DisplayName("5. should update comment.")
    void updateComment() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        CommentRequestDto updatedComment = new CommentRequestDto(
                "Test Reply 1 to Comment 1 on Test Post 1 -- UPDATED"
        );
        HttpEntity<CommentRequestDto> request = new HttpEntity<>(updatedComment, headers);

        ResponseEntity<Void> response = restTemplate
                .exchange("/api/comments/comment/2", HttpMethod.PUT, request, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        System.out.println("response: " + response);

        ResponseEntity<String> getResponse = restTemplate
                .exchange("/api/comments/comment/2", HttpMethod.GET, request, String.class);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        System.out.println("documentContext: " + documentContext.jsonString());
        String content = documentContext.read("$.comment");
        assertEquals(updatedComment.comment(), content);

        System.out.println("response: " + response);
        System.out.println(content);
    }

    @Test
    @DisplayName("6. should remove comment text by comment author")
    void removeCommentByCommentAuthor() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate
                .exchange("/api/comments/comment/5", HttpMethod.DELETE, request, Void.class);

        System.out.println("response: " + response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Should return 204 No Content");

        ResponseEntity<String> getResponse = restTemplate
                .exchange("/api/comments/comment/5", HttpMethod.GET, request, String.class);
        System.out.println("response: " + getResponse);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        String content = documentContext.read("$.comment");

        assertEquals("Comment Removed By Comment Author..", content);

        // assert replies are unchanged.
        List<CommentResponseDto> commentList = commentService.getAllRepliesByParentId(2);
        System.out.println("commentList: " + commentList);
        assertEquals(1, commentList.size());
    }

    @Test
    @DisplayName("7. should remove comment text by blog entry author")
    void removeCommentByBlogEntryAuthor() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate
                .exchange("/api/comments/comment/4", HttpMethod.DELETE, request, Void.class);

        System.out.println("response: " + response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Should return 204 No Content");

        ResponseEntity<String> getResponse = restTemplate
                .exchange("/api/comments/comment/4", HttpMethod.GET, request, String.class);
        System.out.println("response: " + getResponse);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        String content = documentContext.read("$.comment");

        assertEquals("Comment Removed By Blog Author..", content);
    }

    @Test
    @DisplayName("8. get all comments made by authenticated user")
    void getAllCommentsByUsername() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/comments/me", HttpMethod.GET, request, String.class);
        System.out.println("response: "+ response);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray authors = documentContext.read("$..author");
        for (Object author : authors) {
            System.out.println("author? : " + author);
            assertEquals("TestUser", author.toString());
        }
    }

    @Test
    @DisplayName("9. should not get comments when not authenticated")
    void getAllCommentsNotAuthenticated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/comments/me", String.class);
        System.out.println("response: " + response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Should return 401");
    }

    @Test
    @DisplayName("10. should get all top-level comments from blog entry")
    void getAllTopLevelCommentsFromBlogEntry() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts/1/comments", HttpMethod.GET, request, String.class);

        System.out.println("response: "+ response);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200");
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        System.out.println("json: " + documentContext.jsonString());
        String comment_1  = documentContext.read("$[0].comment");
        String comment_2  = documentContext.read("$[1].comment");
        String comment_3  = documentContext.read("$[2].comment");

        System.out.println("comment_1: " + comment_1 +  " comment_2: " + comment_2 + " comment_3: " + comment_3);
        assertEquals("Test Comment on Test Post 1", comment_1);
        assertEquals("Test Comment 2 on Test Post 1", comment_2);
        assertEquals("Test Comment 3 on Test Post 1", comment_3);
    }

    @Test
    @DisplayName("11. exceed max input")
    void testMaxInput() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        String commentText = "Test Comment #2 on Test Post 1";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 135; i++) {
            sb.append(commentText);
        }
        String content = sb.toString();
        CommentRequestDto commentDto = new CommentRequestDto(
            content
        );
        //headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        HttpEntity<CommentRequestDto> request = new HttpEntity<>(commentDto, headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts/1/comments", HttpMethod.POST, request, String.class);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        String commentErr = documentContext.read("$.comment");
        int commentBytes = commentDto.comment().getBytes(StandardCharsets.UTF_8).length;

        System.out.println("response: " + response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 400 BAD REQUEST");
        assertEquals(commentErr, "Input length exceeded! Max: 500 - Used: " + commentBytes);
    }

    @Test
    @DisplayName("12. no input")
    void testNoInput() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        CommentRequestDto commentDto = new CommentRequestDto(
                ""
        );

        //headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        HttpEntity<CommentRequestDto> request = new HttpEntity<>(commentDto, headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts/1/comments", HttpMethod.POST, request, String.class);

        System.out.println("response: " + response);
        System.out.println("response body : " + response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 400");
        assertEquals("{\"comment\":\"Input empty\"}", response.getBody());
    }

    @Test
    @DisplayName("13. should not update by non author")
    void testUpdateCommentNonAuthor() {
        String nonAuthorToken = this.getToken("TestUser2");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + nonAuthorToken);
        CommentRequestDto updatedComment = new CommentRequestDto(
                "Test Reply 1 to Comment 1 on Test Post 1 -- UPDATED"
        );
        HttpEntity<CommentRequestDto> request = new HttpEntity<>(updatedComment, headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/comments/comment/2", HttpMethod.PUT, request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");
        System.out.println("response: " + response);
    }
}
