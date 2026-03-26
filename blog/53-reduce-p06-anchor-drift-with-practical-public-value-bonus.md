# 53. 현실형 온화 기후 시나리오의 1위 drift 줄이기

## 왜 이 작업이 필요했는가

`engine-v12`까지 오면서 추천 엔진은 `baseline 18 / 18`을 유지했고,
`P04`도 해결된 상태였습니다.

이 시점의 다음 우선순위는 `P06`이었습니다.

`P06`은 이런 사용자입니다.

- 너무 덥거나 추운 곳보다 온화한 기후가 좋다
- 생활 리듬은 너무 빠르지 않은 쪽이 좋다
- 생활비는 과하게 높지 않아야 한다
- 도시와 자연은 적당히 섞여 있으면 좋다
- 치안은 중요하다
- 공공서비스도 무시할 수는 없다

즉, “온화하면서도 현실적으로 오래 버티기 쉬운 나라”를 찾는 시나리오입니다.

그런데 `engine-v12`는 여기서

```text
스페인 > 우루과이 > 포르투갈
```

를 주고 있었습니다.

방향은 맞지만,
기대 1위였던 `우루과이`가 아직 `스페인` 뒤에 있는 상태였습니다.

이번에도 weak scenario는 아니지만 `anchor drift`였습니다.

## 어떤 파일이 바뀌는가

- [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
- [RecommendationOfflinePersonaCoverageTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)
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

## 왜 이 로직이 서비스에 있어야 하는가

이번 보정은 화면 문구나 폼 검증 규칙이 아닙니다.

아래 조합일 때만
“비용을 아끼면서도 치안·복지·주거 안정성이 있는 온화한 나라”를
조금 더 높게 보는 추천 규칙입니다.

- `MILD`
- `VALUE_FIRST`
- `SAFETY HIGH`
- `publicService MEDIUM`
- `English MEDIUM`
- `newcomer LOW`
- `MIXED`
- `BALANCED settlement / mobility`

이건 `추천 점수 계산 규칙`이므로
컨트롤러가 아니라 서비스에 있어야 합니다.

## 이번에 넣은 보정

새로 추가한 것은 `practicalPublicValueBonus()`입니다.

핵심 아이디어는 이렇습니다.

- `P06`은 단순히 “싸고 안전한 나라”를 찾는 것이 아니다.
- 온화한 기후, 과하지 않은 도시 밀도, 안정적인 복지·주거 기반이 같이 맞아야 한다.
- 그래서 `priceLevel + climateValue + safety + welfare + housingSpace + newcomerFriendliness`를 함께 보는 좁은 signal이 필요했다.

이번 bonus는 아래처럼 매우 좁게 열었습니다.

- `priceLevel <= 3`
- `climateValue <= 3`
- `safety >= 5`
- `welfare >= 4`
- `housingSpace >= 4`
- `digitalConvenience >= 3`
- `newcomerFriendliness >= 3`
- `urbanityValue <= 3`
- `paceValue <= 2`

이 조건이면 `우루과이`는 strong bonus를 받고,
`스페인`은 기후와 주거 안정성 쪽에서 탈락합니다.

## 실제로 어떻게 바뀌었는가

보정 전 `P06` top 3:

```text
스페인 314 / 우루과이 298 / 포르투갈 295
```

보정 후 `P06` top 3:

```text
우루과이 316 / 스페인 314 / 포르투갈 295
```

즉,
top 3 구성은 유지하면서
기대 1위 anchor만 `우루과이`로 되돌렸습니다.

## 운영 화면에는 어떤 영향이 있었는가

이 변화는 `/dashboard/recommendation/persona-baseline`과
`/dashboard/recommendation/feedback`에 바로 반영됩니다.

- baseline: `18 / 18` 유지
- weak scenario: `0`
- anchor drift: `9 -> 8`
- ops review 우선 시나리오: `P06, P07, P08 -> P07, P08, P09`

즉, `P06`은 이제 운영 우선순위 top 3에서 빠졌습니다.

## 테스트는 무엇을 했는가

- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
  - `P06 -> 우루과이, 스페인, 포르투갈`로 snapshot 고정
- [RecommendationOfflinePersonaCoverageTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)
  - `P06`의 1위가 `우루과이`인지 확인
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
  - anchor drift가 `8`로 줄었는지 확인
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - 우선 시나리오가 `P07, P08, P09`로 바뀌는지 확인
- [RecommendationPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)
- [RecommendationFeedbackIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java)
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)
- 마지막으로 `./gradlew test` 전체 통과

## 한계와 다음 단계

이번 조각은 `P06`의 1위 drift를 줄이는 데는 성공했습니다.

하지만 여전히 `anchor drift`는 `8개` 남아 있습니다.

즉, weak scenario가 없어도
기대 1위와 실제 1위 사이의 미세한 차이는 계속 관리해야 합니다.

그래서 다음 단계는 broad bonus를 더 키우는 것이 아니라,
현재 ops review가 가리키는

- `P07`
- `P08`
- `P09`

중 하나만 골라 다시 좁게 보는 것입니다.

## 면접에서는 이렇게 설명할 수 있다

baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P06`처럼 온화한 기후와 현실적인 생활비, 치안·복지·주거 안정성을 같이 보는 시나리오에만 좁게 작동하는 `practicalPublicValueBonus`를 추가해서, 기대 1위였던 `우루과이`가 `스페인`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `9 -> 8`로 줄였습니다.
