package com.edusmart.controller;

import com.edusmart.dto.AssignmentRequest;
import com.edusmart.dto.AssignmentResponse;
import com.edusmart.entity.Assignment;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;
import com.edusmart.repository.CourseRepository;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.AssignmentService;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final UserRepository userRepo;
    private final CourseRepository courseRepo;

    public AssignmentController(AssignmentService assignmentService, UserRepository userRepo, CourseRepository courseRepo) {
        this.assignmentService = assignmentService;
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
    }

    // ===================== 1. Teacher assigns a new assignment =====================
    @PostMapping("/assign")
    public ResponseEntity<?> assignAssignment(@Valid @RequestBody AssignmentRequest request, Authentication authentication) {
        User teacher = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (teacher.getRole() != User.Role.TEACHER) {
            return ResponseEntity.status(403).body("Access Denied: Only teachers can assign assignments.");
        }

        Course course = courseRepo.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Base assignment
        Assignment baseAssignment = new Assignment();
        baseAssignment.setTitle(request.getTitle());
        baseAssignment.setDescription(request.getDescription());

        // Service creates assignment for the single student
        List<Assignment> savedAssignments = assignmentService.assignAssignment(baseAssignment, course, teacher);

        if (savedAssignments.isEmpty()) {
            return ResponseEntity.ok("Assignment created, but no student is assigned to this course.");
        }

        String message = String.format("Successfully assigned '%s' to student '%s' in course '%s'.",
                request.getTitle(),
                course.getStudent() != null ? course.getStudent().getName() : "N/A",
                course.getTitle());

        return ResponseEntity.status(201).body(message);
    }

    // ===================== 2. Teacher views assignments =====================
    @GetMapping("/teacher")
    public List<AssignmentResponse> getAssignmentsForTeacher(Authentication authentication) {
        User teacher = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (teacher.getRole() != User.Role.TEACHER) {
            throw new RuntimeException("Access Denied: User is not a teacher.");
        }

        return assignmentService.getAssignmentsForTeacher(teacher)
                .stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    // ===================== 3. Teacher approves assignment =====================
    @PostMapping("/approve/{id}")
    public ResponseEntity<AssignmentResponse> approveAssignment(@PathVariable Long id, Authentication authentication) {
        User teacher = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        try {
            Assignment approved = assignmentService.approveAssignment(id, teacher);
            return ResponseEntity.ok(mapToAssignmentResponse(approved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // ===================== 4. Student views their assignments =====================
    @GetMapping("/student")
    public List<AssignmentResponse> getAssignmentsForStudent(Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("Access Denied: User is not a student.");
        }

        // Fetch assignments via service filtering by student
        return assignmentService.getAssignmentsForStudent(student)
                .stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    // ===================== 5. Student submits assignment =====================
    @PostMapping("/submit/{id}")
    public ResponseEntity<AssignmentResponse> submitAssignment(
            @PathVariable Long id,
            Authentication authentication) {

        // 1️⃣ Fetch logged-in student
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getRole() != User.Role.STUDENT) {
            return ResponseEntity.status(403).body(null);
        }

        try {
            // 2️⃣ Submit assignment & trigger emails
            Assignment submitted = assignmentService.submitAssignment(id, student);

            // 3️⃣ Map to DTO for response
            AssignmentResponse response = mapToAssignmentResponse(submitted);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(null); // Unauthorized
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null); // Assignment not found
        }
    }

    // ===================== Helper method =====================
    private AssignmentResponse mapToAssignmentResponse(Assignment a) {
        return new AssignmentResponse(
                a.getId(),
                a.getTitle(),
                a.getDescription(),
                a.getCourse() != null ? a.getCourse().getId() : null,                // courseId
                a.getCourse() != null ? a.getCourse().getTitle() : null,             // courseName
                a.getStudent() != null ? a.getStudent().getId() : null,              // studentId
                a.getStudent() != null ? a.getStudent().getName() : null,            // studentName
                a.getTeacher() != null ? a.getTeacher().getName() : null,            // teacherName
                a.getStatus() != null ? a.getStatus().name() : null,                 // status
                a.getGrade(),                                                         // grade
                a.getFeedback()                                                       // feedback
        );
    }
}