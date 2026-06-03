package com.webdev.bloggingsystem.blog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CategoryDao {
    private final JdbcClient jdbc;
    private final JdbcTemplate jdbcTemplate; //<<- needed as JdbcClient does not have batch support..

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

        if (keyHolder.getKey() == null) throw new RuntimeException("Insert failure, key is null!");
        return keyHolder.getKey().intValue();
    }

    public boolean existsByNameAndNotId(String name, Integer id) {
        String sql = "SELECT 1 FROM categories b WHERE b.category_name = :name";
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        if (id != null) {
            sql = sql + " AND NOT b.id = :id ";
            params.put("id", id);
        }
        sql = sql + " LIMIT 1";
        return jdbc.sql(sql)
                .params(params)
                .query(Integer.class)
                .optional().isPresent();
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

    public Optional<Category> findCategoryById(int id) {
        return jdbc.sql(
                "SELECT * FROM categories " +
                "WHERE id = :id")
                .param("id", id)
                .query(Category.class)
                .optional();
    }

    public List<Category> findAll() {
        return jdbc.sql(
                "SELECT * FROM categories")
                    .query(Category.class)
                    .list();
    }

    public List<SimpleCategoryDto> findAllNames() {
        return jdbc.sql(
                "SELECT id, category_name AS name FROM categories ORDER BY name")
                    .query(SimpleCategoryDto.class)
                    .list();
    }

    public List<Integer> findAllIdsInNames(List<String> categoryNames) {
        return jdbc.sql(
               "SELECT c.id FROM categories c " +
               "WHERE category_name IN (:names)")
                    .param("names", categoryNames)
                    .query(Integer.class)
                    .list();
    }

    public String findCategoryDescriptionByName(String categoryName) {
        return jdbc.sql(
                "SELECT c.description FROM categories c " +
                "WHERE category_name = :categoryName")
                    .param("categoryName", categoryName)
                    .query(String.class)
                    .optional().orElse("No description available.");
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

    public void deleteCategoryById(int id) {
        jdbc.sql(
                "DELETE FROM categories " +
                "WHERE id = :id")
                .param("id", id)
                .update();
    }

    // used in updates to delete and re-insert many-to-many relations
    public void deleteJoinedByBlogId(int blogId) {
        jdbc.sql(
                "DELETE FROM posts_categories " +
                "WHERE post_id = :blogId")
                    .param("blogId", blogId)
                    .update();
    }

}
