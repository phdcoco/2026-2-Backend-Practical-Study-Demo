package com.gdghongik.commerce.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(OrderStatus status) {
        this.status = status;
    }

    public static Order place(OrderItem item) {
        // TODO[W2-7]: CREATED 상태의 Order 를 만들고, addItem 으로 첫 항목을 담아 반환하세요.
        //             항목이 0개인 Order 가 잠깐이라도 바깥에 나가면 안 됩니다.
        throw new UnsupportedOperationException("TODO[W2-7]");
    }

    public void addItem(OrderItem item) {
        // TODO[W2-4]: 항목이 null 이면 IllegalArgumentException 을 던지세요.
        //             CREATED 상태가 아니면 IllegalStateException 을 던지세요.
        //             둘 다 통과하면 orderItems 에 담고 item.assignTo(this) 를 부르세요.
        throw new UnsupportedOperationException("TODO[W2-4]");
    }

    public void cancel() {
        // TODO[W2-5]: SHIPPED 또는 DELIVERED 상태면 IllegalStateException 을 던지세요.
        //             정상이면 상태를 CANCELED 로 바꾸세요.
        throw new UnsupportedOperationException("TODO[W2-5]");
    }

    public Money totalAmount() {
        // TODO[W2-6]: 모든 주문 항목의 subtotal() 을 더한 Money 를 반환하세요.
        throw new UnsupportedOperationException("TODO[W2-6]");
    }

    public List<OrderItem> getOrderItems() {
        // TODO[W2-8]: 바깥에서 목록을 수정할 수 없도록 반환하세요.
        return orderItems;
    }

    public void ship() {
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        this.status = OrderStatus.DELIVERED;
    }
}
