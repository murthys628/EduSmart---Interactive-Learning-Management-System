package com.edusmart.mapper;

import org.springframework.stereotype.Component;

import com.edusmart.dto.QuizAnswerDTO;
import com.edusmart.entity.QuizAnswer;

@Component
public class QuizAnswerMapper {

    public QuizAnswerDTO toDTO(QuizAnswer entity) {
        QuizAnswerDTO dto = new QuizAnswerDTO();
        dto.setId(entity.getId());
        dto.setAttemptId(entity.getAttempt().getId());
        dto.setQuestionId(entity.getQuestion().getId());
        dto.setSelectedOption(entity.getSelectedOption());
        dto.setCorrect(entity.isCorrect());
        dto.setAnsweredAt(entity.getAnsweredAt());
        return dto;
    }
    
    public QuizAnswer toEntity(QuizAnswerDTO dto) {
        if (dto == null) return null;

        QuizAnswer entity = new QuizAnswer();
        entity.setId(dto.getId());
        entity.setSelectedOption(dto.getSelectedOption());
        entity.setCorrect(dto.isCorrect());
        entity.setAnsweredAt(dto.getAnsweredAt());
        // Note: attempt and question must be set separately from DB
        return entity;
    }
}