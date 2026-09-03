package com.gdghongik.commerce.repository;

import com.gdghongik.commerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
