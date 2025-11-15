package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlogEntryRepo extends JpaRepository<BlogEntry, Integer> {
    @EntityGraph(value = "blog-entry-full", type = EntityGraph.EntityGraphType.LOAD)
    Optional<BlogEntry> findBlogEntryById(Integer id);

    @EntityGraph(value = "blog-entry-partial", type = EntityGraph.EntityGraphType.LOAD)
    Optional<BlogEntry> findBlogEntryByIdAndAuthorUsername(Integer id, String authorUsername);

    @EntityGraph(value = "blog-entry-with-author", type = EntityGraph.EntityGraphType.LOAD)
    Optional<BlogEntry> findSimpleBlogEntryById(Integer id);

    @EntityGraph(value = "blog-entry-with-author", type = EntityGraph.EntityGraphType.LOAD)
    Optional<BlogEntry> findSimpleBlogEntryByIdAndAuthorUsername(Integer id, String authorUsername);

    @EntityGraph(value = "blog-entry-partial", type =  EntityGraph.EntityGraphType.LOAD)
    Page<BlogEntry> findAllByIsPublicTrue(Pageable pageable);

    @EntityGraph(value = "blog-entry-partial", type =  EntityGraph.EntityGraphType.LOAD)
    Page<BlogEntry> findAllBlogEntryByAuthorUsername(Pageable pageable, String authorUsername);

    @Modifying
    @Query(value = "DELETE FROM blog_entries WHERE id = :id", nativeQuery = true)
    void deleteBlogEntryById(@Param("id") Integer id);
}