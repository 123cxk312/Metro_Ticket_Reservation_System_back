package com.example.metro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.deepseek")
public record DeepSeekProperties(
        String apiKey,
        String baseUrl,
        String model,
        int maxToolRounds
) {
}
