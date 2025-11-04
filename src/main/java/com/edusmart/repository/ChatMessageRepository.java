package com.edusmart.repository;

import com.edusmart.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Retrieves the last N messages for a specific course chat room, ordered by time.
     * Used for loading chat history when a user enters the room.
     * (We use findTopN to enforce a manageable history size.)
     */
    List<ChatMessage> findTop50ByCourseIdOrderByTimestampDesc(Long courseId);
    
    // You might also need a method to find messages within a certain time range, if history scrolls infinitely.
}