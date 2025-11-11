package com.webdev.bloggingsystem.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class AppUserTest {
    private AppUser appUser;
    private BlogEntry mockBlogEntry;

    @BeforeEach
    public void setUp() {
        appUser = new AppUser();
        appUser.setId(1);
        appUser.setUsername("testUsername");
        appUser.setPassword("testPassword");
        appUser.setEmail("testEmail.com");

        mockBlogEntry = Mockito.mock(BlogEntry.class);
        Mockito.when(mockBlogEntry.getId()).thenReturn(1);
    }

    @Test
    public void testConstructor() {
        AppUser testUser = new AppUser(
                "testUsername",
                "testPassword",
                "testEmail.com"
        );
        testUser.setId(1);

        assertEquals("testUsername", testUser.getUsername());
        assertEquals("testPassword", testUser.getPassword());
        assertEquals("testEmail.com", testUser.getEmail());
    }

    @Test
    @DisplayName("2. appUser entities are equal with same ID")
    public void testEquals() {
        AppUser testUser = new AppUser();
        testUser.setId(1);

        assertEquals(testUser, appUser);
        assertEquals(appUser, testUser);
    }

    @Test
    @DisplayName("3. appUser entities are not equal with null IDs")
    public void testNotEquals() {
        AppUser testUser = new AppUser();
        appUser.setId(null);

        assertNotEquals(testUser, appUser);
        assertNotEquals(appUser, testUser);
    }

    @Test
    @DisplayName("4. null id should have hash of 31 and revert to id when persisted")
    public void testNullIdHashCode() {
        AppUser testUser = new AppUser();
        assertEquals(31, testUser.hashCode());

        testUser.setId(1);
        assertEquals(1, testUser.hashCode());
    }

    @Test
    @DisplayName("5. consistent hashcode to id")
    public void testConsistentHashcode() {
        AppUser testUser = new AppUser();
        testUser.setId(1);
        assertEquals(1, testUser.hashCode());
        assertEquals(Integer.valueOf(1).hashCode(), testUser.hashCode());
    }

}
