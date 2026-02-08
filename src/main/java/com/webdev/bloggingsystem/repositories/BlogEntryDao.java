package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.DTO.SimpleBlogEntry;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class BlogEntryDao {

    private final JdbcClient jdbc;

    public BlogEntryDao(JdbcClient jdbcClient) {
        this.jdbc = jdbcClient;
    }

    public int insert(BlogEntry blogEntry) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.sql(
                "INSERT INTO blog_entries (content, title, description, created_at, updated_at, author_id) " +
                "VALUES (:content, :title, :description, :created_at, :updated_at, :author_id)")
                    .param("content", blogEntry.getContent())
                    .param("title", blogEntry.getTitle())
                    .param("description", blogEntry.getDescription())
                    .param("created_at", blogEntry.getCreatedAt())
                    .param("updated_at", blogEntry.getUpdatedAt())
                    .param("author_id", blogEntry.getAuthorId())
                    .update(keyHolder);

        return keyHolder.getKey().intValue();
    }

    public Optional<BlogEntry> findById(int id) {
        return jdbc.sql(
                "SELECT * FROM blog_entries b " +
                "WHERE b.id = :id")
                    .param("id", id)
                    .query(BlogEntry.class)
                    .optional();
    }

    public int count() {
        return jdbc.sql("SELECT count(b.id) FROM blog_entries b")
                .query(Integer.class)
                .single();
    }

    public List<SimpleBlogEntry> findAllSimple(int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        return jdbc.sql(
                "SELECT b.id, b.title, b.description, b.created_at, " +
                    "GROUP_CONCAT(c.category_name ORDER BY c.category_name ASC) AS category_list " +
                "FROM blog_entries b " +
                "JOIN posts_categories pc ON pc.post_id = b.id " +
                "JOIN categories c ON c.id = pc.category_id " +
                "GROUP BY b.id " +
                "ORDER BY b.id " +
                "LIMIT :pageSize OFFSET :offset")
                    .param("pageSize", pageSize)
                    .param("offset", offset)
                    .query((rs, _) -> simpleBlogEntryExtractor(rs))
                    .list();
    }

    public List<SimpleBlogEntry> findAllSimpleBlogEntriesToCategoryName(String categoryName, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        // first, the subquery limits selection to only post_ids that have relation to specified category_name.
        // second, columns are selected and joined to a concatenated string of all grouped category_names related to those post_ids.
        return jdbc.sql(
                "SELECT b.id, b.title, b.description, b.created_at, " +
                    "GROUP_CONCAT(c.category_name ORDER BY c.category_name ASC) AS category_list " +
                "FROM blog_entries b " +
                "JOIN posts_categories pc ON pc.post_id = b.id " +
                "JOIN categories c ON c.id = pc.category_id " +
                "WHERE b.id IN (" +
                    "SELECT pc_sub.post_id FROM posts_categories pc_sub " +
                    "JOIN categories c_sub ON c_sub.id = pc_sub.category_id " +
                    "WHERE c_sub.category_name = :categoryName) " +
                "GROUP BY b.id " +
                "ORDER BY b.id " +
                "LIMIT :pageSize OFFSET :offset")
                    .param("categoryName", categoryName)
                    .param("pageSize", pageSize)
                    .param("offset", offset)
                    .query((rs, _) -> simpleBlogEntryExtractor(rs))
                    .list();
    }

    public int update(BlogEntry blogEntry) {
        return jdbc.sql(
                "UPDATE blog_entries " +
                "SET title = :title, description = :description, content = :content, updated_at = :updatedAt " +
                "WHERE id = :id")
                    .param("id", blogEntry.getId())
                    .param("title", blogEntry.getTitle())
                    .param("description", blogEntry.getDescription())
                    .param("content", blogEntry.getContent())
                    .param("updatedAt", Instant.now())
                    .update();
    }

    public int deleteById(int id) {
        return jdbc.sql(
                "DELETE from blog_entries WHERE id = :id")
                    .param("id", id)
                    .update();
    }

    private static SimpleBlogEntry simpleBlogEntryExtractor(ResultSet rs) throws SQLException {
        return new SimpleBlogEntry(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toInstant(),
                List.of(rs.getString("category_list").split(","))
        );
    }
}
