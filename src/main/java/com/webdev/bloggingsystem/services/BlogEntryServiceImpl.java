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
import java.time.Instant;
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
        // BlogEntry content, Author, Categories, and top-level Comments with a count of replies.
        logger.debug("getBlogEntryById: findBlogEntryById");
        BlogEntry entry = blogEntryRepo.findBlogEntryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));

        // could use repo to make this check - but, want to be able to allow author to view their own private entries
        if (principalName == null && !entry.isPublic() ||
                principalName != null && !entry.isPublic() && !entry.getAuthor().getUsername().equals(principalName)) {
            throw new ResourceNotFoundException("Entry not found with id " + id);
        }
        logger.debug("found entry: {} with author of {} and categories of {}", entry, entry.getAuthor().getUsername(), entry.getCategories());

        List<Integer> commentIds = entry.getComments().stream().map(Comment::getId).toList();

        Map<Integer, Integer> mapReplyCountToParentCommentIds = commentRepo.countRepliesByParentCommentIds(commentIds)
                        .stream().collect(Collectors.toMap(
                        row -> row.get("parentId", Integer.class),
                        row -> row.get("replyCount", Long.class).intValue()));

        List<CommentResponseDto> commentResponseDtos = entry.getComments().stream()
                        .map(comment -> new CommentResponseDto(
                                comment.getId(),
                                comment.getComment(),
                                comment.getCreatedAt(),
                                comment.getAuthor().getUsername(),
                                mapReplyCountToParentCommentIds.getOrDefault(comment.getId(), 0)
                        )).toList();

        List<Tuple> commentCountResult = commentRepo.countCommentsByBlogEntryIds(List.of(entry.getId()));
        int commentCount = commentCountResult.isEmpty() ? 0
                : commentCountResult.getFirst().get("commentCount", Long.class).intValue();

        return new BlogEntryResponseDto(entry, commentResponseDtos, commentCount);
    }

    // todo : add optional search params - Author.username Category.categoryName, DateBefore, DateAfter
    // todo : will need to create some Specification object (look up)
    //  will also need to extend the Repository to use the JpaSpecificationExecutor<BlogEntry>.
    @Override
    public PaginatedBlogEntriesResponseDto getAllBlogEntries(Pageable pageable, String principleName,
                                                             BlogEntryFilterRequest filterRequest) {
        // Gets a page of BlogEntries for viewing lists or searching, sortable by any field in BlogEntry,
        // Can be public entries or both public and private if principal is available,
        // Entry content, Author, and Categories, will not contain comments,
        // default is descending sort by updatedAt, pageSize 10, pageNumber 0
        // max pageSize is 50, batch size for category join table is set to 50
        logger.debug("getAllBlogEntries");
        Specification<BlogEntry> spec = getBlogEntrySpecification(filterRequest);

        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.DESC, "updatedAt")));

        if (principleName == null) {
            spec = spec.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.isTrue(root.get("isPublic"))));
        } else {
            spec = spec.and((root, query, criteriaBuilder) -> {
                        Join<BlogEntry, AppUser> categoryJoin = root.join("author", JoinType.INNER);
                        return criteriaBuilder.equal(categoryJoin.get("username"), principleName);
                    });
        }

        Page<BlogEntry> blogEntries = blogEntryRepo.findAll(spec, pageRequest);

        blogEntries.getContent().forEach(entry -> {
            entry.getCategories().size();
        });

        List<Integer> blogIds = blogEntries.getContent().stream().map(BlogEntry::getId).toList();

        Map<Integer, Integer> mapCommentCountToBlogIds = commentRepo.countCommentsByBlogEntryIds(blogIds)
                .stream().collect(Collectors.toMap(
                        row -> row.get("blogId", Integer.class),
                        row -> row.get("commentCount", Long.class).intValue()));

        List<BlogEntryResponseDto> responseDtos = new ArrayList<>();
        for (BlogEntry blogEntry : blogEntries.getContent()) {
            responseDtos.add(new BlogEntryResponseDto(blogEntry, List.of(),
                    mapCommentCountToBlogIds.getOrDefault(blogEntry.getId(), 0))
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

    private static Specification<BlogEntry> getBlogEntrySpecification(BlogEntryFilterRequest filterRequest) {
        Specification<BlogEntry> spec = (root, query,criteriaBuilder) -> {
            if (query != null) {
                logger.debug("getBlogEntrySpecification: {}", query);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };

        if (filterRequest.categoryName() != null) {
            logger.debug("getBlogEntrySpecification: filterRequest.categoryName is  {}", filterRequest.categoryName());
            spec = spec.and((root, query, criteriaBuilder) -> {
                Join<BlogEntry, Category> categoryJoin = root.join("categories", JoinType.INNER);
                return criteriaBuilder.equal(
                        categoryJoin.get("categoryName"), filterRequest.categoryName()
                );
            });
        }

        logger.debug("checking other filters...");
        if (filterRequest.afterDate() != null) {
            Instant afterDate = Instant.parse(filterRequest.afterDate());
            spec = spec.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt"), afterDate))
            );
        }

        if (filterRequest.beforeDate() != null) {
            Instant beforeDate = Instant.parse(filterRequest.beforeDate());
            spec = spec.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), beforeDate))
            );
        }

        return spec;
    }

    // todo: create validation logic, use before saving & updating.
    @Transactional
    @Override
    public URI saveEntry(BlogEntryRequestDto blogEntryRequestDto, String principalName, UriComponentsBuilder ucb) {
        logger.debug("saveEntry: getting author {}", principalName);
        AppUser author = appUserRepo.findByUsername(principalName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with name " + principalName));
        logger.debug("saveEntry: getting categories");
        Set<Category> categories = categoryRepo.findByCategoryNameIn(blogEntryRequestDto.categories());
        logger.debug("saveEntry: saving entry");
        BlogEntry savedEntry = blogEntryRepo.save(this.mapRequestToEntity(blogEntryRequestDto, author, categories));
        logger.debug("saveEntry: saved entry {}", savedEntry);
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
        // todo: validate input!!!
        // updates entries fields from request dto
        if (blogEntryRequestDto.title() != null) entry.setTitle(blogEntryRequestDto.title());
        if (blogEntryRequestDto.content() != null) entry.setContent(blogEntryRequestDto.content());
        if (blogEntryRequestDto.isPublic() != null) entry.setPublic(blogEntryRequestDto.isPublic());
        if (blogEntryRequestDto.categories() != null) {
            Set<Category> categories = categoryRepo.findByCategoryNameIn(blogEntryRequestDto.categories());
            Set<Category> categoriesToRemove = new HashSet<>(entry.getCategories());
            logger.debug("removing categories");
            for (Category category : categoriesToRemove) {
                if (!categories.contains(category)) {
                    entry.removeCategory(category);
                }
            }
            logger.debug("adding categories");
            for (Category category : categories) {
                if (!entry.getCategories().contains(category)) {
                    entry.addCategory(category);
                }
            }
        }
        blogEntryRepo.save(entry);
    }

    @Transactional
    @Override
    public void deleteEntryById(Integer id, String principalName) {
        logger.debug("deleteEntryById: ensuring entry by id {} is owned by author name {}", id, principalName);
        BlogEntry entryToDelete = blogEntryRepo.findSimpleBlogEntryByIdAndAuthorUsername(id, principalName)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found with id " + id));

        blogEntryRepo.deleteBlogEntryById(entryToDelete.getId());
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
