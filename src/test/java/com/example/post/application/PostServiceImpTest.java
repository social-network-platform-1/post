package com.example.post.application;

import com.example.post.cache.PostCacheService;
import com.example.post.client.FriendshipClient;
import com.example.post.domain.model.Post;
import com.example.post.domain.model.Visibility;
import com.example.post.domain.repository.PostRepository;
import com.example.post.dto.event.PostCreatedEvent;
import com.example.post.dto.event.PostDeletedEvent;
import com.example.post.dto.event.PostUpdatedEvent;
import com.example.post.dto.request.CreatePostRequest;
import com.example.post.dto.request.UpdatePostRequest;
import com.example.post.dto.response.PostResponse;
import com.example.post.exception.FriendshipServiceUnavailableException;
import com.example.post.exception.PostNotFoundException;
import com.example.post.kafka.KafkaProducer;
import com.example.post.mapper.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImpTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostCacheService postCacheService;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private FriendshipClient friendshipClient;

    @InjectMocks
    private PostServiceImp postService;

    private UUID postId;
    private UUID authorId;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
    }

    @Test
    void createPost_shouldCreatePostAndCacheAndPublishEvent() {
        CreatePostRequest request = new CreatePostRequest(
                "Hello",
                Visibility.PUBLIC
        );

        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content(request.content())
                .visibility(request.visibility())
                .build();

        PostResponse response = mock(PostResponse.class);
        PostCreatedEvent event = mock(PostCreatedEvent.class);

        when(postRepository.save(any(Post.class)))
                .thenReturn(post);

        when(postMapper.toResponse(post))
                .thenReturn(response);

        when(postMapper.toCreatedEvent(post))
                .thenReturn(event);

        PostResponse result =
                postService.createPost(authorId, request);

        assertSame(response, result);

        verify(postRepository).save(any(Post.class));
        verify(postMapper).toResponse(post);
        verify(postCacheService).savePostResponse(response);
        verify(postMapper).toCreatedEvent(post);
        verify(kafkaProducer).sendPostCreated(event);
    }

    @Test
    void getPost_shouldReturnCachedPost() {
        PostResponse response = mock(PostResponse.class);

        when(response.authorId())
                .thenReturn(authorId);

        when(response.visibility())
                .thenReturn(Visibility.PUBLIC);

        when(postCacheService.getPostResponse(postId))
                .thenReturn(Optional.of(response));

        PostResponse result =
                postService.getPost(postId, currentUserId);

        assertSame(response, result);

        verify(postCacheService).getPostResponse(postId);

        verify(postRepository, never())
                .findByIdAndDeletedAtIsNull(any());

        verify(postCacheService, never())
                .savePostResponse(any());
    }

    @Test
    void getPost_shouldGetFromDatabaseWhenCacheMiss() {
        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Hello")
                .visibility(Visibility.PUBLIC)
                .build();

        PostResponse response = mock(PostResponse.class);

        when(postCacheService.getPostResponse(postId))
                .thenReturn(Optional.empty());

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        when(postMapper.toResponse(post))
                .thenReturn(response);

        PostResponse result =
                postService.getPost(postId, currentUserId);

        assertSame(response, result);

        verify(postRepository)
                .findByIdAndDeletedAtIsNull(postId);

        verify(postMapper)
                .toResponse(post);

        verify(postCacheService)
                .savePostResponse(response);
    }

    @Test
    void getPost_shouldThrowExceptionWhenPostNotFound() {
        when(postCacheService.getPostResponse(postId))
                .thenReturn(Optional.empty());

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.empty());

        assertThrows(
                PostNotFoundException.class,
                () -> postService.getPost(postId, currentUserId)
        );

        verify(postRepository)
                .findByIdAndDeletedAtIsNull(postId);

        verify(postCacheService, never())
                .savePostResponse(any());

        verify(postMapper, never())
                .toResponse(any());
    }

    @Test
    void getPost_shouldDenyAccessToPrivatePost() {
        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Private post")
                .visibility(Visibility.PRIVATE)
                .build();

        when(postCacheService.getPostResponse(postId))
                .thenReturn(Optional.empty());

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        assertThrows(
                AccessDeniedException.class,
                () -> postService.getPost(postId, currentUserId)
        );

        verify(postMapper, never())
                .toResponse(any());

        verify(postCacheService, never())
                .savePostResponse(any());
    }

    @Test
    void getPost_shouldAllowAuthorToAccessPrivatePost() {
        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Private post")
                .visibility(Visibility.PRIVATE)
                .build();

        PostResponse response = mock(PostResponse.class);

        when(postCacheService.getPostResponse(postId))
                .thenReturn(Optional.empty());

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        when(postMapper.toResponse(post))
                .thenReturn(response);

        PostResponse result =
                postService.getPost(postId, authorId);

        assertSame(response, result);

        verify(postCacheService)
                .savePostResponse(response);
    }

    @Test
    void getPost_shouldAllowFriendToAccessFriendsPost() {
        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Friends post")
                .visibility(Visibility.FRIENDS)
                .build();

        PostResponse response = mock(PostResponse.class);

        when(postCacheService.getPostResponse(postId))
                .thenReturn(Optional.empty());

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        when(friendshipClient.areFriends(authorId, currentUserId))
                .thenReturn(true);

        when(postMapper.toResponse(post))
                .thenReturn(response);

        PostResponse result =
                postService.getPost(postId, currentUserId);

        assertSame(response, result);

        verify(friendshipClient)
                .areFriends(authorId, currentUserId);
    }

    @Test
    void getPost_shouldDenyAccessWhenUsersAreNotFriends() {
        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Friends post")
                .visibility(Visibility.FRIENDS)
                .build();

        when(postCacheService.getPostResponse(postId))
                .thenReturn(Optional.empty());

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        when(friendshipClient.areFriends(authorId, currentUserId))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> postService.getPost(postId, currentUserId)
        );

        verify(friendshipClient)
                .areFriends(authorId, currentUserId);

        verify(postMapper, never())
                .toResponse(any());

        verify(postCacheService, never())
                .savePostResponse(any());
    }

    @Test
    void updatePost_shouldUpdatePostAndCacheAndPublishEvent() {
        UpdatePostRequest request = new UpdatePostRequest(
                "Updated content",
                Visibility.PUBLIC
        );

        Post post = Post.builder()
                .id(postId)
                .authorId(currentUserId)
                .content("Old content")
                .visibility(Visibility.PUBLIC)
                .build();

        PostResponse response = mock(PostResponse.class);
        PostUpdatedEvent event = mock(PostUpdatedEvent.class);

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        when(postRepository.save(post))
                .thenReturn(post);

        when(postMapper.toResponse(post))
                .thenReturn(response);

        when(postMapper.toUpdatedEvent(post))
                .thenReturn(event);

        PostResponse result =
                postService.updatePost(
                        postId,
                        currentUserId,
                        request
                );

        assertSame(response, result);

        assertEquals("Updated content", post.getContent());
        assertEquals(Visibility.PUBLIC, post.getVisibility());

        verify(postRepository).save(post);
        verify(postCacheService).savePostResponse(response);
        verify(kafkaProducer).sendPostUpdated(event);
    }

    @Test
    void updatePost_shouldDenyAccessForAnotherUser() {
        UpdatePostRequest request = new UpdatePostRequest(
                "Updated",
                Visibility.PUBLIC
        );

        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Original")
                .visibility(Visibility.PUBLIC)
                .build();

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        assertThrows(
                AccessDeniedException.class,
                () -> postService.updatePost(
                        postId,
                        currentUserId,
                        request
                )
        );

        verify(postRepository, never())
                .save(any());

        verify(postCacheService, never())
                .savePostResponse(any());

        verify(kafkaProducer, never())
                .sendPostUpdated(any());
    }

    @Test
    void deletePost_shouldSoftDeleteAndEvictCacheAndPublishEvent() {
        Post post = Post.builder()
                .id(postId)
                .authorId(currentUserId)
                .content("Hello")
                .visibility(Visibility.PUBLIC)
                .build();

        PostDeletedEvent event = mock(PostDeletedEvent.class);

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        when(postRepository.save(post))
                .thenReturn(post);

        when(postMapper.toDeletedEvent(post))
                .thenReturn(event);

        postService.deletePost(postId, currentUserId);

        verify(postRepository).save(post);

        verify(postCacheService)
                .deletePostResponse(postId);

        verify(postMapper)
                .toDeletedEvent(post);

        verify(kafkaProducer)
                .sendPostDeleted(event);
    }

    @Test
    void deletePost_shouldDenyAccessForAnotherUser() {
        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Hello")
                .visibility(Visibility.PUBLIC)
                .build();

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        assertThrows(
                AccessDeniedException.class,
                () -> postService.deletePost(
                        postId,
                        currentUserId
                )
        );

        verify(postRepository, never())
                .save(any());

        verify(postCacheService, never())
                .deletePostResponse(any());

        verify(kafkaProducer, never())
                .sendPostDeleted(any());
    }

    @Test
    void getPostsByAuthor_shouldReturnAllPostsForAuthor() {
        Pageable pageable = PageRequest.of(0, 10);

        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Hello")
                .visibility(Visibility.PRIVATE)
                .build();

        PostResponse response = mock(PostResponse.class);

        Page<Post> postPage =
                new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findByAuthorIdAndDeletedAtIsNull(
                authorId,
                pageable
        )).thenReturn(postPage);

        when(postMapper.toResponse(post))
                .thenReturn(response);

        Page<PostResponse> result =
                postService.getPostsByAuthor(
                        authorId,
                        pageable,
                        authorId
                );

        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().get(0));

        verify(postRepository)
                .findByAuthorIdAndDeletedAtIsNull(
                        authorId,
                        pageable
                );

        verify(postRepository, never())
                .findByAuthorIdAndVisibilityAndDeletedAtIsNull(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void getPostsByAuthor_shouldReturnOnlyPublicPostsForAnotherUser() {
        Pageable pageable = PageRequest.of(0, 10);

        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Public post")
                .visibility(Visibility.PUBLIC)
                .build();

        PostResponse response = mock(PostResponse.class);

        Page<Post> postPage =
                new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository
                .findByAuthorIdAndVisibilityAndDeletedAtIsNull(
                        authorId,
                        Visibility.PUBLIC,
                        pageable
                ))
                .thenReturn(postPage);

        when(postMapper.toResponse(post))
                .thenReturn(response);

        Page<PostResponse> result =
                postService.getPostsByAuthor(
                        authorId,
                        pageable,
                        currentUserId
                );

        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().get(0));

        verify(postRepository)
                .findByAuthorIdAndVisibilityAndDeletedAtIsNull(
                        authorId,
                        Visibility.PUBLIC,
                        pageable
                );

        verify(postRepository, never())
                .findByAuthorIdAndDeletedAtIsNull(
                        authorId,
                        pageable
                );
    }

    @Test
    void getPost_shouldPropagateFriendshipServiceUnavailableException() {
        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .content("Friends post")
                .visibility(Visibility.FRIENDS)
                .build();

        when(postCacheService.getPostResponse(postId))
                .thenReturn(Optional.empty());

        when(postRepository.findByIdAndDeletedAtIsNull(postId))
                .thenReturn(Optional.of(post));

        FriendshipServiceUnavailableException exception =
                new FriendshipServiceUnavailableException(
                        "Friendship service is unavailable",
                        null
                );

        when(friendshipClient.areFriends(authorId, currentUserId))
                .thenThrow(exception);

        FriendshipServiceUnavailableException thrown =
                assertThrows(
                        FriendshipServiceUnavailableException.class,
                        () -> postService.getPost(
                                postId,
                                currentUserId
                        )
                );

        assertSame(exception, thrown);
    }
}