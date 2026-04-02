# [Spring Boot 게임 플랫폼 포트폴리오] 107. demo-lite 홈에 recent streak와 복사용 한 줄 요약을 붙이는 이유

## 1. 이번 글에서 닫을 문제

이전 조각까지 오면 `demo-lite` 홈은 이미 아래를 다시 읽을 수 있었습니다.

- 최근 기록 수
- 플레이한 모드 수
- 브라우저 최고 점수
- 최근 추천 TOP1
- mode별 요약 카드
- 최근 기록 리스트

하지만 여전히 하나가 비어 있었습니다.

> 지금 이 브라우저에서 어떤 흐름으로 플레이 중인지 한 번에 설명하는 문장

즉 숫자는 늘었지만,
홈이 `요약`보다는 아직 `기록 목록`에 가까웠습니다.

그래서 이번 조각의 목표는 두 가지였습니다.

1. 최근 게임 streak를 다시 계산한다
2. 그 결과를 복사 가능한 한 줄 문장으로 묶는다

핵심은 새 저장을 더 만드는 것이 아니라,
이미 있는 browser history를 한 번 더 읽어
**더 설명력 있는 read model**을 만드는 것입니다.

## 2. 이번에 바뀐 파일

- [demo-lite/src/lib/browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)
- [demo-lite/src/app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)
- [demo-lite/src/style.css](/Users/alex/project/worldmap/demo-lite/src/style.css)
- [demo-lite/tests/browser-history.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/browser-history.test.mjs)

문서도 같이 맞췄습니다.

- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [README.md](/Users/alex/project/worldmap/README.md)
- [docs/DEMO_LITE_SCOPE_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_SCOPE_PLAN.md)
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 3. 왜 새 key를 더 만들지 않았나

이번 요구를 가장 단순하게 풀면 localStorage에 아래를 따로 저장할 수도 있습니다.

- 최근 streak
- 최근 클리어 streak
- 공유용 텍스트

하지만 그렇게 하면 write 시점이 늘어납니다.

즉 각 feature가 terminal result를 남길 때마다
이 값들까지 같이 갱신해야 합니다.

`demo-lite`에서는 그보다 아래가 더 안전합니다.

1. feature는 계속 최소 단위 event만 기록한다
2. 홈이 필요한 순간 summary를 다시 계산한다

그래서 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)는
기존 `recent-history`와 mode별 best score를 다시 읽어서 아래만 추가로 계산합니다.

- `recentGameStreak`
- `shareSummaryText`

즉 이번 조각도 write model을 늘린 작업이 아니라,
**기존 browser history 위에 read model을 한 층 더 올린 작업**입니다.

## 4. recent streak를 어떻게 계산하나

여기서 streak는 두 종류로 나눴습니다.

### 4-1. 최근 mode streak

가장 최근 game entry의 mode를 잡고,
같은 mode가 몇 판 연속 이어졌는지 셉니다.

예를 들면 최근 game entry가:

- 수도
- 수도
- 수도
- 국기

순서라면 `수도 3판 연속`입니다.

### 4-2. 최근 clear streak

recent game entry만 다시 읽은 뒤,
앞에서부터 `FINISHED`가 몇 개 연속인지 셉니다.

즉:

- `FINISHED`
- `FINISHED`
- `GAME_OVER`

면 `2판 연속 클리어`입니다.

recommendation entry는 이 streak를 직접 끊지 않습니다.
streak 계산은 `type === "game"`인 기록만 다시 모아서 하기 때문입니다.

이 규칙을 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)에 둔 이유는,
이건 화면 카피가 아니라 **기록 해석 규칙**이기 때문입니다.

## 5. share summary는 어디서 만들어지나

복사용 한 줄 문장도 홈에서 즉석으로 조립하지 않았습니다.

[browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)의
`buildShareSummaryText()`가 아래를 합쳐 텍스트를 만듭니다.

- 최근 기록 수
- 체험한 모드 수
- 최고 점수
- 최근 game streak
- 최근 추천 TOP1

즉 홈은 이 문장을 직접 만드는 곳이 아니라,
**이미 계산된 summary를 보여 주는 곳**입니다.

이렇게 분리한 이유는,
복사 버튼이 생겨도 route shell과 history 계산 규칙이 섞이지 않게 하려는 목적입니다.

## 6. 홈은 무엇만 맡나

[app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)는 이번에도 얇게 유지했습니다.

역할은 두 개뿐입니다.

1. summary object를 렌더링한다
2. 홈에서만 copy interaction을 붙인다

즉 `navigator.clipboard.writeText()`는 홈이 맡지만,
무슨 문자열을 복사할지는 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)가 이미 결정한 상태입니다.

이렇게 하면:

- feature
- history contract
- home shell

이 세 층이 계속 분리됩니다.

## 7. 어디에서 상태가 바뀌나

이번 조각의 상태 변화도 여전히 서버가 아니라 브라우저에 있습니다.

하지만 중요한 점은
**새로운 저장 위치는 늘지 않았다**는 것입니다.

흐름은 아래와 같습니다.

1. 게임 3종은 terminal result일 때 history entry를 남긴다
2. recommendation은 top 3 계산 직후 history entry를 남긴다
3. 홈 `#/`이 열릴 때 [readDemoLiteActivitySummary()](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)를 호출한다
4. summary 안에 recent streak와 share summary가 함께 계산된다
5. 홈은 그 값을 렌더링하고 copy button으로 복사만 수행한다

즉 이번 조각의 핵심은
`feature write -> home read`
구조를 유지한 채
**read 단계의 해석력을 높인 것**입니다.

## 8. 어떤 테스트로 닫았나

이번에는 기존 browser history 테스트를 확장했습니다.

```bash
cd demo-lite
npm test
```

[browser-history.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/browser-history.test.mjs)에서 확인한 것은 아래입니다.

1. recent game streak가 올바르게 계산되는지
2. clear streak가 `FINISHED` 연속 기준으로 계산되는지
3. share summary text에 recent streak와 recent recommendation이 같이 들어가는지

그리고 static build는 다시 아래로 확인했습니다.

```bash
cd demo-lite
npm run build
```

즉 이번 조각의 핵심 검증은
`복사 버튼이 있느냐`가 아니라
**홈이 브라우저 기록을 한 문장 설명으로 다시 묶을 수 있느냐**입니다.

## 9. 이 조각이 왜 중요한가

기능만 보면 아주 작은 polish처럼 보일 수 있습니다.

하지만 `demo-lite`처럼 서버 저장을 뺀 체험판에서는 이런 read model이 꽤 중요합니다.

이제 홈은:

- 단순 최근 기록 모음

이 아니라

- “최근에 어떻게 플레이했고, 지금 무엇을 공유할 수 있는가”를 설명하는 화면

으로 바뀌었습니다.

즉 앱이 조금 더 **사람이 실제로 만져 본 흔적을 요약하는 제품**처럼 보이기 시작한 것입니다.

## 10. 다음으로 볼 것

이제 `demo-lite`는 retained surface 4개와 browser-side summary까지 닫혔습니다.

다음 자연스러운 조각은 둘 중 하나입니다.

1. 실제 static hosting에 올려 공개 URL로 smoke test
2. 복사용 summary에 실제 공개 URL을 포함할지 결정

현재 단계에서는 1번이 더 우선입니다.

## 마무리

이번 조각으로 `demo-lite` 홈에 recent streak와 복사용 한 줄 요약을 붙였습니다. 핵심은 새 저장소나 서버 기능을 늘린 것이 아니라, 기존 localStorage history를 다시 읽어 최근 mode streak, 최근 클리어 streak, share summary text를 계산하게 만든 점입니다. 즉 free-tier 공개용 앱에서도 홈이 단순 링크 모음이 아니라 “지금 이 브라우저에서 어떤 체험을 했는지”를 짧게 설명하는 read model surface가 됐습니다.
