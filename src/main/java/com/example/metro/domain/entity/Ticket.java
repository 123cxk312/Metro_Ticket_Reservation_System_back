package com.example.metro.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.metro.domain.enums.TicketDirection;
import com.example.metro.domain.enums.TicketStatus;
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
@TableName("tickets")
public class Ticket {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("ticket_no")
    private String ticketNo;

    @TableField("line_id")
    private Long lineId;

    @TableField("start_station_id")
    private Long startStationId;

    @TableField("end_station_id")
    private Long endStationId;

    private TicketDirection direction;

    @TableField("travel_date")
    private LocalDate travelDate;

    @TableField("departure_time")
    private LocalTime departureTime;

    @TableField("arrival_time")
    private LocalTime arrivalTime;

    private BigDecimal price;

    @TableField("total_stock")
    private Integer totalStock;

    @TableField("remaining_stock")
    private Integer remainingStock;

    @TableField("sale_start_at")
    private LocalDateTime saleStartAt;

    @TableField("sale_end_at")
    private LocalDateTime saleEndAt;

    private TicketStatus status;
    private Integer version;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
