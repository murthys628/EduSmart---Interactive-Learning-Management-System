package com.edusmart.controller;

import com.edusmart.service.ChatService;
import com.edusmart.dto.ChatResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatHistoryController {

    private final ChatService chatService;

    public ChatHistoryController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * GET /api/v1/chat/history/{courseId}
     * Retrieves the last N messages for a course chat room.
     */
    @GetMapping("/history/{courseId}")
    public ResponseEntity<List<ChatResponseDTO>> getChatHistory(@PathVariable Long courseId) {
        List<ChatResponseDTO> history = chatService.getChatHistory(courseId);
        return ResponseEntity.ok(history);
    }
}