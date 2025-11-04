package com.edusmart.service;

import com.edusmart.controller.NotificationSocketController;
import com.edusmart.dto.NotificationDTO;
import com.edusmart.entity.Notification;
import com.edusmart.entity.User;
import com.edusmart.mapper.NotificationMapper;
import com.edusmart.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSocketController socketController;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               NotificationSocketController socketController) {
        this.notificationRepository = notificationRepository;
        this.socketController = socketController;
    }

    /**
     * ✅ Create and save a new notification, then push via WebSocket.
     */
    @Transactional
    @CacheEvict(value = {"notifications", "notificationUnreadCount"}, key = "#recipient.id")
    public void sendNotification(User recipient, String message) {
        Notification notification = new Notification(recipient, message);
        Notification saved = notificationRepository.save(notification);

        // Convert entity to DTO
        NotificationDTO dto = NotificationMapper.toDTO(saved);

        // Send in real-time via WebSocket
        socketController.sendToUser(recipient.getUsername(), dto);
    }

    @Cacheable(value = "notifications", key = "#user.id")
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    @Cacheable(value = "notificationUnreadCount", key = "#user.id")
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    @Transactional
    @CacheEvict(value = {"notifications", "notificationUnreadCount"}, allEntries = true)
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}