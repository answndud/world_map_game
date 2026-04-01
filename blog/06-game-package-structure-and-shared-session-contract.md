# [Spring Boot 게임 플랫폼 포트폴리오] 06. 게임 패키지 구조와 공통 `Session` 계약을 어떻게 만들었는가

## 1. 이번 글에서 풀 문제

위치 게임이든 수도 퀴즈든 국기 퀴즈든, WorldMap이 "서버 주도 게임 플랫폼"이라면 모든 게임이 최소한 같은 언어로 설명돼야 합니다.

이 글은 아래 문제를 닫습니다.

- 왜 각 게임 패키지를 `application / domain / web`으로 나누는가
- 왜 모든 게임 세션이 [BaseGameSession.java](../src/main/java/com/worldmap/game/common/domain/BaseGameSession.java)를 공유해야 하는가
- 왜 ownership 검사는 `GameSessionAccessContext` 같은 공통 계약으로 다뤄야 하는가
- 왜 stale submit 방어를 각 게임 서비스에 흩뿌리지 않고 공통 guard로 추출해야 하는가

즉, 이 글은 "첫 번째 게임"을 만들기 전에 **모든 게임이 같이 기대는 뼈대**를 설명합니다.

## 2. 최종 도착 상태

이 글이 끝났을 때 아래 구조가 있어야 합니다.

- `game/common` 아래에 공통 session 상태와 submission/access guard가 있다
- `location`, `population`, `capital`, `flag`, `populationbattle` 패키지가 동일한 `application / domain / web` 경계를 따른다
- 모든 게임 세션 엔티티는 `UUID id`, owner, score, round 상태, terminal status를 공통으로 가진다
- 브라우저 요청은 `GameSessionAccessContextResolver`를 통해 현재 member/guest ownership 문맥을 얻는다
- 게임 서비스는 `GameSessionAccessContext.assertCanAccess(...)`로 현재 세션 접근 권한을 검증한다
- answer API는 `GameSubmissionGuard.assertFreshSubmission(...)`로 stale submit을 공통 규약으로 막는다

즉, 이 단계가 끝나면 later stage 게임들은 "새로운 플랫폼"을 또 만드는 게 아니라 **공통 session contract를 구현하는 variant**가 됩니다.

## 3. 먼저 알아둘 개념

### 3-1. game package boundary

WorldMap의 각 게임은 같은 3층을 따릅니다.

- `domain`: 엔티티, 상태 전이
- `application`: 서비스, 정책, view 조립
- `web`: SSR page controller, JSON API controller, request DTO

### 3-2. base session

게임마다 규칙은 달라도, "한 판 전체"라는 추상화는 같습니다.  
그래서 한 판의 공통 속성을 base session으로 뽑습니다.

### 3-3. ownership context

현재 브라우저가 어떤 게임 세션에 접근 가능한지는 아래 둘 중 하나로 설명됩니다.

- 로그인 member id
- guest session key

### 3-4. stale submit

같은 답안이 중복 전송되거나, 이미 다음 stage로 넘어간 뒤 예전 payload가 다시 오면 이를 막아야 합니다.

## 4. 이번 글에서 다룰 파일

- [BaseGameSession.java](../src/main/java/com/worldmap/game/common/domain/BaseGameSession.java)
- [GameSessionStatus.java](../src/main/java/com/worldmap/game/common/domain/GameSessionStatus.java)
- [GameSessionAccessContext.java](../src/main/java/com/worldmap/game/common/application/GameSessionAccessContext.java)
- [GameSubmissionGuard.java](../src/main/java/com/worldmap/game/common/application/GameSubmissionGuard.java)
- [GameSessionAccessContextResolver.java](../src/main/java/com/worldmap/auth/application/GameSessionAccessContextResolver.java)
- [GuestSessionKeyManager.java](../src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java)
- [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)
- [AuthenticatedMemberSession.java](../src/main/java/com/worldmap/auth/application/AuthenticatedMemberSession.java)
- [SessionAccessDeniedException.java](../src/main/java/com/worldmap/common/exception/SessionAccessDeniedException.java)
- [ResourceNotFoundException.java](../src/main/java/com/worldmap/common/exception/ResourceNotFoundException.java)
- [GlobalApiExceptionHandler.java](../src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java)
- [LocationGameSession.java](../src/main/java/com/worldmap/game/location/domain/LocationGameSession.java)
- [PopulationGameSession.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java)
- [GameSessionAccessContextTest.java](../src/test/java/com/worldmap/game/common/application/GameSessionAccessContextTest.java)
- [GameSessionAccessContextResolverTest.java](../src/test/java/com/worldmap/auth/application/GameSessionAccessContextResolverTest.java)
- [MemberSessionManagerTest.java](../src/test/java/com/worldmap/auth/application/MemberSessionManagerTest.java)
- [LocationGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java)
- [PopulationGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java)

## 5. 핵심 도메인 모델 / 상태

### 5-1. `BaseGameSession`

[BaseGameSession.java](../src/main/java/com/worldmap/game/common/domain/BaseGameSession.java)는 `@MappedSuperclass`이며 모든 게임 세션이 공유하는 상태를 담습니다.

공통 필드:

- `UUID id`
- `playerNickname`
- `memberId`
- `guestSessionKey`
- `status`
- `totalRounds`
- `currentRoundNumber`
- `answeredRoundCount`
- `totalScore`
- `startedAt`
- `finishedAt`

이 클래스가 중요한 이유는 later stage에서 어떤 게임이든 "한 판 전체 상태"를 같은 언어로 설명할 수 있게 해 주기 때문입니다.

### 5-2. `GameSessionStatus`

[GameSessionStatus.java](../src/main/java/com/worldmap/game/common/domain/GameSessionStatus.java)는 네 상태를 가집니다.

- `READY`
- `IN_PROGRESS`
- `GAME_OVER`
- `FINISHED`

`GAME_OVER`와 `FINISHED`를 분리한 점이 중요합니다.

- `GAME_OVER`: 실패로 끝남
- `FINISHED`: 성공으로 끝남

later stage result 화면과 leaderboard 반영에서 이 구분이 바로 살아납니다.

### 5-3. `GameSessionAccessContext`

[GameSessionAccessContext.java](../src/main/java/com/worldmap/game/common/application/GameSessionAccessContext.java)는 현재 request가 가진 소유권 정보를 record로 묶습니다.

- `memberId`
- `guestSessionKey`

그리고 `assertCanAccess(BaseGameSession session)`가 실제 접근 검사를 수행합니다.

### 5-4. `GameSubmissionGuard`

[GameSubmissionGuard.java](../src/main/java/com/worldmap/game/common/application/GameSubmissionGuard.java)는 아래 두 값을 비교해 stale submit을 막습니다.

- `actualStageId` vs `expectedStageId`
- `actualAttemptNumber` vs `expectedAttemptNumber`

메시지도 공통으로 고정돼 있습니다.

> 이미 처리된 제출이거나 최신 Stage 상태가 아닙니다. 화면을 새로고침해주세요.

## 6. 설계 구상

### 왜 공통 game layer가 필요한가

각 게임을 처음부터 독립 제품처럼 만들면 초반에는 빠를 수 있습니다.  
하지만 later stage에서 아래를 매번 다시 설명해야 합니다.

- 세션 생성
- current state
- answer 제출
- restart
- result 공개 규칙
- ownership 검사
- stale submit 방어

WorldMap은 이 중 "게임별로 다른 것"보다 "항상 같은 것"이 훨씬 많다고 보고 공통 layer를 먼저 만듭니다.

### 왜 session 공통 속성을 base class로 뺐는가

게임 세션마다 아래는 거의 같습니다.

- 누가 플레이하는가
- 현재 상태가 무엇인가
- 몇 라운드를 진행했는가
- 총점이 얼마인가
- 언제 시작했고 끝났는가

반면 아래는 게임마다 다릅니다.

- lives가 있는가
- 정답 입력 타입이 무엇인가
- stage entity가 어떤 필드를 가지는가

그래서 공통부는 base class로 빼고, 게임별 차이만 subclass에 둡니다.

### 왜 ownership을 UI 조건문이 아니라 도메인 계약으로 다루는가

어떤 세션에 접근 가능한지 프런트에서만 막으면 다음 문제가 남습니다.

- 직접 API 호출
- 다른 브라우저/다른 세션으로 접근
- stale 로그인 세션

그래서 WorldMap은 요청마다 access context를 만들고, 서비스에서 세션 엔티티와 직접 비교합니다.

### 왜 stale submit 방어를 공통 guard로 뺐는가

location, population, capital, flag, battle 모두 같은 리스크를 가집니다.

- 중복 클릭
- 네트워크 재전송
- 오래된 화면에서 제출

이 규칙을 게임마다 복붙하면 메시지와 행동이 쉽게 어긋납니다.  
그래서 guard를 공통화합니다.

## 7. 코드 설명

### 7-1. `BaseGameSession`: 한 판 전체 상태의 공통 규칙

[BaseGameSession.java](../src/main/java/com/worldmap/game/common/domain/BaseGameSession.java)에서 봐야 할 메서드는 아래입니다.

- `startGame(LocalDateTime startedAt)`
- `completeRound(...)`
- `advanceAfterSuccessfulRound(...)`
- `expandTotalRoundsTo(...)`
- `finish(GameSessionStatus terminalStatus, ...)`
- `resetForRestart(Integer totalRounds)`
- `claimOwnership(Long memberId)`

특히 중요한 점:

- `Persistable<UUID>`를 구현해 JPA new entity 판단을 직접 관리한다
- `@PostPersist`, `@PostLoad`에서 `newSession=false`로 바꾼다
- `claimOwnership`은 member id를 심고 guest key를 null로 만든다

즉, 공통 session은 later stage의 guest -> member 귀속 흐름까지 미리 품고 있습니다.

### 7-2. `LocationGameSession`, `PopulationGameSession`: 게임별 차이만 덧붙인다

[LocationGameSession.java](../src/main/java/com/worldmap/game/location/domain/LocationGameSession.java)와 [PopulationGameSession.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java)는 둘 다 `BaseGameSession`을 상속하고 `livesRemaining`만 추가합니다.

둘 다 비슷한 메서드를 가집니다.

- `planNextStage`
- `clearCurrentStage`
- `recordWrongAttempt`
- `restart`

이 similarity가 의미하는 바는 명확합니다.  
게임별 차이는 존재하지만, 공통 loop가 진짜로 재사용되고 있다는 뜻입니다.

### 7-3. `GameSessionAccessContextResolver`: request에서 ownership 문맥을 만든다

[GameSessionAccessContextResolver.java](../src/main/java/com/worldmap/auth/application/GameSessionAccessContextResolver.java)는 request를 읽어 아래 중 하나를 만듭니다.

- 로그인 member면 `memberId`
- 비로그인 guest면 `guestSessionKey`
- 세션도 없으면 anonymous

이 resolver가 필요한 이유는 controller가 직접 session attribute를 뒤적이지 않게 하기 위해서입니다.

### 7-4. `GuestSessionKeyManager`, `CurrentMemberAccessService`

[GuestSessionKeyManager.java](../src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java)는 현재 브라우저 guest 키를 생성/조회/회전합니다.

[CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)는 request/session에서 current member를 해석하고, DB의 현재 회원 상태와 다시 맞춥니다.

중요 포인트:

- request scope cache 사용
- session member가 DB에 없으면 sign out 처리
- 현재 persisted role/nickname으로 session을 sync

즉, access context는 단순 session 문자열 신뢰가 아니라 **현재 저장된 회원 상태를 반영한 문맥**입니다.

### 7-5. `GameSubmissionGuard`: stale submit 공통 방어

[GameSubmissionGuard.java](../src/main/java/com/worldmap/game/common/application/GameSubmissionGuard.java)는 매우 작지만 중요합니다.

later stage answer API는 보통 아래 값을 payload에 함께 받습니다.

- `stageId`
- `expectedAttemptNumber`

서비스는 현재 stage의 실제 값과 payload를 비교하고, mismatch면 `IllegalStateException`을 던집니다.  
그리고 [GlobalApiExceptionHandler.java](../src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java)가 이를 `409 CONFLICT`로 바꿉니다.

즉, stale submit 방어는 UI 한두 줄이 아니라 **공통 domain/application contract + 공통 error contract**의 조합입니다.

## 8. 요청 흐름 / 상태 변화

### 8-1. 세션 생성 전 ownership 문맥

```text
HTTP 요청
-> CurrentMemberAccessService.currentMember(request)
-> 로그인 member가 있으면 memberId 확보
-> 없으면 GuestSessionKeyManager.currentGuestSessionKey(session)
-> GameSessionAccessContextResolver.resolve(request)
-> GameSessionAccessContext(memberId, guestSessionKey)
```

### 8-2. 게임 answer 제출 흐름

```text
POST /api/games/{mode}/sessions/{id}/answer
-> controller
-> GameSessionAccessContextResolver.resolve(request)
-> service가 session 조회
-> accessContext.assertCanAccess(session)
-> GameSubmissionGuard.assertFreshSubmission(...)
-> stage/session 상태 전이
```

### 8-3. result 조회 흐름

```text
GET /api/games/{mode}/sessions/{id}
또는
GET /games/{mode}/result/{id}
-> service가 session 조회
-> ownership 검사
-> terminal 상태인지 확인
-> result view 조립
```

즉, later stage 모든 게임 흐름의 공통 틀은 이미 여기서 정해집니다.

## 9. 실패 케이스 / 예외 처리

- memberId/guestSessionKey가 현재 세션 owner와 다르면: `SessionAccessDeniedException` -> `403`
- sessionId가 없으면: `ResourceNotFoundException` -> `404`
- `expectedStageId`, `expectedAttemptNumber`가 현재와 다르면: `IllegalStateException` -> `409`
- `READY` 또는 종료된 세션에 answer를 제출하면: `IllegalStateException` -> `409`
- member session 정보가 깨졌거나 DB에 member row가 사라졌으면: `CurrentMemberAccessService`가 sign out 처리

이 공통 예외 규약이 있어야 later stage 글에서 "왜 어떤 API가 403이고 어떤 API가 409인가"를 일관되게 설명할 수 있습니다.

## 10. 테스트로 검증하기

공통 contract를 잡는 핵심 테스트는 아래입니다.

### 10-1. [GameSessionAccessContextTest.java](../src/test/java/com/worldmap/game/common/application/GameSessionAccessContextTest.java)

- member owner는 접근 가능
- guest owner는 접근 가능
- 다른 guest key는 거부

### 10-2. [GameSessionAccessContextResolverTest.java](../src/test/java/com/worldmap/auth/application/GameSessionAccessContextResolverTest.java)

- current member가 있으면 member context로 해석
- anonymous guest session이면 guest key context로 해석
- 세션이 없으면 anonymous context

### 10-3. [MemberSessionManagerTest.java](../src/test/java/com/worldmap/auth/application/MemberSessionManagerTest.java)

- 로그인 시 session id 회전
- member id / nickname / role session attribute 저장

### 10-4. 대표 game flow test

- [LocationGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java)
- [PopulationGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java)

이 두 테스트는 stale duplicate wrong answer, duplicate correct answer after stage advance를 실제로 `409`로 막는 흐름까지 검증합니다.
다만 이 조합이 모든 게임 모드의 모든 answer path를 한 번에 자동 증명하는 것은 아닙니다.
현재는 location, population 대표 흐름을 통해 공통 contract가 재사용된다는 사실을 고정하고,
capital, flag, population-battle 쪽 증명은 각 게임 글과 개별 flow test에서 이어서 닫습니다.

실행 명령:

```bash
./gradlew test \
  --tests com.worldmap.game.common.application.GameSessionAccessContextTest \
  --tests com.worldmap.auth.application.GameSessionAccessContextResolverTest \
  --tests com.worldmap.auth.application.MemberSessionManagerTest \
  --tests com.worldmap.game.location.LocationGameFlowIntegrationTest \
  --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest
```

## 11. 회고

공통 game contract는 눈에 띄는 기능이 아니어서 생략하기 쉽지만, 이 층이 없으면 later stage의 모든 게임 글이 중복 설명으로 무너집니다.

이 구조의 장점:

- 모든 게임이 같은 session/status/ownership 언어를 쓴다
- 새로운 게임을 추가할 때 설계가 덜 흔들린다
- hardening 규칙을 공통으로 강화할 수 있다
- auth와 game 사이의 브리지도 재사용된다

### 현재 구현의 한계

- stage/attempt 자체는 게임별 엔티티로 남아 있어 완전한 공통화는 아니다
- 공통 game abstraction이 너무 많아지면 오히려 이해가 어려워질 수 있으므로, 현재는 session/access/guard 정도만 공통화했다
- stale submit과 ownership contract는 공통 guard/source를 공유하지만, 각 게임 서비스가 실제로 그 contract를 어떻게 적용하는지는 개별 flow test와 service 구현을 같이 봐야 한다

## 12. 취업 포인트

### 12-1. 1문장 답변

WorldMap은 각 게임을 독립 서비스처럼 만들지 않고 `BaseGameSession`, ownership context, stale submit guard를 공통 계약으로 먼저 만든 뒤, 게임별 규칙만 variant로 추가하는 구조를 택했습니다.

### 12-2. 30초 답변

이 프로젝트에서 중요한 건 게임이 여러 개라는 사실보다, 모든 게임이 같은 플랫폼 계약을 따른다는 점입니다. `BaseGameSession`이 player, owner, score, round, terminal status를 공통으로 제공하고, `GameSessionAccessContext`와 resolver가 현재 브라우저의 member/guest ownership을 해석합니다. answer API는 공통 `GameSubmissionGuard`로 stale submit을 막고, 각 게임은 lives, stage, scoring 같은 차이만 subclass와 서비스에 추가합니다. 그래서 later stage에서 capital, flag, population-battle을 추가해도 같은 언어로 설명할 수 있습니다.

### 12-3. 예상 꼬리 질문

- 왜 모든 게임을 완전히 독립적으로 만들지 않았나요?
- 왜 stage/attempt는 공통화하지 않고 session만 공통화했나요?
- stale submit 방어를 왜 공통 guard로 뺐나요?
- ownership 검사를 왜 프런트가 아니라 서비스에서 하나요?

## 13. 시작 상태

- country seed와 baseline은 있지만, 게임이 공통으로 기대는 platform contract가 없다
- 나중에 여러 게임을 추가하면 세션/권한/재시작/결과 규칙이 쉽게 분산될 수 있다

## 14. 이번 글에서 바뀌는 파일

- `src/main/java/com/worldmap/game/common/domain/**`
- `src/main/java/com/worldmap/game/common/application/**`
- `src/main/java/com/worldmap/auth/application/GameSessionAccessContextResolver.java`
- `src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java`
- `src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java`
- 각 게임의 `*GameSession` subclass
- 관련 공통/통합 테스트

## 15. 구현 체크리스트

1. `game/common/domain`에 `BaseGameSession`, `GameSessionStatus`를 만든다
2. `game/common/application`에 access context와 submission guard를 만든다
3. auth 쪽에 resolver와 guest key manager를 만든다
4. 각 게임 session이 base session을 상속하게 한다
5. 각 게임 service가 session 조회 뒤 ownership 검사를 수행하게 한다
6. answer API payload에 stage/attempt freshness 정보를 포함하게 한다
7. stale submit을 `409`로 고정한다
8. 공통 + 대표 게임 flow 테스트를 추가한다

## 16. 실행 / 검증 명령

```bash
./gradlew test \
  --tests com.worldmap.game.common.application.GameSessionAccessContextTest \
  --tests com.worldmap.auth.application.GameSessionAccessContextResolverTest \
  --tests com.worldmap.auth.application.MemberSessionManagerTest \
  --tests com.worldmap.game.location.LocationGameFlowIntegrationTest \
  --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest
```

## 17. 산출물 체크리스트

- 모든 게임 세션이 공통 session 상태를 가진다
- member/guest ownership을 같은 규칙으로 검사한다
- stale submit이 공통 메시지와 `409`로 막힌다
- later stage 게임들이 같은 패키지 구조를 따른다

## 18. 글 종료 체크포인트

- 왜 game package는 `application / domain / web`으로 나뉘어야 하는가
- 왜 session 공통 계약이 먼저 있어야 하는가
- 왜 ownership과 stale submit은 게임별 구현이 아니라 공통 계약이어야 하는가
- later stage vertical slice들이 이 글을 어떻게 재사용하는가

## 19. 자주 막히는 지점

- 새 게임을 만들 때 session 구조부터 새로 짜는 것
- ownership 검사를 controller나 프런트 조건문 수준으로만 처리하는 것
- stale submit 방어를 "프런트에서 버튼 disable했으니 됐다"로 생각하는 것
- 공통 abstraction을 너무 많이 만들어 오히려 각 게임 규칙까지 숨겨 버리는 것
