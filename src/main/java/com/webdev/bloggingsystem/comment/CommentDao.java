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

    public List<Comment> getParentCommentsByPostId(int postId) {
        return jdbc.sql(
                "SELECT c.id, c.content, c.created_at, c.updated_at, c.author_id, u.username " +
                "FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "WHERE c.post_id = :postId AND c.parent_comment_id IS NULL " +
                "ORDER BY c.created_at")
                    .param("postId", postId)
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
                authorDto
        );
    }

}
