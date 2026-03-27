# 모든 게임에서 정답 뒤 자동으로 다음 Stage로 넘기기

## 왜 이 조각이 필요한가

위치 게임은 이미 정답을 맞히면 잠깐 점수를 보여 준 뒤 자동으로 다음 Stage로 넘어갔다.

하지만 수도 맞히기, 인구수 맞추기, 인구 비교 퀵 배틀, 국기 퀴즈는
정답을 맞힌 뒤에도 `다음 Stage` 버튼을 한 번 더 눌러야 했다.

즉, 같은 endless run 구조인데
게임마다 리듬이 달랐다.

이번 조각의 목적은 그 차이를 없애는 것이다.

이제는 모든 public 게임이

- 정답 판정
- 획득 점수 잠깐 표시
- 자동 다음 Stage 전환

흐름으로 맞춰진다.

## 이번에 바뀐 파일

- [capital-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/capital-game.js)
- [population-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-game.js)
- [population-battle-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-battle-game.js)
- [flag-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/flag-game.js)
- [capital-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/capital-game/play.html)
- [population-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/play.html)
- [population-battle-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-battle-game/play.html)
- [flag-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/flag-game/play.html)

같이 [README.md](/Users/alex/project/worldmap/README.md),
[PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md),
[WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md),
[50-current-state-rebuild-map.md](/Users/alex/project/worldmap/blog/50-current-state-rebuild-map.md)도 현재 기준으로 맞췄다.

## 무엇을 바꿨나

핵심은 정답 branch의 프론트 루프다.

예전에는 네 게임이 공통으로 이 흐름이었다.

1. 정답 제출
2. 점수 표시
3. `다음 Stage` 버튼 표시
4. 사용자가 버튼 클릭
5. `loadState()`

지금은 이렇게 바뀌었다.

1. 정답 제출
2. 점수 표시
3. `950ms` 대기
4. `loadState()` 자동 호출

즉, 서버 API를 바꾼 게 아니라
기존 `answer -> next state` 흐름을 더 짧게 연결한 것이다.

## 왜 이 로직이 서버가 아니라 JS에 있나

정답 판정과 점수 계산은 여전히 서버가 맡는다.

- `POST /api/games/.../sessions/{id}/answer`
- `GET /api/games/.../sessions/{id}/state`

두 요청 흐름은 그대로다.

이번 조각은 “언제 다음 state를 다시 읽을 것인가”만 바꾼 것이므로
컨트롤러나 서비스보다 각 게임 JS가 맡는 편이 맞다.

즉,

- 정답/점수/하트/클리어 수 계산은 서버
- 점수 overlay를 몇 ms 보여 주고 다음 state를 읽을지는 프론트

로 경계를 유지했다.

## `다음 Stage` 버튼은 어떻게 됐나

play 템플릿에서 버튼을 제거했다.

- [capital-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/capital-game/play.html)
- [population-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/play.html)
- [population-battle-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/population-battle-game/play.html)
- [flag-game/play.html](/Users/alex/project/worldmap/src/main/resources/templates/flag-game/play.html)

이제 정답 흐름에서는
버튼이 아니라 overlay와 stage hint가 잠깐 보이고 다음 문제가 자동으로 열린다.

오답과 게임오버는 그대로다.

즉, 수동 전환이 사라진 건 정답 흐름뿐이다.

## 예외 처리는 어떻게 했나

자동 `loadState()`가 실패하면
interaction을 다시 풀고 message box에 에러를 보여 준다.

이 처리가 없으면
정답 후 자동 전환이 실패했을 때 플레이어가 잠긴 상태로 남을 수 있다.

반대로 `FINISHED`는 자동 state reload가 아니라
기존처럼 `resultPageUrl`로 바로 넘긴다.

즉,

- 진행 중 정답: 자동 다음 Stage
- 게임 종료: 결과 페이지 이동

으로 나뉜다.

## 테스트로 무엇을 확인했나

이번 조각은 서버 API 구조를 바꾼 작업이 아니라
프론트 루프 정리라서, 먼저 JS 문법과 전체 회귀를 확인했다.

```bash
node --check src/main/resources/static/js/location-game.js
node --check src/main/resources/static/js/population-game.js
node --check src/main/resources/static/js/capital-game.js
node --check src/main/resources/static/js/population-battle-game.js
node --check src/main/resources/static/js/flag-game.js
./gradlew test
git diff --check
```

핵심은

- 잔여 `nextStageButton` 참조가 없는지
- 템플릿에서 버튼이 제거된 뒤 JS가 깨지지 않는지
- 서버 회귀 테스트가 그대로 통과하는지

를 같이 보는 것이었다.

## 지금 상태를 어떻게 설명하면 되나

이제 모든 public 게임은
정답을 맞히면 점수만 잠깐 보여 주고,
그다음 Stage를 자동으로 연다.

즉, 플레이어는
“맞히고 한 번 더 누르는” 단계 없이
바로 다음 문제 흐름으로 들어간다.

이 조각은 기능을 더 만든 게 아니라
이미 있는 endless run 구조를 더 가볍게 만든 조정이다.

## 다음 단계

다음 후보는 둘 중 하나다.

- 결과 화면 복기 밀도를 더 줄일지 결정
- 신규 게임 3종 난이도와 copy를 여기서 멈출지 다시 판단

## 면접에서 이렇게 설명할 수 있다

> 위치 게임만 정답 후 자동 전환되고 나머지 게임은 `다음 Stage` 버튼을 다시 눌러야 해서 게임 리듬이 끊겼습니다. 그래서 서버 판정 로직은 그대로 둔 채, 수도/인구수/인구 비교/국기 게임 JS가 정답 응답 뒤 `획득 점수`만 잠깐 보여 주고 약 1초 뒤 같은 세션의 다음 state를 자동으로 다시 읽게 바꿨습니다. 핵심은 상태 변경 책임은 계속 서버에 두고, 프론트는 read flow만 더 짧게 연결했다는 점입니다.
