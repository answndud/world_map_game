# 48. local demo bootstrap에 현재 추천 피드백 샘플 넣기

> 현재 기준 안내
>
> 이 글은 local demo bootstrap에 current recommendation feedback sample을 처음 넣던 단계 기록이다.
> 개념은 그대로 유효하지만, 본문에 적힌 current engine 버전은 당시 기준(`engine-v9`)이다.
> 현재 저장소 재현 기준 버전은 `survey-v4 / engine-v10`이고, 최신 기준은 [49-add-global-hub-bonus-for-warm-city-hubs.md](./49-add-global-hub-bonus-for-warm-city-hubs.md), [50-current-state-rebuild-map.md](./50-current-state-rebuild-map.md), [LOCAL_DEMO_BOOTSTRAP.md](/Users/alex/project/worldmap/docs/LOCAL_DEMO_BOOTSTRAP.md)를 본다.

## 왜 이 작업이 필요했는가

추천 운영 화면은 이미 두 개가 있었습니다.

- `/dashboard/recommendation/feedback`
- `/dashboard/recommendation/persona-baseline`

문제는 fresh local 환경에서 recommendation feedback이 0개라는 점이었습니다.

그러면 `/dashboard/recommendation/feedback`은 계속
`현재 버전 피드백 더 수집`
메모만 보여 줍니다.

즉, 운영 화면 구조는 있어도
실제로 `rank drift 줄이기` 단계까지 local에서 바로 설명하기는 어려웠습니다.

그래서 이번에는 demo bootstrap이
현재 `survey-v4 / engine-v9` 추천 만족도 샘플도 같이 넣도록 바꿨습니다.

## 어떤 파일이 바뀌는가

- [DemoBootstrapService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/demo/application/DemoBootstrapService.java)
- [RecommendationFeedbackRepository.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackRepository.java)
- [DemoBootstrapIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/demo/DemoBootstrapIntegrationTest.java)
- [LOCAL_DEMO_BOOTSTRAP.md](/Users/alex/project/worldmap/docs/LOCAL_DEMO_BOOTSTRAP.md)

## 요청은 어떻게 흐르는가

이 기능은 HTTP 요청이 아니라 앱 시작 흐름입니다.

1. local profile로 서버 시작
2. `DemoBootstrapInitializer`
3. `DemoBootstrapService.ensureLocalDemoData()`
4. 기존 sample run / guest session 생성
5. current `surveyVersion + engineVersion` 피드백 수 확인
6. 5개 미만이면 부족한 개수만큼 샘플 피드백 추가

즉, local demo 상태는
게임 기록과 추천 운영 표본이 함께 재현됩니다.

## 왜 이 로직이 bootstrap service에 있어야 하는가

이건 회원가입, 설문 제출 같은 공개 요청 흐름이 아닙니다.

`개발자가 local에서 어떤 초기 상태를 바로 확인할 수 있어야 하는가`
라는 개발 환경 규칙입니다.

그래서 컨트롤러나 공개 서비스가 아니라
[DemoBootstrapService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/demo/application/DemoBootstrapService.java)가
책임지는 게 맞습니다.

## 이번에 달라진 점

이제 local DB를 비우고 다시 올려도
아래 상태가 같이 생깁니다.

- demo admin 계정
- demo user 계정
- 위치/인구수 sample run
- guest live session
- current recommendation feedback 5개

그래서 `/dashboard/recommendation/feedback`에 들어가면
바로 `rank drift 줄이기` 메모까지 볼 수 있습니다.

## 테스트는 무엇을 했는가

- [DemoBootstrapIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/demo/DemoBootstrapIntegrationTest.java)
  - current `survey-v4 / engine-v9` 피드백 5개 이상 생성 확인
  - ops review가 `rank drift 줄이기`를 반환하는지 확인
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
  - current version 응답 수와 우선 메모 규칙 유지 확인

## 면접에서 어떻게 설명할 수 있는가

“운영 화면은 만들었지만, local에서 빈 상태로 뜨면 실제 흐름을 설명하기 어렵습니다.  
그래서 demo bootstrap이 현재 추천 버전 피드백 5개도 같이 만들게 해서, fresh local 환경에서도 `/dashboard/recommendation/feedback`이 바로 drift 우선 메모를 보여 주도록 재현성을 맞췄습니다.”
