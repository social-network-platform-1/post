package com.example.post.exception;

public class FriendshipServiceUnavailableException
        extends RuntimeException {

    public FriendshipServiceUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
