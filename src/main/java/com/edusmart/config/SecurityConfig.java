package com.edusmart.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.edusmart.entity.User;
import com.edusmart.filter.JwtRequestFilter;
import com.edusmart.service.CustomUserDetails;
import com.edusmart.service.CustomUserDetailsService;
import com.edusmart.service.EmailNotificationService;

import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final EmailNotificationService emailService;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtRequestFilter jwtRequestFilter,
                          EmailNotificationService emailService) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.emailService = emailService;
    }

    // ✅ Keep plain password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString(); // plain
            }

            @Override
            public boolean matches(CharSequence rawPassword, String storedPassword) {
                boolean result = rawPassword.toString().equals(storedPassword);
                System.out.println("Matching password: raw=" + rawPassword + " stored=" + storedPassword + " -> " + result);
                return result;
            }
        };
    }

    // ✅ Authentication manager for both form login and JWT
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    // ✅ Security rules
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ✅ Disable CSRF only for websocket and API paths
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/ws/**", "/app/**", "/topic/**", "/api/**")
                .disable()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/users/login", "/users/signup",
                                 "/css/**", "/js/**", "/images/**", "/error").permitAll()
                
                // 🚨 FIX HERE: WebSocket traffic must be authenticated to find the Principal
                .requestMatchers("/ws/**", "/topic/**", "/app/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN") 
                
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/courses/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/teacher/**", "/student/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                .requestMatchers("/dashboard/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                .requestMatchers("/api/v1/threads/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                .requestMatchers("/api/chat/**").hasAnyRole("TEACHER", "STUDENT")
                .requestMatchers("/api/quizzes/**").hasAnyRole("TEACHER", "STUDENT")
                .requestMatchers("/api/enrollments/**").hasRole("STUDENT")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/student/**").hasRole("STUDENT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/users/login")
                .loginProcessingUrl("/perform_login")
                .successHandler(customSuccessHandler(userDetailsService, emailService))
                .failureUrl("/users/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/users/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    } else {
                        response.sendRedirect("/users/login");
                    }
                })
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler(
            CustomUserDetailsService customUserDetailsService,
            EmailNotificationService emailService) {

        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities().stream()
                    // This converts ROLE_STUDENT -> role_student
                    .map(a -> a.getAuthority().toLowerCase())
                    .collect(Collectors.toList());
            
            System.out.println("DEBUG: User authorities list is: " + authorities);

            String email = null;
            String displayName = "User";

            try {
                // ... (User detail fetching logic remains the same) ...
                Object principal = authentication.getPrincipal();

                if (principal instanceof CustomUserDetails customUserDetails) {
                    User userEntity = customUserDetails.getUser();
                    if (userEntity != null) {
                        email = userEntity.getEmail();
                        displayName = userEntity.getName();
                    }
                } else if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                    User userEntity = customUserDetailsService.loadDomainUserByUsername(springUser.getUsername());
                    if (userEntity != null) {
                        email = userEntity.getEmail();
                        displayName = userEntity.getName();
                    }
                }

                if (email != null && email.contains("@")) {
                    try {
                        emailService.sendLoginNotification(email, displayName);
                        System.out.println("✅ Login notification email sent to: " + email);
                    } catch (Exception e) {
                        System.err.println("❌ Failed to send login email: " + e.getMessage());
                    }
                }

             // 🎯 FINAL FIX FOR REDIRECT: Use startsWith or a more robust filter
                // We assume authorities are "role_admin", "role_teacher", etc.
                if (authorities.stream().anyMatch(a -> a.startsWith("role_admin"))) {
                    response.sendRedirect("/admin/dashboard");
                } else if (authorities.stream().anyMatch(a -> a.startsWith("role_teacher"))) {
                    response.sendRedirect("/teacher/dashboard"); // ⬅️ This is the correct redirect for Neha
                } else if (authorities.stream().anyMatch(a -> a.startsWith("role_student"))) {
                    response.sendRedirect("/student/dashboard");
                } else {
                    System.err.println("⚠️ Unknown role for " + displayName + ". Authorities: " + authorities);
                    response.sendRedirect("/users/login?error=true");
                }

            } catch (Exception e) {
                System.err.println("❌ Error in customSuccessHandler: " + e.getMessage());
                e.printStackTrace();
                response.sendRedirect("/users/login?error=true");
            }
        };
    }
}