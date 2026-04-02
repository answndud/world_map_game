# [Spring Boot 게임 플랫폼 포트폴리오] 101. demo-lite에서 첫 retained 게임으로 수도 맞히기 local-state loop를 여는 이유

## 1. 이번 글에서 닫을 문제

`demo-lite` 앱 셸과 retained route를 먼저 연 뒤에도, 여전히 한 가지 문제가 남아 있었습니다.

> 이 앱이 정말 free-tier 공개용 체험판으로 작동하는가?

shell만 있는 상태에서는 이 질문에 답하기 어렵습니다.

그래서 이번 조각의 목표는 retained route 중 가장 단순한 `수도 맞히기`를 골라,
브라우저 메모리 상태만으로 한 판이 끝까지 도는 첫 실제 loop를 여는 것이었습니다.

## 2. 이번에 바뀐 파일

- [demo-lite/src/features/capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)
- [demo-lite/tests/capital-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/capital-game.test.mjs)
- [demo-lite/src/app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)
- [demo-lite/src/lib/shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)
- [demo-lite/src/routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)
- [demo-lite/src/style.css](/Users/alex/project/worldmap/demo-lite/src/style.css)
- [demo-lite/package.json](/Users/alex/project/worldmap/demo-lite/package.json)

문서도 같이 맞췄습니다.

- [README.md](/Users/alex/project/worldmap/README.md)
- [docs/DEMO_LITE_SCOPE_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_SCOPE_PLAN.md)
- [docs/DEMO_LITE_DECOMPOSITION_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_DECOMPOSITION_PLAN.md)
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 3. 왜 수도 게임부터였나

retained route 네 개 중에서 `capital`이 가장 먼저 붙기 쉬웠습니다.

이유는 단순합니다.

1. [countries.json](/Users/alex/project/worldmap/src/main/resources/data/countries.json)에 이미 `nameKr`, `capitalCityKr`가 있다
2. 국기 이미지나 추천 엔진처럼 추가 자산/계산 복잡도가 낮다
3. 문제 생성과 정답 판정이 pure function으로 잘 잘린다

즉 `flag`, `population-battle`, `recommendation`보다 먼저
demo-lite의 첫 실제 playable loop로 붙이기 가장 안전했습니다.

## 4. 이번에 연 게임 규칙

demo-lite 수도 게임은 full app의 서버 저장형 endless run을 그대로 복제하지 않습니다.

대신 아래처럼 `copy-and-simplify` 했습니다.

- 총 5문제
- 하트 3개
- 오답이면 같은 문제 재시도
- 정답이면 점수만 잠깐 보여 주고 다음 문제로 자동 이동
- 1트 정답 120점, 2트 정답 90점, 3트 이상 60점
- 종료 후 `localStorage`에 브라우저 최고 점수 저장

핵심은 “full app의 약속을 다 가져오지 않고도, 무료 공개용 체험판으로 설명 가능한 최소 게임 루프를 닫는다”는 점입니다.

## 5. 요청 흐름이 아니라 브라우저 흐름으로 닫은 이유

이번 조각은 Spring Boot controller/service를 건드리지 않습니다.

새 흐름은 아래입니다.

1. 브라우저가 [index.html](/Users/alex/project/worldmap/demo-lite/index.html)을 연다
2. [main.js](/Users/alex/project/worldmap/demo-lite/src/main.js)가 현재 hash route를 읽는다
3. [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 `#/games/capital`이면 [capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)를 mount한다
4. [shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)가 generated countries JSON을 읽는다
5. [capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)가 문제 pool 생성, 점수 계산, 하트 감소, round 전환을 모두 브라우저 메모리 상태로 처리한다

즉 이 조각의 핵심은 “요청 처리”가 아니라 **브라우저 안에서 닫는 별도 state machine**입니다.

## 6. 왜 이 로직을 `app.js`가 아니라 `capital-game.js`에 뒀나

[app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)의 책임은 route shell입니다.

즉 아래까지만 맡습니다.

- 현재 route 결정
- 공통 header/navigation 렌더링
- route별 mount 지점 연결

반대로 수도 게임의 책임은 다릅니다.

- 출제 가능한 국가 pool 정리
- distractor 3개 만들기
- lives 감소
- 점수 계산
- 종료 판단

이건 route shell이 아니라 **게임 규칙**입니다.

그래서 [capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)에 아래를 같이 모았습니다.

- `buildCapitalQuestionPool()`
- `createCapitalRound()`
- `createCapitalSession()`
- `submitCapitalAnswer()`
- `renderCapitalGamePage()`
- `mountCapitalGame()`

즉 화면 진입은 `app.js`, 게임 규칙은 `capital-game.js`입니다.

## 7. 데이터는 무엇을 재사용하나

이번 조각도 full app runtime은 재사용하지 않습니다.

대신 build-time에 아래만 복사합니다.

- [countries.json](/Users/alex/project/worldmap/src/main/resources/data/countries.json)
- [flag-assets.json](/Users/alex/project/worldmap/src/main/resources/data/flag-assets.json)
- [flags](/Users/alex/project/worldmap/src/main/resources/static/images/flags)

[shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)는 `public/generated/data/*.json`을 fetch하고,
capital 게임은 그중 `countries`만 씁니다.

즉 demo-lite의 shared contract는 여전히 **정적 데이터**이지,
Spring Boot bean이나 DB repository가 아닙니다.

## 8. 어떤 엣지를 막았나

이번 조각에서 일부러 먼저 막은 것은 아래입니다.

### 8-1. 중복 수도명

한국어 수도명이 중복되면 보기 4개가 헷갈립니다.

그래서 `buildCapitalQuestionPool()`에서 `capitalCityKr` 기준 중복을 먼저 걷었습니다.

### 8-2. 한 러닝에서 같은 나라 반복

`usedIso3Codes`를 별도로 관리해서 같은 나라가 연속으로 다시 나오는 것을 막았습니다.

### 8-3. 정답 직후 즉시 다음 문제 전환

현재 제품 UX 기준으로는 정답을 맞히면 점수만 잠깐 보이고,
그 다음에 자동으로 다음 stage로 가야 합니다.

그래서 demo-lite도 같은 리듬을 따라,
정답이면 `+점수`를 잠깐 보여 준 뒤에만 다음 문제로 전환하게 했습니다.

### 8-4. 서버 전적 없이도 반복 동기 유지

free-tier 공개용 앱에는 서버 저장을 다시 붙이지 않습니다.

대신 `localStorage`에 최고 점수만 남겨 최소한의 반복 플레이 동기를 유지했습니다.

## 9. 테스트는 무엇으로 닫았나

이번에는 Node 내장 테스트로 pure function을 먼저 고정했습니다.

```bash
cd demo-lite
npm test
```

[capital-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/capital-game.test.mjs)는 아래를 확인합니다.

- `capitalCityKr` 중복 제거
- 보기 4개 생성과 정답 1개 유지
- 오답 시 같은 문제 재시도 + lives 감소
- 정답 시 다음 round 전환과 점수 반영
- 다섯 번째 정답 후 종료 처리

그리고 정적 앱 자체는 아래로 다시 확인했습니다.

```bash
cd demo-lite
npm run build
```

즉 이번 조각의 핵심 검증은
“수도 게임 규칙이 pure function으로 닫히는가”와
“static build가 계속 깨지지 않는가”였습니다.

## 10. 지금 남은 것

이제 demo-lite는 더 이상 shell-only가 아닙니다.

하지만 아직 남은 것이 있습니다.

1. `flag` loop
2. `population-battle` loop
3. `recommendation` 결과 loop

즉 다음 조각은 수도 게임을 더 키우는 것이 아니라,
같은 패턴으로 다른 retained route를 하나씩 playable하게 바꾸는 것입니다.

## 11. 30초 답변

demo-lite가 shell만 있는 상태를 넘기기 위해, 첫 retained 게임으로 수도 맞히기 한 판을 local-state로 열었습니다. 메인 Spring Boot 서비스는 건드리지 않고 `countries.json`만 브라우저에서 읽어 5문제 러닝, 3 lives, 오답 재시도, 정답 후 자동 진행, localStorage 최고 점수를 처리합니다. 핵심은 free-tier 공개용 별도 앱에서도 최소 한 게임은 실제로 playable state machine으로 닫았다는 점이고, 이를 `capital-game.test.mjs`와 `npm run build`로 같이 고정했다는 것입니다.
