package com.edusmart.dto;

import java.util.List;

import com.edusmart.entity.User;

/**
 * Data Transfer Object (DTO) for the User entity.
 * Used to safely transfer user data without exposing the password or lazy-loaded collections.
 */
public class UserDTO {

    private Long id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String password;
    private User.Role role; // Using the enum from the entity
    private boolean enabled;
    
    // Optional: Could include a list of course IDs or names if needed on the UI
    // private List<Long> teachingCourseIds;

    // ===== Constructors =====
    
    public UserDTO() {
    }

    // Comprehensive constructor (excluding collections and password)
    public UserDTO(Long id, String name, String username, String email, String phone, String password, User.Role role, boolean enabled) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    // ===== Getters & Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public User.Role getRole() {
        return role;
    }
    
    public String getPassword() { // <--- FIXES THE 'NotReadablePropertyException'
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // Optional: Include a static utility method to map entity to DTO
    public static UserDTO mapToDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getPhone(),
            user.getPassword(),
            user.getRole(),
            user.isEnabled()
        );
    }
}