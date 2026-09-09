package com.example.post.dto.request;

import com.example.post.domain.model.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank
        @Size(min = 1, max = 1000)
        String content,
        @NotNull
        Visibility visibility) {
}
