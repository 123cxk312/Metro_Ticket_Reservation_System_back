package com.example.metro.controller;

import com.example.metro.common.api.ApiResponse;
import com.example.metro.dto.auth.AuthResultResponse;
import com.example.metro.dto.auth.AuthUserResponse;
import com.example.metro.dto.auth.LoginRequest;
import com.example.metro.dto.auth.RegisterRequest;
import com.example.metro.security.UserContextHolder;
import com.example.metro.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResultResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResultResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserResponse> currentUser() {
        Long userId = UserContextHolder.getRequired().id();
        return ApiResponse.success(authService.findCurrentUser(userId));
    }
}
