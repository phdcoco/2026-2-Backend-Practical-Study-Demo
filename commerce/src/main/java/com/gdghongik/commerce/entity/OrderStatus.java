package com.gdghongik.commerce.entity;

public enum OrderStatus {

    CREATED("주문됨"),
    SHIPPED("배송중"),
    DELIVERED("배송완료"),
    CANCELED("취소됨");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
