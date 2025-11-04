package com.edusmart.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.edusmart.dto.ChatResponseDTO;
import com.edusmart.dto.ChatSendDTO;
import com.edusmart.entity.User;
import com.edusmart.service.ChatService;
import com.edusmart.service.UserService;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserService userService;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   ChatService chatService,
                                   UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.userService = userService;
    }

    /**
     * Handles incoming chat messages via WebSocket.
     * Client sends to /app/chat.sendMessage
     */
    @MessageMapping("/chat/send")
    public void sendMessage(@Payload ChatSendDTO chatMessage, Principal principal) {
        if (principal == null) {
            System.out.println("⚠️ Principal is null — cannot identify sender.");
            return;
        }

        User sender = userService.getUserByUsername(principal.getName());
        Long senderId = sender.getId();

        ChatResponseDTO savedMessage = chatService.saveNewMessage(chatMessage, senderId);

        String destination = "/topic/course/" + chatMessage.getCourseId() + "/chat";
        messagingTemplate.convertAndSend(destination, savedMessage);

        System.out.println("📤 Message sent by " + sender.getUsername() + " to " + destination);
    }
}