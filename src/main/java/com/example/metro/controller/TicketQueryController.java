package com.example.metro.controller;

import com.example.metro.common.api.ApiResponse;
import com.example.metro.domain.enums.TicketDirection;
import com.example.metro.dto.ticket.LineOptionResponse;
import com.example.metro.dto.ticket.StationOptionResponse;
import com.example.metro.dto.ticket.TicketSearchItem;
import com.example.metro.service.TicketQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketQueryController {

    private final TicketQueryService ticketQueryService;

    public TicketQueryController(TicketQueryService ticketQueryService) {
        this.ticketQueryService = ticketQueryService;
    }

    @GetMapping("/metro/lines")
    public ApiResponse<List<LineOptionResponse>> listLines() {
        return ApiResponse.success(ticketQueryService.listLines());
    }

    @GetMapping("/metro/stations")
    public ApiResponse<List<StationOptionResponse>> listStations() {
        return ApiResponse.success(ticketQueryService.listStations());
    }

    @GetMapping("/tickets")
    public ApiResponse<List<TicketSearchItem>> searchTickets(
            @RequestParam(required = false) Long startStationId,
            @RequestParam(required = false) Long endStationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(required = false) TicketDirection direction
    ) {
        return ApiResponse.success(ticketQueryService.searchTickets(
                startStationId,
                endStationId,
                travelDate,
                direction
        ));
    }
}
