package com.example.metro.service;

import com.example.metro.domain.entity.Ticket;
import com.example.metro.domain.entity.TicketOrder;
import com.example.metro.domain.enums.OrderStatus;
import com.example.metro.dto.order.CreateOrderRequest;
import com.example.metro.dto.order.OrderDetailItem;
import com.example.metro.exception.BusinessException;
import com.example.metro.mapper.TicketMapper;
import com.example.metro.mapper.TicketOrderMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final DateTimeFormatter ORDER_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final TicketMapper ticketMapper;
    private final TicketOrderMapper ticketOrderMapper;

    public OrderService(TicketMapper ticketMapper, TicketOrderMapper ticketOrderMapper) {
        this.ticketMapper = ticketMapper;
        this.ticketOrderMapper = ticketOrderMapper;
    }

    @Transactional
    public OrderDetailItem createOrder(Long userId, CreateOrderRequest request) {
        int affectedRows = ticketMapper.decreaseStock(
                request.ticketId(),
                request.quantity()
        );

        if (affectedRows == 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "当前车票已售罄、库存不足或不在销售状态"
            );
        }

        ticketMapper.markSoldOutIfEmpty(request.ticketId());

        Ticket ticket = ticketMapper.selectById(request.ticketId());

        if (ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "车票不存在");
        }

        BigDecimal totalAmount = ticket.getPrice()
                .multiply(BigDecimal.valueOf(request.quantity()));

        TicketOrder order = new TicketOrder();
        order.setOrderNo(createOrderNo());
        order.setUserId(userId);
        order.setTicketId(request.ticketId());
        order.setQuantity(request.quantity());
        order.setUnitPrice(ticket.getPrice());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.BOOKED);
        order.setVersion(0);
        order.setCreatedAt(LocalDateTime.now());

        ticketOrderMapper.insert(order);

        OrderDetailItem createdOrder = ticketOrderMapper.findOrderDetail(order.getId(), userId);

        if (createdOrder == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "订单创建失败");
        }

        return createdOrder;
    }

    @Transactional(readOnly = true)
    public List<OrderDetailItem> listCurrentUserOrders(Long userId) {
        return ticketOrderMapper.findOrdersByUserId(userId);
    }

    @Transactional(readOnly = true)
    public OrderDetailItem getCurrentUserOrder(Long userId, Long orderId) {
        OrderDetailItem order = ticketOrderMapper.findOrderDetail(orderId, userId);

        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "订单不存在");
        }

        return order;
    }

    private String createOrderNo() {
        String timePart = LocalDateTime.now().format(ORDER_TIME_FORMAT);
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        return "MT" + timePart + randomPart;
    }
}
