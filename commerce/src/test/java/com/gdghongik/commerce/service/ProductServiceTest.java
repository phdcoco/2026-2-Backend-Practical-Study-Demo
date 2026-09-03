package com.gdghongik.commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gdghongik.commerce.entity.Product;
import com.gdghongik.commerce.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 테스트는 실제 DB나 와부 API 등과 연동하여 더 실무적인 관점에서 테스트합니다.
 * 단위 테스트보다 실행 속도가 느리고 에러의 원인 추적이 복잡할 수 있습니다.
 *
 * @SpringBootTest: 실제로 SpringBoot를 구동시켜 테스트를 진행해봅니다.
 * @ActiveProfiles: test 파일에서는 DataInitializer를 실행하지 않습니다.
 * @Transactional: 테스트가 끝나면 저장한 데이터를 되돌립니다. 테스트 데이터가 원본 데이터에 섞이지 않도록 합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("재고를 정상적으로 감소시킨다")
    void 재고를_정상적으로_감소시킨다() {
        // given - 재고가 10개인 상품이 있다
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));

        // when - 3개를 주문한다
        productService.decreaseStock(product.getId(), 3);

        // then - 재고가 7개가 된다
        Product found = productRepository.findById(product.getId()).orElseThrow();
        assertThat(found.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("수량이 0 이하이면 예외가 발생한다")
    void 수량이_0_이하이면_예외가_발생한다() {
        // given
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));

        // when & then, 상품을 0개 사려고 시도할 때 적절한 예외를 반환하는지 확인합니다.
        assertThatThrownBy(() -> productService.decreaseStock(product.getId(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 예외가 발생한다")
    void 재고보다_많이_주문하면_예외가_발생한다() {
        // given - 재고가 3개인 상품
        Product product = productRepository.save(new Product("무선 마우스", 45_000L, 3));

        // when & then, 재고가 3개인 상품을 4개 사려고 시도할 때 적절한 예외를 반환하는지 확인합니다.
        assertThatThrownBy(() -> productService.decreaseStock(product.getId(), 4))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("판매 중이 아닌 상품은 재고를 줄일 수 없다")
    void 판매중이_아닌_상품은_재고를_줄일_수_없다() {
        // given - 판매가 중지된 상품
        Product product = new Product("단종된 USB 허브", 25_000L, 5);
        product.stopSelling();
        productRepository.save(product);

        // when & then, 상태가 STOPPED인 상품을 사려고 시도할 때 적절한 예외를 반환하는지 확인합니다.
        assertThatThrownBy(() -> productService.decreaseStock(product.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("판매 중인 상품이 아닙니다.");
    }
}
