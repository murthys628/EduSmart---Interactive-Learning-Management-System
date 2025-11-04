package com.edusmart.dto;

import java.time.LocalDateTime;

public class DiscussionThreadDTO {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private Long createdById;
    private String createdByName;   // resolved from UserService
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    private int postCount;          // number of posts in this thread

    // ---------------- Constructors ----------------
    public DiscussionThreadDTO() {
    }

    public DiscussionThreadDTO(Long id, String title, String description,
                               Long courseId, Long createdById, String createdByName,
                               LocalDateTime createdAt, LocalDateTime lastActivityAt, int postCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.courseId = courseId;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.lastActivityAt = lastActivityAt;
        this.postCount = postCount;
    }

    // ---------------- Getters & Setters ----------------
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }
}