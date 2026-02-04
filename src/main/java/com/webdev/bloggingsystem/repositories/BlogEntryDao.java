package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public class BlogEntryDao {

    private final JdbcClient jdbc;
    private final JdbcTemplate jdbcTemplate;

    public BlogEntryDao(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int insertBlogEntry(BlogEntry blogEntry, Set<Integer> categoryIds) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.sql("INSERT INTO blog_entries (content, title, date_published, date_updated, is_public, author_id) " +
                        "VALUES (:content, :title, :date_published, :date_updated, :is_public, :author_id)")
                .param("content", blogEntry.getContent())
                .param("title", blogEntry.getTitle())
                .param("date_published", blogEntry.getCreatedAt())
                .param("date_updated", blogEntry.getUpdatedAt())
                .param("is_public", blogEntry.isPublic())
                .param("author_id", blogEntry.getAuthorId())
                .update(keyHolder);

        int blogId = keyHolder.getKey().intValue();
        // todo : move to category DAO or own method here.. will need to be able to update as well
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Object[]> batchArgs = categoryIds.stream()
                    .map(catId -> new Object[]{blogId, catId})
                    .toList();

            jdbcTemplate.batchUpdate(
                    "INSERT INTO posts_categories (post_id, category_id) VALUES (?, ?)",
                    batchArgs
            );
        }

        return blogId;
    }


}
