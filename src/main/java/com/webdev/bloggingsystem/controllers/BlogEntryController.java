package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.BlogEntryFilterRequest;
import com.webdev.bloggingsystem.dto.BlogEntryRequestDto;
import com.webdev.bloggingsystem.dto.BlogEntryResponseDto;
import com.webdev.bloggingsystem.dto.PaginatedBlogEntriesResponseDto;
import com.webdev.bloggingsystem.exceptions.ResourceNotFoundException;
import com.webdev.bloggingsystem.services.BlogEntryService;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class BlogEntryController {
    private final BlogEntryService blogEntryService;

    public BlogEntryController(BlogEntryService blogEntryService) {
        this.blogEntryService = blogEntryService;
    }

    @GetMapping("/hello")
    public String hello(Principal principal) {
        return "Hello " + principal.getName();
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<BlogEntryResponseDto> getBlogEntry(@PathVariable Integer id,
                                                             Principal principal) {
        // quick check that verifies a user is authenticated before fetching private entries in service (needed here for null handling)
        // returns 200 OK with BlogEntry if found or 404 NOT_FOUND if not
        String username = null;
        if (principal != null) {
            username = principal.getName();
        }
        return ResponseEntity.ok(blogEntryService.getBlogEntryById(id, username));
    }

    // todo: test filtering by dates
    @GetMapping("/posts")
    public ResponseEntity<PaginatedBlogEntriesResponseDto> getAllPublicBlogEntries(
            Pageable pageable,
            @ModelAttribute BlogEntryFilterRequest blogEntryFilter) {
        // returns 200 OK with a page of public only BlogEntries - optionally filtered by categories, after date, and/or before date
        // default page is sorted descending by updatedAt, pageSize=10, pageNumber=0
        System.out.println("getAllBlogEntriesForUser: filters: " + blogEntryFilter.categoryName() + " " + blogEntryFilter.afterDate() + " " + blogEntryFilter.beforeDate());
        return ResponseEntity.ok(blogEntryService.getAllBlogEntries(pageable, null, blogEntryFilter));
    }

    @GetMapping("/posts/me")
    public ResponseEntity<PaginatedBlogEntriesResponseDto> getAllBlogEntriesForUser(
            Pageable pageable, Principal principal,
            @ModelAttribute BlogEntryFilterRequest blogEntryFilter) {
        // returns 200 OK with a page of the authenticated users BlogEntries where they are the Author -
        // optionally filtered by categories, after date, and/or before date
        // default page is sorted descending by updatedAt, pageSize=10, pageNumber=0
        String username;
        if (principal != null) {
            username = principal.getName();
        } else {
            throw new ResourceNotFoundException("User not found, must be logged in!");
        }
        System.out.println("getAllBlogEntriesForUser: filters: " + blogEntryFilter.categoryName() + " " + blogEntryFilter.afterDate() + " " + blogEntryFilter.beforeDate() + " username: " + principal.getName());
        return ResponseEntity.ok(blogEntryService.getAllBlogEntries(pageable, username, blogEntryFilter));
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createBlogEntry(@RequestBody @Valid BlogEntryRequestDto blogEntryRequestDto,
                                                Principal principal, UriComponentsBuilder ucb) {
        // validates and creates BlogEntry from input DTO,
        // returns 400 BAD_REQUEST on validation errors - includes errors in body
        // or 201 CREATED + URI in header if successful
        return ResponseEntity.created(blogEntryService.saveEntry(blogEntryRequestDto, principal.getName(), ucb)).build();
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<?> updateBlogEntry(@PathVariable Integer id,
                                                 @RequestBody @Valid BlogEntryRequestDto blogEntryRequestDto,
                                                 Principal principal) {
        // validates and updates BlogEntry from input DTO,
        // returns 400 BAD_REQUEST on validation errors - includes errors in body
        // 404 NOT_FOUND if authenticated user is not Author,
        // or 204 NO_CONTENT if successful
        blogEntryService.updateEntryById(id, blogEntryRequestDto, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deleteBlogEntry(@PathVariable Integer id, Principal principal) {
        // checks if user is Author of BlogEntry to be deleted,
        // return 404 NOT_FOUND if authenticated user is not Author
        // or 204 NO_CONTENT if successful
        blogEntryService.deleteEntryById(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}