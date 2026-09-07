package com.gdghongik.commerce.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gdghongik.commerce.application.order.dto.CreateOrderCommand;
import com.gdghongik.commerce.application.order.dto.OrderResult;
import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.infrastructure.persistence.ProductJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

// 과제: 이 테스트를 Fake 기반으로 바꾸는 것이 이번 주 과제입니다.
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductJpaRepository productRepository;

    @Test
    @DisplayName("상품을 주문하면 주문이 생성된다")
    void 상품을_주문하면_주문이_생성된다() {
        // given
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));

        // when
        Long orderId = orderService.place(new CreateOrderCommand(product.getId(), 2));

        // then
        OrderResult result = orderService.findById(orderId);
        assertThat(result.status()).isEqualTo("CREATED");
        assertThat(result.totalAmount()).isEqualTo(129_000L * 2);
    }

    @Test
    @DisplayName("없는 상품은 주문할 수 없다")
    void 없는_상품은_주문할_수_없다() {
        assertThatThrownBy(() -> orderService.place(new CreateOrderCommand(999L, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품이 존재하지 않습니다");
    }

    @Test
    @DisplayName("수량이 0이면 주문할 수 없다")
    void 수량이_0이면_주문할_수_없다() {
        // given
        Product product = productRepository.save(new Product("무선 마우스", 45_000L, 5));

        // when & then
        assertThatThrownBy(() -> orderService.place(new CreateOrderCommand(product.getId(), 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("주문 목록을 조회한다")
    void 주문_목록을_조회한다() {
        // given
        Product product = productRepository.save(new Product("한정판 마우스패드", 19_000L, 5));
        orderService.place(new CreateOrderCommand(product.getId(), 1));
        orderService.place(new CreateOrderCommand(product.getId(), 2));

        // when
        var results = orderService.findAll();

        // then
        assertThat(results).hasSize(2);
    }
}
