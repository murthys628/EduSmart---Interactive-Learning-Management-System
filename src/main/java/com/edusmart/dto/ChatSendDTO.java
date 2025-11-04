package com.edusmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatSendDTO {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Content cannot be empty")
    private String content;
    
    // --- Getters and Setters ---
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}