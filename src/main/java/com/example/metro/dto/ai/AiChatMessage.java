package com.example.metro.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record AiChatMessage(
        @NotBlank(message = "消息角色不能为空")
        String role,

        @NotBlank(message = "消息内容不能为空")
        String content
) {
}
