package com.example.post.kafka;

import com.example.post.dto.event.PostCreatedEvent;
import com.example.post.dto.event.PostDeletedEvent;
import com.example.post.dto.event.PostUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPostCreated(PostCreatedEvent postCreatedEvent) {
        kafkaTemplate.send(
                "postCreated",
                postCreatedEvent.postId().toString(),
                postCreatedEvent);
    }

    public void sendPostUpdated(PostUpdatedEvent postUpdatedEvent) {
        kafkaTemplate.send(
                "postUpdated",
                postUpdatedEvent.postId().toString(),
                postUpdatedEvent);
    }

    public void sendPostDeleted(PostDeletedEvent postDeletedEvent) {
        kafkaTemplate.send(
                "postDeleted",
                postDeletedEvent.postId().toString(),
                postDeletedEvent);
    }
}
