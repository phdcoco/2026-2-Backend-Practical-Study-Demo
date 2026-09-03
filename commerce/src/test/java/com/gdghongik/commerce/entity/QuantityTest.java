package com.gdghongik.commerce.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuantityTest {

    @Test
    @DisplayName("수량이 0이면 예외가 발생한다")
    void 수량이_0이면_예외가_발생한다() {
        assertThatThrownBy(() -> Quantity.of(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("수량이 음수면 예외가 발생한다")
    void 수량이_음수면_예외가_발생한다() {
        assertThatThrownBy(() -> Quantity.of(-3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("1개 이상이면 정상 생성된다")
    void 한개_이상이면_생성된다() {
        assertThat(Quantity.of(1).value()).isEqualTo(1);
    }
}
