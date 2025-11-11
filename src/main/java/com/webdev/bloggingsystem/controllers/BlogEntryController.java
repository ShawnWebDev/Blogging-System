package com.webdev.bloggingsystem.controllers;

import com.webdev.bloggingsystem.entities.BlogEntryRequestDto;
import com.webdev.bloggingsystem.entities.BlogEntryResponseDto;
import com.webdev.bloggingsystem.entities.CommentResponseDto;
import com.webdev.bloggingsystem.entities.PaginatedBlogEntriesResponseDto;
import com.webdev.bloggingsystem.services.BlogEntryService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BlogEntryController {
    private final BlogEntryService blogEntryService;

    public BlogEntryController(BlogEntryService blogEntryService) {
        this.blogEntryService = blogEntryService;
    }
    // todo : look out for any n+1 issues here..

    @GetMapping("/hello")
    public String hello(Principal principal) {
        return "Hello " + principal.getName();
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<BlogEntryResponseDto> getBlogEntry(@PathVariable Integer id, Principal principal) {
        return ResponseEntity.ok(blogEntryService.getBlogEntryById(id, principal.getName()));
    }

    @GetMapping("/posts")
    public ResponseEntity<PaginatedBlogEntriesResponseDto> getAllPublicBlogEntries(Pageable pageable) {
        return ResponseEntity.ok(blogEntryService.getAllPublicBlogEntries(pageable));
    }

    // ToDo: need to validate BlogEntryRequestDto fields (in service layer)...
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

    @GetMapping("/comments/{parentId}")
    public ResponseEntity<List<CommentResponseDto>> getAllCommentsByParentId(@PathVariable Integer parentId) {
        return ResponseEntity.ok(blogEntryService.getAllRepliesByParentId(parentId));
    }
}
