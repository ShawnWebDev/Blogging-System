package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.BlogEntryFilterRequest;
import com.webdev.bloggingsystem.dto.BlogEntryRequestDto;
import com.webdev.bloggingsystem.dto.BlogEntryResponseDto;
import com.webdev.bloggingsystem.dto.PaginatedBlogEntriesResponseDto;
import com.webdev.bloggingsystem.services.BlogEntryService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class BlogEntryController {
    private final BlogEntryService blogEntryService;

    public BlogEntryController(BlogEntryService blogEntryService) {
        this.blogEntryService = blogEntryService;
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<BlogEntryResponseDto> getBlogEntry(
            @PathVariable Integer id)
    {
        // returns 200 OK with BlogEntry if found or 404 NOT_FOUND if not
        return ResponseEntity.ok(blogEntryService.getBlogEntryById(id));
    }

    @GetMapping("/posts")
    public ResponseEntity<PaginatedBlogEntriesResponseDto> getAllPublicBlogEntries(
            Pageable pageable, @ModelAttribute BlogEntryFilterRequest blogEntryFilter)
    {
        // returns 200 OK with a page of public only BlogEntries - optionally filtered by categories, after date, and/or before date
        // default page is sorted descending by updatedAt, pageSize=10, pageNumber=0
        System.out.println("getAllBlogEntriesForUser: filters: " + blogEntryFilter.categoryName() + " " + blogEntryFilter.afterDate() + " " + blogEntryFilter.beforeDate());
        return ResponseEntity.ok(blogEntryService.getAllBlogEntries(pageable, blogEntryFilter));
    }

    @GetMapping("/posts/me")
    public ResponseEntity<PaginatedBlogEntriesResponseDto> getAllBlogEntriesForUser(
            Pageable pageable, @ModelAttribute BlogEntryFilterRequest blogEntryFilter)
    {
        // returns 200 OK with a page of the authenticated users BlogEntries where they are the Author -
        // optionally filtered by categories, after date, and/or before date
        // default page is sorted descending by updatedAt, pageSize=10, pageNumber=0
        System.out.println("getAllBlogEntriesForUser: filters: " + blogEntryFilter.categoryName() + " " + blogEntryFilter.afterDate() + " " + blogEntryFilter.beforeDate());
        return ResponseEntity.ok(blogEntryService.getAllBlogEntries(pageable, blogEntryFilter));
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createBlogEntry(
            @RequestBody @Valid BlogEntryRequestDto blogEntryRequestDto, UriComponentsBuilder ucb)
    {
        // validates and creates BlogEntry from input DTO,
        // returns 400 BAD_REQUEST on validation errors - includes errors in body
        // or 201 CREATED + URI in header and DTO in body if successful
        Map.Entry<URI, BlogEntryResponseDto> blogEntryResponseDtoEntry =
                blogEntryService.saveEntry(blogEntryRequestDto, ucb);
        return ResponseEntity.status(201)
                .location(blogEntryResponseDtoEntry.getKey()).body(blogEntryResponseDtoEntry.getValue());
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<?> updateBlogEntry(
            @PathVariable Integer id, @RequestBody @Valid BlogEntryRequestDto blogEntryRequestDto)
    {
        // validates and updates BlogEntry from input DTO,
        // returns 400 BAD_REQUEST on validation errors - includes errors in body
        // 404 NOT_FOUND if authenticated user is not Author,
        // or 200 OK and DTO in body if successful
        return ResponseEntity.ok(blogEntryService.updateEntryById(id, blogEntryRequestDto));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deleteBlogEntry(
            @PathVariable Integer id)
    {
        // checks if user is Author of BlogEntry to be deleted,
        // return 404 NOT_FOUND if authenticated user is not Author
        // or 204 NO_CONTENT if successful
        blogEntryService.deleteEntryById(id);
        return ResponseEntity.noContent().build();
    }
}