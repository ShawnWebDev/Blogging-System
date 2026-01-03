package com.webdev.bloggingsystem.integration;

import com.webdev.bloggingsystem.dto.LoginDto;
import com.webdev.bloggingsystem.dto.RegisterDto;
import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // <-- for H2 testing
public class AuthControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private AppUserRepo appUserRepo;

    @Test
    public void testAuthLogin() {
        LoginDto loginDto = new LoginDto("TestAdmin", "TestPassword");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testAuthLoginBadUsername() {
        LoginDto loginDto = new LoginDto("NotATestAdmin", "TestPassword");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testAuthLoginBadPassword() {
        LoginDto loginDto = new LoginDto("TestAdmin", "NotAPassword");

        ResponseEntity<String> response = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testRegisterUser() {
        RegisterDto registerDto = new RegisterDto(
                "Te5tU$er3", "TestPa5$word", "TestEmail@email.com");

        ResponseEntity<String> registerResponse = restTemplate
                .postForEntity("/auth/register", registerDto, String.class);

        System.out.println(registerResponse);
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());

        Optional<AppUser> appUser = appUserRepo.findAppUserAndRolesByUsername("Te5tU$er3");
        assert(appUser.isPresent());

        LoginDto loginDto = new LoginDto("Te5tU$er3", "TestPa5$word");

        ResponseEntity<String> loginResponse = restTemplate
                .postForEntity("/auth/login", loginDto, String.class);

        System.out.println(loginResponse);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    }

    @Test
    public void testRegisterUserBadEmail() {
        RegisterDto registerDto = new RegisterDto(
                "Te5tU$er3", "TestPa5$word", "TestBadEmail");

        ResponseEntity<String> registerResponse = restTemplate
                .postForEntity("/auth/register", registerDto, String.class);

        System.out.println(registerResponse);
        assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());
    }

    @Test
    public void testRegisterUserTooLongPassword() {
        RegisterDto registerDto = new RegisterDto(
                "Te5tU$er3", "TestPasswordTestPasswordTestPasswordTestPasswordTestPasswordTestPasswordTestPassword55*", "TestEmail@email.com");

        ResponseEntity<String> registerResponse = restTemplate
                .postForEntity("/auth/register", registerDto, String.class);

        System.out.println(registerResponse);
        assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());
    }

    @Test
    public void testRegisterUserTooLongUsername() {
        RegisterDto registerDto = new RegisterDto(
                "TestRegisterTestRegisterTestRegisterTestRegister", "TestPa5$word", "TestEmail@email.com");

        ResponseEntity<String> registerResponse = restTemplate
                .postForEntity("/auth/register", registerDto, String.class);

        System.out.println(registerResponse);
        assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());
    }

    @Test
    public void testRegisterUsernameInvalidCharacters() {
        RegisterDto registerDto = new RegisterDto(
                "TestUser", "TestPa5$word", "TestEmail@email.com");

        ResponseEntity<String> registerResponse = restTemplate
                .postForEntity("/auth/register", registerDto, String.class);

        System.out.println(registerResponse);
        assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());
    }

    @Test
    public void testRegisterPasswordInvalidCharacters() {
        RegisterDto registerDto = new RegisterDto(
                "Te5tU$er3", "TestPa5$word ", "TestEmail@email.com");

        ResponseEntity<String> registerResponse = restTemplate
                .postForEntity("/auth/register", registerDto, String.class);

        System.out.println(registerResponse);
        assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());
    }

    @Test
    public void testRegisterNonUniqueUsername() {
        RegisterDto registerDto = new RegisterDto(
                "Te5tU$er", "TestPa5$word", "TestEmail@email.com");

        ResponseEntity<String> registerResponse = restTemplate
                .postForEntity("/auth/register", registerDto, String.class);

        System.out.println(registerResponse);
        assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());
    }

    @Test
    public void testRegisterBadInputs() {
        RegisterDto registerDto = new RegisterDto(
                "Test User", "TestPassword", "TestBadEmail");

        ResponseEntity<String> registerResponse = restTemplate
                .postForEntity("/auth/register", registerDto, String.class);

        System.out.println(registerResponse);
        assertEquals(HttpStatus.BAD_REQUEST, registerResponse.getStatusCode());
    }
}
