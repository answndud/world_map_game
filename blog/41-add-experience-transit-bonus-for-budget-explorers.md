# [Spring Boot 포트폴리오] 41. 탐색형·교통형 저예산 시나리오에 보정 신호 하나 더 넣기

## 왜 이 조각이 필요했는가

이전 단계에서 `VALUE_FIRST` 사용자의 초과 물가 패널티를 더 강하게 나눴다.

그 결과 `P02`, `P14` 같은 저비용 시나리오는 어느 정도 좋아졌다.

하지만 `P15`는 여전히 아쉬웠다.

원했던 방향은 아래였다.

- 저예산
- 자연형 성향
- 먼저 가볍게 살아보기
- 대중교통 중심 적응

그런데 실제 top 3는 `포르투갈, 뉴질랜드, 남아프리카 공화국`이 나왔다.

즉, “가볍게 적응하면서 교통 중심으로 살아보기 좋은가”라는 신호가 아직 충분히 반영되지 않았다.

## 이번 단계의 목표

목표는 추천 엔진 전체를 다시 뜯는 것이 아니었다.

이번 조각은 딱 하나만 노렸다.

- `EXPERIENCE + TRANSIT_FIRST + VALUE_FIRST`

이 조합일 때만 작은 보정 신호를 넣는다.

즉, 특정 weak scenario를 겨냥한 매우 작은 실험이다.

## 어떤 규칙을 추가했는가

`RecommendationSurveyService`에 `experienceTransitBonus()`를 추가했다.

이 bonus는 아래 조건이 모두 맞을 때만 켜진다.

1. 정착 방식이 `EXPERIENCE`
2. 이동 선호가 `TRANSIT_FIRST`
3. 비용 선호가 `VALUE_FIRST`

그리고 후보 국가는 아래 속성을 함께 본다.

- `transitSupport`
- `newcomerSupport`
- `digitalConvenience`
- `safety`
- `welfare`

즉, 이번 bonus는 “교통만 좋다”가 아니라
`적응하기 쉽고, 디지털 생활이 매끄럽고, 기본 안전성과 생활 안정성도 너무 낮지 않은가`를 같이 보는 신호다.

## 왜 이렇게 좁혔는가

처음에는 `EXPERIENCE + TRANSIT_FIRST` 전체에 bonus를 주었다.

그랬더니 `P17` 같은 도심 탐험형 시나리오까지 흔들렸다.

즉, bonus 범위가 너무 넓었다.

그래서 최종적으로는:

- `VALUE_FIRST`까지 함께 만족할 때만 bonus 적용
- `safety`, `welfare`가 너무 낮은 후보는 제외

이렇게 좁혔다.

덕분에 `P17`은 그대로 두고, `P15`만 움직이는 방향으로 조정할 수 있었다.

## 요청 흐름은 그대로다

런타임 요청 자체는 바뀌지 않았다.

1. 사용자가 `/recommendation/survey`에 들어간다.
2. `RecommendationSurveyForm`이 답변을 `RecommendationSurveyAnswers`로 바꾼다.
3. `RecommendationSurveyService.recommend()`가 각 나라를 점수화한다.
4. 결과 페이지는 top 3를 SSR로 보여준다.
5. 만족도는 `RecommendationFeedbackService`로 저장된다.

이번 조각은 이 흐름을 바꾸지 않고, 3번의 내부 점수식만 조금 조정한 것이다.

## 왜 서비스가 맡아야 하는가

이 bonus는 컨트롤러가 알 필요가 없다.

컨트롤러는 입력을 받고 view를 렌더링하면 된다.

반면 아래 판단은 추천 도메인 규칙이다.

- 어떤 답변 조합에서 bonus를 켤 것인가
- 어떤 profile 속성을 묶어서 볼 것인가
- safety/welfare를 minimum gate로 둘 것인가

그래서 이 로직은 `RecommendationSurveyService`에 있어야 한다.

## 결과가 어떻게 바뀌었는가

이번 조각 후 `P15`의 top 3는 아래처럼 바뀌었다.

- 이전: `포르투갈, 뉴질랜드, 남아프리카 공화국`
- 이후: `포르투갈, 뉴질랜드, 말레이시아`

즉, 원하는 `말레이시아`가 다시 top 3로 올라왔다.

그리고 `P17`은 이전과 같은 top 3를 유지했다.

이건 이번 bonus가 정말로 `P15` 같은 저예산 탐색형 시나리오에만 작동했다는 뜻이다.

## 테스트는 어떻게 고정했는가

이번에는 두 가지를 같이 고정했다.

1. `RecommendationOfflinePersonaSnapshotTest`
   - `engine-v6` 기준 18개 시나리오 top 3 snapshot 재고정
2. `RecommendationOfflinePersonaCoverageTest`
   - `P15`가 `뉴질랜드`, `말레이시아`를 포함하고
   - `남아프리카 공화국`은 포함하지 않는지 검증

즉, “대충 좋아진 것 같다”가 아니라
`어떤 시나리오의 어떤 후보가 바뀌어야 하는가`를 테스트로 명시했다.

## 이번 단계의 의미

추천 엔진을 개선할 때 중요한 건 매번 전체를 다시 만지는 게 아니다.

오히려 더 중요한 건:

- 약한 시나리오를 하나 고른다
- 그 시나리오를 설명할 수 있는 작은 규칙을 추가한다
- 다른 시나리오를 괜히 흔들지 않는지 본다
- 결과를 snapshot으로 다시 고정한다

이 과정을 반복해야 추천 엔진이 설명 가능한 구조로 남는다.

## 면접에서는 이렇게 설명하면 된다

“비용 민감 시나리오를 개선한 뒤에도 `P15` 같은 탐색형·교통형 저예산 케이스는 여전히 원하는 후보를 못 올렸습니다. 그래서 전체 알고리즘을 다시 짜지 않고, `EXPERIENCE + TRANSIT_FIRST + VALUE_FIRST` 조합에서만 작동하는 작은 보정 신호를 추가했습니다. 이 규칙은 `RecommendationSurveyService`가 맡고, `P15`가 `뉴질랜드 + 말레이시아`를 다시 포함하도록 coverage와 snapshot 테스트로 고정했습니다.”

## 다음 단계

다음은 `P04`, `P06`처럼 아직 남유럽 후보가 강하게 남는 균형형/현실형 시나리오를 다시 보는 것이다.

즉, 다음 실험은 `publicService`, `futureBase`, `balanced cost` 구간을 어디까지 조정할지 좁혀야 한다.
