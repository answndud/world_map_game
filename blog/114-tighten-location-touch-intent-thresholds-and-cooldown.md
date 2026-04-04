# 위치 게임 터치/펜 drag 억제 기준 고정

이번 조각은 `3단계 국가 위치 찾기 게임 Level 1`의 아주 작은 입력 안정화 작업이다.

핵심은 이것이다.

- 위치 게임은 이미 `drag vs click` 분리 로직이 있었다.
- `Globe.gl` polygon click만 믿지 않고, 지구본 좌표를 GeoJSON에 다시 대입하는 hit-test fallback도 있었다.
- 그런데 모바일이나 터치펜 환경에서는 **회전 직후 남는 click / polygon event가 너무 쉽게 선택으로 이어질 수 있었다.**

그래서 이번에는 서버 규칙을 바꾸지 않고, **브라우저 입력 의도 판별 기준**만 더 명시적으로 고정했다.

---

## 어디를 고쳤나

파일:

- [/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js)
- [/Users/alex/project/worldmap/src/main/resources/templates/location-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/play.html)
- [/Users/alex/project/worldmap/src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java)

`location-game.js`에서 바뀐 핵심은 세 가지다.

1. 입력 장치별 drag threshold 분리
2. drag 직후 짧은 suppression window 유지
3. 새 pointerdown이 시작되면 이전 cooldown 초기화

지금 값은 이렇게 잡았다.

- mouse: `18px`
- pen: `24px`
- touch: `30px`

즉 손가락 입력일수록 더 넉넉하게 움직임을 drag로 본다.

---

## 기존 흐름은 어땠나

위치 게임 플레이 흐름은 이미 이 구조였다.

1. `pointerdown`에서 시작 좌표 저장
2. `pointermove`에서 이동 거리 계산
3. 일정 threshold를 넘으면 선택 억제
4. `onPolygonClick(...)` 또는 `onGlobeClick(...)`에서 실제 선택

여기서 문제는 **threshold가 너무 단순하면 모바일 회전 직후의 잔여 이벤트를 충분히 걸러내지 못한다**는 점이다.

특히 위치 게임은 `Globe.gl` polygon click과 직접 GeoJSON hit-test를 같이 쓰기 때문에, 선택 이벤트가 들어오는 경로가 하나만 있는 것도 아니다.

---

## 이번에 어떻게 바꿨나

### 1. threshold를 장치별로 나눴다

함수:

- `currentPointerDragThreshold()`

이제 pointer type에 따라 다른 기준을 쓴다.

```js
mouse -> 18
pen   -> 24
touch -> 30
```

이건 복잡한 수학이 아니라, **손가락일수록 같은 움직임이 더 쉽게 accidental drag가 된다는 현실적인 기준**이다.

---

### 2. drag 직후 짧은 suppression window를 유지한다

함수:

- `suppressSelectionAfterDrag()`
- `shouldIgnoreSelection()`

이번 조각은 단순히 “이동 거리가 threshold를 넘었는가”만 보지 않는다.  
drag로 판단된 뒤에는 아주 짧은 시간 동안 선택을 더 막는다.

이유는 회전 직후에 남는 click / polygon event 때문이다.

즉 규칙은 이렇게 된다.

- drag가 감지되면 `selectionBlockedUntil`을 잠깐 미래 시점으로 잡는다
- 그 시간 안에 들어온 선택 시도는 무시한다

이렇게 해야 회전 직후의 accidental tap이 바로 선택으로 이어지지 않는다.

---

### 3. 새 탭이 시작되면 이전 cooldown은 지운다

함수:

- `pointerdown` handler

중요한 보정은 이것이다.

이전 drag의 suppression window가 남아 있어도, **새 pointerdown이 들어오면 그건 이미 새 의도된 입력**으로 본다.  
그래서 `selectionBlockedUntil`을 0으로 되돌린다.

이걸 안 하면:

- 회전 직후 accidental click은 막을 수 있어도
- 사용자가 곧바로 의도적으로 다시 탭한 입력까지 늦게 막히는 느낌이 난다

즉 이번 조각은 “보호는 하되, 플레이어가 바로 다시 탭하는 흐름은 너무 막지 않는다”는 균형을 잡은 것이다.

---

## 요청은 어디서 시작되고 어디서 상태가 바뀌나

이 조각은 서버 상태 전이를 바꾸지 않았다.

상태 변화는 여전히 아래에서 일어난다.

1. `POST /api/games/location/sessions/{id}/answer`
2. `LocationGameService.submitAnswer(...)`
3. `LocationGameSession`, `LocationGameStage`, `LocationGameAttempt` 갱신

이번에 바뀐 것은 **그 이전 브라우저 입력 계층**이다.

- `pointerdown`
- `pointermove`
- `pointerup`
- `onPolygonClick`
- `onGlobeClick`

사이에서 `location-game.js`가 “이 선택이 실제 tap인가, 방금 drag의 잔여 이벤트인가”를 더 엄격하게 판별한다.

즉 이건 도메인 규칙이 아니라 **입력 의도 판별 규칙**이다.

---

## 왜 컨트롤러/서비스가 아니라 JS에 있어야 하나

이 작업은 서버가 알 수 있는 정보가 아니다.

서버는 이런 걸 모른다.

- 손가락이 몇 px 움직였는가
- 드래그 직후 잔여 click이 남았는가
- 마우스였는지 터치였는지

이건 브라우저에서만 알 수 있는 입력 맥락이다.

그래서:

- 컨트롤러에 넣을 이유가 없고
- 서비스에 넣을 이유도 없다

반대로 stale submit, 정답 판정, 하트, Stage 전이는 계속 서버가 맡는다.

즉 책임 분리는 그대로다.

- 브라우저: 입력 의도 판별
- 서버: 상태 전이와 정답 판정

---

## 어떻게 테스트했나

테스트 파일:

- [/Users/alex/project/worldmap/src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java)

새로 고정한 시나리오는 이것이다.

`locationDragSuppressionBlocksImmediatePolygonSelectionButAllowsRetryAfterCooldown()`

검증하는 것:

1. drag 억제 상태를 걸고
2. 바로 polygon click을 보내면
3. submit 버튼이 여전히 비활성 상태여야 한다
4. cooldown 뒤 같은 polygon click을 다시 보내면
5. submit 버튼이 활성화되어야 한다

즉 이번 조각은 “드래그 직후 accidental selection은 막고, 잠시 뒤 재시도는 허용한다”는 계약을 실제 브라우저에서 확인한다.

실행:

```bash
node --check src/main/resources/static/js/location-game.js
./gradlew test --tests com.worldmap.game.location.LocationGameFlowIntegrationTest
./gradlew browserSmokeTest --tests com.worldmap.e2e.BrowserSmokeE2ETest
git diff --check
```

---

## 면접에서 어떻게 설명할까

짧게 말하면 이렇게 설명하면 된다.

> 위치 게임은 이미 drag와 click을 분리하고 hit-test fallback도 있었지만, 모바일에서는 회전 직후 polygon click이 바로 선택으로 이어질 수 있었습니다. 그래서 입력 장치별로 `mouse 18 / pen 24 / touch 30` threshold를 두고, drag 직후에는 짧은 suppression window를 둬 accidental click을 한 번 더 막았습니다. 이건 서버 상태 전이가 아니라 브라우저 입력 의도 판별 규칙이라 `location-game.js`와 browser smoke로 닫았습니다.

---

## 아직 남은 것

이번 조각은 `모바일/펜 입력 안정화 1차`다.

아직 남은 일은:

- 실제 휴대폰/태블릿에서 threshold 체감 확인
- 지구본 회전 감도 자체를 더 줄이거나 키울지 판단
- 사운드/시각 연출 여부 결정

즉, 이번 작업은 위치 게임 Reworking의 입력 계층을 한 단계 더 설명 가능하게 만든 조각이다.
