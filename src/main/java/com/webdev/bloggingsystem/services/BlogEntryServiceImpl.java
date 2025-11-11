package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.entities.*;
import com.webdev.bloggingsystem.exceptions.ResourceNotFoundException;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import com.webdev.bloggingsystem.repositories.BlogEntryRepo;
import com.webdev.bloggingsystem.repositories.CategoryRepo;

import com.webdev.bloggingsystem.repositories.CommentRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class BlogEntryServiceImpl implements BlogEntryService {
    private final static Logger logger  = LoggerFactory.getLogger(BlogEntryServiceImpl.class);
    private final BlogEntryRepo blogEntryRepo;
    private final AppUserRepo appUserRepo;
    private final CategoryRepo categoryRepo;
    private final CommentRepo commentRepo;

    public BlogEntryServiceImpl(BlogEntryRepo blogEntryRepo, AppUserRepo appUserRepo, CategoryRepo categoryRepo,
                                CommentRepo commentRepo) {
        this.blogEntryRepo = blogEntryRepo;
        this.appUserRepo = appUserRepo;
        this.categoryRepo = categoryRepo;
        this.commentRepo = commentRepo;
    }

    @Override
    public BlogEntryResponseDto getBlogEntryById(Integer id, String principalName) {
        logger.debug("getBlogEntryById: findBlogEntryById");
        BlogEntry entry = blogEntryRepo.findBlogEntryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));
        // could use repo to make this check - but, want to be able to allow author to view their own private entries,
        logger.debug("getBlogEntryById: checking author against principal");
        if (!entry.isPublic() && !entry.getAuthor().getUsername().equals(principalName)) {
            throw new ResourceNotFoundException("Entry not found with id " + id);
        }
        logger.debug("getBlogEntryById: calling/building response dto");
        return new BlogEntryResponseDto(entry, true);
    }

    @Override
    public PaginatedBlogEntriesResponseDto getAllPublicBlogEntries(Pageable pageable) {
        // default is descending sort by updatedAt, pageSize 20, pageNumber 0
        Page<BlogEntry> blogEntries = blogEntryRepo.findAllByIsPublicTrue(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "updatedAt"))
                )
        );

        List<BlogEntryResponseDto> responseDtos = new ArrayList<>();
        for (BlogEntry blogEntry : blogEntries.getContent()) {
            responseDtos.add(new BlogEntryResponseDto(blogEntry, false));
        }

        return new PaginatedBlogEntriesResponseDto(
                responseDtos,
                blogEntries.getNumber(),
                blogEntries.getSize(),
                blogEntries.getTotalPages(),
                blogEntries.getTotalElements(),
                blogEntries.isLast(),
                blogEntries.isFirst()
        );
    }

    // todo: create validation logic, use before saving & updating.

    @Override
    public URI saveEntry(BlogEntryRequestDto blogEntryRequestDto, String principalName, UriComponentsBuilder ucb) {
        logger.debug("saveEntry: getting author");
        AppUser author = appUserRepo.findByUsername(principalName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with name " + principalName));
        logger.debug("saveEntry: getting categories");
        Set<Category> categories = categoryRepo.findByCategoryNameIn(blogEntryRequestDto.categories());
        logger.debug("saveEntry: saving entry");
        BlogEntry savedEntry = blogEntryRepo.save(this.mapRequestToEntity(blogEntryRequestDto, author, categories));

        return ucb.path("/api/posts/{id}").buildAndExpand(savedEntry.getId()).toUri();
    }

    @Override
    public void updateEntryById(Integer id, BlogEntryRequestDto blogEntryRequestDto, String principalName) {
        logger.debug("updateEntryById: getting entry by id {}", id);
        BlogEntry entry = blogEntryRepo.findBlogEntryByIdAndAuthorUsername(id, principalName)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));

        logger.debug("updating entry by id {}", id);
        // todo: validate input!!!
        // manually update entry fields from dto
        if (blogEntryRequestDto.title() != null) entry.setTitle(blogEntryRequestDto.title());
        if (blogEntryRequestDto.content() != null) entry.setContent(blogEntryRequestDto.content());
        if (blogEntryRequestDto.isPublic() != null) entry.setPublic(blogEntryRequestDto.isPublic());
        if (blogEntryRequestDto.categories() != null) {
            Set<Category> categories = categoryRepo.findByCategoryNameIn(blogEntryRequestDto.categories());
            entry.setCategories(categories);
        }
        blogEntryRepo.save(entry);
    }

    @Transactional
    @Override
    public void deleteEntryById(Integer id, String principalName) {
        logger.debug("deleteEntryById: getting entry by id {} and author name {}", id, principalName);
        BlogEntry entryToDelete = blogEntryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));

        if (!entryToDelete.getAuthor().getUsername().equals(principalName)) {
            throw new ResourceNotFoundException("Entry not found with id " + id);
        }

        blogEntryRepo.betterDeleteById(entryToDelete.getId());
    }

    @Override
    public List<CommentResponseDto> getAllRepliesByParentId(Integer parentId) {
        List<Comment> comments = commentRepo.findAllByParentCommentId(parentId);
        List<CommentResponseDto> responseDtos;
        if (!comments.isEmpty()) {
            responseDtos = new ArrayList<>();
            for (Comment comment : comments) {
                responseDtos.add(
                        new CommentResponseDto(
                                comment.getId(), comment.getComment(), comment.getCreatedAt(), comment.getAuthor().getUsername()
                        )
                );
            }
            logger.debug("getAllRepliesByParentId: responseDtos: {}", responseDtos);
            return responseDtos;
        }
        return List.of();
    }


    private BlogEntry mapRequestToEntity(BlogEntryRequestDto blogEntryRequestDto, AppUser author,
                                         Set<Category> categories) {
        return new BlogEntry(
                author,
                blogEntryRequestDto.title(),
                blogEntryRequestDto.content(),
                blogEntryRequestDto.isPublic(),
                categories
        );
    }
}
