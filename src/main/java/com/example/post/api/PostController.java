package com.example.post.api;

import com.example.post.dto.request.CreatePostRequest;
import com.example.post.dto.request.UpdatePostRequest;
import com.example.post.dto.response.PostResponse;
import com.example.post.security.CurrentUserProvider;
import com.example.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/posts")
public class PostController  {
    private final PostService postService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public PostResponse createPost(@RequestBody @Valid CreatePostRequest createPostRequest) {
        UUID authorId = currentUserProvider.getCurrentUserId();
        return postService.createPost(authorId, createPostRequest);
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(@PathVariable UUID postId) {
        return postService.getPost(postId, currentUserProvider.getCurrentUserId() );
    }

    @PutMapping("/{postId}")
    public PostResponse updatePost(@PathVariable UUID postId, @RequestBody @Valid UpdatePostRequest updatePostRequest) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return postService.updatePost(postId, currentUserId, updatePostRequest);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable UUID postId) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        postService.deletePost(postId, currentUserId);
    }

    @GetMapping("/author/{authorId}")
    public Page<PostResponse> getPostsByAuthor(@PathVariable UUID authorId, @PageableDefault(
            page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return postService.getPostsByAuthor(authorId, pageable, currentUserId);
    }
}
