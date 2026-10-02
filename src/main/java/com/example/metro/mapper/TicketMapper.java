package com.example.metro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.metro.domain.entity.Ticket;
import com.example.metro.domain.enums.TicketDirection;
import com.example.metro.domain.enums.TicketStatus;
import com.example.metro.dto.ticket.TicketSearchItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

public interface TicketMapper extends BaseMapper<Ticket> {

    List<TicketSearchItem> searchTickets(
            @Param("startStationId") Long startStationId,
            @Param("endStationId") Long endStationId,
            @Param("travelDate") LocalDate travelDate,
            @Param("direction") TicketDirection direction
    );

    List<TicketSearchItem> findAllForAdmin(
            @Param("status") TicketStatus status,
            @Param("keyword") String keyword
    );

    TicketSearchItem findTicketDetail(@Param("ticketId") Long ticketId);

    @Update("""
            UPDATE tickets
            SET remaining_stock = remaining_stock - #{quantity},
                version = version + 1
            WHERE id = #{ticketId}
              AND status = 'ON_SALE'
              AND remaining_stock >= #{quantity}
            """)
    int decreaseStock(
            @Param("ticketId") Long ticketId,
            @Param("quantity") Integer quantity
    );

    @Update("""
            UPDATE tickets
            SET status = 'SOLD_OUT',
                version = version + 1
            WHERE id = #{ticketId}
              AND status = 'ON_SALE'
              AND remaining_stock = 0
            """)
    int markSoldOutIfEmpty(@Param("ticketId") Long ticketId);

    @Update("""
            UPDATE tickets
            SET remaining_stock = LEAST(total_stock, remaining_stock + #{quantity}),
                status = CASE
                    WHEN status = 'SOLD_OUT' AND remaining_stock + #{quantity} > 0
                        THEN 'ON_SALE'
                    ELSE status
                END,
                version = version + 1
            WHERE id = #{ticketId}
            """)
    int increaseStock(
            @Param("ticketId") Long ticketId,
            @Param("quantity") Integer quantity
    );
}
