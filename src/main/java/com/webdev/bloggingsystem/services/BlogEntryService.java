package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.BlogEntryFilterRequest;
import com.webdev.bloggingsystem.dto.BlogEntryRequestDto;
import com.webdev.bloggingsystem.dto.BlogEntryResponseDto;
import com.webdev.bloggingsystem.dto.PaginatedBlogEntriesResponseDto;

import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public interface BlogEntryService {
    BlogEntryResponseDto getBlogEntryById(int id);
    PaginatedBlogEntriesResponseDto getAllBlogEntries(Pageable pageable, BlogEntryFilterRequest filterRequest);
    URI saveEntry(BlogEntryRequestDto blogEntryRequestDto, UriComponentsBuilder ucb);
    void updateEntryById(int id, BlogEntryRequestDto blogEntryRequestDto);
    void deleteEntryById(int id);
}
