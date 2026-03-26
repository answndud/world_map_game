# 남아 있던 internal Level 2 호환 코드를 완전히 제거하기

## 왜 이 조각이 필요했는가

이전 글 [72-roll-back-game-level-2-and-purge-legacy-data.md](./72-roll-back-game-level-2-and-purge-legacy-data.md) 에서 public 제품 기준은 이미 `Level 1-only`로 되돌렸다.

하지만 그 시점에는 설명 비용을 줄이기 위해

- internal enum
- Level 2 점수 정책
- 결과 read model
- 랭킹의 level 축

같은 호환 코드가 일부 남아 있었다.

현재 제품 범위가 Level 1-only로 굳어진 이상,
이 호환 코드를 계속 두면 오히려 코드와 문서가 함께 복잡해진다.

그래서 이번 조각의 목표는 아래 두 가지였다.

1. public만이 아니라 internal code 기준도 정말 `Level 1-only`로 정리한다.
2. legacy DB 정리는 계속 안전하게 유지한다.

## 이번에 바뀐 범위

핵심 파일은 아래다.

- [GameLevelRollbackInitializer.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)
- [LocationGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/application/LocationGameService.java)
- [PopulationGameService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/application/PopulationGameService.java)
- [LeaderboardService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/application/LeaderboardService.java)
- [LocationGameSession.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/domain/LocationGameSession.java)
- [PopulationGameSession.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java)
- [LeaderboardRecord.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java)
- [LeaderboardRecordRepository.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java)
- [GameLevelRollbackInitializerIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java)

그리고 아래 Level 2 전용 글과 설계 문서는 현재 기준에서 제거했다.

- `blog/62` ~ `blog/71`
- `docs/LOCATION_GAME_LEVEL_2_FIRST_SLICE_PLAN.md`

## 요청 흐름은 어떻게 단순해졌는가

이제 게임 시작 요청은 정말로 단순하다.

- `POST /api/games/location/sessions`
- `POST /api/games/population/sessions`

둘 다 닉네임만 받고, 서비스는 항상 Level 1 세션을 만든다.

즉, 현재 요청 흐름에는 더 이상 `gameLevel` 분기가 없다.

### 위치 게임

`LocationGameService`는 이제

- Level enum을 읽지 않고
- 거리/방향 힌트를 계산하지 않고
- hint debt를 점수에 반영하지 않는다.

즉, 다시 `선택 국가 비교 -> 정답/오답 -> 하트/점수 변경`이라는 기본 게임 루프만 남겼다.

### 인구수 게임

`PopulationGameService`도 이제

- 직접 수치 입력형 분기 없이
- 보기 4개 선택형 제출만 처리하고
- precision band / error rate read model을 만들지 않는다.

즉, Level 1 구간형 퀴즈 구조만 남긴 것이다.

### 랭킹

`LeaderboardService`는 이제 `gameMode + scope`만 읽는다.

이전에는 `gameMode + gameLevel + scope` 조합으로 Redis key와 DB fallback을 찾았지만,
지금은 level 축 자체가 없다.

그래서 public `/ranking`, `/stats`, `/mypage`가 더 단순해졌을 뿐 아니라,
service 레벨의 read model도 같이 단순해졌다.

## 왜 rollback initializer는 남겨 뒀는가

핵심은 [GameLevelRollbackInitializer.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java) 이다.

이제 current JPA 스키마에는 `game_level` 필드가 없다.
그래서 initializer도 같이 바꿨다.

- legacy `game_level` 컬럼이 실제로 있으면 purge 수행
- 컬럼이 없으면 그냥 skip

즉,

- fresh schema에서는 아무 일도 하지 않고
- 예전 local PostgreSQL처럼 `game_level` 컬럼이 남아 있는 DB에서만
  `LEVEL_2` row와 Redis `l2` 키를 정리한다.

이렇게 해야 현재 코드를 단순하게 유지하면서도,
오래된 개발 DB를 안전하게 다시 살릴 수 있다.

## 왜 이 로직이 컨트롤러가 아니라 initializer / 서비스에 있어야 하는가

이 조각의 핵심은 HTTP 분기가 아니다.

- legacy schema가 남아 있는가
- legacy row를 purge해야 하는가
- 랭킹 read model이 mode-only인가
- 점수 정책이 Level 1-only인가

이런 기준은 요청 본문보다 도메인 / boot 규칙에 가깝다.

그래서

- legacy 정리는 `GameLevelRollbackInitializer`
- 게임 규칙 단순화는 각 `...Service`, `...ScoringPolicy`
- 랭킹 축 단순화는 `LeaderboardService`

가 맡는 편이 맞다.

## 테스트는 무엇으로 고정했는가

가장 중요한 테스트는 두 가지다.

### 1. legacy DB purge 통합 테스트

[GameLevelRollbackInitializerIntegrationTest.java](/Users/alex/project/worldmap/src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java)

이 테스트는 H2 기본 스키마에는 더 이상 없는 `game_level` 컬럼을
테스트 안에서 직접 추가한 뒤,

- legacy `LEVEL_2` row를 심고
- initializer 실행 후
- 세션 / stage / attempt / leaderboard row와 Redis `l2` 키가 비워지는지

확인한다.

즉, current schema와 legacy DB를 한 테스트에서 같이 설명할 수 있게 만들었다.

### 2. 전체 테스트 재통과

`./gradlew test`

이 검증은 중요하다.
이전에는 Level 2 제거가 일부에만 적용돼 있으면 context 로딩부터 깨졌는데,
이번에는 전체 테스트가 다시 통과해야 “internal 호환 코드 제거가 정말 끝났다”고 말할 수 있기 때문이다.

## 면접에서는 어떻게 설명하면 좋은가

이 조각은 이렇게 설명하면 된다.

> public에서 Level 2를 숨긴 뒤에도 internal enum, 점수 정책, 랭킹 level 축 같은 호환 코드가 남아 있으면 결국 현재 제품 범위를 설명하기 어려워집니다. 그래서 이번에는 위치/인구수 서비스, 랭킹 service, read model에서 Level 2 축을 완전히 제거하고, 대신 startup initializer만 legacy DB 정리 책임으로 남겼습니다. 즉, 현재 코드는 단순하게 유지하면서도 오래된 local DB는 계속 안전하게 복구할 수 있는 구조로 마무리했습니다.

## 지금 기준에서 얻은 상태

- public 제품은 완전히 Level 1-only
- internal code도 Level 1-only
- legacy DB는 startup에서 안전하게 정리
- 블로그와 current-state 재현 가이드도 현재 기준으로 단순화

즉, 이제는 “예전에 이런 실험도 했다”보다
“현재 서비스는 무엇을 제공하는가”를 더 짧고 명확하게 설명할 수 있다.
