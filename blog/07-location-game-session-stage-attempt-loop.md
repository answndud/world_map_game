# [Spring Boot 게임 플랫폼 포트폴리오] 07. 위치 게임으로 `Session / Stage / Attempt` 루프를 어떻게 설계했는가

## 1. 이번 글에서 풀 문제

WorldMap의 시그니처는 3D 지구본 자체가 아닙니다.  
진짜 핵심은 **지구본이라는 입력 UI 위에서도 한 판 전체를 서버가 상태 기계로 일관되게 관리한다**는 점입니다.

이 글은 아래 문제를 닫습니다.

- 왜 위치 게임의 한 판 전체를 `Session`으로 봐야 하는가
- 왜 문제 하나를 `Stage`로 분리해야 하는가
- 왜 제출 한 번을 `Attempt`로 기록해야 하는가
- 왜 지구본 클릭은 프런트가 처리해도 정답 판정과 점수 계산은 서버가 맡아야 하는가
- 왜 ownership, stale submit, terminal result까지 같은 루프 안에서 설명되어야 하는가

즉, 이 글은 WorldMap이 단순 WebGL 데모가 아니라 **서버 주도 게임 플랫폼**이라는 사실을 가장 선명하게 보여 주는 챕터입니다.

## 2. 최종 도착 상태

이 글이 끝났을 때 위치 게임은 아래 상태여야 합니다.

- `GET /games/location/start`에서 시작 페이지를 연다
- `POST /api/games/location/sessions`가 guest/member 기준으로 session과 첫 stage를 만든다
- `GET /api/games/location/sessions/{id}/state`가 현재 stage 번호, difficulty label, score, lives, 목표 국가를 반환한다
- `POST /api/games/location/sessions/{id}/answer`가 선택한 국가 ISO3를 서버에서 판정한다
- 정답이면 다음 stage가 생성되고, 오답이면 같은 stage를 lives 1 감소 상태로 다시 시도한다
- 3번 틀리면 `GAME_OVER`가 되고 terminal result만 열 수 있다
- restart는 같은 session을 재사용하되 progress를 초기화한다
- result 화면에서는 stage/attempt 회고가 가능하지만, 클리어한 stage의 정답과 오답 선택 이름은 과도하게 드러내지 않는다
- 다른 브라우저나 다른 guest session은 이 session의 state/play/result에 접근할 수 없다

즉, 위치 게임의 최종 상태는 "지구본에서 나라를 맞힌다"가 아니라 **server-owned endless run contract가 실제로 작동한다**는 것입니다.

## 3. 먼저 알아둘 개념

### 3-1. endless run

고정 5문제 종료가 아니라 하트가 남아 있는 동안 계속 이어지는 구조입니다.  
현재 stage를 깰수록 다음 stage가 계속 생성됩니다.

### 3-2. stage retry

오답이면 다음 문제로 넘어가지 않고 같은 문제를 다시 시도합니다.  
따라서 "문제 하나"와 "제출 한 번"을 분리해야 합니다.

### 3-3. terminal result

result는 진행 중 상태를 들여다보는 API가 아니라, `GAME_OVER` 또는 `FINISHED` 뒤에만 열 수 있는 terminal resource입니다.

### 3-4. ownership

위치 게임 session은 로그인 member 또는 현재 브라우저 guest key에 귀속됩니다.  
즉, session id를 안다고 해서 아무나 열 수 없습니다.

### 3-5. stale submit

이전 stage 화면이나 중복 클릭으로 예전 payload가 다시 오면, 현재 stage/attempt와 맞지 않기 때문에 `409 CONFLICT`로 거부해야 합니다.

## 4. 이번 글에서 다룰 파일

- [LocationGameSession.java](../src/main/java/com/worldmap/game/location/domain/LocationGameSession.java)
- [LocationGameStage.java](../src/main/java/com/worldmap/game/location/domain/LocationGameStage.java)
- [LocationGameAttempt.java](../src/main/java/com/worldmap/game/location/domain/LocationGameAttempt.java)
- [LocationGameSessionRepository.java](../src/main/java/com/worldmap/game/location/domain/LocationGameSessionRepository.java)
- [LocationGameStageRepository.java](../src/main/java/com/worldmap/game/location/domain/LocationGameStageRepository.java)
- [LocationGameAttemptRepository.java](../src/main/java/com/worldmap/game/location/domain/LocationGameAttemptRepository.java)
- [LocationGameService.java](../src/main/java/com/worldmap/game/location/application/LocationGameService.java)
- [LocationGameDifficultyPolicy.java](../src/main/java/com/worldmap/game/location/application/LocationGameDifficultyPolicy.java)
- [LocationGameScoringPolicy.java](../src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java)
- [LocationGameApiController.java](../src/main/java/com/worldmap/game/location/web/LocationGameApiController.java)
- [LocationGamePageController.java](../src/main/java/com/worldmap/game/location/web/LocationGamePageController.java)
- [LocationGameStateView.java](../src/main/java/com/worldmap/game/location/application/LocationGameStateView.java)
- [LocationGameAnswerView.java](../src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java)
- [LocationGameSessionResultView.java](../src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java)
- [location-game/start.html](../src/main/resources/templates/location-game/start.html)
- [location-game/play.html](../src/main/resources/templates/location-game/play.html)
- [location-game/result.html](../src/main/resources/templates/location-game/result.html)
- [location-game.js](../src/main/resources/static/js/location-game.js)
- [active-countries.geojson](../src/main/resources/static/data/active-countries.geojson)
- [GameSessionAccessContext.java](../src/main/java/com/worldmap/game/common/application/GameSessionAccessContext.java)
- [GameSubmissionGuard.java](../src/main/java/com/worldmap/game/common/application/GameSubmissionGuard.java)
- [LocationGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java)
- [LocationGameDifficultyPolicyTest.java](../src/test/java/com/worldmap/game/location/application/LocationGameDifficultyPolicyTest.java)
- [LocationGameScoringPolicyTest.java](../src/test/java/com/worldmap/game/location/application/LocationGameScoringPolicyTest.java)

## 5. 핵심 도메인 모델 / 상태

### 5-1. `LocationGameSession`

[LocationGameSession.java](../src/main/java/com/worldmap/game/location/domain/LocationGameSession.java)는 한 판 전체 누적 상태입니다.  
실제로는 [BaseGameSession.java](../src/main/java/com/worldmap/game/common/domain/BaseGameSession.java)를 상속하고 `livesRemaining`을 추가합니다.

공통/핵심 상태:

- owner: `memberId` 또는 `guestSessionKey`
- `status`
- `totalRounds`
- `currentStageNumber`
- `clearedStageCount`
- `totalScore`
- `startedAt`, `finishedAt`
- `livesRemaining`

즉, session은 "현재 몇 번째 문제인가"뿐 아니라 **누가 플레이 중이고, 몇 점이며, 아직 살아 있는가**까지 들고 갑니다.

### 5-2. `LocationGameStage`

[LocationGameStage.java](../src/main/java/com/worldmap/game/location/domain/LocationGameStage.java)는 문제 하나의 상태입니다.

대표 상태:

- `stageNumber`
- `targetCountryName`
- `targetCountryIso3Code`
- `status`
- `attemptCount`
- `awardedScore`
- `clearedAt`

stage를 따로 두는 이유는 stage 단위 difficulty, 회고, retry를 설명하기 위해서입니다.

### 5-3. `LocationGameAttempt`

[LocationGameAttempt.java](../src/main/java/com/worldmap/game/location/domain/LocationGameAttempt.java)는 제출 한 번의 기록입니다.

대표 상태:

- `attemptNumber`
- `selectedCountryIso3Code`
- `selectedCountryName`
- `correct`
- `livesRemainingAfter`
- `attemptedAt`

이 엔티티가 있어야 result에서 "1차 오답 / 하트 2", "2차 정답 / 점수 +..." 같은 문구를 정직하게 만들 수 있습니다.

### 5-4. difficulty / score policy

위치 게임은 난이도와 점수를 별도 policy로 분리합니다.

- [LocationGameDifficultyPolicy.java](../src/main/java/com/worldmap/game/location/application/LocationGameDifficultyPolicy.java)
- [LocationGameScoringPolicy.java](../src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java)

즉, 서비스는 상태 전이에 집중하고 "얼마나 어려운가 / 몇 점인가"는 별도 규칙으로 설명합니다.

### 5-5. ownership / freshness contract

위치 게임 vertical slice가 현재 저장소 기준에서 중요한 이유는, 단순 정오답 판정만이 아니라 아래 contract를 모두 품기 때문입니다.

- `GameSessionAccessContext.assertCanAccess(session)`
- `GameSubmissionGuard.assertFreshSubmission(...)`
- terminal result only

즉, 이 게임은 now-hardening 이후의 실제 public contract를 대표합니다.

## 6. 설계 구상

### 왜 `Session / Stage / Attempt`를 나눴는가

게임 한 판을 한 엔티티에 몰아 넣으면 아래를 동시에 설명하기 어렵습니다.

- 같은 문제 재시도
- stage별 difficulty label
- 시도 로그 회고
- 하트 감소와 total score 누적
- stale submit 방어

그래서 WorldMap은 한 판 전체, 문제 하나, 제출 한 번을 의도적으로 분리했습니다.

### 왜 위치 게임을 대표 vertical slice로 택했는가

위치 게임은 WorldMap의 정체성을 가장 잘 보여 줍니다.

- 지구본 기반 UI
- 서버 주도 정답 판정
- 하트/점수/다음 stage 생성
- result 회고
- ownership과 stale submit hardening

즉, 이 프로젝트를 "게임 플랫폼"으로 설명할 때 가장 대표적인 모드입니다.

### 왜 public Level 1은 72개 주요 국가로 제한했는가

[LocationGameService.java](../src/main/java/com/worldmap/game/location/application/LocationGameService.java)는 `LEVEL_ONE_COUNTRY_LIMIT = 72`를 사용합니다.

이 제한을 둔 이유는 다음입니다.

- 지구본 클릭 품질을 먼저 안정화
- 너무 작은 나라 클릭 난이도 급상승 방지
- public 초기 경험을 명확한 주요 국가 중심으로 설계

즉, "전 세계 모두 출제"보다 **제품 품질과 설명 가능성**을 우선한 선택입니다.

### 왜 정답 판정은 서버에 둬야 하는가

위치 게임은 지구본 위 클릭이 핵심이라, 초보자는 정답 판정까지 프런트로 밀고 싶어질 수 있습니다.  
하지만 그렇게 하면 아래가 무너집니다.

- 점수 계산 신뢰성
- lives 감소의 source of truth
- stage/attempt 회고 기록
- stale submit 방어
- 다른 게임과의 공통 contract

그래서 브라우저는 입력을 받고, 서버가 상태를 결정합니다.

## 7. 코드 설명

### 7-1. `LocationGameSession`: endless run용 session subclass

[LocationGameSession.java](../src/main/java/com/worldmap/game/location/domain/LocationGameSession.java)는 `BaseGameSession`을 상속하고 `DEFAULT_LIVES = 3`을 가집니다.

중요 메서드:

- `planNextStage(Integer nextStageNumber)`
- `clearCurrentStage(Integer stageNumber, Integer awardedScore, LocalDateTime clearedAt)`
- `recordWrongAttempt(Integer stageNumber, LocalDateTime attemptedAt)`
- `restart(Integer totalStages)`

`recordWrongAttempt`는 lives를 줄이고 `0`이 되면 `GAME_OVER`로 끝냅니다.  
즉, "실패"는 stage가 아니라 session이 terminal 상태가 되는 사건입니다.

### 7-2. `LocationGameService`: 현재 규칙의 중심

[LocationGameService.java](../src/main/java/com/worldmap/game/location/application/LocationGameService.java)는 이 글의 주인공입니다.

현재 이 클래스가 담당하는 것:

- guest/member 시작 분기
- 국가 pool 정렬
- 첫 stage 생성
- current state 조회
- restart
- answer 판정
- next stage 생성
- result 조립
- leaderboard 반영

중요한 디테일은 두 가지입니다.

#### `findByIdForUpdate`

answer/restart는 `getSessionForUpdate(...)`를 통해 pessimistic write lock 기반 repository를 사용합니다.  
즉, 위치 게임은 now-hardening 이후 session write serialization을 이미 반영한 상태입니다.

#### terminal 때만 leaderboard 기록

`session.getStatus() != IN_PROGRESS`일 때만 [LeaderboardService.java](../src/main/java/com/worldmap/ranking/application/LeaderboardService.java)의 `recordLocationResult(...)`를 호출합니다.  
즉, 진행 중 score는 leaderboard 대상이 아닙니다.

### 7-3. 국가 pool과 difficulty

service는 country repository에서 국가를 population 역순으로 정렬한 뒤 상위 72개만 씁니다.

그리고 [LocationGameDifficultyPolicy.java](../src/main/java/com/worldmap/game/location/application/LocationGameDifficultyPolicy.java)는 stage 번호에 따라 후보 pool을 넓힙니다.

현재 규칙:

- `1~5`: `Sector A · 주요 국가`, 최대 28개
- `6~12`: `Sector B · 지역 확장`, 최대 60개
- `13~25`: `Sector C · 글로벌`, 최대 110개
- `26~45`: `Sector D · 고난도`, 최대 160개
- 그 이후: `Sector E · 전체 국가`

즉, 위치 게임의 난이도는 "정답 판정 방식"이 아니라 **어느 후보군 안에서 나라를 뽑는가**로 조절됩니다.

### 7-4. `LocationGameScoringPolicy`

[LocationGameScoringPolicy.java](../src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java)는 정답이면 아래 점수 체계를 적용합니다.

- base score: `100 + (stageNumber - 1) * 20`
- life bonus: `livesRemaining * 10`
- attempt bonus:
  - 1회차 정답: `30`
  - 2회차 정답: `10`
  - 그 이후: `0`

오답이면 `0점`입니다.

즉, 후반 stage일수록, 하트를 많이 남길수록, 빨리 맞힐수록 점수가 큽니다.

### 7-5. `LocationGameApiController`, `LocationGamePageController`

[LocationGameApiController.java](../src/main/java/com/worldmap/game/location/web/LocationGameApiController.java)는 JSON entrypoint입니다.

- `POST /api/games/location/sessions`
- `GET /state`
- `POST /restart`
- `POST /answer`
- `GET /result`
- `GET /{sessionId}` alias

[LocationGamePageController.java](../src/main/java/com/worldmap/game/location/web/LocationGamePageController.java)는 SSR shell entrypoint입니다.

- `GET /games/location/start`
- `GET /games/location/play/{sessionId}`
- `GET /games/location/result/{sessionId}`

중요한 점은 play/result SSR도 단순 템플릿 반환이 아니라 service의 ownership/result accessibility 규칙을 같이 탑니다.

### 7-6. `location-game.js`: 입력과 표현만 담당

[location-game.js](../src/main/resources/static/js/location-game.js)는 현재도 꽤 두꺼운 파일이지만, 그래도 책임은 명확합니다.

- start form submit
- globe 초기화
- pointer drag/click intent 분리
- 국가 선택 highlight
- state fetch / answer submit
- overlay / feedback
- game-over modal
- restart 후 focus 복귀

반대로 이 파일이 하지 않는 것:

- 정답 판정
- 점수 계산
- lives 차감
- next stage 생성
- result 공개 여부 결정

즉, 위치 게임이 화려해 보여도 실제 rule engine은 서버에 있습니다.

### 7-7. `play.html`과 `result.html`

[location-game/play.html](../src/main/resources/templates/location-game/play.html)은 아래 계약을 문서처럼 보여 줍니다.

- 첫 진입은 SSR
- `data-session-id`로 현재 session 전달
- `globe-stage`는 `tabindex="-1"`를 가진 플레이 영역
- game-over modal은 `role="dialog"`, `aria-describedby`, restart button을 가진다

[location-game/result.html](../src/main/resources/templates/location-game/result.html)은 server-stored stage/attempt 흐름을 다시 보여 줍니다.

중요한 현재 behavior:

- stage별 attempt 로그는 보여 줌
- 하지만 클리어한 stage의 선택 국가명과 정답 국가명을 과도하게 그대로 노출하지 않음

이건 "다시 학습용 정답표"가 아니라 **플레이 흐름 회고**라는 의미를 지키기 위한 선택입니다.

## 8. 요청 흐름 / 상태 변화

### 8-1. 세션 시작

```text
GET /games/location/start
-> start page SSR

POST /api/games/location/sessions
-> 로그인 member면 startMemberGame
-> 아니면 guestSessionKey 생성 후 startGuestGame
-> session 저장
-> 첫 stage 생성
-> playPageUrl 반환
```

### 8-2. 현재 state 조회

```text
GET /api/games/location/sessions/{id}/state
-> GameSessionAccessContextResolver.resolve(request)
-> ownership 검사
-> 현재 session / stage 조회
-> difficulty plan 계산
-> stageId + expectedAttemptNumber 포함 state 반환
```

### 8-3. answer 제출

```text
POST /api/games/location/sessions/{id}/answer
-> ownership 검사
-> currentStageNumber 일치 확인
-> GameSubmissionGuard.assertFreshSubmission(stageId, expectedAttemptNumber)
-> 선택 ISO3를 Country로 조회
-> scoring policy로 정오답/점수 계산
-> attempt 저장
-> 정답이면 다음 stage 생성 + session advance
-> 오답이면 lives 감소, 필요 시 GAME_OVER
-> terminal이면 leaderboard 기록
```

### 8-4. result 조회

```text
GET /api/games/location/sessions/{id}/result
또는
GET /games/location/result/{id}
-> ownership 검사
-> session status가 READY / IN_PROGRESS면 거부
-> stage + attempt 회고 view 조립
-> API 또는 SSR 반환
```

### 8-5. 프런트 입력 흐름

```text
브라우저에서 지구본 클릭
-> location-game.js가 선택 강조
-> 제출 버튼 활성화
-> answer API 호출
-> 응답에 따라 overlay / feedback / next stage / game-over modal 처리
```

즉, 브라우저는 입력을 수집하지만 상태는 언제나 서버가 정합니다.

## 9. 실패 케이스 / 예외 처리

- 선택 국가 없이 제출: request validation 또는 브라우저 submit 방지
- 잘못된 `sessionId`: `ResourceNotFoundException`
- 다른 브라우저 session으로 접근: `403 FORBIDDEN`
- 진행 중 result 조회: `404`
- 오래된 `stageId` / `expectedAttemptNumber`: `409 CONFLICT`
- 현재 stage 번호와 payload 불일치: `409`
- 지원하지 않는 ISO3 제출: `400`
- 국가 데이터가 너무 적으면: 게임 시작 거부
- 지구본에서 드래그를 클릭으로 오인하면: JS에서 pointer intent detection으로 완화

즉, 위치 게임의 리스크는 그래픽보다 **입력 안정성, 소유권, 상태 무결성**에 더 가깝습니다.

## 10. 테스트로 검증하기

### 10-1. [LocationGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java)

이 테스트 클래스는 현재 위치 게임의 핵심 리스크를 거의 다 잡습니다.

검증 내용:

- 5 stage를 넘어 계속 이어지는 endless run
- 오답 시 같은 stage 재시도 + life 감소
- accessible game-over modal shell 렌더링
- stale duplicate wrong answer는 extra life를 먹지 않고 `409`
- stage advance 뒤 duplicate correct answer도 `409`
- 3번 틀리면 `GAME_OVER`
- restart는 같은 session을 재사용하면서 progress를 초기화
- result 페이지가 cleared stage의 선택/정답 이름을 과하게 노출하지 않음
- 다른 브라우저 session이 state/answer/play에 접근하면 `403`

즉, 이 테스트는 단순 happy path가 아니라 **public contract 전체**를 고정합니다.
다만 여기서 "전체"는 위치 게임의 서버 루프와 SSR shell 기준입니다.
실제 WebGL 클릭 감도, 디바이스별 드래그 의도 판별, 브라우저 keyboard trap은 [BrowserSmokeE2ETest.java](../src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java) 같은 별도 브라우저 레일과 함께 읽어야 합니다.

### 10-2. [LocationGameDifficultyPolicyTest.java](../src/test/java/com/worldmap/game/location/application/LocationGameDifficultyPolicyTest.java)

- stage 번호에 따라 candidate pool이 넓어지는지 확인합니다.

### 10-3. [LocationGameScoringPolicyTest.java](../src/test/java/com/worldmap/game/location/application/LocationGameScoringPolicyTest.java)

- stage, attempt, lives 기준 점수 정책을 검증합니다.

실행 명령:

```bash
./gradlew test \
  --tests com.worldmap.game.location.LocationGameFlowIntegrationTest \
  --tests com.worldmap.game.location.application.LocationGameDifficultyPolicyTest \
  --tests com.worldmap.game.location.application.LocationGameScoringPolicyTest
```

## 11. 회고

위치 게임은 WebGL showcase처럼 보이지만, 실제 주인공은 session/stage/attempt와 ownership/stale submit contract입니다.

이 구조의 장점:

- 같은 문제 재시도와 endless run을 자연스럽게 설명할 수 있다
- 입력 UI가 복잡해도 rule engine은 서버에 남는다
- later stage population/capital/flag/battle로 자연스럽게 확장된다
- result, restart, leaderboard까지 같은 contract 안에서 설명된다

### 현재 구현의 한계

- 모바일 드래그/클릭 감도는 여전히 더 다듬을 여지가 있다
- public Level 1은 72개 주요 국가로 제한되어 있다
- polygon click 안정성은 브라우저/디바이스 차이에 영향을 받는다
- `LocationGameFlowIntegrationTest`는 서버 루프와 page contract를 강하게 고정하지만, 실제 3D globe 상호작용의 모든 브라우저별 체감까지 자동 증명하지는 않는다

## 12. 취업 포인트

### 12-1. 1문장 답변

WorldMap 위치 게임은 `Session / Stage / Attempt`를 분리하고 ownership, stale submit, terminal result contract까지 서버가 관리하게 설계해, 지구본 입력 UI 위에서도 서버 주도 게임 플랫폼이라는 정체성을 유지했습니다.

### 12-2. 30초 답변

위치 게임은 `LocationGameSession`, `LocationGameStage`, `LocationGameAttempt`로 한 판 전체, 문제 하나, 제출 한 번을 분리했습니다. `LocationGameService`가 정답 판정, 점수 계산, 하트 감소, 다음 stage 생성, leaderboard 반영을 맡고, 브라우저는 Globe 렌더링과 선택 표현만 담당합니다. 또한 `GameSessionAccessContext`로 현재 브라우저 ownership을 검증하고, `GameSubmissionGuard`로 stale submit을 `409`로 막아 공용 game platform contract를 실제로 보여 줍니다.

### 12-3. 예상 꼬리 질문

- 왜 stage와 attempt를 굳이 나눴나요?
- 왜 정답 판정을 프런트에 두지 않았나요?
- 왜 72개 주요 국가만 쓰나요?
- stale submit 방어는 왜 위치 게임에서도 중요한가요?
- result 페이지가 왜 stage별 정답을 그대로 다 보여 주지 않나요?

## 13. 시작 상태

- game 패키지 골격과 country seed는 있지만 대표 게임 vertical slice가 없는 상태
- 공통 session contract는 있어도, 그 contract가 지구본 기반 UI에서 어떻게 살아나는지는 아직 보여 주지 못한 상태

## 14. 이번 글에서 바뀌는 파일

- `src/main/java/com/worldmap/game/location/domain/**`
- `src/main/java/com/worldmap/game/location/application/**`
- `src/main/java/com/worldmap/game/location/web/**`
- `src/main/resources/templates/location-game/**`
- `src/main/resources/static/js/location-game.js`
- `src/main/resources/static/data/active-countries.geojson`
- 관련 테스트

## 15. 구현 체크리스트

1. `LocationGameSession`, `Stage`, `Attempt` 엔티티를 만든다
2. start/state/answer/result API를 추가한다
3. SSR `start/play/result` 페이지를 추가한다
4. difficulty/scoring policy를 분리한다
5. 주요 국가 pool과 geojson 자산을 정리한다
6. browser selection -> server answer 흐름을 연결한다
7. ownership, stale submit, terminal result contract를 적용한다
8. leaderboard 반영과 result 회고를 연결한다
9. flow/difficulty/scoring 테스트를 고정한다

## 16. 실행 / 검증 명령

```bash
./gradlew test \
  --tests com.worldmap.game.location.LocationGameFlowIntegrationTest \
  --tests com.worldmap.game.location.application.LocationGameDifficultyPolicyTest \
  --tests com.worldmap.game.location.application.LocationGameScoringPolicyTest
```

수동 확인:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

그 뒤 `/games/location/start`에서 실제로 한 판을 시작합니다.

## 17. 산출물 체크리스트

- 위치 게임을 한 판 끝까지 플레이할 수 있다
- 오답이면 같은 stage를 다시 시도한다
- 하트가 모두 사라지면 `GAME_OVER`가 된다
- 다른 브라우저는 세션에 접근하지 못한다
- stale submit이 `409`로 막힌다
- result는 terminal 상태에서만 열린다

## 18. 글 종료 체크포인트

- 왜 위치 게임은 WebGL 효과보다 server-owned state machine이 더 중요한가
- 왜 `Session / Stage / Attempt` 분리가 endless run과 retry 설명에 필요한가
- 왜 ownership과 stale submit이 게임 설계의 일부인가
- 왜 브라우저는 입력만 받고 정답과 점수는 서버가 가져가야 하는가

## 19. 자주 막히는 지점

- 지구본 클릭 좌표가 있으니 정답 판정도 프런트에서 하려는 것
- stage와 attempt를 합쳐 버려 retry와 회고를 약하게 만드는 것
- session 접근 제어를 UI 조건문으로만 처리하는 것
- result를 진행 중 상태에서도 열어 학습용 정답표처럼 만들어 버리는 것
- 위치 게임만 특별 취급해 공통 game contract를 깨는 것
