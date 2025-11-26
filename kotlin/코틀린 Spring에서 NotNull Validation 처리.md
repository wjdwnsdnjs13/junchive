코틀린에서 NotNull Validation은 사용이 안된다.

NotNull 타입으로 선언하게 될 경우 Validation이 걸리기 전 TypeMissMatch가 되어서 Excpetion이 발생한다.

방법은
1. Request 필드들을 Nullable 타입으로 선언하고 Validation을 사용한다
2. 필드는 NotNull타입으로 하고, ExceptionHnadler를 사용해서 Excpetion을 잡는다

1번의 경우 코틀린의 장점을 전부 뭉개버린다고 생각한다. 왜냐하면 나중에 DTO나 Domain으로 매핑할 때 전부 not null assertion(!!)를 사용해야 하기 때문이다.

그래서 ExceptionHandler를 사용하게 됐고 그 과정을 정리했다.

![img.png](img/Kotlin%20Value%20Class-001.png)

```bash
 o.s.w.s.m.s.DefaultHandlerExceptionResolver - Resolved [org.springframework.http.converter.HttpMessageNotReadableException: JSON parse error: Instantiation of [simple type, class member.presentation.request.MemberCreateRequest] value failed for JSON property nickname due to missing (therefore NULL) value for creator parameter nickname which is a non-nullable type]

```

JSON 파싱 과정에서 NotNull 타입인 nickname이 없어서

HttpMessageNotReadableException이 발생하는 것을 볼 수 있다.

이 HttpMessageNotReadableException를 Handler에서 잡으면 예외처리는 바로 해결된다.

다만, 이제 생기는 문제점이 @NotNull 어노테이션을 사용해서 어떤 필드에서 문제가 생겼는지 메시지를 내리고 싶어질 때다.

HttpMessageNotReadableException는 역질렬화 과정에서 발생하기 때문에 직접 알 수는 없다

그래서 원인이 되는 근본 예외로 jackson 라이브러리의 MismatchedInputException가 들어있는데, 얘의 메시지를 파싱하면 어떤 타입을 변환하려다 실패했는지 볼 수 있다.

타입 미스매치 → Long 타입에 String 타입의 데이터를 넣으면 이것도 타입 미스매치로 잡히지 않는가?

## 자바에서

```java
public record TestPostRequest(
        @NotNull(message = "널이면 안됨")
        String str,
        int num
){}

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorCode> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.info("HttpMessageNotReadableException: {}", e.getMessage());

        ErrorCode errorCode;
        Throwable cause = e.getCause();

        if (cause instanceof MismatchedInputException mismatchedInputException) {
            String fieldPath = mismatchedInputException.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));

            errorCode = new ErrorCode(
                    HttpStatus.BAD_REQUEST,
                    "FIELD-400-01",
                    "필드 '" + fieldPath + "'의 값이 누락되었거나 형식이 올바르지 않습니다."
            );
        } else {
            errorCode = new ErrorCode(
                    HttpStatus.BAD_REQUEST,
                    "JSON-400-01",
                    "JSON 파싱 중 에러가 발생했습니다."
            );
        }

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorCode);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = error.getDefaultMessage() != null ? error.getDefaultMessage() : "Error 설정된 메시지가 없습니다.";
            errors.put(field, message);
        });

        return ResponseEntity
                .status(e.getStatusCode())
                .body(errors);
    }
}
```

![img_1.png](img_1.png)

num이 int인데, int로 변환되지 않는 String 값으로 입력한 경우에도 보다시피 TypeMisMatch로 인한 HttpMessageNotReadableException이 발생해 Handler에서 잡혔다.

![img_2.png](img_2.png)

코틀린에서와 다르게 java의 notNull은 MethodArgumentNotValidException으로 잘 잡히는 것을 볼 수 있다.
