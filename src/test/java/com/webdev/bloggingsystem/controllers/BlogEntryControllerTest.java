package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.entities.BlogEntryRequestDto;
import com.webdev.bloggingsystem.entities.LoginDto;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // <-- for H2 testing, MySQL testing will require a different class or something like-
// @BeforeEach that reverts the test data .
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BlogEntryControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // only for TestUser as it is the most used
    private String testUserToken;

    private Integer countJoinTableEntries(Integer postId) {
        // for join table with no repository, only used to check if cascade works when deleting Entry
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posts_categories WHERE post_id = ?",
                Integer.class,
                postId
        );
    }

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
    @DisplayName("1. found id")
    void getBlogEntryById() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts/1", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");

        System.out.println("response: " + response);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        String content = documentContext.read("$.content");

        System.out.println("json: " + documentContext.jsonString());

        assertNotNull(id);
        assertEquals(1, id);
        assertEquals("Test Post 1 - TestAdmin content is here.", content);
    }

    @Test
    @DisplayName("2. not found id")
    void notFoundBlogEntryById() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts/99", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");
        System.out.println("response: " + response);

        assertEquals("Entry not found with id 99", response.getBody());
    }

    @Test
    @DisplayName("3. create and persist new BlogEntry")
    @DirtiesContext // <-- needed to restart application after adding this new data so tests stay consistent with data.sql
    void createBlogEntry() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        BlogEntryRequestDto blogEntryRequestDto = new BlogEntryRequestDto(
                "Testing Http POST",
                "This entry is for testing the Http POST method.",
                List.of("Test Category 1", "Test Category 2"),
                true
        );
        HttpEntity<Object> postEntity = new HttpEntity<>(blogEntryRequestDto, headers);

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/posts", postEntity, Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Should return 201 Created");

        HttpEntity<String> getEntity = new HttpEntity<>(headers);

        // Checks entry was created and correct.
        URI uri = response.getHeaders().getLocation();
        System.out.println("Fetching entry from URI: " + uri);

        ResponseEntity<String> getResponse = restTemplate
                .exchange(uri, HttpMethod.GET, getEntity, String.class);

        System.out.println("GET response: " + getResponse);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode(), "Should return 200");

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String content = documentContext.read("$.content");
        JSONArray categories = documentContext.read("$.categories");

        assertEquals(4, id);
        assertEquals("This entry is for testing the Http POST method.", content);
        assertEquals("Test Category 1", categories.getFirst());
        assertEquals("Test Category 2", categories.get(1));
    }

    @Test
    @DisplayName("4. should return all public BlogEntries")
    void getAllPublicBlogEntries() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts?sort=createdAt,asc", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        System.out.println("response: " + response.getBody());

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // double . to return list of all values of specified key
        JSONArray ids = documentContext.read("$..id");
        JSONArray titles = documentContext.read("$..title");

        // Entry with id 2 is private and should not be included
        assertEquals(2, ids.size());
        assertEquals(List.of(1, 3), ids);

        assertEquals(2, titles.size());
        assertEquals(List.of("Test Post 1", "Test Post 3"), titles);
    }

    @Test
    @DisplayName("5. should return page of BlogEntry")
    void getBlogEntryAsPage() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts?page=0&size=1", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        System.out.println("response: " + response.getBody());
    }

    @Test
    @DisplayName("6. should return sorted page of BlogEntries (last entry by date id=3")
    void getBlogEntryAsSortedPage() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts?page=0&size=1&sort=createdAt,desc", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        System.out.println("json: " + documentContext.jsonString());
        String title  = documentContext.read("$.entries[0].title");

        assertEquals("Test Post 3", title);
    }

    @Test
    @DisplayName("7. should return sorted page using default pageable (descending sort by updatedAt)")
    void getBlogEntryAsSortedPageUsingDefaultPageable() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        System.out.println("response: " + response.getBody());
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray ids = documentContext.read("$..id");
        JSONArray titles = documentContext.read("$..title");
        int totalElements = documentContext.read("$.totalEntries");

        // Entry with id 2 is private and should not be included
        assertEquals(2, ids.size());
        assertEquals(List.of(3, 1), ids);
        assertEquals(2, totalElements);

        assertEquals(2, titles.size());
        assertEquals(List.of("Test Post 3", "Test Post 1"), titles);
    }

    @Test
    @DisplayName("8. should not return entry using bad credentials")
    void blogEntryWithBadCredentials() {
        String token = this.getToken("NotAUser", "TestPassword");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // wrong user, existing password
        ResponseEntity<String> response1 = restTemplate
                .exchange("/api/posts/1", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response1.getStatusCode(), "Should return 401 UNAUTHORIZED");

        token = this.getToken("TestUser", "BadPassword");
        headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        entity = new HttpEntity<>(headers);

        // right user, wrong password
        ResponseEntity<String> response2 = restTemplate
                .exchange("/api/posts/1", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response2.getStatusCode(), "Should return 401 UNAUTHORIZED");
    }

    @Test
    @DisplayName("9. should not allow private entry to be viewed by non-author")
    void getBlogEntryWithNonAuthor() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // test data = BlogEntry with id 2 is private and owned by TestAdmin.
        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts/2", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");
        System.out.println("response: " + response);
    }

    @Test
    @DisplayName("10. should update existing entry with updated title and new categories")
    void updateExistingEntry() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        BlogEntryRequestDto entryToUpdate = new BlogEntryRequestDto(
                "Updated Test Post 3",
                null,
                List.of("Test Category 2", "Test Category 3"),
                null
        );

        HttpEntity<Object> request = new HttpEntity<>(entryToUpdate, headers);
        // putForEntity does not exist.
        ResponseEntity<Void> response = restTemplate
                .exchange("/api/posts/3", HttpMethod.PUT, request, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Should return 204 NO CONTENT");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> getResponse = restTemplate
                .exchange("/api/posts/3", HttpMethod.GET, entity, String.class);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String title = documentContext.read("$.title");
        assertEquals(3, id);
        assertEquals("Updated Test Post 3", title);
    }

    @Test
    @DisplayName("11. should not update non-existent entry")
    void updateNonExistingEntry() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        BlogEntryRequestDto entryToUpdate = new BlogEntryRequestDto(
                "Updated Non Existent Test Post",
                null,
                null,
                null
        );
        HttpEntity<Object> request = new HttpEntity<>(entryToUpdate, headers);

        ResponseEntity<Void> response = restTemplate
                .exchange("/api/posts/99", HttpMethod.PUT, request, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");
    }

    @Test
    @DisplayName("12. should not update entry if non-author")
    void updateNonAuthorEntry() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        BlogEntryRequestDto entryToUpdate = new BlogEntryRequestDto(
                "Updated Non Existent Test Post",
                null,
                null,
                null
        );
        HttpEntity<BlogEntryRequestDto> request = new HttpEntity<>(entryToUpdate, headers);
        ResponseEntity<Void> response = restTemplate
                .exchange("/api/posts/1", HttpMethod.PUT, request, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");
    }

    @Test
    @DisplayName("13. should delete entry")
    @DirtiesContext
    void deleteEntry() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        System.out.println("attempting to delete entry");
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("TestUser", "TestPassword")
                .exchange("/api/posts/3", HttpMethod.DELETE, request, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Should return 204 NO CONTENT");

        System.out.println("checking for deleted entry");
        ResponseEntity<String> getResponse = restTemplate
                .exchange("/api/posts/3", HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode(), "Should return 404 NOT FOUND");
    }

    @Test
    @DisplayName("14. should not delete non-existent entry")
    void deleteNonExistentEntry() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate
                .exchange("/api/posts/99", HttpMethod.DELETE, request, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");
    }

    @Test
    @DisplayName("15. should not delete entry if non-author")
    void deleteNonAuthorEntry() {
        String token = this.getToken("TestUser2", "TestPassword");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate
                .exchange("/api/posts/3", HttpMethod.DELETE, request, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");

        //token = this.getToken("TestUser2", "TestPassword");
        headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        request = new HttpEntity<>(headers);

        ResponseEntity<String> getResponse = restTemplate
                .exchange("/api/posts/3", HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode(), "Should return 200 OK");
    }

    @Test
    @DisplayName("16. category join table should be cascaded on related BlogEntry deletion")
    @DirtiesContext
    void categoryJoinTableCascade() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        Integer initialCategoryCount = countJoinTableEntries(3);
        System.out.println("initialCategoryCount: " + initialCategoryCount);
        assertEquals(1, initialCategoryCount);
        System.out.println("delete entry");
        System.out.println(restTemplate.withBasicAuth("TestUser", "TestPassword")
                .exchange("/api/posts/3", HttpMethod.DELETE, request, Void.class));

        System.out.println("checking for deleted category join table");

        Integer finalCategoryCount = countJoinTableEntries(3);
        System.out.println("finalCategoryCount: " + finalCategoryCount);
        assertEquals(0, finalCategoryCount);
    }
}
