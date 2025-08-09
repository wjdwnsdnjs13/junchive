# Redis
## 개요
Local 환경에서 Redis 설치까지만하고 yaml 설정과 같은 연결 설정을 하나도 하지 않은 상태였습니다.
Config 클래스도 작성 하지 않았고, 물론 Bean 등록도 하지 않았어요.
이때 아무 생각 없이 코드만 작성해서 돌렸는데 정상 동작을 하더라구요...

돌리고 나서는 당연히 연결이 안 되어야 한다는 생각이 들었는데, 정상 동작을 하고, CRUD가 제대로 동작을 해서 오히려 멘붕이 오게 되었어요.
그래서 원인을 찾고 이를 기록해두기 위해서 글을 작성하게 되었어요.

## 상세 상황 설명
아래는 아무 생각 없이 redis를 사용하려고 작성한 코드예요.

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
redis를 사용하기 위한 StringRedisTemplate을 주입받고, 그걸로 redis에 데이터를 넣고 빼는 코드예요.
코드를 보면 알겠지만, 그 어디에도 Redis Config를 등록해주지 않았습니다.

아무 생각 없이 이대로 코드를 돌려서 실행했는데, 정상적으로 동작을 하더라구요...?
'헤헤 성공이다 레디스 써야지~'라고 생각하다가 문득 Bean 등록도 안하고 Config는 작성도 안 했다는 게 떠올랐고, 이 상황에서 왜 정상적으로 동작하는지 궁금해졌어요.

처음에는 Redis에 데이터가 들어가는 게 아니라 local cache에 들어가는건가? 라는 말도 안되는 생각까지 해봤어요.

## Redis 확인
하지만 local 레디스에 정상적으로 데이터가 들어가더라구요...?
redis cli에서 'key *' 명령어로 redis에 직접 접근해 확인해 본 결과 실제로 데이터가 등록되어 있었어요.
연결이 정상적으로 이루어졌고, 데이터도 정상적으로 저장이 됐다는 증거였죠.
![redis 001.png](img/redis%20001.png)

레디스 컨테이너도 정상적으로 시작되고, 연결도 돼요.
![redis 002.png](img/redis%20002.png)

더 멘붕이 오기 시작...

심지어 application.yaml 파일에서 연결 부분을 주석처리해도 정상 동작하더라구요
![redis 003.png](img/redis%20003.png)

## 원인은...?
여러가지로 찾다가 Redis도 Redis 의존성을 추가하면 Auto Configuration이 동작한다는 것을 알게 됐어요.

![redis 004.png](img/redis%20004.png)
spring-boot-starter-data-redis를 의존성으로 추가하면, RedisAutoConfiguration이 동작을 하게 되고, 그 안에서 RedisTemplate과 StringRedisTemplate을 Bean으로 등록해준다고 해요.
얘네가 전부 @ConditionalOnMissingBean으로 되어 있어서 Spring Boot가 기본값으로 알아서 세팅해준다는 것이죠.

application.yaml도 명시하지 않을 경우에는 localhost에 6379 포트를 기본 값으로 연결해줘요.
그래서 로컬 내에 Redis 서버만 켜져 있으면 Redis에 붙어서 정상 동작한다는 것이죠.


## 마무리
간단하게 redis를 사용해보려다가 생긴 궁금증에서 시작된 학습이었는데, 생각보다 쉽게 나오는 내용들이더라구요.

심지어 yaml 설정이 필요 없던 부분은 default 값이 설정되어 있는 거라서 엄청 간단한 거였구요.

Auto Configuration이 편해서 좋은 건 맞지만...
원리를 모르거나 Auto Configuration이 동작한다는 사실 자체를 모르고 있으면 나중에 문제가 생겼을 때 찾기 쉽지 않겠다는 생각이 들었어요.

역시 모를 땐 코드를 보거나 문서 보는 게 최고 인 거 같네요ㅎㅎ
