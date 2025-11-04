package com.edusmart.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    // Keep your sender email configurable
    private static final String FROM_EMAIL = "s99042132@gmail.com"; // can be externalized in application.properties

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Generic method to send an HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content

            mailSender.send(mimeMessage);
            System.out.println("[EmailNotificationService] ✅ Email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("[EmailNotificationService] ❌ Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    /**
     * Notify user after successful login
     */
    public void sendLoginNotification(String to, String name) {
        if (to == null || to.isEmpty()) return;

        String subject = "🔔 EduSmart Login Alert";
        String htmlBody = "<html><body style='font-family:Arial,sans-serif;'>"
                + "<h2>Hello " + name + ",</h2>"
                + "<p>You have successfully logged into <strong>EduSmart</strong>.</p>"
                + "<p>If this wasn't you, please contact support immediately.</p>"
                + "<p><strong>📅 Login Time:</strong> " + LocalDateTime.now() + "</p>"
                + "<br><p>Regards,<br><strong>EduSmart Team</strong></p>"
                + "</body></html>";

        sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Notify student when a new assignment is created
     */
    public void sendAssignmentNotification(String to, String assignmentTitle, String teacherName) {
        if (to == null || to.isEmpty()) return;

        String subject = "📌 New Assignment: " + assignmentTitle;
        String htmlBody = "<html><body style='font-family:Arial,sans-serif;'>"
                + "<h2>Hello Student,</h2>"
                + "<p>A new assignment <strong>" + assignmentTitle + "</strong> has been created by "
                + "<strong>" + teacherName + "</strong>.</p>"
                + "<p>Please check your dashboard and submit it before the deadline.</p>"
                + "<br><p>✅ <strong>EduSmart Team</strong></p>"
                + "</body></html>";

        sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Notify both teacher and student when an assignment is submitted
     */
    public void sendAssignmentSubmissionNotifications(String studentEmail, String teacherEmail,
                                                      String studentName, String teacherName,
                                                      String assignmentTitle) {

        // ✅ Email to Teacher
        if (teacherEmail != null && !teacherEmail.isEmpty()) {
            String subject = "📬 Assignment Submitted: " + assignmentTitle;
            String htmlBody = "<html><body style='font-family:Arial,sans-serif;'>"
                    + "<h2>Hello " + teacherName + ",</h2>"
                    + "<p><strong>" + studentName + "</strong> has submitted the assignment "
                    + "<strong>" + assignmentTitle + "</strong>.</p>"
                    + "<p>Please review it on your <a href='http://localhost:8082/teacher/dashboard'>EduSmart Dashboard</a>.</p>"
                    + "<br><p>Regards,<br><strong>EduSmart Team</strong></p>"
                    + "</body></html>";
            sendHtmlEmail(teacherEmail, subject, htmlBody);
        }

        // ✅ Email to Student
        if (studentEmail != null && !studentEmail.isEmpty()) {
            String subject = "✅ Assignment Submitted Successfully: " + assignmentTitle;
            String htmlBody = "<html><body style='font-family:Arial,sans-serif;'>"
                    + "<h2>Hello " + studentName + ",</h2>"
                    + "<p>Your assignment <strong>" + assignmentTitle + "</strong> has been successfully submitted.</p>"
                    + "<p>Your teacher <strong>" + teacherName + "</strong> will review it soon.</p>"
                    + "<br><p>Good luck!<br><strong>EduSmart Team</strong></p>"
                    + "</body></html>";
            sendHtmlEmail(studentEmail, subject, htmlBody);
        }
    }
}