package com.gdghongik.commerce.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    @DisplayName("금액이 음수면 예외가 발생한다")
    void 금액이_음수면_예외가_발생한다() {
        assertThatThrownBy(() -> Money.of(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("금액은 0원 이상이어야 합니다.");
    }

    @Test
    @DisplayName("0원은 만들 수 있다")
    void 영원은_만들_수_있다() {
        assertThat(Money.of(0L).value()).isZero();
    }

    @Test
    @DisplayName("두 금액을 더한다")
    void 두_금액을_더한다() {
        Money sum = Money.of(1_000L).add(Money.of(500L));

        assertThat(sum).isEqualTo(Money.of(1_500L));
    }

    @Test
    @DisplayName("금액에 수량을 곱한다")
    void 금액에_수량을_곱한다() {
        Money total = Money.of(1_000L).multiply(Quantity.of(3));

        assertThat(total).isEqualTo(Money.of(3_000L));
    }

    @Test
    @DisplayName("금액이 같으면 같은 값으로 취급한다")
    void 금액이_같으면_같은_값이다() {
        assertThat(Money.of(1_000L)).isEqualTo(Money.of(1_000L));
    }

    @Test
    @DisplayName("더해도 원래 금액은 바뀌지 않는다")
    void 더해도_원래_금액은_바뀌지_않는다() {
        Money original = Money.of(1_000L);

        original.add(Money.of(500L));

        assertThat(original).isEqualTo(Money.of(1_000L));
    }
}
