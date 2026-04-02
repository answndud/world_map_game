# Demo-Lite Decomposition Plan

## 목적

이 문서는 현재 풀 기능 `WorldMap` 코드를 깨뜨리지 않고, 무료 공개용 `demo-lite` 트랙을 어떻게 분리할지 **충돌 우선순위 기준**으로 설계하는 문서다.

핵심 질문은 두 가지다.

1. 현재 코드에서 어떤 부분이 DB/Redis 없는 demo-lite와 가장 강하게 충돌하는가
2. 무엇부터 분리해야 main을 건드리지 않고 free-demo를 만들 수 있는가

## 결론 먼저

가장 안전한 전략은 아래다.

1. **main app는 유지**
2. **`demo-lite`는 별도 앱/별도 entrypoint로 만든다**
3. main의 도메인 서비스와 저장소 계층을 억지로 feature flag로 끄지 않는다

즉 추천 전략은 `기존 Spring Boot 앱 안에 if문으로 반쪽 기능을 만들기`가 아니라,
**재사용 가능한 정적 데이터/자산만 가져가는 별도 demo-lite 트랙**이다.

## 왜 in-place feature flag가 위험한가

현재 코드의 핵심 package는 아래처럼 서로 엮여 있다.

- `game/*/application/*GameService`
  - JPA repository
  - `LeaderboardService`
  - ownership/session access context
- `auth/*`
  - `HttpSession`
  - `MemberRepository`
  - guest/member ownership
- `ranking/*`
  - `leaderboard_record`
  - Redis + DB fallback
- `stats/*`, `mypage/*`, `admin/*`
  - 대부분 `leaderboard_record`, member record, feedback record를 read model로 사용

즉 "랭킹만 끄고 게임은 그대로 쓰자"가 잘 안 된다.

현재 game service는 시작부터 아래를 전제한다.

- DB session 저장
- stage/attempt 저장
- terminal 시 leaderboard write
- memberId 또는 guestSessionKey ownership

예:

- [LocationGameService.java](../src/main/java/com/worldmap/game/location/application/LocationGameService.java)
- [CapitalGameService.java](../src/main/java/com/worldmap/game/capital/application/CapitalGameService.java)

둘 다 `CountryRepository + SessionRepository + StageRepository + AttemptRepository + LeaderboardService`를 같이 가진다.

즉 demo-lite는 **현재 service를 살짝 끄는 작업**이 아니라, **현재 서비스 대신 훨씬 얇은 loop를 별도로 만드는 작업**에 가깝다.

## dependency hot spot

### 1. auth / header / current member

가장 먼저 눈에 띄는 cross-cutting dependency는 auth다.

- [SiteHeaderModelAdvice.java](../src/main/java/com/worldmap/web/SiteHeaderModelAdvice.java)
- [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)
- [MemberSessionManager.java](../src/main/java/com/worldmap/auth/application/MemberSessionManager.java)

문제:

- 거의 모든 SSR 화면이 공통 header를 사용한다
- 공통 header는 `currentMember`, `showDashboardLink`를 모델에 넣는다
- 이 모델은 DB-backed member 조회를 전제한다

즉 demo-lite에서 auth를 제거하면, **header부터 분리해야 한다.**

### 2. ranking은 모든 게임 서비스에 박혀 있다

각 게임 service는 terminal 시점에 바로 `LeaderboardService`를 호출한다.

- [LocationGameService.java](../src/main/java/com/worldmap/game/location/application/LocationGameService.java)
- [PopulationGameService.java](../src/main/java/com/worldmap/game/population/application/PopulationGameService.java)
- [CapitalGameService.java](../src/main/java/com/worldmap/game/capital/application/CapitalGameService.java)
- [PopulationBattleGameService.java](../src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameService.java)
- [FlagGameService.java](../src/main/java/com/worldmap/game/flag/application/FlagGameService.java)

즉 랭킹 제거는 `/ranking` 페이지 하나를 지우는 문제가 아니라,
**게임 write path를 다시 정의하는 문제**다.

### 3. stats / mypage / admin는 leaderboard_record와 member record에 강하게 묶여 있다

아래는 전부 persistence-heavy read model이다.

- [StatsPageController.java](../src/main/java/com/worldmap/stats/web/StatsPageController.java)
- [MyPageController.java](../src/main/java/com/worldmap/web/MyPageController.java)
- [AdminPageController.java](../src/main/java/com/worldmap/admin/web/AdminPageController.java)

이 셋은 free demo-lite에서 살릴 이유가 거의 없고,
남겨 두면 DB/Redis 제거가 훨씬 어려워진다.

### 4. recommendation은 결과 자체보다 feedback 저장 경로가 충돌 지점이다

추천 결과 계산 자체는 비교적 가볍다.

하지만 현재 결과 페이지는 아래를 전제한다.

- `feedbackToken`
- `HttpSession` 기반 `RecommendationFeedbackSessionStore`
- `RecommendationFeedbackService`
- `RecommendationFeedbackRepository`
- `/api/recommendation/feedback`

관련 파일:

- [RecommendationPageController.java](../src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java)
- [RecommendationFeedbackApiController.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java)
- [result.html](../src/main/resources/templates/recommendation/result.html)
- [recommendation-feedback.js](../src/main/resources/static/js/recommendation-feedback.js)

즉 demo-lite에서는 추천 엔진을 제거할 필요는 없고,
**feedback 저장 루프만 잘라야 한다.**

### 5. demo bootstrap과 legacy initializer는 로컬 풀앱 기준이다

아래는 free demo-lite와 직접 관계가 없다.

- [DemoBootstrapService.java](../src/main/java/com/worldmap/demo/application/DemoBootstrapService.java)
- [CountrySeedInitializer.java](../src/main/java/com/worldmap/country/application/CountrySeedInitializer.java)
- [GameLevelRollbackInitializer.java](../src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)
- [RecommendationFeedbackLegacyColumnInitializer.java](../src/main/java/com/worldmap/common/config/RecommendationFeedbackLegacyColumnInitializer.java)

특히 `DemoBootstrapService`는 현재 풀앱 read model을 풍성하게 만드는 목적이라,
demo-lite에서는 가져오면 안 된다.

### 6. prod/deploy/test 레일도 현재는 full app를 전제로 한다

아래는 demo-lite에서 그대로 재사용하기 어렵다.

- [application-prod.yml](../src/main/resources/application-prod.yml)
- [RedisSessionProdConfiguration.java](../src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)
- [railway.toml](../railway.toml)
- [build.gradle](../build.gradle)의 `browserSmokeTest`, `publicUrlSmokeTest`
- [BrowserSmokeE2ETest.java](../src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java)

즉 demo-lite는 deploy/test lane도 분리해야 한다.

## safest extraction principle

### 원칙 1. main의 bean graph를 덜 건드린다

`@Profile("demo-lite")`, `@ConditionalOnProperty`를 main app 전역에 많이 넣기 시작하면,

- auth
- ranking
- stats
- admin
- game service
- recommendation feedback

가 동시에 조건 분기를 타게 된다.

이건 충돌 가능성이 크다.

### 원칙 2. 재사용은 data/asset까지만 한다

아래는 재사용 가치가 높다.

- `countries.json`
- `flag-assets.json`
- `static/images/flags/*`
- 질문 문항과 추천 가중치의 아이디어
- 현재 public shell의 카피/스타일 기준

반대로 아래는 직접 재사용하지 않는 편이 안전하다.

- JPA entity / repository
- game service
- `LeaderboardService`
- auth/session service
- admin/stats/mypage read model

### 원칙 3. demo-lite는 copy-and-simplify가 낫다

현재 코드를 feature flag로 지우는 것보다,

- 필요한 화면/JS만 복사
- data source를 정적으로 바꾸고
- state를 localStorage나 browser memory로 단순화

하는 편이 main을 덜 건드린다.

## 추천 구조

### 옵션 비교

#### 옵션 A. main app 안에 feature flag로 demo-lite 넣기

장점:

- 저장소가 하나다

단점:

- bean graph 분기가 커진다
- 테스트 행렬이 급격히 늘어난다
- auth/ranking/game service가 같이 흔들린다

판정:

- 비추천

#### 옵션 B. main app 안에 별도 `demo-lite` profile 추가

장점:

- 앱은 하나로 유지된다

단점:

- 결국 conditional bean과 conditional template가 많아진다
- Spring Boot startup graph가 계속 복잡해진다

판정:

- 차선책

#### 옵션 C. 별도 `demo-lite` 앱 또는 별도 배포 entrypoint

장점:

- main을 거의 건드리지 않는다
- free 배포 제약에 맞게 구조를 처음부터 다르게 설계할 수 있다
- 테스트와 deploy lane을 따로 가져갈 수 있다

단점:

- shell과 자산 복사 비용이 든다

판정:

- **권장안**

## 현재 채택한 구현 선택

문서 단계에서 권장안으로만 두지 않고, 현재 저장소에서는 실제로 아래를 채택했다.

1. sibling 앱 경로: [demo-lite](/Users/alex/project/worldmap/demo-lite)
2. 프론트 스택: `Vite + vanilla JS`
3. route 전략: `hash routing`
4. shared source: 메인 앱의 정적 JSON/국기 자산만 재사용
5. main Spring Boot app와 bean graph는 건드리지 않음

이 선택을 한 이유는 아래와 같다.

- 같은 저장소 안에서 관리하기 쉽다.
- static hosting에 바로 올리기 쉽다.
- `auth`, `ranking`, `stats`, `dashboard`, `game service` conditional branching을 main에 퍼뜨리지 않는다.
- 첫 조각에서 build 가능한 별도 앱을 실제로 만들 수 있다.

## current code 기준 제거 / 분리 우선순위

아래 순서는 "현재 앱을 그대로 지우는 순서"가 아니라,
**demo-lite를 분리할 때 어떤 의존성을 먼저 잘라야 main이 안 흔들리는가**의 순서다.

### 0단계. main 보호선 먼저 긋기

먼저 아래를 명시한다.

1. main은 그대로 유지
2. demo-lite는 별도 배포 대상
3. full app용 test/deploy lane은 그대로 유지

이 단계 없이 코드부터 지우면 충돌이 난다.

현재 이 보호선은 코드 기준으로 아래처럼 반영돼 있다.

- 메인 앱 빌드/테스트/배포 설정은 그대로 유지
- 별도 [demo-lite/package.json](/Users/alex/project/worldmap/demo-lite/package.json)과 [demo-lite/vite.config.mjs](/Users/alex/project/worldmap/demo-lite/vite.config.mjs) 추가
- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)로 별도 실행 명령 분리
- [sync-shared-assets.mjs](/Users/alex/project/worldmap/demo-lite/scripts/sync-shared-assets.mjs)가 메인 저장소 JSON/국기 자산을 `public/generated/`로 복사하고, [shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)는 브라우저에서 이 generated JSON만 fetch한다
- 첫 retained runtime으로 [capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)를 열어 `5 rounds + 3 lives + localStorage best score` 루프를 실제로 붙였다
- 두 번째 retained runtime으로 [flag-game.js](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)를 열어 `flag asset manifest + country seed join`, `same-continent 우선 distractor`, `같은 문제 재시도` 루프를 같은 local-state 패턴으로 붙였다
- 세 번째 retained runtime으로 [population-battle-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)를 열어 `population rank gap 기반 pair`, `difficulty band`, `2-choice battle`, `같은 문제 재시도` 루프를 브라우저 메모리 상태로 붙였다
- retained surface의 마지막 축으로 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)를 열어 `20문항 survey + 30국가 deterministic top 3 result`를 local-state 패턴으로 붙였다
- 마감 조각으로 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)를 추가해 각 feature가 terminal result 또는 recommendation result를 localStorage history로 남기고, [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js) 홈이 이를 cross-mode summary로 다시 읽게 했다
- 첫 retained runtime 예시로 [capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)와 Node 테스트 [capital-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/capital-game.test.mjs)를 추가해, local-state loop가 메인 Spring Boot service 없이도 별도 앱 안에서 독립적으로 닫히는지 증명했다

### 1단계. navigation / header 의존성부터 분리

가장 먼저 분리할 대상:

- [site-header.html](../src/main/resources/templates/fragments/site-header.html)
- [SiteHeaderModelAdvice.java](../src/main/java/com/worldmap/web/SiteHeaderModelAdvice.java)

이유:

- auth, mypage, dashboard link가 공통 shell에 걸려 있다
- retained page를 살리기 전에 header를 demo-lite용으로 따로 가져가야 한다

즉 demo-lite는 full header를 가져오지 말고,
`Home / Games / Recommendation` 정도만 가진 자체 header를 먼저 만든다.

### 2단계. persistence-heavy public surfaces를 먼저 끊는다

두 번째로 끊을 대상:

- `ranking`
- `stats`
- `mypage`
- `admin`

관련 package:

- `src/main/java/com/worldmap/ranking`
- `src/main/java/com/worldmap/stats`
- `src/main/java/com/worldmap/mypage`
- `src/main/java/com/worldmap/admin`

이유:

- 이 영역은 retained free-demo scope에 필요하지 않다
- 제거 가치가 크고, 남겨 둘 이유가 거의 없다

즉 demo-lite에서는 먼저 이 네 영역을 **비대상**으로 선언해야 한다.

### 3단계. auth / ownership를 제거한다

세 번째로 끊을 대상:

- `auth`
- guest ownership claim
- member session

관련 package:

- `src/main/java/com/worldmap/auth`

이유:

- retained free-demo에서는 사용자 계정이 없다
- 게임 시작 API가 `member`/`guestSessionKey` 분기를 하지 않아도 된다

이 단계가 끝나야 retained game loop를 브라우저 local state로 다시 쓰기 쉬워진다.

### 4단계. recommendation feedback 저장 경로를 자른다

네 번째로 끊을 대상:

- `RecommendationFeedbackApiController`
- `RecommendationFeedbackSessionStore`
- `RecommendationFeedbackService`
- 결과 페이지의 feedback form / JS

이유:

- 추천 결과는 유지 가치가 높다
- feedback 저장은 free-demo에서 굳이 유지할 이유가 없다

즉 recommendation은 **엔진은 유지, feedback loop는 제거**가 정답이다.

### 5단계. game service는 재사용하지 말고 retained mode만 가볍게 다시 만든다

가장 중요한 결정이다.

현재 game service는 아래 때문에 demo-lite에 직접 재사용하기 어렵다.

- JPA session/stage/attempt
- `GameSessionAccessContext`
- auth/guest ownership
- `LeaderboardService`

즉 아래 retained 모드:

- capital
- flag
- population-battle

은 demo-lite에서 **새로운 lightweight runtime**으로 다시 만드는 편이 더 안전하다.

권장:

- browser memory state
- localStorage best score
- 정적 country data

비권장:

- current `*GameService`를 conditional bean으로 우회

### 6단계. deploy/test lane을 마지막에 분리한다

마지막으로 분리할 대상:

- `railway.toml`
- `application-prod.yml`
- Redis session prod config
- browser/public smoke

이유:

- 기능 범위가 먼저 닫혀야 배포 설정도 올바르게 줄일 수 있다

즉 배포 설정을 먼저 줄이면 retained scope가 자꾸 흔들린다.

## copy / rewrite / leave 분류

### 그대로 가져갈 것

- `countries.json`
- `flag-assets.json`
- `static/images/flags/*`
- 현재 public shell의 일부 카피/스타일 토큰

### 단순 복사 후 단순화할 것

- 홈 shell
- recommendation survey/result 템플릿
- capital / flag / population-battle 플레이 화면의 일부 UI 구조

### 새로 다시 만들 것

- game runtime state
- score 저장
- recommendation result feedback 없는 submit flow
- demo-lite navigation/header

### main에만 남길 것

- auth
- ranking
- stats
- mypage
- admin
- recommendation feedback persistence
- demo bootstrap
- Redis session prod config
- full browser/public smoke 및 deploy pipeline

## recommended execution sequence

실제 작업 순서는 아래가 가장 안전하다.

1. `demo-lite scope` 고정
2. demo-lite 자체 header/shell 생성
3. ranking/stats/mypage/admin 비포함 선언
4. auth/ownership 비포함 선언
5. recommendation result에서 feedback 제거
6. capital / flag / population-battle lightweight runtime 구현
7. localStorage best score 추가
8. demo-lite 전용 smoke test
9. demo-lite 전용 free-tier 배포

## 하지 말아야 할 것

1. main app 안에 `demo-lite=true` 분기를 광범위하게 넣기
2. current game service를 repository 없이 돌리려고 우회하기
3. Redis/DB 없는 환경에서 `/ranking`, `/stats`까지 억지로 살리기
4. free-demo 때문에 main의 auth/ranking integrity를 약하게 만들기

## 최종 판단

`demo-lite`는 "현재 앱에서 일부 기능을 끄는 모드"보다,
**현재 저장소의 정적 데이터와 제품 문법만 재사용하는 별도 공개 트랙**으로 보는 편이 훨씬 안전하다.

즉 safest path는 아래다.

- main: 지금 그대로 유지
- demo-lite: 별도 shell + 별도 lightweight runtime + free-tier 배포

이렇게 해야 포트폴리오 깊이와 무료 공개 가능성을 둘 다 지킬 수 있다.
