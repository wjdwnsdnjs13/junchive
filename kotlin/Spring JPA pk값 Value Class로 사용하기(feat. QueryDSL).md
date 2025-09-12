## 배경
디프만 코어에서는 코틀린 Spring을 사용하고 있다.
primitive 타입의 장점과 class의 장점을 모두 살리기 위한 목적으로 Value Class를 사용하고 있다.
Value Class의 자세한 건 아래 글을 참고하면 된다.
- 블로그 글 링크 참고

타입 안정성을 위해 식별자 값들을 Value Class로 감싸서 사용하고 있다.
예를 들어 Member 엔티티의 식별자 값인 memberId는 Long 타입이지만, MemberId라는 Value Class로 감싸서 사용하는 것이다.

사실 단순히 Value Class만 공부했을 땐 어디서 사용하면 좋을지 감이 안 잡힐 수 있다.
근데 잘 생각해보면 DB의 식별자 값은 불변하고 보통 Long같은 primitive 타입을 사용한다.
또한 식별자가 같으면 같은 객체로 간주하기 때문에 Value Object의 특징과도 잘 맞아떨어진다.
그래서 도메인의 식별자 값들을 Value Class로 감싸서 사용하고 있다.

해당 글에서는 예제를 통해 JPA Entity의 Pk 값으로 Value Class를 사용하는 법에 대해서 알아보고자 한다.
매우 간단한 예제를 통해 Value Class를 JPA Entity의 Pk로 사용해 볼 예정이다.

## 예제
### 도메인

```kotlin
class LabValueObject(
    val memberId: MemberId = MemberId(0L),
    val age: Long,
    val authorityId: AuthorityId,
)

@JvmInline
value class MemberId(val id: Long)

@JvmInline
value class AuthorityId(val id: Long)
```

LabValueObject 도메인은 memberId, age, authorityId 값을 갖고 있다.
식별자들을 memberId, authorityId라는 Value Class로 감싸서 사용하고 있다.
따라서 equals, hashcode도 Value Class의 값으로 비교하게 된다.

다른 값들은 비교할 필요 없이 식별자 값의 equals만으로도 비교할 수 있게된다.
추가로 타입 안정성을 가져가기 때문에 실수로 Long 타입을 넣거나, 다른 식별자 값을 넣는 실수를 방지할 수 있다.

### 엔티티

```kotlin
@Entity
class LabValueObjectEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    val memberId: MemberId,
    @Column(name = "age")
    val age: Long,
    @Column(name = "authority_id")
    val authorityid: AuthorityId,
) {
    companion object {
        fun from(domain: LabValueObject): LabValueObjectEntity =
            LabValueObjectEntity(
                memberId = domain.memberId,
                age = domain.age,
                authorityid = domain.authorityId
            )
    }
}
```
위와 같이 보통은 Pk 값은 Long, Int 같은 primitive 타입을 사용한다.
그리고 @GeneratedValue 어노테이션을 사용해 자동 증가하도록 설정한다.
그런데 위와 같이 Value Class로 감싼 MemberId 타입을 Pk로 사용해도 문제없이 잘 동작한다.

NotNull 타입으로 걸었는데 이는 JPA가 Insert 시에 @GeneratedValue로 자동 증가하는 값을 넣어주기 때문에 어떤 값이 들어가 있더라도 무시되기 때문이다.

아래는 실제 API를 호출해 Insert를 수행한 결과다.

### API 호출 결과
memberId를 null로 두고 API를 호출한 결과다.

![Entity Pk Value Class-001.png](img/Entity%20Pk%20Value%20Class-001.png)

![Entity Pk Value Class-002.png](img/Entity%20Pk%20Value%20Class-002.png)

Auto Increment가 제대로 적용되어 1, 2, 3, 4 순차적으로 성공적으로 증가했다.

## Value Class를 사용해도 정상 동작하는 이유?
JPA 구현체로 주로 사용되는 Hibernate는 @JvmInline이 붙은 Value Class를 컴파일 타임에 id: Long 처럼 primitive 타입으로 바꿔준다.

이런 자동 언박싱 기능 덕분에 정상 동작이 가능한 것이다.

다만 주의해야할 점은 JPA Provider인 Hibernate가 Value Class를 primitive 타입으로 바꿔주기 때문에, 런타임 시점에서는 MemberId, AuthorityId 같은 Value Class가 아닌 Long 타입으로 인식된다는 점이다.
이는 암시적인 변환이라서 혹시나 일관적이고 명시적인 동작을 원하면 Converter를 직접 설정해주는 방법도 있다.

---

## Query DSL 사용 시 문제점
### KSP 사용 시 QClass 변환에서 오류 발생

```bash
...
MemberPerRoleEntity.memberPerRoleId: Type was not recognised, This may be an entity that has not been annotated with @Entity, or maybe you are using javax instead of jakarta.
...

e: Error occurred in KSP, check log for detail

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':...:kspKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details
...
```

QClass 변환 과정에서 KSP가 Entity의 Long, Int 같은 표준 타입을 받지 못해서 생기는 문제다.

Value Class는 KSP가 변환할 수 없는 타입이기 때문에 생기는 에러다.
KSP는 기본적으로 Long, Int, String 같은 표준 타입만 인식한다.
그래서 Value Class라는 클래스를 인식하지 못하기 때문에 위와 같은 에러가 발생한다.

Converter를 설정하면 해결할 수 있지만, 번거로운 작업이 필요하다.(식별자마다 생성해줘야 할수도...)
(이를 해결하기 위해서는 Spring Converter를 직접 설정해서 Value Object를 Pk에 해당하는 기본 타입(Long, String 등)으로 변환해주는 과정이 필요하다)
이 과정이 귀찮다면 Query DSL 사용 시 Entity에서는 Value Class를 사용하지 않는 것이 좋을 수 있다.

## 마무리
JPA Entity의 Pk 값으로 Value Class를 사용하는 방법에 대해서 알아보았다.
Value Class를 사용하면 타입 안정성을 가져갈 수 있고, equals, hashcode도 Value Class의 값으로 비교할 수 있기 때문에 도메인의 식별자 값들을 Value Class로 감싸서 사용하는 것을 추천한다.
타입 안정성을 갖고 갈 수 있다는 의미는 코드를 작성하면서 발생할 수 있는 휴먼 에러를 방지할 수 있다는 점 때문에 엄청난 이점이라고 생각한다.

예를 들어 MemberId, AuthorityId가 모두 Long 타입일 경우 실수로 MemberId 자리에 AuthorityId를 넣는 실수를 할 수 있다.
그렇게 될 경우 컴파일 언어의 장점인 타입 체크를 통한 오류 방지를 사용할 수 없지만 Value Class로 감싸서 사용하면 컴파일 시점에 타입 체크를 통해 이런 실수를 방지할 수 있다.
추가로 Value Class는 primitive 타입의 장점을 살릴 수 있기 때문에 성능 저하도 거의 없다.

다만 Query DSL 사용 시 KSP가 Value Class를 인식하지 못하는 문제점이 있는 것을 주의하면서 사용해야하긴 한다.

파라미터 Validation 통일이나 이런 곳에서도 사용할 수 있을 것 같아서 많이 시도해보고 있는데 이런 부분들도 성공하게 되면 공유해보도록 하겠다.
