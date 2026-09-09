package com.example.post.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserProviderTest {

    private final CurrentUserProvider currentUserProvider =
            new CurrentUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_shouldReturnUserId() {
        UUID userId = UUID.randomUUID();

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn(userId.toString());

        SecurityContext securityContext =
                mock(SecurityContext.class);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        UUID result =
                currentUserProvider.getCurrentUserId();

        assertEquals(userId, result);
    }
}
