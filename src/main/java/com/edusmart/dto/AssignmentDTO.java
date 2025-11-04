package com.edusmart.dto;

public class AssignmentDTO {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private Long studentId;
    private Long teacherId;
    private String status;
    private String grade;
    private String feedback;

    public AssignmentDTO() {}

    public AssignmentDTO(Long id, String title, String description, Long courseId, Long studentId,
                         Long teacherId, String status, String grade, String feedback) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.courseId = courseId;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.status = status;
        this.grade = grade;
        this.feedback = feedback;
    }

    // Getters and Setters
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

	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public Long getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Long teacherId) {
		this.teacherId = teacherId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
}