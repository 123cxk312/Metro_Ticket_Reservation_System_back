package com.example.metro.dto.ticket;

import com.example.metro.domain.enums.TicketDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateTicketRequest(
        @NotNull(message = "请选择线路")
        Long lineId,

        @NotNull(message = "请选择出发站")
        Long startStationId,

        @NotNull(message = "请选择到达站")
        Long endStationId,

        @NotNull(message = "请选择行驶方向")
        TicketDirection direction,

        @NotNull(message = "请选择乘车日期")
        LocalDate travelDate,

        @NotNull(message = "请选择发车时间")
        LocalTime departureTime,

        @NotNull(message = "请选择到达时间")
        LocalTime arrivalTime,

        @NotNull(message = "请输入价格")
        @DecimalMin(value = "0.01", message = "价格必须大于 0")
        BigDecimal price,

        @NotNull(message = "请输入总库存")
        @Min(value = 1, message = "总库存至少为 1")
        Integer totalStock
) {
}
