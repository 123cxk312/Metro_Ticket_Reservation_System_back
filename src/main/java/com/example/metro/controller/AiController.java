package com.example.metro.controller;

import com.example.metro.common.api.ApiResponse;
import com.example.metro.dto.ai.AiChatRequest;
import com.example.metro.dto.ai.AiChatResponse;
import com.example.metro.security.SecurityUtils;
import com.example.metro.service.AiChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiChatService aiChatService;

    public AiController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request
    ) {
        var currentUser = SecurityUtils.currentUser();
        return ApiResponse.success(aiChatService.chat(
                currentUser.id(),
                currentUser.role(),
                request
        ));
    }
}
