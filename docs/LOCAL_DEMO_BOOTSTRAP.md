# Local Demo Bootstrap

## 목적

local 개발 환경에서 앱을 띄우자마자 아래를 바로 확인할 수 있게 만든다.

- `ADMIN` 계정으로 `/dashboard` 접근
- 일반 `USER` 계정으로 `/mypage`와 로그인 흐름 확인
- `/stats`, `/ranking`에 보이는 샘플 기록 확인
- 진행 중 guest 세션과 완료된 member run을 동시에 가진 상태 재현
- 현재 버전 추천 만족도 표본과 운영 메모 확인

이 문서는 "데이터가 날아가도 어떻게 다시 같은 상태를 만들 수 있는가"를 기록한다.

## 기본 동작

local profile에서 서버가 시작되면 아래 순서로 초기화가 실행된다.

1. `CountrySeedInitializer`
2. `AdminBootstrapInitializer`
3. `DemoBootstrapInitializer`

즉, 국가 시드가 먼저 준비된 뒤 admin 계정과 demo 데이터가 만들어진다.

## 기본 계정

별도 환경변수를 주지 않으면 아래 값이 기본으로 사용된다.

### ADMIN

- 닉네임: `worldmap_admin`
- 비밀번호: `secret123`

### USER

- 닉네임: `orbit_runner`
- 비밀번호: `secret123`

## 기본 샘플 데이터

demo bootstrap은 아래 확인용 데이터를 local DB에 넣는다.

- `orbit_runner` 위치 찾기 완료 run 1개
  - 총점 `420`
  - 클리어 Stage `3`
  - 총 시도 수 `6`
- `orbit_runner` 인구수 퀴즈 완료 run 1개
  - 총점 `390`
  - 클리어 Stage `3`
  - 총 시도 수 `6`
- guest 진행 중 위치 게임 세션 1개
  - `guestSessionKey = demo-guest-live`
  - `status = IN_PROGRESS`
- 현재 추천 버전 만족도 샘플 5개
  - 기준 버전: `survey-v4 / engine-v19`
  - 평균 만족도: 약 `4.4`
  - `/dashboard/recommendation/feedback`에서 `rank drift 줄이기` 메모가 바로 보이는 수준으로 seed

이 데이터로 아래 화면을 바로 점검할 수 있다.

- `/stats`
- `/ranking`
- `/mypage`
- `/dashboard`
- `/dashboard/recommendation/feedback`

## 재생성 방법

DB나 Redis 데이터를 비운 뒤에도 local profile로 앱을 다시 시작하면 같은 확인용 상태를 다시 만들 수 있다.

가장 단순한 절차는 아래다.

1. local DB / Redis 데이터를 비운다.
2. 서버를 local profile로 다시 시작한다.
3. country seed -> admin bootstrap -> demo bootstrap이 다시 실행된다.

예시:

```bash
docker compose down -v
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

이미 데이터가 있는 상태에서 다시 시작해도, demo bootstrap은 `run_signature`, `guestSessionKey`, `nickname`을 기준으로 중복 생성을 피한다.
추천 만족도 샘플은 현재 `surveyVersion + engineVersion` 조합의 응답 수가 5개 미만일 때만 부족한 개수만큼 보충한다.

## 환경변수 오버라이드

저장소 루트에는 기본값이 들어간 gitignored `.env.local`을 같이 둔다.

```bash
set -a
source .env.local
set +a
./gradlew bootRun
```

즉, 매번 긴 환경변수 명령을 직접 치지 않고 `.env.local`을 source한 뒤 local profile로 바로 시작할 수 있다.

### admin 계정

- `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED`
- `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME`
- `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD`

### demo user 계정

- `WORLDMAP_DEMO_BOOTSTRAP_ENABLED`
- `WORLDMAP_DEMO_MEMBER_NICKNAME`
- `WORLDMAP_DEMO_MEMBER_PASSWORD`

## 구현 책임 분리

- `AdminBootstrapService`
  - 운영용 admin 계정을 만든다.
- `DemoBootstrapService`
  - local 확인용 user 계정과 샘플 run / guest 세션을 만든다.
- `MemberCredentialPolicy`
  - signup과 bootstrap이 같은 닉네임 / 비밀번호 규칙을 공유하게 한다.

즉, 공개 회원가입 흐름으로 운영 계정이나 demo 계정을 만들지 않고, local 개발 환경의 재현 가능한 시작 상태를 startup runner가 책임진다.
