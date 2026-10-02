package com.example.metro.security;

import com.example.metro.domain.enums.UserRole;

public record CurrentUser(
        Long id,
        String username,
        UserRole role
) {
}
