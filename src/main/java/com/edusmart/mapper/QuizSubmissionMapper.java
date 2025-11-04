package com.edusmart.mapper;

import com.edusmart.dto.QuizSubmissionResponseDTO;
import com.edusmart.entity.QuizSubmission;

import java.util.stream.Collectors;

public class QuizSubmissionMapper {

    // ⚙️ toEntity() is not needed for submission — handled by service
    // Keep it only for admin update scenarios
    public static QuizSubmission toEntityForUpdate(QuizSubmission existing, QuizSubmissionResponseDTO dto) {
        existing.setScore(dto.getScore());
        existing.setStatus(dto.getStatus());
        return existing;
    }

    // ✅ Safe DTO mapping (null-safe, lazy-friendly)
    public static QuizSubmissionResponseDTO toDTO(QuizSubmission entity) {
        QuizSubmissionResponseDTO dto = new QuizSubmissionResponseDTO();
        dto.setId(entity.getId());
        dto.setQuizId(entity.getQuiz() != null ? entity.getQuiz().getId() : null);
        dto.setStudentId(entity.getStudent() != null ? entity.getStudent().getId() : null);
        dto.setScore(entity.getScore());
        dto.setStatus(entity.getStatus());
        dto.setSubmittedAt(entity.getSubmittedAt());

        if (entity.getQuestionSubmissions() != null) {
            dto.setQuestionSubmissions(
                entity.getQuestionSubmissions().stream().map(q -> {
                    QuizSubmissionResponseDTO.QuestionSubmissionDTO qdto =
                            new QuizSubmissionResponseDTO.QuestionSubmissionDTO();
                    qdto.setQuestionId(q.getQuestionId());
                    qdto.setAnswer(q.getAnswer());
                    qdto.setScore(q.getScore());
                    qdto.setTotalMarks(q.getTotalMarks());
                    return qdto;
                }).collect(Collectors.toList())
            );
        }
        return dto;
    }
}