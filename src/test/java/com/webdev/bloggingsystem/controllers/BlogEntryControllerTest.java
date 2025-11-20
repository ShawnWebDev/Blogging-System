package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.BlogEntryRequestDto;
import com.webdev.bloggingsystem.entities.Comment;
import com.webdev.bloggingsystem.dto.LoginDto;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.webdev.bloggingsystem.repositories.CommentRepo;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private CommentRepo commentRepo;

    // only for TestUser as it is the most used
    private String testUserToken;

    private Integer countCategoriesJoinTableEntries(Integer postId) {
        // for join table with no repository, only used to check if cascade works when deleting Entry
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posts_categories WHERE post_id = ?",
                Integer.class,
                postId
        );
    }

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
    @DisplayName("1. found id")
    void getBlogEntryById() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts/1", String.class);

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
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts/99", String.class);

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
                "This entry is for testing the Http POST method and requires at least 300 characters. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                List.of("Test Category 1", "Test Category 2"),
                true
        );
        HttpEntity<Object> postEntity = new HttpEntity<>(blogEntryRequestDto, headers);

        ResponseEntity<Void> response = restTemplate
                .postForEntity("/api/posts", postEntity, Void.class);

        System.out.println("response: " + response);

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
        assertEquals("This entry is for testing the Http POST method and requires at least 300 characters. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                , content);
        assertEquals("Test Category 1", categories.getFirst());
        assertEquals("Test Category 2", categories.get(1));
    }

    @Test
    @DisplayName("4. should return all public BlogEntries")
    void getAllPublicBlogEntries() {
        //credentials not needed for public Entries
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts?sort=createdAt,asc", String.class);

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
    void getPublicBlogEntryAsPage() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts?page=0&size=1", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        System.out.println("response: " + response.getBody());
    }

    @Test
    @DisplayName("6. should return sorted page of BlogEntries (first entry id by date = 1")
    void getBlogEntryAsSortedPage() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts?page=0&size=1&sort=createdAt,asc", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        System.out.println("json: " + documentContext.jsonString());
        String title  = documentContext.read("$.entries[0].title");
        int id  = documentContext.read("$.entries[0].id");
        assertEquals(1, id);
        assertEquals("Test Post 1", title);
    }

    @Test
    @DisplayName("7. should return sorted page using default pageable (descending sort by updatedAt)")
    void getPublicBlogEntryAsSortedPageUsingDefaultPageable() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts", String.class);

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
    @DisplayName("8. should return entry without credentials")
    void getBlogEntryWithNoCredentials() {
        // wrong user, existing password
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts/1", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        System.out.println("response: " + response.getBody());
    }

    @Test
    @DisplayName("9. should NOT allow private entry to be viewed by non-author")
    void getPrivateBlogEntryWithNonAuthor() {
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
                "This entry is for testing the Http POST method and requires at least 300 characters. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                List.of("Test Category 2", "Test Category 3"),
                true
        );

        HttpEntity<Object> request = new HttpEntity<>(entryToUpdate, headers);

        ParameterizedTypeReference<Map<String, String>> responseType =
                new ParameterizedTypeReference<>() {};
        // putForEntity does not exist.
        ResponseEntity<Map<String, String>> response = restTemplate
                .exchange("/api/posts/3", HttpMethod.PUT, request, responseType);
        System.out.println("response: " + response);

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
                "Updated Non-existent Post",
                "This entry is for testing the Http POST method and requires at least 300 characters. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                List.of("Test Category 2", "Test Category 3"),
                true
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
                "Updated Non-existent Post",
                "This entry is for testing the Http POST method and requires at least 300 characters. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                List.of("Test Category 2", "Test Category 3"),
                true
        );
        HttpEntity<BlogEntryRequestDto> request = new HttpEntity<>(entryToUpdate, headers);
        ResponseEntity<Void> response = restTemplate
                .exchange("/api/posts/1", HttpMethod.PUT, request, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");
        System.out.println("response: " + response);
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
                .exchange("/api/posts/3", HttpMethod.DELETE, request, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Should return 204 NO CONTENT");

        System.out.println("checking for deleted entry");
        ResponseEntity<String> getResponse = restTemplate
                .exchange("/api/posts/3", HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode(), "Should return 404 NOT FOUND");

        System.out.println("Checking associated comments are also deleted...");
        List<Comment> comments = commentRepo.findAllByBlogEntryId(3);
        assertEquals(0, comments.size());
        System.out.println("comments: " + comments);
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
        String token = this.getToken("TestUser2");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        System.out.println("attempting to delete entry");
        ResponseEntity<Void> response = restTemplate
                .exchange("/api/posts/3", HttpMethod.DELETE, request, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");

        //token = this.getToken("TestUser2", "TestPassword");
        headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        request = new HttpEntity<>(headers);

        System.out.println("Checking entry is not deleted...");
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

        Integer initialCategoryCount = this.countCategoriesJoinTableEntries(3);
        System.out.println("initialCategoryCount: " + initialCategoryCount);
        assertEquals(2, initialCategoryCount);
        System.out.println("delete entry");
        System.out.println(restTemplate
                .exchange("/api/posts/3", HttpMethod.DELETE, request, Void.class));

        System.out.println("checking for deleted category join table");

        Integer finalCategoryCount = this.countCategoriesJoinTableEntries(3);
        System.out.println("finalCategoryCount: " + finalCategoryCount);
        assertEquals(0, finalCategoryCount);
    }

    @Test
    @DisplayName("17. should return all BlogEntries for authenticated TestAdmin sorted ascending by createdAt")
    void getAllBlogEntriesForAdmin() {
        HttpHeaders headers = new HttpHeaders();
        String token = this.getToken("TestAdmin");
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts/me?sort=createdAt,asc", HttpMethod.GET, entity, String.class);
        System.out.println("response: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // double dot .. to return list of all values of specified key
        JSONArray ids = documentContext.read("$..id");
        JSONArray titles = documentContext.read("$..title");

        // Entry with id 2 is private and should be included
        assertEquals(2, ids.size());
        assertEquals(List.of(1, 2), ids);

        assertEquals(2, titles.size());
        assertEquals(List.of("Test Post 1", "Test Post 2"), titles);
    }

    @Test
    @DisplayName("18. should return all public BlogEntries with category of Test Category 2")
    void getAllPublicBlogEntriesFilteredByCategory() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts?sort=createdAt,asc&categoryName=Test Category 2", String.class);
        System.out.println("response: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
    }

    @Test
    @DisplayName("19. should return BAD REQUEST and validation errors for POST endpoint")
    void checkCreateValidationErrors() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        BlogEntryRequestDto blogEntryRequestDto = new BlogEntryRequestDto(
                "",
                "This requires at least 300 characters.",
                List.of(),
                true
        );
        HttpEntity<Object> postEntity = new HttpEntity<>(blogEntryRequestDto, headers);
        ParameterizedTypeReference<Map<String, String>> responseType =
                new ParameterizedTypeReference<>() {};

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                "/api/posts", HttpMethod.POST, postEntity, responseType
        );
        System.out.println("response: " + response);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 400 BAD REQUEST");
        Map<String, String> errors = response.getBody();
        assertNotNull(errors);
        assertEquals("Title must be between 3 and 255 characters", errors.get("title"));
        assertEquals("Content must be between 300 and 65,535 characters", errors.get("content"));
        assertEquals("Post must have between 1 and 4 categories", errors.get("categories"));
    }

    @Test
    @DisplayName("20. should return BAD REQUEST and validation errors for PUT endpoint")
    void checkUpdateValidationErrors() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        BlogEntryRequestDto blogEntryRequestDto = new BlogEntryRequestDto(
                "",
                "This requires at least 300 characters.",
                List.of(),
                true
        );
        HttpEntity<Object> putEntity = new HttpEntity<>(blogEntryRequestDto, headers);
        ParameterizedTypeReference<Map<String, String>> responseType =
                new ParameterizedTypeReference<>() {};

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                "/api/posts/3", HttpMethod.PUT, putEntity, responseType
        );

        System.out.println("response: " + response);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 400 BAD REQUEST");
        Map<String, String> errors = response.getBody();
        assertNotNull(errors);
        assertEquals("Title must be between 3 and 255 characters", errors.get("title"));
        assertEquals("Content must be between 300 and 65,535 characters", errors.get("content"));
        assertEquals("Post must have between 1 and 4 categories", errors.get("categories"));
    }

    @Test
    @DisplayName("21. should return user not found")
    void getAllPrivateBlogEntriesForUnknownUser() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts/me?sort=createdAt,asc", String.class);
        System.out.println("response: " + response);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");
    }

    @Test
    @DisplayName("22. should return public BlogEntries after date")
    void getAllPublicBlogEntriesAfterDate() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts?afterDate=2025-09-30", String.class);
        System.out.println("response: " + response);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray ids = documentContext.read("$..id");
        assertEquals(List.of(3, 1), ids);
    }

    @Test
    @DisplayName("23. should BAD_REQUEST and invalid date format")
    void getAllPublicBlogEntriesAfterDateBadDateFormat() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/api/posts?afterDate=2025-09-30T04:00:00", String.class);
        System.out.println("response: " + response);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 404 BAD REQUEST");
        assertEquals("Invalid Date Format - must be yyyy-mm-dd", response.getBody());
    }

    @Test
    @DisplayName("24. should return missing categories requested")
    void createBlogEntryWithMissingCategories() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        BlogEntryRequestDto blogEntryRequestDto = new BlogEntryRequestDto(
                "Testing Http POST",
                "This entry is for testing the Http POST method and requires at least 300 characters. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                List.of("Test Category 7", "Test Category 8"),
                true
        );
        HttpEntity<Object> postEntity = new HttpEntity<>(blogEntryRequestDto, headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/posts", HttpMethod.POST, postEntity, String.class);

        System.out.println("response: " + response);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 NOT FOUND");
        assertEquals("Categories not found: [Test Category 7, Test Category 8]", response.getBody());
    }

}