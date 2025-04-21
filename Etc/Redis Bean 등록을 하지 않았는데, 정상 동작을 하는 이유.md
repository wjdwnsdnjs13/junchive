상황 설명
알아서 local Redis를 찾아서 정상적으로 연결해주고 있음
Bean 등록을 하지 않았음에도 정상 동작을 해서 오히려 멘붕이 온 상태였음

아래는 아무 생각 없이 redis를 사용하려고 작성한 코드다.

```kotlin
@Service
class RedisTestService(
    private val redisTemplate: StringRedisTemplate
) {
    fun set(key: String, value: String, timeoutSeconds: Long = 60) {
        return redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS)
    }

    fun get(key: String): String {
        return redisTemplate.opsForValue().get(key)?: throw RedisException.ResourceNotFound()
    }

    fun delete(key: String): Boolean {
        return redisTemplate.delete(key)
    }
}
```
redis를 사용하기 위한 StringRedisTemplate을 주입받고, 그걸로 redis에 데이터를 넣고 빼는 코드다.
코드를 보면 알겠지만, 그 어디에도 Redis Config를 등록해주지 않았다.

아무 생각 없이 이대로 코드를 돌려서 실행했는데, 정상적으로 동작을 했다.
'헤헤 성공이다 레디스 써야지~'라고 생각하다가 문득 Bean 등록도 안하고 Config는 작성도 안 했다는 게 떠올랐고, 이 상황에서 왜 정상적으로 동작하는지 궁금했다.

처음에는 Redis에 데이터가 들어가는 게 아니라 local cache에 들어가는건가? 라는 생각을 했다.

하지만 local 레디스에 정상적으로 데이터가 들어간다...
![img_1.png](img_1.png)

레디스 컨테이너도 정상적으로 시작되고, 연결도 된다
![img_2.png](img_2.png)

더 멘붕이 오기 시작했다.

심지어 application.yaml 파일에서 연결 부분을 주석처리해도 정상 동작했다.
![img.png](img.png)

여러가지로 찾다가 Redis도 Redis 의존성을 추가하면 Auto Configuration이 동작한다는 것을 알게 됐다.
spring-boot-starter-data-redis를 의존성으로 추가하면, RedisAutoConfiguration이 동작을 하게 되고, 그 안에서 RedisTemplate과 StringRedisTemplate을 Bean으로 등록해준다고 한다.
얘네가 전부 @ConditionalOnMissingBean으로 되어 있어서 Spring Boot가 기본값으로 알아서 세팅해준다는 것이다.

application.yaml도 명시하지 않을 경우에는 기본적으로 localhost:6379로 연결을 해준다.
그래서 로컬 내에 Redis 서버만 켜져 있으면 Redis에 붙어서 정상 동작한다는 것이다.

