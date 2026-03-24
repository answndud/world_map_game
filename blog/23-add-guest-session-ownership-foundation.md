# [Spring Boot 포트폴리오] 23. 게스트 세션 키와 기록 소유권 기반 먼저 심기

## 이번 글의 핵심 질문

이전 글에서 단순 계정 구조를 설계했다.

그런데 바로 회원가입 / 로그인을 붙이면 한 가지 문제가 생긴다.

“지금 만들어지는 게임 기록이 누구 것인지, 나중에 어떻게 계정에 붙일지 아직 데이터로 설명되지 않는다.”

그래서 이번 단계에서는 로그인 화면보다 먼저, 기록 소유권 기반부터 추가했다.

## 왜 로그인보다 ownership이 먼저인가

우리가 만들고 싶은 흐름은 이렇다.

1. 비회원이 몇 판 플레이한다.
2. 같은 브라우저 세션 안에서는 그 기록이 하나로 묶인다.
3. 사용자가 회원가입 또는 로그인한다.
4. 현재 브라우저 세션의 비회원 기록만 그 계정으로 귀속한다.

이 흐름이 성립하려면, 로그인 구현 전에 이미 모든 게임 기록이 공통 식별자를 가져야 한다.

그래서 이번 단계의 핵심은 `guestSessionKey`다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/domain/Member.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/domain/MemberRole.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/domain/MemberRepository.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/application/LocationGameService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/web/LocationGameApiController.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/ranking/application/LeaderboardService.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/auth/GuestSessionOwnershipIntegrationTest.java`

## 무엇을 추가했는가

### 1. `member` 도메인 뼈대

아직 회원가입 / 로그인 기능은 없다.

하지만 이후 계정 귀속의 대상이 될 `member` 엔티티와 repository는 먼저 만들었다.

즉, 계정 기능 전체가 아니라 “계정 소유자라는 개념”만 먼저 데이터 모델에 심은 것이다.

### 2. `GuestSessionKeyManager`

현재 브라우저 세션 안에서 공통으로 쓰는 `guestSessionKey`를 발급하고 유지한다.

이 값은 `HttpSession`에 저장된다.

그래서 같은 브라우저 세션으로 위치 게임과 인구수 게임을 시작하면 같은 key를 공유한다.

### 3. 게임 세션 / 랭킹 레코드 ownership 필드

이제 아래 엔티티는 모두 `memberId`, `guestSessionKey`를 가진다.

- `LocationGameSession`
- `PopulationGameSession`
- `LeaderboardRecord`

현재 단계에서는 로그인 기능이 없으므로 `memberId = null`이고, `guestSessionKey`만 채워진다.

## 요청 흐름은 어떻게 바뀌는가

### 게임 시작

1. 사용자가 위치 또는 인구수 게임 시작 요청
2. 컨트롤러가 `HttpSession`에서 `guestSessionKey` 확보
3. 서비스가 게임 세션 생성
4. 세션 엔티티에 `memberId = null`, `guestSessionKey = ...` 저장

### 게임 종료 후 랭킹 반영

1. 게임오버 발생
2. `LeaderboardService`가 게임 세션을 읽음
3. 세션에 저장된 ownership 정보를 랭킹 레코드에 그대로 복사
4. 이후 로그인 단계에서 이 레코드를 계정으로 귀속할 수 있게 됨

## 왜 이 로직이 컨트롤러가 아니라 서비스 / 도메인에 있어야 하는가

컨트롤러는 현재 브라우저 세션의 key를 꺼내는 역할만 한다.

하지만 ownership을 실제로 어디에, 어떤 규칙으로 남길지는 비즈니스 규칙이다.

- 게임 세션 생성 시 ownership 저장
- 랭킹 레코드 생성 시 ownership 복사
- 나중에 로그인 직후 기록 귀속

이 흐름은 전부 서비스 / 도메인 경계에서 이어져야 한다.

그래야 HTTP 입력 형태가 바뀌어도 기록 소유권 규칙은 유지된다.

## 테스트는 무엇을 했는가

핵심 테스트는 `GuestSessionOwnershipIntegrationTest`다.

이 테스트에서 두 가지를 확인했다.

1. 같은 `MockHttpSession`으로 위치 게임과 인구수 게임을 시작하면 같은 `guestSessionKey`를 공유하는가
2. 게스트 게임오버 후 저장된 `LeaderboardRecord`가 같은 `guestSessionKey`를 유지하는가

추가로 기존 위치 / 인구수 / 랭킹 통합 테스트도 함께 돌려 기존 흐름이 깨지지 않았는지 확인했다.

## 이번 단계의 의미

겉으로 보면 로그인 화면도 없고, 사용자가 바로 체감하는 변화도 작다.

하지만 계정 기능에서 가장 중요한 바닥 공사가 끝난 셈이다.

이제 다음 단계에서 회원가입 / 로그인 / 로그아웃을 붙여도,

“현재 브라우저 세션의 비회원 기록만 계정으로 귀속한다”는 요구를 실제 데이터 모델로 설명할 수 있다.

## 다음 구현 순서

1. 비밀번호 해시 저장 구조 추가
2. 회원가입 / 로그인 / 로그아웃 추가
3. 로그인 직후 현재 `guestSessionKey` 기록 귀속 서비스 추가
4. `/mypage`에 내 점수 / 최근 플레이 / 내 랭킹 연결

## 면접에서는 이렇게 설명하면 된다

“로그인 기능보다 먼저 기록 소유권 기반을 심었습니다. 같은 브라우저 세션의 비회원 기록을 나중에 계정으로 안전하게 귀속하려면, 게임 세션과 랭킹 레코드가 미리 `guestSessionKey`를 갖고 있어야 하기 때문입니다. 컨트롤러는 현재 세션 key를 전달만 하고, 실제 ownership 저장 규칙은 게임 서비스와 랭킹 서비스가 맡도록 분리했습니다.”
