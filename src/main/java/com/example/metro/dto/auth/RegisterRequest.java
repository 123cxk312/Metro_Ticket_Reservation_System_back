package com.example.metro.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "请输入用户名")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]{3,20}$",
                message = "用户名需要 3-20 位字母、数字或下划线"
        )
        String username,

        @NotBlank(message = "请输入密码")
        @Size(min = 6, max = 72, message = "密码长度需要在 6-72 位之间")
        String password,

        @NotBlank(message = "请再次输入密码")
        String confirmPassword,

        @NotBlank(message = "请输入真实姓名")
        @Size(max = 50, message = "真实姓名不能超过 50 个字符")
        String realName,

        @NotBlank(message = "请输入手机号")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的 11 位手机号")
        String phone
) {
}
