package com.webdev.bloggingsystem.blog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class CategoryDao {
    private final JdbcClient jdbc;
    private final JdbcTemplate jdbcTemplate;

    public CategoryDao(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(Category category) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.sql(
                "INSERT INTO categories (category_name, description) " +
                "VALUES (:categoryName, :description)")
                    .param("categoryName", category.getCategoryName())
                    .param("description", category.getDescription())
                    .update(keyHolder);

        return keyHolder.getKey().intValue();
    }

    public int batchInsertJoins(Set<Integer> categoryIds, int blogId) {
        int result = 0;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Object[]> batchArgs = categoryIds.stream()
                    .map(catId -> new Object[]{blogId, catId})
                    .toList();

            result = jdbcTemplate.batchUpdate(
                    "INSERT INTO posts_categories (post_id, category_id) VALUES (?, ?)", batchArgs).length;
        }
        return result;
    }

    public List<Category> findAll() {
        return jdbc.sql(
                "SELECT * FROM categories")
                    .query(Category.class)
                    .list();
    }

    public int update(Category category) {
        return jdbc.sql(
                "UPDATE categories " +
                "SET category_name = :categoryName, description = :description " +
                "WHERE id = :id")
                    .param("id", category.getId())
                    .param("categoryName", category.getCategoryName())
                    .param("description", category.getDescription())
                    .update();
    }
}
