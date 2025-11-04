package com.edusmart.repository;

import com.edusmart.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    // Get all answers for a specific attempt
    List<QuizAnswer> findByAttemptId(Long attemptId);

    // Get all answers for a specific student and quiz (via attempt)
    List<QuizAnswer> findByAttemptQuizIdAndAttemptStudentId(Long quizId, Long studentId);
    
    // ✅ Get all answers for a specific attempt AND student
    List<QuizAnswer> findByAttemptIdAndAttemptStudentId(Long attemptId, Long studentId);
    
    // ✅ Count all answers for a specific student
    int countByAttemptStudentId(Long studentId);
}