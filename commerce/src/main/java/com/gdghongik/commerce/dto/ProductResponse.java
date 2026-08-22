package com.gdghongik.commerce.dto;

import com.gdghongik.commerce.entity.Product;

public record ProductResponse(
        Long id,
        String name,
        long price,
        int stock,
        String status
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getStatus().name()
        );
    }
}
