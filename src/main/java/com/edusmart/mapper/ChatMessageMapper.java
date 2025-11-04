package com.edusmart.mapper;

import com.edusmart.entity.ChatMessage;
import com.edusmart.dto.ChatSendDTO;
import com.edusmart.dto.ChatResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    // Converts DTO to Entity for saving a new message
    public ChatMessage toEntity(ChatSendDTO dto, Long senderId) {
        ChatMessage message = new ChatMessage();
        message.setContent(dto.getContent());
        message.setCourseId(dto.getCourseId());
        message.setSenderId(senderId);
        // timestamp is set by the entity constructor (LocalDateTime.now())
        return message;
    }

    // Converts Entity to DTO for sending chat data to the client
    public ChatResponseDTO toDto(ChatMessage entity) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setCourseId(entity.getCourseId());
        dto.setSenderId(entity.getSenderId());
        dto.setTimestamp(entity.getTimestamp());
        // SenderName will be set in the Service layer
        return dto;
    }
}