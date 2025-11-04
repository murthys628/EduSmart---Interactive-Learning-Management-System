package com.edusmart.service;

import com.edusmart.dto.QuizAnswerDTO;
import com.edusmart.entity.Question;
import com.edusmart.entity.QuizAnswer;
import com.edusmart.entity.QuizAttempt;
import com.edusmart.mapper.QuizAnswerMapper;
import com.edusmart.repository.QuestionRepository;
import com.edusmart.repository.QuizAnswerRepository;
import com.edusmart.repository.QuizAttemptRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizAnswerService {

    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final QuizAnswerMapper mapper;

    public QuizAnswerService(QuizAnswerRepository quizAnswerRepository,
                             QuizAttemptRepository attemptRepository,
                             QuestionRepository questionRepository,
                             QuizAnswerMapper mapper) {
        this.quizAnswerRepository = quizAnswerRepository;
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
        this.mapper = mapper;
    }

    /**
     * Save a single answer for the authenticated student
     */
    @CacheEvict(value = {"answersByAttempt", "answersByStudentQuiz", "latestAnswers", "answerCount"}, allEntries = true)
    public QuizAnswerDTO saveAnswer(Long studentId, Long attemptId, Long questionId, char selectedOption) {
        // 1️⃣ Validate that the attempt belongs to the student
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("This attempt does not belong to the authenticated student");
        }

        // 2️⃣ Find the question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // 3️⃣ Check if the selected option is correct
        boolean isCorrect = question.getCorrectOption() == selectedOption;

        // 4️⃣ Save the answer
        QuizAnswer answer = new QuizAnswer();
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setCorrect(isCorrect);
        answer.setAnsweredAt(LocalDateTime.now());

        QuizAnswer savedAnswer = quizAnswerRepository.save(answer);

        // 5️⃣ Convert to DTO and return
        return mapper.toDTO(savedAnswer);
    }

    /**
     * Get all answers for a specific attempt by the authenticated student
     */
    @Cacheable(value = "answersByAttempt", key = "#attemptId + '-' + #studentId")
    public List<QuizAnswerDTO> getAnswersByAttemptAndStudent(Long attemptId, Long studentId) {
        return quizAnswerRepository.findByAttemptIdAndAttemptStudentId(attemptId, studentId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all answers for a quiz by the authenticated student
     */
    @Cacheable(value = "answersByStudentQuiz", key = "#studentId + '-' + #quizId")
    public List<QuizAnswerDTO> getAnswersByStudentAndQuiz(Long studentId, Long quizId) {
        return quizAnswerRepository.findByAttemptQuizIdAndAttemptStudentId(quizId, studentId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Count all answers by a student
     */
    @Cacheable(value = "answerCount", key = "#studentId")
    public int getAnswersCountByStudent(Long studentId) {
        return quizAnswerRepository.countByAttemptStudentId(studentId);
    }

    /**
     * Get the latest attempt's answers by a student
     */
    @Cacheable(value = "latestAnswers", key = "#studentId")
    public List<QuizAnswerDTO> getLatestAnswersByStudentId(Long studentId) {
        return attemptRepository.findTopByStudentIdOrderByStartedAtDesc(studentId)
                .map(latestAttempt -> quizAnswerRepository.findByAttemptId(latestAttempt.getId())
                        .stream()
                        .map(mapper::toDTO)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}