package com.edusmart.dto;

import java.time.LocalDateTime;

public class EnrollmentDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long quizId;
    private LocalDateTime enrolledAt;
    private boolean completed;
    private Double score;
    private String quizTitle;
    private String scorePercentageString;

    // Constructors
    public EnrollmentDTO() {}

    public EnrollmentDTO(Long id, Long studentId, String studentName, Long quizId, LocalDateTime enrolledAt, boolean completed, Double score) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.quizId = quizId;
        this.enrolledAt = enrolledAt;
        this.completed = completed;
        this.score = score;
    }

    public EnrollmentDTO(Long id, Long studentId, String studentName, Long quizId, String quizTitle, LocalDateTime enrolledAt, boolean completed, Double score, String scorePercentageString) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.quizId = quizId;
        this.enrolledAt = enrolledAt;
        this.completed = completed;
        this.score = score;
        this.quizTitle = quizTitle;
        this.scorePercentageString = scorePercentageString;
    }

    // Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getQuizId() {
		return quizId;
	}

	public void setQuizId(Long quizId) {
		this.quizId = quizId;
	}

	public LocalDateTime getEnrolledAt() {
		return enrolledAt;
	}

	public void setEnrolledAt(LocalDateTime enrolledAt) {
		this.enrolledAt = enrolledAt;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getQuizTitle() {
		return quizTitle;
	}

	public void setQuizTitle(String quizTitle) {
		this.quizTitle = quizTitle;
	}
	
	// 💡 NEW Getter and Setter for the formatted score
	public String getScorePercentageString() {
	    if (score == null) {
	        return "0.00%";
	    }
	    double percentage = score;
	    if (score <= 1) {
	        percentage = score * 100;
	    }
	    return String.format("%.2f%%", percentage);
	}

    public void setScorePercentageString(String scorePercentageString) {
        this.scorePercentageString = scorePercentageString;
    }
}