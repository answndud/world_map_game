# Railway 단일 플랫폼 배포 기준으로 Dockerfile과 prod 런타임을 다시 정리하기

## 왜 이 조각이 필요했나

기존 배포 준비는 `AWS ECS + RDS + Redis` 기준으로 정리돼 있었습니다.

그 방향 자체는 백엔드 포트폴리오 설명력에서 좋습니다.
하지만 지금 요구사항은 더 단순했습니다.

- 당장 AWS를 쓰지 않는다
- 하나의 플랫폼에서 끝내고 싶다
- 도메인이 없어도 먼저 공개 URL이 필요하다
- 루트 `Dockerfile`은 배포보다 로컬 확인용으로만 쓰고 싶다

이 조건에서는 ECS보다 Railway가 더 맞습니다.

그래서 이번 조각의 목적은 `현재 기본 배포 경로를 Railway 단일 플랫폼 기준으로 다시 고정하는 것`이었습니다.

## 이번에 바뀐 파일

- [railway.toml](../railway.toml)
- [build.gradle](../build.gradle)
- [Dockerfile.local](../Dockerfile.local)
- [application-prod.yml](../src/main/resources/application-prod.yml)
- [docs/DEPLOYMENT_RUNBOOK_RAILWAY.md](../docs/DEPLOYMENT_RUNBOOK_RAILWAY.md)
- [RailwayConfigTemplateTest.java](../src/test/java/com/worldmap/common/config/RailwayConfigTemplateTest.java)

## 핵심 결정 1. 루트 Dockerfile을 배포 기준에서 내렸다

Railway는 저장소 루트에 `Dockerfile`이 있으면 그걸 자동 사용합니다.

하지만 지금 원하는 건 Docker 기반 배포가 아니라 Railway의 기본 빌드 흐름입니다.
즉 `Railpack`이 저장소를 읽고 Java 앱으로 배포하는 방식이 더 맞습니다.

그래서 이번에는 루트 `Dockerfile`을 [Dockerfile.local](../Dockerfile.local)로 이름을 바꿨습니다.

의미는 명확합니다.

- `Dockerfile.local`: 로컬 이미지 검증용
- `railway.toml`: 실제 배포 source of truth

## 핵심 결정 2. `bootJar` 산출물을 `worldmap.jar`로 고정했다

Railway start command를 버전 문자열에 묶어 두면 배포 명령이 불안정해집니다.

그래서 이번에는 [build.gradle](../build.gradle)에서 `bootJar` 결과물을 `worldmap.jar`로 고정했습니다.

이 덕분에 Railway start command는 이렇게 단순해집니다.

```toml
startCommand = "java -Dserver.port=$PORT -jar build/libs/worldmap.jar"
```

## 핵심 결정 3. Railway Redis URL과 host 기반 연결을 둘 다 설명 가능하게 정리했다

기존 prod 설정은 Redis를 `host + port` 기준으로만 읽었습니다.

그런데 Railway는 Redis reference variable을 `REDIS_URL`로 바로 주는 흐름이 자연스럽습니다.

그래서 [application-prod.yml](../src/main/resources/application-prod.yml)과 prod runtime 계약을 아래 두 갈래로 설명할 수 있게 정리했습니다.

- Railway가 주는 `SPRING_DATA_REDIS_URL` direct binding
- host 기반 `spring.data.redis.host / port / username / password`

즉 지금은 두 가지 다 가능합니다.

1. URL 하나로 연결
2. host/port/password 개별 값으로 연결

중요한 점은 URL 지원을 빈 문자열 placeholder로 강제하지 않고, Spring Boot가 환경변수를 직접 바인딩하게 두었다는 점입니다.

## `railway.toml`은 어떤 역할을 하나

[railway.toml](../railway.toml)은 지금 배포 source of truth입니다.

핵심 값은 아래입니다.

```toml
[build]
builder = "RAILPACK"
buildCommand = "./gradlew --no-daemon bootJar"

[deploy]
startCommand = "java -Dserver.port=$PORT -jar build/libs/worldmap.jar"
healthcheckPath = "/actuator/health/readiness"
restartPolicyType = "ON_FAILURE"
numReplicas = 1
```

이 의미는 다음과 같습니다.

- builder는 Docker가 아니라 Railpack
- build는 Gradle `bootJar`
- start는 Railway가 주는 `PORT`로 Spring Boot 실행
- health check는 기존 actuator readiness 재사용
- replica는 일단 1개

## 테스트는 무엇으로 닫았나

### 1. `RailwayConfigTemplateTest`

- `builder = "RAILPACK"`
- `buildCommand = "./gradlew --no-daemon bootJar"`
- `startCommand = "java -Dserver.port=$PORT -jar build/libs/worldmap.jar"`
- `healthcheckPath = "/actuator/health/readiness"`
- `numReplicas = 1`

### 2. `ProdProfileConfigTest`

여기서는 Redis URL / username / password까지 prod 설정이 실제로 받을 수 있는지 고정했습니다.

## 지금 남은 것

이제 남은 것은 실제 Railway 플랫폼 작업입니다.

1. Railway 프로젝트 생성
2. GitHub 저장소 연결
3. PostgreSQL 추가
4. Redis 추가
5. 변수 입력
6. Railway 기본 도메인 생성
7. 첫 배포 smoke test

## 무료 공개를 원할 때 왜 full app을 더 깎지 말아야 하나

여기서 자주 생기는 다음 질문이 있습니다.

> Railway나 다른 단일 플랫폼의 free-tier로 공개하려면,
> 지금 Spring Boot 앱에서 기능만 조금 빼면 되지 않을까?

현재 WorldMap 기준으로는 이 접근이 생각보다 위험합니다.

이유는 기능 수보다 **저장형 약속**이 더 문제이기 때문입니다.

지금 full app은 아래를 동시에 갖고 있습니다.

- auth와 member/guest ownership
- `/mypage`
- `/stats`
- `/ranking`
- `/dashboard`
- recommendation feedback persistence
- Redis-backed session과 leaderboard read model

즉 단순히 화면 몇 개를 감추는 것만으로는 free-tier 공개용 app이 되지 않습니다.

오히려 같은 Spring Boot 앱 안에 `demo-lite` profile을 억지로 넣기 시작하면

- 공통 헤더의 auth state
- game service의 leaderboard write
- recommendation 결과의 feedback token/session
- prod profile의 DB/Redis/readiness 전제

가 한 번에 엮이기 때문에, `main` 안정성까지 흔들릴 가능성이 큽니다.

그래서 현재 기준으로 더 안전한 판단은 아래입니다.

1. full app은 그대로 둔다
2. 무료 공개가 꼭 필요하면 별도 `demo-lite` surface를 정의한다
3. retained surface는 `홈 + 수도 + 국기 + 인구 비교 + 추천`처럼 저장형 기능이 없는 흐름만 먼저 남긴다
4. `/stats`, `/ranking`, `/mypage`, `/dashboard`, auth, recommendation feedback 저장은 demo-lite에서 과감히 뺀다

즉 free-tier 문제는 `운영 기능을 공짜에 맞게 축소하는 문제`가 아니라,
**별도 public demo product를 어디까지로 볼 것인지 다시 고정하는 문제**에 가깝습니다.

## planning만으로 끝내지 않고 `demo-lite/` 앱 골격을 먼저 연 이유

문서만 보면 sibling `demo-lite` app 전략이 좋아 보일 수 있습니다.

하지만 실제로 별도 앱 entrypoint가 없으면 아래 판단이 계속 추상적으로 남습니다.

- 같은 저장소에서 정말 별도 앱이 충돌 없이 공존할 수 있는가
- 정적 데이터만 재사용하는 방식이 실제 build에서 성립하는가
- free-tier static hosting에 맞는 route map을 어떻게 잡을 것인가

그래서 planning 다음 첫 조각에서는 [demo-lite](../demo-lite) 디렉터리를 실제로 열었습니다.

이번 첫 조각의 역할은 제한적입니다.

- `Vite + vanilla SPA + hash route` shell
- 전용 header/navigation
- `#/`, `#/games/capital`, `#/games/flag`, `#/games/population-battle`, `#/recommendation` route map
- [countries.json](../src/main/resources/data/countries.json), [flag-assets.json](../src/main/resources/data/flag-assets.json), [static/images/flags](../src/main/resources/static/images/flags) sync script 기반 재사용

즉 아직 game loop를 옮긴 것이 아니라, **별도 앱 전략이 기술적으로 정말 가벼운가**를 먼저 확인한 것입니다.

### 왜 Vite + hash route인가

이번 slice에서 중요한 것은 React냐 아니냐가 아닙니다.

중요한 것은 아래 세 가지입니다.

1. 메인 Spring Boot 앱을 건드리지 않을 것
2. free-tier static hosting에서 rewrite 의존성을 줄일 것
3. 첫 retained route shell을 가장 작은 비용으로 열 것

그래서 이번에는 hash route를 택했습니다.

- static host rewrite가 없어도 동작하기 쉽다
- route를 바로 늘리기 쉽다
- 게임 loop를 붙이기 전에도 navigation contract를 먼저 검증할 수 있다

즉 이 첫 조각은 UI 프레임워크 취향보다 **배포 제약에 맞는 최소 구조**를 고르는 단계에 가깝습니다.

### 왜 shared data만 재사용하는가

이번 skeleton에서 메인 앱과 직접 공유하는 것은 아래뿐입니다.

- [countries.json](../src/main/resources/data/countries.json)
- [flag-assets.json](../src/main/resources/data/flag-assets.json)
- [static/images/flags](../src/main/resources/static/images/flags)

다만 브라우저가 Spring Boot runtime 밖의 파일을 직접 참조하게 두지는 않습니다.

이번 첫 조각에서는 [demo-lite/scripts/sync-shared-assets.mjs](../demo-lite/scripts/sync-shared-assets.mjs)가 메인 저장소 자산을 `demo-lite/public/generated/`로 복사하고, 브라우저는 [demo-lite/src/lib/shared-data.js](../demo-lite/src/lib/shared-data.js)에서 generated JSON을 fetch합니다.

반대로 재사용하지 않는 것은 아래입니다.

- Spring Boot controller/service
- JPA entity/repository
- auth/session
- leaderboard/stats/dashboard

이 판단이 중요한 이유는,
`demo-lite`의 목적이 full app을 브라우저-only로 복제하는 것이 아니라
**무료 공개에서도 설명 가능한 최소 surface만 새로 여는 것**이기 때문입니다.

### 이번 조각에서 실제로 닫힌 것

이제 저장소 안에는 아래 두 세계가 같이 존재합니다.

1. full app
   - Spring Boot + PostgreSQL + Redis + auth + ranking/stats/dashboard
2. demo-lite
   - 별도 앱 디렉터리
   - static-host friendly shell
   - retained route 4개 + 홈
   - shared JSON 재사용

즉 `demo-lite`는 더 이상 문서 속 아이디어가 아니라,
실제로 다음 게임 loop를 하나씩 옮겨 붙일 수 있는 별도 공개 트랙이 되었습니다.

## 면접에서 30초로 설명하면

배포 방향을 Railway 단일 플랫폼 기준으로 다시 정리했습니다. 핵심은 루트 `Dockerfile`을 배포 source of truth에서 내리고 `Dockerfile.local`로 분리한 뒤, `railway.toml`에서 Railpack 빌드와 start command, readiness health check를 고정한 점입니다. 그리고 `bootJar` 산출물을 `worldmap.jar`로 통일하고, prod Redis 설정이 URL 방식도 받을 수 있게 해서 Railway Postgres/Redis reference variable로 바로 연결할 수 있게 만들었습니다.
