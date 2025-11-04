package com.edusmart.service;

import com.edusmart.entity.ChatMessage;
import com.edusmart.repository.ChatMessageRepository;
import com.edusmart.dto.ChatSendDTO;
import com.edusmart.dto.ChatResponseDTO;
import com.edusmart.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatRepository;
    private final ChatMessageMapper chatMapper;
    private final UserService userService;

    public ChatService(ChatMessageRepository chatRepository, 
                       ChatMessageMapper chatMapper, 
                       UserService userService) {
        this.chatRepository = chatRepository;
        this.chatMapper = chatMapper;
        this.userService = userService;
    }

    /**
     * Saves a new chat message to the database. Used by the WebSocket controller.
     */
    @Transactional
    public ChatResponseDTO saveNewMessage(ChatSendDTO dto, Long senderId) {
        ChatMessage message = chatMapper.toEntity(dto, senderId);
        ChatMessage savedMessage = chatRepository.save(message);
        
        return enrichChatDto(savedMessage);
    }

    /**
     * Retrieves the message history for a course. Used by the REST controller.
     */
    @Transactional(readOnly = true)
    public List<ChatResponseDTO> getChatHistory(Long courseId) {
        // Fetches the last 50 messages, ordered descending (most recent first)
        List<ChatMessage> messages = chatRepository.findTop50ByCourseIdOrderByTimestampDesc(courseId);
        
        // Convert to DTOs and enrich
        List<ChatResponseDTO> dtos = messages.stream()
                .map(this::enrichChatDto)
                .collect(Collectors.toList());
        
        // IMPORTANT: Reverse the list so the oldest message is at index 0 (correct display order)
        Collections.reverse(dtos);
        return dtos;
    }

    /**
     * Helper method to fetch author name and complete the DTO.
     */
    private ChatResponseDTO enrichChatDto(ChatMessage message) {
        ChatResponseDTO dto = chatMapper.toDto(message);
        String senderName = userService.findUsernameById(message.getSenderId()); 
        dto.setSenderName(senderName);
        return dto;
    }
}