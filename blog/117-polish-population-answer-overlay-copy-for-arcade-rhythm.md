# 117. 인구수 게임 정답/오답 overlay copy를 아케이드 리듬에 맞게 다듬기

## 왜 이 조각이 필요했나

인구수 게임은 이미 `정답 시 자동 다음 Stage`, `오답 시 같은 Stage 자동 재도전`까지 맞춰져 있었다.

하지만 플레이 중 실제로 보이는 copy는 아직 조금 설명문에 가까웠다.

- 정답 overlay는 `정답 / +점수`까지만 보여 줘서, 지금 총점이 어디까지 올라갔는지 바로 읽기 어려웠다.
- 오답 overlay는 하트만 보여 주고, HUD는 `직전 선택`처럼 조금 딱딱한 표현을 썼다.

이번 조각의 목적은 점수 규칙은 그대로 두고, **플레이 리듬 안에서 보이는 문구만 더 짧고 게임스럽게** 만드는 것이었다.

## 무엇을 바꿨나

핵심 파일은 아래다.

- [population-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-game.js)
- [BrowserSmokeE2ETest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java)

### 정답 overlay

기존:

- `정답`
- `+점수`

변경 후:

- `정답`
- `+점수 · 총점`

즉, 선택 상세를 다시 보여 주지 않으면서도 지금 러닝이 어디까지 올라갔는지는 바로 읽히게 했다.

### 오답 overlay와 HUD

기존:

- overlay: `하트 N개 남음`
- HUD: `직전 선택: ...`

변경 후:

- overlay: `하트 N개 남음 · 다시 추정`
- HUD: `방금 고른 구간: ...`

그리고 정답 직후 상태 라벨도 `정답 처리 완료` 대신 `다음 Stage 준비 중`으로 바꿨다.

이렇게 해서 문구가 결과 설명보다 **현재 게임 루프의 다음 행동**을 더 직접적으로 가리키게 됐다.

## 요청 흐름은 어떻게 유지되나

서버 요청 흐름은 그대로다.

1. `GET /api/games/population/sessions/{id}/state`
2. `POST /api/games/population/sessions/{id}/answer`
3. 정답이면 약 950ms 후 다음 state 재로드
4. 오답이면 약 950ms 후 같은 Stage state 재로드

이번 조각은 이 흐름 속에서 `answer payload`를 **어떤 문구로 재조합해 보여 주는가**만 바꿨다.

## 왜 서비스가 아니라 JS에서 끝냈나

이번 변경은 점수 정책이 아니라 **플레이 중 copy policy**였기 때문이다.

서버는 이미 아래 값을 내려주고 있었다.

- `awardedScore`
- `totalScore`
- `livesRemaining`
- `selectedOptionLabel`
- `outcome`

즉 새 데이터를 계산할 필요는 없고, 브라우저가 그 값을 더 짧은 표현으로 바꾸기만 하면 됐다.

그래서:

- 상태 계산은 서비스 그대로 유지
- 문구 재조합은 [population-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-game.js)가 맡는 구조가 맞다

## 테스트는 무엇으로 닫았나

이번 조각은 browser smoke로 닫았다.

```bash
node --check src/main/resources/static/js/population-game.js
./gradlew browserSmokeTest --tests com.worldmap.e2e.BrowserSmokeE2ETest.populationAnswerFeedbackUsesShortArcadeCopy
git diff --check
```

`BrowserSmokeE2ETest.populationAnswerFeedbackUsesShortArcadeCopy()`는 실제 브라우저에서:

- 오답 overlay가 `다시 추정`을 보여 주는지
- HUD가 `방금 고른 구간`으로 바뀌는지
- 정답 overlay가 `총점`까지 보여 주는지

를 직접 확인한다.

## 아직 남은 것

이번 조각은 **문구 정리**다.

아직 남은 것은:

- 정답/오답 overlay의 motion, 색 강도, 사운드 여부
- streak bonus를 실제 점수 규칙으로 넣을지 여부
- 구간 경계 재조정

즉 이번 작업은 규칙을 바꾼 게 아니라, 같은 규칙을 더 아케이드스럽게 읽히게 만든 단계다.
