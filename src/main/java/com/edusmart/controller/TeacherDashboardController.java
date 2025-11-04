package com.edusmart.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.edusmart.dto.*;
import com.edusmart.entity.*;
import com.edusmart.service.*;

@Controller
@RequestMapping("/teacher")
public class TeacherDashboardController {

    private final UserService userService;
    private final CourseService courseService;
    private final QuizService quizService;
    private final EnrollmentService enrollmentService;

    public TeacherDashboardController(UserService userService,
                                      CourseService courseService,
                                      QuizService quizService,
                                      EnrollmentService enrollmentService) {
        this.userService = userService;
        this.courseService = courseService;
        this.quizService = quizService;
        this.enrollmentService = enrollmentService;
    }

    // ---------------- Helper ----------------
    private User getAuthenticatedTeacher(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found: " + authentication.getName()));
    }

    // ---------------- Dashboard ----------------
    @GetMapping("/dashboard")
    public String showDashboard(Model model, Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<CourseDTO> courses = courseService.findCoursesByTeacherDTO(teacher);

        // ✅ Add teacher info for display
        model.addAttribute("teacherName", teacher.getName());
        model.addAttribute("teacherEmail", teacher.getEmail());
        model.addAttribute("teacherPhone", teacher.getPhone());

        // ✅ Count total courses
        model.addAttribute("courseCount", courses.size());

        // ✅ Count unique students
        long studentCount = courses.stream()
                .map(CourseDTO::getStudentId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        model.addAttribute("studentCount", studentCount);

        // ✅ Count assignments
        long assignmentCount = courses.stream()
                .flatMap(c -> Optional.ofNullable(c.getAssignments()).orElse(Collections.emptyList()).stream())
                .count();
        model.addAttribute("assignmentCount", assignmentCount);

        // ✅ Count graded assignments
        long gradedCount = courses.stream()
                .flatMap(c -> Optional.ofNullable(c.getAssignments()).orElse(Collections.emptyList()).stream())
                .filter(a -> a.getGrade() != null)
                .count();
        model.addAttribute("gradesCount", gradedCount);

        // ✅ Count quizzes
        long quizCount = courses.stream()
                .map(CourseDTO::getId)
                .mapToLong(courseId -> quizService.getAllQuizzesForCourse(courseId).size())
                .sum();
        model.addAttribute("quizCount", quizCount);

        // ✅ Count total enrollments
        long enrollmentCount = courses.stream()
                .map(CourseDTO::getId)
                .flatMap(id -> quizService.getAllQuizzesForCourse(id).stream())
                .mapToLong(q -> enrollmentService.getEnrollmentsByQuiz(q.getId()).size())
                .sum();
        model.addAttribute("enrollmentCount", enrollmentCount);

        return "teacherdashboard";
    }

    // ---------------- Courses ----------------
    @GetMapping("/courses")
    public String showMyCourses(Model model, Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);

        List<CourseDTO> courses = courseService.findCoursesByTeacherDTO(teacher);

        List<CourseResponse> courseResponses = courses.stream()
                .map(c -> new CourseResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getDescription(),
                        teacher.getName(),
                        Optional.ofNullable(c.getStudentId())
                                .map(id -> userService.getUserEntityById(id).getName())
                                .orElse("")
                ))
                .collect(Collectors.toList());

        model.addAttribute("courses", courseResponses);
        model.addAttribute("course", new CourseRequest());
        model.addAttribute("teacherName", teacher.getName());

        return "teacher-course-add";
    }

    @PostMapping("/courses/add")
    public String addCourse(@ModelAttribute("course") CourseRequest courseRequest,
                            Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        courseService.addCourse(courseRequest, teacher);

        // ✅ Clear cache if student was assigned
        if (courseRequest.getStudentUsername() != null && !courseRequest.getStudentUsername().isEmpty()) {
            userService.evictStudentsCache(teacher);
        }

        return "redirect:/teacher/courses";
    }

    // ---------------- Students ----------------
    @GetMapping("/students")
    public String showStudents(Model model, Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<User> students = userService.findStudentsByTeacher(teacher);
        model.addAttribute("students", students);
        model.addAttribute("teacherName", teacher.getName());
        return "teacher-students";
    }

    // ---------------- Assignments ----------------
    @GetMapping("/assignments")
    public String showAssignments(Model model, Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<CourseDTO> courses = courseService.findCoursesByTeacherDTO(teacher);

        List<AssignmentResponse> assignments = courses.stream()
                .flatMap(c -> Optional.ofNullable(c.getAssignments()).orElse(Collections.emptyList()).stream()
                        .map(a -> new AssignmentResponse(
                                a.getId(),
                                a.getTitle(),
                                a.getDescription(),
                                c.getId(),
                                c.getTitle(),
                                a.getStudentId(),
                                Optional.ofNullable(a.getStudentId())
                                        .map(userService::getUserNameById)
                                        .orElse(null),
                                Optional.ofNullable(a.getTeacherId())
                                        .map(userService::getUserNameById)
                                        .orElse(null),
                                Optional.ofNullable(a.getStatus()).orElse("PENDING"),
                                a.getGrade(),
                                a.getFeedback()
                        ))
                ).collect(Collectors.toList());

        model.addAttribute("assignments", assignments);
        model.addAttribute("courses", courses);
        model.addAttribute("assignment", new AssignmentResponse());
        model.addAttribute("teacherName", teacher.getName());

        return "teacher-assignments";
    }

    @PostMapping("/assignments/add")
    public String addAssignment(@ModelAttribute("assignment") AssignmentResponse req,
                                Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        Course course = courseService.getCourseEntityById(req.getCourseId());

        if (course != null && course.getStudent() != null) {
            Assignment assignment = new Assignment();
            assignment.setTitle(req.getTitle());
            assignment.setDescription(req.getDescription());
            assignment.setCourse(course);
            assignment.setTeacher(teacher);
            assignment.setStudent(course.getStudent());
            course.getAssignments().add(assignment);

            courseService.evictAssignmentsCache(course);
            courseService.saveCourse(course);
        }

        return "redirect:/teacher/assignments";
    }

    // ---------------- Quizzes ----------------
    @GetMapping("/quizzes")
    public String showQuizzes(Model model, Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<CourseDTO> courses = courseService.findCoursesByTeacherDTO(teacher);

        List<QuizDTO> quizzes = courses.stream()
                .flatMap(c -> quizService.getAllQuizzesForCourse(c.getId()).stream())
                .collect(Collectors.toList());

        // ✅ Simplify enrollment mapping
        Map<Long, List<EnrollmentDTO>> quizEnrollments = quizzes.stream()
                .collect(Collectors.toMap(
                        QuizDTO::getId,
                        q -> enrollmentService.getEnrollmentsByQuiz(q.getId())
                ));

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("quizEnrollments", quizEnrollments);
        model.addAttribute("teacherName", teacher.getName());

        return "teacher-quizzes";
    }

    // ---------------- Enrollments ----------------
    @GetMapping("/enrollments")
    public String showEnrollments(Model model, Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<CourseDTO> courses = courseService.findCoursesByTeacherDTO(teacher);

        List<QuizDTO> quizzes = courses.stream()
                .flatMap(c -> quizService.getAllQuizzesForCourse(c.getId()).stream())
                .collect(Collectors.toList());

        List<EnrollmentDTO> enrollments = quizzes.stream()
                .flatMap(q -> enrollmentService.getEnrollmentsByQuiz(q.getId()).stream())
                .peek(e -> {
                    if (e.getQuizTitle() == null) {
                        e.setQuizTitle(quizzes.stream()
                                .filter(qz -> qz.getId().equals(e.getQuizId()))
                                .map(QuizDTO::getTitle)
                                .findFirst()
                                .orElse("N/A"));
                    }
                    if (e.getStudentName() == null) {
                        e.setStudentName(userService.getUserNameById(e.getStudentId()));
                    }
                })
                .collect(Collectors.toList());

        model.addAttribute("enrollments", enrollments);
        model.addAttribute("teacherName", teacher.getName());

        return "teacher-enrollments";
    }

    // ---------------- Quiz Creation ----------------
    @GetMapping("/quizzes/add")
    public String showAddQuizForm(Model model, Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        model.addAttribute("courses", courseService.findCoursesByTeacherDTO(teacher));
        model.addAttribute("teacherName", teacher.getName());
        model.addAttribute("quiz", new QuizDTO());
        return "teacher-quiz-add";
    }

    @PostMapping("/quizzes/add")
    public String addQuiz(@ModelAttribute QuizDTO quizDTO,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        User teacher = getAuthenticatedTeacher(authentication);

        if (quizDTO.getCourseId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course must be selected!");
            return "redirect:/teacher/quizzes/add";
        }

        Course course = courseService.getCourseEntityById(quizDTO.getCourseId());
        if (course == null || !course.getTeacher().equals(teacher)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized access!");
            return "redirect:/teacher/quizzes/add";
        }

        quizDTO.setTeacherId(teacher.getId());
        quizService.saveOrUpdateQuiz(quizDTO.toEntity(teacher, course));

        redirectAttributes.addFlashAttribute("successMessage", "Quiz created successfully!");
        return "redirect:/teacher/quizzes";
    }

    // ---------------- Grades ----------------
    @GetMapping("/grades")
    public String showGrades(Model model, Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<CourseDTO> courses = courseService.findCoursesByTeacherDTO(teacher);

        List<AssignmentResponse> gradedAssignments = courses.stream()
                .flatMap(c -> Optional.ofNullable(c.getAssignments())
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(a -> a.getGrade() != null)
                        .map(a -> new AssignmentResponse(
                                a.getId(),
                                a.getTitle(),
                                a.getDescription(),
                                c.getId(),
                                c.getTitle(),
                                a.getStudentId(),
                                userService.getUserNameById(a.getStudentId()),
                                userService.getUserNameById(a.getTeacherId()),
                                Optional.ofNullable(a.getStatus()).orElse("PENDING"),
                                a.getGrade(),
                                a.getFeedback()
                        )))
                .collect(Collectors.toList());

        model.addAttribute("grades", gradedAssignments);
        model.addAttribute("teacherName", teacher.getName());
        return "teacher-grades";
    }

    // ---------------- Profile ----------------
    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        model.addAttribute("teacher", teacher);
        model.addAttribute("teacherName", teacher.getName());
        return "teacher-profile";
    }
}