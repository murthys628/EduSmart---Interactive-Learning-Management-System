package com.edusmart.controller;

import com.edusmart.dto.EnrollmentDTO;
import com.edusmart.dto.QuestionDTO;
import com.edusmart.service.EnrollmentService;
import com.edusmart.service.QuestionService;
import com.edusmart.service.QuizService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student/quiz")
public class QuestionDashboardController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired 
    private QuizService quizService; // Injection is correct

    /**
     * Load quiz questions for the student
     */
    @GetMapping("/{quizId}")
    public String loadQuiz(@PathVariable Long quizId,
                           @RequestParam Long studentId,
                           Model model) {

        // Fetch questions for the quiz
        List<QuestionDTO> questions = questionService.getQuestionsByQuizId(quizId);

        // Check if student is enrolled
        EnrollmentDTO enrollment = enrollmentService.getEnrollment(studentId, quizId);
        if (enrollment == null) {
            model.addAttribute("error", "You are not enrolled in this quiz.");
            return "error";
        }

        model.addAttribute("questions", questions);
        model.addAttribute("quizId", quizId);
        model.addAttribute("studentId", studentId);
        return "quizPage"; // Thymeleaf or JSP page
    }

    /**
     * Submit quiz answers
     */
    @PostMapping("/submit/{quizId}")
    public String submitQuiz(
            @PathVariable Long quizId,
            @RequestParam Long studentId,
            @RequestParam Map<String, String> submittedAnswers, // Contains Q_ID -> Answer
            Model model) {

        // 1️⃣ Check if student is enrolled
        EnrollmentDTO enrollment = enrollmentService.getEnrollment(studentId, quizId);
        if (enrollment == null) {
            model.addAttribute("error", "You are not enrolled in this quiz.");
            return "error";
        }

        // 2️⃣ Evaluate total score
        // FIX: Call the correct service method (quizService.calculateScore)
        // This method expects the raw form map (submittedAnswers)
        double score = quizService.calculateScore(quizId, submittedAnswers); 

        // 3️⃣ Update enrollment after quiz
        // FIX: Call the correct method name (completeQuiz) which contains saveAndFlush
        EnrollmentDTO updatedEnrollment = enrollmentService.completeQuiz(studentId, quizId, score);

        // 4️⃣ Prepare model attributes
        List<QuestionDTO> questions = questionService.getQuestionsByQuizId(quizId);

        // Prepare the answers map for the results page by filtering non-question params
        Map<Long, Character> charAnswersMap = submittedAnswers.entrySet().stream()
                .filter(e -> {
                    // Filter out non-question-related params (like 'studentId')
                    try {
                        Long.parseLong(e.getKey());
                        return true;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                })
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey()), // Question ID as Long
                        e -> e.getValue().trim().toUpperCase().charAt(0) // Answer as Character
                ));


        // 5️⃣ Add attributes to model for Thymeleaf
        model.addAttribute("quiz", Map.of(
                "title", updatedEnrollment.getQuizTitle(), // Use updatedEnrollment to get title
                "questions", questions
        ));
        
        model.addAttribute("answers", charAnswersMap); // Student's submitted answers (Q_ID -> Char)
        model.addAttribute("score", score);
        model.addAttribute("totalMarks", questions.stream().mapToInt(QuestionDTO::getMarks).sum());

        // 6️⃣ Return feedback page
        return "quizResult"; 
    }
}