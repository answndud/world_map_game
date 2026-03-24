# 운영 화면을 `/dashboard`로 바꾸고 ADMIN만 헤더에서 노출하기

## 왜 이 글을 쓰는가

운영 화면이 필요하다고 해서 URL까지 꼭 `/admin`일 필요는 없다.

오히려 이런 문제가 생긴다.

- 플레이어 화면과 너무 동떨어져 보인다.
- 공개 제품보다 개발용 기능이 먼저 보이는 느낌이 난다.
- 헤더에서 `/admin` 링크를 바로 노출하면 제품 몰입이 깨진다.

이번 단계는 이 문제를 정리한다.

핵심 결론은 간단하다.

> 운영자는 `ADMIN` role로 로그인하고, 그때만 헤더에 `Dashboard` 버튼이 보이게 한다.

## 이전 단계의 한계

이전까지는 운영 화면 구조와 권한 제어 자체는 맞았다.

- `/admin/**` 접근 제어
- bootstrap admin 계정
- 추천 운영 화면
- baseline 화면

하지만 플레이어가 보는 제품 언어는 아니었다.

`/admin`이라는 주소와 버튼 이름은 내부 도구처럼 보이고,
공개 서비스 shell과 자연스럽게 이어지지 않았다.

## 이번 단계에서 바꾼 것

이번 단계에서는 세 가지를 같이 바꿨다.

1. 운영 화면 실제 진입 주소를 `/dashboard/**`로 변경
2. public 헤더에서 `ADMIN` 로그인일 때만 `Dashboard` 버튼 노출
3. 기존 `/admin/**`는 `/dashboard/**`로 임시 redirect

즉, 역할은 그대로 admin이지만
표면 언어는 `Dashboard`로 바꾼 것이다.

## 바뀐 파일

- `src/main/resources/templates/fragments/site-header.html`
- `src/main/resources/templates/fragments/admin-header.html`
- `src/main/java/com/worldmap/admin/web/AdminPageController.java`
- `src/main/java/com/worldmap/admin/web/LegacyAdminRedirectController.java`
- `src/main/java/com/worldmap/admin/web/AdminWebConfig.java`
- `src/main/java/com/worldmap/admin/application/AdminDashboardService.java`
- `src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java`
- `src/main/resources/templates/admin/index.html`
- `src/main/resources/templates/admin/recommendation-feedback.html`
- `src/main/resources/templates/admin/recommendation-persona-baseline.html`
- `src/main/resources/templates/auth/login.html`
- `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
- `src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java`
- `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
- `src/test/java/com/worldmap/web/HomeControllerTest.java`
- `src/test/java/com/worldmap/web/MyPageControllerTest.java`

## 요청 흐름

이번 단계의 핵심 흐름은 두 개다.

### 1. public 헤더

```text
페이지 렌더링
-> site-header fragment
-> session.WORLDMAP_MEMBER_ROLE 확인
-> ADMIN이면 Dashboard 링크 노출
-> USER / guest면 Home, My Page만 노출
```

### 2. 운영 화면 진입

```text
GET /dashboard/**
-> AdminAccessInterceptor
-> MemberSessionManager.currentMember()
-> ADMIN role 확인
-> AdminPageController
-> admin template 렌더링
```

기존 `/admin/**`는 바로 화면을 렌더링하지 않고
`/dashboard/**`로 redirect만 한다.

## 왜 컨트롤러가 아니라 fragment / interceptor / redirect controller로 나눴는가

이번 단계는 business logic보다 진입 구조를 정리하는 작업이다.

그래서 역할을 분리했다.

### 1. `site-header`

헤더에서 `Dashboard` 버튼을 보여줄지 말지는
페이지 공통 shell 규칙이다.

그래서 각 컨트롤러가 model에 플래그를 넣기보다
fragment에서 session role을 직접 읽는 편이 단순하다.

### 2. `AdminAccessInterceptor`

운영 화면 접근 권한은 여전히 라우트 입구의 공통 정책이다.

그래서 `/dashboard/**` 보호도 컨트롤러보다 interceptor가 맡는 것이 맞다.

### 3. `LegacyAdminRedirectController`

`/admin/**`를 바로 삭제하면

- 기존 북마크
- 예전 문서 링크
- 기존 테스트

가 한 번에 깨질 수 있다.

그래서 이번 단계에서는 redirect controller를 하나 두고
천천히 `/dashboard/**` 기준으로 옮기게 했다.

## 왜 `/admin`을 바로 지우지 않았는가

기술적으로는 바로 지워도 된다.

하지만 지금은 개발 중인 포트폴리오 프로젝트다.

이 시점에는

- 문서
- 테스트
- 수동 북마크

가 동시에 바뀌는 일이 잦다.

그래서 “새 기준은 `/dashboard`”로 고정하면서도
“옛 주소는 잠깐 redirect”로 남기는 편이 더 안전하다.

## 테스트

이번 단계에서 중요한 테스트는 네 가지다.

### 1. `HomeControllerTest`

guest 화면에서는 `Dashboard`가 안 보이고,
`ADMIN` 세션에서는 헤더에 `Dashboard`가 보이는지 확인했다.

### 2. `AdminPageIntegrationTest`

- `GET /dashboard` ADMIN 접근 성공
- 비로그인 사용자는 `/login?returnTo=/dashboard` redirect
- 일반 USER는 403
- `GET /admin`은 `/dashboard` redirect

를 고정했다.

### 3. `AuthFlowIntegrationTest`

admin 로그인 후 `returnTo=/dashboard`가 실제로 유지되는지 확인했다.

### 4. `RecommendationFeedbackIntegrationTest`

legacy public route인 `/recommendation/feedback-insights`가
이제 `/dashboard/recommendation/feedback`으로 redirect되는지 확인했다.

## 화면 언어를 어떻게 바꿨는가

운영 화면 자체도 `관리자 대시보드`보다
`운영 Dashboard` 중심으로 바꿨다.

이유는 현재 제품 shell 안에서
운영자도 같은 서비스 일부처럼 이동하는 느낌을 주기 위해서다.

즉, 권한은 `ADMIN`,
표면 언어는 `Dashboard`로 분리한 셈이다.

## 면접에서 어떻게 설명할까

이렇게 말하면 된다.

> 운영 화면은 필요했지만 `/admin`이라는 주소와 링크를 public shell에 그대로 노출하면 제품보다 개발용 화면이 먼저 보이는 문제가 있었습니다. 그래서 권한 모델은 그대로 `ADMIN`으로 두고, 실제 운영 진입 주소는 `/dashboard`로 바꿨습니다. public 헤더는 `ADMIN` 로그인 상태일 때만 `Dashboard` 버튼을 보여주고, 기존 `/admin/**`는 한동안 `/dashboard/**`로 redirect하게 만들어 북마크와 테스트를 안전하게 옮겼습니다. 권한 체크는 여전히 interceptor가 맡고, 헤더 노출은 fragment에서 session role을 읽는 방식으로 단순하게 유지했습니다.

## 다음 글 예고

다음 단계는 운영 화면에 실제 수치를 더 붙이는 것이다.

예를 들면 이런 값들이다.

- 전체 회원 수
- 오늘 활성 회원 수
- 오늘 활성 guest 수
- 오늘 완료된 게임 수

즉, 다음 조각은 “운영 화면 주소 정리”가 아니라
진짜 dashboard 지표를 붙이는 단계다.
