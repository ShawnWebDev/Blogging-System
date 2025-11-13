package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlogEntryRepo extends JpaRepository<BlogEntry, Integer> {
    @Query(value = "SELECT b FROM BlogEntry b " +
            "JOIN FETCH b.author JOIN FETCH b.categories " +
            "WHERE b.id = :id AND b.author.username = :authorUsername")
    Optional<BlogEntry> findBlogEntryByIdAndAuthorUsername(Integer id, String authorUsername);

    @Query(value = "SELECT b FROM BlogEntry b " +
            "JOIN FETCH b.author JOIN FETCH b.categories LEFT JOIN FETCH b.comments " +
            "WHERE b.id = :id")
    Optional<BlogEntry> findBlogEntryById(Integer id);

    Optional<BlogEntry> findSimpleBlogEntryById(Integer id);

    // todo n+1 problem with loading categories
    @Query(value = "SELECT b from BlogEntry b " +
            "JOIN FETCH b.author JOIN FETCH b.categories " +
            "WHERE b.isPublic = true",
            countQuery = "SELECT count(b) FROM BlogEntry b WHERE b.isPublic = true")
    Page<BlogEntry> findAllByIsPublicTrue(Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM blog_entries WHERE id = :id", nativeQuery = true)
    void betterDeleteById(@Param("id") Integer id);
}
