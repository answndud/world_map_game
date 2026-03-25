# 따뜻한 초도시 허브 시나리오에 global hub bonus 추가하기

## 왜 이 조각이 필요했는가

`engine-v9`에서 추천 baseline은 `18 / 18`까지 올라갔다.  
즉, 기대 후보가 top 3 안에 하나도 안 들어오는 weak scenario는 없어졌다.

하지만 운영 화면 `/dashboard/recommendation/persona-baseline`을 다시 보면 다른 문제가 남아 있었다.

- `P01`
- `P05`

이 두 시나리오는 기대 후보가 top 3에 들어오기는 하지만, 기대 1위였던 `싱가포르`가 실제로는 `아랍에미리트` 뒤로 밀리고 있었다.

이 상태는 weak scenario는 아니지만, `anchor drift`에 해당한다.

즉, 방향은 맞지만 1위 순위가 아쉬운 경우다.

이번 조각의 목표는 이 drift를 줄이는 것이었다.

## 어떤 상황이 문제였는가

문제 시나리오는 단순히 “따뜻한 곳이 좋다”가 아니었다.

`P01`, `P05`는 아래 조건이 같이 붙는 케이스였다.

- 따뜻한 기후 선호
- 빠른 도시 리듬 선호
- 도시형 환경 선호
- 비용보다 품질 우선
- 영어 지원 필요도 높음
- 디지털 편의 중요도 높음
- 문화/여가 중요도 높음
- 다양성 중요도 높음
- newcomer 친화도 중요

즉, 이건 “따뜻한 나라 추천”이 아니라  
`따뜻하고, 영어가 통하고, 교통과 디지털 환경이 좋고, 문화 밀도가 높은 초도시 허브`를 찾는 상황에 가깝다.

기존 엔진은 이런 조합에서 `아랍에미리트`를 너무 강하게 올리고 있었다.

## 어떻게 고쳤는가

핵심 변경은 [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)에 `globalHubBonus()`를 추가한 것이다.

이 bonus는 아무 때나 켜지지 않는다.

아래 조건을 모두 만족할 때만 작동한다.

- `WARM`
- `FAST`
- `CITY`
- `QUALITY_FIRST`
- `EnglishSupport HIGH`
- `Diversity HIGH`
- `Digital HIGH`
- `Culture HIGH`
- `Newcomer HIGH`

그리고 후보 국가도 아래 속성이 모두 높아야 한다.

- `urbanity`
- `transit`
- `digitalConvenience`
- `diversity`
- `food`
- `cultureScene`
- `safety`

이렇게 좁힌 이유는 간단하다.

bonus를 넓게 켜면

- 미국
- 다른 영어권 고비용 후보
- 이미 상위권이던 도시형 후보

까지 같이 흔들릴 수 있기 때문이다.

이번 조각은 전체 엔진을 다시 흔드는 것이 아니라,  
`P01`, `P05`처럼 warm city hub 수요가 아주 강한 경우만 더 정확하게 읽는 신호 하나를 추가하는 데 집중했다.

## 요청 흐름은 어떻게 유지됐는가

런타임 요청 흐름은 바뀌지 않았다.

1. 사용자가 `/recommendation/survey`에서 20문항 설문을 고른다.
2. `RecommendationSurveyForm`이 답변을 `RecommendationSurveyAnswers`로 변환한다.
3. [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)가 국가 프로필과 비교해 점수를 계산한다.
4. 결과 페이지는 top 3와 핵심 이유를 SSR로 렌더링한다.
5. 만족도는 기존처럼 `surveyVersion + engineVersion + 20개 답변 snapshot`만 저장한다.

즉, 바뀐 것은 컨트롤러나 저장 흐름이 아니라 추천 도메인 서비스 내부의 점수식뿐이다.

## 왜 이 로직은 서비스에 있어야 하는가

`globalHubBonus()`는 화면 조건문이 아니다.

이건 아래를 함께 판단하는 추천 규칙이다.

- 어떤 설문 조합에서만 bonus를 켤 것인가
- 어떤 국가 속성 조합을 “global hub”로 볼 것인가
- 얼마나 좁게 켜야 다른 시나리오를 덜 흔드는가

이건 HTTP 요청 처리보다 추천 엔진 규칙에 가깝다.

그래서 컨트롤러가 아니라 [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)에 있어야 한다.

## 테스트는 무엇을 했는가

먼저 디버그 테스트로 `P01`의 실제 점수를 확인했다.

- 아랍에미리트 `356`
- 싱가포르 `349`
- 미국 `267`

이걸 보고 “gap이 아주 크진 않으니, 좁은 bonus 하나로 뒤집을 수 있다”고 판단했다.

그 다음 아래를 고정했다.

- [RecommendationOfflinePersonaSnapshotTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
  - `P01`, `P05`가 `싱가포르, 아랍에미리트, 미국`인지 확인
- [RecommendationOfflinePersonaCoverageTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)
  - `P01`, `P05`의 1위가 `싱가포르`인지 확인
- [AdminPersonaBaselineServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
  - anchor drift 수가 `13 -> 11`로 줄었는지 확인
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - ops review 우선 시나리오가 `P02, P04, P06`으로 바뀌는지 확인

마지막으로 `./gradlew test` 전체 통과까지 확인했다.

## 이 변경의 의미

이번 조각은 추천 엔진이 “완전히 틀린가”를 고치는 단계가 아니다.

그 단계는 이미 끝났고, 이제는

- 기대 후보는 들어오지만
- 기대 1위가 아닌 경우

를 줄이는 단계다.

즉, 추천 품질이 `weak scenario 제거`에서 `rank drift 정리` 단계로 넘어갔다는 뜻이다.

## 면접에서 이렇게 설명하면 된다

“baseline 18 / 18을 만든 뒤에는 weak scenario보다 순위 drift가 더 중요한 문제가 됐습니다. 그래서 warm/fast/city/high-quality 조합에서만 작동하는 `globalHubBonus`를 추천 엔진에 좁게 추가해, `P01`, `P05`에서 기대 1위였던 싱가포르가 아랍에미리트보다 앞서도록 보정했습니다. 결과적으로 baseline은 유지하면서 anchor drift를 13개에서 11개로 줄였습니다.”
