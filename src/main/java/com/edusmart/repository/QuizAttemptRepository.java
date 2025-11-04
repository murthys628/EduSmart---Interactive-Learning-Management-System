package com.edusmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.edusmart.entity.QuizAttempt;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // All attempts by a specific student
    List<QuizAttempt> findByStudentId(Long studentId);

    // Completed attempts by student
    List<QuizAttempt> findByStudentIdAndCompletedTrue(Long studentId);

    // All attempts for a quiz
    List<QuizAttempt> findByQuizId(Long quizId);   // <-- This is what you need
    
    // All attempts by a specific student for a specific quiz
    List<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);

    // Top scorers for a quiz
    @Query("SELECT q FROM QuizAttempt q WHERE q.quiz.id = :quizId ORDER BY q.score DESC")
    List<QuizAttempt> findTopByQuizIdOrderByScoreDesc(@Param("quizId") Long quizId, Pageable pageable);
    
    // Latest attempt by student (fixed field)
    Optional<QuizAttempt> findTopByStudentIdOrderByStartedAtDesc(Long studentId);
    
    // Latest attempt by a specific student for a specific quiz
    Optional<QuizAttempt> findTopByStudentIdAndQuizIdOrderByStartedAtDesc(Long studentId, Long quizId);
    
    @Query("SELECT a FROM QuizAttempt a JOIN FETCH a.student JOIN FETCH a.quiz WHERE a.id = :attemptId")
    Optional<QuizAttempt> findByIdWithDetails(@Param("attemptId") Long attemptId);
    
 // Latest completed attempt by a student for a specific quiz
    Optional<QuizAttempt> findTopByStudentIdAndQuizIdAndCompletedTrueOrderByCompletedAtDesc(Long studentId, Long quizId);
    
    Optional<QuizAttempt> findByQuizIdAndStudentIdAndCompletedFalse(Long quizId, Long studentId);
}