package com.webdev.bloggingsystem.user;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AppUserDao {
    private final JdbcClient jdbc;

    public AppUserDao(JdbcClient jdbcClient) {
        this.jdbc = jdbcClient;
    }

    public Object[] getUserIdEmailByUsername(String username) {
        return jdbc.sql(
                "SELECT u.id, u.email FROM users u WHERE u.username = :username")
                    .param("username", username)
                    .query((rs, _) -> this.getUserIdEmailByUsernameExtractor(rs))
                    .optional().orElse(null);
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

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public void updateUserActive(String username) {
        jdbc.sql(
                "UPDATE users SET is_active = 1 " +
                "WHERE username = :username")
                    .param("username", username)
                    .update();
    }

    public void insertVerification(String otp, int userId, Instant expires) {
        jdbc.sql(
                "INSERT INTO verification (otp, user_id, expiry) " +
                "VALUES (:otp, :user_id, :expiry)")
                    .param("otp", otp)
                    .param("user_id", userId)
                    .param("expiry", Timestamp.from(expires))
                    .update();
    }

    public Instant getOtpExpirationByUsername(String username) {
        return jdbc.sql(
                "SELECT v.expiry FROM users u " +
                "JOIN verification v on u.id = v.user_id " +
                "WHERE u.username = :username")
                    .param("username", username)
                    .query(Instant.class)
                    .optional().orElse(Instant.now());
    }

    public Object[] getOtpDetailsByUsername(String username) {
        return jdbc.sql(
                "SELECT v.user_id, v.otp, v.expiry FROM users u " +
                "JOIN verification v on u.id = v.user_id " +
                "WHERE u.username = :username")
                    .param("username", username)
                    .query((rs, _) -> this.getOtpDetailsExtractor(rs))
                    .optional().orElse(null);
    }

    public void deleteOtpDetailsByUserId(int userId) {
        jdbc.sql(
                "DELETE from verification WHERE user_id = :user_id")
                    .param("user_id", userId)
                    .update();
    }

    public boolean existsByUsername(String username) {
        return jdbc.sql(
                "SELECT 1 from users u WHERE u.username = :username")
                    .param("username", username)
                    .query(Integer.class)
                    .optional().isPresent();
    }

    public boolean existsByEmail(String email) {
        return jdbc.sql(
                "SELECT 1 from users u WHERE u.email = :email")
                    .param("email", email)
                    .query(Integer.class).optional()
                    .isPresent();
    }

    private Object[] getOtpDetailsExtractor(ResultSet rs) throws SQLException {
        Object[] result = new Object[3];
        result[0] = rs.getInt("user_id");
        result[1] = rs.getString("otp");
        result[2] = rs.getTimestamp("expiry").toInstant();
        return result;
    }

    private Object[] getUserIdEmailByUsernameExtractor(ResultSet rs) throws SQLException {
        Object[] result = new Object[2];
        result[0] = rs.getInt("id");
        result[1] = rs.getString("email");
        return result;
    }

}
