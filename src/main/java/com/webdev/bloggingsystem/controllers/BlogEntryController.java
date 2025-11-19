package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.dto.BlogEntryFilterRequest;
import com.webdev.bloggingsystem.dto.BlogEntryRequestDto;
import com.webdev.bloggingsystem.dto.BlogEntryResponseDto;
import com.webdev.bloggingsystem.dto.PaginatedBlogEntriesResponseDto;
import com.webdev.bloggingsystem.services.BlogEntryService;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Optional;

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
                                                             @AuthenticationPrincipal Optional<Principal> principal) {
        String username = null;
        if (principal != null && principal.isPresent()) {
            username = principal.get().getName();
        }
        return ResponseEntity.ok(blogEntryService.getBlogEntryById(id, username));
    }

    // todo: finish filtering by request params
    @GetMapping("/posts")
    public ResponseEntity<PaginatedBlogEntriesResponseDto> getAllPublicBlogEntries(
            Pageable pageable,
            @ModelAttribute BlogEntryFilterRequest blogEntryFilter) {

        System.out.println("getAllBlogEntriesForUser: filters: " + blogEntryFilter.categoryName() + " " + blogEntryFilter.afterDate() + " " + blogEntryFilter.beforeDate());
        return ResponseEntity.ok(blogEntryService.getAllBlogEntries(pageable, null, blogEntryFilter));
    }

    @GetMapping("/posts/me")
    public ResponseEntity<PaginatedBlogEntriesResponseDto> getAllBlogEntriesForUser(
            Pageable pageable, Principal principal,
            @ModelAttribute BlogEntryFilterRequest blogEntryFilter) {

        System.out.println("getAllBlogEntriesForUser: filters: " + blogEntryFilter.categoryName() + " " + blogEntryFilter.afterDate() + " " + blogEntryFilter.beforeDate() + " username: " + principal.getName());
        return ResponseEntity.ok(blogEntryService.getAllBlogEntries(pageable, principal.getName(), blogEntryFilter));
    }


    // Todo: need to validate BlogEntryRequestDto fields (in service layer or in DTO?)...
    @PostMapping("/posts")
    public ResponseEntity<?> createBlogEntry(@RequestBody @Valid BlogEntryRequestDto blogEntryRequestDto,
                                                Principal principal, UriComponentsBuilder ucb) {

        return ResponseEntity.created(blogEntryService.saveEntry(blogEntryRequestDto, principal.getName(), ucb)).build();
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Void> updateBlogEntry(@PathVariable Integer id,
                                                 @Valid @RequestBody BlogEntryRequestDto blogEntryRequestDto,
                                                 Principal principal) {

        blogEntryService.updateEntryById(id, blogEntryRequestDto, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deleteBlogEntry(@PathVariable Integer id, Principal principal) {

        blogEntryService.deleteEntryById(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}