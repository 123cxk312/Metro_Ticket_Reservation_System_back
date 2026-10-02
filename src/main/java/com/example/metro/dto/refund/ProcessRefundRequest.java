package com.example.metro.dto.refund;

import jakarta.validation.constraints.Size;

public record ProcessRefundRequest(
        @Size(max = 255, message = "处理说明不能超过 255 个字符")
        String remark
) {
}
