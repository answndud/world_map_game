# [Spring Boot 포트폴리오] 42. 균형형 생활 시나리오를 위해 civic base bonus 추가하기

## 왜 이 조각이 필요했는가

이전 단계에서 `P15`는 개선됐다.

하지만 아직 `P04`, `P06` 같은 균형형 시나리오가 아쉬웠다.

공통점은 이랬다.

- 너무 덥거나 춥지 않은 온화한 생활
- 도시와 자연이 적당히 섞인 환경
- 빠르지도 느리지도 않은 생활 리듬
- 안전이나 공공서비스도 꽤 중요함

그런데 추천 결과는 여전히 스페인, 이탈리아 같은 남유럽 후보 쪽으로 쉽게 기울었다.

즉, 기후나 비용은 맞는데
`정착 안정성`이 점수식에서 충분히 한 덩어리로 반영되지 않고 있었다.

## 이번 단계의 목표

이번 조각의 목표는 단순했다.

- `MIXED + BALANCED` 생활을 원하는 사람에게
- `안전 / 공공서비스 / 기본 정착 안정성`을 같이 보는 작은 보정 신호를 추가한다.

즉, 이번에도 추천 엔진 전체를 다시 뜯지 않고
약한 시나리오 한 묶음만 노리는 작은 실험이다.

## 어떤 규칙을 추가했는가

`RecommendationSurveyService`에 `civicBaseBonus()`를 추가했다.

이 bonus는 아래 조건에서만 켜진다.

1. `EnvironmentPreference == MIXED`
2. `PacePreference == BALANCED`
3. `safetyPriority == HIGH` 또는 `publicServicePriority == HIGH`

그 다음 후보 국가에서 아래 값을 함께 본다.

- `safety`
- `welfare`
- `housingSpace`
- `newcomerFriendliness`

즉, 이 bonus는
`살기 편한가`, `행정/복지가 괜찮은가`, `정착 여유가 있는가`, `적응이 쉬운가`
를 한 번 더 같이 읽는 신호다.

## 왜 이렇게 좁혔는가

이 보정이 넓게 켜지면 거의 모든 균형형 시나리오가 북유럽/영어권 쪽으로 끌릴 수 있다.

그래서 조건을 세 겹으로 좁혔다.

- mixed environment
- balanced pace
- safety/public service 강조

그리고 `VALUE_FIRST`인데 `priceLevel >= 4`인 후보는 이 bonus를 못 받게 했다.

그래서 캐나다나 아일랜드처럼 좋은 후보라도, 저비용 성향에서는 너무 쉽게 올라오지 않게 막았다.

## 요청 흐름은 그대로다

런타임 요청은 전혀 바뀌지 않았다.

1. 사용자가 `/recommendation/survey`에 들어간다.
2. 설문 답변이 `RecommendationSurveyAnswers`로 변환된다.
3. `RecommendationSurveyService.recommend()`가 후보를 점수화한다.
4. 결과 페이지가 top 3를 SSR로 보여 준다.
5. 만족도는 `RecommendationFeedbackService`로 저장된다.

이번 조각의 변화는 3번 내부에만 있다.

## 왜 서비스가 맡아야 하는가

이 bonus는 컨트롤러가 알면 안 된다.

컨트롤러는 입력을 받고 페이지를 렌더링하는 역할만 해야 한다.

반면 아래 판단은 추천 도메인 규칙이다.

- 어떤 설문 조합에서 civic bonus를 켤 것인가
- 어떤 profile 속성을 묶어서 볼 것인가
- 저비용 사용자의 경우 어느 가격대부터 bonus를 막을 것인가

그래서 `RecommendationSurveyService`가 책임지는 것이 맞다.

## 결과는 어떻게 바뀌었는가

이번 조각 후 snapshot은 이렇게 움직였다.

- `P04`
  - 이전: `스페인, 이탈리아, 아일랜드`
  - 이후: `스페인, 아일랜드, 우루과이`
- `P06`
  - 이전: `스페인, 이탈리아, 우루과이`
  - 이후: `스페인, 우루과이, 이탈리아`
- `P14`
  - 이전: `스페인, 태국, 말레이시아`
  - 이후: `스페인, 말레이시아, 태국`

즉, `P04`에서는 이탈리아가 빠지고 아일랜드/우루과이가 들어왔다.

`P06`도 완전히 끝난 것은 아니지만, 우루과이가 2위로 올라오면서 한 단계 좋아졌다.

## 테스트는 어떻게 고정했는가

이번 조각에서는 세 가지를 같이 고정했다.

1. `RecommendationOfflinePersonaSnapshotTest`
   - `engine-v7` 기준 18개 시나리오 top 3 재고정
2. `RecommendationOfflinePersonaCoverageTest`
   - `P04`가 `아일랜드`, `우루과이`를 포함하고 `이탈리아`는 빠지는지 검증
   - `P06`이 `우루과이`를 포함하는지 검증
3. 통합 테스트
   - 추천 결과 페이지
   - 만족도 저장
   - dashboard 버전 표기

즉, “조금 나아진 것 같다”가 아니라
`어떤 시나리오에서 어떤 후보가 움직여야 하는가`를 테스트로 남겼다.

## 이번 단계의 의미

추천 엔진을 개선할 때 중요한 건
약한 시나리오를 설명 가능한 규칙으로 하나씩 줄여 가는 것이다.

이번 조각은 그 전형적인 예다.

- 문제: 균형형 시나리오가 남유럽 쪽으로 기운다
- 가설: civic base 신호가 약하다
- 변경: 작은 bonus 추가
- 검증: `P04`, `P06`, `P14` snapshot 확인

이렇게 해야 나중에 “왜 이 점수식을 만들었는가”를 설명할 수 있다.

## 면접에서는 이렇게 설명하면 된다

“추천 엔진을 튜닝할 때는 전체 알고리즘을 자주 갈아엎기보다, 약한 시나리오를 하나씩 겨냥하는 방식으로 갔습니다. 이번에는 `MIXED + BALANCED` 생활을 원하면서 안전이나 공공서비스를 중시하는 경우에만 작동하는 `civicBaseBonus`를 추가했습니다. 이 규칙은 `RecommendationSurveyService`가 맡고, `P04`와 `P06` 결과를 snapshot과 coverage 테스트로 다시 고정했습니다.”

## 다음 단계

다음은 `P06`의 3위 후보에서 왜 아직 `이탈리아`가 남는지 더 좁혀 보는 것이다.

즉, 다음 실험은 `balanced cost` 구간 penalty와 `futureBase / english / newcomer` 중 어느 축을 손볼지 결정하는 조각이 된다.
