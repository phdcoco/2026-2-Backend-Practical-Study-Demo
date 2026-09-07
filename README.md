# WEEK 3 — 4계층 아키텍처와 DIP

> `4계층 아키텍처` · `DIP` · `Repository 추상화` · `Port–Adapter`

## 오늘 할 일 한 줄 요약

`ProductService` 가 **JPA 를 모르게** 만든다.

저장소를 JPA 로 하든 `HashMap` 으로 하든 **서비스 코드가 안 바뀌는 상태**가 오늘의 목표다.
클린 아키텍처에서 말하는 **"데이터베이스는 세부사항이다"** 를 코드로 만드는 것.

> 테스트가 빨라지는 것은 **그 결과**입니다. 기술이 안 붙어 있으니 기술을 안 띄워도 되거든요.
> 목적과 결과를 헷갈리지 마세요.

---

## 시작하기

```bash
git checkout week3
cd commerce
./gradlew test
```

**38개 전부 초록입니다. 이것도 정상입니다.**

1·2주차와 다릅니다. 오늘은 **빨간 테스트가 없어요.** 고장 난 게 없거든요.
오늘 고칠 건 "틀린 코드" 가 아니라 **구조**입니다. 그래서 성적표도 다릅니다.

| | 1·2주차 | 3주차 |
|---|---|---|
| 목표 | 규칙을 객체 안으로 | **규칙을 기술에서 떼어내기** |
| 성공 지표 | 테스트가 초록이 된다 | **기술 의존 grep 이 줄어든다** (빨라지는 건 결과) |

---

## 문제를 먼저 보세요

`ProductServiceTest` **한 클래스만** 실행하세요. 전체 실행하면 안 보입니다.
그리고 1주차에 만든 `ProductTest` 도 단독으로 돌려서 비교하세요.

```
ProductServiceTest    2.4초
ProductTest           0.03초
```

**80배 넘게 차이납니다.** 콘솔을 보면 이유가 나옵니다 — 스프링 부트가 뜨고, H2 가 뜨고, 테이블이 만들어집니다.

> **"수량이 0이면 예외" 하나 확인하려고 데이터베이스를 켰습니다.**

1주차에 이 문제를 한 번 만났었죠. 그때는 **규칙을 `Product` 로 옮겨서** 해결했습니다.
그런데 `ProductService` 자체를 테스트하려면 여전히 저장소가 필요합니다. **오늘은 그걸 해결합니다.**

왜 DB 가 떠야 하는지, `ProductService` 를 열어보세요.

```java
private final ProductJpaRepository productRepository;   // ← infrastructure 패키지
```

이 타입은 `JpaRepository` 를 상속합니다. **JPA 를 아는 타입이에요.**
이게 필드에 있으면 이 클래스를 만들 때 JPA 도 같이 떠야 합니다.

---

## 오늘의 구조

패키지가 4개로 갈렸습니다. **재배치는 이미 끝내뒀습니다.** 파일 옮기는 건 배울 게 없어서요.

```
presentation     바깥에서 들어온 요청을 받는다     Controller, Request/Response
application      순서를 정한다                    Service, Command/Result
domain           규칙을 지킨다                    Product, Order, Money, 그리고 Repository 인터페이스
infrastructure   실제 기술로 처리한다              JPA, Adapter
```

의존 방향이 오늘의 전부입니다.

```
presentation ──> application ──> domain
                                   ↑
                 infrastructure ───┘     ← 화살표가 거꾸로 꺾인다
```

**`infrastructure` 가 `domain` 을 향합니다.** 보통은 위에서 아래로 흐르는데 여기만 거꾸로예요.
이걸 **의존성 역전(DIP)** 이라고 부릅니다. 오늘 이 화살표 하나를 만듭니다.

### 이름 세 개를 구분하세요

| 이름 | 무엇인가 | 어디 있나 |
|---|---|---|
| `ProductRepository` | **Port.** "저장소는 이렇게 생겨야 한다" 는 도메인의 요구 | `domain/product` |
| `ProductRepositoryAdapter` | **Adapter.** 그 요구를 JPA 로 실제 구현 | `infrastructure/persistence` |
| `ProductJpaRepository` | 진짜 JPA 인터페이스. **Adapter 만 이걸 안다** | `infrastructure/persistence` |

---

## 실습 순서

| TODO | 파일 | 할 일 |
|---|---|---|
| **W3-1** | `domain/product/ProductRepository` | `save` · `findById` · `findAll` 정의 |
| **W3-2** | `application/product/ProductService`<br>`application/order/OrderService` | 주입 타입을 `ProductRepository` 로 교체 (**두 파일**) |
| **W3-3** | `infrastructure/.../ProductRepositoryAdapter` | 인터페이스 구현 + `@Repository` |
| **W3-4** | `test/.../FakeProductRepository` | `HashMap` 기반 가짜 저장소 |
| **W3-5** | `test/.../ProductServiceTest` | `@SpringBootTest` 제거, Fake 주입 |
| **W3-6** | (점검) | 아래 grep 이 `1` 로 줄었는지 확인 |

**순서를 지키세요.** W3-2 다음에 일부러 고장이 납니다.

### W3-2 는 두 파일입니다

`ProductJpaRepository` 를 쓰는 곳이 `ProductService` 와 `OrderService` **두 군데**입니다.
둘 다 바꾸세요. 인터페이스로 바꾸고 나면 **쓰는 쪽이 몇 개든 전부 같은 타입만 봅니다.**

### W3-2 를 하면 앱이 안 뜹니다. 정상입니다.

```
APPLICATION FAILED TO START

Description:
Parameter 0 of constructor in ...ProductService required a bean of type
'...domain.product.ProductRepository' that could not be found.

Action:
Consider defining a bean of type '...domain.product.ProductRepository' in your configuration.
```

**컴파일은 됩니다.** 인터페이스는 만들었으니까요.
안 되는 건 스프링이 **그걸 구현한 물건을 못 찾는다**는 겁니다.

> 인터페이스는 "이런 게 있어야 한다" 는 선언일 뿐이고, 실제로 일할 놈이 따로 필요합니다.
> 그게 W3-3 의 Adapter 예요. **오류 메시지가 이미 답을 말해주고 있습니다.**

### W3-6 자가 점검

```bash
grep -rn "org.springframework.data\|jakarta.persistence\|infrastructure" src/main/java/com/gdghongik/commerce/application/ | wc -l
```

| 시점 | 결과 |
|---|---|
| 시작할 때 | **3** |
| 수업이 끝나면 | **1** ← 남는 하나는 `OrderService` 의 주문 저장소. **오늘 과제입니다** |
| 과제까지 끝나면 | **0** |

`0` 이면 **application 계층이 기술을 전혀 모르는 상태**가 된 겁니다.
말이 아니라 숫자로 확인하는 게 오늘의 마무리입니다.

---

## 완료 기준

**주 지표**

- [ ] 위 grep 결과가 `1`
- [ ] `ProductRepository` 에 JPA·Spring 타입이 없다 (`import` 가 `java.util` 뿐)
- [ ] `ProductService` 에서 `Jpa` 가 들어간 타입이 사라졌다
- [ ] **같은 `ProductService` 가 JPA Adapter 로도, `HashMap` Fake 로도 동작한다** — 본문 0줄 변경

**따라오는 것**

- [ ] `./gradlew test` 38개 초록 (시작과 같은 숫자)
- [ ] `ProductServiceTest` 에 `@SpringBootTest` 가 없다
- [ ] `ProductServiceTest` 단독 실행이 **1초 미만**

### 시간도 적어두세요

| | 전 | 후 |
|---|---|---|
| `ProductServiceTest` 단독 | 초 | 초 |

(참고: 약 2.4초 → 0.05초)

**단, 이건 성적표의 2번입니다.** 1번은 위의 grep 이에요.
빠르게 하려고 분리한 게 아니라 **분리했더니 빨라진 것**입니다.

---

## 자주 하는 실수

| 증상 | 원인 |
|---|---|
| `NoSuchBeanDefinitionException` | Adapter 에 `@Repository` 를 안 붙였습니다 |
| Adapter 가 컴파일 안 됨 | 인터페이스와 시그니처가 다릅니다. **반환 타입까지** 맞추세요 |
| `ProductRepository` 에 `JpaRepository` 를 상속시킴 | 그러면 domain 이 다시 JPA 를 압니다. **상속하지 말고 직접 선언**하세요 |
| Fake 에서 저장 후 `getId()` 가 `null` | `ReflectionTestUtils.setField` 로 id 를 넣어야 합니다 |
| Fake 를 만들었는데 테스트가 여전히 느림 | `@SpringBootTest` 가 아직 붙어 있습니다 |
| `OrderService` 만 고치고 앱이 안 뜸 | `ProductService` 도 같이 바꿔야 합니다 (W3-2 는 두 파일) |

> **`ReflectionTestUtils` 는 왜 쓰나요?**
> `Product` 에 `setId()` 가 없어서요. 1주차에 setter 를 지운 결과입니다.
> 진짜 DB 는 id 를 자동으로 붙여주지만 `HashMap` 은 안 해주니, **테스트에서만** 리플렉션으로 넣습니다.
> 운영 코드에서는 쓰지 마세요.

---

## 과제 — 주문 쪽에 똑같이 하세요

수업에서 `Product` 로 한 것을 `Order` 로 반복합니다. **오늘 만든 세 파일이 그대로 답안지입니다.**

**할 일**

- `domain/order/OrderRepository` 인터페이스를 만드세요
- `infrastructure/persistence/OrderRepositoryAdapter` 를 구현하세요
- `OrderService` 의 주입 타입을 바꾸세요
- `test/.../FakeOrderRepository` 를 만드세요
- `OrderServiceTest` 에서 `@SpringBootTest` 를 떼세요

**끝나면 grep 이 `0` 이어야 합니다.**

> `OrderServiceTest` 를 Fake 로 바꿀 때 **`FakeProductRepository` 도 필요합니다.**
> `OrderService` 는 저장소를 두 개 쓰거든요. 수업에서 만든 걸 그대로 넘기면 됩니다.
> **이게 오늘 구조를 만든 이유입니다** — 한 번 만들어두면 다음부터는 그냥 씁니다.

**같이 생각해 볼 것 (답을 적어 오세요)**

1. `OrderRepository` 를 `domain/order` 에 두었습니다. `infrastructure` 에 두면 무엇이 달라지나요?
2. `Adapter` 가 하는 일이 **위임뿐**입니다. 한 줄씩 그대로 넘기죠.
   이렇게 아무것도 안 하는 클래스를 왜 만드나요? 없애고 `OrderJpaRepository` 가 직접
   `OrderRepository` 를 상속하게 하면 안 되나요? **된다면 무엇을 잃나요?**

   > 힌트: 실제로 됩니다. 37줄이 9줄이 되고 테스트도 다 통과해요.
   > **그럼 Port 에 `List<Product> findSellingProducts();` 를 하나 추가해 보세요.**
   > 무슨 일이 일어나는지가 답입니다.
3. `FakeOrderRepository` 는 `HashMap` 이라 트랜잭션도 없고 SQL 도 안 나갑니다.
   **이 Fake 로는 절대 못 잡는 버그**를 하나 들어보세요.

> 3번이 다음 주로 이어집니다. 빠른 테스트가 만능이 아니라는 걸 스스로 찾아보세요.

### 제출

- 코드는 각자 브랜치에 커밋
- 위 질문 답은 `docs/week3-assignment.md` 에

**WIL 주제 (택 1)**

1. Repository 인터페이스를 `domain` 에 두는 것과 `infrastructure` 에 두는 것은 의존 방향 관점에서 무엇이 다른가
2. 저장소 구현을 JPA 에서 `HashMap` 으로 통째로 바꿨는데 `ProductService` 는 왜 한 줄도 안 바뀌었는가
