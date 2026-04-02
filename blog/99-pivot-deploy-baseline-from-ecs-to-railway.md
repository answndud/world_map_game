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

## 면접에서 30초로 설명하면

배포 방향을 Railway 단일 플랫폼 기준으로 다시 정리했습니다. 핵심은 루트 `Dockerfile`을 배포 source of truth에서 내리고 `Dockerfile.local`로 분리한 뒤, `railway.toml`에서 Railpack 빌드와 start command, readiness health check를 고정한 점입니다. 그리고 `bootJar` 산출물을 `worldmap.jar`로 통일하고, prod Redis 설정이 URL 방식도 받을 수 있게 해서 Railway Postgres/Redis reference variable로 바로 연결할 수 있게 만들었습니다.
