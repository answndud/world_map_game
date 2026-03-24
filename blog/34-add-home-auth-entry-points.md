# 홈 첫 화면에 로그인 / 회원가입 진입점 추가하기

## 왜 이 글을 쓰는가

인증 기능이 생겼다고 해서 사용자가 그 기능을 바로 찾는 것은 아니다.

이번 단계 전에는 이런 상태였다.

- `/login`, `/signup`, `/mypage`는 이미 있음
- guest 기록 귀속도 이미 동작함
- 하지만 홈 첫 화면에는 로그인 / 회원가입 버튼이 없음

즉, 계정 기능은 있는데 첫 화면 진입 동선이 약했다.

## 이번에 바꾼 핵심

- guest가 홈에 들어오면 `로그인 / 회원가입` 블록을 본다.
- 로그인 사용자가 홈에 들어오면 `현재 계정 + My Page / 로그아웃` 블록을 본다.
- 상태 변경 로직은 새로 만들지 않고 기존 auth 흐름을 그대로 재사용한다.

## 어떤 파일이 바뀌었는가

- `src/main/resources/templates/home.html`
- `src/main/resources/static/css/site.css`
- `src/test/java/com/worldmap/web/HomeControllerTest.java`

## 요청 흐름

```text
GET /
-> HomeController
-> home.html
-> 현재 HttpSession 상태에 따라 guest/member 블록 분기
```

로그아웃은 기존 흐름을 그대로 쓴다.

```text
POST /logout
-> AuthPageController.logout()
-> MemberSessionManager.signOut()
-> redirect:/mypage
```

즉, 홈은 상태를 바꾸지 않고 이미 있는 상태를 보여 주기만 한다.

## 왜 컨트롤러에서 model flag를 넘기지 않았는가

이번 작업의 핵심은 도메인 로직 추가가 아니라 화면 표현 보강이다.

- 로그인했는가?
- 현재 닉네임은 무엇인가?

이 정보는 이미 세션에 있다.

그래서 `home.html`이 세션을 직접 읽어 guest/member 블록을 나누게 했다. 표현 규칙 때문에 `HomeController`가 session flag를 따로 계산하기 시작하면 controller 책임이 불필요하게 커진다.

반대로 실제 상태 변경은 계속 auth 계층이 맡는다.

- 로그인: `MemberAuthService`
- 세션 저장: `MemberSessionManager`
- 로그아웃: `AuthPageController`

## 무엇이 좋아졌는가

### guest 입장

홈에 들어오자마자 계정을 만들거나 로그인해서 기록을 이어 갈 수 있다.

### member 입장

지금 어떤 계정으로 기록을 쌓고 있는지 바로 보이고, `My Page`나 `로그아웃`으로 바로 갈 수 있다.

### 설명 측면

면접에서 “인증 기능이 있는데 사용자는 어디서 들어오나요?”라는 질문에 답하기 쉬워진다.

## 테스트는 무엇을 했는가

- `HomeControllerTest`
  - guest 홈에서는 `로그인`, `회원가입`이 보여야 한다
  - guest 홈에서는 `로그아웃`이 보이면 안 된다
  - admin 세션 홈에서는 `Dashboard`, `로그아웃`이 보여야 한다
  - admin 세션 홈에서는 guest용 `회원가입`이 보이면 안 된다

## 회고

이번 작업은 작은 UI 조정처럼 보여도, 실제로는 “기능 존재”를 “실제 진입 가능”으로 바꾸는 조각이다.

계정 기능은 구현돼 있어도 첫 화면에서 그 기능으로 들어가는 문이 약하면 사용자는 잘 체감하지 못한다.

## 면접에서는 이렇게 설명할 수 있다

계정과 기록 귀속 기능은 이미 있었지만, 홈 첫 화면에서 바로 로그인이나 회원가입으로 들어가는 버튼이 없어서 진입 동선이 약했습니다. 그래서 홈 템플릿이 현재 세션을 보고, guest면 `로그인 / 회원가입`, 로그인 상태면 `My Page / 로그아웃`을 보여 주게 바꿨습니다. 상태 변경 로직은 새로 만들지 않고 기존 auth 흐름을 그대로 재사용했고, 홈은 그 상태를 표현하는 SSR 역할만 맡도록 유지했습니다.
