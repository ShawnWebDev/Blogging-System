package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.entities.LoginDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // <-- for H2 testing, comment out for MySQL
public class AuthControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testAuthLogin() {
        LoginDto loginDto = new LoginDto("TestAdmin", "TestPassword");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testAuthLoginBadCredentials() {
        LoginDto loginDto = new LoginDto("NotATestAdmin", "TestPassword");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

}
