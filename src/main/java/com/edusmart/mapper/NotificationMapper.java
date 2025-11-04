package com.edusmart.mapper;

import com.edusmart.dto.NotificationDTO;
import com.edusmart.entity.Notification;

public class NotificationMapper {
    public static NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setRecipientId(n.getRecipient() != null ? n.getRecipient().getId() : null);
        dto.setRecipientUsername(n.getRecipient() != null ? n.getRecipient().getUsername() : null);
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}