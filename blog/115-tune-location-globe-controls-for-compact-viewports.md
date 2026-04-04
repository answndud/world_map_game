# 위치 게임 compact viewport 지구본 컨트롤 튜닝

이번 조각은 `3단계 국가 위치 찾기 게임 Level 1`의 작은 상호작용 polish다.

직전 조각에서 이미 아래는 잡혀 있었다.

- drag와 click 분리
- `mouse / pen / touch`별 threshold
- drag 직후 짧은 suppression window

그런데도 모바일 viewport에서는 지구본 체감이 아직 조금 급했다.  
즉 accidental selection은 줄었지만, **회전 속도와 확대 범위 자체가 desktop 기준 그대로라 손에 닿는 느낌이 너무 민감한 부분**이 남아 있었다.

그래서 이번에는 서버 게임 규칙이 아니라, **compact viewport에서만 지구본 컨트롤을 더 보수적으로 만드는 조정**을 했다.

---

## 어디를 고쳤나

파일:

- [/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js)
- [/Users/alex/project/worldmap/src/main/resources/templates/location-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/play.html)
- [/Users/alex/project/worldmap/src/main/resources/templates/location-game/start.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/start.html)
- [/Users/alex/project/worldmap/src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java)

핵심 함수는 이것들이다.

- `applyViewportGlobeControls()`
- `currentViewportGlobeTuning()`
- `isCompactViewport()`

---

## 왜 이 조각이 필요했나

위치 게임의 서버 흐름은 이미 안정적이었다.

- `POST /api/games/location/sessions`
- `GET /api/games/location/sessions/{id}/state`
- `POST /api/games/location/sessions/{id}/answer`

여기서 바뀌는 것은 서버 상태가 아니다.  
문제는 플레이어가 지구본을 만질 때의 브라우저 체감이다.

모바일에서는:

- 조금만 드래그해도 회전이 빨라 보이고
- 너무 가까이 확대되면 다시 맥락을 잃기 쉽고
- 선택 후 카메라가 너무 급하게 당겨지면 Stage 흐름이 튀어 보일 수 있다

즉 이건 정답 판정 문제가 아니라 **viewport별 control tuning 문제**다.

---

## 무엇을 바꿨나

### 1. compact viewport 전용 tuning table 추가

`currentViewportGlobeTuning()`은 viewport 폭이 `760px 이하`인지 보고 값을 나눈다.

desktop:

- `rotateSpeed = 0.62`
- `dampingFactor = 0.08`
- `zoomSpeed = 0.95`
- `minDistance = 140`
- `maxDistance = 420`
- `initialAltitude = 1.86`
- `focusAltitude = 1.45`

compact viewport:

- `rotateSpeed = 0.46`
- `dampingFactor = 0.11`
- `zoomSpeed = 0.76`
- `minDistance = 165`
- `maxDistance = 360`
- `initialAltitude = 2.02`
- `focusAltitude = 1.62`

의도는 단순하다.

- 회전은 조금 더 느리게
- 감속은 조금 더 강하게
- 확대는 조금 덜 깊게
- 초기 시야와 선택 후 시야는 조금 더 멀게

즉 **작은 화면일수록 맥락을 더 많이 남긴다**는 기준이다.

---

### 2. resize 때도 컨트롤을 다시 적용

`syncGlobeSize()`는 기존에는 크기만 다시 맞췄다.

이제는 `applyViewportGlobeControls()`도 같이 호출한다.

즉:

- portrait -> landscape
- window width 변화

가 생겨도 compact/desktop tuning이 다시 적용된다.

중요한 점은 **카메라 위치를 강제로 초기화하지는 않는다는 것**이다.  
현재 플레이 흐름은 유지하고, 컨트롤 감각만 다시 맞춘다.

---

### 3. 선택 후 focus 카메라도 viewport별로 다르게

기존에는 나라를 선택하면 모두 같은 altitude와 같은 duration으로 카메라가 당겨졌다.

이번에는 `handleCountrySelection(...)`에서:

- compact viewport는 더 멀고 천천히
- desktop은 기존보다 빠르고 가까운 쪽 유지

로 나눴다.

이렇게 해야 모바일에서 “나라를 하나 눌렀더니 너무 급하게 줌인된다”는 느낌이 줄어든다.

---

## 요청은 어디서 시작되고 어디서 상태가 바뀌나

상태 변화는 여전히 서버에서 일어난다.

1. 플레이어가 나라를 고른다
2. `POST /api/games/location/sessions/{id}/answer`
3. `LocationGameService.submitAnswer(...)`
4. `LocationGameSession / Stage / Attempt` 갱신

이번 조각에서 바뀐 것은 그 전 단계다.

- 브라우저가 현재 viewport를 읽고
- `Globe.gl controls()`에 어떤 값들을 넣을지 결정하고
- 선택 후 카메라 이동 altitude/duration도 다르게 적용한다

즉 서버 상태 전이는 그대로고, **브라우저 컨트롤 파라미터만 바뀐다.**

---

## 왜 이 로직이 컨트롤러/서비스가 아니라 JS에 있어야 하나

서버는 이런 걸 알 수 없다.

- 현재 viewport 폭
- 지금 compact layout인지
- 실제 브라우저에서 회전이 얼마나 빠르게 체감되는지

이건 전부 브라우저 컨텍스트에서만 알 수 있는 정보다.

그래서:

- 컨트롤러에 넣을 이유가 없고
- 서비스에 넣을 이유도 없다

반대로 하트, Stage, Attempt, 정답 판정은 계속 서버가 맡는다.

즉 책임 분리는 그대로 유지된다.

---

## 어떻게 테스트했나

테스트 파일:

- [/Users/alex/project/worldmap/src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java)

새로 추가한 시나리오:

- `locationCompactViewportUsesSaferGlobeControlTuning()`

이 테스트는:

1. viewport를 `390 x 844`로 바꾸고
2. 위치 게임을 실제 브라우저에서 시작한 뒤
3. `locationReadControlSnapshot()` hook으로 현재 컨트롤 값을 읽어
4. compact viewport 값이 실제로 적용됐는지 확인한다

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

> 위치 게임에서 drag 억제 규칙을 잡은 뒤에도 모바일에서는 지구본 회전과 확대가 아직 조금 급했습니다. 그래서 서버 도메인은 그대로 두고, 브라우저에서 compact viewport일 때만 `rotateSpeed`, `damping`, `zoom range`, `focus altitude`를 더 보수적으로 조정했습니다. 이건 게임 규칙이 아니라 컨트롤 감각 문제라 `location-game.js`와 browser smoke에서 닫는 게 맞았습니다.

---

## 아직 남은 것

이번 조각은 `compact viewport 1차 tuning`이다.

아직 남은 것은:

- 실기기 기준 세부 감도 재조정
- 태블릿 landscape처럼 폭은 넓지만 touch 비중이 큰 환경 확인
- 사운드/시각 연출 여부 결정

즉 이번 작업은 위치 게임 Reworking에서 **입력 억제 다음 단계인 viewport-aware control tuning** 조각으로 보면 된다.
