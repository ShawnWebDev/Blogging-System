package com.webdev.bloggingsystem.errorHandling;

public class BlogEntryException extends RuntimeException {
    public BlogEntryException(String message) {
        super(message);
    }
}
