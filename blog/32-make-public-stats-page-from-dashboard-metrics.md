# Dashboard 숫자 중 공개 가능한 것만 `/stats`로 분리하기

## 왜 이 글을 쓰는가

운영 화면이 생기면 이런 고민이 바로 나온다.

- 운영 수치를 일반 사용자도 보면 좋지 않을까?
- 그렇다고 `/dashboard`를 모두에게 열어도 될까?

이번 조각에서는 이 두 질문을 분리해서 풀었다.

- 일반 사용자에게는 공개 가능한 활동 수치만 보여 주는 `/stats`
- 운영자에게는 내부 품질 신호까지 보는 `/dashboard`

즉, 같은 숫자를 두 번 구현한 것이 아니라 `공개 read model`과 `운영 read model`의 경계를 만든 작업이다.

## 이번에 바뀐 핵심

- `AdminDashboardService`에 있던 활동 수치 계산을 `ServiceActivityService`로 분리했다.
- `/stats` 페이지를 새로 만들었다.
- public 헤더에 `Stats` 링크를 추가했다.
- `/stats`는 총 가입자 수, 오늘 활성 플레이어 수, 오늘 시작된 세션 수, 오늘 완료된 run 수, 오늘 위치/인구수 Top 3만 보여 준다.
- 추천 만족도, 설문 버전, persona baseline은 여전히 `/dashboard`에만 남긴다.

## 왜 `/dashboard`를 그대로 공개하지 않았는가

운영 화면에는 사용자에게 필요 없는 정보가 많다.

- surveyVersion
- engineVersion
- 추천 만족도 분포
- persona weak scenario

이 정보는 운영자가 설문을 개선할 때는 중요하지만, 일반 플레이어에게는 오히려 제품보다 내부 구현이 먼저 보이게 만든다.

그래서 `/stats`는 "서비스가 살아 있다"는 신호만 주는 페이지로 따로 뺐다.

## 어떤 파일이 바뀌었는가

- `src/main/java/com/worldmap/stats/application/ServiceActivityService.java`
- `src/main/java/com/worldmap/stats/application/ServiceActivityView.java`
- `src/main/java/com/worldmap/stats/web/StatsPageController.java`
- `src/main/resources/templates/stats/index.html`
- `src/main/resources/templates/fragments/site-header.html`
- `src/main/java/com/worldmap/admin/application/AdminDashboardService.java`
- `src/main/java/com/worldmap/admin/application/AdminDashboardView.java`
- `src/test/java/com/worldmap/stats/StatsPageControllerTest.java`

## 요청 흐름

```text
GET /stats
-> StatsPageController
-> ServiceActivityService.loadTodayActivity()
-> LeaderboardService.getLeaderboard(LOCATION, DAILY, 3)
-> LeaderboardService.getLeaderboard(POPULATION, DAILY, 3)
-> stats/index.html
```

## 왜 계산 로직을 컨트롤러에 두지 않았는가

`/stats`와 `/dashboard`가 같은 기준의 숫자를 써야 하기 때문이다.

만약 컨트롤러마다 각각 계산하면 아래 문제가 생긴다.

- 공개 화면과 운영 화면 숫자가 서로 달라질 수 있다.
- "오늘 활성 플레이어" 정의가 두 군데로 갈라진다.
- 테스트도 화면 단위로 중복된다.

그래서 활동 수치는 `ServiceActivityService`가 책임지고, 컨트롤러는 어떤 화면에 실을지만 결정하게 했다.

## 숫자는 어디서 읽는가

한 저장소에서 다 읽지 않는다.

- 총 가입자 수: `member_account`
- 오늘 활성 플레이어: 위치/인구수 게임 세션의 `startedAt`
- 오늘 시작된 세션 수: 위치/인구수 게임 세션 count
- 오늘 완료된 run 수: `leaderboard_record.finishedAt`
- 오늘 Top 3: Redis + `leaderboard_record`를 읽는 기존 `LeaderboardService`

즉, "가입", "플레이 시작", "플레이 완료"는 서로 다른 이벤트라 source of truth도 다르게 둔다.

## 테스트는 무엇을 했는가

- `StatsPageControllerTest`
  - guest가 `/stats`에 들어오면 `Stats`는 보이지만 `Dashboard`는 안 보이는지 확인
  - admin 세션이면 `Dashboard` 버튼이 같이 보이는지 확인
  - 공개 수치와 Top 3가 렌더링되는지 확인

## 회고

이번 작업은 숫자를 더 붙인 작업처럼 보이지만, 실제로는 정보 공개 범위를 설계한 작업이다.

- 모든 운영 화면을 공개하지 않는다.
- 그렇다고 서비스 활력 신호를 숨기지도 않는다.
- public과 admin이 같은 숫자를 공유하도록 read model을 먼저 공통화한다.

이 구분이 있어야 나중에 dashboard를 더 키워도 public 화면과 역할이 섞이지 않는다.

## 면접에서는 이렇게 설명할 수 있다

운영 Dashboard를 모두에게 공개하지 않고, 공개 가능한 활동 수치만 모아 `/stats`를 따로 만들었습니다. 핵심은 public과 admin이 서로 다른 화면을 보더라도, 활동 지표 계산 기준은 `ServiceActivityService` 하나에서 공유하게 만든 점입니다. 그래서 가입자 수, 오늘 활성 플레이어 수, 완료 run 수 같은 숫자를 한 번 정의하고 두 화면이 같이 재사용하게 했습니다.
