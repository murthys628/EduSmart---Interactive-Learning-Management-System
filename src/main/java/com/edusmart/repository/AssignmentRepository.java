package com.edusmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.edusmart.entity.Assignment;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // Fetch all assignments for a teacher
    List<Assignment> findByTeacher(User teacher);

    // Fetch all assignments for a course
    List<Assignment> findByCourse(Course course);

    // Fetch all assignments for a student
    List<Assignment> findByStudent(User student);
}