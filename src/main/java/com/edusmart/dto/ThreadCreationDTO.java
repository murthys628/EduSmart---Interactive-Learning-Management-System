package com.edusmart.dto;

import java.io.Serializable;

public class ThreadCreationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private Long courseId;
    private Long creatorId;
    
    // <--- FIX: ADDED DESCRIPTION FIELD --->
    private String description;

    // --- Constructors ---
    public ThreadCreationDTO() {
        // Default constructor required for JSON deserialization
    }

    public ThreadCreationDTO(String title, Long courseId, Long creatorId, String description) {
        this.title = title;
        this.courseId = courseId;
        this.creatorId = creatorId;
        this.description = description; // Initialize new field
    }

    // --- Getters and Setters ---
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

    // <--- FIX: ADDED GETTER AND SETTER FOR DESCRIPTION --->
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // --- toString() for Debugging ---
    @Override
    public String toString() {
        return "ThreadCreationDTO{" +
                "title='" + title + '\'' +
                ", courseId=" + courseId +
                ", creatorId=" + creatorId +
                ", description='" + description + '\'' + // Updated toString
                '}';
    }
}