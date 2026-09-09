package com.example.post.dto.response;

import java.util.UUID;

public record PostOwnerResponse(
        UUID postId,
        UUID authorId
) {}
