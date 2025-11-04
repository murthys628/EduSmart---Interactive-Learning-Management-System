package com.edusmart.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for quiz submissions with detailed grading results.
 */
public class QuizSubmissionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long quizId;
    private Long studentId;
    private Integer score;
    private String status;
    private LocalDateTime submittedAt;
    private List<QuestionSubmissionDTO> questionSubmissions;

    // ---------- Getters & Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public List<QuestionSubmissionDTO> getQuestionSubmissions() { return questionSubmissions; }
    public void setQuestionSubmissions(List<QuestionSubmissionDTO> questionSubmissions) {
        this.questionSubmissions = questionSubmissions;
    }

    // ---------- Nested DTO ----------
    public static class QuestionSubmissionDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long questionId;
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

    @Override
    public String toString() {
        return "QuizSubmissionResponseDTO{" +
                "id=" + id +
                ", quizId=" + quizId +
                ", studentId=" + studentId +
                ", score=" + score +
                ", status='" + status + '\'' +
                ", submittedAt=" + submittedAt +
                ", questionSubmissions=" + questionSubmissions +
                '}';
    }
}