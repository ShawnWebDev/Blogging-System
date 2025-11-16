package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.LoginDto;
import com.webdev.bloggingsystem.services.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> createToken(@RequestBody LoginDto loginDto) {
        logger.debug("Received request to create token for user {}", loginDto.username());
        String token = authService.login(loginDto);
        logger.debug("Created token {} ", token);
        return ResponseEntity.ok(token);
    }

}
