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

/**
 * 바깥에서 값을 마음대로 바꿀 수 없도록 @Setter 를 두지 않습니다.
 */
@Entity
@Getter
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

    /**
     * 재고를 quantity만큼 줄입니다.
     *
     * 재고를 줄여도 되는지에 대한 판단은 변경되는 주체인 Product 엔티티가 직접 판단한다.
     */
    public void decreaseStock(Quantity quantity) {
        if (this.status != SellingStatus.SELLING) {
            throw new IllegalStateException("판매 중인 상품이 아닙니다.");
        }
        if (this.stock < quantity.value()) {
            throw new IllegalStateException("재고가 부족합니다. 남은 재고=" + this.stock);
        }

        this.stock -= quantity.value();

        if (this.stock == 0) {
            this.status = SellingStatus.SOLD_OUT;
        }
    }

}
