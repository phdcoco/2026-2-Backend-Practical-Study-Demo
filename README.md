# WEEK 2 — Entity · Value Object · Aggregate 와 불변식

> `Entity` · `Value Object` · `Aggregate Root` · `불변식` · `정적 팩터리`

## 오늘 할 일 한 줄 요약

1주차 과제로 분석한 **주문 규칙 6개를 코드로 옮긴다.**
그리고 그 규칙이 **깨질 수 없게** 만든다.

---

## 시작하기

```bash
git checkout week2
cd commerce
./gradlew test
```

**30개 중 17개 실패합니다. 정상입니다.**
1주차에서 만든 10개는 그대로 통과하고, 오늘 채울 자리 17개가 빨간색입니다.

---

## 1주차와 무엇이 다른가

| | 1주차 | 2주차 |
|---|---|---|
| 다루는 것 | `Product` 하나 | `Order` · `OrderItem` · `Money` · `Quantity` |
| 규칙의 성격 | 자기 필드만 보면 판단됨 | **여러 객체를 한꺼번에 봐야 판단됨** |
| 여러분이 쓰는 것 | 테스트 코드 | **도메인 코드** |

**오늘은 테스트를 여러분이 쓰지 않습니다.** 테스트는 이미 다 작성돼 있어요.
빨간 테스트를 초록으로 만드는 게 오늘 실습입니다. 1주차와 반대 방향이죠.

---

## 오늘의 등장 인물

```
Order            주문.  오늘의 주인공
 ├── OrderItem   주문 항목.  "키보드 2개"
 ├── Money       금액
 └── Quantity    수량
     OrderStatus 주문 상태
```

`OrderItem` 과 `OrderStatus` 는 **완성된 상태로 제공**됩니다. 열어보시되 고치지 마세요.
여러분이 채울 곳은 `Money` · `Quantity` · `Order` 세 개입니다.

### `OrderItem` 을 먼저 열어보세요

주문 항목이 **`Product` 를 전혀 모른다**는 점을 확인하고 가세요.

```java
public static OrderItem create(Long productId, String productName, Money price, Quantity quantity)
```

`Product` 객체를 받지 않습니다. **필요한 값만 따로따로 받습니다.**

이유가 두 가지입니다.

**첫째, 주문 시점 스냅샷.**
`productName` 과 `price` 는 상품의 *지금* 이름·가격이 아니라 **주문할 때 찍어둔 값**입니다.
판매자가 나중에 상품명이나 가격을 바꿔도 이미 나간 주문서는 그대로여야 하니까요.

`@ManyToOne Product product` 로 들고 있으면 상품 가격을 고치는 순간 지난 주문 금액이 전부 따라 바뀝니다.

**둘째, 주문과 상품은 서로 다른 덩어리다.**
`Product` 는 1주차에 자기 규칙을 스스로 지키는 객체가 됐죠. 주문도 마찬가지고요.
이렇게 각자 규칙을 책임지는 덩어리끼리는 **객체를 주고받지 않습니다.**

> 오늘 이 "덩어리" 에 이름을 붙입니다.

### 이름 규칙

정적 팩터리 이름이 셋 다 다릅니다. 규칙이 있습니다.

| | 이름 | 왜 |
|---|---|---|
| `Money`, `Quantity` | `of` | 자바 관례. `List.of`, `Optional.of` 와 같은 결 |
| `OrderItem` | `create` | 값 여러 개로 만드는 Entity |
| `Order` | **`place`** | **도메인 행위가 있으면 그 동사를 쓴다** |

## 실습 순서

| TODO | 파일 | 할 일 |
|---|---|---|
| **W2-1** | `Money` | 생성자에서 음수 금액 거부 |
| **W2-2** | `Money` | `add`, `multiply` — 새 `Money` 를 반환 |
| **W2-3** | `Quantity` | 생성자에서 1개 미만 거부 |
| **W2-4** | `Order` | `addItem` — CREATED 상태에서만 허용 |
| **W2-5** | `Order` | `cancel` — 배송 시작 후 취소 금지 |
| **W2-6** | `Order` | `totalAmount` — 항목 금액의 합 |
| **W2-7** | `Order` | `place` — 항목 없는 주문을 만들 수 없게 |
| **W2-8** | `Order` | `getOrderItems` — 바깥에서 수정 불가 |

IntelliJ 에서 `TODO[W2` 로 검색하면 한 번에 나옵니다.

**W2-1 → W2-2 → W2-3 순서로 하세요.** `Money` 와 `Quantity` 가 완성돼야 `Order` 가 동작합니다.
**W2-7 은 맨 마지막에** 하세요. 이유는 아래에 있습니다.

### 단계별 통과 기준

| 끝나면 | 초록이 되는 것 |
|---|---|
| W2-1~2 | `MoneyTest` 6개 |
| W2-3 | `QuantityTest` 3개 |
| W2-4~6 | `OrderTest` 대부분 |
| W2-7 | `OrderTest` 전부 + `OrderPersistenceTest` |
| W2-8 | 30개 전부 |

---

## 오늘의 새 문법 — 정적 팩터리 메서드

`Money` 를 열어보면 생성자가 `private` 입니다.

```java
private Money(long amount) { ... }

public static Money of(long amount) {
    return new Money(amount);
}
```

`new Money(1000)` 은 못 쓰고 `Money.of(1000)` 만 쓸 수 있습니다.
**만드는 길이 하나뿐이니, 검증도 한 곳에만 두면 됩니다.**

`Order` 도 같은 모양입니다. 다만 이름이 `of` 가 아니라 **`place`** 예요.

```java
OrderItem item = OrderItem.create(1L, "기계식 키보드", Money.of(129_000L), Quantity.of(2));
Order order = Order.place(item);
```

`new Order()` 보다 "주문한다" 는 말에 가깝죠.
1주차에 `setStatus(STOPPED)` 를 `stopSelling()` 으로 바꾼 것과 같은 이야기입니다.

> **JPA 때문에 `protected` 기본 생성자는 남아 있습니다.** Hibernate 가 객체를 만들 때 필요해서예요.
> `protected` 라 바깥에서는 못 씁니다.

> 참고로 **생성자를 `public` 으로 열어두고 정적 팩터리를 안 쓰는 코드도 흔합니다.**
> 검증이 생성자 안에 있으면 그것만으로도 규칙은 지켜지니까요.
> 정적 팩터리는 거기에 **이름을 붙일 수 있다**는 장점이 더해진 것이라고 보시면 됩니다.

---

## W2-7 을 마지막에 하는 이유

W2-4·5·6 을 먼저 끝내고 나면, 이런 코드가 아직 가능합니다.

```java
Order order = new Order();        // 항목이 0개인 주문
order.addItem(item);              // 그다음에 항목을 담는다
```

중간에 **항목이 하나도 없는 주문이 잠깐 존재**합니다.
"주문에는 항목이 1개 이상" 이라는 규칙이 그 순간 깨져 있는 거예요.

`Order.place()` 로 통로를 막으면 그런 순간 자체가 사라집니다.
**정적 팩터리는 문법이 아니라 규칙을 지키는 도구입니다.**

---

## 오늘 지키는 규칙 6개

| # | 규칙 | 어디서 지키나 | 확인하는 테스트 |
|---|---|---|---|
| 1 | 주문에는 항목이 1개 이상 | `Order.place()` | `항목_없이는_주문할_수_없다` |
| 2 | 수량은 1개 이상 | `Quantity` 생성자 | `수량이_0이면_예외가_발생한다` |
| 3 | 금액은 음수 불가 | `Money` 생성자 | `금액이_음수면_예외가_발생한다` |
| 4 | 항목은 주문을 통해서만 변경 | `getOrderItems()` | `주문항목_목록을_바깥에서_수정할_수_없다` |
| 5 | 총액 = 항목 금액의 합 | `Order.totalAmount()` | `주문_총액은_항목_금액의_합과_같다` |
| 6 | 배송 시작 후 취소 불가 | `Order.cancel()` | `배송이_시작된_주문은_취소할_수_없다` |

1주차 과제에서 여러분이 찾은 규칙이 이겁니다.
**글로 쓴 게 그대로 코드가 됩니다.**

---

## 완료 기준

- [ ] `./gradlew test` 초록 (30개 전부)
- [ ] `Money` · `Quantity` 에 setter 가 없다
- [ ] `new Order()` 를 클래스 밖에서 쓸 수 없다
- [ ] `order.getOrderItems().add(...)` 가 실패한다

### 스스로 점검하기

오늘 만든 코드가 규칙을 제대로 지키는지 확인하는 목록입니다.

**`Money` · `Quantity` (값 객체)**

- [ ] 생성자에서 유효성 검증을 하는가
- [ ] setter 가 **없는가**
- [ ] 값을 바꾸는 메서드가 **새 객체를 반환**하는가 (`add`, `multiply`)
- [ ] 값이 같으면 같은 객체로 취급되는가 (`equals` / `hashCode`)
- [ ] `@Embeddable` 이 붙어 있는가 — 별도 테이블 없이 같은 테이블에 저장됩니다

**`Order` (주문 덩어리의 대표)**

- [ ] 바깥에서 `OrderItem` 을 직접 만들 수 없는가
- [ ] 내부 목록을 바깥에서 수정할 수 없는가
- [ ] `@ManyToOne` 에 `FetchType.LAZY` 가 붙어 있는가
- [ ] 필수 값의 null 검증을 하는가

---

## 자주 하는 실수

| 증상 | 원인 |
|---|---|
| `UnsupportedOperationException: TODO[W2-x]` | 아직 구현 전입니다. 정상 |
| `Money.add()` 후 원래 값이 바뀜 | `this.amount += ...` 로 쓰셨습니다. **새 `Money` 를 반환**해야 합니다 |
| `totalAmount()` 가 0원 | `reduce` 의 시작값을 `Money.ZERO` 로 두셨는지 확인 |
| `OrderPersistenceTest` 만 빨강 | W2-7 이나 W2-4 가 아직 미완성입니다 |
| `Order.place()` 안에서 `orderItems.add()` 직접 호출 | 동작은 하지만 `addItem()` 을 부르세요. 검증을 건너뛰게 됩니다 |

---

## 과제 — 두 개 모두 필수입니다

### 과제 1 · `Product` 도 `Quantity` 를 쓰도록 바꾸기

지금 우리 코드에는 틈이 하나 있습니다.

```java
Product.decreaseStock(int quantity)                  // 1주차 코드
OrderItem.create(..., Quantity quantity)             // 오늘 만든 코드
```

같은 "수량"인데 한쪽은 `int`, 한쪽은 `Quantity` 입니다.
1주차에 `Product` 를 만들 때는 `Quantity` 가 없었으니까요. **이제 생겼으니 맞춰봅시다.**

**할 일**

- `Product.decreaseStock` 이 `Quantity` 를 받도록 바꾸세요
- `ProductService` 와 `ProductTest` 도 따라 고치세요
- **기존 테스트가 전부 초록이어야 합니다**

마지막 줄이 이 과제의 핵심입니다. 시그니처를 바꾸는 리팩터링인데 테스트가 그대로 초록이면
겉으로 드러나는 동작을 바꾸지 않았다는 뜻이죠. **1주차에 만든 테스트가 여기서 안전벨트가 됩니다.**

**같이 생각해 볼 것 (답을 적어 오세요)**

1. `Product` 의 검증 세 개 중 **하나가 없어집니다.** 어느 것이고, 왜 없어지나요?
2. `ProductService` 는 `int` 를 받아야 할까요, `Quantity` 를 받아야 할까요?
   원시 타입을 VO 로 바꾸는 경계를 어디에 두는 게 좋을까요?
3. `ProductTest` 의 `수량이_0_이하이면_예외가_발생한다` 는 고친 뒤에도 초록입니다.
   그런데 **이 테스트가 지금 검증하는 게 `Product` 가 맞나요?**

### 과제 2 · 부분 취소

주문 항목 하나만 취소하는 기능을 추가하세요.

```java
public void cancelItem(OrderItem item)
```

**할 일**

- `Order` 에 `cancelItem` 을 구현하세요
- 테스트를 **최소 3개** 작성하세요

**규칙끼리 부딪히는 지점이 있습니다**

- 항목을 하나 빼면 총액은 어떻게 되나요? (오늘 만든 `totalAmount` 를 다시 보세요)
- **마지막 항목을 빼면 규칙 1번("주문에는 항목이 1개 이상")은 어떻게 되죠?**

마지막 질문에는 **정답이 없습니다.** 마지막 항목은 못 빼게 막을 수도 있고,
빼면 주문 전체가 취소되게 할 수도 있어요.
**어떻게 정했는지, 왜 그렇게 정했는지**를 적어 오시면 됩니다.

> 참고: 실제 서비스라면 `cancelItem(Long orderItemId)` 처럼 id 로 받는 게 보통입니다.
> 여기서는 DB 없이 테스트할 수 있도록 `OrderItem` 을 직접 받게 했습니다.

### 제출

- 코드는 각자 브랜치에 커밋
- 위 질문들에 대한 답은 `docs/week2-assignment.md` 에 작성

**WIL 주제 (택 1)**

1. 가격을 `long price` 로 두었을 때와 `Money` VO 로 두었을 때 무엇이 달라졌는가
2. `new Order(...)` 를 열어두는 것과 `Order.place(...)` 만 열어두는 것은 불변식 관점에서 무엇이 다른가
