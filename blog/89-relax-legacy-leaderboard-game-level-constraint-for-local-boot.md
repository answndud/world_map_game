# local boot에서 legacy leaderboard game_level 제약 풀기

## 왜 이 조각이 필요한가

제품 기준에서는 위치/인구수 Level 2를 이미 걷어냈다.

코드도 정리했고, startup rollback도 넣어 두었다.

그런데 local DB가 예전 스키마를 아직 갖고 있으면
부팅 시점에 다른 종류의 문제가 남을 수 있다.

이번에 실제로 드러난 문제는 두 가지였다.

- `leaderboard_record.game_level` 컬럼은 여전히 남아 있음
- 그 컬럼이 `NOT NULL`
- `leaderboard_record_game_mode_check`가 예전 두 게임만 허용하고 있음
- 현재 [LeaderboardRecord.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java)는 그 컬럼을 더 이상 모름
- 따라서 demo bootstrap이 새 run을 넣을 때 insert가 실패함

즉, row 정리만으로는 부족했고
legacy 제약까지 같이 풀어야 local demo가 현재 코드 기준으로 다시 살아났다.

## 이번에 바뀐 파일

- [GameLevelRollbackInitializer.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)
- [GameLevelRollbackInitializerIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java)

같이 [README.md](/Users/alex/project/worldmap/README.md),
[PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md),
[WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md),
[50-current-state-rebuild-map.md](/Users/alex/project/worldmap/blog/50-current-state-rebuild-map.md)도 현재 기준으로 맞췄다.

## 무엇을 바꿨나

핵심은 [GameLevelRollbackInitializer.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)다.

기존에는 이 초기화기가

- `location_game_session.game_level`
- `population_game_session.game_level`
- `leaderboard_record.game_level`

컬럼이 남아 있는지 확인하고,
그중 `LEVEL_2` row와 Redis `l2` 키를 지우는 역할만 했다.

이제 initializer는 먼저 stale check constraint를 제거한다.

```sql
ALTER TABLE leaderboard_record DROP CONSTRAINT leaderboard_record_game_mode_check
```

그리고 `leaderboard_record.game_level` 컬럼이 있으면
아래를 수행한다.

```sql
ALTER TABLE leaderboard_record ALTER COLUMN game_level DROP NOT NULL
```

그 다음 예전 `LEVEL_2` row를 지운다.

즉, rollback 대상이 단순 row가 아니라
현재 엔티티와 충돌하는 schema constraint까지 넓어진 것이다.

## 왜 이 로직이 DemoBootstrapService가 아니라 initializer에 있어야 하나

문제는 demo bootstrap이 아니라
**demo bootstrap보다 먼저 풀려야 하는 schema 호환성**이다.

startup 순서는 현재 이렇게 간다.

1. country seed
2. admin bootstrap
3. recommendation legacy column initializer
4. game level rollback initializer
5. demo bootstrap

이번 문제는 5번이 시작되기 전에 4번에서 해결돼야 한다.

따라서 이 책임은
[DemoBootstrapService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/demo/application/DemoBootstrapService.java)가 아니라
[GameLevelRollbackInitializer.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)가 맡는 편이 맞다.

## 테스트로 무엇을 고정했나

[GameLevelRollbackInitializerIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java)에서

- legacy `game_level` 컬럼을 준비하고
- `leaderboard_record.game_level`을 일부러 `NOT NULL`로 만든 뒤
- `leaderboard_record_game_mode_check`를 `LOCATION`, `POPULATION`만 허용하는 예전 형태로 바꾼 뒤
- initializer를 실행하고
- `LEVEL_2` row가 지워졌는지
- `game_level`이 `nullable`로 돌아왔는지
- stale check constraint가 사라졌는지

를 같이 검증했다.

그리고 [DemoBootstrapIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/demo/DemoBootstrapIntegrationTest.java)로
demo bootstrap이 여전히 현재 기준 sample run을 정상 생성하는지도 같이 확인했다.

실행:

```bash
./gradlew test --tests com.worldmap.common.config.GameLevelRollbackInitializerIntegrationTest --tests com.worldmap.demo.DemoBootstrapIntegrationTest
```

## 지금 상태를 어떻게 설명하면 되나

현재 local boot는
“Level 2 row를 지우는 것”에서 끝나지 않는다.

이제는

- legacy `game_level` 컬럼이 남아 있어도
- 현재 엔티티가 그 컬럼을 모르더라도
- startup initializer가 예전 제약을 먼저 풀고
- demo bootstrap이 current 모델 기준으로 sample run을 넣는다.

즉, local 재현성 기준이 한 단계 더 안정해졌다.

## 다음 단계

다음 후보는 둘 중 하나다.

- local smoke를 한 번 더 돌려 다른 legacy 제약이 없는지 확인
- 신규 게임 3종 이후 public surface와 demo flow를 계속 다듬기

## 면접에서 이렇게 설명할 수 있다

> Level 2 코드를 지운 뒤에도 local DB에 `leaderboard_record.game_level NOT NULL` 제약과 예전 `game_mode` check constraint가 남아 있으면, 현재 엔티티가 새 게임 모드를 저장할 때 demo bootstrap insert가 실패할 수 있었습니다. 그래서 `GameLevelRollbackInitializer`가 예전 `LEVEL_2` row를 지우는 것에 더해, legacy `game_level` 제약과 stale `game_mode` check까지 같이 정리하게 바꿨습니다. 덕분에 현재 코드 기준 local demo가 예전 DB에서도 바로 올라오게 됐습니다.
