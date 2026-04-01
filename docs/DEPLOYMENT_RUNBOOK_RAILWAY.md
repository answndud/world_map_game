# WorldMap Railway 배포 런북

최종 업데이트: 2026-04-01

## 1. 목적

이 문서는 `한 플랫폼에서 끝나는 Spring Boot 배포`를 원하는 현재 프로젝트 기준으로, 초보자도 Railway에서 처음부터 끝까지 따라갈 수 있게 정리한 실행 문서다.

목표는 아래 4개다.

- Railway에 GitHub 저장소 연결
- PostgreSQL / Redis / 앱을 같은 프로젝트 안에서 연결
- `railway.toml` 기준으로 Railpack 배포
- 배포 후 로그인, 게임, 랭킹, dashboard까지 smoke test

## 2. 왜 Railway인가

현재 조건은 아래와 같다.

- AWS를 지금 바로 쓰지 않는다
- 하나의 플랫폼에서 배포하고 싶다
- 도메인이 없어도 먼저 공개 URL이 필요하다
- 루트 `Dockerfile`은 배포보다 로컬 확인용으로만 쓰고 싶다

이 조건에서는 Railway가 현재 구조와 가장 잘 맞는다.

- GitHub 연결만으로 바로 배포 가능
- Railway 기본 도메인 제공
- 같은 프로젝트 안에 PostgreSQL / Redis / 웹 앱을 같이 둘 수 있음
- `railway.toml`로 build/start/healthcheck를 저장소에 고정 가능

## 3. 현재 저장소 기준 source of truth

지금 Railway 배포 기준으로 봐야 하는 파일은 아래다.

- [railway.toml](/Users/alex/project/worldmap/railway.toml)
- [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)
- [build.gradle](/Users/alex/project/worldmap/build.gradle)
- [Dockerfile.local](/Users/alex/project/worldmap/Dockerfile.local)

원칙:

- Railway는 `railway.toml` + Railpack으로 배포한다.
- [Dockerfile.local](/Users/alex/project/worldmap/Dockerfile.local)은 로컬 이미지 검증용이다.
- `bootJar` 결과 파일 이름은 `worldmap.jar`로 고정돼 있다.

## 4. Railway가 앱을 띄우는 방식

현재 [railway.toml](/Users/alex/project/worldmap/railway.toml) 기준 흐름은 아래다.

1. builder: `RAILPACK`
2. build command: `./gradlew --no-daemon bootJar`
3. start command: `java -Dserver.port=$PORT -jar build/libs/worldmap.jar`
4. health check: `/actuator/health/readiness`

즉 운영 런타임 흐름은 아래처럼 설명하면 된다.

`Railway build -> bootJar 생성 -> Railway start command -> Spring Boot prod profile -> readiness probe`

## 5. 실제 배포 순서

### 5.1 GitHub 연결

1. Railway 로그인
2. `New Project`
3. `Deploy from GitHub repo`
4. `answndud/world_map_game` 저장소 선택

### 5.2 PostgreSQL 추가

1. 같은 Railway 프로젝트에서 `New`
2. `Database`
3. `PostgreSQL` 선택

### 5.3 Redis 추가

1. 같은 Railway 프로젝트에서 `New`
2. `Database`
3. `Redis` 선택

### 5.4 앱 서비스 만들기

GitHub 저장소를 연결하면 앱 서비스가 생성된다.

중요:

- Railway가 루트 `Dockerfile`을 자동 사용하지 않도록, 현재 저장소는 [Dockerfile.local](/Users/alex/project/worldmap/Dockerfile.local)로 이름을 바꿔 두었다.
- 따라서 Railway는 `railway.toml`의 `builder = "RAILPACK"` 기준으로 배포한다.

## 6. Railway 환경변수 설정

앱 서비스에 아래 변수를 넣는다.

### 6.1 PostgreSQL 연결

- `SPRING_DATASOURCE_URL`
  - `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}`
- `SPRING_DATASOURCE_USERNAME`
  - `${{Postgres.PGUSER}}`
- `SPRING_DATASOURCE_PASSWORD`
  - `${{Postgres.PGPASSWORD}}`

### 6.2 Redis 연결

가장 단순한 방법은 URL 하나를 쓰는 것이다.

- `SPRING_DATA_REDIS_URL`
  - `${{Redis.REDIS_URL}}`

필요하면 개별 값으로도 넣을 수 있다.

- `SPRING_DATA_REDIS_HOST`
- `SPRING_DATA_REDIS_PORT`
- `SPRING_DATA_REDIS_USERNAME`
- `SPRING_DATA_REDIS_PASSWORD`

현재 [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)은 host 기반 `host/port/username/password` 계약을 명시하고 있고, `SPRING_DATA_REDIS_URL`은 Spring Boot 환경변수 바인딩으로 직접 받을 수 있다.

### 6.3 공통 prod 변수

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATA_REDIS_SSL_ENABLED=false`
- `WORLDMAP_DEMO_BOOTSTRAP_ENABLED=false`
- `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED=true`
- `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME=worldmap_admin`
- `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD=<직접 생성한 비밀번호>`
- `WORLDMAP_RANKING_KEY_PREFIX=leaderboard`

## 7. Networking / Domain

Railway는 기본 공개 도메인을 생성할 수 있다.

순서:

1. 앱 서비스 선택
2. `Settings`
3. `Networking`
4. `Generate Domain`

이 주소가 첫 공개 URL이다.

도메인이 없어도 된다.

## 8. Health check 기준

Railway는 [railway.toml](/Users/alex/project/worldmap/railway.toml)의 아래 값을 본다.

- `healthcheckPath = "/actuator/health/readiness"`

즉 현재 운영 건강도 판단 기준은:

- 앱 프로세스가 떠 있는가
- DB 연결이 되는가
- Redis 연결이 되는가
- readiness state가 `UP`인가

## 9. 첫 배포 후 smoke test

최소 확인 순서:

1. `/`
2. `/stats`
3. `/ranking`
4. `/login`
5. 새 계정 회원가입
6. 게임 한 판 완료
7. 랭킹 반영
8. `worldmap_admin` 로그인
9. `/dashboard`
10. 추천 설문 제출

## 10. 지금 당장 해야 할 일 체크리스트

- [x] Railway용 [railway.toml](/Users/alex/project/worldmap/railway.toml) 추가
- [x] 로컬 전용 [Dockerfile.local](/Users/alex/project/worldmap/Dockerfile.local)로 분리
- [x] prod Redis 설정이 `URL/host/password`를 모두 받을 수 있게 보강
- [ ] Railway 프로젝트 생성
- [ ] GitHub 저장소 연결
- [ ] PostgreSQL 추가
- [ ] Redis 추가
- [ ] 앱 서비스 환경변수 입력
- [ ] Railway 기본 도메인 생성
- [ ] 첫 배포 성공
- [ ] smoke test 기록 남기기

## 11. 한 줄 결론

현재 WorldMap은 `Railway 단일 플랫폼 배포` 기준으로 `railway.toml + application-prod.yml + worldmap.jar` 조합까지 준비된 상태다. 다음 단계는 AWS 인프라가 아니라, Railway 프로젝트 생성과 환경변수 입력, 첫 공개 URL 확인이다.
