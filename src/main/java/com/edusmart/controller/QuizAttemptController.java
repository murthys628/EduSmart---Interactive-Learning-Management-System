package com.edusmart.controller;

import com.edusmart.dto.QuizAttemptDTO;
import com.edusmart.dto.QuizStatsDTO;
import com.edusmart.service.QuizAttemptService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-attempts")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        this.quizAttemptService = quizAttemptService;
    }

    // =============================================================
    // 🔹 START or RESUME a Quiz Attempt
    // =============================================================
    @PostMapping("/start")
    public ResponseEntity<?> startAttempt(@RequestParam Long quizId, @RequestParam Long studentId) {
        try {
            QuizAttemptDTO dto = quizAttemptService.startOrResumeAttempt(quizId, studentId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =============================================================
    // 🔹 COMPLETE Quiz Attempt
    // =============================================================
    @PostMapping("/complete/{attemptId}")
    public ResponseEntity<?> completeAttempt(@PathVariable Long attemptId,
                                             @RequestParam int score,
                                             @RequestParam int totalMarks) {
        try {
            QuizAttemptDTO dto = quizAttemptService.completeAttempt(attemptId, score, totalMarks);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =============================================================
    // 🔹 GET all Attempts by a Student
    // =============================================================
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getAttemptsByStudent(@PathVariable Long studentId) {
        try {
            List<QuizAttemptDTO> attempts = quizAttemptService.getAttemptsByStudent(studentId);
            return ResponseEntity.ok(attempts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =============================================================
    // 🔹 GET Remaining Time for Timer (frontend polling support)
    // =============================================================
    @GetMapping("/{attemptId}/remaining-time")
    public ResponseEntity<?> getRemainingTime(@PathVariable Long attemptId) {
        try {
            long remainingSeconds = quizAttemptService.getRemainingTimeSeconds(attemptId);
            return ResponseEntity.ok(remainingSeconds);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =============================================================
    // 🔹 FORCE EXPIRE if Timer is Over (auto-complete)
    // =============================================================
    @PostMapping("/{attemptId}/expire-if-over")
    public ResponseEntity<?> expireIfOver(@PathVariable Long attemptId) {
        try {
            boolean expired = quizAttemptService.expireIfTimeOver(attemptId);
            return ResponseEntity.ok(expired ? "Attempt expired and completed." : "Still active.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =============================================================
    // 🔹 GET QUIZ STATISTICS (optional dashboard)
    // =============================================================
    @GetMapping("/quiz/{quizId}/stats")
    public ResponseEntity<?> getQuizStats(@PathVariable Long quizId) {
        try {
            QuizStatsDTO stats = quizAttemptService.getQuizStats(quizId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}