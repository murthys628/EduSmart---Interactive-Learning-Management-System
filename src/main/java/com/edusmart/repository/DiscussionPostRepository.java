package com.edusmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edusmart.entity.DiscussionPost;

import java.util.List;

@Repository
public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {
    
    /**
     * Retrieves all posts belonging to a specific thread, ordered by creation time (oldest first).
     * This is crucial for displaying the discussion history when a thread page loads.
     */
    List<DiscussionPost> findAllByThreadIdOrderByCreatedAtAsc(Long threadId);
    
    /**
     * ✅ Counts the total number of posts in a given thread.
     * Used to display the post count in the discussion thread list.
     */
    long countByThreadId(Long threadId);
    
    // Optional: Get the most recent post for a thread (useful for summary)
    // DiscussionPost findTopByThreadIdOrderByCreatedAtDesc(Long threadId);
}