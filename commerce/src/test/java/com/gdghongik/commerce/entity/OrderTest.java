package com.gdghongik.commerce.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    private OrderItem keyboardItem;
    private OrderItem mouseItem;

    @BeforeEach
    void setUp() {
        keyboardItem = OrderItem.create(1L, "기계식 키보드", Money.of(129_000L), Quantity.of(2));
        mouseItem = OrderItem.create(2L, "무선 마우스", Money.of(45_000L), Quantity.of(3));
    }

    @Test
    @DisplayName("주문하면 주문 항목이 하나 생긴다")
    void 주문하면_항목이_하나_생긴다() {
        Order order = Order.place(keyboardItem);

        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("항목 없이는 주문할 수 없다")
    void 항목_없이는_주문할_수_없다() {
        assertThatThrownBy(() -> Order.place(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목이 있어야 주문할 수 있습니다.");
    }

    @Test
    @DisplayName("주문에 항목을 더 추가할 수 있다")
    void 주문에_항목을_추가할_수_있다() {
        Order order = Order.place(keyboardItem);

        order.addItem(mouseItem);

        assertThat(order.getOrderItems()).hasSize(2);
    }

    @Test
    @DisplayName("배송이 시작되면 항목을 추가할 수 없다")
    void 배송이_시작되면_항목을_추가할_수_없다() {
        Order order = Order.place(keyboardItem);
        order.ship();

        assertThatThrownBy(() -> order.addItem(mouseItem))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("주문 확정 이후에는 항목을 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("주문 총액은 항목 금액의 합과 같다")
    void 주문_총액은_항목_금액의_합과_같다() {
        Order order = Order.place(keyboardItem);
        order.addItem(mouseItem);

        Money expected = Money.of(129_000L * 2 + 45_000L * 3);

        assertThat(order.totalAmount()).isEqualTo(expected);
    }

    @Test
    @DisplayName("주문을 취소할 수 있다")
    void 주문을_취소할_수_있다() {
        Order order = Order.place(keyboardItem);

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("배송이 시작된 주문은 취소할 수 없다")
    void 배송이_시작된_주문은_취소할_수_없다() {
        Order order = Order.place(keyboardItem);
        order.ship();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("배송이 시작된 주문은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("배송이 완료된 주문도 취소할 수 없다")
    void 배송이_완료된_주문도_취소할_수_없다() {
        Order order = Order.place(keyboardItem);
        order.deliver();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("주문 항목 목록을 바깥에서 수정할 수 없다")
    void 주문항목_목록을_바깥에서_수정할_수_없다() {
        Order order = Order.place(keyboardItem);

        assertThatThrownBy(() -> order.getOrderItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
