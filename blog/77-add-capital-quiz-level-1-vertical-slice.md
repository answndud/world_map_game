# 수도 맞히기 Level 1 vertical slice를 현재 구조에 붙이기

## 왜 이 조각이 필요한가

새 게임을 추가할 때 중요한 건 “얼마나 새로워 보이느냐”보다
`현재 데이터와 공통 게임 구조를 얼마나 자연스럽게 재사용하느냐`다.

수도 맞히기는 그 조건에 가장 잘 맞는다.

- `country.capitalCity`가 이미 있다.
- 위치/인구수 게임이 이미 `session -> stage -> attempt`로 정리돼 있다.
- 4지선다 입력형이라 SSR + 바닐라 JS 셸을 그대로 재사용하기 쉽다.

그래서 이번 조각의 목표는
`새 게임 하나를 완전히 다른 방식으로 만드는 것`이 아니라,
`기존 서버 주도 게임 구조를 세 번째 모드에 그대로 적용할 수 있음을 증명하는 것`이었다.

## 이번 조각에서 만든 것

이번에는 `수도 맞히기 Level 1 vertical slice`를 끝까지 연결했다.

- `capital` game mode 추가
- `capital_game_session / capital_game_stage / capital_game_attempt` 저장 구조 추가
- 시작 / 상태 / 답안 제출 / 재시작 / 결과 조회 API 추가
- SSR 시작 / 플레이 / 결과 화면 추가
- 랭킹 반영 추가
- 공개 홈 / `/ranking` / `/stats`에 capital 모드 노출

즉, 수도 맞히기는 더 이상 “다음에 만들 게임”이 아니라
현재 public 제품에 실제로 붙은 세 번째 기본 게임이다.

## 어떤 파일이 바뀌는가

핵심 구현 파일은 아래다.

### 게임 도메인과 서비스

- [CapitalGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/application/CapitalGameService.java)
- [CapitalGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/application/CapitalGameOptionGenerator.java)
- [CapitalGameScoringPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/application/CapitalGameScoringPolicy.java)
- [CapitalGameDifficultyPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/application/CapitalGameDifficultyPolicy.java)
- [CapitalGameSession.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/domain/CapitalGameSession.java)
- [CapitalGameStage.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/domain/CapitalGameStage.java)
- [CapitalGameAttempt.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/domain/CapitalGameAttempt.java)

### 웹과 화면

- [CapitalGameApiController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/web/CapitalGameApiController.java)
- [CapitalGamePageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/web/CapitalGamePageController.java)
- [start.html](/Users/alex/project/worldmap/src/main/resources/templates/capital-game/start.html)
- [play.html](/Users/alex/project/worldmap/src/main/resources/templates/capital-game/play.html)
- [result.html](/Users/alex/project/worldmap/src/main/resources/templates/capital-game/result.html)
- [capital-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/capital-game.js)

### 공용 read model 연결

- [LeaderboardGameMode.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardGameMode.java)
- [LeaderboardService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/application/LeaderboardService.java)
- [LeaderboardPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java)
- [ServiceActivityService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/stats/application/ServiceActivityService.java)
- [StatsPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/stats/web/StatsPageController.java)
- [HomeController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/web/HomeController.java)

## 요청 흐름

요청 흐름은 기존 게임과 최대한 같다.

1. `GET /games/capital/start`
2. `POST /api/games/capital/sessions`
3. `GET /api/games/capital/sessions/{sessionId}/state`
4. `POST /api/games/capital/sessions/{sessionId}/answer`
5. 필요하면 `POST /api/games/capital/sessions/{sessionId}/restart`
6. `GET /api/games/capital/sessions/{sessionId}`
7. 게임오버 시 `leaderboard_record` 저장 + Redis 랭킹 반영

중요한 점은,
컨트롤러가 게임 규칙을 직접 들고 있지 않다는 것이다.

컨트롤러는 요청을 받고 서비스에 넘길 뿐이고,
실제 상태 변화는 [CapitalGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/application/CapitalGameService.java)에서 일어난다.

## 왜 서비스와 도메인에 둬야 하는가

수도 게임의 핵심 규칙은 아래다.

- Stage 생성
- 하트 감소
- 정답 시 점수 부여
- 게임오버 판정
- 다음 Stage 생성
- 결과 read model 구성

이 규칙을 컨트롤러에 두면
HTTP 요청 처리 코드와 게임 규칙이 섞인다.

반대로 서비스와 도메인에 두면

- 웹 진입점은 얇게 유지되고
- 테스트가 쉬워지고
- 같은 세션 / Stage / Attempt 구조를 다른 게임에도 다시 적용하기 쉬워진다.

특히 새 게임 추가에서는 이 구조 재사용이 중요하다.

## 보기 생성은 어떻게 했나

수도 보기 4개는 [CapitalGameOptionGenerator.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/application/CapitalGameOptionGenerator.java)가 만든다.

규칙은 단순하다.

1. 정답 국가의 수도를 먼저 고정한다.
2. 같은 대륙 국가 수도를 먼저 distractor로 모은다.
3. 부족하면 전체 국가 수도에서 보충한다.
4. 수도명이 중복되면 normalized key로 제거한다.
5. 마지막에 정답을 4개 보기 중 랜덤 위치에 넣는다.

왜 같은 대륙 우선이냐면,
완전히 무작위로만 뽑으면 보기가 너무 쉬워지거나 어색해지기 쉽기 때문이다.

즉, “보기를 서버가 만든다”는 점뿐 아니라
`보기를 어떤 기준으로 어렵게 만들었는가`도 설명 가능해야 했다.

## 랭킹과 공개 화면은 어떻게 연결했나

이번 조각은 게임 화면만 만든 걸로 끝나지 않았다.

- 홈 모드 카드에 수도 맞히기 추가
- `/ranking`에 capital 보드 추가
- `/stats`에 capital 완료 run 수와 Top 3 추가

이 연결을 같이 한 이유는,
새 게임이 “진짜 서비스 모드”인지 “실험용 화면”인지는
공개 read model까지 연결됐는지로 드러나기 때문이다.

## 테스트

이번 조각에서 고정한 핵심 테스트는 아래다.

- [CapitalGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/capital/CapitalGameFlowIntegrationTest.java)
  - endless run이 5 Stage 이후에도 계속되는가
  - 오답 시 하트가 줄고 같은 Stage를 다시 하는가
  - 3번 틀리면 GAME_OVER가 되는가
  - restart가 같은 sessionId로 리셋되는가
- [LeaderboardIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java)
  - capital 게임오버가 실제 랭킹에 반영되는가
  - `/ranking` SSR에 capital 보드가 보이는가
- [StatsPageControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/stats/StatsPageControllerTest.java)
  - 공개 Stats에 capital 활동 카드와 Top 3가 렌더링되는가
- [HomeControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/web/HomeControllerTest.java)
  - 홈 모드 카드에 수도 맞히기가 보이는가

실행 확인:

```bash
node --check src/main/resources/static/js/capital-game.js
./gradlew test --tests com.worldmap.game.capital.CapitalGameFlowIntegrationTest --tests com.worldmap.ranking.LeaderboardIntegrationTest --tests com.worldmap.stats.StatsPageControllerTest --tests com.worldmap.web.HomeControllerTest
./gradlew test
```

## 이번 조각에서 배운 점

새 게임 확장은 “새 규칙”보다 “기존 구조 재사용”이 더 중요할 수 있다.

수도 맞히기는 완전히 새로운 아키텍처가 필요하지 않았다.
오히려 위치/인구수 게임에서 이미 설명 가능한 구조를 그대로 재사용했기 때문에,

- 구현이 빨랐고
- 테스트 포인트가 명확했고
- 문서화도 쉬웠다.

이건 다음 게임인 인구 비교 퀵 배틀에도 그대로 도움이 된다.

## 다음 단계

다음 작은 조각은 `인구 비교 퀵 배틀 Level 1 설계`다.

수도 맞히기로 “새 모드 하나를 공통 구조에 붙이는 법”은 이미 증명했다.
이제는 `population` 데이터를 다른 게임 감각으로 재활용하는 쪽으로 넘어가면 된다.

## 면접에서 이렇게 설명할 수 있다

> 수도 맞히기는 `country.capitalCity`가 이미 있어서 현재 구조에 가장 작게 붙일 수 있는 새 게임이었습니다. 그래서 위치/인구수와 같은 endless run, 하트 3개, session-stage-attempt 구조를 그대로 재사용하고, 보기 생성만 same-continent 우선 정책으로 새로 만들었습니다. 덕분에 새 게임 하나를 추가하면서도 세션 시작, 정답 판정, 랭킹 반영, public stats/ranking 연결까지 같은 서버 주도 구조로 설명할 수 있게 됐습니다.
