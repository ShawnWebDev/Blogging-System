package com.webdev.bloggingsystem.services;

import com.webdev.bloggingsystem.dto.*;
import com.webdev.bloggingsystem.entities.*;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public BlogEntryServiceImpl(BlogEntryRepo blogEntryRepo, AppUserRepo appUserRepo, CategoryRepo categoryRepo,
                                CommentRepo commentRepo) {
        this.blogEntryRepo = blogEntryRepo;
        this.appUserRepo = appUserRepo;
        this.categoryRepo = categoryRepo;
        this.commentRepo = commentRepo;
    }

    @Override
    public BlogEntryResponseDto getBlogEntryById(Integer id, String principalName) {
        // gets single BlogEntry with full entity graph for viewing it in entirety
        // BlogEntry content, Author, Categories, top-level Comments with Comment Author, and a count of replies for each comment,
        // and a total count of all comments.
        logger.debug("getBlogEntryById: findBlogEntryById");
        BlogEntry entry = blogEntryRepo.findBlogEntryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));

        // could use repo to make this check - but, want to be able to allow author to view their own private entries
        if (principalName == null && !entry.isPublic() ||
                principalName != null && !entry.isPublic() && !entry.getAuthor().getUsername().equals(principalName)) {
            throw new ResourceNotFoundException("Entry not found with id " + id);
        }
        logger.debug("found entry: {} with author of {} and categories of {}", entry, entry.getAuthor().getUsername(), entry.getCategories());

        // all top-level comment ids - joined where parent_comment_id IS NULL
        List<Integer> commentIds = entry.getComments().stream().map(Comment::getId).toList();
        // maps reply count to the top-level comment ids
        Map<Integer, Integer> mapReplyCountToParentCommentIds = commentRepo.countRepliesByParentCommentIds(commentIds)
                        .stream().collect(Collectors.toMap(
                        row -> row.get("parentId", Integer.class),
                        row -> row.get("replyCount", Long.class).intValue()));
        // creates list of DTOs of comments with reply count
        List<CommentResponseDto> commentResponseDtos = entry.getComments().stream()
                        .map(comment -> new CommentResponseDto(
                                comment.getId(),
                                comment.getComment(),
                                comment.getCreatedAt(),
                                comment.getAuthor().getUsername(),
                                mapReplyCountToParentCommentIds.getOrDefault(comment.getId(), 0)
                        )).toList();
        // returns a list of key value pairs of [blogId : total comment count] (only one since this is getting one entry)
        List<Tuple> commentCountResult = commentRepo.countCommentsByBlogEntryIds(List.of(entry.getId()));
        // if list is emtpy total comment count is 0
        int commentCount = commentCountResult.isEmpty() ? 0
                : commentCountResult.getFirst().get("commentCount", Long.class).intValue();

        return new BlogEntryResponseDto(entry, commentResponseDtos, commentCount);
    }

    @Override
    public PaginatedBlogEntriesResponseDto getAllBlogEntries(Pageable pageable, String principleName,
                                                             BlogEntryFilterRequest filterRequest) {
        // Gets a page of BlogEntries for viewing lists or searching, sortable by any field in BlogEntry,
        // Can be public or users public and private entries if principalName is available,
        // DTOs will include Entry content, Author, and Categories, will not contain comments - just total count of them,
        // default page is descending sort by updatedAt, pageSize 10, pageNumber 0
        // max pageSize is set to 50 so batch size for category join table is set to 50
        // called by endpoint methods - getAllPublicBlogEntries() and getAllBlogEntriesForUser()
        logger.debug("getAllBlogEntries");
        // defines a Specification object for criteria builder to build a filtering query
        Specification<BlogEntry> spec = getBlogEntrySpecification(filterRequest, principleName);

        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.DESC, "updatedAt")));

        // repo sends query with built filter and defined page, returns page
        Page<BlogEntry> blogEntries = blogEntryRepo.findAll(spec, pageRequest);
        // signal to Hibernate to fetch categories separately in a batch to avoid fetching duplicate BlogEntries for each category (Cartesian Product).
        logger.debug("batch a categories query..");
        blogEntries.getContent().forEach(BlogEntry::getCategories);
        // extract BlogEntry ids to use in comment counts
        List<Integer> blogIds = blogEntries.getContent().stream().map(BlogEntry::getId).toList();
        // map BlogEntry ids to total comment counts
        Map<Integer, Integer> mapCommentCountToBlogIds = commentRepo.countCommentsByBlogEntryIds(blogIds)
                .stream().collect(Collectors.toMap(
                        row -> row.get("blogId", Integer.class),
                        row -> row.get("commentCount", Long.class).intValue()));
        // build BlogEntryResponseDtos setting comment counts with default value of 0 if not in map
        List<BlogEntryResponseDto> responseDtos = new ArrayList<>();
        for (BlogEntry blogEntry : blogEntries.getContent()) {
            responseDtos.add(new BlogEntryResponseDto(blogEntry, List.of(),
                    mapCommentCountToBlogIds.getOrDefault(blogEntry.getId(), 0))
            );
        }
        // build page dto with BlogEntryResponseDtos and page info
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
    public URI saveEntry(BlogEntryRequestDto blogEntryRequestDto, String principalName, UriComponentsBuilder ucb) {
        // has to fetch User and Categories to verify they exist before defining relation to the BlogEntry to be created,
        // input is validated in DTO/Controller with jakarta.validation
        logger.debug("saveEntry: getting author {}", principalName);
        AppUser author = appUserRepo.findByUsername(principalName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with name " + principalName));

        logger.debug("saveEntry: getting categories");
        Set<Category> categories = categoryRepo.findByCategoryNameIn(blogEntryRequestDto.categories());
        validateCategories(blogEntryRequestDto, categories);

        logger.debug("saveEntry: saving entry");
        // fields other than categories and author are validated with jakarta.validation in DTO
        BlogEntry savedEntry = blogEntryRepo.save(new BlogEntry(
                author,
                blogEntryRequestDto.title(),
                blogEntryRequestDto.content(),
                blogEntryRequestDto.isPublic(),
                categories
        ));
        logger.debug("saveEntry: saved entry {}", savedEntry);
        // returns endpoint which the saved entry can be found (in response header)
        return ucb.path("/api/posts/{id}").buildAndExpand(savedEntry.getId()).toUri();
    }

    @Transactional
    @Override
    public void updateEntryById(Integer id, BlogEntryRequestDto blogEntryRequestDto, String principalName) {
        logger.debug("updateEntryById: getting entry by id {} and author {}", id, principalName);
        BlogEntry entry = blogEntryRepo.findBlogEntryByIdAndAuthorUsername(id, principalName)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));
        logger.debug("found entry: {} with author of {}", entry.toString(), entry.getAuthor().getUsername());

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
    }

    @Transactional
    @Override
    public void deleteEntryById(Integer id, String principalName) {
        logger.debug("deleteEntryById: ensuring entry by id {} is owned by author name {}", id, principalName);
        // ensures authorized user is Author of BlogEntry to be deleted
        BlogEntry entryToDelete = blogEntryRepo.findSimpleBlogEntryByIdAndAuthorUsername(id, principalName)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));

        // uses fetched BlogEntry's id to delete after verification
        blogEntryRepo.deleteBlogEntryById(entryToDelete.getId());
    }

    private static void validateCategories(BlogEntryRequestDto blogEntryRequestDto, Set<Category> categories) {
        Set<String> dtoCategories = new HashSet<>(blogEntryRequestDto.categories());
        if (categories.size() != dtoCategories.size()) {
            logger.debug("categories are not equal");
            dtoCategories.removeAll(categories.stream().map(Category::getCategoryName).collect(Collectors.toSet()));
            throw new ResourceNotFoundException("Categories not found: "  + dtoCategories);
        }
    }

    private static Specification<BlogEntry> getBlogEntrySpecification(BlogEntryFilterRequest filterRequest, String principleName) {
        // set base Specification object to use DISTINCT select
        Specification<BlogEntry> spec = (root, query,criteriaBuilder) -> {
            if (query != null) {
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };

        // "posts" endpoint will pass null username to get public
        // "me" endpoint will pass username as String to get all for authenticated user
        if (principleName == null) {
            spec = spec.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.isTrue(root.get("isPublic"))));
        } else {
            spec = spec.and((root, query, criteriaBuilder) -> {
                Join<BlogEntry, AppUser> authorJoin = root.join("author", JoinType.INNER);
                return criteriaBuilder.equal(authorJoin.get("username"), principleName);
            });
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
}
