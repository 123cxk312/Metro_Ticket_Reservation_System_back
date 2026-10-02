package com.example.metro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.metro.domain.entity.TicketOrder;
import com.example.metro.dto.order.OrderDetailItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TicketOrderMapper extends BaseMapper<TicketOrder> {

    List<OrderDetailItem> findOrdersByUserId(@Param("userId") Long userId);

    OrderDetailItem findOrderDetail(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );
}
