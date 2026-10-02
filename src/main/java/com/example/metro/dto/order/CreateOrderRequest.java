package com.example.metro.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull(message = "请选择车票")
        Long ticketId,

        @NotNull(message = "请输入预约数量")
        @Min(value = 1, message = "预约数量至少为 1")
        @Max(value = 5, message = "单次最多预约 5 张")
        Integer quantity
) {
}
