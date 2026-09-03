package com.gdghongik.commerce.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 스프링을 띄우지 않고, Repository도 주입하지 않습니다.
 * 단위 테스트는 Java 코드 그 자체로 로직상의 결함이 없는지 테스트합니다.
 */
class ProductTest {

    @Test
    @DisplayName("재고를 정상적으로 감소시킨다")
    // 메서드명은 한글로 해도 되고, 영어로 해도 됩니다.
    void 재고를_정상적으로_감소시킨다() {
        // given
        Product product = new Product("기계식 키보드", 129_000L, 10);

        // when
        product.decreaseStock(3);

        // then, 10에서 3을 빼면 7이겠죠?
        assertThat(product.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("수량이 0 이하이면 예외가 발생한다")
    void 수량이_0_이하이면_예외가_발생한다() {
        // given
        Product product = new Product("기계식 키보드", 129_000L, 10);

        // when & then, 상품을 0개 사려고 시도할 때 적절한 예외를 반환하는지 확인합니다.
        assertThatThrownBy(() -> product.decreaseStock(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 예외가 발생한다")
    void 재고보다_많이_주문하면_예외가_발생한다() {
        // given
        Product product = new Product("무선 마우스", 45_000L, 3);

        // when & then, 재고가 3개인 상품을 4개 사려고 시도할 때 적절한 예외를 반환하는지 확인합니다.
        assertThatThrownBy(() -> product.decreaseStock(4))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("판매 중이 아닌 상품은 재고를 줄일 수 없다")
    void 판매중이_아닌_상품은_재고를_줄일_수_없다() {
        // given
        Product product = new Product("단종된 USB 허브", 25_000L, 5);
        product.stopSelling();

        // when & then, 상태가 STOPPED인 상품을 사려고 시도할 때 적절한 예외를 반환하는지 확인합니다.
        assertThatThrownBy(() -> product.decreaseStock(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("판매 중인 상품이 아닙니다.");
    }

    @Test
    @DisplayName("재고가 0이 되면 판매 상태가 SOLD_OUT 으로 바뀐다")
    void 재고가_0이_되면_품절_상태가_된다() {
        // given - 재고가 1개인 상품
        Product product = new Product("한정판 마우스패드", 19_000L, 1);

        // when
        product.decreaseStock(1);

        // then, 재고가 1개뿐인 상품을 1개 구매할 때 재고가 0이 되고 SOLD_OUT 상태로 적절히 변경하는지 확인합니다.
        assertThat(product.getStock()).isZero();
        assertThat(product.getStatus()).isEqualTo(SellingStatus.SOLD_OUT);
    }
}
