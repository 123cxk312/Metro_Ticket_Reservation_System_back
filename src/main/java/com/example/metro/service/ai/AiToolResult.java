package com.example.metro.service.ai;

import com.example.metro.dto.ticket.TicketSearchItem;

import java.util.List;

public record AiToolResult(
        String content,
        List<TicketSearchItem> tickets
) {

    public AiToolResult {
        tickets = tickets == null ? List.of() : List.copyOf(tickets);
    }
}
