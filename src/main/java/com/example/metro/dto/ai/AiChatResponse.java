package com.example.metro.dto.ai;

import com.example.metro.dto.ticket.TicketSearchItem;

import java.util.List;

public record AiChatResponse(
        String answer,
        List<TicketSearchItem> tickets,
        boolean needsClarification
) {

    public AiChatResponse {
        tickets = tickets == null ? List.of() : List.copyOf(tickets);
    }
}
