package com.edusmart.controller;

import com.edusmart.dto.ThreadCreationDTO;
import com.edusmart.dto.ThreadResponseDTO;
import com.edusmart.service.DiscussionThreadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/threads")
public class DiscussionThreadController {

    private final DiscussionThreadService threadService;

    public DiscussionThreadController(DiscussionThreadService threadService) {
        this.threadService = threadService;
    }

    // Endpoint to create a new discussion thread
    // The Long userId would typically come from the authenticated user principal
    @PostMapping
    public ResponseEntity<ThreadResponseDTO> createThread(
            @RequestBody ThreadCreationDTO dto,
            @AuthenticationPrincipal Long userId) { 
        
        // Placeholder for user ID until Spring Security integration is complete
        Long authenticatedUserId = (userId != null) ? userId : 1L; 
        
        ThreadResponseDTO newThread = threadService.createNewThread(dto, authenticatedUserId);
        return new ResponseEntity<>(newThread, HttpStatus.CREATED);
    }
    
    // 🎯 NEW METHOD: Endpoint to fully update an existing discussion thread
    // This resolves the 405 Method Not Allowed error for PUT requests.
    @PutMapping("/{threadId}")
    public ResponseEntity<ThreadResponseDTO> updateThread(
            @PathVariable Long threadId,
            @RequestBody ThreadCreationDTO updateDto) {
        
        // NOTE: We assume threadService.updateExistingThread handles the logic 
        // to update the creatorId when it is passed in the DTO for administrative updates.
        ThreadResponseDTO updatedThread = threadService.updateExistingThread(threadId, updateDto); 

        return ResponseEntity.ok(updatedThread);
    }

    // Endpoint to get all threads for a specific course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ThreadResponseDTO>> getThreadsByCourse(@PathVariable Long courseId) {
        List<ThreadResponseDTO> threads = threadService.getThreadsByCourse(courseId);
        return ResponseEntity.ok(threads);
    }

    // Endpoint to get a single thread by ID
    @GetMapping("/{threadId}")
    public ResponseEntity<ThreadResponseDTO> getThreadById(@PathVariable Long threadId) {
        ThreadResponseDTO thread = threadService.getThreadById(threadId);
        return ResponseEntity.ok(thread);
    }
}