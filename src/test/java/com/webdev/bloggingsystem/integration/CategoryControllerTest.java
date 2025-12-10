package com.webdev.bloggingsystem.integration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.webdev.bloggingsystem.dto.CategoryRequestDto;
import com.webdev.bloggingsystem.dto.LoginDto;
import com.webdev.bloggingsystem.services.CategoryService;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CategoryControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CategoryService categoryService;

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

    // todo continue here.
    @Test
    @DisplayName("Create category")
    void createCategory() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.testUserToken);

        CategoryRequestDto categoryDto = new CategoryRequestDto(
                "New category 1",
                "New category 1 description."
        );
        HttpEntity<CategoryRequestDto> request = new HttpEntity<>(categoryDto, headers);

        ResponseEntity<Void> response = restTemplate
                .exchange("/api/categories", HttpMethod.POST, request, Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Should return 201 CREATED");
    }

    @Test
    @DisplayName("Update category")
    void updateCategory() {}

    @Test
    @DisplayName("Delete category")
    void deleteCategory() {}

}