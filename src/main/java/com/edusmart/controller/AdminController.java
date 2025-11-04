package com.edusmart.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.edusmart.dto.CourseDTO;
import com.edusmart.entity.User;
import com.edusmart.exception.ResourceNotFoundException;
import com.edusmart.repository.UserRepository;
import com.edusmart.service.CourseService;
import com.edusmart.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final CourseService courseService;

    public AdminController(UserService userService, UserRepository userRepo, CourseService courseService) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.courseService = courseService;
    }

    // --------------------------------------------------------------------
    // 👤 PROFILE: Get logged-in admin profile
    // --------------------------------------------------------------------
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getAdminProfile(Authentication authentication) {
        String username = authentication.getName();

        User admin = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "username", username));

        return ResponseEntity.ok(Map.of(
                "id", admin.getId(),
                "name", admin.getName(),
                "username", admin.getUsername(),
                "email", admin.getEmail(),
                "phone", admin.getPhone(),
                "role", admin.getRole().name()
        ));
    }

    // --------------------------------------------------------------------
    // ✏️ PROFILE: Update logged-in admin details
    // --------------------------------------------------------------------
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateAdminProfile(
            @RequestBody Map<String, String> updates,
            Authentication authentication) {

        String username = authentication.getName();

        User admin = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "username", username));

        if (updates.containsKey("name") && !updates.get("name").isBlank()) {
            admin.setName(updates.get("name").trim());
        }
        if (updates.containsKey("email") && !updates.get("email").isBlank()) {
            admin.setEmail(updates.get("email").trim());
        }
        if (updates.containsKey("phone") && !updates.get("phone").isBlank()) {
            admin.setPhone(updates.get("phone").trim());
        }

        userRepo.save(admin);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "id", admin.getId(),
                "name", admin.getName(),
                "email", admin.getEmail(),
                "phone", admin.getPhone()
        ));
    }

    // --------------------------------------------------------------------
    // 📋 USERS: Get all users (Admin overview)
    // --------------------------------------------------------------------
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepo.findAll();

        List<Map<String, Object>> result = users.stream()
                .map(u -> {
                    return Map.<String, Object>of(
                            "id", u.getId(),
                            "name", u.getName() != null ? u.getName() : "",
                            "username", u.getUsername() != null ? u.getUsername() : "",
                            "email", u.getEmail() != null ? u.getEmail() : "",
                            "phone", u.getPhone() != null ? u.getPhone() : "",
                            "role", (u.getRole() != null ? u.getRole().name() : "UNKNOWN")
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // --------------------------------------------------------------------
    // 🎓 COURSES: Get all courses (Admin view)
    // --------------------------------------------------------------------
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourseDTOs();
        return ResponseEntity.ok(courses);
    }

    // --------------------------------------------------------------------
    // 🚮 DELETE USER (Admin can delete any user)
    // --------------------------------------------------------------------
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error deleting user: " + e.getMessage()));
        }
    }
}