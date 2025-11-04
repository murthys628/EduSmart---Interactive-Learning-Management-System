package com.edusmart.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edusmart.entity.Assignment;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;
import com.edusmart.repository.AssignmentRepository;

import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepo;
    private final EmailNotificationService emailService;

    public AssignmentService(AssignmentRepository assignmentRepo,
                             EmailNotificationService emailService) {
        this.assignmentRepo = assignmentRepo;
        this.emailService = emailService;
    }

    // ===================== Teacher assigns a new assignment =====================
    @Transactional
    @CacheEvict(value = "assignments", allEntries = true)
    public List<Assignment> assignAssignment(Assignment baseAssignment, Course course, User teacher) {
        if (course == null) {
            throw new IllegalArgumentException("Cannot assign an assignment without a valid course.");
        }

        User student = course.getStudent(); // Single student
        if (student == null) {
            return List.of(); // No student enrolled
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(baseAssignment.getTitle());
        assignment.setDescription(baseAssignment.getDescription());
        assignment.setCourse(course);
        assignment.setTeacher(teacher);
        assignment.setStudent(student);

        Assignment saved = assignmentRepo.save(assignment);

        sendNewAssignmentNotification(saved);

        return List.of(saved);
    }

    private void sendNewAssignmentNotification(Assignment assignment) {
        User student = assignment.getStudent();
        if (student == null || student.getEmail() == null) return;

        String subject = "📌 New Assignment: " + assignment.getTitle();
        String htmlBody = "<html><body>"
                + "<h3>Hello " + student.getName() + ",</h3>"
                + "<p>A new assignment <strong>" + assignment.getTitle() + "</strong> has been assigned for <strong>"
                + assignment.getCourse().getTitle() + "</strong> by "
                + assignment.getTeacher().getName() + ".</p>"
                + "<p><b>Description:</b> " + assignment.getDescription() + "</p>"
                + "<p>Please complete it before the deadline.</p>"
                + "<br><p>✅ EduSmart Team</p>"
                + "</body></html>";

        emailService.sendHtmlEmail(student.getEmail(), subject, htmlBody);
    }

    // ===================== Student submits assignment =====================
    @Transactional
    @CacheEvict(value = "assignments", allEntries = true)
    public Assignment submitAssignment(Long assignmentId, User student) {
        Assignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getStudent().equals(student)) {
            throw new IllegalArgumentException("Authorization failure: Assignment does not belong to this student.");
        }

        assignment.setStatus(Assignment.Status.SUBMITTED);
        Assignment saved = assignmentRepo.save(assignment);

        // Notify teacher
        User teacher = assignment.getTeacher();
        if (teacher != null && teacher.getEmail() != null) {
            String subject = "✅ Assignment Submitted: " + assignment.getTitle();
            String htmlBody = "<html><body>"
                    + "<h3>Hello " + teacher.getName() + ",</h3>"
                    + "<p>Student <strong>" + student.getName() + "</strong> has submitted the assignment <strong>"
                    + assignment.getTitle() + "</strong>.</p>"
                    + "<p>Please review it.</p><br><p>✅ EduSmart Team</p>"
                    + "</body></html>";

            emailService.sendHtmlEmail(teacher.getEmail(), subject, htmlBody);
        }

        return saved;
    }

    // ===================== Teacher approves assignment =====================
    @Transactional
    @CacheEvict(value = "assignments", allEntries = true)
    public Assignment approveAssignment(Long assignmentId, User actingUser) {
        Assignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getTeacher().equals(actingUser)) {
            throw new IllegalArgumentException("Authorization failure: Only the assigning teacher can approve this.");
        }

        assignment.setStatus(Assignment.Status.APPROVED);
        Assignment saved = assignmentRepo.save(assignment);

        // Notify student
        User student = assignment.getStudent();
        if (student != null && student.getEmail() != null) {
            String subject = "🎉 Assignment Approved: " + assignment.getTitle();
            String htmlBody = "<html><body>"
                    + "<h3>Congratulations " + student.getName() + "!</h3>"
                    + "<p>Your assignment <strong>" + assignment.getTitle() + "</strong> has been approved by "
                    + assignment.getTeacher().getName() + ".</p>"
                    + "<br><p>✅ EduSmart Team</p>"
                    + "</body></html>";

            emailService.sendHtmlEmail(student.getEmail(), subject, htmlBody);
        }

        return saved;
    }

    // ===================== Teacher grades assignment =====================
    @Transactional
    @CacheEvict(value = "assignments", allEntries = true)
    public Assignment gradeAssignment(Long assignmentId, String grade, String feedback, User actingTeacher) {
        Assignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + assignmentId));

        if (!assignment.getTeacher().equals(actingTeacher)) {
            throw new IllegalArgumentException("Authorization failure: Only the assigning teacher can grade this.");
        }

        assignment.setGrade(grade);
        assignment.setFeedback(feedback);
        assignment.setStatus(Assignment.Status.APPROVED);

        Assignment saved = assignmentRepo.save(assignment);

        User student = assignment.getStudent();
        if (student != null && student.getEmail() != null) {
            String subject = "🔔 Assignment Graded: " + assignment.getTitle();
            String htmlBody = "<html><body>"
                    + "<h3>Hello " + student.getName() + ",</h3>"
                    + "<p>Your assignment <strong>" + assignment.getTitle() + "</strong> for "
                    + assignment.getCourse().getTitle() + " has been graded.</p>"
                    + "<p><b>Grade:</b> <strong>" + (grade != null ? grade : "N/A") + "</strong></p>"
                    + "<p><b>Feedback:</b> " + (feedback != null ? feedback : "No feedback provided.") + "</p>"
                    + "<br><p>✅ EduSmart Team</p>"
                    + "</body></html>";
            emailService.sendHtmlEmail(student.getEmail(), subject, htmlBody);
        }

        return saved;
    }

    // ===================== Cached READ operations =====================

    @Cacheable(value = "assignments", key = "'teacher:' + #teacher.id")
    public List<Assignment> getAssignmentsForTeacher(User teacher) {
        return assignmentRepo.findByTeacher(teacher);
    }

    @Cacheable(value = "assignments", key = "'course:' + #course.id")
    public List<Assignment> getAssignmentsForCourse(Course course) {
        return assignmentRepo.findByCourse(course);
    }

    @Cacheable(value = "assignments", key = "'student:' + #student.id")
    public List<Assignment> getAssignmentsForStudent(User student) {
        return assignmentRepo.findByStudent(student);
    }

    // Alias for student fetch
    @Cacheable(value = "assignments", key = "'student:' + #student.id")
    public List<Assignment> findAssignmentsByStudent(User student) {
        return assignmentRepo.findByStudent(student);
    }
}