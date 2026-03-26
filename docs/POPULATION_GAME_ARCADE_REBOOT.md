# Population Game Arcade Reboot

## 목적

이 문서는 현재 `국가 인구수 맞추기 Level 1`을 단순한 4지선다 퀴즈에서 `짧고 반복 플레이 가능한 아케이드형 서버 주도 게임`으로 다시 설계하기 위한 기준 문서다.

핵심은 세 가지다.

- 고정 5문제 제출형 구조를 버리고 `하트 기반 Stage 진행형`으로 바꾼다.
- 숫자 나열형 보기에서 벗어나 `빠르게 판단 가능한 인구 규모 문제`로 다시 정의한다.
- 위치 게임과 같은 수준으로 `HUD, 자동 진행, 게임오버, 재시작` 흐름을 맞춘다.

## 현재 구현 상태

- 완료:
  - `BaseGameSession` 기반 공통 세션 구조 재사용
  - `PopulationGameSession`, `PopulationGameStage`, `PopulationGameAttempt` 도메인 1차 구현
  - `POST /sessions`, `GET /state`, `POST /answer`, `POST /restart`, `GET /result` API 구현
  - 하트 3개, 같은 Stage 재시도, `GAME_OVER`, 같은 `sessionId` 재시작 구현
  - endless Stage 생성과 단계별 난이도 / 점수 정책 1차 구현
  - `PopulationGameOptionGenerator`로 인구 규모 구간 4개 생성
  - SSR 시작 / 플레이 / 결과 페이지, HUD, 게임오버 모달 1차 구현
  - 옵션 생성 단위 테스트와 상태 전이 통합 테스트 구현
- 남은 문제:
  - HUD와 결과 화면의 아케이드 연출은 아직 위치 게임보다 단순하다.
  - 현재 구간 경계가 모든 국가 분포에서 충분히 직관적인지는 더 검증이 필요하다.

## 1. 왜 다시 설계하는가

현재 인구수 게임은 구조적으로는 맞다.

- 서버가 문제를 만든다.
- 서버가 정답을 판정한다.
- 세션과 라운드를 저장한다.

하지만 실제 플레이 감각은 약하다.

- 고정 5문제라 긴장감이 적다.
- 틀려도 하트나 패널티가 없어 아케이드 감각이 없다.
- 정답 제출 뒤 사용자가 다시 버튼을 눌러야 다음 문제로 간다.
- 보기 숫자가 모두 실제 인구수라 빠르게 읽기 어렵다.
- 결과 화면도 게임 디브리프보다 표 중심 리포트에 가깝다.

즉, 지금 상태는 `공통 구조 검증용 퀴즈`로는 충분했지만, 위치 게임처럼 대표 모드 수준의 완성도는 아니다.

## 2. 리부트 목표

### 게임 경험 목표

- 한 문제를 빠르게 보고 바로 판단할 수 있어야 한다.
- 틀리면 하트가 줄고 같은 Stage를 다시 시도해야 한다.
- 맞히면 짧은 성공 피드백 뒤 자동으로 다음 Stage로 넘어가야 한다.
- 하트가 모두 사라지면 게임오버가 되어야 한다.
- 게임오버 후에는 같은 `sessionId`로 다시 시작하거나 홈으로 돌아갈 수 있어야 한다.
- Stage가 올라갈수록 난이도와 점수가 함께 올라가야 한다.

### 포트폴리오 목표

- 위치 게임과 같은 서버 주도 상태 관리 구조를 다른 모드에도 적용했다는 점을 보여준다.
- `왜 위치 게임과 완전히 같은 구조는 아니고, 어디를 공유하고 어디를 다르게 두는가`를 설명할 수 있어야 한다.
- 숫자 퀴즈도 단순 CRUD가 아니라 상태 전이가 있는 게임으로 설계했다는 점을 강조할 수 있어야 한다.

### 비주얼 목표

- 위치 게임과 같은 `cold space HUD` 톤을 유지한다.
- 현재의 폼 제출형 레이아웃을 `게임 HUD + 결과 오버레이 + 게임오버 모달` 구조로 재편한다.

## 3. 새 게임 정의

### Level 1 방향

Level 1은 `정확한 인구 숫자 4개 보기`보다 `인구 규모를 빠르게 맞히는 게임`이 되어야 한다.

권장 입력 방식:

- 서버가 국가 1개를 낸다.
- 사용자는 보기 4개 중 `가장 맞는 인구 규모`를 고른다.
- 보기는 숫자 자체보다 `사람이 빨리 읽을 수 있는 구간 또는 압축 표현`으로 준다.

예시:

- `1천만 미만`
- `1천만 ~ 5천만`
- `5천만 ~ 1억`
- `1억 이상`

또는

- `약 2,700만`
- `약 5,200만`
- `약 1.2억`
- `약 3.4억`

현재 public 구현 기준은 `구간형 Level 1`이다.

이유:

- Level 1은 반응 속도와 직관이 중요하다.
- 지금처럼 정밀한 숫자 4개를 한 번에 읽는 부담을 줄일 수 있다.

## 4. 최종 게임 루프

### 세션 시작

- 사용자가 닉네임을 입력하고 시작한다.
- 서버는 세션을 만들고 아래 기본값을 준다.
  - `livesRemaining = 3`
  - `currentStageNumber = 1`
  - `totalScore = 0`
  - `status = IN_PROGRESS`

### 한 Stage의 흐름

1. 서버가 국가 1개와 보기 4개를 만든다.
2. 프론트는 상단 HUD에 Stage, Score, Hearts를 보여준다.
3. 사용자는 보기 하나를 고른다.
4. 제출하면 서버가 정답 여부와 점수를 계산한다.
5. 정답이면:
   - 점수를 준다.
   - Stage를 `CLEARED`로 만든다.
   - 서버가 다음 Stage를 생성한다.
   - 짧은 성공 연출 뒤 자동으로 다음 Stage로 이동한다.
6. 오답이면:
   - Attempt를 남긴다.
   - 하트를 1개 줄인다.
   - 같은 Stage를 다시 시도한다.
7. 하트가 0이면:
   - 세션을 `GAME_OVER`로 종료한다.
   - 탈락 모달을 보여준다.
   - 같은 세션으로 재시작하거나 홈으로 돌아갈 수 있다.

### 종료 조건

- 하트가 모두 사라지면 `GAME_OVER`
- 현재 Level 1은 위치 게임과 같은 `endless run` 구조를 목표로 한다.

## 5. 난이도 정책 초안

현재는 난이도가 사실상 없다.

- 라운드 수는 5로 고정
- 보기 생성은 인구가 비슷한 국가를 섞는 정도
- 점수는 맞으면 100, 틀리면 0

리부트 후에는 아래 축으로 난도를 올리는 것이 좋다.

### Stage 초반

- 세계적으로 익숙한 국가 위주
- 인구 차이가 큰 보기
- 구간 폭이 넓은 쉬운 보기

### Stage 중반

- 중간 규모 국가 확대
- 비슷한 인구대 국가를 보기로 섞음
- 보기 차이를 줄여 오답 유도

### Stage 후반

- 덜 익숙한 국가 확대
- 비슷한 인구 규모 국가끼리 출제

권장 설명 방식:

- `국가 풀 난도`
- `보기 간 인구 차이 난도`
- `표현 방식 난도`

즉, 난도는 단순히 “국가가 어려움” 하나가 아니라 `문제 대상 + 보기 설계 + 표현 방식`의 조합으로 본다.

## 6. 점수 정책 초안

위치 게임과 같은 방식으로 `Stage 진행`과 `리스크 관리`를 같이 반영해야 한다.

권장 공식:

- `baseScore = 80 + ((stageNumber - 1) * 15)`
- `attemptBonus`
  - 첫 시도 정답: `+30`
  - 두 번째 시도 정답: `+10`
  - 세 번째 이상 정답: `+0`
- `lifeBonus = livesRemaining * 10`
- `streakBonus`
  - 3연속 정답마다 `+20`

의도:

- 뒤 Stage일수록 점수가 커진다.
- 적은 시도로 맞히면 보상이 커진다.
- 하트를 잘 지키면 점수가 오른다.
- 연속 정답으로 퀴즈 게임다운 템포를 만든다.

## 7. 백엔드 도메인 재설계

현재 `PopulationGameSession + PopulationGameRound`는 1회 제출형 퀴즈에는 맞지만, 재시도형 아케이드 게임으로는 부족하다.

권장 구조:

### 7.1 Session

`PopulationGameSession`

- `id`
- `playerNickname`
- `status`
  - `READY`
  - `IN_PROGRESS`
  - `GAME_OVER`
  - `FINISHED`
- `currentStageNumber`
- `clearedStageCount`
- `totalScore`
- `livesRemaining`
- `currentStreak`
- `startedAt`
- `finishedAt`

### 7.2 Stage

`PopulationGameStage`

- `id`
- `sessionId`
- `stageNumber`
- `targetCountryIso3Code`
- `targetCountryName`
- `targetPopulation`
- `populationYear`
- `difficultyTier`
- `optionPayload`
- `correctOptionNumber`
- `status`
  - `PENDING`
  - `CLEARED`
  - `FAILED`
- `attemptCount`
- `awardedScore`
- `clearedAt`

### 7.3 Attempt

`PopulationGameAttempt`

- `id`
- `stageId`
- `attemptNumber`
- `selectedOptionNumber`
- `selectedOptionLabel`
- `correct`
- `livesRemainingAfter`
- `awardedScore`
- `attemptedAt`

### 왜 Attempt가 필요한가

- 같은 Stage에서 여러 번 틀릴 수 있다.
- 어떤 오답을 골랐는지 기록해야 한다.
- 연속 정답 / 오답 패턴을 설명하기 쉽다.
- 나중에 “어떤 인구대에서 자주 틀리는가” 분석이 가능하다.

## 8. API 재설계 초안

현재:

- `POST /sessions`
- `GET /state`
- `POST /answer`
- `POST /restart`
- `GET /result`

이 1차 틀은 이미 구현됐다. 남은 작업은 응답 표현과 HUD polish를 더 다듬는 것이다.

### 세션 시작

- `POST /api/games/population/sessions`

### 현재 Stage 조회

- `GET /api/games/population/sessions/{sessionId}/state`

응답 예시:

- `stageNumber`
- `difficultyLabel`
- `clearedStageCount`
- `totalScore`
- `livesRemaining`
- `targetCountryName`
- `populationYear`
- `options`

### 답 제출

- `POST /api/games/population/sessions/{sessionId}/answer`

응답 예시:

- `outcome = CORRECT | WRONG | GAME_OVER | FINISHED`
- `awardedScore`
- `totalScore`
- `livesRemaining`
- `nextStageNumber`
- `correctOptionNumber`
- `correctLabel`

### 같은 세션 재시작

- `POST /api/games/population/sessions/{sessionId}/restart`

위치 게임과 같은 규칙으로 같은 세션을 초기화하는 쪽이 설명 가능하다.

## 9. 프론트 UX 재설계

현재는 `문제 카드 + 보기 카드 + 제출 버튼 + 다음 라운드 버튼` 구조다.

새 구조는 이쪽이 맞다.

- 상단 HUD
  - `Stage`
  - `Score`
  - `♥ ♥ ♥`
  - `Streak`
- 중앙 문제 카드
  - 국가명
  - 기준 연도
  - 짧은 지시문
- 보기 영역
  - 큰 카드 4개
  - 선택 상태가 명확한 버튼형
- 피드백 오버레이
  - `Correct`
  - `Wrong`
  - `+점수`
- 게임오버 모달
  - 탈락 안내
  - 같은 세션 다시 시작
  - 홈으로 이동

### 버릴 것

- `다음 라운드 불러오기` 버튼
- 테이블형 결과 중심 흐름
- 지나치게 설명형인 안내 문구

## 10. 개발 순서

이 순서로 가는 것이 가장 좋다.

1. 리부트 규칙 문서 확정
2. `Stage / Attempt / Lives` 구조로 도메인 재설계
3. `GET state`, `POST answer`, `POST restart` API 재설계
4. 점수 정책 / 난이도 정책 강화
5. 플레이 화면을 HUD 구조로 재작성
6. 자동 다음 Stage / 게임오버 모달 구현
7. 결과 화면을 디브리프형으로 재작성
8. 테스트 보강

## 11. 반드시 설명할 수 있어야 하는 질문

- 왜 5문제 고정 퀴즈를 버리고 endless Stage 구조로 바꾸는가?
- 왜 현재의 `PopulationGameRound`만으로는 부족한가?
- 왜 Level 1은 정확 숫자보다 구간형 또는 압축 표현이 더 적합한가?
- 왜 인구수 게임도 하트와 재시도를 서버가 관리해야 하는가?
- 왜 `다음 라운드 불러오기` 버튼을 제거하는가?

## 12. 다음 구현 단위

다음 실제 구현은 아래 한 조각으로 자르는 것이 맞다.

- `PopulationGame 리부트 2차`
  - 구간 경계와 난이도 체감 조정
  - HUD / 결과 화면 polish
  - 연속 정답 보너스와 연출 보강

현재는 상태 모델이 한 번 고정됐고, 다음 단계는 “숫자 보기 자체를 어떤 사용자 경험으로 보여줄 것인가”를 더 선명하게 만드는 것이다.
