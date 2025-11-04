package com.edusmart.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for clarity on transaction boundaries

import com.edusmart.dto.UserDTO;
import com.edusmart.entity.User;
import com.edusmart.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ----------------- GET USER ENTITY BY ID -----------------
    /**
     * We avoid caching here to prevent JPA proxy deserialization issues.
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        System.out.println("Fetching user from DB with id: " + id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // ----------------- GET USER BY USERNAME -----------------
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ----------------- UPDATE USER ENTITY -----------------
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // ----------------- GET STUDENTS BY TEACHER -----------------
    @Transactional(readOnly = true)
    public List<User> findStudentsByTeacher(User teacher) {
        System.out.println("Fetching students for teacher from DB...");
        return userRepository.findStudentsByTeacher(teacher);
    }

    // ----------------- EVICT STUDENT CACHE -----------------
    @CacheEvict(value = "studentsByTeacher", key = "#teacher.id")
    public void evictStudentsCache(User teacher) {
        // Cache eviction handled automatically
    }

    // ----------------- GET USER NAME BY ID -----------------
    /**
     * Retrieves the user's name (used for display in discussions/UI).
     * Uses caching for high performance.
     */
    @Cacheable(value = "userNameCache", key = "#id")
    @Transactional(readOnly = true)
    public String getUserNameById(Long id) {
        System.out.println("Fetching username from DB for ID: " + id);
        return userRepository.findById(id)
                .map(User::getName)
                .orElse(null);
    }
    
    // ----------------- FIND USERNAME BY ID (DELEGATE METHOD FOR DISCUSSION SERVICE) -----------------
    /**
     * Provides a public accessor for the author's name, throwing an exception if not found.
     * Delegates to the cached method to utilize caching logic.
     */
    @Transactional(readOnly = true)
    public String findUsernameById(Long id) {
        String userName = getUserNameById(id);
        
        if (userName == null) {
            throw new RuntimeException("User (Author) not found with ID: " + id);
        }
        return userName;
    }

    // ----------------- GET ALL USERS AS DTO -----------------
    @Cacheable(value = "allUsersCache")
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUserDTOs() {
        System.out.println("Fetching all users (DTOs) from DB...");
        return userRepository.findAll()
                .stream()
                .map(UserDTO::mapToDTO)
                .collect(Collectors.toList());
    }

    // ----------------- GET ALL STUDENTS -----------------
    @Cacheable(value = "studentListCache")
    @Transactional(readOnly = true)
    public List<UserDTO> getAllStudents() {
        System.out.println("Fetching all students (DTOs) from DB...");
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.STUDENT)
                .map(UserDTO::mapToDTO)
                .collect(Collectors.toList());
    }

    // ----------------- GET USER DTO BY ID -----------------
    @Cacheable(value = "userDTOCache", key = "#id")
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        System.out.println("Fetching user DTO from DB with id: " + id);
        User user = getUserEntityById(id);
        UserDTO dto = UserDTO.mapToDTO(user);
        dto.setPassword(null); // Hide password
        return dto;
    }

    // ----------------- SAVE OR UPDATE USER -----------------
    @CacheEvict(value = {
            "userDTOCache", "allUsersCache", "studentListCache", "userNameCache", "userDTOByUsernameCache"
    }, allEntries = true)
    @Transactional
    public void saveUser(UserDTO dto) {
        if (dto.getId() == null) {
            // --- NEW USER CREATION ---
            if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Password is required for new user creation.");
            }

            User user = new User();
            user.setName(dto.getName());
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword()); // plain text for now (no encoding)
            user.setRole(dto.getRole());
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            // --- EXISTING USER UPDATE ---
            User existingUser = getUserEntityById(dto.getId());
            existingUser.setName(dto.getName());
            existingUser.setEmail(dto.getEmail());
            existingUser.setRole(dto.getRole());

            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                existingUser.setPassword(dto.getPassword()); // plain text for now
            }

            userRepository.save(existingUser);
        }
    }

    // ----------------- DELETE USER -----------------
    @CacheEvict(value = {
            "userDTOCache", "allUsersCache", "studentListCache", "userNameCache", "userDTOByUsernameCache"
    }, allEntries = true)
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete: User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // ----------------- COUNT TEACHERS -----------------
    @Cacheable(value = "teacherCountCache")
    @Transactional(readOnly = true)
    public int countTeachers() {
        System.out.println("Fetching teacher count from DB...");
        return (int) userRepository.countByRole(User.Role.TEACHER);
    }

    // ----------------- COUNT ALL USERS -----------------
    @Cacheable(value = "userCountCache")
    @Transactional(readOnly = true)
    public long countAllUsers() {
        System.out.println("Fetching total user count from DB...");
        return userRepository.count();
    }

    // ----------------- GET USER DTO BY USERNAME -----------------
    @Cacheable(value = "userDTOByUsernameCache", key = "#username")
    @Transactional(readOnly = true)
    public UserDTO getUserDTOByUsername(String username) {
        System.out.println("Fetching user DTO by username from DB...");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        UserDTO dto = UserDTO.mapToDTO(user);
        dto.setPassword(null);
        return dto;
    }

    // ----------------- PROFILE UPDATE -----------------
    @CacheEvict(value = {
            "userDTOCache", "allUsersCache", "studentListCache", "userNameCache",
            "userDTOByUsernameCache"
    }, allEntries = true)
    @Transactional
    public void updateProfile(UserDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("User ID must be provided for update.");
        }

        User existingUser = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getId()));

        existingUser.setName(dto.getName());
        existingUser.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existingUser.setPassword(dto.getPassword()); // plain text, since you’re not encoding
        }

        userRepository.save(existingUser);
    }
    
    // ----------------- GET USER ENTITY BY USERNAME (non-optional helper) -----------------
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
}