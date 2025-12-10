package com.webdev.bloggingsystem.services;


import com.webdev.bloggingsystem.dto.BlogEntryFilterRequest;
import com.webdev.bloggingsystem.dto.BlogEntryResponseDto;
import com.webdev.bloggingsystem.dto.PaginatedBlogEntriesResponseDto;
import com.webdev.bloggingsystem.dto.UserProfile;
import com.webdev.bloggingsystem.dto.BlogEntryRequestDto;
import com.webdev.bloggingsystem.entities.BlogEntry;
import com.webdev.bloggingsystem.entities.Category;
import com.webdev.bloggingsystem.entities.AppUser;
import com.webdev.bloggingsystem.exceptions.ResourceNotFoundException;
import com.webdev.bloggingsystem.repositories.AppUserRepo;
import com.webdev.bloggingsystem.repositories.BlogEntryRepo;
import com.webdev.bloggingsystem.repositories.CategoryRepo;
import com.webdev.bloggingsystem.repositories.CommentRepo;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BlogEntryServiceImpl implements BlogEntryService {
    private final static Logger logger  = LoggerFactory.getLogger(BlogEntryServiceImpl.class);

    private final BlogEntryRepo blogEntryRepo;
    private final AppUserRepo appUserRepo;
    private final CategoryRepo categoryRepo;
    private final CommentRepo commentRepo;
    private final AuthService authService;

    public BlogEntryServiceImpl(
            BlogEntryRepo blogEntryRepo, AppUserRepo appUserRepo, CategoryRepo categoryRepo, CommentRepo commentRepo,
            AuthService authService)
    {
        this.blogEntryRepo = blogEntryRepo;
        this.appUserRepo = appUserRepo;
        this.categoryRepo = categoryRepo;
        this.commentRepo = commentRepo;
        this.authService = authService;
    }

    @Override
    public BlogEntryResponseDto getBlogEntryById(Integer id)
    {
        logger.debug("getBlogEntryById: findBlogEntryById");
        BlogEntry entry = blogEntryRepo.findBlogEntryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));

        String authorUsername = appUserRepo.findUsernameById(entry.getAuthorId());

        UserProfile userProfile = authService.getUserProfile();
        // allow author to view their own private entries if authenticated
        if (userProfile == null && !entry.isPublic() ||
                userProfile != null && !entry.isPublic() && !authorUsername.equals(userProfile.username())) {
            throw new ResourceNotFoundException("Entry not found with id " + id);
        }

        Tuple commentCountResult = commentRepo.countCommentsByBlogEntryId(entry.getId());

        int commentCount = commentCountResult != null ?
                commentCountResult.get("commentCount" , Long.class).intValue() : 0;

        logger.debug("commentCount: {} ", commentCount);
        logger.debug("entry: {}", entry);

        return new BlogEntryResponseDto(entry, authorUsername, commentCount);
    }

    @Override
    public PaginatedBlogEntriesResponseDto getAllBlogEntries(
            Pageable pageable, BlogEntryFilterRequest filterRequest)
    {
        // Gets a page of BlogEntries for viewing lists or searching, sortable by any field in BlogEntry,
        // Can be public or users public and private entries if principalName is available,
        // DTOs will include Entry content, Author, and Categories, will not contain comments - just total count of them,
        // comments can only be accessed and created by authorized users
        // default page is descending sort by updatedAt, pageSize 10, pageNumber 0
        // max pageSize is set to 50 so batch size for category join table is set to 50
        // called by endpoint methods - getAllPublicBlogEntries()"/posts" and getAllBlogEntriesForUser()"/posts/me"
        logger.debug("getAllBlogEntries");
        UserProfile userProfile = authService.getUserProfile();
        Integer authorId = null;
        if (userProfile != null) {
            authorId = appUserRepo.findIdByUsername(userProfile.username());
        }
        // defines a Specification object for criteria builder to build the filtering query
        Specification<BlogEntry> spec = getBlogEntrySpecification(filterRequest, userProfile, authorId);
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.DESC, "updatedAt")
                )
        );
        Page<BlogEntry> blogEntries = blogEntryRepo.findAll(spec, pageRequest);
        // signal to Hibernate to fetch categories separately in a batch to avoid fetching duplicate BlogEntries for each category
        blogEntries.getContent().forEach(BlogEntry::getCategories);

        // extract BlogEntry ids to use in comment counts and username fetching
        List<Integer> blogIds = blogEntries.getContent().stream().map(BlogEntry::getId).toList();
        // map BlogEntry ids to total comment counts
        Map<Integer, Integer> mapCommentCountToBlogIds = commentRepo.countCommentsInBlogEntryIds(blogIds)
                .stream().collect(Collectors.toMap(
                                row -> row.get("blogId", Integer.class),
                                row -> row.get("commentCount", Long.class).intValue()
                        )
                );

        Set<Integer> authorIds = blogEntries.getContent().stream().map(BlogEntry::getAuthorId).collect(Collectors.toSet());
        // map author ids to username
        Map<Integer, String> mapAuthorIdToUsername = appUserRepo.findUsernamesById(authorIds)
                .stream().collect(Collectors.toMap(
                                row -> row.get("userId", Integer.class),
                                row -> row.get("username", String.class)
                        )
                );

        List<BlogEntryResponseDto> responseDtos = new ArrayList<>();
        for (BlogEntry blogEntry : blogEntries.getContent()) {
            responseDtos.add(new BlogEntryResponseDto(
                    blogEntry,
                    mapAuthorIdToUsername.get(blogEntry.getAuthorId()),
                    mapCommentCountToBlogIds.getOrDefault(blogEntry.getId(), 0)
                    )
            );
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

    @Transactional
    @Override
    public URI saveEntry(BlogEntryRequestDto blogEntryRequestDto, UriComponentsBuilder ucb)
    {
        // has to fetch User and Categories to verify they exist before defining relation to the BlogEntry to be created,
        // input is validated in DTO/Controller with jakarta.validation
        UserProfile userProfile = authService.getUserProfile();
        Integer authorId = appUserRepo.findIdByUsername(userProfile.username());

        logger.debug("saveEntry: getting author {}", userProfile.username());
        AppUser authorRef = appUserRepo.getReferenceById(authorId);

        logger.debug("saveEntry: getting categories");
        Set<Category> categories = categoryRepo.findByCategoryNameIn(blogEntryRequestDto.categories());
        validateCategories(blogEntryRequestDto, categories);

        logger.debug("saveEntry: saving entry");
        // fields other than categories and author are validated with jakarta.validation in DTO
        BlogEntry savedEntry = blogEntryRepo.save(
                new BlogEntry(
                        authorRef,
                        blogEntryRequestDto.title(),
                        blogEntryRequestDto.content(),
                        blogEntryRequestDto.isPublic(),
                        categories
                )
        );

        logger.debug("saveEntry: saved entry {}", savedEntry);
        // returns endpoint which the saved entry can be found (in response header)
        return ucb.path("/api/posts/{id}").buildAndExpand(savedEntry.getId()).toUri();
    }

    @Transactional
    @Override
    public void updateEntryById(Integer id, BlogEntryRequestDto blogEntryRequestDto)
    {
        logger.debug("updateEntryById: getting entry by id {}", id);
        BlogEntry entry = blogEntryRepo.findBlogEntryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));
        String authorName = appUserRepo.findUsernameById(entry.getAuthorId());
        logger.debug("found entry: {} with author of {}", entry, authorName);

        logger.debug("getting user profile");
        UserProfile userProfile = authService.getUserProfile();
        if (userProfile.username().equals(authorName) || userProfile.isAdmin()) {
            logger.debug("updating entry by id {}", id);
            // updates BlogEntry fields from request dto
            if (blogEntryRequestDto.title() != null) entry.setTitle(blogEntryRequestDto.title());
            if (blogEntryRequestDto.content() != null) entry.setContent(blogEntryRequestDto.content());
            if (blogEntryRequestDto.isPublic() != null) entry.setPublic(blogEntryRequestDto.isPublic());
            if (blogEntryRequestDto.categories() != null) {
                Set<Category> categories = categoryRepo.findByCategoryNameIn(blogEntryRequestDto.categories());
                validateCategories(blogEntryRequestDto, categories);

                Set<Category> categoriesToRemove = new HashSet<>(entry.getCategories());
                // loops through categories set in current BlogEntry - removing the ones not found in update request
                logger.debug("removing categories");
                for (Category category : categoriesToRemove) {
                    if (!categories.contains(category)) {
                        entry.removeCategory(category);
                    }
                }
                // loops through categories from update request - adding ones not in current BlogEntry
                logger.debug("adding categories");
                for (Category category : categories) {
                    if (!entry.getCategories().contains(category)) {
                        entry.addCategory(category);
                    }
                }
            }
            // updates changed BlogEntry fields only - Categories in category join table are Cascaded in database
            blogEntryRepo.save(entry);
        } else {
            throw new ResourceNotFoundException("Entry not found with id " + id);
        }
    }

    @Transactional
    @Override
    public void deleteEntryById(Integer id)
    {
        BlogEntry entryToDelete = blogEntryRepo.findBlogEntryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));

        String authorUsername = appUserRepo.findUsernameById(entryToDelete.getAuthorId());
        logger.debug("deleteEntryById: ensuring entry by id {} is owned by author name {}", id, authorUsername);

        // ensures authorized user is Author of BlogEntry to be deleted
        UserProfile userProfile = authService.getUserProfile();
        if (userProfile.username().equals(authorUsername) || userProfile.isAdmin()) {
            // uses fetched BlogEntry's id to delete after verification
            blogEntryRepo.deleteBlogEntryById(entryToDelete.getId());
        } else {
            throw new ResourceNotFoundException("Entry not found with id " + id);
        }
    }

    private static Specification<BlogEntry> getBlogEntrySpecification(
            BlogEntryFilterRequest filterRequest, UserProfile userProfile, Integer authorId)
    {
        // set base Specification object to use DISTINCT select
        Specification<BlogEntry> spec = (root, query,criteriaBuilder) -> {
            if (query != null) {
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };

        // "posts" endpoint will pass null username to get all public
        // "me" endpoint will pass username as String to get all for authenticated user
        if (userProfile == null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.isTrue(root.get("isPublic")));
        } else {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("authorId"), authorId));
        }
        // adds category filter
        if (filterRequest.categoryName() != null) {
            logger.debug("getBlogEntrySpecification: filterRequest.categoryName is  {}", filterRequest.categoryName());
            spec = spec.and((root, query, criteriaBuilder) -> {
                // needs to utilize join table
                Join<BlogEntry, Category> categoryJoin = root.join("categories", JoinType.INNER);
                return criteriaBuilder.equal(
                        categoryJoin.get("categoryName"), filterRequest.categoryName()
                );
            });
        }
        // adds specified after date filter
        if (filterRequest.afterDate() != null) {
            // parse String date to Instant, DateTimeParseException intercepted in global handler
            LocalDate afterDate = LocalDate.parse(filterRequest.afterDate());
            spec = spec.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt"), afterDate))
            );
        }
        // adds specified before date filter
        if (filterRequest.beforeDate() != null) {
            // parse String date to Instant, DateTimeParseException intercepted in global handler
            LocalDate beforeDate = LocalDate.parse(filterRequest.beforeDate());
            spec = spec.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), beforeDate))
            );
        }

        return spec;
    }

    private static void validateCategories(
            BlogEntryRequestDto blogEntryRequestDto, Set<Category> categories)
    {
        // gets category names as Set from dto List
        Set<String> dtoCategories = new HashSet<>(blogEntryRequestDto.categories());
        if (categories.size() != dtoCategories.size()) {
            logger.debug("categories are not equal");
            // removes found categories by name and adds unfound names to response body
            dtoCategories.removeAll(categories.stream().map(Category::getCategoryName).collect(Collectors.toSet()));
            throw new ResourceNotFoundException("Categories not found: "  + dtoCategories);
        }
    }
}