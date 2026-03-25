# 47. 추천 만족도 운영 화면에 다음 액션 메모 붙이기

## 왜 이 작업이 필요했는가

추천 운영 화면은 두 군데로 나뉘어 있었습니다.

- `/dashboard/recommendation/feedback`
  - 버전별 만족도 집계
- `/dashboard/recommendation/persona-baseline`
  - weak scenario, anchor drift, active signal

문제는 운영자가 이 두 화면을 오가며 직접 해석해야 했다는 점입니다.

- 지금은 피드백을 더 모아야 하는가?
- weak scenario를 먼저 봐야 하는가?
- 아니면 baseline은 충분하고 이제 rank drift를 줄일 차례인가?

이 판단을 화면이 바로 도와주지 못하고 있었습니다.

그래서 이번에는 `/dashboard/recommendation/feedback`이
현재 버전 피드백과 baseline 상태를 함께 읽고,
다음 액션을 한 줄로 정리하는 `ops review`를 추가했습니다.

## 어떤 파일이 바뀌는가

- [AdminRecommendationOpsReviewService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewService.java)
- [AdminRecommendationOpsReviewView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewView.java)
- [AdminPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/web/AdminPageController.java)
- [recommendation-feedback.html](/Users/alex/project/worldmap/src/main/resources/templates/admin/recommendation-feedback.html)
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)

## 요청은 어떻게 흐르는가

운영 요청 흐름은 이렇게 됩니다.

1. `GET /dashboard/recommendation/feedback`
2. `AdminPageController`
3. `RecommendationFeedbackService.summarizeByVersion()`
4. `AdminRecommendationOpsReviewService.loadReview()`
5. `recommendation-feedback.html`

여기서 `AdminRecommendationOpsReviewService`는 두 source를 합칩니다.

- 현재 버전 만족도 표본 수 / 평균 점수
- baseline matched / weak / anchor drift 수

그리고 다음 액션을 아래 순서로 결정합니다.

1. 현재 버전 응답이 5개 미만이면 `피드백 더 수집`
2. weak scenario가 남아 있으면 `weak scenario 먼저`
3. 평균 만족도가 낮으면 `설문 문구 / helper text 점검`
4. 그 외에는 `rank drift 줄이기`

## 왜 이 로직이 서비스에 있어야 하는가

이 판단은 단순 표시 로직이 아닙니다.

- 어떤 신호를 우선 볼 것인가
- 표본 수가 얼마나 되면 “판단 가능”하다고 볼 것인가
- weak와 drift 중 무엇을 먼저 볼 것인가

이건 템플릿 if문보다 운영 규칙에 가깝습니다.

그래서 [AdminRecommendationOpsReviewService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewService.java)가
우선순위를 계산하고,
템플릿은 결과만 보여 주게 분리했습니다.

## 이번에 얻은 운영 화면의 차이

이제 추천 만족도 운영 화면은 단순 표가 아니라,

- 현재 버전 응답 수
- 현재 버전 평균 만족도
- baseline 18 / 18 여부
- anchor drift 개수
- 다음 액션 메모
- 우선 볼 시나리오 ID 3개

를 한 번에 보여 줍니다.

예를 들어 현재 버전 응답이 너무 적으면,
아무리 baseline이 좋아도 먼저 “표본 수집”을 하라고 말합니다.

반대로 응답이 충분하고 weak가 없으면,
이제는 “rank drift 줄이기”가 다음 액션이라고 말해 줍니다.

## 테스트는 무엇을 했는가

- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - 현재 버전 피드백 2개일 때 `현재 버전 피드백 더 수집`
  - 현재 버전 피드백 5개 이상일 때 `rank drift 줄이기`
- [AdminPageIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)
  - 운영 화면에 `운영 판단 메모`와 액션 문구가 실제로 보이는지 확인

## 면접에서 어떻게 설명할 수 있는가

“추천 운영은 만족도 표와 baseline 화면을 따로 보는 것으로 끝나지 않습니다.  
그래서 현재 버전 피드백과 baseline drift를 한 서비스에서 합쳐, 운영 화면이 다음 액션을 바로 메모로 보여 주도록 만들었습니다.  
덕분에 이제는 표본 수집이 먼저인지, weak scenario를 볼지, drift를 줄일지를 서비스가 일관된 규칙으로 판단합니다.”
