# [Spring Boot 게임 플랫폼 포트폴리오] 103. demo-lite에서 세 번째 retained 게임으로 인구 비교 배틀 local-state loop를 여는 이유

## 1. 이번 글에서 닫을 문제

`demo-lite`에 수도 맞히기와 국기 퀴즈를 먼저 붙인 뒤에도, free-tier 체험판으로는 아직 한 가지가 남아 있었습니다.

> 이 앱이 퀴즈 모음이 아니라, 실제로 다른 입력 리듬을 가진 게임도 담고 있는가?

`population-battle`은 4지선다 퀴즈가 아니라 좌우 2-choice battle이고,
메인 앱에서는 `difficulty band`, `하트`, `점수 증가 폭`이 같이 움직입니다.

그래서 이번 조각의 목표는 `demo-lite`의 세 번째 retained 게임으로 `인구 비교 배틀`을 붙여,
퀴즈형이 아닌 battle형 loop도 브라우저 메모리 상태만으로 끝까지 도는지 닫는 것이었습니다.

## 2. 이번에 바뀐 파일

- [demo-lite/src/features/population-battle-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)
- [demo-lite/tests/population-battle-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/population-battle-game.test.mjs)
- [demo-lite/src/app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)
- [demo-lite/src/routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)
- [demo-lite/src/style.css](/Users/alex/project/worldmap/demo-lite/src/style.css)

문서도 같이 맞췄습니다.

- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [README.md](/Users/alex/project/worldmap/README.md)
- [docs/DEMO_LITE_SCOPE_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_SCOPE_PLAN.md)
- [docs/DEMO_LITE_DECOMPOSITION_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_DECOMPOSITION_PLAN.md)
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 3. 왜 population-battle를 세 번째 retained game으로 골랐나

`capital`은 텍스트 퀴즈, `flag`는 이미지 퀴즈입니다.

여기에 `population-battle`이 붙으면 demo-lite retained game 3종이 아래처럼 나뉩니다.

1. 텍스트 4지선다
2. 이미지 4지선다
3. 좌우 2-choice battle

즉 free-tier 체험판이 단순히 카피만 다른 퀴즈 셋이 아니라,
**입력 방식이 다른 세 가지 대표 surface**를 가진다는 점을 바로 보여 줄 수 있습니다.

## 4. 메인 앱 계약에서 무엇을 가져오고 무엇을 버렸나

메인 앱의 `population-battle`은 아래를 가집니다.

- stage별 difficulty band
- 좌우 2-choice 선택
- 하트 3개
- stage/attempt 저장
- stale submit guard
- leaderboard write

demo-lite에서는 이 중 아래만 남겼습니다.

- stage별 difficulty 감각
- 좌우 2-choice 선택
- 하트 3개
- 정답/오답 후 자동 진행 리듬
- stage와 lives를 반영한 점수식

아래는 제거했습니다.

- DB session / stage / attempt 저장
- stale submit guard
- leaderboard write
- result page의 상세 서버 read model

즉 이번 조각도 `copy-and-simplify`입니다.

## 5. 문제 pair는 어떻게 만들었나

핵심은 [buildPopulationBattleQuestionPool()](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)와
[buildRounds()](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)입니다.

먼저 `countries.json`에서 아래 조건을 만족하는 나라만 모읍니다.

- `nameKr`가 있다
- `population`이 숫자다
- 이름 중복이 아니다

그다음 인구수 내림차순으로 정렬해 `populationRank`를 붙입니다.

Stage는 총 5개로 고정했고, 각 Stage는 서로 다른 gap band를 씁니다.

- Band A · 큰 격차
- Band B · 넉넉한 차이
- Band C · 근접 비교
- Band D · 고난도
- Band E · 초근접

즉 메인 앱의 endless difficulty 계획을 free-tier용 fixed 5-stage run으로 줄인 것입니다.

## 6. 왜 5 Stage fixed run으로 단순화했나

메인 앱의 `population-battle`은 사실상 endless에 가깝습니다.

하지만 demo-lite는 이미 `capital`, `flag`를 모두 `5 rounds` 기준으로 닫았습니다.

그래서 이번에도 retained demo 전체의 리듬을 맞추기 위해 아래를 택했습니다.

- 하트 3개는 유지
- 2-choice battle은 유지
- difficulty band는 유지
- 대신 run 길이는 5 Stage로 고정

즉 main과 완전히 같은 loop를 재현하는 것보다,
**free-tier 공개용 별도 앱 안에서 일관된 플레이 세션 길이**를 먼저 맞췄습니다.

## 7. 점수식은 왜 main 감각을 남겼나

이번 조각은 길이는 줄였지만, 점수 감각은 메인 앱 쪽을 최대한 살렸습니다.

[calculateAwardedScore()](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)는 아래를 씁니다.

- 기본 점수: `90 + (stage-1) * 15`
- life bonus: `livesRemaining * 10`
- attempt bonus:
  - 1트 `+30`
  - 2트 `+10`
  - 3트 이상 `+0`

즉 "뒤로 갈수록 stage 가치가 커지고, 하트를 아끼며 빨리 맞히는 쪽이 유리하다"는 메인 배틀의 감각은 그대로 유지했습니다.

## 8. 새 브라우저 흐름

메인 Spring Boot 앱의 요청 흐름은 바뀌지 않았습니다.

이번 조각에서 새로 닫은 건 demo-lite의 브라우저 흐름입니다.

1. 브라우저가 [index.html](/Users/alex/project/worldmap/demo-lite/index.html)을 연다
2. [main.js](/Users/alex/project/worldmap/demo-lite/src/main.js)가 현재 hash route를 읽는다
3. [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 `#/games/population-battle`이면 [population-battle-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)를 mount한다
4. [shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)가 generated countries JSON을 읽는다
5. [population-battle-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)가 pair 생성, 정답 판정, 하트 감소, stage 전환을 모두 브라우저 메모리 상태로 처리한다

즉 이 조각의 핵심도 API가 아니라 **브라우저 안에서 닫는 battle state machine**입니다.

## 9. 왜 이 로직을 `app.js`가 아니라 `population-battle-game.js`에 뒀나

[app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)의 책임은 여전히 route shell입니다.

- 현재 route 결정
- 공통 header/navigation 렌더링
- route별 mount 연결

반면 배틀 게임의 책임은 다릅니다.

- playable country pool 정리
- rank-gap pair 생성
- difficulty band 적용
- lives 감소
- 점수 계산
- 종료 판단

이건 route shell이 아니라 **게임 규칙**이므로,
[population-battle-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)에 모았습니다.

## 10. 어떤 엣지를 먼저 막았나

### 10-1. 인구수 누락 국가

`population`이 숫자가 아닌 국가는 pool에서 제외합니다.

배틀은 정답 판정이 인구수 비교에 달려 있으므로,
이 기준을 느슨하게 두면 stage 자체가 흔들립니다.

### 10-2. 중복 pair

같은 run에서 같은 나라 pair가 반복되면 체감이 급격히 나빠집니다.

그래서 같은 `iso3Code` 조합은 한 번만 쓰게 막았습니다.

다만 country 전체 reuse까지 막지는 않았습니다.
free-tier retained runtime에서는 구현 복잡도보다 pair 반복 억제만 먼저 막는 편이 더 단순했습니다.

### 10-3. 정답/오답 직후 정보 노출

현재 제품 방향에 맞춰,
정답 직후에는 점수만 보여 주고 더 인구가 많은 나라 이름을 다시 강조하지 않습니다.
오답이어도 정답이나 population 숫자를 바로 공개하지 않고 같은 Stage를 다시 풀게 둡니다.

## 11. 테스트는 무엇으로 닫았나

이번에도 Node 내장 테스트로 pure function을 먼저 고정했습니다.

```bash
cd demo-lite
npm test
```

[population-battle-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/population-battle-game.test.mjs)는 아래를 확인합니다.

- population 기반 pool 생성과 내림차순 정렬
- 5 Stage / 2-choice 세션 시작
- 오답 시 같은 Stage 재시도 + lives 감소
- 정답 시 다음 Stage 전환과 점수 반영
- 다섯 번째 정답 후 종료 처리

그리고 정적 앱 전체는 아래로 다시 확인했습니다.

```bash
cd demo-lite
npm run build
```

즉 이번 조각의 핵심 검증은
“배틀 pair 생성과 점수 규칙이 pure function으로 닫히는가”와
“static build가 계속 깨지지 않는가”였습니다.

## 12. 지금 남은 것

이제 demo-lite retained game 3종은 모두 playable합니다.

- `capital`
- `flag`
- `population-battle`

남은 것은 하나입니다.

1. `recommendation` 결과 loop

즉 다음 조각은 새로운 게임을 더 붙이는 것이 아니라,
retained surface의 마지막 축인 추천 결과를 local-state로 닫는 것입니다.

## 13. 30초 답변

demo-lite의 세 번째 retained 게임으로 인구 비교 배틀 한 판을 열었습니다. 메인 Spring Boot 서비스는 건드리지 않고 `countries.json`의 population 값을 브라우저에서 읽어 rank-gap 기반 pair를 만들고, difficulty band, 2-choice 선택, 3 lives, 오답 재시도, 정답 후 자동 진행, localStorage 최고 점수를 처리합니다. 핵심은 free-tier 공개용 앱에서도 퀴즈뿐 아니라 arcade battle 성격의 게임까지 실제 playable state machine으로 닫았다는 점이고, 이를 `population-battle-game.test.mjs`와 `npm run build`로 함께 고정했다는 것입니다.
