# ECS에서 앱이 어떤 설정으로 떠야 하는지 먼저 분리하기

## 왜 이 조각이 필요한가

Dockerfile이 생겼다고 바로 배포 준비가 끝나는 건 아니다.

이미지가 있어도 여전히 이런 문제가 남는다.

- DB 주소는 어디서 읽는가
- Redis 주소는 어디서 읽는가
- local demo bootstrap은 prod에서 꺼야 하지 않나
- ALB 뒤에서 forwarded header는 어떻게 처리하나

즉, Dockerfile 다음에는 반드시
`prod profile이 어떤 값을 source of truth로 삼는지`
를 먼저 분리해야 한다.

이번 조각의 목적은
`application-prod.yml`을 추가해서 ECS task definition이 주입할 환경변수와 prod 기본값을 한 곳에 모으는 것이다.

## 이번에 바뀐 파일

- [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)
- [ProdProfileConfigTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)

같이 아래 문서도 현재 기준으로 맞췄다.

- [README.md](/Users/alex/project/worldmap/README.md)
- [DEPLOYMENT_RUNBOOK_AWS_ECS.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md)
- [PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 어떤 설정을 prod로 분리했나

핵심은 네 가지다.

### 1. datasource

prod에서는 DB 정보를 코드에 넣지 않는다.

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

즉, ECS task definition이나 Secrets Manager/SSM이 실제 값을 주입한다.

### 2. redis

랭킹과 이후 세션 확장을 위해 Redis도 prod에서 환경변수로 받는다.

```yaml
spring:
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST}
      port: ${SPRING_DATA_REDIS_PORT:6379}
      ssl:
        enabled: ${SPRING_DATA_REDIS_SSL_ENABLED:false}
```

여기서 중요한 건 `ssl.enabled` 분기다.

ElastiCache에서 transit encryption을 켜면
애플리케이션 쪽도 TLS를 알아야 한다.

그래서 런북에서 말한 TLS 분기를
이번에 실제 prod profile에 먼저 반영했다.

### 3. local demo bootstrap 차단

prod에서는 local 시연용 계정과 sample run이 생기면 안 된다.

그래서 아래를 고정했다.

```yaml
worldmap:
  demo:
    bootstrap:
      enabled: false
```

즉, prod는 local처럼 `orbit_runner`, demo sample run을 만들지 않는다.

### 4. forwarded headers

ALB 뒤에서 Spring Boot가 돌아가면
forwarded header를 읽어야 redirect와 secure cookie 판단이 맞아진다.

이번 조각에서는 prod 기준을 먼저

```yaml
server:
  forward-headers-strategy: native
```

로 고정했다.

이건 ALB가 일반적인 `X-Forwarded-*` 헤더를 넘긴다는 전제에서
가장 단순한 기본값이다.

## 왜 `ddl-auto=update`를 유지했나

배포 런북에서 이미 정리했듯이
현재 프로젝트는 아직 Flyway가 없다.

테이블도 꽤 많고,
startup initializer가 legacy schema를 정리하는 로직도 있다.

이 상태에서 지금 바로 Flyway를 넣기보다,
첫 공개 배포 기준으로는

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

가 현실적이다.

즉, 이번 조각은 이상적인 최종형보다
**현재 프로젝트를 실제로 올릴 수 있는 기준**을 먼저 정한 것이다.

나중에는

- Flyway baseline
- `ddl-auto=validate`

로 옮겨가면 된다.

## 왜 테스트를 따로 만들었나

설정 파일은 눈으로만 보면 쉽게 틀린다.

특히 이번처럼 environment placeholder가 많으면

- 키 이름 오타
- prod에서 demo bootstrap이 켜진 상태
- Redis TLS 분기 누락
- forwarded header 설정 누락

같은 문제가 생기기 쉽다.

그래서 [ProdProfileConfigTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)를 추가했다.

이 테스트는 `application-prod.yml`을 직접 읽어서
아래가 들어 있는지만 확인한다.

- `spring.docker.compose.enabled=false`
- `spring.thymeleaf.cache=true`
- `spring.jpa.hibernate.ddl-auto=update`
- `server.forward-headers-strategy=native`
- `worldmap.demo.bootstrap.enabled=false`
- datasource / redis env placeholder

즉, 이번 테스트의 목적은 앱 context를 띄우는 것이 아니라
**prod profile 계약이 깨지지 않는지 막는 것**이다.

## 요청 흐름은 어떻게 달라지나

HTTP 요청 흐름은 그대로다.

하지만 배포 입력 흐름은 달라진다.

이전에는:

- local profile
- docker compose
- sample run bootstrap

에 기대고 있었다.

이제 prod에서는:

1. ECS task definition이 env/secrets를 주입
2. Spring Boot가 `application-prod.yml`을 읽음
3. datasource / redis / bootstrap / forwarded header 기준이 정해진 상태로 뜸

즉, 이 조각은 기능 추가가 아니라
**운영 입력 경로를 분리하는 작업**이다.

## 테스트

실행:

```bash
./gradlew test --tests com.worldmap.common.config.ProdProfileConfigTest
git diff --check
```

이번에는 prod profile 값 자체를 읽는 테스트만 추가했다.

아직 context load 테스트를 안 넣은 이유는
실제 datasource/redis endpoint가 없는 상태에서
설정 계약을 먼저 고정하는 편이 더 작고 안전한 조각이기 때문이다.

## 아직 남은 것

이번 조각은 컨테이너 빌드만 연 것이다.

아직 아래는 남아 있다.

- JVM 메모리 옵션
- graceful shutdown
- Actuator readiness/liveness
- Secrets Manager / SSM 연동
- Spring Session + Redis

즉, prod profile은 생겼지만
아직 ECS에 올렸을 때 “운영에 안전한 기준”은 다 안 채워진 상태다.

## 다음 단계

다음 작은 조각은
`forwarded headers + JVM 메모리 옵션 + graceful shutdown + Actuator`
를 한 번에 묶는 것이다.

이 네 개는 ALB 뒤에서 앱이 실제로 안전하게 뜨는 기준이라
같은 묶음으로 가는 편이 설명하기 좋다.

## 면접에서 이렇게 설명할 수 있다

> Dockerfile 다음 조각으로 `application-prod.yml`을 분리했습니다. 핵심은 ECS에서 datasource와 redis를 환경변수로 읽고, demo bootstrap은 끄고, forwarded header와 Redis TLS 분기 기준을 prod profile에 모아 둔 것입니다. 즉 로컬 설정과 운영 설정의 책임을 분리해서, 배포 환경에서 앱이 어떤 입력을 받아 떠야 하는지 설명 가능하게 만들었습니다.
