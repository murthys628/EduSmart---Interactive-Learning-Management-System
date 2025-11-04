package com.edusmart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.edusmart.dto.ChatResponseDTO;
import com.edusmart.dto.CourseDTO;
import com.edusmart.entity.User;
import com.edusmart.service.ChatService;
import com.edusmart.service.CourseService;
import com.edusmart.service.UserService;
import static com.edusmart.entity.User.Role.*;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dashboard/chat")
public class ChatDashboardController {

    private final ChatService chatService;
    private final UserService userService;
    private final CourseService courseService;

    public ChatDashboardController(ChatService chatService, UserService userService, CourseService courseService) {
        this.chatService = chatService;
        this.userService = userService;
        this.courseService = courseService;
    }

    @GetMapping
    public String viewChatRoom(@RequestParam(value = "courseId", required = false) Long courseId,
                               Model model,
                               Principal principal,
                               HttpServletRequest request) {

        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.getUserByUsername(principal.getName());

        if (courseId == null) {
            // Logic to display Chat Overview page
            List<CourseDTO> chatCourses;

            // ✅ Determine courses based on user role (Teacher vs Student)
            if (user.getRole() != null && user.getRole() == TEACHER) {
                chatCourses = courseService.getCoursesTaughtWithChat(user.getId());
            } else {
                chatCourses = courseService.getEnrolledCoursesWithChat(user.getId());
            }

            // ✅ Add correct role to the model as a STRING for Thymeleaf
            model.addAttribute("currentUserRole", user.getRole() != null ? user.getRole().name() : "UNKNOWN");

            model.addAttribute("pageTitle", "Live Chat Overview");
            model.addAttribute("currentUsername", user.getUsername());
            model.addAttribute("enrolledCourses", chatCourses);
            model.addAttribute("currentUri", request.getRequestURI());

            return "chat-overview";
        }

        // ---------- Chat Room Page ----------
        List<ChatResponseDTO> messageHistory = chatService.getChatHistory(courseId);

        model.addAttribute("courseId", courseId);
        model.addAttribute("messages", messageHistory);
        model.addAttribute("pageTitle", "Live Course Chat (Course " + courseId + ")");
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("currentUsername", user.getUsername());

        // ✅ Make sure Thymeleaf gets a STRING role
        model.addAttribute("currentUserRole", user.getRole() != null ? user.getRole().name() : "UNKNOWN");

        model.addAttribute("websocketTopic", "/topic/course/" + courseId + "/chat");

        return "chatroom";
    }
}