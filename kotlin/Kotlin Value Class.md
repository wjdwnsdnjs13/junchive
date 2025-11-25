## 배경
코틀린과 자바에서는 객체 동등성 비교에서 equals를 오버라이딩하지 않는 이상 객체의 참조를 비교하고 Value Class는 값이 동일하면 동일한 객체로 간주한다.

또한 Value Class는 불변하다는 특징을 갖고 있다.
 -> value 값이 가변이면 원치 않는 값 변경으로 인해 예상치 못 한 결과값이 나오게 될 수 있기 때문이다.

코틀린에서는 Value Class 키워드를 사용해 inline class로 구현한다.

inline class는 내부에 변수 하나만 선언한다.
- JVM은 런타임에 primitive 타입에 많은 최적화를 적용하는데, 래퍼 타입으로 하게 되면 이점을 잃게 된다.
- 이때 inline class로 구현하면 primitive 타입의 이점을 가져갈 수 있다.

## 주의해야 할 점

JVM 관점에서 런타임 시 Value Class는 Primitive 타입이 되기 때문에 메서드 시그니처에서 주의해야 한다.

```kotlin
@JvmInline
value class MemberId(val id: Long)

@JvmInline
value class AuthorityId(val id: Long)

fun getMemberBy(memberId: MemberId) {}

fun getMemberBy(authorityId: AuthorityId) {}

fun getMemberBy(longValue: Long) {}
```

해당 코드는 컴파일에서는 정상동작한다.

하지만, ‘런타임에서는 Long 타입으로 인식되기에 원하는 동작이 이루어지지 않을 수 있다.’라는 의문이 생긴 사람이 있을 수도 있다.

그래서 코틀린은 Mangling이라는 기법을 사용한다. (함수의 이름 뒤에 hashcode를 붙이는 것이다.)

```kotlin
class LabValueObjcet(
    val memberId: MemberId,
    val age: Long,
    val authority: Authority,
)

class Authority(
    val authorityId: AuthorityId = AuthorityId(1L),
)

@JvmInline
value class MemberId(val id: Long)

@JvmInline
value class AuthorityId(val id: Long)

```

- 객체는 참조 값

![img.png](img/Kotlin%20Value%20Class-001.png)

- Value Class는 primitive 값이 들어감

Value Class의 활용 - pk값 ~~

를 참고하면 Value Class의 활용에 대한 것을 볼 수 있음.
