# 국기 게임 난이도 단계와 결과 카피를 플레이어 기준으로 다시 정리하기

## 왜 이 조각이 필요한가

국기 게임은 이미 동작하고 있었다.

asset pool도 36개로 넓혔고,
distractor fallback도 `same continent -> 인접 대륙 -> 전체 pool`으로 정리한 상태였다.

하지만 플레이어가 보는 문구는 아직 내부 구현 느낌이 남아 있었다.

- `Pool A · 대표 국기`
- `Pool B · 자산 확장`
- `Pool C · 전체 국기`

이 라벨은 개발자에게는 의미가 있지만,
플레이어 입장에서는 지금 무엇이 쉬워지고 무엇이 어려워지는지 바로 읽기 어렵다.

게다가 초반 라운드가 실제로 더 쉬운 문제만 내는지 코드로도 분명하게 드러나지 않았다.

이번 조각의 목적은 이 둘을 같이 정리하는 것이다.

## 이번에 바뀐 파일

### 난이도 정책과 서비스

- [FlagGameDifficultyPlan.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameDifficultyPlan.java)
- [FlagGameDifficultyPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameDifficultyPolicy.java)
- [FlagGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameService.java)

### state / answer / result read model

- [FlagGameStateView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameStateView.java)
- [FlagGameAnswerView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameAnswerView.java)
- [FlagGameStageResultView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameStageResultView.java)

### 화면과 JS

- [play.html](/Users/alex/project/worldmap/src/main/resources/templates/flag-game/play.html)
- [result.html](/Users/alex/project/worldmap/src/main/resources/templates/flag-game/result.html)
- [flag-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/flag-game.js)

### 테스트

- [FlagGameDifficultyPolicyTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagGameDifficultyPolicyTest.java)
- [FlagGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/FlagGameFlowIntegrationTest.java)

## 난이도 단계를 어떻게 바꿨나

이제 국기 게임 난이도는 아래 세 단계다.

1. `기본 라운드`
2. `확장 라운드`
3. `전체 라운드`

중요한 건 이름만 바꾼 게 아니라는 점이다.

[FlagGameDifficultyPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameDifficultyPolicy.java)는
이제 각 단계마다

- 플레이어 라벨
- 한 줄 가이드
- candidate pool 크기
- 초반 안정화 규칙 여부

를 같이 돌려준다.

## 초반 라운드를 실제로 더 쉽게 만든 방법

핵심은 [FlagGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameService.java)의
`selectEligibleTargetCountries()`다.

초반 `기본 라운드`에서는
same-continent distractor가 최소 3개 가능한 대륙만 먼저 target pool로 쓴다.

현재 36개 pool 기준으로 보면,
같은 대륙 distractor가 충분한 쪽은 대략 이런 그룹이다.

- 유럽
- 아시아
- 아프리카
- 남미

반대로 북미나 오세아니아는 같은 대륙 후보 수가 적어서,
초반 라운드 target으로는 우선 출제하지 않는다.

즉, 예전에는 “초반이 쉽다”고 말만 할 수 있었다면,
이제는 실제로 초반 target selection 규칙이 더 쉬운 쪽으로 고정됐다.

## 왜 이 로직이 컨트롤러가 아니라 서비스 / 정책에 있어야 하나

이건 UI 텍스트 문제가 아니라
`무슨 나라를 언제 문제로 내느냐`에 대한 게임 규칙이다.

따라서

- 난이도 단계 정의는 [FlagGameDifficultyPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameDifficultyPolicy.java)
- 실제 출제 대상 pool 선택은 [FlagGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameService.java)

가 맡아야 한다.

컨트롤러는 여전히 요청을 서비스에 넘기기만 한다.

즉, 이번 조각은 “문구 polish”처럼 보여도
실제로는 서버 도메인 규칙과 read model 정리 작업이다.

## state / answer / result에는 무엇이 추가됐나

이번에는 player-facing 설명을 위해 read model도 함께 바꿨다.

- `GET /state`
  - `difficultyGuide` 추가
- `POST /answer`
  - `nextDifficultyGuide` 추가
- `GET /result`
  - stage별 `difficultyLabel` 추가

그래서 플레이 중에는

- 지금 라운드가 어떤 구간인지
- 다음 Stage가 어떤 구간인지

를 JS가 바로 안내할 수 있고,
결과 화면에서도
각 Stage가 `기본 / 확장 / 전체` 중 어느 구간이었는지 다시 볼 수 있다.

## 결과 카피는 왜 같이 바꿨나

결과 화면은 지금까지
“점수와 진행 결과 중심”이라는 식으로 다소 추상적이었다.

하지만 국기 게임은 난이도 구간이 생긴 뒤에는
`어느 구간에서 오래 버텼는가`가 더 중요한 설명 포인트다.

그래서 [result.html](/Users/alex/project/worldmap/src/main/resources/templates/flag-game/result.html)에서는

- hero copy
- result banner copy
- Stage 표의 `구간` 열

을 같이 추가했다.

즉, 결과 화면도 이제 단순 점수 로그가 아니라
난이도 progression 로그처럼 읽히게 바뀌었다.

## 테스트로 무엇을 고정했나

[FlagGameDifficultyPolicyTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/application/FlagGameDifficultyPolicyTest.java)에서

- `기본 / 확장 / 전체 라운드` 라벨
- 각 guide 문자열
- candidate pool 크기
- early round stable target 규칙

을 고정했다.

[FlagGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/flag/FlagGameFlowIntegrationTest.java)에서는

- `GET /state`의 `difficultyGuide`
- `POST /answer`의 `nextDifficultyGuide`
- Stage 1 target이 북미/오세아니아가 아닌지
- 결과 HTML의 `구간`, `기본 라운드`

까지 같이 검증했다.

실행:

```bash
node --check src/main/resources/static/js/flag-game.js
./gradlew test --tests com.worldmap.game.flag.application.FlagGameDifficultyPolicyTest --tests com.worldmap.game.flag.application.FlagGameOptionGeneratorTest --tests com.worldmap.game.flag.FlagGameFlowIntegrationTest
./gradlew test
```

## 지금 상태를 어떻게 설명하면 되나

이제 국기 게임은 단순히
“국기 자산 36개를 쓰는 게임”이 아니다.

현재는

- 초기 라운드는 비교가 쉬운 대륙에서 시작하고
- 중반부터 지역 fallback이 열리고
- 후반에는 전체 pool이 풀리는

progression 규칙을 가진 게임이다.

그리고 이 progression이
state / answer / result 화면까지 같은 서버 규칙으로 연결된다.

## 다음 단계

다음 후보는 둘 중 하나다.

- 국기 게임 세부 난이도와 결과 카피를 한 번 더 다듬기
- 신규 게임 3종의 홈 / 랭킹 / Stats 밀도를 다시 정리하기

## 면접에서 이렇게 설명할 수 있다

> 국기 게임은 자산과 distractor fallback만으로도 동작했지만, 플레이어가 체감하는 난이도 단계가 코드와 화면에 함께 드러나야 설명력이 더 좋아집니다. 그래서 `FlagGameDifficultyPolicy`를 `기본 / 확장 / 전체 라운드`로 다시 정의하고, `FlagGameService`가 초반에는 same-continent distractor가 충분한 대륙만 먼저 출제하도록 바꿨습니다. 또 이 규칙을 `difficultyGuide`와 결과 화면의 `구간` 표시까지 연결해서, 난이도 progression도 서버 정책으로 설명할 수 있게 정리했습니다.
