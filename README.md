# WEEK 2 해설 — Entity · Value Object · Aggregate 와 불변식

> 이 브랜치는 **정답**입니다. 문제는 `week2` 브랜치입니다.
> 먼저 직접 해보고 오세요. **과제 정답도 여기 함께 들어 있습니다.**

```bash
git diff week2 week2-done          # 전부
git diff week2 week2-done --stat   # 요약
```

---

## 무엇이 바뀌었나

수업 부분과 과제 부분이 섞여 있으니 나눠서 보세요.

```
[수업]
 Money.java       +5   -5     검증 한 줄, 계산 두 줄
 Quantity.java    +3   -1     검증 한 줄
 Order.java      +22  -14     규칙 다섯 개

[과제 1 · Quantity 전환]
 Product.java     +3   -6     검증 하나가 사라진다
 ProductService   +2   -1     경계에서 변환
 ProductTest      +5   -5

[과제 2 · 부분 취소]
 Order.java      +15   -0     cancelItem
 OrderTest       +47   -0     테스트 4개
```

수업 부분만 보면 **30줄 추가가 전부**입니다. 새로 만든 파일은 하나도 없어요.
껍데기는 이미 다 있었고, **여러분은 규칙만 채웠습니다.**

---

## 어디를 봐야 하나

### 1. `Money` — 검증이 생성자에 있다

```java
private Money(long amount) {
    if (amount < 0) {
        throw new IllegalArgumentException("금액은 0원 이상이어야 합니다.");
    }
    this.amount = amount;
}
```

1주차 `Product.decreaseStock()` 과 검증 자체는 똑같습니다. **다른 건 위치예요.**

지난주에는 메서드 안에 넣었죠. 그러면 그 메서드를 안 부르면 그만입니다.
이번엔 **생성자**입니다. `Money` 는 태어나는 순간부터 음수일 수가 없어요.

> 이 프로젝트 어디에서도 음수 금액은 만들어지지 않습니다.

### 2. `Money.add` / `multiply` — 새 객체를 돌려준다

```java
public Money add(Money other) {
    return Money.of(this.amount + other.amount);
}
```

`this.amount += other.amount` 가 아닙니다. **원래 `Money` 는 건드리지 않습니다.**

```java
Money original = Money.of(1_000L);
original.add(Money.of(500L));
// original 은 여전히 1000원
```

만원짜리 지폐가 오천원으로 변하지 않는 것과 같습니다.
`더해도_원래_금액은_바뀌지_않는다` 테스트가 이걸 확인합니다.

### 3. `Order.addItem` — 검증과 연결이 한 곳에

```java
public void addItem(OrderItem item) {
    if (item == null) {
        throw new IllegalArgumentException("주문 항목이 있어야 주문할 수 있습니다.");
    }
    if (this.status != OrderStatus.CREATED) {
        throw new IllegalStateException("주문 확정 이후에는 항목을 변경할 수 없습니다.");
    }

    this.orderItems.add(item);
    item.assignTo(this);
}
```

마지막 줄을 보세요. `assignTo` 는 **package-private** 입니다.

바깥에서 `OrderItem.create(...)` 로 항목을 만들 수는 있지만,
**아무 주문에나 갖다 붙일 수는 없습니다.** 붙이는 건 `Order.addItem` 을 통해야만 하고,
거기엔 상태 검증이 있죠.

### 4. `Order.place` — 세 줄

```java
public static Order place(OrderItem item) {
    Order order = new Order(OrderStatus.CREATED);
    order.addItem(item);
    return order;
}
```

**null 검증이 여기 없다는 걸 보세요.** `addItem` 이 대신 해줍니다.

이게 "검증을 한 곳에 모은다" 의 실제 이득입니다. 새 진입점이 생겨도 검증을 복사할 필요가 없어요.

그리고 `place` 를 만든 순간 `OrderTest` 여덟 개가 한꺼번에 초록이 됐죠.
**모든 테스트가 `Order.place()` 로 주문을 만들기 때문**입니다. 만드는 길이 하나뿐이라서요.

> 1주차에는 **바꾸는 길**을 하나로 만들었고(`@Setter` 제거),
> 2주차에는 **만드는 길**을 하나로 만들었습니다(`private` 생성자).

### 5. `Order.totalAmount` — 저장하지 않고 계산한다

```java
public Money totalAmount() {
    return orderItems.stream()
            .map(OrderItem::subtotal)
            .reduce(Money.ZERO, Money::add);
}
```

`private Money totalAmount;` 로 저장할 수도 있었습니다. 그러면 조회는 빨라지죠.

대신 항목이 추가될 때마다, 취소될 때마다 다시 계산해서 넣어야 합니다.
**한 번만 빠뜨리면 총액과 항목 합이 어긋납니다.**

> **다만 "계산할 수 있으면 무조건 저장하지 않는다" 는 아닙니다.**
> 결제처럼 **그때 그 값이 기록으로 남아야 하는 경우**에는 저장하는 게 맞습니다.
> 기준은 이겁니다 — **항목이 바뀌면 따라 바뀌어야 하면 계산, 그때 값이 남아야 하면 저장.**
> 여기서는 앞쪽이라 계산했습니다.

### 6. `getOrderItems` — 마지막 문

```java
public List<OrderItem> getOrderItems() {
    return Collections.unmodifiableList(orderItems);
}
```

한 줄인데 이게 없으면 앞의 모든 게 무너집니다.

```java
order.getOrderItems().clear();   // 문이 열려 있으면 이게 된다
```

항목이 전부 사라지는데 `Order` 는 그걸 모릅니다. 총액도, 상태 검증도 다 건너뛰죠.

---

## 규칙 여섯 개가 지금 어디 있나

| # | 규칙 | 지키는 곳 |
|---|---|---|
| 1 | 주문에는 항목이 1개 이상 | `Order.place()` — 항목 없이 만들 방법이 없다 |
| 2 | 수량은 1개 이상 | `Quantity` 생성자 |
| 3 | 금액은 음수 불가 | `Money` 생성자 |
| 4 | 항목은 주문을 통해서만 | `assignTo` package-private + `getOrderItems()` |
| 5 | 총액 = 항목 합 | `Order.totalAmount()` — 계산하므로 어긋날 수 없다 |
| 6 | 배송 시작 후 취소 불가 | `Order.cancel()` |

**1주차 과제에서 글로 쓴 여섯 문장이 코드가 됐습니다.**

---

## 자주 나온 다른 답

| 이렇게 하신 분 | 의견 |
|---|---|
| `totalAmount` 를 for 문으로 작성 | 괜찮습니다. 결과만 같으면 돼요 |
| 검증을 `validateXxx()` private 메서드로 분리 | 좋습니다. 규칙이 늘어나면 그렇게 갑니다 |
| `place` 안에 null 검증을 또 씀 | 동작은 합니다. `addItem` 에 이미 있어서 중복이에요 |
| `cancel` 에서 `CANCELED` 도 막음 | 더 엄격한 선택입니다 |
| `Money.add` 에서 `new Money(...)` 사용 | 같은 클래스라 `private` 생성자를 직접 부를 수 있습니다 |

**틀린 것 두 가지**

- **`this.amount += other.amount`** — VO 를 바꿔버렸습니다. `더해도_원래_금액은_바뀌지_않는다` 가 빨갛게 됩니다
- **`place` 에서 `orderItems.add()` 를 직접 호출** — `addItem` 을 건너뛰어서 상태 검증도 `assignTo` 도 빠집니다.
  `order_id` 가 null 인 항목이 저장되려다 실패합니다

---

## 과제 1 해설 · `Product` 도 `Quantity` 로

### 검증 하나가 사라집니다

```java
public void decreaseStock(Quantity quantity) {
    // if (quantity <= 0) ... ← 없어졌다
    if (this.status != SellingStatus.SELLING) { ... }
    if (this.stock < quantity.value()) { ... }
    ...
}
```

`Quantity` 를 받는 순간 "수량은 1개 이상" 을 `Product` 가 검사할 이유가 없어집니다.
**0개짜리 `Quantity` 는 애초에 만들어질 수 없으니까요.**

수업에서 본 그 표를 다시 보세요.

```
자기 값만 보면 되는 규칙   →  Quantity 가 지킨다
여러 개를 봐야 하는 규칙   →  Product 가 지킨다
```

1주차에 `Product` 가 지키던 규칙 하나가 **더 작은 단위로 내려간** 겁니다.

### 어디서 변환할 것인가

```java
public void decreaseStock(Long productId, int quantity) {
    product.decreaseStock(Quantity.of(quantity));   // Service 에서 변환
}
```

**다른 선택도 맞습니다.**

| 선택 | 장점 | 단점 |
|---|---|---|
| Service 가 `int` 를 받고 안에서 변환 | Controller 가 도메인 타입을 몰라도 됨 | 변환 지점이 Service 마다 반복 |
| Service 가 `Quantity` 를 받음 | 잘못된 수량이 Service 에 도달하지 못함 | Presentation 이 도메인 타입을 알게 됨 |

정답은 없습니다. 다만 어느 쪽이든 **경계는 하나여야** 합니다. 여기저기서 변환하면 다시 흩어집니다.

### 초록불인데 아무것도 검증하지 않는 테스트

```java
void 수량이_0_이하이면_예외가_발생한다() {
    Product product = new Product("기계식 키보드", 129_000L, 10);

    assertThatThrownBy(() -> product.decreaseStock(Quantity.of(0)))
            .isInstanceOf(IllegalArgumentException.class);
}
```

이 테스트는 여전히 초록입니다. **그런데 예외를 던지는 건 `Quantity.of(0)` 입니다.**
`decreaseStock` 은 호출되지도 않아요. `Product` 를 테스트한다고 써놓고 `Quantity` 를 테스트하고 있는 거죠.

같은 내용이 `QuantityTest` 에 이미 있으니 **이 테스트는 지우는 게 맞습니다.**

> 초록불이 항상 옳은 건 아닙니다. **무엇을 검증하는 테스트인지**를 같이 봐야 합니다.

---

## 과제 2 해설 · 부분 취소

```java
public void cancelItem(OrderItem item) {
    if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
        throw new IllegalStateException("배송이 시작된 주문은 취소할 수 없습니다.");
    }
    if (!this.orderItems.contains(item)) {
        throw new IllegalArgumentException("이 주문의 항목이 아닙니다.");
    }

    this.orderItems.remove(item);

    if (this.orderItems.isEmpty()) {
        this.status = OrderStatus.CANCELED;
    }
}
```

### 총액은 손댈 필요가 없습니다

항목을 빼면 총액이 줄어야 하죠. 그런데 `totalAmount` 를 고친 곳이 없습니다.

**계산해서 돌려주기 때문**입니다. 필드로 저장했다면 여기서 다시 계산해 넣어야 했고,
그걸 빠뜨리면 총액이 어긋났을 겁니다.

### 마지막 항목을 빼면 — 여기가 판단입니다

이 답안은 **주문 전체를 취소** 하는 쪽을 골랐습니다.

```java
if (this.orderItems.isEmpty()) {
    this.status = OrderStatus.CANCELED;
}
```

현실에서 항목을 전부 뺀 주문은 취소된 주문이고,
"항목이 1개 이상" 이라는 규칙을 깨지 않은 채로 상태만 옮길 수 있기 때문입니다.

**다른 선택도 맞습니다.**

```java
if (this.orderItems.size() == 1) {
    throw new IllegalStateException("마지막 항목은 취소할 수 없습니다. 주문을 취소하세요.");
}
```

**어느 쪽이든, 고른 이유를 설명할 수 있으면 맞습니다.**

---

## `OrderPersistenceTest` 는 왜 있나

여러분이 채우지 않은, 제가 넣어둔 테스트입니다. H2 에 실제로 저장하고 다시 꺼내봅니다.

1. **`Money` 와 `Quantity` 가 컬럼으로 저장되는가** — `@Embeddable` 이라 별도 테이블 없이
   `order_item` 테이블에 `price`, `quantity` 컬럼으로 들어갑니다
2. **주문과 항목이 함께 저장되고 함께 조회되는가** — `cascade = ALL` 이 동작하는지

이 매핑이 틀리면 **5주차에 문제가 생깁니다.** 그때 이 연관관계로 조회 성능 문제를 다루거든요.

---

## 다음 주

규칙은 제자리를 찾았습니다. 주문에 관한 건 주문이 지키고, 금액은 금액이 지키고요.

그런데 이 주문을 **저장하는 코드**는 어디 있어야 할까요?

지금 우리 `ProductService` 는 `JpaRepository` 를 직접 알고 있습니다.
데이터베이스를 바꾸면? JPA 를 안 쓰게 되면? Service 를 다 고쳐야 하죠.

**그게 다음 주 주제입니다.**
