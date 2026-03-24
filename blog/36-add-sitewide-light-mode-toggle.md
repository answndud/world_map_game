# [Spring Boot 포트폴리오] 36. 공통 셸에 다크/라이트 테마 토글 붙이기

## 왜 필요한가

사이트 기본 톤은 차가운 우주 분위기의 다크 테마다.
하지만 사용자 취향이나 시연 환경에 따라 밝은 화면이 더 편할 수 있다.

이때 테마를 서버 세션이나 DB에 넣으면 구조가 괜히 무거워진다.
이번 요구사항은 `개인 취향 UI 상태`이기 때문에 브라우저 로컬 상태로 처리하는 편이 더 맞다.

## 구현 방향

- 기본 테마는 그대로 다크
- 헤더에서 다크/라이트 토글 가능
- 사용자가 고른 테마는 `localStorage`에 저장
- public 화면과 dashboard가 같은 테마 상태를 공유
- 실제 색상 변화는 `html[data-theme]`와 CSS 변수로 처리

## 바꾼 파일

- `site.css`
- `fragments/site-header.html`
- `fragments/admin-header.html`
- `static/js/theme-toggle.js`

## 요청 흐름이 아니라 UI 상태 흐름

이 기능은 서버 요청으로 상태를 바꾸지 않는다.

1. 페이지가 열리면 header fragment의 inline script가 `worldmap-theme`를 읽는다.
2. `html[data-theme]`를 먼저 맞춘다.
3. `theme-toggle.js`가 버튼 클릭을 받는다.
4. `light` 또는 `dark`를 localStorage에 저장한다.
5. CSS 변수 세트가 바뀌면서 사이트 전체 색이 같이 바뀐다.

즉, 이건 `HTTP 요청 흐름`이 아니라 `클라이언트 로컬 UI 상태 흐름`이다.

## 왜 컨트롤러가 아니라 프론트에서 처리하나

이번 상태는 비즈니스 데이터가 아니다.

- 점수
- 랭킹
- 추천 결과
- 게임 상태

이런 것은 서버가 가져야 한다.

반면 테마는 사용자의 로컬 표시 취향이다.
브라우저 새로고침이나 다른 페이지 이동에서만 유지되면 충분하다.

그래서 이 상태를 세션이나 DB로 올리지 않고, 공통 shell과 localStorage에만 두는 것이 더 단순하고 설명 가능하다.

## 테스트

- `node --check src/main/resources/static/js/theme-toggle.js`
- `./gradlew test`

이번 조각은 UI shell 변경이라 별도 서버 기능 테스트를 늘리기보다, 공통 JS 문법 검사와 기존 회귀 테스트로 검증했다.

## 취업 포인트

면접에서는 이렇게 설명하면 된다.

“테마는 비즈니스 상태가 아니라 UI 상태라서 서버가 가질 이유가 없었습니다. 그래서 공통 header fragment가 localStorage 값을 읽어 `html[data-theme]`를 먼저 맞추고, CSS 변수 레이어가 실제 색을 바꾸게 했습니다. 덕분에 public 화면과 dashboard가 같은 토글 구조를 공유하면서도 서버 로직은 건드리지 않았습니다.”

## 다음 단계

다음은 light theme에서 실제 게임 화면, 특히 지구본 화면이 얼마나 자연스럽게 보이는지까지 확인하면서 세부 색을 더 다듬는 것이다.
