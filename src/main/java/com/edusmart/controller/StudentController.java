package com.edusmart.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.CourseResponse;
import com.edusmart.entity.Assignment;
import com.edusmart.entity.User;
import com.edusmart.exception.ResourceNotFoundException;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.AssignmentService;
import com.edusmart.service.CourseService;
import com.edusmart.service.UserService;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final CourseService courseService;
    private final UserRepository userRepo;
    private final UserService userService;
    private final AssignmentService assignmentService;

    public StudentController(CourseService courseService,
                             UserRepository userRepo,
                             UserService userService,
                             AssignmentService assignmentService) {
        this.courseService = courseService;
        this.userRepo = userRepo;
        this.userService = userService;
        this.assignmentService = assignmentService;
    }

    // --------------------------------------------------------------------
    // 🧾 COURSES: Get all enrolled courses for the logged-in student
    // --------------------------------------------------------------------
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getCourses(Authentication authentication) {
        String username = authentication.getName();

        User student = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", username));

        List<CourseResponse> courses = courseService.findCoursesByStudentDTO(student).stream()
                .map(dto -> {
                    String teacherName = dto.getTeacherId() != null
                            ? userService.getUserEntityById(dto.getTeacherId()).getName()
                            : null;
                    String studentName = dto.getStudentId() != null
                            ? userService.getUserEntityById(dto.getStudentId()).getName()
                            : null;

                    return new CourseResponse(
                            dto.getId(),
                            dto.getTitle(),
                            dto.getDescription(),
                            teacherName,
                            studentName
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(courses);
    }

    // --------------------------------------------------------------------
    // 👤 PROFILE: Update student's profile details
    // --------------------------------------------------------------------
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> updates,
            Authentication authentication) {

        String username = authentication.getName();

        User student = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", username));

        // Update only valid, non-empty fields
        if (updates.containsKey("name") && !updates.get("name").isBlank()) {
            student.setName(updates.get("name").trim());
        }
        if (updates.containsKey("email") && !updates.get("email").isBlank()) {
            student.setEmail(updates.get("email").trim());
        }
        if (updates.containsKey("phone") && !updates.get("phone").isBlank()) {
            student.setPhone(updates.get("phone").trim());
        }

        userRepo.save(student);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "studentId", student.getId(),
                "name", student.getName(),
                "email", student.getEmail(),
                "phone", student.getPhone()
        ));
    }

    // --------------------------------------------------------------------
    // 🏆 GRADES: View all grades and feedback for the logged-in student
    // --------------------------------------------------------------------
    @GetMapping("/grades")
    public ResponseEntity<List<Map<String, Object>>> getGrades(Authentication authentication) {
        String username = authentication.getName();

        User student = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", username));

        List<Assignment> assignments = assignmentService.findAssignmentsByStudent(student);

        List<Map<String, Object>> grades = assignments.stream()
                .map(a -> Map.<String, Object>of(
                        "assignmentId", a.getId(),
                        "title", a.getTitle(),
                        "course", a.getCourse() != null ? a.getCourse().getTitle() : "N/A",
                        "grade", a.getGrade() != null ? a.getGrade() : "N/A",
                        "feedback", a.getFeedback() != null ? a.getFeedback() : "None Provided",
                        "status", a.getStatus() != null ? a.getStatus().name() : "UNKNOWN"
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(grades);
    }
}