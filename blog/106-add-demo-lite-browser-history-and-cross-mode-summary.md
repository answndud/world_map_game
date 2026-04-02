# [Spring Boot 게임 플랫폼 포트폴리오] 106. demo-lite 홈에 browser recent play와 cross-mode summary를 붙이는 이유

## 1. 이번 글에서 닫을 문제

`demo-lite`는 이제 retained surface 4개가 모두 playable합니다.

- 수도 맞히기
- 국기 퀴즈
- 인구 비교 배틀
- 국가 추천

그런데 홈으로 돌아오면 문제가 하나 남아 있었습니다.

> 방금 무엇을 했는지 다시 읽을 수 없다

즉 각 feature는 playable하지만,
앱 전체로 보면 `최근 활동`과 `모드별 요약`이 전혀 보이지 않았습니다.

full app에서는 이 역할을 아래가 맡습니다.

- `/ranking`
- `/stats`
- `/mypage`

하지만 `demo-lite`는 이 셋을 모두 의도적으로 제거했습니다.

그래서 이번 조각의 목표는
**서버 저장 없이도 홈이 “최근에 뭘 했는지” 다시 읽게 만드는 얇은 browser read model**을 붙이는 것이었습니다.

## 2. 이번에 바뀐 파일

- [demo-lite/src/lib/browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)
- [demo-lite/src/app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)
- [demo-lite/src/features/capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)
- [demo-lite/src/features/flag-game.js](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)
- [demo-lite/src/features/population-battle-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)
- [demo-lite/src/features/recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)
- [demo-lite/tests/browser-history.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/browser-history.test.mjs)

문서도 같이 맞췄습니다.

- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [README.md](/Users/alex/project/worldmap/README.md)
- [docs/DEMO_LITE_SCOPE_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_SCOPE_PLAN.md)
- [docs/DEMO_LITE_DECOMPOSITION_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_DECOMPOSITION_PLAN.md)
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 3. 왜 `browser-history.js`를 따로 만들었나

이번 요구는 단순히 localStorage를 쓰는 것이 아니었습니다.

필요한 것은 두 단계였습니다.

1. 각 feature가 종료 시점에 기록을 남긴다
2. 홈이 그 기록들을 다시 조합해서 읽는다

즉 단순 helper보다 **얇은 read/write contract**가 필요했습니다.

그래서 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)를 따로 만들고 아래 책임을 모았습니다.

- 게임 종료 기록 저장
- recommendation 결과 저장
- 최근 기록 배열 읽기
- mode별 best score 읽기
- 홈에 필요한 summary object 만들기

이걸 [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js) 안에 바로 넣으면,
route shell과 browser persistence 규칙이 섞여 버립니다.

## 4. 어떤 기록을 남기나

### 게임

게임 3종은 terminal result일 때만 기록합니다.

- `FINISHED`
- `GAME_OVER`

저장하는 값은 아래 정도만 남깁니다.

- mode
- 총점
- 정답 수
- 총 제출 수
- 상태
- browser best score
- 한 줄 summary

즉 Stage 상세를 다시 다 저장하지 않고,
**홈에서 읽기 좋은 요약 이벤트**만 남깁니다.

### 추천

추천은 게임과 다르게 점수 구조가 없습니다.

그래서 recommendation은 아래만 남깁니다.

- 최근 TOP1 나라 이름
- ISO3
- 결과 수

즉 recommendation은 "몇 점"보다
**최근에 어떤 나라가 1위로 나왔는가**만 홈에서 다시 읽습니다.

## 5. 왜 홈이 이걸 다시 읽어야 하나

feature 안에서 끝난 결과는 그 화면을 떠나면 사라집니다.

`demo-lite`가 공개용 체험판처럼 보이려면,
홈이 최소한 아래를 말해 줘야 합니다.

- 최근 기록이 몇 개 있는지
- 어떤 모드를 플레이해 봤는지
- 브라우저 최고 점수가 무엇인지
- 최근 추천 TOP1이 무엇이었는지

그래서 [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)는 `#/` 홈을 렌더링할 때
[readDemoLiteActivitySummary()](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)를 읽어 아래 세 섹션을 그립니다.

1. browser 최근 플레이 요약
2. 모드별 브라우저 요약
3. 최근 기록

즉 `demo-lite` 홈은 이제 단순 route index가 아니라,
**브라우저 안에서 다시 조합한 read model surface**가 됩니다.

## 6. 어디에서 상태가 바뀌나

이번 조각의 핵심 상태 변화는 서버가 아니라 브라우저입니다.

### 6-1. 게임 3종

- [capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)
- [flag-game.js](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)
- [population-battle-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)

이 세 파일은 `FINISHED` 또는 `GAME_OVER`가 되는 순간
[recordDemoLiteGameRun()](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)를 호출합니다.

즉 state change는 "정답 판정" 시점이 아니라,
**run이 terminal 상태가 되는 순간**입니다.

### 6-2. recommendation

[recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)는
top 3를 계산한 직후
[recordDemoLiteRecommendationResult()](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)를 호출합니다.

즉 recommendation은 저장이 아니라
**최근 추천 결과 요약만 남깁니다.**

## 7. 요청 흐름 대신 브라우저 흐름

메인 앱 요청 흐름은 바뀌지 않았습니다.

새로 생긴 건 `demo-lite`의 브라우저 흐름입니다.

1. 사용자가 게임을 끝내거나 추천 결과를 계산한다
2. 각 feature가 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)에 history entry를 저장한다
3. 사용자가 홈 `#/`으로 돌아온다
4. [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 `readDemoLiteActivitySummary()`를 호출한다
5. 홈이 recent count / active mode / best score / recommendation top1 / recent list를 다시 그린다

즉 이번 조각은 API 요청이 아니라
**feature write -> home read** 형태의 browser-side read model입니다.

## 8. 어떤 테스트로 닫았나

이번에는 새로운 storage contract를 직접 테스트로 고정했습니다.

```bash
cd demo-lite
npm test
```

[browser-history.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/browser-history.test.mjs)는 아래를 확인합니다.

1. game run 기록이 recent entry 앞쪽에 쌓이는지
2. mode별 best score를 summary가 다시 읽는지
3. recommendation 결과가 최근 TOP1로 노출되는지

그리고 static build 전체는 다시 아래로 확인했습니다.

```bash
cd demo-lite
npm run build
```

즉 이번 조각의 핵심 검증은
"localStorage를 쓰는가"가 아니라
**홈에서 다시 읽을 수 있는 read model 계약이 안정적으로 유지되는가**입니다.

## 9. 이 조각이 왜 중요한가

기능만 보면 작은 추가처럼 보일 수 있습니다.

하지만 설계 관점에서는 꽤 중요한 변화입니다.

이제 `demo-lite`는:

- 게임 3종과 추천이 각각 따로 노는 체험판

이 아니라

- 브라우저 단위 recent play와 mode summary를 가진 작은 앱

으로 한 단계 올라왔습니다.

즉 서버를 빼더라도
**surface 간 연결감**은 만들어 둔 셈입니다.

## 10. 지금 남은 것

이제 `demo-lite`는 retained surface와 홈 summary까지 닫혔습니다.

남은 것은 더 높은 수준의 polish입니다.

예를 들면:

1. recent streak
2. shareable summary
3. 정적 호스팅 실제 배포
4. recommendation 결과 copy polish

즉 다음 조각은 “기능 하나 더 추가”보다,
**지금 만든 browser read model을 얼마나 공개 앱답게 다듬을 것인가**에 가까울 가능성이 큽니다.

## 11. 30초 답변

demo-lite 홈에 browser recent play와 cross-mode summary를 붙였습니다. 각 게임은 종료 시점에, 추천은 결과 계산 시점에 localStorage history entry를 남기고, 홈은 그 기록과 mode별 best score를 다시 읽어 최근 활동과 요약 카드를 그립니다. 핵심은 서버 저장 없이도 demo-lite가 “방금 무엇을 했는지 기억하는 체험판 앱”처럼 보이게 만든 점입니다.
