package com.example.post.dto.event;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public record PostDeletedEvent(
        @NotBlank
        UUID postId,
        @NotBlank
        UUID authorId,
        @NotBlank
        String content,
        @NotBlank
        Instant deleteAt
) {
}
