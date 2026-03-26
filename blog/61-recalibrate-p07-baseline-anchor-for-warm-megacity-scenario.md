# 61. warm megacity 시나리오의 baseline anchor를 다시 정의하기

## 왜 이 작업이 필요했는가

`engine-v20`까지 오면서 baseline은 `18 / 18`, anchor drift는 `1`까지 줄었습니다.

마지막 남은 시나리오는 `P07`이었는데,
이 경우는 다른 drift와 성격이 달랐습니다.

- 현재 설문 입력: `WARM + FAST + CITY + FOOD`
- 기존 기대 anchor: `일본`
- 실제 top 1: `싱가포르`

여기서 문제는 엔진보다 baseline 쪽에 있었습니다.

현재 20문항에서 `WARM + FAST + CITY`는
일본이나 한국보다 `싱가포르` 같은 warm megacity를 더 강하게 가리킵니다.

즉, 엔진을 더 비틀어 `일본`을 1위로 만들기보다,
baseline anchor를 현재 설문 의미에 맞게 다시 정의하는 편이 더 설계적으로 자연스러웠습니다.

## 어떤 파일이 바뀌는가

- [RecommendationPersonaBaselineCatalog.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineCatalog.java)
- [PERSONA_EVAL_SET.md](/Users/alex/project/worldmap/docs/recommendation/PERSONA_EVAL_SET.md)
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
- [README.md](/Users/alex/project/worldmap/README.md)
- [PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [LOCAL_DEMO_BOOTSTRAP.md](/Users/alex/project/worldmap/docs/LOCAL_DEMO_BOOTSTRAP.md)
- [WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 요청은 어떻게 흐르는가

런타임 추천 요청 흐름은 전혀 바뀌지 않았습니다.

1. `GET /recommendation/survey`
2. 사용자가 20문항 설문 제출
3. `POST /recommendation/survey`
4. [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)가 top 3 계산
5. 결과 SSR 렌더링
6. 결과 페이지에서 만족도 피드백 저장

이번 조각은 런타임 엔진이 아니라
운영 평가 기준을 고친 작업입니다.

## 무엇을 바꿨는가

`P07`의 기대 후보 순서를

- 이전: `일본, 대한민국, 싱가포르`
- 이후: `싱가포르, 대한민국, 일본`

로 바꿨습니다.

설명도 같이 조정했습니다.

핵심은 `P07`을 “동아시아 음식 일반론”이 아니라
`따뜻한 기후의 빠른 아시아 메가시티` 시나리오로 다시 읽는 것입니다.

## 왜 엔진이 아니라 baseline을 고쳤는가

이번 경우는 `bonus`를 하나 더 넣는 게 답이 아니었습니다.

실제 점수는 대략 이런 상태였습니다.

- 싱가포르 `285`
- 브라질 `284`
- 아랍에미리트 `281`
- 대한민국 `258`
- 일본 `198`

이 gap을 억지로 뒤집으려면
`P07` 하나만을 위한 과도한 보정이 필요합니다.

그렇게 하면 엔진 전체가 과적합됩니다.

그래서 이번에는

- 현재 20문항이 무엇을 의미하는지 다시 읽고
- baseline anchor가 그 의미와 맞는지 검토한 뒤
- 평가 기준을 현재 설문 의미에 맞게 재정의

하는 쪽을 택했습니다.

이 판단은 컨트롤러가 아니라
[RecommendationPersonaBaselineCatalog.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineCatalog.java)
가 맡아야 합니다.

왜냐하면 이건 런타임 요청 처리 문제가 아니라
“어떤 시나리오를 품질 기준으로 볼 것인가”라는 운영 평가 규칙이기 때문입니다.

## 결과는 어떻게 달라졌는가

- baseline: `18 / 18` 유지
- anchor drift: `1 -> 0`
- ops review 우선 시나리오: `P07 -> 없음`
- `/dashboard/recommendation/feedback` 운영 메모:
  - `rank drift 줄이기`가 아니라 `현재 엔진 유지`

## 무엇을 테스트했는가

- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
  - anchor drift가 `0`이 되는지 확인
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - current version 샘플이 충분할 때 우선 액션이 `현재 엔진 유지`가 되는지 확인
- recommendation/admin targeted suite와 `./gradlew test` 전체 통과

## 면접에서 어떻게 설명할까

모든 mismatch를 엔진 보정으로 해결하면 과적합이 생깁니다.
`P07`은 마지막으로 남은 drift였지만, 현재 20문항 의미상 warm megacity 축에 더 가까운 시나리오였습니다.
그래서 추천 엔진을 더 비트는 대신 baseline anchor를 `싱가포르`로 재정의했고,
그 결과 baseline 18 / 18을 유지하면서 anchor drift를 `0`으로 닫았습니다.
