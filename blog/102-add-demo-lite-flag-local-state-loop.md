# [Spring Boot 게임 플랫폼 포트폴리오] 102. demo-lite에서 두 번째 retained 게임으로 국기 퀴즈 local-state loop를 여는 이유

## 1. 이번 글에서 닫을 문제

`demo-lite`에 수도 맞히기 한 판을 먼저 붙인 뒤에도, free-tier 체험판으로는 아직 한 가지가 부족했습니다.

> 이 앱이 텍스트 퀴즈 하나만 되는 가벼운 샘플이 아니라, 실제 제품의 두 번째 성격까지 보여 줄 수 있는가?

`capital`은 정적 텍스트 데이터만 있으면 되지만, `flag`는 이미지 자산과 출제 가능 국가 pool을 같이 다뤄야 합니다.

그래서 이번 조각의 목표는 `demo-lite`의 두 번째 retained 게임으로 `국기 퀴즈`를 붙여,
브라우저 메모리 상태만으로도 asset-backed quiz 한 판이 끝까지 도는지 닫는 것이었습니다.

## 2. 이번에 바뀐 파일

- [demo-lite/src/features/flag-game.js](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)
- [demo-lite/tests/flag-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/flag-game.test.mjs)
- [demo-lite/src/app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)
- [demo-lite/src/routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)
- [demo-lite/src/style.css](/Users/alex/project/worldmap/demo-lite/src/style.css)

같은 조각에서 수도 게임 쪽 테스트/설명도 현재 상태에 맞게 같이 정리했습니다.

- [demo-lite/src/features/capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)
- [demo-lite/tests/capital-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/capital-game.test.mjs)

문서도 같이 맞췄습니다.

- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [README.md](/Users/alex/project/worldmap/README.md)
- [docs/DEMO_LITE_SCOPE_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_SCOPE_PLAN.md)
- [docs/DEMO_LITE_DECOMPOSITION_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_DECOMPOSITION_PLAN.md)
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 3. 왜 국기 게임이 두 번째 retained game인가

`population-battle`보다 먼저 `flag`를 연 이유는, free-tier demo-lite에서 보여 줄 수 있는 제품 메시지가 더 넓기 때문입니다.

1. `capital`은 텍스트 기반 퀴즈다
2. `flag`는 이미지 자산 기반 퀴즈다
3. 이 둘이 같이 열리면 `demo-lite`가 단순 JSON 퀴즈가 아니라는 점이 바로 보인다

또한 현재 메인 앱에는 이미 아래 자산 계약이 있습니다.

- [flag-assets.json](/Users/alex/project/worldmap/src/main/resources/data/flag-assets.json)
- [flags](/Users/alex/project/worldmap/src/main/resources/static/images/flags)

즉 `flag`는 새 데이터를 만드는 작업이 아니라,
이미 있는 `asset manifest + country seed`를 `demo-lite`에서도 설명 가능한 방식으로 다시 묶는 작업이었습니다.

## 4. 이번에 연 게임 규칙

demo-lite 국기 게임도 full app의 서버 저장형 endless run을 그대로 복제하지 않습니다.

대신 아래처럼 `copy-and-simplify` 했습니다.

- 총 5문제
- 하트 3개
- 오답이면 같은 문제 재시도
- 정답이면 점수만 잠깐 보여 주고 다음 문제로 자동 이동
- 점수는 `120 - (같은 문제에서 틀린 횟수 * 20)`을 기본으로 하고 최저 `60`점
- 종료 후 `localStorage`에 브라우저 최고 점수 저장

즉 `capital`과 같은 리듬을 유지하되,
문제 생성 쪽만 `국기 asset`에 맞게 확장했습니다.

## 5. 국기 문제 pool은 어떻게 만들었나

핵심은 [buildFlagQuestionPool()](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)입니다.

이 함수는 아래 두 소스를 합칩니다.

1. [countries.json](/Users/alex/project/worldmap/src/main/resources/data/countries.json)
2. [flag-assets.json](/Users/alex/project/worldmap/src/main/resources/data/flag-assets.json)

join key는 `iso3Code`입니다.

즉 playable pool은 다음 조건을 동시에 만족하는 나라만 들어갑니다.

- 국가 데이터가 있다
- `nameKr`가 있다
- 국기 manifest entry가 있다
- generated flag path로 바꿀 수 있다

이렇게 한 이유는 국기 게임이 "이름만 있는 나라"가 아니라,
**실제 이미지 파일이 준비된 나라만 문제로 써야 하는 asset-backed mode**이기 때문입니다.

## 6. 보기 생성 규칙은 왜 same-continent 우선인가

현재 메인 앱의 국기 게임도 대륙 기반 distractor 품질을 중요하게 봅니다.

그래서 demo-lite도 그 방향을 유지했습니다.

- 먼저 같은 대륙 나라로 distractor를 모은다
- 부족하면 fallback 대륙 순서로 보강한다
- 그래도 부족하면 전체 pool에서 채운다

이걸 [pickFlagOptions()](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)에 두었습니다.

이 로직을 [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)에 두지 않은 이유는 단순합니다.

- `app.js`의 책임: route shell과 mount
- `flag-game.js`의 책임: 출제 가능 pool, 보기 생성, lives/점수/종료 규칙

즉 same-continent 우선 규칙은 화면 진입 문제가 아니라 **게임 규칙**이므로,
[flag-game.js](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)가 가져야 맞습니다.

## 7. 새 브라우저 흐름

메인 Spring Boot 앱의 요청 흐름은 바뀌지 않았습니다.

이번 조각에서 새로 닫은 건 demo-lite의 브라우저 흐름입니다.

1. 브라우저가 [index.html](/Users/alex/project/worldmap/demo-lite/index.html)을 연다
2. [main.js](/Users/alex/project/worldmap/demo-lite/src/main.js)가 현재 hash route를 읽는다
3. [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 `#/games/flag`이면 [flag-game.js](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)를 mount한다
4. [shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)가 generated countries/flag manifest를 읽는다
5. [flag-game.js](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)가 playable pool, 점수 계산, 하트 감소, stage 전환을 모두 브라우저 메모리 상태로 처리한다

즉 이 조각의 핵심은 API 요청이 아니라,
**정적 자산을 읽는 브라우저 state machine**입니다.

## 8. 어떤 엣지를 먼저 막았나

### 8-1. asset path 정규화

메인 manifest는 `/images/flags/...` 기준 경로를 가집니다.

하지만 demo-lite는 build-time에 이 자산을 `public/generated/flags/...`로 복사합니다.

그래서 [demoLiteFlagPath()](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)에서 경로를 먼저 정규화했습니다.

### 8-2. 국가명 중복

국기 문제는 보기 4개가 모두 서로 다른 나라명이어야 합니다.

그래서 playable pool을 만들 때 `nameKr` 기준 중복을 걷었습니다.

### 8-3. 정답/오답 직후 정보 노출

현재 제품 방향에 맞춰, 정답 직후에는 점수만 보여 주고 정답 이름을 다시 설명하지 않습니다.
오답일 때도 바로 정답을 공개하지 않고, 같은 문제를 다시 풀게 둡니다.

즉 demo-lite도 full app과 같은 `즉시 정답 노출 축소` 정책을 유지합니다.

## 9. 테스트는 무엇으로 닫았나

이번에도 Node 내장 테스트로 pure function을 먼저 고정했습니다.

```bash
cd demo-lite
npm test
```

[flag-game.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/flag-game.test.mjs)는 아래를 확인합니다.

- country + flag asset join으로 playable pool 생성
- 5라운드 / 3 lives 세션 시작
- 오답 시 같은 문제 재시도 + lives 감소
- 정답 시 다음 round 전환과 점수 반영
- 마지막 정답 후 종료 처리

그리고 정적 앱 전체는 아래로 다시 확인했습니다.

```bash
cd demo-lite
npm run build
```

즉 이번 조각의 핵심 검증은
“국기 자산 기반 문제 생성이 pure function으로 닫히는가”와
“static build가 계속 깨지지 않는가”였습니다.

## 10. 지금 남은 것

이제 demo-lite는 shell-only도 아니고, 텍스트 퀴즈 하나만 있는 상태도 아닙니다.

현재 playable retained game은 아래 두 개입니다.

- `capital`
- `flag`

하지만 아직 남은 것이 있습니다.

1. `population-battle` loop
2. `recommendation` 결과 loop

즉 다음 조각은 `flag`를 더 고도화하는 것이 아니라,
같은 local-state 패턴으로 나머지 retained route를 하나씩 playable하게 바꾸는 것입니다.

## 11. 30초 답변

demo-lite의 두 번째 retained 게임으로 국기 퀴즈 한 판을 열었습니다. 메인 Spring Boot 서비스는 건드리지 않고 `countries.json`과 `flag-assets.json`을 브라우저에서 읽어 `iso3Code` 기준 playable pool을 만들고, same-continent 우선 distractor와 5문제 러닝, 3 lives, 오답 재시도, 정답 후 자동 진행, localStorage 최고 점수를 처리합니다. 핵심은 free-tier 공개용 앱에서도 텍스트 퀴즈뿐 아니라 asset-backed 이미지 퀴즈까지 실제 playable state machine으로 닫았다는 점이고, 이를 `flag-game.test.mjs`와 `npm run build`로 함께 고정했다는 것입니다.
