# local 프로필에서 admin / user 계정과 샘플 run을 자동으로 만드는 이유

## 왜 이 글을 쓰는가

시연 가능한 포트폴리오는 "기능이 있다"로 끝나면 부족하다.

바로 확인할 수 있어야 한다.

- admin은 `/dashboard`에 들어갈 수 있어야 한다.
- 일반 사용자는 로그인 후 `/mypage`를 볼 수 있어야 한다.
- `/stats`, `/ranking`에는 샘플 기록이 떠 있어야 한다.

그런데 DB를 비우면 이 상태가 바로 사라진다.

이번 조각은 이 문제를 local demo bootstrap으로 해결한 작업이다.

## 이번에 만든 것

local profile로 서버를 시작하면 아래가 자동으로 만들어진다.

- `worldmap_admin` / `secret123`
- `orbit_runner` / `secret123`
- `orbit_runner` 위치 찾기 완료 run 1개
- `orbit_runner` 인구수 퀴즈 완료 run 1개
- guest 진행 중 위치 게임 세션 1개
- current `survey-v4 / engine-v10` 추천 만족도 샘플 5개

즉, 앱을 켜자마자 admin, user, public 화면을 모두 바로 시연할 수 있는 상태가 된다.

## 어떤 파일이 바뀌었는가

- `src/main/java/com/worldmap/demo/application/DemoBootstrapProperties.java`
- `src/main/java/com/worldmap/demo/application/DemoBootstrapService.java`
- `src/main/java/com/worldmap/demo/application/DemoBootstrapInitializer.java`
- `src/main/java/com/worldmap/auth/domain/Member.java`
- `src/main/java/com/worldmap/country/application/CountrySeedInitializer.java`
- `src/main/java/com/worldmap/admin/application/AdminBootstrapInitializer.java`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-test.yml`
- `src/test/java/com/worldmap/demo/DemoBootstrapIntegrationTest.java`
- `docs/LOCAL_DEMO_BOOTSTRAP.md`

## 왜 signup이나 SQL seed가 아니라 bootstrap service인가

이 데이터는 "회원가입 예시"가 아니라 "시연 가능한 시작 상태"다.

그래서 공개 플로우와 분리하는 게 맞다.

- admin 계정은 signup으로 만들지 않는다.
- sample run은 raw SQL보다 실제 도메인 객체를 써서 만드는 편이 설명하기 쉽다.
- local 환경에서만 켜고 싶다.

이 조건을 동시에 만족하려면 startup runner + service가 가장 단순했다.

## 실행 흐름

```text
앱 시작(local)
-> CountrySeedInitializer
-> AdminBootstrapInitializer
-> DemoBootstrapInitializer
-> DemoBootstrapService.ensureLocalDemoData()
```

## 왜 실행 순서를 고정했는가

demo sample run을 만들려면 국가 데이터가 먼저 있어야 한다.

예를 들어 위치 게임 sample run은 `KOR`, `JPN`, `BRA`, `CAN` 같은 국가 엔티티를 실제로 찾아서 stage를 만든다.

그래서 순서를 이렇게 고정했다.

- 10: country seed
- 20: admin bootstrap
- 30: demo bootstrap

이 순서가 깨지면 demo bootstrap이 sample run 생성을 건너뛰거나 실패할 수 있다.

## `DemoBootstrapService`가 실제로 하는 일

### 1. demo user 보장

`orbit_runner` 계정이 없으면 만들고, 있으면 비밀번호를 local 기본값으로 다시 맞춘다.

### 2. sample leaderboard run 보장

`runSignature`를 기준으로 중복 생성은 피한다.

- `demo:location:orbit_runner:1`
- `demo:population:orbit_runner:1`

### 3. 진행 중 guest 세션 보장

`demo-guest-live` guestSessionKey가 없을 때만 하나 만든다.

## 왜 `runSignature`를 두었는가

local 서버는 여러 번 재시작할 수 있다.

그때마다 sample run이 계속 쌓이면 시연 데이터가 망가진다.

그래서 sample run에는 사람이 읽을 수 있는 고정 signature를 부여하고, 이미 있으면 다시 만들지 않게 했다.

## 테스트는 무엇을 했는가

- `DemoBootstrapIntegrationTest`
  - local + test 프로필로 컨텍스트를 올렸을 때
  - `worldmap_admin`이 `ADMIN`인지
  - `orbit_runner`가 `USER`인지
  - 두 계정 비밀번호 hash가 실제로 저장됐는지
  - demo leaderboard run 2개가 생성됐는지
  - `demo-guest-live` 진행 중 세션이 있는지
  - sample population 완료 세션이 member ownership으로 남는지
  - current `survey-v4 / engine-v10` 추천 피드백이 최소 5개 보장되는지

## DB가 날아가면 어떻게 하나

문서로도 남겼지만 원리는 단순하다.

1. DB / Redis를 비운다.
2. local profile로 서버를 다시 시작한다.
3. seed -> admin bootstrap -> demo bootstrap이 다시 돈다.

즉, 시연 상태를 수동으로 다시 만들 필요가 없다.

## 회고

이 작업은 개발 편의 기능 같지만, 포트폴리오에서는 꽤 중요하다.

면접이나 시연 때 아래를 바로 보여줄 수 있기 때문이다.

- guest 플레이
- 회원 로그인 후 `/mypage`
- admin의 `/dashboard`
- public `/stats`

그리고 이 상태가 우연이 아니라 재현 가능하다는 점까지 설명할 수 있다.

## 면접에서는 이렇게 설명할 수 있다

local 시연 상태를 매번 손으로 만들지 않기 위해 startup runner 기반 demo bootstrap을 붙였습니다. local profile로 앱이 뜨면 country seed 이후에 admin 계정, 일반 user 계정, 샘플 leaderboard run, guest 진행 중 세션을 자동으로 생성합니다. 공개 signup이나 SQL script 대신 service로 만든 이유는, 실제 도메인 객체와 ownership 구조를 그대로 따라가면서도 DB를 비워도 같은 상태를 재현할 수 있게 하기 위해서입니다.
