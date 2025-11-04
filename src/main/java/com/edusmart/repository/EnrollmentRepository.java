package com.edusmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

import com.edusmart.entity.Enrollment;

import org.springframework.transaction.annotation.Propagation; // 👈 New Import Needed!

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>{

	Optional<Enrollment> findByStudentIdAndQuizId(long studentId, long quizId);

    @QueryHints(@jakarta.persistence.QueryHint(name = "org.hibernate.cacheable", value = "false"))
    List<Enrollment> findByQuizId(long quizId);

    List<Enrollment> findByStudentId(long studentId);
    
    // -------------------------------------------------------------
    // 🎯 FINAL FIX: ENSURE TRANSACTION AND PERSISTENCE CONTEXT CLEARING
    // -------------------------------------------------------------
    
    @Modifying(clearAutomatically = true) // Prevents stale data reads
    // 🛑 CHANGE MADE: Forces this update to run in its OWN, separate transaction.
    // A failure here WILL NOT roll back the primary QuizAttempt save in the service.
    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    @Query("UPDATE Enrollment e SET e.completed = true, e.score = :finalScore " +
           "WHERE e.studentId = :sId AND e.quizId = :qId")
    void updateEnrollmentCompletionStatus(
        @Param("sId") Long studentId, 
        @Param("qId") Long quizId, 
        @Param("finalScore") Double finalScore
    );
}