package com.example.metro.controller;

import com.example.metro.common.api.ApiResponse;
import com.example.metro.dto.refund.CreateRefundRequest;
import com.example.metro.dto.refund.RefundDetailItem;
import com.example.metro.security.SecurityUtils;
import com.example.metro.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public ApiResponse<RefundDetailItem> createRefund(
            @Valid @RequestBody CreateRefundRequest request
    ) {
        Long userId = SecurityUtils.currentUser().id();
        return ApiResponse.success(refundService.createRefund(userId, request));
    }

    @GetMapping("/my")
    public ApiResponse<List<RefundDetailItem>> listMyRefunds() {
        Long userId = SecurityUtils.currentUser().id();
        return ApiResponse.success(refundService.listCurrentUserRefunds(userId));
    }
}
