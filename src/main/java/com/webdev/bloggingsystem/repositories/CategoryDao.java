package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
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

    public int batchInsertJoins(Set<Integer> categoryIds, int blogId) {
        int result = 0;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Object[]> batchArgs = categoryIds.stream()
                    .map(catId -> new Object[]{blogId, catId})
                    .toList();

            result = jdbcTemplate.batchUpdate(
            "INSERT INTO posts_categories (post_id, category_id) VALUES (?, ?)",
            batchArgs).length;
        }
        return result;
    }

    public List<Category> findAllCategoriesToBlogId(int blogId) {
        return jdbc.sql(
                "SELECT * FROM categories c " +
                "JOIN posts_categories pc ON pc.category_id = c.id " +
                "WHERE pc.post_id = :blogId ORDER BY c.id")
                .param("blogId", blogId)
                .query(Category.class)
                .list();
    }

    public List<Category> findAllCategoriesIn(Set<Integer> categoryIds) {
        return jdbc.sql(
                "SELECT * FROM categories c " +
                        "WHERE c.id IN (:categoryIds) ORDER BY c.id ")
                .param("categoryIds", categoryIds)
                .query(Category.class)
                .list();
    }
}
