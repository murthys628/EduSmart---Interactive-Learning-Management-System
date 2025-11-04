package com.edusmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edusmart.entity.Quiz;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // 1️⃣ Fetch all quizzes with their associated course
    @Query("SELECT q FROM Quiz q JOIN FETCH q.course")
    List<Quiz> findAllWithCourse();

    // 2️⃣ Fetch quizzes by a specific course (used in QuizService)
    @Query("SELECT q FROM Quiz q JOIN FETCH q.course WHERE q.course.id = :courseId")
    List<Quiz> findByCourseId(@Param("courseId") Long courseId);

    // 3️⃣ Fetch quizzes created by a specific teacher
    @Query("SELECT q FROM Quiz q JOIN FETCH q.course WHERE q.teacher.id = :teacherId")
    List<Quiz> findByTeacherId(@Param("teacherId") Long teacherId);

    // 4️⃣ Fetch quizzes where a specific student has submissions (for student dashboard)
    @Query("""
           SELECT DISTINCT q FROM Quiz q
           JOIN FETCH q.course c
           JOIN q.submissions s
           WHERE s.student.id = :studentId
           """)
    List<Quiz> findQuizzesByStudentId(@Param("studentId") Long studentId);
}