package com.example.post.service;

import com.example.post.dto.request.CreatePostRequest;
import com.example.post.dto.request.UpdatePostRequest;
import com.example.post.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostService {
    PostResponse createPost(UUID authorId, CreatePostRequest request);
    PostResponse getPost(UUID postId, UUID currentUserId);
    PostResponse updatePost(UUID postId, UUID currentUserId, UpdatePostRequest updatePostRequest);
    void deletePost(UUID postId,  UUID currentUserId);
    Page<PostResponse> getPostsByAuthor(UUID authorId, Pageable pageable, UUID currentUserId);
}
