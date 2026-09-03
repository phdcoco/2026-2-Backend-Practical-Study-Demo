package com.gdghongik.commerce.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("재고를 정상적으로 감소시킨다")
    void 재고를_정상적으로_감소시킨다() {
        // given
        Product product = new Product("기계식 키보드", 129_000L, 10);

        // when
        product.decreaseStock(3);

        // then
        assertThat(product.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("수량이 0 이하이면 예외가 발생한다")
    void 수량이_0_이하이면_예외가_발생한다() {
        // TODO[W1-6]: ProductServiceTest 의 같은 테스트를 Product 만으로 검증하도록 옮겨 오세요.
        fail("TODO[W1-6] 을 작성하세요");
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 예외가 발생한다")
    void 재고보다_많이_주문하면_예외가_발생한다() {
        // TODO[W1-6]: 위와 같은 방식으로 옮겨 오세요.
        fail("TODO[W1-6] 을 작성하세요");
    }

    @Test
    @DisplayName("판매 중이 아닌 상품은 재고를 줄일 수 없다")
    void 판매중이_아닌_상품은_재고를_줄일_수_없다() {
        // TODO[W1-6]: 위와 같은 방식으로 옮겨 오세요.
        fail("TODO[W1-6] 을 작성하세요");
    }

    @Test
    @DisplayName("재고가 0이 되면 판매 상태가 SOLD_OUT 으로 바뀐다")
    void 재고가_0이_되면_품절_상태가_된다() {
        // given
        Product product = new Product("한정판 마우스패드", 19_000L, 1);

        // when
        product.decreaseStock(1);

        // then
        assertThat(product.getStock()).isZero();
        assertThat(product.getStatus()).isEqualTo(SellingStatus.SOLD_OUT);
    }
}
