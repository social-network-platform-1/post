package com.example.post.client;

import com.example.post.exception.FriendshipServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FriendshipClient {

    private final RestClient friendshipRestClient;

    public boolean areFriends(
            UUID firstUserId,
            UUID secondUserId
    ) {
        try {
            Boolean result = friendshipRestClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/v1/friendships/check")
                            .queryParam("firstUserId", firstUserId)
                            .queryParam("secondUserId", secondUserId)
                            .build()
                    )
                    .retrieve()
                    .body(Boolean.class);

            return Boolean.TRUE.equals(result);

        } catch (RestClientException exception) {
            throw new FriendshipServiceUnavailableException(
                    "Friendship service is unavailable",
                    exception
            );
        }
    }
}
