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
        // TODO[W2-1]: 금액이 0원 미만이면 IllegalArgumentException 을 던지세요.
        this.amount = amount;
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public Money add(Money other) {
        // TODO[W2-2]: 두 금액을 더한 새 Money 를 반환하세요.
        throw new UnsupportedOperationException("TODO[W2-2]");
    }

    public Money multiply(Quantity quantity) {
        // TODO[W2-2]: 금액에 수량을 곱한 새 Money 를 반환하세요.
        throw new UnsupportedOperationException("TODO[W2-2]");
    }

    public long value() {
        return amount;
    }

    @Override
    public String toString() {
        return amount + "원";
    }
}
