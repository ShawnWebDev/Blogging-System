package com.webdev.bloggingsystem.user;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class AppUserDao {
    private final JdbcClient jdbc;

    public AppUserDao(JdbcClient jdbcClient) {
        this.jdbc = jdbcClient;
    }

    public Integer findUserIdByUsername(String username) {
        return jdbc.sql(
                "SELECT u.id FROM users u WHERE u.username = :username")
                .param("username", username).query(Integer.class).single();
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

    public int insert(AppUser appUser) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.sql(
                "INSERT INTO users (username, password, role, email) " +
                "VALUES (:username, :password, :role, :email)")
                    .param("username", appUser.getUsername())
                    .param("password", appUser.getPassword())
                    .param("role", appUser.getRole().name())
                    .param("email", appUser.getEmail())
                    .update(keyHolder);

        return keyHolder.getKey().intValue();
    }

    public void insertVerification(int otp, int userId, Instant expires) {
        jdbc.sql(
                "INSERT INTO verification (otp, user_id, expiry) " +
                "VALUES (:otp, :user_id, :expiry)")
                    .param("otp", otp)
                    .param("user_id", userId)
                    .param("expiry", Timestamp.from(expires))
                    .update();
    }

    public Instant getExpires(int userId) {
        return jdbc.sql(
                "SELECT expiry FROM verification " +
                "WHERE user_id = :userId")
                    .param("userId", userId)
                    .query(Instant.class).optional().orElse(null);
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
