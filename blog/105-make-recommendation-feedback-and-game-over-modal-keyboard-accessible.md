# 105. 추천 만족도와 게임오버 모달을 키보드로도 제대로 쓰게 만들기

## 왜 이 조각이 필요했나

라이트 모드 색과 레이아웃을 많이 다듬었어도,
키보드로 실제로 써 보면 두 군데가 여전히 어색했습니다.

첫째, 추천 결과 화면의 만족도 입력은 “1점~5점 중 하나를 고르는 단일 선택”인데
실제 구현은 `button` 다섯 개를 `aria-pressed`로 토글하는 방식이었습니다.

둘째, `population-battle` 게임오버 모달은 화면에만 떠 있을 뿐,
focus가 모달 안으로 들어오지 않고 뒤쪽 header나 버튼으로 tab이 빠질 수 있었습니다.

이번 조각의 목표는 서버 도메인을 바꾸는 것이 아니라,
이미 있는 UI가 브라우저와 보조기술 입장에서 **정말 그 의미대로 동작하게** 만드는 것입니다.

## 바뀐 파일

- [recommendation/result.html](/Users/alex/project/worldmap/src/main/resources/templates/recommendation/result.html)
- [recommendation-feedback.js](/Users/alex/project/worldmap/src/main/resources/static/js/recommendation-feedback.js)
- [population-battle-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-battle-game/play.html)
- [population-battle-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-battle-game.js)
- [site.css](/Users/alex/project/worldmap/src/main/resources/static/css/site.css)
- [RecommendationPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)
- [PopulationBattleGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/populationbattle/PopulationBattleGameFlowIntegrationTest.java)

## 1. 추천 만족도는 button 묶음이 아니라 radio가 맞다

이전 마크업은 대략 이런 구조였습니다.

```html
<div role="radiogroup">
  <button type="button" data-score="1">1점</button>
  <button type="button" data-score="2">2점</button>
  ...
</div>
```

겉보기는 비슷하지만, 이 구조는 브라우저 입장에서
“단일 선택 입력”이 아니라 “토글 가능한 버튼 몇 개”에 가깝습니다.

그래서 이번에는 [recommendation/result.html](/Users/alex/project/worldmap/src/main/resources/templates/recommendation/result.html)에서
`fieldset + radio`로 다시 바꿨습니다.

```html
<fieldset class="rating-fieldset">
  <legend class="sr-only">추천 만족도 선택</legend>
  <div class="rating-button-row">
    <label class="rating-score-option">
      <input class="rating-score-input" type="radio" name="satisfactionScore" value="1">
      <span>1점</span>
    </label>
    ...
  </div>
</fieldset>
```

이렇게 바꾸면 좋은 점이 분명합니다.

- 브라우저가 기본 radio semantics를 제공한다.
- 스크린리더가 “라디오 그룹”과 선택 상태를 제대로 읽는다.
- 키보드에서 `Tab`과 방향키 동작이 더 자연스럽다.
- hidden input으로 score를 따로 복제할 필요가 없다.

## 2. JS는 점수 계산이 아니라 선택 상태만 맞춘다

[recommendation-feedback.js](/Users/alex/project/worldmap/src/main/resources/static/js/recommendation-feedback.js)의 역할도 단순해졌습니다.

이전에는 클릭된 button의 `data-score`를 hidden input으로 옮기고,
선택된 button에 `aria-pressed`를 붙였습니다.

지금은 `input[name='satisfactionScore']`의 `change`만 받습니다.

```js
const scoreInputs = Array.from(form.querySelectorAll("input[name='satisfactionScore']"));

scoreInputs.forEach((input) => {
    input.addEventListener("change", () => {
        syncSelectedScore(scoreInputs);
        submitButton.disabled = !hasSelectedScore(scoreInputs);
        hideFeedbackMessage(messageBox);
    });
});
```

즉, JS가 “입력 의미를 만든다”가 아니라
이미 있는 네이티브 입력 위에서 **스타일과 submit 활성화만 보조**하게 된 것입니다.

이 방향이 더 좋은 이유는,
입력 의미를 브라우저 표준에 맡기면 JS가 깨져도 기본 입력은 남기 때문입니다.

## 3. 메시지는 보여주기만 하지 말고 읽히게도 해야 한다

피드백 성공/실패 메시지는 화면에만 보이면 절반만 끝난 것입니다.

그래서 [recommendation/result.html](/Users/alex/project/worldmap/src/main/resources/templates/recommendation/result.html)의
메시지 박스에 live region을 붙였습니다.

```html
<div
  id="recommendation-feedback-message"
  class="feedback-inline-message"
  role="status"
  aria-live="polite"
  aria-atomic="true"
  hidden></div>
```

이제 JS가 메시지 텍스트를 바꾸면
보조기술도 그 변화를 공지할 수 있습니다.

## 4. 게임오버 모달은 진짜 focus scope가 되어야 한다

`population-battle`의 게임오버 모달은
“보고 지나가는 장식”이 아니라 다음 행동을 강제하는 terminal UI입니다.

그래서 [population-battle-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-battle-game/play.html)에서
dialog 속성을 조금 더 명시했습니다.

```html
<section
  class="game-over-modal__panel"
  role="dialog"
  aria-modal="true"
  aria-labelledby="population-battle-game-over-title"
  aria-describedby="population-battle-game-over-summary"
  tabindex="-1">
```

그리고 핵심은 [population-battle-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-battle-game.js)입니다.

모달이 열릴 때:

1. `.page-shell`에 `inert`를 건다.
2. `restartButton`으로 focus를 이동한다.
3. `keydown`에서 `Tab` / `Shift+Tab`을 모달 안에서 순환시킨다.
4. `Escape`는 모달을 닫지 않고 다시 모달 안 focus를 유지한다.

코드 흐름은 대략 이렇습니다.

```js
function showGameOverModal(payload) {
    gameOverSummary.textContent = `하트를 모두 잃었습니다. 최종 점수 ${payload.totalScore}점으로 종료되었습니다.`;
    gameOverModal.hidden = false;
    pageShell.inert = true;
    document.addEventListener("keydown", handleGameOverModalKeydown);
    restartButton.focus();
}
```

그리고 restart가 성공하면
같은 세션을 다시 불러온 뒤 첫 번째 선택 input으로 focus를 돌립니다.

즉, modal open 이후의 focus 경로도
게임 루프 일부로 취급한 것입니다.

## 5. 공통 CSS에도 visible focus를 넣었다

색을 바꿔도 keyboard user가 현재 위치를 못 보면 사용성은 여전히 낮습니다.

그래서 [site.css](/Users/alex/project/worldmap/src/main/resources/static/css/site.css)에
공통 `:focus-visible` 규칙을 추가했습니다.

- `site-nav-link`
- `theme-toggle`
- `ghost-link`
- `primary-button`
- `secondary-button`
- `field input`
- `option-card`
- `rating-score-option`

핵심은 “마우스 hover와 별개로, 키보드 focus도 분명히 보이게 한다”입니다.

## 요청 흐름은 어떻게 보나

추천 결과 화면:

1. `POST /recommendation/survey`
2. `RecommendationPageController`
3. `recommendation/result.html` 렌더링
4. 사용자가 radio로 점수 선택
5. `recommendation-feedback.js`가 submit 활성화
6. `POST /api/recommendation/feedback`

population-battle 게임오버:

1. `POST /api/games/population-battle/sessions/{sessionId}/answer`
2. 서버가 `GAME_OVER` 응답
3. `population-battle-game.js`가 `showGameOverModal()` 호출
4. 브라우저가 modal focus scope를 열고 action 선택을 기다림

여기서 중요한 점은,
**게임 상태나 피드백 저장 규칙은 서버가 그대로 책임지고**
이번 조각은 브라우저가 그 상태를 더 정확하게 표현하도록 만든 것이라는 점입니다.

## 테스트는 무엇으로 확인했나

- [RecommendationPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)
  - 결과 페이지에 실제 `type="radio" name="satisfactionScore"`가 렌더링되는지 확인
  - 예전 hidden score input이 사라졌는지 확인
- [PopulationBattleGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/populationbattle/PopulationBattleGameFlowIntegrationTest.java)
  - play page에 dialog shell, `aria-describedby`, `tabindex="-1"`가 들어가는지 확인
- `node --check`
  - `recommendation-feedback.js`
  - `population-battle-game.js`

이번 턴에는 실제 브라우저 E2E까지는 붙이지 않았습니다.
그래서 focus trap 동작은 코드와 마크업 기준으로 먼저 고정한 상태입니다.

## 이번 조각에서 배운 점

접근성은 “예쁘게 보이게 만든 뒤 나중에 붙이는 옵션”이 아니라,
현재 UI가 **무슨 의미인지 코드가 정확히 말하도록 만드는 작업**에 가깝습니다.

특히 이런 규칙을 기억하기 좋습니다.

- 단일 선택이면 radio를 먼저 본다.
- 모달이면 focus 진입, 범위, 복귀를 먼저 본다.
- 성공/실패 메시지는 live region인지 먼저 본다.

## 면접에서는 이렇게 설명하면 된다

> 추천 만족도 입력은 화면상으로는 단일 선택인데 실제 구현은 토글 버튼 묶음이라 접근성이 어긋나 있었습니다. 그래서 결과 페이지를 `fieldset + radio`로 바꾸고, 메시지는 live region으로 공지되게 정리했습니다. 또 population-battle 게임오버 모달은 `inert`와 focus trap을 붙여 뒤쪽 화면으로 tab이 빠지지 않게 만들었습니다. 서버의 게임 판정 규칙은 그대로 두고, SSR 템플릿과 브라우저 JS에서 입력 semantics와 포커스 흐름만 바로잡은 조각입니다.
