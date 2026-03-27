# local demo bootstrap에 수도/인구 비교 sample run도 넣기

## 왜 이 조각이 필요한가

국기 게임 sample run을 local demo bootstrap에 넣은 뒤에도
한 가지 어색한 점이 남아 있었다.

- `/stats`, `/ranking`에서 위치 / 인구수 / 국기 보드는 바로 보인다.
- 그런데 수도 맞히기와 인구 비교 퀵 배틀 보드는 첫 플레이 전까지 비어 있다.

즉, 새 게임을 public 제품에 붙였는데도
local demo에서는 “모든 보드가 바로 살아 있는 상태”를 한 번에 보여 주지 못했다.

이번 조각의 목적은 이 마지막 빈칸을 메우는 것이다.

## 이번에 바뀐 파일

- [DemoBootstrapService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/demo/application/DemoBootstrapService.java)
- [DemoBootstrapIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/demo/DemoBootstrapIntegrationTest.java)
- [LOCAL_DEMO_BOOTSTRAP.md](/Users/alex/project/worldmap/docs/LOCAL_DEMO_BOOTSTRAP.md)
- [50-current-state-rebuild-map.md](/Users/alex/project/worldmap/blog/50-current-state-rebuild-map.md)

같이 [README.md](/Users/alex/project/worldmap/README.md), [PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md), [WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)도 현재 기준으로 맞췄다.

## 무엇을 추가했나

local profile에서 서버가 시작되면 이제 아래 sample run들이 같이 들어간다.

- 위치 찾기 완료 run 1개
- 인구수 퀴즈 완료 run 1개
- 수도 맞히기 완료 run 1개
- 인구 비교 퀵 배틀 완료 run 1개
- 국기 퀴즈 완료 run 1개
- guest 진행 중 위치 게임 세션 1개
- 현재 추천 버전 만족도 sample 5개

즉, `orbit_runner`는 이제 다섯 게임의 완료 run을 가진 demo user가 된다.

## 이번에도 startup bootstrap 흐름이다

이 조각은 public HTTP 요청이 아니라 startup bootstrap 흐름이다.

순서는 그대로 이렇다.

1. `CountrySeedInitializer`
2. `AdminBootstrapInitializer`
3. `RecommendationFeedbackLegacyColumnInitializer`
4. `GameLevelRollbackInitializer`
5. `DemoBootstrapInitializer`

여기서 [DemoBootstrapService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/demo/application/DemoBootstrapService.java)가 마지막 단계에서 demo user와 sample run을 채운다.

즉, 이 변경은 페이지 버튼 추가가 아니라
“local에서 어떤 시연 상태를 기본으로 재현할 것인가”를 고정한 것이다.

## 왜 service에 둬야 하나

핵심 이유는 demo 데이터도 실제 게임 규칙을 따라야 하기 때문이다.

이번에 추가한 두 run은 그냥 leaderboard row만 넣지 않았다.

- 수도 맞히기 sample run은 `CapitalGameSession / Stage / Attempt`
- 인구 비교 퀵 배틀 sample run은 `PopulationBattleGameSession / Stage / Attempt`

을 실제 도메인 패턴대로 만들고,
마지막에 [LeaderboardRecord.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java)까지 반영한다.

즉, local demo도 “우회한 fixture”가 아니라
현재 제품이 설명 가능한 상태를 startup service로 재생성하는 구조다.

## 어떤 sample run이 들어가나

이번 조각에서 추가한 run signature는 아래 두 개다.

- `demo:capital:orbit_runner:1`
- `demo:population-battle:orbit_runner:1`

결과는 현재 기준으로 아래처럼 고정된다.

- capital
  - 총점 `285`
  - 클리어 Stage `2`
  - 총 시도 수 `4`
- population-battle
  - 총점 `275`
  - 클리어 Stage `2`
  - 총 시도 수 `4`

그래서 local에서 서버를 띄우자마자
`/ranking`, `/stats`에서 `capital`, `population-battle` 보드도 빈 상태가 아니다.

## 왜 지금 이 두 개를 같이 넣었나

이번엔 범위를 작게 유지하되,
“신규 게임 3종이 모두 local demo에서 바로 보이는가”를 한 번에 닫는 게 더 중요했다.

즉, 이제는

- 수도 맞히기
- 인구 비교 퀵 배틀
- 국기 퀴즈

세 신규 게임이 전부 public 보드에서 seed 직후 바로 확인된다.

## 테스트

[DemoBootstrapIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/demo/DemoBootstrapIntegrationTest.java)에서 아래를 고정했다.

- `worldmap_admin` 계정 존재
- `orbit_runner` 계정 존재
- 위치 / 인구수 / 수도 / 인구 비교 / 국기 sample run signature 존재
- capital / population-battle finished session count 존재
- current recommendation feedback sample 5개 이상
- ops review가 현재 기준으로 유지되는가

실행:

```bash
./gradlew test --tests com.worldmap.demo.DemoBootstrapIntegrationTest --tests com.worldmap.stats.StatsPageControllerTest
./gradlew test
```

## 지금 상태를 어떻게 설명하면 되나

이제 local demo는 이렇게 말할 수 있다.

- user/admin 계정이 자동 생성된다.
- 추천 피드백과 운영 메모가 바로 보인다.
- 위치 / 인구수 / 수도 / 인구 비교 / 국기 sample run이 모두 같이 seed된다.
- 그래서 `/stats`, `/ranking`의 다섯 게임 보드를 서버 재기동 직후 바로 확인할 수 있다.

즉, 새 게임을 여러 개 열어도
demo 환경이 비어 있어 설명이 끊기는 문제를 줄인 것이다.

## 다음 단계

이제 local demo 기준으로는 신규 게임 5종이 모두 보인다.

다음 후보는 이 둘이다.

- 국기 asset pool 12개를 더 넓히기
- 신규 게임 3종의 난이도 / 결과 카피 / 홈 카드 밀도를 다시 다듬기

## 면접에서 이렇게 설명할 수 있다

> 국기 sample run을 넣은 뒤에도 수도 맞히기와 인구 비교 퀵 배틀 보드는 local demo에서 비어 있어서, 신규 게임 5종이 모두 살아 있다는 걸 한 번에 보여 주기 어려웠습니다. 그래서 startup `DemoBootstrapService`에 `CAPITAL`, `POPULATION_BATTLE` sample run도 추가하고, 세션 / Stage / Attempt와 leaderboard row를 실제 게임 규칙과 같은 패턴으로 같이 만들게 했습니다. 덕분에 local profile로 서버를 다시 띄우기만 해도 `/stats`, `/ranking`의 다섯 게임 보드를 바로 시연할 수 있게 됐습니다.
