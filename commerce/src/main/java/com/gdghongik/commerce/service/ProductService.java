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

    // TODO[W1-5]: 조회 -> product.decreaseStock(quantity) -> 저장 3줄로 줄이고, Product 의 @Setter 를 지우세요.
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
