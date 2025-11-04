package com.edusmart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.PostCreationDTO;
import com.edusmart.dto.ThreadResponseDTO;
import com.edusmart.entity.User;
import com.edusmart.service.DiscussionPostService;
import com.edusmart.service.DiscussionThreadService;
import com.edusmart.service.UserService;
import static com.edusmart.entity.User.Role.*;
import java.security.Principal;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/dashboard/discussions")
public class DiscussionThreadDashboardController {

    private final DiscussionThreadService threadService;
    private final DiscussionPostService postService;
    private final UserService userService;

    public DiscussionThreadDashboardController(DiscussionThreadService threadService,
                                               DiscussionPostService postService,
                                               UserService userService) {
        this.threadService = threadService;
        this.postService = postService;
        this.userService = userService;
    }

    // --- 1. LIST ALL THREADS (or by Course/Role) ---
    // Maps to: GET /dashboard/discussions or /dashboard/discussions?courseId=X
    @GetMapping
    public String listDiscussions(@RequestParam(value = "courseId", required = false) Long courseId,
                                  Model model,
                                  Principal principal,
                                  HttpServletRequest request) {

        if (principal == null) return "redirect:/login";

        String username = principal.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found: " + username));

        List<ThreadResponseDTO> threads;
        String title;

        if (courseId != null) {
            threads = threadService.getThreadsByCourse(courseId);
            title = "Discussions for Course: " + courseId;
            model.addAttribute("courseId", courseId); // Correctly sets the courseId for the hidden form field
        } else if (currentUser.getRole() == TEACHER || currentUser.getRole() == ADMIN) {
            threads = threadService.getAllThreadsByTeacherUsername(username);
            title = "All Discussions Across Teaching Courses";
        } else if (currentUser.getRole() == STUDENT) {
            threads = threadService.getAllThreadsByStudentUsername(username);
            // Updated title to reflect the general nature of this view
            title = "Discussions for All Your Enrolled Courses";
        } else {
            // Fallback for other roles or general view
            threads = threadService.getAllThreads(); 
            title = "All Discussions";
        }

        model.addAttribute("threads", threads);
        model.addAttribute("pageTitle", title);
        model.addAttribute("currentUsername", username);
        model.addAttribute("currentURI", request.getRequestURI());

        return "threads-list";
    }

    // --- 2. SHOW NEW DISCUSSION FORM (REMOVED: Using Modal on list page) ---
    
    // --- 3. CREATE DISCUSSION (REMOVED: Handled by REST Controller and AJAX) ---

    // --- 4. VIEW SINGLE THREAD + POSTS ---
    // Maps to: GET /dashboard/discussions/{threadId}
    @GetMapping("/{threadId}") 
    public String viewThreadDetail(@PathVariable Long threadId,
                                   Model model,
                                   Principal principal,
                                   HttpServletRequest request) {

        if (principal == null) return "redirect:/login";

        User currentUser = userService.getUserByUsername(principal.getName());
        ThreadResponseDTO thread = threadService.getThreadById(threadId);

        if (thread == null) {
            model.addAttribute("errorMessage", "Thread not found!");
            return "redirect:/dashboard/discussions";
        }

        // Add the thread's courseId to the model for potential use in the detail view
        // This is good practice if you need to link back to the course list
        model.addAttribute("courseId", thread.getCourseId());
        
        model.addAttribute("thread", thread);
        model.addAttribute("pageTitle", thread.getTitle());
        model.addAttribute("currentURI", request.getRequestURI());

        model.addAttribute("posts", postService.getPostsByThread(threadId));

        PostCreationDTO postCreationDTO = new PostCreationDTO();
        postCreationDTO.setThreadId(threadId);
        model.addAttribute("postCreationDTO", postCreationDTO);

        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUserRole", currentUser.getRole().name());

        return "thread-detail";
    }
}