# [Spring Boot 게임 플랫폼 포트폴리오] 08. 인구수 퀴즈로 4지선다 endless arcade loop를 어떻게 확장했는가

## 1. 이번 글에서 풀 문제

위치 게임이 잘 돌아간다고 해서 공통 게임 루프가 완성된 것은 아닙니다.  
WorldMap이 진짜 플랫폼이라면, 입력 방식이 전혀 다른 quiz형 게임에도 같은 session contract가 자연스럽게 들어가야 합니다.

이 글은 아래 문제를 닫습니다.

- 왜 population quiz를 두 번째 대표 vertical slice로 택했는가
- 왜 절대 숫자 입력이 아니라 `population scale band`를 정답 단위로 택했는가
- 왜 보기 생성과 정답 판정은 서버가 담당하고, 프런트는 선택과 피드백만 담당해야 하는가
- 왜 quiz형 게임에서도 `state -> answer -> next stage / game over -> result` loop를 유지해야 하는가

## 2. 최종 도착 상태

이 글이 끝났을 때 population game은 아래처럼 동작해야 합니다.

- `GET /games/population/start`가 시작 페이지를 SSR로 제공한다
- `POST /api/games/population/sessions`가 guest/member 기준으로 세션과 첫 stage를 만든다
- `GET /api/games/population/sessions/{id}/state`가 현재 stage, 보기 4개, difficulty label, lives, score를 반환한다
- `POST /api/games/population/sessions/{id}/answer`가 선택한 보기 번호를 서버에서 판정한다
- 정답이면 다음 stage가 자동 준비되고, 오답이면 같은 stage를 하트 1개 줄인 채 다시 시도한다
- 하트를 모두 잃으면 `GAME_OVER`가 되고, restart로 같은 session을 Stage 1부터 다시 시작할 수 있다
- terminal 상태에서는 `/games/population/result/{sessionId}`에서 stage/attempt 회고가 가능하다

즉, population game은 "퀴즈 하나 추가"가 아니라 **공통 endless game loop가 지도 UI 밖에서도 재사용됨을 증명하는 단계**입니다.

## 3. 먼저 알아둘 개념

### 3-1. scale band

인구 절댓값을 직접 맞히게 하지 않고, 인구 규모 구간을 정답 단위로 씁니다.

예:

- `1천만 미만`
- `1천만 ~ 3천만`
- `3천만 ~ 7천만`
- ...

### 3-2. option generator

보기 4개는 프런트가 만드는 것이 아니라 서버가 stage마다 생성합니다.

### 3-3. endless quiz loop

quiz형 게임이어도 rhythm은 location과 같습니다.

- 정답: 다음 stage
- 오답: 같은 stage 재시도
- lives 0: game over

### 3-4. accessible game-over modal

population game은 later stage accessibility hardening 대상이기도 합니다.  
즉, terminal UI도 keyboard contract를 지켜야 합니다.

## 4. 이번 글에서 다룰 파일

- [PopulationGameSession.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java)
- [PopulationGameStage.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameStage.java)
- [PopulationGameAttempt.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameAttempt.java)
- [PopulationGameSessionRepository.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameSessionRepository.java)
- [PopulationGameStageRepository.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameStageRepository.java)
- [PopulationGameAttemptRepository.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameAttemptRepository.java)
- [PopulationGameService.java](../src/main/java/com/worldmap/game/population/application/PopulationGameService.java)
- [PopulationGameDifficultyPolicy.java](../src/main/java/com/worldmap/game/population/application/PopulationGameDifficultyPolicy.java)
- [PopulationGameScoringPolicy.java](../src/main/java/com/worldmap/game/population/application/PopulationGameScoringPolicy.java)
- [PopulationGameOptionGenerator.java](../src/main/java/com/worldmap/game/population/application/PopulationGameOptionGenerator.java)
- [PopulationScaleBandCatalog.java](../src/main/java/com/worldmap/game/population/application/PopulationScaleBandCatalog.java)
- [PopulationOptionLabelFormatter.java](../src/main/java/com/worldmap/game/population/application/PopulationOptionLabelFormatter.java)
- [PopulationGameApiController.java](../src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java)
- [PopulationGamePageController.java](../src/main/java/com/worldmap/game/population/web/PopulationGamePageController.java)
- [PopulationGameStateView.java](../src/main/java/com/worldmap/game/population/application/PopulationGameStateView.java)
- [PopulationGameAnswerView.java](../src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java)
- [population-game/play.html](../src/main/resources/templates/population-game/play.html)
- [population-game.js](../src/main/resources/static/js/population-game.js)
- [PopulationGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java)
- [PopulationGameOptionGeneratorTest.java](../src/test/java/com/worldmap/game/population/application/PopulationGameOptionGeneratorTest.java)

## 5. 핵심 도메인 모델 / 상태

### 5-1. `PopulationGameSession`

[PopulationGameSession.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java)는 `BaseGameSession`을 상속하고 `livesRemaining`만 추가합니다.

핵심 메서드:

- `planNextStage`
- `clearCurrentStage`
- `recordWrongAttempt`
- `restart`

즉, 한 판 전체 상태는 공통 session contract를 그대로 따릅니다.

### 5-2. `PopulationGameStage`

[PopulationGameStage.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameStage.java)는 문제 하나의 상태입니다.

필드:

- `stageNumber`
- `countryId`
- `countryIso3Code`
- `targetCountryName`
- `targetPopulation`
- `populationYear`
- option 4개 lower bound
- `correctOptionNumber`
- `status`
- `attemptCount`
- `awardedScore`
- `clearedAt`

즉, stage는 "어느 나라가 문제였고, 어떤 보기 4개가 제시됐으며, 몇 번 만에 풀었는가"를 담습니다.

### 5-3. `PopulationGameAttempt`

[PopulationGameAttempt.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameAttempt.java)는 제출 한 번의 기록입니다.

필드:

- `attemptNumber`
- `selectedOptionNumber`
- `selectedPopulation`
- `correct`
- `livesRemainingAfter`
- `attemptedAt`

이 엔티티가 있어야 result 화면에서 "1차 오답 / 2차 정답" 같은 회고가 가능합니다.

### 5-4. `PopulationScaleBandCatalog`

[PopulationScaleBandCatalog.java](../src/main/java/com/worldmap/game/population/application/PopulationScaleBandCatalog.java)는 현재 8개 구간을 가집니다.

- `1천만 미만`
- `1천만 ~ 3천만`
- `3천만 ~ 7천만`
- `7천만 ~ 1억 5천만`
- `1억 5천만 ~ 3억`
- `3억 ~ 6억`
- `6억 ~ 10억`
- `10억 이상`

이 catalog가 "정답 구간"의 source of truth입니다.

## 6. 설계 구상

### 왜 population game이 두 번째 vertical slice인가

위치 게임은 WorldMap의 정체성을 보여 주지만 입력 자체가 특수합니다.  
반면 population game은 전형적인 4지선다 UI입니다.

즉, population game은 아래 질문을 검증하기에 좋습니다.

- 공통 session/stage/attempt loop가 특정 UI에 종속되지 않는가
- 4-choice quiz에서도 서버 주도 정답 판정이 자연스러운가
- 점수, lives, restart, result contract가 그대로 재사용되는가

### 왜 절대 숫자 맞히기가 아니라 scale band인가

절댓값을 직접 맞히게 하면 게임성이 크게 떨어집니다.

- 지나치게 어려움
- 오답 원인 설명이 약함
- 보기 생성이 불안정함

반면 구간 기반 문제로 바꾸면 아래가 쉬워집니다.

- 보기 4개 생성
- 오답 피드백 설명
- 난이도 단계화
- "가장 가까운 규모"라는 직관적 목표 설정

### 왜 option generation을 서버에 두는가

보기 생성 규칙은 단순 UI 로직이 아니라 게임 규칙입니다.

- target country population이 어느 band인가
- band window를 어디서 자를 것인가
- correct option number를 무엇으로 볼 것인가

이걸 프런트로 밀면 later stage에서 stale submit 방어, result 회고, 재현성 설명이 모두 약해집니다.

### 왜 같은 endless loop를 유지하는가

만약 population game만 "고정 8문제 끝" 같은 별도 규칙을 쓰면, 프로젝트는 플랫폼이 아니라 mini apps 모음처럼 보입니다.

WorldMap은 quiz형 게임에도 같은 rhythm을 유지합니다.

- 같은 session contract
- 같은 terminal result contract
- 같은 restart contract
- 같은 leaderboard 반영 조건

## 7. 코드 설명

### 7-1. `PopulationGameService`: 이 글의 중심

[PopulationGameService.java](../src/main/java/com/worldmap/game/population/application/PopulationGameService.java)는 population game의 실제 규칙을 담당합니다.

주요 책임:

- guest/member 세션 시작
- 국가 pool 정렬
- stage 생성
- difficulty plan 계산
- 보기 4개 생성
- answer 판정
- session/stage/attempt 상태 전이
- terminal result 조립
- leaderboard 반영

이 로직을 controller가 아니라 service에 두는 이유는, HTTP 요청보다 게임 규칙이 중심이기 때문입니다.

### 7-2. 시작 로직: `startGuestGame`, `startMemberGame`

service는 guest와 member를 구분하지만, 결국 내부적으로 `startGame(...)`으로 합쳐 처리합니다.

흐름:

1. 모든 국가를 인구순으로 정렬
2. 최소 4개 국가가 있는지 확인
3. `PopulationGameSession.ready(...)`
4. 첫 stage 생성
5. `session.startGame(LocalDateTime.now())`
6. play page URL 포함 `PopulationGameStartView` 반환

즉, 세션 시작과 첫 문제 생성이 한 transaction 안에서 닫힙니다.

### 7-3. `PopulationGameDifficultyPolicy`

[PopulationGameDifficultyPolicy.java](../src/main/java/com/worldmap/game/population/application/PopulationGameDifficultyPolicy.java)는 stage 번호에 따라 출제 pool을 넓힙니다.

현재 규칙:

- `1~5`: `Band A · 초대형 국가`, 최대 24개 후보
- `6~12`: `Band B · 지역 확장`, 최대 60개 후보
- `13~24`: `Band C · 글로벌`, 최대 110개 후보
- `25~40`: `Band D · 고난도`, 최대 160개 후보
- 그 이후: `Band E · 전체 국가`

즉, 난이도는 보기 수가 아니라 **출제 후보군의 크기**로 조절합니다.

### 7-4. `PopulationGameOptionGenerator`

[PopulationGameOptionGenerator.java](../src/main/java/com/worldmap/game/population/application/PopulationGameOptionGenerator.java)는 target country의 인구를 기준으로 정답 band를 찾고, 그 주변 4-band window를 구성합니다.

핵심 규칙:

- band는 catalog 기준
- correct band index를 찾는다
- 4개 window가 벗어나지 않도록 시작점을 clamp
- option은 각 band의 `lowerBoundInclusive`
- `correctOptionNumber`는 1~4 중 하나

즉, 보기 생성은 무작위 숫자 4개 뿌리기가 아니라 **정답 근처의 의미 있는 규모 구간 4개**를 만드는 과정입니다.

### 7-5. `PopulationGameScoringPolicy`

[PopulationGameScoringPolicy.java](../src/main/java/com/worldmap/game/population/application/PopulationGameScoringPolicy.java)는 다음 규칙을 가집니다.

- 오답: `0점`
- 정답 base score: `90 + (stageNumber - 1) * 15`
- life bonus: `livesRemaining * 10`
- attempt bonus:
  - 1회차 정답: `30`
  - 2회차 정답: `10`
  - 그 이후: `0`

즉, 후반 stage일수록, lives를 많이 남길수록, 빨리 맞힐수록 점수가 큽니다.

### 7-6. `PopulationGameStateView`: stale submit 방어용 정보 포함

[PopulationGameStateView.java](../src/main/java/com/worldmap/game/population/application/PopulationGameStateView.java)는 단순 화면 정보만 주지 않습니다.

포함 값:

- `stageId`
- `expectedAttemptNumber`

이 두 값이 있어야 answer API에서 stale submit을 공통 guard로 막을 수 있습니다.

### 7-7. `PopulationGameAnswerView`: 프런트가 다음 행동을 결정할 최소 정보

[PopulationGameAnswerView.java](../src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java)는 아래를 함께 돌려줍니다.

- 정오답 여부
- 선택한 보기와 정답 보기
- awarded score
- total score
- lives remaining
- next stage info
- game status
- `outcome`
- `resultPageUrl`

즉, 프런트는 이 응답으로 overlay, feedback, redirect, game-over modal을 결정하지만 **정답 계산 자체는 하지 않습니다**.

### 7-8. `population-game.js`와 `play.html`

[population-game/play.html](../src/main/resources/templates/population-game/play.html)은 SSR shell을 만들고, `population-game.js`는 다음만 담당합니다.

- start form submit
- current state fetch
- 보기 선택 UI
- answer submit
- overlay / feedback / game-over modal
- restart 후 focus 복귀

현재 play 템플릿은 accessible game-over modal shell도 포함합니다.

- `role="dialog"`
- `aria-describedby="population-game-over-summary"`
- `tabindex="-1"`

그리고 JS는 `window.createGameOverModalController(...)`를 사용해 focus trap과 keyboard contract를 공통 helper로 재사용합니다.

## 8. 요청 흐름 / 상태 변화

### 8-1. 세션 시작

```text
GET /games/population/start
-> PopulationGamePageController.startPage()
-> population-game/start SSR

POST /api/games/population/sessions
-> member면 startMemberGame
-> guest면 guestSessionKey 생성 후 startGuestGame
-> session 저장
-> 첫 stage 생성
-> playPageUrl 반환
```

### 8-2. 현재 state 조회

```text
GET /api/games/population/sessions/{id}/state
-> ownership 검사
-> 현재 stage 조회
-> difficulty plan 계산
-> options + expectedAttemptNumber 포함 state view 반환
```

### 8-3. answer 제출

```text
POST /api/games/population/sessions/{id}/answer
-> ownership 검사
-> current stage 일치 여부 확인
-> GameSubmissionGuard로 stale submit 검증
-> scoring policy로 정오답/점수 계산
-> attempt 저장
-> 정답이면 다음 stage 생성 + session advance
-> 오답이면 lives 차감, 필요 시 GAME_OVER
-> terminal이면 leaderboard 기록
```

### 8-4. result 조회

```text
GET /games/population/result/{sessionId}
-> ownership 검사
-> terminal 상태 확인
-> stage + attempt 회고 view 조립
-> SSR result 페이지 렌더링
```

## 9. 실패 케이스 / 예외 처리

- 국가가 4개 미만이면: 게임 시작 자체를 거부
- 보기 번호가 1~4 범위를 벗어나면: `400`
- 현재 stage 번호와 payload가 다르면: `409`
- stale `stageId`, `expectedAttemptNumber`면: `409`
- 진행 중이 아닌 세션에 answer를 보내면: `409`
- 다른 브라우저가 세션을 열면: `403`
- 진행 중에 result를 열면: `404`

즉, population game의 진짜 복잡성은 보기 UI보다 **상태 계약과 제출 무결성**에 있습니다.

## 10. 테스트로 검증하기

핵심 테스트는 두 축입니다.

### 10-1. [PopulationGameOptionGeneratorTest.java](../src/test/java/com/worldmap/game/population/application/PopulationGameOptionGeneratorTest.java)

무엇을 검증하나:

- 보기 4개가 생성되는지
- 중복이 없는지
- 정답 band lower bound가 옵션 안에 포함되는지
- `correctOptionNumber`가 1~4 사이인지

즉, quiz의 핵심인 보기 생성 품질을 unit test로 잡습니다.

### 10-2. [PopulationGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java)

무엇을 검증하나:

- 5 stage를 넘어 계속 이어지는 endless run
- 오답 시 같은 stage 재시도와 life 감소
- accessible game-over modal shell 존재
- stale duplicate wrong answer는 extra life를 먹지 않고 `409`
- stage advance 뒤 duplicate correct answer도 `409`
- 3번 틀리면 `GAME_OVER`
- restart는 같은 session을 재사용하되 progress를 초기화
- result 페이지가 cleared stage의 선택/정답 세부를 적절히 숨기는지

이 테스트 클래스 하나가 arcade rhythm, integrity, accessibility regression을 같이 잡습니다.
다만 여기서 accessibility는 SSR shell과 서버 루프 기준입니다.
실제 `Tab / Shift+Tab / Escape / restart 후 focus return` browser contract는 [BrowserSmokeE2ETest.java](../src/test/java/com/worldmap/e2e/BrowserSmokeE2ETest.java)의 population modal E2E와 함께 읽어야 닫힙니다.

실행 명령:

```bash
./gradlew test \
  --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest \
  --tests com.worldmap.game.population.application.PopulationGameOptionGeneratorTest
```

## 11. 회고

population game은 "위치 게임 외의 두 번째 게임"이 아니라, **공통 game platform 가설의 검증 단계**였습니다.

이 구조의 장점:

- 지도 UI가 아닌 quiz UI에서도 같은 loop를 재사용한다
- 보기 생성과 정답 판정을 서버가 들고 간다
- stale submit, restart, result, leaderboard까지 같은 계약으로 설명된다

### 현재 구현의 한계

- scale band 경계는 앞으로 더 정교하게 조정할 수 있다
- 오답 피드백의 서술과 시각 표현은 더 다듬을 여지가 있다
- 국가 pool 난이도와 통계적 밸런싱은 later tuning 대상이다
- option generator와 flow test가 현재 루프를 강하게 고정하지만, 구간 경계가 실제 사용자 체감 난이도와 가장 잘 맞는지까지는 자동으로 증명하지 않는다

## 12. 취업 포인트

### 12-1. 1문장 답변

인구수 게임은 위치 게임의 서버 주도 `Session / Stage / Attempt` 루프를 4지선다 퀴즈에도 그대로 재사용하고, 보기 생성과 정답 판정을 모두 서버에 둬 공통 게임 플랫폼 설계를 증명한 사례입니다.

### 12-2. 30초 답변

population game은 `PopulationGameSession`, `PopulationGameStage`, `PopulationGameAttempt`로 한 판, 문제 하나, 제출 한 번을 분리하고, 서비스가 인구순 국가 pool에서 출제 대상을 고른 뒤 `PopulationScaleBandCatalog`와 `PopulationGameOptionGenerator`로 보기 4개를 만듭니다. 브라우저는 선택과 피드백만 담당하고, 정답 판정, 점수 계산, lives 감소, 다음 stage 생성, stale submit 방어는 전부 서버가 맡습니다. 그래서 위치 게임과 입력 UI는 달라도 같은 endless arcade loop를 공유한다고 설명할 수 있습니다.

### 12-3. 예상 꼬리 질문

- 왜 절대 인구 수치가 아니라 구간 맞히기로 만들었나요?
- 보기 4개 생성은 왜 서버가 해야 하나요?
- 위치 게임과 population game은 무엇을 공유하고 무엇이 다르나요?
- stale submit 방어는 quiz형 게임에서도 왜 필요한가요?

## 13. 시작 상태

- location game vertical slice는 있지만, quiz형 4-choice game이 같은 contract를 재사용하는지는 검증되지 않은 상태
- stage/attempt/restart/result 규칙을 다른 입력 방식에도 그대로 적용할 수 있는지 불명확한 상태

## 14. 이번 글에서 바뀌는 파일

- `src/main/java/com/worldmap/game/population/domain/**`
- `src/main/java/com/worldmap/game/population/application/**`
- `src/main/java/com/worldmap/game/population/web/**`
- `src/main/resources/templates/population-game/**`
- `src/main/resources/static/js/population-game.js`
- `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
- `src/test/java/com/worldmap/game/population/application/PopulationGameOptionGeneratorTest.java`

## 15. 구현 체크리스트

1. `PopulationGameSession`, `Stage`, `Attempt` 엔티티를 만든다
2. stage에 option 4개와 correct option number를 저장한다
3. `PopulationScaleBandCatalog`를 정의한다
4. `PopulationGameOptionGenerator`를 구현한다
5. difficulty/scoring policy를 분리한다
6. service에서 start/state/answer/restart/result를 구현한다
7. SSR page와 JS를 연결한다
8. flow/integrity/accessibility 테스트를 추가한다

## 16. 실행 / 검증 명령

```bash
./gradlew test \
  --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest \
  --tests com.worldmap.game.population.application.PopulationGameOptionGeneratorTest
```

수동으로 확인하려면:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

그 뒤 `/games/population/start`에서 실제로 한 판 시작합니다.

## 17. 산출물 체크리스트

- population game을 한 판 끝까지 플레이할 수 있다
- stage마다 보기 4개가 서버에서 생성된다
- 정답이면 다음 stage, 오답이면 같은 stage 재시도 루프가 유지된다
- 3번 틀리면 `GAME_OVER`가 된다
- restart와 result contract가 동작한다
- stale submit이 `409`로 막힌다

## 18. 글 종료 체크포인트

- 왜 population game은 두 번째 vertical slice로 적합한가
- 왜 quiz형 게임에도 같은 endless loop가 필요한가
- 왜 option generation을 서버에 둬야 하는가
- 왜 state payload에 `stageId`, `expectedAttemptNumber`가 포함돼야 하는가

## 19. 자주 막히는 지점

- 보기 생성과 정답 인덱스를 프런트에서 계산하는 것
- population game만 별도의 고정형 quiz로 만들어 공통 loop를 깨는 것
- stage와 attempt를 합쳐 버려 result 회고와 stale submit 방어를 약하게 만드는 것
- result 페이지를 진행 중 상태에서도 열 수 있게 두는 것
