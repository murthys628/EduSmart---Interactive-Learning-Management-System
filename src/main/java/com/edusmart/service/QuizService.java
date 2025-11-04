package com.edusmart.service;

import com.edusmart.dto.QuestionDTO;
import com.edusmart.dto.QuizDTO;
import com.edusmart.entity.Course;
import com.edusmart.entity.Question;
import com.edusmart.entity.Quiz;
import com.edusmart.exception.ResourceNotFoundException;
import com.edusmart.mapper.QuestionMapper;
import com.edusmart.repository.CourseRepository;
import com.edusmart.repository.QuizRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@CacheConfig(cacheNames = {"quizCache"}) // All Redis cache ops use "quizCache"
public class QuizService {

    private final QuizRepository quizRepo;
    private final CourseRepository courseRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizService(QuizRepository quizRepo, CourseRepository courseRepo) {
        this.quizRepo = quizRepo;
        this.courseRepo = courseRepo;
    }

    // ----------------- CREATE OR UPDATE QUIZ -----------------
    @CacheEvict(value = "quizCache", allEntries = true)
    public QuizDTO saveOrUpdateQuiz(Quiz quiz) {
        Quiz savedQuiz = quizRepo.save(quiz);
        return mapToDTO(savedQuiz);
    }

    // ----------------- DELETE QUIZ -----------------
    @CacheEvict(value = "quizCache", key = "#quizId")
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));
        quizRepo.delete(quiz);
    }

    // ----------------- GET QUIZ BY COURSE -----------------
    @Transactional(readOnly = true)
    @Cacheable(value = "quizCache", key = "'course:' + #courseId + ':' + #studentId")
    public QuizDTO getQuizByCourse(Long courseId, Long studentId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (course.getStudent() == null || !course.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Quiz", "courseId", courseId);
        }

        Quiz quiz = course.getQuiz();
        if (quiz == null) {
            throw new ResourceNotFoundException("Quiz", "courseId", courseId);
        }

        return mapToDTO(quiz);
    }

    // ----------------- GET QUIZ BY ID -----------------
    @Transactional(readOnly = true)
    @Cacheable(value = "quizCache", key = "#quizId")
    public QuizDTO getQuizById(Long quizId) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));
        return mapToDTO(quiz);
    }

    // ----------------- GET QUIZZES FOR STUDENT -----------------
    @Transactional(readOnly = true)
    @Cacheable(value = "quizCache", key = "'student:' + #studentId")
    public List<QuizDTO> getQuizzesForStudent(Long studentId) {
        List<Quiz> quizzes = quizRepo.findQuizzesByStudentId(studentId);
        return mapToDTOList(quizzes);
    }

    // ----------------- GET ALL QUIZZES FOR A SPECIFIC COURSE -----------------
    @Transactional(readOnly = true)
    @Cacheable(value = "quizCache", key = "'courseAll:' + #courseId")
    public List<QuizDTO> getAllQuizzesForCourse(Long courseId) {
        List<Quiz> quizzes = quizRepo.findByCourseId(courseId);
        return mapToDTOList(quizzes);
    }

    // ----------------- GET ALL QUIZZES -----------------
    @Transactional(readOnly = true)
    @Cacheable(value = "quizCache", key = "'allQuizzes'")
    public List<QuizDTO> getAllQuizzesWithCourse() {
        List<Quiz> quizzes = quizRepo.findAllWithCourse();
        return mapToDTOList(quizzes);
    }

    // ----------------- GET QUIZZES FOR STUDENT FROM ENROLLMENTS -----------------
    @Transactional(readOnly = true)
    @Cacheable(value = "quizCache", key = "'enrollments:' + #quizIds.hashCode()")
    public List<QuizDTO> getQuizzesForStudentFromEnrollments(List<Long> quizIds) {
        if (quizIds == null || quizIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Quiz> quizzes = quizRepo.findAllById(quizIds);
        return mapToDTOList(quizzes);
    }

    // ----------------- CALCULATE SCORE -----------------
    @Transactional(readOnly = true)
    public double calculateScore(Long quizId, Map<String, String> submittedAnswers) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));

        double totalScore = 0.0;

        for (Question question : quiz.getQuestions()) {
            String studentAnswer = submittedAnswers.get(String.valueOf(question.getId()));
            if (studentAnswer != null && !studentAnswer.trim().isEmpty()) {
                char studentAnswerChar = Character.toUpperCase(studentAnswer.trim().charAt(0));
                char correctChar = Character.toUpperCase(question.getCorrectOption());
                if (studentAnswerChar == correctChar) {
                    totalScore += question.getMarks();
                }
            }
        }

        System.out.println("DEBUG: Calculated score for quiz " + quizId + " = " + totalScore);
        return totalScore;
    }

    // ----------------- ENTITY TO DTO MAPPERS -----------------
    private List<QuizDTO> mapToDTOList(List<Quiz> quizzes) {
        if (quizzes == null || quizzes.isEmpty()) {
            return Collections.emptyList();
        }
        return quizzes.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private QuizDTO mapToDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCourseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null);
        dto.setCourseTitle(quiz.getCourse() != null ? quiz.getCourse().getTitle() : null);
        dto.setTeacherId(quiz.getTeacher() != null ? quiz.getTeacher().getId() : null);
        dto.setTotalMarks(quiz.getTotalMarks());
        dto.setDurationMinutes(quiz.getDurationMinutes());
        dto.setStartDate(quiz.getStartDate());
        dto.setCreatedAt(quiz.getCreatedAt());

        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            List<QuestionDTO> questionDTOs = quiz.getQuestions().stream()
                    .map(QuestionMapper::toDTO)
                    .collect(Collectors.toList());
            dto.setQuestions(questionDTOs);
        }

        return dto;
    }
}