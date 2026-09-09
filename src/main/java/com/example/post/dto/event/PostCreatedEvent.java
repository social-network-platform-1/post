package com.example.post.dto.event;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public record PostCreatedEvent(
        UUID postId,
        UUID authorId,
        @NotBlank
        String content,
        Instant createAt
) {
}
