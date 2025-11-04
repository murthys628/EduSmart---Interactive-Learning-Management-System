package com.edusmart.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.edusmart.dto.AssignmentResponse;
import com.edusmart.dto.EnrollmentDTO;
import com.edusmart.dto.QuizAttemptDTO;
import com.edusmart.dto.QuizDTO;
import com.edusmart.entity.Assignment;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;
import com.edusmart.exception.ResourceNotFoundException;
import com.edusmart.exception.UnauthorizedAccessException;
import com.edusmart.repository.AssignmentRepository;
import com.edusmart.repository.CourseRepository;
import com.edusmart.repository.QuizRepository;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.CustomUserDetails;
import com.edusmart.service.EmailNotificationService;
import com.edusmart.service.EnrollmentService;
import com.edusmart.service.QuizAnswerService;
import com.edusmart.service.QuizAttemptService;
import com.edusmart.service.QuizService;

import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.lang.Collections;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final AssignmentRepository assignmentRepo;
    private final EnrollmentService enrollmentService;
    private final QuizService quizService;
    private final EmailNotificationService emailService;
    private final QuizRepository quizRepo;
    private final QuizAttemptService quizAttemptService;
    private final QuizAnswerService quizAnswerService;

    public StudentDashboardController(
            UserRepository userRepo,
            CourseRepository courseRepo,
            AssignmentRepository assignmentRepo,
            EmailNotificationService emailService,
            EnrollmentService enrollmentService,
            QuizService quizService,
            QuizRepository quizRepo,
            QuizAttemptService quizAttemptService,
            QuizAnswerService quizAnswerService) {

        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.assignmentRepo = assignmentRepo;
        this.emailService = emailService;
        this.enrollmentService = enrollmentService;
        this.quizService = quizService;
        this.quizRepo = quizRepo;
        this.quizAttemptService = quizAttemptService;
        this.quizAnswerService = quizAnswerService;
    }

    // ----------------- DASHBOARD -----------------
    @GetMapping("/dashboard")
    public String showDashboard(Model model, Authentication authentication) {
        // 1️⃣ Get logged-in student
        String username = authentication.getName();
        User student = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", username));

        // ✅ Add student details to the model
        model.addAttribute("studentName", student.getName());
        model.addAttribute("studentEmail", student.getEmail());
        model.addAttribute("studentPhone", student.getPhone());

        // 2️⃣ Get courses the student is enrolled in
        List<Course> courses = courseRepo.findByStudent(student);
        model.addAttribute("courseCount", courses.size());

        // 3️⃣ Count assignments for this student
        long assignmentCount = courses.stream()
                .flatMap(course -> course.getAssignments().stream())
                .filter(a -> a.getStudent() != null && a.getStudent().equals(student))
                .count();
        model.addAttribute("assignmentCount", assignmentCount);

        // 4️⃣ Fetch enrollments for this student
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudent(student.getId());

        // 5️⃣ Extract quizIds from enrollments
        List<Long> quizIds = enrollments.stream()
                .map(EnrollmentDTO::getQuizId)
                .filter(id -> id != null)
                .toList();

        // 6️⃣ Fetch quizzes based on these enrollment quizIds
        List<QuizDTO> quizzes = quizService.getQuizzesForStudentFromEnrollments(quizIds);

        // 🛠 Fix LinkedHashMap → QuizDTO conversion issue
        List<QuizDTO> safeQuizzes = quizzes.stream()
                .map(q -> {
                    if (q instanceof QuizDTO) return q;
                    var map = (java.util.Map<?, ?>) q;
                    QuizDTO dto = new QuizDTO();
                    dto.setId(map.get("id") != null ? ((Number) map.get("id")).longValue() : null);
                    dto.setTitle((String) map.get("title"));
                    dto.setDescription((String) map.get("description"));
                    dto.setCourseTitle((String) map.get("courseTitle"));
                    dto.setCourseId(map.get("courseId") != null ? ((Number) map.get("courseId")).longValue() : null);
                    dto.setTeacherId(map.get("teacherId") != null ? ((Number) map.get("teacherId")).longValue() : null);
                    dto.setTotalMarks(map.get("totalMarks") != null ? ((Number) map.get("totalMarks")).intValue() : 0);
                    dto.setDurationMinutes(map.get("durationMinutes") != null ? ((Number) map.get("durationMinutes")).intValue() : 0);
                    return dto;
                })
                .collect(Collectors.toList());

        // ✅ Use safeQuizzes instead of raw quizzes
        model.addAttribute("quizzes", safeQuizzes);
        model.addAttribute("quizCount", safeQuizzes.size());
        model.addAttribute("enrollmentCount", enrollments.size());

        // 8️⃣ Fetch quiz attempts for this student
        List<QuizAttemptDTO> attempts = quizAttemptService.getAttemptsByStudent(student.getId());
        model.addAttribute("quizAttemptCount", attempts.size());

        // 9️⃣ Fetch total quiz answers for this student
        int quizAnswerCount = quizAnswerService.getAnswersCountByStudent(student.getId());
        model.addAttribute("quizAnswerCount", quizAnswerCount);

        // 🔟 Get first/latest attempt ID for direct linking
        Long firstAttemptId = attempts.isEmpty() ? null : attempts.get(0).getId();
        model.addAttribute("firstAttemptId", firstAttemptId);

        return "student-dashboard";
    }

    // ----------------- MY COURSES -----------------
    @GetMapping("/courses")
    public String showMyCourses(Model model, Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        List<Course> courses = courseRepo.findByStudent(student);
        model.addAttribute("courses", courses);
        model.addAttribute("studentName", student.getName());

        return "student-course";
    }

    // ----------------- COURSE DETAILS -----------------
    @GetMapping("/course/{courseId}")
    public String viewCourse(@PathVariable Long courseId, Model model, Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        model.addAttribute("course", course);
        model.addAttribute("studentName", student.getName());

        return "student-course-details";
    }

    // ----------------- MY ASSIGNMENTS -----------------
    @GetMapping("/assignments")
    public String showAssignments(Model model, Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        List<AssignmentResponse> assignments = courseRepo.findByStudent(student)
                .stream()
                .flatMap(c -> c.getAssignments().stream()
                        .filter(a -> a.getStudent() != null && a.getStudent().equals(student))
                        .map(a -> new AssignmentResponse(
                                a.getId(),
                                a.getTitle(),
                                a.getDescription(),
                                c.getId(),
                                c.getTitle(),
                                a.getStudent() != null ? a.getStudent().getId() : null,
                                a.getStudent() != null ? a.getStudent().getName() : null,
                                a.getTeacher() != null ? a.getTeacher().getName() : null,
                                a.getStatus() != null ? a.getStatus().name() : null,
                                a.getGrade(),
                                a.getFeedback()
                        ))
                )
                .toList();

        model.addAttribute("assignments", assignments);
        model.addAttribute("studentName", student.getName());
        return "student-assignments";
    }

    @GetMapping("/course/{courseId}/assignments")
    public String viewAssignmentsForCourse(@PathVariable Long courseId, Model model, Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        List<AssignmentResponse> assignments = course.getAssignments()
                .stream()
                .filter(a -> a.getStudent() != null && a.getStudent().equals(student))
                .map(a -> new AssignmentResponse(
                        a.getId(),
                        a.getTitle(),
                        a.getDescription(),
                        course.getId(),
                        course.getTitle(),
                        a.getStudent() != null ? a.getStudent().getId() : null,
                        a.getStudent() != null ? a.getStudent().getName() : null,
                        a.getTeacher() != null ? a.getTeacher().getName() : null,
                        a.getStatus() != null ? a.getStatus().name() : null,
                        a.getGrade(),
                        a.getFeedback()
                ))
                .toList();

        model.addAttribute("assignments", assignments);
        model.addAttribute("course", course);
        model.addAttribute("studentName", student.getName());
        return "student-course-assignments";
    }

    // ----------------- PROFILE -----------------
    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        model.addAttribute("student", student);
        return "student-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("student") User updatedStudent,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        if (updatedStudent.getName() != null && !updatedStudent.getName().isEmpty()) {
            student.setName(updatedStudent.getName());
        }
        if (updatedStudent.getEmail() != null && !updatedStudent.getEmail().isEmpty()) {
            student.setEmail(updatedStudent.getEmail());
        }
        if (updatedStudent.getPhone() != null && !updatedStudent.getPhone().isEmpty()) {
            student.setPhone(updatedStudent.getPhone());
        }

        userRepo.save(student);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/student/profile";
    }

    // ----------------- COMPLETE ASSIGNMENT -----------------
    @PostMapping("/assignments/{assignmentId}/complete")
    public String completeAssignment(@PathVariable Long assignmentId,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {

        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        Assignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));

        if (!assignment.getStudent().equals(student)) {
            throw new UnauthorizedAccessException("You are not authorized to complete this assignment");
        }

        assignment.setStatus(Assignment.Status.SUBMITTED);
        assignmentRepo.save(assignment);

        if (assignment.getTeacher() != null && assignment.getTeacher().getEmail() != null) {
            String subject = "Assignment Submitted: " + assignment.getTitle();
            String body = "<h3>Hello " + assignment.getTeacher().getName() + ",</h3>"
                    + "<p>Student <strong>" + student.getName() + "</strong> has submitted the assignment "
                    + "<strong>" + assignment.getTitle() + "</strong>.</p>"
                    + "<p>Please review it on your dashboard.</p>"
                    + "<br><p>✅ EduSmart Team</p>";
            emailService.sendHtmlEmail(assignment.getTeacher().getEmail(), subject, body);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Assignment marked as submitted!");
        return "redirect:/student/course/" + assignment.getCourse().getId() + "/assignments";
    }

    // ----------------- MY GRADES -----------------
    @GetMapping("/grades")
    public String showGrades(Model model, Authentication authentication) {
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        List<Course> courses = courseRepo.findByStudent(student);

        List<AssignmentResponse> grades = courses.stream()
                .flatMap(course -> course.getAssignments().stream()
                        .filter(a -> a.getStudent() != null && a.getStudent().equals(student))
                        .map(a -> {
                            AssignmentResponse r = new AssignmentResponse();
                            r.setId(a.getId());
                            r.setTitle(a.getTitle());
                            r.setDescription(a.getDescription());
                            r.setCourseId(a.getCourse() != null ? a.getCourse().getId() : null);
                            r.setCourseName(course.getTitle());
                            r.setStudentName(a.getStudent() != null ? a.getStudent().getName() : null);
                            r.setTeacherName(a.getTeacher() != null ? a.getTeacher().getName() : null);
                            r.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
                            r.setGrade(a.getGrade());
                            r.setFeedback(a.getFeedback());
                            return r;
                        })
                )
                .collect(Collectors.toList());

        model.addAttribute("grades", grades);
        model.addAttribute("studentName", student.getName());
        return "student-grades";
    }

    // ----------------- ENROLLMENTS -----------------
    @GetMapping("/enrollments")
    public String showEnrollments(Model model, Authentication authentication) {
        // 1️⃣ Get logged-in student
        User student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "username", authentication.getName()));

        // 2️⃣ Fetch enrollments
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudent(student.getId());

        // 3️⃣ Populate quizTitle safely
        enrollments.forEach(e -> {
            if (e.getQuizId() != null) {
                e.setQuizTitle(
                    quizRepo.findById(e.getQuizId())
                            .map(q -> q.getTitle())
                            .orElse("N/A")
                );
            } else {
                e.setQuizTitle("N/A");
            }
        });

        // 4️⃣ Add to model
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("studentName", student.getName());

        return "student-enrollments";
    }
    
    @GetMapping("/quizzes")
    public String listQuizzes(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails.getId();

        // Fetch all quizzes for this student
        List<QuizDTO> quizzes = quizAttemptService.getQuizzesForStudent(studentId);

        // Add to model for Thymeleaf template
        model.addAttribute("quizzes", quizzes);

        return "student_quizzes_list"; // Make sure this file exists in /templates
    }
    
 // ----------------- VIEW QUIZ ATTEMPT ANSWERS -----------------
    @GetMapping("/quiz-answers/attempt/{attemptId}")
    public String viewQuizAnswers(@PathVariable Long attemptId, Model model,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long studentId = userDetails.getId();

        // Fetch quiz attempt for this student
        QuizAttemptDTO attempt = quizAttemptService.getAttemptsByStudent(studentId)
                .stream()
                .filter(a -> a.getId().equals(attemptId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "id", attemptId));

        // Fetch answers for this attempt
        var answers = quizAnswerService.getAnswersByAttemptAndStudent(attemptId, studentId);

        model.addAttribute("attempt", attempt);
        model.addAttribute("answers", answers);
        model.addAttribute("quizId", attempt.getQuizId());

        return "student_quiz_answers"; // HTML template file
    }
}