package com.edusmart.dto;

import java.time.LocalDateTime;

public class QuizAnswerDTO {
    private Long id;
    private Long attemptId;
    private Long questionId;
    private char selectedOption;
    private boolean isCorrect;
    private LocalDateTime answeredAt;

    // Getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAttemptId() {
		return attemptId;
	}

	public void setAttemptId(Long attemptId) {
		this.attemptId = attemptId;
	}

	public Long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public char getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedOption(char selectedOption) {
		this.selectedOption = selectedOption;
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public void setCorrect(boolean correct) {
		isCorrect = correct;
	}

	public LocalDateTime getAnsweredAt() {
		return answeredAt;
	}

	public void setAnsweredAt(LocalDateTime answeredAt) {
		this.answeredAt = answeredAt;
	}
}