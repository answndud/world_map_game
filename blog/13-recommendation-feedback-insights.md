# [Spring Boot 포트폴리오] 13. 설문 / 엔진 버전별 만족도 집계 기준 정리하기

## 이번 글의 핵심 질문

만족도 피드백을 저장하기 시작했다면, 그 다음 질문은 자연스럽다.

“이 데이터를 어떤 기준으로 읽어야 설문을 실제로 개선할 수 있을까?”

이번 단계에서는 `RecommendationFeedback`를 `surveyVersion + engineVersion` 기준으로 묶어 평균 점수, 응답 수, 1~5점 분포를 보여주는 최소 집계 화면과 API를 만들었다.

## 왜 이 단계가 필요한가

피드백을 저장하는 것만으로는 아직 개선 루프가 완성되지 않는다.

설문을 바꾸거나 점수식을 조정한 뒤에는 적어도 아래는 볼 수 있어야 한다.

1. 어떤 설문 버전이 평균 점수가 높은가
2. 어떤 엔진 버전이 응답 수가 충분한가
3. 평균이 비슷해도 낮은 점수가 몰려 있는 버전은 아닌가

즉, 이번 단계의 목적은 “저장”이 아니라 “비교 가능한 읽기 기준”을 만드는 것이다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackRepository.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackVersionSummaryProjection.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackInsightsView.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackSummaryView.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java`
- `/Users/alex/project/worldmap/src/main/resources/templates/recommendation/feedback-insights.html`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`

## 집계 단위를 왜 버전 조합으로 잡았는가

이번 프로젝트에서는 추천 결과 자체를 저장하지 않는다.

그 대신 설문을 실제로 바꿀 수 있는 최소 비교 단위를 `surveyVersion + engineVersion`으로 잡았다.

이렇게 하면 질문 문항을 바꿨는지, 점수식을 바꿨는지에 따라 만족도 차이가 나는지 비교하기 쉬워진다.

즉, 이번 단계의 핵심 단위는 “개별 추천 결과”가 아니라 “어떤 버전 조합이 얼마나 만족도를 받았는가”다.

## 어떤 값을 집계했는가

현재는 너무 많은 지표를 넣지 않았다.

버전 조합마다 아래 값만 본다.

- `responseCount`
- `averageSatisfaction`
- `score1Count`
- `score2Count`
- `score3Count`
- `score4Count`
- `score5Count`
- `lastSubmittedAt`

평균 점수만 보면 표본 수가 적은 버전을 과대평가할 수 있어서, 응답 수와 1~5점 분포를 반드시 같이 보여주도록 했다.

## 왜 이 로직은 컨트롤러가 아니라 서비스에 있어야 하는가

컨트롤러는 조회 진입점만 열어 주면 된다.

실제 규칙은 아래에 있다.

1. 어떤 기준으로 그룹핑할지
2. 어떤 지표를 같이 보여줄지
3. 평균 점수와 전체 평균을 어떻게 계산할지
4. 페이지와 API가 같은 집계 기준을 공유하도록 어떻게 묶을지

이건 화면 렌더링이 아니라 비즈니스 기준이므로 `RecommendationFeedbackService.summarizeByVersion()`가 책임져야 한다.

`RecommendationFeedbackRepository`는 DB 그룹핑을 하고, 서비스는 그 결과를 `RecommendationFeedbackInsightsView`와 `RecommendationFeedbackSummaryView`로 변환해 API와 SSR이 같이 쓰게 만든다.

## Repository는 무엇을 하도록 했는가

이번에는 데이터를 다 읽어 와서 자바에서 그룹핑하지 않았다.

`RecommendationFeedbackRepository.summarizeByVersion()`에서 DB가 바로 아래 집계를 하게 했다.

- 버전 조합별 count
- avg
- 1점~5점 조건부 합계
- 마지막 응답 시각

이렇게 해야 데이터가 더 늘어나도 “집계 기준은 저장소 쿼리에서, 화면 표현은 서비스와 템플릿에서”라고 설명하기 좋다.

## 화면은 왜 별도 페이지로 분리했는가

결과 페이지에 바로 긴 집계 표를 넣으면 추천 경험과 개선용 분석이 섞인다.

그래서 `/recommendation/feedback-insights`라는 별도 SSR 페이지로 분리했다.

추천 결과 페이지에서는 링크만 열어 주고, 실제 분석은 별도 화면에서 보게 했다.

이렇게 하면 사용자용 추천 흐름과 내부용 개선 흐름이 분리돼 설명도 더 쉬워진다.

## 테스트는 무엇을 고정했는가

이번에는 추천 계산 테스트가 아니라 집계 읽기 테스트가 중요했다.

`RecommendationFeedbackIntegrationTest`에서 두 가지를 확인했다.

1. `GET /api/recommendation/feedback/summary`가 버전별 응답 수와 평균 점수를 올바르게 계산하는가
2. `GET /recommendation/feedback-insights`가 실제 요약 화면을 렌더링하는가

즉, “저장된 피드백이 실제 개선 지표로 읽히는가”를 테스트로 고정한 셈이다.

## 면접에서는 이렇게 설명하면 된다

“추천 결과 자체는 저장하지 않지만, 설문과 엔진 버전별 만족도 집계는 조회할 수 있게 만들었습니다. `RecommendationFeedback`를 `surveyVersion + engineVersion` 기준으로 그룹핑해서 평균 점수, 응답 수, 1~5점 분포를 계산하고, 이 집계 결과를 API와 SSR 화면에서 같이 쓰도록 `RecommendationFeedbackService`에 모았습니다. 그래서 어떤 버전이 실제로 더 만족도가 높은지 비교할 수 있습니다.”

## 다음 글

다음 단계는 `오프라인 AI-assisted 설문 개선 루프 정리하기`다.

이제 추천 계산과 만족도 집계의 최소 구조는 잡혔으니, 다음에는 서브 에이전트로 문항 후보와 페르소나 시나리오를 만들고, 실제 만족도 데이터와 함께 비교해 설문을 개선하는 운영 루프를 정리한다.
