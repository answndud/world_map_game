# 44. dashboard baseline 화면이 현재 엔진 결과를 직접 읽게 만들기

## 왜 이 조각이 필요했나

추천 엔진을 계속 튜닝하다 보니 문제가 하나 생겼다.

- 테스트는 최신 엔진 결과를 기준으로 계속 고정되고 있는데
- `/dashboard/recommendation/persona-baseline`은 예전 weak scenario를 하드코딩으로 보여주고 있었다.

즉, 테스트와 운영 화면이 서로 다른 기준을 보고 있었다.

이 상태로는 운영 화면이 더 이상 믿을 수 있는 baseline 화면이 아니다.

## 무엇을 바꿨나

이번에는 baseline 시나리오 정의를 test helper에만 두지 않고, main source의 공용 카탈로그로 올렸다.

- `RecommendationPersonaBaselineCatalog`
- `RecommendationPersonaBaselineScenario`

이제 18개 baseline 시나리오는 테스트와 dashboard가 같은 카탈로그를 공유한다.

## 요청은 어떻게 흐르는가

운영 화면 요청은 이렇게 흐른다.

1. `GET /dashboard/recommendation/persona-baseline`
2. `AdminPageController`
3. `AdminPersonaBaselineService.loadBaseline()`
4. `RecommendationPersonaBaselineCatalog.scenarios()` 로 18개 시나리오 로드
5. 각 시나리오를 `RecommendationSurveyService.recommend()`에 다시 넣어 현재 top 3 계산
6. `expectedCandidates`와 교집합이 없는 경우만 weak scenario로 분류
7. SSR 템플릿 렌더링

즉, dashboard는 더 이상 “옛날에 정해 둔 weak scenario 목록”을 보여주지 않는다.
현재 엔진이 실제로 어떤 결과를 내는지 다시 계산해서 보여준다.

## 왜 서비스가 맡아야 하나

이 로직은 템플릿이 하면 안 된다.

이유는 weak scenario 판정 자체가 하나의 read model 규칙이기 때문이다.

- 어떤 시나리오를 읽을지
- 현재 top 3를 어떻게 계산할지
- expected 후보와 어떻게 비교할지
- weak / active-signal을 어떻게 나눌지

이건 화면 표현이 아니라 운영 판단 규칙이라 `AdminPersonaBaselineService`가 맡는 편이 맞다.

## 테스트는 어떻게 바뀌었나

기존 테스트도 새 카탈로그를 같이 보게 바꿨다.

- `RecommendationOfflinePersonaCoverageTest`
- `RecommendationOfflinePersonaSnapshotTest`

그리고 운영 화면 전용으로 아래를 추가했다.

- `AdminPersonaBaselineServiceIntegrationTest`

여기서 확인한 핵심은 두 가지다.

1. total scenario는 18개로 유지되는가
2. weak scenario 목록은 실제로 expected 후보와 current top 3가 겹치지 않는 경우만 나오는가

## 이번 조각의 의미

이제 추천 엔진을 다음에 또 튜닝해도, baseline 운영 화면은 자동으로 최신 엔진 결과를 따라간다.

즉,

- 테스트 자산
- 운영 화면
- 추천 엔진

이 세 가지가 같은 baseline 시나리오 정의를 공유하게 됐다.

## 면접에서 어떻게 설명할까

“추천 엔진을 계속 튜닝하는 프로젝트라서, 테스트와 운영 화면이 서로 다른 baseline을 보면 안 됐습니다. 그래서 baseline 시나리오를 공용 카탈로그로 올리고, dashboard가 현재 엔진 결과를 다시 계산해서 weak scenario를 보여주게 만들었습니다. 덕분에 테스트와 운영 화면이 같은 평가 자산을 공유합니다.”
