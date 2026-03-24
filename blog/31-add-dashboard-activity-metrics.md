# Dashboard에 회원 수와 오늘 활성 지표 붙이기

## 왜 이 글을 쓰는가

운영 화면 주소를 `/dashboard`로 바꿨다면
그 다음에는 진짜 dashboard다운 숫자가 있어야 한다.

예를 들면 이런 값이다.

- 전체 회원 수
- 오늘 활성 회원 수
- 오늘 활성 게스트 수
- 오늘 시작된 세션 수
- 오늘 완료된 게임 수

이번 단계는 이 숫자를 어디서 어떻게 읽을지 고정하는 작업이다.

## 이전 단계의 한계

이전 `/dashboard`는 운영 진입 구조는 정리됐지만
실제 운영 수치는 거의 추천 관련 정보만 보였다.

즉, 이런 질문에는 바로 답하기 어려웠다.

- 지금 회원이 몇 명인가?
- 오늘 실제 플레이한 사람은 몇 명인가?
- 오늘 완료된 게임이 몇 판인가?

운영자가 제일 먼저 보고 싶은 기초 수치가 빠져 있었던 셈이다.

## 이번 단계에서 바꾼 것

`/dashboard` 첫 화면에 아래 지표 카드를 추가했다.

- `TOTAL MEMBERS`
- `TODAY ACTIVE MEMBERS`
- `TODAY ACTIVE GUESTS`
- `TODAY STARTED SESSIONS`
- `TODAY COMPLETED RUNS`
- `TODAY MODE SPLIT`

마지막 카드는 위치/인구수 모드별 오늘 완료 수를 같이 보여준다.

## 어떤 데이터를 어디서 읽는가

이번 단계에서 가장 중요한 설계는
`지표마다 source of truth를 다르게 둔다`는 점이다.

### 1. 총 회원 수

이건 당연히 `member_account`가 맞다.

즉, `MemberRepository.count()`를 읽는다.

### 2. 오늘 활성 회원 / 게스트

이 값은 “오늘 게임을 시작했는가”가 기준이다.

그래서 `leaderboard_record`가 아니라
각 게임 세션의 `startedAt`을 봐야 한다.

- 위치 게임 세션
- 인구수 게임 세션

두 repository에서 오늘 시작한 세션을 읽고,

- 회원은 `memberId`
- 게스트는 `guestSessionKey`

를 distinct로 합친다.

### 3. 오늘 완료 게임 수

이 값은 “오늘 끝난 run”이 기준이다.

그래서 source of truth는 `leaderboard_record.finishedAt`이다.

완료된 게임 수는 run 단위라서
원본 세션보다 `leaderboard_record`가 더 자연스럽다.

## 왜 한 테이블에서 다 읽지 않았는가

겉으로는 dashboard 숫자 카드지만,
사실 이 단계의 핵심은 read model 분리다.

억지로 한 테이블에서 다 읽으려 하면 설명이 꼬인다.

- 회원 수는 member
- 오늘 시작은 game session
- 오늘 완료는 leaderboard_record

가 더 자연스럽다.

즉, 이번 단계는 “dashboard 카드 추가”이기도 하지만
동시에 “운영 지표의 source of truth를 분리”한 단계이기도 하다.

## 바뀐 파일

- `src/main/java/com/worldmap/game/location/domain/LocationGameSessionRepository.java`
- `src/main/java/com/worldmap/game/population/domain/PopulationGameSessionRepository.java`
- `src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
- `src/main/java/com/worldmap/admin/application/AdminDashboardActivityView.java`
- `src/main/java/com/worldmap/admin/application/AdminDashboardView.java`
- `src/main/java/com/worldmap/admin/application/AdminDashboardService.java`
- `src/main/resources/templates/admin/index.html`
- `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`

## 요청 흐름

```text
GET /dashboard
-> AdminAccessInterceptor
-> AdminPageController.dashboard()
-> AdminDashboardService.loadDashboard()
-> MemberRepository.count()
-> game session repository distinct startedAt 집계
-> LeaderboardRecordRepository finishedAt 집계
-> AdminDashboardView
-> admin/index.html 렌더링
```

## repository를 어떻게 나눴는가

### 게임 세션 repository

위치/인구수 각 repository에 아래 조회를 추가했다.

- 오늘 시작된 세션 수
- 오늘 시작한 distinct memberId 목록
- 오늘 시작한 distinct guestSessionKey 목록

이 값은 서비스에서 합쳐서 최종 active 수를 만든다.

왜냐하면 같은 회원이 위치/인구수 두 모드를 모두 했을 수 있기 때문이다.

즉, repository는 “모드별 raw distinct 값”만 주고,
최종 집계는 서비스가 한다.

### leaderboard repository

여기서는 오늘 완료된 run 수를 읽는다.

- 전체 완료 수
- 위치 게임 완료 수
- 인구수 게임 완료 수

완료 run 집계는 `leaderboard_record`가 가장 잘 설명한다.

## 왜 컨트롤러가 아니라 서비스인가

컨트롤러는 여전히 `/dashboard` 진입만 맡는다.

이번 단계의 핵심 판단은 아래다.

- 어떤 지표를 어떤 저장소에서 읽을지
- 시작 기준과 완료 기준을 어떻게 나눌지
- 두 게임 세션 repository의 distinct 값을 어떻게 합칠지

이건 HTTP 분기보다 운영 read model 조합 규칙이다.

그래서 `AdminDashboardService`가 맡는 것이 맞다.

## 테스트

이번 단계에서 중요한 테스트는 `AdminPageIntegrationTest`다.

실제로 아래 데이터를 넣었다.

- 회원 2명
- 오늘 시작한 member 세션 1개
- 오늘 시작한 guest 세션 1개
- 오늘 완료된 위치 run 1개
- 오늘 완료된 인구수 run 1개

그 뒤 `/dashboard` HTML에서

- `TOTAL MEMBERS`
- `TODAY ACTIVE MEMBERS`
- `TODAY ACTIVE GUESTS`
- `TODAY COMPLETED RUNS`
- `L 1 / P 1`

이 실제로 보이는지 검증했다.

즉, 이번 단계는 단순 템플릿 수정보다
운영 지표 계산 기준을 테스트로 고정한 것이다.

## 면접에서 어떻게 설명할까

이렇게 말하면 된다.

> Dashboard에 운영 수치를 붙일 때 한 테이블에서 다 읽지 않았습니다. 총 회원 수는 `member_account`, 오늘 활성 회원/게스트는 각 게임 세션의 `startedAt`, 오늘 완료 게임 수는 `leaderboard_record.finishedAt`를 source of truth로 삼았습니다. 그리고 위치/인구수 두 모드에서 중복된 사용자를 중복 계산하지 않도록 distinct memberId, guestSessionKey를 서비스에서 합쳤습니다. 컨트롤러는 `/dashboard` 진입만 맡고, 실제 운영 read model 조합은 `AdminDashboardService`가 담당하게 했습니다.

## 다음 글 예고

이제 dashboard shell과 기초 수치는 생겼다.

다음 단계는 이런 값들이다.

- 총 회원 수 대비 오늘 활성 비율
- 최근 7일 추이
- 추천 피드백 응답 추이
- 모드별 일간 플레이 추이
