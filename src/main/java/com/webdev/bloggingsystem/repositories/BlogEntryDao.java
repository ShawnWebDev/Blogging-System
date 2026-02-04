package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Repository
public class BlogEntryDao {

    private final JdbcClient jdbc;

    public BlogEntryDao(JdbcClient jdbcClient) {
        this.jdbc = jdbcClient;
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

        int id = keyHolder.getKey().intValue();

        StringBuilder sb = new StringBuilder("INSERT INTO posts_categories (post_id, category_id) VALUES ");
        for (Integer categoryId : categoryIds) {
            sb.append("(").append(id).append(", ").append(categoryId).append("),");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";");

        jdbc.sql(sb.toString()).update();

        return id;
    }


}
