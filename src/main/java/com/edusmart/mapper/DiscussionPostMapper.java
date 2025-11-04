package com.edusmart.mapper;

import com.edusmart.dto.PostCreationDTO;
import com.edusmart.dto.PostResponseDTO;
import com.edusmart.entity.DiscussionPost;
import com.edusmart.entity.DiscussionThread;
import org.springframework.stereotype.Component;

@Component
public class DiscussionPostMapper {

    // =========================================================================
    // Converts PostCreationDTO → DiscussionPost Entity
    // =========================================================================
    public DiscussionPost toEntity(PostCreationDTO dto, Long authorId) {
        DiscussionPost post = new DiscussionPost();
        post.setContent(dto.getContent());
        post.setAuthorId(authorId);

        // ✅ Set parent thread reference properly
        DiscussionThread thread = new DiscussionThread();
        thread.setId(dto.getThreadId());
        post.setThread(thread);

        return post;
    }

    // =========================================================================
    // Converts DiscussionPost Entity → PostResponseDTO
    // =========================================================================
    public PostResponseDTO toDto(DiscussionPost entity) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setAuthorId(entity.getAuthorId());

        // ✅ Get threadId safely from related entity
        if (entity.getThread() != null) {
            dto.setThreadId(entity.getThread().getId());
        }

        dto.setCreatedAt(entity.getCreatedAt());
        // ⚠️ authorName will be set later in service (via UserService)
        return dto;
    }
}