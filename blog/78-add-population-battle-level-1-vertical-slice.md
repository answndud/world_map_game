# 인구 비교 퀵 배틀 Level 1 vertical slice를 현재 구조에 붙이기

## 왜 이 조각이 필요한가

수도 맞히기까지 붙인 뒤에 바로 다음으로 보기 좋은 건
`지금 있는 country 데이터만으로 또 다른 게임 리듬을 만들 수 있는가`다.

인구 비교 퀵 배틀은 그 질문에 가장 직접적으로 답한다.

- `country.population`은 이미 있다.
- 랭킹, 세션, Stage, Attempt 구조도 이미 있다.
- 하지만 인구수 퀴즈처럼 `정답 구간을 읽는 게임`이 아니라
  `두 나라 중 더 많은 쪽을 바로 고르는 게임`이라 리듬이 완전히 다르다.

즉, 이번 조각의 목표는
`인구 데이터를 또 쓴다`가 아니라
`같은 데이터로도 다른 아케이드 모드를 설계할 수 있다`는 걸 증명하는 것이다.

## 이번 조각에서 만든 것

이번에는 `population-battle` game mode를 public 제품에 끝까지 연결했다.

- `population_battle_game_session / stage / attempt` 저장 구조 추가
- 시작 / 상태 / 답안 제출 / 재시작 / 결과 조회 API 추가
- SSR 시작 / 플레이 / 결과 화면 추가
- 좌/우 2-choice 비교형 입력 흐름 추가
- 랭킹 반영 추가
- 공개 홈 / `/ranking` / `/stats`에 population battle 모드 노출

즉, 인구 비교 퀵 배틀은 더 이상 설계 메모가 아니라
현재 public 제품에 실제로 붙은 네 번째 기본 게임이다.

## 어떤 파일이 바뀌는가

### 게임 도메인과 서비스

- [PopulationBattleGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameService.java)
- [PopulationBattleGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameOptionGenerator.java)
- [PopulationBattleGameDifficultyPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameDifficultyPolicy.java)
- [PopulationBattleGameScoringPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameScoringPolicy.java)
- [PopulationBattleGameSession.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/domain/PopulationBattleGameSession.java)
- [PopulationBattleGameStage.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/domain/PopulationBattleGameStage.java)
- [PopulationBattleGameAttempt.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/domain/PopulationBattleGameAttempt.java)

### 웹과 화면

- [PopulationBattleGameApiController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/web/PopulationBattleGameApiController.java)
- [PopulationBattleGamePageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/web/PopulationBattleGamePageController.java)
- [start.html](/Users/alex/project/worldmap/src/main/resources/templates/population-battle-game/start.html)
- [play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-battle-game/play.html)
- [result.html](/Users/alex/project/worldmap/src/main/resources/templates/population-battle-game/result.html)
- [population-battle-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-battle-game.js)

### 공용 read model 연결

- [LeaderboardGameMode.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardGameMode.java)
- [LeaderboardService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/application/LeaderboardService.java)
- [LeaderboardPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java)
- [ServiceActivityService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/stats/application/ServiceActivityService.java)
- [StatsPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/stats/web/StatsPageController.java)
- [HomeController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/web/HomeController.java)

## 요청 흐름

요청 흐름은 기존 게임과 같은 패턴을 유지한다.

1. `GET /games/population-battle/start`
2. `POST /api/games/population-battle/sessions`
3. `GET /api/games/population-battle/sessions/{sessionId}/state`
4. `POST /api/games/population-battle/sessions/{sessionId}/answer`
5. 필요하면 `POST /api/games/population-battle/sessions/{sessionId}/restart`
6. `GET /api/games/population-battle/sessions/{sessionId}`
7. 게임오버 시 `leaderboard_record` 저장 + Redis 랭킹 반영

중요한 점은,
컨트롤러가 pair 생성 규칙을 직접 들고 있지 않다는 것이다.

컨트롤러는 요청을 받고 서비스에 넘길 뿐이고,
실제 상태 변화는 [PopulationBattleGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameService.java)에서 일어난다.

## 왜 서비스와 도메인에 둬야 하는가

인구 비교 퀵 배틀의 핵심 규칙은 아래다.

- 어떤 두 나라를 비교쌍으로 뽑을지 결정
- 좌/우 보기 배치를 섞기
- 정답 여부를 판정
- 오답 시 같은 Stage 재시도
- 게임오버 판정
- 다음 Stage 생성

이 규칙을 컨트롤러에 두면
HTTP 요청 처리 코드와 게임 규칙이 한 파일에 섞인다.

반대로 서비스와 도메인에 두면

- 웹 진입점은 얇게 유지되고
- pair selection 규칙을 테스트하기 쉬워지고
- `session -> stage -> attempt` 구조를 새 게임에도 그대로 재사용할 수 있다.

이번 조각의 핵심은 새 게임을 만들면서도
새로운 아키텍처를 만들지 않았다는 데 있다.

## 비교쌍은 어떻게 만들었나

비교쌍 생성은 [PopulationBattleGameDifficultyPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameDifficultyPolicy.java)와
[PopulationBattleGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameService.java)가 함께 맡는다.

기본 아이디어는 단순하다.

1. 전체 국가를 인구 순으로 정렬한다.
2. Stage 번호에 따라 `candidatePoolSize`, `minimumRankGap`, `maximumRankGap`를 정한다.
3. 그 범위 안에서 비교쌍을 뽑는다.
4. 좌/우 배치는 [PopulationBattleGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameOptionGenerator.java)가 다시 섞는다.

왜 절대 인구값이 아니라 `rank gap`을 기준으로 잡았냐면,
문제 난이도를 설명하기가 더 쉽기 때문이다.

- 초반: 순위 차이가 큰 비교
- 중반: 순위가 가까운 비교
- 후반: 근접한 고난도 비교

즉, 이 게임의 난이도는 “숫자가 크냐 작냐”보다
`얼마나 헷갈리는 두 나라를 붙였는가`로 설명된다.

## 테스트는 무엇으로 고정했나

이번 조각은 아래 테스트로 고정했다.

- [PopulationBattleGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/populationbattle/PopulationBattleGameFlowIntegrationTest.java)
  - endless run 진행
  - 2-choice state payload
  - 오답 시 같은 Stage 재시도
  - 3회 오답 시 GAME_OVER
  - 같은 sessionId restart
- [LeaderboardIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java)
  - population battle run이 public `/ranking`에 반영되는지 확인
- [StatsPageControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/stats/StatsPageControllerTest.java)
  - `/stats`에 battle 활동 카드와 일간 Top 보드가 보이는지 확인
- [HomeControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/web/HomeControllerTest.java)
  - 홈 모드 카드에 인구 비교 퀵 배틀이 노출되는지 확인

실행한 검증은 아래다.

```bash
node --check src/main/resources/static/js/population-battle-game.js
./gradlew test --tests com.worldmap.game.populationbattle.PopulationBattleGameFlowIntegrationTest --tests com.worldmap.ranking.LeaderboardIntegrationTest --tests com.worldmap.stats.StatsPageControllerTest --tests com.worldmap.web.HomeControllerTest
./gradlew test
git diff --check
```

## 이 조각에서 배우는 포인트

이번 조각이 중요한 이유는
`같은 country 데이터라도 게임 규칙을 다르게 설계하면 완전히 다른 모드가 된다`
는 점을 보여주기 때문이다.

인구수 퀴즈는
정답 구간을 읽는 게임이고,
인구 비교 퀵 배틀은
두 나라 중 더 큰 쪽을 빠르게 고르는 게임이다.

그래서 둘 다 `population`을 쓰지만,
문제 생성 규칙과 플레이 감각은 전혀 다르다.

이 차이를 서버 정책으로 명확히 분리해 두면
면접에서도 “왜 같은 데이터를 쓰는 두 게임을 따로 만들었는가”를 설명할 수 있다.

## 아직 남은 점

local demo bootstrap에는 아직 `population-battle` 샘플 run을 넣지 않았다.

그래서 fresh local 환경에서는 첫 플레이 전까지
`/stats`, `/ranking`의 battle 보드가 비어 있을 수 있다.

이건 다음 조각에서

- demo seed를 추가할지
- 아니면 실제 플레이 후 채워지는 보드로 둘지

판단하면 된다.
