package com.edusmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO representing a full quiz submission with detailed answers.
 */
public class QuizSubmissionRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @Valid
    @NotEmpty(message = "Question submissions cannot be empty")
    private List<QuestionSubmissionDTO> questionSubmissions = new ArrayList<>();

    // ---------- Getters & Setters ----------
    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public List<QuestionSubmissionDTO> getQuestionSubmissions() { return questionSubmissions; }
    public void setQuestionSubmissions(List<QuestionSubmissionDTO> questionSubmissions) {
        this.questionSubmissions = questionSubmissions;
    }

    // ---------- Nested DTO ----------
    public static class QuestionSubmissionDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Question ID is required")
        private Long questionId;

        @NotNull(message = "Answer cannot be null")
        private String answer;

        private Integer score;
        private Integer totalMarks;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public Integer getTotalMarks() { return totalMarks; }
        public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

        @Override
        public String toString() {
            return "QuestionSubmissionDTO{" +
                    "questionId=" + questionId +
                    ", answer='" + answer + '\'' +
                    ", score=" + score +
                    ", totalMarks=" + totalMarks +
                    '}';
        }
    }

    // ---------- Helper ----------
    public void addQuestion(Long questionId, String answer) {
        QuestionSubmissionDTO q = new QuestionSubmissionDTO();
        q.setQuestionId(questionId);
        q.setAnswer(answer);
        this.questionSubmissions.add(q);
    }

    @Override
    public String toString() {
        return "QuizSubmissionRequestDTO{" +
                "quizId=" + quizId +
                ", studentId=" + studentId +
                ", questionSubmissions=" + questionSubmissions +
                '}';
    }
}