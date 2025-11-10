package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.entities.*;
import com.webdev.bloggingsystem.repositories.AppUserRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // <-- for H2 testing, change to runtime for MySQL
public class AuthControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private AppUserRepo appUserRepo;

    private String getJwtToken(String username, String password) {
        LoginDto loginDto = new LoginDto(username, password);

        ResponseEntity<AuthResponseDto> response = restTemplate
                .postForEntity("/api/auth/login", loginDto, AuthResponseDto.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Login failed for user: " + username);
        }
        System.out.println("User Logged In As : " + username);
        return response.getBody().accessToken();
    }

    @Test
    @DisplayName("1. admin should be allowed to register user")
    public void registerUserAsAdmin() {
        String token = this.getJwtToken("TestAdmin", "TestPassword");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        RegistrationDto registrationDto = new RegistrationDto(
                "RegisterTest", "TestPassword", "TestEmail@email.com");
        HttpEntity<RegistrationDto> entity = new HttpEntity<>(registrationDto, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity("/api/auth/register", entity, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("User Registration Successful", response.getBody());

        Optional<AppUser> appUser = appUserRepo.findByUsername("RegisterTest");
        Assertions.assertTrue(appUser.isPresent());
        System.out.println("New Registered User: " + appUser.get());
        System.out.println(response);
    }

    @Test
    @DisplayName("2. user should not be allowed to register user")
    public void registerUserAsUser() {
        String token = this.getJwtToken("TestUser", "TestPassword");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        RegistrationDto registrationDto = new RegistrationDto(
                "RegisterTest", "TestPassword", "TestEmail@email.com");

        HttpEntity<RegistrationDto> entity = new HttpEntity<>(registrationDto, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity("/api/auth/register", entity, String.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        Optional<AppUser> appUser = appUserRepo.findByUsername("RegisterTest");
        Assertions.assertFalse(appUser.isPresent());
        System.out.println(response);
    }

    @Test
    @DisplayName("3. should not allow user to be registered with already used username")
    public void registerUserUsedUsername() {
        String token = this.getJwtToken("TestUser", "TestPassword");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        RegistrationDto registrationDto = new RegistrationDto(
                "TestUser", "TestPassword", "TestEmail@email.com");

        HttpEntity<RegistrationDto> entity = new HttpEntity<>(registrationDto, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity("/api/auth/register", entity, String.class);

        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        System.out.println(response);
    }

    @Test
    @DisplayName("4. should login existing user")
    public void loginUser() {
        LoginDto loginDto = new LoginDto("TestUser", "TestPassword");

        ResponseEntity<AuthResponseDto> response = restTemplate
                .postForEntity("/api/auth/login", loginDto, AuthResponseDto.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        //Assertions.assertEquals("Login Successful", response.getBody());
        System.out.println(response);
    }

    @Test
    @DisplayName("5. should not login existing user with incorrect password")
    public void loginUserBadPassword() {
        LoginDto loginDto = new LoginDto("TestUser", "BadPassword");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/api/auth/login", loginDto, String.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println(response);
    }

    @Test
    @DisplayName("6. should not login user with non-existent username")
    public void loginUserBadUsername() {
        LoginDto loginDto = new LoginDto("BadUser", "TestPassword");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/api/auth/login", loginDto, String.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        System.out.println(response);
    }
}
