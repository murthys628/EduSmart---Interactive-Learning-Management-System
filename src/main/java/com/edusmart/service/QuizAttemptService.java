package com.edusmart.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edusmart.dto.QuizAttemptDTO;
import com.edusmart.dto.QuizDTO;
import com.edusmart.dto.QuizStatsDTO;
import com.edusmart.entity.Quiz;
import com.edusmart.entity.QuizAttempt;
import com.edusmart.entity.User;
import com.edusmart.mapper.QuizAttemptMapper;
import com.edusmart.repository.EnrollmentRepository;
import com.edusmart.repository.QuizAttemptRepository;
import com.edusmart.repository.QuizRepository;
import com.edusmart.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizAttemptMapper mapper;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository,
                              QuizRepository quizRepository,
                              UserRepository userRepository,
                              EnrollmentRepository enrollmentRepository,
                              QuizAttemptMapper mapper) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.mapper = mapper;
    }

    // ============================================================
    // 🔹 START OR RESUME ATTEMPT
    // ============================================================
    @Caching(evict = {
            @CacheEvict(value = "attemptsByStudent", key = "#studentId"),
            @CacheEvict(value = "completedAttemptsByStudent", key = "#studentId"),
            @CacheEvict(value = "latestAttemptByStudent", key = "#studentId"),
            @CacheEvict(value = "quizStats", key = "#quizId"),
            @CacheEvict(value = "topScorers", allEntries = true)
    })
    public QuizAttemptDTO startOrResumeAttempt(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // 🔸 Check for existing active (not completed) attempt
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizIdAndStudentId(quizId, studentId);
        for (QuizAttempt a : attempts) {
            if (!a.isCompleted()) {
                System.out.println("⏳ Resuming existing attempt ID: " + a.getId());
                return mapper.toDTO(a);
            }
        }

        // 🔸 Check attempt limits
        int maxAttempts = quiz.getMaxAttempts();
        long completedCount = attempts.stream().filter(QuizAttempt::isCompleted).count();

        if (completedCount >= maxAttempts) {
            throw new RuntimeException("You have reached the maximum number of attempts for this quiz.");
        }

        // 🔸 Create new attempt
        QuizAttempt newAttempt = new QuizAttempt();
        newAttempt.setQuiz(quiz);
        newAttempt.setStudent(student);
        quizAttemptRepository.save(newAttempt);

        System.out.println("🆕 Started new attempt ID: " + newAttempt.getId());
        return mapper.toDTO(newAttempt);
    }

    // ============================================================
    // 🔹 COMPLETE ATTEMPT
    // ============================================================
    @Caching(evict = {
            @CacheEvict(value = "attemptsByStudent", key = "#attempt.student.id", condition = "#attempt != null"),
            @CacheEvict(value = "completedAttemptsByStudent", key = "#attempt.student.id", condition = "#attempt != null"),
            @CacheEvict(value = "latestAttemptByStudent", key = "#attempt.student.id", condition = "#attempt != null"),
            @CacheEvict(value = "quizStats", allEntries = true),
            @CacheEvict(value = "topScorers", allEntries = true)
    })
    public QuizAttemptDTO completeAttempt(Long attemptId, int score, int totalMarks) {
        QuizAttempt attempt = quizAttemptRepository.findByIdWithDetails(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found or details missing."));

        attempt.completeAttempt(score, totalMarks);
        quizAttemptRepository.save(attempt);

        // 🔸 Update enrollment progress
        try {
            Long studentId = attempt.getStudent().getId();
            Long quizId = attempt.getQuiz().getId();
            double percentageScore = (totalMarks > 0) ? (double) score * 100 / totalMarks : 0.0;

            enrollmentRepository.updateEnrollmentCompletionStatus(studentId, quizId, percentageScore);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to update enrollment progress: " + e.getMessage());
        }

        return mapper.toDTO(attempt);
    }

    // ============================================================
    // 🔹 TIMER LOGIC SUPPORT
    // ============================================================
    @Transactional(readOnly = true)
    public long getRemainingTimeSeconds(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        int durationMinutes = attempt.getQuiz().getDurationMinutes();
        if (durationMinutes <= 0) return Long.MAX_VALUE;

        long elapsedSeconds = Duration.between(attempt.getStartedAt(), LocalDateTime.now()).toSeconds();
        long totalSeconds = durationMinutes * 60L;
        return Math.max(totalSeconds - elapsedSeconds, 0);
    }

    @Transactional
    public boolean expireIfTimeOver(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.isCompleted()) return false;

        long remaining = getRemainingTimeSeconds(attemptId);
        if (remaining <= 0) {
            attempt.completeAttempt(attempt.getScore(), attempt.getTotalMarks());
            quizAttemptRepository.save(attempt);
            System.out.println("⏰ Attempt auto-completed due to time expiration.");
            return true;
        }
        return false;
    }

    // ============================================================
    // 🔹 FETCH METHODS (CACHED)
    // ============================================================
    @Cacheable(value = "attemptsByStudent", key = "#studentId")
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO> getAttemptsByStudent(Long studentId) {
        return quizAttemptRepository.findByStudentId(studentId)
                .stream().map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "completedAttemptsByStudent", key = "#studentId")
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO> getCompletedAttemptsByStudent(Long studentId) {
        return quizAttemptRepository.findByStudentIdAndCompletedTrue(studentId)
                .stream().map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "attemptsByQuiz", key = "#quizId")
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO> getAttemptsByQuiz(Long quizId) {
        return quizAttemptRepository.findByQuizId(quizId)
                .stream().map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "topScorers", key = "#quizId + '-' + #limit")
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO> getTopScorers(Long quizId, int limit) {
        return quizAttemptRepository.findTopByQuizIdOrderByScoreDesc(quizId, PageRequest.of(0, limit))
                .stream().map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "quizStats", key = "#quizId")
    @Transactional(readOnly = true)
    public QuizStatsDTO getQuizStats(Long quizId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);

        int total = attempts.size();
        int completed = (int) attempts.stream().filter(QuizAttempt::isCompleted).count();
        double avg = attempts.stream().mapToInt(QuizAttempt::getScore).average().orElse(0);
        int highest = attempts.stream().mapToInt(QuizAttempt::getScore).max().orElse(0);

        QuizStatsDTO stats = new QuizStatsDTO();
        stats.setTotalAttempts(total);
        stats.setCompletedAttempts(completed);
        stats.setAverageScore(avg);
        stats.setHighestScore(highest);
        return stats;
    }

    @Cacheable(value = "latestAttemptByStudent", key = "#studentId")
    @Transactional(readOnly = true)
    public QuizAttemptDTO getLatestAttemptByStudentId(Long studentId) {
        return quizAttemptRepository.findTopByStudentIdOrderByStartedAtDesc(studentId)
                .map(mapper::toDTO)
                .orElseGet(() -> {
                    QuizAttemptDTO dto = new QuizAttemptDTO();
                    dto.setStudentId(studentId);
                    dto.setScore(0);
                    dto.setTotalMarks(0);
                    dto.setCompleted(false);
                    dto.setStartedAt(null);
                    dto.setCompletedAt(null);
                    return dto;
                });
    }

    @Cacheable(value = "quizzesForStudent", key = "#studentId")
    @Transactional(readOnly = true)
    public List<QuizDTO> getQuizzesForStudent(Long studentId) {
        return quizRepository.findQuizzesByStudentId(studentId)
                .stream()
                .map(quiz -> {
                    QuizDTO dto = new QuizDTO();
                    dto.setId(quiz.getId());
                    dto.setTitle(quiz.getTitle());

                    if (quiz.getCourse() != null) {
                        dto.setCourseId(quiz.getCourse().getId());
                        dto.setCourseTitle(quiz.getCourse().getTitle());
                    }

                    if (quiz.getTeacher() != null) {
                        dto.setTeacherId(quiz.getTeacher().getId());
                    }

                    quizAttemptRepository
                            .findTopByStudentIdAndQuizIdAndCompletedTrueOrderByCompletedAtDesc(studentId, quiz.getId())
                            .ifPresentOrElse(latestCompletedAttempt -> {
                                dto.setTotalMarks(latestCompletedAttempt.getTotalMarks());
                                dto.setStartDate(latestCompletedAttempt.getStartedAt());
                                dto.setDurationMinutes((int) Duration.between(
                                        latestCompletedAttempt.getStartedAt(),
                                        latestCompletedAttempt.getCompletedAt()
                                ).toMinutes());
                                dto.setCompleted(true);
                            }, () -> quizAttemptRepository
                                    .findTopByStudentIdAndQuizIdOrderByStartedAtDesc(studentId, quiz.getId())
                                    .ifPresent(latestAttempt -> {
                                        dto.setTotalMarks(latestAttempt.getTotalMarks());
                                        dto.setStartDate(latestAttempt.getStartedAt());
                                        dto.setDurationMinutes(0);
                                        dto.setCompleted(false);
                                    }));

                    return dto;
                })
                .toList();
    }
}