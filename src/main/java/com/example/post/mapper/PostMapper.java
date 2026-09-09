package com.example.post.mapper;

import com.example.post.domain.model.Post;
import com.example.post.dto.event.PostCreatedEvent;
import com.example.post.dto.event.PostDeletedEvent;
import com.example.post.dto.event.PostUpdatedEvent;
import com.example.post.dto.response.PostResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponse toResponse(Post post);

    PostCreatedEvent toCreatedEvent(Post post);

    PostUpdatedEvent toUpdatedEvent(Post post);

    PostDeletedEvent toDeletedEvent(Post post);
}
