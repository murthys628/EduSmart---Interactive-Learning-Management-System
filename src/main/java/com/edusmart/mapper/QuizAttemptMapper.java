package com.edusmart.mapper;

import com.edusmart.dto.QuizAttemptDTO;
import com.edusmart.entity.QuizAttempt;
import com.edusmart.entity.Quiz;
import com.edusmart.entity.User;
import org.springframework.stereotype.Component;

@Component
public class QuizAttemptMapper {

    public QuizAttemptDTO toDTO(QuizAttempt attempt) {
        QuizAttemptDTO dto = new QuizAttemptDTO();
        dto.setId(attempt.getId());
        dto.setQuizId(attempt.getQuiz() != null ? attempt.getQuiz().getId() : null);
        dto.setQuizTitle(attempt.getQuiz() != null ? attempt.getQuiz().getTitle() : null);
        dto.setStudentId(attempt.getStudent() != null ? attempt.getStudent().getId() : null);
        dto.setStudentName(attempt.getStudent() != null ? attempt.getStudent().getName() : null);
        dto.setScore(attempt.getScore());
        dto.setTotalMarks(attempt.getTotalMarks());
        dto.setCompleted(attempt.isCompleted());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());
        return dto;
    }

    public QuizAttempt toEntity(QuizAttemptDTO dto, Quiz quiz, User student) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);

        if (dto.isCompleted()) {
            attempt.completeAttempt(dto.getScore(), dto.getTotalMarks());
        } else {
            attempt.setScore(dto.getScore());
            attempt.setTotalMarks(dto.getTotalMarks());
        }

        return attempt;
    }
}