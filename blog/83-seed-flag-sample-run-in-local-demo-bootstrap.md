# local demo bootstrap에 국기 퀴즈 sample run을 넣기

## 왜 이 조각이 필요한가

국기 게임 vertical slice는 이미 public 제품에 열려 있었다.

하지만 local demo 기준으로는 한 가지 어색한 점이 남아 있었다.

- `/games/flag/start`로 직접 들어가면 게임은 된다.
- 그런데 `/stats`, `/ranking`에서는 flag 보드가 비어 있다.

즉, 새 게임을 만들었는데도 서버를 막 띄운 직후에는
“이 모드가 실제로 돌아가고 있다”는 신호가 약했다.

이번 조각의 목적은 이 틈을 메우는 것이다.

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
- 국기 퀴즈 완료 run 1개
- guest 진행 중 위치 게임 세션 1개
- 현재 추천 버전 만족도 sample 5개

즉, `orbit_runner`는 이제 위치 / 인구수 / 국기 기록을 가진 demo user가 된다.

## 요청 흐름이 아니라 startup 흐름이다

이번 조각은 public HTTP 요청이 아니라 startup bootstrap 흐름이다.

순서는 그대로 이렇다.

1. `CountrySeedInitializer`
2. `AdminBootstrapInitializer`
3. `RecommendationFeedbackLegacyColumnInitializer`
4. `GameLevelRollbackInitializer`
5. `DemoBootstrapInitializer`

여기서 [DemoBootstrapService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/demo/application/DemoBootstrapService.java)가 마지막 단계에서 demo user와 sample run을 채운다.

즉, 이건 컨트롤러나 페이지 변경이 아니라
“local에서 어떤 설명 가능한 시작 상태를 재현할 것인가”를 정하는 작업이다.

## 왜 service에 둬야 하나

가장 중요한 이유는 sample 데이터도 current source of truth를 따라야 하기 때문이다.

국기 게임 sample run은 그냥 임의 나라 이름을 꽂아 넣지 않았다.

대신 [FlagQuestionCountryPoolService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolService.java)가 보장한

`country seed ∩ flag manifest ∩ 실제 파일 존재`

교집합에서만 국가를 골라 만들었다.

그래야 local demo 데이터도 실제 서비스 규칙과 같은 설명을 공유할 수 있다.

즉:

- public 게임은 `FlagGameService`
- local demo seed는 `DemoBootstrapService`

가 맡지만, 둘 다 같은 출제 가능 국가 pool read model을 기준으로 움직인다.

## 지금 어떤 sample run이 들어가나

이번 조각에서 추가한 run signature는 아래다.

- `demo:flag:orbit_runner:1`

이 run은 현재 기준으로

- 총점 `285`
- 클리어 Stage `2`
- 총 시도 수 `4`

상태를 가진다.

그래서 local에서 서버를 띄우자마자 `/ranking`, `/stats`에서 flag 보드가 비어 있지 않다.

## 왜 국기만 먼저 sample run을 넣었나

이번엔 범위를 작게 잡았다.

이유는 flag game이 막 public 제품에 추가된 상태였기 때문이다.

즉 우선순위는

- 막 추가한 새 모드를 public 보드에서 바로 보이게 만들기

였고, 그 기준에서는 flag sample run 하나가 가장 작은 조각이었다.

수도 맞히기와 인구 비교 퀵 배틀도 나중에 같은 이유로 넣을 수 있지만,
이번 턴에서는 그 둘까지 같이 묶지 않았다.

## 테스트

[DemoBootstrapIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/demo/DemoBootstrapIntegrationTest.java)에서 아래를 고정했다.

- `worldmap_admin` 계정 존재
- `orbit_runner` 계정 존재
- 위치 / 인구수 / 국기 sample run signature 존재
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
- 위치 / 인구수 / 국기 sample run이 있어서 public `/stats`, `/ranking`에서 새 모드 상태를 바로 확인할 수 있다.

즉, 새 게임을 만들었을 때
“코드는 있는데 시연 상태는 비어 있음” 문제를 줄인 것이다.

## 다음 단계

다음 후보는 둘 중 하나다.

- `capital`, `population-battle` sample run도 local demo에 추가하기
- 국기 자산 pool 12개를 더 넓히기

지금은 국기 게임이 막 열린 상태였기 때문에,
먼저 local demo 설명 가능성을 맞추는 쪽을 택했다.

## 면접에서 이렇게 설명할 수 있다

> 국기 게임을 public 제품에 붙인 뒤에도 local demo에서는 보드가 비어 있어서, 새 모드가 실제로 돌아간다는 신호가 약했습니다. 그래서 startup `DemoBootstrapService`에 `FLAG` sample run 하나를 더 넣고, 그 데이터도 임의 문자열이 아니라 `FlagQuestionCountryPoolService`가 보장한 출제 가능 국가 subset을 기준으로 만들었습니다. 덕분에 서버를 local profile로 다시 띄우기만 해도 `/stats`와 `/ranking`에서 국기 게임이 실제로 돌고 있다는 걸 바로 보여 줄 수 있게 됐습니다.
