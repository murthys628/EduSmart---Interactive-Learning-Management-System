package com.edusmart.mapper;

import com.edusmart.dto.EnrollmentDTO;
import com.edusmart.entity.Enrollment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class EnrollmentMapper {

    // Convert Entity -> DTO
    public static EnrollmentDTO toDTO(Enrollment enrollment, String quizTitle) {
        if (enrollment == null) return null;

        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setStudentId(enrollment.getStudentId());
        dto.setQuizId(enrollment.getQuizId());
        dto.setCompleted(enrollment.isCompleted());
        dto.setScore(enrollment.getScore());

        // Convert Date -> LocalDateTime
        Date enrolledAt = enrollment.getEnrolledAt();
        if (enrolledAt != null) {
            dto.setEnrolledAt(LocalDateTime.ofInstant(enrolledAt.toInstant(), ZoneId.systemDefault()));
        }

        dto.setQuizTitle(quizTitle); // optional, pass quiz title if available
        return dto;
    }

    // Convert DTO -> Entity
    public static Enrollment toEntity(EnrollmentDTO dto) {
        if (dto == null) return null;

        Enrollment enrollment = new Enrollment();
        enrollment.setId(dto.getId());
        enrollment.setStudentId(dto.getStudentId());
        enrollment.setQuizId(dto.getQuizId());
        enrollment.setCompleted(dto.isCompleted());
        enrollment.setScore(dto.getScore());

        // Convert LocalDateTime -> Date
        LocalDateTime enrolledAt = dto.getEnrolledAt();
        if (enrolledAt != null) {
            enrollment.setEnrolledAt(Date.from(enrolledAt.atZone(ZoneId.systemDefault()).toInstant()));
        }

        return enrollment;
    }
}