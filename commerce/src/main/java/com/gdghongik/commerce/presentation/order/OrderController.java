package com.gdghongik.commerce.presentation.order;

import com.gdghongik.commerce.application.order.OrderService;
import com.gdghongik.commerce.presentation.order.dto.CreateOrderRequest;
import com.gdghongik.commerce.presentation.order.dto.OrderResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody CreateOrderRequest request) {
        Long orderId = orderService.place(request.toCommand());
        return OrderResponse.from(orderService.findById(orderId));
    }

    @GetMapping("/{orderId}")
    public OrderResponse findById(@PathVariable Long orderId) {
        return OrderResponse.from(orderService.findById(orderId));
    }

    @GetMapping
    public List<OrderResponse> findAll() {
        return orderService.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }
}
