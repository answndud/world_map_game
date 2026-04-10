# 119. demo-lite recommendation을 `survey-v4 / engine-v20` 기준으로 잠그기

## 왜 이 조각이 필요했나

`demo-lite` recommendation은 이미 `20문항 + 30국가` 구조까지 와 있었다.

문제는 "어디까지 메인 앱과 같은가"를 설명하는 기준이 약했다는 점이다.

- 질문 수는 같다
- 후보 국가 수도 같다
- top 3도 deterministic하게 나온다

하지만 이 정도만으로는 `현재 메인 엔진과 같은 버전인가`, `대표 시나리오에서 같은 1위가 나오는가`를 바로 말하기 어렵다.

그래서 이번 조각은 기능 추가보다 **정합성 고정**이 목적이다.

## 무엇을 바꿨나

핵심 파일은 아래다.

- [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)
- [recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)
- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [docs/DEMO_LITE_SCOPE_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_SCOPE_PLAN.md)

[recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)에 아래 버전 상수를 추가했다.

- `DEMO_LITE_RECOMMENDATION_SURVEY_VERSION = "survey-v4"`
- `DEMO_LITE_RECOMMENDATION_ENGINE_VERSION = "engine-v20"`

그리고 [recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)에 메인 recommendation 테스트의 대표 anchor scenario를 그대로 옮겼다.

- `USA`: temperate high english global city
- `CAN`: temperate family bridge
- `MYS`: warm value balanced public
- `NZL`: exploratory nature runway

즉 이제 demo-lite recommendation은 `문항 수가 비슷한 브라우저 포트`가 아니라, **현재 메인 버전 라벨과 대표 시나리오까지 같이 가진 브라우저 포트**라고 설명할 수 있다.

## 요청은 어디서 시작되고 상태는 어디서 바뀌나

요청 시작점은 여전히 `#/recommendation`이다.

흐름은 단순하다.

1. [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 route를 렌더한다.
2. [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)가 20문항 답변을 받는다.
3. 같은 파일 안의 계산 함수가 30개 국가 프로필을 점수화해 top 3를 만든다.
4. 결과 화면은 브라우저에서 바로 렌더된다.
5. 최근 추천 1위만 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)에 저장된다.

이번 조각에서 바뀐 건 2번의 계산 계약을 설명하는 기준이다.

즉 추천 결과를 계산하는 핵심 로직은 여전히 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)에 있어야 한다.
이건 컨트롤러 비슷한 route shell이 아니라, 추천 규칙 자체를 들고 있는 domain 성격의 코드이기 때문이다.

## 왜 서비스/도메인 자리에 둬야 하나

메인 앱에서는 이 책임이 `RecommendationSurveyService`에 있다.

demo-lite에는 Spring service가 없지만, 역할은 같다.

- route shell은 입력을 모은다
- recommendation runtime은 점수와 순위를 계산한다

만약 이 계산을 [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js) 같은 route 조립 코드에 흩뿌리면:

- 버전 상수를 어디가 책임지는지 흐려지고
- 대표 시나리오 테스트를 한 파일 기준으로 잠그기 어려워지고
- "왜 이 나라가 1위인가"를 설명할 때 읽어야 할 파일이 늘어난다

그래서 이번 조각도 계산 기준은 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)에 모으고, 테스트는 [recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)에서 잠그는 구조를 유지했다.

## 무엇을 테스트했나

아래를 고정했다.

- recommendation route가 여전히 `20문항 / 30국가` 구조인지
- 버전 상수가 `survey-v4 / engine-v20`인지
- 메인 anchor scenario 4개에서 demo-lite top1이 같은지

실행 명령은 아래다.

```bash
cd demo-lite
npm test
npm run build
npm run verify:pages
```

즉 이번 검증은 "화면이 뜬다"보다 "설명 기준이 메인과 맞다"를 먼저 본다.

## 아직 의도적으로 안 한 것

이번 조각은 아래를 하지 않았다.

- feedback 저장
- ops review 화면
- baseline 18개 전체를 demo-lite에 복제

이건 의도적이다.

`demo-lite`는 정적 공개 체험판이라서, 메인 운영 루프까지 다 복제하면 sibling app을 나눈 이유가 약해진다.

## 다음 조각은 무엇인가

이제 recommendation 쪽에서 더 자연스러운 다음 조각은:

- 추천 결과 설명 카드 polish
- 공유용 summary copy polish

같은 **결과 표현 개선**이다.

반대로 `feedback persistence`나 `ops baseline`을 demo-lite에 얹는 건 현재 분리 기준과 맞지 않는다.
