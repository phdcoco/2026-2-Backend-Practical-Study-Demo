package com.gdghongik.commerce.application.order;

import com.gdghongik.commerce.domain.order.Order;
import com.gdghongik.commerce.domain.order.OrderRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

public class FakeOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            ReflectionTestUtils.setField(order, "id", ++sequence);
        }
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }
}
