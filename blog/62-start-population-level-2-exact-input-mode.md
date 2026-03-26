# 인구수 게임 Level 2를 직접 수치 입력형으로 시작하기

## 이 글에서 다루는 내용

이번 조각에서는 `국가 인구수 맞추기`에 Level 2를 붙였다.

핵심은 새 게임을 따로 만드는 것이 아니라,
기존 Level 1이 이미 가지고 있던

- 세션
- Stage
- Attempt
- 하트
- 재시작
- endless 진행

구조를 그대로 재사용하면서,
입력 방식과 정답 판정 정책만 바꾸는 것이다.

즉 이번 글의 주제는
`Level 1 구조를 깨지 않고 Level 2를 어떻게 확장할 것인가`
이다.

## 왜 지금 이 작업을 하는가

Level 1은 구간 4지선다라서 빠르게 플레이하기 좋다.

하지만 포트폴리오 관점에서는 여기서 한 단계 더 가야 한다.

- 사용자가 직접 숫자를 입력하게 만들고
- 서버가 오차율을 계산하고
- 오차율에 따라 부분 점수를 주는 구조

까지 보여 주면,
단순 퀴즈 앱이 아니라
`정답 판정 로직이 있는 백엔드 게임 서비스`
라는 점이 더 선명해진다.

## 이번 조각에서 바뀐 구조

### 1. 세션이 game level을 저장한다

파일:

- [PopulationGameLevel.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/domain/PopulationGameLevel.java)
- [PopulationGameSession.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java)

이제 인구수 게임 세션은 `LEVEL_1`, `LEVEL_2` 중 하나로 시작한다.

중요한 점은
Stage나 Attempt 구조를 새로 만들지 않았다는 것이다.

그 대신 세션이 “이번 판이 어떤 입력 규칙을 쓰는가”를 들고 있고,
서비스가 그 level에 따라 분기한다.

## 2. Level 2는 직접 숫자를 입력한다

파일:

- [StartPopulationGameRequest.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/web/StartPopulationGameRequest.java)
- [SubmitPopulationAnswerRequest.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/web/SubmitPopulationAnswerRequest.java)
- [PopulationGameApiController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java)
- [PopulationGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/application/PopulationGameService.java)

요청 흐름은 이렇게 바뀐다.

1. `POST /api/games/population/sessions`
2. body에 `gameLevel`을 같이 보낸다
3. 서비스가 해당 level로 세션을 만든다
4. `GET /state`는 현재 level을 내려준다
5. `POST /answer`
6. Level 1이면 `selectedOptionNumber`
7. Level 2이면 `submittedPopulation`
8. 서버가 level에 따라 판정 policy를 바꾼다

즉,
같은 엔드포인트를 유지하면서
서버가 level에 따라 규칙만 바꾸는 구조다.

## 왜 컨트롤러가 아니라 서비스 / policy가 맡아야 하는가

직접 수치 입력형에서 중요한 건
HTTP 요청을 받는 것보다
`오차율을 어떻게 판정하고 점수를 어떻게 줄 것인가`
이다.

그래서 이번 조각에서 핵심 로직은
컨트롤러가 아니라 별도 policy로 분리했다.

파일:

- [PopulationGamePrecisionScoringPolicy.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/application/PopulationGamePrecisionScoringPolicy.java)

이 policy는

- 입력 수치
- 실제 인구수
- 현재 stage
- 시도 횟수
- 남은 하트

를 받아서

- 정답 여부
- 점수
- 오차율

을 계산한다.

이렇게 해 두면
나중에 threshold나 점수 band를 바꿀 때도
서비스 전체를 흔들지 않아도 된다.

## 현재 Level 2 판정 규칙

이번 첫 조각에서는
너무 복잡하게 시작하지 않고
설명 가능한 기준으로 정했다.

- 오차율 `5% 이하`: 최고 점수 band
- 오차율 `12% 이하`: 중간 점수 band
- 오차율 `20% 이하`: 낮은 점수 band
- 오차율 `20% 초과`: 오답

즉,
Level 2도 여전히 하트를 잃을 수 있지만,
정답 안에서도 오차율에 따라 점수가 달라진다.

## 기존 구조를 그대로 재사용한 부분

이 조각에서 중요한 건 “무엇을 안 바꿨는가”다.

계속 유지한 것:

- `PopulationGameSession`
- `PopulationGameStage`
- `PopulationGameAttempt`
- 같은 세션 재시작
- endless Stage 생성
- 하트 감소 규칙

바꾼 것:

- 시작 시 level 선택
- state 응답에서 입력 방식 분기
- answer 판정 policy 분기
- 결과 화면 표시 방식

즉,
Level 2는 새 게임이 아니라
`같은 게임 루프 위의 다른 입력 정책`
으로 확장했다.

## 랭킹과 기록은 어떻게 처리했는가

파일:

- [LeaderboardGameLevel.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardGameLevel.java)
- [LeaderboardService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/application/LeaderboardService.java)
- [MyPageService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/mypage/application/MyPageService.java)

Level 2 run은 이제 `leaderboard_record.game_level=LEVEL_2`로 저장된다.

다만 이번 조각에서는 공개 `/ranking` 화면까지 한 번에 넓히지 않았다.

대신 먼저:

- 저장은 level별로 정확히 나누고
- `/mypage`에서는 최근 플레이와 최고 기록에 `Level 1 / Level 2` 라벨을 붙였다

즉,
도메인 데이터는 먼저 올바르게 저장하고,
public 노출은 다음 조각으로 미루는 순서다.

## 프론트는 어떻게 바뀌었나

파일:

- [population-game/start.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/start.html)
- [population-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/play.html)
- [population-game/result.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/result.html)
- [population-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-game.js)

시작 화면:

- Level 1 / Level 2 카드 선택

플레이 화면:

- Level 1이면 기존 보기 4개
- Level 2이면 숫자 입력칸

결과 화면:

- Level 2일 때는 `정답 구간`이 아니라 `정답 값`

즉,
SSR 틀은 그대로 두고
현재 level state에 따라 입력 영역만 바꾸는 방식이다.

## 테스트

이번 조각에서 중요한 테스트는 두 가지다.

### 1. Level 2 흐름 통합 테스트

파일:

- [PopulationGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java)

확인한 것:

- Level 2 state는 options가 비어 있다
- exact input 제출이 된다
- 정확 입력이면 `errorRatePercent=0.0`
- 멀리 틀리면 하트가 줄어든다

### 2. precision scoring 단위 테스트

파일:

- [PopulationGamePrecisionScoringPolicyTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/population/application/PopulationGamePrecisionScoringPolicyTest.java)

확인한 것:

- 5% 이내면 최고 점수 band
- 20% 초과면 오답

## 이 작업으로 설명할 수 있게 된 것

이제 이 프로젝트는 인구수 게임에서

- 보기형 Level 1
- 직접 입력형 Level 2

를 같은 도메인 구조 위에서 설명할 수 있다.

즉 면접에서는 이렇게 말할 수 있다.

> Level 1은 빠른 구간 선택형으로 만들고, Level 2는 같은 세션·Stage·Attempt 구조를 유지한 채 직접 수치 입력과 오차율 점수 정책만 추가했습니다. 그래서 구조를 갈아엎지 않고도 난이도를 올릴 수 있게 설계했습니다.

## 다음 단계

이번 조각 다음에는 두 가지가 자연스럽다.

1. 공개 `/ranking` 화면에 Level 2 필터를 붙이기
2. 위치 찾기 게임 Level 2 첫 조각 시작하기
