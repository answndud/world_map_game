# prod에서만 Spring Session Redis를 켜서 멀티태스크 준비하기

이제 배포 준비에서 남은 핵심 질문은 이거였다.

> ECS task를 2개로 늘리면 로그인 세션과 guest 세션은 어떻게 유지할까?

현재 프로젝트는 로그인과 게스트 식별 모두 `HttpSession`을 많이 쓴다.

- `MemberSessionManager`
- `GuestSessionKeyManager`
- `/dashboard` role 검사
- 게임 시작 시 member / guest 분기

즉 컨테이너가 1개일 때는 문제가 없지만, task가 2개가 되면 세션을 컨테이너 메모리 안에만 두는 구조로는 버틸 수 없다.

이번 조각은 그 문제를 해결하기 위한 첫 단계다.

## 핵심 전략

이번에는 코드를 크게 뒤엎지 않았다.

대신 기준을 이렇게 잡았다.

- local/test: 기존 servlet session 유지
- prod: Spring Session Redis 활성화

즉 비즈니스 코드는 그대로 `HttpSession`만 보고,
실제 backing store만 prod에서 Redis로 바꾸는 방식이다.

## 변경 파일

- [build.gradle](/Users/alex/project/worldmap/build.gradle)
- [application.yml](/Users/alex/project/worldmap/src/main/resources/application.yml)
- [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)
- [ProdProfileConfigTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)
- [RedisSessionConfigurationIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/RedisSessionConfigurationIntegrationTest.java)

## 1. 의존성 추가

먼저 Redis-backed session을 위해 의존성을 추가했다.

```gradle
implementation 'org.springframework.session:spring-session-data-redis'
```

이 라이브러리가 들어오면 Spring Boot는 session store를 Redis로 연결할 준비를 한다.

## 2. 기본값은 `none`

중요한 건 local/test 개발 흐름을 깨지 않는 것이다.

그래서 local/test는 auto-configuration을 명시적으로 제외해 기존 servlet session 흐름을 유지하도록 정리했다.

## 3. prod에서만 Redis-backed session 활성화

실제 Redis-backed session 활성화는 [RedisSessionProdConfiguration.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)에 모았다.

```java
@Configuration
@Profile("prod")
@EnableRedisIndexedHttpSession(
    maxInactiveIntervalInSeconds = 60 * 60 * 24 * 14,
    redisNamespace = "worldmap:session"
)
```

즉 `prod` 프로필일 때만 Redis session repository가 생긴다.

반대로 [application-local.yml](/Users/alex/project/worldmap/src/main/resources/application-local.yml) 과 [application-test.yml](/Users/alex/project/worldmap/src/main/resources/application-test.yml) 은 `SessionAutoConfiguration`을 제외해서 예전 servlet session 흐름을 그대로 유지한다.

쿠키와 timeout 기준은 [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)에 남겼다.

```yaml
server:
  servlet:
    session:
      timeout: 14d
      cookie:
        name: WMSESSION
        http-only: true
        same-site: lax
        secure: true
```

이 설정이 의미하는 건 아래와 같다.

- `RedisSessionProdConfiguration`
  - prod에서는 세션 backing store를 Redis로 쓴다.
- `redisNamespace=worldmap:session`
  - Redis key 충돌을 줄이기 위한 namespace다.
- `timeout=14d`
  - 세션 유효 기간 기준이다.
- `cookie.name=WMSESSION`
  - 운영 세션 쿠키 이름을 명시적으로 고정했다.
- `same-site=lax`
  - 일반적인 브라우저 이동에서 안전한 기본값이다.
- `secure=true`
  - HTTPS 뒤에서만 쿠키를 보내도록 한다.

## 4. 왜 `MemberSessionManager`는 안 바꿨나

이 조각의 중요한 설계 포인트는
`MemberSessionManager`, `GuestSessionKeyManager`를 거의 안 건드렸다는 점이다.

그 이유는 이 코드들이 이미 `HttpSession` 인터페이스에만 의존하고 있기 때문이다.

즉:

- 로그인 시 attribute 저장
- guest key 저장
- role 읽기

이 책임은 그대로 두고,
그 아래 backing store만 prod에서 Redis로 바꾸는 편이 더 설명 가능하다.

이게 바로 이번 조각의 장점이다.

> 비즈니스 로직은 유지하고, 세션 저장 책임만 인프라로 옮겼다.

## 5. 테스트

이번 조각은 아래 기준으로 닫았다.

- `./gradlew test --tests com.worldmap.common.config.ProdProfileConfigTest --tests com.worldmap.common.config.RedisSessionConfigurationIntegrationTest`
- `./gradlew test`
- `git diff --check`

[ProdProfileConfigTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)는 prod profile에 아래 값이 실제로 들어갔는지 확인한다.

- `spring.session.store-type=redis`
- `spring.session.redis.namespace`
- `server.servlet.session.timeout=14d`
- `server.servlet.session.cookie.name=WMSESSION`
- `same-site=lax`
- `secure=true`

[RedisSessionConfigurationIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/RedisSessionConfigurationIntegrationTest.java)는 prod+test 조합으로 컨텍스트를 띄운 뒤 Redis 기반 session repository bean이 실제로 생성되는지 확인한다.

## 6. 아직 끝난 건 아니다

이 조각은 “설정 추가”이지, “멀티태스크 검증 완료”가 아니다.

아직 남은 건 아래다.

1. 실제 ECS에서 Redis 연결 확인
2. 로그인 후 새 요청이 다른 task에서도 유지되는지 확인
3. 그 다음에만 `desiredCount=2`로 올리기

즉 지금은

- 이론적으로 준비됨
- 코드 기준은 고정됨
- 실제 배포 검증은 아직 남음

상태다.

## 면접에서 30초 설명

> 이번에는 prod에서만 Spring Session Redis를 켰습니다. 핵심은 로그인/게스트 코드를 거의 바꾸지 않고, `HttpSession`의 backing store만 Redis로 바꾼 점입니다. 그래서 local/test는 기존 servlet session을 유지하고, prod에서는 `WMSESSION` 쿠키와 14일 TTL, Redis namespace를 기준으로 멀티태스크 배포 준비 상태를 만들었습니다.
