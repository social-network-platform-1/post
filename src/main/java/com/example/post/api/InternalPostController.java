package com.example.post.api;

import com.example.post.application.PostServiceImp;
import com.example.post.dto.response.PostOwnerResponse;
import com.example.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/posts")
public class InternalPostController {

    private final PostServiceImp postService;

    @GetMapping("/{postId}/owner")
    public ResponseEntity<PostOwnerResponse> getPostOwner(
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(
                postService.getPostOwner(postId)
        );
    }
}
