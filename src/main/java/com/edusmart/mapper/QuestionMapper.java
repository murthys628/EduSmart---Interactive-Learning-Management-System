package com.edusmart.mapper;

import com.edusmart.dto.QuestionDTO;
import com.edusmart.entity.Question;
import com.edusmart.entity.Quiz;

public class QuestionMapper {

    // Convert DTO -> Entity
    public static Question toEntity(QuestionDTO dto, Quiz quiz) {
        Question question = new Question();
        question.setQuiz(quiz);  // associate with quiz
        question.setQuestionText(dto.getQuestionText());
        question.setOptionA(dto.getOptionA());
        question.setOptionB(dto.getOptionB());
        question.setOptionC(dto.getOptionC());
        question.setOptionD(dto.getOptionD());
        question.setCorrectOption(dto.getCorrectOption());
        question.setMarks(dto.getMarks());

        // No need to set createdAt; Hibernate will auto-generate
        return question;
    }

    // Convert Entity -> DTO
    public static QuestionDTO toDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuizId(question.getQuiz().getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setCorrectOption(question.getCorrectOption());
        dto.setMarks(question.getMarks());
        dto.setCreatedAt(question.getCreatedAt()); // optional: can include createdAt in DTO for display
        return dto;
    }
}