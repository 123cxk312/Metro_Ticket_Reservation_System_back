package com.example.metro.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.metro.domain.entity.RefundRequest;
import com.example.metro.domain.entity.TicketOrder;
import com.example.metro.domain.enums.OrderStatus;
import com.example.metro.domain.enums.RefundStatus;
import com.example.metro.dto.refund.CreateRefundRequest;
import com.example.metro.dto.refund.ProcessRefundRequest;
import com.example.metro.dto.refund.RefundDetailItem;
import com.example.metro.exception.BusinessException;
import com.example.metro.mapper.RefundRequestMapper;
import com.example.metro.mapper.TicketMapper;
import com.example.metro.mapper.TicketOrderMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefundService {

    private final RefundRequestMapper refundRequestMapper;
    private final TicketOrderMapper ticketOrderMapper;
    private final TicketMapper ticketMapper;

    public RefundService(
            RefundRequestMapper refundRequestMapper,
            TicketOrderMapper ticketOrderMapper,
            TicketMapper ticketMapper
    ) {
        this.refundRequestMapper = refundRequestMapper;
        this.ticketOrderMapper = ticketOrderMapper;
        this.ticketMapper = ticketMapper;
    }

    @Transactional
    public RefundDetailItem createRefund(Long userId, CreateRefundRequest request) {
        TicketOrder order = ticketOrderMapper.selectById(request.orderId());

        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "订单不存在");
        }

        if (order.getStatus() != OrderStatus.BOOKED) {
            throw new BusinessException(HttpStatus.CONFLICT, "当前订单不能申请退票");
        }

        Long existingCount = refundRequestMapper.selectCount(
                Wrappers.<RefundRequest>lambdaQuery()
                        .eq(RefundRequest::getOrderId, order.getId())
        );

        if (existingCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "当前订单已经提交过退票申请");
        }

        int orderUpdated = ticketOrderMapper.update(
                null,
                Wrappers.<TicketOrder>lambdaUpdate()
                        .eq(TicketOrder::getId, order.getId())
                        .eq(TicketOrder::getStatus, OrderStatus.BOOKED)
                        .set(TicketOrder::getStatus, OrderStatus.REFUND_PENDING)
                        .set(TicketOrder::getVersion, order.getVersion() + 1)
        );

        if (orderUpdated == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "订单状态已经发生变化");
        }

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderId(order.getId());
        refundRequest.setUserId(userId);
        refundRequest.setReason(request.reason().trim());
        refundRequest.setStatus(RefundStatus.PENDING);
        refundRequest.setVersion(0);
        refundRequest.setCreatedAt(LocalDateTime.now());

        try {
            refundRequestMapper.insert(refundRequest);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "当前订单已经提交过退票申请");
        }

        return getRefundDetail(refundRequest.getId());
    }

    @Transactional(readOnly = true)
    public List<RefundDetailItem> listCurrentUserRefunds(Long userId) {
        return refundRequestMapper.findRefundsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<RefundDetailItem> listRefundsForAdmin(RefundStatus status) {
        return refundRequestMapper.findRefundsForAdmin(status);
    }

    @Transactional
    public RefundDetailItem approveRefund(
            Long refundId,
            Long adminId,
            ProcessRefundRequest request
    ) {
        RefundRequest refundRequest = getPendingRefund(refundId);
        TicketOrder order = getOrderForRefund(refundRequest);
        LocalDateTime now = LocalDateTime.now();
        String remark = normalizeRemark(request);

        int refundUpdated = refundRequestMapper.update(
                null,
                Wrappers.<RefundRequest>lambdaUpdate()
                        .eq(RefundRequest::getId, refundId)
                        .eq(RefundRequest::getStatus, RefundStatus.PENDING)
                        .set(RefundRequest::getStatus, RefundStatus.APPROVED)
                        .set(RefundRequest::getHandledBy, adminId)
                        .set(RefundRequest::getHandledAt, now)
                        .set(RefundRequest::getHandleRemark, remark)
                        .set(RefundRequest::getVersion, refundRequest.getVersion() + 1)
        );

        if (refundUpdated == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "退票申请已经被处理");
        }

        int orderUpdated = ticketOrderMapper.update(
                null,
                Wrappers.<TicketOrder>lambdaUpdate()
                        .eq(TicketOrder::getId, order.getId())
                        .eq(TicketOrder::getStatus, OrderStatus.REFUND_PENDING)
                        .set(TicketOrder::getStatus, OrderStatus.REFUNDED)
                        .set(TicketOrder::getVersion, order.getVersion() + 1)
        );

        if (orderUpdated == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "订单状态已经发生变化");
        }

        ticketMapper.increaseStock(order.getTicketId(), order.getQuantity());
        return getRefundDetail(refundId);
    }

    @Transactional
    public RefundDetailItem rejectRefund(
            Long refundId,
            Long adminId,
            ProcessRefundRequest request
    ) {
        RefundRequest refundRequest = getPendingRefund(refundId);
        TicketOrder order = getOrderForRefund(refundRequest);
        LocalDateTime now = LocalDateTime.now();

        int refundUpdated = refundRequestMapper.update(
                null,
                Wrappers.<RefundRequest>lambdaUpdate()
                        .eq(RefundRequest::getId, refundId)
                        .eq(RefundRequest::getStatus, RefundStatus.PENDING)
                        .set(RefundRequest::getStatus, RefundStatus.REJECTED)
                        .set(RefundRequest::getHandledBy, adminId)
                        .set(RefundRequest::getHandledAt, now)
                        .set(RefundRequest::getHandleRemark, normalizeRemark(request))
                        .set(RefundRequest::getVersion, refundRequest.getVersion() + 1)
        );

        if (refundUpdated == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "退票申请已经被处理");
        }

        int orderUpdated = ticketOrderMapper.update(
                null,
                Wrappers.<TicketOrder>lambdaUpdate()
                        .eq(TicketOrder::getId, order.getId())
                        .eq(TicketOrder::getStatus, OrderStatus.REFUND_PENDING)
                        .set(TicketOrder::getStatus, OrderStatus.BOOKED)
                        .set(TicketOrder::getVersion, order.getVersion() + 1)
        );

        if (orderUpdated == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "订单状态已经发生变化");
        }

        return getRefundDetail(refundId);
    }

    private RefundRequest getPendingRefund(Long refundId) {
        RefundRequest refundRequest = refundRequestMapper.selectById(refundId);

        if (refundRequest == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "退票申请不存在");
        }

        if (refundRequest.getStatus() != RefundStatus.PENDING) {
            throw new BusinessException(HttpStatus.CONFLICT, "退票申请已经被处理");
        }

        return refundRequest;
    }

    private TicketOrder getOrderForRefund(RefundRequest refundRequest) {
        TicketOrder order = ticketOrderMapper.selectById(refundRequest.getOrderId());

        if (order == null || order.getStatus() != OrderStatus.REFUND_PENDING) {
            throw new BusinessException(HttpStatus.CONFLICT, "订单状态不能进行退票处理");
        }

        return order;
    }

    private RefundDetailItem getRefundDetail(Long refundId) {
        RefundDetailItem detail = refundRequestMapper.findRefundDetail(refundId);

        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "退票申请不存在");
        }

        return detail;
    }

    private String normalizeRemark(ProcessRefundRequest request) {
        if (request == null || request.remark() == null || request.remark().isBlank()) {
            return null;
        }

        return request.remark().trim();
    }
}
