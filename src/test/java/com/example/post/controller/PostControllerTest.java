package com.example.post.controller;

import com.example.post.api.PostController;
import com.example.post.domain.model.Visibility;
import com.example.post.dto.request.CreatePostRequest;
import com.example.post.dto.request.UpdatePostRequest;
import com.example.post.dto.response.PostResponse;
import com.example.post.security.CurrentUserProvider;
import com.example.post.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    void createPost_shouldReturnCreatedPost() throws Exception {
        UUID currentUserId = UUID.randomUUID();

        CreatePostRequest request = new CreatePostRequest(
                "Hello",
                Visibility.PUBLIC
        );

        PostResponse response = mock(PostResponse.class);

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(currentUserId);

        when(postService.createPost(
                eq(currentUserId),
                any(CreatePostRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "content": "Hello",
                                        "visibility": "PUBLIC"
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        verify(postService).createPost(
                eq(currentUserId),
                any(CreatePostRequest.class)
        );
    }

    @Test
    void getPost_shouldReturnPost() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        PostResponse response = mock(PostResponse.class);

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(currentUserId);

        when(postService.getPost(
                postId,
                currentUserId
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/posts/{postId}", postId)
                )
                .andExpect(status().isOk());

        verify(currentUserProvider)
                .getCurrentUserId();

        verify(postService)
                .getPost(postId, currentUserId);
    }

    @Test
    void updatePost_shouldReturnUpdatedPost() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        PostResponse response = mock(PostResponse.class);

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(currentUserId);

        when(postService.updatePost(
                eq(postId),
                eq(currentUserId),
                any(UpdatePostRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/posts/{postId}", postId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "content": "Updated content",
                                        "visibility": "PUBLIC"
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        verify(postService).updatePost(
                eq(postId),
                eq(currentUserId),
                any(UpdatePostRequest.class)
        );
    }

    @Test
    void deletePost_shouldReturnNoContent() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(currentUserId);

        doNothing()
                .when(postService)
                .deletePost(postId, currentUserId);

        mockMvc.perform(
                        delete("/api/v1/posts/{postId}", postId)
                )
                .andExpect(status().isNoContent());

        verify(postService)
                .deletePost(postId, currentUserId);
    }

    @Test
    void getPostsByAuthor_shouldReturnPosts() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        PostResponse response = mock(PostResponse.class);

        Page<PostResponse> page =
                new PageImpl<>(List.of(response));

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(currentUserId);

        when(postService.getPostsByAuthor(
                eq(authorId),
                any(Pageable.class),
                eq(currentUserId)
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/posts/author/{authorId}", authorId)
                )
                .andExpect(status().isOk());

        verify(postService).getPostsByAuthor(
                eq(authorId),
                any(Pageable.class),
                eq(currentUserId)
        );
    }

    @Test
    void getPostsByAuthor_shouldPassPageable() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(currentUserId);

        when(postService.getPostsByAuthor(
                eq(authorId),
                any(Pageable.class),
                eq(currentUserId)
        )).thenReturn(Page.empty());

        mockMvc.perform(
                        get("/api/v1/posts/author/{authorId}", authorId)
                                .param("page", "2")
                                .param("size", "20")
                )
                .andExpect(status().isOk());

        verify(postService).getPostsByAuthor(
                eq(authorId),
                argThat(pageable ->
                        pageable.getPageNumber() == 2
                                && pageable.getPageSize() == 20
                ),
                eq(currentUserId)
        );
    }

    @Test
    void createPost_shouldReturnBadRequestWhenRequestIsInvalid()
            throws Exception {

        UUID currentUserId = UUID.randomUUID();

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(currentUserId);

        mockMvc.perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "content": "",
                                        "visibility": "PUBLIC"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());

        verify(postService, never())
                .createPost(
                        any(),
                        any(CreatePostRequest.class)
                );
    }
}

