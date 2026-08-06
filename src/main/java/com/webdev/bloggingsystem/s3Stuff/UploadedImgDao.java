package com.webdev.bloggingsystem.s3Stuff;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UploadedImgDao {
    private final JdbcClient jdbc;

    public UploadedImgDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }


}
