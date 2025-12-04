## SAML

- 기업 환경의 웹 앱 SSO을 위한 표준이다.
    - XML 기반 독립적인 표준이다.
- 보안 토큰
    - SAML Assertion (XML 형식)
- 모바일 친화성이 낮다
    - 브라우저 리다이렉션에 크게 의존한다
- API 보안은 고려되지 않고, 인증에만 집중한다
- 구현이 복잡하고 XML 파싱과 처리가 무겁고 복잡하다
- 주로 브라우저를 통한 통신이라고 보면 된다

### 로그인 흐름

![image.png](attachment:c2dfadef-2d67-4f57-81e9-859c7214b697:image.png)

- **"SAML Assertion 제출"**
    - **현재 표현:** 사용자 → 서비스 서버
    - **더 정확한 표현**
        - IdP는 `SAML Response`가 담긴 HTML Form을 부라우저에 보내고, 브라우즈는 수신하자마자 JavaScript에 의해 자동으로 서비스 서버로 POST 제출한다
        - 사용자가 직접 `제출` 버튼을 누르는 게 아니다
        - 이러한 방식을 `SAML HTTP Bindings` 라고 부른다고 한다.
- `SAML Assertion` vs `SAML Response`
    - IdP가 생성해서 서비스 서버에게 주는 건 `SAML Response`라는 XML 문서이다
        - `SAML Assertion`은 `SAML Response`에 포함된 이름이나 이메일 같은 사용자 정보와 인증 정보를 담은 핵심 데이터 조각이다

---

## OIDC

- 웹, 모바일, API 환경에 맞는 SSO 기술이다
- 보안 토큰
    - ID 토큰 (JWT 형식)
- 모바일 친화성이 높다
    - API 기반 통신에 최적화되어 있다
- OAuth 2.0 기반으로, API 보안이 핵심 기능 중 하나이다
- JSON과 Rest API가 익숙한 대부분의 개발자에게 구현 복잡도가 낮다
- 브라우저와 서버 간 직접 통신 모두 용이하다

### 로그인 흐름

![image.png](attachment:05d77417-9aa3-43f4-b2f7-259c81bfc635:image.png)

- OAuth 서버라는 용어
    - OIDC는 OAuth 2.0 프로토콜 위에 만들어진 ‘인증’ 계층이다
        - OAuth 2.0의 Authorization Server(권한 서버) 역할과 OIDC의 OP(OpenID Provider) 역할을 동시에 수행한다
    - 인증 서버라는 의미의 OP(OpenID Provider) 서버 혹은 KeyCloak와 같은 OIDC 용어를 명시하는 것이 더욱 명확하다

---

## 마무리
간단하게 보면 SAML은 XML 기반으로 브라우저 중심이고, OIDC는 JSON 기반으로 API 중심이다.
따라서 RestAPI 환경이거나 모바일도 고려한다면 OIDC가 더 적합하다고 볼 수 있다.

대부분 요즘 환경에서는 RestAPI인데다 모바일/웹 따로 구분지어서 개발하는 경우가 잘 없어서 OIDC가 더 적합할 수 있다.
실제로 최근 대부분의 SSO 솔루션은 OIDC 형태를 많이 사용하는 것을 알 수 있다.
SAML도 지원하는 경우가 있지만, 최근 개발된 SAML을 지원하지 않고 OIDC만 지원하는 경우도 많다.

그럼에도 SAML을 공부해야하는 이유는 내 경우처럼 예전 레거시 시스템을 유지보수하게 될 경우 OIDC를 지원하지 않거나 간접적으로 사용가능한 경우가 있다.
아니면 SAML은 공식지원되지만 OIDC는 비공식적으로만(3rd Party 등) 지원하는 경우도 있었다.

따라서 SAML과 OIDC의 차이를 알고, 상황에 맞게 적용하는 것이 중요한 것 같다.
(결론은 대부분 OIDC를 쓸 것이고 그게 좋은 것 같기도 하다.)
