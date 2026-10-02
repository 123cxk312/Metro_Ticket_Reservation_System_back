package com.example.metro.dto.auth;

import com.example.metro.domain.entity.User;
import com.example.metro.domain.enums.UserRole;

public record AuthUserResponse(
        Long id,
        String username,
        String realName,
        String phone,
        UserRole role
) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getPhone(),
                user.getRole()
        );
    }
}
