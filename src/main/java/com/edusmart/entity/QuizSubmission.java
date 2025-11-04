package com.edusmart.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_submission")
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------- Relations ----------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // ---------- Properties ----------
    @Column
    private Integer score;

    @Column
    private Integer totalMarks; // 🆕 Added for total possible marks

    @Column(length = 50)
    private String status; // e.g., "completed", "in-progress"

    @Column(nullable = false)
    private Boolean completed = false; // 🆕 Added to track completion state

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime startedAt; // 🆕 Added for quiz start time

    @Column
    private LocalDateTime completedAt; // 🆕 Added for quiz completion time

    // ---------- Embedded per-question submissions ----------
    @ElementCollection
    @CollectionTable(
        name = "quiz_submission_questions",
        joinColumns = @JoinColumn(name = "quiz_submission_id")
    )
    private List<QuizQuestionSubmission> questionSubmissions = new ArrayList<>();

    // ---------- Constructors ----------
    public QuizSubmission() {}

    public QuizSubmission(Quiz quiz, User student, Integer score, String status) {
        this.quiz = quiz;
        this.student = student;
        this.score = score;
        this.status = status;
    }

    // ---------- Getters & Setters ----------
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Quiz getQuiz() {
		return quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	public User getStudent() {
		return student;
	}

	public void setStudent(User student) {
		this.student = student;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Integer getTotalMarks() {
		return totalMarks;
	}

	public void setTotalMarks(Integer totalMarks) {
		this.totalMarks = totalMarks;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

    public List<QuizQuestionSubmission> getQuestionSubmissions() { return questionSubmissions; }
    public void setQuestionSubmissions(List<QuizQuestionSubmission> questionSubmissions) {
        this.questionSubmissions = questionSubmissions;
    }

    // ---------- Utility Methods ----------
    public void addQuestionSubmission(QuizQuestionSubmission questionSubmission) {
        if (questionSubmissions == null) questionSubmissions = new ArrayList<>();
        questionSubmissions.add(questionSubmission);
    }

    // ---------- Builder ----------
    public static QuizSubmissionBuilder builder() { return new QuizSubmissionBuilder(); }

    public static class QuizSubmissionBuilder {
        private Quiz quiz;
        private User student;
        private Integer score;
        private String status;
        private List<QuizQuestionSubmission> questionSubmissions = new ArrayList<>();

		public QuizSubmissionBuilder quiz(Quiz quiz) {
			this.quiz = quiz;
			return this;
		}

		public QuizSubmissionBuilder student(User student) {
			this.student = student;
			return this;
		}

		public QuizSubmissionBuilder score(Integer score) {
			this.score = score;
			return this;
		}

		public QuizSubmissionBuilder status(String status) {
			this.status = status;
			return this;
		}

		public QuizSubmissionBuilder questionSubmissions(List<QuizQuestionSubmission> questionSubmissions) {
			this.questionSubmissions = questionSubmissions;
			return this;
		}

        public QuizSubmission build() {
            QuizSubmission submission = new QuizSubmission(quiz, student, score, status);
            submission.setQuestionSubmissions(questionSubmissions);
            return submission;
        }
    }

    // ---------- Embedded class for per-question submissions ----------
    @Embeddable
    public static class QuizQuestionSubmission {
        private Long questionId;
        private String answer;
        private Integer score;
        private Integer totalMarks;

        public QuizQuestionSubmission() {}

        public QuizQuestionSubmission(Long questionId, String answer, Integer score, Integer totalMarks) {
            this.questionId = questionId;
            this.answer = answer;
            this.score = score;
            this.totalMarks = totalMarks;
        }

		public Long getQuestionId() {
			return questionId;
		}

		public void setQuestionId(Long questionId) {
			this.questionId = questionId;
		}

		public String getAnswer() {
			return answer;
		}

		public void setAnswer(String answer) {
			this.answer = answer;
		}

		public Integer getScore() {
			return score;
		}

		public void setScore(Integer score) {
			this.score = score;
		}

		public Integer getTotalMarks() {
			return totalMarks;
		}

		public void setTotalMarks(Integer totalMarks) {
			this.totalMarks = totalMarks;
		}
    }
}