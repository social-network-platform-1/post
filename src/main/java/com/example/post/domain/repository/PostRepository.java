package com.example.post.domain.repository;

import com.example.post.domain.model.Post;
import com.example.post.domain.model.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    Optional<Post> findByIdAndDeletedAtIsNull(UUID postId);
    Page<Post> findByAuthorIdAndDeletedAtIsNull(UUID authorId, Pageable pageable);
    Page<Post> findByAuthorIdAndVisibilityAndDeletedAtIsNull(
            UUID authorId,
            Visibility visibility,
            Pageable pageable
    );

}
