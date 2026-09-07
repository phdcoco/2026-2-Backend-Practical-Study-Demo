package com.gdghongik.commerce.presentation.order.dto;

import com.gdghongik.commerce.application.order.dto.OrderResult;

public record OrderResponse(Long orderId, String status, long totalAmount) {

    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(result.orderId(), result.status(), result.totalAmount());
    }
}
