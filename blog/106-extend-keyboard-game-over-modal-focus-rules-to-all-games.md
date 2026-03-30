# 106. 남은 4개 게임에도 같은 game over modal focus 규칙 적용하기

## 왜 이 후속 조각이 필요했나

직전 조각에서 추천 결과 화면의 만족도 입력을 `radio`로 바꾸고,
`population-battle` 게임오버 모달에만 focus trap을 붙였습니다.

문제는 제품 전체 기준으로 보면 규칙이 반쪽만 맞았다는 점입니다.

- `population-battle`는 modal이 실제 dialog처럼 동작
- `location / population / capital / flag`는 아직 화면에만 뜨는 overlay

같은 제품 안에서 terminal modal의 동작이 게임마다 다르면
사용자 경험도 흔들리고, 설명과 QA 포인트도 늘어납니다.

그래서 이번에는 남은 4개 게임에도
같은 keyboard focus 규칙을 그대로 확장했습니다.

## 이번에 바뀐 파일

- [location-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/play.html)
- [population-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/play.html)
- [capital-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/capital-game/play.html)
- [flag-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/flag-game/play.html)
- [location-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js)
- [population-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-game.js)
- [capital-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/capital-game.js)
- [flag-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/flag-game.js)
- [site.css](/Users/alex/project/worldmap/src/main/resources/static/css/site.css)
- [LocationGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java)
- [PopulationGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java)
- [CapitalGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/capital/CapitalGameFlowIntegrationTest.java)
- [FlagGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/FlagGameFlowIntegrationTest.java)

## 1. modal shell부터 같은 규칙으로 맞췄다

네 게임의 템플릿은 구조가 거의 같았습니다.

```html
<div id="...-game-over-modal" class="game-over-modal" hidden>
  <div class="game-over-modal__backdrop"></div>
  <section class="game-over-modal__panel" role="dialog" aria-modal="true" ...>
```

이번 후속 조각에서는 이 panel에
`aria-describedby`와 `tabindex="-1"`를 추가했습니다.

예를 들어 [capital-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/capital-game/play.html)에서는
이렇게 바뀝니다.

```html
<section
  class="game-over-modal__panel"
  role="dialog"
  aria-modal="true"
  aria-labelledby="capital-game-over-title"
  aria-describedby="capital-game-over-summary"
  tabindex="-1">
```

이렇게 하면 dialog title뿐 아니라 summary도 함께 읽힐 수 있고,
필요할 때 panel 자체를 fallback focus target으로 삼을 수 있습니다.

## 2. 핵심은 JS에서 modal을 실제 focus scope로 다루는 것이다

각 게임 JS에 같은 패턴을 넣었습니다.

### modal open

`showGameOverModal()`에서:

1. modal을 보여 준다.
2. `.page-shell`에 `inert`를 건다.
3. `keydown` listener를 붙인다.
4. `restartButton` 또는 panel에 focus를 보낸다.

예를 들면 [population-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-game.js)에서는:

```js
function showGameOverModal(payload) {
    gameOverSummary.textContent = `Stage ${payload.stageNumber}에서 탈락했습니다. 현재 총점 ${payload.totalScore}점, 다시 시작하면 같은 세션으로 Stage 1부터 이어집니다.`;
    gameOverModal.hidden = false;
    pageShell.inert = true;
    document.addEventListener("keydown", handleGameOverModalKeydown);
    (restartButton || gameOverPanel)?.focus();
}
```

### modal close

`hideGameOverModal()`에서는 반대로:

- modal hidden
- `pageShell.inert = false`
- keydown listener 제거

를 처리합니다.

즉, modal open/close가 이제 시각 효과가 아니라
**실제 focus 범위의 열림/닫힘**이 된 것입니다.

## 3. `Tab / Shift+Tab / Escape` 규칙도 같게 맞췄다

네 게임에 모두 `handleGameOverModalKeydown()`을 넣었습니다.

규칙은 단순합니다.

- `Escape`
  - modal을 닫지 않는다
  - 다시 restart 버튼 또는 panel로 focus를 유지한다
- `Tab`
  - 마지막 포커스 가능 요소에서 다음으로 가면 첫 요소로 순환
- `Shift+Tab`
  - 첫 요소에서 뒤로 가면 마지막 요소로 순환

왜 `Escape`로 닫지 않느냐?

이 modal은 도움말이나 알림이 아니라
하트를 모두 잃은 뒤 `홈으로 / 다시 시작` 중 하나를 고르게 하는 terminal modal이기 때문입니다.

즉, dismissible modal이 아니라
다음 행동 결정을 요구하는 상태에 더 가깝습니다.

## 4. restart 뒤 포커스 복귀 지점은 게임마다 다르다

여기서 공통 helper를 바로 만들지 않은 이유가 나옵니다.

모달 trap 자체는 거의 같지만,
restart 뒤 focus를 어디로 돌릴지는 게임마다 다릅니다.

### population / capital / flag

이 세 게임은 첫 번째 enabled radio input으로 바로 돌릴 수 있습니다.

```js
function focusFirstPlayableOption() {
    optionsBox.querySelector("input[name='capital-option']:not([disabled])")?.focus();
}
```

restart 성공 후 `await loadState()` 다음에 이 함수를 호출하면 됩니다.

### location

location은 radio input이 아니라 지구본이 주 인터랙션입니다.

그래서 [location-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/play.html)에서
`#globe-stage`에 `tabindex="-1"`와 `aria-label`을 추가하고,
restart 뒤에는 그 anchor에 포커스를 돌렸습니다.

이건 “location game이 이제 완전히 키보드로 플레이 가능하다”는 뜻은 아닙니다.
이번 조각의 목표는 그보다 작습니다.

- hidden modal에 focus가 남지 않게 한다
- 사용자가 다시 게임 surface로 돌아왔다는 사실을 분명히 한다

## 5. 공통 helper를 지금 당장 만들지 않은 이유

처음 보면 네 파일에 비슷한 코드가 들어가므로
helper를 빼고 싶어집니다.

하지만 이번 조각에서는 일부러 그렇게 하지 않았습니다.

이유는 두 가지입니다.

1. 현재 스크립트들은 각 게임별 독립 IIFE 구조다.
2. restart 후 focus 복귀 지점이 게임마다 다르다.

지금 helper를 만들면,
공용 스크립트 로딩 방식과 옵션 구조까지 같이 결정해야 해서
오히려 작은 접근성 조각이 큰 리팩터링으로 번질 수 있습니다.

그래서 이번에는 먼저 **다섯 게임이 같은 동작 규칙을 가지게 맞추고**,
그 다음 공통화 여부를 판단하는 편을 택했습니다.

## 요청 흐름은 어떻게 설명하면 되나

예를 들어 수도 게임 기준으로 보면:

1. `POST /api/games/capital/sessions/{sessionId}/answer`
2. 서버가 `GAME_OVER`를 응답
3. `capital-game.js`가 `showGameOverModal(payload)` 호출
4. 브라우저가 dialog에 focus를 넣고 배경 `.page-shell`을 `inert` 처리
5. 사용자가 `홈으로` 또는 `다시 시작하기` 선택
6. restart 성공 시 `loadState()` 뒤 첫 option input으로 focus 복귀

핵심은 서버 게임 로직이 아니라
**브라우저가 terminal game state를 dialog focus scope로 정확히 표현한다**는 점입니다.

## 테스트는 무엇으로 확인했나

이번에는 네 게임 모두 같은 형태의 SSR 회귀 테스트를 추가했습니다.

- [LocationGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java)
- [PopulationGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java)
- [CapitalGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/capital/CapitalGameFlowIntegrationTest.java)
- [FlagGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/FlagGameFlowIntegrationTest.java)

각 테스트는 play page를 열고 아래를 확인합니다.

- `role="dialog"`
- `aria-describedby="...-game-over-summary"`
- `tabindex="-1"`

그리고 JS는 `node --check`로 구문 검사를 통과시켰습니다.

아직 없는 것은 브라우저 E2E입니다.
즉, 실제 `Tab / Shift+Tab / Escape`를 브라우저에서 누르는 테스트는 다음 단계 과제입니다.

## 이번 조각에서 배운 점

접근성 정리는 추상적인 “좋아 보이는 원칙”보다
같은 제품 안에서 **동작 규칙을 일관되게 맞추는 일**이 더 중요합니다.

이번처럼 이미 한 화면에서 검증한 규칙이 있다면:

1. 먼저 한 화면에서 기준을 만든다
2. 나머지 화면에 같은 규칙을 확장한다
3. 그다음 helper 추출 여부를 판단한다

이 순서가 설명도 쉽고 리스크도 낮습니다.

## 면접에서는 이렇게 설명하면 된다

> population-battle에서 먼저 잡은 게임오버 모달 포커스 규칙을 위치/인구수/수도/국기까지 확장했습니다. 다섯 게임 모두 modal이 뜨면 배경 화면을 `inert`로 막고, dialog 안에서만 tab이 돌고, restart가 끝나면 다시 플레이 가능한 surface로 focus가 돌아가도록 맞췄습니다. 서버 게임 로직은 그대로 두고, SSR 템플릿과 페이지 JS에서 terminal UI 상태를 실제 focus scope로 표현한 조각입니다.
