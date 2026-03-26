# 52. 온화한 공공서비스형 시나리오의 1위 drift 줄이기

## 왜 이 작업이 필요했는가

`engine-v11`까지 오면서 추천 엔진은 `baseline 18 / 18`을 유지하고 있었습니다.

즉,
기대 후보가 top 3 안에 아예 안 들어오는 `weak scenario`는 이미 0개였습니다.

이 시점부터 더 중요한 것은
`기대 1위가 실제로도 1위인가`였습니다.

이번 조각에서 본 시나리오는 `P04`입니다.

- 온화한 기후
- 사계절 변화는 어느 정도 있는 편
- 생활 속도는 너무 빠르지도 느리지도 않음
- 도시와 자연의 균형
- 영어 지원은 중간 정도면 충분
- 공공서비스와 기본 생활 안정성이 중요

그런데 현재 엔진은 이 시나리오에서

```text
스페인 > 아일랜드 > 우루과이
```

를 주고 있었습니다.

문제는 방향 자체는 맞지만,
기대 1위였던 `우루과이`가 아직 3위라는 점이었습니다.

즉, 이것도 weak scenario는 아니지만 `anchor drift`였습니다.

## 어떤 파일이 바뀌는가

- [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
- [RecommendationPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)
- [RecommendationFeedbackIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java)
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)
- [recommendation-feedback.html](/Users/alex/project/worldmap/src/main/resources/templates/admin/recommendation-feedback.html)

## 요청은 어떻게 흐르는가

런타임 추천 흐름은 그대로 유지했습니다.

1. `GET /recommendation/survey`
2. `POST /recommendation/survey`
3. [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java) 가 점수 계산
4. `recommendation/result` SSR 렌더링
5. `POST /api/recommendation/feedback`

이번 조각에서 바뀐 것은
`RecommendationSurveyService.recommend()` 내부 점수식뿐입니다.

즉, 컨트롤러나 라우트는 건드리지 않고
현재 엔진이 어떤 후보를 1위로 미는지만 더 정교하게 조정했습니다.

## 왜 이 로직이 서비스에 있어야 하는가

이번 보정은 화면 문구나 폼 검증이 아닙니다.

아래 조합일 때만
“온화한 기후, 공공서비스, 기본 정착 안정성”을
조금 더 높게 보는 추천 규칙입니다.

- `MILD`
- `BALANCED`
- `MIXED`
- `EnglishSupport MEDIUM`
- `publicService HIGH`
- `settlement BALANCED`
- `mobility BALANCED`

이건 HTTP 처리나 템플릿 조건이 아니라
`추천 점수 계산 규칙`이므로
컨트롤러가 아니라 서비스에 있어야 합니다.

## 이번에 넣은 보정

새로 추가한 것은 `temperatePublicBaseBonus()`입니다.

핵심 아이디어는 이렇습니다.

- 온화한 나라라고 해서 무조건 맞는 것은 아니다.
- 사계절 변화, 안전, 공공서비스, 디지털 생활 편의, 적당한 주거 여유가 같이 맞아야 한다.
- 즉, “살기 편한 온화한 균형형 나라”를 별도 신호로 봐야 한다.

이번 보정은 아주 좁게 열었습니다.

- `priceLevel <= 3`
- `climateValue <= 3`
- `seasonality >= 4`
- `safety >= 4`
- `welfare >= 3`
- `housingSpace >= 4`
- `digitalConvenience >= 4`

이 조건을 만족하면 strong bonus,
조금 약한 경우에는 half bonus만 주도록 나눴습니다.

## 실제로 어떻게 바뀌었는가

보정 전 `P04` top 3:

```text
스페인 350 / 아일랜드 336 / 우루과이 331
```

보정 후 `P04` top 3:

```text
우루과이 351 / 스페인 350 / 아일랜드 336
```

즉,
top 3 후보 구성은 유지하면서
기대 1위 anchor만 `우루과이`로 되돌렸습니다.

## 운영 화면에는 어떤 영향이 있었는가

이 변화는 `/dashboard/recommendation/persona-baseline`과
`/dashboard/recommendation/feedback`에도 바로 반영됩니다.

- baseline: `18 / 18` 유지
- weak scenario: `0`
- anchor drift: `10 -> 9`
- ops review 우선 시나리오: `P04, P06, P07 -> P06, P07, P08`

즉, `P04`는 이제 운영 우선순위 top 3에서 빠졌습니다.

## 테스트는 무엇을 했는가

- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
  - `P04 -> 우루과이, 스페인, 아일랜드`로 snapshot 고정
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
  - anchor drift가 `9`로 줄었는지 확인
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - 우선 시나리오가 `P06, P07, P08`으로 바뀌는지 확인
- [RecommendationPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)
- [RecommendationFeedbackIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java)
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)
- 마지막으로 `./gradlew test` 전체 통과

## 한계와 다음 단계

이번 조각은 `P04`의 1위 drift를 줄이는 데는 성공했습니다.

하지만 아직 `anchor drift`는 `9개` 남아 있습니다.
즉, weak scenario는 없어도
기대 1위가 아직 어긋나는 시나리오는 계속 관리해야 합니다.

그래서 다음 단계는 broad bonus를 더 키우는 것이 아니라,
현재 ops review가 가리키는

- `P06`
- `P07`
- `P08`

중 하나만 골라 다시 좁게 보는 것입니다.

## 면접에서는 이렇게 설명할 수 있다

baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P04`처럼 온화한 기후와 공공서비스를 함께 보는 균형형 시나리오에만 좁게 작동하는 `temperatePublicBaseBonus`를 추가해서, 기대 1위였던 우루과이가 스페인보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `10 -> 9`로 줄였습니다.
