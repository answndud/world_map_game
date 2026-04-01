# [Spring Boot 게임 플랫폼 포트폴리오] 03. Docker Compose로 PostgreSQL, Redis 개발 환경을 어떻게 고정했는가

## 1. 이번 글에서 풀 문제

Spring Boot baseline이 떠도, 데이터 저장소와 세션/랭킹 캐시가 일관되게 붙지 않으면 프로젝트는 쉽게 "내 환경에서만 되는 데모"가 됩니다.

WorldMap에서는 아래 문제를 빨리 닫아야 했습니다.

- 로컬 개발자가 언제나 같은 PostgreSQL과 Redis 위에서 실행되게 하려면 어떻게 해야 하는가
- 왜 `application-local.yml`과 `application-test.yml`이 서로 다른 저장 전략을 가져야 하는가
- 왜 local에서는 container dependency를 쓰고, test에서는 그 의존을 최대한 끊어야 하는가

이 글은 현재 저장소 기준의 **개발용 runtime baseline**을 설명합니다.

## 2. 최종 도착 상태

이 글이 끝났을 때 아래 상태가 만들어져야 합니다.

- [compose.yaml](../compose.yaml)이 PostgreSQL 16과 Redis 7을 로컬 공용 개발 인프라로 띄운다
- PostgreSQL은 `worldmap/worldmap/worldmap` 기준으로 접근 가능하다
- Redis는 `6379`에서 AOF(`appendonly yes`)로 실행된다
- 두 컨테이너 모두 healthcheck를 가져서 Spring Boot가 기동 시점을 판단하기 쉽다
- `local` 프로필은 Docker Compose를 자동으로 띄우고, JPA `ddl-auto=update` 기준으로 개발 생산성을 우선한다
- `test` 프로필은 H2 기반 in-memory DB로 돌아가고, Session auto config를 제외해 테스트를 더 가볍게 돌린다

즉, baseline 다음 단계의 목표는 "실행 가능한 서버"를 **항상 같은 dev runtime 위에서 반복 실행 가능하게 만드는 것**입니다.

## 3. 먼저 알아둘 개념

### 3-1. Docker Compose as local runtime contract

Compose는 단순 편의 스크립트가 아니라, 팀과 AI 에이전트가 공유하는 **로컬 인프라 계약**입니다.

### 3-2. local profile

개발자가 직접 브라우저로 기능을 만져 볼 때 쓰는 profile입니다.  
이 환경은 빠른 수정과 재기동을 우선합니다.

### 3-3. test profile

JUnit/MockMvc/통합 테스트를 돌릴 때 쓰는 profile입니다.  
여기서는 속도와 격리가 중요하므로, local과 똑같이 Compose를 붙이지 않습니다.

### 3-4. session / ranking infrastructure

WorldMap은 later stage에서 Redis를 session과 leaderboard에 사용합니다.  
따라서 Redis는 "나중에 추가할 캐시"가 아니라 초기에 존재를 전제로 해야 하는 런타임 축입니다.

## 4. 이번 글에서 다룰 파일

- [compose.yaml](../compose.yaml)
- [application-local.yml](../src/main/resources/application-local.yml)
- [application-test.yml](../src/main/resources/application-test.yml)
- [README.md](../README.md)

이 글의 중심은 HTTP controller가 아니라 **개발 실행 환경 설정**입니다.

## 5. 핵심 도메인 모델 / 상태

이 글의 핵심은 비즈니스 엔티티가 아니라 **런타임 상태**입니다.

### 5-1. local Postgres state

- port: `5432`
- db: `worldmap`
- user: `worldmap`
- password: `worldmap`
- named volume: `worldmap-postgres-data`

이 상태가 유지돼야 country seed, 게임 세션, 인증 데이터가 로컬에서 지속적으로 남습니다.

### 5-2. local Redis state

- port: `6379`
- named volume: `worldmap-redis-data`
- command: `redis-server --appendonly yes`

later stage의 leaderboard/session 동작을 local에서 흉내 내려면 Redis가 항상 같은 포트와 persistence 옵션으로 떠 있어야 합니다.

### 5-3. local Spring Boot state

`local` 프로필은 다음 성격을 가집니다.

- Compose를 자동으로 시작한다
- Thymeleaf cache를 끈다
- JPA schema를 빠르게 갱신한다
- demo/admin bootstrap을 환경변수 기본값으로 쉽게 켤 수 있다

### 5-4. test Spring Boot state

`test` 프로필은 다음 성격을 가집니다.

- Compose를 쓰지 않는다
- H2로 빠르게 뜬다
- session auto config를 제외해 Redis session에 덜 묶인다
- ranking key prefix를 `test:leaderboard`로 분리한다

## 6. 설계 구상

### 왜 개발 환경을 Docker Compose로 고정했는가

WorldMap은 데이터와 캐시를 동시에 쓰는 서버입니다.  
로컬 환경이 사람마다 다르면 아래 문제가 바로 생깁니다.

- 어떤 사람은 PostgreSQL, 어떤 사람은 H2만 씀
- Redis 유무에 따라 랭킹/세션 재현성이 달라짐
- "내 환경에서는 된다"는 설명 불가 상태가 생김

그래서 로컬에서만큼은 "정답 runtime"을 Compose로 고정하는 편이 낫습니다.

### 왜 local과 test를 일부러 다르게 뒀는가

개발자가 브라우저로 직접 눌러 보는 환경과, CI에서 빠르게 테스트하는 환경은 목적이 다릅니다.

- `local`: 사람의 체감과 수동 확인
- `test`: 자동화, 속도, 격리

만약 둘을 완전히 같게 두면 테스트가 느려지고, 반대로 둘을 완전히 분리하되 규약 없이 운영하면 설명이 약해집니다.  
WorldMap은 **local은 Compose, test는 H2 + 최소 Redis 가정**으로 역할을 나눴습니다.

### 왜 Redis를 local baseline에 일찍 넣었는가

Redis를 later stage까지 미루면 다음 문제가 생깁니다.

- 랭킹/세션 설계를 처음부터 설명하기 어려움
- local에서 public `/ranking`이나 session ownership을 끝까지 검증하기 어려움
- ECS 배포 단계에서 갑자기 인프라 설명이 튀어 보임

즉, Redis는 기능이 아니라 **플랫폼 성격**의 일부라서 early baseline에 같이 둡니다.

### 왜 test에서는 Session auto config를 끄는가

현재 저장소는 운영에서 Redis-backed session을 쓰지만, 모든 테스트가 그 비용을 그대로 가져갈 필요는 없습니다.

그래서 local/test는 공통으로 아래를 제외합니다.

- `org.springframework.boot.autoconfigure.session.SessionAutoConfiguration`

이렇게 해야 테스트는 더 빠르고 단순하게 돌고, prod 전용 session wiring은 별도 글에서 분리해 설명할 수 있습니다.

## 7. 코드 설명

### 7-1. `compose.yaml`: 로컬 저장소와 캐시를 한 파일로 묶는다

[compose.yaml](../compose.yaml)은 서비스 두 개만 띄웁니다.

#### PostgreSQL

- image: `postgres:16-alpine`
- port: `5432:5432`
- db/user/password: 모두 `worldmap`
- healthcheck: `pg_isready -U worldmap -d worldmap`

#### Redis

- image: `redis:7-alpine`
- port: `6379:6379`
- command: `redis-server --appendonly yes`
- healthcheck: `redis-cli ping`

이 파일에서 중요한 것은 "서비스 개수"가 아니라 **WorldMap이 현재 어떤 외부 런타임에 기대는지 명확히 드러난다**는 점입니다.

### 7-2. `application-local.yml`: 사람이 직접 만지는 개발 환경

[application-local.yml](../src/main/resources/application-local.yml)의 핵심은 아래입니다.

- `spring.docker.compose.lifecycle-management: start_only`
- `spring.thymeleaf.cache: false`
- `spring.jpa.hibernate.ddl-auto: update`
- `spring.sql.init.mode: never`
- `worldmap.legacy.rollback.enabled: true`
- admin/demo bootstrap 기본값 활성화

즉, local은 다음 철학을 가집니다.

- 서버를 빨리 띄운다
- HTML/CSS/SSR 수정이 바로 보이게 한다
- schema 변경을 빠르게 반영한다
- 데모 계정/관리자 계정을 쉽게 만든다

### 7-3. `application-test.yml`: 자동화 테스트 전용 경량 환경

[application-test.yml](../src/main/resources/application-test.yml)은 local과 intentionally 다릅니다.

- `docker.compose.enabled: false`
- datasource: H2 in-memory
- `ddl-auto: create-drop`
- `spring.data.redis.host: localhost`
- `spring.data.redis.port: 6379`
- `worldmap.admin.bootstrap.enabled: false`
- `worldmap.demo.bootstrap.enabled: false`
- `worldmap.ranking.key-prefix: test:leaderboard`

핵심은 다음입니다.

- 테스트는 Compose에 의존하지 않는다
- DB는 매 실행마다 깨끗하게 뜬다
- ranking key prefix는 테스트와 로컬을 섞지 않는다

### 7-4. local/test가 공유하는 것과 다른 것

둘 다 공유하는 것:

- Spring Boot 애플리케이션 구조
- country seed 로딩
- 게임/랭킹/추천 컨트롤러와 서비스
- session auto configuration exclusion

둘이 다른 것:

- DB 종류
- Compose 사용 여부
- bootstrap 기본값
- schema lifecycle
- Redis 사용 강도

이 구분이 문서에 없으면 독자는 왜 어떤 테스트는 Redis 없이 되고, 어떤 검증은 local에서만 하는지 이해하기 어렵습니다.

## 8. 요청 흐름 / 상태 변화

이 글의 흐름은 HTTP보다 **startup flow**가 중요합니다.

### 8-1. local 부팅 흐름

```text
docker compose up -d
-> postgres / redis container 기동
-> healthcheck 통과

./gradlew bootRun --args='--spring.profiles.active=local'
-> local profile 로드
-> datasource / redis endpoint 연결
-> country seed / bootstrap runner 실행
-> GET / 로 홈 화면 확인
```

### 8-2. test 실행 흐름

```text
./gradlew test
-> test profile 로드
-> H2 datasource 기동
-> compose 사용 안 함
-> 각 통합 테스트가 독립적으로 context 기동
```

### 8-3. 상태 변화 관점

- Compose 단계에서 바뀌는 상태: PostgreSQL volume, Redis AOF 데이터
- local 단계에서 바뀌는 상태: 실제 app data와 demo/admin bootstrap
- test 단계에서 바뀌는 상태: H2 메모리 DB, 테스트용 Redis key prefix

즉, local과 test는 둘 다 "실행"이지만 state persistence 성격이 다릅니다.

## 9. 실패 케이스 / 예외 처리

- local에서 Compose 없이 `bootRun`하면: datasource/redis 연결 실패로 바로 부팅이 흔들릴 수 있다
- PostgreSQL만 있고 Redis가 없으면: later stage의 ranking/session 재현성이 약해진다
- test가 Compose에 의존하면: CI가 느려지고 깨지기 쉽다
- local/test가 같은 Redis key prefix를 쓰면: 테스트 쓰레기 key가 로컬 확인을 오염시킬 수 있다
- `ddl-auto=update`를 prod에 그대로 가져가면: 운영 schema 관리가 위험해진다

특히 마지막은 중요합니다.  
`update`는 local 생산성을 위한 선택이지, 운영 전략이 아닙니다.

## 10. 테스트로 검증하기

이 단계는 코드 단위 테스트보다 **실행 환경 검증** 비중이 큽니다.

현재 저장소 기준으로 가장 현실적인 검증은 아래 순서입니다.

```bash
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

그 다음 브라우저나 `curl`로 아래를 확인합니다.

```bash
curl -I http://localhost:8080
```

자동화 테스트 관점에서는 다음 포인트가 이어집니다.

- [HomeControllerTest.java](../src/test/java/com/worldmap/web/HomeControllerTest.java): test profile에서 app shell이 뜨는지
- 다음 글의 [ApplicationConfigTest.java](../src/test/java/com/worldmap/common/config/ApplicationConfigTest.java): base profile이 local을 강제하지 않는지
- 다음 글의 [ProdProfileConfigTest.java](../src/test/java/com/worldmap/common/config/ProdProfileConfigTest.java): prod가 local-friendly 옵션을 끌고 가지 않는지

즉, 이 단계 자체는 환경 smoke가 중심이고, profile contract의 세밀한 자동 검증은 `04`에서 닫힙니다.
이 글에서 자동으로 증명되는 범위는 아직 작습니다.
Compose 기반 local runtime의 체감은 `docker compose up`과 `bootRun`으로 사람이 직접 확인해야 하고,
JUnit이 직접 고정하는 profile contract는 다음 글의 설정 테스트로 넘어갑니다.

## 11. 회고

개발 환경 글이 약하면 프로젝트가 "코드는 있는데 실행은 사람마다 다름" 상태가 되기 쉽습니다.  
WorldMap은 baseline 다음 글에서 곧바로 Compose runtime을 고정해, 이후 모든 데모와 수동 QA의 토대를 만들었습니다.

이 설계의 장점은 다음입니다.

- local 실행 경험이 재현 가능하다
- 데이터와 캐시를 함께 설명할 수 있다
- 운영 환경(ECS/RDS/Redis) 설명으로 자연스럽게 이어진다

### 현재 구현의 한계

- local profile은 여전히 개발 생산성을 위해 `ddl-auto=update`를 쓴다
- Compose 자체를 JUnit으로 직접 검증하지는 않는다
- Redis session은 local/test에서 일부러 단순화하고, prod에서만 정식으로 활성화한다
- local에서 Compose가 "자동으로 뜬다"는 경험도 Docker Desktop/CLI와 Spring Boot compose 지원이 실제로 가능한 머신이라는 전제를 가진다

## 12. 취업 포인트

### 12-1. 1문장 답변

WorldMap은 로컬 실행 경험을 사람마다 다르게 두지 않기 위해 Docker Compose로 PostgreSQL과 Redis를 먼저 고정하고, `local`과 `test` profile을 목적에 맞게 분리했습니다.

### 12-2. 30초 답변

Spring Boot baseline 다음에는 개발 환경의 일관성이 중요해서 `compose.yaml`로 PostgreSQL 16과 Redis 7을 고정했습니다. `local` 프로필은 Compose를 자동으로 띄우고 Thymeleaf cache off, `ddl-auto=update`, demo/admin bootstrap으로 브라우저 수동 검증을 쉽게 만들었고, `test` 프로필은 H2와 Compose off로 자동 테스트를 가볍게 유지했습니다. 즉, 같은 프로젝트라도 local은 사람 중심, test는 자동화 중심으로 역할을 분리한 것이 핵심입니다.

### 12-3. 예상 꼬리 질문

- 왜 local과 test를 똑같이 두지 않았나요?
- 왜 Redis를 이렇게 일찍 개발 환경에 넣었나요?
- 왜 test에서 H2를 쓰면서도 Redis 설정 일부를 남겼나요?
- `ddl-auto=update`는 왜 local에서만 허용하나요?

## 13. 시작 상태

- Spring Boot baseline은 뜨지만, 어떤 DB/Redis 위에서 실행해야 하는지 팀 공용 계약이 없다
- 로컬 개발자 환경에 따라 PostgreSQL/Redis 유무가 달라질 수 있다
- 테스트와 수동 실행 환경의 역할 분리가 명확하지 않다

## 14. 이번 글에서 바뀌는 파일

- `compose.yaml`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-test.yml`
- 관련 README 실행 가이드

## 15. 구현 체크리스트

1. Compose에 PostgreSQL과 Redis 서비스를 정의한다
2. 둘 다 healthcheck와 named volume을 추가한다
3. `local` profile에 Compose lifecycle, cache off, `ddl-auto=update`를 설정한다
4. `test` profile에는 H2 datasource와 Compose off를 설정한다
5. bootstrap 기본값을 local에서는 열고 test에서는 닫는다
6. Redis key prefix를 test 전용으로 분리한다
7. local 부팅 smoke를 실제로 확인한다

## 16. 실행 / 검증 명령

```bash
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
curl -I http://localhost:8080
```

자동화 테스트 일부를 곁들여 확인하려면:

```bash
./gradlew test --tests com.worldmap.web.HomeControllerTest
```

## 17. 산출물 체크리스트

- PostgreSQL과 Redis를 한 명령으로 띄울 수 있다
- `local` profile에서 브라우저로 직접 앱을 확인할 수 있다
- `test` profile은 Compose 없이도 통합 테스트를 돌릴 수 있다
- local 데이터와 test 데이터의 성격이 분리된다

## 18. 글 종료 체크포인트

- 왜 개발 환경을 Compose로 먼저 고정해야 하는가
- 왜 local은 사람 중심, test는 자동화 중심으로 다르게 설계해야 하는가
- 왜 Redis를 early runtime dependency로 취급해야 하는가
- 왜 `ddl-auto=update`는 local에서만 허용해야 하는가

## 19. 자주 막히는 지점

- "테스트도 똑같이 Compose 붙이면 더 진짜 같다"는 이유로 자동화까지 무겁게 만드는 것
- Redis를 나중 문제로 미뤄 local에서 아예 띄우지 않는 것
- local/test/prod의 목적 차이를 무시하고 같은 설정을 복붙하는 것
- healthcheck 없이 컨테이너만 띄워 놓고 기동 불안정을 환경 탓으로 넘기는 것
