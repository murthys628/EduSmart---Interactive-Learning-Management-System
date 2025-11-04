package com.edusmart.dto;

import com.edusmart.entity.Assignment;

public class AssignmentResponse {

    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private Long studentId;
    private String studentName;
    private String teacherName;
    private String status;
    private String grade;
    private String feedback;

    // ------------------- No-args constructor -------------------
    public AssignmentResponse() {}

    // ------------------- Full constructor -------------------
    public AssignmentResponse(Long id, String title, String description,
                              Long courseId, String courseName,
                              Long studentId, String studentName,
                              String teacherName, String status,
                              String grade, String feedback) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.courseId = courseId;
        this.courseName = courseName;
        this.studentId = studentId;
        this.studentName = studentName;
        this.teacherName = teacherName;
        this.status = status;
        this.grade = grade;
        this.feedback = feedback;
    }

    // ------------------- Getters & Setters -------------------
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

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
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

    // ------------------- Static Mapper from Assignment Entity -------------------
    public static AssignmentResponse fromEntity(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setCourseId(assignment.getCourse() != null ? assignment.getCourse().getId() : null);
        response.setCourseName(assignment.getCourse() != null ? assignment.getCourse().getTitle() : null);
        response.setStudentId(assignment.getStudent() != null ? assignment.getStudent().getId() : null);
        response.setStudentName(assignment.getStudent() != null ? assignment.getStudent().getName() : null);
        response.setTeacherName(assignment.getTeacher() != null ? assignment.getTeacher().getName() : null);
        response.setStatus(assignment.getStatus() != null ? assignment.getStatus().name() : null);
        response.setGrade(assignment.getGrade());
        response.setFeedback(assignment.getFeedback());
        return response;
    }
}