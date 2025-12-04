package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.BlogEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface BlogEntryRepo extends PagingAndSortingRepository<BlogEntry, Integer>, JpaSpecificationExecutor<BlogEntry> {
    @EntityGraph(value = "blog-entry-partial")
    Optional<BlogEntry> findBlogEntryById(@Param("id") int id);

    //todo : remove this - fetch author username separately
    @EntityGraph(value = "blog-entry-with-author", type = EntityGraph.EntityGraphType.LOAD)
    Optional<BlogEntry> findSimpleBlogEntryById(Integer id);

    @NonNull
    @Override
//    @EntityGraph(value = "blog-entry-with-partial", type =  EntityGraph.EntityGraphType.LOAD)
    Page<BlogEntry> findAll(@Nullable Specification<BlogEntry> spec, @NonNull Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM blog_entries WHERE id = :id", nativeQuery = true)
    void deleteBlogEntryById(@Param("id") Integer id);
}