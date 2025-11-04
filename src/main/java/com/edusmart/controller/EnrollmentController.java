package com.edusmart.controller;

import com.edusmart.dto.EnrollmentDTO;
import com.edusmart.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // ----------------- Enroll a student in a quiz -----------------
    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentDTO> enrollStudent(@RequestBody EnrollmentDTO enrollmentDTO) {
        EnrollmentDTO enrollment = enrollmentService.enrollStudent(
                enrollmentDTO.getStudentId(), 
                enrollmentDTO.getQuizId()
        );
        if (enrollment == null) {
            return ResponseEntity.badRequest().body(null); // already enrolled
        }
        return ResponseEntity.ok(enrollment);
    }

    // ----------------- Get all enrollments of a student -----------------
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollments(@PathVariable long studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    // ----------------- Complete a quiz -----------------
    @PostMapping("/complete")
    public ResponseEntity<EnrollmentDTO> completeQuiz(@RequestBody EnrollmentDTO enrollmentDTO) {
        EnrollmentDTO enrollment = enrollmentService.completeQuiz(
                enrollmentDTO.getStudentId(),
                enrollmentDTO.getQuizId(),
                enrollmentDTO.getScore()
        );
        return ResponseEntity.ok(enrollment);
    }
}