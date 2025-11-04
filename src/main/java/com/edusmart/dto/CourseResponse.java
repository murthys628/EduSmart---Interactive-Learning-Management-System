package com.edusmart.dto;

public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private String teacherName;
    private String studentName; // single student

    public CourseResponse(Long id, String title, String description, String teacherName, String studentName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.teacherName = teacherName;
        this.studentName = studentName;
    }

    // Getters & Setters
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

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}
}