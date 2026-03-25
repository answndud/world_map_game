# 46. dashboard persona baseline에 anchor drift까지 보이게 하기

## 왜 이 작업이 필요했는가

`engine-v9`에서 baseline은 `18 / 18`까지 올라갔습니다.  
즉, 기대 후보가 최소 1개는 top 3에 들어오지 않는 시나리오는 없어졌습니다.

문제는 여기서부터입니다.

- 이제 `weak scenario`만 보면 다음 개선 대상을 잡기 어렵습니다.
- 예를 들어 기대 후보는 들어오지만, 기대했던 1위 후보가 계속 2위나 3위에 머물 수 있습니다.
- 이런 경우는 “완전히 틀린 추천”은 아니지만, 순위 품질은 아직 아쉬운 상태입니다.

그래서 이번 조각에서는 `/dashboard/recommendation/persona-baseline`이

1. weak scenario
2. active signal scenario
3. anchor drift scenario

를 함께 보게 만들었습니다.

여기서 `anchor drift`는
`기대 후보는 top 3에 남아 있지만, 기대 1위 후보가 현재 top 1은 아닌 경우`를 뜻합니다.

## 어떤 파일이 바뀌는가

- [AdminPersonaBaselineService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java)
- [AdminPersonaBaselineView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/application/AdminPersonaBaselineView.java)
- [AdminPersonaBaselineScenarioView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/application/AdminPersonaBaselineScenarioView.java)
- [recommendation-persona-baseline.html](/Users/alex/project/worldmap/src/main/resources/templates/admin/recommendation-persona-baseline.html)
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)

## 요청은 어떻게 흐르는가

운영 화면 요청 흐름은 그대로입니다.

1. `GET /dashboard/recommendation/persona-baseline`
2. `AdminPageController`
3. `AdminPersonaBaselineService.loadBaseline()`
4. `RecommendationPersonaBaselineCatalog`의 18개 시나리오를 현재 엔진에 다시 넣음
5. 각 시나리오의 현재 top 3를 계산
6. `weak / anchor drift / active signal`로 나눠서 SSR 렌더링

즉, 이 화면은 DB에 저장된 별도 운영 테이블을 읽는 것이 아니라,
현재 추천 엔진 결과를 다시 계산해서 읽는 read model입니다.

## 왜 이 로직이 서비스에 있어야 하는가

이 분류는 단순 뷰 포맷이 아닙니다.

- weak scenario: 기대 후보가 top 3에 하나도 없음
- anchor drift: 기대 후보는 있지만 기대 1위가 top 1은 아님
- active signal: 새 문항이 실제 후보 구성을 바꾸는지 확인하는 비교군

이 판단은 템플릿이 하면 안 됩니다.  
템플릿은 결과를 보여 주기만 하고, 어떤 시나리오가 어떤 그룹인지 판단하는 책임은
[AdminPersonaBaselineService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java)가 가져가야 합니다.

## 이번에 얻은 운영 시야

현재 기준에서는

- baseline matched: `18 / 18`
- weak scenario: `0`
- anchor drift: `13`
- active signal: `4`

즉, 이제 추천 품질은 “완전히 빗나간 경우”보다
“방향은 맞는데 1위가 덜 설득력 있는 경우”가 더 중요한 단계로 넘어갔습니다.

이게 중요한 이유는 다음 tuning을
무작정 새 bonus 추가로 하기보다,
`어떤 시나리오의 rank drift를 줄일지` 기준으로 더 작게 자를 수 있기 때문입니다.

## 테스트는 무엇을 했는가

- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
  - matched `18 / 18`
  - weak `0`
  - anchor drift `13`
  - active signal `4`
  - drift scenario id 집합 고정
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)
  - 페이지에 `ANCHOR DRIFT`, `1위 재검토 대상`, `P11`이 보이는지 확인

## 면접에서 어떻게 설명할 수 있는가

“baseline이 18 / 18이 되면 weak scenario만으로는 다음 개선 포인트를 잡기 어려워집니다.  
그래서 운영 화면 read model을 확장해서, 기대 후보는 top 3에 있지만 기대 1위가 아닌 `anchor drift`를 따로 계산하도록 만들었습니다.  
덕분에 추천 엔진이 완전히 빗나간 경우와 순위가 아쉬운 경우를 분리해서 볼 수 있습니다.”
