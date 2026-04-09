package com.webdev.bloggingsystem.comment;

import com.webdev.bloggingsystem.user.AuthorDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
                "SELECT c.id, c.content, c.created_at, c.updated_at, c.author_id, u.username, " +
                    "COALESCE(r.reply_count, 0) AS reply_count " +
                "FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "LEFT JOIN ( " +
                    "SELECT parent_comment_id, COUNT(*) AS reply_count " +
                    "FROM comments WHERE parent_comment_id IS NOT NULL " +
                    "GROUP BY parent_comment_id) r " +
                    "ON r.parent_comment_id = c.id " +
                "WHERE c.post_id = :postId AND c.parent_comment_id IS NULL " +
                "ORDER BY c.created_at")
                    .param("postId", postId)
                    .query((rs, _) -> commentExtractor(rs))
                    .list();
    }

    public List<Comment> getReplyCommentsByParentId(int parentCommentId) {
        return jdbc.sql(
                "SELECT c.id, c.content, c.created_at, c.updated_at, c.author_id, u.username, " +
                    "COALESCE(r.reply_count, 0) AS reply_count " +
                "FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "LEFT JOIN ( " +
                    "SELECT parent_comment_id, COUNT(*) AS reply_count " +
                    "FROM comments WHERE parent_comment_id IS NOT NULL " +
                    "GROUP BY parent_comment_id) r " +
                    "ON r.parent_comment_id = c.id " +
                "WHERE c.parent_comment_id = :parentCommentId " +
                "ORDER BY c.created_at")
                    .param("parentCommentId", parentCommentId)
                    .query((rs, _) -> commentExtractor(rs))
                    .list();
    }



    private static Comment commentExtractor(ResultSet rs) throws SQLException {
        AuthorDto authorDto = new AuthorDto(
                rs.getInt("author_id"), rs.getString("username")
        );
        return new Comment(
                rs.getInt("id"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                authorDto,
                rs.getInt("reply_count")
        );
    }

}
