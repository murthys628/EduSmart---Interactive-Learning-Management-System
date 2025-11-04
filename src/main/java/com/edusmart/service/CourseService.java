package com.edusmart.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edusmart.dto.AssignmentDTO;
import com.edusmart.dto.CourseDTO;
import com.edusmart.dto.CourseRequest;
import com.edusmart.entity.Assignment;
import com.edusmart.entity.Course;
import com.edusmart.entity.User;
import com.edusmart.repository.CourseRepository;
import com.edusmart.repository.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    // ----------------- ADD COURSE -----------------
    @Transactional
    @CacheEvict(
        value = {
            "coursesByTeacherDTO",
            "coursesByStudentDTO",
            "allCoursesDTO",
            "courseDTOById",
            "assignmentsByCourseDTO"
        },
        allEntries = true
    )
    public Course addCourse(CourseRequest request, User teacher) {
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setTeacher(teacher);
        // Assuming you have a default setter for chatEnabled:
        // course.setChatEnabled(true); 

        if (request.getStudentUsername() != null && !request.getStudentUsername().isEmpty()) {
            User student = userRepository.findByUsername(request.getStudentUsername())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            course.setStudent(student);
        }

        return courseRepository.save(course);
    }

    // ----------------- GET ALL COURSES DTO (for dropdowns or admin) -----------------
    @Cacheable(value = "allCoursesDTO")
    public List<CourseDTO> getAllCourseDTOs() {
        return courseRepository.findAll()
                .stream()
                .map(this::toCourseDTO)
                .toList();
    }

    // ----------------- GET COURSES BY TEACHER DTO -----------------
    @Cacheable(value = "coursesByTeacherDTO", key = "#teacher.id")
    public List<CourseDTO> findCoursesByTeacherDTO(User teacher) {
        return courseRepository.findByTeacher(teacher)
                .stream()
                .map(this::toCourseDTO)
                .collect(Collectors.toList());
    }
    
    // =========================================================================
    // Existing Method: Get All Course IDs Taught by a Teacher
    // =========================================================================
    @Transactional(readOnly = true)
    @Cacheable(value = "courseIdsByTeacherUsername", key = "#username")
    public Set<Long> getCourseIdsByTeacherUsername(String username) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found with username: " + username));
        
        List<Course> courses = courseRepository.findByTeacher(teacher);
        
        return courses.stream()
                      .map(Course::getId)
                      .collect(Collectors.toSet());
    }
    // =========================================================================

    // =========================================================================
    // Existing Method: Get All Course IDs Enrolled by a Student
    // =========================================================================
    @Transactional(readOnly = true)
    @Cacheable(value = "courseIdsByStudentUsername", key = "#username")
    public Set<Long> getCourseIdsByStudentUsername(String username) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found with username: " + username));
        
        // NOTE: This relies on CourseRepository having a findAllByStudentId(Long id) method.
        return courseRepository.findAllByStudentId(student.getId()).stream()
                      .map(Course::getId)
                      .collect(Collectors.toSet());
    }
    // =========================================================================


    // ----------------- GET COURSES BY STUDENT DTO -----------------
    @Cacheable(value = "coursesByStudentDTO", key = "#student.id")
    public List<CourseDTO> findCoursesByStudentDTO(User student) {
        return courseRepository.findAllByStudentId(student.getId()) 
                .stream()
                .map(this::toCourseDTO)
                .collect(Collectors.toList());
    }
    
    // =========================================================================
    // 🛑 METHOD REMOVED/REPLACED: getEnrolledCoursesForStudent is now split below.
    // =========================================================================
    
    // =========================================================================
    // ✅ NEW FIX: Get Courses Taught by Teacher (for Chat Dashboard)
    // =========================================================================
    @Transactional(readOnly = true)
    @Cacheable(value = "teacherChatCourses", key = "#teacherId")
    public List<CourseDTO> getCoursesTaughtWithChat(Long teacherId) {
        // ASSUMPTION: CourseRepository has a method to find courses by teacher ID.
        // We also assume you might have a property like 'isChatEnabled' on the Course entity.
        // For simplicity, we are fetching all taught courses here.
        
        List<Course> taughtCourses = courseRepository.findAllByTeacherId(teacherId); 
        
        return taughtCourses.stream()
                // You may add a filter here if the Course entity has a boolean chatEnabled field:
                // .filter(Course::isChatEnabled)
                .map(this::toCourseDTO)
                .collect(Collectors.toList());
    }
    // =========================================================================

    // =========================================================================
    // ✅ NEW FIX: Get Courses Enrolled by Student (for Chat Dashboard)
    // =========================================================================
    @Transactional(readOnly = true)
    @Cacheable(value = "studentChatCourses", key = "#studentId")
    public List<CourseDTO> getEnrolledCoursesWithChat(Long studentId) {
        // Fetch courses using the existing method for courses where the student_id matches.
        
        List<Course> enrolledCourses = courseRepository.findAllByStudentId(studentId);
        
        return enrolledCourses.stream()
                // You may add a filter here if the Course entity has a boolean chatEnabled field:
                // .filter(Course::isChatEnabled) 
                .map(this::toCourseDTO)
                .collect(Collectors.toList());
    }
    // =========================================================================


    // ----------------- GET ASSIGNMENTS BY COURSE DTO -----------------
    @Cacheable(value = "assignmentsByCourseDTO", key = "#course.id")
    public List<AssignmentDTO> findAssignmentsByCourseDTO(Course course) {
        if (course.getAssignments() == null) return List.of();
        return course.getAssignments()
                .stream()
                .map(this::toAssignmentDTO)
                .collect(Collectors.toList());
    }

    // ----------------- EVICT ASSIGNMENTS CACHE -----------------
    @CacheEvict(value = "assignmentsByCourseDTO", key = "#course.id")
    public void evictAssignmentsCache(Course course) {}

    // ----------------- GET COURSE ENTITY -----------------
    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    // ----------------- GET COURSE DTO BY ID -----------------
    @Cacheable(value = "courseDTOById", key = "#id")
    public CourseDTO getCourseDTOById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        return toCourseDTO(course);
    }

    // ----------------- SAVE OR UPDATE COURSE -----------------
    @Transactional
    @CacheEvict(
        value = {
            "coursesByTeacherDTO",
            "coursesByStudentDTO",
            "allCoursesDTO",
            "courseDTOById",
            "assignmentsByCourseDTO"
        },
        allEntries = true
    )
    public void saveOrUpdateCourse(CourseDTO courseDTO) {
        Course course;

        if (courseDTO.getId() != null) {
            course = courseRepository.findById(courseDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Update failed: Course not found with ID " + courseDTO.getId()));

            // Update basic fields
            course.setTitle(courseDTO.getTitle());
            course.setDescription(courseDTO.getDescription());
            updateCourseRelationships(course, courseDTO);

        } else {
            // New course
            course = new Course();
            course.setTitle(courseDTO.getTitle());
            course.setDescription(courseDTO.getDescription());
            updateCourseRelationships(course, courseDTO);
        }

        courseRepository.save(course);
    }

    private void updateCourseRelationships(Course course, CourseDTO courseDTO) {
        if (courseDTO.getTeacherId() != null) {
            User teacher = userRepository.findById(courseDTO.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found with ID: " + courseDTO.getTeacherId()));
            course.setTeacher(teacher);
        }

        if (courseDTO.getStudentId() != null) {
            User student = userRepository.findById(courseDTO.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found with ID: " + courseDTO.getStudentId()));
            course.setStudent(student);
        } else {
            course.setStudent(null);
        }
    }

    // ----------------- SAVE COURSE ENTITY -----------------
    @Transactional
    @CacheEvict(value = {"allCoursesDTO", "courseDTOById"}, allEntries = true)
    public void saveCourse(Course course) {
        courseRepository.save(course);
    }

    // ----------------- HELPER METHODS -----------------
    private CourseDTO toCourseDTO(Course course) {
        List<AssignmentDTO> assignments = course.getAssignments() != null
                ? course.getAssignments().stream().map(this::toAssignmentDTO).collect(Collectors.toList())
                : List.of();

        String studentUsername = course.getStudent() != null
                ? course.getStudent().getUsername()
                : null;
        
        String teacherName = course.getTeacher() != null 
                ? course.getTeacher().getName()
                : null;

        return new CourseDTO(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getTeacher() != null ? course.getTeacher().getId() : null,
                teacherName,
                course.getStudent() != null ? course.getStudent().getId() : null,
                studentUsername,
                assignments
        );
    }

    private AssignmentDTO toAssignmentDTO(Assignment assignment) {
        return new AssignmentDTO(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getCourse() != null ? assignment.getCourse().getId() : null,
                assignment.getStudent() != null ? assignment.getStudent().getId() : null,
                assignment.getTeacher() != null ? assignment.getTeacher().getId() : null,
                assignment.getStatus() != null ? assignment.getStatus().name() : "PENDING",
                assignment.getGrade(),
                assignment.getFeedback()
        );
    }

    // ----------------- SEARCH, REPORTS, ANALYTICS -----------------
    @Cacheable(value = "searchCourses", key = "#searchQuery")
    public List<CourseDTO> searchCourses(String searchQuery) {
        List<Course> foundCourses = courseRepository.findBySearchTerm(searchQuery);
        return foundCourses.stream()
                .map(CourseDTO::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "courseCount")
    public long countAllCourses() {
        return courseRepository.count();
    }

    @Cacheable(value = "recentCourses", key = "#limit")
    public List<CourseDTO> getMostRecentCourses(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Course> topCourses = courseRepository.findTopCourses(pageable);
        return topCourses.stream()
                .map(CourseDTO::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "allCoursesForReport")
    public List<CourseDTO> getAllCoursesForReport() {
        List<Course> allCourses = courseRepository.findAllCoursesOrderedByRecent();
        return allCourses.stream()
                .map(CourseDTO::mapToDTO)
                .collect(Collectors.toList());
    }
}