package com.edusmart.controller;

import com.edusmart.dto.PostCreationDTO;
import com.edusmart.dto.PostEditDTO;
import com.edusmart.entity.User;
import com.edusmart.service.DiscussionPostService;
import com.edusmart.service.UserService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/dashboard/discussions/posts")
public class DiscussionPostDashboardController {

    private final DiscussionPostService postService;
    private final UserService userService;

    public DiscussionPostDashboardController(DiscussionPostService postService,
                                             UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    // ------------------ CREATE POST ------------------
    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute("postCreationDTO") PostCreationDTO dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {

        if (principal == null) return "redirect:/login";

        // ✅ Extra safety: ensure threadId is not null or zero
        if (dto.getThreadId() == null || dto.getThreadId() == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid thread ID while posting your reply.");
            return "redirect:/dashboard/discussions";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.postCreationDTO", result);
            redirectAttributes.addFlashAttribute("postCreationDTO", dto);
            return "redirect:/dashboard/discussions/" + dto.getThreadId();
        }

        try {
            User user = userService.getUserByUsername(principal.getName());
            postService.saveNewPost(dto, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Your reply was posted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to post reply: " + e.getMessage());
        }

        return "redirect:/dashboard/discussions/" + dto.getThreadId();
    }

    // ------------------ DELETE POST ------------------
    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long postId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        try {
            User user = userService.getUserByUsername(principal.getName());
            Long threadId = postService.deletePostById(postId, user.getId(), user.getRole());
            redirectAttributes.addFlashAttribute("successMessage", "Post deleted successfully.");
            return "redirect:/dashboard/discussions/" + threadId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard/discussions";
        }
    }

    // ------------------ EDIT POST (SHOW FORM) ------------------
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long postId,
                               Principal principal,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        try {
            User user = userService.getUserByUsername(principal.getName());
            PostEditDTO postEditDTO = postService.getPostForEdit(postId, user.getId(), user.getRole());

            model.addAttribute("postEditDTO", postEditDTO);
            return "edit-post"; // 👈 This should point to your Thymeleaf edit form

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard/discussions";
        }
    }

    // ------------------ UPDATE POST ------------------
    @PostMapping("/update")
    public String updatePost(@Valid @ModelAttribute("postEditDTO") PostEditDTO dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {

        if (principal == null) return "redirect:/login";

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.postEditDTO", result);
            redirectAttributes.addFlashAttribute("postEditDTO", dto);
            return "redirect:/dashboard/discussions/posts/edit/" + dto.getId();
        }

        try {
            User user = userService.getUserByUsername(principal.getName());
            Long threadId = postService.updatePost(dto, user.getId(), user.getRole());
            redirectAttributes.addFlashAttribute("successMessage", "Post updated successfully!");
            return "redirect:/dashboard/discussions/" + threadId;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard/discussions";
        }
    }
}