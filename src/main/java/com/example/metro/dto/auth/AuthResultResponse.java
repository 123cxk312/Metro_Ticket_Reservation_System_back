package com.example.metro.dto.auth;

public record AuthResultResponse(
        String token,
        AuthUserResponse user
) {
}
