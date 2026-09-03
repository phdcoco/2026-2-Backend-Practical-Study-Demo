package com.gdghongik.commerce.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gdghongik.commerce.entity.Money;
import com.gdghongik.commerce.entity.Order;
import com.gdghongik.commerce.entity.OrderStatus;
import com.gdghongik.commerce.entity.OrderItem;
import com.gdghongik.commerce.entity.Quantity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class OrderPersistenceTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("주문과 주문 항목이 함께 저장되고 함께 조회된다")
    void 주문과_항목이_함께_저장된다() {
        Order order = Order.place(
                OrderItem.create(1L, "기계식 키보드", Money.of(129_000L), Quantity.of(2)));
        order.addItem(
                OrderItem.create(2L, "무선 마우스", Money.of(45_000L), Quantity.of(3)));
        Long orderId = orderRepository.save(order).getId();

        entityManager.flush();
        entityManager.clear();

        Order found = orderRepository.findById(orderId).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(found.getOrderItems()).hasSize(2);
        assertThat(found.totalAmount())
                .isEqualTo(Money.of(129_000L * 2 + 45_000L * 3));
    }

    @Test
    @DisplayName("주문 항목에는 주문 당시의 상품명과 가격이 남는다")
    void 주문_당시의_상품명과_가격이_남는다() {
        Order order = Order.place(
                OrderItem.create(1L, "기계식 키보드", Money.of(129_000L), Quantity.of(1)));
        Long orderId = orderRepository.save(order).getId();

        entityManager.flush();
        entityManager.clear();

        Order found = orderRepository.findById(orderId).orElseThrow();

        assertThat(found.getOrderItems().get(0).getProductName()).isEqualTo("기계식 키보드");
        assertThat(found.getOrderItems().get(0).getPrice()).isEqualTo(Money.of(129_000L));
    }
}
