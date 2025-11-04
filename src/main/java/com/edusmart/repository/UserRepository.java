package com.edusmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.edusmart.entity.Course;
import com.edusmart.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    // ✅ ADDED: Find a User by their email
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Fetch all students of a teacher (Corrected for Many-to-Many)
    @Query("""
    	    SELECT DISTINCT c.student
    	    FROM Course c
    	    WHERE c.teacher = :teacher
    	""")
    	List<User> findStudentsByTeacher(@Param("teacher") User teacher);
    
    // Find all users (students) enrolled in a specific course
    List<User> findAllByEnrolledCourses(Course course);
    
    List<User> findAllByRole(User.Role role);
    
    // Count users by role
    long countByRole(User.Role role);
}