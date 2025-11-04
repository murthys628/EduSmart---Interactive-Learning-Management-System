package com.edusmart.controller;

import com.edusmart.dto.PostCreationDTO;
import com.edusmart.dto.PostResponseDTO;
import com.edusmart.service.DiscussionPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discussion/posts")
public class DiscussionPostController {

    private final DiscussionPostService postService;

    public DiscussionPostController(DiscussionPostService postService) {
        this.postService = postService;
    }

    /**
     * ✅ POST /api/v1/discussion/posts
     * Creates a new discussion post inside a thread.
     * Example JSON:
     * {
     *   "threadId": 1,
     *   "content": "This course is really helpful for practical tasks!"
     * }
     */
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestBody PostCreationDTO dto,
            @AuthenticationPrincipal Long userId) {

        // Temporary fallback until JWT-based user extraction is wired in
        Long authorId = (userId != null) ? userId : 7L; // e.g., userId=7

        PostResponseDTO createdPost = postService.saveNewPost(dto, authorId);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    /**
     * ✅ GET /api/v1/discussion/posts/thread/{threadId}
     * Fetches all posts under a specific thread.
     */
    @GetMapping("/thread/{threadId}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByThread(@PathVariable Long threadId) {
        List<PostResponseDTO> posts = postService.getPostsByThread(threadId);
        return ResponseEntity.ok(posts);
    }
}