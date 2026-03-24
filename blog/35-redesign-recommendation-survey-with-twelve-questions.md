# [Spring Boot 포트폴리오] 35. 추천 설문을 12문항 trade-off 구조로 다시 설계하기

## 왜 다시 손봤나

기존 추천 설문은 동작은 했지만 질문이 너무 단순했다.

예를 들어 `물가 허용 범위는 어느 정도인가요?`라고 물으면 대부분은 낮은 물가를 고른다.
이 질문만으로는 “비싸더라도 인프라, 안전, 편의가 좋다면 감수할 의향이 있는가” 같은 실제 trade-off를 잘 읽을 수 없다.

그래서 이번에는 질문 수를 12개로 늘리는 것보다, 질문의 성격 자체를 바꾸는 데 더 집중했다.

## 이번에 바꾼 방향

기존 축:

- 기후
- 생활 속도
- 물가
- 도시/자연
- 영어 중요도
- 최우선 기준 1개
- 정착 성향
- 이동 방식

새 축:

- 기후 방향
- 기후 적응 성향
- 생활 속도
- 비용과 생활 품질의 균형
- 생활 환경
- 영어 지원 필요도
- 치안 우선도
- 공공 서비스 우선도
- 음식 만족도 우선도
- 문화 다양성 우선도
- 정착 성향
- 이동 방식

핵심은 `무조건 좋은 것`을 묻는 대신 `무엇을 감수할 수 있는가`를 묻는 구조로 바꿨다는 점이다.

## 어디를 바꿨나

- `RecommendationSurveyAnswers`
- `RecommendationSurveyForm`
- `RecommendationQuestionCatalog`
- `RecommendationSurveyService`
- `RecommendationFeedbackPayloadView`
- `RecommendationFeedbackRequest`
- `RecommendationFeedback`
- `recommendation/survey.html`
- `recommendation/result.html`

## 요청 흐름

1. `GET /recommendation/survey`
2. `RecommendationQuestionCatalog`가 12개 문항과 선택지를 만든다.
3. 사용자가 설문을 제출한다.
4. `RecommendationSurveyForm`이 12개 답변을 `RecommendationSurveyAnswers`로 변환한다.
5. `RecommendationSurveyService.recommend()`가 30개 국가 프로필과 비교해 top 3를 계산한다.
6. 결과 페이지는 12개 답변 요약과 top 3 후보를 SSR로 보여준다.
7. 사용자가 만족도를 남기면 `RecommendationFeedbackRequest -> RecommendationFeedbackService`로 12개 답변 스냅샷이 저장된다.

## 왜 서비스에 둬야 하나

질문 wording 자체는 카탈로그 문제지만, 추천 품질을 실제로 바꾸는 것은 점수식이다.

이번에 새로 들어간 판단은 모두 도메인 로직이다.

- 높은 생활비를 얼마나 penalty로 줄 것인가
- 영어 지원이 `핵심 조건`이면 얼마나 더 크게 볼 것인가
- 극단 기후 mismatch를 어떻게 깎을 것인가
- 치안/공공 서비스/음식/다양성 중요도를 어떻게 따로 점수화할 것인가

이 규칙은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다.

## 테스트

- `RecommendationPageIntegrationTest`
- `RecommendationFeedbackIntegrationTest`
- `RecommendationSurveyServiceTest`
- `RecommendationOfflinePersonaCoverageTest`
- `RecommendationOfflinePersonaSnapshotTest`

이번엔 오프라인 baseline도 같이 다시 고정했다.
즉, 질문만 늘리고 끝낸 게 아니라 “새 질문 구조에서 실제 top 3가 어떻게 움직이는지”까지 테스트로 같이 남겼다.

## 취업 포인트

면접에서는 이렇게 설명하면 된다.

“추천 설문이 단순하면 대부분 비슷한 답을 고르게 됩니다. 그래서 질문 수를 12개로 늘리는 것보다, `무엇을 좋아하느냐`가 아니라 `무엇을 감수할 수 있느냐`를 묻는 방향으로 다시 설계했습니다. 그리고 그 입력은 `RecommendationSurveyAnswers`로 정규화하고, `RecommendationSurveyService`가 deterministic하게 top 3를 계산하도록 유지했습니다.”

## 다음 단계

다음은 이 12문항 구조 위에서 weak scenario를 다시 튜닝하는 것이다.

- 어떤 시나리오가 여전히 어색한지
- 어느 penalty를 더 키워야 하는지
- 만족도 데이터와 baseline이 같은 방향으로 움직이는지
