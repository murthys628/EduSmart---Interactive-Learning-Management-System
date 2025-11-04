package com.edusmart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Landing page
    @GetMapping({"/", "/home"})
    public String home() {
        return "home"; // returns home.html
    }

    // Login page
    @GetMapping("/users/login")
    public String login() {
        return "login"; // returns login.html
    }

    // Signup page
    @GetMapping("/users/signup")
    public String signup() {
        return "signup"; // returns signup.html
    }
}