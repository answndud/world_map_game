# [Spring Boot 게임 플랫폼 포트폴리오] 14. `/dashboard` 운영 surface와 operations card를 어떻게 설계했는가

## 1. 이번 글에서 풀 문제

운영 화면은 "숫자를 더 많이 보여 주는 페이지"가 아닙니다.

같은 서비스 활동량을 보더라도, public 화면과 운영 화면은 질문이 다릅니다.

public 사용자는 이렇게 묻습니다.

- 오늘 서비스가 얼마나 돌았지
- 상위 기록은 누가 있지

운영자는 이렇게 묻습니다.

- 지금 어디부터 봐야 하지
- 추천 품질은 현재 버전에서 안정적인가
- public 화면과 운영 화면의 경계가 유지되고 있나
- 다음 tuning은 피드백을 더 모아야 하나, baseline drift를 줄여야 하나

즉 `/dashboard`는 `/stats`의 확장판이 아니라 **운영자가 결정을 내리기 위한 별도 surface**입니다.

현재 코드 기준으로 이 글이 닫아야 하는 질문은 아래입니다.

- 왜 `/dashboard`를 `/stats`와 분리했는가
- 왜 admin 첫 화면에 raw table dump가 아니라 route card와 focus item이 필요한가
- 왜 recommendation feedback와 persona baseline review를 별도 화면으로 나누되, 같은 dashboard 허브 아래에 두었는가
- 왜 dashboard는 read-only hub로 시작했는가
- 왜 `/dashboard`가 [ServiceActivityView.java](../src/main/java/com/worldmap/stats/application/ServiceActivityView.java)를 공유해도 `/stats`와 다른 화면인가
- 왜 이 화면의 권한 경계는 admin hardening 글과 분리하되, 설계 글에서는 current-role revalidation 문맥까지 같이 적어야 하는가

이 글을 다 읽으면 현재 저장소 기준으로 아래를 다시 구현할 수 있어야 합니다.

- `/dashboard` 운영 허브 화면
- route card / focus item 중심의 read-only operations surface
- recommendation feedback review 화면
- persona baseline review 화면
- legacy `/admin`에서 `/dashboard`로 이어지는 URL 정리

## 2. 최종 도착 상태

현재 저장소의 최종 도착 상태를 먼저 고정하겠습니다.

### 2-1. `/dashboard`는 admin-only read-only 허브다

[AdminPageController.java](../src/main/java/com/worldmap/admin/web/AdminPageController.java)의 `/dashboard`는 현재 쓰기 액션을 하지 않습니다.

대신 아래를 한 화면에서 보여 줍니다.

- 오늘 서비스 활동 요약
- 추천 운영 상태
- 운영 바로 가기 route card
- 운영 원칙 focus item
- 현재 범위 메모

즉 첫 화면의 목적은 "수정"이 아니라 **판단을 위한 진입 허브**입니다.

### 2-2. dashboard 첫 화면은 `AdminDashboardView` 하나로 묶인다

[AdminDashboardService.java](../src/main/java/com/worldmap/admin/application/AdminDashboardService.java)가 [AdminDashboardView.java](../src/main/java/com/worldmap/admin/application/AdminDashboardView.java)를 만듭니다.

이 상위 view는 아래를 함께 묶습니다.

- 현재 survey version
- 현재 engine version
- `ServiceActivityView`
- 질문 수 / 후보 수
- 총 feedback 응답 수
- tracked version count
- overall average satisfaction
- admin route list
- focus item list

즉 첫 화면은 서비스 활동, 추천 운영 상태, 다음 이동 동선을 한 번에 보여 줍니다.

### 2-3. recommendation 운영 review는 별도 화면으로 분리돼 있다

`/dashboard/recommendation/feedback`는 아래 둘을 같이 봅니다.

- version summary table
- 현재 운영 판단 메모

이 화면은 [AdminRecommendationOpsReviewService.java](../src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewService.java)가 "지금은 피드백을 더 모아야 하는가, weak scenario를 먼저 정리해야 하는가, rank drift를 줄여야 하는가, 아니면 현 상태를 유지해도 되는가"를 한 줄로 요약해 줍니다.

### 2-4. persona baseline review도 별도 화면으로 분리돼 있다

`/dashboard/recommendation/persona-baseline`는 아래를 보여 줍니다.

- total scenario count
- matched floor
- weak scenario count
- active-signal scenario count
- anchor drift scenario count
- weak scenario 카드
- anchor drift 테이블
- active-signal 비교 테이블

즉 dashboard는 "추천 운영"이라는 큰 문제를 다시 두 화면으로 나눕니다.

### 2-5. legacy `/admin`은 `/dashboard` 체계로 redirect된다

[LegacyAdminRedirectController.java](../src/main/java/com/worldmap/admin/web/LegacyAdminRedirectController.java)는 옛 경로를 아래로 연결합니다.

- `/admin` -> `/dashboard`
- `/admin/recommendation/feedback` -> `/dashboard/recommendation/feedback`
- `/admin/recommendation/persona-baseline` -> `/dashboard/recommendation/persona-baseline`

즉 운영 surface의 canonical path는 `/dashboard`입니다.

## 3. 시작 상태

이 글을 쓰기 전 흔한 잘못된 시작 상태는 아래 둘 중 하나입니다.

### 3-1. `/stats`를 운영 화면처럼 확장하는 상태

처음에는 이렇게 생각하기 쉽습니다.

- 이미 `/stats`가 있으니 admin 전용 섹션만 좀 더 붙이면 되지 않나

하지만 이렇게 하면 금방 문제가 생깁니다.

- public-safe metric과 internal note가 섞인다
- guest도 보는 페이지와 admin만 보는 페이지가 한 템플릿에 공존한다
- "왜 이 정보는 public에 나오고 저 정보는 admin에만 나오나" 설명이 어렵다

### 3-2. dashboard를 raw table dump로 만드는 상태

반대로 "admin이니까 더 많은 테이블을 그대로 보여 주면 되지 않나"라고 생각할 수도 있습니다.

이 방식도 좋지 않습니다.

- 운영자가 무엇부터 봐야 하는지 판단하기 어렵다
- recommendation feedback와 persona baseline 같은 품질 신호가 흩어진다
- admin 화면이 product surface가 아니라 DB browser처럼 보인다

WorldMap은 이 두 문제를 **read-only admin hub + review screen 분리**로 풉니다.

## 4. 먼저 알아둘 개념

### 4-1. operations card

이 글에서 operations card는 "운영자가 지금 확인해야 할 정보나 다음 이동 지점"을 카드 단위로 보여 주는 구조입니다.

현재는 두 종류가 있습니다.

- route card
- focus item

### 4-2. route card

route card는 현재 dashboard에서 어디로 이어질지 안내합니다.

현재 route는 아래 다섯 개입니다.

- 공개 Stats 점검
- 추천 만족도 집계
- 페르소나 baseline
- 공개 홈 점검
- 추천 설문 점검

즉 admin 허브는 운영자에게 "다음 클릭"을 알려 줍니다.

### 4-3. focus item

focus item은 "운영 원칙"을 짧게 적은 카드형 메모입니다.

현재 예시는 아래와 같습니다.

- public 화면은 제품 언어만 유지
- 추천 결과는 저장하지 않고 만족도만 수집
- Dashboard는 ADMIN role 세션으로만 접근
- 오늘 활성 수는 game session 시작 기준

즉 화면 자체가 운영 규칙 문서 역할도 겸합니다.

### 4-4. ops review

ops review는 단순 집계 표가 아닙니다.

예를 들어 recommendation feedback review는 아래를 하나의 판단으로 연결합니다.

- current version response count
- current average satisfaction
- baseline matched scenario count
- weak scenario count
- anchor drift count

이 수치를 가지고 "다음 tuning 우선순위"를 제안합니다.

### 4-5. weak scenario / anchor drift / active signal

persona baseline review에서 쓰는 세 용어도 먼저 고정해야 합니다.

- weak scenario: 기대 후보가 현재 top 3에 전혀 없음
- anchor drift: 기대 후보는 top 3에 있지만 기대 1위 anchor가 현재 1위가 아님
- active signal: 새 설문 문항이 실제 후보 구성에 변화를 주는지 확인하는 비교 시나리오

이 세 용어가 recommendation 운영 화면의 핵심 vocabulary입니다.

## 5. 이번 글에서 다룰 파일

### 5-1. dashboard 메인 read model

- [AdminDashboardService.java](../src/main/java/com/worldmap/admin/application/AdminDashboardService.java)
- [AdminDashboardView.java](../src/main/java/com/worldmap/admin/application/AdminDashboardView.java)
- [AdminDashboardRouteView.java](../src/main/java/com/worldmap/admin/application/AdminDashboardRouteView.java)
- [AdminDashboardFocusView.java](../src/main/java/com/worldmap/admin/application/AdminDashboardFocusView.java)

### 5-2. recommendation 운영 review

- [AdminRecommendationOpsReviewService.java](../src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewService.java)
- [AdminRecommendationOpsReviewView.java](../src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewView.java)

### 5-3. persona baseline review

- [AdminPersonaBaselineService.java](../src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java)
- [AdminPersonaBaselineView.java](../src/main/java/com/worldmap/admin/application/AdminPersonaBaselineView.java)
- [AdminPersonaBaselineScenarioView.java](../src/main/java/com/worldmap/admin/application/AdminPersonaBaselineScenarioView.java)

### 5-4. web/controller/template

- [AdminPageController.java](../src/main/java/com/worldmap/admin/web/AdminPageController.java)
- [LegacyAdminRedirectController.java](../src/main/java/com/worldmap/admin/web/LegacyAdminRedirectController.java)
- [index.html](../src/main/resources/templates/admin/index.html)
- [recommendation-feedback.html](../src/main/resources/templates/admin/recommendation-feedback.html)
- [recommendation-persona-baseline.html](../src/main/resources/templates/admin/recommendation-persona-baseline.html)
- [admin-header.html](../src/main/resources/templates/fragments/admin-header.html)

### 5-5. shared source와 권한 경계

- [ServiceActivityService.java](../src/main/java/com/worldmap/stats/application/ServiceActivityService.java)
- [ServiceActivityView.java](../src/main/java/com/worldmap/stats/application/ServiceActivityView.java)
- [AdminAccessInterceptor.java](../src/main/java/com/worldmap/admin/web/AdminAccessInterceptor.java)
- [AdminAccessGuard.java](../src/main/java/com/worldmap/auth/application/AdminAccessGuard.java)

### 5-6. 테스트

- [AdminPageIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)
- [AdminPersonaBaselineServiceIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)

## 6. 왜 `/dashboard`를 `/stats`와 분리했는가

### 6-1. source가 같아도 surface는 다를 수 있다

가장 중요한 포인트입니다.

현재 `/stats`와 `/dashboard`는 둘 다 [ServiceActivityView.java](../src/main/java/com/worldmap/stats/application/ServiceActivityView.java)를 쓸 수 있습니다.

하지만 질문이 다릅니다.

`/stats`:

- public-safe today metrics
- daily top records

`/dashboard`:

- 지금 어떤 운영 review로 들어가야 하는가
- 추천 버전 상태는 어떤가
- 공개 화면 점검 route는 어디인가
- 운영 원칙은 무엇인가

즉 source 공유와 화면 공유는 같은 말이 아닙니다.

### 6-2. public-safe metric과 internal note를 섞지 않기 위해서

`/stats`는 모든 방문자에게 보이는 페이지입니다.

따라서 아래는 여기서 빼야 합니다.

- survey version
- engine version
- feedback response count
- baseline drift
- 운영 판단 메모

이런 것은 `/dashboard`로만 들어가야 합니다.

### 6-3. 운영자는 "다음 액션"이 보여야 한다

public 화면은 읽고 끝날 수 있습니다.
하지만 admin 화면은 다음 동작이 있어야 합니다.

- 공개 홈 점검
- public 설문 점검
- feedback review 열기
- baseline review 열기

그래서 route card가 필요합니다.

### 6-4. 운영 원칙을 화면 안에 드러내기 위해서

운영 화면이 단순 metric 나열이면 보는 사람마다 해석이 달라집니다.

focus item을 따로 둔 이유는 아래를 화면 자체에서 반복하기 위해서입니다.

- 무엇을 public에 숨기고
- 무엇을 internal에만 두고
- 어떤 집계 기준을 쓰는지

즉 dashboard는 ops manual의 첫 장 역할도 합니다.

## 7. `AdminDashboardService`가 실제로 하는 일

[AdminDashboardService.java](../src/main/java/com/worldmap/admin/application/AdminDashboardService.java)는 dashboard 첫 화면의 조립점입니다.

### 7-1. 공통 source를 모은다

현재 service는 아래 source를 읽습니다.

- `serviceActivityService.loadTodayActivity()`
- `recommendationFeedbackService.summarizeByVersion()`
- `recommendationQuestionCatalog.questions().size()`
- `recommendationCountryProfileCatalog.profiles().size()`

즉 activity, feedback, question count, candidate count를 한 view로 묶습니다.

### 7-2. `AdminDashboardView`는 운영 첫 화면의 aggregate다

[AdminDashboardView.java](../src/main/java/com/worldmap/admin/application/AdminDashboardView.java)는 아래 성격의 필드를 같이 가집니다.

| 필드 묶음 | 의미 |
| --- | --- |
| `currentSurveyVersion`, `currentEngineVersion` | 현재 public recommendation runtime 상태 |
| `activity` | 오늘 서비스 활동량 |
| `recommendationQuestionCount`, `recommendationCandidateCount` | recommendation catalog 규모 |
| `totalFeedbackResponses`, `trackedVersionCount`, `overallAverageSatisfaction` | 운영자가 바로 보는 feedback health |
| `adminRoutes` | 다음 운영 화면으로 이동할 링크 |
| `focusItems` | 운영 원칙 메모 |

즉 dashboard는 "숫자만", "링크만", "메모만"이 아니라 이 세 가지를 한 번에 보여 주는 aggregate입니다.

### 7-3. route card는 왜 service에서 고정하나

`adminRoutes()`는 현재 다섯 개 경로를 service에서 고정합니다.

- `/stats`
- `/dashboard/recommendation/feedback`
- `/dashboard/recommendation/persona-baseline`
- `/`
- `/recommendation/survey`

이걸 템플릿에서 하드코딩하지 않은 이유는, route set 자체가 현재 운영 surface의 일부이기 때문입니다.

즉 "운영자가 어디를 점검해야 하는가"는 view 정책이지 단순 마크업이 아닙니다.

### 7-4. focus item은 왜 service에서 고정하나

`focusItems()`도 마찬가지입니다.

현재 focus item은 단순 카피가 아니라 운영 규칙입니다.

- public 화면은 제품 언어만 유지
- 만족도만 수집
- admin role 세션만 접근
- active 수는 session 시작 기준

즉 설계 원칙을 runtime dashboard에 노출하는 셈이므로 service가 view 정책으로 들고 있는 편이 맞습니다.

## 8. dashboard 첫 화면은 템플릿에서 어떻게 읽히나

[index.html](../src/main/resources/templates/admin/index.html)는 dashboard를 네 블록으로 나눕니다.

### 8-1. 서비스 활동 요약

여기서는 [ServiceActivityView.java](../src/main/java/com/worldmap/stats/application/ServiceActivityView.java)를 그대로 읽습니다.

- total members
- today active members
- today active guests
- today started sessions
- today completed runs
- today mode split

여기서 중요한 점은 `/stats`와 같은 source를 쓰되, 카피와 layout은 운영용으로 다르게 두었다는 것입니다.

### 8-2. 추천 운영 상태

이 블록은 recommendation 버전과 feedback health를 보여 줍니다.

- current survey
- current engine
- question count
- candidate pool
- total feedback
- overall avg

즉 public recommendation screen이 아닌, **현재 운영 기준점**을 보여 줍니다.

### 8-3. 운영 바로 가기

여기가 route card section입니다.

현재 dashboard를 read-only hub로 만든 이유도 여기서 드러납니다.

운영자가 다음으로 해야 할 행동을 "현재 허브에서 다른 화면으로 이동"하는 방식으로 풀고 있기 때문입니다.

### 8-4. 운영 원칙 / 현재 범위

마지막 split section은 아래 역할을 합니다.

- 왼쪽: focus item
- 오른쪽: 현재 dashboard의 scope 메모

즉 dashboard는 단순 숫자판이 아니라 "무엇을 하도록 설계됐는지"를 스스로 설명하는 화면입니다.

## 9. recommendation feedback review는 무엇을 보여 주나

`/dashboard/recommendation/feedback`는 단순 집계표가 아닙니다.

### 9-1. current version health

[recommendation-feedback.html](../src/main/resources/templates/admin/recommendation-feedback.html)는 먼저 아래를 보여 줍니다.

- current survey
- current engine
- total feedback
- tracked versions
- overall average satisfaction

즉 지금 공개 중인 recommendation runtime이 어떤 상태인지 한 번에 봅니다.

### 9-2. `AdminRecommendationOpsReviewService`가 priority action을 계산한다

[AdminRecommendationOpsReviewService.java](../src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewService.java)의 `loadReview()`는 단순 summary pass-through가 아닙니다.

아래 source를 결합합니다.

- `RecommendationFeedbackInsightsView`
- `AdminPersonaBaselineView`
- 현재 survey/engine version

그리고 아래 우선순위 결정을 내립니다.

1. current version 응답 수가 너무 적은가
2. weak scenario가 남아 있는가
3. 만족도가 낮은가
4. anchor drift가 남아 있는가
5. 아니면 현 상태 유지인가

즉 이 서비스는 "다음 tuning 방향"을 계산합니다.

### 9-3. 왜 feedback review가 baseline service를 다시 읽나

운영 판단은 feedback score만으로 충분하지 않기 때문입니다.

예를 들어 평균 점수가 높아도 baseline drift가 심하면 tuning 우선순위가 달라질 수 있습니다.

그래서 `AdminRecommendationOpsReviewService`가 [AdminPersonaBaselineService.java](../src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java)를 함께 읽습니다.

### 9-4. 왜 이걸 dashboard 첫 화면에 다 넣지 않았나

feedback review는 테이블과 priority note가 길어집니다.

따라서 dashboard 첫 화면에는 "운영 상태 요약"까지만 놓고, 세부 review는 별도 화면으로 분리했습니다.

이게 route card 구조를 둔 이유이기도 합니다.

## 10. persona baseline review는 무엇을 보여 주나

`/dashboard/recommendation/persona-baseline`는 recommendation 품질을 오프라인 시나리오 기준으로 읽는 화면입니다.

### 10-1. `AdminPersonaBaselineService`의 핵심 질문

[AdminPersonaBaselineService.java](../src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java)는 baseline catalog의 각 scenario에 대해 현재 engine 결과를 다시 계산합니다.

질문은 세 가지입니다.

1. 기대 후보 중 하나라도 top 3에 들어오는가
2. 기대 1위 anchor가 현재 1위인가
3. active-signal scenario에서 기대한 차이가 보이는가

### 10-2. view 분류 규칙

service는 scenario를 세 묶음으로 나눕니다.

- weak
- anchor drift
- active signal

그리고 [AdminPersonaBaselineView.java](../src/main/java/com/worldmap/admin/application/AdminPersonaBaselineView.java)에 아래 숫자를 담습니다.

- total scenario count
- matched scenario count
- weak scenario count
- active signal scenario count
- anchor drift scenario count

### 10-3. `AdminPersonaBaselineScenarioView`가 왜 필요한가

[AdminPersonaBaselineScenarioView.java](../src/main/java/com/worldmap/admin/application/AdminPersonaBaselineScenarioView.java)는 개별 scenario를 아래처럼 설명 가능한 형태로 묶습니다.

- scenario id
- title
- expected anchor candidate
- current anchor candidate
- expected top candidates
- current top candidates
- focus point

즉 운영자가 "왜 이 시나리오가 문제인지"를 바로 읽을 수 있습니다.

### 10-4. template는 세 분류를 다른 block으로 읽는다

[recommendation-persona-baseline.html](../src/main/resources/templates/admin/recommendation-persona-baseline.html)는 아래 세 block을 나눕니다.

- weak scenario 카드
- anchor drift table
- active-signal table

즉 모든 scenario를 한 표에 섞지 않고 "어떤 종류의 문제인가"부터 드러냅니다.

## 11. `/dashboard`의 권한 경계는 이 글에서 어디까지 다뤄야 하나

권한 hardening 전체는 17번 글의 주제입니다.
그래도 dashboard 글에서 최소한의 연결은 해야 합니다.

### 11-1. 왜 권한 이야기를 완전히 빼면 안 되나

`/dashboard`는 admin surface이기 때문입니다.
누가 볼 수 있는지 모르면 화면 구조 설명도 반쪽입니다.

### 11-2. 현재 흐름은 이렇게 요약하면 충분하다

```text
GET /dashboard
-> AdminAccessInterceptor
-> unauthenticated면 /login?returnTo=/dashboard
-> current member/current role 재검증
-> ADMIN만 통과
-> AdminPageController
-> AdminDashboardService
-> admin/index SSR
```

즉 이 글에서는 "dashboard가 admin-only surface"라는 사실만 연결하고, 자세한 hardening taxonomy는 17번 글로 넘기는 편이 맞습니다.

## 12. legacy `/admin` redirect는 왜 남겼나

이건 작은 detail처럼 보이지만 운영 surface 정리에서는 중요합니다.

### 12-1. canonical path를 `/dashboard`로 바꾸고 싶다

현재 제품 언어에서 `/dashboard`가 더 명확합니다.

- public `/stats`
- private `/dashboard`

라는 축이 생기기 때문입니다.

### 12-2. 그래도 예전 링크는 깨지면 안 된다

그래서 [LegacyAdminRedirectController.java](../src/main/java/com/worldmap/admin/web/LegacyAdminRedirectController.java)가 `/admin`을 `/dashboard`로 연결합니다.

즉 URL 개편도 product surface migration의 일부로 설명할 수 있습니다.

## 13. 실패 케이스와 예외 처리

### 13-1. 운영 데이터가 거의 없을 때

feedback이 없거나 baseline이 비어도 화면 구조가 깨지면 안 됩니다.

현재 템플릿은 empty state를 따로 둡니다.

- feedback summary table empty row
- weak scenario empty state
- anchor drift empty state

### 13-2. `/stats`와 `/dashboard` 정보가 섞이는 경우

이건 설계 실패입니다.

아래는 `/stats`에 나오면 안 됩니다.

- current survey/engine version
- ops priority action
- baseline drift
- admin route card

### 13-3. recommendation review가 raw recommendation 결과를 저장하려는 경우

현재 운영 원칙은 "추천 결과 자체는 저장하지 않고 만족도와 답변 스냅샷만 수집"입니다.

focus item에도 이 규칙이 들어 있습니다.

즉 ops screen이 recommendation runtime을 분석하더라도, 저장 정책은 여전히 최소화됩니다.

### 13-4. route card가 너무 많아지는 경우

dashboard 첫 화면은 허브여야지 사이트맵이 되면 안 됩니다.

현재는 다섯 route로 제한해 "다음 점검 포인트"만 드러냅니다.

### 13-5. route card와 focus item을 template에 하드코딩해 버리는 경우

그러면 운영 surface 정책이 service 밖으로 퍼져 버립니다.

현재는 service가 source of truth입니다.

## 14. 테스트로 검증하기

### 14-1. `AdminPageIntegrationTest`

[AdminPageIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)는 admin surface 전체의 가장 강한 통합 증거입니다.

#### `adminDashboardRendersCurrentRecommendationOpsOverview`

막는 리스크:

- dashboard 첫 화면이 activity, recommendation state, admin rule copy를 제대로 못 보여 주는 문제

검증 내용:

- dashboard SSR 성공
- 서비스 활동 요약 render
- mode split render
- current survey / engine render
- "세션 memberId로 현재 회원 role을 다시 조회" copy render

#### `adminRecommendationFeedbackPageRendersVersionSummaryTable`

막는 리스크:

- feedback review 화면이 ops review와 version summary table을 동시에 못 보여 주는 문제

#### `adminPersonaBaselinePageRendersWeakAndActiveSignalSections`

막는 리스크:

- baseline 화면이 weak/active-signal/drift 정보를 운영자가 읽기 쉬운 구조로 못 보여 주는 문제

#### `adminRoutesRedirectUnauthenticatedUsersToLogin`

막는 리스크:

- 비로그인 사용자가 애매한 403/500을 보는 문제

#### `adminRoutesRejectNonAdminMembers`

막는 리스크:

- 일반 user가 admin surface를 볼 수 있는 문제

#### `adminRoutesRejectSessionsWhoseCurrentRoleWasRevoked`

막는 리스크:

- 옛 admin session이 role 강등 뒤에도 살아 있는 문제

### 14-2. `AdminRecommendationOpsReviewServiceIntegrationTest`

[AdminRecommendationOpsReviewServiceIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)는 priority action 계산을 고정합니다.

#### `loadReviewPrioritizesCollectingFeedbackWhenCurrentVersionSampleIsSmall`

막는 리스크:

- 표본이 적은데도 성급히 tuning 결론을 내리는 문제

검증 내용:

- response count 2
- baseline 18/18
- anchor drift 0
- priority action = `현재 버전 피드백 더 수집`

#### `loadReviewPrioritizesRankDriftWhenCurrentVersionSampleIsEnough`

현재 테스트 이름은 그렇지만, 현재 기대값은 `현재 엔진 유지`입니다.

즉 테스트가 보여 주는 핵심은 "응답 수와 baseline 상태에 따라 priority title이 바뀐다"는 점입니다.

### 14-3. `AdminPersonaBaselineServiceIntegrationTest`

[AdminPersonaBaselineServiceIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)는 baseline quality floor를 고정합니다.

검증 내용:

- total scenario 18
- matched scenario 18
- weak 0
- active signal 4
- anchor drift 0
- active scenario ids = `P15`, `P16`, `P17`, `P18`

즉 recommendation 운영 화면의 품질 하한선이 테스트로도 고정됩니다.
다만 이 테스트 묶음이 dashboard를 full ops console로 증명하는 것은 아닙니다.
현재 자동으로 고정되는 것은 read-only 운영 허브, priority action 계산, baseline taxonomy이고,
incident 대응, write action, 알림/승인 workflow 같은 운영 기능은 현재 범위 밖입니다.

## 15. 이 글만 보고 다시 구현하는 순서

### 15-1. 1단계: admin surface를 public surface와 분리한다

먼저 `/stats`와 `/dashboard`를 다른 경로, 다른 템플릿, 다른 controller로 나눕니다.

### 15-2. 2단계: dashboard first-screen aggregate를 정의한다

`AdminDashboardView` 같은 상위 view를 만들고 아래를 묶습니다.

- activity
- recommendation runtime state
- route cards
- focus items

### 15-3. 3단계: route card와 focus item을 service에서 고정한다

template 하드코딩이 아니라 service policy로 둡니다.

### 15-4. 4단계: recommendation review를 feedback summary와 baseline 판단으로 분리한다

feedback review는 단순 table이 아니라 priority action 계산까지 하게 만듭니다.

### 15-5. 5단계: persona baseline을 scenario taxonomy로 분류한다

최소 아래 세 묶음을 만듭니다.

- weak
- anchor drift
- active signal

### 15-6. 6단계: admin surface를 read-only hub로 시작한다

초기에는 write action보다 "무엇을 봐야 하는가"를 먼저 고정합니다.

### 15-7. 7단계: legacy `/admin`을 `/dashboard` 체계로 정리한다

운영 URL도 canonical path를 갖게 만듭니다.

## 16. 구현 체크리스트

- `/dashboard`는 `/stats`와 다른 템플릿/컨트롤러를 쓴다
- dashboard first screen은 `AdminDashboardView` 하나로 묶인다
- `AdminDashboardService`가 route card와 focus item을 만든다
- activity summary와 recommendation runtime state가 같은 화면에 들어간다
- feedback review는 version summary table과 priority note를 같이 보여 준다
- baseline review는 weak/drift/active-signal taxonomy를 가진다
- legacy `/admin`은 `/dashboard`로 redirect된다
- admin surface는 read-only hub로 시작한다
- public/private 경계 copy가 템플릿에도 드러난다

## 17. 실행 / 검증 명령

이 글의 핵심 흐름은 아래 테스트 묶음으로 확인할 수 있습니다.

```bash
./gradlew test \
  --tests com.worldmap.admin.AdminPageIntegrationTest \
  --tests com.worldmap.admin.AdminRecommendationOpsReviewServiceIntegrationTest \
  --tests com.worldmap.admin.AdminPersonaBaselineServiceIntegrationTest
```

## 18. 산출물 체크리스트

이 단계가 끝났다면 아래를 설명할 수 있어야 합니다.

- 왜 `/dashboard`는 `/stats`의 확장판이 아닌가
- dashboard 첫 화면이 왜 route card와 focus item을 가지는가
- recommendation feedback review와 persona baseline review가 왜 분리되어야 하는가
- 왜 admin surface는 초기엔 read-only hub로 시작하는가
- 왜 source를 공유해도 public/private surface는 달라질 수 있는가

## 19. 현재 구현의 한계

### 19-1. write action이 거의 없다

현재 dashboard는 운영 판단 허브입니다.

- 알림 승인
- 배포 스위치
- 직접적인 rule edit

같은 기능은 아직 없습니다.

### 19-2. incident/alert 운영 도구는 없다

현재 focus는 recommendation quality와 public surface 점검입니다.

즉 full ops console은 아닙니다.

### 19-3. dashboard first screen의 route set은 아직 고정 목록이다

### 19-4. recommendation 운영 판단은 현재 feedback + baseline source에 집중한다

즉 현재 테스트는 recommendation quality review 허브를 강하게 고정하지만,
infra alert, error budget, 배포 승인 같은 broader ops decision까지 포함하는 구조는 아니다.

현재 route는 service에서 하드코딩한 다섯 개입니다.

후에 운영 기능이 늘면 grouping 전략이 더 필요할 수 있습니다.

### 19-4. recommendation ops는 아직 tuning workflow를 직접 실행하진 않는다

현재는 판단을 위한 read model까지만 제공합니다.

즉 "어디를 먼저 봐야 하는가"는 정리하지만 "버튼 한 번으로 tuning 반영"까지는 가지 않습니다.

## 20. 자주 막히는 지점

### 20-1. dashboard를 raw DB table 나열로 만드는 것

운영자는 더 많은 행이 아니라 더 좋은 판단 surface가 필요합니다.

### 20-2. `/stats`와 `/dashboard`를 같은 템플릿에서 if문으로 해결하려는 것

공개 경계와 운영 경계가 흐려집니다.

### 20-3. recommendation review를 public recommendation result와 같은 층으로 보는 것

public result는 사용자 경험이고, dashboard review는 운영 품질 관리입니다.

### 20-4. route card를 링크 모음 정도로만 생각하는 것

현재 route set 자체가 운영 허브의 정보 구조입니다.

### 20-5. focus item을 단순 마케팅 카피처럼 쓰는 것

현재 focus item은 운영 규칙을 짧게 재선언하는 역할입니다.

## 21. 이 글을 30초로 설명하면

WorldMap의 `/dashboard`는 `/stats`보다 숫자가 더 많은 페이지가 아니라, 운영자가 지금 어디를 봐야 하는지 빠르게 판단하게 만드는 read-only 허브입니다. `AdminDashboardService`가 `ServiceActivityView`, recommendation runtime 상태, route card, focus item을 묶어 첫 화면을 만들고, recommendation quality는 `AdminRecommendationOpsReviewService`가 current version feedback과 baseline을 합쳐 다음 tuning 우선순위를 제안하며, `AdminPersonaBaselineService`는 18개 시나리오를 weak/anchor drift/active signal로 나눠서 보여 줍니다. 그래서 source 일부는 public stats와 공유해도, surface와 판단 목적은 완전히 다른 admin product로 설명할 수 있습니다.

## 22. 면접에서 바로 받을 수 있는 꼬리 질문

### 22-1. 왜 `/stats`와 `/dashboard`를 분리했나요?

public-safe metric과 internal ops signal의 질문이 다르기 때문입니다. `/stats`는 누구나 보는 서비스 현황이고, `/dashboard`는 admin의 다음 판단과 이동을 돕는 운영 허브입니다.

### 22-2. 왜 dashboard 첫 화면에 route card가 필요한가요?

dashboard는 모든 것을 한 화면에 다 담기보다, 운영자가 지금 어떤 review 화면으로 들어가야 하는지 빠르게 판단하게 만드는 허브 역할을 하기 때문입니다.

### 22-3. recommendation feedback review와 persona baseline review를 왜 나눴나요?

하나는 실제 응답 분포와 version health를 보고, 다른 하나는 오프라인 baseline 시나리오 품질을 봅니다. 둘을 섞으면 운영 질문이 흐려집니다.

### 22-4. 왜 dashboard를 read-only로 시작했나요?

지금 단계에서는 "무엇을 보고 어떻게 판단할 것인가"를 먼저 고정하는 게 중요했고, write action보다 설명 가능한 운영 surface를 먼저 만드는 편이 제품과 포트폴리오 둘 다에 유리했기 때문입니다.

## 23. 다음 글과의 연결

이 글은 운영 화면 자체의 구조를 다룹니다.
다음 단계에서는 public scope를 다시 정리하고 신규 게임 3종이 public surface에 어떻게 합쳐졌는지를 봐야 합니다.

- 이전 글: [13-mypage-and-public-stats-read-models.md](./13-mypage-and-public-stats-read-models.md)
- 다음 글: [15-public-scope-reset-and-new-games-lineup.md](./15-public-scope-reset-and-new-games-lineup.md)
- 권한 하드닝 후속 글: [17-game-integrity-current-member-and-role-revalidation.md](./17-game-integrity-current-member-and-role-revalidation.md)
