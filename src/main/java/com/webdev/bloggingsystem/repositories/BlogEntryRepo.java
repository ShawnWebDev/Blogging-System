package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface BlogEntryRepo extends JpaRepository<BlogEntry, Integer>, JpaSpecificationExecutor<BlogEntry> {
    @EntityGraph(value = "blog-entry-partial")
    Optional<BlogEntry> findBlogEntryById(@Param("id") int id);

    @NonNull
    @Override
    Page<BlogEntry> findAll(@Nullable Specification<BlogEntry> spec, @NonNull Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM blog_entries WHERE id = :id", nativeQuery = true)
    void deleteBlogEntryById(@Param("id") Integer id);
}