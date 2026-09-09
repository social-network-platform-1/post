package com.example.post.cache;

import com.example.post.dto.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostCacheService {
    private final RedisTemplate<String, PostResponse> redisTemplate;
    private static final Duration POST_TTL = Duration.ofMinutes(10);

    public Optional<PostResponse> getPostResponse(UUID postId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(key(postId)));
    };

    public void savePostResponse(PostResponse postResponse) {
        redisTemplate.opsForValue().set(key(postResponse.id()), postResponse, POST_TTL);
    }

    public void deletePostResponse(UUID postId) {
        redisTemplate.delete(key(postId));
    }

    private String key(UUID postId) {
        return "post:" + postId.toString();
    }


}
