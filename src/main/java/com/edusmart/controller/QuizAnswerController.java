package com.edusmart.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.QuizAnswerDTO;
import com.edusmart.service.CustomUserDetails;
import com.edusmart.service.QuizAnswerService;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-answers")
public class QuizAnswerController {

    private final QuizAnswerService answerService;

    public QuizAnswerController(QuizAnswerService answerService) {
        this.answerService = answerService;
    }

    /**
     * Submit a single answer for the authenticated student
     */
    @PostMapping("/submit")
    public QuizAnswerDTO submitAnswer(@RequestParam Long attemptId,
                                      @RequestParam Long questionId,
                                      @RequestParam char selectedOption,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails.getId(); // secure student ID
        return answerService.saveAnswer(studentId, attemptId, questionId, selectedOption);
    }

    /**
     * Get all answers for a specific attempt of the authenticated student
     */
    @GetMapping("/attempt/{attemptId}")
    public List<QuizAnswerDTO> getAnswersByAttempt(@PathVariable Long attemptId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails.getId();
        return answerService.getAnswersByAttemptAndStudent(attemptId, studentId);
    }

    /**
     * Get all answers for a quiz of the authenticated student
     */
    @GetMapping("/quiz/{quizId}")
    public List<QuizAnswerDTO> getAnswersByQuiz(@PathVariable Long quizId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails.getId();
        return answerService.getAnswersByStudentAndQuiz(studentId, quizId);
    }
}