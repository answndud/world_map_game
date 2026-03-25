# 51. 저비용 음식·다문화 시나리오의 1위 drift 줄이기

## 왜 이 작업이 필요했는가

추천 엔진이 `baseline 18 / 18`까지 올라온 뒤에는
`기대 후보가 top 3에 들어오는가`보다
`기대 1위가 실제로도 1위인가`가 더 중요해졌습니다.

이번 조각에서 본 시나리오는 `P02`였습니다.

- 따뜻한 기후
- 생활비는 낮아야 함
- 음식 만족도 중요
- 다문화 적응성도 중요
- 초반 정착 장벽이 너무 높으면 안 됨

그런데 현재 엔진은 이 시나리오에서
`태국 > 말레이시아 > 스페인` 순서를 주고 있었습니다.

문제는 방향은 맞지만,
`말레이시아`가 기대 1위인데 계속 2위로 밀린다는 점이었습니다.

즉, weak scenario는 아니지만 `anchor drift`였습니다.

## 어떤 파일이 바뀌는가

- [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
- [RecommendationOfflinePersonaCoverageTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
- [RecommendationPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)
- [RecommendationFeedbackIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java)
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)

## 요청은 어떻게 흐르는가

런타임 추천 흐름은 그대로입니다.

1. `GET /recommendation/survey`
2. `POST /recommendation/survey`
3. [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java) 가 점수 계산
4. `recommendation/result` SSR 렌더링
5. `POST /api/recommendation/feedback`

이번 조각에서 바뀐 것은
`RecommendationSurveyService.recommend()` 안의 점수식뿐입니다.

## 왜 이 로직이 서비스에 있어야 하는가

이번 보정은 “폼 값 하나를 바꾸는 UI 규칙”이 아닙니다.

아래 설문 조합일 때만
“저렴하면서도 초반 적응이 쉬운 음식·다문화 시작점”을
조금 더 높게 보는 추천 규칙입니다.

- `WARM`
- `VALUE_FIRST`
- `FOOD HIGH`
- `DIVERSITY MEDIUM 이상`
- `English MEDIUM`
- `MIXED`
- `BALANCED settlement / mobility`

이건 HTTP 처리나 템플릿 조건이 아니라
`추천 점수 계산 규칙`이므로
컨트롤러가 아니라 서비스에 있어야 합니다.

## 이번에 넣은 보정

새로 추가한 것은 `foodieStarterBonus()`입니다.

핵심 아이디어는 이렇습니다.

- 단순히 “물가가 낮다”만으로는 부족하다.
- 음식과 다문화 환경이 좋아도,
  영어 적응 난도와 초기 정착 장벽이 너무 높으면 초반 진입이 어렵다.
- 그래서 `food + diversity + newcomer friendliness + english support + digital convenience`를 같이 본다.

즉,

- `말레이시아`처럼 저렴하면서 적응성이 비교적 좋은 후보는 strong bonus
- `태국`처럼 음식과 다양성은 강하지만 적응성이 조금 약한 후보는 smaller bonus
- `스페인`처럼 조건은 좋지만 `priceLevel`이 높은 후보는 bonus 없음

이 구조로 좁게 보정했습니다.

## 실제로 어떻게 바뀌었는가

디버그 테스트로 `P02`의 실제 점수를 먼저 확인했습니다.

보정 전:

```text
태국 316 / 말레이시아 310 / 스페인 297
```

보정 후:

```text
말레이시아 318 / 태국 316 / 스페인 297
```

즉, top 3는 그대로 유지하면서
기대 1위 anchor만 `말레이시아`로 되돌렸습니다.

## 운영 화면에는 어떤 영향이 있었는가

이 변화는 `/dashboard/recommendation/persona-baseline`과
`/dashboard/recommendation/feedback`에도 바로 반영됩니다.

- baseline: `18 / 18` 유지
- weak scenario: `0`
- anchor drift: `11 -> 10`
- ops review 우선 시나리오: `P02, P04, P06 -> P04, P06, P07`

즉, `P02`는 더 이상 운영 우선순위 top 3에 남지 않게 됐습니다.

## 테스트는 무엇을 했는가

- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
  - `P02 -> 말레이시아, 태국, 스페인`으로 snapshot 고정
- [RecommendationOfflinePersonaCoverageTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)
  - `P02`의 1위가 `말레이시아`인지 확인
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
  - anchor drift가 `10`으로 줄었는지 확인
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - 우선 시나리오가 `P04, P06, P07`으로 바뀌는지 확인
- [RecommendationPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)
- [RecommendationFeedbackIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java)
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)
- 마지막으로 `./gradlew test` 전체 통과

## 한계와 다음 단계

이번 조각은 `P02`의 1위 drift를 줄이는 데는 성공했습니다.

하지만 top 3 전체가 기대 예시와 완전히 같아진 것은 아닙니다.
아직 3위는 `멕시코`가 아니라 `스페인`입니다.

그래서 다음 단계는 broad food/diversity bonus를 더 키우는 게 아니라,
현재 운영 우선순위로 남아 있는

- `P04`
- `P06`
- `P07`

중 하나를 다시 좁게 보는 것이 맞습니다.

## 면접에서는 이렇게 설명할 수 있다

baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 저비용이면서 음식과 다문화 적응성을 같이 보는 `P02` 시나리오에만 좁게 작동하는 `foodieStarterBonus`를 추가해서, 기대 1위였던 말레이시아가 태국보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `11 -> 10`으로 줄였습니다.
