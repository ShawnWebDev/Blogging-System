package com.webdev.bloggingsystem.controllers;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.webdev.bloggingsystem.entities.LoginDto;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    private String testUserToken;

    private String getToken(String username, String password) {
        LoginDto loginDto = new LoginDto(username, password);

        ResponseEntity<String> response = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        return response.getBody();
    }

    @BeforeAll
    public void beforeAll() {
        this.testUserToken = this.getToken("TestUser", "TestPassword");
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

        String commentText = "Test Comment #2 on Test Post 1";

        HttpEntity<String> request = new HttpEntity<>(commentText, headers);

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
        assertEquals(commentText, comment);

        System.out.println("response: " + response);
        System.out.println("documentContext: " + documentContext.jsonString());
    }

    @Test
    @DisplayName("3. should create a reply comment and return it's uri.")
    @DirtiesContext
    void createReplyCommentAndReturnUri() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        String commentText = "Test Reply 3 to Comment 1 on Test Post 1";

        HttpEntity<String> request = new HttpEntity<>(commentText, headers);

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
        assertEquals(commentText, comment);

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

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        String comment  = documentContext.read("$.comment");
        assertEquals("Test Comment on Test Post 1", comment);

        System.out.println("response: " + response);
    }

    @Test
    @DisplayName("5. should update comment.")
    void updateComment() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        String updatedComment = "Test Reply 1 to Comment 1 on Test Post 1 -- UPDATED";
        HttpEntity<String> request = new HttpEntity<>(updatedComment, headers);

        ResponseEntity<Void> response = restTemplate
                .exchange("/api/comments/comment/2", HttpMethod.PUT, request, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Should return 204 No Content");
        System.out.println("response: " + response);

        ResponseEntity<String> getResponse = restTemplate
                .exchange("/api/comments/comment/2", HttpMethod.GET, request, String.class);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        System.out.println("documentContext: " + documentContext.jsonString());
        String content = documentContext.read("$.comment");
        assertEquals(updatedComment, content);

        System.out.println("response: " + response);
        System.out.println(content);
    }

}
