package com.edusmart.service;

import com.edusmart.dto.QuestionDTO;
import com.edusmart.entity.Question;
import com.edusmart.entity.Quiz;
import com.edusmart.mapper.QuestionMapper;
import com.edusmart.repository.QuestionRepository;
import com.edusmart.repository.QuizRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    // ✅ Create or Update a question — must evict cache
    @CacheEvict(value = {"question", "questionsByQuiz"}, allEntries = true)
    public QuestionDTO saveQuestion(QuestionDTO dto) {
        Quiz quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        Question question = QuestionMapper.toEntity(dto, quiz);
        Question saved = questionRepository.save(question);
        return QuestionMapper.toDTO(saved);
    }

    // ✅ Get a question by ID — cached
    @Cacheable(value = "question", key = "#id")
    @Transactional(readOnly = true)
    public QuestionDTO getQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return QuestionMapper.toDTO(question);
    }

    // ✅ Get all questions for a quiz — cached
    @Cacheable(value = "questionsByQuiz", key = "#quizId")
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsByQuizId(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return questionRepository.findByQuiz(quiz).stream()
                .map(QuestionMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ Delete a question — must evict cache
    @CacheEvict(value = {"question", "questionsByQuiz"}, allEntries = true)
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}