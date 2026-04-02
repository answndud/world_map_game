# [Spring Boot 게임 플랫폼 포트폴리오] 04. `application.yml`과 profile 전략으로 local, test, browser-smoke, prod를 어떻게 분리했는가

## 1. 이번 글에서 풀 문제

프로젝트가 커질수록 가장 먼저 무너지는 것은 코드보다 **환경 경계**입니다.

WorldMap은 아래 질문을 분명히 해야 했습니다.

- base `application.yml`에는 무엇만 남기고, 무엇은 profile로 내려야 하는가
- 왜 `local`, `test`, `browser-smoke`, `prod`가 서로 다른 목적을 가져야 하는가
- 왜 `prod`에서는 `ddl-auto=validate`, Redis session, readiness probe를 같이 봐야 하는가
- 왜 base profile이 `local`을 강제하면 안 되는가

이 글은 현재 저장소 기준의 profile contract를 설명합니다.  
즉, "예전에 local default였던 시절"이 아니라 **지금 무엇이 source of truth인지**를 적습니다.

## 2. 최종 도착 상태

이 글이 끝났을 때 설정은 아래처럼 분리돼 있어야 합니다.

- [application.yml](../src/main/resources/application.yml)은 공통값만 담고 `spring.profiles.default=local`을 강제하지 않는다
- [application-local.yml](../src/main/resources/application-local.yml)은 개발 생산성 중심이다
- [application-test.yml](../src/main/resources/application-test.yml)은 빠른 자동화 테스트 중심이다
- [application-browser-smoke.yml](../src/main/resources/application-browser-smoke.yml)은 브라우저 smoke가 local Redis에 기대지 않도록 독립된 전제를 만든다
- [application-prod.yml](../src/main/resources/application-prod.yml)은 `ddl-auto=validate`, graceful shutdown, forwarded headers, readiness `db+redis+ping`, secure session cookie 기준을 가진다
- [RedisSessionProdConfiguration.java](../src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)이 `prod` 프로필에서만 Redis-backed HTTP session을 켠다

즉, 이 단계가 끝나면 "어디서 어떤 profile을 써야 하는가"를 코드로 설명할 수 있어야 합니다.

## 3. 먼저 알아둘 개념

### 3-1. base config

모든 환경이 공유해야 하는 최소 공통값만 둡니다.  
base에 너무 많은 값을 넣으면 later stage에서 환경 책임이 뒤섞입니다.

### 3-2. profile contract

profile은 단순 값 덮어쓰기가 아니라 **실행 목적**입니다.

- `local`: 사람이 직접 만지는 개발 환경
- `test`: 자동화 테스트 환경
- `browser-smoke`: 브라우저 smoke 독립 환경
- `prod`: 실제 배포 환경

### 3-3. prod-safe defaults

운영에서 가장 중요한 것은 "편하게 돌기"보다 "실수로 위험한 값이 켜지지 않기"입니다.

### 3-4. Redis-backed session

로컬과 테스트에서는 단순 servlet session으로 충분하지만, 운영에서는 task 재기동과 스케일아웃을 견디는 session 저장소가 필요합니다.

## 4. 이번 글에서 다룰 파일

- [application.yml](../src/main/resources/application.yml)
- [application-local.yml](../src/main/resources/application-local.yml)
- [application-test.yml](../src/main/resources/application-test.yml)
- [application-browser-smoke.yml](../src/main/resources/application-browser-smoke.yml)
- [application-prod.yml](../src/main/resources/application-prod.yml)
- [RedisSessionProdConfiguration.java](../src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)
- [ApplicationConfigTest.java](../src/test/java/com/worldmap/common/config/ApplicationConfigTest.java)
- [ProdProfileConfigTest.java](../src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)
- [BrowserSmokeProfileConfigTest.java](../src/test/java/com/worldmap/common/config/BrowserSmokeProfileConfigTest.java)
- [RedisSessionConfigurationIntegrationTest.java](../src/test/java/com/worldmap/common/config/RedisSessionConfigurationIntegrationTest.java)

## 5. 핵심 도메인 모델 / 상태

이 글의 핵심 모델은 엔티티가 아니라 **환경별 런타임 상태**입니다.

### 5-1. base application state

모든 환경이 공유하는 값:

- app name
- Thymeleaf template prefix/suffix
- `spring.data.redis.repositories.enabled=false`
- `spring.jpa.open-in-view=false`
- actuator exposure
- country seed on
- ranking key prefix default
- legacy rollback default false

### 5-2. local state

개발 생산성 중심:

- Compose 자동 시작
- cache off
- `ddl-auto=update`
- demo/admin bootstrap 기본 활성
- legacy rollback 활성

### 5-3. test state

자동화 격리 중심:

- H2
- `create-drop`
- compose off
- bootstrap off
- 테스트용 ranking key prefix

### 5-4. prod state

운영 안정성 중심:

- datasource/redis host를 env var로 받음
- `ddl-auto=validate`
- graceful shutdown
- secure cookie
- readiness에 `db`, `redis`, `ping`
- demo bootstrap off

### 5-5. browser-smoke state

브라우저 smoke가 실수로 로컬 Redis에 기대지 않도록,

- Redis host: `127.0.0.1`
- Redis port: `6390`
- legacy rollback false

로 고정합니다.

## 6. 설계 구상

### 왜 base `application.yml`에서 local default를 제거했는가

과거에는 `spring.profiles.default=local` 같은 선택이 편해 보일 수 있습니다.  
하지만 규모가 커지면 이 값이 아래 위험을 만듭니다.

- 운영에서 profile 누락 시 local-friendly 값이 켜질 수 있음
- CI/스크립트 실행 시 의도치 않게 local behavior가 섞임
- "지금 어떤 환경으로 도는가"를 문서만 봐서는 알기 어려움

그래서 현재 저장소는 base config에서 local default를 강제하지 않습니다.

### 왜 profile별 목적을 먼저 나눴는가

설정 파일을 값 묶음으로만 보면 글이 약해집니다.  
WorldMap은 먼저 "누가 이 환경을 쓰는가"를 나눕니다.

- 개발자: `local`
- 테스트 러너: `test`
- 브라우저 smoke: `browser-smoke`
- 배포 런타임: `prod`

이렇게 해야 각 값의 존재 이유가 설명됩니다.

### 왜 prod에서 `ddl-auto=validate`가 중요한가

운영에서 schema를 앱이 마음대로 바꾸게 두면, 서비스는 편해 보여도 데이터 변경 경로를 설명할 수 없습니다.

WorldMap은 production-ready 단계에서 다음 원칙으로 바꿨습니다.

- local: `update`
- test: `create-drop`
- prod: `validate`

즉, 운영은 "현재 schema가 기대와 일치하는지 확인"만 하고, migration 전략은 별도로 다룹니다.

### 왜 Redis session을 prod 전용으로 뒀는가

local/test까지 Redis session을 강제하면 개발과 테스트가 너무 무거워집니다.  
하지만 prod에서는 task 재기동/수평 확장 때문에 durable session이 필요합니다.

그래서 session 저장 전략도 환경별 목적에 맞게 나눴습니다.

## 7. 코드 설명

### 7-1. `application.yml`: 정말 공통인 것만 남긴다

[application.yml](../src/main/resources/application.yml)에서 봐야 할 핵심은 "없는 값"입니다.

현재 base config는 아래만 둡니다.

- `spring.application.name=worldmap`
- Thymeleaf prefix/suffix
- Redis repository 비활성화
- `spring.jpa.open-in-view=false`
- Whitelabel error off
- actuator health/info 노출
- `worldmap.seed.countries.enabled=true`
- `worldmap.ranking.key-prefix=leaderboard`
- `worldmap.legacy.rollback.enabled=false`

그리고 중요한 점:

- `spring.profiles.default`가 없다

이 한 줄이 없어야 base가 local을 강제하지 않는다는 뜻이 성립합니다.

### 7-2. `application-local.yml`: 브라우저 수동 검증을 빠르게 만든다

[application-local.yml](../src/main/resources/application-local.yml)은 개발자 편의 환경입니다.

핵심 값:

- `SessionAutoConfiguration` 제외
- Thymeleaf cache off
- Docker Compose `start_only`
- `spring.jpa.hibernate.ddl-auto=update`
- SQL init off
- `worldmap.legacy.rollback.enabled=true`
- admin bootstrap 기본 on
- demo bootstrap 기본 on

즉, local은 "빠르게 띄우고, 바로 만지고, 바로 확인하는 환경"입니다.

### 7-3. `application-test.yml`: 통합 테스트는 빠르고 격리되게

[application-test.yml](../src/main/resources/application-test.yml)은 아래를 고정합니다.

- `docker.compose.enabled=false`
- H2 datasource
- `ddl-auto=create-drop`
- Redis host `localhost:6379`
- admin/demo bootstrap off
- `worldmap.ranking.key-prefix=test:leaderboard`

여기서 Redis host/port가 남아 있는 이유는 later stage의 ranking read path 일부가 Redis endpoint를 참조하기 때문입니다.  
다만 browser smoke에서는 이 의존까지 다시 끊습니다.

### 7-4. `application-browser-smoke.yml`: 로컬 Redis에 기대지 않는 smoke

[application-browser-smoke.yml](../src/main/resources/application-browser-smoke.yml)은 매우 작지만 중요합니다.

- Redis host: `127.0.0.1`
- Redis port: `6390`
- legacy rollback false

의도는 명확합니다.  
브라우저 smoke가 실수로 local에서 켜 둔 Redis `6379`에 붙어 "우연히" 통과하면 안 됩니다.

### 7-5. `application-prod.yml`: 운영 안정성 계약

[application-prod.yml](../src/main/resources/application-prod.yml)은 현재 운영 규칙을 가장 명확하게 보여 줍니다.

핵심 항목:

- Thymeleaf cache true
- docker compose off
- `spring.lifecycle.timeout-per-shutdown-phase=20s`
- datasource URL/username/password를 env로 받음
- `ddl-auto=validate`
- Redis host/port/SSL env 기반
- `server.forward-headers-strategy=native`
- `server.shutdown=graceful`
- session timeout 14d
- cookie name `WMSESSION`
- cookie `http-only=true`, `same-site=lax`, `secure=true`
- health probe group: liveness / readiness
- readiness includes `db,redis,ping`
- demo bootstrap off

이 설정은 단순 YAML이 아니라 **ECS/ALB 뒤에서 안전하게 돌리기 위한 운영 계약**입니다.

### 7-6. `RedisSessionProdConfiguration`: prod에서만 Redis session on

[RedisSessionProdConfiguration.java](../src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)는 아래를 선언합니다.

- `@Profile("prod")`
- `@EnableRedisIndexedHttpSession`
- max inactive interval 14일
- namespace `worldmap:session`

중요한 이유:

- local/test는 기존 servlet session 흐름을 유지
- prod만 Redis-backed session으로 올라감

즉, session 저장소도 profile 경계 위에서 설명됩니다.

## 8. 요청 흐름 / 상태 변화

이 글은 HTTP 요청보다 **profile resolution flow**가 핵심입니다.

### 8-1. 일반 local 실행 흐름

```text
./gradlew bootRun --args='--spring.profiles.active=local'
-> application.yml 로드
-> application-local.yml overlay
-> local datasource / compose / bootstrap 규칙 적용
-> 브라우저에서 개발 서버 확인
```

### 8-2. 자동화 테스트 흐름

```text
./gradlew test
-> application.yml 로드
-> application-test.yml overlay
-> H2 / compose off / test key prefix 적용
-> MockMvc / integration test 실행
```

### 8-3. browser smoke 흐름

```text
./gradlew browserSmokeTest
-> test profile + browser-smoke profile 조합
-> base + test + application-browser-smoke.yml
-> Redis 6390로 우회
-> 로컬 Redis에 기대지 않는 브라우저 smoke 실행
```

### 8-4. prod 실행 흐름

```text
SPRING_PROFILES_ACTIVE=prod
-> application.yml 로드
-> application-prod.yml overlay
-> RedisSessionProdConfiguration 활성화
-> prod datasource / redis / readiness / secure cookie 기준 적용
```

## 9. 실패 케이스 / 예외 처리

- base가 local default를 강제하면: profile 누락 시 운영에서도 local-friendly 값이 켜질 수 있다
- prod에 `ddl-auto=update`가 남아 있으면: runtime이 schema를 변경할 수 있다
- browser smoke가 local Redis를 그대로 보면: CI나 클린 머신에서 재현이 안 된다
- local/test/prod session 전략이 섞이면: 같은 앱인데 환경에 따라 로그인/ownership 동작이 설명 불가능해진다
- readiness가 Redis를 보지 않으면: 앱은 떠 있어도 public ranking/session 관련 문제를 초기에 못 잡는다

## 10. 테스트로 검증하기

현재 profile contract를 직접 고정하는 테스트는 아래 네 축입니다.

### 10-1. [ApplicationConfigTest.java](../src/test/java/com/worldmap/common/config/ApplicationConfigTest.java)

- base `application.yml`에 `spring.profiles.default`가 없는지
- `worldmap.legacy.rollback.enabled=false`가 기본인지

### 10-2. [ProdProfileConfigTest.java](../src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java)

- prod가 ECS-ready 기본값을 갖는지
- `ddl-auto=validate`, graceful shutdown, secure cookie, readiness group이 맞는지
- datasource/redis가 env placeholder 기반인지

### 10-3. [BrowserSmokeProfileConfigTest.java](../src/test/java/com/worldmap/common/config/BrowserSmokeProfileConfigTest.java)

- browser-smoke profile이 Redis를 `127.0.0.1:6390`로 돌리는지
- legacy rollback이 꺼져 있는지

### 10-4. [RedisSessionConfigurationIntegrationTest.java](../src/test/java/com/worldmap/common/config/RedisSessionConfigurationIntegrationTest.java)

- `prod` profile일 때 실제 session repository가 Redis-backed implementation으로 올라오는지

즉, 현재 자동 테스트가 직접 고정하는 범위는 `YAML 값과 wiring`입니다.
이 테스트들은 `prod`에서 Redis session이 실제 다중 task 재기동 이후에도 유지되는지,
또는 readiness payload가 배포 환경에서 기대한 contributor 상태를 모두 내는지까지는 증명하지 않습니다.
그 범위는 뒤의 runtime/verification 글에서 수동 운영 절차와 함께 읽어야 합니다.

실행 명령은 아래입니다.

```bash
./gradlew test \
  --tests com.worldmap.common.config.ApplicationConfigTest \
  --tests com.worldmap.common.config.ProdProfileConfigTest \
  --tests com.worldmap.common.config.BrowserSmokeProfileConfigTest \
  --tests com.worldmap.common.config.RedisSessionConfigurationIntegrationTest
```

실행 확인용 예시는 아래지만, 이것도 "로컬에서 profile 해석이 되는가"를 보는 수준입니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

## 11. 회고

profile 전략은 보통 "YAML 정리" 정도로 취급되지만, 실제로는 프로젝트의 실행 철학을 가장 노골적으로 보여 줍니다.

WorldMap에서 이 구조의 장점은 아래입니다.

- base/local/test/prod의 목적이 명확하다
- 운영 위험한 값이 local에서만 머문다
- 브라우저 smoke와 prod runtime을 설명 가능한 profile로 분리했다
- Redis session도 "운영에서만 필요한 책임"으로 깔끔하게 설명된다

### 현재 구현의 한계

- migration 도구는 아직 별도 전략으로 남아 있다
- local은 여전히 `ddl-auto=update`에 기대는 편의성이 있다
- test profile 일부는 ranking read path 때문에 Redis endpoint 가정을 완전히 버리지는 못했다
- `RedisSessionConfigurationIntegrationTest`는 prod wiring test이지, 실제 scale-out session persistence smoke는 아니다

## 12. 취업 포인트

### 12-1. 1문장 답변

WorldMap은 base/local/test/browser-smoke/prod를 목적별로 분리하고, prod에서는 `ddl-auto=validate`, Redis session, readiness probe, secure cookie를 명시적으로 고정해 운영 위험을 낮췄습니다.

### 12-2. 30초 답변

처음에는 단순히 profile을 나누는 수준이 아니라, 각 환경의 사용자를 먼저 정의했습니다. base `application.yml`에는 공통값만 두고 local default를 제거했고, `local`은 빠른 개발, `test`는 자동화, `browser-smoke`는 로컬 Redis 독립성, `prod`는 ECS 운영 안전성을 담당하게 했습니다. 특히 prod는 `ddl-auto=validate`, readiness `db+redis+ping`, secure session cookie, graceful shutdown을 같이 묶고, Redis-backed session도 `@Profile("prod")`로만 켜서 환경 책임을 설명 가능하게 만들었습니다.

### 12-3. 예상 꼬리 질문

- 왜 base config에서 local default를 제거했나요?
- 왜 prod만 Redis session을 켜나요?
- 왜 browser smoke에 별도 profile이 필요한가요?
- `ddl-auto=validate`로 바꾼 뒤 schema 변경은 어떻게 하나요?

## 13. 시작 상태

- Spring Boot는 뜨지만 환경 간 경계가 모호하다
- local-friendly 옵션이 어디까지 운영으로 새어 나갈지 설명하기 어렵다
- browser smoke와 prod runtime을 profile 수준에서 분리하지 못했다

## 14. 이번 글에서 바뀌는 파일

- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-test.yml`
- `src/main/resources/application-browser-smoke.yml`
- `src/main/resources/application-prod.yml`
- `src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java`
- `src/test/java/com/worldmap/common/config/ApplicationConfigTest.java`
- `src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java`
- `src/test/java/com/worldmap/common/config/BrowserSmokeProfileConfigTest.java`
- `src/test/java/com/worldmap/common/config/RedisSessionConfigurationIntegrationTest.java`

## 15. 구현 체크리스트

1. 공통값을 `application.yml`로 모은다
2. base에서 local default를 제거한다
3. local profile에 개발 친화 옵션을 둔다
4. test profile에 H2/compose off를 둔다
5. browser-smoke profile로 Redis 독립성을 만든다
6. prod profile에 datasource/redis env, graceful shutdown, readiness, secure cookie를 둔다
7. prod 전용 Redis session configuration을 추가한다
8. YAML 구조와 Redis session wiring을 테스트로 고정한다

## 16. 실행 / 검증 명령

```bash
./gradlew test \
  --tests com.worldmap.common.config.ApplicationConfigTest \
  --tests com.worldmap.common.config.ProdProfileConfigTest \
  --tests com.worldmap.common.config.BrowserSmokeProfileConfigTest \
  --tests com.worldmap.common.config.RedisSessionConfigurationIntegrationTest
```

실행 확인용 예시:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

## 17. 산출물 체크리스트

- base config가 local을 강제하지 않는다
- local/test/browser-smoke/prod의 목적이 서로 분리된다
- prod는 `ddl-auto=validate`, readiness `db+redis+ping`, secure cookie를 가진다
- prod에서만 Redis-backed session이 켜진다
- profile contract가 테스트로 고정된다

## 18. 글 종료 체크포인트

- 왜 profile은 값 묶음이 아니라 실행 목적이어야 하는가
- 왜 base config에서 local default를 제거해야 하는가
- 왜 prod의 session, schema, health probe 계약을 early 문서로 남겨야 하는가
- 왜 browser smoke는 별도 profile이 필요한가

## 19. 자주 막히는 지점

- base `application.yml`에 local-friendly 값을 너무 많이 넣는 것
- prod와 local을 거의 같은 YAML로 두고 "나중에 정리하자"고 미루는 것
- browser smoke가 local machine 상태에 우연히 의존해도 괜찮다고 보는 것
- Redis session을 local/test/prod 전부에 강제해서 개발과 테스트를 unnecessarily 무겁게 만드는 것
