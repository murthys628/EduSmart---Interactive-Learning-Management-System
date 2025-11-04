package com.edusmart.controller;

import com.edusmart.dto.NotificationDTO;
import com.edusmart.entity.User;
import com.edusmart.mapper.NotificationMapper;
import com.edusmart.service.NotificationService;
import com.edusmart.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationRestController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // 🔹 Get all notifications for logged-in user
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(Principal principal) {
        // ✅ Safely fetch user by username
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));

        // ✅ Map entity list to DTO list
        List<NotificationDTO> notifications = notificationService.getUserNotifications(user)
                .stream()
                .map(NotificationMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(notifications);
    }

    // 🔹 Get unread count
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Principal principal) {
        // ✅ Same fix applied here
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));

        long unreadCount = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(unreadCount);
    }

    // 🔹 Mark as read
    @PostMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    // 🔹 (Optional) Send manual test notification via Postman
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestParam Long userId, @RequestParam String message) {
    	User user = userService.getUserEntityById(userId);
        notificationService.sendNotification(user, message);
        return ResponseEntity.ok("Notification sent");
    }
}