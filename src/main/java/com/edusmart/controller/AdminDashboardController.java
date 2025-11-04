package com.edusmart.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.edusmart.dto.CourseDTO;
import com.edusmart.dto.UserDTO;
import com.edusmart.service.CourseService;
import com.edusmart.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
	
	private final UserService userService;
	private final CourseService courseService;
	
	public AdminDashboardController(UserService userService, CourseService courseService) {
        this.userService = userService;
        this.courseService = courseService;
    }

    
	@GetMapping("/dashboard")
	public String showAdminDashboard(Model model) {
	    // ✅ Get logged-in username
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName();

	    // ✅ Get user details
	    UserDTO adminDTO = userService.getUserDTOByUsername(username);

	    // ✅ Pass info to frontend
	    model.addAttribute("userName", adminDTO.getName());
	    model.addAttribute("userEmail", adminDTO.getEmail());
	    model.addAttribute("userPhone", adminDTO.getPhone());

	    return "admin-dashboard";
	}
    
    // This handles the full path: /admin/users
    @GetMapping("/users") 
    public String manageUsers(Model model) {
        
    	// Fetch the list of all users
        List<UserDTO> users = userService.getAllUserDTOs(); 
        
        model.addAttribute("users", users); 
        model.addAttribute("userCount", users.size());
        
        return "admin-users"; 
    }
    
 
    // --------------------------------------------------
    @GetMapping("/courses")
    public String manageCourses(Model model) {
        @SuppressWarnings("unchecked")
        List<CourseDTO> courses = (List<CourseDTO>) courseService.getAllCourseDTOs(); 
        
        model.addAttribute("courses", courses); 
        model.addAttribute("courseCount", courses.size());
        
        return "admin-courses"; 
    }
    
    @GetMapping("/courses/new") // Correct mapping for the new course form: /admin/courses/new
    public String showNewCourseForm(Model model) {
        
        CourseDTO courseDTO = new CourseDTO(); 
        model.addAttribute("course", courseDTO); 
        
        try {
            // Fetch student data for the dropdown
            List<UserDTO> studentList = userService.getAllStudents(); // Assuming this method exists
            model.addAttribute("students", studentList);
        } catch (Exception e) {
            System.err.println("Error fetching students for course form: " + e.getMessage());
            model.addAttribute("error", "Failed to load student selection list.");
            model.addAttribute("students", List.of()); 
        }
        
        // The form template is expected to be 'course_form' based on existing logic
        return "course_form"; 
    }
    
    /**
     * Handles the GET request to display the form for editing an existing course.
     * Maps to: /admin/courses/edit/{id}
     */
    @GetMapping("/courses/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model) {
        try {
            // 1. Fetch the existing CourseDTO by ID (must be implemented in CourseService)
            CourseDTO course = courseService.getCourseDTOById(id); 
            
            // 2. Add the DTO to the model to pre-fill the form
            model.addAttribute("course", course);
            
            // 3. Fetch student data for the dropdown (reuse logic from showNewCourseForm)
            List<UserDTO> studentList = userService.getAllStudents(); 
            model.addAttribute("students", studentList);
            
            // The form template is expected to be 'course_form'
            return "course_form"; 
        } catch (Exception e) {
            System.err.println("Error loading course for edit: " + e.getMessage());
            // Handle course not found or other errors
            model.addAttribute("errorMessage", "Course not found or error loading: " + e.getMessage());
            // Redirect back to the manage list on error
            return "redirect:/admin/courses"; 
        }
    }
    
    /**
     * Handles the POST request to save a new course or update an existing course.
     * Maps to: /admin/courses/save
     */
    @PostMapping("/courses/save")
    public String saveCourse(@ModelAttribute("course") CourseDTO courseDTO, 
                             RedirectAttributes redirectAttributes) {
        
        try {
            // The Service layer handles the logic for both new (ID is null) and update (ID exists).
            courseService.saveOrUpdateCourse(courseDTO);
            
            String message = (courseDTO.getId() == null) ? "New course created successfully!" : "Course updated successfully!";
            redirectAttributes.addFlashAttribute("message", message);
            
        } catch (Exception e) {
            // Log the error and add a flash attribute
            System.err.println("Error saving course: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error saving course: " + e.getMessage());
            
            // Redirect back to the list on error
            return "redirect:/admin/courses"; 
        }
        
        return "redirect:/admin/courses"; // Redirect to the course list
    }
    
    // --------------------------------------------------
    // Below are existing User, Search, Report, and Profile methods...
    // --------------------------------------------------
    
    /**
     * Corrected method to handle the GET request for the New User Form: /admin/new
     * It now includes the list of roles required by user_form.html.
     */
    @GetMapping("/users/new") 
    public String showNewUserForm(Model model) {
        
        // 1. Create a new UserDTO object to bind the form fields to
        model.addAttribute("user", new UserDTO()); 
        
        // 🛑 FIX: Add the list of available roles for the dropdown in user_form.html
        // NOTE: Replace List.of(...) with actual data fetched from a service if available.
        List<String> roles = List.of("ADMIN", "TEACHER", "STUDENT");
        model.addAttribute("roles", roles); 
        
        // 2. Return the name of your Thymeleaf template
        return "user_form"; 
    }
    
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        
        // 1. Fetch the existing UserDTO data using the ID
        // You must implement this method in UserService
        UserDTO user = userService.getUserById(id); 
        
        // 2. IMPORTANT: Clear the password field. We don't want to send the hashed
        //    password back to the form, and the field should appear blank for security.
        user.setPassword(null); 
        model.addAttribute("user", user);
        
        // 3. Add roles list (required by user_form.html)
        List<String> roles = List.of("ADMIN", "TEACHER", "STUDENT");
        model.addAttribute("roles", roles);
        
        return "user_form"; // Reuses the same form template
    }
    
    // 2. Handle POST request for saving a New or Edited User: /admin/users/save
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") UserDTO userDTO, 
                           RedirectAttributes redirectAttributes) {
        
        try {
            // The Service layer must handle:
            // a) Hashing the password if it's new or updated.
            // b) Retaining the old password if the field is empty (for edits).
            userService.saveUser(userDTO); 
            
            String message = (userDTO.getId() == null) ? "New user created successfully!" : "User updated successfully!";
            redirectAttributes.addFlashAttribute("message", message);
            
        } catch (Exception e) {
            // Log the error and add a flash attribute
            System.err.println("Error saving user: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error saving user: " + e.getMessage());
            
            // Redirect back to the list or the form if ID is null (new user)
            return "redirect:/admin/users"; 
        }
        
        return "redirect:/admin/users"; // Redirect to the user list
    }


    // 3. Handle GET request for deleting a User: /admin/users/delete/{id}
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        
        try {
            userService.deleteUser(id); 
            redirectAttributes.addFlashAttribute("message", "User ID " + id + " deleted successfully!");
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }

        return "redirect:/admin/users"; // Redirect back to the user list
    }
    
    @GetMapping("/search")
    public String searchContent(@RequestParam("query") String searchQuery, Model model) {
        
        // 1. Log or print the search query
        System.out.println("Processing course search for query: " + searchQuery);
        
        // 2. Perform the search logic (CourseService ONLY)
        // You MUST implement this searchCourses method in your CourseService.
        List<CourseDTO> foundCourses = courseService.searchCourses(searchQuery); 
        
        // 3. Add results and the original query to the model
        model.addAttribute("searchQuery", searchQuery);
        // Remove the line for 'users'
        model.addAttribute("courses", foundCourses);
        
        // 4. Return the view to display the search results
        // You still need to create 'admin-search-results.html'
        return "admin-search-results"; 
    }
    
    @GetMapping("/reports")
    public String viewReports(Model model) {
        
        // --- UPDATED LOGIC: Get ALL courses instead of a limited top list ---
        List<CourseDTO> allCourses = courseService.getAllCoursesForReport(); 
        
        // Renaming the model attribute to reflect ALL courses, but if your Thymeleaf
        // template expects "topCourses", keep the original name to avoid template changes.
        model.addAttribute("topCourses", allCourses);
        
        // If you had other report calculations, those would remain here.
        
        return "admin-reports"; 
    }
    
    @GetMapping("/profile")
    public String showAdminProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO adminDTO = userService.getUserDTOByUsername(username);
        model.addAttribute("user", adminDTO);
        return "admin-profile";
    }

    // 2. PROCESS PROFILE UPDATE
    @PostMapping("/profile/update")
    public String updateAdminProfile(@ModelAttribute("user") UserDTO updatedDTO, Model model) {
        userService.updateProfile(updatedDTO);
        model.addAttribute("message", "Profile updated successfully!");
        return "admin-profile";
    }
}