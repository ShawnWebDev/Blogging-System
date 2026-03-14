package com.webdev.bloggingsystem.blog;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class BlogEntryDao {

    private final JdbcClient jdbc;

    public BlogEntryDao(JdbcClient jdbcClient) {
        this.jdbc = jdbcClient;
    }

    public int insert(BlogEntry blogEntry) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.sql(
                "INSERT INTO blog_entries (content, title, description, slug, thumbnail_url, thumbnail_alt, in_progress) " +
                "VALUES (:content, :title, :description, :slug, :thumbnail_url, :thumbnail_alt, :in_progress)")
                    .param("content", blogEntry.getContent())
                    .param("title", blogEntry.getTitle())
                    .param("slug", blogEntry.getSlug())
                    .param("description", blogEntry.getDescription())
                    .param("thumbnail_url", blogEntry.getThumbnailUrl())
                    .param("thumbnail_alt", blogEntry.getThumbnailAlt())
                    .param("in_progress", blogEntry.getInProgress())
                    .update(keyHolder);

        return keyHolder.getKey().intValue();
    }

    public Optional<BlogEntry> findById(int id) {
        return jdbc.sql("SELECT b.id, b.title, b.description, b.content, b.created_at, b.updated_at, b.slug, b.thumbnail_url, b.thumbnail_alt, b.in_progress, " +
                        "GROUP_CONCAT(c.category_name ORDER BY c.category_name ASC) AS category_list " +
                                "FROM blog_entries b " +
                                "LEFT JOIN posts_categories pc ON pc.post_id = b.id " +
                                "LEFT JOIN categories c ON c.id = pc.category_id " +
                                "WHERE b.id = :id " +
                                "GROUP BY b.id")
                    .param("id", id)
                    .query((rs, _) -> singleBlogEntryExtractor(rs))
                    .optional();
    }

    public Optional<BlogEntry> findBySlug(String slug) {
        return jdbc.sql("SELECT b.id, b.title, b.description, b.content, b.created_at, b.updated_at, b.slug, b.thumbnail_url, b.thumbnail_alt, b.in_progress, " +
                        "GROUP_CONCAT(c.category_name ORDER BY c.category_name ASC) AS category_list " +
                        "FROM blog_entries b " +
                        "LEFT JOIN posts_categories pc ON pc.post_id = b.id " +
                        "LEFT JOIN categories c ON c.id = pc.category_id " +
                        "WHERE b.slug = :slug " +
                        "GROUP BY b.id")
                .param("slug", slug)
                .query((rs, _) -> singleBlogEntryExtractor(rs))
                .optional();
    }

    public int count() {
        return jdbc.sql("SELECT count(b.id) FROM blog_entries b")
                .query(Integer.class)
                .single();
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

    public List<SimpleBlogEntryDto> findAllSimple(int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        return jdbc.sql(
                "SELECT b.id, b.title, b.description, b.created_at, b.thumbnail_url, b.thumbnail_alt, " +
                    "GROUP_CONCAT(c.category_name ORDER BY c.category_name ASC) AS category_list " +
                "FROM blog_entries b " +
                "LEFT JOIN posts_categories pc ON pc.post_id = b.id " +
                "LEFT JOIN categories c ON c.id = pc.category_id " +
                "WHERE NOT b.in_progress " +
                "GROUP BY b.id " +
                "ORDER BY b.id " +
                "LIMIT :pageSize OFFSET :offset")
                    .param("pageSize", pageSize)
                    .param("offset", offset)
                    .query((rs, _) -> simpleBlogEntryExtractor(rs))
                    .list();
    }

    public List<SimpleBlogEntryDto> findAllSimpleBlogEntriesToCategoryName(String categoryName, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        // first, the subquery limits selection to only post_ids that have relation to specified category_name.
        // second, columns are selected and joined to a concatenated string of all grouped category_names related to those post_ids.
        return jdbc.sql(
                "SELECT b.id, b.title, b.description, b.created_at, b.thumbnail_url, b.thumbnail_alt, " +
                    "GROUP_CONCAT(c.category_name ORDER BY c.category_name ASC) AS category_list " +
                "FROM blog_entries b " +
                "JOIN posts_categories pc ON pc.post_id = b.id " +
                "JOIN categories c ON c.id = pc.category_id " +
                "WHERE b.id IN (" +
                    "SELECT pc_sub.post_id FROM posts_categories pc_sub " +
                    "JOIN categories c_sub ON c_sub.id = pc_sub.category_id " +
                    "WHERE c_sub.category_name = :categoryName) AND NOT b.in_progress " +
                "GROUP BY b.id " +
                "ORDER BY b.id " +
                "LIMIT :pageSize OFFSET :offset")
                    .param("categoryName", categoryName)
                    .param("pageSize", pageSize)
                    .param("offset", offset)
                    .query((rs, _) -> simpleBlogEntryExtractor(rs))
                    .list();
    }

    public List<SimpleBlogEntryDto> findAllSimpleInProgress () {
        return jdbc.sql(
                        "SELECT b.id, b.title, b.description, b.created_at, b.thumbnail_url, b.thumbnail_alt, " +
                                "GROUP_CONCAT(c.category_name ORDER BY c.category_name ASC) AS category_list " +
                                "FROM blog_entries b " +
                                "LEFT JOIN posts_categories pc ON pc.post_id = b.id " +
                                "LEFT JOIN categories c ON c.id = pc.category_id " +
                                "WHERE b.in_progress " +
                                "GROUP BY b.id " +
                                "ORDER BY b.id")
                .query((rs, _) -> simpleBlogEntryExtractor(rs))
                .list();
    }

    public int update(BlogEntry blogEntry) {
        return jdbc.sql(
                "UPDATE blog_entries " +
                "SET title = :title, description = :description, content = :content, slug = :slug, thumbnail_url = :thumbnailUrl, thumbnail_alt = :thumbnailAlt, in_progress = :in_progress " +
                "WHERE id = :id")
                    .param("id", blogEntry.getId())
                    .param("title", blogEntry.getTitle())
                    .param("description", blogEntry.getDescription())
                    .param("content", blogEntry.getContent())
                    .param("slug", blogEntry.getSlug())
                    .param("thumbnailUrl", blogEntry.getThumbnailUrl())
                    .param("thumbnailAlt", blogEntry.getThumbnailAlt())
                    .param("in_progress", blogEntry.getInProgress())
                    .update();
    }

    public int deleteById(int id) {
        return jdbc.sql(
                "DELETE from blog_entries WHERE id = :id")
                    .param("id", id)
                    .update();
    }

    private static SimpleBlogEntryDto simpleBlogEntryExtractor(ResultSet rs) throws SQLException {
        return new SimpleBlogEntryDto(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toInstant(),
                List.of(rs.getString("category_list").split(",")),
                rs.getString("thumbnail_url"),
                rs.getString("thumbnail_alt")
        );
    }

    private static BlogEntry singleBlogEntryExtractor(ResultSet rs) throws SQLException {
        if (rs.wasNull() || rs.getString("category_list") == null) return null;

        return new BlogEntry(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getString("slug"),
                List.of(rs.getString("category_list").split(",")),
                rs.getString("thumbnail_url"),
                rs.getString("thumbnail_alt"),
                rs.getBoolean("in_progress")
        );
    }
}
