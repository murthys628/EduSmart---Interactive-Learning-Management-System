package com.edusmart.dto;

public class UserResponse {
    private String name;
    private String email;
    private String username;

    public UserResponse(String name, String email, String username) {
        this.name = name;
        this.email = email;
        this.username = username;
    }

    // Getters
	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}
}