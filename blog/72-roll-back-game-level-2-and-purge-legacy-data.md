# 공개 Level 2 실험을 롤백하고 legacy 데이터를 정리하기

> 현재 기준 안내:
> public Level 2 롤백 이후, 남아 있던 internal Level 2 호환 코드까지 제거한 최신 상태는 [73-remove-internal-level-2-compatibility-code.md](./73-remove-internal-level-2-compatibility-code.md)를 먼저 보는 것이 안전하다.

## 왜 이 조각이 필요했는가

위치 찾기와 인구수 맞추기 모두 Level 2 실험을 한 번 열어 봤지만, 현재 제품 기준에서는 얻는 가치보다 설명 비용이 더 컸다.

- 시작 화면에서 선택지가 늘어나면서 진입 UX가 복잡해졌다.
- `/ranking`, `/stats`, `/mypage`까지 Level 2 read model이 번져서 public 표면이 무거워졌다.
- 기존 DB와 Redis에는 `LEVEL_2` 세션, 시도, 랭킹 키가 남아 있어 “기능을 숨겼다”만으로는 현재 상태를 깔끔하게 설명하기 어려웠다.

그래서 이번 조각의 목표는 두 가지였다.

1. public 서비스 기준을 다시 `Level 1-only`로 단순화한다.
2. 예전 DB/Redis에 남아 있는 Level 2 흔적도 startup에서 같이 정리한다.

## 이번에 바뀐 범위

핵심 파일은 아래다.

- [GameLevelRollbackInitializer.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)
- [LocationGameApiController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/web/LocationGameApiController.java)
- [PopulationGameApiController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java)
- [LeaderboardPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java)
- [StatsPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/stats/web/StatsPageController.java)
- [MyPageService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/mypage/application/MyPageService.java)
- [ranking/index.html](/Users/alex/project/worldmap/src/main/resources/templates/ranking/index.html)
- [stats/index.html](/Users/alex/project/worldmap/src/main/resources/templates/stats/index.html)
- [mypage.html](/Users/alex/project/worldmap/src/main/resources/templates/mypage.html)
- [location-game/result.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/result.html)
- [population-game/result.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/result.html)
- [location-game/start.html](/Users/alex/project/worldmap/src/main/resources/templates/location-game/start.html)
- [population-game/start.html](/Users/alex/project/worldmap/src/main/resources/templates/population-game/start.html)

## 요청 흐름은 어떻게 바뀌었는가

### 1. 게임 시작은 이제 항상 Level 1로 수렴한다

요청은 그대로 시작 화면에서 `POST /api/games/location/sessions`, `POST /api/games/population/sessions`로 들어온다.

하지만 지금은 컨트롤러가 `gameLevel`을 더 이상 public 규칙으로 쓰지 않는다.

- [LocationGameApiController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/web/LocationGameApiController.java)
- [PopulationGameApiController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java)

둘 다 현재는 서비스의 기본 `start...Game(...)` 오버로드만 호출해서 Level 1 세션으로 시작한다.

즉, 이 1차 롤백 시점에는 start API가 public 기준을 다시 Level 1-only로 수렴시키는 방향으로 닫혔다.

### 2. public read model에서 Level 2를 제거했다

이제 public 표면은 다시 단순하다.

- `/ranking`: `위치/인구수`, `전체/일간`만 유지
- `/stats`: 공개 활동 지표와 공개 랭킹만 유지
- `/mypage`: 전체 최고 기록, 최근 플레이, 플레이 성향만 유지

여기서 중요한 점은 “템플릿에서 가리기”로 끝내지 않았다는 것이다.

- [LeaderboardPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java)는 더 이상 `locationLevel2All`, `populationLevel2All` 같은 SSR 모델을 만들지 않는다.
- [StatsPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/stats/web/StatsPageController.java)도 Level 2 highlight 모델을 읽지 않는다.
- [MyPageService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/mypage/application/MyPageService.java)도 Level 2 best run read model을 만들지 않는다.

즉, read model 책임부터 Level 1-only 기준으로 다시 닫았다.

결과 화면과 플레이 JS도 같은 기준으로 정리했다.

- `Level 1 / Level 2` 라벨 제거
- 거리/방향 힌트, precision band, hint debt 같은 Level 2 카피 제거
- 현재 사용자에게는 “기본 모드 하나”처럼 보이도록 통일

## 기존 DB와 Redis 데이터는 어떻게 지우는가

핵심은 [GameLevelRollbackInitializer.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java) 이다.

앱이 시작되면 이 initializer가 먼저 아래를 실행한다.

1. `location_game_session.game_level = 'LEVEL_2'` 세션 삭제
2. 그 세션에 속한 `location_game_stage`, `location_game_attempt` 삭제
3. `population_game_session.game_level = 'LEVEL_2'` 세션 삭제
4. 그 세션에 속한 `population_game_stage`, `population_game_attempt` 삭제
5. `leaderboard_record.game_level = 'LEVEL_2'` 삭제
6. Redis `leaderboard:*:*:l2*` 키 삭제

즉, “앞으로 안 쓰겠다”가 아니라 “기존 흔적도 boot 시 정리한다”로 닫았다.

## 왜 당시에는 enum까지 바로 지우지 않았는가

이 부분이 설계적으로 중요하다.

`LEVEL_2` enum 상수를 즉시 지우면, old DB에 그 값이 남아 있는 상태에서 JPA가 로딩 단계부터 깨질 수 있다.

그래서 이번 조각은 두 단계로 나눴다.

1. public surface rollback
2. startup purge

먼저 product 기준을 Level 1-only로 되돌리고, boot 시 기존 Level 2 row를 지우는 initializer를 넣었다.
그 뒤 internal enum과 실험 코드는 후속 조각에서 실제로 제거했다.

즉, 이번 조각은 “기능 삭제”보다 `호환성을 깨지 않는 롤백`에 가깝다.

## 테스트는 무엇으로 고정했는가

가장 중요한 테스트는 [GameLevelRollbackInitializerIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java) 이다.

이 테스트는 직접 Level 1 / Level 2 데이터를 같이 만든 뒤 initializer를 실행해서 아래를 검증한다.

- Level 2 location session / stage / attempt 삭제
- Level 2 population session / stage / attempt 삭제
- Level 2 leaderboard record 삭제
- Redis `l2` 키 삭제
- Level 1 데이터는 그대로 유지

같이 고정한 테스트는 아래다.

- [LocationGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java)
- [PopulationGameFlowIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java)
- [LeaderboardIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java)
- [StatsPageControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/stats/StatsPageControllerTest.java)
- [MyPageControllerTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/web/MyPageControllerTest.java)
- [MyPageServiceIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/mypage/MyPageServiceIntegrationTest.java)

즉, public surface와 data purge를 같이 검증했다.

## 면접에서는 어떻게 설명하면 좋은가

이 조각은 이렇게 설명하면 된다.

> Level 2를 실험적으로 열어 봤지만, 현재 제품 기준에서는 복잡도에 비해 가치가 낮다고 판단했습니다. 그래서 UI만 숨기지 않고, start 흐름과 `/ranking`, `/stats`, `/mypage` read model을 먼저 Level 1-only로 되돌렸습니다. 동시에 startup initializer를 둬서 old DB와 Redis에 남아 있던 `LEVEL_2` 흔적도 부팅 시 자동으로 지우게 만들었습니다. 핵심은 기능 삭제가 아니라, 호환성을 깨지 않는 롤백 순서를 설계했다는 점입니다.
