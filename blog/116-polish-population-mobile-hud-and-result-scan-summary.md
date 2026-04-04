# 116. 인구수 게임 모바일 HUD와 결과 핵심 수치 정리

## 왜 이 조각을 먼저 했나

`4단계 국가 인구수 맞추기 게임 Level 1`은 서버 루프 자체는 이미 안정적이었다. `POST /api/games/population/sessions`, `GET /state`, `POST /answer`, `POST /restart`, `GET /result`가 모두 동작하고, 정답/오답 처리도 서버가 맡는다.

그런데 모바일에서는 두 가지가 아쉬웠다.

- 플레이 HUD가 `현재 Stage / 난이도 / 누적 점수 / 클리어 수 / 하트` 순서라, 실제 플레이 중 가장 먼저 봐야 하는 점수와 진행감이 덜 앞으로 왔다.
- 결과 화면은 recap strip이 생겼지만, `총점 / 클리어 / 마지막 Stage`를 한 줄로 빠르게 읽는 요약이 없어서 모바일에서 시선 이동이 길었다.

이번 조각은 점수 규칙이나 도메인 모델을 바꾸는 일이 아니라, 이미 서버가 내려주는 상태를 **더 짧은 순서로 다시 읽게 만드는 표현 계층 정리**였다.

## 무엇을 바꿨나

핵심 파일은 아래다.

- [play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/play.html)
- [result.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/result.html)
- [population-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-game.js)
- [site.css](/Users/alex/project/worldmap/src/main/resources/static/css/site.css)
- [PopulationGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java)

### 1. 플레이 HUD 우선순위 재배치

`population-game.js`의 `renderStatus()`는 이제 상태 카드를 이 순서로 그린다.

1. `Stage`
2. `점수`
3. `클리어`
4. `하트`
5. `난이도`

즉, 실제 플레이 도중 가장 자주 보는 값이 앞으로 왔다.

동시에 플레이 화면의 가이드 영역도 더 짧게 줄였다.

- `선택 상태 -> 선택`
- `진행 가이드 -> 가이드`
- `아직 선택하지 않았습니다. -> 선택 없음`
- `선택 중: ... -> 선택: ...`

### 2. 모바일에서 HUD를 2열 compact grid로 유지

`site.css`에 `stats-grid--compact-hud`, `stats-grid--result-summary` modifier를 추가했다.

공통 `.stats-grid`는 640px 이하에서 1열로 떨어지지만, 인구수 게임 HUD와 결과 통계는 이번 조각에서 예외로 두었다.

- 플레이 HUD는 모바일에서도 2열 유지
- 결과 통계도 모바일에서 2열 유지
- 카드 패딩과 숫자 크기도 한 단계 줄여 밀도를 맞춤

이렇게 한 이유는 인구수 게임은 `하트 / 점수 / Stage`를 동시에 보는 리듬이 강해서, 모바일에서 1열 세로 나열로 길어지면 실제 플레이 템포가 끊기기 때문이다.

### 3. 결과 배너 아래 한 줄 요약 추가

`result.html`에는 기존 recap strip 아래에 `result-banner__summary-line`을 추가했다.

표현은 이렇게 간단하다.

- `총점 500점 · 클리어 5개 · 마지막 Stage 6`

즉 recap strip이 세부 지표를 보여 주고, 그 아래 한 줄이 핵심 수치를 한 번 더 묶는다.

## 요청 흐름은 어떻게 유지되나

이번 조각은 서버 요청 흐름을 바꾸지 않았다.

그대로:

1. `GET /games/population/play/{sessionId}`로 SSR 플레이 페이지를 연다.
2. 브라우저는 `GET /api/games/population/sessions/{id}/state`로 현재 Stage 상태를 읽는다.
3. `POST /api/games/population/sessions/{id}/answer`로 서버가 정답 여부와 점수를 판정한다.
4. 게임 종료 후 `GET /games/population/result/{sessionId}`로 SSR 결과 페이지를 본다.

달라진 것은 그 흐름 속에서 **같은 payload를 어떤 순서로 배치해 보여 주느냐**뿐이다.

## 왜 컨트롤러나 서비스가 아니라 템플릿/JS에서 끝냈나

이건 `점수 정책`이나 `Stage 생성 규칙`이 아니라 **표현 우선순위** 문제였기 때문이다.

서버는 이미 아래 값을 내려주고 있었다.

- `stageNumber`
- `difficultyLabel`
- `clearedStageCount`
- `totalScore`
- `livesRemaining`
- `currentStageNumber`
- `firstTryClearCount`
- `totalAttemptCount`

이번 작업은 그 값을 더 잘 읽히게 재배치한 것이므로:

- 상태 계산은 서비스가 그대로 유지
- 표현 순서와 copy는 `population-game.js`, `play.html`, `result.html`, `site.css`가 맡는 구조가 맞다

즉, **읽기 순서를 바꾸기 위해 도메인 로직을 건드리지 않았다**는 점이 핵심이다.

## 테스트는 무엇으로 닫았나

이번 조각은 아래로 닫았다.

```bash
node --check src/main/resources/static/js/population-game.js
./gradlew test --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest
git diff --check
```

`PopulationGameFlowIntegrationTest`에서는:

- 플레이 페이지의 `stats-grid--compact-hud`
- 축약된 `선택`, `가이드` 라벨
- 결과 페이지의 `결과 핵심 수치`

같은 shell을 확인하도록 보강했다.

## 아직 약한 부분

이번 조각은 **모바일 가독성**만 다뤘다.

아직 남은 것은:

- 정답/오답 overlay copy를 더 아케이드스럽게 다듬을지
- 연속 정답 bonus를 실제 점수 규칙으로 넣을지
- 구간 경계가 지금 플레이 감각에 맞는지 다시 조정할지

즉, 이번 작업은 `표현 계층 1차 정리`이고, 규칙 자체는 다음 조각에서 판단하게 된다.
