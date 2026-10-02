package com.example.metro.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.metro.domain.enums.RefundStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("refund_requests")
public class RefundRequest {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("user_id")
    private Long userId;

    private String reason;
    private RefundStatus status;
    private Integer version;

    @TableField("handled_by")
    private Long handledBy;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
