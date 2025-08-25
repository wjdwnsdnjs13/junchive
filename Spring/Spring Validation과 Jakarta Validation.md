## 배경
사내 신규 프로젝트를 진행하는 중, Validation 구현 과정에서 팀원분께서 Jakarta Validation 의존성을 추가하신 상태로 Validation 작업을 진행했다.
코드 리뷰에서도 큰 위화감을 못 느꼈는데 Validation이 정상 동작을 안 해서 트러블 슈팅에 적지 않은 시간을 사용했다.

그러는 과정에서 Spring Validation과 Jakarta Validation의 차이를 알게 되었고, 정리 차원에서 글을 작성하게 되었다.
한 번 이해하면 크게 어려운 내용은 아니지만, 몰랐을 때에는 혼란이 올 수 있는 내용이라 생각하기 때문에 같은 상황에 처한 분들에게 도움이 되었으면 한다.
 
## 의존성 차이
### Spring
`implementation(”org.springframework.boot:spring-boot-starter-validation”)`

### Jakarta
`implementation("jakarta.validation:jakarta.validation-api")`

위와 같이 Spring과 Jakarta 각각의 Validation 의존성이 존재한다.
Spring Validation 의존성을 추가했을 땐 정상 동작을 하지만, Jakarta Validation 의존성만 추가했을 땐 Validation이 정상적으로 동작하지 않는 문제가 발생했다.

## 어노테이션
validation import를 보면 아래와 같이 대부분이 jakarta.validation에서 가져온다.

```kotlin
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
```

그래서 나도 코드 리뷰 당시 Jakarta 의존성을 추가하는 것에 위화감을 느끼지 못했고, 서두에서 말한 것처럼 의문이 생기게 되었다.

jakarta가 아니라 왜 spring의 validation을 가져와야 할까?

## Jakarta Validation

@NotNull, @Size 등의 Validation 규칙은 Jakarta에서 정의하고 있다.
 ->프레임워크에 종속되지 않는 Java 표준 스펙이다.

다만 Jakarta Validation에는 스펙에 대한 명세만 있어서 Hibernate Validator 같은 구현체가 필요하다.
구현체 없이 Jakarta만 사용할 경우에는 @Valid를 붙여도 유효성 검사 수행 후 BindingResult에 결과를 담아줄 수 없다.
그 의미는 유효성 검사의 결과를 핸들링할 수 없고, 최종적으로 우리가 원하는 형식으로 Validation 결과를 반환할 수 없다는 뜻이다.

그렇기에 직접 Validator 객체를 생성하고 validate() 메서드를 호출해줘야 한다.
```kotlin
fun someServiceLogic블라블라(request: CreateMemberRequest) {
    // 1. Validator 인스턴스 생성
    val validator = Validation.buildDefaultValidatorFactory().validator

    // 2. validate() 메서드를 수동으로 호출
    val violations = validator.validate(reque)

    // 3. 검증 실패 시 예외 처리 또는 오류 로직 직접 구현
    if (violations.isNotEmpty()) {
        violations.forEach {
            println(it.message)
        }
        throw IllegalArgumentException("입력값이 유효하지 않습니다.")
    }
    println("유효성 검증 완료")
}
```

이렇게 하면 유효성 검사가 수행되고, 검증 결과를 직접 핸들링할 수 있다.
하지만, 이걸 직접하면 매번 Validator를 생성하고 validate()를 호출하는 코드가 반복된다.
AOP로 처리하는 등의 방법도 있겠지만, Spring Validation을 사용하는 게 훨씬 편리하다.
 -> Spring Validation에서 AOP로 자동으로 처리해준다.

## Spring Validation
SpringValidation은 @Valid 어노테이션이 붙은 대상에게 Jakarta Validation을 활성화하고 실제 유효성 검사를 수행하도록 한다.
검증 결과는 BindingResult 객체에 담아 컨트롤러에게 전달된다.
Jakarta와 구현체를 모두 포함하고 있다.

그렇다면 Spring Validation이 Jakarta Validation을 포함하고 있기 때문에 Spring Validation만 추가하면 되는 것일까?
Spring Validation이 자동으로 처리해주는 부분을 알아보자.

### Spring Validation이 자동으로 처리해주는 부분

- Validator Auto Config
    - 전역으로 사용 가능한 ValidatorFactory나 Validator를 자동으로 빈으로 등록해줌
    - Hibernate Validator로 만들어짐
- @Valid 어노테이션으로 검증 자동 실행
    - @Valid 어노테이션이 붙은 메서드 호출 직전 등록된 Validator를 사용해 Request 객체의 유효성을 자동으로 검사해줌
    - validator.validate()를 호출할 필요가 없어짐
    - AOP를 사용해 동작함
- 검증 결과 자동 처리(BindingResult)
    - 검증 결과를 BindingResult나 Errors 객체에 자동으로 담아줌
- 예외 자동 변환 및 처리
    - BindingResult가 파라미터에 없으면 스프링에서 자동으로 MethodArgumentNotValidException 예외를 발생시켜줌
    - 400 예외와 함께 기본 오류 메시지를 담아줌

이렇게 되기 때문에 우리는 Spring Validation을 사용하면 편리하게 Validation을 구현할 수 있게 되는 것이다.

---

## 결론
- Jakarta Validation → 어노테이션을 활용한 제약 조건 등을 정의한 표준 명세
- Spring Validation → Jakarta를 포함하고, 실제 유효성 검사를 구현

Jakarta Validation을 사용하면 Spring Validation이 자동으로 처리해주는 부분을 직접 구현해야 한다.
그 부분이 상당히 귀찮고 AOP로 처리할 수도 있겠지만, Spring Validation에서 AOP로 처리해준다.
커스텀을 해도 보통 MethodArgumentNotValidException을 잡아서 처리하는 게 대부분이기 때문에 validate() 함수까지 커스텀할 것이 아니라면 Spring Validation을 사용하는 게 편할 것이다.

### 찐 결론
**결론은 Jakarta만 사용해도 가능하다.(직접 매번 Validation을 걸어주기만 한다면…)
다만, validator.validate() 를 커스텀해서 따로 처리할 필요가 없다면 Spring Validation을 사용하는 게 훨씬 낫다!**

