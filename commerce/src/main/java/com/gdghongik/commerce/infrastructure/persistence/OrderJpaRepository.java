package com.gdghongik.commerce.infrastructure.persistence;

import com.gdghongik.commerce.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
