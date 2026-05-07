package com.webdev.bloggingsystem.user;

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
                "SELECT u.id, u.username, u.password, u.email, u.is_active, u.role " +
                        "FROM users u " +
                        "WHERE u.username = :username")
                .param("username", username)
                .query(AppUser.class).optional();
    }

    public Optional<AuthorDto> findAuthorByUsername(String username) {
        return jdbc.sql(
                "SELECT u.id, u.username " +
                        "FROM users u " +
                        "WHERE u.username = :username")
                .param("username", username)
                .query(AuthorDto.class).optional();
    }

    public boolean existsByUsername(String username) {
        return jdbc.sql(
                "SELECT 1 from users u WHERE u.username = :username")
                .param("username", username)
                .query(Integer.class).optional()
                .isPresent();
    }

    public boolean existsByEmail(String email) {
        return jdbc.sql(
                        "SELECT 1 from users u WHERE u.email = :email")
                .param("email", email)
                .query(Integer.class).optional()
                .isPresent();
    }




}
