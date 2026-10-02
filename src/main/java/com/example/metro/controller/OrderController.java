package com.example.metro.controller;

import com.example.metro.common.api.ApiResponse;
import com.example.metro.dto.order.CreateOrderRequest;
import com.example.metro.dto.order.OrderDetailItem;
import com.example.metro.security.UserContextHolder;
import com.example.metro.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderDetailItem> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        Long userId = UserContextHolder.getRequired().id();
        return ApiResponse.success(orderService.createOrder(userId, request));
    }

    @GetMapping
    public ApiResponse<List<OrderDetailItem>> listOrders() {
        Long userId = UserContextHolder.getRequired().id();
        return ApiResponse.success(orderService.listCurrentUserOrders(userId));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailItem> getOrder(@PathVariable Long orderId) {
        Long userId = UserContextHolder.getRequired().id();
        return ApiResponse.success(orderService.getCurrentUserOrder(userId, orderId));
    }
}
