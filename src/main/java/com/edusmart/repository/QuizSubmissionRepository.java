package com.edusmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edusmart.entity.QuizSubmission;

import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    List<QuizSubmission> findByStudentId(Long studentId);
    
    List<QuizSubmission> findByQuizId(Long quizId);
    
    // 🔹 Add this method — it’s the one used by your service:
    List<QuizSubmission> findByQuizIdAndStudentId(Long quizId, Long studentId);
}