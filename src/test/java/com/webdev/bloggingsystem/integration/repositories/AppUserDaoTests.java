package com.webdev.bloggingsystem.integration.repositories;

import com.webdev.bloggingsystem.user.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@JdbcTest
@ActiveProfiles("test")
@Testcontainers
@Import(AppUserDao.class)
public class AppUserDaoTests {
    @Autowired
    private AppUserDao appUserDao;

    @Container
    @ServiceConnection
    static MariaDBContainer mariadbContainer = new MariaDBContainer("mariadb:lts-ubi9");

    @Test
    void getUserByUsername() {
        Optional<AppUser> user = appUserDao.findByUsername("TestAdmin");

        assertTrue(user.isPresent());
        System.out.println("Fetched user: " + user.get());

        assertEquals("TestAdmin", user.get().getUsername());
    }

    @Test
    void getAuthorByUsername() {
        Optional<AuthorDto> author = appUserDao.findAuthorByUsername("TestAdmin");

        System.out.println("Fetched AuthorDto: " + author);

        assertTrue(author.isPresent());
        assertEquals(1, author.get().id());
    }

    @Test
    void getUserIdEmailByUsername() {
        Object[] UserIdEmail = appUserDao.getUserIdEmailByUsername("TestAdmin");

        System.out.println("Fetched UserId and Email: " + Arrays.toString(UserIdEmail));

        assertNotNull(UserIdEmail);
        assertEquals(2, UserIdEmail.length);
        assertEquals(1, (int) UserIdEmail[0]);
        assertEquals("TestAdmin@email.com", UserIdEmail[1].toString());
    }

    @Test
    void insert() {
        AppUser appUser = new AppUser(
                "InsertedUser", "$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y", "InsertedUser@email.com"
        );
        appUser.setRole(RoleType.USER);

        int userId = appUserDao.insert(appUser);

        System.out.println("Inserted user ID: " + userId);
        assertTrue(userId > 4);
        Optional<AppUser> insertedUser = appUserDao.findByUsername("InsertedUser");

        assertTrue(insertedUser.isPresent());
        assertEquals(RoleType.USER, insertedUser.get().getRole());
        assertEquals("InsertedUser@email.com",  insertedUser.get().getEmail());
    }

    @Test
    void insertNonUnique() {
        AppUser appUser = new AppUser(
                "TestAdmin", "$2a$10$bKQEC46DI0S.2SlYfTxLZOM9dabIOFaeQ/eOW8raycdxgopNVAc8y", "TestAdmin@email.com"
        );
        appUser.setRole(RoleType.ADMIN);

        assertThrows(DuplicateKeyException.class, () -> appUserDao.insert(appUser));
    }

    @Test
    void updateActiveStatus() {
        appUserDao.updateUserActive("Te5tU$er");
        Optional<AppUser> updatedUser = appUserDao.findByUsername("Te5tU$er");

        assertTrue(updatedUser.isPresent());
        System.out.println("Updated user: " + updatedUser.get());

        assertTrue(updatedUser.get().getIsActive());
        assertEquals(RoleType.USER, updatedUser.get().getRole());
        assertEquals("Te5tU$er@email.com", updatedUser.get().getEmail());
    }

    @Test
    void insertVerification() {
        appUserDao.insertVerification("$2a$10$otpEncryptedString", 1, Instant.parse("2026-05-19T10:00:00Z"), 1);
        Object[] otpDetail = appUserDao.getOtpDetailsByUsername("TestAdmin");

        System.out.println("Fetched OTP detail: " + Arrays.toString(otpDetail));
        assertNotNull(otpDetail);
        assertEquals(3, otpDetail.length);
        assertEquals(1, (int) otpDetail[0]);
        assertEquals("$2a$10$otpEncryptedString", otpDetail[1].toString());
        assertEquals(Instant.parse("2026-05-19T10:00:00Z"), otpDetail[2]);

        Instant otpExpiration = appUserDao.getOtpExpirationByUsername("TestAdmin");
        assertEquals(Instant.parse("2026-05-19T10:00:00Z"), otpExpiration);

        appUserDao.deleteOtpDetailsByUserId(1);
        otpDetail = appUserDao.getOtpDetailsByUsername("TestAdmin");
        assertNull(otpDetail);
    }

    @Test
    void getNonExistentOtpExpirationByUsername() {
        Instant expiration = appUserDao.getOtpExpirationByUsername("TestAdmin");
        assertNotNull(expiration);
        assertEquals(Instant.parse("2011-11-11T11:00:00Z"), expiration);
    }

    @Test
    void getNonExistentOtpDetailsByUsername() {
        Object[] otpDetail = appUserDao.getOtpDetailsByUsername("TestAdmin");
        System.out.println("Fetched OTP detail: " + Arrays.toString(otpDetail));
        assertNull(otpDetail);
    }

}
