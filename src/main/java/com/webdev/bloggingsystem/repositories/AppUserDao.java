package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.entities.DTO.Author;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AppUserDao {
    private final JdbcClient jdbc;

    public AppUserDao(JdbcClient jdbcClient) {
        this.jdbc = jdbcClient;
    }

    public Optional<AppUser> findByUsername(String username) {
        return jdbc.sql(
                "SELECT u.id, u.username, u.password, u.is_active, u.role " +
                        "FROM users u " +
                        "WHERE u.username = :username"
        ).param("username", username).query(AppUser.class).optional();
    }

    public Optional<Author> findAuthorById(int id) {
        return jdbc.sql(
                "SELECT u.id, u.username " +
                        "FROM users u " +
                        "WHERE u.id = :id"
        ).param("id", id).query(Author.class).optional();
    }



}
