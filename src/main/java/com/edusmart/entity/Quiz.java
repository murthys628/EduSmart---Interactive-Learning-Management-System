package com.edusmart.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ---------- Relations ----------
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizSubmission> submissions = new ArrayList<>();

    // ---------- Properties ----------
    @Column(name = "total_marks", columnDefinition = "INT DEFAULT 0")
    private int totalMarks = 0;

    @Column(name = "duration_minutes", columnDefinition = "INT DEFAULT 30")
    private int durationMinutes = 30;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "max_attempts", columnDefinition = "INT DEFAULT 3")
    private int maxAttempts = 3; // default value

    // ---------- Constructors ----------
    public Quiz() {}

    public Quiz(Long id, String title, String description, Course course, User teacher,
                int totalMarks, int durationMinutes, LocalDateTime startDate, LocalDateTime createdAt, int maxAttempts) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.course = course;
        this.teacher = teacher;
        this.totalMarks = totalMarks;
        this.durationMinutes = durationMinutes;
        this.startDate = startDate;
        this.createdAt = createdAt;
        this.maxAttempts = maxAttempts;
    }

    // ---------- Getters & Setters ----------
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

	public Course getCourse() {
		return course;
	}
	
    public void setCourse(Course course) {
        this.course = course;
        if (course != null) {
            course.setQuiz(this); // bidirectional
        }
    }

	public User getTeacher() {
		return teacher;
	}

	public void setTeacher(User teacher) {
		this.teacher = teacher;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public List<QuizSubmission> getSubmissions() {
		return submissions;
	}

	public void setSubmissions(List<QuizSubmission> submissions) {
		this.submissions = submissions;
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

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

    // ---------- Utility Methods ----------
    public void addSubmission(QuizSubmission submission) {
        if (submissions == null) submissions = new ArrayList<>();
        submissions.add(submission);
        submission.setQuiz(this);
    }

    public void addQuestion(Question question) {
        if (questions == null) questions = new ArrayList<>();
        questions.add(question);
        question.setQuiz(this);
    }

    // ---------- Builder ----------
    public static QuizBuilder builder() { return new QuizBuilder(); }

    public static class QuizBuilder {
        private Long id;
        private String title;
        private String description;
        private Course course;
        private User teacher;
        private int totalMarks = 0;
        private int durationMinutes = 30;
        private LocalDateTime startDate;
        private LocalDateTime createdAt;
        private int maxAttempts = 3;

		public QuizBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public QuizBuilder title(String title) {
			this.title = title;
			return this;
		}

		public QuizBuilder description(String description) {
			this.description = description;
			return this;
		}

		public QuizBuilder course(Course course) {
			this.course = course;
			return this;
		}

		public QuizBuilder teacher(User teacher) {
			this.teacher = teacher;
			return this;
		}

		public QuizBuilder totalMarks(int totalMarks) {
			this.totalMarks = totalMarks;
			return this;
		}

		public QuizBuilder durationMinutes(int durationMinutes) {
			this.durationMinutes = durationMinutes;
			return this;
		}

		public QuizBuilder startDate(LocalDateTime startDate) {
			this.startDate = startDate;
			return this;
		}

		public QuizBuilder createdAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public QuizBuilder maxAttempts(int maxAttempts) {
			this.maxAttempts = maxAttempts;
			return this;
		}

        public Quiz build() {
            return new Quiz(id, title, description, course, teacher, totalMarks, durationMinutes,
                    startDate, createdAt, maxAttempts);
        }
    }
}