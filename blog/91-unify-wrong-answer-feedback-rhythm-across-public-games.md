# 모든 게임의 오답 피드백 시간을 같은 리듬으로 맞추기

## 왜 이 조각이 필요한가

정답 뒤 자동으로 다음 Stage로 넘어가게 만든 뒤에도,
오답 피드백은 게임마다 조금씩 느낌이 달랐다.

- 어떤 게임은 stage hint 문구가 달랐고
- 어떤 게임은 지연 시간이 하드코딩돼 있었고
- 위치 게임은 같은 `950ms`를 써도 코드상 의미가 덜 분명했다

즉, 플레이어 입장에서는
`정답 리듬`은 비슷해졌지만
`오답 뒤 다시 시도하는 리듬`은 완전히 통일되지 않은 상태였다.

이번 조각의 목적은 이 차이를 줄이는 것이다.

## 이번에 바뀐 파일

- [location-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/location-game.js)
- [population-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-game.js)
- [capital-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/capital-game.js)
- [population-battle-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/population-battle-game.js)
- [flag-game.js](/Users/alex/project/worldmap/src/main/resources/static/js/flag-game.js)
- 각 게임의 `start.html`, `play.html` script version

같이 [README.md](/Users/alex/project/worldmap/README.md),
[PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md),
[WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md),
[50-current-state-rebuild-map.md](/Users/alex/project/worldmap/blog/50-current-state-rebuild-map.md)도 현재 기준으로 맞췄다.

## 무엇을 바꿨나

핵심은 다섯 게임 JS가
정답/오답 피드백 시간을 같은 이름의 상수로 다루게 만든 것이다.

예전에는 `950`, `1100` 같은 숫자가 파일마다 흩어져 있었다.

지금은 각 play 루프가 이런 식으로 읽힌다.

- `STAGE_FEEDBACK_DELAY_MS = 950`
- `FINISH_REDIRECT_DELAY_MS = 1100`

그리고 오답 branch는 공통으로

1. 오답 overlay 노출
2. stage hint에 “잠시 뒤 같은 Stage를 다시 시도” 안내
3. 약 `950ms` 뒤 overlay 숨김
4. 입력 잠금 해제
5. 같은 Stage 재시도 상태로 복귀

흐름으로 맞춰졌다.

## 요청 흐름은 어떻게 유지됐나

서버 API는 바뀌지 않았다.

- `POST /api/games/.../sessions/{id}/answer`
- 필요하면 `GET /api/games/.../sessions/{id}/state`

정답/오답, 하트 차감, 점수 계산은 여전히 서버가 맡는다.

이번 조각은
그 결과를 **얼마 동안 보여 주고 언제 입력을 다시 풀지**
만 정리한 것이다.

즉,

- 상태 변경 책임은 서버
- 플레이 템포는 JS

라는 경계를 유지했다.

## 왜 컨트롤러나 서비스가 아니라 JS인가

이번에 바뀐 것은
`오답이냐 아니냐` 자체가 아니라
`오답 결과를 몇 ms 보여 줄 것이냐`다.

그건 서버 판정 규칙보다
플레이 화면 루프에 더 가깝다.

그래서 서비스에 “950ms 뒤 다시 시도” 같은 개념을 넣지 않고,
각 게임 JS가 같은 상수와 같은 단계 순서로 움직이게 정리했다.

위치 게임도 이 기준에 맞춰
정답/오답 직후 stage hint 문구를 다른 게임과 비슷한 톤으로 맞췄다.

## 테스트로 무엇을 확인했나

이번 조각은 프론트 루프 정리라
먼저 JS 문법과 전체 서버 회귀를 같이 확인했다.

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

- JS 문법이 깨지지 않았는지
- script version 갱신으로 캐시 꼬임이 줄었는지
- 서버 테스트가 그대로 통과하는지

를 같이 보는 것이었다.

## 지금 상태를 어떻게 설명하면 되나

이제 public 다섯 게임은

- 정답이면 `획득 점수`를 잠깐 보여 주고 자동 다음 Stage
- 오답이면 overlay를 잠깐 보여 주고 자동으로 같은 Stage 재시도 상태 복귀

라는 공통 템포를 가진다.

즉, 게임 규칙은 다르지만
플레이 리듬은 같은 플랫폼 안의 모드처럼 읽힌다.

## 다음 단계

다음 후보는 둘 중 하나다.

- 게임오버 직전 마지막 오답 피드백도 더 짧게 조정할지 판단
- 새 게임 3종의 난이도와 결과 카피를 여기서 멈출지 다시 판단

## 면접에서 이렇게 설명할 수 있다

> 정답 자동 전환 뒤에도 오답 overlay와 입력 잠금 해제 시점은 게임마다 조금씩 달랐습니다. 그래서 서버 판정 로직은 그대로 둔 채, 다섯 게임 JS가 오답 피드백을 약 `950ms`만 보여 준 뒤 같은 Stage 재시도 상태로 자동 복귀하도록 맞췄습니다. 핵심은 점수와 하트 계산은 계속 서버가 맡고, 프론트는 게임 템포만 공통 상수로 정리했다는 점입니다.
