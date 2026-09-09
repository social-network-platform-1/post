package com.example.post.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    private final HttpServletRequest request =
            mock(HttpServletRequest.class);


    @Test
    void handlePostNotFound_shouldReturn404() {
        String message = "Post not found";

        PostNotFoundException exception =
                new PostNotFoundException(message);

        when(request.getRequestURI())
                .thenReturn("/api/v1/posts/123");

        ResponseEntity<ErrorResponse> result =
                handler.handlePostNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                result.getStatusCode()
        );

        ErrorResponse body = result.getBody();

        assertNotNull(body);

        assertEquals(404, body.status());
        assertEquals("Not Found", body.error());
        assertEquals(message, body.message());
        assertEquals("/api/v1/posts/123", body.path());
        assertNotNull(body.timestamp());
    }

    @Test
    void handleAccessDenied_shouldReturn403() {
        String message = "You cannot modify another user's post";

        AccessDeniedException exception =
                new AccessDeniedException(message);

        when(request.getRequestURI())
                .thenReturn("/api/v1/posts/123");

        ResponseEntity<ErrorResponse> result =
                handler.handleAccessDenied(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                result.getStatusCode()
        );

        ErrorResponse body = result.getBody();

        assertNotNull(body);

        assertEquals(403, body.status());
        assertEquals("Forbidden", body.error());
        assertEquals(message, body.message());
        assertEquals("/api/v1/posts/123", body.path());
        assertNotNull(body.timestamp());
    }

    @Test
    void handleIllegalArgument_shouldReturn400() {
        String message = "Invalid argument";

        IllegalArgumentException exception =
                new IllegalArgumentException(message);

        ResponseEntity<ErrorResponse> result =
                handler.handleIllegalArgument(exception);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                result.getStatusCode()
        );

        ErrorResponse body = result.getBody();

        assertNotNull(body);

        assertEquals(400, body.status());
        assertEquals("Bad request", body.error());
        assertEquals(message, body.message());
        assertNull(body.path());
        assertNotNull(body.timestamp());
    }

    @Test
    void handleFriendshipServiceUnavailable_shouldReturn503() {
        String message = "Friendship service is unavailable";

        FriendshipServiceUnavailableException exception =
                new FriendshipServiceUnavailableException(
                        message,
                        null
                );

        ResponseEntity<ErrorResponse> result =
                handler.handleFriendshipServiceUnavailable(
                        exception
                );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                result.getStatusCode()
        );

        ErrorResponse body = result.getBody();

        assertNotNull(body);

        assertEquals(503, body.status());
        assertEquals("Service Unavailable", body.error());
        assertEquals(message, body.message());
        assertNull(body.path());
        assertNull(body.timestamp());
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturn400() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        when(exception.getMessage())
                .thenReturn("Validation failed");

        ResponseEntity<ErrorResponse> result =
                handler.handleMethodArgumentNotValid(
                        exception
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                result.getStatusCode()
        );

        ErrorResponse body = result.getBody();

        assertNotNull(body);

        assertEquals(400, body.status());
        assertEquals("Validation failed", body.error());
        assertEquals(
                "Validation failed",
                body.message()
        );

        assertNull(body.timestamp());
        assertNull(body.path());
    }
}
