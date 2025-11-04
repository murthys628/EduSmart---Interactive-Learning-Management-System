package com.edusmart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edusmart.dto.PostCreationDTO;
import com.edusmart.dto.PostEditDTO;
import com.edusmart.dto.PostResponseDTO;
import com.edusmart.entity.DiscussionPost;
import com.edusmart.entity.DiscussionThread;
import com.edusmart.entity.User;
import com.edusmart.mapper.DiscussionPostMapper;
import com.edusmart.repository.DiscussionPostRepository;
import com.edusmart.repository.DiscussionThreadRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscussionPostService {

    private final DiscussionPostRepository postRepository;
    private final DiscussionThreadRepository threadRepository;
    private final DiscussionThreadService threadService;
    private final DiscussionPostMapper postMapper;
    private final UserService userService;

    public DiscussionPostService(DiscussionPostRepository postRepository,
                                 DiscussionThreadRepository threadRepository,
                                 DiscussionThreadService threadService,
                                 DiscussionPostMapper postMapper,
                                 UserService userService) {
        this.postRepository = postRepository;
        this.threadRepository = threadRepository;
        this.threadService = threadService;
        this.postMapper = postMapper;
        this.userService = userService;
    }

    // =========================================================================
    // METHOD: Save new post and update thread activity
    // =========================================================================
    @Transactional
    public PostResponseDTO saveNewPost(PostCreationDTO dto, Long authorId) {

        // ✅ Fetch parent thread properly
        DiscussionThread thread = threadRepository.findById(dto.getThreadId())
                .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + dto.getThreadId()));

        // ✅ Create and link post
        DiscussionPost post = new DiscussionPost(dto.getContent(), thread, authorId);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        DiscussionPost savedPost = postRepository.save(post);

        // ✅ Update parent thread last activity
        threadService.updateThreadLastActivity(dto.getThreadId(), LocalDateTime.now());

        return enrichPostDto(savedPost);
    }

    // =========================================================================
    // METHOD: Retrieve all posts for a given thread
    // =========================================================================
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getPostsByThread(Long threadId) {
        return postRepository.findAllByThreadIdOrderByCreatedAtAsc(threadId).stream()
                .map(this::enrichPostDto)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Helper: Enrich DTO with author name
    // =========================================================================
    private PostResponseDTO enrichPostDto(DiscussionPost post) {
        PostResponseDTO dto = postMapper.toDto(post);
        String authorName = userService.findUsernameById(post.getAuthorId());
        dto.setAuthorName(authorName);
        return dto;
    }

    // =========================================================================
    // METHOD: Delete a discussion post
    // =========================================================================
    @Transactional
    public Long deletePostById(Long postId, Long userId, User.Role userRole) {
        DiscussionPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        Long threadId = post.getThread().getId(); // ✅ updated for relation
        boolean isAuthor = post.getAuthorId().equals(userId);
        boolean isAdminOrTeacher = userRole.equals(User.Role.ADMIN) || userRole.equals(User.Role.TEACHER);

        if (isAuthor || isAdminOrTeacher) {
            postRepository.delete(post);
            return threadId;
        } else {
            throw new SecurityException("User is not authorized to delete this post.");
        }
    }

    // =========================================================================
    // METHOD: Retrieve post for editing (to prefill edit form)
    // =========================================================================
    @Transactional(readOnly = true)
    public PostEditDTO getPostForEdit(Long postId, Long userId, User.Role userRole) {
        DiscussionPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        boolean isAuthor = post.getAuthorId().equals(userId);
        boolean isAdminOrTeacher = userRole.equals(User.Role.ADMIN) || userRole.equals(User.Role.TEACHER);

        if (!isAuthor && !isAdminOrTeacher) {
            throw new SecurityException("You are not authorized to edit this post.");
        }

        PostEditDTO dto = new PostEditDTO();
        dto.setId(post.getId());
        dto.setThreadId(post.getThread().getId()); // ✅ fixed for relationship
        dto.setContent(post.getContent());
        return dto;
    }

    // =========================================================================
    // METHOD: Update an existing post
    // =========================================================================
    @Transactional
    public Long updatePost(PostEditDTO dto, Long userId, User.Role userRole) {
        DiscussionPost post = postRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + dto.getId()));

        Long threadId = post.getThread().getId(); // ✅ updated for relationship
        boolean isAuthor = post.getAuthorId().equals(userId);
        boolean isAdminOrTeacher = userRole.equals(User.Role.ADMIN) || userRole.equals(User.Role.TEACHER);

        if (!isAuthor && !isAdminOrTeacher) {
            throw new SecurityException("You are not authorized to edit this post.");
        }

        post.setContent(dto.getContent());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
        threadService.updateThreadLastActivity(threadId, LocalDateTime.now());
        return threadId;
    }
}