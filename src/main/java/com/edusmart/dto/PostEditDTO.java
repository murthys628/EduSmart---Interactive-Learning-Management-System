package com.edusmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PostEditDTO {

    @NotNull(message = "Post ID is required")
    private Long id;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    @NotNull(message = "Thread ID is required")
    private Long threadId;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }
}