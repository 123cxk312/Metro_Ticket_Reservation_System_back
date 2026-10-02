package com.example.metro.controller;

import com.example.metro.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "metro-ticket-reservation-system",
                "time", OffsetDateTime.now().toString()
        ));
    }
}
