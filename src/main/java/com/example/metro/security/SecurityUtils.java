package com.example.metro.security;

import com.example.metro.domain.enums.UserRole;
import com.example.metro.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CurrentUser currentUser() {
        return UserContextHolder.getRequired();
    }

    public static void requireAdmin() {
        if (currentUser().role() != UserRole.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }
    }
}
