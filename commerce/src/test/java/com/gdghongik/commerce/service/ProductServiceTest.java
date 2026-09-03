package com.gdghongik.commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import com.gdghongik.commerce.entity.Product;
import com.gdghongik.commerce.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
        // given
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));

        // when
        productService.decreaseStock(product.getId(), 3);

        // then
        Product found = productRepository.findById(product.getId()).orElseThrow();
        assertThat(found.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("수량이 0 이하이면 예외가 발생한다")
    void 수량이_0_이하이면_예외가_발생한다() {
        // given
        Product product = productRepository.save(new Product("기계식 키보드", 129_000L, 10));

        // TODO[W1-1]: 수량이 0 이하일 때 예외가 발생하는지 검증하세요.
        fail("TODO[W1-1] 을 작성하세요");
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 예외가 발생한다")
    void 재고보다_많이_주문하면_예외가_발생한다() {
        // given
        Product product = productRepository.save(new Product("무선 마우스", 45_000L, 3));

        // TODO[W1-2]: 재고보다 많이 주문하면 예외가 발생하는지 검증하세요.
        fail("TODO[W1-2] 를 작성하세요");
    }

    @Test
    @DisplayName("판매 중이 아닌 상품은 재고를 줄일 수 없다")
    void 판매중이_아닌_상품은_재고를_줄일_수_없다() {
        // given
        Product product = new Product("단종된 USB 허브", 25_000L, 5);
        product.stopSelling();
        productRepository.save(product);

        // TODO[W1-3]: 판매 중이 아닌 상품이면 예외가 발생하는지 검증하세요.
        fail("TODO[W1-3] 을 작성하세요");
    }
}
