# Spring Boot 백엔드 심화 스터디 — 전체 6회 설계서

> 부제: 테스트와 도메인 설계부터 JPA 심화, AWS 배포까지
> 이 문서는 확정된 커리큘럼 방향을 실행 가능한 수준으로 구체화한 강사용 설계서입니다.
> **🔒 확정** = 논의를 거쳐 확정된 항목 · **🔸 선택** = 제가 추가로 제안하는 선택 사항(채택 전까지 확정안 아님)

---

## 0. 공통 규칙

### 0.1 기술 스택 (확정)

| 항목 | 값 |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 3.5.16 (Gradle 9.5.1 조합 빌드 검증 완료) |
| 빌드 | Gradle (Groovy DSL) |
| 1~5주차 DB | H2 (in-memory / file) |
| 6주차 실행 DB | AWS RDS PostgreSQL |
| 6주차 배포 | EC2 + `docker run` |
| 테스트 | JUnit 5 + AssertJ (+ Mockito는 3주차 확장에서만) |
| 문서화 | springdoc-openapi 2.8.17 (Swagger UI) |

### 0.2 시간 표기 규칙

모든 회차는 다음 세 단계로 분리합니다. **Core 40분 안에 수업 목표가 완결되어야 하며**, Extension이 진행되지 못해도 학습에 결손이 없어야 합니다.

- **Core (40분)** — 필수. 반드시 이 안에서 목표 달성 + 실습 결과물 산출
- **Extension (+20분, 총 60분)** — 시간이 남을 때만. 진도가 아니라 심화
- **WIL** — 수업 중 다루지 않고 글로 정리하게 하는 주제

### 0.3 저장소 · 브랜치 전략 (설계 핵심)

```
week0-baseline   ← 사전 배포. 기존 6주 학습 수준의 레거시 CRUD
week1-start      ← 1주차 수업 시작 시 checkout
week1-done       ← 1주차 완성본
week2-start      = week1-done + 2주차 보일러플레이트(학생이 안 쓸 코드)
week2-done
...
week6-done → main
```

**`weekN-start`를 매주 새로 배포하는 것이 이 과정의 생존 장치입니다.**
지난 주 실습을 못 끝냈거나 결석한 수강생도 `git checkout weekN-start` 한 줄로 복귀할 수 있어야 누적 이탈이 발생하지 않습니다. 6주 누적 프로젝트에서 3주차에 낙오하면 4~6주차가 통째로 무너지므로, 이 장치 없이는 커리큘럼이 성립하지 않습니다.

`weekN-start`에는 학생이 채울 자리를 다음 형식으로 표시합니다.

```java
// TODO-1: 수량이 0 이하이면 IllegalArgumentException 을 던지세요.
//         힌트: 메시지는 "수량은 1개 이상이어야 합니다"
throw new UnsupportedOperationException("TODO-1");
```

`UnsupportedOperationException`으로 채워두면 미구현 상태에서 테스트가 **컴파일은 되고 실행은 실패**하므로, 빨강→초록 전환이 그대로 보입니다.

### 0.4 최종 도달 패키지 구조 (6주차 완료 시점)

```
com.gdghongik.commerce
├── ProductApplication.java
├── presentation                      ← 3주차에 확정
│   ├── product/ProductController.java
│   ├── product/dto/DecreaseStockRequest.java
│   ├── order/OrderController.java
│   ├── order/dto/CreateOrderRequest.java
│   ├── order/dto/OrderResponse.java
│   └── advice/GlobalExceptionHandler.java   ← 4주차에 409 추가
├── application
│   ├── product/ProductService.java
│   ├── order/OrderService.java
│   ├── order/dto/CreateOrderCommand.java
│   └── order/dto/OrderResult.java
├── domain
│   ├── product/Product.java                 ← 1주차 행위, 4주차 @Version
│   ├── product/SellingStatus.java
│   ├── product/ProductRepository.java       ← 3주차 과제
│   ├── order/Order.java                     ← 2주차 Aggregate Root
│   ├── order/OrderLine.java
│   ├── order/OrderStatus.java
│   ├── order/OrderRepository.java           ← 3주차 (인터페이스)
│   └── common/Money.java, Quantity.java     ← 2주차 VO
└── infrastructure
    └── persistence
        ├── SpringDataOrderRepository.java   ← 5주차 fetch join 추가
        ├── OrderRepositoryAdapter.java
        ├── SpringDataProductRepository.java
        └── ProductRepositoryAdapter.java
```

### 0.5 전 회차 공통 설명 순서 원칙

수강생은 "CRUD는 짜봤지만 설계는 처음"인 단계입니다. 모든 회차를 다음 4박자로 고정합니다.

1. **문제를 먼저 눈으로 보여준다** — 깨진 테스트, 이상한 SQL 로그, 재고 음수. 개념 설명이 아니라 증상부터.
2. **왜 그런지 판서로 설명한다** — 코드가 아니라 그림/타임라인. 5~8분 이내.
3. **학생이 직접 고친다** — TODO 채우기. 이 구간이 회차당 20~26분으로 가장 길어야 함.
4. **고친 결과를 숫자로 확인한다** — 테스트 통과 개수, 쿼리 개수, HTTP 상태 코드, Service 줄 수.

**개념 → 코드 순서로 가르치지 않습니다. 증상 → 개념 → 코드 순서입니다.** DDD 용어를 먼저 던지면 이 레벨에서는 암기 과목이 됩니다.

---

## 1. WEEK 1 — 테스트로 시작하는 도메인 리팩터링

`JUnit` · `AssertJ` · `도메인 리팩터링` · `Entity 행위`

### 1.1 구체적 학습 목표 (수업 후 학생이 할 수 있어야 하는 것)

1. `@Test`, given-when-then 구조로 테스트 메서드를 직접 작성할 수 있다.
2. `assertThat(...).isEqualTo(...)` 와 `assertThatThrownBy(...).isInstanceOf(...)` 를 구분해 쓸 수 있다.
3. 테스트 실패 메시지를 읽고 "기대값 / 실제값 / 실패 지점"을 지목할 수 있다.
4. Service 메서드에서 **검증 · 계산 · 상태 변경**을 골라내 Entity 메서드로 옮길 수 있다.
5. 리팩터링 전후로 테스트가 동일하게 통과하는 것을 확인하고, 테스트가 안전망 역할을 했다고 설명할 수 있다.
6. setter를 열어둔 Entity에서 규칙이 깨질 수 있는 지점을 한 가지 이상 말할 수 있다.

### 1.2 시간 배분

**Core 40분**

| 시간 | 구간 | 내용 |
|---|---|---|
| 0:00–0:05 | 도입 | Before/After 코드 1장 비교. "오늘 이 20줄을 3줄로 만들고, 그게 안전하다는 걸 테스트로 증명한다" |
| 0:05–0:12 | 강사 라이브 | `ProductServiceTest` 첫 케이스를 강사가 처음부터 타이핑. given-when-then / assertThat / assertThatThrownBy 4개 문법만 |
| 0:12–0:22 | **실습 1** | TODO-1~3: 실패 케이스 3개 채우기 (수량 0 이하 / 재고 부족 / 판매중지 상품) |
| 0:22–0:32 | **실습 2** | TODO-4~5: `Product.decreaseStock()` 구현 → `ProductService` 축소 → 테스트 재실행 (그대로 초록) |
| 0:32–0:38 | **실습 3** | TODO-6: 규칙 테스트를 `ProductTest`(순수 단위)로 이동. 실행 시간 비교 (예: 2.4초 → 0.03초) |
| 0:38–0:40 | 정리 | Service 줄 수 diff 확인 + 과제 안내 |

**Extension +20분**

| 시간 | 내용 |
|---|---|
| +0~6 | 재고 0이 되면 `SOLD_OUT` 자동 전환 규칙 추가 + 테스트 (규칙을 도메인에 넣으면 어디에나 자동 적용됨을 체감) |
| +6~12 | `IllegalStateException` → `OutOfStockException` 도메인 예외로 분리 |
| +12~18 | 일부러 테스트를 깨뜨리고 실패 메시지 읽기 훈련 (`hasMessage`, `hasMessageContaining`) |
| +18~20 | 정리 |

### 1.3 시작 코드 vs 완성 코드

**`week1-start` — 레거시 상태**

```java
// application 계층이 아직 없음. service 패키지 그대로.
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다"));

        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
        }
        if (product.getStatus() != SellingStatus.SELLING) {
            throw new IllegalStateException("판매 중인 상품이 아닙니다");
        }
        if (product.getStock() < quantity) {
            throw new IllegalStateException("재고가 부족합니다");
        }

        product.setStock(product.getStock() - quantity);
        if (product.getStock() == 0) {
            product.setStatus(SellingStatus.SOLD_OUT);
        }
        productRepository.save(product);
    }
}
```

`Product`는 `@Getter @Setter`가 붙은 순수 데이터 홀더입니다.

**`week1-done` — 완성 상태**

```java
@Transactional
public void decreaseStock(Long productId, int quantity) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다"));
    product.decreaseStock(quantity);   // 규칙은 전부 여기 안으로
    productRepository.save(product);
}
```

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id @GeneratedValue
    private Long id;
    private String name;
    private long price;
    private int stock;
    @Enumerated(EnumType.STRING)
    private SellingStatus status;

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
        }
        if (this.status != SellingStatus.SELLING) {
            throw new IllegalStateException("판매 중인 상품이 아닙니다");
        }
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다");
        }
        this.stock -= quantity;
        if (this.stock == 0) {
            this.status = SellingStatus.SOLD_OUT;
        }
    }
}
```

**핵심 diff 요약** — `@Setter` 제거 · Service 20줄 → 3줄 · Product +20줄 · 테스트 파일 1개 추가

### 1.4 학생 TODO

| # | 파일 | 할 일 |
|---|---|---|
| 1 | `ProductServiceTest` | 수량이 0 이하일 때 예외가 발생하는지 검증 |
| 2 | `ProductServiceTest` | 재고보다 많이 주문하면 예외가 발생하는지 검증 |
| 3 | `ProductServiceTest` | 판매 중이 아닌 상품이면 예외가 발생하는지 검증 |
| 4 | `Product` | `decreaseStock(int quantity)` 구현 (검증 3개 + 차감) |
| 5 | `ProductService` | 조회 → 행위 호출 → 저장 3줄로 축소, setter 호출 제거 |
| 6 | `ProductTest` | 4개 케이스를 Spring 없이 도는 순수 단위 테스트로 이동 |

### 1.5 강사 사전 준비물

- `week0-baseline`, `week1-start`, `week1-done` 브랜치
- `ProductServiceTest`의 **정상 케이스 1개는 완성 상태로 제공** (형식을 보고 따라 쓰게)
- Before/After 코드 슬라이드 1장 (좌우 비교)
- 테스트 실패 화면 캡처 (빨강 → 초록)
- 자주 나오는 오류 대응 노트: `Product` 기본 생성자 누락, `@Entity` 인식 실패, AssertJ import 혼동(`org.assertj.core.api.Assertions.assertThat`)

### 1.6 실습 결과물 (완료 판정 기준)

- `ProductTest` 4개 이상 초록
- `ProductService.decreaseStock` 이 3줄이고 `set`으로 시작하는 호출이 0건
- `Product`에 `@Setter`가 없음
- `ProductTest` 실행에 Spring 컨텍스트가 뜨지 않음 (실행 시간으로 확인)

### 1.7 과제 · WIL

**과제 (🔒 확정): 주문 도메인 분석 — 상태·행위·비즈니스 규칙 찾기**
자연어 분석 과제. 코드 구현 없음. 제출 양식:

```markdown
1. 핵심 용어 (5개 이상)   — 용어 / 한 줄 정의
2. 주요 행위 (5개 이상)   — 누가 무엇을 한다
3. 주문 상태와 상태 전이  — 상태 목록 + "A → B 는 언제 가능한가"
4. 비즈니스 규칙 (5개 이상) — "~해야 한다 / ~할 수 없다" 형식
5. 잘못된 상황 (3개 이상)   — 이 규칙이 깨지면 무슨 사고가 나는가
6. 각 규칙을 담당할 객체 후보 — 규칙 / 담당 후보 / 그렇게 생각한 이유
```

Entity·VO·Aggregate 분류는 요구하지 않습니다(2주차 주제). 6번은 "정답"이 아니라 "그렇게 생각한 이유"를 평가합니다.

**WIL 주제:** setter로 상태를 바꿀 때와 메서드로 바꿀 때, 규칙이 깨지는 지점은 어디가 다른가

---

## 2. WEEK 2 — Entity·Value Object·Aggregate와 불변식

`Entity` · `Value Object` · `Aggregate Root` · `불변식`

### 2.1 구체적 학습 목표

1. 식별자 유무 / 동등성 판단 기준 / 가변성 3가지로 Entity와 VO를 구분할 수 있다.
2. `Money`, `Quantity`를 VO로 구현하고 검증을 **생성자**에 둘 수 있다.
3. Aggregate Root가 "외부에서 들어오는 유일한 문"이라는 역할을 설명할 수 있다.
4. Order Aggregate의 불변식 6개를 코드로 표현하고 각각에 대응하는 테스트를 지목할 수 있다.
5. `getOrderLines()`가 수정 가능한 리스트를 반환할 때 불변식이 깨지는 경로를 설명할 수 있다.
6. 1주차 과제에서 자신이 찾은 규칙이 어느 객체로 갔는지 매핑할 수 있다.

### 2.2 시간 배분

**Core 40분**

| 시간 | 구간 | 내용 |
|---|---|---|
| 0:00–0:05 | 과제 정산 | **1주차 과제 표준 분석 답안 배포**. 학생 답안과 비교 (편차 흡수 장치) |
| 0:05–0:12 | 개념 | Entity vs VO 판별표 → Aggregate/Root → "왜 Order 밖에서 OrderLine을 못 만지게 하나" |
| 0:12–0:20 | **실습 1** | TODO-1~3: `Money`, `Quantity` 생성자 검증 + 계산 메서드 |
| 0:20–0:32 | **실습 2** | TODO-4~7: `Order`의 `addLine` / `cancel` / `totalAmount` / 생성 검증 |
| 0:32–0:38 | **실습 3** | TODO-8: `getOrderLines()` 방어 + 불변식 테스트 전체 실행 |
| 0:38–0:40 | 정리 | 불변식 ↔ 테스트 매핑표 확인 |

**Extension +20분**

| 시간 | 내용 |
|---|---|
| +0~7 | `Money.equals/hashCode` 직접 구현 → VO 동등성 vs Entity 동일성 테스트로 비교 |
| +7~14 | `Money`를 `@Embeddable`로 매핑 → H2 콘솔에서 실제 컬럼 확인 |
| +14~18 | `OrderStatus`에 `canCancel()` 전이 메서드 추가 (enum이 규칙을 갖는 형태) |
| +18~20 | 정리 |

### 2.3 시작 코드 vs 완성 코드

**⚠️ 이 회차가 40분 압박이 가장 큽니다.** VO 2개 + Aggregate + 불변식 6개를 백지에서 40분에 쓰는 것은 불가능하므로, **껍데기는 전부 제공하고 학생은 규칙 본문만 채웁니다.**

`week2-start`가 **제공하는 것** (학생이 작성하지 않음):
- `Money`, `Quantity` 클래스 골격 + `equals`/`hashCode` 완성본
- `OrderLine` 전체 (필드, 생성자, JPA 매핑)
- `OrderStatus` enum
- `Order`의 필드 · JPA 연관관계 매핑(`@OneToMany(cascade = ALL, orphanRemoval = true)`) · 생성자 시그니처
- `OrderTest`, `MoneyTest`, `QuantityTest`에 **테스트 이름만 있고 본문이 비어 실패하는** 케이스 목록

`week2-start`가 **비워두는 것** (학생 TODO): 생성자 검증문, 행위 메서드 본문, 컬렉션 방어 코드.

**완성 코드 발췌**

```java
public class Money {
    public static final Money ZERO = new Money(0L);
    private final long amount;

    public Money(long amount) {
        if (amount < 0) {                                   // TODO-1
            throw new IllegalArgumentException("금액은 0원 이상이어야 합니다");
        }
        this.amount = amount;
    }

    public Money add(Money other) { return new Money(this.amount + other.amount); }      // TODO-2
    public Money multiply(Quantity q) { return new Money(this.amount * q.value()); }     // TODO-2
}
```

```java
@Entity
@Table(name = "orders")
public class Order {

    @Id @GeneratedValue private Long id;
    @Enumerated(EnumType.STRING) private OrderStatus status;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();

    public void addLine(Product product, Quantity quantity) {      // TODO-4
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("주문 확정 이후에는 항목을 변경할 수 없습니다");
        }
        this.orderLines.add(OrderLine.of(this, product, quantity));
    }

    public void cancel() {                                          // TODO-5
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송이 시작된 주문은 취소할 수 없습니다");
        }
        this.status = OrderStatus.CANCELED;
    }

    public Money totalAmount() {                                    // TODO-6
        return orderLines.stream().map(OrderLine::subtotal)
                         .reduce(Money.ZERO, Money::add);
    }

    public List<OrderLine> getOrderLines() {                        // TODO-8
        return Collections.unmodifiableList(orderLines);
    }
}
```

### 2.4 불변식 ↔ 코드 ↔ 테스트 매핑 (수업 중 화면에 띄울 표)

| # | 불변식 | 보호 위치 | 테스트 이름 |
|---|---|---|---|
| 1 | 주문에는 항목이 1개 이상 | `Order` 생성 시 검증 | `주문_항목이_없으면_주문을_생성할_수_없다` |
| 2 | 수량은 1개 이상 | `Quantity` 생성자 | `수량이_0이면_예외가_발생한다` |
| 3 | 가격은 음수 불가 | `Money` 생성자 | `금액이_음수면_예외가_발생한다` |
| 4 | 항목 변경은 Order 경유 | `getOrderLines()` 불변 반환 + `addLine` | `주문항목_리스트를_외부에서_수정할_수_없다` |
| 5 | 총액 = 항목 합계 | `Order.totalAmount()` | `주문_총액은_항목_금액의_합과_같다` |
| 6 | 배송 시작 후 취소 불가 | `Order.cancel()` | `배송이_시작된_주문은_취소할_수_없다` |

### 2.5 학생 TODO

| # | 파일 | 할 일 |
|---|---|---|
| 1 | `Money` | 생성자에서 음수 금액 거부 |
| 2 | `Money` | `add`, `multiply` — 새 인스턴스를 반환(불변) |
| 3 | `Quantity` | 생성자에서 1 미만 거부 |
| 4 | `Order` | `addLine` — CREATED 상태에서만 허용 |
| 5 | `Order` | `cancel` — SHIPPED/DELIVERED 취소 금지 |
| 6 | `Order` | `totalAmount` — 항목 합계 |
| 7 | `Order` | 정적 팩터리에서 항목 0개 거부 |
| 8 | `Order` | `getOrderLines()` 불변 리스트 반환 |

### 2.6 강사 사전 준비물

- **1주차 과제 표준 분석 답안** (배포용 1페이지) — 이게 없으면 학생별 분석 편차 때문에 2주차 시작점이 흔들립니다
- `week2-start` / `week2-done`
- Entity vs VO 판별표 슬라이드 1장
- 불변식 매핑표 (2.4) 인쇄물 또는 화면 고정
- `OrderLine.subtotal()`, `OrderLine.of()` 완성 제공
- 자주 나오는 오류: `Order`↔`OrderLine` 양방향 연결 누락으로 `order_id` null, VO에 기본 생성자 없어서 JPA 로딩 실패

### 2.7 실습 결과물

- `MoneyTest` / `QuantityTest` / `OrderTest` 합계 8개 이상 초록
- 불변식 6개가 모두 "그 규칙을 어긴 테스트가 실패로 잡히는" 상태
- `Order` 외부에서 `orderLines.add(...)` 를 시도하면 컴파일 또는 런타임에 막힘

### 2.8 과제 · WIL

**과제:** Order Aggregate에 **"라인 단위 부분 취소"** 규칙을 추가한다고 할 때
(a) 새로 필요한 불변식과 (b) 기존 불변식 중 수정이 필요한 것을 찾고, (c) `cancelLine(Long orderLineId)` 를 구현 + 테스트 2개 작성.
→ 총액 재계산, 전량 취소 시 주문 상태 전이, "항목 1개 이상" 불변식과의 충돌이 자연스럽게 드러납니다.

**WIL 주제:** 가격을 `long price`로 두었을 때와 `Money` VO로 두었을 때 무엇이 달라졌는가 (검증 위치, 중복, 타입 안정성 관점)

---

## 3. WEEK 3 — 4계층 아키텍처와 DIP: Port–Adapter 맛보기

`4계층 아키텍처` · `DIP` · `Repository 추상화` · `Port–Adapter`

### 3.1 구체적 학습 목표

1. Presentation / Application / Domain / Infrastructure 각각의 책임을 한 문장으로 말할 수 있다.
2. Application이 `JpaRepository`에 직접 의존할 때 생기는 문제를 **테스트 실행 시간**으로 체감하고 설명할 수 있다.
3. Domain에 `OrderRepository` 인터페이스를 정의하고 JPA 타입이 시그니처에 노출되지 않게 작성할 수 있다.
4. Infrastructure에 Adapter를 두어 인터페이스를 구현하고, 생성자 주입으로 연결되는 과정을 설명할 수 있다.
5. `FakeOrderRepository`로 DB 없이 Application Service를 테스트할 수 있다.
6. Port(인터페이스)와 Adapter(구현)라는 용어로 자신의 코드 구조를 설명할 수 있다.

### 3.2 시간 배분

**Core 40분**

| 시간 | 구간 | 내용 |
|---|---|---|
| 0:00–0:06 | 개념 | 4계층 책임 + 의존 방향 화살표 그림 1장 |
| 0:06–0:12 | 문제 목격 | 현재 `OrderServiceTest` 실행 → Spring 컨텍스트 + H2 기동 시간 관찰. "주문 규칙 하나 검증하려고 DB가 뜬다" |
| 0:12–0:20 | **실습 1** | TODO-1~2: Domain에 `OrderRepository` 정의 → Application 의존 타입 교체 → **컴파일 에러 발생** |
| 0:20–0:28 | **실습 2** | TODO-3: Infrastructure에 `OrderRepositoryAdapter` 작성 → 앱 기동 성공 |
| 0:28–0:36 | **실습 3** | TODO-4~5: `FakeOrderRepository` 작성 → `@SpringBootTest` 제거 → 재실행 시간 비교 |
| 0:36–0:40 | 정리 | Port–Adapter 그림 + TODO-6 의존성 점검 + 다음 주 예고 |

0:12–0:20 구간에서 **의도적으로 컴파일 에러를 내는 것이 이 회차의 교육 장치**입니다. "인터페이스만 만들었더니 스프링이 주입할 구현체를 못 찾는다" → Adapter의 필요성이 스스로 도출됩니다.

**Extension +20분**

| 시간 | 내용 |
|---|---|
| +0~8 | 같은 테스트를 Mockito로 작성 → Fake와 비교. 언제 무엇을 쓰는가 (상태 검증 vs 호출 검증) |
| +8~14 | 두 번째 Port 시연: `NotificationSender` 인터페이스 + Fake 구현 (Repository 말고도 적용됨을 확인) |
| +14~18 | 🔸 선택: ArchUnit으로 "application 패키지는 infrastructure를 import할 수 없다"를 테스트로 고정 |
| +18~20 | 정리 |

### 3.3 시작 코드 vs 완성 코드

**⚠️ 패키지 대이동은 강사가 미리 끝내둡니다.** 파일을 옮기고 import를 고치는 작업은 학습 가치가 없으면서 15분을 먹습니다.

`week3-start`가 **제공하는 것**:
- 이미 `presentation / application / domain / infrastructure` 4개 패키지로 재배치 완료된 코드
- `CreateOrderCommand`, `OrderResult` 완성본
- `SpringDataOrderRepository extends JpaRepository<Order, Long>` (Infrastructure에 위치)
- **단, `OrderService`가 `SpringDataOrderRepository`를 직접 주입받는 상태** ← 오늘 고칠 문제
- `OrderServiceTest`가 `@SpringBootTest`로 되어 있는 상태

**Before (week3-start)**

```java
package com.gdghongik.commerce.application.order;

import com.gdghongik.commerce.infrastructure.persistence.SpringDataOrderRepository; // ← 아래 계층을 올려다봄

@Service
@RequiredArgsConstructor
public class OrderService {
    private final SpringDataOrderRepository orderRepository;   // 구현에 직접 의존
    ...
}
```

**After (week3-done)**

```java
// domain/order/OrderRepository.java  ← Port
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
}
```

```java
// infrastructure/persistence/OrderRepositoryAdapter.java  ← Adapter
@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository jpaRepository;

    @Override public Order save(Order order) { return jpaRepository.save(order); }
    @Override public Optional<Order> findById(Long id) { return jpaRepository.findById(id); }
    @Override public List<Order> findAll() { return jpaRepository.findAll(); }
}
```

```java
// application/order/OrderService.java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;   // Domain의 추상화만 앎
    ...
}
```

```java
// test — infrastructure 없이 도는 Fake
public class FakeOrderRepository implements OrderRepository {
    private final Map<Long, Order> store = new HashMap<>();
    private long sequence = 0L;

    @Override public Order save(Order order) {
        if (order.getId() == null) ReflectionTestUtils.setField(order, "id", ++sequence);
        store.put(order.getId(), order);
        return order;
    }
    @Override public Optional<Order> findById(Long id) { return Optional.ofNullable(store.get(id)); }
    @Override public List<Order> findAll() { return new ArrayList<>(store.values()); }
}
```

### 3.4 학생 TODO

| # | 파일 | 할 일 |
|---|---|---|
| 1 | `domain/order/OrderRepository` | `save`, `findById`, `findAll` 정의. **JPA/Spring Data 타입을 시그니처에 쓰지 말 것** |
| 2 | `application/order/OrderService` | 주입 타입을 `OrderRepository`로 교체 (여기서 컴파일 에러 발생) |
| 3 | `infrastructure/.../OrderRepositoryAdapter` | 인터페이스 구현 + `@Repository` 등록 → 기동 성공 확인 |
| 4 | `test/.../FakeOrderRepository` | `HashMap` 기반 구현 |
| 5 | `OrderServiceTest` | `@SpringBootTest` 제거, Fake 주입해서 직접 생성 |
| 6 | (점검) | application 패키지에 아래 import가 0건인지 확인 |

TODO-6 자가 점검 명령:

```bash
grep -rn "org.springframework.data\|jakarta.persistence\|infrastructure" src/main/java/com/gdghongik/commerce/application/ | wc -l
```

결과가 `0`이면 DIP가 실제로 적용된 것입니다. 말이 아니라 숫자로 확인시키는 것이 포인트입니다.

### 3.5 강사 사전 준비물

- `week3-start`(패키지 재배치 완료) / `week3-done`
- 4계층 의존 방향 그림 1장 — **화살표가 Infrastructure → Domain으로 거꾸로 꺾이는 지점을 강조**
- `CreateOrderCommand` / `OrderResult` / Controller 매핑 완성본
- `ReflectionTestUtils` 사용 이유 설명 노트 (Fake에서 id 부여)
- 자주 나오는 오류: `@Repository` 누락 → `NoSuchBeanDefinitionException` / 인터페이스와 Adapter의 시그니처 불일치

### 3.6 실습 결과물

- Application 패키지의 JPA·Spring Data import 0건 (위 grep로 확인)
- `OrderServiceTest`가 Spring 없이 통과, 실행 시간 1초 미만
- 애플리케이션 기동 및 기존 API 정상 동작 (구조만 바뀌고 동작은 동일)

### 3.7 과제 · WIL

**과제:** `Product`에도 동일한 DIP를 적용한다. `domain/product/ProductRepository` 인터페이스 정의 → `ProductRepositoryAdapter` 구현 → `ProductService` 의존 교체 → `FakeProductRepository`로 1주차 테스트 재실행.
→ **이 과제 결과가 곧 `week4-start`입니다.** 4주차 동시성 실습이 Product를 쓰기 때문에, 여기서 Product 계층을 정리해두면 다음 주가 그대로 이어집니다.

**WIL 주제:** Repository 인터페이스를 Domain에 두는 것과 Infrastructure에 두는 것은 무엇이 다른가 (의존 방향 관점에서)

---

## 4. WEEK 4 — 동시 주문으로 배우는 영속성 컨텍스트와 DB Lock

`영속성 컨텍스트` · `트랜잭션` · `Lost Update` · `낙관적 락` · `비관적 락`

### 4.1 구체적 학습 목표

1. 영속성 컨텍스트 · 변경 감지 · flush · commit의 순서를 타임라인으로 그릴 수 있다.
2. `@Transactional`이 원자성은 보장하지만 **다른 트랜잭션이 같은 행을 동시에 읽는 것은 막지 않는다**고 설명할 수 있다.
3. 두 트랜잭션의 read–check–write 타임라인을 겹쳐 그리고 Lost Update가 발생하는 지점을 지목할 수 있다.
4. `@Version` 하나로 낙관적 락이 동작하는 원리(UPDATE ... WHERE version = ?)를 SQL로 설명할 수 있다.
5. 낙관적 락 충돌 예외를 HTTP 409로 변환할 수 있다.
6. 낙관적 락과 비관적 락의 선택 기준(충돌 빈도)을 말할 수 있다.

### 4.2 시간 배분

**Core 40분**

| 시간 | 구간 | 내용 |
|---|---|---|
| 0:00–0:08 | 복습 | 영속성 컨텍스트 / 변경 감지 / flush / commit 타임라인 판서. "언제 SQL이 나가는가" |
| 0:08–0:14 | **문제 목격** | 강사 제공 `ConcurrentStockDecreaseTest` 실행 → 재고 1개인데 주문 2건 성공. 로그로 확인 |
| 0:14–0:20 | 원인 분석 | 두 트랜잭션 타임라인 겹쳐 그리기. `@Transactional`을 붙여도 안 막히는 이유 |
| 0:20–0:28 | **실습 1** | TODO-1~2: `@Version` 추가 → 재실행 → 1건 성공 / 1건 `ObjectOptimisticLockingFailureException` |
| 0:28–0:36 | **실습 2** | TODO-3: 예외 → 409 Conflict 매핑 + API 레벨 확인 |
| 0:36–0:40 | 정리 | 낙관/비관 비교표 + TODO-4(판단 근거 주석) 안내 |

**Extension +20분**

| 시간 | 내용 |
|---|---|
| +0~8 | 비관적 락: `@Lock(LockModeType.PESSIMISTIC_WRITE)` 적용 → 두 요청 모두 순차 성공하는 모습 관찰 |
| +8~13 | `SELECT ... FOR UPDATE` 로그 확인, Lock Timeout · Deadlock 개념 소개 |
| +13~18 | 충돌 시 재시도 루프(최대 3회) 구현 후 결과 비교 |
| +18~20 | **H2 ≠ PostgreSQL/MySQL 경고** — 락 동작·타임아웃·데드락은 실제 DB와 다를 수 있음 |

### 4.3 시작 코드 vs 완성 코드

`week4-start` 제공물의 핵심은 **동시성 테스트 하네스 전체**입니다. 학생에게 `ExecutorService` / `CountDownLatch`를 백지에서 쓰게 하지 않습니다.

```java
// 강사 제공 — 학생은 읽고 실행만 한다
@SpringBootTest
class ConcurrentStockDecreaseTest {

    @Autowired ProductService productService;
    @Autowired SpringDataProductRepository productRepository;

    @Test
    void 재고가_1개일_때_동시에_2건이_주문하면_1건만_성공해야_한다() throws Exception {
        Product product = productRepository.save(Product.of("한정판 키보드", 100_000L, 1));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(threadCount);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();                                  // 동시 출발
                    productService.decreaseStock(product.getId(), 1);
                    success.incrementAndGet();
                } catch (Exception e) {
                    failure.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await();

        Product found = productRepository.findById(product.getId()).orElseThrow();
        System.out.printf("성공=%d 실패=%d 남은재고=%d%n", success.get(), failure.get(), found.getStock());

        assertThat(success.get()).isEqualTo(1);   // @Version 적용 전에는 여기서 실패한다
        assertThat(found.getStock()).isZero();
    }
}
```

> **강사 주의:** 이 테스트 클래스에 `@Transactional`을 붙이면 안 됩니다. 테스트 트랜잭션이 롤백되고 스레드가 같은 영속성 컨텍스트를 공유하지 않아 시나리오가 성립하지 않습니다. 대신 `@AfterEach`에서 수동으로 데이터를 정리하고, H2는 `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`로 둡니다.

**완성 코드의 diff는 단 두 줄입니다.**

```java
@Entity
public class Product {
    @Id @GeneratedValue private Long id;
    ...
    @Version
    private Long version;      // ← 이 한 줄
}
```

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ObjectOptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("STOCK_CONFLICT", "다른 주문이 먼저 처리되었습니다. 다시 시도해 주세요."));
    }
}
```

**"두 줄로 해결되는 문제를, 두 줄이 왜 필요한지 아는 데 40분을 쓴다"** 는 것이 이 회차의 성격입니다. 코드 분량이 적은 대신 원리 설명 시간이 깁니다.

### 4.4 학생 TODO

| # | 파일 | 할 일 |
|---|---|---|
| 1 | `Product` | `@Version private Long version;` 추가 |
| 2 | (실행) | 동시성 테스트 재실행 → 성공 1 / 실패 1 / 재고 0 확인, 콘솔 출력 기록 |
| 3 | `GlobalExceptionHandler` | 낙관적 락 예외 → 409 Conflict 매핑 |
| 4 | `ProductService` | 충돌 시 **재시도할지 그냥 실패시킬지** 결정하고, 그 판단 근거를 주석으로 3줄 작성 |

TODO-4는 코드가 아니라 **설계 판단 훈련**입니다. 정답은 없고 "재고 차감은 사용자에게 실패를 알리는 편이 안전하다 / 결제 이후라면 재시도가 낫다" 같은 근거를 쓰게 합니다.

### 4.5 강사 사전 준비물

- `ConcurrentStockDecreaseTest` 전체 (위 코드)
- 성공/실패 카운트 출력 유틸 (콘솔에 바로 보이게)
- 두 트랜잭션 타임라인 판서 자료 (T1 read → T2 read → T1 write → T2 write)
- `@Version` 적용 후 실제로 나가는 UPDATE SQL 캡처 (`where id=? and version=?`)
- 낙관/비관 비교표 슬라이드
- H2 테스트 설정 (`MODE=PostgreSQL`), 테스트 간 데이터 정리 `@AfterEach`
- 확장용 `findByIdForUpdate` 완성본

### 4.6 실습 결과물

- `@Version` 적용 후 동시성 테스트 초록 (성공 1 / 실패 1 / 재고 0)
- 충돌 시 API 응답이 500이 아니라 **409**
- UPDATE SQL에 `version = ?` 조건이 포함된 로그 캡처

### 4.7 과제 · WIL

**과제:** 재고 1개 상품에 **동시 주문 10건**을 넣고 성공/실패 분포를 측정한다. 스레드 수를 2 → 10으로 바꾸고 3회 반복 실행해 결과를 표로 기록한 뒤, 성공이 항상 정확히 1건인지 확인하고 그 이유를 설명한다.

**WIL 주제 (비관적 락 담당):** 우리 커머스의 재고 차감 시나리오에는 낙관적 락과 비관적 락 중 무엇이 맞는가. 충돌 빈도 · 대기 시간 · 데드락 위험을 근거로 서술. H2에서 검증한 결과를 PostgreSQL에 그대로 믿을 수 없는 이유도 한 문단 포함.

---

## 5. WEEK 5 — JPA 연관관계 조회와 N+1

`연관관계` · `LAZY` · `EAGER` · `N+1` · `Fetch Join`

### 5.1 구체적 학습 목표

1. LAZY 연관관계가 프록시로 로딩되고, 접근 시점에 추가 SQL이 나간다는 것을 로그로 확인할 수 있다.
2. Order 10건 조회 후 OrderLine 접근 시 쿼리가 11개 나가는 이유를 설명할 수 있다.
3. **EAGER로 바꿔도 `findAll`에서는 여전히 N+1이 난다**는 것을 직접 확인하고, EAGER가 해결책이 아닌 이유를 말할 수 있다.
4. JPQL `join fetch`를 작성해 쿼리를 1개로 줄일 수 있다.
5. 3주차에 만든 Adapter 구조에서 조회 최적화가 어느 계층의 일인지 지목할 수 있다.
6. fetch join의 한계(컬렉션 페이징, 컬렉션 2개 이상)를 한 가지 이상 말할 수 있다.

### 5.2 시간 배분

**Core 40분**

| 시간 | 구간 | 내용 |
|---|---|---|
| 0:00–0:06 | 개념 | 연관관계와 프록시. "LAZY는 안 가져온 게 아니라 나중에 가져온다" |
| 0:06–0:14 | **실습 1** | TODO-1~2: SQL 로그 켜고 Order 10건 조회 → 라인 접근 → 쿼리 수 세기(11개) |
| 0:14–0:20 | 원인 분석 | 1(목록) + N(각 건) 판서 |
| 0:20–0:28 | **실습 2** | TODO-3: `fetch = EAGER`로 바꿔 재실행 → **여전히 11개**. 결과 기록 |
| 0:28–0:36 | **실습 3** | TODO-4~6: `join fetch` JPQL 작성 → 쿼리 1개 → Adapter 연결 |
| 0:36–0:40 | 정리 | `distinct` / 페이징 경고 + N+1과 인덱스의 층위 구분 |

0:20–0:28의 **EAGER 실험이 이 회차의 핵심 교육 장치**입니다. "N+1이면 EAGER로 바꾸면 되지 않나"는 이 레벨에서 거의 모두가 떠올리는 오해인데, 직접 돌려보고 쿼리 수가 그대로인 것을 보면 말로 설명하는 것보다 훨씬 강하게 남습니다.

**Extension +20분**

| 시간 | 내용 |
|---|---|
| +0~6 | `@BatchSize` / `hibernate.default_batch_fetch_size` → 11개가 2개로 줄어드는 것 확인 |
| +6~12 | fetch join + 페이징 → `HHH000104 firstResult/maxResults specified with collection fetch` 경고 재현 |
| +12~17 | DTO Projection으로 필요한 컬럼만 조회 |
| +17~20 | 🔸 선택 (강사 시연만): `EXPLAIN`과 인덱스 — "N+1은 쿼리 개수 문제, 인덱스는 쿼리 한 번의 비용 문제" |

### 5.3 시작 코드 vs 완성 코드

`week5-start` 제공물:
- 테스트 데이터 시더 (Order 10건 × OrderLine 3건) — `@BeforeEach`에서 실행
- SQL 쿼리 카운터 유틸 (Hibernate `Statistics` 기반)
- `application-test.yml`의 로그 설정 (주석 처리된 상태 — TODO-1에서 학생이 켬)
- `OrderQueryTest`에 쿼리 수를 단언하는 실패 테스트

```java
// 강사 제공 — 쿼리 수를 자동으로 세는 유틸
public class QueryCounter {
    private final Statistics statistics;

    public QueryCounter(EntityManagerFactory emf) {
        this.statistics = emf.unwrap(SessionFactory.class).getStatistics();
        this.statistics.setStatisticsEnabled(true);
        this.statistics.clear();
    }
    public long count() { return statistics.getPrepareStatementCount(); }
}
```

**Before (week5-start) — Adapter의 findAll**

```java
@Override
public List<Order> findAll() {
    return jpaRepository.findAll();          // 1 + N
}
```

**After (week5-done)**

```java
// infrastructure/persistence/SpringDataOrderRepository.java
public interface SpringDataOrderRepository extends JpaRepository<Order, Long> {

    @Query("select distinct o from Order o join fetch o.orderLines")
    List<Order> findAllWithLines();
}
```

```java
// infrastructure/persistence/OrderRepositoryAdapter.java
@Override
public List<Order> findAll() {
    return jpaRepository.findAllWithLines();   // 1
}
```

> **여기가 3주차 구조의 회수 지점입니다.** Domain의 `OrderRepository` 인터페이스도, Application의 `OrderService`도 **한 글자도 바뀌지 않았는데** 쿼리가 11개에서 1개로 줄었습니다. "조회 성능 최적화는 Infrastructure의 관심사"라는 것을 코드로 증명하는 순간이므로, 이 문장을 반드시 짚고 넘어가세요.

### 5.4 학생 TODO

| # | 파일 | 할 일 |
|---|---|---|
| 1 | `application-test.yml` | `show-sql`, `format_sql`, `org.hibernate.SQL: debug` 활성화 |
| 2 | `OrderQueryTest` | `findAll()` 후 각 Order의 라인에 접근하고 쿼리 수 기록 (11개) |
| 3 | `Order` / `OrderLine` | fetch 전략을 EAGER로 바꿔 재실행 → 쿼리 수 기록 → **LAZY로 되돌리기** |
| 4 | `SpringDataOrderRepository` | `@Query`로 `join fetch` 작성 |
| 5 | `OrderRepositoryAdapter` | `findAll()`이 새 메서드를 쓰도록 연결 |
| 6 | `OrderQueryTest` | `assertThat(counter.count()).isEqualTo(1)` 로 단언 → 초록 |

TODO-3에서 **LAZY로 되돌리는 것까지가 TODO**입니다. 되돌리지 않으면 6주차까지 EAGER가 남습니다.

### 5.5 강사 사전 준비물

- 테스트 데이터 시더 + `QueryCounter` 유틸
- SQL 로그 설정 (주석 처리 상태로 배포)
- 1 + N 판서 자료
- EAGER 실험 결과 기록표 (학생이 숫자를 적어넣는 빈 표)

| 실행 조건 | 예상 쿼리 수 | 실제 쿼리 수 |
|---|---|---|
| LAZY + `findAll` + 라인 접근 | | |
| EAGER + `findAll` + 라인 접근 | | |
| LAZY + `join fetch` | | |

- 확장용: `@BatchSize` 설정본, 페이징 경고 재현 코드, DTO Projection 완성본

### 5.6 실습 결과물

- 위 3행 표가 숫자로 채워짐 (11 / 11 / 1)
- `OrderQueryTest`의 쿼리 수 단언이 1로 통과
- Domain·Application 계층 diff가 0줄인 상태로 성능 개선 완료

### 5.7 과제 · WIL

**과제:** 주문 **단건 상세 조회** API(`GET /orders/{id}`)에서 실행되는 쿼리 수를 세고, 필요하면 fetch join을 적용해 before/after를 기록한다. 목록 조회와 단건 조회 중 어느 쪽이 N+1에 취약한지, 그 이유는 무엇인지 3줄로 정리.

**WIL 주제:** N+1 문제와 인덱스 문제는 각각 어느 층의 문제인가. 둘을 혼동하면 어떤 잘못된 처방이 나오는가.

---

## 6. WEEK 6 — Docker 이미지 기반 AWS CI/CD 배포

`Docker` · `AWS` · `GitHub Actions` · `CI/CD` · `RDS`

### 6.1 구체적 학습 목표

1. Spring Boot JAR가 Docker 이미지가 되는 과정을 Dockerfile 각 줄과 대응시켜 설명할 수 있다.
2. CI에서 테스트가 실패하면 이미지 빌드와 배포가 중단되는 것을 직접 확인할 수 있다.
3. 같은 이미지가 환경변수와 profile만 바꿔 H2 대신 RDS에 붙는 것을 설명할 수 있다.
4. Push → 테스트 → 빌드 → 이미지 Push → 배포 → Health Check 흐름을 순서대로 말할 수 있다.
5. 배포된 URL의 Swagger로 주문 API를 호출하고 RDS에 데이터가 남는 것을 확인할 수 있다.
6. H2에서 통과한 테스트가 실제 DB에서 깨질 수 있는 지점을 한 가지 이상 지목할 수 있다.

### 6.2 시간 배분

**Core 40분**

| 시간 | 구간 | 내용 |
|---|---|---|
| 0:00–0:06 | 도입 | 오늘의 전체 흐름 그림 1장. "내 노트북에서만 되는 앱"에서 "누구나 부르는 API"로 |
| 0:06–0:14 | **실습 1** | TODO-1: Dockerfile 읽고 빈칸 채우기 → 로컬 이미지 빌드 → `docker run`으로 실행 확인 |
| 0:14–0:22 | **실습 2** | TODO-2~3: workflow의 테스트 스텝 추가 + 실패 시 중단 조건 확인 |
| 0:22–0:30 | **실습 3** | TODO-4: push → Actions 로그 실시간 관찰 → 초록 |
| 0:30–0:36 | **실습 4** | TODO-5~6: 배포 URL `/actuator/health` 호출 → Swagger에서 주문 생성 → RDS 반영 확인 |
| 0:36–0:40 | 마무리 | H2 vs RDS 차이 + **6주 전체 회고** (1주차 Product 20줄 → 오늘 배포된 API까지의 경로 되짚기) |

**Extension +20분**

| 시간 | 내용 |
|---|---|
| +0~6 | **일부러 실패시키기**: 테스트 하나를 깨뜨린 커밋 push → Actions 빨강 → 이미지 빌드 스텝이 skip되는 것 확인. "테스트를 통과한 경우에만 배포된다"의 증명 |
| +6~12 | Docker layer caching / Gradle 캐시로 CI 시간 단축 |
| +12~17 | 컨테이너 로그 확인 (`docker logs`), 환경변수 오타로 기동 실패시켰다가 복구 |
| +17~20 | 마무리 |

시간이 빠듯하면 "일부러 실패시키기"는 **강사가 미리 만들어둔 빨강 워크플로 실행 링크**를 보여주는 것으로 대체합니다.

### 6.3 강사 사전 준비물 (이 회차는 준비물이 곧 수업입니다)

**인프라**

| 항목 | 내용 |
|---|---|
| EC2 | Amazon Linux 2023, Docker 설치 완료, 보안그룹 8080 인바운드 오픈 |
| 레지스트리 | **GHCR(`ghcr.io`) 권장** — GitHub Actions에서 `GITHUB_TOKEN`만으로 push 가능해 ECR보다 IAM 준비가 없음 |
| RDS | PostgreSQL, 스키마 사전 생성, EC2 보안그룹만 5432 허용 |
| Secrets | `EC2_HOST`, `EC2_SSH_KEY`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| 확인 URL | `http://<EC2 퍼블릭 IP>:8080/swagger-ui/index.html`, `/actuator/health` |

**코드**

- `Dockerfile` (멀티스테이지, 일부 빈칸)
- `.github/workflows/deploy.yml` (뼈대, 일부 빈칸)
- `application-prod.yml` (환경변수 바인딩, `ddl-auto: validate`)
- 예비 시연 자료: 성공 워크플로 실행 링크 · 실패 워크플로 실행 링크 · 배포 과정 녹화본 · 로컬 `docker compose` fallback

```dockerfile
# 강사 제공 (일부 빈칸)
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
# TODO-1: 운영 환경에서 prod 프로파일로 뜨도록 실행 명령을 완성하세요
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# .github/workflows/deploy.yml (뼈대)
name: deploy
on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      # TODO-2: 전체 테스트를 실행하는 스텝을 추가하세요 (H2 사용)

      - name: Build image
        run: docker build -t ghcr.io/${{ github.repository }}:${{ github.sha }} .

      - name: Login & Push
        run: |
          echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker push ghcr.io/${{ github.repository }}:${{ github.sha }}

      - name: Deploy to EC2
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ec2-user
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            docker pull ghcr.io/${{ github.repository }}:${{ github.sha }}
            docker stop commerce || true && docker rm commerce || true
            docker run -d --name commerce -p 8080:8080 \
              -e SPRING_PROFILES_ACTIVE=prod \
              -e DB_URL=${{ secrets.DB_URL }} \
              -e DB_USERNAME=${{ secrets.DB_USERNAME }} \
              -e DB_PASSWORD=${{ secrets.DB_PASSWORD }} \
              ghcr.io/${{ github.repository }}:${{ github.sha }}

      - name: Health check
        run: |
          sleep 20
          curl -f http://${{ secrets.EC2_HOST }}:8080/actuator/health
```

### 6.4 ⚠️ 반드시 먼저 결정해야 할 것: 학생별 배포 충돌

**수강생 전원이 같은 EC2에 배포하면 서로의 컨테이너를 죽입니다.** 확정안에 명시되지 않은 항목이므로 수업 전에 반드시 정해야 합니다. 두 가지 안 중 하나를 택하세요.

**안 A (권장) — 학생은 CI까지, 배포는 강사 저장소에서**
- 학생: 저장소를 fork → workflow의 **테스트 + 이미지 빌드 + GHCR push**까지 각자 성공시킴 (초록 배지 획득)
- 배포 job은 `if: github.repository == '<강사 저장소>'` 조건으로 학생 fork에서는 skip
- 실제 EC2 배포는 강사가 강의 중 실시간으로 1회 실행하고 전원이 같은 URL로 호출
- 장점: 준비 부담·실패 위험이 가장 낮고, 학생 전원이 "내 커밋이 이미지가 되는" 경험은 그대로 함

**안 B — 학생별 포트/컨테이너 분리 (10명 이하일 때만)**
- 컨테이너명 `commerce-${{ github.actor }}`, 포트 8081~8090을 사전 배정
- 보안그룹에 해당 포트 범위 오픈
- 장점: 각자 자기 URL을 가짐 / 단점: 포트 배정·EC2 리소스·정리 부담

**🔸 선택 제안:** 안 A를 기본으로 하고, 희망자에 한해 수업 후 안 B로 개별 배포를 열어주는 하이브리드.

### 6.5 학생 TODO

| # | 파일 | 할 일 |
|---|---|---|
| 1 | `Dockerfile` | ENTRYPOINT에 prod 프로파일 지정 완성 → 로컬 빌드 & 실행 |
| 2 | `deploy.yml` | `./gradlew test` 스텝 추가 |
| 3 | `deploy.yml` | 테스트 실패 시 이후 스텝이 실행되지 않는 것을 확인 (기본 동작 확인 + `needs`/`if` 이해) |
| 4 | (실행) | 본인 fork에 push → Actions 초록 → GHCR에 이미지 생성 확인 |
| 5 | (확인) | 배포 URL `/actuator/health` 가 `{"status":"UP"}` 인지 확인 |
| 6 | (확인) | Swagger에서 주문 생성 → 조회로 RDS 반영 확인 |

### 6.6 실습 결과물

- 본인 저장소 Actions 초록 배지 + GHCR 이미지 태그(커밋 SHA)
- 배포 URL Health Check 200
- Swagger로 생성한 주문이 조회 API에 나타남
- (확장 시) 실패 커밋에서 빌드가 중단된 로그 스크린샷

### 6.7 과제 · WIL

**과제:** 본인 fork의 Actions를 초록으로 만들고, ① 테스트 통과 로그 ② 이미지 태그 ③ (안 A라면 강사 배포 URL의) Swagger 호출 결과 3장을 캡처해 제출. 6주 전체 회고 1페이지 첨부.

**WIL 주제:** H2에서 통과한 테스트가 RDS PostgreSQL에서는 깨질 수 있는 지점 3가지를 찾고 이유를 설명하라.
(예상 답: 예약어·대소문자 처리 차이 / `ddl-auto` 자동 생성 스키마와 실제 스키마의 불일치 / 락·격리 수준 동작 차이 / 시퀀스 전략 차이 / 날짜·시간 타입 정밀도)

---

## 7. 회차 간 코드 연결 맵

### 7.1 파일 단위 연결

| 파일 | W1 | W2 | W3 | W4 | W5 | W6 |
|---|---|---|---|---|---|---|
| `Product` | **행위 추가** | – | 과제: DIP | **`@Version`** | – | 그대로 배포 |
| `ProductService` | **3줄로 축소** | – | 과제: 의존 교체 | 재시도 판단 | – | – |
| `Order` | – | **Aggregate Root 신설** | Domain 배치 | – | N+1 재현 대상 | – |
| `Money` / `Quantity` | – | **VO 신설** | Domain 배치 | – | – | – |
| `OrderRepository`(interface) | – | – | **신설(Port)** | – | 시그니처 불변 | – |
| `OrderRepositoryAdapter` | – | – | **신설(Adapter)** | – | **fetch join 연결** | – |
| `GlobalExceptionHandler` | – | – | – | **409 추가** | – | – |
| `Dockerfile` / workflow | – | – | – | – | – | **신설** |

### 7.2 서사 연결 (수업 도입부에서 매번 짚을 한 문장)

- **W1 → W2**: "Product 하나는 행위를 갖게 됐다. 그런데 Order는 객체가 여러 개다. 여러 객체에 걸친 규칙은 누가 지키나?"
- **W2 → W3**: "규칙은 Order 안에 안전하게 있다. 그런데 Order를 저장하는 코드가 어디에 있어야 하지? 지금 Service가 JPA를 직접 알고 있다."
- **W3 → W4**: "이제 DB 없이도 테스트가 돈다. 그런데 진짜 DB에서 **두 사람이 동시에** 주문하면?"
- **W4 → W5**: "동시성은 막았다. 이번엔 혼자 쓰는데도 느리다. SQL 로그를 켜보자."
- **W5 → W6**: "테스트가 전부 초록이고 쿼리도 최적화됐다. 이걸 내 노트북 밖으로 내보내자."

각 회차 도입 1분을 이 문장에 쓰면 6주가 하나의 이야기로 묶입니다.

### 7.3 테스트 전략 누적표

| 주차 | 테스트 종류 | Spring 컨텍스트 | DB | 대표 파일 |
|---|---|---|---|---|
| 1 | Domain 단위 | ✗ | ✗ | `ProductTest` |
| 2 | VO·Aggregate 불변식 | ✗ | ✗ | `OrderTest`, `MoneyTest` |
| 3 | Application (Fake) | ✗ | ✗ | `OrderServiceTest` |
| 4 | 동시성 통합 | ✓ | H2 | `ConcurrentStockDecreaseTest` |
| 5 | Repository 조회 | ✓(`@DataJpaTest`) | H2 | `OrderQueryTest` |
| 6 | 전체 (CI) | 혼합 | H2 | `./gradlew test` |

**설명 포인트:** 위로 갈수록 빠르고 많이 만들 수 있고, 아래로 갈수록 느리지만 실제에 가깝습니다. 6주에 걸쳐 이 표가 한 줄씩 채워지는 것을 매주 보여주면 "테스트 피라미드"를 용어 없이 체득시킬 수 있습니다.

---

## 8. 분량 축소 제안 (요청 사항 10번)

40분을 초과할 위험이 있는 지점과, 그에 대한 구체적 처방입니다.

### 8.1 위험도 순위

| 순위 | 회차 | 위험 요인 | 처방 |
|---|---|---|---|
| 1 | **W2** | VO 2개 + Aggregate + 불변식 6개 = 40분 초과 확실 | 껍데기·JPA 매핑·`equals/hashCode`·`OrderLine` 전체를 `week2-start`에 완성 제공. 학생은 **검증문과 행위 본문만** 작성. 불변식 6개 중 Core에서는 4개(#2·#3·#5·#6)만 다루고 #1·#4는 Extension 또는 과제로 이동 |
| 2 | **W3** | 패키지 재배치 작업이 순수 노동으로 15분 소모 | 재배치를 `week3-start`에 완료 배포. 학생은 인터페이스 정의 + Adapter + Fake 3개만 작성 |
| 3 | **W6** | 외부 의존(네트워크·AWS·레지스트리) 실패 시 회차 붕괴 | 안 A(학생은 CI까지) 채택 + 녹화본·성공/실패 워크플로 링크·로컬 fallback 3중 대비 |
| 4 | **W4** | 영속성 컨텍스트 복습이 길어지면 실습 시간 잠식 | 복습을 8분으로 타이머 고정. 상세 설명은 사전 배포 자료로 돌리고 수업에서는 **타임라인 그림 한 장**만 |
| 5 | **W5** | fetch join 문법·`distinct`·페이징 한계를 다 다루면 초과 | Core는 "쿼리 세기 → EAGER 실험 → fetch join 1개 작성"만. `distinct` 필요성은 결과를 보여주고 한 문장 설명, 페이징 한계는 Extension |
| 6 | **W1** | 여유 있음 | 남는 시간을 실패 메시지 읽기 훈련에 사용 |

### 8.2 회차별 "잘라도 되는 것" 명시

수업 중 시계를 보고 판단할 수 있도록, 각 회차의 **첫 번째 컷 대상**을 미리 정해둡니다.

| 회차 | 시간 부족 시 가장 먼저 자를 것 | 절대 자르면 안 되는 것 |
|---|---|---|
| W1 | 실습 3(`ProductTest` 이동) → 과제로 | `Product.decreaseStock` 구현 |
| W2 | 불변식 #1·#4 → 과제로 | `Money`/`Quantity` 생성자 검증, `Order.cancel()` |
| W3 | 실습 3(Fake) → 강사 시연으로 | `OrderRepository` 정의 + Adapter 구현 |
| W4 | 실습 2(409 매핑) → 강사 시연으로 | `@Version` 적용 + 동시성 테스트 초록 |
| W5 | 실습 2(EAGER 실험) → 강사 시연으로 | 쿼리 개수 세기 + fetch join 적용 |
| W6 | 실습 1(로컬 Docker 빌드) → 시연으로 | push → Actions 초록 → 배포 URL 호출 |

### 8.3 전 회차 공통 시간 방어책

- **강사 라이브 코딩은 회차당 최대 7분.** 그 이상은 학생이 따라 치다가 실습 시간을 잃습니다.
- **TODO는 회차당 6~8개, 각 2~4줄.** 한 TODO가 10줄을 넘으면 40분에 안 들어옵니다.
- **개념 설명 총량은 회차당 14분 이하.** W4만 예외(원리 이해가 목적이라 16분).
- 실습 시작 전 **"지금 몇 분 남았고 TODO 몇 번까지 가면 된다"** 를 칠판에 적어두기.

---

## 9. 🔸 선택 제안 (확정안 아님 — 채택 여부 결정 필요)

1. **`weekN-start` 브랜치 매주 배포** — 0.3절. 낙오 복구 장치. **채택 강권**
2. **1주차 과제 표준 분석 답안 배포** — 2주차 시작점 통일용. **채택 강권**
3. **6주차 배포 충돌 대응 안 A/B 결정** — 6.4절. **수업 전 결정 필수**
4. **레지스트리로 ECR 대신 GHCR 사용** — IAM 준비 없이 `GITHUB_TOKEN`만으로 동작
5. **W5 EAGER 실험을 Core에 포함** — 원래 확정안에 명시되지 않았으나, "EAGER는 해결책이 아니다"라는 확정 목표를 가장 효율적으로 달성하는 장치
6. **W3 Extension에 ArchUnit 1개 테스트** — 의존 방향을 테스트로 고정. 취향에 따라 생략 가능
7. **W4 TODO-4(판단 근거 주석)** — 코드가 아닌 설계 판단 훈련. 코드 분량이 적은 회차의 밀도를 채움
8. **매 회차 도입 1분 서사 문장** — 7.2절

---

## 10. 다음 작업 (이 문서 승인 후)

| 순서 | 산출물 |
|---|---|
| 1 | `week0-baseline` 프로젝트 스캐폴딩 (Gradle, 패키지, 레거시 CRUD, H2 설정, Swagger) |
| 2 | W1 `start`/`done` 코드 + 테스트 |
| 3 | W2~W3 `start`/`done` 코드 |
| 4 | W4~W5 `start`/`done` 코드 + 강사 제공 테스트 하네스 |
| 5 | W6 Dockerfile / workflow / `application-prod.yml` |
| 6 | 회차별 강의안(슬라이드 아웃라인) + 과제 안내문 |
