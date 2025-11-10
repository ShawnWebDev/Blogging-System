package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.entities.LoginDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // <-- for H2 testing, comment out for MySQL
public class AuthControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    public String getToken(String username, String password) {
        LoginDto loginDto = new LoginDto(username, password);

        ResponseEntity<String> response = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        return response.getBody();
    }

    @Test
    public void testAuth() {
        System.out.println(this.getToken("TestAdmin", "TestPassword"));
    }

}
