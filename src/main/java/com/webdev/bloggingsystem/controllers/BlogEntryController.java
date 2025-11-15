package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.entities.BlogEntryRequestDto;
import com.webdev.bloggingsystem.entities.BlogEntryResponseDto;
import com.webdev.bloggingsystem.entities.PaginatedBlogEntriesResponseDto;
import com.webdev.bloggingsystem.services.BlogEntryService;

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
    public ResponseEntity<BlogEntryResponseDto> getBlogEntry(@PathVariable Integer id, Principal principal) {

        return ResponseEntity.ok(blogEntryService.getBlogEntryById(id, principal.getName()));
    }

    // todo: finish filtering by request params
    @GetMapping("/posts")
    public ResponseEntity<PaginatedBlogEntriesResponseDto> getAllPublicBlogEntries(
            Pageable pageable,
            @RequestParam(name = "category-name", required = false ) String categoryName,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "after-date", required = false) String afterDate,
            @RequestParam(name = "before-date", required = false) String beforeDate) {

        System.out.println("getAllPublicBlogEntries: filters: " + categoryName + " " + username + " " + afterDate + " " + beforeDate);
        return ResponseEntity.ok(blogEntryService.getAllBlogEntries(pageable, null));
    }

    @GetMapping("/posts/me")
    public ResponseEntity<PaginatedBlogEntriesResponseDto> getAllBlogEntriesForUser(
            Pageable pageable, Principal principal,
            @RequestParam(name = "category-name", required = false ) String categoryName,
            @RequestParam(name = "after-date", required = false) String afterDate,
            @RequestParam(name = "before-date", required = false) String beforeDate) {

        System.out.println("getAllBlogEntriesForUser: filters: " + categoryName + " " + afterDate + " " + beforeDate + " username: " + principal.getName());
        return ResponseEntity.ok(blogEntryService.getAllBlogEntries(pageable, principal.getName()));
    }


    // Todo: need to validate BlogEntryRequestDto fields (in service layer or in DTO?)...
    @PostMapping("/posts")
    public ResponseEntity<Void> createBlogEntry(@RequestBody BlogEntryRequestDto blogEntryRequestDto,
                                                Principal principal, UriComponentsBuilder ucb) {

        return ResponseEntity.created(blogEntryService.saveEntry(blogEntryRequestDto, principal.getName(), ucb)).build();
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Void> updateBlogEntry(@PathVariable Integer id,
                                                 @RequestBody BlogEntryRequestDto blogEntryRequestDto,
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
