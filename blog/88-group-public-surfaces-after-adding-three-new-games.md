# 신규 게임 3종이 들어온 뒤 public 홈, 랭킹, Stats를 다시 묶기

## 왜 이 조각이 필요한가

수도 맞히기, 인구 비교 퀵 배틀, 국기 퀴즈까지 열고 나니
public 제품이 다시 길어지기 시작했다.

기능은 늘었지만, 플레이어가 첫 화면에서 바로 읽어야 하는 정보도 같이 늘어난 상태였다.

특히 아래 세 표면이 다시 복잡해졌다.

- 홈의 모드 카드
- `/ranking`의 게임 전환 필터
- `/stats`의 서비스 지표와 Top 보드

이번 조각의 목적은 게임을 더 만드는 게 아니라,
이미 늘어난 게임 수를 플레이어가 더 빨리 읽을 수 있게 public surface를 재배치하는 것이다.

## 이번에 바뀐 파일

### 홈 read model과 템플릿

- [ModeCardView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/web/view/ModeCardView.java)
- [HomeController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/web/HomeController.java)
- [home.html](/Users/alex/project/worldmap/src/main/resources/templates/home.html)

### 공개 랭킹과 Stats

- [index.html](/Users/alex/project/worldmap/src/main/resources/templates/ranking/index.html)
- [index.html](/Users/alex/project/worldmap/src/main/resources/templates/stats/index.html)
- [site.css](/Users/alex/project/worldmap/src/main/resources/static/css/site.css)

### 테스트

- [HomeControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/web/HomeControllerTest.java)
- [StatsPageControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/stats/StatsPageControllerTest.java)
- [LeaderboardIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java)

## 홈은 어떻게 다시 묶었나

핵심은 [ModeCardView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/web/view/ModeCardView.java)에
`group` 필드를 추가한 것이다.

이제 홈 카드는 단순히 나열되지 않고,
[HomeController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/web/HomeController.java)가
아래 기준으로 그룹을 먼저 정한다.

- `arcade`
  - 위치 찾기
  - 인구 비교 퀵 배틀
  - 인구수 맞추기
- `quiz`
  - 수도 맞히기
  - 국기 퀴즈
- `discover`
  - 나라 추천

[home.html](/Users/alex/project/worldmap/src/main/resources/templates/home.html)은
이 grouping을 그대로 읽어

- `아케이드 러너`
- `퀵 퀴즈와 추천`

두 구역으로 모드를 보여 준다.

즉, 홈은 이제 “게임 6개를 한 줄로 보여 주는 화면”이 아니라,
“빠른 반복 플레이”와 “짧은 퀴즈/탐색”을 먼저 구분해 주는 진입 화면이 됐다.

## 왜 group 필드를 템플릿이 아니라 read model에 두었나

이 분류는 단순 CSS 배치가 아니라
public 제품이 모드를 어떤 언어로 소개할 것인가에 대한 표현 규칙이다.

따라서 템플릿에서 카드 이름을 보고 임시 분기하는 것보다,
[ModeCardView.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/web/view/ModeCardView.java)처럼
read model에 grouping을 올리는 편이 더 설명 가능하다.

컨트롤러는 여전히 상태를 바꾸지 않는다.

`GET / -> HomeController -> home.html`

흐름은 그대로고,
이번에는 그 read model이 “어떤 카드가 어떤 성격의 모드인가”를 함께 전달할 뿐이다.

## `/ranking`은 왜 버튼 라벨을 줄였나

랭킹은 다섯 게임을 모두 보여 줘야 하지만,
긴 버튼 라벨은 필터 행을 다시 무겁게 만들었다.

그래서 [index.html](/Users/alex/project/worldmap/src/main/resources/templates/ranking/index.html)에서
게임 필터를 아래처럼 줄였다.

- `위치`
- `수도`
- `국기`
- `배틀`
- `인구`

중요한 건 정렬 규칙은 하나도 안 바뀌었다는 점이다.

여전히

- 요청은 `GET /ranking`
- polling은 `GET /api/rankings/{gameMode}`
- 정렬과 DB fallback, Redis key 선택은 [LeaderboardService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/application/LeaderboardService.java)

가 맡는다.

즉, 이번 조각은 “랭킹 로직 변경”이 아니라
“public 전환 밀도 조정”이다.

## `/stats`는 무엇을 분리했나

이전에는 오늘의 활동 숫자와 게임별 완료 수, Top 보드가 한 흐름으로 길게 이어져 있었다.

이번에는 [index.html](/Users/alex/project/worldmap/src/main/resources/templates/stats/index.html)에서
그걸 세 덩어리로 나눴다.

1. `오늘의 활동`
   - registered players
   - today active players
   - today started sessions
   - today completed runs
2. `게임별 완료 수`
   - location / capital / flag / population battle / population
3. `Top 보드`
   - `아케이드 상위 기록`
   - `퀵 퀴즈 상위 기록`

핵심은 같은 숫자를 더 만들지 않았다는 것이다.

여전히 지표는

- [ServiceActivityService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/stats/application/ServiceActivityService.java)
- [LeaderboardService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/application/LeaderboardService.java)

가 만들고,
템플릿이 그 숫자를 어떤 순서로 먼저 읽히게 할지 바꾼 것뿐이다.

## 테스트는 무엇을 고정했나

[HomeControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/web/HomeControllerTest.java)에서는

- `아케이드 러너`
- `퀵 퀴즈와 추천`

구역이 실제로 렌더링되는지 확인했다.

[StatsPageControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/stats/StatsPageControllerTest.java)에서는

- `게임별 완료 수`
- `아케이드 상위 기록`
- `퀵 퀴즈 상위 기록`

구조와 새 게임 3종 라벨이 같이 보이는지 검증했다.

[LeaderboardIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java)에서는
랭킹 SSR 화면이

- `게임 종류`
- `id="ranking-mode-capital"`
- `id="ranking-mode-flag"`
- `id="ranking-mode-population-battle"`

를 실제로 포함하는지 고정했다.

실행:

```bash
./gradlew test --tests com.worldmap.web.HomeControllerTest --tests com.worldmap.stats.StatsPageControllerTest --tests com.worldmap.ranking.LeaderboardIntegrationTest
./gradlew test
git diff --check
```

## 지금 상태를 어떻게 설명하면 되나

지금 public 제품은 게임 수가 늘어도
그걸 그대로 한 줄에 더 얹지 않는다.

대신

- 홈은 플레이 성격별로
- 랭킹은 짧은 필터로
- Stats는 서비스 지표와 게임 하이라이트를 분리해서

읽기 비용을 줄인다.

즉, 이번 조각의 핵심은
“서버 구조는 유지하고 public surface만 다시 읽기 쉽게 만들었다”는 점이다.

## 다음 단계

다음 후보는 둘 중 하나다.

- 국기 게임 세부 난이도를 더 조정할지 결정
- 신규 게임 3종의 public copy를 여기서 멈출지, 더 줄일지 한 번 더 판단

## 면접에서 이렇게 설명할 수 있다

> 신규 게임 3종을 붙인 뒤 public 홈, 랭킹, Stats가 다시 길어졌습니다. 그래서 기능을 더 만들지 않고, 홈 카드는 `아케이드 러너 / 퀵 퀴즈와 추천`으로 묶고, 랭킹은 짧은 버튼 전환으로, Stats는 서비스 지표와 게임별 완료 수, Top 보드를 분리해 재정리했습니다. 핵심은 정렬과 집계 규칙은 그대로 서버가 맡고, public surface의 읽기 비용만 낮췄다는 점입니다.
