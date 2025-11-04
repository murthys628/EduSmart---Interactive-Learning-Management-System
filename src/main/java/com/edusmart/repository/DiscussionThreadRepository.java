package com.edusmart.repository;

import com.edusmart.entity.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection; // ⬅️ NEW IMPORT for Collection/Set
import java.util.List;

@Repository
public interface DiscussionThreadRepository extends JpaRepository<DiscussionThread, Long> {
    
	List<DiscussionThread> findAllByOrderByUpdatedAtDesc();
	
    // Custom query method to find all threads belonging to a specific course
    List<DiscussionThread> findAllByCourseIdOrderByUpdatedAtDesc(Long courseId);

    // 🎯 FIX: Method to find all threads belonging to a collection of Course IDs (used for 'Show All Discussions')
    List<DiscussionThread> findAllByCourseIdInOrderByUpdatedAtDesc(Collection<Long> courseIds);
}