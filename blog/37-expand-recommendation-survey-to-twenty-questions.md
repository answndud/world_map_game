# [Spring Boot 포트폴리오] 37. 추천 설문을 20문항 생활 시나리오형으로 다시 확장하기

## 왜 또 바꿨는가

이전 12문항 설문은 분명 처음보다 좋아졌다.

하지만 실제로 살아볼 나라를 고를 때는 질문 축이 더 필요했다.

예를 들어 아래 같은 차이가 있었다.

- 따뜻한 날씨가 좋은가
- 사계절이 뚜렷한 게 좋은가
- 날씨가 완벽하지 않아도 적응 가능한가
- 집이 조금 작아도 도심 접근성이 중요한가
- 처음 가는 곳에서 친절하게 적응할 수 있는 분위기가 중요한가
- 디지털 결제, 온라인 행정, 인터넷처럼 생활 인프라가 매끄러워야 하는가
- 지금 편한 곳이 중요한가, 앞으로 기반이 탄탄한 곳이 중요한가

즉, 질문 수를 늘리는 목적은 단순히 "더 많이 묻기"가 아니라 `생활 조건을 더 잘 분해하기`였다.

## 이번 단계의 목표

이번 조각의 목표는 세 가지였다.

1. 추천 설문을 20문항으로 늘린다.
2. 질문 문구를 "좋아하나요?"보다 `무엇을 감수할 수 있나요?` 중심으로 다듬는다.
3. 설문만 늘리지 않고, 나라 프로필과 점수식도 같이 확장한다.

## 어떤 축을 새로 추가했는가

기존 추천 엔진은 climate, pace, price, urbanity, english, safety, welfare, food, diversity 정도만 강하게 보고 있었다.

이번에는 아래 축을 더 추가했다.

- `seasonality`
- `housingSpace`
- `digitalConvenience`
- `cultureScene`
- `newcomerFriendliness`

그리고 설문 답변 enum에도 아래 축이 새로 생겼다.

- `SeasonStylePreference`
- `CrowdPreference`
- `HousingPreference`
- `NewcomerSupportNeed`
- `WorkLifePreference`
- `FutureBasePreference`

핵심은 질문과 나라 속성이 서로 직접 연결되게 만든 것이다.

## 요청 흐름

이번에도 추천 요청 흐름 자체는 바뀌지 않았다.

1. 사용자가 `/recommendation/survey`에 들어간다.
2. `RecommendationQuestionCatalog`가 20개 문항과 선택지를 만든다.
3. 사용자가 답을 고르고 제출한다.
4. `RecommendationSurveyForm`이 20개 답변을 `RecommendationSurveyAnswers`로 변환한다.
5. `RecommendationSurveyService`가 나라 프로필 30개와 비교해 top 3를 계산한다.
6. 결과 페이지는 20개 답변 요약과 top 3 후보를 SSR로 보여준다.
7. 만족도를 남기면 `RecommendationFeedbackRequest -> RecommendationFeedbackService`로 20개 답변 snapshot이 저장된다.

즉, 요청은 그대로지만 입력 해상도와 계산 해상도가 같이 올라갔다.

## 왜 서비스가 계산을 맡는가

이 로직은 컨트롤러가 아니라 `RecommendationSurveyService`에 있어야 한다.

이유는 아래 규칙이 모두 추천 도메인 규칙이기 때문이다.

- climate mismatch를 어떻게 penalty로 줄 것인가
- 높은 생활비 초과를 얼마나 강하게 깎을 것인가
- newcomer support를 영어 지원과 현지 친화도 중 어떤 조합으로 볼 것인가
- work-life intensity를 어떤 profile 조합으로 계산할 것인가
- future base를 안전, 복지, 주거 여유 중 어떤 비중으로 볼 것인가

컨트롤러는 입력을 받는 역할만 해야 하고, 추천 계산 규칙은 서비스가 맡아야 설명 가능한 구조가 된다.

## 테스트는 무엇을 고쳤는가

20문항 구조가 들어가면 테스트도 같이 바뀌어야 한다.

이번에는 아래를 같이 고쳤다.

- `RecommendationSurveyServiceTest`
- `RecommendationPageIntegrationTest`
- `RecommendationFeedbackIntegrationTest`
- `RecommendationOfflinePersonaFixtures`
- `RecommendationOfflinePersonaCoverageTest`
- `RecommendationOfflinePersonaSnapshotTest`

특히 중요했던 건 offline persona baseline이다.

질문이 늘어나면 top 3 추천 순서도 바뀔 수밖에 없어서, `survey-v4 / engine-v4` 기준으로 snapshot을 다시 고정했다.

## 이번 단계에서 얻은 것

이제 설문은 단순 취향 체크보다 훨씬 실제 생활 시나리오에 가까워졌다.

예를 들어 아래를 더 잘 가를 수 있다.

- 따뜻한 곳을 좋아하지만 사계절은 원하지 않는 사람
- 활기 있는 도시를 좋아하지만 과밀한 동네는 싫어하는 사람
- 높은 생활비를 감수하더라도 디지털 인프라와 공공 서비스를 원하는 사람
- 지금 내 적응보다 장기 기반이 더 중요한 사람

## 면접에서 이렇게 설명하면 된다

“처음에는 추천 설문이 너무 단순해서 비슷한 답변이 많이 나왔습니다. 그래서 질문 수만 늘린 게 아니라, 집 크기와 도심 접근성, newcomer 친화도, 디지털 생활 편의, 문화·여가, 장기 기반 같은 실제 생활 축을 추가했습니다. 그리고 이 질문들이 실제 추천 결과에 반영되도록 나라 프로필과 점수식, feedback snapshot, offline persona baseline까지 같이 확장했습니다.”

## 다음 단계

다음은 `survey-v4 / engine-v4` 결과를 기준으로 weak scenario를 다시 튜닝하는 것이다.

특히 남유럽·저예산·균형형 시나리오에서 어떤 나라가 과하게 자주 올라오는지 더 봐야 한다.
