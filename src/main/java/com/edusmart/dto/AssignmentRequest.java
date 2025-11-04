package com.edusmart.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AssignmentRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long courseId; // Assign to a course
    
    @NotNull
    @Size(min = 1, message = "At least one student must be selected")
    private List<String> studentUsernames;

    // ===== Getters & Setters =====
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
	
	public List<String> getStudentUsernames() {
        return studentUsernames;
    }
    public void setStudentUsernames(List<String> studentUsernames) {
        this.studentUsernames = studentUsernames;
    }
}