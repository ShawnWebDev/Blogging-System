package com.webdev.bloggingsystem.integration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.webdev.bloggingsystem.dto.CategoryRequestDto;
import com.webdev.bloggingsystem.dto.CategoryResponseDto;
import com.webdev.bloggingsystem.dto.LoginDto;
import com.webdev.bloggingsystem.repositories.CategoryRepo;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CategoryControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    CategoryRepo categoryRepo;

    private String testUserToken;

    private String getToken(String username) {
        LoginDto loginDto = new LoginDto(username, "TestPassword");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        return response.getBody();
    }

    @BeforeAll
    public void beforeAll() {
        this.testUserToken = this.getToken("TestAdmin");
    }

    @Test
    @DisplayName("Get all categories")
    void getAllCategories() {
        ResponseEntity<String> allCategories = restTemplate.getForEntity("/api/categories", String.class);
        System.out.println(allCategories.getBody());

        assertEquals(HttpStatus.OK, allCategories.getStatusCode(), "Should return 200 OK");
        DocumentContext documentContext = JsonPath.parse(allCategories.getBody());
        JSONArray categoryNames = documentContext.read("$..categoryName");
        System.out.println(categoryNames);
        assertEquals(3, categoryNames.size(), "three categories should be returned");
    }

    @Test
    @DisplayName("Cannot create category without admin role")
    void createCategoryNonAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.getToken("TestUser"));

        CategoryRequestDto categoryDto = new CategoryRequestDto(
                "New category 1",
                "New category 1 description."
        );
        HttpEntity<CategoryRequestDto> request = new HttpEntity<>(categoryDto, headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/categories", HttpMethod.POST, request, String.class);

        System.out.println("response: " + response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Should return 403 FORBIDDEN");
    }

    @Test
    @DisplayName("Create category with admin role")
    @DirtiesContext
    void createCategoryAdmin() {
        CategoryRequestDto categoryDto = new CategoryRequestDto(
                "New category **",
                "New category description."
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<CategoryRequestDto> request = new HttpEntity<>(categoryDto, headers);

        ResponseEntity<CategoryResponseDto> response = restTemplate
                .exchange("/api/categories", HttpMethod.POST, request, CategoryResponseDto.class);

        System.out.println("response: " + response);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
    }

    @Test
    @DisplayName("Update category")
    void updateCategory() {
        CategoryRequestDto categoryDto = new CategoryRequestDto(
                "Updated category 1",
                "Updated category 1 - description test."
        );
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<CategoryRequestDto> request = new HttpEntity<>(categoryDto, headers);

        ResponseEntity<CategoryResponseDto> response = restTemplate
                .exchange("/api/categories/1", HttpMethod.PUT, request, CategoryResponseDto.class);

        System.out.println("response: " + response);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
    }

    @Test
    @DisplayName("Delete category")
    @DirtiesContext
    void deleteCategory() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        int count = categoryRepo.countPostsWithCategoryId(1);
        System.out.println("count of posts with category id before delete: " + count);

        ResponseEntity<Void> response = restTemplate
                .exchange("/api/categories/1", HttpMethod.DELETE, request, Void.class);

        System.out.println("response: " + response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Should return 204 NO CONTENT");

        ResponseEntity<String> allCategories = restTemplate.getForEntity("/api/categories", String.class);
        assertEquals(HttpStatus.OK, allCategories.getStatusCode(), "Should return 200 OK");

        DocumentContext documentContext = JsonPath.parse(allCategories.getBody());
        JSONArray categoryNames = documentContext.read("$..categoryName");
        System.out.println(categoryNames);
        assertEquals(2, categoryNames.size(), "two categories should be returned");

        ResponseEntity<String> blogEntryResponse = restTemplate
                .getForEntity("/api/posts?sort=createdAt,asc&categoryName=Test Category 2", String.class);

        System.out.println("blogEntryResponse: " + blogEntryResponse);

        int count2 = categoryRepo.countPostsWithCategoryId(1);
        System.out.println("count of posts with category id before delete: " + count2);
    }

    @Test
    @DisplayName("Do not create category with name over max bytes")
    void createCategoryWithNameOverMaxBytes() {
        CategoryRequestDto categoryDto = new CategoryRequestDto(
                "New category **New category **New category **New category **New category ** " +
                        "New category **New category **New category **New category **New category **" +
                        "New category **New category **New category **New category **New category **" +
                        "New category **New category **New category **New category **New category **",
                "New category description."
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<CategoryRequestDto> request = new HttpEntity<>(categoryDto, headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/categories", HttpMethod.POST, request, String.class);

        System.out.println("response: " + response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 401 BAD REQUEST");
        assertEquals("{\"categoryName\":\"Input length exceeded, max size 255\"}", response.getBody());
    }

    @Test
    @DisplayName("Do not create category with name over max bytes")
    void createCategoryWithDescriptionOverMaxBytes() {
        CategoryRequestDto categoryDto = new CategoryRequestDto(
                "New category **",
                "New category description ***** New category description ***** New category description *****" +
                        "New category description ***** New category description ***** New category description *****" +
                        "New category description ***** New category description ***** New category description *****."
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);
        HttpEntity<CategoryRequestDto> request = new HttpEntity<>(categoryDto, headers);

        ResponseEntity<String> response = restTemplate
                .exchange("/api/categories", HttpMethod.POST, request, String.class);

        System.out.println("response: " + response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 401 BAD REQUEST");
        assertEquals("{\"description\":\"Input length exceeded\"}", response.getBody());
    }

}