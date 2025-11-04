package com.edusmart.dto;

import java.time.LocalDateTime;

public class ChatMessageDTO {

    private Long id;
    private Long courseId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;

    // 🧱 Default Constructor
    public ChatMessageDTO() {
    }

    // 🧩 Parameterized Constructor
    public ChatMessageDTO(Long id, Long courseId, Long senderId, String senderName, String content, LocalDateTime timestamp) {
        this.id = id;
        this.courseId = courseId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }

    // 🟩 Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // 🧾 Optional: toString() for debugging/logging
    @Override
    public String toString() {
        return "ChatMessageDTO{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", senderId=" + senderId +
                ", senderName='" + senderName + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}