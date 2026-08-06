package com.webdev.bloggingsystem.s3Stuff;

public record UploadedImg(
        int postId,
        String url
) { }