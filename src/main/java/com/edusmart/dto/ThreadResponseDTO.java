package com.edusmart.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ThreadResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private Long courseId;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int postCount;

    // --- Constructors ---
    public ThreadResponseDTO() {
        // Default constructor (needed for frameworks like Jackson)
    }

    public ThreadResponseDTO(Long id, String title, Long courseId, Long creatorId, String creatorName,
                             LocalDateTime createdAt, LocalDateTime updatedAt, int postCount) {
        this.id = id;
        this.title = title;
        this.courseId = courseId;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postCount = postCount;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    // --- toString() for Debugging ---
    @Override
    public String toString() {
        return "ThreadResponseDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", courseId=" + courseId +
                ", creatorId=" + creatorId +
                ", creatorName='" + creatorName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", postCount=" + postCount +
                '}';
    }
}