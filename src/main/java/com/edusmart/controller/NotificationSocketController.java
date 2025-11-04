package com.edusmart.controller;

import com.edusmart.dto.NotificationDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 🔹 Send a real-time notification to a specific user.
     * This method can be reused from any service.
     */
    public void sendToUser(String username, NotificationDTO notificationDTO) {
        try {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    notificationDTO
            );
            System.out.println("📡 Sent WebSocket notification to user: " + username);
        } catch (Exception e) {
            System.err.println("⚠️ WebSocket push failed for user " + username + ": " + e.getMessage());
        }
    }
}