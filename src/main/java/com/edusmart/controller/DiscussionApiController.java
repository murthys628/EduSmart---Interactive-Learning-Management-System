package com.edusmart.controller;

import com.edusmart.dto.PostCreationDTO;
import com.edusmart.dto.PostResponseDTO;
import com.edusmart.entity.User;
import com.edusmart.service.DiscussionPostService;
import com.edusmart.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionApiController {

    private final DiscussionPostService postService;
    private final UserService userService;

    public DiscussionApiController(DiscussionPostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    // =========================================================================
    // ✅ GET: All posts for a thread
    // =========================================================================
    @GetMapping("/{threadId}/posts")
    public ResponseEntity<List<PostResponseDTO>> getPosts(@PathVariable Long threadId) {
        List<PostResponseDTO> posts = postService.getPostsByThread(threadId);
        return ResponseEntity.ok(posts);
    }

    // =========================================================================
    // ✅ POST: Create a new post in a thread
    // =========================================================================
    @PostMapping("/{threadId}/posts")
    public ResponseEntity<?> createPost(@PathVariable Long threadId,
                                        @RequestBody PostCreationDTO dto,
                                        Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized: Please log in to post.");
        }

        try {
            User user = userService.getUserByUsername(principal.getName());
            dto.setThreadId(threadId); // Ensure the DTO links to the correct thread
            PostResponseDTO response = postService.saveNewPost(dto, user.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error creating post: " + e.getMessage());
        }
    }

    // =========================================================================
    // ✅ (Optional) DELETE: Remove a post
    // =========================================================================
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId,
                                        Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            User user = userService.getUserByUsername(principal.getName());
            Long threadId = postService.deletePostById(postId, user.getId(), user.getRole());
            return ResponseEntity.ok("Post deleted successfully from thread ID: " + threadId);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error deleting post: " + e.getMessage());
        }
    }
}