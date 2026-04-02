# [Spring Boot 게임 플랫폼 포트폴리오] 13. `/mypage`와 공개 `/stats` read model을 어떻게 나눴는가

## 1. 이번 글에서 풀 문제

게임 세션과 leaderboard row를 잘 저장했다고 해서 곧바로 사용자 화면이 만들어지지는 않습니다.

이 시점부터는 다른 문제가 생깁니다.

- 로그인한 회원은 "내 기록"을 보고 싶다
- 비로그인 방문자는 "서비스 전체가 어떻게 돌아가는지"를 보고 싶다
- 운영자는 같은 활동량을 보더라도 더 많은 운영 힌트와 내부 링크를 보고 싶다

즉 같은 원본 데이터를 읽더라도 **누가 보는가**에 따라 필요한 정보가 달라집니다.

현재 프로젝트에서 이 문제는 세 화면으로 나뉩니다.

1. `/mypage`
2. `/stats`
3. `/dashboard`

이 글은 그중 앞의 두 개를 다룹니다.

- 왜 `/mypage`와 `/stats`를 같은 페이지로 합치지 않았는가
- 왜 leaderboard row를 그대로 템플릿에 뿌리지 않았는가
- 왜 `MyPageService`와 `ServiceActivityService`가 따로 필요한가
- `/stats`와 `/dashboard`가 같은 `ServiceActivityView`를 써도 왜 같은 화면이 아닌가
- `/mypage`의 `currentRank`는 왜 "기록 당시 순위"가 아니라 "현재 보드 기준 순위"라고 설명해야 하는가

이 글을 다 읽으면 현재 저장소 기준으로 아래를 다시 구현할 수 있어야 합니다.

- member 전용 기록 허브 `/mypage`
- 비로그인도 볼 수 있는 공개 지표 `/stats`
- 개인 read model과 public read model의 분리
- 공통 activity source와 서로 다른 SSR surface
- current member를 model advice에서 주입하고 controller는 조합만 하는 구조

## 2. 최종 도착 상태

현재 코드베이스의 최종 도착 상태를 먼저 고정하겠습니다.

### 2-1. `/mypage`는 로그인한 member의 개인 기록 허브다

[MyPageController.java](../src/main/java/com/worldmap/web/MyPageController.java)는 model에 들어 있는 `currentMember`를 기준으로 [MyPageService.java](../src/main/java/com/worldmap/mypage/application/MyPageService.java)의 `loadDashboard(memberId)`를 호출합니다.

이 결과는 [MyPageDashboardView.java](../src/main/java/com/worldmap/mypage/application/MyPageDashboardView.java) 하나로 묶여 SSR 템플릿 [mypage.html](../src/main/resources/templates/mypage.html)에 전달됩니다.

현재 `/mypage`는 아래 블록으로 구성됩니다.

- profile + 총 완료 플레이 수
- 게임별 best run 카드
- 게임별 performance summary
- 최근 완료 run 10개

### 2-2. `/stats`는 공개 가능한 서비스 지표만 보여 준다

[StatsPageController.java](../src/main/java/com/worldmap/stats/web/StatsPageController.java)는 [ServiceActivityService.java](../src/main/java/com/worldmap/stats/application/ServiceActivityService.java)의 `loadTodayActivity()`와 [LeaderboardService.java](../src/main/java/com/worldmap/ranking/application/LeaderboardService.java)의 daily Top 3 read model을 조합해 [stats/index.html](../src/main/resources/templates/stats/index.html)에 넘깁니다.

현재 `/stats`는 아래만 공개합니다.

- 전체 회원 수
- 오늘 활성 플레이어 수
- 오늘 시작된 세션 수
- 오늘 완료된 run 수
- 게임별 오늘 완료 수
- 각 게임 daily Top 3

즉 member별 상세 run 히스토리나 운영 메모는 여기로 나오지 않습니다.

### 2-3. `/dashboard`는 `/stats`의 상위 호환이 아니라 별도 운영 화면이다

[AdminDashboardService.java](../src/main/java/com/worldmap/admin/application/AdminDashboardService.java)는 내부적으로 [ServiceActivityService.java](../src/main/java/com/worldmap/stats/application/ServiceActivityService.java)의 `ServiceActivityView`를 재사용합니다.

하지만 `/dashboard`는 여기에 아래를 더 붙입니다.

- recommendation feedback summary
- persona baseline
- 운영용 route 링크
- 운영 판단용 focus item

즉 `/stats`는 public surface, `/dashboard`는 ops surface입니다.

### 2-4. current rank는 historical rank가 아니라 current board rank다

현재 `/mypage`의 best run과 recent play는 [LeaderboardRecordRepository.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java)의 정렬 결과를 지금 시점에 다시 계산한 rank를 보여 줍니다.

즉 [MyPageService.java](../src/main/java/com/worldmap/mypage/application/MyPageService.java)의 `rankFor(...)`는 "record 당시 고정 순위"를 들고 있지 않습니다.

그래서 현재 UI와 문구도 아래처럼 맞춰져 있습니다.

- `현재 #1`
- `현재 순위 계산 중`

이건 읽기 모델 설명에서 매우 중요합니다.

## 3. 시작 상태

이 글을 쓰기 전 흔한 잘못된 시작 상태는 아래 둘 중 하나입니다.

### 3-1. leaderboard row만 있으면 화면도 자동으로 된다고 생각하는 상태

초기에는 이렇게 오해하기 쉽습니다.

- 이미 `LeaderboardRecord`가 있으니 `/mypage`도 그걸 그대로 뿌리면 되지 않나
- 이미 session count를 셀 수 있으니 `/stats`도 대충 repository count만 화면에 넘기면 되지 않나

하지만 이렇게 하면 금방 한계가 나옵니다.

- `/mypage`는 개인 기록 허브인데 raw leaderboard row만으로는 성향 요약을 만들 수 없다
- `/stats`는 공개 화면인데 raw 내부 지표를 그대로 노출할 수 없다
- 같은 원본을 읽더라도 목적별 view 객체가 없으면 템플릿이 과도하게 복잡해진다

### 3-2. `/mypage`, `/stats`, `/dashboard`를 한 DTO로 우겨 넣는 상태

이건 반대 방향의 실수입니다.

- 개인 기록
- 공개 지표
- 운영 지표

를 하나의 giant DTO에 넣기 시작하면 아래 문제가 생깁니다.

- 권한 경계가 흐려진다
- 어떤 필드가 public safe인지 설명하기 어려워진다
- controller/template가 if문으로 뒤덮인다

WorldMap은 이 문제를 **read model 분리**로 풉니다.

## 4. 먼저 알아둘 개념

### 4-1. write model과 read model

현재 프로젝트의 원본 쓰기 모델은 대체로 아래입니다.

- `*_game_session`
- `*_game_stage`
- `*_game_attempt`
- `leaderboard_record`

하지만 화면이 바로 이 row들을 읽는 것은 아닙니다.

화면은 아래처럼 가공된 read model을 받습니다.

- `MyPageDashboardView`
- `MyPageBestRunView`
- `MyPageModePerformanceView`
- `MyPageRecentPlayView`
- `ServiceActivityView`
- `LeaderboardView`

### 4-2. user-centric read model

`/mypage` 같은 화면은 "한 명의 현재 member"를 중심으로 읽습니다.

즉 질문이 아래와 같습니다.

- 나는 몇 판을 끝냈는가
- 게임별 최고 run은 무엇인가
- 최근 플레이는 무엇인가
- 내 플레이 성향은 어떤가

### 4-3. public service metric

`/stats`는 특정 회원이 아니라 서비스 전체 흐름을 봅니다.

즉 질문이 아래처럼 바뀝니다.

- 오늘 몇 명이 플레이했는가
- guest와 member는 각각 몇 명인가
- 어떤 게임이 많이 끝났는가
- 지금 public 보드 상위는 누구인가

### 4-4. shared metric source, different surface

`ServiceActivityView`는 `/stats`와 `/dashboard`에서 같이 씁니다.

하지만 surface는 다릅니다.

- `/stats`: public-safe metrics만 노출
- `/dashboard`: 같은 metrics 위에 운영 판단 정보와 admin route를 얹음

즉 source를 공유해도 surface는 분리될 수 있습니다.

### 4-5. current rank vs rank at record time

이건 꼭 짚어야 합니다.

현재 `/mypage`의 rank는 `LeaderboardRecord`에 저장된 historical rank가 아닙니다.
`MyPageService.rankFor(...)`가 **현재 전체 보드를 다시 정렬해서 찾은 순위**입니다.

그래서 이 프로젝트에서는 다음 표현이 맞습니다.

- `현재 랭킹 위치`
- `currentRank`

다음 표현은 현재 코드와 맞지 않습니다.

- `기록 당시 순위`
- `rankAtRecordTime`

## 5. 이번 글에서 다룰 파일

### 5-1. mypage read model

- [MyPageService.java](../src/main/java/com/worldmap/mypage/application/MyPageService.java)
- [MyPageDashboardView.java](../src/main/java/com/worldmap/mypage/application/MyPageDashboardView.java)
- [MyPageBestRunView.java](../src/main/java/com/worldmap/mypage/application/MyPageBestRunView.java)
- [MyPageModePerformanceView.java](../src/main/java/com/worldmap/mypage/application/MyPageModePerformanceView.java)
- [MyPageRecentPlayView.java](../src/main/java/com/worldmap/mypage/application/MyPageRecentPlayView.java)
- [MyPageController.java](../src/main/java/com/worldmap/web/MyPageController.java)
- [mypage.html](../src/main/resources/templates/mypage.html)

### 5-2. stats read model

- [ServiceActivityService.java](../src/main/java/com/worldmap/stats/application/ServiceActivityService.java)
- [ServiceActivityView.java](../src/main/java/com/worldmap/stats/application/ServiceActivityView.java)
- [StatsPageController.java](../src/main/java/com/worldmap/stats/web/StatsPageController.java)
- [stats/index.html](../src/main/resources/templates/stats/index.html)

### 5-3. shared source와 current member 주입

- [LeaderboardService.java](../src/main/java/com/worldmap/ranking/application/LeaderboardService.java)
- [LeaderboardRecordRepository.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java)
- [SiteHeaderModelAdvice.java](../src/main/java/com/worldmap/web/SiteHeaderModelAdvice.java)

### 5-4. `/dashboard`와의 비교용 파일

- [AdminDashboardService.java](../src/main/java/com/worldmap/admin/application/AdminDashboardService.java)
- [AdminDashboardView.java](../src/main/java/com/worldmap/admin/application/AdminDashboardView.java)

### 5-5. 테스트

- [MyPageServiceIntegrationTest.java](../src/test/java/com/worldmap/mypage/MyPageServiceIntegrationTest.java)
- [MyPageControllerTest.java](../src/test/java/com/worldmap/web/MyPageControllerTest.java)
- [StatsPageControllerTest.java](../src/test/java/com/worldmap/stats/StatsPageControllerTest.java)

## 6. `/mypage`와 `/stats`를 왜 분리했는가

### 6-1. 보는 사람이 다르다

`/mypage`는 로그인한 member가 자기 기록을 봅니다.

즉 private-ish user surface입니다.

`/stats`는 비로그인 방문자도 볼 수 있습니다.

즉 public surface입니다.

같은 정보를 다 보여 주면 안 됩니다.

### 6-2. 질문 자체가 다르다

`/mypage`가 답해야 하는 질문:

- 내가 최근에 뭘 했나
- 내 최고 run은 뭔가
- 나는 어떤 게임에서 1트 클리어율이 높은가

`/stats`가 답해야 하는 질문:

- 오늘 서비스가 얼마나 돌았나
- 어떤 게임이 많이 완료됐나
- 오늘의 상위 기록은 누구인가

즉 둘은 source가 아니라 **질문**이 다릅니다.

### 6-3. 권한 경계도 다르다

`/mypage`는 current member가 없으면 기록 허브를 채우지 않습니다.
대신 guest prompt를 보여 줍니다.

`/stats`는 로그인 없이도 열립니다.
하지만 운영자용 focus item이나 recommendation version summary는 여기에 노출하지 않습니다.

### 6-4. `/dashboard`와의 차이도 분명해야 한다

`/stats`를 public dashboard처럼 만들면 안 됩니다.

[AdminDashboardService.java](../src/main/java/com/worldmap/admin/application/AdminDashboardService.java)는 같은 `ServiceActivityView`를 써도 아래를 더 얹습니다.

- 추천 운영 상태
- feedback response 집계
- admin route 카드
- 운영 판단 메모

즉 `/stats`는 "공개 가능한 오늘의 현황", `/dashboard`는 "운영자가 판단하는 내부 화면"입니다.

## 7. current member는 어디서 들어오나

`/mypage`는 controller가 session을 직접 읽지 않습니다.

### 7-1. `SiteHeaderModelAdvice`

[SiteHeaderModelAdvice.java](../src/main/java/com/worldmap/web/SiteHeaderModelAdvice.java)는 모든 SSR 요청에 아래 model attribute를 심습니다.

- `currentMember`
- `isAuthenticated`
- `authenticatedNickname`
- `showDashboardLink`

이 값은 [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)가 현재 request 기준으로 member를 다시 해석한 결과입니다.

### 7-2. `MyPageController`는 조합만 한다

[MyPageController.java](../src/main/java/com/worldmap/web/MyPageController.java)는 아래만 합니다.

1. model에서 `currentMember`를 읽음
2. 있으면 `myPageService.loadDashboard(memberId)`
3. 없으면 dashboard를 넣지 않음
4. 템플릿 이름 `mypage` 반환

즉 controller는 read model assembly를 직접 하지 않습니다.

### 7-3. 왜 이 구조가 좋은가

이렇게 하면 `/mypage` controller는 "누가 로그인했는지"를 다시 푸는 책임에서 벗어납니다.

- auth 해석은 `CurrentMemberAccessService`
- 공통 model 주입은 `SiteHeaderModelAdvice`
- read model 조합은 `MyPageService`

SSR에서도 계층 경계가 분명합니다.

## 8. `MyPageService`가 실제로 만드는 read model

[MyPageService.java](../src/main/java/com/worldmap/mypage/application/MyPageService.java)의 `loadDashboard(memberId)`는 단순 repository pass-through가 아닙니다.

한 member의 기록 허브를 만들기 위해 여러 source를 합칩니다.

### 8-1. 먼저 member 존재를 확인한다

가장 먼저 [MemberRepository.java](../src/main/java/com/worldmap/auth/domain/MemberRepository.java)에서 member를 다시 읽습니다.

없으면 `ResourceNotFoundException`을 던집니다.

즉 `/mypage` read model도 member id만 믿고 바로 조합하지 않습니다.

### 8-2. 총 완료 플레이 수는 leaderboard 기준이다

```text
leaderboardRecordRepository.countByMemberId(memberId)
```

이 값은 "run이 실제로 끝나 leaderboard row로 남은 횟수"를 뜻합니다.

즉 단순 session 시작 횟수나 stage 시도 횟수가 아닙니다.

### 8-3. 최근 플레이도 leaderboard 기준이다

```text
findByMemberIdOrderByFinishedAtDesc(memberId, PageRequest.of(0, 10))
```

즉 최근 플레이는 "최근 완료된 run 10개"입니다.
진행 중 세션이나 중간 stage는 여기 들어오지 않습니다.

### 8-4. best run도 game mode별 leaderboard 기준이다

각 게임 mode마다 아래 조회를 합니다.

```text
findFirstByMemberIdAndGameModeOrderByRankingScoreDescFinishedAtAsc(...)
```

즉 best run은 아래 기준입니다.

- 먼저 `rankingScore`가 높은 run
- 동점이면 더 빨리 끝난 run

### 8-5. performance summary는 leaderboard가 아니라 stage raw data를 읽는다

이 부분이 중요합니다.

`MyPageService`는 성향 요약을 만들 때 leaderboard row만 쓰지 않습니다.

게임별로 아래를 계산합니다.

- 완료된 run 수
- clear된 stage 수
- 1트 클리어율
- 평균 시도 수

이걸 위해 각 게임의 stage repository에서 `status = CLEARED`인 stage를 읽어 `attemptCount`를 모읍니다.

즉 성향 요약은 "run 결과 요약"이 아니라 **stage-level raw performance aggregation**입니다.

### 8-6. 다섯 게임을 모두 같은 축으로 정리한다

현재 `MyPageService`는 아래 순서로 mode를 고정합니다.

1. 위치
2. 수도
3. 국기
4. 인구 비교 배틀
5. 인구수

best run과 performance summary 모두 이 순서를 따릅니다.

즉 `/mypage`는 화면 카드 순서도 서비스에서 고정합니다.

## 9. `/mypage` view 객체를 왜 여러 개로 나눴는가

거대한 DTO 하나로도 만들 수는 있습니다.

하지만 현재는 아래처럼 쪼개 둡니다.

- [MyPageDashboardView.java](../src/main/java/com/worldmap/mypage/application/MyPageDashboardView.java)
- [MyPageBestRunView.java](../src/main/java/com/worldmap/mypage/application/MyPageBestRunView.java)
- [MyPageModePerformanceView.java](../src/main/java/com/worldmap/mypage/application/MyPageModePerformanceView.java)
- [MyPageRecentPlayView.java](../src/main/java/com/worldmap/mypage/application/MyPageRecentPlayView.java)

이렇게 한 이유는 세 가지입니다.

### 9-1. 템플릿 블록 책임이 분리된다

- 상단 요약
- best run 카드
- performance 카드
- recent play 리스트

각 블록이 어떤 데이터만 보는지 바로 보입니다.

### 9-2. 테스트가 쉬워진다

예를 들어 performance summary가 잘못 나오면 `MyPageModePerformanceView` 쪽만 보면 됩니다.

### 9-3. 나중에 일부 블록만 바꾸기 쉽다

예를 들어 최근 플레이에 새 필드를 추가해도 best run 구조를 건드리지 않아도 됩니다.

## 10. `currentRank`는 어떻게 계산되나

이건 `/mypage` 설명에서 가장 자주 헷갈리는 지점입니다.

### 10-1. `rankFor(...)`의 실제 동작

[MyPageService.java](../src/main/java/com/worldmap/mypage/application/MyPageService.java)의 `rankFor(...)`는 아래를 합니다.

1. 특정 game mode의 모든 leaderboard row를 현재 정렬 순서로 읽음
2. target record id를 찾아 index + 1 반환

즉 `currentRank`는 **현재 전체 보드를 다시 본 결과**입니다.

### 10-2. 왜 historical rank가 아닌가

현재 `LeaderboardRecord`에는 "기록 당시 몇 위였는가"라는 필드가 없습니다.

저장된 것은 아래뿐입니다.

- total score
- ranking score
- finishedAt
- game mode

그래서 historical snapshot rank는 현재 모델의 책임이 아닙니다.

### 10-3. UI 문구가 어떻게 맞춰져 있나

[mypage.html](../src/main/resources/templates/mypage.html)는 아래처럼 씁니다.

- `현재 #${bestRun.currentRank}`
- `현재 #${play.currentRank}`
- rank가 null이면 `현재 순위 계산 중`

즉 템플릿 문구도 current rank semantics에 맞춰져 있습니다.

### 10-4. 왜 이 점을 블로그에서 명시해야 하나

그렇지 않으면 면접에서 바로 꼬입니다.

예를 들어 "best run 카드의 rank는 기록 당시 순위인가요?"라는 질문에 현재 코드와 다른 답을 하게 됩니다.

현재 정확한 답은 아래입니다.

> 아니요. 현재 보드를 다시 계산한 현재 순위입니다.

## 11. `ServiceActivityService`가 실제로 만드는 public metric

[ServiceActivityService.java](../src/main/java/com/worldmap/stats/application/ServiceActivityService.java)의 `loadTodayActivity()`는 오늘 하루의 활동량을 계산합니다.

### 11-1. 기준 시간 범위

기준은 아래입니다.

- `todayStart = LocalDate.now().atStartOfDay()`
- `tomorrowStart = todayStart.plusDays(1)`

즉 오늘 00:00부터 내일 00:00 전까지를 집계합니다.

### 11-2. active member 수는 session start 기준 distinct memberId다

다섯 게임 repository에서 오늘 시작된 session의 distinct member id를 모아 set으로 합칩니다.

즉 "오늘 활성 member"는 leaderboard completion 기준이 아니라 **게임 시작 기준**입니다.

### 11-3. active guest 수는 session start 기준 distinct guestSessionKey다

guest도 마찬가지로 오늘 시작된 session의 distinct guest key를 모아 set으로 합칩니다.

즉 public activity는 member와 guest를 모두 플레이어로 셉니다.

### 11-4. started session 수는 단순 합계다

다섯 게임 session repository의 `countByStartedAtGreaterThanEqualAndStartedAtLessThan(...)`를 전부 더합니다.

즉 "오늘 시작된 세션 수"는 distinct player 수와 다릅니다.

- 한 사람이 여러 판을 시작하면 세션 수는 늘어난다
- active player 수는 distinct owner 기준으로 유지된다

### 11-5. completed run 수는 leaderboard 기준이다

오늘 완료 run 수는 `leaderboard_record.finishedAt` 기준으로 계산합니다.

즉 시작은 session, 완료는 leaderboard라는 서로 다른 원본을 씁니다.

### 11-6. 게임별 완료 수도 leaderboard 기준이다

`todayLocationCompletedRunCount`, `todayCapitalCompletedRunCount` 등은 모두 game mode별 leaderboard count입니다.

즉 `/stats`는 "오늘 시작된 세션"과 "오늘 완료된 run"을 의도적으로 분리해서 보여 줍니다.

## 12. `/stats`는 leaderboard read model을 어떻게 조합하나

[StatsPageController.java](../src/main/java/com/worldmap/stats/web/StatsPageController.java)는 두 종류의 read model을 합칩니다.

1. `ServiceActivityView`
2. game mode별 `LeaderboardView`

### 12-1. activity는 one-shot aggregate다

`activity` attribute는 오늘의 활동량 요약 하나입니다.

### 12-2. Top 3는 mode별 `LeaderboardService.getLeaderboard(...)`다

controller는 아래를 mode별로 호출합니다.

```text
leaderboardService.getLeaderboard(gameMode, DAILY, 3)
```

즉 `/stats`는 랭킹 API 로직을 새로 만들지 않고, 공통 leaderboard read path를 재사용합니다.

### 12-3. 왜 `/stats` controller가 aggregation을 직접 하지 않나

controller가 직접 repository를 여러 개 읽기 시작하면 아래가 무너집니다.

- `/dashboard`와 공통 source를 공유하기 어렵다
- fallback 정책과 read contract가 흩어진다
- 테스트가 controller 중심으로 비대해진다

그래서 activity는 `ServiceActivityService`, top board는 `LeaderboardService`에 둡니다.

## 13. `/stats`와 `/dashboard`는 무엇이 다른가

이 차이를 명확히 써야 public/private 경계가 보입니다.

### 13-1. 공통점

둘 다 아래 source를 공유합니다.

- `ServiceActivityView`
- leaderboard 기반 completed run 수
- 오늘 활동량 집계

### 13-2. 차이점

`/stats`는 아래만 보여 줍니다.

- public-safe today metrics
- 게임별 완료 수
- public top records

`/dashboard`는 여기에 더해 아래를 봅니다.

- recommendation feedback response 수
- survey/engine version
- admin route 카드
- 운영 focus item

즉 `/stats`는 제품 소개면, `/dashboard`는 운영 제어판입니다.

### 13-3. 왜 둘을 하나로 합치지 않았나

합치면 아래 문제가 생깁니다.

- public 페이지에 운영 정보가 섞인다
- 로그인/권한 제어 로직이 모호해진다
- "누가 무엇을 봐야 하는가" 설명이 어려워진다

그래서 source는 공유하되 surface를 나눴습니다.

## 14. 템플릿은 각 read model을 어떻게 해석하나

### 14-1. `mypage.html`

[mypage.html](../src/main/resources/templates/mypage.html)는 guest와 member UI를 강하게 나눕니다.

#### member 상태

- hero에 현재 nickname과 계정 연결 상태
- dashboard summary 카드
- performance 카드
- recent play
- 바로가기와 logout 버튼

#### guest 상태

- "기록을 남기려면 로그인" hero
- account 연결 안내
- signup/login CTA

즉 템플릿도 read model을 그냥 렌더링하지 않고, 현재 인증 상태에 따라 아예 다른 surface를 보여 줍니다.

### 14-2. `stats/index.html`

[stats/index.html](../src/main/resources/templates/stats/index.html)는 공개 기준을 분명히 적습니다.

- 오늘 활동
- 게임별 완료 수
- 아케이드 상위 기록
- 퀵 퀴즈 상위 기록
- "운영 판단용 내부 지표는 Dashboard에서만 봅니다"

즉 템플릿 copy 자체가 public/private 경계를 문서처럼 설명합니다.

## 15. 실패 케이스와 예외 처리

### 15-1. 비로그인 `/mypage`

현재 `/mypage`는 redirect하지 않습니다.
guest prompt를 보여 주는 public shell로 남깁니다.

이 점이 중요합니다.

- 로그인 강제가 아니라 기록 연결 유도
- 제품 첫 진입을 막지 않음

### 15-2. member는 있지만 완료 run이 거의 없는 경우

`bestRuns`나 `modePerformances`, `recentPlays`가 비어 있을 수 있습니다.

[mypage.html](../src/main/resources/templates/mypage.html)는 이 경우 메시지 박스를 보여 줍니다.

즉 empty state도 read model 계약의 일부입니다.

### 15-3. public stats에 개인 정보가 섞이는 경우

이건 설계 실패입니다.

현재 `/stats`는 아래만 공개합니다.

- aggregate count
- player nickname이 보이는 top board

하지만 개인의 플레이 히스토리, member id, feedback summary 같은 것은 공개하지 않습니다.

### 15-4. current rank를 historical rank처럼 설명하는 경우

현재 코드와 맞지 않습니다.

반드시 "현재 보드 기준 순위"라고 설명해야 합니다.

### 15-5. Redis가 비어 있거나 읽기 실패하는 경우

`/stats`의 top board는 [LeaderboardService.java](../src/main/java/com/worldmap/ranking/application/LeaderboardService.java)의 공통 fallback 경로를 따릅니다.

즉 Redis read가 실패해도 DB top record로 fallback할 수 있습니다.

이건 stats controller가 아니라 leaderboard service 계약입니다.

## 16. 테스트로 검증하기

### 16-1. `MyPageServiceIntegrationTest`

[MyPageServiceIntegrationTest.java](../src/test/java/com/worldmap/mypage/MyPageServiceIntegrationTest.java)는 개인 read model의 핵심을 보여 줍니다.

#### `loadDashboardIncludesRawStagePerformanceByMode`

막는 리스크:

- performance summary가 leaderboard row만 보고 잘못 계산되는 문제

검증 내용:

- location과 population completed run 수
- cleared stage 수
- 1트 클리어율
- 평균 시도 수

즉 stage raw data aggregation이 맞는지 본다.

#### `loadDashboardIncludesAllFiveGameModes`

막는 리스크:

- 신규 게임 3종이 `/mypage`에서 빠지는 문제

검증 내용:

- totalCompletedRuns가 5
- bestRuns와 modePerformances가 다섯 게임 모두 포함
- recentPlays도 다섯 게임 label을 모두 포함
- 각 recent play의 `currentRank`가 null이 아님

### 16-2. `MyPageControllerTest`

[MyPageControllerTest.java](../src/test/java/com/worldmap/web/MyPageControllerTest.java)는 SSR shell 관점에서 `/mypage`를 검증합니다.

#### `myPageShowsGuestPromptWhenNotLoggedIn`

막는 리스크:

- 비로그인 사용자가 빈 페이지나 500을 보는 문제

검증 내용:

- guest prompt copy
- signup/login CTA
- dashboard link 비노출

#### `myPageShowsConnectedMemberStateWhenLoggedIn`

막는 리스크:

- current member 연결 상태와 dashboard block이 제대로 렌더링되지 않는 문제

검증 내용:

- nickname
- best run
- performance
- recent play
- logout

### 16-3. `StatsPageControllerTest`

[StatsPageControllerTest.java](../src/test/java/com/worldmap/stats/StatsPageControllerTest.java)는 public stats surface를 검증합니다.

#### `statsPageRendersPublicMetricsWithoutDashboardLinkForGuest`

막는 리스크:

- guest가 stats를 못 보거나 dashboard/admin 정보가 섞이는 문제

검증 내용:

- activity render
- five-mode daily top render
- dashboard link 비노출

#### `statsPageShowsDashboardLinkForAdminSession`

막는 리스크:

- admin current member가 있을 때 공통 shell의 dashboard link가 사라지는 문제

검증 내용:

- admin session이면 stats 페이지에서도 header의 Dashboard 링크가 보임

즉 이 테스트 묶음은 `member read model`, `public stats surface`, `SSR shell` 기준을 강하게 고정합니다.
다만 실제 production 데이터량에서의 응답 시간, 랭킹 fallback 성능, historical rank snapshot 같은 읽기 모델의 장기 운영 요구사항까지 자동 증명하는 것은 아닙니다.

## 17. 이 글만 보고 다시 구현하는 순서

### 17-1. 1단계: public/private surface를 먼저 나눈다

먼저 화면 목적을 고정합니다.

- `/mypage`: member-centric
- `/stats`: public-centric
- `/dashboard`: ops-centric

### 17-2. 2단계: current member를 공통 model advice로 주입한다

controller마다 session을 직접 읽지 말고 [SiteHeaderModelAdvice.java](../src/main/java/com/worldmap/web/SiteHeaderModelAdvice.java) 같은 공통 진입점을 둡니다.

### 17-3. 3단계: `/mypage` read model을 설계한다

최소 아래 블록으로 나눕니다.

- top summary
- best runs
- mode performance
- recent plays

그리고 giant DTO 하나 대신 목적별 view record로 분리합니다.

### 17-4. 4단계: best run과 recent play는 leaderboard 기준으로 잡는다

완료 run과 ranking score가 기준이 되게 합니다.

### 17-5. 5단계: performance summary는 stage raw data에서 계산한다

1트 클리어율과 평균 시도 수는 leaderboard만으로 충분하지 않으므로 stage repository를 읽게 합니다.

### 17-6. 6단계: `/stats`는 public aggregate와 top board만 보여 준다

activity aggregate는 별도 service, top board는 공통 leaderboard service를 재사용합니다.

### 17-7. 7단계: `/dashboard`는 같은 source 위에 admin-only 정보를 얹는다

source 공유와 surface 분리를 동시에 설명할 수 있게 합니다.

## 18. 구현 체크리스트

- `/mypage`는 current member가 있을 때만 dashboard read model을 채운다
- `/mypage`는 guest에게 redirect가 아니라 guest prompt를 보여 준다
- `MyPageDashboardView`와 하위 view record가 있다
- best run은 mode별 leaderboard 기준이다
- recent play는 최근 완료 run 10개다
- performance summary는 stage cleared raw data에서 계산한다
- `/stats`는 `ServiceActivityView`와 mode별 `LeaderboardView`를 조합한다
- `/stats`는 public-safe metrics만 노출한다
- `/dashboard`는 같은 activity source를 재사용하지만 ops-only 정보를 별도로 붙인다
- `currentRank`는 현재 보드 기준이라고 문구/구현이 일치한다

## 19. 실행 / 검증 명령

이 글의 핵심 흐름은 아래 테스트 묶음으로 확인할 수 있습니다.

```bash
./gradlew test \
  --tests com.worldmap.mypage.MyPageServiceIntegrationTest \
  --tests com.worldmap.web.MyPageControllerTest \
  --tests com.worldmap.stats.StatsPageControllerTest
```

## 20. 산출물 체크리스트

이 단계가 끝났다면 아래를 설명할 수 있어야 합니다.

- 왜 `/mypage`와 `/stats`는 같은 source를 읽어도 다른 read model이 필요한가
- 왜 `/mypage`는 leaderboard row만으로는 충분하지 않은가
- 왜 performance summary는 stage raw data를 읽어야 하는가
- 왜 `/stats`는 `/dashboard`의 public 버전이 아닌가
- 왜 `currentRank`는 historical rank가 아니라 current board rank인가

## 21. 현재 구현의 한계

### 21-1. `/mypage`의 rank는 historical snapshot이 아니다

이건 limitation이자 현재 의도입니다.

나중에 "기록 당시 몇 위였는가"를 보여 주려면 별도 field나 snapshot 전략이 필요합니다.

### 21-2. `/stats`는 운영용 상세도를 intentionally 숨긴다

현재는 아래를 public에 노출하지 않습니다.

- recommendation feedback summary
- persona baseline
- 운영 판단 메모
- admin route 링크

### 21-3. `/mypage`와 `/stats`는 현재 read model shape를 고정할 뿐 analytics warehouse는 아니다

현재 테스트와 구현은 사용자 기록 허브와 public activity surface를 설명하기에는 충분하지만,
기간 비교, cohort, historical snapshot 같은 분석용 읽기 모델까지 제공하는 단계는 아니다.

### 21-3. deep history drill-down은 아직 없다

현재 `/mypage`는 최근 10개 run과 요약 중심입니다.

stage 단위 replay나 상세 타임라인은 아직 없습니다.

### 21-4. activity 집계는 "오늘" 기준 one-shot aggregate다

장기 추세나 주간/월간 분석은 아직 범위 밖입니다.

## 22. 자주 막히는 지점

### 22-1. leaderboard row 하나로 모든 화면이 해결된다고 생각하는 것

개인 성향 요약은 stage raw data가 필요합니다.

### 22-2. `/mypage`와 `/stats`를 같은 DTO로 묶는 것

권한 경계와 surface 목적이 흐려집니다.

### 22-3. `currentRank`를 historical rank처럼 설명하는 것

현재 코드와 다릅니다.

### 22-4. `/stats`와 `/dashboard`를 같은 화면으로 생각하는 것

source는 공유해도 surface와 공개 범위가 다릅니다.

### 22-5. controller가 직접 요약 계산을 하게 만드는 것

그러면 서비스 경계와 테스트 포인트가 무너집니다.

## 23. 이 글을 30초로 설명하면

WorldMap은 같은 게임 결과라도 읽는 사람이 다르다고 보고 `/mypage`와 `/stats`를 별도 read model로 분리했습니다. `/mypage`는 `MyPageService`가 leaderboard row와 stage raw data를 함께 읽어 best run, 최근 플레이, 1트 클리어율, 평균 시도 수를 member 기준으로 조합하고, `/stats`는 `ServiceActivityService`와 `LeaderboardService`가 오늘의 공개 활동량과 daily Top 3만 보여 줍니다. 또 `/dashboard`는 같은 `ServiceActivityView`를 재사용해도 운영용 추천 지표와 route를 더 얹는 별도 surface라서, source 공유와 surface 분리를 동시에 설명할 수 있습니다.

## 24. 면접에서 바로 받을 수 있는 꼬리 질문

### 24-1. 왜 `/mypage`와 `/stats`를 한 페이지로 합치지 않았나요?

읽는 사람과 공개 범위가 다르기 때문입니다. `/mypage`는 개인 기록 허브이고, `/stats`는 비로그인도 보는 public surface입니다.

### 24-2. 왜 performance summary는 leaderboard가 아니라 stage를 읽나요?

1트 클리어율과 평균 시도 수는 run 결과만으로는 충분하지 않고, clear된 stage의 `attemptCount`가 필요하기 때문입니다.

### 24-3. `/stats`와 `/dashboard`는 뭐가 다른가요?

둘 다 `ServiceActivityView`를 쓸 수 있지만 `/stats`는 public-safe metrics와 top board만 보여 주고, `/dashboard`는 recommendation 운영 지표와 admin route까지 붙이는 ops surface입니다.

### 24-4. `/mypage`의 순위는 언제 기준인가요?

현재 구현은 기록 당시 순위가 아니라 현재 보드를 다시 정렬한 current rank입니다.

## 25. 다음 글과의 연결

이 글은 member/private/public read model 분리를 다룹니다.
다음 단계에서는 운영 화면 자체를 어떤 카드와 판단 흐름으로 구성했는지를 봐야 합니다.

- 이전 글: [12-simple-auth-member-session-and-admin-entry.md](./12-simple-auth-member-session-and-admin-entry.md)
- 다음 글: [14-dashboard-admin-surface-and-operations-cards.md](./14-dashboard-admin-surface-and-operations-cards.md)
- 권한/재검증 후속 글: [17-game-integrity-current-member-and-role-revalidation.md](./17-game-integrity-current-member-and-role-revalidation.md)
