package com.edusmart.controller;

import com.edusmart.dto.EnrollmentDTO;
import com.edusmart.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dashboard/enrollments")
public class EnrollmentDashboardController {

    private final EnrollmentService enrollmentService;

    public EnrollmentDashboardController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // Show all enrollments
    @GetMapping
    public String viewAllEnrollments(Model model) {
        List<EnrollmentDTO> enrollments = enrollmentService.getAllEnrollments();
        model.addAttribute("enrollments", enrollments);
        return "enrollments/list"; // Thymeleaf or JSP page
    }

    // View enrollments by student
    @GetMapping("/student/{studentId}")
    public String viewEnrollmentsByStudent(@PathVariable long studentId, Model model) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        model.addAttribute("enrollments", enrollments);
        return "enrollments/list";
    }

    // View enrollments by quiz
    @GetMapping("/quiz/{quizId}")
    public String viewEnrollmentsByQuiz(@PathVariable long quizId, Model model) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByQuiz(quizId);
        model.addAttribute("enrollments", enrollments);
        return "enrollments/list";
    }

    // Mark enrollment as completed (optional dashboard action)
    @PostMapping("/complete")
    public String completeEnrollment(@RequestParam long studentId,
                                     @RequestParam long quizId,
                                     @RequestParam double score) {
        enrollmentService.completeQuiz(studentId, quizId, score);
        return "redirect:/dashboard/enrollments";
    }
}