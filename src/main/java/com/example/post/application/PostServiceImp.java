package com.example.post.application;

import com.example.post.cache.PostCacheService;
import com.example.post.client.FriendshipClient;
import com.example.post.domain.model.Post;
import com.example.post.domain.model.Visibility;
import com.example.post.domain.repository.PostRepository;
import com.example.post.dto.request.CreatePostRequest;
import com.example.post.dto.request.UpdatePostRequest;
import com.example.post.dto.response.PostOwnerResponse;
import com.example.post.dto.response.PostResponse;
import com.example.post.exception.PostNotFoundException;
import com.example.post.kafka.KafkaProducer;
import com.example.post.mapper.PostMapper;
import com.example.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PostServiceImp implements PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PostCacheService postCacheService;
    private final KafkaProducer kafkaProducer;
    private final FriendshipClient friendshipClient;

    @Override
    public PostResponse createPost(UUID authorId, CreatePostRequest request) {
        Post post = Post.builder()
                .authorId(authorId)
                .content(request.content())
                .visibility(request.visibility())
                .build();

        post = postRepository.save(post);

        PostResponse response = postMapper.toResponse(post);

        postCacheService.savePostResponse(response);

        kafkaProducer.sendPostCreated(postMapper.toCreatedEvent(post));

        return response;
    }

    @Override
    public PostResponse getPost(UUID postId, UUID currentUserId) {
        Optional<PostResponse> cachedPost =
                postCacheService.getPostResponse(postId);

        if (cachedPost.isPresent()) {
            PostResponse response = cachedPost.get();

            validateAccess(response, currentUserId);

            return response;
        }

        Post post = findActivePostById(postId);

        validateAccess(post, currentUserId);

        PostResponse response = postMapper.toResponse(post);

        postCacheService.savePostResponse(response);

        return response;
    }

    @Override
    public PostResponse updatePost(UUID postId, UUID currentUserId, UpdatePostRequest updatePostRequest) {
        Post post = findActivePostById(postId);

        validateAuthor(post, currentUserId);

        post.setContent(updatePostRequest.content());
        post.setVisibility(updatePostRequest.visibility());

        post = postRepository.save(post);


        PostResponse response = postMapper.toResponse(post);

        postCacheService.savePostResponse(response);

        kafkaProducer.sendPostUpdated(postMapper.toUpdatedEvent(post));

        return response;
    }

    @Override
    public void deletePost(UUID postId, UUID currentUserId) {
        Post post = findActivePostById(postId);

        validateAuthor(post, currentUserId);

        post.onDeleted();

        post = postRepository.save(post);

        postCacheService.deletePostResponse(postId);

        kafkaProducer.sendPostDeleted(postMapper.toDeletedEvent(post));
    }

    @Override
    public Page<PostResponse> getPostsByAuthor(UUID authorId, Pageable pageable, UUID currentUserId) {
        if(authorId.equals(currentUserId)) {
            return postRepository
                    .findByAuthorIdAndDeletedAtIsNull(authorId, pageable)
                    .map(postMapper::toResponse);
        }
        return postRepository
                .findByAuthorIdAndVisibilityAndDeletedAtIsNull(authorId, Visibility.PUBLIC, pageable)
                .map(postMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PostOwnerResponse getPostOwner(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found: " + postId)
                );

        return new PostOwnerResponse(
                post.getId(),
                post.getAuthorId()
        );
    }

    private Post findActivePostById(UUID postId) {
        return postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    private void validateAuthor(Post post, UUID currentUserId) {
        if(!post.getAuthorId().equals(currentUserId)){
            throw new AccessDeniedException("You cannot modify another user's post");
        }
    }

    private void validateAccess(
            UUID authorId,
            Visibility visibility,
            UUID currentUserId
    ) {
        switch (visibility) {
            case PUBLIC -> {
                break;
            }

            case PRIVATE -> {
                if (!authorId.equals(currentUserId)) {
                    throw new AccessDeniedException(
                            "You cannot access this post"
                    );
                }
                break;
            }

            case FRIENDS -> {
                boolean areFriends = friendshipClient.areFriends(authorId, currentUserId);

                if (!areFriends) {
                    throw new AccessDeniedException("Access allowed only for friends");
                }
                break;
            }
        }
    }

    private void validateAccess(
            PostResponse postResponse,
            UUID currentUserId
    ) {
        validateAccess(
                postResponse.authorId(),
                postResponse.visibility(),
                currentUserId
        );
    }

    private void validateAccess(
            Post post,
            UUID currentUserId
    ) {
        validateAccess(
                post.getAuthorId(),
                post.getVisibility(),
                currentUserId
        );
    }
}
