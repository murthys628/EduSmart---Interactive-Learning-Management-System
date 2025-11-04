package com.edusmart.controller;

import com.edusmart.dto.QuizDTO;
import com.edusmart.entity.User;
import com.edusmart.exception.ResourceNotFoundException;
import com.edusmart.service.QuizService;
import com.edusmart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/quizzes")
public class QuizApiController {

    private final QuizService quizService;
    private final UserRepository userRepo;

    @Autowired
    public QuizApiController(QuizService quizService, UserRepository userRepo) {
        this.quizService = quizService;
        this.userRepo = userRepo;
    }

    // ----------------- GET ALL QUIZZES FOR LOGGED-IN STUDENT -----------------
    @GetMapping
    public List<QuizDTO> getAllQuizzesForStudent(Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student", "username", authentication.getName()));

        return quizService.getQuizzesForStudent(student.getId());
    }

    // ----------------- GET QUIZ BY COURSE FOR LOGGED-IN STUDENT -----------------
    @GetMapping("/course/{courseId}")
    public QuizDTO getQuizByCourse(@PathVariable Long courseId, Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student", "username", authentication.getName()));

        return quizService.getQuizByCourse(courseId, student.getId());
    }

    // ----------------- GET ALL QUIZZES FOR A SPECIFIC COURSE (ADMIN/TEACHER) -----------------
    @GetMapping("/all/course/{courseId}")
    public List<QuizDTO> getAllQuizzesForCourse(@PathVariable Long courseId) {
        return quizService.getAllQuizzesForCourse(courseId);
    }

    // ----------------- GET ALL QUIZZES IN SYSTEM (ADMIN) -----------------
    @GetMapping("/all")
    public List<QuizDTO> getAllQuizzes() {
        return quizService.getAllQuizzesWithCourse();
    }
}