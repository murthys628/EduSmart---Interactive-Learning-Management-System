package com.edusmart.mapper;

import com.edusmart.dto.QuizDTO;
import com.edusmart.entity.Quiz;

public class QuizMapper {

    public static QuizDTO toDTO(Quiz quiz) {
        if (quiz == null) return null;

        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCourseId(quiz.getCourse().getId());
        dto.setTotalMarks(quiz.getTotalMarks());
        dto.setDurationMinutes(quiz.getDurationMinutes());
        dto.setStartDate(quiz.getStartDate());
        return dto;
    }

    public static Quiz toEntity(QuizDTO dto) {
        if (dto == null) return null;

        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTotalMarks(dto.getTotalMarks());
        quiz.setDurationMinutes(dto.getDurationMinutes());
        quiz.setStartDate(dto.getStartDate());
        // quiz.setCourse(...)  // set this in service layer from DB
        return quiz;
    }
}