package com.edusmart.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "enrollments") // optional, defaults to class name
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long studentId;

    private long quizId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date enrolledAt;

    private boolean completed;

    private Double score;

    // Constructors
    public Enrollment() {}

    public Enrollment(long studentId, long quizId) {
        this.studentId = studentId;
        this.quizId = quizId;
        this.completed = false;
        this.score = 0.0;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getStudentId() {
        return studentId;
    }
    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public long getQuizId() {
        return quizId;
    }
    public void setQuizId(long quizId) {
        this.quizId = quizId;
    }

    public Date getEnrolledAt() {
        return enrolledAt;
    }
    public void setEnrolledAt(Date enrolledAt) {
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
}