package com.example.metro.dto.refund;

import com.example.metro.domain.enums.RefundStatus;
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
public class RefundDetailItem {

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String userName;
    private String reason;
    private RefundStatus status;
    private String lineName;
    private String startStationName;
    private String endStationName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate travelDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime departureTime;

    private BigDecimal totalAmount;
    private Long handledBy;
    private String handledByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handledAt;

    private String handleRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
