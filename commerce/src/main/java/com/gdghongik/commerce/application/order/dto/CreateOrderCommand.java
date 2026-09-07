package com.gdghongik.commerce.application.order.dto;

public record CreateOrderCommand(Long productId, int quantity) {
}
