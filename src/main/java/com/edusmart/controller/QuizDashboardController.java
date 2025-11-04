package com.edusmart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.EnrollmentDTO;
import com.edusmart.dto.QuizDTO;
import com.edusmart.entity.User;
import com.edusmart.exception.ResourceNotFoundException;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.EnrollmentService;
import com.edusmart.service.QuizService;

import java.util.List;

@Controller
@RequestMapping("/student/quiz")
public class QuizDashboardController {

    private final QuizService quizService;
    private final UserRepository userRepo;
    private final EnrollmentService enrollmentService;

    public QuizDashboardController(QuizService quizService, UserRepository userRepo, EnrollmentService enrollmentService) {
        this.quizService = quizService;
        this.userRepo = userRepo;
        this.enrollmentService = enrollmentService;
    }

    // ----------------- SHOW ALL QUIZZES -----------------
    @GetMapping
    public String showStudentQuizzes(Model model, Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudent(student.getId());
        List<Long> quizIds = enrollments.stream()
                .map(EnrollmentDTO::getQuizId)
                .filter(id -> id != null)
                .toList();

        List<QuizDTO> quizzes = quizService.getQuizzesForStudentFromEnrollments(quizIds);

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("studentName", student.getName());

        return "student-quizzes"; // shows table of quizzes
    }

    // ----------------- START A QUIZ -----------------
    @GetMapping("/start/{quizId}")
    public String startQuiz(@PathVariable Long quizId, Model model, Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        QuizDTO quiz = quizService.getQuizById(quizId); // fetch single quiz

        model.addAttribute("quiz", quiz);
        model.addAttribute("studentName", student.getName());
        
        // 💡 FIX: Add the full student object to the model to resolve ${student.id}
        model.addAttribute("student", student); 

        return "student-quiz"; // Thymeleaf template for a single quiz
    }
    
 // ----------------- ATTEMPT THE QUIZ -----------------
    @GetMapping("/attempt/{quizId}")
    public String attemptQuiz(
        @PathVariable Long quizId, 
        @RequestParam Long studentId, 
        Model model, 
        Authentication authentication
    ) {
        // 1. Get the authenticated student and VERIFY ID (Security check)
        User student = userRepo.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        // Ensure the ID passed in the URL matches the logged-in user's ID
        if (!student.getId().equals(studentId)) {
            // Log this security violation, then throw a specific exception
            throw new ResourceNotFoundException("Student", "id", studentId); 
            // Note: Using ResourceNotFoundException for security here is a defensive measure
            // A dedicated 'UnauthorizedAccessException' is often better practice.
        }
        
        // 2. Fetch the quiz data 
        // Assuming quizService.getQuizById(quizId) fetches the QuizDTO which includes the List<Question>
        QuizDTO quiz = quizService.getQuizById(quizId); 
        
        if (quiz == null) {
            throw new ResourceNotFoundException("Quiz", "id", quizId);
        }
        
        // 3. Add necessary objects to the Model
        model.addAttribute("quiz", quiz); 
        
        // Add the student ID (Needed for the hidden input in the HTML form)
        // We can use student.getId() which we've verified
        model.addAttribute("studentId", student.getId()); 
        
        // Add the student object for any other page elements (e.g., student name in sidebar)
        model.addAttribute("student", student); 

        // The return statement must match the filename (quizPage.html)
        return "quizPage"; 
    }
}