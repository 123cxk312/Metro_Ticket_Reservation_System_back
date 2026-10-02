package com.example.metro.controller;

import com.example.metro.common.api.ApiResponse;
import com.example.metro.domain.enums.RefundStatus;
import com.example.metro.dto.refund.ProcessRefundRequest;
import com.example.metro.dto.refund.RefundDetailItem;
import com.example.metro.security.SecurityUtils;
import com.example.metro.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/refunds")
public class AdminRefundController {

    private final RefundService refundService;

    public AdminRefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping
    public ApiResponse<List<RefundDetailItem>> listRefunds(
            @RequestParam(required = false) RefundStatus status
    ) {
        SecurityUtils.requireAdmin();
        return ApiResponse.success(refundService.listRefundsForAdmin(status));
    }

    @PutMapping("/{refundId}/approve")
    public ApiResponse<RefundDetailItem> approveRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody(required = false) ProcessRefundRequest request
    ) {
        SecurityUtils.requireAdmin();
        Long adminId = SecurityUtils.currentUser().id();
        return ApiResponse.success(refundService.approveRefund(refundId, adminId, request));
    }

    @PutMapping("/{refundId}/reject")
    public ApiResponse<RefundDetailItem> rejectRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody(required = false) ProcessRefundRequest request
    ) {
        SecurityUtils.requireAdmin();
        Long adminId = SecurityUtils.currentUser().id();
        return ApiResponse.success(refundService.rejectRefund(refundId, adminId, request));
    }
}
