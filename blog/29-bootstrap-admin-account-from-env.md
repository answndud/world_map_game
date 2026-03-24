# 환경변수로 운영용 admin 계정 bootstrap 하기

## 왜 이 글을 쓰는가

`/admin` 접근 제어까지 붙이고 나면 바로 생기는 질문이 있다.

- 그러면 운영자는 어떤 계정으로 `/admin`에 들어가는가?
- 일반 회원가입 화면으로 admin 계정을 만들게 할 것인가?
- 기존 `USER` 계정을 어떻게 `ADMIN`으로 바꿀 것인가?

이번 단계는 이 질문에 답하기 위한 것이다.

핵심 결론은 단순하다.

> 운영용 admin 계정은 공개 회원가입 흐름이 아니라, 서버 시작 시 환경변수로 bootstrap 한다.

## 이전 단계의 한계

이전까지는 `/admin/**`가 `ADMIN` role 세션만 통과하도록 막아뒀다.

이 접근 제어 자체는 맞았다.

하지만 실제 운영 경로는 비어 있었다.

- admin 계정을 만드는 UI는 없다.
- DB에서 role을 직접 바꾸는 수동 운영만 남아 있다.
- 포트폴리오 시연 때도 “이 계정은 미리 DB에서 바꿨습니다”라고 설명해야 한다.

이 상태는 구조는 맞아도 운영 경험은 불완전하다.

## 이번 단계에서 바꾼 것

서버 시작 시 아래 환경변수를 읽어 운영용 admin 계정을 자동으로 준비한다.

- `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED`
- `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME`
- `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD`

동작은 두 가지다.

1. 해당 닉네임 계정이 없으면 새 `ADMIN` 계정을 만든다.
2. 해당 닉네임 계정이 이미 있으면 비밀번호를 갱신하고 `ADMIN`으로 승격한다.

즉, 운영자는 signup 화면을 거치지 않아도 `/admin`에 바로 들어갈 수 있다.

## 왜 signup UI로 admin을 만들지 않았는가

이 프로젝트의 계정 기능 목적은 커뮤니티가 아니다.

사용자 계정은 아래 정도면 충분하다.

- 닉네임 유지
- 점수 누적
- 내 전적 보기

반면 admin 계정은 완전히 다른 성격이다.

- 공개 화면에 노출하면 안 된다.
- 운영 환경에서만 준비되면 된다.
- 일반 사용자 흐름과 분리하는 편이 단순하다.

그래서 admin 계정 provisioning은 `회원가입 기능`이 아니라 `운영 설정`으로 보는 것이 맞다.

## 바뀐 파일

- `src/main/java/com/worldmap/auth/application/MemberCredentialPolicy.java`
- `src/main/java/com/worldmap/auth/application/MemberAuthService.java`
- `src/main/java/com/worldmap/auth/domain/Member.java`
- `src/main/java/com/worldmap/admin/application/AdminBootstrapProperties.java`
- `src/main/java/com/worldmap/admin/application/AdminBootstrapService.java`
- `src/main/java/com/worldmap/admin/application/AdminBootstrapInitializer.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-test.yml`
- `src/test/java/com/worldmap/admin/application/AdminBootstrapServiceTest.java`
- `src/test/java/com/worldmap/admin/AdminBootstrapIntegrationTest.java`

## 요청 흐름

이번 단계는 사용자 클릭에서 시작되지 않는다.

```text
애플리케이션 시작
-> AdminBootstrapInitializer.run()
-> AdminBootstrapService.ensureBootstrapAdmin()
-> AdminBootstrapProperties 읽기
-> MemberRepository.findByNicknameIgnoreCase()
-> 없으면 ADMIN 계정 생성
-> 있으면 비밀번호 갱신 + ADMIN 승격
```

즉, 이 단계의 진입점은 컨트롤러가 아니라 startup runner다.

## 왜 컨트롤러가 아니라 runner + service인가

admin bootstrap은 HTTP 요청이 없어도 항상 같은 규칙으로 실행돼야 한다.

컨트롤러에 두면 안 되는 이유는 분명하다.

- 공개 라우트가 생긴다.
- 운영 규칙이 사용자 요청 흐름에 섞인다.
- 서버가 뜰 때 admin 계정이 준비되지 않을 수 있다.

반대로 `ApplicationRunner + Service` 조합은 책임이 분명하다.

- `AdminBootstrapInitializer`
  - 서버 시작 시점을 잡는다.
- `AdminBootstrapService`
  - 계정 생성 / 승격 / 비밀번호 갱신 규칙을 담당한다.

## 왜 credential policy를 따로 뺐는가

이번 단계에서 중요한 점은 admin bootstrap도 회원가입과 같은 기본 규칙을 따라야 한다는 것이다.

예를 들면 이런 값은 둘 다 동일하게 검증해야 한다.

- 닉네임 길이
- 공백 허용 여부
- 비밀번호 최소 길이

그래서 이 규칙을 `MemberAuthService` 안에만 두지 않고
`MemberCredentialPolicy`로 분리했다.

이렇게 하면

- 회원가입
- 로그인
- admin bootstrap

이 같은 credential 규칙을 공유할 수 있다.

## 동작 규칙

현재 bootstrap 규칙은 아래와 같다.

### 1. disabled면 아무 일도 하지 않는다

기본값은 꺼져 있다.

즉, 로컬이나 테스트에서 별도 설정이 없으면 자동 admin 생성은 실행되지 않는다.

### 2. 설정이 잘못되면 빠르게 실패한다

enabled인데 닉네임/비밀번호가 비어 있으면
서버 시작 단계에서 바로 예외를 던진다.

이유는 “조용히 무시”보다 “설정 오류를 즉시 발견”하는 쪽이 운영에 더 안전하기 때문이다.

### 3. 기존 계정이 있으면 승격한다

이미 있는 `USER` 계정도 bootstrap 닉네임과 같으면

- password hash를 새 값으로 바꾸고
- role을 `ADMIN`으로 바꾼다.

즉, 운영자가 먼저 일반 계정으로 가입했어도 다시 만들 필요가 없다.

## 테스트

이번 단계에서 중요한 테스트는 세 개다.

### 1. `AdminBootstrapServiceTest`

단위 테스트에서 아래를 각각 고정했다.

- 신규 admin 생성
- 기존 `USER` 계정 승격
- enabled인데 비밀번호가 비어 있을 때 fail-fast

### 2. `AdminBootstrapIntegrationTest`

실제 Spring Boot 컨텍스트를 띄우고

- bootstrap enabled
- nickname/password 주입

상태에서 서버 시작 후 admin 계정이 실제로 생기는지 확인했다.

비밀번호도 평문이 아니라 hash로 저장되는지 같이 본다.

### 3. 기존 인증 흐름 회귀 확인

`AuthFlowIntegrationTest`와 전체 `./gradlew test`를 다시 돌려
기존 회원가입 / 로그인 / `/admin` 접근 흐름이 깨지지 않았는지 확인했다.

## 운영에서 어떻게 쓰는가

예를 들면 로컬에서는 이렇게 켤 수 있다.

```bash
export WORLDMAP_ADMIN_BOOTSTRAP_ENABLED=true
export WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME=worldmap_admin
export WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD=secret123
./gradlew bootRun
```

그러면 서버가 뜰 때 `worldmap_admin` 계정이 준비되고,
그 계정으로 `/login` 후 `/admin`에 들어갈 수 있다.

## 면접에서 어떻게 설명할까

이렇게 말하면 된다.

> admin 화면을 role로 막은 뒤에는 실제 운영자가 어떤 계정으로 들어갈지 정해야 했습니다. 일반 회원가입 UI로 admin 계정을 만들면 공개 흐름과 운영 계정이 섞이기 때문에, 서버 시작 시 환경변수로 admin 계정을 bootstrap 하도록 바꿨습니다. `ApplicationRunner`가 시작 시점을 잡고, `AdminBootstrapService`가 계정 생성 또는 기존 USER 승격을 처리합니다. 또 회원가입과 bootstrap이 서로 다른 규칙으로 흩어지지 않게 `MemberCredentialPolicy`를 분리해 닉네임/비밀번호 정책을 함께 재사용하도록 했습니다.

## 다음 글 예고

이제 8단계 핵심 범위는 거의 닫혔다.

다음 후보는 두 가지다.

1. `/mypage`에 기간별 누적 통계를 더 붙이기
2. Level 2와 실시간성 고도화 단계로 넘어가기
