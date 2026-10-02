package com.example.metro.dto.ticket;

import com.example.metro.domain.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(
        @NotNull(message = "请选择车票状态")
        TicketStatus status
) {
}
