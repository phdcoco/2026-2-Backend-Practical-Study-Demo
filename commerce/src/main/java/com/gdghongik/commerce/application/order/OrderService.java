package com.gdghongik.commerce.application.order;

import com.gdghongik.commerce.application.order.dto.CreateOrderCommand;
import com.gdghongik.commerce.application.order.dto.OrderResult;
import com.gdghongik.commerce.domain.common.Money;
import com.gdghongik.commerce.domain.common.Quantity;
import com.gdghongik.commerce.domain.order.Order;
import com.gdghongik.commerce.domain.order.OrderItem;
import com.gdghongik.commerce.domain.product.Product;
import com.gdghongik.commerce.domain.order.OrderRepository;
import com.gdghongik.commerce.domain.product.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    @Transactional
    public Long place(CreateOrderCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "상품이 존재하지 않습니다. id=" + command.productId()));

        OrderItem item = OrderItem.create(
                product.getId(),
                product.getName(),
                Money.of(product.getPrice()),
                Quantity.of(command.quantity()));

        return orderRepository.save(Order.place(item)).getId();
    }

    public OrderResult findById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다. id=" + orderId));
        return OrderResult.from(order);
    }

    public List<OrderResult> findAll() {
        return orderRepository.findAll().stream()
                .map(OrderResult::from)
                .toList();
    }
}
