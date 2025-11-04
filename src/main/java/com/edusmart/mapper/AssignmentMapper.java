package com.edusmart.mapper;

import com.edusmart.dto.AssignmentResponse;
import com.edusmart.entity.Assignment;

public class AssignmentMapper {

	public static AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),                                                      // id
                assignment.getTitle(),                                                   // title
                assignment.getDescription(),                                             // description
                assignment.getCourse() != null ? assignment.getCourse().getId() : null,  // courseId
                assignment.getCourse() != null ? assignment.getCourse().getTitle() : null, // courseName
                assignment.getStudent() != null ? assignment.getStudent().getId() : null, // studentId
                assignment.getStudent() != null ? assignment.getStudent().getName() : null, // studentName
                assignment.getTeacher() != null ? assignment.getTeacher().getName() : null, // teacherName
                assignment.getStatus() != null ? assignment.getStatus().name() : null,   // status
                assignment.getGrade(),                                                   // grade
                assignment.getFeedback()                                                 // feedback
        );
    }
}