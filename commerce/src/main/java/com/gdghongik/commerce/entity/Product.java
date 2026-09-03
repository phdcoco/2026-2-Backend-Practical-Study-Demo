package com.gdghongik.commerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private long price;

    private int stock;

    @Enumerated(EnumType.STRING)
    private SellingStatus status;

    public Product(String name, long price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = SellingStatus.SELLING;
    }

    public void stopSelling() {
        this.status = SellingStatus.STOPPED;
    }

    public void decreaseStock(int quantity) {
        // TODO[W1-4]: 재고 감소 규칙을 여기에 구현하세요.
        throw new UnsupportedOperationException("TODO[W1-4]");
    }
}
