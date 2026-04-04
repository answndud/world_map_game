# 위치 게임 탈락 모달과 결과 상단 recap polish

이번 조각은 `3단계 국가 위치 찾기 게임 Level 1`의 작은 마감 작업이다.  
요점은 단순하다. 위치 게임의 서버 상태 전이는 이미 안정적이었지만, **탈락 모달과 결과 상단이 너무 일반적인 문장 위주라 한 판을 빠르게 읽기 어려웠다.**

그래서 이번에는 새 게임 규칙이나 새 테이블을 만들지 않고, **이미 서버가 내려주고 있던 값들을 더 읽기 좋은 terminal feedback surface로 재배치**했다.

---

## 왜 지금 이걸 했나

위치 게임은 이 프로젝트의 대표 모드다.

요청 흐름은 이미 설명 가능했다.

- `POST /api/games/location/sessions`
- `GET /api/games/location/sessions/{id}/state`
- `POST /api/games/location/sessions/{id}/answer`
- `GET /games/location/result/{id}`

문제는 플레이어가 탈락했을 때였다.

- 모달은 `탈락했습니다` 같은 일반 문장만 먼저 보였다.
- 결과 페이지는 상세 표는 있었지만, “어디서 멈췄고 얼마나 버텼는지”를 상단에서 바로 읽기 어려웠다.

즉 데이터가 부족한 게 아니라, **표현 계층이 terminal state를 충분히 압축해서 보여 주지 못하고 있었다.**

---

## 무엇을 바꿨나

### 1. 탈락 모달에 run recap 3칸 추가

파일:

- [/Users/alex/project/worldmap/src/main/resources/templates/location-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/play.html)
- [/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js)

이제 게임오버 모달은 문장만 보여 주지 않고 아래 세 칸을 함께 보여 준다.

- `탈락 Stage`
- `클리어`
- `총점`

여기서 중요한 점은 **새 API를 만들지 않았다는 것**이다.  
`LocationGameAnswerView`가 이미 내려주던

- `stageNumber`
- `clearedStageCount`
- `totalScore`

를 그대로 써서 브라우저가 recap card를 만들게 했다.

즉 이 조각은 서버 계산 변경이 아니라 **기존 answer payload의 재구성**이다.

---

### 2. 결과 상단에 빠른 요약 strip 추가

파일:

- [/Users/alex/project/worldmap/src/main/resources/templates/location-game/result.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/result.html)

결과 페이지 상단 `result-banner` 아래에 빠른 요약 strip를 추가했다.

- `마지막 Stage`
- `1트 클리어 Stage`
- `총 시도`

하단의 Stage/Attempt 테이블은 그대로 남긴다.  
차이는 역할 분리다.

- 상단 strip: 한 판을 빠르게 스캔
- 하단 테이블: 상세 복기

이렇게 두 계층으로 나누니 결과 화면이 더 설명 가능해졌다.

---

### 3. overlay / modal entry motion 추가

파일:

- [/Users/alex/project/worldmap/src/main/resources/static/css/site.css](/Users/alex/project/worldmap/src/main/resources/static/css/site.css)

`stage-overlay`와 `game-over-modal__panel`에는 `rise-in` entry motion을 붙였다.

이건 과한 연출이 아니라, 정답/탈락 판정이 나타날 때 **정보 덩어리가 한 박자 분명하게 들어오게 만드는 정도**의 작은 motion이다.

---

## 요청은 어디서 시작되고 어디서 상태가 바뀌나

이 조각은 상태 전이 자체를 바꾸지 않았다.

상태 변화는 여전히 서버에서 일어난다.

1. `POST /api/games/location/sessions/{id}/answer`
2. `LocationGameService.submitAnswer(...)`
3. `LocationGameSession`, `LocationGameStage`, `LocationGameAttempt` 상태 갱신
4. answer payload 반환

이번에 바뀐 것은 그다음이다.

- 브라우저의 [/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js)가
  `GAME_OVER` payload를 받아 modal recap을 렌더링
- [/Users/alex/project/worldmap/src/main/resources/templates/location-game/result.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/result.html)이
  기존 result read model을 상단 요약 strip로 다시 보여 줌

즉 **도메인 상태는 서비스가 바꾸고, terminal feedback surface는 템플릿/JS가 더 잘 읽히게 만든다**는 책임 분리를 유지했다.

---

## 왜 이 로직이 컨트롤러가 아니라 JS / 템플릿에 있어야 하나

이번 작업은 새 게임 규칙을 만든 게 아니다.

- 점수 계산 변경 없음
- 하트 차감 규칙 변경 없음
- Stage 생성 규칙 변경 없음
- DB 스키마 변경 없음

바뀐 것은 **이미 계산된 상태를 어떤 밀도로 보여 주는가**다.

그래서:

- 컨트롤러에 조건 분기를 늘릴 이유가 없고
- 서비스에 presentation 전용 문구 조합을 넣을 이유도 없다

이 조각은 표현 계층에서 닫는 게 맞다.

---

## 테스트는 무엇으로 막았나

테스트 파일:

- [/Users/alex/project/worldmap/src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java)

확인한 것:

- play page가 여전히 접근 가능한 dialog shell을 렌더링하는지
- game-over recap container가 실제 SSR HTML에 있는지
- result page가 `빠른 요약`, `마지막 Stage`, `1트 클리어 Stage`를 포함하는지
- 기존처럼 정답/선택 상세를 다시 노출하지 않는지

실행:

```bash
node --check src/main/resources/static/js/location-game.js
./gradlew test --tests com.worldmap.game.location.LocationGameFlowIntegrationTest
git diff --check
```

---

## 면접에서 어떻게 설명할까

짧게 설명하면 이렇다.

> 위치 게임은 서버 상태 전이 자체는 안정적이었지만, 탈락 모달과 결과 상단이 너무 일반적인 문장 위주라 한 판을 빠르게 읽기 어려웠습니다. 그래서 새 집계를 만들지 않고 기존 answer payload와 result read model을 재조합해서, 게임오버 모달에는 `탈락 Stage / 클리어 / 총점`, 결과 상단에는 `마지막 Stage / 1트 클리어 / 총 시도` recap을 추가했습니다. 상태 계산은 그대로 두고 terminal feedback surface만 더 읽히게 만든 조각입니다.

---

## 아직 남은 것

이번 조각은 위치 게임 `Reworking` 중 하나만 닫은 것이다.

아직 남아 있는 건:

- 모바일 드래그 감도와 지구본 상호작용 미세 조정
- 사운드 사용 여부 결정
- 결과/게임오버 copy를 더 공격적으로 다듬을지 판단

즉, 이번 작업은 **위치 게임 완성도 작업의 terminal feedback 정리 조각**으로 보는 게 정확하다.
