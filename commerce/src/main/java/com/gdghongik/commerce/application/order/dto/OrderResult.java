package com.gdghongik.commerce.application.order.dto;

import com.gdghongik.commerce.domain.order.Order;

public record OrderResult(Long orderId, String status, long totalAmount) {

    public static OrderResult from(Order order) {
        return new OrderResult(
                order.getId(),
                order.getStatus().name(),
                order.totalAmount().value());
    }
}
