# [Spring Boot 포트폴리오] 25. 로그인 직후 현재 브라우저의 guest 기록을 계정으로 귀속하기

## 이번 글의 핵심 질문

회원가입 / 로그인과 `memberId` ownership 저장까지 붙였다고 해도, 아직 한 가지 문제가 남아 있었다.

비회원으로 먼저 게임을 하다가 로그인하면, 이미 쌓인 기록은 여전히 guest ownership으로 남는다.

그러면 사용자는 “왜 로그인했는데 방금 한 기록이 내 계정에 안 보이지?”라는 느낌을 받게 된다.

이번 단계의 질문은 이거다.

“현재 브라우저 세션에서 쌓인 guest 기록만 안전하게 계정으로 이어붙일 수 있을까?”

## 이번 단계의 범위

이번 구현은 아래까지만 다룬다.

- 회원가입 / 로그인 직후 guest 기록 귀속
- 게임 세션 / 랭킹 레코드 ownership 전환

아직 하지 않은 것은 이거다.

- `/mypage` 실제 전적 집계 표시
- admin 접근 제어

즉, 이번 단계는 “로그인 순간 ownership이 이어지는가”를 해결하는 조각이다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/application/GuestProgressClaimService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/application/GameSessionAccessContextResolver.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/application/MemberSessionManager.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/web/AuthPageController.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/common/exception/SessionAccessDeniedException.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/common/application/GameSessionAccessContext.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/domain/LocationGameSessionRepository.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/domain/PopulationGameSessionRepository.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/application/LocationGameService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/capital/application/CapitalGameService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/flag/application/FlagGameService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/auth/application/MemberSessionManagerTest.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/auth/application/GameSessionAccessContextResolverTest.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/game/common/application/GameSessionAccessContextTest.java`

## 왜 “현재 브라우저 세션”만 귀속하는가

처음 설계부터 이 프로젝트는 guest 기록을 영구 복구 대상으로 보지 않았다.

의도는 분명하다.

- 비회원은 바로 플레이
- 기록을 계속 남기고 싶으면 로그인

그래서 귀속 범위도 단순하게 간다.

- 현재 `HttpSession`의 `guestSessionKey`
- 그 key를 가진 미귀속 레코드만
- 로그인한 member에게 연결

이렇게 해야 구조를 설명하기 쉽고, 예외 케이스도 줄어든다.

## 요청 흐름은 어떻게 되는가

### 회원가입 / 로그인 성공 직후

1. `AuthPageController`가 회원가입 또는 로그인 성공
2. `GuestSessionKeyManager.currentGuestSessionKey()`로 현재 guest key 확인
3. `GuestProgressClaimService.claimGuestRecords(memberId, guestSessionKey)` 호출
4. 게임 세션 / 랭킹 레코드 중 `memberId IS NULL`인 guest 기록만 조회
5. 각 엔티티의 `claimOwnership(memberId)` 호출
6. `memberId` 채우고 `guestSessionKey`는 비움
7. 로그인 세션 유지 후 `/mypage`로 이동

## 왜 별도 서비스로 분리했는가

컨트롤러에 직접 구현할 수도 있다.

하지만 guest 귀속은 아래 규칙을 가진 도메인 전환이다.

- 어떤 레코드를 귀속할지
- 이미 member인 레코드는 제외할지
- guest key는 지우고 memberId만 남길지
- 어떤 엔티티까지 같은 규칙으로 묶을지

이건 화면 흐름보다 ownership 규칙에 가깝다.

그래서 `GuestProgressClaimService`로 분리하는 것이 맞다.

## 왜 nickname snapshot은 바꾸지 않았는가

이 부분이 중요하다.

guest 기록을 계정에 귀속한다고 해서 과거 기록의 표시 이름까지 바꾸면,

- 당시 랭킹 화면
- 과거 플레이 요약
- 기록 시점 스냅샷

의 의미가 흔들릴 수 있다.

그래서 이번 단계에서는:

- 소유자만 `memberId`로 변경
- `playerNickname`은 당시 값 유지

라는 기준을 택했다.

즉, “누구 것이냐”와 “당시 어떤 이름으로 표시됐느냐”를 분리한 것이다.

## 테스트는 무엇을 했는가

핵심 테스트는 `AuthFlowIntegrationTest`에 추가했다.

시나리오는 이렇다.

1. 같은 브라우저 세션으로 guest 위치 게임 시작
2. 게임오버까지 가서 랭킹 레코드 생성
3. 같은 세션으로 회원가입
4. 기존 게임 세션과 랭킹 레코드가 모두 `memberId` ownership으로 바뀌는지 확인
5. `guestSessionKey`는 비워졌는지 확인

즉, “로그인 화면이 뜬다”가 아니라 “실제로 기존 guest 기록이 계정으로 넘어갔다”까지 검증한 것이다.

## 이번 단계의 의미

이제 계정 기능은 아래 두 단계를 모두 넘었다.

1. 로그인 후 새 게임은 `memberId` ownership으로 저장
2. 로그인 직전까지 하던 guest 기록도 현재 계정으로 귀속

그래서 다음 단계에서 `/mypage`에 실제 전적 집계를 붙이면, guest와 member 흐름이 한 화면에서 자연스럽게 이어질 수 있다.

## 후속 보강: 귀속만으로는 세션이 안전해지지 않는다

여기서 한 가지를 더 봐야 한다.

guest 기록 귀속을 붙였다고 해서 자동으로 안전해지는 것은 아니다.

만약 누군가가 `sessionId` URL만 알아도 아래가 가능하다면 문제가 남는다.

- 다른 브라우저에서 게임 state 조회
- 다른 브라우저에서 답안 제출
- 진행 중인데도 result 페이지/API로 정답과 시도 이력 먼저 보기

즉 ownership 필드를 심은 것과,
실제로 그 ownership을 요청 시점에 강제하는 것은 다른 문제다.

그래서 이번 후속 보강에서는 “현재 브라우저 세션만 그 게임을 열 수 있는가”를 코드로 닫았다.

## 게임 요청을 현재 브라우저 ownership에 묶는 방법

이번 보강에서 추가한 핵심은 두 개다.

- `GameSessionAccessContext`
- `GameSessionAccessContextResolver`

흐름은 단순하다.

1. 컨트롤러가 현재 `HttpSession`에서 `memberId`와 `guestSessionKey`를 읽는다.
2. 이 값을 `GameSessionAccessContext`로 만든다.
3. 서비스는 `getSession(sessionId, accessContext)`로 세션을 읽는다.
4. access context와 세션 ownership이 일치하지 않으면 바로 막는다.

이렇게 하면 권한 판단이 컨트롤러마다 흩어지지 않고,
실제 게임 상태를 읽는 서비스 진입점에서 한 번에 강제된다.

중요한 점은 access context가 `memberId`만 들고 가지 않는다는 점이다.

로그인 직후라도 같은 브라우저 세션에는 기존 `guestSessionKey`가 남아 있을 수 있다.
그래서 현재 보강은:

- member ownership이면 `memberId`로 통과
- guest ownership이면 같은 브라우저의 `guestSessionKey`로 통과

라는 기준을 같이 가진다.

## 왜 진행 중 result는 404로 닫았는가

이전에는 세션이 살아 있기만 하면 `result`를 읽을 수 있었다.

하지만 이건 개념상 맞지 않는다.

결과 페이지는 “게임이 끝난 뒤에 생기는 terminal resource”여야 한다.

그래서 이번 보강에서는:

- `READY`
- `IN_PROGRESS`

상태에서는 result를 아직 없는 resource로 취급한다.

즉, 페이지와 API 모두 `404`를 돌린다.

이렇게 하면 진행 중 정답과 시도 로그를 먼저 보는 지름길을 막을 수 있다.

## 왜 로그인 시 세션 ID도 회전했는가

로그인 성공 후 세션에 `memberId`만 심으면 session fixation 문제가 남는다.

그래서 `MemberSessionManager.signIn()`이 `request.changeSessionId()`를 먼저 호출하도록 바꿨다.

핵심은 이렇다.

- guest 흐름은 그대로 유지
- 세션 attribute 기반 단순 구조도 그대로 유지
- 하지만 인증 성공 직후 세션 ID는 새로 발급

즉 구조를 무겁게 만들지 않고도,
로그인 순간의 세션 보안 기준은 한 단계 올릴 수 있다.

## 다음 구현 순서

1. `/mypage`에 내 최고 점수 / 최근 플레이 / 내 랭킹 연결
2. admin 접근 제어 추가
3. 필요하면 guest 귀속 결과를 사용자에게 더 분명하게 안내

## 면접에서는 이렇게 설명하면 된다

“단순 로그인만 붙이면 로그인 직전까지 하던 guest 기록이 따로 남기 때문에 사용자 경험이 끊깁니다. 그래서 `GuestProgressClaimService`를 만들어 회원가입/로그인 성공 직후 현재 브라우저의 `guestSessionKey` 기록만 `memberId` ownership으로 바꾸게 했습니다. 이때 표시용 닉네임 snapshot은 유지하고 소유자만 전환해서, 과거 기록 표시 안정성과 계정 귀속을 동시에 만족시켰습니다.”

후속 보강까지 같이 설명하면 이렇게 이어갈 수 있다.

“여기서 끝내면 `sessionId`만 아는 다른 브라우저가 게임 상태와 결과를 볼 수 있어서, ownership 필드가 실제 보호 규칙으로 이어지지 않습니다. 그래서 컨트롤러가 현재 세션의 `memberId + guestSessionKey`를 `GameSessionAccessContext`로 만들고, 서비스가 세션을 읽을 때 그 ownership을 먼저 검증하게 했습니다. 또 result는 게임이 끝난 뒤에만 존재하는 terminal resource로 다시 정의해서 진행 중에는 404를 돌리고, 로그인 순간에는 `changeSessionId()`로 세션 ID도 회전시켰습니다.”
