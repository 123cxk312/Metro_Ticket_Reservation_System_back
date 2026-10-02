package com.example.metro.dto.refund;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRefundRequest(
        @NotNull(message = "请选择订单")
        Long orderId,

        @NotBlank(message = "请填写退票原因")
        @Size(max = 255, message = "退票原因不能超过 255 个字符")
        String reason
) {
}
