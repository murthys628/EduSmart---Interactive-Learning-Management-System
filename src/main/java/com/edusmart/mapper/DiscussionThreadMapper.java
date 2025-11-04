package com.edusmart.mapper;

import org.springframework.stereotype.Component;

import com.edusmart.dto.ThreadCreationDTO;
import com.edusmart.dto.ThreadResponseDTO;
import com.edusmart.entity.DiscussionThread;
import com.edusmart.service.UserService;

@Component
public class DiscussionThreadMapper {

    private final UserService userService;

    public DiscussionThreadMapper(UserService userService) {
        this.userService = userService;
    }

    /**
     * Converts a ThreadCreationDTO into a DiscussionThread entity.
     */
    public DiscussionThread toEntity(ThreadCreationDTO dto, Long creatorId) {
        DiscussionThread thread = new DiscussionThread();
        thread.setTitle(dto.getTitle());
        thread.setCourseId(dto.getCourseId()); // <-- This line correctly maps the courseId
        thread.setCreatorId(creatorId);
        return thread;
    }

    /**
     * Converts a DiscussionThread entity into a ThreadResponseDTO for the client.
     */
    public ThreadResponseDTO toDto(DiscussionThread entity) {
        ThreadResponseDTO dto = new ThreadResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setCourseId(entity.getCourseId());
        dto.setCreatorId(entity.getCreatorId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Fetch creator name safely
        try {
            String creatorName = userService.getUserNameById(entity.getCreatorId());
            dto.setCreatorName(creatorName != null ? creatorName : "Unknown User");
        } catch (Exception e) {
            // Avoid breaking the mapper if user lookup fails
            dto.setCreatorName("Unknown User");
            System.err.println("⚠️ Failed to fetch creator name for ID " + entity.getCreatorId() + ": " + e.getMessage());
        }

        return dto;
    }
}