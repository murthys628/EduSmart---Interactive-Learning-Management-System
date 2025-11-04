package com.edusmart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.edusmart.dto.AssignmentRequest;
import com.edusmart.dto.AssignmentResponse;
import com.edusmart.dto.CourseRequest;
import com.edusmart.dto.QuizDTO;
import com.edusmart.entity.Assignment;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;
import com.edusmart.mapper.AssignmentMapper;
import com.edusmart.mapper.CourseMapper;
import com.edusmart.repository.CourseRepository;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.AssignmentService;
import com.edusmart.service.CourseService;
import com.edusmart.service.QuizService;
import com.edusmart.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final CourseRepository courseRepo;
    private final CourseService courseService;
    private final UserRepository userRepo;
    private final AssignmentService assignmentService;
    private final QuizService quizService;
    private final UserService userService;

    public TeacherController(CourseRepository courseRepo,
                             UserRepository userRepo,
                             AssignmentService assignmentService,
                             CourseService courseService,
                             QuizService quizService,
                             UserService userService) {
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.quizService = quizService;
        this.userService = userService;
    }

    // ===== Create Course =====
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody CourseRequest request,
                                          Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);

        if (teacher.getRole() != User.Role.TEACHER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: User is not a teacher.");
        }

        Course saved = courseService.addCourse(request, teacher);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CourseMapper.toResponse(saved));
    }

    // ===== Create Assignment =====
    @PostMapping("/assignments")
    public ResponseEntity<?> createAssignment(@RequestBody AssignmentRequest request,
                                              Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);

        Course course = courseRepo.findById(request.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (!course.getTeacher().equals(teacher)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Course is not taught by this teacher.");
        }

        Assignment baseAssignment = new Assignment();
        baseAssignment.setTitle(request.getTitle());
        baseAssignment.setDescription(request.getDescription());

        List<Assignment> savedAssignments = assignmentService.assignAssignment(baseAssignment, course, teacher);

        if (savedAssignments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Assignment created, but no student is assigned to this course.");
        }

        List<AssignmentResponse> responses = savedAssignments.stream()
                .map(AssignmentMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // ===== Grade Assignment =====
    @PostMapping("/assignments/grade")
    public ResponseEntity<?> gradeAssignment(@RequestBody Map<String, Object> request,
                                             Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);

        Long assignmentId = ((Number) request.get("assignmentId")).longValue();
        String grade = (String) request.get("grade");
        String feedback = (String) request.get("feedback");

        try {
            Assignment gradedAssignment = assignmentService.gradeAssignment(assignmentId, grade, feedback, teacher);

            return ResponseEntity.ok(Map.of(
                    "message", "Assignment graded successfully",
                    "assignmentId", gradedAssignment.getId(),
                    "status", gradedAssignment.getStatus().name(),
                    "grade", gradedAssignment.getGrade()
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Assignment not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else if (e.getMessage().contains("Authorization failure")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access Denied: " + e.getMessage());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
    }

    // ===== Update Teacher Profile =====
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates,
                                           Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);

        teacher.setName(updates.getOrDefault("name", teacher.getName()));
        teacher.setEmail(updates.getOrDefault("email", teacher.getEmail()));
        teacher.setPhone(updates.getOrDefault("phone", teacher.getPhone()));

        userService.updateUser(teacher);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "teacherId", teacher.getId(),
                "name", teacher.getName(),
                "email", teacher.getEmail(),
                "phone", teacher.getPhone()
        ));
    }

    // ===== All Assignments with Grades =====
    @GetMapping("/assignments/grades")
    public ResponseEntity<List<AssignmentResponse>> getAllGrades(Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);

        List<AssignmentResponse> grades = courseRepo.findByTeacher(teacher)
                .stream()
                .flatMap(course -> course.getAssignments().stream())
                .map(a -> {
                    AssignmentResponse response = AssignmentMapper.toResponse(a);
                    response.setCourseName(a.getCourse() != null ? a.getCourse().getTitle() : null);
                    response.setStudentName(a.getStudent() != null ? a.getStudent().getName() : null);
                    return response;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(grades);
    }

    // ===== Assignments by Course =====
    @GetMapping("/courses/{courseId}/grades")
    public ResponseEntity<List<AssignmentResponse>> getGradesByCourse(@PathVariable Long courseId,
                                                                      Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (!course.getTeacher().equals(teacher)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this course");
        }

        List<AssignmentResponse> grades = course.getAssignments()
                .stream()
                .map(a -> {
                    AssignmentResponse response = AssignmentMapper.toResponse(a);
                    response.setCourseName(course.getTitle());
                    response.setStudentName(a.getStudent() != null ? a.getStudent().getName() : null);
                    return response;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(grades);
    }

    // ===== Assignments by Student =====
    @GetMapping("/students/{studentId}/grades")
    public ResponseEntity<List<AssignmentResponse>> getGradesByStudent(@PathVariable Long studentId,
                                                                       Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        List<AssignmentResponse> grades = courseRepo.findByTeacher(teacher)
                .stream()
                .flatMap(course -> course.getAssignments().stream())
                .filter(a -> a.getStudent() != null && a.getStudent().equals(student))
                .map(a -> {
                    AssignmentResponse response = AssignmentMapper.toResponse(a);
                    response.setCourseName(a.getCourse() != null ? a.getCourse().getTitle() : null);
                    response.setStudentName(student.getName());
                    return response;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(grades);
    }

    // ===== Quiz CRUD =====
    @PostMapping("/quizzes")
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody QuizDTO quizDTO,
                                              Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        Course course = courseService.getCourseEntityById(quizDTO.getCourseId());

        if (!course.getTeacher().equals(teacher)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot create a quiz for a course you don't own.");
        }

        quizDTO.setTeacherId(teacher.getId());
        QuizDTO savedQuiz = quizService.saveOrUpdateQuiz(quizDTO.toEntity(teacher, course));

        return ResponseEntity.status(HttpStatus.CREATED).body(savedQuiz);
    }

    @PutMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable Long quizId,
                                              @RequestBody QuizDTO quizDTO,
                                              Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        Course course = courseService.getCourseEntityById(quizDTO.getCourseId());

        if (!course.getTeacher().equals(teacher)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot update a quiz for a course you don't own.");
        }

        var quizEntity = quizDTO.toEntity(teacher, course);
        quizEntity.setId(quizId);
        QuizDTO updatedQuiz = quizService.saveOrUpdateQuiz(quizEntity);

        return ResponseEntity.ok(updatedQuiz);
    }

    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long quizId,
                                        Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        quizService.deleteQuiz(quizId);
        return ResponseEntity.ok(Map.of("message", "Quiz deleted successfully"));
    }

    @GetMapping("/courses/{courseId}/quizzes")
    public ResponseEntity<List<QuizDTO>> getQuizzesByCourse(@PathVariable Long courseId,
                                                            Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        validateTeacherCourse(courseId, teacher);

        List<QuizDTO> quizzes = quizService.getAllQuizzesForCourse(courseId);
        return ResponseEntity.ok(quizzes);
    }

    // ===== Helper Methods =====
    private User getAuthenticatedTeacher(Authentication authentication) {
        User teacher = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if (teacher.getRole() != User.Role.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: not a teacher");
        }

        return teacher;
    }

    private Course validateTeacherCourse(Long courseId, User teacher) {
        Course course = courseService.getCourseEntityById(courseId);
        if (!course.getTeacher().equals(teacher)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: this course is not yours");
        }
        return course;
    }
}