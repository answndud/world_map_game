# [Spring Boot 포트폴리오] 17. 추천 설문을 8문항으로 확장하기

## 이번 글의 핵심 질문

추천 설문이 너무 짧으면 사용자는 “이 정도 질문으로 정말 나한테 맞는 나라를 고를 수 있나?”라는 느낌을 받기 쉽다.

이번 단계의 질문은 이것이다.

“추천 계산 구조는 그대로 서버가 맡기면서, 설문 입력 밀도는 어떻게 자연스럽게 늘릴 수 있을까?”

이번에는 기존 6문항에 `정착 성향`, `이동 생활 방식` 두 문항을 추가해서 전체 설문을 8문항으로 확장했다.

## 왜 이 단계가 필요한가

기존 6문항은 기후, 생활 속도, 물가, 도시/자연, 영어, 최우선 기준을 빠르게 고르는 데는 충분했다.

하지만 실제 사용자 입장에서는 아래 두 축이 빠져 있었다.

1. 나는 이 나라를 짧게 경험해 보고 싶은가, 장기 정착까지 고려하는가
2. 일상 이동에서 대중교통 / 도보 생활이 중요한가, 넓은 공간과 느긋한 이동도 괜찮은가

즉, “후보를 가르는 마지막 체감 질문”이 조금 부족했다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java`
- `/Users/alex/project/worldmap/src/main/resources/templates/recommendation/survey.html`
- `/Users/alex/project/worldmap/src/main/resources/templates/recommendation/result.html`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`

## 어떤 질문을 추가했는가

이번에 추가한 질문은 두 개다.

### 1. 정착 성향

- `가볍게 살아보기`
- `둘 다 고려`
- `장기 정착 안정성`

이 질문은 단순히 “좋아 보이는 나라”보다, 실제로 오래 머무를 생각까지 있는지를 더 명확하게 드러낸다.

### 2. 이동 생활 방식

- `대중교통 / 도보 중심`
- `둘 다 괜찮음`
- `여유 공간 / 차량 이동도 괜찮음`

이 질문은 도시 인프라와 생활 리듬을 사용자가 더 직관적으로 고르게 만든다.

## 왜 새 국가 프로필 필드를 늘리지 않았는가

이번 단계에서는 국가 프로필 카탈로그에 완전히 새로운 축을 대거 추가하지 않았다.

대신 기존에 이미 있던 값들을 조합했다.

- 정착 성향:
  - 안전
  - 복지
  - 영어 친화도
  - 문화 다양성 / 음식
- 이동 생활 방식:
  - 도시성
  - 생활 속도
  - 안전

이렇게 한 이유는 지금 단계의 목적이 “질문 수를 늘리되 설명 가능한 구조를 유지하는 것”이기 때문이다.

즉, 새 질문을 넣되 현재 프로필 데이터로 충분히 설명 가능한 범위 안에서 확장했다.

## 왜 이 로직은 컨트롤러가 아니라 서비스에 있어야 하는가

추가 질문이 실제 추천 순위에 영향을 주는 순간, 이건 단순한 폼 필드가 아니라 추천 도메인 규칙이 된다.

그래서 아래 로직은 `RecommendationSurveyService`에 있다.

1. `정착 성향` 점수 계산
2. `이동 생활 방식` 점수 계산
3. 기존 질문과 합산한 최종 총점 계산
4. top 3 정렬

컨트롤러는 여전히 얇다.

- 설문 GET 렌더링
- POST validation
- 서비스 호출
- 결과 페이지 SSR

## 피드백 저장 구조도 왜 같이 바꿔야 하는가

이번 단계는 설문 화면만 바뀐 것이 아니다.

결과 페이지의 만족도 피드백도 같이 바꿨다.

왜냐하면 지금 프로젝트는 추천 결과를 저장하지 않는 대신,

- `surveyVersion`
- `engineVersion`
- `satisfactionScore`
- 사용자가 선택한 답변 스냅샷

만 남기고 있기 때문이다.

즉, 질문 수가 8개로 늘면 피드백 스냅샷도 8개 답변 기준으로 같이 늘어야 한다.

그래서 `RecommendationFeedbackRequest`, `RecommendationFeedback`, 결과 페이지 hidden field까지 함께 확장했다.

## 테스트는 무엇을 확인했는가

이번 단계에서는 세 가지를 같이 봤다.

1. `RecommendationSurveyServiceTest`
   - 기존 deterministic top 3가 유지되는가
   - 새 질문이 실제로 순위에 반영될 수 있는가
2. `RecommendationPageIntegrationTest`
   - 설문 제출 POST가 8문항 기준으로 동작하는가
   - 결과 페이지가 `survey-v2`, `engine-v2`를 포함하는가
3. `RecommendationFeedbackIntegrationTest`
   - 만족도 피드백 저장이 8개 답변 스냅샷 기준으로 동작하는가

## 면접에서는 이렇게 설명하면 된다

“추천 설문이 너무 짧으면 체감상 정보가 부족해 보일 수 있어서, 기존 6문항에 `정착 성향`과 `이동 생활 방식`을 추가해 8문항으로 확장했습니다. 중요한 건 질문 수만 늘린 것이 아니라, 새 답변도 `RecommendationSurveyService` 안에서 기존 국가 프로필 값과 합산되도록 만들어 deterministic 추천 구조를 유지한 점입니다. 또 만족도 피드백은 결과 저장 대신 답변 스냅샷만 남기기 때문에, 피드백 엔티티와 결과 페이지 hidden payload도 같이 8답변 기준으로 확장했습니다.”

## 다음 글

다음 단계는 이 8문항 구조를 바탕으로 다시 `engine-v2` 실험을 하는 것이다.

이제는 질문 수가 늘었기 때문에, coverage와 snapshot을 같이 보면서 어떤 문항이 실제 품질 개선에 도움이 되는지 더 분명하게 비교할 수 있다.
