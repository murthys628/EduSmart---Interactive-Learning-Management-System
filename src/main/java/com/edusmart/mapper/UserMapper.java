package com.edusmart.mapper;

import com.edusmart.dto.UserResponse;
import com.edusmart.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return new UserResponse(
            user.getName(),
            user.getEmail(),
            user.getUsername()
        );
    }
}