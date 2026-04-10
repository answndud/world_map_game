# 120. demo-lite recommendation 결과를 한 줄로 설명하고 공유할 수 있게 다듬기

## 왜 이 조각이 필요했나

`demo-lite` recommendation은 이미 `20문항 -> top 3 국가` 흐름을 끝까지 보여 준다.

하지만 결과 화면을 보고 바로 이런 문장을 만들기는 어려웠다.

- "내가 어떤 기준으로 골랐는지"
- "왜 1위가 이 나라인지"
- "이 결과를 친구에게 한 줄로 어떻게 보낼지"

즉 계산은 끝나는데, **설명과 공유는 아직 덜 닫힌 상태**였다.

이번 조각의 목적은 추천 엔진을 더 복잡하게 만드는 게 아니라, 이미 계산된 결과를 플레이어 언어로 한 번 더 정리하는 것이다.

## 무엇을 바꿨나

핵심 파일은 아래다.

- [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)
- [recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)

[recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)에 결과 read model을 하나 더 붙였다.

추가된 값은 아래 네 가지다.

- `summary.headline`
- `summary.highlightLabels`
- `summary.narrative`
- `summary.shareText`

즉 top 3만 계산하고 끝내는 대신, 그 결과를 다시 **짧게 읽는 층**을 만들었다.

## 요청은 어디서 시작되고 어디서 상태가 바뀌나

흐름 자체는 바뀌지 않는다.

1. `#/recommendation`에서 20문항을 제출한다.
2. [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)가 top 3 국가를 계산한다.
3. 같은 파일 안에서 답변을 다시 읽어 `선호 키워드 3~4개`를 뽑는다.
4. top 1 국가와 그 이유를 이용해 설명 문장과 share text를 만든다.
5. 결과 화면은 `추천 요약 -> 공유 문장 -> top 3 카드` 순으로 렌더된다.

중요한 점은 상태가 바뀌는 곳이 늘지 않았다는 것이다.

- 서버 저장 없음
- localStorage feedback 없음
- recommendation recent top1 기록만 기존과 동일

즉 이번 조각은 **새 persistence가 아니라 결과 표현 계층 추가**다.

## 왜 이 로직이 app shell이 아니라 recommendation runtime 안에 있어야 하나

이 요약은 단순한 문구 꾸미기가 아니다.

아래 두 정보를 동시에 알아야 한다.

- 어떤 답변이 들어왔는가
- 어떤 나라가 top 1~3으로 계산됐는가

이건 route를 붙이는 [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)보다, 실제 추천 계산을 아는 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)가 갖고 있어야 더 설명 가능하다.

만약 app shell에서 따로 문자열을 조립하면:

- 계산 로직과 결과 해석 로직이 분리되고
- 테스트도 두 파일을 같이 열어야 하고
- "왜 이 결과가 나왔는지"를 추적하기 어려워진다

그래서 이번에도 recommendation domain/runtime 파일 안에서 summary를 만들고, 화면은 그 결과를 렌더만 하게 두었다.

## 어떤 방식으로 요약했나

전체 20문항을 모두 자연어로 풀어쓰면 오히려 길어진다.

그래서 아래처럼 핵심 신호만 뽑는다.

- 기후
- 도시 vs 자연
- 비용 vs 품질
- 영어 적응 / 안전 / 공공 서비스 같은 강한 정착 신호
- 필요할 때 pace, 장기 정착 방향

이렇게 만든 `highlightLabels`를 바탕으로:

- 결과 헤드라인
- top1 설명 문장
- 공유용 한 줄 텍스트

를 만든다.

예를 들어 `뉴질랜드`가 1위인 탐색형 시나리오라면, `자연 접근성`, `비용 효율`, `느긋한 리듬` 같은 키워드가 같이 따라붙는다.

## 복사 기능은 어떻게 닫았나

결과 카드 아래에 `요약 복사` 버튼을 추가했다.

동작 순서는 단순하다.

1. 가능하면 `navigator.clipboard.writeText()` 사용
2. 실패하면 hidden textarea + `execCommand("copy")` fallback
3. 둘 다 실패하면 문장은 그대로 남겨 두고, 직접 복사 안내만 표시

즉 "복사 기능이 안 되면 화면이 깨진다"가 아니라, **자동 복사만 실패하고 내용은 그대로 읽을 수 있게** 만들었다.

## 무엇을 테스트했나

[recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)에서 아래를 추가로 고정했다.

- 결과 객체에 `summary`가 항상 포함되는지
- `summary.narrative`가 실제 top1 국가명을 포함하는지
- `summary.highlightLabels`가 최소 3개 이상 생성되는지
- 탐색형 뉴질랜드 시나리오에서 `자연 접근성`, `비용 효율`, `1위 뉴질랜드`가 share text에 들어가는지

검증 명령은 그대로 아래다.

```bash
cd demo-lite
npm test
npm run build
npm run verify:pages
```

## 이번 조각으로 무엇이 좋아졌나

이제 demo-lite recommendation은:

- 단순히 top 3 카드를 나열하는 화면이 아니라
- "내 취향이 어떤 키워드로 읽혔는지"
- "왜 1위가 이 나라인지"
- "이 결과를 어떻게 공유할지"

까지 한 화면 안에서 설명할 수 있다.

즉 결과 화면이 더 예뻐진 것보다, **사용자가 직접 설명하기 쉬운 구조**에 한 걸음 더 가까워졌다.

## 다음 조각은 무엇인가

이 다음에 가장 자연스러운 후보는:

- top 3 비교 기준 breakdown
- 질문 프리셋 / persona quick start

같은 해석 보조 기능이다.

반대로 persistence나 ops review를 demo-lite에 넣는 건 여전히 범위를 벗어난다.
