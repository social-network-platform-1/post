package com.example.post.security;

import com.example.post.api.PostController;
import com.example.post.dto.response.PostResponse;
import com.example.post.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(PostController.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    void requestWithoutAuthentication_shouldReturn401() throws Exception {
        UUID postId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/posts/{postId}", postId)
                )
                .andExpect(status().isUnauthorized());

        verify(postService, never())
                .getPost(any(), any());
    }

    @Test
    void requestWithAuthentication_shouldBeAllowed() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        PostResponse response = mock(PostResponse.class);

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(currentUserId);

        when(postService.getPost(
                postId,
                currentUserId
        )).thenReturn(response);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        currentUserId.toString(),
                        null,
                        List.of()
                );

        mockMvc.perform(
                        get("/api/v1/posts/{postId}", postId)
                                .with(authentication(authentication))
                )
                .andExpect(status().isOk());

        verify(currentUserProvider)
                .getCurrentUserId();

        verify(postService)
                .getPost(postId, currentUserId);
    }
}

