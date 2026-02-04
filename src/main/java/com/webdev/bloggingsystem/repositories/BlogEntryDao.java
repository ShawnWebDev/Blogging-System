package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;

import com.webdev.bloggingsystem.entities.DTO.SimpleBlogEntry;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


//todo:
// -change findAll() to use pagination,
// -add findByCategory() & sortByDateUpdated()
// -add update & delete methods,
// -test

@Repository
public class BlogEntryDao {

    private final JdbcClient jdbc;

    public BlogEntryDao(JdbcClient jdbcClient) {
        this.jdbc = jdbcClient;
    }

    public int insert(BlogEntry blogEntry) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.sql("INSERT INTO blog_entries (content, title, description, created_at, updated_at, is_public, author_id) " +
                        "VALUES (:content, :title, :description, :created_at, :updated_at, :is_public, :author_id)")
                .param("content", blogEntry.getContent())
                .param("title", blogEntry.getTitle())
                .param("description", blogEntry.getDescription())
                .param("created_at", blogEntry.getCreatedAt())
                .param("updated_at", blogEntry.getUpdatedAt())
                .param("is_public", blogEntry.isPublic())
                .param("author_id", blogEntry.getAuthorId())
                .update(keyHolder);

        return keyHolder.getKey().intValue();
    }

    public Optional<BlogEntry> findById(int id) {
        return jdbc.sql("SELECT * FROM blog_entries b " +
                "WHERE b.id = :id")
                .param("id", id).query(BlogEntry.class).optional();
    }

    public List<BlogEntry> findAllFull() {
        return jdbc.sql("SELECT * FROM blog_entries")
                .query(BlogEntry.class)
                .list();
    }

    public List<SimpleBlogEntry> findAllSimple() {
        return jdbc.sql("SELECT id, title, description, created_at FROM blog_entries")
                .query(SimpleBlogEntry.class)
                .list();
    }




}
