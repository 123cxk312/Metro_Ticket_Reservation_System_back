package com.example.metro.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.metro.domain.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("ticket_orders")
public class TicketOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("ticket_id")
    private Long ticketId;

    private Integer quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    private OrderStatus status;
    private Integer version;

    @TableField("cancelled_at")
    private LocalDateTime cancelledAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
