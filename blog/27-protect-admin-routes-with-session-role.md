# 세션 role로 `/admin` 운영 화면 접근 제어 붙이기

## 왜 이 글을 쓰는가

`/admin` 화면을 따로 만들었다고 해서 운영 화면 분리가 끝난 것은 아니다.

정말 중요한 기준은 이거다.

- public 사용자가 이 화면에 들어올 수 없는가
- 일반 회원과 운영자를 어떻게 구분하는가
- 이 규칙을 어디에 두었는가

이번 단계에서는 이 질문에 답하기 위해 `/admin/**`를 실제로 보호했다.

## 이번 단계의 목표

현재 프로젝트는 Spring Security 전체를 아직 도입하지 않았다.

대신 이미 있는 단순 세션 로그인 구조 위에 `ADMIN` role 기반 진입 제한을 작게 얹는 것이 이번 목표다.

즉, 범위는 이 정도다.

- 비로그인 사용자는 `/login`으로 보낸다.
- 일반 `USER`는 `/admin`에 못 들어간다.
- `ADMIN`만 운영 화면에 들어간다.

## 바뀐 파일

- `src/main/java/com/worldmap/admin/web/AdminAccessInterceptor.java`
- `src/main/java/com/worldmap/admin/web/AdminWebConfig.java`
- `src/main/java/com/worldmap/auth/web/AuthPageController.java`
- `src/main/resources/templates/auth/login.html`
- `src/main/resources/templates/admin/index.html`
- `src/main/resources/templates/error/403.html`
- `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
- `src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java`

## 왜 컨트롤러가 아니라 인터셉터인가

이 규칙은 admin 화면의 비즈니스 로직이 아니다.

`AdminDashboardService`나 `RecommendationFeedbackService`가 알 필요도 없다.

이건 "요청이 admin 라우트에 들어오기 전에 공통으로 막아야 하는 정책"이다.

그래서 컨트롤러마다 이런 코드를 넣는 것은 맞지 않는다.

```java
if (notLoggedIn) { ... }
if (userRole) { ... }
```

이런 분기가 admin 메서드마다 반복되면 금방 지저분해진다.

그래서 이번 단계에서는 `HandlerInterceptor`로 묶었다.

## 요청 흐름

```text
GET /admin/**
-> AdminAccessInterceptor.preHandle()
-> MemberSessionManager.currentMember()
-> role 검사
-> 통과 시 AdminPageController
-> AdminDashboardService / RecommendationFeedbackService / AdminPersonaBaselineService
```

## 접근 규칙

### 1. 비로그인 사용자

세션이 없거나 로그인 정보가 없으면 로그인 페이지로 보낸다.

이때 그냥 `/login`으로만 보내지 않고, 원래 가려던 경로를 `returnTo`로 붙인다.

예:

```text
/admin -> /login?returnTo=/admin
```

### 2. 일반 USER

로그인은 했지만 role이 `USER`이면 운영 화면 접근을 막는다.

이 경우는 403으로 처리한다.

### 3. ADMIN

role이 `ADMIN`이면 정상적으로 admin 화면에 들어간다.

## 로그인 복귀 흐름도 같이 정리한 이유

운영자가 `/admin`으로 들어왔는데 로그인되어 있지 않다면,
로그인 후 다시 `/admin`으로 돌아가야 흐름이 자연스럽다.

그래서 로그인 폼은 `returnTo`를 그대로 들고 가고,
로그인 성공 후에는 안전한 내부 경로만 허용해서 그쪽으로 다시 보낸다.

이때 외부 redirect는 막았다.

- `/something` 형태만 허용
- `//evil.com` 같은 값은 무시
- 비어 있거나 이상하면 `/mypage`로 보냄

## 이 단계에서 중요한 도메인 설명

이번 단계에서 인증과 권한 체크는 분리해서 봐야 한다.

- 인증:
  - `MemberAuthService`
  - `MemberSessionManager`
- 권한 체크:
  - `AdminAccessInterceptor`

즉, 로그인 여부와 비밀번호 검증은 auth 쪽 책임이고,
admin 진입 가능 여부는 web entry policy다.

이 분리가 있어야 나중에 Spring Security로 바꾸더라도 역할을 설명하기 쉽다.

## 테스트

이번 단계에서 중요한 테스트는 두 개다.

### 1. `AdminPageIntegrationTest`

이 테스트는 세 가지를 본다.

- 비로그인 사용자는 `/login?returnTo=/admin`으로 가는가
- 일반 `USER`는 403으로 막히는가
- `ADMIN` 세션은 dashboard / feedback / baseline에 모두 들어가는가

### 2. `AuthFlowIntegrationTest`

이 테스트는 admin 사용자가 `returnTo=/admin`을 들고 로그인했을 때,
정말 `/admin`으로 다시 돌아가는지 확인한다.

## 지금 남아 있는 것

현재는 admin role을 부여하는 UI가 없다.

즉, admin 계정은 이런 방식 중 하나로 준비해야 한다.

- DB에서 role을 직접 `ADMIN`으로 변경
- bootstrap 스크립트
- 환경변수 기반 초기 admin 생성

이번 단계에서는 접근 제어 자체만 붙이고,
admin provisioning은 다음 판단으로 남겼다.

## 면접에서 어떻게 설명할까

이렇게 말하면 된다.

> 운영 화면은 컨트롤러마다 if문으로 막지 않고, `/admin/**` 앞에서 동작하는 `AdminAccessInterceptor`로 보호했습니다. 세션에 저장된 `memberId`, `nickname`, `role` 중 `role`을 읽어서 비로그인 사용자는 로그인으로 보내고, 일반 회원은 403으로 막고, `ADMIN`만 운영 화면에 진입시켰습니다. 이렇게 해서 기존 단순 세션 로그인 구조를 유지하면서도 운영 화면을 public과 권한 기준으로 분리했습니다.

## 다음 글 예고

다음 단계에서는 두 방향 중 하나로 갈 수 있다.

1. `/mypage`에 더 세밀한 전적 통계를 붙인다.
2. admin 계정 provisioning 방식을 고정한다.

현재 흐름상 다음 우선순위는 `/mypage` 세부 지표 또는 admin bootstrap 정리다.
