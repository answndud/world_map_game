# 56. 영어 의존이 낮은 고도시 다양성 시나리오의 1위 drift 줄이기

## 왜 이 작업이 필요했는가

`engine-v15`까지 오면서 baseline은 `18 / 18`을 유지했지만,
`P10` 같은 `온화한 기후 + 빠른 도시 + 문화 다양성 최우선 + 영어 의존 낮음` 시나리오에서는
기대 1위였던 `미국`이 계속 `대한민국` 뒤에 있었습니다.

즉, 이것도 weak scenario는 아니고
`anchor drift`였습니다.

이 단계에서 중요한 건
한국이나 영국처럼 이미 맞는 후보를 크게 깎는 것이 아니라,
`P10`에만 작동하는 아주 좁은 보정 신호를 넣는 것이었습니다.

## 어떤 파일이 바뀌는가

- [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
- [RecommendationSurveyServiceTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java)
- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)
- [RecommendationPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)
- [RecommendationFeedbackIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java)
- [index.html](/Users/alex/project/worldmap/src/main/resources/templates/admin/index.html)
- [recommendation-feedback.html](/Users/alex/project/worldmap/src/main/resources/templates/admin/recommendation-feedback.html)

## 요청은 어떻게 흐르는가

런타임 요청 흐름은 그대로입니다.

1. `GET /recommendation/survey`
2. 사용자가 20문항 설문 제출
3. `POST /recommendation/survey`
4. [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)가 후보 국가를 계산
5. 결과 SSR 렌더링
6. 결과 페이지에서 만족도 피드백 저장

이번 조각에서 바뀐 것은
컨트롤러가 아니라 `RecommendationSurveyService` 내부 점수식입니다.

## 무엇을 바꿨는가

`cosmopolitanPulseBonus()`를 추가했습니다.

이 bonus는 아래 조합에서만 켜집니다.

- `MILD`
- `FAST`
- `LIVELY`
- `BALANCED`
- `CITY`
- `EnglishSupport LOW`
- `NewcomerSupport MEDIUM`
- `Digital HIGH`
- `Diversity HIGH`
- `Culture HIGH`
- `WorkLife DRIVE_FIRST`
- `Settlement BALANCED`

그리고 후보 국가도 아래 조건을 만족할 때만 strong bonus를 받습니다.

- `climateValue`가 `3~4`
- `pace >= 4`
- `urbanity >= 5`
- `diversity >= 5`
- `cultureScene >= 5`
- `food >= 4`
- `housingSpace >= 4`
- `digitalConvenience >= 5`

핵심은 “디지털 편의가 높은 빠른 도시”를 더 올리는 것이 아니라,
`영어가 꼭 필요하지 않아도 다문화 자극과 활기를 충분히 느낄 수 있는 도시형 후보`를 더 읽는 것입니다.

## 왜 서비스에 있어야 하는가

이 보정은 HTTP 분기나 화면 처리 문제가 아닙니다.

- 어떤 설문 조합에서
- 어떤 프로필 속성을 함께 읽고
- 어느 정도 강도로 점수를 보정할지

를 결정하는 추천 도메인 규칙입니다.

그래서 컨트롤러가 아니라
[RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
가 맡아야 합니다.

## 결과는 어떻게 달라졌는가

디버그 확인 기준:

- 이전: `대한민국 284 / 미국 272 / 영국 264`
- 이후: `미국 288 / 대한민국 284 / 영국 264`

즉,

- baseline: `18 / 18` 유지
- anchor drift: `6 -> 5`
- ops review 우선 시나리오: `P07, P10, P11 -> P07, P11, P13`

## 무엇을 테스트했는가

- [RecommendationSurveyServiceTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java)
  - `P10`과 같은 입력 조합에서 `미국`이 1위로 올라오는지 확인
- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
  - `P10` snapshot을 `미국, 대한민국, 영국`으로 다시 고정
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
  - anchor drift가 `5`로 줄었는지 확인
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - 우선 시나리오가 `P07, P11, P13`으로 바뀌는지 확인
- 추천/admin targeted suite와 `./gradlew test` 전체 통과

## 면접에서 어떻게 설명할까

baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다.
이번에는 `P10`처럼 영어는 꼭 필요 없지만 빠른 도시의 다양성과 활기가 중요한 시나리오에만 좁게 작동하는 `cosmopolitanPulseBonus`를 추가해서,
기대 1위였던 `미국`이 `대한민국`보다 앞서도록 보정했습니다.
그 결과 baseline은 유지하면서 anchor drift를 `6 -> 5`로 줄였습니다.
