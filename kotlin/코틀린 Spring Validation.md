# Validation 추가
## Jakarta, Spring validation 차이는?
validation을 찾아보다보면 jakarta 라이브러리도 있고, spring 라이브러리도 있다.
그리고 validation에서 사용되는 대부분의 어노테이션들이 jakarta.validation 패키지에 속해있다.
간단하게 설명하지면 jakarta.validation 패키지의 어노테이션은 표준이고, 실제 검증 로직 실행은 Spring Validation이 맡는다는 내용이다.
혹시나 이 내용이 궁금하다면 아래 정리된 글을 참고하면 도움이 될 것이다.
* 글 수정 중입니다. *

## 의존성 추가
spring에서 Validation을 사용하기 위해서는 의존성을 추가해야 한다.
의존성을 추가하지 않아도 validation에 사용되는 어노테이션들은 사용할 수 있지만, 실제로는 검증이 이루어지지 않는다.
그 상세한 이유는 위에 나온 jakarta, spring validation 차이 게시물에서 확인할 수 있다.

하여튼 우리는 validation을 사용하기 위해서 spring validation 라이브러리인 `spring-boot-starter-validation` 의존성을 추가해야 한다.
```
implementation(”org.springframework.boot:spring-boot-starter-validation”)
```

## Request 클래스에 validation 추가
```kotlin
data class MemberCreateRequest(
    @field:Size(min = 2, max = 50, message = "회원 별명은 2-50자 사이여야 합니다")
    val nickname: String,
    @field:NotBlank(message = "회원 비밀번호는 필수 입력값입니다")
    @field:Size(min = 10, max = 30, message = "회원 비밀번호는은 10-30자 사이여야 합니다")
    val password: String,
    @field:NotBlank(message = "회원의 성은 필수 입력값입니다")
    @field:Size(min = 2, max = 50, message = "회원의 성은 2-50자 사이여야 합니다")
    val lastName: String,
    @field:Size(min = 2, max = 50, message = "회원 이름은 2-50자 사이여야 합니다")
    val firstName: String,
    @field:NotBlank(message = "이메일은 비어있을 수 없습니다.")
    @field:Email(message = "유효하지 않은 이메일 형식입니다.")
    val email: String,
)
```
위 코드와 같이 Request 클래스에 사용하려는 어노테이션을 필드에 붙여줘야한다.
근데 Java로 시작한 분들은 의문을 느낄 수 있다.
Java에서는 그냥 되기 때문에 나도 맨 처음에 field: 키워드를 붙이지 않았었고, 안 되는 이유를 몰랐다.
결론만 얘기하면 코틀린에서는 validation을 사용하기 위해서는 반드시 @field: 어노테이션을 붙여줘야 한다.
그 이유는 아래에서 설명하겠다.

## Controller에서 검증하려는 파라미터에 @Valid 어노테이션 사용
```kotlin
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
fun create(
    @RequestBody @Valid memberCreateRequest: MemberCreateRequest,
) = memberCommandService.createMember(memberCreateRequest.toDomain())
```

## Java와 달리 field값이 붙는 이유
Java에서 넘어오면 가장 큰 궁금증이 @field: 가 왜 붙는지 일 것이다.
나도 Java에서 계속 validation을 사용하다가 코틀린으로 넘어오면서 처음에 이 부분이 이해가 안 갔다.

코틀린의 val/var 키워드로 선언한 프로퍼티는 필드와 getter 등 여러 요소를 포함하고 있다.
이때 field는 Java 바이트코드의 필드에 적용되도록 지정하는 키워드다.
field를 붙이지 않으면 해당 어노테이션을 어디에 붙여야하는지 모호함이 생기게 된다.
따라서 field 키워드를 사용해 필드 값에 사용하는 어노테이션임을 명시해야 하는 것이다.

## 그 외 타겟 지정 키워드
- field
    - 필드에 적용
- get
    - getter 메서드
- set
    - var프로퍼티의 setter
- param
    - 생성자의 파라미터
- property
    - 코틀린 프로퍼티 자체(JAVA에서는 보이지 않음)
- setparam
    - setter 메서드의 파라미터