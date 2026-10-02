package com.example.metro.dto.order;

import com.example.metro.domain.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailItem {

    private Long id;
    private String orderNo;
    private Long userId;
    private Long ticketId;
    private String ticketNo;
    private String lineName;
    private String startStationName;
    private String endStationName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate travelDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime departureTime;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private OrderStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
