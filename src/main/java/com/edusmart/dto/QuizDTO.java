package com.edusmart.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.edusmart.entity.Course;
import com.edusmart.entity.Quiz;
import com.edusmart.entity.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object (DTO) for Quiz entity.
 * Used for transferring quiz data between layers (Controller, Service, UI, API).
 */
public class QuizDTO {

    private Long id;

    @NotBlank(message = "Quiz title is required")
    private String title;

    private String description;

    /** The name of the course this quiz belongs to (for display only). */
    private String courseTitle;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private Long teacherId;

    private int totalMarks;

    private int durationMinutes;

    private LocalDateTime startDate;

    private LocalDateTime createdAt;

    private List<QuestionDTO> questions;

    /** Indicates if the quiz attempt has been completed */
    private boolean completed;

    // ---------------- Constructors ----------------

    public QuizDTO() {}

    public QuizDTO(Long id, String title, String description, String courseTitle,
                   Long courseId, Long teacherId, int totalMarks, int durationMinutes,
                   LocalDateTime startDate, LocalDateTime createdAt, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.courseTitle = courseTitle;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.totalMarks = totalMarks;
        this.durationMinutes = durationMinutes;
        this.startDate = startDate;
        this.createdAt = createdAt;
        this.completed = completed;
    }

    // ---------------- Getters and Setters ----------------

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

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // ---------------- Utility ----------------

    @Override
    public String toString() {
        return "QuizDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", courseTitle='" + courseTitle + '\'' +
                ", courseId=" + courseId +
                ", teacherId=" + teacherId +
                ", totalMarks=" + totalMarks +
                ", durationMinutes=" + durationMinutes +
                ", startDate=" + startDate +
                ", createdAt=" + createdAt +
                ", completed=" + completed +
                '}';
    }

    // ---------------- Convert DTO to Entity ----------------
    public Quiz toEntity(User teacher, Course course) {
        Quiz quiz = new Quiz();
        quiz.setId(this.id); // null if new, set for update
        quiz.setTitle(this.title);
        quiz.setDescription(this.description);
        quiz.setCourse(course);
        quiz.setTeacher(teacher);
        quiz.setTotalMarks(this.totalMarks);
        quiz.setDurationMinutes(this.durationMinutes);
        quiz.setStartDate(this.startDate);
        quiz.setCreatedAt(this.createdAt != null ? this.createdAt : LocalDateTime.now());
        return quiz;
    }

    // ---------------- Map Entity to DTO ----------------
    public static QuizDTO mapToDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());

        if (quiz.getCourse() != null) {
            dto.setCourseTitle(quiz.getCourse().getTitle());
            dto.setCourseId(quiz.getCourse().getId());
        }

        if (quiz.getTeacher() != null) {
            dto.setTeacherId(quiz.getTeacher().getId());
        }

        dto.setTotalMarks(quiz.getTotalMarks());
        dto.setDurationMinutes(quiz.getDurationMinutes());
        dto.setStartDate(quiz.getStartDate());
        dto.setCreatedAt(quiz.getCreatedAt());

        // Default completed false; actual value can be set from QuizAttemptDTO
        dto.setCompleted(false);

        return dto;
    }
}