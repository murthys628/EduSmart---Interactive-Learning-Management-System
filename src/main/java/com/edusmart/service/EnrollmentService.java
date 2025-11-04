package com.edusmart.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.edusmart.dto.EnrollmentDTO;
import com.edusmart.entity.Enrollment;
import com.edusmart.entity.Quiz;
import com.edusmart.entity.QuizAttempt;
import com.edusmart.mapper.EnrollmentMapper;
import com.edusmart.repository.EnrollmentRepository;
import com.edusmart.repository.QuizAttemptRepository;
import com.edusmart.repository.QuizRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, QuizRepository quizRepository,
                             QuizAttemptRepository quizAttemptRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    // ✅ ENROLL student in a quiz — evict related caches
    @CacheEvict(value = {"enrollmentsByStudent", "enrollmentsByQuiz", "allEnrollments"}, allEntries = true)
    public EnrollmentDTO enrollStudent(long studentId, long quizId) {
        if (enrollmentRepository.findByStudentIdAndQuizId(studentId, quizId).isPresent()) {
            return null; // Already enrolled
        }

        Enrollment enrollment = new Enrollment(studentId, quizId);
        enrollment.setEnrolledAt(new Date());
        Enrollment saved = enrollmentRepository.save(enrollment);

        return EnrollmentMapper.toDTO(saved, null);
    }

    // ✅ Get all enrollments for a student
    @Cacheable(value = "enrollmentsByStudent", key = "#studentId")
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByStudent(long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(e -> EnrollmentMapper.toDTO(e, null))
                .collect(Collectors.toList());
    }

    // ✅ Complete quiz — must evict affected caches
    @CacheEvict(value = {"enrollmentsByStudent", "enrollmentsByQuiz", "enrollment"}, allEntries = true)
    public EnrollmentDTO completeQuiz(long studentId, long quizId, double score) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndQuizId(studentId, quizId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setCompleted(true);
        enrollment.setScore(score);

        Enrollment updated = enrollmentRepository.saveAndFlush(enrollment);
        return EnrollmentMapper.toDTO(updated, null);
    }

    // ✅ Get all enrollments for a quiz
    @Cacheable(value = "enrollmentsByQuiz", key = "#quizId")
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByQuiz(long quizId) {
        return enrollmentRepository.findByQuizId(quizId)
                .stream()
                .map(e -> {
                    String quizTitle = quizRepository.findById(e.getQuizId())
                            .map(Quiz::getTitle)
                            .orElse("N/A");

                    Optional<QuizAttempt> latestAttemptOpt =
                            quizAttemptRepository.findTopByStudentIdAndQuizIdOrderByStartedAtDesc(
                                    e.getStudentId(), e.getQuizId());

                    int score = 0;
                    int totalMarks = 1;
                    boolean completed = false;

                    if (latestAttemptOpt.isPresent()) {
                        QuizAttempt latestAttempt = latestAttemptOpt.get();
                        score = latestAttempt.getScore();
                        totalMarks = latestAttempt.getTotalMarks() > 0 ? latestAttempt.getTotalMarks() : 1;
                        completed = latestAttempt.isCompleted();
                    }

                    EnrollmentDTO dto = EnrollmentMapper.toDTO(e, quizTitle);
                    dto.setCompleted(completed);
                    double percentage = ((double) score / totalMarks) * 100;
                    dto.setScorePercentageString(String.format("%.2f%%", percentage));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ✅ Get all enrollments (admin use)
    @Cacheable(value = "allEnrollments")
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .stream()
                .map(e -> EnrollmentMapper.toDTO(e, null))
                .collect(Collectors.toList());
    }

    // ✅ Get single enrollment
    @Cacheable(value = "enrollment", key = "#studentId + '-' + #quizId")
    @Transactional(readOnly = true)
    public EnrollmentDTO getEnrollment(Long studentId, Long quizId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndQuizId(studentId, quizId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        return EnrollmentMapper.toDTO(enrollment, null);
    }
}