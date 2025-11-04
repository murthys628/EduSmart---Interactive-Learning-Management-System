package com.edusmart.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.QuizAttemptDTO;
import com.edusmart.dto.QuizDTO;
import com.edusmart.entity.Quiz;
import com.edusmart.service.CustomUserDetails;
import com.edusmart.service.QuizAttemptService;
import com.edusmart.service.QuizService; // ✅ Needed to load quiz details

import java.util.List;

@Controller
@RequestMapping("/student")
public class QuizAttemptUIController {

    private final QuizAttemptService quizAttemptService;
    private final QuizService quizService; // ✅ Inject quiz service

    public QuizAttemptUIController(QuizAttemptService quizAttemptService,
                                   QuizService quizService) {
        this.quizAttemptService = quizAttemptService;
        this.quizService = quizService;
    }

    // ✅ Display all attempts for a quiz (existing route)
    @GetMapping("/quiz-attempts/quiz/{quizId}")
    public String viewAttemptsByQuiz(@PathVariable Long quizId, Model model) {
        model.addAttribute("attempts", quizAttemptService.getAttemptsByQuiz(quizId));
        return "quiz_attempts_list";
    }

    // ✅ New route for /student/quiz-attempts (for UI template)
    @GetMapping("/quiz-attempts")
    public String viewAllAttempts(@RequestParam(required = false) Long quizId,
                                  Model model,
                                  Authentication authentication) {

        Long studentId = ((CustomUserDetails) authentication.getPrincipal()).getId();

        List<QuizAttemptDTO> attempts;
        String quizTitle = "All Quizzes";

        if (quizId != null) {
            attempts = quizAttemptService.getAttemptsByQuiz(quizId)
                    .stream()
                    .filter(a -> a.getStudentId().equals(studentId))
                    .toList();
            if (!attempts.isEmpty()) {
                quizTitle = attempts.get(0).getQuizTitle();
            }
        } else {
            attempts = quizAttemptService.getAttemptsByStudent(studentId);
        }

        model.addAttribute("attempts", attempts);
        model.addAttribute("quizTitle", quizTitle);

        return "quiz_attempts_list";
    }

    // ✅ Display completed attempts for a student
    @GetMapping("/quiz-attempts/student/{studentId}/completed")
    public String viewCompletedAttempts(@PathVariable Long studentId, Model model) {
        model.addAttribute("attempts", quizAttemptService.getCompletedAttemptsByStudent(studentId));
        return "student_completed_attempts";
    }

    // ✅ Display quiz stats
    @GetMapping("/quiz-attempts/quiz/{quizId}/stats")
    public String viewQuizStats(@PathVariable Long quizId, Model model) {
        model.addAttribute("stats", quizAttemptService.getQuizStats(quizId));
        return "quiz_stats";
    }

    // ✅ Display top scorers
    @GetMapping("/quiz-attempts/quiz/{quizId}/top-scorers")
    public String viewTopScorers(@PathVariable Long quizId,
                                 @RequestParam(defaultValue = "10") int limit,
                                 Model model) {
        model.addAttribute("topScorers", quizAttemptService.getTopScorers(quizId, limit));
        return "quiz_top_scorers";
    }

    // 🚀 NEW — Launch the actual quiz page with timer and attempt tracking
    @GetMapping("/quiz/{quizId}/start")
    public String startQuiz(@PathVariable Long quizId,
                            Authentication authentication,
                            Model model) {
        Long studentId = ((CustomUserDetails) authentication.getPrincipal()).getId();

        // ✅ 1. Create or resume attempt
        QuizAttemptDTO attempt = quizAttemptService.startOrResumeAttempt(quizId, studentId);

        // ✅ 2. Fetch quiz details
        QuizDTO quiz = quizService.getQuizById(quizId);

        // ✅ 3. Add model attributes
        model.addAttribute("quiz", quiz);
        model.addAttribute("studentId", studentId);
        model.addAttribute("currentAttemptId", attempt.getId());

        // ✅ 4. Return page
        return "quizPage";
    }
}