package com.edusmart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private int score = 0;

    @Column(name = "total_marks", nullable = false)
    private int totalMarks = 0;

    @Column(nullable = false)
    private boolean completed = false;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ✅ Changed to Integer to avoid null assignment error
    @Column(name = "duration_minutes")
    private Integer durationMinutes = 0;

    @Column(name = "status", length = 20)
    private String status = "IN_PROGRESS";

    // ===== Constructors =====
    public QuizAttempt() {}

    public QuizAttempt(Quiz quiz, User student) {
        this.quiz = quiz;
        this.student = student;
        // ✅ Defensive null-check to avoid NPE
        this.durationMinutes = (quiz != null) ? quiz.getDurationMinutes() : 0;
        this.status = "IN_PROGRESS";
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDateTime getStartedAt() { return startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ===== Helper Methods =====
    public void completeAttempt(int score, int totalMarks) {
        this.score = score;
        this.totalMarks = totalMarks;
        this.completed = true;
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }

    public boolean isTimeExpired() {
        if (startedAt == null || durationMinutes == null || durationMinutes <= 0) return false;
        LocalDateTime end = startedAt.plusMinutes(durationMinutes);
        return LocalDateTime.now().isAfter(end);
    }
}