package com.webdev.bloggingsystem.s3Stuff;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UploadedImgDao {
    private final JdbcClient jdbc;

    public UploadedImgDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(UploadedImg uploadedImg) {
        jdbc.sql(
            "INSERT INTO uploaded_images (post_id, url) " +
            "VALUES (:post_id, :url) ")
                .param("post_id", uploadedImg.postId())
                .param("url", uploadedImg.url())
                .update();
    }

    public List<UploadedImg> findAllByPostId(int postId) {
        return jdbc.sql(
                "SELECT post_id, url FROM uploaded_images " +
                "WHERE post_id = :postId")
                .param("postId", postId)
                .query((rs, _) -> uploadedImgExtractor(rs))
                .list();
    }

    private UploadedImg uploadedImgExtractor(ResultSet rs) throws SQLException {
        return new UploadedImg(
                rs.getInt("post_id"),
                rs.getString("url")
        );
    }
}
