package com.gdghongik.commerce.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Long productId;

    private String productName;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price"))
    private Money price;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity"))
    private Quantity quantity;

    private OrderItem(Long productId, String productName, Money price, Quantity quantity) {
        if (productId == null || productName == null || price == null || quantity == null) {
            throw new IllegalArgumentException("주문 항목에 빠진 값이 있습니다.");
        }
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public static OrderItem create(Long productId, String productName, Money price, Quantity quantity) {
        return new OrderItem(productId, productName, price, quantity);
    }

    public Money subtotal() {
        return price.multiply(quantity);
    }

    void assignTo(Order order) {
        this.order = order;
    }
}
