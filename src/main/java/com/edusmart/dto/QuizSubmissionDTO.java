package com.edusmart.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Simple submission DTO for quizzes — used for lightweight quiz answer submissions.
 */
public class QuizSubmissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotEmpty(message = "Question IDs cannot be empty")
    private List<Long> questionIds;

    @NotEmpty(message = "Selected options cannot be empty")
    private List<Character> selectedOptions;

    public QuizSubmissionDTO() {}

    public QuizSubmissionDTO(Long studentId, Long quizId,
                             List<Long> questionIds, List<Character> selectedOptions) {
        this.studentId = studentId;
        this.quizId = quizId;
        this.questionIds = questionIds;
        this.selectedOptions = selectedOptions;
    }

    // Getters & Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public List<Long> getQuestionIds() { return questionIds; }
    public void setQuestionIds(List<Long> questionIds) { this.questionIds = questionIds; }

    public List<Character> getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(List<Character> selectedOptions) { this.selectedOptions = selectedOptions; }

    @Override
    public String toString() {
        return "QuizSubmissionDTO{" +
                "studentId=" + studentId +
                ", quizId=" + quizId +
                ", questionIds=" + questionIds +
                ", selectedOptions=" + selectedOptions +
                '}';
    }
}