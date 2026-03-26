# 60. 탐색형 자연 정착 시나리오의 1위 drift 줄이기

## 왜 이 작업이 필요했는가

`engine-v19`까지 오면서 baseline은 `18 / 18`을 유지했지만,
`P15` 같은 `온화한 기후 + 자연 + 저비용 + 교통 중심 + 가볍게 살아보기` 시나리오에서는
기대 1위였던 `뉴질랜드`보다 `포르투갈`이 계속 먼저 나오고 있었습니다.

즉, 이것도 weak scenario는 아니고
`anchor drift`였습니다.

이 단계에서 중요한 건 탐색형 자연 시나리오 전체를 다시 흔드는 것이 아니라,
`P15`에만 가까운 조합에서 `뉴질랜드`처럼 영어 적응과 여유, 안전, 생활 공간이 함께 받쳐주는 후보를
아주 좁게 올리는 것이었습니다.

## 어떤 파일이 바뀌는가

- [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
- [RecommendationSurveyServiceTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java)
- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
- [RecommendationOfflinePersonaCoverageTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)
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

`exploratoryNatureRunwayBonus()`를 추가했습니다.

이 bonus는 아래 조합에서만 켜집니다.

- `MILD`
- `SeasonStyle BALANCED`
- `SeasonTolerance MEDIUM`
- `Pace RELAXED`
- `Crowd CALM`
- `VALUE_FIRST`
- `SPACE_FIRST`
- `NATURE`
- `TRANSIT_FIRST`
- `EnglishSupport MEDIUM`
- `NewcomerSupport LOW`
- `Safety HIGH`
- `PublicService MEDIUM`
- `Digital LOW`
- `Food LOW`
- `Diversity LOW`
- `Culture LOW`
- `WorkLife LIFE_FIRST`
- `Settlement EXPERIENCE`
- `FutureBase LIGHT_START`

그리고 후보 국가는 아래 조건을 만족할 때만 strong bonus를 받습니다.

- `climateValue 2~3`
- `seasonality >= 4`
- `paceValue <= 1`
- `urbanityValue <= 3`
- `englishSupport >= 5`
- `safety >= 5`
- `housingSpace >= 5`
- `newcomerFriendliness >= 4`
- `priceLevel <= 4`

핵심은 “저비용 자연 시나리오” 전반을 올리는 것이 아니라,
`먼저 가볍게 살아보면서도 영어 적응과 안전, 생활 공간이 함께 받쳐주는 탐색형 정착지`를 읽는 것입니다.

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

테스트 확인 기준:

- 이전: `포르투갈 / 뉴질랜드 / 말레이시아`
- 이후: `뉴질랜드 / 포르투갈 / 말레이시아`

즉,

- baseline: `18 / 18` 유지
- anchor drift: `2 -> 1`
- ops review 우선 시나리오: `P07, P15 -> P07`

## 무엇을 테스트했는가

- [RecommendationSurveyServiceTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java)
  - `P15`와 같은 입력 조합에서 `뉴질랜드`가 1위로 올라오는지 확인
- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
  - `P15` snapshot을 `뉴질랜드, 포르투갈, 말레이시아`로 다시 고정
- [RecommendationOfflinePersonaCoverageTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)
  - `P15`가 `뉴질랜드`, `말레이시아`를 계속 포함하고 엉뚱한 저품질 후보가 끼지 않는지 확인
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
  - anchor drift가 `1`로 줄었는지 확인
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - 우선 시나리오가 `P07` 하나로 좁혀지는지 확인
- 추천/admin targeted suite와 `./gradlew test` 전체 통과

## 면접에서 어떻게 설명할까

baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다.
이번에는 `P15`처럼 자연형 탐색 정착 시나리오에만 좁게 작동하는 `exploratoryNatureRunwayBonus`를 추가해서,
기대 1위였던 `뉴질랜드`가 `포르투갈`보다 앞서도록 보정했습니다.
그 결과 baseline은 유지하면서 anchor drift를 `2 -> 1`로 줄였습니다.
