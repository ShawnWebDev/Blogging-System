package com.webdev.bloggingsystem.blog;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class BlogEntryDao {

    private final JdbcClient jdbc;

    public BlogEntryDao(JdbcClient jdbcClient) {
        this.jdbc = jdbcClient;
    }

    public int insert(BlogEntry blogEntry) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.sql(
                "INSERT INTO blog_entries (content, title, description, slug, thumbnail_url, thumbnail_alt, in_progress, code_url, demo_url, is_portfolio, has_article) " +
                "VALUES (:content, :title, :description, :slug, :thumbnail_url, :thumbnail_alt, :in_progress, :code_url, :demo_url, :is_portfolio, :has_article)")
                    .param("content", blogEntry.getContent())
                    .param("title", blogEntry.getTitle())
                    .param("slug", blogEntry.getSlug())
                    .param("description", blogEntry.getDescription())
                    .param("thumbnail_url", blogEntry.getThumbnailUrl())
                    .param("thumbnail_alt", blogEntry.getThumbnailAlt())
                    .param("in_progress", blogEntry.getInProgress())
                    .param("code_url", blogEntry.getCodeUrl())
                    .param("demo_url", blogEntry.getDemoUrl())
                    .param("is_portfolio", blogEntry.isPortfolio())
                    .param("has_article", blogEntry.getHasArticle())
                    .update(keyHolder);

        if (keyHolder.getKey() == null) throw new RuntimeException("Insert failure, key is null!");
        return keyHolder.getKey().intValue();
    }

    public Optional<BlogEntry> findById(int id) {
        return jdbc.sql(
                "SELECT b.id, b.title, b.description, b.content, b.created_at, b.updated_at, b.slug, b.thumbnail_url, b.thumbnail_alt, b.in_progress, b.code_url, b.demo_url, b.is_portfolio, b.has_article, " +
                    "GROUP_CONCAT(c.category_name ORDER BY c.category_name ASC) AS category_list " +
                "FROM blog_entries b " +
                "LEFT JOIN posts_categories pc ON pc.post_id = b.id " +
                "LEFT JOIN categories c ON c.id = pc.category_id " +
                "WHERE b.id = :id " +
                "GROUP BY b.id")
                    .param("id", id)
                    .query((rs, _) -> fullBlogEntryExtractor(rs))
                    .optional();
    }

    public boolean existsByTitleAndNotId(String title, Integer id) {
        String sql = "SELECT 1 FROM blog_entries b WHERE b.title = :title";
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        if (id != null) {
            sql = sql + " AND NOT b.id = :id ";
            params.put("id", id);
        }
        sql = sql + " LIMIT 1";
        return jdbc.sql(sql)
                .params(params)
                .query(Integer.class)
                .optional().isPresent();
    }

    public List<SimpleBlogEntryDto> findAllSimple() {
        return jdbc.sql(
                "SELECT b.id, b.slug, b.title, b.description, b.created_at, b.thumbnail_url, b.thumbnail_alt " +
                "FROM blog_entries b " +
                "WHERE NOT b.in_progress AND b.has_article " +
                "ORDER BY b.created_at DESC")
                    .query((rs, _) -> simpleBlogEntryExtractor(rs))
                    .list();
    }

    public List<SimplePortfolioEntryDto> findAllSimplePortfolio() {
        return jdbc.sql(
                "SELECT b.id, b.slug, b.title, b.description, b.created_at, b.thumbnail_url, b.thumbnail_alt, b.code_url, b.demo_url, b.has_article " +
                "FROM blog_entries b " +
                "WHERE NOT b.in_progress AND b.is_portfolio " +
                "ORDER BY b.created_at DESC")
                .query((rs, _) -> simplePortfolioEntryExtractor(rs))
                .list();
    }

    public List<SimpleBlogEntryDto> findAllSimpleBlogEntriesToCategoryName(String categoryName) {
        // first, the subquery limits selection to only post_ids that have relation to specified category_name.
        // second, columns are selected and joined to a concatenated string of all grouped category_names related to those post_ids.
        return jdbc.sql(
                "SELECT b.id, b.slug, b.title, b.description, b.created_at, b.thumbnail_url, b.thumbnail_alt " +
                "FROM blog_entries b " +
                "WHERE b.id IN ( " +
                    "SELECT pc_sub.post_id FROM posts_categories pc_sub " +
                    "JOIN categories c_sub ON c_sub.id = pc_sub.category_id " +
                    "WHERE c_sub.category_name = :categoryName " +
                        ") AND NOT b.in_progress AND b.has_article " +
                "ORDER BY b.created_at DESC")
                    .param("categoryName", categoryName)
                    .query((rs, _) -> simpleBlogEntryExtractor(rs))
                    .list();
    }

    public List<SimpleBlogEntryDto> findAllSimpleInProgress () {
        return jdbc.sql(
                "SELECT b.id, b.slug, b.title, b.description, b.created_at, b.thumbnail_url, b.thumbnail_alt " +
                "FROM blog_entries b " +
                "WHERE b.in_progress or NOT b.has_article " +
                "ORDER BY b.created_at")
                    .query((rs, _) -> simpleBlogEntryExtractor(rs))
                    .list();
    }

    public int update(BlogEntry blogEntry) {
        return jdbc.sql(
                "UPDATE blog_entries " +
                "SET title = :title, description = :description, content = :content, slug = :slug," +
                        " thumbnail_url = :thumbnailUrl, thumbnail_alt = :thumbnailAlt, in_progress = :in_progress, code_url = :code_url, demo_url = :demo_url, is_portfolio = :is_portfolio " +
                "WHERE id = :id")
                    .param("id", blogEntry.getId())
                    .param("title", blogEntry.getTitle())
                    .param("description", blogEntry.getDescription())
                    .param("content", blogEntry.getContent())
                    .param("slug", blogEntry.getSlug())
                    .param("thumbnailUrl", blogEntry.getThumbnailUrl())
                    .param("thumbnailAlt", blogEntry.getThumbnailAlt())
                    .param("in_progress", blogEntry.getInProgress())
                    .param("code_url", blogEntry.getCodeUrl())
                    .param("demo_url", blogEntry.getDemoUrl())
                    .param("is_portfolio", blogEntry.isPortfolio())
                    .update();
    }

    public int deleteById(int id) {
        return jdbc.sql(
                "DELETE FROM blog_entries WHERE id = :id")
                    .param("id", id)
                    .update();
    }


    private SimpleBlogEntryDto simpleBlogEntryExtractor(ResultSet rs) throws SQLException {
        return new SimpleBlogEntryDto(
                rs.getInt("id"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getString("thumbnail_url"),
                rs.getString("thumbnail_alt")
        );
    }

    private SimplePortfolioEntryDto simplePortfolioEntryExtractor(ResultSet rs) throws SQLException {
        return new SimplePortfolioEntryDto(
                rs.getInt("id"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getString("thumbnail_url"),
                rs.getString("thumbnail_alt"),
                rs.getString("code_url"),
                rs.getString("demo_url"),
                rs.getBoolean("has_article")
        );
    }

    private BlogEntry fullBlogEntryExtractor(ResultSet rs) throws SQLException {
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        String concatCategories = rs.getString("category_list");
        return new BlogEntry(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toInstant(),
                updatedAt == null ? null : updatedAt.toInstant(),
                rs.getString("slug"),
                concatCategories == null ? List.of() : List.of(concatCategories.split(",")),
                rs.getString("thumbnail_url"),
                rs.getString("thumbnail_alt"),
                rs.getBoolean("in_progress"),
                rs.getString("code_url"),
                rs.getString("demo_url"),
                rs.getBoolean("is_portfolio"),
                rs.getBoolean("has_article")
        );
    }
}
