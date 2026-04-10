# 121. demo-lite recommendation에 top3 비교 기준 카드를 붙이기

## 왜 이 조각이 필요했나

이전 조각에서 `demo-lite` recommendation 결과 화면에는 아래가 들어갔다.

- 선호 키워드
- top1 설명 문장
- 공유용 한 줄 요약

하지만 아직 부족한 부분이 하나 남아 있었다.

`1위는 알겠는데 2위, 3위와 뭐가 다른가?`

즉 결과는 나오지만, **후보 간 차이를 같은 기준으로 비교해서 읽는 화면**은 없었다.

이번 조각은 그 공백을 메우는 작업이다.

## 무엇을 만들었나

핵심 파일은 아래다.

- [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)
- [style.css](/Users/alex/project/worldmap/demo-lite/src/style.css)
- [recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)

[recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)에 `comparison.rows`를 추가했다.

이 구조는 아래처럼 생긴다.

- `title`
- `selectedLabel`
- `candidates`

그리고 축은 현재 다섯 개로 고정했다.

- 기후 감각
- 생활 환경
- 생활비 감각
- 초기 적응
- 정착 기반

즉 top3 국가를 이 다섯 축에서 같은 방식으로 다시 읽게 만든다.

## 요청은 어디서 시작되고 어디서 상태가 바뀌나

흐름은 바뀌지 않는다.

1. 사용자가 `#/recommendation`에서 20문항을 제출한다.
2. [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)가 기존처럼 top3 국가를 계산한다.
3. 같은 파일 안에서 결과를 다시 읽어 `comparison.rows`를 만든다.
4. 결과 화면은 `추천 요약 -> 비교 기준 카드 -> 내가 고른 기준 -> top3 카드` 순으로 렌더된다.

즉 이번 조각에서 바뀐 상태는 없다.

- 새 저장 없음
- 새 API 없음
- localStorage 변경 없음

추가된 건 **결과를 다시 읽는 표현 계층**이다.

## 왜 이 로직이 recommendation runtime 안에 있어야 하나

비교 카드도 route shell에서 만드는 대신 recommendation runtime 안에 둔 이유는 단순하다.

이 카드는 아래 두 데이터를 동시에 알아야 한다.

- 사용자의 답변
- top3 추천 결과

이건 [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js) 같은 shell보다, 실제 추천 계산을 알고 있는 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)가 책임지는 편이 더 설명 가능하다.

그래야:

- summary
- comparison
- top3

가 같은 source of truth를 본다.

## 왜 raw score 표 대신 descriptor 카드로 갔나

가장 쉬운 방법은 숫자 표를 그대로 뿌리는 것이다.

하지만 demo-lite에는 그게 오히려 과했다.

- 화면이 디버그처럼 보일 수 있고
- 점수 계산식 설명이 길어지고
- 공개 체험판 톤과 안 맞는다

그래서 이번에는 raw score 대신, profile 값에서 바로 읽을 수 있는 짧은 descriptor를 썼다.

예를 들어:

- `climateValue` -> `따뜻한 편 / 온화한 편 / 선선한 편`
- `urbanityValue` -> `도시 쪽 / 균형형 / 자연 쪽`
- `priceLevel` -> `부담 낮음 / 중간 / 높은 편`
- `englishSupport + newcomer + digital` -> `초기 적응 쉬움 / 보통 / 준비 필요`
- `futureBase()` -> `기반 강함 / 균형형 / 가벼운 편`

이 방식이면 사용자는 숫자를 해석하지 않아도, 후보 차이를 바로 읽을 수 있다.

## 무엇을 테스트했나

[recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)에서 아래를 고정했다.

- result에 `comparison.rows`가 들어 있는지
- row 제목이 다섯 축으로 고정되는지
- 모든 row가 top3 후보 3개를 다 가지는지
- `뉴질랜드` 탐색형 시나리오에서 `정착 기반` row 안에 `뉴질랜드 / 기반 강함` 같은 descriptor가 실제로 나오는지

검증 명령은 아래다.

```bash
cd demo-lite
npm test
npm run build
npm run verify:pages
```

## 이번 조각으로 무엇이 좋아졌나

이제 demo-lite recommendation 결과는:

- 1위가 누구인지
- 왜 1위가 나왔는지
- 2위, 3위는 어디가 다른지

를 같은 화면 안에서 더 자연스럽게 설명할 수 있다.

즉 공개 데모 recommendation이 단순한 `top3 카드 모음`에서, **비교 가능한 결과 화면**으로 한 단계 올라왔다.

## 다음 조각은 무엇인가

이 다음에 더 자연스러운 후보는:

- 대표 페르소나 프리셋
- quick start 질문 세트

같이 설문 진입 비용을 줄이는 조각이다.

반대로 raw score 디버그 표를 그대로 공개 화면에 넣는 건 현재 demo-lite 톤과는 맞지 않는다.
