package com.edusmart.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.edusmart.entity.User;
import com.edusmart.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ✅ Used by Spring Security for authentication.
     * This should NOT be cached directly, because caching UserDetails can cause
     * serialization/deserialization problems (especially with Redis).
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔎 Attempting login for username: " + username);
        
        // 🎯 FIX: The 'user' variable MUST be declared and initialized here.
        // It calls the helper method to fetch the User entity from the database.
        User user = loadDomainUserByUsername(username); 

        if (user == null) {
            System.out.println("❌ No user found in DB for username: " + username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        System.out.println("✅ Found user: " + user.getUsername() +
                           " | password=" + user.getPassword() +
                           " | role=" + user.getRole() +
                           " | enabled=" + user.isEnabled());

        // 🎯 FIX: Use the declared 'user' object to get the role name.
        // Get the role name (e.g., STUDENT) and convert to uppercase.
        String roleName = user.getRole().name().toUpperCase(); 

        // Return the CustomUserDetails, using the simple capitalized role name 
        // (e.g., 'STUDENT') as the authority.
        return new CustomUserDetails(user, 
        		List.of(new SimpleGrantedAuthority("ROLE_" + roleName)) // ⬅️ The fix suggested previously
        );
    }

    /**
     * ✅ Helper method to get the full User entity.
     * This is safe to cache because it's a simple JPA object.
     */
    @Cacheable(value = "userCache", key = "#username", unless = "#result == null")
    public User loadDomainUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}