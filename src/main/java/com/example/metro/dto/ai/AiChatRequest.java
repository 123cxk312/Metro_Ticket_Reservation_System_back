package com.example.metro.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AiChatRequest(
        @NotBlank(message = "请输入要咨询的问题")
        @Size(max = 1000, message = "问题内容不能超过 1000 个字符")
        String message,

        List<@Valid AiChatMessage> history
) {

    public AiChatRequest {
        history = history == null ? List.of() : List.copyOf(history);
    }
}
