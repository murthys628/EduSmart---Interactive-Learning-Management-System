package com.edusmart.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.CourseDTO;
import com.edusmart.dto.CourseRequest;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;
import com.edusmart.service.CourseService;
import com.edusmart.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    public CourseController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    // ===== Show Add Course Form =====
    @GetMapping("/add")
    public String showCourseForm(Model model) {
        model.addAttribute("courseRequest", new CourseRequest());
        return "course_form"; // Thymeleaf template
    }

    // ===== Add Course =====
    @PostMapping("/add")
    public String addCourse(@ModelAttribute CourseRequest courseRequest,
                            Authentication authentication, Model model) {
        try {
            User teacher = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            Course savedCourse = courseService.addCourse(courseRequest, teacher);
            model.addAttribute("message", "Course saved successfully! ID: " + savedCourse.getId());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to save course: " + e.getMessage());
        }

        return "course_form";
    }

    // ===== Show My Courses =====
    @GetMapping("/my")
    public String showMyCourses(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CourseDTO> courses; // Use DTO list
        if (currentUser.getRole() == User.Role.TEACHER) {
            // Use the service method that returns CourseDTOs
            courses = courseService.findCoursesByTeacherDTO(currentUser);
        } else {
            courses = courseService.findCoursesByStudentDTO(currentUser);
        }

        model.addAttribute("courses", courses);
        model.addAttribute("currentUser", currentUser);

        return "my-courses";
    }
}