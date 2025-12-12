package com.webdev.bloggingsystem.repositories;

import com.webdev.bloggingsystem.entities.Category;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CategoryRepo extends CrudRepository<Category, Integer> {
    Set<Category> findByCategoryNameIn(List<String> categories);

    @Query(value = "SELECT COUNT(*) FROM posts_categories WHERE category_id = ?", nativeQuery = true)
    Integer countPostsWithCategoryId(Integer category_id);

    @Query(value = "SELECT category_id AS categoryId, count(post_id) as postCount " +
            "FROM posts_categories WHERE category_id IN :categoryIds GROUP BY category_id", nativeQuery = true)
    List<Tuple> countPostsInCategoryIds(@Param("categoryIds") List<Integer> categoryIds);
}
