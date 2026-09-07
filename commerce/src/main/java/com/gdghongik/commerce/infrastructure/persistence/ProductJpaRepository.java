package com.gdghongik.commerce.infrastructure.persistence;

import com.gdghongik.commerce.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
}
