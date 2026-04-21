package com.webdev.bloggingsystem.comment;

import com.webdev.bloggingsystem.user.AuthorDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class CommentDao {
    private final JdbcClient jdbc;

    public CommentDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // The subquery runs count() once. then, is joined to result set as reply_count after parent query is finished.
    // COALESCE sets a default of 0 for NULL values (top-level comments have NULL on parent_comment_id).
    // LEFT JOIN ensures top-level comments without replies are still included.
    public List<Comment> getParentCommentsByPostId(int postId) {
        return jdbc.sql(
                "SELECT c.id, c.content, c.created_at, c.updated_at, c.post_id, c.author_id, u.username, " +
                    "COALESCE(r.reply_count, 0) AS reply_count " +
                "FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "LEFT JOIN ( " +
                    "SELECT parent_comment_id, COUNT(*) AS reply_count " +
                    "FROM comments WHERE parent_comment_id IS NOT NULL " +
                    "GROUP BY parent_comment_id) r " +
                    "ON r.parent_comment_id = c.id " +
                "WHERE c.post_id = :postId AND c.parent_comment_id IS NULL " +
                "ORDER BY c.created_at desc")
                    .param("postId", postId)
                    .query((rs, _) -> commentExtractor(rs))
                    .list();
    }

    public List<Comment> getReplyCommentsByParentId(int parentCommentId) {
        return jdbc.sql(
                "SELECT c.id, c.content, c.created_at, c.updated_at, c.post_id, c.author_id, u.username, " +
                    "COALESCE(r.reply_count, 0) AS reply_count " +
                "FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "LEFT JOIN ( " +
                    "SELECT parent_comment_id, COUNT(*) AS reply_count " +
                    "FROM comments WHERE parent_comment_id IS NOT NULL " +
                    "GROUP BY parent_comment_id) r " +
                    "ON r.parent_comment_id = c.id " +
                "WHERE c.parent_comment_id = :parentCommentId " +
                "ORDER BY c.created_at desc")
                    .param("parentCommentId", parentCommentId)
                    .query((rs, _) -> commentExtractor(rs))
                    .list();
    }

    public Optional<Comment> getCommentById(int commentId) {
        return jdbc.sql(
                "SELECT c.id, c.content, c.created_at, c.updated_at, c.post_id, c.parent_comment_id,c.author_id, u.username, 0 AS reply_count " +
                "FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "WHERE c.id = :commentId")
                    .param("commentId", commentId)
                    .query((rs, _) -> fullCommentExtractor(rs))
                    .optional();
    }

    public Optional<String> getCommentContentByCommentId(int commentId) {
        return jdbc.sql(
                "SELECT content FROM comments " +
                "WHERE id = :commentId")
                    .param("commentId", commentId)
                    .query(String.class)
                    .optional();
    }

    public boolean existsCommentByIdInEntry(int commentId, int entryId) {
        return jdbc.sql(
                "SELECT 1 FROM comments c " +
                "WHERE c.id = :commentId AND c.post_id = :entryId")
                    .param("commentId", commentId)
                    .param("entryId", entryId)
                    .query(Integer.class)
                    .optional().isPresent();
    }

    public int insert(Comment comment) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql(
            "INSERT INTO comments (content, parent_comment_id, author_id, post_id) " +
            "VALUES (:content, :parentCommentId, :authorId, :postId)")
                .param("content", comment.getContent())
                .param("parentCommentId", comment.getParentCommentId())
                .param("authorId", comment.getAuthor().id())
                .param("postId", comment.getBlogEntryId())
                .update(keyHolder);

        return keyHolder.getKey().intValue();
    }

    public int update(int commentId, String content) {
        return jdbc.sql(
                "UPDATE comments " +
                "SET content = :content " +
                "WHERE id = :commentId")
                    .param("content", content)
                    .param("commentId", commentId)
                    .update();
    }


    private Comment commentExtractor(ResultSet rs) throws SQLException {
        AuthorDto authorDto = new AuthorDto(
                rs.getInt("author_id"), rs.getString("username")
        );
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new Comment(
                rs.getInt("id"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toInstant(),
                updatedAt != null ? updatedAt.toInstant() : null,
                rs.getInt("post_id"),
                authorDto,
                rs.getInt("reply_count")
        );
    }

    private Comment fullCommentExtractor(ResultSet rs) throws SQLException {
        AuthorDto authorDto = new AuthorDto(
                rs.getInt("author_id"), rs.getString("username")
        );
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new Comment(
                rs.getInt("id"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toInstant(),
                updatedAt != null ? updatedAt.toInstant() : null,
                rs.getInt("post_id"),
                rs.getInt("parent_comment_id"),
                authorDto,
                rs.getInt("reply_count")
        );
    }

}
