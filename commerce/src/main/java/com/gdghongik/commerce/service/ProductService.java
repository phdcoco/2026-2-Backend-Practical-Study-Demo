package com.gdghongik.commerce.service;

import com.gdghongik.commerce.entity.Product;
import com.gdghongik.commerce.entity.SellingStatus;
import com.gdghongik.commerce.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

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
     * 재고를 줄인다.
     *
     * 이 메서드는 한 번에 네 가지 일을 한다.
     *   1. 상품을 찾는다
     *   2. 요청이 올바른지 검사한다
     *   3. 재고를 계산한다
     *   4. 판매 상태를 바꾼다
     *
     * 2~4번은 '상품이라면 당연히 지켜야 하는 규칙'인데 Product 밖에 나와 있다.
     * 그래서 Product 를 직접 만지는 다른 코드가 생기면 이 규칙은 지켜지지 않는다.
     */
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. id=" + productId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        if (product.getStatus() != SellingStatus.SELLING) {
            throw new IllegalStateException("판매 중인 상품이 아닙니다.");
        }
        if (product.getStock() < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 남은 재고=" + product.getStock());
        }

        product.setStock(product.getStock() - quantity);

        if (product.getStock() == 0) {
            product.setStatus(SellingStatus.SOLD_OUT);
        }

        productRepository.save(product);
    }
}
