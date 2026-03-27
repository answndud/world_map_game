# 국기 보고 나라 맞히기 Level 1 vertical slice를 현재 구조에 붙이기

## 왜 이 조각이 필요한가

국기 게임은 사용자가 가장 직관적으로 이해하는 모드 중 하나다.

하지만 이 게임은 규칙보다 자산이 먼저인 모드이기도 하다.

우리는 이미 이전 조각에서

- `FlagAssetCatalog`
- `flag-assets.json`
- sample SVG 12개
- `FlagQuestionCountryPoolService`

까지 만들어 둔 상태였다.

이제 필요한 건 그 준비물을 실제 public 게임으로 연결하는 일이었다.

즉 이번 조각의 목표는

- 국기 자산 subset만으로도
- 현재 `session -> stage -> attempt -> leaderboard` 구조를 그대로 재사용해
- 다섯 번째 게임 모드를 public 제품에 붙일 수 있다는 걸 증명하는 것이다.

## 이번에 만든 것

이번에는 `국기 보고 나라 맞히기 Level 1 vertical slice`를 끝까지 연결했다.

- `flag` game mode 추가
- `flag_game_session / stage / attempt` 저장 구조 추가
- 시작 / 상태 / 답안 제출 / 재시작 / 결과 조회 API 추가
- SSR 시작 / 플레이 / 결과 화면 추가
- 랭킹 반영 추가
- 공개 홈 / `/ranking` / `/stats`에 flag 모드 노출

즉, 국기 게임은 더 이상 “준비 중인 후보 모드”가 아니라
현재 public 제품에 실제로 붙은 다섯 번째 기본 게임이다.

## 어떤 파일이 바뀌는가

### 게임 도메인과 서비스

- [FlagGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameService.java)
- [FlagGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameOptionGenerator.java)
- [FlagGameDifficultyPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameDifficultyPolicy.java)
- [FlagGameScoringPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameScoringPolicy.java)
- [FlagGameSession.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/domain/FlagGameSession.java)
- [FlagGameStage.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/domain/FlagGameStage.java)
- [FlagGameAttempt.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/domain/FlagGameAttempt.java)

### 출제 source of truth

- [FlagAssetCatalog.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagAssetCatalog.java)
- [FlagQuestionCountryPoolService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolService.java)

### 웹과 화면

- [FlagGameApiController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/web/FlagGameApiController.java)
- [FlagGamePageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/web/FlagGamePageController.java)
- [start.html](/Users/alex/project/worldmap/src/main/resources/templates/flag-game/start.html)
- [play.html](/Users/alex/project/worldmap/src/main/resources/templates/flag-game/play.html)
- [result.html](/Users/alex/project/worldmap/src/main/resources/templates/flag-game/result.html)
- [flag-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/flag-game.js)

### 공용 read model 연결

- [LeaderboardGameMode.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardGameMode.java)
- [LeaderboardService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/application/LeaderboardService.java)
- [LeaderboardPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java)
- [ServiceActivityService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/stats/application/ServiceActivityService.java)
- [StatsPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/stats/web/StatsPageController.java)
- [HomeController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/web/HomeController.java)

## 요청 흐름

요청 흐름은 기존 게임과 거의 같다.

1. `GET /games/flag/start`
2. `POST /api/games/flag/sessions`
3. `GET /api/games/flag/sessions/{sessionId}/state`
4. `POST /api/games/flag/sessions/{sessionId}/answer`
5. 필요하면 `POST /api/games/flag/sessions/{sessionId}/restart`
6. `GET /api/games/flag/sessions/{sessionId}`
7. 게임오버 시 `leaderboard_record` 저장 + Redis 랭킹 반영

중요한 점은, 국기 게임도 컨트롤러가 규칙을 직접 갖고 있지 않다는 것이다.

컨트롤러는 요청을 해석해서 서비스에 넘기기만 하고,
실제 상태 변화는 [FlagGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameService.java)에서 일어난다.

## 왜 서비스와 도메인에 둬야 하는가

국기 게임의 핵심 규칙은 아래다.

- 출제 가능한 국가 pool에서 문제 국가 고르기
- 국기 이미지 경로와 나라 보기 4개 만들기
- 하트 감소
- 정답 시 점수 부여
- 게임오버 판정
- 다음 Stage 생성
- 결과 read model 구성

이걸 컨트롤러에 두면

- HTTP 요청 처리
- 자산 subset 규칙
- 게임 점수 규칙

이 한 클래스에 섞인다.

반대로 서비스와 도메인에 두면

- 웹 진입점은 얇게 유지되고
- 테스트가 쉬워지고
- `FlagQuestionCountryPoolService` 같은 선행 read model과도 자연스럽게 연결된다.

즉, 국기 게임은 “이미지 하나 보여 주는 페이지”가 아니라
기존 게임과 같은 서버 주도 상태 머신으로 설명돼야 한다.

## 문제 생성은 어떻게 했나

문제 source는 [FlagQuestionCountryPoolService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolService.java)다.

즉, 국기 게임은 `country` 전체를 직접 읽지 않는다.

항상 아래 교집합만 읽는다.

`country seed ∩ flag manifest ∩ 실제 파일 존재`

그 위에서 [FlagGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameOptionGenerator.java)가 보기 4개를 만든다.

규칙은 단순하다.

1. 정답 국가를 먼저 고정한다.
2. 같은 대륙 국가를 먼저 distractor로 모은다.
3. 부족하면 전체 출제 가능 pool에서 보충한다.
4. 나라 이름이 중복되면 제거한다.
5. 마지막에 정답을 4개 보기 중 랜덤 위치에 넣는다.

즉, 국기 자산은 12개뿐이지만
문제 생성 기준과 보기 생성 기준은 이미 서버 정책으로 설명 가능하다.

## 지금 왜 12개 subset으로도 public vertical slice를 열었나

이건 의도적인 선택이다.

국기 게임은 자산 수가 적다는 이유로 끝없이 미루기 쉽다.

하지만 현재 프로젝트에서 더 중요한 건

- 새 게임 하나를 지금 구조에 어떻게 붙이는가
- 자산 기반 모드도 같은 `session / stage / attempt` 구조를 유지할 수 있는가
- 랭킹과 `/stats`까지 같은 read model로 연결할 수 있는가

를 증명하는 것이다.

그래서 이번에는 12개 subset이라도 먼저 vertical slice를 열고,
자산 확대는 다음 조각으로 넘겼다.

## 랭킹과 공개 화면은 어떻게 연결했나

이번 조각도 게임 화면만 만든 것으로 끝나지 않았다.

- 홈 모드 카드에 국기 게임 추가
- `/ranking`에 flag 보드 추가
- `/stats`에 flag 활동 카드와 Top 3 추가

이 연결을 같이 한 이유는,
새 게임이 정말 제품 모드인지 확인하려면
공개 read model까지 따라붙는지 봐야 하기 때문이다.

## 테스트

이번 조각에서 고정한 핵심 테스트는 아래다.

- [FlagGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/FlagGameFlowIntegrationTest.java)
  - endless run이 계속 진행되는가
  - 오답 시 하트가 줄고 같은 Stage를 다시 하는가
  - 3번 틀리면 GAME_OVER가 되는가
  - restart가 같은 sessionId로 리셋되는가
- [LeaderboardIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java)
  - flag 게임오버가 실제 랭킹에 반영되는가
  - `/ranking` SSR에 flag 보드가 보이는가
- [StatsPageControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/stats/StatsPageControllerTest.java)
  - 공개 Stats에 flag 활동 카드와 Top 3가 렌더링되는가
- [HomeControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/web/HomeControllerTest.java)
  - 홈 모드 카드에 국기 게임이 보이는가

실행 확인:

```bash
node --check src/main/resources/static/js/flag-game.js
./gradlew test --tests com.worldmap.game.flag.FlagGameFlowIntegrationTest --tests com.worldmap.game.flag.application.FlagAssetCatalogTest --tests com.worldmap.game.flag.application.FlagQuestionCountryPoolServiceIntegrationTest --tests com.worldmap.ranking.LeaderboardIntegrationTest --tests com.worldmap.stats.StatsPageControllerTest --tests com.worldmap.web.HomeControllerTest
./gradlew test
```

## 이번 조각에서 배운 점

자산 기반 게임도 결국은 같은 서버 주도 구조로 설명 가능해야 한다.

국기 게임은 “이미지 파일을 보여 준다”가 핵심이 아니라,

- 어떤 국가를 출제 가능 대상으로 볼지
- 어디에서 문제를 만들고
- 어디서 점수와 하트를 바꾸고
- 어떻게 랭킹과 공개 stats까지 이어지는지

를 현재 구조 안에서 보여 주는 작업이었다.

즉, 자산이 필요한 게임이라고 해서 별도 방식으로 빠지지 않고,
오히려 기존 구조를 더 잘 재사용할수록 설명 가능성이 높아진다.

## 다음 단계

다음 작은 조각은 둘 중 하나다.

- 국기 자산 pool을 12개보다 더 넓히기
- local demo bootstrap에 flag sample run을 추가하기

즉, 지금은 vertical slice 자체는 닫혔고,
다음부터는 `운영 가능성`과 `재현성`을 더 높이는 조각으로 넘어가면 된다.

## 면접에서 이렇게 설명할 수 있다

> 국기 게임은 자산이 필요한 모드라서 먼저 `FlagAssetCatalog`와 출제 가능 국가 pool을 고정한 뒤, 그걸 읽는 `flag` 세션 / Stage / Attempt 구조를 열었습니다. 컨트롤러는 요청만 받고, 실제 문제 생성과 정답 판정은 `FlagGameService`가 맡습니다. 덕분에 국기 자산처럼 정적 파일이 필요한 모드도 기존 위치/인구수/수도 게임과 같은 서버 주도 패턴으로 랭킹과 공개 stats까지 연결해 설명할 수 있게 됐습니다.
