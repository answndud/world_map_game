# [Spring Boot 게임 플랫폼 포트폴리오] 09. Redis leaderboard와 공개 `/ranking` 페이지를 어떻게 만들었는가

## 1. 이번 글에서 풀 문제

게임 한 판이 끝났다고 해서 곧바로 랭킹 화면이 되는 것은 아닙니다.  
게임 세션은 쓰기 모델이고, 랭킹은 읽기 모델입니다.

WorldMap에서는 아래 질문을 먼저 정리해야 했습니다.

- 왜 게임 세션을 그대로 랭킹 row로 쓰지 않고 `LeaderboardRecord`를 별도 엔티티로 두는가
- 왜 Redis Sorted Set과 DB row를 같이 유지하는가
- 왜 `/ranking`, `/stats`, 일부 `/mypage`는 같은 read model 축으로 설명되어야 하는가
- 왜 초기 SSR은 한 보드만 그리고, 나머지는 client-side에서 지연 로드하는가
- 왜 Redis가 죽어도 public read path는 계속 살아 있어야 하는가

즉, 이 글은 단순 "실시간 랭킹 추가"가 아니라 **쓰기 모델과 읽기 모델 분리 + Redis cache + fallback 운영 전략**을 설명합니다.

## 2. 최종 도착 상태

이 글이 끝났을 때 랭킹 시스템은 아래 상태여야 합니다.

- terminal run만 leaderboard에 반영된다
- 게임 모드별 최종 결과가 [LeaderboardRecord.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java) row로 저장된다
- Redis Sorted Set이 전체/일간 top-N 조회를 빠르게 제공한다
- `runSignature`와 unique constraint로 duplicate terminal submit을 방지한다
- `/ranking`은 SSR 첫 화면과 client polling을 섞어 현재 보드만 갱신한다
- 첫 SSR은 `location:ALL` 한 보드만 렌더링하고, 나머지는 placeholder로 defer한다
- `/api/rankings/{gameMode}`가 현재 보드 데이터를 JSON으로 내려준다
- Redis read가 실패해도 DB fallback으로 `/ranking`과 `/stats`가 계속 열린다

즉, 랭킹의 최종 상태는 "점수표를 보여 준다"가 아니라 **운영 가능한 public read model이 있다**는 것입니다.

## 3. 먼저 알아둘 개념

### 3-1. terminal run

진행 중 세션이 아니라, `GAME_OVER`나 `FINISHED`로 끝난 최종 결과만 leaderboard 대상이 됩니다.

### 3-2. write model vs read model

게임 세션은 진행 상태를 들고 있고, 랭킹은 비교 가능한 최종 결과를 들고 있습니다.  
둘을 분리해야 later stage에서 `/mypage`, `/stats`, `/dashboard`까지 같은 read model 언어로 설명할 수 있습니다.

### 3-3. Redis Sorted Set

점수 기준 top-N 조회에 적합한 Redis 자료구조입니다.  
WorldMap은 여기서 실제 row 전체를 저장하지 않고 `LeaderboardRecord.id`를 member로 넣고 score를 정렬 점수로 씁니다.

### 3-4. fallback

캐시가 죽어도 public read path는 살아 있어야 합니다.  
즉, Redis는 성능 계층이지 source of truth가 아닙니다.

### 3-5. active board polling

현재 사용자가 보고 있는 보드만 15초 간격으로 갱신하고, 나머지 보드는 필요할 때만 불러오는 전략입니다.

## 4. 이번 글에서 다룰 파일

- [LeaderboardRecord.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java)
- [LeaderboardRecordRepository.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java)
- [LeaderboardGameMode.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardGameMode.java)
- [LeaderboardScope.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardScope.java)
- [LeaderboardService.java](../src/main/java/com/worldmap/ranking/application/LeaderboardService.java)
- [LeaderboardRankingPolicy.java](../src/main/java/com/worldmap/ranking/application/LeaderboardRankingPolicy.java)
- [LeaderboardView.java](../src/main/java/com/worldmap/ranking/application/LeaderboardView.java)
- [LeaderboardEntryView.java](../src/main/java/com/worldmap/ranking/application/LeaderboardEntryView.java)
- [LeaderboardApiController.java](../src/main/java/com/worldmap/ranking/web/LeaderboardApiController.java)
- [LeaderboardPageController.java](../src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java)
- [StatsPageController.java](../src/main/java/com/worldmap/stats/web/StatsPageController.java)
- [ranking/index.html](../src/main/resources/templates/ranking/index.html)
- [stats/index.html](../src/main/resources/templates/stats/index.html)
- [ranking.js](../src/main/resources/static/js/ranking.js)
- [LeaderboardIntegrationTest.java](../src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java)
- [LeaderboardPageControllerTest.java](../src/test/java/com/worldmap/ranking/LeaderboardPageControllerTest.java)
- [RedisUnavailableLeaderboardFallbackIntegrationTest.java](../src/test/java/com/worldmap/ranking/RedisUnavailableLeaderboardFallbackIntegrationTest.java)
- [StatsPageControllerTest.java](../src/test/java/com/worldmap/stats/StatsPageControllerTest.java)

## 5. 핵심 도메인 모델 / 상태

### 5-1. `LeaderboardRecord`

[LeaderboardRecord.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java)는 게임 세션과 별도로 저장되는 최종 결과 row입니다.

핵심 필드:

- `runSignature`
- `sessionId`
- `gameMode`
- `playerNickname`
- `memberId`
- `guestSessionKey`
- `totalScore`
- `rankingScore`
- `clearedStageCount`
- `totalAttemptCount`
- `leaderboardDate`
- `finishedAt`

중요한 점은 `rankingScore`가 단순 `totalScore`가 아니라 정렬 정책이 반영된 값이라는 것입니다.

### 5-2. `LeaderboardGameMode`, `LeaderboardScope`

[LeaderboardGameMode.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardGameMode.java)는 현재 5개 게임을 갖습니다.

- `LOCATION`
- `CAPITAL`
- `FLAG`
- `POPULATION_BATTLE`
- `POPULATION`

[LeaderboardScope.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardScope.java)는 두 범위를 가집니다.

- `ALL`
- `DAILY`

즉, 랭킹은 "게임 모드 x 집계 범위" 조합으로 읽힙니다.

### 5-3. Redis leaderboard key

[LeaderboardService.java](../src/main/java/com/worldmap/ranking/application/LeaderboardService.java)는 아래 키 규칙을 사용합니다.

- 전체: `"{prefix}:all:{gameModePath}"`
- 일간: `"{prefix}:daily:{gameModePath}:{yyyy-mm-dd}"`

예:

- `leaderboard:all:location`
- `leaderboard:daily:location:2026-04-01`

즉, Redis key 설계도 read model의 일부입니다.

### 5-4. `LeaderboardView`

[LeaderboardView.java](../src/main/java/com/worldmap/ranking/application/LeaderboardView.java)는 SSR과 API가 공유하는 read model입니다.

필드:

- `gameMode`
- `scope`
- `targetDate`
- `entries`

entries는 [LeaderboardEntryView.java](../src/main/java/com/worldmap/ranking/application/LeaderboardEntryView.java)로 표현되며, rank/player/score/cleared/attempt/finishedAt을 가집니다.

## 6. 설계 구상

### 왜 게임 세션을 그대로 랭킹 row로 쓰지 않았는가

게임 세션은 아래를 들고 있습니다.

- 진행 상태
- 현재 stage
- restart 이력
- lives
- owner

랭킹은 아래만 필요합니다.

- 비교 가능한 최종 score
- 어떤 게임 모드인가
- 몇 stage를 깼는가
- 몇 번 시도했는가
- 언제 끝났는가

즉, game session을 그대로 읽기 모델로 쓰면 "현재 상태"와 "최종 결과"가 섞입니다.  
그래서 final run만 `LeaderboardRecord`로 압축합니다.

### 왜 Redis와 DB를 둘 다 쓰는가

Redis만 쓰면:

- durable history가 약함
- 소유권 claim, `/mypage`, analytics 연결이 약함
- 장애 시 공용 랭킹이 그대로 멈춤

DB만 쓰면:

- top-N read 성능과 polling 설명이 약함
- "실시간 보드"의 캐시 전략이 불분명함

그래서 WorldMap은 다음 조합을 택합니다.

- DB: durable source of truth
- Redis: top-N read cache

### 왜 `/ranking`과 `/stats`가 같은 read path를 재사용하는가

두 화면은 보이는 UI는 달라도 사실상 같은 질문을 합니다.

- 지금 top score가 무엇인가
- 오늘 상위 기록은 누구인가

즉, 둘 다 public read model이며, source를 다르게 두면 오히려 설명이 복잡해집니다.

### 왜 첫 SSR은 한 보드만 그리는가

현재 [LeaderboardPageController.java](../src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java)는 `location:ALL` 한 보드만 SSR로 읽습니다.

이유:

- 첫 진입 TTFB 절감
- 당장 보이지 않는 9개 보드를 서버가 다 계산할 필요가 없음
- 이후 client-side interaction으로 필요한 보드만 fetch 가능

즉, SSR도 "전부 미리 계산"이 아니라 **first meaningful board만 즉시 보여 주는 전략**입니다.

## 7. 코드 설명

### 7-1. `LeaderboardRankingPolicy`: 정렬 기준을 코드로 고정

[LeaderboardRankingPolicy.java](../src/main/java/com/worldmap/ranking/application/LeaderboardRankingPolicy.java)는 `rankingScore`를 계산합니다.

현재 가중치:

- `SCORE_WEIGHT = 1_000_000`
- `CLEAR_WEIGHT = 1_000`
- `ATTEMPT_BONUS_CAP = 999`

계산식:

- `totalScore * SCORE_WEIGHT`
- `+ clearedStageCount * CLEAR_WEIGHT`
- `+ attemptBonus`

여기서 attempt bonus는 시도 수가 적을수록 높아집니다.

즉, 정렬은 단순 점수 비교가 아니라 **점수 > 클리어 수 > 시도 효율** 순입니다.  
`finishedAt`은 DB query 정렬에서 마지막 tie-breaker 역할을 합니다.

### 7-2. `LeaderboardRecord`: duplicate submit을 막는 durable row

[LeaderboardRecord.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java)는 `run_signature` unique constraint를 가집니다.

`runSignature`는 현재 다음 값으로 만듭니다.

```text
{gameMode}:{sessionId}:{finishedAt}
```

즉, 같은 terminal run이 중복 반영되는 것을 DB 레벨에서도 막습니다.

### 7-3. `LeaderboardService.recordResult(...)`

[LeaderboardService.java](../src/main/java/com/worldmap/ranking/application/LeaderboardService.java)의 write path는 이 글의 핵심입니다.

흐름:

1. `finishedAt == null`이면 거부
2. `runSignature` 중복이면 no-op
3. `LeaderboardRankingPolicy`로 `rankingScore` 계산
4. `LeaderboardRecord`를 DB에 `saveAndFlush`
5. unique race면 `DataIntegrityViolationException`을 잡고 no-op
6. commit 이후 `syncRecordToRedis(record)`

여기서 중요한 점은 **Redis sync를 afterCommit에 태운다**는 것입니다.  
즉, DB write가 확정되기 전에는 cache를 갱신하지 않습니다.

이 write path는 [LocationGameService.java](../src/main/java/com/worldmap/game/location/application/LocationGameService.java), [PopulationGameService.java](../src/main/java/com/worldmap/game/population/application/PopulationGameService.java), [CapitalGameService.java](../src/main/java/com/worldmap/game/capital/application/CapitalGameService.java), [FlagGameService.java](../src/main/java/com/worldmap/game/flag/application/FlagGameService.java), [PopulationBattleGameService.java](../src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameService.java)에서 terminal 상태가 확정될 때만 호출됩니다.  
즉, leaderboard write는 컨트롤러에서 "요청이 왔으니 기록"하는 구조가 아니라, **각 게임 service가 세션 상태를 `FINISHED` 또는 `GAME_OVER`로 바꾸는 바로 그 시점**에만 발생합니다.

### 7-4. `getLeaderboard(...)`: read path의 두 단계 전략

`getLeaderboard(gameMode, scope, limit)` 흐름은 아래와 같습니다.

1. limit sanitize (`1~50`)
2. daily면 `LocalDate.now()` targetDate 계산
3. Redis에서 top record id 목록 조회
4. Redis hit면 DB에서 그 id 순서대로 row 복원
5. Redis miss거나 일부 row 불일치면 DB top-N query fallback
6. fallback 결과를 다시 Redis에 warm/rebuild
7. `LeaderboardView` 조립

즉, Redis는 row 전체 저장소가 아니라 **정렬된 id index**에 가깝습니다.

### 7-5. Redis 장애 fallback

`topRecordIdsFromRedis(...)`, `trySyncRecordsToRedis(...)`, `tryRebuildRedisKey(...)`는 `DataAccessException`을 잡고 로그만 남깁니다.

의미:

- Redis read 실패 -> DB fallback
- Redis warm/rebuild 실패 -> public response는 계속 반환

즉, `/ranking`과 `/stats`는 "캐시가 죽으면 같이 죽는 화면"이 아니라 **공용 정보 화면은 살리고 캐시만 나중에 회복하는 구조**입니다.

### 7-6. `LeaderboardPageController`: initial SSR을 한 보드로 제한

[LeaderboardPageController.java](../src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java)는 현재 딱 하나만 model에 넣습니다.

- `locationAll`

이 선택은 [LeaderboardPageControllerTest.java](../src/test/java/com/worldmap/ranking/LeaderboardPageControllerTest.java)로도 고정돼 있습니다.  
즉, 이 컨트롤러의 책임은 "랭킹 계산"이 아니라 **첫 화면에 무엇을 즉시 SSR할지 결정하는 것**입니다.

### 7-7. `ranking/index.html`: placeholder-first SSR

[ranking/index.html](../src/main/resources/templates/ranking/index.html)은 10개 보드 패널을 모두 정의하지만, 대부분은 `data-initial-rendered="false"`와 placeholder row를 가집니다.

현재 구조:

- mode buttons: location/capital/flag/population-battle/population
- scope buttons: ALL/DAILY
- `location:ALL`만 SSR 데이터 포함
- 나머지는 "보드를 열면 랭킹을 불러옵니다." placeholder

즉, template 자체가 initial SSR defer 전략을 드러냅니다.

### 7-8. `ranking.js`: active board만 15초 polling

[ranking.js](../src/main/resources/static/js/ranking.js)는 현재 랭킹 UX의 중심입니다.

책임:

- active mode/scope 상태 유지
- 현재 보드만 fetch
- 15초 간격 자동 갱신
- visibility change 시 pause/resume
- manual refresh queue
- daily panel copy에 `targetDate` 반영
- placeholder board를 실제 payload로 hydrate

이 파일이 중요한 이유는 "실시간"을 무조건 websocket으로 만들지 않고도, **현재 보는 보드에만 polling cost를 집중하는 구조**를 보여 주기 때문입니다.

### 7-9. `/stats`도 같은 read path를 쓴다

[StatsPageController.java](../src/main/java/com/worldmap/stats/web/StatsPageController.java)는 `ServiceActivityService`와 별도로 `LeaderboardService.getLeaderboard(...)`를 다섯 번 호출합니다.

- `LOCATION + DAILY + 3`
- `CAPITAL + DAILY + 3`
- `FLAG + DAILY + 3`
- `POPULATION_BATTLE + DAILY + 3`
- `POPULATION + DAILY + 3`

즉, `/stats`는 별도 stats 전용 랭킹 집계 로직을 만들지 않습니다.  
[stats/index.html](../src/main/resources/templates/stats/index.html)은 이 다섯 `LeaderboardView`를 그대로 테이블로 렌더링할 뿐입니다.

이 설계가 중요한 이유는, `/ranking`과 `/stats`가 UI는 달라도 결국 같은 질문을 하기 때문입니다.

- 지금 상위 기록이 누구인가
- 오늘의 대표 run이 무엇인가

따라서 public read model의 source of truth도 하나여야 합니다.  
`/stats`를 별도 query나 별도 캐시 체계로 만들면, public surface끼리 서로 다른 top score를 보여 줄 위험이 생깁니다.

## 8. 요청 흐름 / 상태 변화

### 8-1. write path: 게임 종료 -> leaderboard 반영

```text
게임 service가 terminal 상태 결정
-> LeaderboardService.record{GameMode}Result(...)
-> runSignature 생성
-> LeaderboardRecord DB 저장
-> afterCommit
-> Redis ALL / DAILY key에 record id + rankingScore 추가
```

### 8-2. read path: SSR 첫 화면

```text
GET /ranking
-> LeaderboardPageController
-> getLeaderboard(LOCATION, ALL, 10)
-> Redis hit면 id 목록 -> DB row 복원
-> miss면 DB top-N -> Redis warm
-> locationAll 모델만 렌더링
```

### 8-3. read path: 보드 전환 / polling

```text
브라우저에서 mode/scope 버튼 클릭
-> ranking.js active board 변경
-> GET /api/rankings/{gameMode}?scope=...
-> current board tbody 갱신
-> 15초 간격으로 현재 board만 refresh
```

### 8-4. Redis 장애 시 fallback

```text
GET /ranking 또는 /stats 또는 /api/rankings/{gameMode}
-> LeaderboardService.getLeaderboard(...)
-> Redis read DataAccessException
-> DB top-N query
-> public response 계속 반환
-> Redis warm/rebuild는 best effort
```

## 9. 실패 케이스 / 예외 처리

- 진행 중 세션을 랭킹에 넣으면: 비교 대상이 오염된다
- `finishedAt` 없는 run을 기록하면: service가 거부한다
- duplicate terminal submit이 오면: `runSignature` 중복/DB unique race를 no-op로 처리한다
- Redis만 믿으면: Redis 장애 때 public ranking이 같이 죽는다
- DB만 믿으면: top-N read 성능과 캐시 전략 설명이 약하다
- 전체 보드를 매번 polling하면: 불필요한 네트워크 비용과 서버 부하가 커진다
- limit이 `1~50` 범위를 벗어나면: `400`
- Redis에 id는 있는데 DB row가 일부 없으면: DB fallback + key rebuild로 수복한다

즉, 랭킹의 핵심 리스크는 "점수 계산"보다 **중복 기록, cache drift, 장애 대응**에 가깝습니다.

## 10. 테스트로 검증하기

### 10-1. [LeaderboardIntegrationTest.java](../src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java)

이 테스트는 단순 location 랭킹만 보지 않습니다.

검증 내용:

- location game terminal run이 실제 leaderboard row와 Redis key에 반영되는지
- duplicate terminal submit이 더 이상 추가 기록을 만들지 않는지
- `/api/rankings/location?scope=ALL`, `?scope=DAILY`가 맞게 응답하는지
- `/ranking`이 `locationAll`만 SSR로 읽고 placeholder를 유지하는지
- capital, flag, population-battle 등 다른 게임도 각자 key를 가지는지

즉, read/write 양쪽을 함께 고정합니다.

### 10-2. [LeaderboardPageControllerTest.java](../src/test/java/com/worldmap/ranking/LeaderboardPageControllerTest.java)

- 현재 `/ranking`이 첫 렌더에서 `LOCATION + ALL` 한 보드만 service에 요청하는지
- 나머지 보드는 model에 싣지 않는지

즉, initial SSR 전략을 테스트로 고정합니다.

### 10-3. [RedisUnavailableLeaderboardFallbackIntegrationTest.java](../src/test/java/com/worldmap/ranking/RedisUnavailableLeaderboardFallbackIntegrationTest.java)

- Redis가 `127.0.0.1:6390`으로 죽어 있는 환경에서
  - `/api/rankings/location`
  - `/ranking`
  - `/stats`
  가 모두 DB fallback으로 계속 읽히는지 검증합니다.

이 테스트는 production-ready 관점에서 매우 중요합니다.  
랭킹이 예쁘게 뜨는지보다, **캐시 장애에도 public read path가 사망하지 않는지**를 보장하기 때문입니다.
다만 이 보장은 현재 저장소의 simulated Redis-unavailable 조건과 public controller/read path 기준입니다.
실제 운영에서 Redis 지연, warm/rebuild backlog, 관측 지표 부족까지 한 번에 해결됐다는 뜻은 아닙니다.

### 10-4. [StatsPageControllerTest.java](../src/test/java/com/worldmap/stats/StatsPageControllerTest.java)

- `/stats`가 5개 게임의 `DAILY top 3`를 같은 `LeaderboardService` read path로 불러오는지
- guest와 admin session에서 public shell 차이가 어떻게 보이는지
- dashboard 링크 노출만 role에 따라 달라지고, stats 본문 데이터는 같은 controller/service contract를 타는지

즉, `/stats`도 별도 예외 화면이 아니라 public read model surface의 일부라는 사실을 테스트로 고정합니다.

실행 명령:

```bash
./gradlew test \
  --tests com.worldmap.ranking.LeaderboardIntegrationTest \
  --tests com.worldmap.ranking.LeaderboardPageControllerTest \
  --tests com.worldmap.ranking.RedisUnavailableLeaderboardFallbackIntegrationTest
```

## 11. 회고

랭킹은 Redis showcase가 아니라 "쓰기 모델과 읽기 모델을 분리하고, cache와 durable source를 같이 설명하는 단계"였습니다.

이 구조의 장점:

- terminal run만 비교 대상으로 유지한다
- Redis와 DB를 함께 써도 source of truth가 흔들리지 않는다
- `/ranking`, `/stats`, 일부 `/mypage`가 같은 read model 언어를 재사용한다
- initial SSR과 active board polling을 같이 설명할 수 있다
- Redis 장애 시 public read path가 계속 살아 있다

### 현재 구현의 한계

- 실시간 push는 아직 도입하지 않았고 polling 기반이다
- 더 세밀한 검색/기간 필터는 현재 범위 밖이다
- Redis warm/rebuild는 best effort라 대규모 운영에서는 별도 관측 지표가 더 필요할 수 있다
- fallback 테스트가 `/api/rankings`, `/ranking`, `/stats`를 강하게 고정하지만, production traffic 규모에서의 latency 특성이나 cache rebuild 비용까지 자동으로 증명하지는 않는다

## 12. 취업 포인트

### 12-1. 1문장 답변

WorldMap은 terminal game result를 `LeaderboardRecord`라는 별도 read model로 저장하고, Redis Sorted Set과 DB fallback을 함께 써서 빠르면서도 운영 가능한 public `/ranking`을 만들었습니다.

### 12-2. 30초 답변

게임 세션은 진행 상태를 들고 있어서 랭킹 row로 그대로 쓰지 않았고, 끝난 결과만 `LeaderboardRecord`로 분리했습니다. `LeaderboardService`가 `runSignature`와 `rankingScore`를 기준으로 DB에 durable row를 저장하고, commit 이후 Redis Sorted Set에 `record id -> ranking score`를 동기화합니다. `/ranking`은 첫 화면에 `location:ALL` 한 보드만 SSR로 그리고, 나머지는 client-side에서 active board만 15초 polling합니다. 또 Redis가 죽어도 DB fallback으로 `/ranking`과 `/stats`를 계속 읽게 해서 public read path를 살렸습니다.

### 12-3. 예상 꼬리 질문

- 왜 게임 세션과 랭킹 row를 분리했나요?
- 왜 Redis와 DB를 둘 다 쓰나요?
- `runSignature`는 왜 필요한가요?
- 왜 `/ranking` 첫 SSR을 한 보드만 그리나요?
- Redis 장애 때 fallback을 꼭 넣어야 하나요?

## 13. 시작 상태

- 게임 종료 결과는 있지만 public ranking read model이 없는 상태
- top-N read와 durable history를 동시에 설명할 구조가 없는 상태
- `/ranking`과 `/stats`가 기대는 공용 read path가 없는 상태

## 14. 이번 글에서 바뀌는 파일

- `src/main/java/com/worldmap/ranking/domain/**`
- `src/main/java/com/worldmap/ranking/application/**`
- `src/main/java/com/worldmap/ranking/web/**`
- `src/main/java/com/worldmap/stats/web/StatsPageController.java`
- `src/main/resources/templates/ranking/index.html`
- `src/main/resources/templates/stats/index.html`
- `src/main/resources/static/js/ranking.js`
- 관련 통합 테스트

## 15. 구현 체크리스트

1. `LeaderboardRecord`, `LeaderboardGameMode`, `LeaderboardScope`를 만든다
2. `LeaderboardRankingPolicy`로 정렬 점수 계산 규칙을 분리한다
3. 각 게임 service에서 terminal 시점에만 `record{GameMode}Result(...)`를 호출한다
4. DB row 저장과 Redis sync를 분리한다
5. `runSignature`와 unique constraint로 duplicate terminal submit을 막는다
6. `/api/rankings/{gameMode}`를 만든다
7. `/ranking`은 initial SSR 한 보드만 그리고 나머지는 defer한다
8. `ranking.js`로 active board polling을 구현한다
9. Redis fallback 테스트를 추가한다

## 16. 실행 / 검증 명령

```bash
./gradlew test \
  --tests com.worldmap.ranking.LeaderboardIntegrationTest \
  --tests com.worldmap.ranking.LeaderboardPageControllerTest \
  --tests com.worldmap.ranking.RedisUnavailableLeaderboardFallbackIntegrationTest
```

수동 확인:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

그 뒤 `/ranking`과 `/stats`를 실제 브라우저에서 확인합니다.

## 17. 산출물 체크리스트

- terminal run만 leaderboard에 반영된다
- `LeaderboardRecord`가 durable read model로 저장된다
- Redis Sorted Set에서 top-N을 빠르게 읽을 수 있다
- `/ranking`은 현재 보드만 자동 갱신한다
- Redis 장애 시에도 `/ranking`, `/stats`가 계속 열린다

## 18. 글 종료 체크포인트

- 왜 랭킹은 game session이 아니라 별도 read model이어야 하는가
- 왜 Redis와 DB를 같이 두는가
- 왜 `runSignature`와 afterCommit sync가 필요한가
- 왜 `/ranking`은 first SSR과 active polling을 같이 써야 하는가
- 왜 public read path에는 fallback이 꼭 있어야 하는가

## 19. 자주 막히는 지점

- session row를 그대로 랭킹 화면에 쓰려는 것
- Redis를 source of truth처럼 다루는 것
- duplicate terminal submit을 "거의 안 오겠지" 수준으로 넘기는 것
- 첫 SSR에서 모든 보드를 다 미리 계산하는 것
- public read path의 fallback을 나중 문제로 미루는 것
