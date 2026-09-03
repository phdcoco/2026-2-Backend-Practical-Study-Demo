package com.gdghongik.commerce.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {

    public static final Money ZERO = Money.of(0L);

    private long amount;

    private Money(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0원 이상이어야 합니다.");
        }
        this.amount = amount;
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public Money add(Money other) {
        return Money.of(this.amount + other.amount);
    }

    public Money multiply(Quantity quantity) {
        return Money.of(this.amount * quantity.value());
    }

    public long value() {
        return amount;
    }

    @Override
    public String toString() {
        return amount + "원";
    }
}
