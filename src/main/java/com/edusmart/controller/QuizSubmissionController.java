package com.edusmart.controller;

import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.QuizSubmissionRequestDTO;
import com.edusmart.dto.QuizSubmissionResponseDTO;
import com.edusmart.entity.Question;
import com.edusmart.repository.QuestionRepository;
import com.edusmart.service.QuizSubmissionService;

@RestController
@RequestMapping("/api/quiz-submissions")
public class QuizSubmissionController {

    private final QuizSubmissionService service;
    private final QuestionRepository questionRepo;

    public QuizSubmissionController(QuizSubmissionService service, QuestionRepository questionRepo) {
        this.service = service;
        this.questionRepo = questionRepo;
    }

    @PostMapping("/submit")
    public QuizSubmissionResponseDTO submitQuiz(@RequestBody QuizSubmissionRequestDTO dto) {
        return service.submitQuiz(dto);
    }

    @GetMapping("/{id}")
    public QuizSubmissionResponseDTO getSubmission(@PathVariable Long id) {
        return service.getSubmission(id);
    }
    
    @PostMapping("/submit-single")
    public QuizSubmissionResponseDTO submitSingleQuestion(
            @RequestParam Long quizId,
            @RequestParam Long studentId,
            @RequestParam Long questionId,
            @RequestParam String selectedOption) {

        // Fetch question entity (you need questionRepo injected)
        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Calculate score
        int score = 0;
        if (selectedOption != null && !selectedOption.isEmpty()) {
            char selected = selectedOption.toUpperCase().charAt(0);
            if (selected == question.getCorrectOption()) {
                score = question.getMarks();
            }
        }

        // Build DTO
        QuizSubmissionRequestDTO dto = new QuizSubmissionRequestDTO();
        dto.setQuizId(quizId);
        dto.setStudentId(studentId);
        dto.addQuestion(questionId, selectedOption);
        dto.getQuestionSubmissions().get(0).setScore(score);  // set the score

        return service.submitQuiz(dto);
    }
    
    // ---------- DELETE endpoint ----------
    @DeleteMapping("/{id}")
    public String deleteSubmission(@PathVariable Long id) {
        service.deleteSubmission(id);
        return "Submission with ID " + id + " has been deleted successfully.";
    }
}