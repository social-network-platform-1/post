package com.example.post.dto.response;

import com.example.post.domain.model.Visibility;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorId,
        String content,
        Visibility visibility,
        Instant createdAt,
        Instant updatedAt,
        int likesCount,
        int commentsCount
) {
}
