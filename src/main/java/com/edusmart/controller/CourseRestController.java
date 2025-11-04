package com.edusmart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.CourseDTO;
import com.edusmart.dto.CourseRequest;
import com.edusmart.dto.CourseResponse;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;
import com.edusmart.service.CourseService;
import com.edusmart.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseRestController {

    private final CourseService courseService;
    private final UserService userService;

    public CourseRestController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    // ----------------- ADD COURSE FOR SINGLE STUDENT -----------------
    @PostMapping("/add")
    public ResponseEntity<?> addCourse(@RequestBody CourseRequest request, Authentication authentication) {
        try {
            // 1. Get logged-in teacher
            User teacher = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Teacher not found: " + authentication.getName()));

            // 2. Authorization check
            if (teacher.getRole() != User.Role.TEACHER) {
                return ResponseEntity.status(403).body("Access denied. Only teachers can add courses.");
            }

            // 3. Add course (single student)
            Course savedCourse = courseService.addCourse(request, teacher);

            // 4. Success response
            return ResponseEntity.status(201)
                    .body("Course created successfully with ID: " + savedCourse.getId());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal error: " + e.getMessage());
        }
    }

    // ----------------- GET COURSES FOR LOGGED-IN USER -----------------
    @GetMapping("/my")
    public ResponseEntity<List<CourseResponse>> getMyCourses(Authentication authentication) {
        // Get the currently logged-in user
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch courses using DTOs
        List<CourseDTO> courseDTOs;
        if (currentUser.getRole() == User.Role.TEACHER) {
            // Pass User, not ID
            courseDTOs = courseService.findCoursesByTeacherDTO(currentUser);
        } else {
            courseDTOs = courseService.findCoursesByStudentDTO(currentUser);
        }

        // Convert CourseDTO to CourseResponse
        List<CourseResponse> responseList = courseDTOs.stream()
                .map(c -> {
                    String teacherName = null;
                    String studentName = null;

                    if (c.getTeacherId() != null)
                    {
                        teacherName = userService.getUserEntityById(c.getTeacherId()).getName();
                    }
                    if (c.getStudentId() != null) {
                        studentName = userService.getUserEntityById(c.getStudentId()).getName();
                    }

                    return new CourseResponse(
                            c.getId(),
                            c.getTitle(),
                            c.getDescription(),
                            teacherName,
                            studentName
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}