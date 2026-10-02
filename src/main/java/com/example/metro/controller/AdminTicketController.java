package com.example.metro.controller;

import com.example.metro.common.api.ApiResponse;
import com.example.metro.domain.enums.TicketStatus;
import com.example.metro.dto.ticket.CreateTicketRequest;
import com.example.metro.dto.ticket.TicketSearchItem;
import com.example.metro.dto.ticket.UpdateTicketStatusRequest;
import com.example.metro.security.SecurityUtils;
import com.example.metro.service.AdminTicketService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tickets")
public class AdminTicketController {

    private final AdminTicketService adminTicketService;

    public AdminTicketController(AdminTicketService adminTicketService) {
        this.adminTicketService = adminTicketService;
    }

    @PostMapping
    public ApiResponse<TicketSearchItem> createTicket(
            @Valid @RequestBody CreateTicketRequest request
    ) {
        SecurityUtils.requireAdmin();
        Long adminId = SecurityUtils.currentUser().id();
        return ApiResponse.success(adminTicketService.createTicket(adminId, request));
    }

    @GetMapping
    public ApiResponse<List<TicketSearchItem>> listTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String keyword
    ) {
        SecurityUtils.requireAdmin();
        return ApiResponse.success(adminTicketService.listTickets(status, keyword));
    }

    @PutMapping("/{ticketId}/status")
    public ApiResponse<TicketSearchItem> updateStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest request
    ) {
        SecurityUtils.requireAdmin();
        return ApiResponse.success(adminTicketService.updateStatus(ticketId, request.status()));
    }
}
