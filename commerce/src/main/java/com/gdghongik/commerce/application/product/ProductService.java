package com.gdghongik.commerce.application.product;

import com.gdghongik.commerce.domain.common.Quantity;
import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.infrastructure.persistence.ProductJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    // TODO[W3-2]: 아래 주입 타입을 domain 의 ProductRepository 로 바꾸세요.
    //             OrderService 에도 같은 타입이 있습니다. 거기도 같이 바꾸세요.
    //             바꾸고 나면 애플리케이션이 뜨지 않습니다. 오류 메시지를 읽고 W3-3 으로 가세요.
    private final ProductJpaRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. id=" + productId));
    }

    @Transactional
    public Product create(String name, long price, int stock) {
        return productRepository.save(new Product(name, price, stock));
    }

    /**
     * 이제 이 메서드는 상품을 ID로 찾고, 상품의 재고를 줄이고, 변경된 상품 정보를 저장하는 작업을 하라고 명령합니다.
     * 재고를 줄여도 되는지에 대한 판단은 Product가 직접 합니다.
     */
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = findById(productId);
        product.decreaseStock(Quantity.of(quantity));
        productRepository.save(product);
    }
}
