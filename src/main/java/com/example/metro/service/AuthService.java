package com.example.metro.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.metro.domain.entity.User;
import com.example.metro.domain.enums.UserRole;
import com.example.metro.dto.auth.AuthResultResponse;
import com.example.metro.dto.auth.AuthUserResponse;
import com.example.metro.dto.auth.LoginRequest;
import com.example.metro.dto.auth.RegisterRequest;
import com.example.metro.exception.BusinessException;
import com.example.metro.mapper.UserMapper;
import com.example.metro.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResultResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "两次输入的密码不一致");
        }

        Long usernameCount = userMapper.selectCount(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, request.username())
        );

        if (usernameCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名已经被使用");
        }

        Long phoneCount = userMapper.selectCount(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getPhone, request.phone())
        );

        if (phoneCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "手机号已经被使用");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setRole(UserRole.USER);
        user.setStatus(1);
        user.setVersion(0);

        userMapper.insert(user);

        return new AuthResultResponse(
                jwtService.generateToken(user),
                AuthUserResponse.from(user)
        );
    }

    @Transactional(readOnly = true)
    public AuthResultResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, request.username())
        );

        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "当前账号已经被禁用");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        return new AuthResultResponse(
                jwtService.generateToken(user),
                AuthUserResponse.from(user)
        );
    }

    @Transactional(readOnly = true)
    public AuthUserResponse findCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "当前用户不存在");
        }

        return AuthUserResponse.from(user);
    }
}
