package com.example.post.exception;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String message) {
        super(message);
    }

    public PostNotFoundException(UUID postId) {
        super(String.format("Post with id %s not found", postId));
    }
}
