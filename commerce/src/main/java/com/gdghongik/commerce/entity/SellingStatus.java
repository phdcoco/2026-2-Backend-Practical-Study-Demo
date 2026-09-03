package com.gdghongik.commerce.entity;

import lombok.Getter;

@Getter
public enum SellingStatus {
    SELLING("판매중"),
    SOLD_OUT("품절"),
    STOPPED("판매중지");

    private final String description;

    SellingStatus(String description) {
        this.description = description;
    }

}
