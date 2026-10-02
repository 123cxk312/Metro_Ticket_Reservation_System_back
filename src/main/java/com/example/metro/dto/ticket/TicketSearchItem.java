package com.example.metro.dto.ticket;

import com.example.metro.domain.enums.TicketDirection;
import com.example.metro.domain.enums.TicketStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class TicketSearchItem {

    private Long id;
    private String ticketNo;
    private Long lineId;
    private String lineName;
    private Long startStationId;
    private String startStationName;
    private Long endStationId;
    private String endStationName;
    private TicketDirection direction;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate travelDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime departureTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime arrivalTime;

    private BigDecimal price;
    private Integer totalStock;
    private Integer remainingStock;
    private TicketStatus status;
}
