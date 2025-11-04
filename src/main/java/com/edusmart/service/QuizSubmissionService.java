package com.edusmart.service;

import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edusmart.dto.QuizSubmissionRequestDTO;
import com.edusmart.dto.QuizSubmissionResponseDTO;
import com.edusmart.entity.Question;
import com.edusmart.entity.Quiz;
import com.edusmart.entity.QuizAttempt;
import com.edusmart.entity.QuizSubmission;
import com.edusmart.entity.User;
import com.edusmart.mapper.QuizSubmissionMapper;
import com.edusmart.repository.EnrollmentRepository;
import com.edusmart.repository.QuestionRepository;
import com.edusmart.repository.QuizAttemptRepository;
import com.edusmart.repository.QuizRepository;
import com.edusmart.repository.QuizSubmissionRepository;
import com.edusmart.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Transactional
@CacheConfig(cacheNames = {"quizSubmissionCache"})
public class QuizSubmissionService {

    private final QuizSubmissionRepository submissionRepo;
    private final QuizRepository quizRepo;
    private final UserRepository userRepo;
    private final QuestionRepository questionRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final QuizAttemptRepository quizAttemptRepository;

    public QuizSubmissionService(QuizSubmissionRepository submissionRepo,
                                 QuizRepository quizRepo,
                                 UserRepository userRepo,
                                 QuestionRepository questionRepo,
                                 EnrollmentRepository enrollmentRepo,
                                 QuizAttemptRepository quizAttemptRepository) {
        this.submissionRepo = submissionRepo;
        this.quizRepo = quizRepo;
        this.userRepo = userRepo;
        this.questionRepo = questionRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    // ---------- Submit quiz ----------
    @Transactional
    @CacheEvict(value = "quizSubmissionCache", allEntries = true) // clear stale cache after new submission
    public QuizSubmissionResponseDTO submitQuiz(QuizSubmissionRequestDTO dto) {

        Quiz quiz = quizRepo.findById(dto.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        User student = userRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Find latest submission
        List<QuizSubmission> existingSubs = submissionRepo.findByQuizIdAndStudentId(quiz.getId(), student.getId());
        QuizSubmission submission = existingSubs.stream()
                .max(Comparator.comparing(QuizSubmission::getSubmittedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseGet(() -> {
                    QuizSubmission newSub = new QuizSubmission();
                    newSub.setQuiz(quiz);
                    newSub.setStudent(student);
                    newSub.setStatus("in-progress");
                    newSub.setScore(0);
                    newSub.setTotalMarks(0);
                    newSub.setStartedAt(LocalDateTime.now());
                    return newSub;
                });

        // Evaluate answers
        List<QuizSubmission.QuizQuestionSubmission> questionSubs = new ArrayList<>();
        int totalScore = 0;
        int totalMarks = 0;

        for (QuizSubmissionRequestDTO.QuestionSubmissionDTO qDto : dto.getQuestionSubmissions()) {
            Question question = questionRepo.findById(qDto.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found: " + qDto.getQuestionId()));

            int marks = question.getMarks();
            totalMarks += marks;
            int score = 0;

            if (qDto.getAnswer() != null && !qDto.getAnswer().isEmpty()) {
                char selected = qDto.getAnswer().trim().toUpperCase().charAt(0);
                char correct = Character.toUpperCase(question.getCorrectOption());
                if (selected == correct) {
                    score = marks;
                }
            }

            totalScore += score;
            questionSubs.add(new QuizSubmission.QuizQuestionSubmission(
                    question.getId(), qDto.getAnswer(), score, marks
            ));
        }

        submission.setQuestionSubmissions(questionSubs);
        submission.setScore(totalScore);
        submission.setTotalMarks(totalMarks);
        submission.setCompleted(true);
        submission.setCompletedAt(LocalDateTime.now());
        submission.setStatus("completed");

        QuizSubmission savedSubmission = submissionRepo.save(submission);

        // Record in quiz attempts table
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setScore(totalScore);
        attempt.setTotalMarks(totalMarks);
        attempt.setCompleted(true);
        attempt.setCompletedAt(LocalDateTime.now());

        quizAttemptRepository.save(attempt);

        return QuizSubmissionMapper.toDTO(savedSubmission);
    }

    // ---------- Get submission by ID ----------
    @Cacheable(value = "quizSubmissionCache", key = "'submission:' + #id")
    @Transactional(readOnly = true)
    public QuizSubmissionResponseDTO getSubmission(Long id) {
        return submissionRepo.findById(id)
                .map(QuizSubmissionMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    // ---------- Get all submissions ----------
    @Cacheable(value = "quizSubmissionCache", key = "'allSubmissions'")
    @Transactional(readOnly = true)
    public List<QuizSubmissionResponseDTO> getAllSubmissions() {
        return submissionRepo.findAll().stream()
                .map(QuizSubmissionMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ---------- Get submissions by student ----------
    @Cacheable(value = "quizSubmissionCache", key = "'studentSubs:' + #studentId")
    @Transactional(readOnly = true)
    public List<QuizSubmissionResponseDTO> getSubmissionsByStudent(Long studentId) {
        return submissionRepo.findByStudentId(studentId).stream()
                .map(QuizSubmissionMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ---------- Get submissions by quiz ----------
    @Cacheable(value = "quizSubmissionCache", key = "'quizSubs:' + #quizId")
    @Transactional(readOnly = true)
    public List<QuizSubmissionResponseDTO> getSubmissionsByQuiz(Long quizId) {
        return submissionRepo.findByQuizId(quizId).stream()
                .map(QuizSubmissionMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ---------- Delete submission ----------
    @CacheEvict(value = "quizSubmissionCache", allEntries = true)
    public void deleteSubmission(Long id) {
        QuizSubmission submission = submissionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        submissionRepo.delete(submission);
    }
}