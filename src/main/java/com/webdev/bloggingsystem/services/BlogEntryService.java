package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.entities.BlogEntryRequestDto;
import com.webdev.bloggingsystem.entities.BlogEntryResponseDto;
import com.webdev.bloggingsystem.entities.CommentResponseDto;
import com.webdev.bloggingsystem.entities.PaginatedBlogEntriesResponseDto;

import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

public interface BlogEntryService {
    BlogEntryResponseDto getBlogEntryById(Integer id, String principalName);
    PaginatedBlogEntriesResponseDto getAllPublicBlogEntries(Pageable pageable);
    URI saveEntry(BlogEntryRequestDto blogEntryRequestDto, String principalName, UriComponentsBuilder ucb);
    void updateEntryById(Integer id, BlogEntryRequestDto blogEntryRequestDto, String principalName);
    void deleteEntryById(Integer id, String principalName);
}
