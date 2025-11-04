package com.edusmart.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.QuizAnswerDTO;
import com.edusmart.dto.QuizAttemptDTO;
import com.edusmart.service.CustomUserDetails;
import com.edusmart.service.QuizAnswerService;
import com.edusmart.service.QuizAttemptService;

import java.util.List;

@Controller
@RequestMapping("/dashboard/quiz-answers")
public class QuizAnswerDashboardController {

    private final QuizAnswerService answerService;
    private final QuizAttemptService quizAttemptService;

    public QuizAnswerDashboardController(QuizAnswerService answerService, QuizAttemptService quizAttemptService) {
        this.answerService = answerService;
        this.quizAttemptService = quizAttemptService;
    }

    // Show all answers for a specific attempt
    @GetMapping("/attempt/{attemptId}")
    public String showAnswersByAttempt(@PathVariable Long attemptId,
                                       Model model,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Get the logged-in student ID
        Long studentId = userDetails.getId();

        // Fetch answers only for this student
        List<QuizAnswerDTO> answers = answerService.getAnswersByAttemptAndStudent(attemptId, studentId);

        model.addAttribute("answers", answers);
        return "quiz_answers_list"; // Thymeleaf template
    }

    // Show all answers for a student in a quiz
    @GetMapping("/student/{studentId}/quiz/{quizId}")
    public String showAnswersByStudentAndQuiz(@PathVariable Long studentId,
                                              @PathVariable Long quizId,
                                              Model model) {
        List<QuizAnswerDTO> answers = answerService.getAnswersByStudentAndQuiz(studentId, quizId);
        model.addAttribute("answers", answers);
        return "student_quiz_answers"; // Thymeleaf template
    }
    
    @GetMapping("/latest")
    public String showLatestAnswers(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails.getId();

        // 1. Fetch the LATEST Quiz Attempt object.
        // This attempt object contains the metadata (Score, Start Date, Duration, etc.)
        QuizAttemptDTO latestAttempt = quizAttemptService.getLatestAttemptByStudentId(studentId);
        
        // Check if an attempt was found
        if (latestAttempt == null) {
            // If no attempt is found, pass a null attempt and an empty list of answers
            model.addAttribute("attempt", null);
            model.addAttribute("answers", java.util.Collections.emptyList());
            return "quiz_answers_list"; 
        }

        // 2. Use the Attempt ID to fetch the corresponding Answers
        List<QuizAnswerDTO> latestAnswers = 
            answerService.getAnswersByAttemptAndStudent(latestAttempt.getId(), studentId);
            
        // 3. Add both the metadata (attempt) and the details (answers) to the model
        model.addAttribute("attempt", latestAttempt);
        model.addAttribute("answers", latestAnswers);

        return "quiz_answers_list"; 
    }
}