package com.edusmart.dto;

import com.edusmart.entity.Course;
import java.util.List;

public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private Long teacherId;
    // 🟢 Field is already here
    private String teacherName;
    private Long studentId;
    private String studentUsername;
    private List<AssignmentDTO> assignments;

    // ===== Constructors =====
    public CourseDTO() {
    }

    /**
     * Constructor including all primary fields and the list of assignments.
     */
    // 🟢 UPDATED: Added teacherName to the constructor
    public CourseDTO(Long id, String title, String description, Long teacherId, String teacherName, Long studentId, String studentUsername, List<AssignmentDTO> assignments) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
        this.teacherName = teacherName; // <-- NEW
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.assignments = assignments;
    }

    /**
     * Constructor including all primary fields but excluding the list of assignments.
     */
    // 🟢 UPDATED: Added teacherName to the constructor
    public CourseDTO(Long id, String title, String description, Long teacherId, String teacherName, Long studentId, String studentUsername) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
        this.teacherName = teacherName; // <-- NEW
        this.studentId = studentId;
        this.studentUsername = studentUsername;
    }

    // ===== Getters & Setters =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
    
    // 🟢 Getter and Setter for teacherName (was missing, now included)
    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public List<AssignmentDTO> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<AssignmentDTO> assignments) {
        this.assignments = assignments;
    }
    
    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }
    
    // 🟢 CRITICALLY UPDATED: mapToDTO now populates teacherName
    public static CourseDTO mapToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        
        // 1. Map simple fields
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        
        // 2. Map Teacher details (assuming Course entity has a getTeacher() method)
        if (course.getTeacher() != null) {
            dto.setTeacherId(course.getTeacher().getId());
            // 🟢 NEW MAPPING: Populating the teacherName field
            dto.setTeacherName(course.getTeacher().getName());
        }
        
        // 3. Map Student details (assuming Course entity has a getStudent() method)
        if (course.getStudent() != null) {
            dto.setStudentId(course.getStudent().getId());
            dto.setStudentUsername(course.getStudent().getUsername()); 
        }
        
        // NOTE: Collections like assignments are usually excluded or lazily loaded 
        // in a simple search DTO mapping.
        
        return dto;
    }
}