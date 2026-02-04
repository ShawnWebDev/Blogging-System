package com.webdev.bloggingsystem.integration.repositories;


import com.webdev.bloggingsystem.entities.AppUser;

import com.webdev.bloggingsystem.entities.Author;
import com.webdev.bloggingsystem.repositories.AppUserDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Import(AppUserDao.class)
public class UserDaoTest extends BaseRepoTest {

    @Autowired
    private AppUserDao appUserDao;

    @Test
    void getUserByUsername() {
        Optional<AppUser> user = appUserDao.findByUsername("TestAdmin");

        System.out.println(user);
        Assertions.assertTrue(user.isPresent());
        Assertions.assertEquals("TestAdmin", user.get().getUsername());
    }

    @Test
    void getAuthorById() {
        Optional<Author> author = appUserDao.findAuthorById(1);

        System.out.println(author);
        Assertions.assertTrue(author.isPresent());
        Assertions.assertEquals("TestAdmin", author.get().username());
    }


}
