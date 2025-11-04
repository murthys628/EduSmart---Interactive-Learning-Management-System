package com.edusmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.edusmart.entity.Course;
import com.edusmart.entity.User;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Find all courses taught by a teacher (using the User entity)
    List<Course> findByTeacher(User teacher);

    // Count courses for a teacher
    long countByTeacher(User teacher);

    // Find all students taught by a teacher (distinct)
    @Query("SELECT DISTINCT c.student FROM Course c WHERE c.teacher = :teacher")
    List<User> findStudentsByTeacher(@Param("teacher") User teacher);

    // Find all courses for a specific student (using the User entity)
    List<Course> findByStudent(User student);
    
    @Query("SELECT c FROM Course c WHERE " +
    "LOWER(c.title) LIKE LOWER(CONCAT(:query, '%'))") 
    List<Course> findBySearchTerm(@Param("query") String query);
    
    @Query("SELECT c FROM Course c ORDER BY c.id DESC") // Orders by most recently created
    List<Course> findTopCourses(Pageable pageable);
    
    @Query("SELECT c FROM Course c ORDER BY c.id DESC")
    List<Course> findAllCoursesOrderedByRecent();
    
    // =========================================================================
    // ✅ NEW METHODS FOR ROLE-BASED CHAT FETCHING
    // These methods are required by CourseService.java for the ChatController fix.
    // =========================================================================
    
    /**
     * Finds all courses linked to a specific student ID (for student enrollment).
     * This uses Spring Data JPA's query creation from method name, mapping to 
     * the 'student' field's ID property in the Course entity.
     */
    List<Course> findAllByStudentId(Long studentId); 

    /**
     * Finds all courses taught by a specific teacher ID (for teacher assignment).
     * This uses Spring Data JPA's query creation from method name, mapping to 
     * the 'teacher' field's ID property in the Course entity.
     */
    List<Course> findAllByTeacherId(Long teacherId); // ⬅️ Add this line!
}