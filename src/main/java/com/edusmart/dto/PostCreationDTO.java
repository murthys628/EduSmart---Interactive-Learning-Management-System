package com.edusmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PostCreationDTO {
    
    @NotNull(message = "Thread ID is required")
    private Long threadId;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    // --- Getters and Setters ---
    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}