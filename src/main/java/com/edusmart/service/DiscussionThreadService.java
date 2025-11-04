package com.edusmart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edusmart.dto.DiscussionThreadDTO;
import com.edusmart.dto.ThreadCreationDTO;
import com.edusmart.dto.ThreadResponseDTO;
import com.edusmart.entity.DiscussionPost;
import com.edusmart.entity.DiscussionThread;
import com.edusmart.mapper.DiscussionThreadMapper;
import com.edusmart.repository.DiscussionPostRepository;
import com.edusmart.repository.DiscussionThreadRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DiscussionThreadService {

    private final DiscussionThreadRepository threadRepository;
    private final DiscussionThreadMapper threadMapper;
    private final CourseService courseService;
    private final UserService userService;
    private final DiscussionPostRepository postRepository;

    public DiscussionThreadService(
            DiscussionThreadRepository threadRepository,
            DiscussionThreadMapper threadMapper,
            CourseService courseService,
            UserService userService,
            DiscussionPostRepository postRepository) {
        this.threadRepository = threadRepository;
        this.threadMapper = threadMapper;
        this.courseService = courseService;
        this.userService = userService;
        this.postRepository = postRepository;
    }

    // =========================================================================
    // ✅ Create a new thread (UPDATED LOGIC)
    // =========================================================================
    @Transactional
    public ThreadResponseDTO createNewThread(ThreadCreationDTO dto, Long authenticatedUserId) {
        
        // 1. Create and Save the DiscussionThread entity (Header data)
        DiscussionThread thread = threadMapper.toEntity(dto, authenticatedUserId);
        thread.setCreatedAt(LocalDateTime.now());
        thread.setUpdatedAt(LocalDateTime.now());

        // Save first to get the generated thread ID
        DiscussionThread savedThread = threadRepository.save(thread); 

        // 2. Create and Save the Initial DiscussionPost (Content data)
        DiscussionPost initialPost = new DiscussionPost();
        initialPost.setContent(dto.getDescription()); 
        
        // **FIXED LINE 1:** Use setThread(DiscussionThread) for the ManyToOne relationship
        initialPost.setThread(savedThread); 
        
        // **FIXED LINE 2:** Use setAuthorId(Long) as defined in your DiscussionPost entity
        initialPost.setAuthorId(authenticatedUserId); 
        
        // Timestamps are automatically handled by @PrePersist in DiscussionPost,
        // but setting manually here ensures consistency if hooks are missed/ignored by JPA config
        initialPost.setCreatedAt(LocalDateTime.now()); 
        
        postRepository.save(initialPost); // Save the actual content post

        // 3. Prepare Response
        ThreadResponseDTO response = threadMapper.toDto(savedThread);
        // Set post count to 1 because we just created the first post.
        response.setPostCount(1); 
        
        return response;
    }
    
    // NOTE: This overload assumes DiscussionThreadDTO also carries the 'description' 
    // for the initial post, which is a structural inconsistency but is maintained 
    // to reuse the primary createNewThread method.
    @Transactional
    public ThreadResponseDTO createNewThread(DiscussionThreadDTO dto, Long userId) {
        // Convert from DiscussionThreadDTO → ThreadCreationDTO
        ThreadCreationDTO creationDTO = new ThreadCreationDTO();
        creationDTO.setTitle(dto.getTitle());
        creationDTO.setCourseId(dto.getCourseId());
        // Assume DiscussionThreadDTO has getDescription() to fill out ThreadCreationDTO
        // creationDTO.setDescription(dto.getDescription()); 

        // Reuse existing createNewThread method
        return createNewThread(creationDTO, userId);
    }

    // =========================================================================
    // ✅ Update an existing thread
    // =========================================================================
    @Transactional
    public ThreadResponseDTO updateExistingThread(Long threadId, ThreadCreationDTO dto) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

        thread.setTitle(dto.getTitle());
        thread.setCourseId(dto.getCourseId());
        thread.setUpdatedAt(LocalDateTime.now());

        DiscussionThread updated = threadRepository.save(thread);
        ThreadResponseDTO response = threadMapper.toDto(updated);
        response.setPostCount((int) postRepository.countByThreadId(threadId));
        return response;
    }

    // =========================================================================
    // ✅ Get all threads for a specific course
    // =========================================================================
    @Transactional(readOnly = true)
    public List<ThreadResponseDTO> getThreadsByCourse(Long courseId) {
        return threadRepository.findAllByCourseIdOrderByUpdatedAtDesc(courseId)
                .stream()
                .map(thread -> {
                    ThreadResponseDTO dto = threadMapper.toDto(thread);
                    dto.setPostCount((int) postRepository.countByThreadId(thread.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // ✅ Get all threads by teacher (based on courses they teach)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<ThreadResponseDTO> getAllThreadsByTeacherUsername(String username) {
        Set<Long> courseIds = courseService.getCourseIdsByTeacherUsername(username);

        if (courseIds.isEmpty()) return List.of();

        return threadRepository.findAllByCourseIdInOrderByUpdatedAtDesc(courseIds)
                .stream()
                .map(thread -> {
                    ThreadResponseDTO dto = threadMapper.toDto(thread);
                    dto.setPostCount((int) postRepository.countByThreadId(thread.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // ✅ Get all threads visible to a student (based on enrolled courses)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<ThreadResponseDTO> getAllThreadsByStudentUsername(String username) {
        Set<Long> courseIds = courseService.getCourseIdsByStudentUsername(username);

        if (courseIds.isEmpty()) return List.of();

        return threadRepository.findAllByCourseIdInOrderByUpdatedAtDesc(courseIds)
                .stream()
                .map(thread -> {
                    ThreadResponseDTO dto = threadMapper.toDto(thread);
                    dto.setPostCount((int) postRepository.countByThreadId(thread.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // ✅ Get a single thread by its ID
    // =========================================================================
    @Transactional(readOnly = true)
    public ThreadResponseDTO getThreadById(Long threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

        ThreadResponseDTO dto = threadMapper.toDto(thread);
        dto.setPostCount((int) postRepository.countByThreadId(threadId));
        return dto;
    }

    // =========================================================================
    // ✅ Update last activity timestamp when new posts are added
    // =========================================================================
    @Transactional
    public void updateThreadLastActivity(Long threadId, LocalDateTime time) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

        thread.setUpdatedAt(time);
        threadRepository.save(thread);
    }

    // =========================================================================
    // ✅ Get all threads (admin view)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<ThreadResponseDTO> getAllThreads() {
        List<DiscussionThread> threads = threadRepository.findAllByOrderByUpdatedAtDesc();

        return threads.stream().map(thread -> {
            ThreadResponseDTO dto = threadMapper.toDto(thread);
            long postCount = postRepository.countByThreadId(thread.getId());
            dto.setPostCount((int) postCount);
            return dto;
        }).collect(Collectors.toList());
    }
}