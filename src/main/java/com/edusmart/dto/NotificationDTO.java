package com.edusmart.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private Long recipientId;          // ✅ new: helps server identify user
    private String recipientUsername;  // ✅ new: used for convertAndSendToUser(username)
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationDTO() {}

    public NotificationDTO(Long id, Long recipientId, String recipientUsername,
                           String message, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.recipientUsername = recipientUsername;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}