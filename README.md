# WEEK 3 해설 — 4계층 아키텍처와 DIP

> `week3` 와 이 브랜치의 차이를 보세요.
> ```bash
> git diff week3 week3-done
> ```

## 오늘 만든 것

**한 줄로 말하면 — `ProductService` 가 JPA 를 모르게 됐습니다.**

| | 시작 | 끝 |
|---|---|---|
| **application 의 기술 의존 (grep)** | 3건 | **0건** ← 주 지표 |
| `./gradlew test` | 38개 초록 | 38개 초록 (**변화 없음**) |
| `ProductServiceTest` 단독 | 2.4초 | 0.05초 (결과) |
| `OrderServiceTest` 단독 | 1.9초 | 0.05초 (결과) |

**테스트 결과는 하나도 안 바뀌었습니다.** 겉으로 드러나는 동작을 바꾸지 않고 안을 바꾸는 것 — 1주차에 배운 리팩터링 그대로입니다.

> **속도는 목적이 아니라 결과입니다.**
> 목적은 **규칙을 기술에서 떼어내는 것**이고, 빨라진 건 기술이 안 붙어 있으니 기술을 안 띄워도 되기 때문입니다.
> 그래서 성적표의 1번이 grep 이에요.

---

## 1. Port — 도메인이 요구하는 쪽

```java
// domain/product/ProductRepository.java
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
}
```

**`import` 에 `org.springframework` 도 `jakarta.persistence` 도 없습니다.** 이게 전부입니다.

이 인터페이스는 "저장소를 어떻게 만들지" 를 말하지 않습니다. **"도메인이 저장소에 뭘 원하는지"** 만 말해요.
그래서 이게 `infrastructure` 가 아니라 `domain` 에 있습니다. **요구하는 쪽이 소유합니다.**

> 흔한 오해: "인터페이스는 구현체 옆에 두는 것 아닌가요?"
> 그러면 화살표가 안 꺾입니다. `domain → infrastructure` 가 되어버려요.

## 2. Adapter — 위임만 하는 클래스

```java
// infrastructure/persistence/ProductRepositoryAdapter.java
@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override public Product save(Product product) { return jpaRepository.save(product); }
    ...
}
```

**하는 일이 없습니다.** 그냥 넘깁니다. 그런데 이 "아무것도 안 하는 클래스" 가 화살표를 꺾습니다.

```
application ──> ProductRepository (domain)
                        ↑ implements
                ProductRepositoryAdapter (infrastructure)
```

`implements` 라는 단어 하나가 **infrastructure 를 domain 쪽으로 향하게** 만듭니다.
`@Repository` 를 붙여서 스프링이 이 놈을 `ProductRepository` 자리에 꽂아줍니다.

## 3. Service — 한 줄 차이

```diff
- private final ProductJpaRepository productRepository;
+ private final ProductRepository productRepository;
```

**메서드 본문은 한 글자도 안 고쳤습니다.** `save`, `findById` 이름이 같으니까요.
일부러 그렇게 인터페이스를 설계한 겁니다.

이 한 줄이 바뀌자 `ProductService` 는 **JPA 가 뭔지 모르는 클래스**가 됐습니다.

## 4. Fake — DB 없이 돌리기

```java
public class FakeProductRepository implements ProductRepository {
    private final Map<Long, Product> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            ReflectionTestUtils.setField(product, "id", ++sequence);
        }
        store.put(product.getId(), product);
        return product;
    }
    ...
}
```

`HashMap` 하나입니다. 그런데 `ProductService` 입장에서는 **진짜 저장소와 구별이 안 됩니다.**
`ProductRepository` 로만 보이니까요.

```java
@BeforeEach
void setUp() {
    productRepository = new FakeProductRepository();
    productService = new ProductService(productRepository);
}
```

`@SpringBootTest` 도, `@Autowired` 도, `@Transactional` 도 없습니다. **`new` 만 두 번.**

## 5. 그래서 무엇이 진짜 바뀌었나 ★

**같은 `ProductService` 가 두 가지 저장소로 동작합니다.**

```java
new ProductService(new ProductRepositoryAdapter(jpa))   // JPA · 진짜 DB · SQL
new ProductService(new FakeProductRepository())          // HashMap · 메모리
```

기술이 완전히 다릅니다. 하나는 SQL 을 날리고 하나는 `HashMap` 에 넣어요.
그런데 **`ProductService` 본문은 한 글자도 다르지 않습니다.**

`ProductService` 가 아는 것은 이것뿐입니다.

> "`ProductRepository` 라는 게 있고, 거기에 `save` 를 부르면 저장된다."

그게 JPA 인지, `HashMap` 인지, 나중에 외부 API 가 될지 **모르고 알 필요도 없습니다.**
클린 아키텍처에서 말하는 **"데이터베이스는 세부사항이다"** 가 코드로 나타난 모습입니다.

**이게 오늘 한 일입니다.** 아래 속도 이야기는 그 결과예요.

### 언제 값을 하나

5주차에 주문 목록 조회 쿼리를 11개에서 1개로 줄입니다.

| | |
|---|---|
| 고치는 파일 | `OrderRepositoryAdapter` — 오늘 만든 그 자리 |
| 안 고치는 파일 | `OrderService`, `Order`, `OrderItem` |

**기술을 바꾸는데 규칙이 안 흔들립니다.** 오늘 이걸 만들어둔 값이 그때 나옵니다.

## 6. 그리고 44배 빨라졌습니다

없어진 것들입니다.

```
스프링 부트 기동          컴포넌트 스캔, 빈 생성, 프록시 생성
H2 데이터베이스 기동      JDBC 커넥션 풀
Hibernate 초기화          엔티티 스캔, 테이블 DDL 생성
트랜잭션 관리             begin / rollback
```

**하나도 안 하게 됐습니다.** 코드를 빠르게 만든 게 아니라, **기술이 안 붙어 있으니 기술을 안 띄워도 되는 것**뿐입니다.

빠르게 하려고 분리한 게 아니라, **분리했더니 빨라진 것**입니다. 순서를 헷갈리지 마세요.

---

## 과제 정답 — Order 쪽

수업에서 한 것을 그대로 반복하면 됩니다. 파일 이름만 다릅니다.

| 수업 | 과제 |
|---|---|
| `domain/product/ProductRepository` | `domain/order/OrderRepository` |
| `infrastructure/.../ProductRepositoryAdapter` | `infrastructure/.../OrderRepositoryAdapter` |
| `test/.../FakeProductRepository` | `test/.../FakeOrderRepository` |

`OrderServiceTest` 만 조금 다릅니다. **저장소가 두 개** 필요해요.

```java
@BeforeEach
void setUp() {
    productRepository = new FakeProductRepository();
    orderService = new OrderService(new FakeOrderRepository(), productRepository);
}
```

수업에서 만든 `FakeProductRepository` 를 그대로 씁니다. **한 번 만들어두면 다음부터는 그냥 쓰는 것** — 오늘 구조를 만든 이유가 이겁니다.

### 질문 1 · Repository 인터페이스를 `infrastructure` 에 두면?

**화살표가 안 꺾입니다.**

```
domain ──> OrderRepository (infrastructure)      ← domain 이 infrastructure 를 안다
```

지금은 `domain` 이 아무것도 안 바라봅니다. 그래서 `domain` 만 떼어내서 다른 프로젝트에 붙일 수 있어요.
인터페이스를 아래로 내리면 그게 안 됩니다. **"어디에 두느냐" 가 곧 "누가 누구를 아느냐" 입니다.**

### 질문 2 · Adapter 를 없애면?

**됩니다.** 이렇게 하면 스프링 데이터가 알아서 구현해 줍니다.

```java
public interface ProductRepositoryAdapter
        extends JpaRepository<Product, Long>, ProductRepository {
}
```

37줄이 9줄이 되고, 테스트도 전부 통과합니다. **실무에서 쓰는 팀도 있습니다.**

그런데 **Port 에 도메인 언어로 메서드를 하나만 추가하면 무너집니다.**

```java
// domain/product/ProductRepository.java
List<Product> findSellingProducts();   // 도메인이 쓰고 싶은 이름
```

```
PropertyReferenceException:
No property 'findSellingProducts' found for type 'Product'
```

병합하면 **도메인 인터페이스가 곧 스프링 데이터 인터페이스**가 됩니다.
스프링 데이터가 거기 선언된 메서드 이름을 **전부 파싱해서 쿼리를 만들려고 하거든요.**
`findSellingProducts` 를 "`Product` 의 `sellingProducts` 속성을 찾아라" 로 해석하고 기동에 실패합니다.

피하려면 도메인 메서드 이름을 `findByStatus(SellingStatus)` 로 바꿔야 합니다.
**도메인 언어를 프레임워크 문법에 맞추는 것**이죠.

나눠두면 셋이 각자 자기 언어를 씁니다.

```java
// domain — 도메인 언어
List<Product> findSellingProducts();

// infrastructure/Adapter — 번역
public List<Product> findSellingProducts() {
    return jpaRepository.findByStatus(SellingStatus.SELLING);
}

// infrastructure/JPA — 스프링 데이터 문법
List<Product> findByStatus(SellingStatus status);
```

> **`ProductJpaRepository` 를 따로 두는 이유는 도메인이 자기 언어로 이름을 지을 수 있게 하려는 겁니다.**

지금 우리 Port 가 `save`·`findById`·`findAll` 셋뿐이라 병합해도 멀쩡해 보이는 것뿐이에요.
이름이 우연히 `JpaRepository` 와 같아서요.

부수적으로 하나 더 — **병합 타입을 직접 변수로 선언하면 컴파일 에러가 납니다.**

```
error: reference to save is ambiguous
```

`JpaRepository.save(S)` 와 `ProductRepository.save(Product)` 가 둘 다 상속되는데
서로 override 관계가 아니라 컴파일러가 못 고릅니다. Port 타입으로만 쓰면 안 터지지만,
**"이 타입은 절대 직접 쓰지 마세요" 라는 규칙을 하나 더 기억해야 합니다.**

### 질문 3 · Fake 로 못 잡는 버그

`HashMap` 은 DB 가 아닙니다. 다음은 Fake 테스트가 **전부 통과하는데 운영에서 터지는** 것들입니다.

| 못 잡는 것 | 언제 배우나 |
|---|---|
| 두 사람이 동시에 주문해서 재고가 음수가 된다 | **4주차** |
| 주문 10건 조회했더니 쿼리가 11번 나간다 | **5주차** |
| 트랜잭션이 롤백돼야 하는데 안 된다 | 4주차 |
| `not null` 제약을 어겨서 INSERT 가 실패한다 | — |

**그래서 `OrderPersistenceTest` 는 `@DataJpaTest` 로 남겨뒀습니다.** 지우지 않았어요.

> 빠른 테스트가 만능이 아닙니다. **빠른 테스트를 많이, 느린 테스트를 적게** 두는 게 목표예요.
> 오늘 한 일은 "느린 걸 없앤 것" 이 아니라 **"느려도 되는 것만 느리게 남긴 것"** 입니다.

---

## 자주 하는 실수

| 증상 | 원인 |
|---|---|
| `NoSuchBeanDefinitionException` | Adapter 에 `@Repository` 누락 |
| Adapter 컴파일 실패 | 반환 타입 불일치. `List` 인지 `Optional` 인지 확인 |
| `OrderRepository extends JpaRepository` 로 작성 | 그러면 domain 이 다시 JPA 를 압니다. 질문 2 를 보세요 |
| Fake 저장 후 `getId()` 가 null | `ReflectionTestUtils.setField` 누락 |
| `ProductService` 만 고치고 앱이 안 뜸 | `OrderService` 도 같은 타입을 씁니다 |
| 테스트가 여전히 느림 | `@SpringBootTest` 가 아직 붙어 있습니다 |

---

## 다음 주

이제 `application` 은 기술을 모르고, 테스트는 DB 없이 돕니다.

> **그런데 진짜 DB 에서 두 사람이 동시에 같은 상품을 주문하면 어떻게 될까요?**

오늘 만든 Fake 로는 절대 못 잡는 문제입니다. 질문 3 에 적으신 그거예요.
