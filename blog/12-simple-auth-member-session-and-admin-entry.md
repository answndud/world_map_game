# [Spring Boot 게임 플랫폼 포트폴리오] 12. simple auth, member session, admin entry를 어떻게 최소 구조로 닫았는가

## 1. 이번 글에서 풀 문제

WorldMap은 게임 플랫폼입니다.

즉 계정 시스템의 목적이 일반적인 커뮤니티 서비스와 다릅니다.

- 이메일 인증을 잘 만드는 것
- 소셜 로그인 버튼을 늘리는 것
- 프로필 편집 화면을 만드는 것

이런 것이 1차 목표가 아닙니다.

현재 프로젝트에서 계정이 필요한 이유는 더 단순합니다.

1. guest로 하던 플레이 기록을 계정 기준으로 이어 붙이기 위해
2. `/mypage`에서 내 전적을 모아 보기 위해
3. `/dashboard` 같은 운영 화면에 role 기반으로 들어가기 위해

그래서 이 글은 "화려한 인증"이 아니라 **설명 가능한 최소 인증 구조**를 만드는 방법을 다룹니다.

현재 코드 기준으로 이 글이 닫아야 하는 질문은 아래입니다.

- 왜 OAuth/JWT가 아니라 `nickname + password + session`으로 시작했는가
- 회원가입과 로그인에서 어떤 규칙을 어디에 두었는가
- 로그인 뒤 브라우저 세션은 어떻게 member session으로 바뀌는가
- 왜 `changeSessionId()`가 꼭 들어가야 하는가
- admin 계정은 누가, 언제, 어떤 설정값으로 만드는가
- `/dashboard`는 왜 세션의 role 문자열을 그대로 믿지 않고 현재 DB role을 다시 보는가

이 글을 다 읽으면 현재 저장소 기준으로 아래를 다시 구현할 수 있어야 합니다.

- `member_account` 기반 simple auth
- SSR `/signup`, `/login`, `/logout`
- session fixation 대응이 들어간 member session 관리
- `returnTo`가 붙는 admin login redirect
- local/prod 설정에 따라 달라지는 bootstrap admin provisioning
- `/dashboard` 접근 시 `ADMIN` role 재검증

## 2. 최종 도착 상태

현재 저장소의 최종 도착 상태를 먼저 고정하겠습니다.

### 2-1. 회원 모델은 최소 필드만 가진다

[Member.java](../src/main/java/com/worldmap/auth/domain/Member.java)는 아래만 가집니다.

- `nickname`
- `passwordHash`
- `role`
- `createdAt`
- `lastLoginAt`

이 정도면 현재 프로젝트 목적에는 충분합니다.

- ownership 귀속
- `/mypage`
- admin 진입
- local/prod bootstrap

### 2-2. 인증은 `nickname + password` 기반이다

현재 회원가입/로그인은 [MemberAuthService.java](../src/main/java/com/worldmap/auth/application/MemberAuthService.java)가 담당합니다.

- signup: 닉네임 정규화, 중복 확인, 비밀번호 정책 확인, hash 저장
- login: 닉네임 정규화, 회원 조회, password hash 비교, `lastLoginAt` 갱신

### 2-3. 로그인 뒤에는 세션 ID를 회전한다

[MemberSessionManager.java](../src/main/java/com/worldmap/auth/application/MemberSessionManager.java)의 `signIn(...)`는 무조건 `request.changeSessionId()`를 호출합니다.

즉 로그인 성공 뒤 기존 session id를 그대로 유지하지 않습니다.

### 2-4. 세션에는 현재 member snapshot만 저장한다

현재 member session은 아래 세 attribute로 구성됩니다.

- `WORLDMAP_MEMBER_ID`
- `WORLDMAP_MEMBER_NICKNAME`
- `WORLDMAP_MEMBER_ROLE`

이 세션 값은 "권한의 최종 source of truth"가 아니라 **현재 요청을 해석하는 단서**입니다.

실제 접근 제어에서는 필요할 때 DB를 다시 봅니다.

### 2-5. signup/login 직후 guest claim이 이어진다

[AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)는 인증 성공 뒤 곧바로 [GuestProgressClaimService.java](../src/main/java/com/worldmap/auth/application/GuestProgressClaimService.java)를 호출합니다.

즉 이 글의 auth는 blog 11의 guest ownership과 바로 이어집니다.

```text
인증 성공
-> 현재 guest key 조회
-> 있으면 claim
-> signIn(changeSessionId)
-> redirect
```

### 2-6. admin 계정은 bootstrap으로 준비할 수 있다

현재는 수동 SQL 없이도 설정만 있으면 bootstrap admin을 만들 수 있습니다.

- local: 기본값으로 켜짐
- prod: 기본값으로 꺼짐

관련 클래스는 아래입니다.

- [AdminBootstrapProperties.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapProperties.java)
- [AdminBootstrapService.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapService.java)
- [AdminBootstrapInitializer.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapInitializer.java)

### 2-7. `/dashboard`는 로그인 여부와 admin 여부를 분리해서 검사한다

현재 `/dashboard` 접근은 두 단계로 나뉩니다.

1. 비로그인인가
2. 로그인은 됐지만 현재 DB role이 `ADMIN`인가

이 경계는 아래 조합으로 구현합니다.

- [AdminAccessInterceptor.java](../src/main/java/com/worldmap/admin/web/AdminAccessInterceptor.java)
- [AdminAccessGuard.java](../src/main/java/com/worldmap/auth/application/AdminAccessGuard.java)
- [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)

즉 session 안에 `ADMIN` 문자열이 남아 있어도, 현재 DB member row가 `USER`로 강등되면 접근을 막습니다.

## 3. 시작 상태

이 글을 쓰기 전의 흔한 잘못된 시작 상태는 대체로 아래 둘 중 하나입니다.

### 3-1. guest는 있는데 member는 없는 상태

blog 11까지 구현하면 guest ownership은 이미 존재합니다.

- `guestSessionKey`
- guest 플레이
- guest claim

하지만 member model 자체가 없으면 아래를 설명할 수 없습니다.

- `/mypage`
- `/dashboard`
- role 기반 운영 접근

즉 guest ownership만으로는 "기록을 계정으로 이어 간다"는 제품 설명이 반쯤만 완성됩니다.

### 3-2. member는 있는데 세션/권한 경계가 허술한 상태

반대로 단순 회원가입/로그인 폼만 넣으면 겉보기에는 auth가 된 것처럼 보입니다.

하지만 아래가 빠지면 금방 무너집니다.

- password hash
- session fixation 대응
- current role 재검증
- 안전한 `returnTo`
- bootstrap admin

즉 "로그인 버튼이 있다"와 "설명 가능한 인증 구조가 있다"는 전혀 다른 이야기입니다.

## 4. 먼저 알아둘 개념

### 4-1. simple auth

현재 프로젝트의 simple auth는 아래를 뜻합니다.

- identifier는 email이 아니라 nickname
- credential은 password 하나
- state 저장은 JWT가 아니라 server-side session

즉 로그인 복잡도를 의도적으로 낮추고, 게임/랭킹/추천이 중심인 제품의 설명 축을 지키는 선택입니다.

### 4-2. member session

member session은 로그인된 브라우저의 현재 회원 상태를 나타내는 session attribute 집합입니다.

현재 저장값은 아래 세 개뿐입니다.

- member id
- nickname
- role

이 값은 이후 SSR header, `/mypage`, `/dashboard` 진입 판단, game start 분기에서 재사용됩니다.

### 4-3. session fixation 대응

session fixation은 공격자가 미리 고정한 session id를 로그인 후에도 그대로 쓰게 만드는 문제입니다.

현재 저장소에서는 로그인 성공 뒤 `request.changeSessionId()`로 이를 줄입니다.

즉 "인증 전 세션"과 "인증 후 세션"의 경계를 명시적으로 나눕니다.

### 4-4. bootstrap admin

bootstrap admin은 운영 계정을 애플리케이션 시작 시점에 보장하는 방식입니다.

현재 프로젝트는 이 기능을 아래 목적에 한정해 둡니다.

- local demo에서 바로 `/dashboard`를 열기 위해
- prod에서도 첫 운영자 계정을 코드/설정 기준으로 재현 가능하게 하기 위해

### 4-5. current role revalidation

이건 auth의 핵심 개념입니다.

현재 세션에 `ADMIN`이 들어 있다고 해서 곧바로 관리자 접근을 허용하지 않습니다.

대신 현재 요청에서 다시 아래를 확인합니다.

1. 세션의 member attribute 형식이 정상인가
2. DB에 그 member row가 실제로 존재하는가
3. 현재 role이 정말 `ADMIN`인가

이 개념이 있어야 admin 권한 회수나 stale session을 제대로 설명할 수 있습니다.

### 4-6. `returnTo`

`returnTo`는 로그인 전에 보려던 화면으로 되돌리는 경로입니다.

현재는 주로 `/dashboard` 진입에서 사용됩니다.

하지만 아무 문자열이나 redirect하면 open redirect 문제가 생길 수 있으므로, [AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)는 아래만 허용합니다.

- `/`로 시작하는 내부 경로
- `//`로 시작하지 않는 값

그 외는 `/mypage`로 fallback합니다.

## 5. 이번 글에서 다룰 파일

이 글을 실제로 다시 만들려면 아래 파일들을 같이 봐야 합니다.

### 5-1. domain과 repository

- [Member.java](../src/main/java/com/worldmap/auth/domain/Member.java)
- [MemberRole.java](../src/main/java/com/worldmap/auth/domain/MemberRole.java)
- [MemberRepository.java](../src/main/java/com/worldmap/auth/domain/MemberRepository.java)

### 5-2. auth application layer

- [MemberCredentialPolicy.java](../src/main/java/com/worldmap/auth/application/MemberCredentialPolicy.java)
- [MemberPasswordHasher.java](../src/main/java/com/worldmap/auth/application/MemberPasswordHasher.java)
- [MemberAuthService.java](../src/main/java/com/worldmap/auth/application/MemberAuthService.java)
- [AuthenticatedMemberSession.java](../src/main/java/com/worldmap/auth/application/AuthenticatedMemberSession.java)
- [MemberSessionManager.java](../src/main/java/com/worldmap/auth/application/MemberSessionManager.java)
- [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)
- [AdminAccessGuard.java](../src/main/java/com/worldmap/auth/application/AdminAccessGuard.java)

### 5-3. web layer

- [AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)
- [LoginForm.java](../src/main/java/com/worldmap/auth/web/LoginForm.java)
- [SignupForm.java](../src/main/java/com/worldmap/auth/web/SignupForm.java)
- [AdminAccessInterceptor.java](../src/main/java/com/worldmap/admin/web/AdminAccessInterceptor.java)
- [AdminWebConfig.java](../src/main/java/com/worldmap/admin/web/AdminWebConfig.java)
- [AdminPageController.java](../src/main/java/com/worldmap/admin/web/AdminPageController.java)
- [LegacyAdminRedirectController.java](../src/main/java/com/worldmap/admin/web/LegacyAdminRedirectController.java)

### 5-4. bootstrap과 설정

- [AdminBootstrapProperties.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapProperties.java)
- [AdminBootstrapService.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapService.java)
- [AdminBootstrapInitializer.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapInitializer.java)
- [application-local.yml](../src/main/resources/application-local.yml)
- [application-prod.yml](../src/main/resources/application-prod.yml)

### 5-5. SSR 템플릿

- [login.html](../src/main/resources/templates/auth/login.html)
- [signup.html](../src/main/resources/templates/auth/signup.html)

### 5-6. 테스트

- [AuthFlowIntegrationTest.java](../src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java)
- [MemberSessionManagerTest.java](../src/test/java/com/worldmap/auth/application/MemberSessionManagerTest.java)
- [CurrentMemberAccessServiceTest.java](../src/test/java/com/worldmap/auth/application/CurrentMemberAccessServiceTest.java)
- [AdminBootstrapIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminBootstrapIntegrationTest.java)
- [AdminBootstrapServiceTest.java](../src/test/java/com/worldmap/admin/application/AdminBootstrapServiceTest.java)
- [AdminPageIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)
- [AdminAccessGuardTest.java](../src/test/java/com/worldmap/auth/application/AdminAccessGuardTest.java)

## 6. 핵심 도메인 모델과 상태를 먼저 고정하기

### 6-1. `Member`는 최소 계정 모델이다

[Member.java](../src/main/java/com/worldmap/auth/domain/Member.java)는 `member_account` 테이블을 나타냅니다.

핵심 필드는 아래입니다.

| 필드 | 의미 |
| --- | --- |
| `id` | 회원 PK |
| `nickname` | 로그인 identifier이자 현재 프로젝트의 표시 이름 |
| `passwordHash` | bcrypt hash |
| `role` | `USER` 또는 `ADMIN` |
| `createdAt` | 생성 시각 |
| `lastLoginAt` | 최근 로그인 시각 |

이 모델이 최소 구조인 이유는 아래와 같습니다.

- email 없이도 로그인 가능
- role 하나만으로 `/dashboard` 접근 설명 가능
- `lastLoginAt`으로 로그인 이벤트 추적 가능

### 6-2. role은 enum 두 값만 가진다

[MemberRole.java](../src/main/java/com/worldmap/auth/domain/MemberRole.java)는 아래 둘만 가집니다.

- `USER`
- `ADMIN`

현재 프로젝트 범위에서는 이 정도면 충분합니다.

- guest는 member가 아니므로 role 바깥의 상태다
- 운영 화면은 `ADMIN` 하나만 구분하면 된다

### 6-3. session state는 member snapshot이다

[MemberSessionManager.java](../src/main/java/com/worldmap/auth/application/MemberSessionManager.java)는 아래 세 attribute를 session에 저장합니다.

- `WORLDMAP_MEMBER_ID`
- `WORLDMAP_MEMBER_NICKNAME`
- `WORLDMAP_MEMBER_ROLE`

즉 session state는 member row 전체가 아니라, 현재 요청을 해석하는 데 필요한 최소 snapshot입니다.

### 6-4. auth 상태를 표로 정리하면

| 상태 | member row | session attribute | 의미 |
| --- | --- | --- | --- |
| guest | 없음 또는 있어도 로그인 안 함 | 없음 | 비회원 상태 |
| signed-in user | `role=USER` | id/nickname/role 저장 | 일반 member 상태 |
| signed-in admin | `role=ADMIN` | id/nickname/role 저장 | admin member 상태 |
| stale/revoked session | 세션에는 값이 있지만 DB row 없음 또는 role이 바뀜 | request마다 재검증 필요 | current member 재해석이 필요한 상태 |

### 6-5. admin bootstrap 상태

admin 계정은 아래 둘 중 하나입니다.

- 이미 존재하던 member를 `ADMIN`으로 승격
- 아직 없으면 새 `ADMIN` member 생성

즉 bootstrap은 "무조건 새 계정 insert"가 아닙니다.

이게 중요한 이유는 local/demo/prod 어디에서든 같은 nickname을 source of truth로 삼기 위해서입니다.

## 7. 왜 이 정도 auth 구조가 현재 프로젝트에 맞는가

### 7-1. 왜 OAuth가 아니라 simple auth인가

현재 프로젝트의 중심은 인증이 아닙니다.

- 서버 주도 게임 상태
- endless run
- Redis leaderboard
- recommendation engine

이런 축이 주인공입니다.

여기에 OAuth, refresh token, provider mapping, callback error handling까지 초기에 넣으면 설명 축이 흐려집니다.

simple auth를 고른 이유는 세 가지입니다.

1. 제품 목적과 맞다
2. 내가 직접 모든 계층을 설명할 수 있다
3. guest ownership에서 member ownership으로 이어지는 흐름을 가장 작게 닫을 수 있다

### 7-2. 왜 JWT가 아니라 session 기반인가

현재 public/admin surface는 SSR 비중이 높습니다.

- 홈
- `/mypage`
- `/dashboard`
- login/signup

이 구조에서는 session 기반이 더 단순합니다.

- 서버가 현재 member를 바로 읽기 쉽다
- Thymeleaf SSR과 잘 맞는다
- logout과 stale session cleanup을 한 곳에서 설명하기 쉽다

### 7-3. 왜 admin bootstrap을 같은 글에 넣는가

dashboard 상세 내용은 뒤 글에서 더 다루더라도, admin 진입점 자체는 member/role 모델 위에 있습니다.

즉 "admin 계정은 어디서 시작되는가"를 auth 기초 글에서 닫아 두는 편이 더 자연스럽습니다.

### 7-4. 왜 current role revalidation까지 같이 묶는가

겉보기에는 "로그인 글"과 "권한 재검증 글"이 따로 보일 수 있습니다.

하지만 실제 제품에서는 아래가 한 묶음입니다.

- member session을 어떻게 심는가
- 그 session을 어디까지 신뢰하는가
- admin 접근에서는 무엇을 다시 확인하는가

그래서 이 글은 simple auth에서 끝나지 않고 admin entry까지 같이 설명해야 현재 코드와 맞습니다.

## 8. credential policy와 password hashing

### 8-1. `MemberCredentialPolicy`가 하는 일

[MemberCredentialPolicy.java](../src/main/java/com/worldmap/auth/application/MemberCredentialPolicy.java)는 credential 입력 규칙을 고정합니다.

#### nickname 규칙

- null 불가
- trim 후 2자 이상 20자 이하
- 공백 포함 불가

#### password 규칙

- null/blank 불가
- 4자 이상 100자 이하

이 규칙을 service 쪽에도 두는 이유는 폼 검증만으로는 충분하지 않기 때문입니다.

예를 들어 HTML 폼을 우회한 요청이 들어와도 service가 마지막 방어선을 가져야 합니다.

### 8-2. 왜 nickname 정규화를 service에서 다시 하는가

`normalizeNickname(rawNickname)`은 trim을 수행합니다.

즉 아래를 같은 identifier로 본다는 뜻입니다.

- `" orbit_runner "`
- `"orbit_runner"`

현재 프로젝트는 닉네임을 case-insensitive로 조회하므로, identifier 처리 규칙이 auth controller마다 갈리지 않게 service 정책으로 묶는 편이 맞습니다.

### 8-3. `MemberPasswordHasher`가 하는 일

[MemberPasswordHasher.java](../src/main/java/com/worldmap/auth/application/MemberPasswordHasher.java)는 `BCryptPasswordEncoder`를 감싼 작은 컴포넌트입니다.

역할은 두 가지뿐입니다.

- `hash(rawPassword)`
- `matches(rawPassword, passwordHash)`

이렇게 한 겹 감싼 이유는 나중에 구현을 바꾸더라도 auth service가 encoder 구체 타입에 묶이지 않게 하기 위해서입니다.

### 8-4. 왜 password hash가 domain이 아니라 application에 있나

비밀번호 hashing은 member entity의 고유 도메인 규칙이라기보다, 인증 애플리케이션 계층이 외부 라이브러리를 통해 수행하는 기술적 작업입니다.

즉 `Member`가 BCrypt를 직접 알 필요는 없습니다.

## 9. `MemberAuthService`가 실제로 하는 일

[MemberAuthService.java](../src/main/java/com/worldmap/auth/application/MemberAuthService.java)는 signup/login의 핵심 규칙을 담당합니다.

### 9-1. signup 흐름

`signUp(rawNickname, rawPassword)`는 아래 순서입니다.

1. `normalizeNickname(rawNickname)`
2. `validatePassword(rawPassword)`
3. `existsByNicknameIgnoreCase(nickname)`로 중복 확인
4. `memberPasswordHasher.hash(rawPassword)`
5. `Member.create(nickname, passwordHash, USER)`
6. `member.markLoggedIn(now)`
7. 저장

여기서 중요한 점은 signup 직후에도 `lastLoginAt`을 찍는다는 것입니다.

즉 회원가입은 동시에 첫 로그인 이벤트이기도 합니다.

### 9-2. login 흐름

`login(rawNickname, rawPassword)`는 아래 순서입니다.

1. 닉네임 정규화
2. 비밀번호 형식 검증
3. `findByNicknameIgnoreCase(nickname)`
4. `memberPasswordHasher.matches(...)`
5. `member.markLoggedIn(now)`
6. member 반환

여기서 service는 session을 직접 만지지 않습니다.

즉 인증 규칙과 session 상태 전환을 분리합니다.

### 9-3. 왜 controller가 아니라 service여야 하는가

`MemberAuthService`가 service여야 하는 이유는 분명합니다.

- HTTP form binding은 auth의 본질이 아니다
- 닉네임 정규화, 중복 체크, hash 비교는 요청 형식과 무관하다
- 나중에 API/CLI/bootstrap 등 다른 진입점이 생겨도 같은 규칙을 재사용할 수 있다

즉 controller는 입력과 redirect를 다루고, auth 규칙은 service에 둡니다.

### 9-4. signup/login이 직접 role을 결정하는 이유

현재 signup은 무조건 `USER`를 생성합니다.

admin은 signup에서 만들지 않습니다.

admin이 생기는 경로는 별도로 제한합니다.

- bootstrap admin provisioning
- 기존 member 승격

이렇게 경로를 분리해야 일반 사용자 signup 흐름과 운영자 provisioning 흐름을 설명하기 쉽습니다.

## 10. `MemberSessionManager`가 실제로 하는 일

[MemberSessionManager.java](../src/main/java/com/worldmap/auth/application/MemberSessionManager.java)는 현재 auth 구조의 중심입니다.

### 10-1. `signIn(request, member)`

이 메서드는 두 단계입니다.

1. `request.changeSessionId()`
2. `syncMember(httpSession, member)`

즉 signIn은 단순 attribute 세팅이 아니라 session fixation 대응까지 포함한 작업입니다.

### 10-2. `syncMember(session, member)`

이 메서드는 현재 member row를 session snapshot으로 씁니다.

- id
- nickname
- role

이 메서드가 따로 있는 이유는 [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)가 DB의 최신 nickname/role을 다시 읽은 뒤 session을 덮어쓸 수 있게 하기 위해서입니다.

### 10-3. `signOut(session)`

signOut은 아래 세 attribute를 지웁니다.

- member id
- member nickname
- member role

현재 프로젝트는 logout 시 session invalidate 대신, 현재 member snapshot만 지우는 쪽을 택합니다.

### 10-4. `currentMember(session)`

이 메서드는 session에 저장된 값을 읽어 [AuthenticatedMemberSession.java](../src/main/java/com/worldmap/auth/application/AuthenticatedMemberSession.java)으로 바꿉니다.

형식이 깨진 경우에는 `Optional.empty()` 또는 `MemberRole.valueOf(...)` 예외가 날 수 있습니다.

이 예외는 상위의 [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)가 받아서 stale session 정리에 사용합니다.

### 10-5. 왜 session manager를 따로 두는가

세션 조작이 controller 곳곳에 흩어지면 금방 아래 문제가 생깁니다.

- session id 회전 누락
- attribute 이름 불일치
- signOut 규칙 불일치
- current member parsing 중복

그래서 `HttpSession` 관련 규칙을 한 클래스로 모았습니다.

## 11. SSR auth 화면과 `AuthPageController`

[AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)는 auth의 web entrypoint입니다.

### 11-1. GET `/signup`

흐름:

1. 현재 member가 이미 있으면 `/mypage`로 redirect
2. 없으면 빈 `SignupForm`을 model에 넣고 `auth/signup` 렌더

[signup.html](../src/main/resources/templates/auth/signup.html)는 이 구조를 그대로 보여 줍니다.

- 닉네임 입력
- 비밀번호 입력
- "이 브라우저의 guest 기록도 계정으로 이어진다"는 설명

즉 guest claim이 signup UX 설명 안에 이미 들어가 있습니다.

### 11-2. POST `/signup`

흐름:

1. bean validation error 확인
2. `memberAuthService.signUp(...)`
3. 현재 guest key가 있으면 claim
4. `memberSessionManager.signIn(request, member)`
5. `/mypage` redirect

에러가 나면 같은 signup 화면으로 돌아가고 `authErrorMessage`를 model에 남깁니다.

### 11-3. GET `/login`

흐름:

1. 현재 member가 있으면 `/mypage` redirect
2. 없으면 `LoginForm`과 `returnTo`를 model에 넣고 `auth/login` 렌더

[login.html](../src/main/resources/templates/auth/login.html)는 `returnTo`가 있을 때 아래 안내 메시지를 보여 줍니다.

- "로그인 후 원래 보려던 Dashboard 화면으로 다시 이동합니다."

즉 admin entry와 auth entry가 같은 폼에서 연결됩니다.

### 11-4. POST `/login`

흐름:

1. bean validation error 확인
2. `memberAuthService.login(...)`
3. 현재 guest key가 있으면 claim
4. `memberSessionManager.signIn(request, member)`
5. `resolvePostLoginRedirect(returnTo)` 결과로 redirect

### 11-5. POST `/logout`

흐름:

1. `memberSessionManager.signOut(httpSession)`
2. `guestSessionKeyManager.rotateGuestSessionKey(httpSession)`
3. `/mypage` redirect

즉 logout은 auth 글이면서 동시에 guest ownership 글과도 닿아 있습니다.

### 11-6. 왜 controller는 claim과 redirect만 조합하고, auth 규칙은 service에 둬야 하나

controller는 아래만 맡습니다.

- 폼 바인딩
- validation error 처리
- redirect 결정
- guest claim과 signIn 순서 조합

실제 규칙은 다른 계층에 있습니다.

- credential 검증: `MemberAuthService`
- session state transition: `MemberSessionManager`
- guest ownership migration: `GuestProgressClaimService`

이 경계가 있어야 auth 코드가 커져도 설명이 유지됩니다.

## 12. `returnTo`가 왜 중요한가

`returnTo`는 작은 기능처럼 보이지만, admin entry UX와 보안 둘 다에 걸립니다.

### 12-1. 사용 시나리오

비로그인 사용자가 `/dashboard`를 열면 [AdminAccessInterceptor.java](../src/main/java/com/worldmap/admin/web/AdminAccessInterceptor.java)가 아래로 redirect합니다.

```text
/login?returnTo=/dashboard
```

그 뒤 로그인에 성공하면 [AuthPageController.java](../src/main/java/com/worldmap/auth/web/AuthPageController.java)의 `resolvePostLoginRedirect(returnTo)`가 `/dashboard`로 다시 보냅니다.

### 12-2. 왜 검증이 필요한가

만약 `returnTo`를 그대로 redirect에 쓰면 아래 같은 문제가 생길 수 있습니다.

- 외부 URL로 redirect
- `//evil.example.com` 같은 open redirect

그래서 현재 구현은 아래만 허용합니다.

- 빈 값이면 `/mypage`
- `/`로 시작하지 않으면 `/mypage`
- `//`로 시작하면 `/mypage`
- 그 외의 내부 경로만 허용

### 12-3. 이 로직이 왜 controller에 있나

`returnTo`는 HTTP request parameter이자 redirect 정책입니다.

즉 domain 규칙이 아니라 web layer 정책이므로 controller private method로 두는 편이 맞습니다.

## 13. bootstrap admin provisioning

이제 admin entry의 시작점을 봅니다.

### 13-1. bootstrap property contract

[AdminBootstrapProperties.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapProperties.java)는 아래 세 값을 가집니다.

- `enabled`
- `nickname`
- `password`

즉 bootstrap admin은 복잡한 설정 객체가 아니라 "켜는가, 어떤 nickname인가, 어떤 password인가"로만 정의됩니다.

### 13-2. local과 prod 설정 차이

[application-local.yml](../src/main/resources/application-local.yml)에서는 기본값이 아래입니다.

- `enabled=true`
- `nickname=worldmap_admin`
- `password=secret123`

즉 로컬 데모에서는 바로 admin 계정이 생기도록 기본값을 켭니다.

[application-prod.yml](../src/main/resources/application-prod.yml)에서는 기본값이 아래입니다.

- `enabled=false`
- `nickname=` 빈 값
- `password=` 빈 값

즉 운영에서는 명시적으로 환경 변수를 넣지 않으면 bootstrap이 일어나지 않습니다.

### 13-3. `AdminBootstrapService`의 실제 규칙

[AdminBootstrapService.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapService.java)는 `ensureBootstrapAdmin()` 한 메서드로 끝납니다.

순서는 아래입니다.

1. `enabled`가 false면 바로 반환
2. nickname 정규화
3. password 정책 검증
4. password hash 생성
5. 같은 nickname 회원을 조회
6. 있으면 `provisionAdmin(passwordHash)`로 승격
7. 없으면 새 `ADMIN` member 생성

즉 bootstrap admin은 단순 insert가 아니라 **create or promote** 규칙입니다.

### 13-4. 왜 기존 member를 승격하는 경로를 두는가

이 경로가 없으면 아래가 번거로워집니다.

- local/demo에서 이미 만들어 둔 member를 admin으로 바꾸고 싶을 때
- 운영에서 bootstrap nickname을 기존 사용자와 맞추고 싶을 때

따라서 같은 nickname을 source of truth로 삼고, 있으면 승격하는 편이 설명 가능성이 높습니다.

### 13-5. 왜 ApplicationRunner로 연결하는가

[AdminBootstrapInitializer.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapInitializer.java)는 `ApplicationRunner`입니다.

즉 앱 시작이 끝난 뒤 아래를 수행합니다.

```text
run()
-> adminBootstrapService.ensureBootstrapAdmin()
```

이 구조를 택한 이유는 단순합니다.

- 런타임 첫 진입 전에 admin 계정이 준비되어야 한다
- 별도 HTTP endpoint를 만들 필요가 없다
- "앱이 올라오면서 운영자 계정을 보장한다"는 설명이 쉬워진다

## 14. `/dashboard` 접근 제어

admin entry는 계정 생성만으로 끝나지 않습니다.
실제로 들어갈 수 있어야 합니다.

### 14-1. interceptor가 먼저 가른다

[AdminWebConfig.java](../src/main/java/com/worldmap/admin/web/AdminWebConfig.java)는 아래 경로에 [AdminAccessInterceptor.java](../src/main/java/com/worldmap/admin/web/AdminAccessInterceptor.java)를 붙입니다.

- `/dashboard`
- `/dashboard/**`
- `/admin`
- `/admin/**`

즉 admin surface는 공통 인터셉터 앞단을 통과해야 합니다.

### 14-2. 비로그인 사용자는 login으로 redirect한다

[AdminAccessInterceptor.java](../src/main/java/com/worldmap/admin/web/AdminAccessInterceptor.java)의 `preHandle(...)`은 `request.getSession(false)`가 없으면 바로 login redirect를 보냅니다.

redirect target은 아래 형식입니다.

```text
/login?returnTo=<현재 URI>
```

즉 admin entry는 강제로 로그인 화면을 경유합니다.

### 14-3. 로그인된 사용자는 `AdminAccessGuard`로 다시 검증한다

세션이 있다고 곧바로 허용하지 않습니다.

[AdminAccessGuard.java](../src/main/java/com/worldmap/auth/application/AdminAccessGuard.java)가 아래를 다시 봅니다.

1. 현재 member가 있는가
2. 현재 role이 `ADMIN`인가

### 14-4. 왜 guard가 session 문자열을 그대로 믿지 않는가

`AdminAccessGuard`는 내부에서 [CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)를 사용합니다.

즉 아래를 다시 확인합니다.

- 세션 attribute가 정상인가
- DB에 그 member가 실제로 있는가
- 현재 role이 무엇인가

이 때문에 아래 시나리오를 막을 수 있습니다.

- 예전에 admin이었지만 role이 USER로 강등된 경우
- member가 삭제된 경우
- 세션 role 문자열이 깨진 경우

### 14-5. non-admin은 왜 redirect가 아니라 403인가

현재 정책은 아래처럼 나뉩니다.

- 비로그인: 로그인하면 해결될 수 있으므로 login redirect
- 로그인했지만 권한 부족: 로그인 문제는 아니므로 `403 Forbidden`

이 차이를 분명히 해야 운영 화면 접근 규칙이 설명됩니다.

### 14-6. legacy `/admin` 경로는 왜 남겨 뒀는가

[LegacyAdminRedirectController.java](../src/main/java/com/worldmap/admin/web/LegacyAdminRedirectController.java)는 예전 `/admin` 진입점을 `/dashboard` 쪽으로 보냅니다.

즉 운영 surface의 canonical path는 `/dashboard`이지만, 이전 링크나 습관적인 진입은 아직 깨지지 않게 유지합니다.

## 15. current member와 current role 재검증

이 부분이 없으면 simple auth 글이 너무 얕아집니다.

### 15-1. `CurrentMemberAccessService`가 하는 일

[CurrentMemberAccessService.java](../src/main/java/com/worldmap/auth/application/CurrentMemberAccessService.java)는 현재 request 또는 session을 기준으로 "진짜 현재 회원"을 다시 해석합니다.

순서는 아래입니다.

1. `MemberSessionManager.currentMember(httpSession)`로 session snapshot 파싱
2. `memberRepository.findById(memberId)`로 DB row 재조회
3. 없으면 `signOut(httpSession)` 하고 empty
4. 있으면 최신 nickname/role로 `syncMember(...)`
5. request scope에서는 결과를 attribute cache에 저장

### 15-2. 왜 auth 글에서도 이 서비스가 중요하나

로그인은 session snapshot을 **만드는 일**입니다.
하지만 운영 환경에서는 그 snapshot을 **언제까지 신뢰할지**가 더 중요합니다.

즉 auth는 아래 두 단계로 봐야 합니다.

- 로그인 성공 시 snapshot 생성
- 이후 요청에서 snapshot을 현재 DB 상태로 재해석

### 15-3. admin entry와의 관계

admin entry는 이 서비스 없이는 설명이 성립하지 않습니다.

세션만 믿으면 아래 문제가 생깁니다.

- 강등된 admin이 계속 `/dashboard`에 들어갈 수 있다
- 삭제된 member가 로그인된 것처럼 보인다

현재는 `CurrentMemberAccessService -> AdminAccessGuard` 조합으로 이 문제를 막습니다.

## 16. SSR 템플릿은 무엇을 설명하고 있나

### 16-1. `login.html`

[login.html](../src/main/resources/templates/auth/login.html)는 단순 폼이 아닙니다.

본문 copy에서 이미 현재 정책을 설명합니다.

- 이 브라우저 guest 기록이 현재 계정으로 이어진다
- `returnTo`가 있으면 dashboard로 돌아간다

즉 템플릿도 제품 정책을 숨기지 않습니다.

### 16-2. `signup.html`

[signup.html](../src/main/resources/templates/auth/signup.html)는 account의 목적을 명시합니다.

- 커뮤니티가 아닌 기록 유지 목적
- 현재 브라우저의 비회원 기록도 전환

이건 작은 것처럼 보여도 중요합니다.

왜냐하면 이 프로젝트의 auth가 "대형 서비스의 auth 복제"가 아니라 "이 제품에 맞는 최소 계정 모델"이라는 점을 UI 카피에서도 드러내기 때문입니다.

## 17. 요청 흐름을 시퀀스로 정리하기

### 17-1. signup

```text
GET /signup
-> CurrentMemberAccessService.currentMember(request)
-> 이미 로그인 상태면 /mypage redirect
-> 아니면 signup form SSR

POST /signup
-> bean validation
-> MemberAuthService.signUp(nickname, password)
-> GuestSessionKeyManager.currentGuestSessionKey(session)
-> 있으면 GuestProgressClaimService.claimGuestRecords(memberId, guestSessionKey)
-> MemberSessionManager.signIn(request, member)
-> request.changeSessionId()
-> session attribute sync
-> redirect:/mypage
```

### 17-2. login

```text
GET /login?returnTo=/dashboard
-> CurrentMemberAccessService.currentMember(request)
-> 이미 로그인 상태면 /mypage redirect
-> 아니면 login form SSR + returnTo model

POST /login
-> bean validation
-> MemberAuthService.login(nickname, password)
-> GuestSessionKeyManager.currentGuestSessionKey(session)
-> 있으면 GuestProgressClaimService.claimGuestRecords(memberId, guestSessionKey)
-> MemberSessionManager.signIn(request, member)
-> request.changeSessionId()
-> session attribute sync
-> resolvePostLoginRedirect(returnTo)
-> redirect:/dashboard 또는 /mypage
```

### 17-3. logout

```text
POST /logout
-> MemberSessionManager.signOut(session)
-> GuestSessionKeyManager.rotateGuestSessionKey(session)
-> redirect:/mypage
```

### 17-4. admin bootstrap

```text
애플리케이션 시작
-> AdminBootstrapInitializer.run()
-> AdminBootstrapService.ensureBootstrapAdmin()
-> enabled=false면 종료
-> nickname/password 정책 검증
-> 기존 member 있으면 ADMIN 승격
-> 없으면 ADMIN 새 생성
```

### 17-5. `/dashboard` 접근

```text
GET /dashboard
-> AdminAccessInterceptor.preHandle()
-> session 없으면 /login?returnTo=/dashboard redirect
-> 있으면 AdminAccessGuard.authorize(request)
-> CurrentMemberAccessService.currentMember(request)
-> current member 없음 -> unauthenticated
-> role != ADMIN -> forbidden
-> role == ADMIN -> allow
-> AdminPageController가 dashboard SSR
```

## 18. 실패 케이스와 예외 처리

### 18-1. nickname 중복 signup

[MemberAuthService.java](../src/main/java/com/worldmap/auth/application/MemberAuthService.java)는 `existsByNicknameIgnoreCase(...)`가 true면 `IllegalStateException("이미 사용 중인 닉네임입니다.")`를 던집니다.

controller는 이 메시지를 signup 화면에 노출합니다.

### 18-2. password 정책 미달

`MemberCredentialPolicy.validatePassword(...)`가 즉시 `IllegalArgumentException`을 던집니다.

즉 "나중에 hash 단계에서 실패"가 아니라 정책 위반을 먼저 분리합니다.

### 18-3. 잘못된 비밀번호 login

현재 login 실패 메시지는 의도적으로 하나로 통일됩니다.

- 회원 없음
- hash mismatch

둘 다 `"닉네임 또는 비밀번호가 올바르지 않습니다."`

즉 user enumeration을 줄이는 방향입니다.

### 18-4. `returnTo`가 외부 URL이거나 `//`로 시작

`resolvePostLoginRedirect(...)`가 `/mypage`로 fallback합니다.

즉 외부 redirect를 허용하지 않습니다.

### 18-5. 비로그인 `/dashboard` 접근

[AdminAccessInterceptor.java](../src/main/java/com/worldmap/admin/web/AdminAccessInterceptor.java)는 login redirect를 보냅니다.

### 18-6. 일반 USER의 `/dashboard` 접근

로그인은 되어 있어도 [AdminAccessGuard.java](../src/main/java/com/worldmap/auth/application/AdminAccessGuard.java)가 `FORBIDDEN`을 반환하고, interceptor가 `403`을 냅니다.

### 18-7. 세션에는 ADMIN이 남아 있는데 DB에서는 USER로 강등된 경우

[AdminPageIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)의 `adminRoutesRejectSessionsWhoseCurrentRoleWasRevoked`가 이 시나리오를 고정합니다.

즉 세션이 아니라 current DB role이 최종 기준입니다.

### 18-8. bootstrap enabled인데 password가 비어 있는 경우

[AdminBootstrapService.java](../src/main/java/com/worldmap/admin/application/AdminBootstrapService.java)는 [MemberCredentialPolicy.java](../src/main/java/com/worldmap/auth/application/MemberCredentialPolicy.java) 검증에서 즉시 실패합니다.

즉 잘못된 운영 입력을 조용히 무시하지 않습니다.

## 19. 테스트로 검증하기

이 글은 테스트 이름까지 같이 이해해야 재현이 됩니다.

### 19-1. `AuthFlowIntegrationTest`

[AuthFlowIntegrationTest.java](../src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java)는 auth와 guest claim, current member fallback이 한 브라우저 세션에서 어떻게 이어지는지 보여 줍니다.

#### `signupCreatesSimpleAccountAndKeepsMemberSession`

막는 리스크:

- signup 뒤 raw password 저장
- session id 미회전
- member session attribute 미설정

검증 내용:

- password hash가 raw password와 다름
- `lastLoginAt`이 채워짐
- session id가 바뀜
- `/mypage`에서 로그인 상태 UI가 보임

#### `loginFailureStaysOnLoginPageWithErrorMessage`

막는 리스크:

- login 실패가 redirect로 흘러 에러 메시지가 사라지는 문제

#### `adminLoginRedirectsBackToRequestedDashboardRoute`

막는 리스크:

- admin entry 시 login 뒤 원래 보던 화면으로 복귀하지 못하는 문제

검증 내용:

- `returnTo=/dashboard` login 뒤 `/dashboard` redirect
- session id 회전

#### `deletedMemberSessionFallsBackToGuestStateOnHomeAndMyPage`

막는 리스크:

- 삭제된 회원 세션이 계속 로그인 UI를 보이는 문제

#### `deletedMemberSessionCanOpenLoginPageInsteadOfRedirectingToMyPage`

막는 리스크:

- stale member session 때문에 login 화면을 못 보는 문제

#### `deletedMemberSessionStartsNewGameAsGuestOwnership`

막는 리스크:

- stale member가 새 game session을 invalid memberId로 시작하는 문제

### 19-2. `MemberSessionManagerTest`

[MemberSessionManagerTest.java](../src/test/java/com/worldmap/auth/application/MemberSessionManagerTest.java)는 session contract 자체를 고정합니다.

#### `signInRotatesSessionIdAndStoresMemberAttributes`

막는 리스크:

- session fixation
- attribute 누락

#### `syncMemberOverwritesSessionAttributesWithPersistedMemberState`

막는 리스크:

- nickname/role 최신 상태와 session snapshot이 어긋나는 문제

### 19-3. `AdminBootstrapIntegrationTest`

[AdminBootstrapIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminBootstrapIntegrationTest.java)는 실제 Spring context에서 아래를 검증합니다.

- `worldmap.admin.bootstrap.enabled=true`
- nickname/password 설정
- 앱 시작 뒤 해당 nickname의 member가 존재
- role이 `ADMIN`
- 저장된 hash가 입력 password와 match

즉 bootstrap이 진짜 startup 경로에서 작동하는지 보여 줍니다.

### 19-4. `AdminBootstrapServiceTest`

[AdminBootstrapServiceTest.java](../src/test/java/com/worldmap/admin/application/AdminBootstrapServiceTest.java)는 bootstrap 세부 규칙을 단위로 보여 줍니다.

#### `ensureBootstrapAdminCreatesAdminWhenMissing`

막는 리스크:

- admin이 없을 때 bootstrap이 아무 것도 하지 않는 문제

#### `ensureBootstrapAdminPromotesExistingMemberToAdmin`

막는 리스크:

- 기존 member가 있을 때 중복 row를 만드는 문제

#### `ensureBootstrapAdminFailsFastWhenEnabledWithoutPassword`

막는 리스크:

- 잘못된 운영 입력을 조용히 삼키는 문제

### 19-5. `AdminPageIntegrationTest`

[AdminPageIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java)는 실제 admin entry와 current-role revalidation을 보여 줍니다.

#### `adminDashboardRendersCurrentRecommendationOpsOverview`

막는 리스크:

- admin이 실제 dashboard page를 못 여는 문제

#### `adminRoutesRedirectUnauthenticatedUsersToLogin`

막는 리스크:

- 비로그인 사용자가 애매한 403/500을 보는 문제

#### `adminRoutesRejectNonAdminMembers`

막는 리스크:

- 일반 user가 dashboard에 들어가는 문제

#### `adminRoutesRejectSessionsWhoseCurrentRoleWasRevoked`

막는 리스크:

- 세션에 남아 있는 옛 ADMIN role 때문에 권한 회수 후에도 접근되는 문제

### 19-6. `AdminAccessGuardTest`

[AdminAccessGuardTest.java](../src/test/java/com/worldmap/auth/application/AdminAccessGuardTest.java)는 guard 자체의 경계를 보여 줍니다.

- current admin이면 `ALLOWED`
- current non-admin이면 `FORBIDDEN`
- current member가 비어 있으면 `UNAUTHENTICATED`
- request 기반 authorize도 current member 재사용 가능

즉 이 테스트 묶음은 simple auth, session rotation, bootstrap admin, current-role revalidation의 핵심 계약을 강하게 고정합니다.
다만 OAuth, email verification, password reset, 다중 기기 세션 관리 같은 확장 인증 시나리오까지 자동 증명하는 것은 아닙니다.
현재 범위는 의도적으로 `nickname + password + session + current-role guard`에 집중합니다.

## 20. 이 글만 보고 다시 구현하는 순서

### 20-1. 1단계: `Member`와 `MemberRole`을 만든다

먼저 아래만 있는 최소 member 모델을 만듭니다.

- nickname
- passwordHash
- role
- createdAt
- lastLoginAt

그리고 `USER`, `ADMIN` enum을 만듭니다.

### 20-2. 2단계: credential policy와 password hasher를 분리한다

폼 검증과는 별개로 service에서 재사용할 수 있게 아래를 만듭니다.

- `normalizeNickname`
- `validatePassword`
- `hash`
- `matches`

### 20-3. 3단계: `MemberAuthService`를 만든다

아래 두 메서드를 만듭니다.

- `signUp(rawNickname, rawPassword)`
- `login(rawNickname, rawPassword)`

이 단계에서 아래를 반드시 넣습니다.

- nickname normalize
- password validate
- duplicate nickname 차단
- hash 저장
- login 시 hash 비교
- `lastLoginAt` 갱신

### 20-4. 4단계: `MemberSessionManager`를 만든다

아래 세 메서드를 먼저 고정합니다.

- `signIn(request, member)`
- `syncMember(session, member)`
- `signOut(session)`

그리고 `signIn(...)`에는 반드시 `changeSessionId()`를 넣습니다.

### 20-5. 5단계: auth controller와 SSR form을 만든다

`/signup`, `/login`, `/logout`을 SSR 기준으로 열고,

- 로그인되어 있으면 `/mypage` redirect
- validation error는 같은 폼으로 복귀
- 성공 시 redirect

구조를 만듭니다.

### 20-6. 6단계: guest claim을 연결한다

blog 11과 연결되는 단계입니다.

```text
인증 성공
-> currentGuestSessionKey 읽기
-> 있으면 claim
-> signIn
```

이 순서를 넣어야 비회원 기록이 계정으로 이어집니다.

### 20-7. 7단계: bootstrap admin을 만든다

property, service, initializer를 추가합니다.

그리고 local/prod에서 기본값을 다르게 둡니다.

### 20-8. 8단계: admin interceptor/guard를 붙인다

`/dashboard`와 `/admin` 경로를 인터셉터로 묶고, guard에서 current member/current role을 다시 확인합니다.

### 20-9. 9단계: stale session 정리까지 연결한다

`CurrentMemberAccessService`가 session snapshot을 DB 기준으로 다시 해석하게 해야 admin entry가 진짜로 닫힙니다.

## 21. 구현 체크리스트

- `member_account` 모델이 있다
- nickname/password 기반 simple auth가 service에 있다
- password hash를 저장한다
- signup/login 뒤 `lastLoginAt`이 갱신된다
- 로그인 시 session id가 회전한다
- session에는 id/nickname/role snapshot만 저장한다
- signup/login은 현재 guest 기록 claim과 연결된다
- logout은 member snapshot을 지우고 guest key를 회전시킨다
- bootstrap admin은 설정값으로 생성 또는 승격된다
- `/dashboard`는 비로그인 redirect, 비-admin 403, current admin only 허용 규칙을 가진다
- stale/revoked role 세션은 DB 기준으로 다시 해석된다

## 22. 실행 / 검증 명령

이 글의 핵심 흐름은 아래 테스트 묶음으로 확인할 수 있습니다.

```bash
./gradlew test \
  --tests com.worldmap.auth.AuthFlowIntegrationTest \
  --tests com.worldmap.auth.application.MemberSessionManagerTest \
  --tests com.worldmap.auth.application.CurrentMemberAccessServiceTest \
  --tests com.worldmap.admin.AdminBootstrapIntegrationTest \
  --tests com.worldmap.admin.application.AdminBootstrapServiceTest \
  --tests com.worldmap.admin.AdminPageIntegrationTest \
  --tests com.worldmap.auth.application.AdminAccessGuardTest
```

## 23. 산출물 체크리스트

이 단계가 끝났다면 아래를 설명할 수 있어야 합니다.

- 왜 WorldMap은 simple auth가 현재 제품 범위에 맞는가
- 회원가입과 로그인 규칙이 왜 controller가 아니라 service에 있는가
- 로그인 뒤 세션 ID 회전이 왜 필요한가
- bootstrap admin이 local/prod에서 어떤 차이를 갖는가
- `/dashboard`가 왜 세션 role 문자열을 그대로 믿지 않는가
- role이 강등된 기존 admin 세션을 어떻게 막는가

## 24. 현재 구현의 한계

### 24-1. OAuth, 이메일 인증, 비밀번호 재설정은 없다

이건 기능 누락이 아니라 현재 범위 선택입니다.

지금 프로젝트의 핵심은 게임 도메인과 기록 ownership이지, 계정 기능 자체가 아닙니다.

### 24-2. 세션 기반 구조라 다중 기기 세션 관리 UI는 없다

현재는 "이 브라우저의 현재 session"을 설명하는 데 집중합니다.

- 모든 세션 보기
- 강제 로그아웃
- 세션 목록 관리

같은 기능은 아직 없습니다.

### 24-3. bootstrap admin과 role 재검증은 현재 member/account 모델 기준이다

즉 현재 테스트는 `member_account`와 session snapshot 위의 현재 구조를 강하게 고정하지만,
조직/권한 그룹/세분화된 RBAC 같은 더 큰 권한 모델까지 자동으로 대비하는 것은 아니다.

### 24-3. bootstrap admin은 운영 편의 기능이지 full admin lifecycle은 아니다

즉 아래는 아직 범위 밖입니다.

- 운영자 생성/회수 UI
- role audit log
- approval workflow

현재는 "첫 운영 진입점"을 설명 가능한 수준으로 고정하는 데 목적이 있습니다.

### 24-4. auth와 admin entry는 최소 모델이라 확장 시 문서도 같이 커져야 한다

예를 들어 OAuth를 붙이면 아래가 모두 바뀝니다.

- identifier 모델
- bootstrap admin 정책
- current member source
- session contract

즉 이 글의 단순함은 현재 스코프 위에서만 성립합니다.

## 25. 자주 막히는 지점

### 25-1. signup/login 규칙을 controller에 다 넣는 것

그렇게 하면 web layer와 auth 규칙이 섞입니다.

### 25-2. 로그인 뒤 session id 회전을 빼먹는 것

form login이 단순하다고 해서 fixation 대응을 빼면 안 됩니다.

### 25-3. 비밀번호 정책 검증을 프런트 폼에만 두는 것

service에도 같은 검증이 있어야 합니다.

### 25-4. bootstrap admin을 무조건 새 insert로만 생각하는 것

현재는 "create or promote"가 맞습니다.

### 25-5. `/dashboard` 접근에서 세션 role 문자열만 믿는 것

현재 DB role과 current member를 다시 봐야 합니다.

### 25-6. `returnTo`를 그대로 redirect하는 것

open redirect 문제가 생길 수 있습니다.

## 26. 이 글을 30초로 설명하면

WorldMap은 게임 기록 유지가 목적이라 `nickname + password + session` 기반 simple auth로 시작했습니다. `MemberAuthService`가 signup/login 규칙과 password hash를 맡고, `MemberSessionManager`가 로그인 뒤 `changeSessionId()`로 세션 경계를 다시 만든 다음 id/nickname/role snapshot을 심습니다. 운영자 계정은 `AdminBootstrapService`가 local/prod 설정에 따라 생성하거나 승격하고, `/dashboard`는 `AdminAccessInterceptor + AdminAccessGuard + CurrentMemberAccessService` 조합으로 현재 DB role을 다시 확인해 비로그인, 일반 user, 강등된 옛 admin session까지 구분해서 막습니다.

## 27. 면접에서 바로 받을 수 있는 꼬리 질문

### 27-1. 왜 OAuth가 아니라 simple auth였나요?

현재 제품의 핵심은 게임 도메인, 기록 ownership, leaderboard, recommendation입니다. simple auth가 guest claim, `/mypage`, `/dashboard`까지 설명하는 데 가장 작고 명확한 선택이었습니다.

### 27-2. 왜 session 기반을 택했나요?

현재 public/admin surface가 SSR 중심이고, 현재 member를 server-side에서 바로 읽는 편이 `/mypage`, `/dashboard`, guest claim, stale session cleanup을 설명하기 가장 쉽기 때문입니다.

### 27-3. 왜 로그인 직후 `changeSessionId()`를 넣었나요?

단순한 auth라도 인증 전 세션과 인증 후 세션의 경계를 분명히 나눠야 session fixation 위험을 줄일 수 있기 때문입니다.

### 27-4. admin 계정은 누가 언제 만들게 했나요?

`ApplicationRunner`인 `AdminBootstrapInitializer`가 앱 시작 시 `AdminBootstrapService.ensureBootstrapAdmin()`를 호출하게 했습니다. local은 기본값으로 켜고, prod는 환경 변수를 명시한 경우에만 켜지게 분리했습니다.

### 27-5. 세션에 `ADMIN`이 있으면 그냥 허용하면 안 되나요?

안 됩니다. 권한이 회수되거나 member row가 삭제된 상태에서도 session attribute는 남을 수 있기 때문에, `CurrentMemberAccessService`로 현재 DB member와 role을 다시 확인한 뒤 `AdminAccessGuard`가 최종 판정을 내려야 합니다.

## 28. 다음 글과의 연결

이 글은 member 계정과 admin entry의 최소 구조를 닫습니다.
다음 단계에서는 이 계정 구조를 바탕으로 실제 기록 허브와 공개/운영 read model을 어떻게 읽는지를 봐야 합니다.

- 앞선 guest ownership 글: [11-guest-session-ownership-and-progress-claim.md](./11-guest-session-ownership-and-progress-claim.md)
- 다음 글: [13-mypage-and-public-stats-read-models.md](./13-mypage-and-public-stats-read-models.md)
- 운영 권한 재검증 후속 글: [17-game-integrity-current-member-and-role-revalidation.md](./17-game-integrity-current-member-and-role-revalidation.md)
