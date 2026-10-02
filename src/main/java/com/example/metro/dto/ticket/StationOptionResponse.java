package com.example.metro.dto.ticket;

public record StationOptionResponse(
        Long id,
        String stationCode,
        String stationName,
        String city
) {
}
