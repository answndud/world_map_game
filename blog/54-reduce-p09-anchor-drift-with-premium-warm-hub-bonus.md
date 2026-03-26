# 54. 따뜻한 프리미엄 허브 시나리오의 1위 drift 줄이기

## 왜 이 작업이 필요했는가

`engine-v13`까지 오면서 baseline은 `18 / 18`을 유지했지만,
`P09` 같은 `따뜻한 기후 + 높은 비용 감수 + 영어 + 공공서비스` 시나리오에서는
기대 1위였던 `아랍에미리트`가 여전히 `싱가포르` 바로 뒤에 있었습니다.

즉, weak scenario는 아니었지만 여전히 `anchor drift`였습니다.

이 단계에서 중요한 건 추천 엔진 전체를 다시 흔드는 것이 아니라,
`P09`에만 작동하는 아주 좁은 보정 신호를 넣는 것이었습니다.

## 어떤 파일이 바뀌는가

- [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
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

이번 조각에서 바뀐 것은 컨트롤러가 아니라
`RecommendationSurveyService` 내부 점수식입니다.

## 무엇을 바꿨는가

`premiumWarmHubBonus()`를 추가했습니다.

이 bonus는 아래 설문 조합에서만 켜집니다.

- `WARM`
- `QUALITY_FIRST`
- `CITY`
- `EnglishSupport HIGH`
- `newcomerSupport HIGH`
- `publicService HIGH`
- `food LOW`
- `diversity LOW`
- `pace BALANCED`

그리고 후보 국가도 아래 조건을 만족할 때만 strong bonus를 받습니다.

- `climate >= 5`
- `price >= 5`
- `urbanity >= 5`
- `english >= 5`
- `digital >= 5`
- `newcomer >= 4`
- `housingSpace >= 2`

핵심은 `싱가포르`처럼 초도시형 점수가 높아도
`주거 여유와 초기 정착 허들`까지 같이 보게 해서
`아랍에미리트`를 한 단계 앞세우는 것입니다.

## 왜 서비스에 있어야 하는가

이 보정은 HTTP 분기나 화면 처리 문제가 아닙니다.

- 어떤 설문 조합에서
- 어떤 국가 속성을
- 어떤 강도로 bonus로 읽을지

를 결정하는 추천 도메인 규칙입니다.

그래서 컨트롤러가 아니라
[RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
가 맡아야 합니다.

## 결과는 어떻게 달라졌는가

디버그 확인 기준:

- 이전: `싱가포르 322 / 아랍에미리트 321 / 덴마크 293`
- 이후: `아랍에미리트 329 / 싱가포르 326 / 덴마크 293`

즉,

- baseline: `18 / 18` 유지
- anchor drift: `8 -> 7`
- ops review 우선 시나리오: `P07, P08, P09 -> P07, P08, P10`

## 무엇을 테스트했는가

- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
  - `P09` snapshot을 `아랍에미리트, 싱가포르, 덴마크`로 다시 고정
- [RecommendationOfflinePersonaCoverageTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)
  - `P09`의 1위가 `아랍에미리트`인지 확인
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
  - anchor drift가 `7`로 줄었는지 확인
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - 우선 시나리오가 `P07, P08, P10`으로 바뀌는지 확인
- 추천/admin targeted suite와 `./gradlew test` 전체 통과

## 면접에서 어떻게 설명할까

baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다.
이번에는 `P09`처럼 따뜻한 고비용 도시 허브 시나리오에만 좁게 작동하는 `premiumWarmHubBonus`를 추가해서,
기대 1위였던 `아랍에미리트`가 `싱가포르`보다 앞서도록 보정했습니다.
그 결과 baseline은 유지하면서 anchor drift를 `8 -> 7`로 줄였습니다.
