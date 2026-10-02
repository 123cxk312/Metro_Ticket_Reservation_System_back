package com.example.metro.security;

import com.example.metro.config.JwtProperties;
import com.example.metro.domain.entity.User;
import com.example.metro.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generatesAndParsesToken() {
        JwtService jwtService = new JwtService(new JwtProperties(
                "test-secret-key-for-jwt-service-at-least-32-bytes",
                60_000
        ));

        User user = new User();
        user.setId(10L);
        user.setUsername("test_user");
        user.setRole(UserRole.USER);

        String token = jwtService.generateToken(user);
        CurrentUser currentUser = jwtService.parseToken(token);

        assertThat(currentUser.id()).isEqualTo(10L);
        assertThat(currentUser.username()).isEqualTo("test_user");
        assertThat(currentUser.role()).isEqualTo(UserRole.USER);
    }
}
