package com.example.metro.dto.ticket;

public record LineOptionResponse(
        Long id,
        String lineCode,
        String lineName,
        String city
) {
}
