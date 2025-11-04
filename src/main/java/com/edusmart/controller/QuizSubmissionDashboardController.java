package com.edusmart.controller;

import com.edusmart.dto.QuizSubmissionResponseDTO;
import com.edusmart.service.QuizSubmissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dashboard/quiz-submissions")
public class QuizSubmissionDashboardController {

    private final QuizSubmissionService submissionService;

    public QuizSubmissionDashboardController(QuizSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    // ---------- List all submissions ----------
    @GetMapping("/all")
    public String listAllSubmissions(Model model) {
        List<QuizSubmissionResponseDTO> submissions = submissionService.getAllSubmissions();
        model.addAttribute("submissions", submissions);
        model.addAttribute("filterType", "all");
        return "quiz_submissions"; // Template: quiz_submissions.html
    }

    // ---------- List submissions filtered by student ----------
    @GetMapping("/student/{studentId}")
    public String listSubmissionsByStudent(@PathVariable Long studentId, Model model) {
        List<QuizSubmissionResponseDTO> submissions = submissionService.getSubmissionsByStudent(studentId);
        model.addAttribute("submissions", submissions);
        model.addAttribute("filterType", "student");
        model.addAttribute("filterId", studentId);
        return "quiz_submissions"; // Same template
    }

    // ---------- List submissions filtered by quiz ----------
    @GetMapping("/quiz/{quizId}")
    public String listSubmissionsByQuiz(@PathVariable Long quizId, Model model) {
        List<QuizSubmissionResponseDTO> submissions = submissionService.getSubmissionsByQuiz(quizId);
        model.addAttribute("submissions", submissions);
        model.addAttribute("filterType", "quiz");
        model.addAttribute("filterId", quizId);
        return "quiz_submissions"; // Same template
    }

    // ---------- View single submission details ----------
    @GetMapping("/view/{id}")
    public String viewSubmission(@PathVariable Long id, Model model) {
        QuizSubmissionResponseDTO submission = submissionService.getSubmission(id);
        model.addAttribute("submission", submission);
        return "quiz_submission_view"; // Template: quiz_submission_view.html
    }
}