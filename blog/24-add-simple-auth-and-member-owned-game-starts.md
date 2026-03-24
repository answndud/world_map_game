# [Spring Boot 포트폴리오] 24. 단순 회원가입 / 로그인과 member 소유 게임 시작 연결하기

## 이번 글의 핵심 질문

이전 단계에서 `memberId`, `guestSessionKey` 기반 ownership은 먼저 심었다.

그런데 아직 사용자는 로그인 자체를 할 수 없었다.

즉, 데이터 모델은 준비됐지만 제품 흐름은 아직 비어 있었다.

그래서 이번 단계의 질문은 이거다.

“비회원 플레이는 그대로 유지하면서, 기록을 남기고 싶은 사람은 닉네임 + 비밀번호만으로 로그인하고 새 게임부터 계정 소유로 쌓이게 만들 수 있을까?”

## 이번 단계의 범위

이번 글에서는 아래까지만 구현한다.

- `회원가입`
- `로그인`
- `로그아웃`
- `My Page`의 guest 유도 / 로그인 상태 분기
- 로그인 사용자의 새 게임 기록을 `memberId` ownership으로 저장

아직 하지 않은 것은 이것이다.

- 기존 guest 기록을 로그인 직후 계정으로 귀속
- `/mypage` 실제 전적 조회

즉, 이번 단계는 “계정 연결의 첫 동작”을 만드는 조각이다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/application/MemberAuthService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/application/MemberPasswordHasher.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/application/MemberSessionManager.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/web/AuthPageController.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/web/SignupForm.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/auth/web/LoginForm.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/web/MyPageController.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/application/LocationGameService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java`
- `/Users/alex/project/worldmap/src/main/resources/templates/auth/signup.html`
- `/Users/alex/project/worldmap/src/main/resources/templates/auth/login.html`
- `/Users/alex/project/worldmap/src/main/resources/templates/mypage.html`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/auth/GuestSessionOwnershipIntegrationTest.java`

## 왜 Spring Security 전체를 바로 넣지 않았는가

이번 프로젝트의 계정 목적은 커뮤니티 보안 제품이 아니다.

핵심은 아래뿐이다.

- 닉네임 유지
- 점수 누적
- 내 전적 조회

그래서 이번 단계에서는 설명 비용이 큰 Spring Security 전체 구성보다,

- BCrypt 비밀번호 해시
- `HttpSession` 기반 단순 로그인 상태 저장
- 컨트롤러에서 현재 member 확인

정도로 먼저 닫는 편이 더 적절했다.

## 요청 흐름은 어떻게 되는가

### 회원가입

1. `/signup` form 제출
2. `AuthPageController`가 form validation 수행
3. `MemberAuthService.signUp()`가 닉네임 정규화, 중복 확인, BCrypt 해시 저장
4. `MemberSessionManager`가 현재 `HttpSession`에 로그인 상태 저장
5. `/mypage`로 redirect

### 로그인

1. `/login` form 제출
2. `MemberAuthService.login()`이 닉네임/비밀번호 검증
3. `lastLoginAt` 갱신
4. `MemberSessionManager`가 세션에 `memberId`, `nickname`, `role` 저장
5. `/mypage`로 redirect

### 로그인 후 게임 시작

1. 사용자가 위치/인구수 게임 시작 API 호출
2. 게임 컨트롤러가 현재 세션의 member를 먼저 확인
3. 로그인 상태면 `startMemberGame(memberId, nickname)`으로 진입
4. 게임 세션이 `memberId` ownership으로 저장
5. 게임오버 시 랭킹 레코드도 같은 ownership을 복사

## 왜 컨트롤러가 아니라 서비스에 인증 규칙을 둬야 하는가

컨트롤러는 HTTP form과 `HttpSession`을 다루는 곳이다.

하지만 아래 규칙은 전부 비즈니스 규칙이다.

- 닉네임 정규화
- 중복 닉네임 차단
- BCrypt 해시 저장
- 비밀번호 검증
- 로그인 시간 갱신

그래서 이 책임은 `MemberAuthService`가 가져간다.

게임 시작도 마찬가지다.

컨트롤러는 “현재 로그인 사용자가 있는가”만 확인하고,

- member 소유로 시작할지
- guest 소유로 시작할지

라는 규칙은 게임 서비스가 분기해서 처리한다.

## `My Page`는 왜 완전 보호하지 않았는가

헤더를 `Home / My Page`만 남긴 상태이기 때문에,

로그인 진입점도 자연스러워야 했다.

그래서 이번 단계의 `/mypage`는:

- guest에게는 로그인/회원가입 유도 화면
- 로그인 사용자에게는 계정 연결 shell

로 동작한다.

이렇게 하면 헤더를 복잡하게 늘리지 않고도 계정 진입 흐름을 유지할 수 있다.

## 테스트는 무엇을 했는가

### `AuthFlowIntegrationTest`

- 회원가입 후 세션 로그인 유지 확인
- 비밀번호가 평문이 아니라 해시로 저장되는지 확인
- 로그인 실패 시 오류 메시지 노출 확인

### `GuestSessionOwnershipIntegrationTest`

- 로그인 사용자로 시작한 게임이 `memberId` ownership을 가지는지 확인
- 게임오버 후 랭킹 레코드도 같은 `memberId` ownership을 가지는지 확인

즉, “로그인 화면이 뜬다”가 아니라 “로그인 상태가 실제 게임 기록 소유권으로 이어진다”까지 본 것이다.

## 이번 단계의 한계

아직 로그인 이전에 쌓인 guest 기록은 자동으로 member로 옮기지 않는다.

그래서 현재 상태는:

- guest로 하던 게임 기록은 guest 그대로
- 로그인 후 새로 시작한 게임부터 member 소유

다음 단계에서 이 둘을 연결해야 진짜 “기록 유지” 기능이 완성된다.

## 다음 구현 순서

1. 로그인 직후 `guestSessionKey` 기록 귀속 서비스 추가
2. `/mypage`에 내 최고 점수 / 최근 플레이 / 내 랭킹 연결
3. admin 접근 제어 추가

## 면접에서는 이렇게 설명하면 된다

“8단계에서는 Spring Security 전체보다 설명 가능한 단순 계정 구조를 먼저 선택했습니다. `MemberAuthService`가 닉네임 중복, BCrypt 해시, 로그인 검증을 처리하고, `MemberSessionManager`가 세션 로그인 상태를 유지합니다. 이후 게임 시작 컨트롤러는 현재 세션의 member를 확인해 guest start와 member start를 분기하고, 로그인 사용자의 새 게임 기록은 `memberId` ownership으로 저장되게 만들었습니다.”
