# [Spring Boot 게임 플랫폼 포트폴리오] 11. guest session ownership과 progress claim을 어떻게 설계했는가

## 1. 이번 글에서 풀 문제

WorldMap은 로그인부터 강제하지 않습니다.

홈에 들어오자마자 바로 아래 흐름이 열려 있어야 합니다.

1. 위치 게임 시작
2. 인구수 게임 시작
3. 수도 게임 시작
4. 인구 비교 퀵 배틀 시작
5. 국기 게임 시작

문제는 여기서 바로 생깁니다.

- 비회원이 플레이한 기록은 누구 소유인가
- 같은 브라우저에서 게임을 여러 개 시작하면 그 기록을 어떻게 하나의 흐름으로 설명할 것인가
- 회원가입하거나 로그인했을 때 비회원 기록을 어떤 기준으로 계정에 붙일 것인가
- 잘못하면 다른 브라우저의 비회원 기록까지 가져오지 않는가
- 로그아웃하거나 회원이 삭제된 뒤에는 어떤 상태로 돌아가야 하는가

이 글은 이 문제를 `guestSessionKey -> memberId` 승격 구조로 푸는 방법을 다룹니다.

핵심은 두 가지입니다.

- guest를 허용하되 ownership은 절대 비워 두지 않는다
- claim은 "현재 브라우저의 기록만" member ownership으로 승격한다

이 글을 다 읽으면 현재 저장소 기준으로 아래를 다시 구현할 수 있어야 합니다.

- 비회원도 5개 게임을 즉시 시작하는 구조
- 브라우저 세션 단위 guest identity 생성 방식
- 모든 게임 세션과 leaderboard row에 ownership을 남기는 방식
- signup/login 직후 current browser guest 기록을 member ownership으로 옮기는 방식
- logout과 stale member fallback 이후 다시 guest 상태로 돌아가는 방식

## 2. 최종 도착 상태

현재 코드베이스의 최종 도착 상태를 먼저 고정하겠습니다.

### 2-1. guest 사용자는 계정 없이도 5개 게임을 바로 시작한다

비회원은 `/api/games/*/sessions`를 호출하면 닉네임과 함께 게임을 시작할 수 있습니다.

이때 서버는 아래 둘 중 하나를 owner로 저장합니다.

- 로그인된 회원이면 `memberId`
- 비회원이면 `guestSessionKey`

owner가 전혀 없는 row는 허용하지 않습니다.

### 2-2. 같은 브라우저 세션은 같은 guestSessionKey를 공유한다

현재 브라우저에서 위치, 인구수, 수도, 배틀, 국기 게임을 순서대로 시작해도 모두 같은 `guestSessionKey`를 사용합니다.

즉 guest identity는 "게임별 임시 값"이 아니라 "현재 브라우저 session이 가진 비회원 owner"입니다.

### 2-3. signup/login 성공 직후 현재 브라우저의 guest 기록만 claim한다

회원가입이나 로그인에 성공하면 [AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)가 현재 세션의 guest key를 읽고, [GuestProgressClaimService.java](../src/main/java/com/worldmap/auth/application/GuestProgressClaimService.java)로 아래 row를 한 번에 member ownership으로 옮깁니다.

- 5개 게임 session row
- leaderboard row

옮길 때 바꾸는 값은 owner뿐입니다.

- `memberId`는 채운다
- `guestSessionKey`는 null로 비운다
- `playerNickname` 같은 플레이 당시 표시 값은 유지한다

### 2-4. 로그인 후 새 게임은 guest가 아니라 member ownership으로 시작한다

로그인된 상태에서 게임 시작 API를 호출하면 더 이상 `guestSessionKey`를 쓰지 않고 `startMemberGame(...)`을 타게 됩니다.

즉 "claim된 과거 guest 기록"과 "앞으로 생길 member 기록"이 같은 `memberId` 아래로 모입니다.

### 2-5. logout 이후에는 새 guest boundary가 생긴다

`POST /logout`은 단순히 member session attribute만 지우지 않습니다.
[GuestSessionKeyManager.java](../src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java)의 `rotateGuestSessionKey(...)`를 호출해서 **새 guest key**를 부여합니다.

그래서 로그인 전 guest 기록과, 로그아웃 뒤 새로 쌓을 guest 기록이 같은 owner로 이어지지 않습니다.

### 2-6. 삭제되거나 깨진 member session은 guest로 되돌린다

현재 세션에 `memberId`, `nickname`, `role` attribute가 남아 있어도, DB에 그 회원 row가 없거나 role 값이 깨져 있으면 [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)가 세션을 정리합니다.

즉 stale session은 "반쯤 로그인된 상태"로 남지 않고 다시 guest처럼 동작합니다.

이 흐름까지 포함해야 현재 저장소의 ownership 모델을 정확히 설명할 수 있습니다.

## 3. 시작 상태

이 글을 쓰기 전 기준으로는 보통 아래 둘 중 하나의 잘못된 모델로 흐르기 쉽습니다.

### 3-1. guest를 완전 익명으로 두는 모델

이 방식은 구현은 쉬워 보입니다.

- 그냥 세션만 하나 만들고
- DB row에는 owner를 남기지 않거나
- playerNickname만 대충 남기면 되기 때문입니다

하지만 이 모델은 곧바로 한계에 부딪힙니다.

- 같은 브라우저에서 여러 게임을 이어서 했다는 설명이 불가능해진다
- 로그인 후 "이전 기록 이어받기"를 설명할 수 없다
- `/mypage`, `/ranking`, `/stats`, `/dashboard`에서 owner별 집계가 흐려진다
- 나중에 access control을 붙이려 해도 기초 identity가 없다

### 3-2. guest 기록을 member 기록과 섞어 저장하는 모델

예를 들어 "로그인 성공 뒤 앞으로는 member로 저장하면 되지 않나"라고 생각할 수 있습니다.

문제는 이미 guest로 플레이한 과거 기록입니다.

- 그 기록은 계정에 붙지 않는다
- 사용자는 로그인했는데도 이전 플레이가 사라진 것처럼 느낀다
- 포트폴리오 설명에서도 "게스트에서 회원으로 넘어가는 ownership migration"을 말할 수 없다

WorldMap은 이 두 문제를 피하려고 guest identity를 명시적으로 만들고, claim도 명시적으로 수행합니다.

## 4. 먼저 알아둘 개념

### 4-1. guestSessionKey

`guestSessionKey`는 비회원 브라우저를 식별하는 현재 `HttpSession` 기반 키입니다.

중요한 점은 두 가지입니다.

- DB PK가 아니라 session attribute다
- 로그인 전 identity를 표현하는 값이지, 영구 계정이 아니다

현재 구현에서 attribute 이름은 아래 상수로 고정됩니다.

- [GuestSessionKeyManager.java](../src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java)
  - `WORLDMAP_GUEST_SESSION_KEY`

### 4-2. member session

로그인 뒤에는 guest key가 아니라 아래 세 attribute가 현재 브라우저의 member identity가 됩니다.

- `WORLDMAP_MEMBER_ID`
- `WORLDMAP_MEMBER_NICKNAME`
- `WORLDMAP_MEMBER_ROLE`

이 계약은 [MemberSessionManager.java](../src/main/java/com/worldmap/auth/application/MemberSessionManager.java)가 관리합니다.

### 4-3. ownership

이 글에서 ownership은 UI 상태가 아닙니다.
실제 DB row가 누구 소유인지 나타내는 서버 데이터입니다.

WorldMap은 모든 플레이 기록이 아래 둘 중 하나를 갖도록 설계합니다.

- `memberId`
- `guestSessionKey`

즉 "비회원"과 "무소유"를 분리합니다.

### 4-4. claim

claim은 guest 기록을 member 기록으로 **복사**하는 것이 아니라 **소유권을 이전**하는 작업입니다.

현재 구현에서 claim은 아래처럼 동작합니다.

- `memberId`를 채운다
- 기존 `guestSessionKey`를 null로 비운다
- 기록 내용은 유지한다

이 때문에 `/mypage`나 랭킹에서 "예전 guest 플레이가 내 계정 기록으로 이어졌다"라고 설명할 수 있습니다.

### 4-5. browser-bound continuity

claim은 "전체 guest 기록"을 가져오지 않습니다.
오직 **현재 브라우저 세션이 가진 guestSessionKey**와 연결된 row만 옮깁니다.

이 제약은 의도적인 제품 정책입니다.

- 다른 브라우저의 익명 기록을 잘못 가져오지 않기 위해서
- 교차 디바이스 병합 문제를 현재 스코프 밖으로 밀어내기 위해서
- 포트폴리오 기준에서 설명 가능한 ownership 모델을 유지하기 위해서

### 4-6. stale member fallback

현재 세션에 member attribute가 남아 있어도, 실제 DB member row가 사라졌거나 role 값이 깨져 있으면 그 세션을 그대로 믿지 않습니다.

[CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)가 현재 회원 row를 다시 읽고, 없으면 세션을 정리해 guest처럼 되돌립니다.

이건 auth hardening 글의 주제이기도 하지만, ownership 관점에서도 중요합니다.
왜냐하면 "이 브라우저가 guest owner로 시작할지, member owner로 시작할지"를 최종적으로 결정하기 때문입니다.

## 5. 이번 글에서 다룰 파일

이 글을 실제로 다시 구현하려면 아래 파일들을 같이 봐야 합니다.

### 5-1. 핵심 애플리케이션 서비스와 컨트롤러

- [GuestSessionKeyManager.java](../src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java)
- [GuestProgressClaimService.java](../src/main/java/com/worldmap/auth/application/GuestProgressClaimService.java)
- [MemberSessionManager.java](../src/main/java/com/worldmap/auth/application/MemberSessionManager.java)
- [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)
- [MemberAuthService.java](../src/main/java/com/worldmap/auth/application/MemberAuthService.java)
- [AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)

### 5-2. ownership가 실제로 저장되는 도메인

- [BaseGameSession.java](../src/main/java/com/worldmap/game/common/domain/BaseGameSession.java)
- [LeaderboardRecord.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java)

### 5-3. guest game start가 실제로 열리는 API 진입점

- [LocationGameApiController.java](../src/main/java/com/worldmap/game/location/web/LocationGameApiController.java)
- [PopulationGameApiController.java](../src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java)
- [CapitalGameApiController.java](../src/main/java/com/worldmap/game/capital/web/CapitalGameApiController.java)
- [PopulationBattleGameApiController.java](../src/main/java/com/worldmap/game/populationbattle/web/PopulationBattleGameApiController.java)
- [FlagGameApiController.java](../src/main/java/com/worldmap/game/flag/web/FlagGameApiController.java)

### 5-4. claim 대상이 되는 저장소

- [LocationGameSessionRepository.java](../src/main/java/com/worldmap/game/location/domain/LocationGameSessionRepository.java)
- [PopulationGameSessionRepository.java](../src/main/java/com/worldmap/game/population/domain/PopulationGameSessionRepository.java)
- [CapitalGameSessionRepository.java](../src/main/java/com/worldmap/game/capital/domain/CapitalGameSessionRepository.java)
- [PopulationBattleGameSessionRepository.java](../src/main/java/com/worldmap/game/populationbattle/domain/PopulationBattleGameSessionRepository.java)
- [FlagGameSessionRepository.java](../src/main/java/com/worldmap/game/flag/domain/FlagGameSessionRepository.java)
- [LeaderboardRecordRepository.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java)

### 5-5. 이 구조를 고정하는 테스트

- [GuestSessionOwnershipIntegrationTest.java](../src/test/java/com/worldmap/auth/GuestSessionOwnershipIntegrationTest.java)
- [AuthFlowIntegrationTest.java](../src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java)
- [CurrentMemberAccessServiceTest.java](../src/test/java/com/worldmap/auth/application/CurrentMemberAccessServiceTest.java)
- [MemberSessionManagerTest.java](../src/test/java/com/worldmap/auth/application/MemberSessionManagerTest.java)

## 6. 핵심 상태를 표로 먼저 고정하기

이 글은 DB 테이블보다 먼저 **ownership state**를 이해해야 합니다.

### 6-1. 현재 브라우저의 identity 상태

| 상태 | session attribute | 의미 |
| --- | --- | --- |
| guest before first play | 없음 | 아직 guest key도 member session도 없는 브라우저 |
| active guest | `WORLDMAP_GUEST_SESSION_KEY` | 비회원이지만 현재 브라우저 owner는 있는 상태 |
| active member | `WORLDMAP_MEMBER_ID`, `WORLDMAP_MEMBER_NICKNAME`, `WORLDMAP_MEMBER_ROLE` | 로그인된 계정 플레이 상태 |
| stale member | member attribute는 있으나 DB member 없음 또는 role 값 깨짐 | `CurrentMemberAccessService`가 정리 후 guest처럼 동작해야 하는 상태 |

중요한 포인트는 첫 줄입니다.

WorldMap은 홈 진입 즉시 guest key를 발급하지 않습니다.
현재 guest key는 **비회원이 실제로 게임 시작 API를 칠 때** 만들어집니다.

즉 "첫 방문 브라우저"와 "플레이를 시작한 guest 브라우저"는 구분됩니다.

### 6-2. DB row의 ownership 상태

| row 종류 | guest 상태 | member 상태 |
| --- | --- | --- |
| `*_game_session` | `memberId = null`, `guestSessionKey != null` | `memberId != null`, `guestSessionKey = null` |
| `leaderboard_record` | `memberId = null`, `guestSessionKey != null` | `memberId != null`, `guestSessionKey = null` |

현재 모델은 아래 둘을 항상 만족하려고 합니다.

1. guest 기록도 owner가 있다
2. member로 claim된 기록은 guest owner를 끌고 가지 않는다

### 6-3. claim 전후 상태 변화

| 시점 | `memberId` | `guestSessionKey` | `playerNickname` |
| --- | --- | --- | --- |
| guest 플레이 직후 | `null` | `guest-...` | guest가 입력한 닉네임 |
| claim 직후 | member PK | `null` | 그대로 유지 |

여기서 `playerNickname`을 유지하는 이유도 설명할 수 있어야 합니다.

WorldMap은 "플레이 당시 보였던 이름"과 "현재 owner"를 다르게 관리합니다.

- owner는 기록 귀속의 기준
- `playerNickname`은 당시 보드/결과에 노출된 표시 이름

그래서 claim 뒤에도 기존 row의 `playerNickname`이 꼭 member의 현재 닉네임으로 바뀌어야 하는 것은 아닙니다.

## 7. 왜 이런 설계를 택했는가

### 7-1. 왜 guest 플레이를 막지 않았는가

이 서비스의 첫 가치 제안은 "지금 바로 플레이"입니다.

만약 로그인부터 강제하면 아래가 약해집니다.

- 홈에서 게임과 추천을 빠르게 체험시키는 데 실패한다
- 면접에서도 제품 진입 흐름이 무거워진다
- 랭킹/게임/추천이 중심인 서비스인데 auth가 먼저 보인다

그래서 WorldMap은 회원 기능을 community가 아니라 **기록 유지 기능**으로 둡니다.

### 7-2. 왜 guest를 무소유 상태로 두지 않았는가

guest는 계정이 없을 뿐이지, ownership이 없는 것이 아닙니다.

guest를 무소유로 두면 다음이 무너집니다.

- 같은 브라우저에서 여러 게임을 했다는 연속성
- guest 결과와 leaderboard row의 추적 가능성
- 로그인 뒤 claim 스토리
- 나중에 access control을 붙일 기준

그래서 "익명"과 "무소유"를 분리했습니다.

### 7-3. 왜 claim을 로그인 성공 직후 수행했는가

claim을 나중에 수동 버튼으로 빼면 흐름이 복잡해집니다.

- 사용자가 잊을 수 있다
- 어떤 기록이 아직 guest인지 설명이 어려워진다
- `/mypage`와 랭킹에서 일시적으로 비일관한 상태가 생긴다

로그인/회원가입 직후 현재 guest key를 읽고 한 번에 claim하면 아래가 단순해집니다.

- "로그인 전 기록이 로그인 후에도 이어진다"를 한 문장으로 설명 가능
- claim 실패와 session 전환을 같은 request 안에서 다룸
- 현재 브라우저 기준 continuity를 가장 자연스럽게 제공

### 7-4. 왜 cross-device merge를 하지 않았는가

guest identity는 브라우저 세션에 묶여 있습니다.

이 제약은 단점이 아니라 현재 프로젝트 범위를 안정화하는 장치입니다.

- 다른 디바이스의 guest 기록을 안전하게 연결할 증거가 없다
- 이메일이나 외부 식별자도 없다
- 억지로 global merge를 넣으면 데이터 오염 가능성이 커진다

따라서 현재 스코프에서는 **same-browser continuity**까지만 책임집니다.

## 8. guestSessionKey lifecycle

### 8-1. key 생성 시점

[GuestSessionKeyManager.java](../src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java)는 세 가지 메서드만 가집니다.

- `ensureGuestSessionKey(HttpSession)`
- `currentGuestSessionKey(HttpSession)`
- `rotateGuestSessionKey(HttpSession)`

핵심은 `ensure`와 `current`를 분리한 점입니다.

- `ensure`는 없으면 만든다
- `current`는 읽기만 하고 없으면 빈 값을 돌려준다

이 구분이 중요한 이유는 아래와 같습니다.

- game start API는 비회원이 실제 플레이를 시작하는 지점이므로 `ensure`를 써야 한다
- signup/login은 이미 있던 guest identity를 읽어 claim할지 말지만 결정하면 되므로 `current`를 써야 한다

### 8-2. key 생성 규칙

현재 생성 규칙은 아주 단순합니다.

- prefix: `guest-`
- body: `UUID.randomUUID()`

즉 예시는 아래 형태입니다.

```text
guest-550e8400-e29b-41d4-a716-446655440000
```

이 키는 암호학적 토큰이 아니라 guest ownership 구분자입니다.
현재 브라우저 세션 안에서 row owner를 이어 주는 역할만 하면 충분합니다.

### 8-3. 같은 브라우저에서 재사용되는 이유

`ensureGuestSessionKey(...)`는 아래 규칙으로 동작합니다.

1. session attribute에 기존 값이 있으면 그대로 재사용
2. 없거나 blank면 새 key 생성
3. session attribute에 다시 저장

이 때문에 같은 브라우저 세션에서 여러 게임을 시작해도 owner가 계속 이어집니다.

이 계약을 실제로 고정하는 테스트가 [GuestSessionOwnershipIntegrationTest.java](../src/test/java/com/worldmap/auth/GuestSessionOwnershipIntegrationTest.java)의 `sameBrowserSessionSharesGuestSessionKeyAcrossGameModes`입니다.

### 8-4. logout에서 rotate를 따로 두는 이유

로그아웃은 `ensure`가 아니라 `rotate`를 씁니다.

이유는 분명합니다.

- 이전 guest key를 그대로 재사용하면
- 로그인 전 guest 기록과 로그아웃 뒤 새 guest 기록이
- 같은 owner 아래로 다시 이어질 수 있기 때문입니다

즉 logout은 단순히 member attribute만 지우는 작업이 아니라 **새 guest boundary를 여는 작업**입니다.

## 9. ownership이 실제로 저장되는 곳

guest ownership 글이라고 해서 auth 클래스만 보면 안 됩니다.
실제 owner가 남는 row를 봐야 합니다.

### 9-1. 모든 게임 session은 `BaseGameSession`에서 owner 필드를 공유한다

[BaseGameSession.java](../src/main/java/com/worldmap/game/common/domain/BaseGameSession.java)는 다섯 게임 session이 공통으로 상속하는 mapped superclass입니다.

여기에는 아래 필드가 들어 있습니다.

- `playerNickname`
- `memberId`
- `guestSessionKey`

즉 ownership 모델은 위치 게임만의 구현이 아닙니다.
다섯 게임이 모두 같은 owner contract를 공유합니다.

### 9-2. claim은 세션 row에서 어떻게 동작하는가

`BaseGameSession.claimOwnership(Long memberId)`는 단순하지만 중요합니다.

```text
memberId를 채운다
guestSessionKey를 null로 만든다
```

이 메서드가 domain에 있어야 하는 이유는, claim이 "화면 전환"이 아니라 **session row의 상태 전이 규칙**이기 때문입니다.

컨트롤러가 `setMemberId`, `setGuestSessionKey(null)`를 흩뿌리면 다섯 게임에서 같은 규칙을 보장하기 어렵습니다.

### 9-3. leaderboard row도 같은 owner contract를 가진다

[LeaderboardRecord.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java)도 아래 필드를 가집니다.

- `memberId`
- `guestSessionKey`
- `playerNickname`

그리고 마찬가지로 `claimOwnership(Long memberId)`를 가집니다.

즉 claim 범위는 "게임 세션만"이 아닙니다.
랭킹 보드에 이미 반영된 guest run도 같은 시점에 member ownership으로 승격됩니다.

### 9-4. 왜 session과 leaderboard 둘 다 claim해야 하는가

둘 중 하나라도 빠지면 설명이 깨집니다.

- game session만 claim하고 leaderboard를 안 옮기면 랭킹 귀속이 어긋난다
- leaderboard만 claim하고 session을 안 옮기면 `/mypage` 최근 플레이가 어긋난다

그래서 현재 구현은 둘을 같은 service에서 함께 claim합니다.

## 10. guest 게임 시작 흐름

이제 실제 요청이 어디서 시작되고 어디서 상태가 바뀌는지 보겠습니다.

### 10-1. guest start API는 다섯 게임 모두 같은 패턴을 쓴다

| 게임 | 시작 API | controller | service 진입 |
| --- | --- | --- | --- |
| 위치 | `POST /api/games/location/sessions` | [LocationGameApiController.java](../src/main/java/com/worldmap/game/location/web/LocationGameApiController.java) | `startGuestGame(...)` 또는 `startMemberGame(...)` |
| 인구수 | `POST /api/games/population/sessions` | [PopulationGameApiController.java](../src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java) | `startGuestGame(...)` 또는 `startMemberGame(...)` |
| 수도 | `POST /api/games/capital/sessions` | [CapitalGameApiController.java](../src/main/java/com/worldmap/game/capital/web/CapitalGameApiController.java) | `startGuestGame(...)` 또는 `startMemberGame(...)` |
| 배틀 | `POST /api/games/population-battle/sessions` | [PopulationBattleGameApiController.java](../src/main/java/com/worldmap/game/populationbattle/web/PopulationBattleGameApiController.java) | `startGuestGame(...)` 또는 `startMemberGame(...)` |
| 국기 | `POST /api/games/flag/sessions` | [FlagGameApiController.java](../src/main/java/com/worldmap/game/flag/web/FlagGameApiController.java) | `startGuestGame(...)` 또는 `startMemberGame(...)` |

현재 패턴은 다 똑같습니다.

1. `CurrentMemberAccessService.currentMember(request)`로 현재 member 여부를 먼저 확인
2. member가 있으면 `startMemberGame(memberId, nickname)`
3. member가 없으면 `guestSessionKeyManager.ensureGuestSessionKey(session)`
4. `startGuestGame(nickname, guestSessionKey)`

### 10-2. 왜 controller는 분기만 하고 ownership 저장은 service/domain에 맡기는가

controller의 책임은 현재 request가 member인지 guest인지만 결정하는 것입니다.

실제 상태 변화는 service와 domain이 맡아야 합니다.

- service는 session row를 생성한다
- domain session은 `memberId` 또는 `guestSessionKey`를 들고 생성된다

즉 "이 브라우저가 guest인가 member인가"는 controller가 보고,
"그 owner를 가진 게임 row를 어떻게 만든다"는 service/domain이 책임집니다.

### 10-3. 위치 게임을 예로 보면 흐름이 더 명확하다

[LocationGameApiController.java](../src/main/java/com/worldmap/game/location/web/LocationGameApiController.java)의 `start(...)`는 아래 순서로 동작합니다.

```text
POST /api/games/location/sessions
-> CurrentMemberAccessService.currentMember(request)
-> member면 startMemberGame(memberId, nickname)
-> 아니면 ensureGuestSessionKey(session)
-> startGuestGame(request.nickname, guestSessionKey)
```

[LocationGameService.java](../src/main/java/com/worldmap/game/location/application/LocationGameService.java)의 `startGuestGame(...)`는 다시 아래를 수행합니다.

```text
normalizeNickname
-> LocationGameSession.ready(playerNickname, null, guestSessionKey, 1)
-> save(session)
-> createNextStage(...)
-> startGame(now)
```

즉 guest start에서 실제로 상태가 바뀌는 지점은 service/domain입니다.

### 10-4. 이 흐름이 왜 중요한가

이 구조 덕분에 모든 게임이 같은 설명을 공유합니다.

- member면 member owner로 시작
- 아니면 guest owner로 시작

게임별로 달라지는 것은 문제 생성 로직뿐입니다.
ownership 모델은 공통 계약으로 유지됩니다.

## 11. guest run이 leaderboard까지 이어지는 흐름

guest ownership은 start에서 끝나지 않습니다.
게임이 끝났을 때 leaderboard row에도 같은 owner가 남아야 합니다.

### 11-1. guest game 종료 시 leaderboard row도 guest owner를 가진다

[GuestSessionOwnershipIntegrationTest.java](../src/test/java/com/worldmap/auth/GuestSessionOwnershipIntegrationTest.java)의 `leaderboardRecordKeepsGuestOwnershipWhenGuestGameEnds`는 이 계약을 고정합니다.

테스트가 하는 일은 단순합니다.

1. guest browser session으로 위치 게임 시작
2. 같은 stage에서 세 번 틀려 `GAME_OVER`
3. 생성된 `LeaderboardRecord`를 조회

검증은 아래입니다.

- `memberId == null`
- `guestSessionKey == locationSession.getGuestSessionKey()`
- `playerNickname == "guest-ranker"`

즉 guest ownership은 leaderboard에서도 유지됩니다.

### 11-2. 왜 leaderboard까지 owner를 남겨야 하는가

랭킹은 단순 읽기 화면이 아닙니다.
나중에 claim 대상이 되기 때문입니다.

만약 guest run이 leaderboard에서 owner를 잃어버리면 다음 단계가 불가능해집니다.

- login 뒤 이 row를 member로 claim할 수 없다
- `/mypage`와 `/ranking`의 ownership 세계가 분리된다

따라서 guest ownership 설계는 game session과 leaderboard를 따로 보면 안 됩니다.

## 12. signup 직후 claim 흐름

이제 핵심인 claim으로 들어갑니다.

### 12-1. signup request에서 실제로 일어나는 순서

[AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)의 `POST /signup`은 아래 순서로 동작합니다.

```text
POST /signup
-> MemberAuthService.signUp(nickname, password)
-> GuestSessionKeyManager.currentGuestSessionKey(httpSession)
-> 있으면 GuestProgressClaimService.claimGuestRecords(memberId, guestSessionKey)
-> MemberSessionManager.signIn(request, member)
-> redirect:/mypage
```

순서를 일부러 자세히 적는 이유가 있습니다.

현재 구현은 **claim을 먼저 하고 signIn을 나중에** 합니다.

### 12-2. 왜 signIn 전에 current guest key를 읽는가

`signIn(...)`은 `request.changeSessionId()`를 호출합니다.

이건 session fixation을 줄이기 위한 안전장치입니다.

현재 servlet session 모델에서는 `changeSessionId()`가 attribute를 유지하지만, 코드를 읽는 사람 입장에서 가장 안전한 설명은 아래입니다.

1. 로그인 전 브라우저가 들고 있던 guest identity를 먼저 읽는다
2. 그 identity에 걸린 row를 claim한다
3. 그 다음 session id를 회전시키고 member session을 심는다

즉 ownership migration과 session fixation 대응이 한 request 안에서 명확히 분리됩니다.

### 12-3. signup에서 claim 대상이 되는 row

[GuestProgressClaimService.java](../src/main/java/com/worldmap/auth/application/GuestProgressClaimService.java)는 아래 순서로 현재 guest key에 묶인 row를 모두 찾습니다.

1. location session
2. population session
3. capital session
4. flag session
5. population-battle session
6. leaderboard record

그리고 각각 `claimOwnership(memberId)`를 호출합니다.

즉 signup은 account row를 만드는 것에서 끝나지 않고, current browser guest history를 **current account history**로 승격합니다.

### 12-4. claim 대상 조회가 `memberId is null` 조건을 갖는 이유

모든 repository 조회는 아래 패턴을 씁니다.

```text
findAllByGuestSessionKeyAndMemberIdIsNull(guestSessionKey)
```

이 조건이 중요한 이유는 이미 claim된 row를 다시 건드리지 않기 위해서입니다.

즉 claim은 "guest owner가 아직 남아 있는 row"에만 작동합니다.

### 12-5. signup claim을 실제로 고정하는 테스트

[AuthFlowIntegrationTest.java](../src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java)의 `signupClaimsCurrentGuestRecordsIntoMemberOwnership`가 이 흐름을 가장 잘 보여 줍니다.

테스트는 아래를 수행합니다.

1. guest browser session으로 다섯 게임을 모두 시작
2. 위치 게임을 끝내 leaderboard row까지 하나 만들기
3. 같은 browser session으로 `/signup`
4. 모든 session row와 leaderboard row를 다시 조회

검증 포인트는 아래입니다.

- 5개 게임 session 모두 `memberId == 새 회원 id`
- 5개 게임 session 모두 `guestSessionKey == null`
- leaderboard row도 `memberId == 새 회원 id`
- leaderboard row도 `guestSessionKey == null`
- 각 row의 `playerNickname`은 guest 당시 값 그대로 유지

즉 claim은 owner만 옮기고 플레이 기록의 의미는 바꾸지 않습니다.

## 13. login 직후 claim 흐름

signup과 login은 "회원 row를 만드는가"만 다르고, guest ownership을 member ownership으로 승격하는 흐름은 거의 같습니다.

### 13-1. login request 순서

[AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)의 `POST /login`은 아래 순서입니다.

```text
POST /login
-> MemberAuthService.login(nickname, password)
-> GuestSessionKeyManager.currentGuestSessionKey(httpSession)
-> 있으면 GuestProgressClaimService.claimGuestRecords(memberId, guestSessionKey)
-> MemberSessionManager.signIn(request, member)
-> redirect:resolvePostLoginRedirect(returnTo)
```

### 13-2. `returnTo`가 있어도 claim 순서는 바뀌지 않는다

admin login처럼 `returnTo=/dashboard`가 붙는 경우에도 claim과 signIn 순서는 같습니다.

즉 redirect 목적지가 `/mypage`든 `/dashboard`든, ownership migration은 인증 직후 같은 방식으로 처리됩니다.

### 13-3. login claim을 고정하는 테스트

[AuthFlowIntegrationTest.java](../src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java)의 `loginClaimsCurrentGuestRecordsIntoMemberOwnership`는 기존 회원 계정이 있는 상태에서 login을 했을 때도 다섯 게임 row가 모두 member ownership으로 승격되는지 확인합니다.

검증 포인트는 signup 때와 같습니다.

- 5개 게임 session 모두 `memberId` 채워짐
- 5개 게임 session 모두 `guestSessionKey` 비워짐
- guest 당시 닉네임은 session row에 그대로 남음

### 13-4. 왜 login/signup 둘 다 claim을 수행해야 하는가

한쪽만 지원하면 제품 설명이 어색해집니다.

- 회원가입은 잘 되는데, 기존 회원 로그인은 guest 기록을 이어받지 못한다
- 또는 반대로 login만 되고 signup은 안 된다

이 둘은 사용자 입장에서 모두 "계정으로 들어가는 순간"이므로 ownership migration도 같은 품질로 제공해야 합니다.

## 14. 로그인된 member가 새 게임을 시작할 때

claim이 끝난 뒤에는 "과거 guest 기록을 member로 옮기는 단계"가 끝났습니다.
그 다음부터는 새 게임 자체가 member ownership으로 바로 시작해야 합니다.

### 14-1. 현재 member 여부는 항상 먼저 다시 확인한다

각 게임 start controller는 먼저 [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)의 `currentMember(request)`를 호출합니다.

이 서비스는 단순히 session attribute만 읽지 않습니다.

1. `MemberSessionManager.currentMember(httpSession)`로 session attribute 형식을 확인
2. `memberRepository.findById(memberId)`로 실제 DB row를 다시 읽음
3. 있으면 최신 nickname/role로 session을 동기화
4. 없으면 session을 정리하고 guest처럼 취급

즉 "로그인된 것처럼 보이는 세션"과 "실제로 유효한 member"를 구분합니다.

### 14-2. member start는 guest key를 만들지 않는다

현재 member가 유효하면 controller는 아래로 분기합니다.

```text
startMemberGame(currentMember.memberId(), currentMember.nickname())
```

이 경로에서는 `guestSessionKeyManager.ensureGuestSessionKey(...)`를 부르지 않습니다.

즉 로그인 뒤 새로 시작한 게임은 처음부터 아래 상태로 생성됩니다.

- `memberId != null`
- `guestSessionKey == null`

### 14-3. 이 계약을 고정하는 테스트

[GuestSessionOwnershipIntegrationTest.java](../src/test/java/com/worldmap/auth/GuestSessionOwnershipIntegrationTest.java)의 `loggedInMemberStartsGamesWithMemberOwnership`가 이를 보여 줍니다.

검증은 아래입니다.

- login된 browser session으로 시작한 새 위치 게임 session의 `memberId`는 회원 id다
- `guestSessionKey`는 null이다
- `playerNickname`은 로그인 회원의 nickname이다
- 이 게임이 끝나 생성된 leaderboard row도 `memberId`를 가진다

즉 claim 뒤의 세계와 새로 생성되는 world state가 같은 ownership 규칙을 공유합니다.

## 15. logout과 guest boundary 회전

guest ownership 글에서 logout은 빼면 안 됩니다.
왜냐하면 login만큼이나 ownership 경계를 다시 나누는 행위이기 때문입니다.

### 15-1. logout request 순서

[AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)의 `POST /logout`은 아래 두 줄이 전부입니다.

```text
memberSessionManager.signOut(httpSession)
guestSessionKeyManager.rotateGuestSessionKey(httpSession)
```

그리고 `/mypage`로 redirect합니다.

### 15-2. 왜 session invalidate 대신 attribute 제거 + guest rotate를 쓰는가

현재 프로젝트는 session 자체를 완전히 무효화하기보다, ownership 경계를 명시적으로 다시 설정하는 쪽을 택합니다.

그 이유는 아래와 같습니다.

- auth/game 코드는 계속 `HttpSession` 인터페이스에만 의존할 수 있다
- logout 직후에도 guest 사용자가 다시 바로 플레이할 수 있다
- 이전 guest key와 새 guest key를 분리해 새 anonymous run boundary를 만들 수 있다

### 15-3. 왜 rotate가 중요한가

만약 logout 뒤에도 이전 guest key를 그대로 유지하면 아래 문제가 생길 수 있습니다.

- login 전 guest history
- login 중 member history
- logout 뒤 새 guest history

가 같은 브라우저에서 애매하게 이어질 수 있습니다.

rotate는 이 경계를 끊습니다.

즉 로그아웃 후 새 guest는 "예전 guest"가 아니라 "새 anonymous owner"입니다.

### 15-4. 현재 테스트의 한계

현재 저장소에는 logout 뒤 새 guest key가 이전 것과 달라지는지를 직접 고정하는 dedicated integration test는 없습니다.

하지만 코드 계약은 명확합니다.

- `signOut`은 member attribute를 지움
- `rotateGuestSessionKey`는 무조건 새 UUID 기반 key를 세팅함

이 부분은 후속 hardening 테스트로 추가할 가치가 있습니다.

## 16. stale member fallback까지 ownership 글에 넣어야 하는 이유

처음 보면 "삭제된 회원 fallback은 17번 integrity 글 얘기 아닌가?"라고 생각할 수 있습니다.
하지만 현재 저장소 기준에서는 guest/member ownership 분기 자체를 바꾸기 때문에 이 글에도 넣는 편이 맞습니다.

### 16-1. stale session이 남으면 어떤 문제가 생기는가

예를 들어 session attribute에는 아래가 남아 있다고 합시다.

- `WORLDMAP_MEMBER_ID = 42`
- `WORLDMAP_MEMBER_NICKNAME = orbit_runner`
- `WORLDMAP_MEMBER_ROLE = USER`

그런데 DB에는 id 42 회원이 이미 삭제됐습니다.

이 상태를 그대로 믿으면 새 게임 시작 시 문제가 생깁니다.

- 존재하지 않는 memberId로 game row를 만들 수 있다
- 헤더/마이페이지/UI는 로그인처럼 보이지만 실제 owner는 깨진다
- admin link visibility도 틀어질 수 있다

### 16-2. CurrentMemberAccessService가 하는 일

[CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)는 현재 request/session에 대해 다음을 보장합니다.

1. session attribute 형식이 정상인지 본다
2. member row가 실제로 존재하는지 DB에서 다시 본다
3. 존재하면 session nickname/role을 최신 DB 값으로 덮어쓴다
4. 존재하지 않으면 `memberSessionManager.signOut(httpSession)`로 세 attribute를 지운다
5. malformed role이면 예외를 삼키고 역시 signOut한다

즉 "유효한 member가 아니면 guest로 돌아간다"는 정책을 일관되게 적용합니다.

### 16-3. 이 정책을 보여 주는 테스트

[AuthFlowIntegrationTest.java](../src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java)의 아래 테스트들이 여기에 해당합니다.

- `deletedMemberSessionFallsBackToGuestStateOnHomeAndMyPage`
- `deletedMemberSessionCanOpenLoginPageInsteadOfRedirectingToMyPage`
- `deletedMemberSessionFallsBackToGuestPromptOnGameStartPage`
- `deletedMemberSessionStartsNewGameAsGuestOwnership`

그리고 unit 수준에서는 [CurrentMemberAccessServiceTest.java](../src/test/java/com/worldmap/auth/application/CurrentMemberAccessServiceTest.java)가 아래를 고정합니다.

- persisted member state로 session nickname/role 다시 동기화
- deleted member session sign-out
- malformed role 정리
- request당 한 번만 DB 조회하는 cache

이 부분을 guest ownership 글에서 같이 설명해야, "현재 브라우저가 guest인지 member인지 누가 최종 판정하는가"가 분명해집니다.

## 17. GuestProgressClaimService가 왜 service여야 하는가

이 글에서 가장 자주 틀리기 쉬운 설계 포인트입니다.

### 17-1. 컨트롤러에 넣으면 생기는 문제

claim 로직을 `AuthPageController` 안에서 직접 구현하면 대체로 아래 형태가 됩니다.

```text
location repository 조회
population repository 조회
capital repository 조회
flag repository 조회
battle repository 조회
leaderboard repository 조회
각 row setMemberId(...)
각 row setGuestSessionKey(null)
```

이건 두 가지 면에서 나쁩니다.

- 인증 요청 처리와 ownership migration 규칙이 섞인다
- 다른 진입점에서 같은 규칙을 재사용하기 어렵다

### 17-2. service에 두면 경계가 선명해진다

[GuestProgressClaimService.java](../src/main/java/com/worldmap/auth/application/GuestProgressClaimService.java)는 아래 질문 하나만 책임집니다.

> "현재 guest key에 묶인 기록을 member owner로 어떻게 일괄 승격할 것인가?"

그래서 controller는 아래만 담당합니다.

- 인증 성공 여부
- 어떤 redirect로 보낼지
- 현재 session에서 guest key를 읽을지

실제 record migration은 service가 맡습니다.

### 17-3. claim 자체는 domain method가 맡는다

service가 repository 조회를 orchestration하고, 실제 row 상태 전이는 domain method가 맡습니다.

- game session row: `BaseGameSession.claimOwnership(memberId)`
- leaderboard row: `LeaderboardRecord.claimOwnership(memberId)`

즉 책임이 세 층으로 나뉩니다.

- controller: request 진입과 흐름 조합
- service: 어느 row들을 함께 옮길지 orchestration
- domain/entity: owner state transition

이 경계가 설명 가능성 측면에서 가장 중요합니다.

## 18. MemberSessionManager가 왜 따로 필요한가

claim 서비스만 보고 있으면 "그냥 controller에서 session attribute 세 개 넣으면 되지 않나?"라는 생각이 들 수 있습니다.

하지만 [MemberSessionManager.java](../src/main/java/com/worldmap/auth/application/MemberSessionManager.java)를 분리해 둔 이유가 있습니다.

### 18-1. session fixation 대응

`signIn(HttpServletRequest request, Member member)`는 먼저 `request.changeSessionId()`를 호출합니다.

즉 로그인 성공 뒤 session id를 회전시키는 규칙이 auth controller 곳곳에 흩어지지 않습니다.

이 정책을 고정하는 테스트가 [MemberSessionManagerTest.java](../src/test/java/com/worldmap/auth/application/MemberSessionManagerTest.java)의 `signInRotatesSessionIdAndStoresMemberAttributes`입니다.

### 18-2. 최신 member 상태 동기화

`syncMember(...)`는 request/session 어디에서든 "현재 회원 row를 session attribute로 어떻게 반영할 것인가"를 한 곳에 모읍니다.

그래서 [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)가 DB의 nickname/role을 다시 읽은 뒤 세션을 최신화할 수 있습니다.

### 18-3. signOut 규칙 일관화

`signOut(...)`는 아래 세 attribute를 지우는 단일 진입점입니다.

- member id
- member nickname
- member role

이런 기본 계약이 한 곳에 없으면 stale session cleanup과 logout 정책이 금방 어긋납니다.

## 19. 요청 흐름을 한 번에 보는 시퀀스

### 19-1. guest 첫 플레이

```text
브라우저 session 생성
-> 아직 guestSessionKey 없음
-> POST /api/games/location/sessions
-> CurrentMemberAccessService.currentMember(request) == empty
-> GuestSessionKeyManager.ensureGuestSessionKey(session)
-> guest-UUID 생성
-> LocationGameService.startGuestGame(nickname, guestSessionKey)
-> LocationGameSession.ready(..., memberId=null, guestSessionKey=guest-UUID)
-> DB 저장
```

### 19-2. 같은 브라우저로 다른 게임 시작

```text
POST /api/games/capital/sessions
-> CurrentMemberAccessService.currentMember(request) == empty
-> GuestSessionKeyManager.ensureGuestSessionKey(session)
-> 기존 guest-UUID 재사용
-> CapitalGameService.startGuestGame(nickname, same guest-UUID)
-> DB 저장
```

이 흐름이 다섯 게임에 공통으로 반복됩니다.

### 19-3. guest run 종료 후 leaderboard row 생성

```text
정답/오답 루프 진행
-> terminal 상태 도달
-> LeaderboardService.record...Result(...)
-> LeaderboardRecord.create(..., memberId=null, guestSessionKey=guest-UUID, ...)
-> DB 저장
```

### 19-4. signup 또는 login

```text
POST /signup 또는 /login
-> MemberAuthService.signUp/login
-> GuestSessionKeyManager.currentGuestSessionKey(httpSession)
-> guest-UUID 발견
-> GuestProgressClaimService.claimGuestRecords(memberId, guest-UUID)
-> 5개 game session + leaderboard row를 findAllByGuestSessionKeyAndMemberIdIsNull(...)
-> 각 row claimOwnership(memberId)
-> MemberSessionManager.signIn(request, member)
-> changeSessionId()
-> session attribute에 member 정보 저장
-> redirect:/mypage 또는 returnTo
```

### 19-5. 이후 새 게임 시작

```text
POST /api/games/flag/sessions
-> CurrentMemberAccessService.currentMember(request) == member
-> FlagGameService.startMemberGame(memberId, nickname)
-> FlagGameSession.ready(..., memberId=memberId, guestSessionKey=null)
-> DB 저장
```

### 19-6. logout

```text
POST /logout
-> MemberSessionManager.signOut(session)
-> GuestSessionKeyManager.rotateGuestSessionKey(session)
-> 새 guest-UUID 생성
-> redirect:/mypage
```

### 19-7. 삭제된 member session으로 새 게임 시작 시도

```text
POST /api/games/location/sessions
-> CurrentMemberAccessService.currentMember(request)
-> session에는 member attribute 있음
-> memberRepository.findById(memberId) == empty
-> MemberSessionManager.signOut(session)
-> currentMember == empty
-> GuestSessionKeyManager.ensureGuestSessionKey(session)
-> guest start로 fallback
```

이 일련의 흐름을 이해하면 현재 저장소의 guest/member ownership 모델을 정확히 다시 세울 수 있습니다.

## 20. 실패 케이스와 예외 처리

### 20-1. guest key가 없는 signup/login

회원가입이나 로그인 전에 guest로 플레이한 적이 없으면 `currentGuestSessionKey(httpSession)`은 비어 있습니다.

현재 controller는 아래처럼 동작합니다.

- 값이 있으면 claim
- 없으면 아무 것도 하지 않음

즉 claim은 no-op가 됩니다.

### 20-2. `memberId`가 null이거나 guest key가 blank인 claim 호출

[GuestProgressClaimService.java](../src/main/java/com/worldmap/auth/application/GuestProgressClaimService.java)의 `claimGuestRecords(...)`는 아래 조건이면 그냥 반환합니다.

- `memberId == null`
- `guestSessionKey == null`
- `guestSessionKey.isBlank()`

즉 잘못된 입력이 들어와도 불필요한 repository 조회를 하지 않습니다.

### 20-3. 이미 claim된 row 재처리

repository 조회 조건이 `memberId is null`을 포함하기 때문에 이미 member ownership으로 넘어간 row는 다시 건드리지 않습니다.

### 20-4. 잘못된 비밀번호

login 실패는 [MemberAuthService.java](../src/main/java/com/worldmap/auth/application/MemberAuthService.java)가 `IllegalArgumentException`으로 막고, [AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)가 로그인 화면에 에러 메시지를 남기고 끝냅니다.

즉 ownership claim은 인증 성공 뒤에만 열린다.

### 20-5. 삭제된 회원 세션

`CurrentMemberAccessService`가 세션을 정리한 뒤 guest로 fallback합니다.

즉 deleted member session은 "불완전한 로그인 상태"로 남지 않습니다.

### 20-6. malformed role 문자열

`MemberSessionManager.currentMember(...)`가 `MemberRole.valueOf(...)`에서 터질 수 있는 예외를 [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)가 잡고, signOut 처리합니다.

즉 깨진 session attribute도 guest로 복구됩니다.

### 20-7. logout 뒤 이전 guest key 재사용

현재 구현은 `rotateGuestSessionKey(...)`를 호출하므로 이전 guest key를 그대로 들고 가지 않습니다.

즉 새 anonymous run은 새 owner 경계에서 시작합니다.

## 21. 테스트로 검증하기

이 글은 테스트 이름을 같이 봐야 진짜 재현 가능합니다.

### 21-1. `GuestSessionOwnershipIntegrationTest`

[GuestSessionOwnershipIntegrationTest.java](../src/test/java/com/worldmap/auth/GuestSessionOwnershipIntegrationTest.java)는 ownership 모델의 가장 직접적인 증거입니다.

#### `sameBrowserSessionSharesGuestSessionKeyAcrossGameModes`

막는 리스크:

- 게임별로 guest identity가 달라져 claim이 끊기는 문제

검증 내용:

- 같은 `MockHttpSession`으로 시작한 위치/인구수/수도/국기/배틀 session이 모두 같은 `guestSessionKey`를 공유한다
- 모두 `memberId`는 null이다

#### `leaderboardRecordKeepsGuestOwnershipWhenGuestGameEnds`

막는 리스크:

- guest run이 leaderboard에 쓰일 때 owner를 잃는 문제

검증 내용:

- guest game 종료 뒤 생성된 `LeaderboardRecord`의 `guestSessionKey`가 game session과 같다
- `memberId`는 null이다

#### `loggedInMemberStartsGamesWithMemberOwnership`

막는 리스크:

- 로그인 후에도 새 게임이 guest owner로 시작하는 문제

검증 내용:

- signup 뒤 시작한 game session은 `memberId`를 갖고 `guestSessionKey`는 null이다
- 종료 후 leaderboard row도 member owner를 갖는다

### 21-2. `AuthFlowIntegrationTest`

[AuthFlowIntegrationTest.java](../src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java)는 auth + ownership 연결 흐름을 고정합니다.

#### `signupCreatesSimpleAccountAndKeepsMemberSession`

막는 리스크:

- signup 성공 뒤 세션 id가 회전하지 않는 문제

검증 내용:

- password hash가 raw password가 아님
- `lastLoginAt`이 채워짐
- session id가 바뀜
- `/mypage`에서 로그인 상태 UI가 보임

#### `signupClaimsCurrentGuestRecordsIntoMemberOwnership`

막는 리스크:

- signup 뒤 guest 기록이 계정으로 이어지지 않는 문제

검증 내용:

- 5개 게임 session 모두 member owner로 claim
- leaderboard row도 claim
- `/mypage`에서 최근 플레이와 count가 읽힘

#### `loginClaimsCurrentGuestRecordsIntoMemberOwnership`

막는 리스크:

- 기존 회원 login에는 claim이 빠지는 문제

검증 내용:

- 기존 회원 login 뒤에도 다섯 게임 session이 member owner로 승격

#### `deletedMemberSessionFallsBackToGuestStateOnHomeAndMyPage`

막는 리스크:

- 삭제된 회원 세션이 로그인된 것처럼 계속 보이는 문제

검증 내용:

- 홈과 `/mypage`가 guest UI로 돌아간다
- session member attribute가 비워진다

#### `deletedMemberSessionStartsNewGameAsGuestOwnership`

막는 리스크:

- stale member session이 존재하지 않는 member id로 새 game session을 만드는 문제

검증 내용:

- 새로 시작한 game session은 `memberId == null`
- `guestSessionKey`는 새로 존재

### 21-3. `CurrentMemberAccessServiceTest`

[CurrentMemberAccessServiceTest.java](../src/test/java/com/worldmap/auth/application/CurrentMemberAccessServiceTest.java)는 current member 재검증을 단위 수준에서 보여 줍니다.

#### `currentMemberSynchronizesSessionWithPersistedMemberState`

막는 리스크:

- session nickname/role이 DB와 달라져도 그대로 남는 문제

#### `currentMemberSignsOutDeletedMemberSession`

막는 리스크:

- 삭제된 회원 세션이 정리되지 않는 문제

#### `currentMemberClearsMalformedSessionRole`

막는 리스크:

- 잘못된 role 문자열 때문에 500이 나는 문제

#### `currentMemberCachesResolvedMemberPerRequest`

막는 리스크:

- 같은 request 안에서 member row를 반복 조회하는 문제

### 21-4. `MemberSessionManagerTest`

[MemberSessionManagerTest.java](../src/test/java/com/worldmap/auth/application/MemberSessionManagerTest.java)는 session attribute 계약 그 자체를 보여 줍니다.

#### `signInRotatesSessionIdAndStoresMemberAttributes`

막는 리스크:

- session fixation
- member session attribute 누락

#### `syncMemberOverwritesSessionAttributesWithPersistedMemberState`

막는 리스크:

- 최신 nickname/role 동기화 실패

이 테스트 묶음은 same-browser guest continuity, claim, stale member fallback, session snapshot 계약을 강하게 고정합니다.
다만 cross-device guest merge나 장기 세션 복구, 여러 브라우저 사이의 ownership 통합까지 자동 증명하는 것은 아닙니다.
현재 모델은 의도적으로 "현재 브라우저 세션" 범위에 집중합니다.

## 22. 이 글만 보고 다시 구현하는 순서

현재 저장소를 몰라도 이 단계만 다시 만들려면 아래 순서가 가장 안전합니다.

### 22-1. 1단계: member와 guest identity를 session에서 분리한다

먼저 [MemberSessionManager.java](../src/main/java/com/worldmap/auth/application/MemberSessionManager.java)와 [GuestSessionKeyManager.java](../src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java)를 만듭니다.

이 단계에서 고정해야 할 것:

- member session attribute 이름
- guest session attribute 이름
- `signIn / signOut / syncMember`
- `ensure / current / rotate`

### 22-2. 2단계: 모든 game session과 leaderboard row에 owner 필드를 넣는다

다음으로 아래 필드를 game/leaderboard 쪽에 넣습니다.

- `memberId`
- `guestSessionKey`

그리고 domain method를 만듭니다.

- `claimOwnership(memberId)`

이 단계에서 중요한 것은 controller가 필드를 직접 만지지 않게 하는 것입니다.

### 22-3. 3단계: guest game start와 member game start를 분리한다

각 start controller에서 아래 분기를 넣습니다.

```text
currentMember 존재 -> startMemberGame
없음 -> ensureGuestSessionKey -> startGuestGame
```

다섯 게임이 모두 같은 패턴을 갖도록 맞춥니다.

### 22-4. 4단계: claim service를 추가한다

[GuestProgressClaimService.java](../src/main/java/com/worldmap/auth/application/GuestProgressClaimService.java) 같은 orchestration service를 만들고, 다섯 게임 repository와 leaderboard repository를 주입합니다.

그리고 조회 패턴을 통일합니다.

```text
findAllByGuestSessionKeyAndMemberIdIsNull(guestSessionKey)
```

### 22-5. 5단계: signup/login에 claim을 연결한다

[AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)에서 아래 순서를 넣습니다.

```text
인증 성공
-> currentGuestSessionKey 읽기
-> 있으면 claimGuestRecords
-> signIn(request, member)
-> redirect
```

### 22-6. 6단계: logout과 stale member fallback을 추가한다

이 단계까지 넣어야 현재 저장소 수준입니다.

- logout 시 `signOut + rotateGuestSessionKey`
- current member 조회 시 DB 재검증
- 삭제된 회원이면 guest로 정리

### 22-7. 7단계: 테스트로 잠근다

최소 아래 네 층을 나눠 테스트합니다.

- guest ownership integration
- auth flow integration
- current member unit
- member session manager unit

## 23. 구현 체크리스트

- guest key를 session attribute로 발급하는 manager가 있다
- member session attribute를 관리하는 manager가 있다
- 모든 game session row가 `memberId` 또는 `guestSessionKey`를 가진다
- leaderboard row도 같은 owner 계약을 가진다
- 5개 게임 start API가 모두 `member -> startMemberGame`, `guest -> ensureGuestSessionKey + startGuestGame` 패턴을 쓴다
- signup 직후 current browser guest records를 claim한다
- login 직후 current browser guest records를 claim한다
- claim은 5개 game session + leaderboard row를 함께 옮긴다
- logout 시 member attribute를 지우고 guest key를 회전시킨다
- stale member session은 guest로 정리된다
- 테스트로 same-browser continuity, member start ownership, claim, stale fallback을 막고 있다

## 24. 실행 / 검증 명령

이 글의 핵심 흐름은 아래 테스트 묶음으로 확인할 수 있습니다.

```bash
./gradlew test \
  --tests com.worldmap.auth.GuestSessionOwnershipIntegrationTest \
  --tests com.worldmap.auth.AuthFlowIntegrationTest \
  --tests com.worldmap.auth.application.CurrentMemberAccessServiceTest \
  --tests com.worldmap.auth.application.MemberSessionManagerTest
```

## 25. 산출물 체크리스트

이 단계가 끝났다면 아래가 만족돼야 합니다.

- 비회원이 계정 없이 5개 게임을 바로 시작할 수 있다
- 같은 브라우저 세션은 모든 게임에서 같은 `guestSessionKey`를 공유한다
- 모든 game session과 leaderboard row는 `memberId` 또는 `guestSessionKey` 중 하나를 owner로 가진다
- signup/login 직후 현재 브라우저의 guest 기록이 member ownership으로 승격된다
- 로그인 후 새 게임은 guest가 아니라 member ownership으로 시작한다
- logout 뒤에는 새 guest boundary가 열린다
- 삭제된 member session은 guest로 정리된다

## 26. 현재 구현의 한계

### 26-1. cross-device guest merge는 지원하지 않는다

현재 guest identity는 브라우저 세션에 묶여 있습니다.

즉 아래는 지원하지 않습니다.

- 모바일 guest 기록과 데스크톱 guest 기록 자동 병합
- 쿠키가 사라진 뒤 anonymous history 복원

이건 현재 스코프 밖으로 남겨 둔 의도적인 제약입니다.

### 26-2. logout 뒤 guest key 회전에 대한 dedicated integration test는 아직 없다

즉 logout 뒤 새 guest boundary가 열려야 한다는 규칙은 코드와 단위/통합 테스트 조합으로 설명되지만,
`로그아웃 -> 즉시 새 guest 플레이 시작` 전체를 하나의 전용 integration flow로 잠그는 수준까지는 아직 아니다.

코드 계약은 명확하지만, 이 부분은 추후 auth hardening test를 추가할 가치가 있습니다.

### 26-3. claim은 "현재 브라우저"의 guest key만 본다

이건 제한이자 정책입니다.

장점:

- 잘못된 병합을 막는다

단점:

- 다른 브라우저에서 guest로 했던 기록을 계정으로 모을 수 없다

현재 프로젝트에서는 이 trade-off를 설명 가능성 쪽으로 택했습니다.

## 27. 자주 막히는 지점

### 27-1. guest를 "완전 익명"으로 처리하는 것

guest에게 owner를 주지 않으면 뒤에서 claim을 구현할 수 없습니다.

### 27-2. 일부 게임만 claim하고 신규 게임을 빼먹는 것

현재 저장소는 다섯 게임을 모두 public scope로 운영합니다.
따라서 claim 범위도 다섯 게임 전부여야 합니다.

### 27-3. leaderboard row를 claim 범위에서 빼먹는 것

그러면 `/mypage`는 맞는데 `/ranking`은 안 맞는 이상한 상태가 됩니다.

### 27-4. claim 로직을 controller에 직접 쓰는 것

인증과 ownership migration 규칙이 섞이기 시작합니다.

### 27-5. logout 때 guest key를 rotate하지 않는 것

그러면 이전 guest history와 새 anonymous history의 경계가 흐려집니다.

### 27-6. stale member session을 정리하지 않는 것

삭제된 회원이나 깨진 role 때문에 "로그인된 것처럼 보이지만 실제로는 invalid한 세션"이 남습니다.

## 28. 이 글을 30초로 설명하면

WorldMap은 guest도 바로 플레이하게 하되, `guestSessionKey`로 현재 브라우저의 비회원 owner를 만들고 모든 게임 session과 leaderboard row에 owner를 남깁니다. 그리고 signup/login 직후 `GuestProgressClaimService`가 현재 브라우저 guest 기록만 `memberId`로 승격하고, 이후 새 게임은 member ownership으로 시작하게 해서 `/mypage`와 랭킹 흐름이 자연스럽게 이어지도록 설계했습니다.

## 29. 면접에서 바로 받을 수 있는 꼬리 질문

### 29-1. 왜 guest 플레이를 아예 막지 않았나요?

제품의 첫 가치가 즉시 체험이기 때문입니다.
다만 ownership을 비워 두지 않기 위해 guestSessionKey를 따로 둡니다.

### 29-2. 왜 guest 기록을 현재 브라우저 기준으로만 claim하나요?

외부 식별자 없이 cross-device guest merge를 허용하면 잘못된 병합 위험이 커집니다.
현재 프로젝트 범위에서는 same-browser continuity까지만 책임집니다.

### 29-3. 왜 claim은 서비스에 두고, 실제 상태 전이는 domain method에 두나요?

controller는 request 조합, service는 여러 repository orchestration, domain은 row 상태 전이라는 경계를 유지해야 다섯 게임과 leaderboard에 같은 규칙을 설명 가능하게 재사용할 수 있기 때문입니다.

### 29-4. 로그아웃 뒤 session을 완전히 invalidate하지 않은 이유는 뭔가요?

현재 프로젝트는 `HttpSession` 인터페이스를 유지한 채 ownership 경계만 명시적으로 다시 잡는 쪽을 택했습니다. 그래서 signOut으로 member attribute를 비우고 rotateGuestSessionKey로 새 guest boundary를 열어, 즉시 guest 플레이를 다시 시작할 수 있게 했습니다.

## 30. 다음 글과의 연결

이 글은 "guest가 어떻게 owner를 갖고 member로 승격되는가"를 다룹니다.
다음 단계에서 봐야 할 것은 이 owner를 기반으로 실제 기록 허브와 운영 read model을 어떻게 읽는가입니다.

- 다음 글: [12-simple-auth-member-session-and-admin-entry.md](./12-simple-auth-member-session-and-admin-entry.md)
- 이어지는 읽기: [13-mypage-and-public-stats-read-models.md](./13-mypage-and-public-stats-read-models.md)
- hardening 관점의 후속 글: [17-game-integrity-current-member-and-role-revalidation.md](./17-game-integrity-current-member-and-role-revalidation.md)
