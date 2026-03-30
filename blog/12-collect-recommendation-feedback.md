# [Spring Boot 포트폴리오] 12. 추천 결과는 저장하지 않고 만족도 피드백만 수집하기

## 이번 글의 핵심 질문

설문 추천 기능을 만들면 자연스럽게 이런 고민이 생긴다.

“추천 결과를 전부 저장해야 나중에 설문을 개선할 수 있는 걸까?”

이번 단계에서는 그 질문에 `아니오`라고 답했다.

추천 결과 top 3 자체는 저장하지 않고, 대신 결과 페이지에서 `1~5점 만족도`, `surveyVersion`, `engineVersion`, 그리고 사용자가 고른 6개 답변만 익명으로 저장하는 구조를 붙였다.

이 글은 hidden field payload를 쓰던 첫 버전 기록이다.
현재 코드 기준으로는 hidden answer snapshot을 다시 보내지 않고,
서버가 `feedbackToken -> 추천 문맥`을 세션에 저장한 뒤 점수만 받는다.
이 후속 정리는 [104-bind-recommendation-feedback-to-session-token-and-lock-summary-api.md](./104-bind-recommendation-feedback-to-session-token-and-lock-summary-api.md)에서 설명한다.

## 왜 이 단계가 필요한가

추천 결과를 저장하면 분석 포인트는 늘어나지만, 그만큼 저장 범위도 커진다.

이 프로젝트의 현재 목표는 “개인 추천 이력 서비스”가 아니라 “설문과 추천 엔진을 계속 개선할 수 있는 최소 신호를 모으는 것”이다.

그래서 이번 단계에서는 저장 범위를 아주 명확하게 제한했다.

1. 추천 결과 top 3는 저장하지 않는다.
2. 사용자의 만족도 점수만 받는다.
3. 어떤 설문 버전과 엔진 버전에서 나온 평가인지만 같이 남긴다.
4. 어떤 답변 조합이 낮은 점수를 받았는지 볼 수 있도록 6개 답변 스냅샷도 같이 남긴다.

이렇게 하면 “결과 저장”은 피하면서도 설문 품질 개선에 필요한 최소 데이터를 확보할 수 있다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyResultView.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackPayloadView.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackSubmission.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackRepository.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java`
- `/Users/alex/project/worldmap/src/main/resources/templates/recommendation/result.html`
- `/Users/alex/project/worldmap/src/main/resources/static/js/recommendation-feedback.js`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`

## 어떤 식으로 흐름을 바꿨는가

기존 추천 흐름은 설문을 제출하면 결과 페이지를 보여주는 데서 끝났다.

이번에는 결과 페이지 하단에 만족도 섹션을 추가했다.

흐름은 이렇게 된다.

1. 사용자가 `/recommendation/survey`에서 설문을 제출한다.
2. 서버는 `RecommendationSurveyService.recommend()`로 top 3 결과를 계산한다.
3. 서버는 결과 페이지를 렌더링하면서 `surveyVersion`, `engineVersion`, 답변 스냅샷을 hidden field로 같이 내려준다.
4. 사용자가 만족도 1~5점을 고른다.
5. 브라우저는 `/api/recommendation/feedback`에 JSON을 POST한다.
6. 서버는 익명 피드백 레코드만 저장한다.

핵심은 `추천 결과 계산`과 `만족도 수집`이 분리되어 있다는 점이다.

## 왜 hidden field로 답변 스냅샷을 다시 내려주는가

결과 페이지는 이미 추천 계산이 끝난 상태다.

이 시점에는 서버가 `RecommendationSurveyResultView`를 만들고 있으므로, 여기 안에 `feedback payload`를 같이 실어 보내는 것이 가장 단순하다.

즉, 결과 페이지는 “보여주기 전용”이 아니라, 바로 다음 만족도 제출에 필요한 최소 컨텍스트도 같이 가진다.

이번 구현에서는 이 역할을 `RecommendationFeedbackPayloadView`가 맡는다.

## 왜 이 로직은 컨트롤러가 아니라 서비스에 있어야 하는가

컨트롤러는 요청을 받고 응답을 내보내는 입구일 뿐이다.

이번 단계의 핵심 규칙은 아래에 있다.

1. 추천 결과는 저장하지 않는다.
2. 저장할 것은 `surveyVersion`, `engineVersion`, `satisfactionScore`, 6개 답변 enum 스냅샷뿐이다.
3. 만족도 점수는 1~5 범위만 허용한다.

이건 request mapping이 아니라 비즈니스 규칙이기 때문에 `RecommendationFeedbackService`가 책임지는 편이 맞다.

컨트롤러는 `RecommendationFeedbackRequest`를 받고 검증한 뒤, 서비스에 넘겨 저장 결과를 반환하는 역할만 한다.

## 어떤 엔티티를 만들었는가

이번에는 추천 결과 엔티티가 아니라 `RecommendationFeedback`만 새로 만들었다.

이 엔티티가 가지는 값은 아래와 같다.

- `surveyVersion`
- `engineVersion`
- `satisfactionScore`
- `climatePreference`
- `pacePreference`
- `budgetPreference`
- `environmentPreference`
- `englishImportance`
- `priorityFocus`

즉, “이 사람이 어떤 결과를 받았는가”보다 “어떤 버전과 어떤 답변 조합에서 만족도를 몇 점 줬는가”에 초점을 맞춘다.

## 프론트는 무엇을 담당하는가

프론트는 딱 두 가지만 한다.

1. 1~5점 버튼 중 하나를 선택하게 한다.
2. 선택 후 `/api/recommendation/feedback`를 비동기로 호출한다.

중복 제출 방지는 프론트에서 버튼 잠금으로 먼저 처리하고, 점수 유효성은 서버가 `@Min`, `@Max` 검증으로 한 번 더 막는다.

즉, UX 보조는 프론트가 하고, 저장 규칙의 최종 보장은 서버가 한다.

## 테스트는 무엇을 고정했는가

이번에는 추천 계산 자체보다 피드백 저장 흐름을 고정하는 테스트가 중요했다.

`RecommendationFeedbackIntegrationTest`에서 두 가지를 확인했다.

1. 정상 요청이면 실제로 피드백 레코드가 저장되는가
2. 만족도 점수가 6처럼 범위를 벗어나면 400으로 거절되는가

또 `RecommendationPageIntegrationTest`에서는 결과 페이지가 실제로 `추천 만족도`, `survey-v1`, `engine-v1`을 포함하는지 확인했다.

## 면접에서는 이렇게 설명하면 된다

“추천 결과 자체는 저장하지 않기로 했고, 대신 설문을 개선할 수 있는 최소 신호만 수집했습니다. 결과 페이지에서 1~5점 만족도와 설문 버전, 엔진 버전, 사용자가 고른 6개 답변만 익명으로 저장하고, 이 데이터를 기준으로 어떤 버전이 더 만족도가 높은지 나중에 비교할 수 있게 했습니다. 저장 규칙은 `RecommendationFeedbackService`에 두고, 결과 페이지와 피드백 API는 별도 흐름으로 분리했습니다.”

## 다음 글

다음 단계는 `설문 / 엔진 버전별 만족도 집계 기준 정리하기`다.

이제 피드백 데이터는 모이기 시작했으니, 다음에는 평균 점수, 응답 수, 버전별 비교 기준을 어떻게 볼지 정리해야 한다.
