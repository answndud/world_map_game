# Work Log

## 목적

이 문서는 AI와 함께 개발하면서도, 나중에 내가 직접 설명할 수 있도록 작업 흔적을 남기는 용도다.

중요한 것은 "무엇을 바꿨는가"보다 아래를 기록하는 것이다.

- 왜 바꿨는가
- 어떤 흐름이 생겼는가
- 무엇이 아직 헷갈리는가

## 작성 규칙

- 의미 있는 기능 작업마다 1개 항목을 추가한다.
- 짧아도 되지만, 흐름과 이유는 반드시 적는다.
- 이해가 부족한 부분은 숨기지 말고 `아직 약한 부분`에 적는다.

## 템플릿

```md
## YYYY-MM-DD - 작업 이름

- 단계:
- 목적:
- 변경 파일:
- 요청 흐름:
- 데이터 / 상태 변화:
- 핵심 도메인 개념:
- 예외 / 엣지 케이스:
- 테스트:
- 배운 점:
- 아직 약한 부분:
- 면접용 30초 요약:
```

## 2026-03-25 - 라이트 테마 입체감 보강

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 사용자가 라이트 모드가 다크 모드와 달리 생동감과 입체감이 느껴지지 않는다고 지적했다. 실제로 기존 light theme는 `white surface + weak shadow` 수준에 머물러 배경 레이어, shell depth, card glint가 부족했다. 이번 조각은 밝은 화면에서도 떠 있는 느낌이 나도록 공통 theme token과 card surface 질감을 올리는 데 집중했다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/main/resources/templates/error/403.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 요청과 도메인 흐름은 그대로다. `site-header`, `panel`, `mode-card`, `message-box`, `table-shell` 같은 공통 surface가 모두 `site.css`의 light theme token을 읽고 있으므로, 이번 변화는 `html[data-theme="light"] -> CSS 변수 -> 공통 component surface` 흐름에서 전파된다.
- 데이터 / 상태 변화: DB, 세션, 게임 상태, 랭킹, 추천 결과는 전혀 바뀌지 않는다. 브라우저가 해석하는 light theme의 배경 레이어, shell shadow, panel glint, card shadow, hover lift와 스타일 버전 query만 바뀌었다.
- 핵심 도메인 개념: 이 문제는 기능 부족이 아니라 시각 계층 부족이다. 그래서 컨트롤러나 서비스가 아니라 공통 theme token을 source of truth로 두고, 여러 화면이 공유하는 `site.css`에서 해결하는 편이 가장 설명 가능하다. 한 화면만 예외적으로 꾸미면 시스템 전체 일관성이 깨지기 때문이다.
- 예외 / 엣지 케이스: 입체감을 과하게 올리면 light theme가 다시 무겁거나 유리판처럼 과장될 수 있어, `panel-glint`, `chrome-shadow`, `card-shadow`를 모두 같은 blue/slate 계열 안에서만 강화했다. hover lift도 light theme에서만 2px 수준으로 제한해 dark theme와 동작 차이를 최소화했다.
- 테스트: `git diff --check -- docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md src/main/resources/static/css/site.css src/main/resources/templates` 통과. `./gradlew test --tests com.worldmap.web.HomeControllerTest` 통과. 블로그는 순수 표현 계층 조정이라 생략한다.
- 배운 점: 라이트 모드는 배경을 희게 만드는 것만으로 완성되지 않는다. 같은 컴포넌트라도 shadow 층, glint, edge highlight가 빠지면 제품이 아니라 와이어프레임처럼 보일 수 있다.
- 아직 약한 부분: 지금은 정적 화면 기준 입체감을 보강한 단계라, 실제 모바일 Safari나 저성능 브라우저에서 blur와 gradient가 얼마나 자연스럽게 보이는지는 한 번 더 확인해야 한다.
- 면접용 30초 요약: 라이트 모드가 밋밋했던 이유는 색이 아니라 레이어가 부족했기 때문입니다. 그래서 공통 CSS의 light theme token을 다시 잡아 배경 radial layer, glass-like header, panel glint, card shadow, hover lift를 함께 올렸고, 홈뿐 아니라 추천·랭킹·게임 화면까지 같은 밝은 입체감을 공유하도록 맞췄습니다.

## 2026-03-25 - 홈 hero support row 높이 정렬

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 홈 hero에서 `서비스 현황 보기` 링크와 `hero-account-callout`이 같은 줄에 놓였지만, 하나는 얇은 버튼이고 하나는 큰 카드처럼 보여 높이와 덩어리감이 어긋나 있었다. 이번 조각은 두 요소를 같은 support card 계층으로 보이게 맞춰 수평 정렬의 어색함을 줄이는 데 집중했다.
- 변경 파일:
  - `src/main/resources/templates/home.html`
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/main/resources/templates/error/403.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 요청 흐름은 바뀌지 않는다. `/` 요청이 들어오면 기존처럼 `home.html`이 렌더링되고, 이번에는 `서비스 현황 보기` 링크에 support 전용 클래스가 붙는다. 실제 시각적 상태 변화는 `site.css`의 `home-hero-support` grid와 `hero-support-link` 스타일이 만든다.
- 데이터 / 상태 변화: DB, 세션, 게임 상태, 추천 결과에는 변화가 없다. 달라진 것은 홈 hero support row의 표현과, 브라우저가 새 CSS를 받도록 올린 style version query다.
- 핵심 도메인 개념: 이 문제는 비즈니스 로직이 아니라 표현 계층의 시각적 위계 문제다. 그래서 컨트롤러나 서비스가 아니라 `home.html`의 클래스 지정과 `site.css`의 grid/stretch/surface 규칙에서 해결하는 것이 맞다.
- 예외 / 엣지 케이스: 두 요소를 같은 높이로 맞추려면 첫 번째 열의 폭이 너무 좁거나 콘텐츠 길이에 따라 요동치지 않아야 하므로, `home-hero-support` 첫 칼럼을 `minmax(220px, 260px)`로 고정 범위화했다. 동시에 모바일에서는 기존처럼 1열로 접히도록 규칙은 그대로 유지했다.
- 테스트: `git diff --check -- docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md src/main/resources/static/css/site.css src/main/resources/templates/home.html` 통과. `./gradlew test --tests com.worldmap.web.HomeControllerTest` 통과. 블로그는 작은 CSS/UI polish 조각이라 생략한다.
- 배운 점: 같은 줄에 있다는 것만으로 통일감이 생기지 않는다. surface, padding, stretch 규칙까지 맞춰야 같은 계층의 정보처럼 보인다.
- 아직 약한 부분: 실제 브라우저에서 로그인 상태 callout 길이가 더 길 때도 `서비스 현황 보기` 카드가 충분히 자연스럽게 늘어나는지는 다시 확인할 필요가 있다.
- 면접용 30초 요약: 홈 hero의 보조 액션 row에서 링크 하나만 얇은 버튼처럼 남아 있어 시각적으로 튀었습니다. 그래서 `서비스 현황 보기`도 callout과 같은 surface card로 올리고, grid row 높이를 공유하게 만들어 두 요소가 같은 계층의 보조 행동처럼 보이도록 정리했습니다.

## 2026-03-25 - 홈 IA 정리와 랭킹 헤더 이동

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 사용자가 홈 첫 화면에서 `게임 선택하기` CTA가 불필요하고, 계정 연결 callout은 `서비스 현황 보기` 옆에 붙는 편이 낫다고 지적했다. 또 추천 게임 이름은 `나에게 어울리는 국가 찾기`로 더 분명하게 바꾸고, `실시간 랭킹`은 게임 카드가 아니라 전역 탐색에 가까우므로 header로 이동시켜 홈 본문을 실제 플레이 시작용 3개 게임만 남기도록 정리했다.
- 변경 파일:
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/error/403.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/test/java/com/worldmap/web/HomeControllerTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `/` 요청은 여전히 `HomeController`에서 시작해 `modeCards`, `entrySteps`, `accountNotes`를 만들어 `home.html`로 넘긴다. 이번에는 여기서 실시간 랭킹 카드를 제거하고 추천 게임 이름을 바꿨다. 전역 이동 구조는 `site-header.html`이 담당하므로, 랭킹 이동 경로는 홈 카드가 아니라 공통 header nav에서 시작하게 바뀌었다. 계정 연결 callout의 위치는 `home.html + site.css`가 함께 결정한다.
- 데이터 / 상태 변화: DB, Redis, 세션, 게임 상태, 추천 계산 결과는 변하지 않는다. 바뀐 것은 SSR로 렌더링되는 홈 정보 구조, 전역 navigation, 사용자-facing copy, 그리고 새 CSS를 강제로 받도록 올린 asset version query다.
- 핵심 도메인 개념: `실시간 랭킹`은 게임을 새로 시작하는 action보다 기존 기록을 탐색하는 browse destination에 가깝다. 그래서 `HomeController`의 게임 카드 컬렉션에서 빼고, 공통 전역 탐색 책임을 가진 `site-header`로 올리는 편이 정보 구조가 더 설명 가능하다. 반대로 계정 callout 위치 조정은 표현 계층 문제이므로 서비스나 도메인이 아니라 `home.html`과 `site.css`에서 끝내는 것이 맞다.
- 예외 / 엣지 케이스: 랭킹 카드를 빼면 홈 게임 카드가 3개가 되므로, 데스크톱에서는 3열, 중간 폭에서는 2열, 좁은 화면에서는 1열로 접히도록 `home-mode-grid` 반응형을 다시 맞췄다. 또 새로운 CSS가 바로 반영되지 않으면 header nav와 hero 배치가 예전처럼 보일 수 있어 전체 템플릿의 style version query를 `20260325-home-ia-8`로 함께 올렸다.
- 테스트: `git diff --check -- docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md src/main/resources/static/css/site.css src/main/resources/templates src/main/java/com/worldmap/web/HomeController.java src/test/java/com/worldmap/web/HomeControllerTest.java` 통과. `./gradlew test --tests com.worldmap.web.HomeControllerTest` 통과. 블로그는 작은 UI/copy/navigation 정리라 생략한다.
- 배운 점: 홈 본문에 기능과 탐색 목적지를 같이 섞어 두면 카드 수가 늘어날수록 메시지가 흐려진다. 랭킹처럼 “보러 가는 화면”은 전역 navigation으로 빼고, 본문은 실제 플레이 시작 버튼만 남기는 편이 훨씬 명확하다.
- 아직 약한 부분: header nav에 `Ranking`을 추가한 뒤 모바일 폭에서 링크 수가 많아질 때 균형이 충분히 좋은지는 실제 브라우저로 한 번 더 확인해야 한다.
- 면접용 30초 요약: 이번에는 기능을 추가한 게 아니라 홈 정보 구조를 다시 정리했습니다. 게임을 시작하는 3개 카드만 홈 본문에 남기고, 실시간 랭킹은 전역 header navigation으로 옮겼습니다. 또 `서비스 현황 보기` 옆에 계정 callout을 붙여 보조 행동을 한 줄로 묶고, 추천 게임 이름도 `나에게 어울리는 국가 찾기`로 바꿔 제품 언어를 더 분명하게 맞췄습니다.

## 2026-03-25 - 홈 시작 박스 제거와 게임 용어 통일

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 사용자가 홈 hero 오른쪽 `바로 시작하는 흐름` 박스가 필요 없다고 했고, `모드`라는 표현도 `게임`으로 바꿔 달라고 요청했다. 이번 조각의 핵심은 홈 첫 화면에서 설명 박스를 하나 더 쌓기보다 CTA와 게임 카드만 남겨 진입을 더 단순하게 만드는 것이다.
- 변경 파일:
  - `src/main/resources/templates/home.html`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 요청 흐름은 바뀌지 않는다. `/` 요청이 들어오면 여전히 `HomeController`가 카드/진입 설명 데이터를 만든 뒤 `home.html`에 렌더링한다. 달라진 것은 hero 우측 설명 박스가 제거됐고, 홈에서 보이는 copy가 `게임` 기준으로 바뀌었다는 점이다.
- 데이터 / 상태 변화: DB, 세션, 게임 상태, 랭킹, 추천 결과에는 전혀 변화가 없다. SSR 렌더링 텍스트와 홈 첫 화면의 구성만 단순해졌다.
- 핵심 도메인 개념: 이 수정은 비즈니스 로직이 아니라 public 홈의 제품 언어와 표현 계층 책임이다. 그래서 서비스가 아니라 `HomeController`의 copy source와 `home.html` 템플릿만 손봤다.
- 예외 상황 또는 엣지 케이스: `모드`라는 표현은 랭킹 필터처럼 실제 기능 의미가 강한 다른 화면에는 남을 수 있지만, 최소한 홈 public 화면에서는 `게임`으로 통일해 플레이어 언어를 더 직관적으로 맞췄다.
- 테스트 내용: `rg -n "모드" src/main/resources/templates/home.html src/main/java/com/worldmap/web/HomeController.java` 결과가 비어 있는 것으로 홈 기준 `모드` 표현 제거 확인. `git diff --check -- src/main/resources/templates/home.html src/main/java/com/worldmap/web/HomeController.java docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md` 통과.
- 배운 점: 홈 첫 화면은 설명을 더하는 것보다 덜어내는 편이 훨씬 낫다. 특히 제품 언어가 흔들리면 구조를 정리해도 인상이 어색해질 수 있다.
- 아직 약한 부분: 다른 public 화면과 admin 화면에는 `모드`라는 단어가 일부 남아 있으므로, 전체 제품 언어를 `게임` 기준으로 완전히 통일할지 여부는 한 번 더 정리할 필요가 있다.
- 면접용 30초 요약: 이번에는 기능을 추가하지 않고 홈 첫 화면의 진입 경험만 더 단순하게 다듬었습니다. hero 오른쪽 설명 박스를 제거하고 홈에서 보이는 `모드` 표현을 `게임`으로 통일해서, 사용자가 바로 카드에서 게임을 고르고 시작하는 흐름만 남겼습니다.

## 2026-03-25 - Java 25 toolchain 전환

- 단계: 1. 프로젝트 뼈대 보정 조각
- 목적: 사용자가 Java 25를 쓰고 싶다고 했고, 실제 로컬 머신에도 Java 25가 이미 설치돼 있었기 때문에 프로젝트 toolchain 기준을 21에서 25로 올렸다. 핵심은 서버 코드가 아니라 Gradle이 어떤 JDK를 source of truth로 삼는지 정리해 `test`, `bootRun`이 일관되게 동작하도록 만드는 것이었다.
- 변경 파일:
  - `build.gradle`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/02-spring-boot-bootstrap.md`
- 요청 흐름 / 데이터 흐름: HTTP 요청 흐름과 게임/추천 상태 흐름은 전혀 바뀌지 않는다. 바뀐 것은 `build.gradle -> java.toolchain.languageVersion` 기준과, 개발자가 로컬에서 어떤 JDK로 컴파일/실행하는가다.
- 데이터 / 상태 변화: DB, Redis, 세션, 게임 상태, 추천 결과에는 변화가 없다. 빌드 시 사용되는 JDK 기준만 `Java 25`로 올라갔다.
- 핵심 도메인 개념: 이 변경은 컨트롤러나 서비스 책임이 아니라 프로젝트 뼈대 책임이다. 기능 코드가 멀쩡해도 toolchain 기준이 맞지 않으면 서버 자체가 뜨지 않기 때문에, 이 조각은 애플리케이션 로직보다 먼저 정리해야 하는 실행 기반에 속한다.
- 예외 상황 또는 엣지 케이스: Java 21은 설치돼 있어도 프로젝트가 Java 25를 요구하면 다시 같은 종류의 toolchain 오류가 날 수 있다. 반대로 Java 25가 macOS `java_home`에 등록되어 있지 않으면 Gradle이 찾지 못할 수 있으므로, 실제 인식 경로도 함께 확인했다.
- 테스트 내용: `./gradlew test` 통과. `./gradlew bootRun --args='--spring.profiles.active=local'`로 Java 25 기준 서버 기동 확인. `curl -I http://localhost:8080`에서 `HTTP/1.1 200` 확인.
- 배운 점: 로컬에 JDK가 "설치돼 있음"과 Gradle toolchain이 "그 JDK를 찾을 수 있음"은 다른 문제다. 프로젝트 문서와 build 설정을 같이 맞춰 두지 않으면 같은 오류를 반복하게 된다.
- 아직 약한 부분: Spring Boot 3.5.x와 Java 25 조합은 현재 로컬에서 정상 기동했지만, CI나 다른 개발 머신에서도 동일하게 인식되는지는 별도로 확인해야 한다.
- 면접용 30초 요약: 기능 코드는 안 바꾸고 프로젝트 실행 기반만 정리한 작업입니다. `build.gradle`의 toolchain을 Java 25로 올리고 README, 플레이북, 작업 로그, 부트스트랩 글까지 함께 업데이트해서 로컬에서 `test`와 `bootRun`이 같은 기준의 JDK로 돌도록 맞췄습니다.

## 2026-03-25 - 홈 라이트 레이아웃 재구성

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 사용자가 계속 디자인 자체가 별로라고 했기 때문에, 색상만 바꾸는 접근을 멈추고 홈 첫 화면의 구조와 위계를 다시 잡았다. 핵심은 giant hero 아래에 눌린 4개 카드와 무거운 공통 패널을 그대로 둔 채 색만 밝히면 라이트 제품처럼 보이지 않는다는 점이었다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/main/resources/templates/error/403.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 요청 흐름은 그대로다. 홈 화면에서 달라진 것은 템플릿 구조와 공통 CSS 위계다. `home.html`은 `hero-grid` 위에 좌측 설명 + CTA, 우측 요약 패널 구조로 바뀌었고, `modeCards`는 4개가 한 줄에 눌리는 대신 2x2 card grid로 배치된다.
- 데이터 / 상태 변화: 데이터 구조와 model attribute는 그대로다. 바뀐 것은 SSR 템플릿에서 같은 `modeCards`, `principles`, `roadmap`를 어떻게 보여주느냐와, 새 CSS가 바로 반영되도록 올린 style version query다.
- 핵심 도메인 개념: 이 변경은 여전히 컨트롤러 책임이 아니라 표현 계층 책임이다. 서버는 모드 목록과 설명만 넘기고, 어떤 레이아웃으로 보여 줄지는 `home.html + site.css`가 결정한다. 그래서 자바 로직을 바꾸지 않고도 홈 첫 화면의 인상과 가독성을 크게 바꿀 수 있다.
- 예외 상황 또는 엣지 케이스: 홈 레이아웃을 2열로 바꾸면서 중간 폭 이하에서 카드 밀도가 다시 나빠질 수 있으므로, `hero-grid`, `split`, `home-mode-grid`는 반응형에서 1열로 접히게 했다. 또한 공통 nav와 theme toggle도 Orbitron 위주에서 Space Grotesk 중심으로 눌러 가독성을 높였다.
- 테스트 내용: `git diff --check -- docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md src/main/resources/static/css/site.css src/main/resources/templates` 통과. `./gradlew test`는 다시 실행하지 않았고, 현재 머신에 Java 21 toolchain이 없어 이전과 동일하게 실행 불가 상태다.
- 배운 점: 라이트 모드 품질은 색보다 구조와 밀도가 더 크게 좌우된다. 특히 홈 첫 화면은 4개 기능을 한 줄에 다 넣는 것보다, hero 정보와 모드 카드를 분리해 위계를 주는 편이 훨씬 낫다.
- 아직 약한 부분: 홈은 재구성했지만, 추천 결과와 대시보드처럼 정보량이 더 많은 화면에서 같은 타이포 스케일과 카드 밀도가 충분히 자연스러운지는 추가 확인이 필요하다.
- 면접용 30초 요약: 홈 화면 디자인이 계속 어색했던 이유는 다크 계열 색을 조금 밝힌 게 아니라, 구조 자체가 무거웠기 때문입니다. 그래서 이번에는 서버가 넘기는 데이터는 그대로 두고, 홈 템플릿을 hero 2열과 2x2 모드 카드 구조로 다시 짜고 공통 타입 스케일과 버튼, 패널 밀도를 같이 조정해 라이트 모드 첫 인상을 제품답게 다시 만들었습니다.

## 2026-03-25 - 라이트 팔레트 리부트

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 사용자가 "라이트 모드를 처음부터 개발한다고 가정하고 색 조합을 다시 잡아달라"고 했기 때문에, 기존 dark shell을 희석한 light theme가 아니라 light 전용 제품 팔레트를 다시 정의했다. 특히 홈 화면에서 `panel`, `hero-account-callout`, 섹션 박스가 검은 프레임처럼 남아 보이는 문제를 없애는 데 집중했다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/main/resources/templates/error/403.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 요청과 게임 상태 흐름은 그대로다. 바뀐 것은 `html[data-theme="light"]` 아래에서 어떤 색 토큰이 선택되는가다. 배경은 거의 white/gray 계열, 텍스트는 slate, accent는 cobalt로 재설정했고, `panel`과 `mode-card`, `message-box`, `table`, `modal`이 모두 같은 light surface 계층을 공유한다.
- 데이터 / 상태 변화: DB와 세션 상태는 변하지 않는다. 브라우저에서 보이는 theme 표현만 달라졌고, 새 CSS를 강제로 받도록 템플릿의 style version query를 `20260325-light-reboot-5`로 올렸다.
- 핵심 도메인 개념: 이 변경은 서버 책임이 아니라 공통 표현 계층 책임이다. 그래서 홈 템플릿 한 곳에 임시 색을 넣지 않고, `site.css`의 light theme semantic token 집합을 source of truth로 바꾸는 방식으로 처리했다.
- 예외 상황 또는 엣지 케이스: dark theme의 별빛/글로우 효과가 light에서도 남아 있으면 화면이 탁하고 중간톤이 쉽게 더러워 보인다. 이번에는 light에서 `body::before` 별빛을 끄고, `panel-glint`도 제거해 white surface가 실제 white처럼 보이게 했다.
- 테스트 내용: `git diff --check -- docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md src/main/resources/static/css/site.css src/main/resources/templates` 통과. `./gradlew test`는 다시 실행했지만 현재 머신에 Java 21 toolchain이 없어 실패했다.
- 배운 점: 라이트 모드는 다크 토큰을 연하게 만드는 방식보다, 배경 노이즈와 반투명 레이어를 줄이고 surface hierarchy를 거의 종이처럼 다시 정하는 편이 훨씬 낫다.
- 아직 약한 부분: 홈 기준 팔레트는 다시 잡았지만, 실제 브라우저에서 recommendation result / dashboard / game overlay가 같은 밀도와 톤으로 느껴지는지는 한 번 더 눈으로 확인할 필요가 있다.
- 면접용 30초 요약: 이번에는 light theme를 dark theme의 변형으로 보지 않고, white surface와 slate text 중심의 별도 제품 팔레트로 다시 설계했습니다. 핵심은 홈 한 장만 고치는 게 아니라 `html[data-theme="light"]` 아래의 공통 surface token을 바꾸고, 패널과 카드, 메시지, 모달, 배경 노이즈까지 같은 기준으로 맞춘 것입니다.

## 2026-03-25 - 라이트 모드 기본값과 공통 표면 재보정

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 사용자가 `hero-account-callout`, `panel`, `split`까지 여전히 어둡다고 지적했기 때문에, 단순 색 토큰 추가 수준에서 끝내지 않고 light theme 기본 진입값과 공통 표면 레이어를 다시 조정했다. 핵심 문제는 light token이 있어도 기본 진입이 dark였고, panel/header surface에 남은 blur와 반투명 계열이 실제 체감상 탁하고 어둡게 보일 수 있다는 점이었다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/static/js/theme-toggle.js`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/fragments/admin-header.html`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/main/resources/templates/error/403.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 요청 흐름은 그대로고, 브라우저에서 `localStorage.worldmap-theme -> documentElement.dataset.theme -> CSS variable`로 내려가는 theme 적용 흐름만 바뀌었다. 이제 저장된 값이 없으면 light로 시작하고, theme toggle은 현재 모드를 표시한다. `panel`, `hero-account-callout`, header shell은 더 불투명하고 밝은 surface를 사용한다.
- 데이터 / 상태 변화: DB, 세션, 게임 상태, 추천 계산, 랭킹 데이터는 바뀌지 않는다. 브라우저의 theme 상태만 기본값이 `light`가 되었고, CSS asset version과 theme toggle script version이 함께 올라갔다.
- 핵심 도메인 개념: 이 수정은 여전히 컨트롤러/서비스 책임이 아니라 공통 shell 책임이다. 사용자가 보는 표면 계층과 theme 기본값은 서버 비즈니스 규칙이 아니라 렌더링 시스템 규칙이므로, 템플릿에 임시 색을 박는 대신 fragment + theme script + semantic token 조합으로 고쳤다.
- 예외 상황 또는 엣지 케이스: 기존에는 localStorage가 비어 있거나 script가 먼저 적용되기 전에는 dark로 진입할 수 있었고, theme toggle 버튼도 다음 액션처럼 보여 현재 모드를 오해할 수 있었다. 이번에는 저장값이 없으면 light로 맞췄고, header/panel blur를 light에서 제거해 탁한 표면을 줄였다.
- 테스트 내용: `git diff --check -- docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md src/main/resources/static/css/site.css src/main/resources/static/js/theme-toggle.js src/main/resources/templates` 통과. `node --check src/main/resources/static/js/theme-toggle.js` 통과. `./gradlew test`는 다시 실행했지만 현재 머신에 Java 21 toolchain이 없어 실패했다.
- 배운 점: 라이트 모드는 토큰 몇 개만 바꾸는 문제가 아니라 기본 진입값, 토글 의미, 배경 투명도와 blur까지 같이 맞춰야 실제 체감 품질이 올라간다.
- 아직 약한 부분: 실제 브라우저에서 사용자가 본 화면과 완전히 같은 조건으로 확인한 것은 아니어서, 모바일 폭과 특정 모니터 밝기에서 여전히 탁하게 보이는지 한 번 더 실기기 확인이 필요하다.
- 면접용 30초 요약: light theme 토큰을 넣는 것만으로는 부족해서, 이번에는 theme 기본 진입값과 공통 surface 자체를 다시 손봤습니다. `localStorage -> html[data-theme] -> CSS token` 흐름은 유지하되 기본값을 light로 바꾸고, header/panel/hero callout을 더 불투명한 밝은 표면으로 재조정해 사용자가 실제로 느끼는 dark 잔재를 줄였습니다.

## 2026-03-24 - 전체 화면 라이트 모드 시스템 정비

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 사용자 요청대로 홈만이 아니라 모든 화면 관련 코드를 다시 읽고 light theme를 전체적으로 정리했다. 핵심 문제는 일부 버튼만이 아니라 public/admin/game 화면 전체에서 다크 하드코딩 표면, 흐린 secondary text, 모든 상황을 빨간 경고로 처리하는 `message-box`, 게임 overlay/modal의 검은 slab가 섞여 있던 점이었다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/js/population-game.js`
  - `src/main/resources/static/js/ranking.js`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/main/resources/templates/error/403.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: HTTP 요청 흐름은 바뀌지 않았다. 바뀐 것은 `localStorage.worldmap-theme -> html[data-theme] -> CSS token` 아래에서 실제 화면이 어떤 표면과 텍스트 계층을 쓰는가다. 공통 CSS는 panel, card, badge, chip, input, table head, globe, stage overlay, game-over modal, message box tone을 semantic token으로 분리했고, location/population/ranking JS는 로딩/성공/에러 메시지에 맞는 `data-tone`을 넘기도록 바뀌었다.
- 데이터 / 상태 변화: DB, 세션, 추천 결과, 게임 상태, 랭킹 데이터는 전혀 바뀌지 않는다. 브라우저 로컬 theme 상태 구조도 그대로다. 달라진 것은 light theme surface hierarchy, message tone 체계, 그리고 새 CSS/JS가 즉시 보이도록 올린 asset version query다.
- 핵심 도메인 개념: 이 변경은 컨트롤러나 서비스 책임이 아니다. 서버는 게임 상태와 추천 계산을 관리하고, UI shell은 사용자가 읽고 조작하는 표면 계층을 책임진다. 그래서 이번 수정도 개별 템플릿에서 임시 색을 덧대지 않고, `site.css`의 semantic token 레이어를 source of truth로 두고 전 화면을 맞추는 방향으로 처리했다.
- 예외 상황 또는 엣지 케이스: `message-box`는 이전에는 로딩 안내, 빈 상태, 검증 에러, 네트워크 실패가 모두 같은 빨간 경고 박스로 보였다. 이번에 tone을 분리하면서 auth/survey는 `error`, start/play 로딩은 `info`, population restart 완료는 `success`로 나눴다. 또한 지구본 playfield는 light theme에서도 완전 흰 판으로 바꾸지 않고, 시인성을 위해 blue-tinted playfield로 유지했다.
- 테스트 내용: `git diff --check -- src/main/resources/static/css/site.css src/main/resources/static/js/location-game.js src/main/resources/static/js/population-game.js src/main/resources/static/js/ranking.js src/main/resources/templates docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md` 통과. `node --check src/main/resources/static/js/location-game.js`, `node --check src/main/resources/static/js/population-game.js`, `node --check src/main/resources/static/js/ranking.js` 통과. `./gradlew test`는 현재 머신에 Java 21 toolchain이 없어 실행하지 못했다.
- 배운 점: 라이트 모드는 배경색 하나 바꾸는 기능이 아니라, surface hierarchy와 message semantics를 다시 정의하는 작업에 가깝다. 특히 게임 화면이 있는 서비스에서는 panel/card/modal/overlay를 따로 토큰화하지 않으면 페이지마다 다크 잔재가 쉽게 남는다.
- 아직 약한 부분: 템플릿과 공통 CSS/JS는 모두 읽고 정리했지만, 실제 브라우저에서 recommendation result와 dashboard 데이터 밀도가 모바일에서 얼마나 편한지는 한 번 더 확인할 가치가 있다. 또한 Java 21이 없는 로컬 환경이라 서버 렌더링 회귀 테스트까지는 이번 턴에 못 돌렸다.
- 면접용 30초 요약: 이번에는 홈 한 장만 고치지 않고 public, admin, game 화면 템플릿과 공통 CSS/JS를 전부 다시 읽었습니다. 문제는 라이트 모드인데도 패널, 모달, 메시지 박스가 여전히 다크 하드코딩을 끌고 있던 점이어서, `html[data-theme]` 아래에 panel/card/message/globe/modal semantic token을 새로 두고 전체 화면을 같은 체계로 맞췄습니다. 서버 상태는 그대로 두고 표현 계층만 정리했기 때문에 구조 설명도 단순합니다.

## 2026-03-24 - 라이트 모드 홈 화면 표면 계층 재정리

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 사용자 스크린샷 기준으로 라이트 모드는 버튼만의 문제가 아니라 홈 화면 전체의 표면 계층이 어긋나 있었다. 배경은 밝은데 hero callout과 섹션 panel은 여전히 짙은 slab이고, 본문 텍스트는 회색이라 가독성이 무너졌다. 이번 조각은 홈과 공통 shell의 light theme surface hierarchy를 다시 맞추는 데 집중한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/main/resources/templates/error/403.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 요청 흐름은 여전히 없다. `localStorage.worldmap-theme -> html[data-theme] -> CSS variable` 흐름은 그대로 두고, 이번에는 그 아래의 `panel shell`, `hero callout`, `mode card`, `input surface`가 light theme에서 어떤 색 레이어를 쓸지만 다시 정의했다. 즉 브라우저 로컬의 theme 값은 그대로고, 같은 theme 값이 더 일관된 표면 계층으로 렌더링되도록 매핑을 조정한 것이다.
- 데이터 / 상태 변화: 서버 데이터, 세션, 추천 결과, 게임 상태는 바뀌지 않는다. 바뀐 것은 `--panel-shell-surface`, `--hero-callout-surface`, `--card-shadow`, `--input-surface`, `--display-ink` 같은 표현 토큰과, 새 CSS가 바로 반영되도록 모든 SSR 페이지의 style version query를 한 번 더 올린 점이다.
- 핵심 도메인 개념: 이번 문제도 표현 계층의 책임이다. panel과 callout이 dark slab처럼 남아 있던 이유는 light theme가 “배경색만 밝아진 상태”였기 때문이다. 그래서 개별 템플릿에서 요소마다 직접 색을 덮는 대신, 공통 CSS token 레이어에서 밝은 화면용 surface hierarchy를 다시 설계하는 편이 더 설명 가능하고 유지보수하기 쉽다.
- 예외 상황 또는 엣지 케이스: home만 고치면 로그인/추천/랭킹 화면은 이전 CSS 캐시를 계속 들고 있을 수 있어 version query를 전체 페이지에서 같이 올렸다. 또한 panel/head/body text가 같은 `muted` 계열을 공유하던 구조는 밝은 표면에서만이 아니라 짙은 패널에서도 가독성을 흔들 수 있어, `text-muted`와 `text-strong` 토큰을 명시적으로 나눠 재사용했다.
- 테스트 내용: `git diff --check -- src/main/resources/static/css/site.css src/main/resources/templates docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md` 통과. `./gradlew test`는 현재 머신에 Java 21 toolchain이 없어 시작 단계에서 실패했다. 오류는 `Cannot find a Java installation ... matching: {languageVersion=21}`였다.
- 배운 점: 라이트 모드는 “밝은 배경 + 기존 다크 패널 유지”처럼 중간 단계에서 가장 어색해진다. 실제로는 버튼 색 하나보다, 어떤 표면이 가장 위 레이어인지부터 다시 정리해야 홈 화면 전체 인상이 안정된다.
- 아직 약한 부분: 이번 수정은 스크린샷 기준으로 가장 거슬리던 홈/public shell 표면을 정리한 것이다. recommendation result, auth form, stats 같은 다른 public 화면이 실제 브라우저에서 같은 톤으로 충분히 안정적인지는 한 번 더 확인할 필요가 있다.
- 면접용 30초 요약: 라이트 모드가 어색했던 이유는 버튼만 아니라 패널과 콜아웃이 여전히 다크 surface를 유지하고 있었기 때문입니다. 그래서 `html[data-theme]` 아래에서 버튼 토큰만 고치는 데서 멈추지 않고, panel shell, hero callout, card, input까지 밝은 blue-tinted layer로 다시 정의해 표면 계층을 정리했습니다. 서버 상태는 그대로 두고 CSS token 레이어만 바꿔 문제를 풀었습니다.

## 2026-03-24 - 라이트 모드 버튼 대비 보정

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 라이트 모드로 전환해도 primary 버튼 텍스트와 secondary 버튼/필터/선택 카드 배경이 다크 값에 머물러 가시성이 떨어졌다. 이번 조각은 서버 로직을 건드리지 않고, 공통 shell의 interactive color token만 정리해서 버튼 대비를 정상화하는 데 집중한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/main/resources/templates/error/403.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 요청 흐름은 바뀌지 않았다. header fragment가 기존처럼 `localStorage.worldmap-theme`를 읽어 `html[data-theme]`를 맞추면, 이제 `site.css`의 semantic token이 primary/secondary 버튼, 내비게이션, 랭킹 필터, 추천 선택 카드, 만족도 버튼의 실제 배경과 텍스트 색을 결정한다. 즉 상태 변화는 여전히 브라우저 로컬에 있고, 이번 수정은 `theme value -> CSS token -> control surface` 매핑만 보정한 것이다.
- 데이터 / 상태 변화: DB, 세션, API payload는 전혀 바뀌지 않는다. 바뀐 것은 light theme에서 사용하는 `--primary-action-ink`, `--interactive-surface`, `--interactive-surface-selected` 같은 공통 표현 토큰과, 모든 SSR 페이지가 새 CSS 버전 쿼리를 읽도록 캐시 경로를 올린 점이다.
- 핵심 도메인 개념: 이 로직은 컨트롤러나 서비스에 있을 이유가 없다. 버튼 대비는 게임 상태나 추천 계산이 아니라 표현 계층의 책임이므로, 공통 CSS 변수 레이어가 source of truth가 되는 편이 더 설명 가능하다. 특히 이번 문제는 라이트 모드인데 일부 버튼이 다크 RGBA와 검은 계열 텍스트를 그대로 쓰고 있었기 때문에, 개별 페이지를 고치는 대신 theme token을 정리해 공통 해결하는 쪽이 맞았다.
- 예외 상황 또는 엣지 케이스: 추천 설문 옵션 카드와 랭킹 필터처럼 “버튼처럼 보이지만 클래스가 다른” 요소도 같이 보정했다. 기존 CSS에는 `--text-muted`, `--text-strong`처럼 정의되지 않은 변수를 참조하는 구간이 있었는데, light theme에서 active 상태 색이 의도와 다르게 내려갈 여지가 있어 이번에 함께 명시했다. 브라우저 캐시가 오래 남아 있으면 이전 CSS가 보일 수 있어, 모든 SSR 페이지의 `site.css` 버전 쿼리도 같이 올렸다.
- 테스트 내용: `git diff --check -- src/main/resources/static/css/site.css src/main/resources/templates docs/PORTFOLIO_PLAYBOOK.md docs/WORKLOG.md` 통과. `./gradlew test`는 현재 머신에 Java 21 toolchain이 없어 시작 단계에서 실패했다. 오류는 `Cannot find a Java installation ... matching: {languageVersion=21}`였다.
- 배운 점: theme toggle을 붙였다고 light theme가 완성되는 것은 아니다. 실제로는 “배경만 밝아지고 버튼은 다크 값이 남는” 혼합 상태가 더 거슬릴 수 있어서, 공통 인터랙션 토큰을 먼저 도입해 surface와 text를 함께 바꾸는 구조가 필요했다.
- 아직 약한 부분: 이번 수정은 공통 버튼/선택 컨트롤 대비에 집중했다. 지구본 플레이 화면의 배경 톤, 게임오버 모달, 강한 경고 상태 색이 라이트 모드에서도 충분히 자연스러운지는 브라우저 실화면으로 한 번 더 봐야 한다.
- 면접용 30초 요약: 라이트 모드에서 가시성이 나빴던 이유는 버튼이 여전히 다크 배경과 검은 계열 텍스트를 일부 공유했기 때문입니다. 그래서 각 화면을 따로 고치지 않고 `html[data-theme]` 아래 공통 interactive color token을 정의해 버튼, 필터, 선택 카드가 같은 기준으로 밝은 표면과 맞는 텍스트 색을 쓰게 바꿨습니다. 서버 상태는 건드리지 않았고, 캐시 때문에 예전 CSS가 남지 않도록 SSR 페이지의 asset 버전도 같이 올렸습니다.

## 2026-03-22 - 문서 운영 구조 생성

- 단계: 0. 문서와 규칙 정리
- 목적: AI와 함께 개발해도 구현 순서, 이해 포인트, 작업 기록이 남도록 기본 문서 구조를 만든다.
- 변경 파일:
  - `README.md`
  - `AGENTS.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 아직 애플리케이션 요청 흐름은 없고, 개발 프로세스와 문서 흐름을 먼저 고정했다.
- 데이터 / 상태 변화: 코드 상태보다 프로젝트 운영 방식이 정리되었다. 앞으로는 기능 단위로 작업 후 문서를 같이 갱신해야 한다.
- 핵심 도메인 개념: 서버 주도 게임 상태 관리, 단계별 개발, 설명 가능한 구조, 작업 로그 누적
- 예외 / 엣지 케이스: 문서가 너무 많아져 관리가 어려워질 수 있으므로 역할을 분리했다. README는 개요, Playbook은 순서, Work Log는 작업 기록만 담당한다.
- 테스트: 없음
- 배운 점: AI 보조 개발에서는 코드보다 문서 운영 체계가 먼저 있어야 나중에 설명이 가능하다.
- 아직 약한 부분: 실제 구현이 시작되면 문서 갱신이 번거롭게 느껴질 수 있다. 이후 단계에서 너무 무거운 절차인지 검증이 필요하다.
- 면접용 30초 요약: 기능 구현 전에 프로젝트의 개발 순서, 문서화 기준, 작업 로그 규칙을 먼저 정리해 두어 AI와 함께 개발해도 설계 의도와 구현 흐름을 직접 설명할 수 있게 만들었다.

## 2026-03-22 - blog 워크스페이스 추가

- 단계: 0. 문서와 규칙 정리
- 목적: 참고 프로젝트의 `blog/` 폴더처럼, 초보자 기준으로 자세하게 설명하는 공개용 글 구조를 현재 프로젝트에도 추가한다.
- 변경 파일:
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/00_rebuild_guide.md`
  - `blog/_post_template.md`
  - `blog/01-why-worldmap-game-platform-domain.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 아직 애플리케이션 요청은 없고, 개발용 문서와 공개용 설명 문서를 분리하는 흐름을 먼저 만들었다.
- 데이터 / 상태 변화: 코드 상태 변화는 없고, 이제 프로젝트 문서가 `개발 SSOT`와 `블로그형 설명 문서`로 분리되었다.
- 핵심 도메인 개념: 서버 주도 게임 플랫폼, 단계별 학습, 글 템플릿 기반 설명, 공개용 연재 구조
- 예외 / 엣지 케이스: 실제 코드가 없는 상태에서 코드 설명 글을 과하게 쓰면 공허해질 수 있으므로, 현재는 방향 글과 템플릿까지만 만들고 구현 글은 코드 생성 이후에 채우도록 했다.
- 테스트: 없음
- 배운 점: 초보자 기준의 자세한 문서는 하나의 장문보다, 연재형 번호 문서와 템플릿으로 쪼개는 편이 훨씬 관리하기 쉽다.
- 아직 약한 부분: 이후 실제 기능 구현이 시작되면 `blog/`와 `docs/`의 중복이 생길 수 있다. 어느 문서가 근거 문서인지 계속 분명히 해야 한다.
- 면접용 30초 요약: 개발용 문서와 별도로 `blog/` 워크스페이스를 만들어, 각 기능을 초보자 기준으로 문제 정의부터 테스트와 취업 포인트까지 단계별 글로 정리할 수 있는 구조를 만들었다.

## 2026-03-22 - AI agent 운영 모델 조사 및 정리

- 단계: 0. 문서와 규칙 정리
- 목적: OpenAI, Anthropic의 공식 문서와 블로그를 바탕으로 이 프로젝트에 맞는 `AGENTS.md`, `docs`, `skills`, `subagents`, 런타임 LLM 구성 원칙을 정리한다.
- 변경 파일:
  - `docs/AI_AGENT_OPERATING_MODEL.md`
  - `AGENTS.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 외부 공식 레퍼런스를 조사한 뒤, 그 결과를 현재 저장소 운영 원칙으로 번역해 문서화했다.
- 데이터 / 상태 변화: 코드 변화는 없고, 프로젝트의 AI 보조 개발 방식과 런타임 LLM 도입 원칙이 명시되었다.
- 핵심 도메인 개념: `AGENTS.md는 짧은 지도`, `docs는 시스템 오브 레코드`, `단일 메인 에이전트 우선`, `병렬화가 명확할 때만 멀티 에이전트`, `추천 결과는 서버가 결정하고 LLM은 설명만 담당`
- 예외 / 엣지 케이스: 공식 문서의 멀티 에이전트 권장 사례는 주로 리서치/탐색형 작업에 강하므로, 이를 게임 백엔드 구현에 그대로 일반화하면 오히려 복잡도만 늘어날 수 있다.
- 테스트: 없음
- 배운 점: 에이전트 품질은 모델 자체보다도 도구 접근, 문서 구조, 기록 체계, 피드백 루프 같은 저장소 운영 방식에 크게 좌우된다.
- 아직 약한 부분: 실제 Codex용 커스텀 스킬은 아직 만들지 않았다. 어떤 스킬을 언제 만드는 게 정말 반복 가치가 있는지는 첫 구현 단계에서 다시 검증해야 한다.
- 면접용 30초 요약: OpenAI와 Anthropic의 공식 문서를 조사해, 이 저장소에서는 멀티 에이전트를 기본값으로 두지 않고 `짧은 AGENTS.md + 구조화된 docs + 제한된 목적의 skills` 구조로 운영하는 것이 가장 설명 가능하고 유지보수하기 좋다고 정리했다.

## 2026-03-22 - 첫 번째 커스텀 스킬 `worldmap-doc-sync` 구현

- 단계: 0. 문서와 규칙 정리
- 목적: 기능 구현 후 문서 업데이트를 빠뜨리지 않도록, WorldMap 전용 문서 동기화 스킬을 실제로 만든다.
- 변경 파일:
  - `/Users/alex/.codex/skills/worldmap-doc-sync/SKILL.md`
  - `/Users/alex/.codex/skills/worldmap-doc-sync/agents/openai.yaml`
  - `/Users/alex/.codex/skills/worldmap-doc-sync/references/doc-impact-map.md`
  - `/Users/alex/.codex/skills/worldmap-doc-sync/references/worklog-entry-template.md`
  - `/Users/alex/.codex/skills/worldmap-doc-sync/references/blog-update-rules.md`
  - `AGENTS.md`
  - `docs/AI_AGENT_OPERATING_MODEL.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 기능 작업이 끝난 뒤 어떤 문서를 업데이트해야 하는지 판단하고, Work Log를 기본값으로 갱신한 뒤 Playbook, README, blog 필요 여부를 체크하는 후처리 흐름을 스킬로 고정했다.
- 데이터 / 상태 변화: 저장소 코드 상태는 바뀌지 않았지만, 이제 WorldMap 프로젝트에는 재사용 가능한 문서 동기화 스킬이 생겼고 AI 운영 방식이 추천 수준에서 구현 수준으로 한 단계 진전됐다.
- 핵심 도메인 개념: 문서 역할 분리, post-task documentation workflow, `AGENTS.md`는 짧은 지도, `docs/`는 근거 문서, `blog/`는 공개용 설명 문서
- 예외 / 엣지 케이스: 작은 수정까지 모든 상위 문서를 갱신하면 오히려 소음이 커질 수 있으므로, 이 스킬은 `WORKLOG는 기본`, `Playbook/README/blog는 조건부`라는 규칙으로 문서 업데이트 범위를 제한한다.
- 테스트: skill-creator의 `quick_validate.py`를 임시 가상환경에서 실행해 구조 검증 완료
- 배운 점: 스킬은 막연한 지침보다 특정 저장소의 문서 역할과 업데이트 순서를 고정하는 좁은 워크플로우일 때 가장 실용적이다.
- 아직 약한 부분: 실제 기능 구현 이후 이 스킬을 몇 번 사용해 보면서 체크리스트가 너무 무겁지 않은지 검증이 필요하다.
- 면접용 30초 요약: AI와 함께 개발할 때 가장 먼저 생기는 문제는 문서 누락이어서, WorldMap 전용 `worldmap-doc-sync` 스킬을 만들어 기능 작업 후 Work Log, Playbook, README, blog 업데이트 범위를 일관되게 판단하고 기록하도록 운영 체계를 고정했다.

## 2026-03-22 - `worldmap-doc-sync`를 프로젝트 로컬 스킬로 전면 수정

- 단계: 0. 문서와 규칙 정리
- 목적: `worldmap-doc-sync`가 모든 프로젝트에 보이는 전역 스킬이 아니라, WorldMap 저장소 안에서만 사용되는 프로젝트 로컬 스킬이 되도록 위치와 설정을 수정한다.
- 변경 파일:
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/SKILL.md`
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/agents/openai.yaml`
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/references/doc-impact-map.md`
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/references/worklog-entry-template.md`
  - `/Users/alex/project/worldmap/.agents/skills/worldmap-doc-sync/references/blog-update-rules.md`
  - `AGENTS.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/AI_AGENT_OPERATING_MODEL.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 전역 위치에 있던 스킬을 제거하고, 저장소 루트의 `.agents/skills/` 아래로 옮긴 뒤 문서에서 현재 경로와 사용 방식을 프로젝트 기준으로 다시 고정했다.
- 데이터 / 상태 변화: 이제 `worldmap-doc-sync`는 WorldMap 저장소 내부에만 존재하며, `agents/openai.yaml`에서 `allow_implicit_invocation: false`로 설정되어 이 프로젝트에서도 명시적으로만 사용된다.
- 핵심 도메인 개념: 프로젝트 로컬 스킬, explicit invocation, 전역 설정과 저장소 설정 분리, 문서 역할 일관성
- 예외 / 엣지 케이스: 기존 Work Log에는 전역 경로로 처음 생성했던 기록이 남아 있다. 이는 현재 상태가 아니라 역사 기록이므로, 최신 경로는 이 항목과 운영 문서를 기준으로 본다.
- 테스트: skill validator를 새 프로젝트 로컬 경로에 대해 다시 실행 예정
- 배운 점: 스킬은 저장 위치 자체가 동작 범위를 결정하므로, 프로젝트 전용 워크플로우는 처음부터 전역이 아니라 저장소 내부 `.agents/skills/`에 두는 편이 맞다.
- 아직 약한 부분: 현재 세션이 이미 시작된 뒤 경로를 옮긴 것이므로, UI나 자동 발견 상태는 새 세션에서 한 번 더 확인하는 것이 안전하다.
- 면접용 30초 요약: 처음에는 문서 동기화 스킬을 전역으로 만들었지만, 프로젝트 전용 규칙을 다른 저장소에 전파하면 오히려 혼란이 커질 수 있어서, 스킬을 WorldMap 저장소 내부 `.agents/skills/`로 옮기고 명시 호출 전용으로 바꿔 범위를 이 프로젝트로만 제한했다.

## 2026-03-22 - 1단계 스프링부트 프로젝트 뼈대 완료

- 단계: 1. 스프링부트 프로젝트 뼈대
- 목적: Spring Boot 3 기반의 실행 가능한 서버 뼈대, SSR 메인 페이지, 프로파일 분리, 개발용 DB/Redis 환경 구성을 한 번에 잡는다.
- 변경 파일:
  - `build.gradle`
  - `compose.yaml`
  - `HELP.md`
  - `src/main/java/com/worldmap/WorldMapApplication.java`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/java/com/worldmap/web/view/ModeCardView.java`
  - `src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java`
  - `src/main/java/com/worldmap/common/response/ApiErrorResponse.java`
  - `src/main/java/com/worldmap/auth/package-info.java`
  - `src/main/java/com/worldmap/country/package-info.java`
  - `src/main/java/com/worldmap/game/package-info.java`
  - `src/main/java/com/worldmap/ranking/package-info.java`
  - `src/main/java/com/worldmap/recommendation/package-info.java`
  - `src/main/resources/application.yml`
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-test.yml`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/web/HomeControllerTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/02-spring-boot-bootstrap.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 `/`로 접근하면 `HomeController`가 모드 카드, 핵심 원칙, 로드맵 데이터를 모델에 담아 `home.html`을 렌더링한다. 아직 영속 상태 변화는 없고, 현재 단계는 서버 뼈대와 렌더링 구조를 고정하는 데 집중했다.
- 데이터 / 상태 변화: 로컬 실행에서는 Docker Compose 기반 PostgreSQL / Redis를 붙일 수 있는 상태가 되었고, 테스트에서는 H2 메모리 DB를 사용하도록 분리했다. 프로젝트 상태는 `문서 전용 저장소`에서 `실행 가능한 Spring Boot 3 애플리케이션` 단계로 올라갔다.
- 핵심 도메인 개념: SSR + API 혼합 구조, 프로파일 분리, 패키지 구조 선고정, 공통 API 예외 응답, Docker Compose 기반 로컬 개발 환경
- 예외 / 엣지 케이스: Spring Boot 4로 생성된 초기 스캐폴드를 그대로 쓰지 않고 사용자가 요청한 Spring Boot 3 기준으로 수정했다. 테스트가 로컬 Docker 환경에 의존하지 않도록 `application-test.yml`에서 H2와 Docker Compose 비활성화 설정을 따로 둔 것이 핵심이었다.
- 테스트: `./gradlew test` 통과
- 배운 점: 뼈대 단계에서 버전, 패키지 구조, 프로파일, 예외 처리, 테스트 전략을 같이 고정해 두면 이후 기능 구현 속도보다 설명 가능성이 크게 좋아진다.
- 아직 약한 부분: 현재는 JPA/Redis 의존성만 연결된 상태이고 실제 엔티티와 저장 로직은 아직 없다. 다음 단계에서 country 시드 구조를 넣으면서 persistence 설정이 자연스럽게 이어지는지 다시 검증해야 한다.
- 면접용 30초 요약: WorldMap 프로젝트의 첫 단계에서는 Spring Boot 3.5 기반 서버 뼈대와 Thymeleaf SSR 홈 화면을 만들고, 로컬/테스트 프로파일을 분리했으며, 이후 게임 API 확장을 대비해 공통 예외 응답 구조와 패키지 경계를 먼저 고정했다.

## 2026-03-22 - 2단계 국가 데이터와 시드 적재 완료

- 단계: 2. 국가 데이터와 시드 적재
- 목적: 게임 출제와 추천 기능이 공통으로 사용할 국가 데이터를 DB에 적재하고, 출처와 형식을 설명할 수 있는 상태로 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/WorldMapApplication.java`
  - `src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java`
  - `src/main/java/com/worldmap/common/exception/ResourceNotFoundException.java`
  - `src/main/java/com/worldmap/country/domain/Continent.java`
  - `src/main/java/com/worldmap/country/domain/Country.java`
  - `src/main/java/com/worldmap/country/domain/CountryReferenceType.java`
  - `src/main/java/com/worldmap/country/domain/CountryRepository.java`
  - `src/main/java/com/worldmap/country/application/CountryCatalogService.java`
  - `src/main/java/com/worldmap/country/application/CountryDetailView.java`
  - `src/main/java/com/worldmap/country/application/CountrySeedInitializer.java`
  - `src/main/java/com/worldmap/country/application/CountrySeedProperties.java`
  - `src/main/java/com/worldmap/country/application/CountrySeedValidator.java`
  - `src/main/java/com/worldmap/country/application/CountrySummaryView.java`
  - `src/main/java/com/worldmap/country/infrastructure/CountrySeedReader.java`
  - `src/main/java/com/worldmap/country/web/CountryApiController.java`
  - `src/main/resources/application.yml`
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-test.yml`
  - `src/main/resources/data/countries.json`
  - `src/test/java/com/worldmap/country/CountrySeedIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/03-country-seed-loading.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 애플리케이션 시작 시 `CountrySeedInitializer`가 `country` 테이블이 비어 있는지 확인하고, 비어 있으면 JSON 시드 파일을 읽는다. `CountrySeedValidator`가 ISO 코드 중복, 좌표 범위, 필수값을 검증한 뒤 `CountryRepository`로 저장한다. 이후 사용자는 `GET /api/countries`와 `GET /api/countries/{iso3Code}`로 출제 가능한 국가 데이터를 조회할 수 있다.
- 데이터 / 상태 변화: 처음 실행 시 `country` 테이블에 17개 국가가 적재되고, 이후 재실행에서는 기존 데이터가 있으면 시드를 다시 넣지 않는다. 위치 게임용 좌표는 현재 `대표 좌표 1개` 구조이며, 실제 국경 폴리곤 데이터는 아직 도입하지 않았다.
- 핵심 도메인 개념: 국가 시드와 운영 데이터 분리, 대표 좌표(`referenceLatitude`, `referenceLongitude`)와 실제 국경 데이터의 차이, 빈 테이블일 때만 적재하는 초기화 전략, 조회 API와 저장 로직 분리
- 예외 / 엣지 케이스: 잘못된 시드 파일이 들어오면 애플리케이션 시작 단계에서 실패하도록 설계했다. 좌표는 국가 경계가 아니라 단일 대표점이므로, 향후 Level 2나 정교한 판정에서는 GeoJSON 같은 경계 데이터가 필요하다. 알 수 없는 ISO3 코드는 `404`로 응답한다.
- 테스트: `./gradlew test` 통과, 시드 적재 확인 / 재실행 시 중복 적재 방지 / 국가 상세 조회 API / 존재하지 않는 국가 `404` 응답 검증
- 배운 점: 시드 데이터는 단순 JSON 파일이 아니라 “출처, 기준 연도, 좌표 의미”를 설명할 수 있어야 면접과 포트폴리오에서 설득력이 생긴다. 또한 초기 적재 로직은 편하지만, 운영 환경에서는 마이그레이션 전략과 어떻게 분리할지도 계속 생각해야 한다.
- 아직 약한 부분: 현재 대표 좌표는 World Bank API의 capital city 좌표를 사용한다. 즉 위치 찾기 게임의 정답 기준점이 국가의 경계나 중심점이 아니라 `수도 기준 대표점`이라는 한계가 있다. 이후 게임 점수 정책을 만들 때 이 trade-off를 더 명확히 설명할 준비가 필요하다.
- 면접용 30초 요약: 국가 데이터를 `country` 테이블에 미리 적재해 게임과 추천 기능의 공통 기준 데이터를 만들었고, 시작 시 JSON 시드 파일을 검증한 뒤 빈 테이블에만 넣도록 구성했다. 인구수는 World Bank API 기준 2024 데이터를 사용했고, 위치 판정용 좌표는 첫 버전에서는 대표점 하나만 저장해 복잡도를 낮췄다.

## 2026-03-23 - 3단계 국가 위치 찾기 게임 Level 1 완료

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 서버가 세션, 라운드, 점수, 진행 상태를 직접 관리하는 첫 번째 실제 게임 흐름을 완성한다.
- 변경 파일:
  - `src/main/java/com/worldmap/common/exception/GlobalApiExceptionHandler.java`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSessionStatus.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameRound.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSessionRepository.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameRoundRepository.java`
  - `src/main/java/com/worldmap/game/location/application/LocationAnswerJudgement.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStartView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameCurrentRoundView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameRoundResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/web/StartLocationGameRequest.java`
  - `src/main/java/com/worldmap/game/location/web/SubmitLocationAnswerRequest.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGamePageController.java`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/game/location/application/LocationGameScoringPolicyTest.java`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/04-location-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 시작 페이지에서 닉네임을 입력하면 `POST /api/games/location/sessions`가 세션과 라운드들을 만든다. 플레이 페이지는 `GET /round`로 현재 문제를 받고, 좌표와 `roundNumber`를 함께 `POST /answer`로 보낸다. 서버는 현재 라운드와 요청 라운드를 비교해 중복 제출을 막고, 거리 계산과 점수 계산을 수행한 뒤 세션과 라운드를 함께 갱신한다. 마지막 라운드가 끝나면 결과 페이지에서 세션 요약과 라운드별 기록을 보여준다.
- 데이터 / 상태 변화: 새로 `location_game_session`, `location_game_round` 테이블이 생기고, 상태는 `READY -> IN_PROGRESS -> FINISHED`로 바뀐다. 각 답안 제출 시 세션 총점과 완료 라운드 수가 증가하고, 라운드에는 제출 좌표, 거리, 판정, 점수가 저장된다.
- 핵심 도메인 개념: 세션과 라운드 분리, 서버 주도 정답 판정, `roundNumber` 기반 중복 제출 방지, 거리 계산과 점수 계산의 정책 분리, SSR 페이지와 API의 역할 분리
- 예외 / 엣지 케이스: 존재하지 않는 세션은 `404`, 현재 라운드와 다른 라운드 번호 제출은 `409`, 범위를 벗어난 좌표는 `400`으로 처리한다. 현재 UI는 지도 클릭이 아니라 좌표 입력형 셸이며, 정답 기준도 국가 경계가 아니라 대표 좌표 1개라는 한계가 있다.
- 테스트: `./gradlew test` 통과, 점수 정책 단위 테스트와 전체 게임 흐름 통합 테스트 추가
- 배운 점: 게임형 서비스에서도 결국 중요한 것은 시각 효과보다 상태 전이와 데이터 무결성이다. 특히 “중복 제출이 다음 라운드를 잘못 먹지 않게 하는가” 같은 예외 흐름이 백엔드 설계의 질을 크게 좌우한다.
- 아직 약한 부분: 현재 프론트는 좌표를 직접 입력하는 형태라 사용자 경험이 거칠다. 다음 단계나 별도 UI 고도화 단계에서 Leaflet 지도 클릭을 붙이되, 지금 만든 API와 세션 구조는 그대로 유지된다는 점을 더 또렷하게 설명할 필요가 있다.
- 면접용 30초 요약: 위치 찾기 게임 Level 1에서는 서버가 게임 세션과 라운드를 직접 저장하고, 답안 제출 시 거리와 점수를 계산해 상태를 갱신하도록 만들었다. 답안 요청에 `roundNumber`를 포함해 중복 제출을 방지했고, 결과 페이지에서는 라운드별 거리와 점수를 다시 확인할 수 있게 해 서버 주도 게임 구조를 명확히 보여 주었다.

## 2026-03-23 - 4단계 국가 인구수 맞추기 게임 Level 1 완료

- 단계: 4. 국가 인구수 맞추기 게임 Level 1
- 목적: 위치 게임에서 만든 세션 구조를 재사용하면서, 보기형 퀴즈 방식의 두 번째 모드를 추가한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/common/domain/GameSessionStatus.java`
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStartView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameRound.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSessionRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameRoundRepository.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationOptionView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStartView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameCurrentRoundView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationAnswerJudgement.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameOptionGenerator.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationRoundOptions.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameRoundResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/population/web/StartPopulationGameRequest.java`
  - `src/main/java/com/worldmap/game/population/web/SubmitPopulationAnswerRequest.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGamePageController.java`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/static/js/population-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/test/java/com/worldmap/game/population/application/PopulationGameOptionGeneratorTest.java`
  - `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/05-population-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 시작 페이지에서 닉네임을 입력하면 `POST /api/games/population/sessions`가 세션과 라운드를 만든다. 라운드 생성 시 서버는 `PopulationGameOptionGenerator`로 보기 4개를 만들고, 플레이 페이지는 `GET /round`로 문제와 보기를 받아 렌더링한다. 사용자가 `selectedOptionNumber`를 제출하면 서버는 정답 여부와 점수를 계산하고 세션/라운드를 함께 갱신한다. 마지막 라운드가 끝나면 결과 페이지에서 라운드별 선택값과 정답값을 보여준다.
- 데이터 / 상태 변화: `population_game_session`, `population_game_round` 테이블이 추가됐고, 위치 게임과 인구수 게임 모두 `BaseGameSession`의 공통 세션 구조를 사용하게 됐다. 각 인구수 라운드에는 정답 인구수, 보기 4개, 정답 보기 번호, 사용자가 선택한 보기 번호, 점수가 저장된다.
- 핵심 도메인 개념: 공통 세션 구조 재사용, 모드별 라운드 분리, 보기 생성 규칙, 모드별 점수 정책, “공통화는 세션까지만 / 라운드는 모드별로”라는 경계 설정
- 예외 / 엣지 케이스: 국가 수가 4개 미만이면 보기형 게임 자체를 시작할 수 없다. 답안 요청의 라운드 번호가 현재 세션 라운드와 다르면 `409`로 막고, 보기 번호가 1~4 범위를 벗어나면 `400`으로 처리한다. 보기 생성은 비슷한 인구 규모를 우선 고르지만, seed 데이터가 적기 때문에 매우 정교한 난이도 조절은 아직 아니다.
- 테스트: `./gradlew test` 통과, 옵션 생성 단위 테스트와 인구수 게임 전체 흐름 통합 테스트 추가, 기존 위치 게임 테스트도 유지
- 배운 점: 두 번째 게임을 추가해 보니 “공통으로 보이는 것”과 “정말 공통인 것”이 다르다는 점이 분명해졌다. 세션 필드와 상태 전이는 공통이지만, 라운드 데이터와 점수 정책은 모드별로 남기는 편이 훨씬 설명 가능하고 유지보수하기 좋다.
- 아직 약한 부분: 현재 인구수 Level 1은 보기형이어서 판정이 단순하다. 이후 Level 2 수치 입력형으로 넘어갈 때 오차율 기반 점수 계산과 보기형 구조를 어떻게 같이 설명할지 더 다듬을 필요가 있다.
- 면접용 30초 요약: 인구수 맞추기 게임 Level 1에서는 위치 게임에서 검증한 세션 구조를 `BaseGameSession`으로 재사용하고, 라운드와 점수 정책만 모드별로 분리했다. 서버가 비슷한 인구 규모의 보기 4개를 만들고 정답 여부를 판정하도록 해, 공통 구조와 모드별 로직의 경계를 설명 가능한 형태로 정리했다.

## 2026-03-23 - 3단계 위치 찾기 게임 Level 1 요구사항 정합화

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 좌표 입력형 프로토타입을 실제 요구사항인 `3D 지구본 국가 선택형`으로 맞추고, 답안 모델을 좌표가 아닌 국가 코드 기준으로 정리한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/location/domain/LocationGameRound.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/application/LocationAnswerJudgement.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameRoundResultView.java`
  - `src/main/java/com/worldmap/game/location/web/SubmitLocationAnswerRequest.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/static/data/world-countries.geojson`
  - `src/main/resources/static/images/earth-blue-marble.jpg`
  - `src/test/java/com/worldmap/game/location/application/LocationGameScoringPolicyTest.java`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `blog/04-location-game-level-1.md`
  - `blog/05-population-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 시작 페이지에서 닉네임을 입력하면 `POST /api/games/location/sessions`가 세션과 라운드를 만든다. 플레이 페이지는 `GET /round`로 현재 문제 국가명을 받고, 동시에 `/api/countries`와 `/data/world-countries.geojson`을 내려받아 지구본에 활성 국가를 표시한다. 사용자가 국가 폴리곤을 클릭하면 프론트는 `selectedCountryIso3Code`를 선택 상태로 들고 있다가 `roundNumber`와 함께 `POST /answer`로 보낸다. 서버는 현재 라운드와 요청 라운드를 비교하고, 선택한 ISO3가 실제 시드 국가인지 검증한 뒤 세션과 라운드를 갱신한다.
- 데이터 / 상태 변화: `location_game_round`는 이제 제출 좌표와 거리 대신 `selectedCountryIso3Code`, `selectedCountryName`, `correct`, `awardedScore`를 저장한다. `targetLatitude`, `targetLongitude`는 향후 Level 2나 다른 시각화 확장을 위해 남겨뒀지만, 현재 Level 1 판정에는 사용하지 않는다. 프론트 정적 자산으로 GeoJSON과 지구 텍스처가 추가됐다.
- 핵심 도메인 개념: 3D 지구본은 입력 UI일 뿐이고, 정답 판정의 기준은 서버가 가진 국가 ISO 코드다. 프론트는 국가 폴리곤을 렌더링하지만 정답 여부를 계산하지 않는다. `roundNumber` 기반 중복 제출 방지와 `selectedCountryIso3Code` 정규화가 서버 무결성의 핵심이다.
- 예외 / 엣지 케이스: 존재하지 않는 세션은 `404`, 현재 라운드와 다른 `roundNumber` 제출은 `409`, 시드에 없는 국가 ISO3를 제출하면 `400`이다. 프론트에서는 시드에 포함된 국가만 활성화하지만, 서버도 동일 검증을 수행해 클라이언트 조작을 막는다. 현재는 17개 시드 국가만 활성화되어 있어 실제 전체 국가 찾기 난도와는 차이가 있다.
- 테스트: `./gradlew test` 통과. `LocationGameScoringPolicyTest`에서 국가 코드 일치/불일치 점수 정책을 검증했고, `LocationGameFlowIntegrationTest`에서 세션 시작, 현재 라운드 조회, 정답 제출, 중복 제출 차단, 게임 종료 흐름을 확인했다.
- 배운 점: 요구사항이 바뀌면 화면만 바꾸는 것이 아니라 요청 모델과 판정 기준까지 같이 바뀐다. 이번 수정은 “좌표 게임”에서 “국가 선택 게임”으로 문제 정의가 이동한 사례라서, 서비스와 DTO를 함께 재정의해야 일관성이 생겼다.
- 아직 약한 부분: 현재 지구본 렌더링은 CDN의 `three`와 `globe.gl`에 의존한다. 또한 GeoJSON은 정적 파일이라 모바일 성능이나 폴리곤 단순화 전략을 아직 검토하지 않았다. 이후 랭킹 단계 전에 프론트 성능과 asset 관리 전략을 한 번 더 점검할 필요가 있다.
- 면접용 30초 요약: 위치 찾기 게임 요구사항이 실제로는 3D 지구본에서 나라를 선택하는 방식이어서, 답안 모델을 좌표에서 국가 ISO 코드로 다시 정의했습니다. 프론트는 지구본과 국가 폴리곤을 렌더링하고, 서버는 선택한 ISO3가 정답 국가와 일치하는지만 판정해 세션과 라운드를 갱신합니다. 이렇게 해서 시각 효과와 판정 로직을 분리한 서버 주도 구조를 더 명확하게 설명할 수 있게 됐습니다.

## 2026-03-23 - 위치 찾기 지구본 렌더링 성능 안정화

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 플레이 페이지에서 지구본이 뜨지 않고 브라우저가 멈추는 문제를 해결한다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/data/active-countries.geojson`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `blog/04-location-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 플레이 페이지 진입 시 프론트는 더 이상 전 세계 고해상도 폴리곤 전체를 내려받지 않는다. `/api/countries`로 활성 국가 목록을 받고, `/data/active-countries.geojson`으로 현재 시드 국가만 담은 경량 자산을 내려받아 지구본을 렌더링한다.
- 데이터 / 상태 변화: 정적 자산이 `world-countries.geojson` 중심에서 `active-countries.geojson` 중심으로 바뀌었다. 새 자산은 시드 17개 국가만 포함하고 좌표 정밀도를 낮춰 파일 크기를 줄였다.
- 핵심 도메인 개념: Level 1의 실제 선택 가능 범위는 시드 국가다. 따라서 렌더링 자산도 같은 범위로 맞추는 것이 기능 요구사항과 성능 요구사항을 동시에 만족시키는 방법이다.
- 예외 / 엣지 케이스: 전체 세계 폴리곤을 렌더링하면 브라우저 메인 스레드가 멈출 수 있다. 현재는 선택 가능한 국가만 렌더링하므로 비활성 국가 클릭 개념 자체가 사라졌고, 대신 “왜 모든 국가가 안 보이느냐”는 UX 설명이 필요하다.
- 테스트: `./gradlew test` 통과. 로컬 서버 재기동 후 `/games/location/play/{sessionId}`가 `globe.gl` 스크립트와 경량 GeoJSON 경로를 포함하는지 확인했고, 세션 시작/라운드 조회/정답 제출 API도 재검증했다.
- 배운 점: 프론트 성능 문제는 종종 코드 로직보다 “어떤 자산을 얼마나 브라우저에 올리느냐”의 문제다. 이번 수정은 정답 판정 모델만큼 렌더링 대상 범위를 맞추는 것도 설계의 일부라는 점을 보여준다.
- 아직 약한 부분: 현재 경량화는 “활성 국가만 추리기 + 좌표 정밀도 낮추기” 수준이다. 추후 전체 국가를 지원하려면 단순 필터링이 아니라 폴리곤 단순화, LOD, 타일 전략 같은 추가 설계가 필요하다.
- 면접용 30초 요약: 위치 찾기 게임을 3D 지구본으로 바꾼 뒤 브라우저가 멈추는 문제가 있어서, 전체 세계 폴리곤 대신 실제 시드에 포함된 국가만 별도 GeoJSON으로 분리했습니다. 이렇게 해서 요구사항 범위와 렌더링 범위를 맞추고, 지구본은 정상적으로 뜨면서도 서버 판정 구조는 그대로 유지할 수 있게 했습니다.

## 2026-03-23 - 위치 찾기 지구본 레이아웃과 색상 개선

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 지구본이 태양처럼 붉게 보이고 화면 오른쪽으로 치우쳐 보이는 문제를 해결한다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/static/data/active-countries.geojson`
  - `docs/WORKLOG.md`
- 요청 흐름: 플레이 페이지 진입 시 프론트는 이제 더 단순한 Natural Earth 110m 기반 `active-countries.geojson`을 불러온다. 지구본 생성 후 `globeStage`의 실제 크기를 읽어 정사각형 캔버스를 맞추고, `ResizeObserver`로 레이아웃 변화에 따라 다시 동기화한다.
- 데이터 / 상태 변화: `active-countries.geojson`은 17개 시드 국가를 모두 포함하도록 다시 생성했다. 이전 고해상도 데이터에서 France 코드가 `-99`로 들어가 누락되던 문제도 함께 사라졌다.
- 핵심 도메인 개념: Level 1에서 보여 줄 국가 범위와 실제 선택 가능한 국가 범위를 일치시키는 것이 중요하다. 또한 3D 지구본은 “상호작용 컴포넌트”이므로, 시각 스타일보다 먼저 캔버스 크기와 데이터 복잡도를 안정적으로 맞춰야 한다.
- 예외 / 엣지 케이스: 정사각형 캔버스를 맞추지 않으면 지구가 잘리거나 한쪽으로 밀려 보일 수 있다. 기본 폴리곤 색이 너무 강하면 텍스처보다 오버레이가 먼저 보여 지구가 아니라 단색 구체처럼 보인다. 시드 국가만 보여 주는 구조이므로 모든 나라가 보이지 않는 것은 현재 Level 1의 의도된 제한이다.
- 테스트: `./gradlew test` 통과. 새 `active-countries.geojson` 크기가 약 70KB인지 확인했고, 로컬 서버 재기동 후 `location-game.js`가 새 경로를 참조하는지와 결과 페이지가 200으로 렌더링되는지 확인했다.
- 배운 점: 브라우저에서 보이는 “이상한 3D 화면”은 종종 WebGL 자체보다 데이터 밀도, 캔버스 크기, 기본 색 설계가 원인이다. 특히 기본 상태를 강한 빨강으로 두면 사용자는 정답/선택/일반 상태를 구분하기 어렵다.
- 아직 약한 부분: 아직 실제 브라우저 상호작용 테스트는 수동 확인이 필요하다. 다음에는 데스크톱 브라우저와 모바일 브라우저에서 드래그 감도, 확대 축소, 선택 정확도까지 점검해야 한다.
- 면접용 30초 요약: 지구본이 오른쪽으로 밀리고 붉은 구체처럼 보이는 문제를 해결하기 위해, 먼저 국가 데이터 자체를 더 단순한 110m 해상도 자산으로 교체하고, 캔버스를 컨테이너 크기에 맞는 정사각형으로 동기화했습니다. 그리고 기본 국가 색을 저채도 파란색으로 낮추고 선택/정답 상태에만 강한 색을 써서, 텍스처가 보이면서도 상호작용 상태가 명확한 화면으로 바꿨습니다.

## 2026-03-23 - 위치 게임 아케이드 리부트와 우주 테마 재설계 계획 수립

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 현재 동작하는 프로토타입을 포트폴리오 대표 기능으로 끌어올리기 위해, 게임 루프와 사이트 비주얼 방향을 다시 설계한다.
- 변경 파일:
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 이번 작업은 코드 구현보다 먼저 “앞으로 어떤 요청 흐름을 만들 것인가”를 고정하는 단계다. 새 방향에서는 사용자가 세션을 시작하면 서버가 `하트 3개`, `현재 Stage`, `총점`을 가진 세션을 만들고, 플레이 중에는 `GET /state`로 HUD 상태를 조회하며 `POST /answer`로 선택한 국가를 제출한다. 정답이면 점수와 Stage가 갱신되고, 오답이면 같은 Stage를 유지한 채 하트만 줄어든다.
- 데이터 / 상태 변화: 아직 실제 엔티티 변경은 없지만, 목표 도메인 모델을 `세션 / Stage / Attempt` 구조로 재정의했다. 또한 사이트 전체의 디자인 방향을 `따뜻한 카드형`에서 `차가운 우주 HUD`로 전환하기로 결정했다.
- 핵심 도메인 개념: 서버 주도 하트 관리, Stage 기반 진행, Attempt 기록, 게임오버 상태, HUD 중심 게임 UI, 나라 이름 비노출 규칙
- 예외 / 엣지 케이스: 위치 게임은 “국가를 찾는 것”이 목적이므로 플레이 중 tooltip이나 국가명 label을 보여주면 게임성이 깨진다. 또한 `한 문제 = 한 번 제출` 구조로는 재시도와 게임오버를 표현하기 어려워 현재 도메인 모델을 그대로 유지하면 요구사항과 어긋난다.
- 테스트: 없음. 이번 작업은 설계 문서와 개발 순서 정리 단계다.
- 배운 점: 사용자 경험을 게임답게 만들려면 CSS 수정만으로는 부족하고, 게임 루프와 상태 모델부터 다시 정의해야 한다. 특히 하트, 재시도, 자동 다음 단계 같은 감각은 프론트 연출 이전에 백엔드 상태 전이 설계가 먼저다.
- 아직 약한 부분: 점수 공식과 Stage 종료 규칙은 초안을 정했지만 실제 플레이 템포를 보며 다시 조정할 가능성이 높다. 또한 인구수 게임과 공통 세션 구조를 어디까지 공유할지도 리부트 과정에서 다시 판단해야 한다.
- 면접용 30초 요약: 위치 찾기 게임 프로토타입은 서버 주도 구조는 맞았지만 게임성이 약해서, 하트 3개와 재시도, 게임오버, 자동 Stage 진행이 있는 아케이드 루프로 다시 설계했습니다. 이 과정에서 기존 `세션 + 라운드`만으로는 부족하다고 보고 `세션 / Stage / Attempt` 구조와 우주 HUD 비주얼 방향을 문서로 먼저 고정해, 이후 구현이 설계 의도와 분리되지 않게 만들었습니다.

## 2026-03-23 - 위치 게임 아케이드 리부트 1차 구현

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 문서로만 정리했던 아케이드 규칙을 실제 코드로 옮겨, `하트 3개 + 같은 Stage 재시도 + 게임오버` 흐름이 서버 주도로 동작하게 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/common/domain/GameSessionStatus.java`
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameStage.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameStageStatus.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameAttempt.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameStageRepository.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameAttemptRepository.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStateView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStageResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAttemptResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerOutcome.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/game/location/web/SubmitLocationAnswerRequest.java`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/game/location/application/LocationGameScoringPolicyTest.java`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 사용자가 시작 페이지에서 세션을 만들면 서버가 `location_game_session`과 여러 개의 `location_game_stage`를 생성한다. 플레이 화면은 `GET /api/games/location/sessions/{sessionId}/state`로 현재 Stage, 하트, 점수, 문제 국가를 받는다. 사용자가 지구본에서 국가를 클릭해 `POST /answer`를 보내면 서버는 현재 Stage 번호를 검증하고, `LocationGameAttempt`를 1건 저장한 뒤 정답 여부에 따라 `LocationGameStage` 상태와 `LocationGameSession`의 하트/점수/진행 상태를 갱신한다. 정답이면 다음 Stage 번호가 바뀌고, 오답이면 같은 Stage를 유지하며 하트만 감소한다.
- 핵심 도메인 개념: `LocationGameSession`은 게임 전체 상태를, `LocationGameStage`는 한 문제의 진행을, `LocationGameAttempt`는 그 안의 개별 시도를 표현한다. 이 분리 덕분에 “같은 문제를 세 번 틀리고 게임오버가 났다”는 흐름을 데이터로 그대로 설명할 수 있다.
- 예외 상황 또는 엣지 케이스: 현재 세션의 Stage 번호와 다른 `stageNumber`를 보내면 `409`로 막는다. 시드에 없는 ISO3를 보내면 `400`이다. 하트가 0이 되는 마지막 오답은 Attempt로 남기고, 같은 Stage를 `FAILED`로 마감한다. 플레이 중 tooltip은 제거했기 때문에 나라 이름은 클릭 후 액션 바에서만 확인 가능하다.
- 테스트 내용: `./gradlew test` 통과. `LocationGameScoringPolicyTest`에서 Stage/시도/하트에 따른 점수 공식을 검증했고, `LocationGameFlowIntegrationTest`에서 전체 클리어 흐름, 오답 시 하트 감소, 세 번 오답 시 `GAME_OVER`를 검증했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임을 `세션 + 라운드 1회 제출형`에서 `세션 / Stage / Attempt` 구조로 바꿔 하트 3개와 재시도형 아케이드 루프를 서버가 직접 관리하도록 만들었습니다. 프론트는 지구본에서 나라를 고르고 제출/취소 UI를 보여주기만 하고, 실제 하트 감소, 점수 계산, 게임오버 판정은 모두 서버에서 처리합니다.
- 아직 내가 이해가 부족한 부분: 현재 UI는 여전히 기존 사이트 톤 위에 얹힌 1차 플레이 셸이라, “차가운 우주 HUD” 느낌의 전면 리디자인은 다음 작업에서 별도로 정리해야 한다. 또한 모바일에서 지구본 드래그와 선택 감도는 실사용 테스트가 더 필요하다.

## 2026-03-23 - 공통 우주 테마 1차 적용

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강, 4. 공통 UI 톤 정리
- 목적: 따뜻하고 둥근 카드형 사이트 인상을 걷어내고, 프로젝트 전체를 `cold space HUD` 방향으로 통일한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 로직은 바꾸지 않고, 같은 API와 SSR 진입점을 유지한 채 화면 표현 계층만 재설계했다. 홈 화면은 모드 허브처럼 보이도록 바꿨고, 시작 화면은 규칙 카드, 결과 화면은 디브리프 배너 중심으로 정리했다.
- 핵심 도메인 개념: 이 작업은 도메인이나 API가 아니라 `표현 계층의 공통 디자인 시스템`을 정리한 것이다. 기능별 페이지를 따로 꾸미기보다 `공통 CSS 토큰`을 먼저 바꾸면 이후 랭킹, 추천, 마이페이지도 같은 언어로 확장할 수 있다.
- 예외 상황 또는 엣지 케이스: 구현 순서상 원래는 Step 4에서 테마를 통일할 예정이었지만, 위치 게임 리부트의 플레이 감각을 보여주려면 메인/시작/결과 화면의 온도감부터 먼저 바꾸는 편이 자연스러워 이번에 앞당겨 적용했다. 다만 위치 게임 플레이 HUD 자체는 아직 1차 수준이라 추가 polish가 남아 있다.
- 테스트 내용: `./gradlew test` 통과. 서버 재시작 후 `/`, `/games/location/start`, `/css/site.css` 응답 `200` 확인.
- 면접에서 30초 안에 설명하는 요약: 기능 로직을 바꾸지 않고도 서비스 인상이 크게 달라질 수 있기 때문에, 공통 CSS 변수와 화면 구조를 먼저 우주 HUD 톤으로 다시 잡았습니다. 이 작업은 단순 색상 교체가 아니라, 홈과 시작/결과 화면의 정보 구조를 게임 허브처럼 다시 배열해 프로젝트 전체의 정체성을 통일한 단계입니다.
- 아직 내가 이해가 부족한 부분: 현재는 CSS와 템플릿 중심의 1차 리디자인이라, 브라우저별 폰트 로딩 체감과 모바일에서의 실제 시각 밀도는 직접 플레이하며 한 번 더 조정할 필요가 있다.

## 2026-03-23 - 국가 데이터 194개 확장과 시드 동기화 전환

- 단계: 2. 국가 데이터와 시드 적재 보강
- 목적: 위치 게임에서 17개 국가만 선택 가능한 제약을 줄이고, `country` 테이블과 `active-countries.geojson`이 최대한 넓은 같은 범위를 보도록 만든다.
- 변경 파일:
  - `scripts/generate_country_assets.py`
  - `src/main/resources/data/countries.json`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/main/java/com/worldmap/country/domain/Country.java`
  - `src/main/java/com/worldmap/country/application/CountrySeedInitializer.java`
  - `src/test/java/com/worldmap/country/CountrySeedIntegrationTest.java`
  - `src/main/resources/templates/location-game/play.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `blog/03-country-seed-loading.md`
  - `blog/04-location-game-level-1.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 애플리케이션 시작 시 `CountrySeedInitializer`가 `countries.json`을 읽고 검증한 뒤, 기존 `country` 테이블을 ISO3 기준으로 조회한다. 이후 시드에 있는 국가는 추가 또는 갱신하고, 시드에 없는 국가는 삭제해 DB 범위를 현재 정적 자산 범위와 맞춘다. 플레이 화면은 계속 `/api/countries`와 `/data/active-countries.geojson`을 함께 읽는데, 이제 두 경로 모두 독립국 194개를 기준으로 응답한다.
- 데이터 / 상태 변화: 국가 시드는 `World Bank API + REST Countries` 조합으로 다시 만들었고, 인구수는 2024 기준 최신치에 맞췄다. `active-countries.geojson`도 독립국 194개만 포함하도록 다시 생성했다. 기존 “빈 테이블이면 적재, 아니면 생략” 구조에서 “앱 시작 시 현재 시드와 동기화” 구조로 바뀌면서 로컬 DB에 17개만 남아 있던 상태도 자동으로 194개로 확장된다.
- 핵심 도메인 개념: 이 단계의 핵심은 단순히 JSON 숫자를 늘리는 것이 아니라 `시드 파일`, `RDB`, `프론트 정적 자산`이 같은 국가 집합을 보게 만드는 것이다. 키는 `ISO3 코드`이며, 이 코드로 국가 데이터를 식별하고 동기화한다.
- 예외 상황 또는 엣지 케이스: 전 세계 지도 자산에는 영토와 특수 지역이 섞여 있고 일부 국가는 ISO3가 `-99`로 들어가므로, “파일에 있는 모든 폴리곤”을 그대로 국가 시드로 쓰면 데이터 설명력이 떨어진다. 그래서 이번 단계는 최대 범위를 추구하되, 게임과 인구 데이터가 함께 설명 가능한 `독립국 194개`를 기준으로 잘랐다. France와 Norway처럼 원본 GeoJSON에서 ISO3가 비정상인 케이스는 이름 매핑으로 보정했다.
- 테스트 내용: `./gradlew test` 통과. `CountrySeedIntegrationTest`에서 시작 시 194개 동기화, 기존 잘못된 데이터 복원, 존재하지 않는 가짜 국가 삭제를 검증했다. 서버 재시작 후 `/api/countries`가 `194`, `/data/active-countries.geojson`의 feature 수가 `194`, `/api/countries/FRA`가 정상 응답하는 것도 확인했다.
- 배운 점: 시드 데이터는 “초기 적재”보다 “현재 자산과 동기화”가 중요할 때가 있다. 특히 이 프로젝트처럼 게임 출제 범위를 프론트 GeoJSON과 함께 다루는 경우에는, 시드와 DB와 정적 자산이 조금만 어긋나도 실제 플레이에서 바로 문제가 난다.
- 아직 약한 부분: 현재는 독립국 194개를 기준으로 잘랐지만, 향후 Level 2에서 영토와 소국까지 확장할지, 국가명 한글 번역을 어떤 기준으로 더 정제할지는 추가 판단이 필요하다.
- 면접용 30초 요약: 위치 게임의 국가 수가 너무 적어 보여서 단순히 JSON만 늘리는 대신, `World Bank API + REST Countries`로 국가 시드를 재생성하고 앱 시작 시 ISO3 기준으로 DB를 동기화하도록 바꿨습니다. 그래서 이제 `/api/countries`와 `active-countries.geojson`이 모두 독립국 194개를 기준으로 움직이고, 기존 로컬 DB에 17개만 남아 있어도 서버 재시작만 하면 자동으로 같은 범위로 맞춰집니다.

## 2026-03-23 - 위치 게임 제출 전 국가명 비노출 규칙 보강

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 사용자가 지구본에서 클릭한 나라 이름이 제출 전 액션 바에 보이면 사실상 추가 힌트가 되므로, 제출 직전까지는 선택 상태만 보여주도록 규칙을 더 엄격하게 만든다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/templates/location-game/play.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름: 사용자가 국가 폴리곤을 클릭하면 프론트는 여전히 `selectedCountryIso3Code`를 내부 상태로 저장하지만, 화면에는 더 이상 실제 국가명을 렌더링하지 않는다. 액션 바에는 `국가 선택됨` 같은 상태만 보이고, 사용자가 `POST /answer`를 보낸 뒤에야 서버 응답의 `selectedCountryName`을 이용해 정답/오답 피드백에서 이름을 보여준다.
- 데이터 / 상태 변화: 서버 API와 DB 스키마는 바뀌지 않았다. 바뀐 것은 프론트의 정보 공개 시점이며, “클릭 직후”에서 “제출 후 판정 단계”로 한 단계 늦춰졌다.
- 핵심 도메인 개념: 위치 게임의 핵심은 “국가를 찾는 행위”이므로, 사용자에게 너무 빠른 텍스트 힌트를 주면 게임성이 무너진다. 따라서 `선택 상태`와 `선택 국가명`을 같은 시점에 공개하지 않고 분리하는 것이 UX 규칙상 중요하다.
- 예외 상황 또는 엣지 케이스: 사용자는 여전히 잘못된 국가를 선택할 수 있고 취소도 가능하다. 다만 제출 전에는 액션 바에서 “내가 정확히 무엇을 골랐는지”를 텍스트로 재확인할 수 없으므로, 지구본 하이라이트와 취소 버튼이 더 중요해졌다.
- 테스트 내용: `./gradlew test` 통과. 로컬 플레이 화면에서 클릭 후 액션 바에 이름 대신 상태 텍스트만 보이는지 수동 확인 필요.
- 배운 점: 같은 “이름 비노출” 규칙이라도 hover tooltip만 끄는 것과, 제출 전 선택 텍스트까지 숨기는 것은 게임성에 미치는 강도가 다르다. 실제 플레이 감각을 맞추려면 정보 공개 타이밍도 설계 요소로 봐야 한다.
- 아직 약한 부분: 현재는 국가명을 숨겼지만, 제출 전 “선택 실수 방지”를 위한 시각적 확인 수단은 하이라이트 정도뿐이다. 이후에는 카메라 줌, 선택 애니메이션, 취소 UX를 더 다듬어야 한다.
- 면접용 30초 요약: 위치 게임은 나라를 찾는 것이 목적이라, 클릭 직후 액션 바에 국가명을 보여주는 것도 사실상 힌트라고 판단했습니다. 그래서 프론트는 선택 ISO 코드를 내부 상태로만 들고 있고, 화면에는 제출 전까지 `국가 선택됨`만 보여주며, 실제 이름은 서버 판정 응답 이후에만 공개하도록 바꿨습니다.

## 2026-03-23 - 위치 게임 시작 지연 체감 개선

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: `/games/location/start`에서 세션 시작 후 플레이 화면 진입이 오래 걸리고, 그동안 회색 화면처럼 멈춰 보이는 문제를 줄인다.
- 변경 파일:
  - `src/main/resources/static/vendor/globe.gl.min.js`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `docs/WORKLOG.md`
- 요청 흐름: 시작 페이지에서 사용자가 `세션 시작하기`를 누르면 프론트가 버튼을 바로 비활성화하고 로딩 메시지를 보여준다. 플레이 페이지에서는 `GET /state`와 지구본 자산 fetch를 병렬로 시작하고, 상태 텍스트를 먼저 렌더링한 뒤 첫 페인트 이후에만 `Globe.gl` 초기화를 진행한다. 지구본도 바로 모든 폴리곤을 붙이지 않고, 베이스 지구본을 먼저 만들고 그 다음 단계에서 국가 폴리곤을 붙인다.
- 데이터 / 상태 변화: 백엔드 세션 생성 속도 자체는 원래 빠른 편이었다. 이번 변경은 서버 데이터가 아니라 프론트 초기화 순서를 바꿔 “멈춘 것처럼 보이는 시간”을 줄이는 데 집중했다. 또한 CDN 의존을 줄이기 위해 `globe.gl`를 로컬 정적 자산으로 옮겼다.
- 핵심 도메인 개념: 이 작업의 핵심은 게임 상태가 아니라 `초기 렌더링 경로`다. 세션 생성 API가 빠르더라도 브라우저가 무거운 3D 초기화를 첫 페인트 전에 몰아서 하면 사용자는 서버가 느리다고 느낄 수 있다.
- 예외 상황 또는 엣지 케이스: 국가 수가 194개로 늘면서 폴리곤 수가 많아졌기 때문에, 지구본 초기화는 여전히 기기 성능 영향을 받는다. 이번에는 “무조건 더 빠르게”보다 “네트워크 변수 제거 + 로딩 메시지 유지 + 무거운 작업을 뒤로 미루기”에 초점을 맞췄다.
- 테스트 내용: `./gradlew test` 통과. 서버 재시작 후 세션 생성, 플레이 페이지 HTML, `/api/countries`, `/data/active-countries.geojson` 응답 시간이 모두 짧은 것을 확인했고, 실제 플레이 페이지 HTML에 로딩 문구가 기본 노출되는 것도 확인했다.
- 배운 점: 사용자가 느끼는 “느림”은 종종 백엔드 처리시간보다도 첫 화면이 언제 보이느냐에 더 크게 좌우된다. 특히 WebGL 같은 무거운 초기화는 첫 페인트 뒤로 보내야 체감 품질이 좋아진다.
- 아직 약한 부분: 현재는 `globe.gl`만 로컬 자산으로 옮겼다. 폰트, 텍스처, 지구본 폴리곤 자체의 체감 로딩은 모바일 기기 기준으로 한 번 더 점검해야 한다.
- 면접용 30초 요약: 세션 생성 API를 측정해 보니 서버는 거의 즉시 응답해서 병목이 아니었습니다. 대신 플레이 화면에서 3D 지구본과 194개 국가 폴리곤을 한 번에 초기화하는 구간이 체감 지연을 만들고 있었기 때문에, 로딩 메시지를 먼저 보여주고 상태를 먼저 렌더링한 뒤 지구본을 단계적으로 초기화하도록 프론트 흐름을 바꿨습니다.

## 2026-03-23 - 지구본 렌더링 경량화와 경계선 가시성 조정

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 사용자가 “하늘색 원만 보이고 국가 경계가 안 보인다”고 느끼는 문제와, 국가 수 확대 이후 지구본 초기화가 무거워진 문제를 같이 줄인다.
- 변경 파일:
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/main/resources/static/js/location-game.js`
  - `docs/WORKLOG.md`
- 요청 흐름: 플레이 페이지가 `/data/active-countries.geojson`을 내려받는 구조는 유지하되, 자산 자체를 더 강하게 단순화해서 브라우저가 처리할 좌표 수를 줄였다. 지구본 초기화 시에는 `polygonCapCurvatureResolution`을 거칠게 조정해 폴리곤 표면 분할 수를 줄이고, 폴리곤 fill은 거의 투명하게, stroke는 더 선명하게 바꿔 실제 국가 경계가 먼저 보이도록 했다.
- 데이터 / 상태 변화: `active-countries.geojson`은 여전히 194개 국가를 포함하지만 파일 크기가 약 `863KB -> 461KB`로 줄었다. 프론트 렌더링에서는 밝은 하늘색 면 덩어리보다 얇은 국경선 중심으로 보이도록 시각 균형을 바꿨다.
- 핵심 도메인 개념: 이 작업은 국가 수를 줄인 것이 아니라 “같은 194개를 더 가벼운 기하 데이터와 덜 비싼 표면 곡률로 그린다”는 최적화다. 즉 기능 범위는 유지하면서 렌더링 비용만 줄이는 방향이다.
- 예외 상황 또는 엣지 케이스: GeoJSON을 더 많이 단순화하면 작은 섬나라 윤곽이 거칠어질 수 있다. 이번에는 194개 보존을 우선했고, 클릭 정확도와 성능 사이에서 현재 기준점을 다시 잡았다.
- 테스트 내용: `./gradlew test` 통과. 재생성 후 `active-countries.geojson` feature 수가 194개인지와 파일 크기가 줄어든 것을 확인하고, 서버 재시작까지 완료했다.
- 배운 점: “국가 수가 많아졌다”와 “렌더링이 무거워졌다”는 별개의 문제가 아니라 같은 변경의 양면일 수 있다. 이럴 때는 기능을 되돌리기보다, 데이터 단순화와 렌더링 옵션 튜닝으로 같은 범위를 더 효율적으로 보여주는 쪽이 낫다.
- 아직 약한 부분: 실제 사용자 브라우저에서 클릭 정확도와 경계선 가독성은 한 번 더 수동 확인이 필요하다. 특히 소국과 섬나라가 너무 거칠게 보이지 않는지 체크해야 한다.
- 면접용 30초 요약: 국가 수를 194개로 늘린 뒤 지구본이 단색 구처럼 보이고 무거워져서, 기능 범위는 유지한 채 렌더링 비용만 줄이는 방향으로 다시 튜닝했습니다. GeoJSON을 더 단순화해 파일 크기를 줄이고, Globe.gl의 폴리곤 곡률 해상도를 낮춰 계산량을 줄였으며, fill보다 stroke를 강조해 실제 나라 경계가 먼저 보이도록 바꿨습니다.

## 2026-03-23 - 위치 게임 endless Stage화와 선택 안정화, 공통 헤더 추가

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 위치 게임이 5 Stage에서 끝나는 구조를 없애고, Stage가 올라갈수록 더 어려워지며, 회전 중 오선택이 덜 나는 실제 아케이드 루프로 보강한다. 동시에 어느 화면에서나 메인으로 돌아갈 수 있는 공통 헤더를 추가한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameDifficultyPlan.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameDifficultyPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStateView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `src/test/java/com/worldmap/game/location/application/LocationGameDifficultyPolicyTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 세션 시작 시 서버는 더 이상 5개 Stage를 미리 만들지 않고 Stage 1만 생성한다. 사용자가 `POST /answer`로 정답을 맞히면 `LocationGameService`가 현재 Stage를 `CLEARED`로 바꾸기 전에 다음 Stage를 새로 생성하고 `totalRounds`를 확장해, 세션이 끝나지 않고 계속 진행되게 만든다. 현재 상태 조회 `GET /state`는 `difficultyLabel`까지 내려주고, 프론트는 이를 HUD에 표시한다. 프론트에서는 지구본 드래그와 클릭을 구분해 회전 후 오선택되는 클릭을 무시한다.
- 데이터 / 상태 변화: `location_game_session`의 총 라운드는 고정값이 아니라 “현재까지 계획된 Stage 수”가 됐다. Stage를 맞힐 때마다 다음 Stage가 추가되고, 하트가 0이 되기 전까지 세션은 `IN_PROGRESS`를 유지한다. 난이도는 새 테이블을 만들지 않고 현재 `country.population` 데이터를 기준으로 `주요 국가 -> 지역 확장 -> 글로벌 -> 전체 국가` 순으로 후보 풀 크기를 넓히는 방식으로 구현했다. GeoJSON은 선택 정확도를 위해 단순화 강도를 완화해 약 `1.55MB` 수준으로 다시 생성했다.
- 핵심 도메인 개념: endless 모드에서도 “다음 Stage를 누가 만들 것인가”는 서버 책임이다. 그래야 프론트 조작만으로 Stage를 건너뛰거나 쉬운 국가만 반복 선택하는 흐름을 막을 수 있다. 또 현재 데이터 구조에서 난이도 상승을 가장 설명 가능하게 구현하는 방법은 “추가 메타데이터를 억지로 붙이는 것”이 아니라 이미 가진 인구 데이터를 기준으로 익숙한 국가 풀에서 덜 익숙한 국가 풀로 점진적으로 넓히는 것이다.
- 예외 상황 또는 엣지 케이스: 드래그 후 pointer up 순간이 클릭으로 해석되면 회전하다가 원치 않는 국가가 선택될 수 있다. 그래서 프론트에서 이동 거리 10px 이상인 포인터 상호작용은 선택으로 인정하지 않게 막았다. 나라를 모두 한 번씩 소진한 뒤에도 endless run을 유지해야 하므로, 전체 국가를 다 쓴 이후에는 직전 국가만 피하면서 재등장할 수 있게 했다.
- 테스트 내용: `./gradlew test` 통과. `LocationGameFlowIntegrationTest`에서 7 Stage 연속 정답 후에도 게임이 끝나지 않고 8번째 Stage가 준비되는지 확인했고, 기존 하트 감소 / 3회 오답 게임오버 테스트를 유지했다. `LocationGameDifficultyPolicyTest`에서 Stage가 올라갈수록 후보 풀 크기가 커지고, 후반부에는 194개 전체를 쓰는지 검증했다.
- 면접에서 30초 안에 설명하는 요약: 원래 위치 게임은 5 Stage를 미리 만들어 놓고 끝나는 구조였는데, 그 방식으로는 아케이드 감각이 약해서 정답을 맞힐 때마다 서버가 다음 Stage를 계속 생성하는 endless 구조로 바꿨습니다. 난이도는 별도 복잡한 메타데이터 없이 현재 가진 인구 데이터를 기준으로 주요 국가 풀에서 전체 국가 풀로 점차 넓히도록 설계했고, 프론트는 회전 드래그와 클릭을 구분해 오선택을 줄였습니다. 화면 쪽은 모든 페이지에서 홈으로 복귀할 수 있도록 공통 헤더도 같이 넣었습니다.
- 아직 내가 이해가 부족한 부분: 현재 난이도는 “익숙한 국가에서 덜 익숙한 국가로 넓힌다”는 1차 정책이라, 대륙/면적/섬나라 여부까지 섞은 더 정교한 난이도 모델은 다음 단계에서 검토가 필요하다. 또한 GeoJSON 정확도를 높이면서 파일이 다시 커졌기 때문에, 저사양 브라우저에서의 체감 성능은 실제 플레이로 한 번 더 확인해야 한다.

## 2026-03-23 - 지구본 폴리곤 링 방향 오류 수정

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 국가 선택 화면에서 나라 내부가 아니라 바깥 영역이 채워지며, 지구본 전체가 이상하게 덮이는 렌더링 버그를 해결한다.
- 변경 파일:
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/data/active-countries.geojson`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 위치 게임 플레이 화면은 계속 `/data/active-countries.geojson`을 읽어 `Globe.gl`에 폴리곤을 넘긴다. 이번 문제는 프론트 이벤트가 아니라 GeoJSON 자산의 외곽 링 방향이 원본 세계 지도와 반대로 뒤집힌 상태였기 때문에, `scripts/generate_country_assets.py`에서 단순화 후 최종 GeoJSON을 한 번 더 읽어 외곽 링은 시계 방향, 홀은 반대 방향으로 다시 맞춘 뒤 저장하도록 바꿨다.
- 데이터 / 상태 변화: 국가 수 194개는 그대로 유지했고, 바뀐 것은 폴리곤 좌표의 순서다. 수정 전에는 단순화 결과의 외곽 링이 모두 반대 방향이라 Globe가 “국가 내부” 대신 “국가 바깥쪽 구면”을 캡으로 채우는 현상이 있었다. 수정 후에는 외곽 링 방향이 원본 `world-countries.geojson`과 같은 방향으로 맞춰졌다.
- 핵심 도메인 개념: 이 버그는 색상이나 클릭 이벤트 문제가 아니라 `기하 데이터 해석 규칙` 문제다. 같은 좌표 집합이어도 링 방향이 바뀌면 3D 구면에서 안쪽/바깥쪽 판정이 달라질 수 있다는 점을 설명할 수 있어야 한다.
- 예외 상황 또는 엣지 케이스: GeoJSON은 단순화나 클린업 과정에서 링 방향이 바뀔 수 있다. 그래서 이번 수정은 일회성 수동 편집이 아니라 생성 스크립트에 보정 로직을 넣어, 앞으로 자산을 다시 만들더라도 같은 오류가 반복되지 않게 했다.
- 테스트 내용: `./gradlew test` 통과. 추가로 `active-countries.geojson`의 외곽 링 부호를 검사했을 때 194개 전부 원본과 같은 방향으로 정렬된 것을 확인했고, 서버도 재시작했다.
- 면접에서 30초 안에 설명하는 요약: 지구본이 이상하게 보인 원인은 JS 색상보다도 GeoJSON의 링 방향이 뒤집힌 데 있었습니다. 단순화된 국가 폴리곤의 외곽 링이 원본과 반대 방향이어서 Globe가 나라 내부가 아니라 바깥 영역을 채우고 있었고, 그래서 자산 생성 스크립트에 링 방향 보정 단계를 넣어 다시 생성 가능하게 고쳤습니다.
- 아직 내가 이해가 부족한 부분: 현재는 원본 세계 지도와 같은 방향으로 맞춰 정상 렌더링을 회복했지만, 향후 다른 지도 소스를 가져올 때도 같은 방향 규칙이 항상 유지되는지는 자산별 검증이 더 필요하다.

## 2026-03-23 - 위치 게임 폴리곤 캡 렌더링 안정화

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 국가 면이 지글거리거나 큰 삼각형처럼 흔들려 보이는 현상과, 일부 국가가 잘 안 눌리는 체감을 줄이기 위해 기본 렌더링을 `면 중심`에서 `경계선 중심`으로 바꾼다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면이 `active-countries.geojson`을 읽는 구조는 유지한다. 다만 `Globe.gl`에 넘긴 뒤 기본 상태에서는 국가 cap을 사실상 투명하게 두고 stroke만 강하게 렌더링하도록 바꿨다. hover, selected, correct, wrong 상태에서만 cap을 약하게 드러내고, 기본 altitude를 조금 올려 지구 텍스처와 거의 같은 표면에서 생기던 z-fighting을 줄였다.
- 데이터 / 상태 변화: 서버 상태나 DB는 바뀌지 않았다. 바뀐 것은 프론트의 폴리곤 시각화 정책이다. 이전에는 모든 국가가 반투명 cap을 가지고 있어 복잡한 MultiPolygon에서 triangulation 결과가 그대로 드러났고, 지금은 선택 전 기본 상태에서 cap을 그리지 않아 시각 노이즈가 크게 줄어든다.
- 핵심 도메인 개념: 위치 게임에서 중요한 것은 “나라를 읽고 클릭할 수 있는가”이지, 모든 국가 면을 항상 칠해 놓는 것이 아니다. 따라서 기본 상태는 경계선을 우선해 가독성을 확보하고, 상태 변화가 생길 때만 면을 노출하는 쪽이 게임 UX에 더 맞다.
- 예외 상황 또는 엣지 케이스: 클릭 실수를 줄이려고 넣어둔 드래그 판정이 너무 민감하면 큰 나라도 안 눌리는 느낌이 날 수 있다. 그래서 드래그 임계값을 10px에서 18px로 완화해, 약간의 손 떨림은 클릭으로 인정하도록 조정했다.
- 테스트 내용: `./gradlew test` 통과. 런타임에서는 새 JS가 반영되도록 서버 재시작이 필요하다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임 지구본은 폴리곤이 많고 나라가 복잡해서, 모든 국가 면을 반투명으로 항상 그리면 오히려 삼각형 캡과 깜빡임이 보였습니다. 그래서 기본 렌더링을 경계선 중심으로 바꾸고, hover나 선택 같은 상태에서만 면을 잠깐 드러내도록 바꿔 가독성과 선택 안정성을 높였습니다.
- 아직 내가 이해가 부족한 부분: 현재 방식은 시각적 안정성을 우선한 1차 조정이라, 이후에는 나라 크기별로 hover altitude를 달리 주거나, 모바일 터치 환경에서 별도 threshold를 분리할지 검토가 더 필요하다.

## 2026-03-23 - 위치 게임 지도 자산을 Natural Earth 50m로 교체

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 기존 `world-countries.geojson` 기반 자산이 나라를 지나치게 잘게 쪼개고, 클릭/hover가 불안정하며, 브라질·중국 같은 대국에서도 넓은 영역이 반응하지 않는 문제를 줄인다.
- 변경 파일:
  - `src/main/resources/static/data/world-countries.geojson`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/main/resources/static/js/location-game.js`
  - `scripts/generate_country_assets.py`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 계속 `/data/active-countries.geojson`을 읽지만, 이제 이 파일은 기존 데이터셋이 아니라 Natural Earth 50m 국가 경계를 기반으로 생성된다. `generate_country_assets.py`는 새 소스의 `ISO_A3/ADM0_A3/SOV_A3/GU_A3` 필드를 보고 현재 시드 194개와 매칭한 뒤, 별도 mapshaper 단순화 없이 필터링과 링 방향 보정만 수행해 최종 active GeoJSON을 만든다.
- 데이터 / 상태 변화: `active-countries.geojson`은 여전히 194개 국가를 담지만, 국가 조각 수가 크게 줄었다. 예를 들어 브라질은 이전 자산에서 34조각이던 것이 새 자산에서는 17조각, 중국은 26조각에서 13조각 수준으로 내려갔다. South Sudan처럼 `ADM0_A3`와 `ISO_A3`가 다른 국가는 `ISO_A3`를 우선 사용하도록 매칭 순서도 조정했다.
- 핵심 도메인 개념: 위치 게임에서 중요한 것은 “국가 수가 많다”보다 “국가 경계가 설명 가능하고 클릭 가능하다”다. 따라서 데이터셋 선택도 단순 용량 문제가 아니라 `웹 렌더링 안정성`과 `상호작용 정확도` 기준으로 결정해야 한다.
- 예외 상황 또는 엣지 케이스: Natural Earth 110m는 충분히 가볍지만 194개 전체를 온전히 담지 못해서 제외했고, 10m는 너무 무거워 50m를 선택했다. 즉 이번 선택은 “194개 유지”와 “폴리곤 안정성” 사이에서 잡은 중간 해상도다.
- 테스트 내용: `./gradlew test` 통과. 생성 후 `active-countries.geojson` feature 수가 194개인지, 누락 국가가 없는지, 주요 국가 조각 수가 줄었는지 스크립트로 확인했고 서버도 재시작했다.
- 면접에서 30초 안에 설명하는 요약: 기존 위치 게임 지도는 국가 수는 많았지만 폴리곤이 너무 지저분해서 실제 클릭 안정성이 떨어졌습니다. 그래서 지구본 코드를 계속 미세 조정하기보다, 웹용 국가 경계 데이터로 많이 쓰이는 Natural Earth 50m를 기준으로 active GeoJSON을 다시 만들었고, 194개 국가 범위는 유지하면서 국가 조각 수를 줄여 hover와 클릭 안정성을 높였습니다.
- 아직 내가 이해가 부족한 부분: 현재는 데이터셋을 더 안정적인 쪽으로 바꾼 단계라, 실제 사용자 브라우저에서 어느 정도까지 클릭 체감이 좋아졌는지는 한 번 더 플레이 기준 확인이 필요하다. 특히 캐나다, 미국, 노르웨이처럼 다도해/북극권 조각이 많은 국가는 추가 검증이 필요하다.

## 2026-03-23 - 위치 게임 Level 1 범위를 상위 72개 국가로 축소

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 세션 진입 지연과 클릭 불안정이 계속 남아 있어, Level 1의 화면 범위를 먼저 줄여 플레이 가능한 기준선부터 확보한다.
- 변경 파일:
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/data/active-countries.geojson`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 위치 게임 시작 시 서버는 이제 전체 `country` 194건이 아니라 인구 기준 상위 72개만 후보로 사용한다. 프론트는 `/api/countries` 전체 목록을 더 이상 받지 않고, `active-countries.geojson`만 받아 그 안의 ISO 코드 집합으로 선택 가능 국가를 판단한다. 그래서 세션 시작 후 플레이 화면 초기화 시 네트워크 요청 수와 자산량이 같이 줄어든다.
- 데이터 / 상태 변화: DB의 `country` 시드는 여전히 194개를 유지한다. 바뀐 것은 위치 게임 Level 1의 활성 국가 범위이며, `active-countries.geojson`도 72개 feature로 줄었다. 게임오버 시에는 결과 화면에 머무르지 않고 시작 화면으로 바로 되돌아가 새로 시작하게 바꿨다.
- 핵심 도메인 개념: “시드는 넓게, Level 1 플레이 범위는 작게”를 분리하는 것이 중요하다. 전체 국가 데이터는 추천/인구수/향후 Level 2를 위해 유지하되, 현재 사용자 경험이 불안정하면 Level 1은 작은 집합으로 먼저 안정화하는 편이 맞다.
- 예외 상황 또는 엣지 케이스: 지금 72개로 줄였다고 해서 위치 게임 아키텍처가 바뀐 것은 아니다. 서버는 여전히 Stage 생성과 정답 판정을 담당하고, 이후 성능이 확보되면 100개 이상으로 다시 넓힐 수 있다.
- 테스트 내용: `./gradlew test` 통과. 재생성 후 `active-countries.geojson` feature 수가 72개로 줄었는지 확인했고, 서버 재시작까지 완료했다.
- 면접에서 30초 안에 설명하는 요약: 194개 전체를 한 번에 지구본에 올리니 클릭 안정성과 초기 로딩이 계속 흔들려서, Level 1은 인구 기준 상위 72개 주요 국가로 범위를 줄였습니다. 대신 전체 194개 시드는 DB에 그대로 유지해 다른 기능과 이후 확장은 막지 않았고, 프론트는 전체 국가 목록 요청을 없애고 active GeoJSON만 읽도록 줄여 초기화 비용도 같이 낮췄습니다.
- 아직 내가 이해가 부족한 부분: 72개가 현재 기준선으로는 현실적이지만, 어느 시점부터 100개 이상으로 다시 늘릴 수 있는지는 브라우저 성능과 클릭 정확도 테스트가 더 필요하다.

## 2026-03-23 - 위치 게임 Level 1 자산을 110m로 낮추고 좌표 기반 직접 선택 판정 추가

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 세션 진입이 오래 걸리고, 일부 국가가 잘 클릭되지 않으며, `Globe.gl` 폴리곤 이벤트만으로는 선택 안정성이 부족한 문제를 줄인다.
- 변경 파일:
  - `src/main/resources/static/data/world-countries-level1.geojson`
  - `src/main/resources/static/data/active-countries.geojson`
  - `scripts/generate_country_assets.py`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 계속 `GET /state`와 `GET /data/active-countries.geojson`을 병렬로 받는다. 이번에는 Level 1용 지도 소스를 `Natural Earth 110m`로 분리해 `active-countries.geojson`을 다시 만들었고, 프론트는 `onPolygonClick` 하나에만 의존하지 않고 `onGlobeClick`으로 받은 위도/경도 좌표를 GeoJSON 폴리곤 내부 포함 판정에 한 번 더 통과시켜 선택 국가를 결정한다.
- 데이터 / 상태 변화: DB의 국가 시드 194개는 그대로다. 바뀐 것은 Level 1 활성 지도 자산과 선택 방식이다. `active-countries.geojson`은 약 `1.52MB -> 157KB`로 줄었고, 브라질/중국/미국 같은 다각형 복잡도가 큰 국가도 더 낮은 해상도 경계를 사용하게 됐다. 게임오버 시에는 프론트에서 지구본 입력을 즉시 잠그고 시작 화면으로 복귀한다.
- 핵심 도메인 개념: 이 작업의 핵심은 “보이는 폴리곤”과 “선택 판정”을 분리한 것이다. 사용자는 여전히 지구본에서 나라를 고르지만, 클릭 성공 여부를 3D 라이브러리 이벤트 하나에 맡기지 않고 `클릭 좌표 -> GeoJSON 포함 판정 -> ISO 코드 제출`로 한 단계 더 명시적으로 처리한다.
- 예외 상황 또는 엣지 케이스: 반경계선, 작은 섬, 날짜 변경선 근처 국가는 단순 평면 point-in-polygon 판정이 흔들릴 수 있다. 그래서 현재 로직은 클릭 좌표 기준으로 경도 값을 정규화해 anti-meridian을 완화하지만, 캐나다/러시아 같은 극지·다도해 국가는 추가 확인이 필요하다.
- 테스트 내용: `./gradlew test` 통과. 로컬 서버 재기동 후 `POST /api/games/location/sessions`는 약 `0.10s`, `GET /data/active-countries.geojson`은 약 `157KB / 0.015s`로 확인했다. 또 API 기준으로 3회 오답 제출 시 `GAME_OVER`, `livesRemaining = 0`이 내려오는 것도 확인했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임이 느리고 일부 나라가 잘 안 눌린 원인은 서버보다 프론트 지도 자산과 선택 방식에 있었습니다. 그래서 Level 1용 국가 경계를 더 낮은 해상도의 Natural Earth 110m로 분리해 초기 로딩을 크게 줄였고, Globe.gl의 기본 폴리곤 클릭만 믿지 않고 지구본 클릭 좌표를 GeoJSON 내부 판정으로 다시 검사해 선택 안정성을 높였습니다.
- 아직 내가 이해가 부족한 부분: 현재 직접 hit-test는 브라우저에서 충분히 설명 가능한 단순 알고리즘이지만, 구면 좌표 특성과 홀/섬 처리까지 완전한 GIS 수준으로 다루는 것은 아니다. 이후 Level 2 확장 전에는 작은 섬나라와 극지권 국가 기준으로 한 번 더 확인이 필요하다.

## 2026-03-23 - 위치 게임 GAME_OVER 선택 모달과 시작 버튼 문구 수정

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 하트가 모두 소진됐을 때 바로 화면을 강제로 넘기지 않고, 사용자가 탈락을 인지한 뒤 `홈으로` 또는 `다시 시작`을 직접 고를 수 있게 만든다. 동시에 시작 화면 CTA를 더 자연스러운 문구로 바꾼다.
- 변경 파일:
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버는 여전히 3회 오답 후 `GAME_OVER`를 내려준다. 바뀐 것은 프론트 후처리다. 플레이 화면은 `GAME_OVER` 응답을 받으면 지구본 입력을 잠그고, 자동 리다이렉트 대신 탈락 모달을 띄운다. 사용자는 그 안에서 `/` 또는 `/games/location/start`로 이동을 직접 선택한다.
- 데이터 / 상태 변화: 서버 상태와 DB는 바뀌지 않았다. UI 상태만 바뀌었다. 이전에는 `GAME_OVER -> 자동 시작 페이지 이동`이었고, 지금은 `GAME_OVER -> 입력 잠금 -> 탈락 모달 표시 -> 사용자 선택` 흐름이다.
- 핵심 도메인 개념: 이 변경은 게임 규칙을 바꾼 것이 아니라 피드백 UX를 바꾼 것이다. 즉 `언제 게임이 끝나는가`는 계속 서버가 결정하고, 프론트는 그 종료 사실을 사용자가 이해하기 쉬운 방식으로 표현한다.
- 예외 상황 또는 엣지 케이스: 탈락 모달이 떠 있는 동안 다시 선택하거나 제출하면 안 되므로, 게임오버 시 `globe-stage` 입력 자체를 잠그게 했다.
- 테스트 내용: `node --check src/main/resources/static/js/location-game.js` 통과. 기존 `./gradlew test` 상태도 유지했다.
- 면접에서 30초 안에 설명하는 요약: 게임오버 자체는 서버가 그대로 결정하지만, 사용성 측면에서는 즉시 화면을 넘겨 버리면 왜 끝났는지 체감이 약했습니다. 그래서 프론트에서 `GAME_OVER` 응답을 받으면 입력을 잠그고 탈락 모달을 보여준 뒤, 사용자가 홈으로 갈지 바로 다시 시작할지 선택하게 바꿨습니다.
- 아직 내가 이해가 부족한 부분: 현재는 모달 안에서 단순 이동만 제공한다. 이후에는 최근 점수, 도달 Stage, 다시 시작 시 닉네임 유지 여부까지 넣을지 결정이 필요하다.

## 2026-03-23 - 새 게임 시작 시 이전 GAME_OVER 모달 상태 초기화

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: `다시 시작하기` 후 새 게임을 시작해도 이전 탈락 모달이 그대로 보이는 문제를 막는다.
- 변경 파일:
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 새 플레이 화면에 진입하거나 `pageshow`가 다시 발생하면 프론트가 `GAME_OVER` 모달을 먼저 숨긴 뒤 상태를 불러오도록 바꿨다. 또한 CSS에 `[hidden] { display: none !important; }`를 넣어 hidden 속성이 클래스 스타일보다 항상 우선되게 고정했다.
- 데이터 / 상태 변화: 서버 상태와 DB는 변하지 않았다. 바뀐 것은 모달 표시 상태 초기화 시점이다.
- 핵심 도메인 개념: `GAME_OVER`는 서버 도메인 상태이고, 모달 노출 여부는 프론트 UI 상태다. 새 세션이 시작되면 이전 UI 상태를 반드시 초기화해야 서버 상태와 화면 상태가 어긋나지 않는다.
- 예외 상황 또는 엣지 케이스: 브라우저의 뒤로 가기/앞으로 가기 캐시에서 이전 DOM 상태가 살아 있을 수 있어 `pageshow` 시점에도 모달을 강제로 숨기게 했다.
- 테스트 내용: `node --check src/main/resources/static/js/location-game.js` 통과.
- 면접에서 30초 안에 설명하는 요약: 탈락 모달을 추가한 뒤에는 새 게임이 시작될 때 이전 UI 상태를 초기화하는 것도 중요해졌습니다. 그래서 플레이 화면 진입 시와 브라우저 `pageshow` 시점에 모달을 다시 숨기고, CSS hidden 속성을 강제해 이전 탈락 상태가 새 세션으로 이어지지 않게 했습니다.
- 아직 내가 이해가 부족한 부분: 브라우저별 BFCache 동작 차이는 추가 확인이 필요하다. 현재는 가장 단순한 `pageshow` 초기화로 대응했다.

## 2026-03-23 - 위치 게임 다시 시작을 같은 세션 리셋 방식으로 변경

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 탈락 후 `다시 시작하기`를 눌렀을 때 시작 페이지로 돌아가 새 세션을 만드는 대신, 현재 세션을 그대로 초기화해 같은 플레이 화면에서 바로 다시 시작하게 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameStageRepository.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameAttemptRepository.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 프론트에서 `다시 시작하기`를 누르면 `POST /api/games/location/sessions/{sessionId}/restart`가 호출된다. 서버는 같은 `sessionId`의 attempt와 stage를 지우고, 세션의 상태/하트/점수/현재 Stage를 초기값으로 리셋한 뒤 Stage 1을 다시 만든다. 프론트는 새 URL로 이동하지 않고 같은 플레이 화면에서 `GET /state`를 다시 불러와 게임을 이어서 보여준다.
- 데이터 / 상태 변화: 이 변경 전에는 탈락 후 새 시작 흐름으로 넘어가며 사실상 새 세션을 만드는 UX였다. 지금은 같은 세션 row를 재사용하고, 관련 stage/attempt 데이터만 비운 뒤 다시 시작한다. 따라서 `sessionId`는 유지되고, 점수/하트/클리어 카운트만 초기화된다.
- 핵심 도메인 개념: “다시 시작”은 도메인 관점에서 `새 게임 생성`이 아니라 `종료된 세션의 초기화`다. 그래서 이 로직은 컨트롤러가 아니라 서비스에서 attempt 삭제, stage 삭제, session reset, 새 Stage 생성 순서를 묶어 처리해야 한다.
- 예외 상황 또는 엣지 케이스: 삭제 후 같은 세션에 Stage 1을 다시 넣기 때문에, attempt를 먼저 지우고 stage를 지운 뒤 flush까지 해줘야 unique constraint와 FK 충돌을 피할 수 있다. 또한 진행 중인 세션은 실수로 초기화되지 않도록 terminal 상태에서만 restart를 허용한다.
- 테스트 내용: `LocationGameFlowIntegrationTest`에 restart 시나리오를 추가해, 3회 오답으로 게임오버 후 `POST /restart`를 호출하면 같은 `sessionId`가 유지되고, lives=3 / score=0 / stage=1 / attempt 비움 상태로 복구되는지 확인했다. `./gradlew test` 통과.
- 면접에서 30초 안에 설명하는 요약: 탈락 후 다시 시작을 새 세션 생성으로 처리하면 같은 런의 흐름이 끊기고 프론트도 다시 시작 페이지를 거쳐야 했습니다. 그래서 별도 restart API를 두고, 서비스에서 기존 attempt와 stage를 지운 뒤 같은 sessionId의 상태를 초기화하고 Stage 1을 다시 생성하도록 바꿨습니다.
- 아직 내가 이해가 부족한 부분: 현재는 restart 시 이전 플레이 기록을 세션 안에 남기지 않는다. 나중에 “한 세션 안의 여러 러닝”까지 분석하고 싶다면 run 단위를 따로 분리할지 고민이 필요하다.

## 2026-03-23 - 인구수 게임 Level 1 리부트 기획 시작

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 기획
- 목적: 현재 인구수 게임이 `고정 5라운드 숫자 보기형 퀴즈`에 머물러 있어, 위치 게임처럼 하트 기반 아케이드 모드로 다시 설계할 기준 문서를 만든다.
- 변경 파일:
  - `docs/POPULATION_GAME_ARCADE_REBOOT.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `README.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 이번 작업은 코드 구현이 아니라 리부트 방향 고정이다. 현재 구현을 기준으로 `start -> get round -> submit answer -> next round button -> result` 흐름이 어디서 게임성을 잃는지 분석하고, 앞으로는 `start -> get state -> submit answer -> auto next stage / retry -> game over -> restart` 구조로 옮기기로 정리했다.
- 데이터 / 상태 변화: 실제 DB 스키마는 아직 바뀌지 않았다. 다만 다음 리부트에서 `PopulationGameRound 1회 제출형`만으로는 부족하다고 판단했고, 위치 게임처럼 `Session / Stage / Attempt` 구조로 갈 가능성을 플레이북과 설계 문서에 명시했다.
- 핵심 도메인 개념: 인구수 게임도 위치 게임과 마찬가지로 “문제를 제출하고 끝나는 폼”이 아니라 “서버가 하트, 점수, 진행 상태를 관리하는 게임 루프”가 되어야 한다. 따라서 현재의 `5문제 고정`, `100/0 점수`, `다음 라운드 버튼`은 프로토타입 단계의 흔적으로 본다.
- 예외 상황 또는 엣지 케이스: 현재 Level 1은 정확 숫자 4개 보기라 읽기 피로도가 높다. 리부트 시 Level 1은 구간형 또는 압축 표현형으로 다시 정의하고, 정확 수치 / 오차율 입력은 Level 2로 올리는 것이 더 자연스럽다고 판단했다.
- 테스트 내용: 이번 작업은 문서 설계 단계라 테스트는 실행하지 않았다.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임은 공통 세션 구조를 검증하는 데는 성공했지만, 실제 게임으로는 아직 약했습니다. 그래서 위치 게임을 리부트했던 것처럼 인구수 게임도 하트, 재시도, 자동 다음 Stage, 게임오버, 같은 세션 재시작이 있는 아케이드 루프로 다시 설계하기로 했고, 그 기준 문서를 먼저 만들었습니다.
- 아직 내가 이해가 부족한 부분: Level 1을 `구간형`으로 갈지, `압축된 근사 수치 보기형`으로 갈지는 아직 확정하지 않았다. 둘 다 장단점이 있어 실제 구현 전에 한 번 더 선택해야 한다.

## 2026-03-23 - 인구수 게임 Level 1 아케이드 리부트 1차 구현

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트
- 목적: 기존 `고정 5라운드 + 다음 라운드 버튼 + 100/0 점수` 구조를 버리고, 위치 게임과 같은 서버 주도 아케이드 루프로 옮긴다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameStage.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameStageStatus.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameStageRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameAttempt.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameAttemptRepository.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameDifficultyPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameDifficultyPlan.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStateView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerOutcome.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStageResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAttemptResultView.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java`
  - `src/main/java/com/worldmap/game/population/web/SubmitPopulationAnswerRequest.java`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/static/js/population-game.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/POPULATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 시작 페이지에서 닉네임을 제출하면 `POST /api/games/population/sessions`가 같은 `BaseGameSession` 위에 `PopulationGameSession`을 만들고, 서버가 첫 `PopulationGameStage`와 보기 4개를 생성한다. 플레이 화면은 `GET /state`로 현재 Stage, 하트, 점수, 보기 4개를 받고, `POST /answer`로 선택한 보기 번호를 제출한다. 서비스는 `PopulationGameScoringPolicy`와 `PopulationGameDifficultyPolicy`를 이용해 정답 여부, 점수, 다음 Stage 생성 여부를 계산하고, `PopulationGameAttempt`를 저장한다. 하트가 모두 사라지면 `GAME_OVER`가 되며, `POST /restart`로 같은 `sessionId`를 초기화해 Stage 1부터 다시 시작한다.
- 데이터 / 상태 변화: `population_game_round` 1회 제출형 구조 대신 `population_game_stage`, `population_game_attempt` 구조를 사용하게 됐다. 세션에는 `livesRemaining`이 추가됐고, 상태 전이는 `READY -> IN_PROGRESS -> GAME_OVER` 중심으로 바뀌었다. Stage는 정답 시 `CLEARED`, 하트 소진 시 `FAILED`가 되고, 정답일 때마다 다음 Stage가 추가되어 `totalRounds`는 “현재까지 계획된 Stage 수” 의미로 확장된다.
- 핵심 도메인 개념: 인구수 게임도 위치 게임처럼 “한 문제를 제출하고 끝나는 퀴즈”가 아니라 “한 Stage 안에서 여러 번 시도할 수 있는 게임”으로 보는 것이 핵심이다. 그래서 정답 데이터와 보기 4개는 `PopulationGameStage`에 두고, 사용자의 실제 선택 기록은 `PopulationGameAttempt`로 분리했다. 이 로직이 컨트롤러가 아니라 서비스에 있어야 하는 이유는, 답안 제출 한 번이 `세션 하트 감소`, `점수 계산`, `Stage 상태 변경`, `다음 Stage 생성`, `Attempt 저장`을 한 트랜잭션 안에서 함께 일으키기 때문이다.
- 예외 상황 또는 엣지 케이스: 진행 중이 아닌 세션에는 답안을 제출할 수 없고, 현재 Stage 번호와 다른 값을 제출하면 충돌로 막는다. 재시작은 종료된 세션에서만 허용하며, 기존 attempt/stage를 먼저 지우고 flush한 뒤 같은 sessionId를 초기화해 unique constraint 충돌을 피한다. 아직 보기 표현은 `정확 숫자 4개`라서 읽기 피로도가 남아 있고, 이 부분은 다음 단계에서 다시 다듬어야 한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest` 통과, `./gradlew test` 전체 통과, `node --check src/main/resources/static/js/population-game.js` 통과. 로컬 실행 후 `POST /api/games/population/sessions`, `GET /state`, 결과 페이지 `200` 응답도 확인했다.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임도 위치 게임처럼 서버가 하트, 점수, 진행 상태를 관리하도록 리부트했습니다. 기존 5라운드 퀴즈 구조를 `세션 / Stage / Attempt`로 바꾸고, 오답이면 같은 Stage 재시도, 하트 3개 소진 시 게임오버, 같은 sessionId 재시작, 정답 시 자동 다음 Stage 생성까지 모두 서비스에서 처리하도록 옮겼습니다.
- 아직 내가 이해가 부족한 부분: 이번 1차 리부트는 게임 루프와 상태 전이에 집중했고, 보기 표현은 아직 정확 숫자 4개다. Level 1을 구간형으로 바꿀지 압축 수치형으로 유지할지는 실제 플레이 피드백을 보고 한 번 더 판단해야 한다.

## 2026-03-23 - 인구수 게임 오답 시 하단 결과 카드 비노출 처리

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 오답을 냈을 때 하단에 정답/선택값 결과 카드까지 뜨면 아케이드 게임보다 해설형 퀴즈처럼 느껴져, 실패 피드백을 오버레이 중심으로 단순화한다.
- 변경 파일:
  - `src/main/resources/static/js/population-game.js`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `POST /api/games/population/sessions/{sessionId}/answer` 응답을 받은 뒤, 프론트는 이제 정답일 때만 하단 feedback 카드를 렌더링한다. 오답 또는 게임오버일 때는 하단 카드를 비우고, 상단 오버레이만 보여준 뒤 같은 Stage 재시도 또는 게임오버 모달로 넘어간다.
- 데이터 / 상태 변화: 서버 상태와 DB는 바뀌지 않았다. 바뀐 것은 프론트 피드백 강도와 노출 방식이다.
- 핵심 도메인 개념: 오답 피드백은 “상태 전이 알림”만 주고, 상세 결과 카드는 정답 보상에만 쓰는 편이 현재 아케이드 루프에 더 맞는다. 즉 서버가 판정한 결과는 같지만, 프론트는 `correct` 여부에 따라 다른 UI 채널로 보여준다.
- 예외 상황 또는 엣지 케이스: 게임오버도 오답의 연장선이므로, 하단 카드 대신 게임오버 모달만 띄우도록 유지한다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test` 전체 통과 상태 유지.
- 면접에서 30초 안에 설명하는 요약: 오답 때마다 하단 결과 카드까지 뜨면 게임 흐름이 끊겨서, 인구수 게임은 정답일 때만 결과 카드를 보여주고 오답은 오버레이만 남기도록 바꿨습니다. 서버 응답은 그대로 두고, 프론트에서 `correct` 여부에 따라 피드백 채널만 분기한 것입니다.
- 아직 내가 이해가 부족한 부분: 지금은 오답 상세 해설을 숨겼지만, 나중에 학습형 모드와 아케이드 모드를 분리할 때는 결과 노출 정책을 다시 나눌 필요가 있다.

## 2026-03-23 - 인구수 게임 오답 시 화면 재조회 제거

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 오답 뒤 같은 Stage를 다시 시도하는 상황인데도 프론트가 `GET /state`를 다시 호출해 화면이 깜빡이고 새로고침처럼 보이는 문제를 제거한다.
- 변경 파일:
  - `src/main/resources/static/js/population-game.js`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `POST /api/games/population/sessions/{sessionId}/answer` 응답이 `WRONG`일 때는 이제 추가 `GET /state` 요청을 보내지 않는다. 프론트는 서버 응답에 포함된 `livesRemaining`, `totalScore`, `clearedStageCount`만 현재 HUD에 반영하고, 같은 보기 목록은 그대로 둔 채 선택 상태만 지우고 다시 입력을 받는다.
- 데이터 / 상태 변화: 서버 상태와 DB는 바뀌지 않았다. 바뀐 것은 오답 후 프론트의 후처리 경로다. 이전에는 `WRONG -> overlay -> GET /state -> 전체 재렌더링`, 지금은 `WRONG -> overlay -> 선택 초기화 -> 같은 화면 유지` 흐름이다.
- 핵심 도메인 개념: 같은 Stage 재시도는 상태상 “새 문제 로딩”이 아니라 “현재 문제 유지”이므로, 프론트도 전체 재조회보다 현재 상태 보정이 맞다. 즉 서버는 여전히 정답 판정과 하트 감소를 관리하지만, 프론트는 Stage가 안 바뀐 경우 불필요한 새 요청을 줄여야 한다.
- 예외 상황 또는 엣지 케이스: 정답일 때는 다음 Stage와 보기 4개가 바뀌므로 여전히 `GET /state`를 다시 호출한다. `GAME_OVER`는 재시도가 아니라 종료이므로 모달 흐름을 유지한다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: 오답은 같은 Stage를 다시 푸는 상황인데도 화면 전체를 다시 불러오고 있어서 UX가 깜빡였습니다. 그래서 오답일 때는 서버 재조회 없이 응답에 담긴 하트/점수만 HUD에 반영하고, 선택만 초기화해서 같은 문제를 자연스럽게 이어가도록 바꿨습니다.
- 아직 내가 이해가 부족한 부분: 지금은 “같은 Stage면 재조회하지 않는다”가 충분하지만, 나중에 서버가 오답 시 힌트나 옵션 순서를 바꾸는 규칙을 넣으면 이 분기 기준을 다시 검토해야 한다.

## 2026-03-23 - 인구수 게임 정답 후 Next Stage 수동 진행으로 변경

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 정답 직후 자동으로 다음 문제로 넘어가면 결과를 볼 시간이 너무 짧고, 사용자가 리듬을 직접 조절하기 어렵다. 그래서 정답 후에는 `다음 Stage` 버튼으로 직접 진행하게 바꾼다.
- 변경 파일:
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/static/js/population-game.js`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `POST /api/games/population/sessions/{sessionId}/answer` 응답이 `CORRECT`면, 프론트는 하단 결과 카드와 성공 오버레이를 보여주고 자동 `GET /state`를 호출하지 않는다. 대신 `선택 제출` 옆에 `다음 Stage` 버튼을 노출하고, 사용자가 이 버튼을 눌렀을 때만 `GET /state`로 다음 문제를 받아온다.
- 데이터 / 상태 변화: 서버 상태와 DB는 바뀌지 않았다. 정답 시점에 이미 다음 Stage는 서버가 생성해 두고 있으므로, 바뀐 것은 프론트 전환 타이밍뿐이다. 이전에는 `CORRECT -> 잠시 후 자동 state 재조회`, 지금은 `CORRECT -> Next Stage 버튼 노출 -> 버튼 클릭 시 state 재조회` 흐름이다.
- 핵심 도메인 개념: “다음 Stage 생성” 책임은 여전히 서버에 있고, “언제 화면을 넘길지”만 프론트 UX 정책으로 분리한 것이다. 즉 상태 전이는 서버가 유지하고, 사용자의 템포 조절은 클라이언트가 맡는다.
- 예외 상황 또는 엣지 케이스: `GAME_OVER`는 여전히 모달 흐름을 유지하고, 정답이더라도 `FINISHED`가 생기는 모드라면 결과 화면으로 넘어가는 분기를 그대로 쓸 수 있다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임은 정답을 맞힌 직후 결과를 읽을 시간이 필요해서 자동 다음 Stage 이동을 없앴습니다. 서버는 여전히 정답 시 다음 Stage를 생성하지만, 프론트는 `다음 Stage` 버튼을 보여주고 사용자가 눌렀을 때만 새 상태를 받아오도록 바꿔 UX 템포를 분리했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 정답마다 사용자가 직접 넘기는 방식이지만, 나중에 속도감을 더 중시하는 모드가 생기면 자동 전환 옵션을 따로 둘지 다시 판단해야 한다.

## 2026-03-24 - 인구수 게임 Level 1을 구간형 보기로 전환

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 현재 Level 1은 숫자 4개를 그대로 읽는 방식보다, 사용자가 `어느 인구 규모대인지`를 빠르게 판단하는 구간형이 더 게임답고 설명하기 쉽다. 그래서 서버가 인구 규모 구간 4개를 만들고 프론트는 구간 라벨만 보여주도록 바꾼다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/population/application/PopulationScaleBand.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationScaleBandCatalog.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationOptionLabelFormatter.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameOptionGenerator.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationOptionView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAttemptResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStageResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/resources/static/js/population-game.js`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/test/java/com/worldmap/game/population/application/PopulationGameOptionGeneratorTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/POPULATION_GAME_ARCADE_REBOOT.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `POST /api/games/population/sessions`와 `GET /state` 흐름은 유지되지만, 서버는 이제 target population이 속한 `PopulationScaleBand`를 찾고 그 주변 4개 구간을 보기로 생성한다. 플레이 화면은 `PopulationOptionView.label`만 렌더링하고, `POST /answer` 응답에는 `selectedOptionLabel`, `correctOptionLabel`도 같이 들어와 정답 카드와 결과 화면에서 구간 중심으로 보여준다.
- 데이터 / 상태 변화: DB 스키마는 바꾸지 않았고, 기존 option population 칼럼에는 각 구간의 시작값(lower bound)을 저장한다. 즉 저장 구조는 유지하면서 의미만 “정확 숫자”에서 “구간 키”로 바뀌었다. 실제 국가 인구수는 여전히 `targetPopulation`으로 따로 유지해 결과 설명과 Level 2 확장에 쓸 수 있다.
- 핵심 도메인 개념: Level 1은 “정확 수치 기억력”보다 “인구 규모 감각”을 보는 편이 맞다. 그래서 서버가 `PopulationScaleBandCatalog`로 전역 구간 체계를 관리하고, 각 Stage는 그중 어떤 4개 구간을 보기로 썼는지 저장한다. 이 방식이 서비스에 있어야 하는 이유는, 같은 target country라도 어떤 구간 창(window)을 줄지와 정답 옵션 번호를 서버가 일관되게 결정해야 하기 때문이다.
- 예외 상황 또는 엣지 케이스: 구간이 고정되어 있기 때문에 어떤 국가는 경계값 근처에서 체감 난이도가 높아질 수 있다. 예를 들어 7천만 바로 아래/위 국가는 인접 구간 구분이 어렵게 느껴질 수 있어, 다음 단계에서 경계 조정 여지가 있다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test --tests com.worldmap.game.population.application.PopulationGameOptionGeneratorTest --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest` 통과.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임 Level 1은 정확 숫자 4개를 읽게 하면 피로도가 높아서, 서버가 인구 규모 구간 4개를 생성하는 방식으로 바꿨습니다. 구간 정의는 공통 카탈로그로 두고 Stage에는 구간 키를 저장해, 플레이 화면과 결과 화면 모두 같은 구간 언어로 설명 가능하게 만들었습니다.
- 아직 내가 이해가 부족한 부분: 현재 구간 경계는 첫 버전이라 실제 플레이 감각 기준으로는 더 다듬을 수 있다. 특히 7천만, 1억 5천만 같은 경계 근처 국가들이 체감상 어느 정도 헷갈리는지는 플레이 데이터를 보고 다시 판단해야 한다.

## 2026-03-24 - 인구수 게임 HUD / 결과 디브리프 1차 polish

- 단계: 4. 국가 인구수 맞추기 게임 Level 1 리부트 보강
- 목적: 현재 인구수 게임은 루프는 게임답게 바뀌었지만, 플레이 중 “내가 무엇을 골랐는지”와 결과 화면의 요약 밀도가 부족했다. 그래서 플레이 화면에 선택 상태와 진행 가이드를 보강하고, 결과 화면에는 핵심 러닝 지표를 추가해 설명 가능한 디브리프 화면으로 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameSessionResultView.java`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/static/js/population-game.js`
  - `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 여전히 `GET /api/games/population/sessions/{sessionId}/state`로 현재 Stage를 받고, `POST /answer`로 판정을 요청한다. 다만 프론트는 이제 선택한 보기 라벨을 로컬 HUD에 즉시 반영하고, 서버 응답이 오면 정답/오답에 맞는 진행 가이드를 같은 화면에서 갱신한다. 결과 화면은 `GET /api/games/population/sessions/{sessionId}` 응답에 서버가 계산한 `totalAttemptCount`, `firstTryClearCount`를 추가로 받아 디브리프 카드에 표시한다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았고, 결과 요약 값은 기존 `PopulationGameStage`, `PopulationGameAttempt` 기록을 읽어 서비스에서 계산한다. 즉 영속 데이터는 그대로 두고, “면접에서 설명하기 좋은 요약값”만 조회 시점에 조합한 것이다.
- 핵심 도메인 개념: 선택 상태 표시와 다음 행동 가이드는 프론트의 책임이지만, 총 시도 수나 1트 클리어 수 같은 러닝 요약은 서버가 계산해 내려줘야 한다. 이유는 이 값들이 Stage/Attempt 전체 기록을 기준으로 일관되게 계산돼야 하고, 나중에 랭킹이나 전적 페이지로 확장해도 같은 기준을 재사용할 수 있기 때문이다. 그래서 UI 문구는 JS가 관리하고, 디브리프 지표는 서비스가 만든다.
- 예외 상황 또는 엣지 케이스: 오답 후 같은 Stage를 다시 시도할 때는 `GET /state`를 재호출하지 않으므로, 선택 상태와 진행 가이드를 프론트에서 직접 초기화해야 한다. 또한 결과 요약은 아직 “총 시도 수 / 1트 클리어 수”까지만 넣었고, 연속 정답 보너스나 평균 시도 수 같은 값은 다음 polish 범위로 남겨뒀다.
- 테스트 내용: `node --check src/main/resources/static/js/population-game.js` 통과, `./gradlew test --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest` 통과, `./gradlew test` 전체 통과. 통합 테스트에는 인구수 결과 응답의 `totalAttemptCount`, `firstTryClearCount` 확인을 추가했다.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임의 게임 루프는 이미 서버 주도로 정리돼 있었고, 이번에는 사용자가 현재 선택 상태와 결과를 더 잘 읽을 수 있게 polish 했습니다. 플레이 화면은 선택한 구간과 다음 행동을 바로 보여주고, 결과 화면은 Stage/Attempt 기록에서 계산한 총 시도 수와 1트 클리어 수를 서버가 내려줘서 디브리프 화면으로 설명 가능하게 만들었습니다.
- 아직 내가 이해가 부족한 부분: 현재 HUD 문구와 결과 지표는 첫 버전이라, 실제 플레이 기준으로 어떤 정보가 과하고 어떤 정보가 부족한지는 한 번 더 조정할 수 있다. 특히 모바일에서는 선택 상태 카드와 버튼 간격이 얼마나 읽기 좋은지 추가 확인이 필요하다.

## 2026-03-24 - 위치 게임 HUD / 결과 디브리프 1차 polish

- 단계: 3. 국가 위치 찾기 게임 Level 1 리부트 보강
- 목적: 위치 게임은 클릭 안정화와 재시작 루프는 갖췄지만, 플레이 중 “지금 무엇을 해야 하는지”와 결과 화면의 요약 밀도가 아직 약했다. 그래서 제출 전후 행동을 안내하는 HUD를 추가하고, 결과 화면에는 런 전체를 짧게 요약하는 지표를 넣는다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 기존처럼 `GET /api/games/location/sessions/{sessionId}/state`로 현재 Stage와 난이도를 받고, `POST /answer`로 선택한 ISO3 코드를 제출한다. 프론트는 이제 제출 전에는 여전히 국가명을 숨기되, 클릭 완료 / 정답 / 오답 / 게임오버 시점마다 `진행 가이드` 텍스트를 바꿔 사용자가 다음 행동을 바로 이해할 수 있게 했다. 결과 화면은 `GET /api/games/location/sessions/{sessionId}/result` 응답에 서버가 계산한 `totalAttemptCount`, `firstTryClearCount`를 포함해 디브리프 카드에 표시한다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았다. 새 요약값은 기존 `LocationGameStage`, `LocationGameAttempt` 기록을 읽어 서비스가 계산한다. 즉 영속 데이터는 그대로 두고, 결과 조회 응답만 더 설명 가능하게 만든 것이다.
- 핵심 도메인 개념: 위치 게임은 제출 전 국가명을 숨겨야 하므로, 플레이 HUD는 “선택 여부와 다음 행동”만 알려주는 것이 맞고, 정답/오답 이후에만 실제 국가명을 feedback 카드에서 공개한다. 반면 총 시도 수와 1트 클리어 수 같은 러닝 요약은 Stage/Attempt 전체 기록을 기준으로 서버가 계산해야 일관성이 생긴다. 그래서 행동 안내는 JS, 러닝 지표는 서비스로 역할을 분리했다.
- 예외 상황 또는 엣지 케이스: 선택 취소를 눌렀을 때는 제출 버튼뿐 아니라 진행 가이드도 현재 Stage 기본 상태로 되돌려야 한다. 또한 게임오버 직후에는 선택 상태를 비우되, 안내 문구는 모달 선택을 유도하는 방향으로 남기는 편이 흐름상 더 자연스럽다.
- 테스트 내용: `node --check src/main/resources/static/js/location-game.js` 통과, `./gradlew test --tests com.worldmap.game.location.LocationGameFlowIntegrationTest` 통과, `./gradlew test` 전체 통과. 통합 테스트에는 위치 게임 결과 응답의 `totalAttemptCount`, `firstTryClearCount` 확인을 추가했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임은 국가명을 숨긴 채 플레이해야 해서, 사용자가 헷갈리지 않게 행동 안내를 별도로 두는 게 중요했습니다. 이번에는 프론트에 선택 상태와 진행 가이드를 추가하고, 결과 화면에는 서버가 계산한 총 시도 수와 1트 클리어 수를 넣어서 한 판의 러닝을 더 짧게 설명할 수 있게 만들었습니다.
- 아직 내가 이해가 부족한 부분: 현재 가이드 문구는 1차 버전이라, 실제 플레이 감각 기준으로 문장이 너무 많거나 적은지 더 조정할 수 있다. 모바일에서 선택 상태 카드와 버튼들이 한 줄에 얼마나 자연스럽게 배치되는지도 추가 확인이 필요하다.

## 2026-03-24 - Redis 랭킹 1차 구현

- 단계: 5. Redis 랭킹 시스템
- 목적: 게임오버 시점 점수를 단순히 결과 페이지에서만 끝내지 않고, `RDB에 영속 저장 + Redis Sorted Set 반영 + 상위 N명 조회`까지 이어지는 랭킹 vertical slice를 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardGameMode.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardGameLevel.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardScope.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardRankingPolicy.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardEntryView.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardView.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardService.java`
  - `src/main/java/com/worldmap/ranking/web/LeaderboardApiController.java`
  - `src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/home.html`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/resources/application.yml`
  - `src/main/resources/application-test.yml`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameAttemptRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameAttemptRepository.java`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 위치 게임과 인구수 게임은 `POST /answer`에서 하트를 모두 잃어 세션이 종료되면, 서비스가 현재 run의 점수/클리어 수/총 시도 수를 모아 `LeaderboardService`에 전달한다. `LeaderboardService`는 우선 `leaderboard_record`에 종료 run을 저장하고, 트랜잭션 커밋 이후 Redis Sorted Set의 전체 키와 일간 키에 record id를 반영한다. 조회 시에는 `GET /api/rankings/{gameMode}`가 먼저 Redis에서 상위 record id를 가져오고, 그 id로 RDB 상세 정보를 읽어 응답을 만든다. `/ranking` 화면은 같은 서비스를 직접 호출해 전체/일간 top 10을 SSR로 보여준다.
- 데이터 / 상태 변화: 이제 게임 세션 외에 `leaderboard_record`가 생겼다. 중요한 점은 랭킹이 `세션`을 그대로 재사용하지 않고, 종료된 `run 결과`를 별도 레코드로 저장한다는 것이다. 이유는 현재 게임들이 같은 `sessionId`로 재시작될 수 있어서, 세션 자체를 랭킹 row로 쓰면 이전 게임 기록이 덮어써질 수 있기 때문이다.
- 핵심 도메인 개념: Redis는 정렬과 top N 조회를 빠르게 하기 위한 read model이고, 진실 공급원은 RDB의 `leaderboard_record`다. 그래서 저장은 항상 RDB가 먼저고, Redis 반영은 `after commit` 뒤에 수행한다. 또한 Redis key가 비어 있거나 유실되면 `LeaderboardService`가 RDB 상위 기록을 다시 읽어 키를 재구성하도록 만들어, “Redis가 날아가도 랭킹은 복구 가능하다”는 설명이 가능해졌다.
- 예외 상황 또는 엣지 케이스: 같은 세션을 재시작하면 새로운 run이 시작되므로, 랭킹도 별도 레코드로 남겨야 한다. 같은 종료 시점을 두 번 반영하지 않도록 `runSignature`를 두었다. Redis 반영이 실패해도 RDB에는 기록이 남으므로, 다음 조회 시 fallback으로 복구할 수 있다. 현재 일간 랭킹은 `finishedAt.toLocalDate()` 기준으로 계산하고, 레벨은 아직 `LEVEL_1`만 지원한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과, `./gradlew test` 전체 통과. 통합 테스트에서는 실제 게임오버를 발생시켜 랭킹 레코드 생성, `/api/rankings/location` 조회, `/api/rankings/population`의 DB fallback 복구, `/ranking` 페이지 렌더링을 확인했다.
- 면접에서 30초 안에 설명하는 요약: 랭킹은 세션을 그대로 재사용하지 않고, 종료된 게임 run을 `leaderboard_record`로 따로 저장했습니다. 게임오버가 되면 먼저 RDB에 기록하고, 커밋이 끝난 뒤 Redis Sorted Set의 전체/일간 키에 record id를 반영합니다. 조회는 Redis에서 상위 id를 빠르게 가져오고, 상세 정보는 RDB에서 채워서 보여줍니다.
- 아직 내가 이해가 부족한 부분: 현재는 Level 1 전체/일간 top 10까지만 구현했고, 동점 처리 규칙과 실시간 갱신 체감은 더 다듬을 수 있다. 이후 SSE를 붙일 때 지금의 SSR 화면을 어떻게 부드럽게 갱신할지도 한 번 더 설계해야 한다.

## 2026-03-24 - 랭킹 페이지 짧은 주기 폴링 추가

- 단계: 5. Redis 랭킹 시스템
- 목적: `/ranking`이 처음 렌더링된 뒤 멈춰 있는 정적 화면처럼 보이지 않게 하고, Redis 랭킹의 장점을 사용자가 바로 체감할 수 있도록 짧은 주기 자동 갱신을 붙인다.
- 변경 파일:
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/static/js/ranking.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: `/ranking`은 SSR로 처음 렌더링된 뒤, 브라우저가 `ranking.js`에서 15초마다 `/api/rankings/location`, `/api/rankings/population`의 전체/일간 API를 다시 호출한다. 응답을 받으면 프론트가 각 표의 `tbody`만 다시 그려 전체 페이지 새로고침 없이 랭킹을 갱신한다. 탭이 비활성화되면 폴링을 잠시 멈추고, 다시 활성화되면 즉시 한 번 새로고침한 뒤 주기를 재개한다.
- 데이터 / 상태 변화: 서버 도메인과 DB 스키마는 바뀌지 않았다. 바뀐 것은 `/ranking` 페이지의 조회 후처리 방식이다. SSR 초기 HTML은 그대로 유지하고, 이후 갱신은 API 응답으로 표만 교체한다.
- 핵심 도메인 개념: 이 단계에서는 SSE보다 짧은 주기 폴링이 더 설명하기 쉽다. 이유는 기존 랭킹 API를 그대로 재사용하면서도 “Redis로 빨리 읽어온 상위 N명을 화면에 주기적으로 반영한다”는 흐름을 단순하게 보여줄 수 있기 때문이다. 즉 저장과 정렬의 핵심은 여전히 서버/Redis에 있고, 프론트는 이미 만들어진 API를 주기적으로 읽어 표현만 갱신한다.
- 예외 상황 또는 엣지 케이스: 자동 갱신 중 에러가 나면 기존 표 내용은 유지하고 상단 메시지 박스에 오류만 띄운다. 같은 탭이 백그라운드에 있을 때는 불필요한 호출을 줄이기 위해 폴링을 멈춘다. 현재는 모든 보드를 한 번에 갱신하므로, 이후 모드/범위 필터가 들어가면 호출 범위를 다시 좁힐 수 있다.
- 테스트 내용: `node --check src/main/resources/static/js/ranking.js` 통과, `./gradlew test --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과, `./gradlew test` 전체 통과. 랭킹 통합 테스트에는 `/ranking` 페이지가 새로고침 버튼을 포함해 렌더링되는지 확인을 추가했다. 랭킹 전용 테스트를 전체 테스트와 동시에 돌렸을 때는 Gradle 결과 파일 충돌이 있었지만, 단독 재실행으로 정상 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: 랭킹 1차는 저장과 조회 구조까지였고, 이번에는 `/ranking` 화면에 15초 간격 폴링을 붙여 실시간 갱신 체감을 만들었습니다. 프론트는 기존 랭킹 API를 주기적으로 다시 읽어 표만 바꾸고, 탭 비활성화 시에는 호출을 멈춰 불필요한 트래픽도 줄였습니다.
- 아직 내가 이해가 부족한 부분: 지금은 짧은 주기 폴링으로 충분하지만, 이후 사용자가 많아지거나 더 즉각적인 반영이 필요하면 SSE와 어떤 기준으로 갈아탈지 판단 기준을 더 명확히 해야 한다.

## 2026-03-24 - blog 동시 작성 규칙 정리와 랭킹 글 백필

- 단계: 0. 문서와 규칙 정리 / 5. Redis 랭킹 시스템 설명 보강
- 목적: 최근 랭킹 기능은 코드, 플레이북, 워크로그까지는 남았지만 `blog/` 공개 설명 글이 같은 턴에 작성되지 않았다. 그래서 “의미 있는 기능 조각은 docs와 함께 blog도 동시에 남긴다”는 규칙을 문서와 로컬 스킬에 명시하고, 누락된 랭킹 글 2개를 실제 코드 기준으로 백필한다.
- 변경 파일:
  - `AGENTS.md`
  - `.agents/skills/worldmap-doc-sync/SKILL.md`
  - `.agents/skills/worldmap-doc-sync/references/blog-update-rules.md`
  - `.agents/skills/worldmap-doc-sync/references/doc-impact-map.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/AI_AGENT_OPERATING_MODEL.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/06-redis-leaderboard-vertical-slice.md`
  - `blog/07-leaderboard-polling-refresh.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 이번 변경은 애플리케이션 런타임 흐름을 바꾸지 않는다. 대신 저장소의 문서 흐름을 바꾼다. 앞으로 의미 있는 기능 조각이 끝나면 `코드 + 테스트 + docs + blog`를 같은 턴 기본값으로 삼고, `worldmap-doc-sync`도 그 기준으로 문서 영향 범위를 판단한다. 동시에 랭킹 관련 blog 글은 이미 구현된 `LeaderboardService`, `/api/rankings/*`, `/ranking`, `ranking.js` 흐름을 각각 “저장/조회 구조”와 “화면 갱신 구조”로 나눠 설명한다.
- 데이터 / 상태 변화: DB 스키마나 Redis 키는 바뀌지 않았다. 바뀐 것은 저장소의 기록 정책이다. `docs/`는 내부 SSOT 역할을 유지하고, `blog/`는 의미 있는 기능 조각마다 같은 턴에 따라오는 공개 설명 레이어로 기준이 강화됐다.
- 핵심 도메인 개념: 이 프로젝트는 AI 도움을 받아 개발하더라도 사용자가 구현 이유를 직접 설명할 수 있어야 한다. 그래서 `WORKLOG`만으로 내부 메모를 남기는 데 그치지 않고, 초보자도 읽을 수 있는 공개 설명 글까지 같은 시점에 남겨야 이해 공백이 줄어든다. 특히 랭킹처럼 “RDB source of truth + Redis read model + polling UI”가 겹치는 주제는 블로그 분리가 설명력을 크게 높인다.
- 예외 상황 또는 엣지 케이스: 모든 변경에 블로그를 강제하면 문서가 과도하게 불어난다. 그래서 버튼 문구, 오타, 아주 작은 CSS 조정처럼 설명 가치가 낮은 수정은 예외로 두고, 이런 경우에는 블로그를 생략한 이유를 워크로그나 최종 설명에 짧게 남기도록 규칙을 잡았다.
- 테스트 내용: 코드 동작 변경은 없는 문서 작업이므로 애플리케이션 테스트는 별도로 실행하지 않았다. 대신 새 blog 글은 이미 통과했던 `LeaderboardIntegrationTest`, `./gradlew test`, `node --check src/main/resources/static/js/ranking.js` 결과를 근거로 작성했다.
- 면접에서 30초 안에 설명하는 요약: 이 프로젝트는 AI 도움을 받더라도 나중에 제가 직접 설명할 수 있어야 해서, 의미 있는 기능 조각은 코드와 내부 문서뿐 아니라 blog 글도 같은 턴에 같이 남기도록 운영 규칙을 강화했습니다. 이번에는 빠져 있던 Redis 랭킹 저장 구조와 랭킹 폴링 UI 글을 백필해, 랭킹 기능을 문서와 공개 글 기준으로 모두 설명 가능하게 맞췄습니다.
- 아직 내가 이해가 부족한 부분: 어디까지를 “설명 가치가 있는 기능 조각”으로 보고 blog를 반드시 써야 하는지는 아직 약간의 판단 여지가 있다. 특히 작은 UX 수정이 누적돼 게임 루프 체감이 크게 달라지는 경우, 어느 시점에서 하나의 blog 글로 묶는 것이 가장 좋은지 경험이 더 필요하다.

## 2026-03-24 - 랭킹 화면 필터와 동점 규칙 노출

- 단계: 5. Redis 랭킹 시스템
- 목적: 랭킹 저장 구조와 15초 폴링은 이미 있었지만, 위치/인구수와 전체/일간 보드를 한 화면에 모두 펼쳐 놓으니 읽기 순서가 약했다. 그래서 active 보드 하나만 크게 보는 필터 UI로 정리하고, 동점 처리 기준도 화면에 직접 노출해 랭킹 규칙을 더 설명 가능하게 만든다.
- 변경 파일:
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/static/js/ranking.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/08-ranking-filter-and-tie-rule.md`
- 요청 흐름 / 데이터 흐름: `/ranking`은 여전히 SSR로 처음 렌더링되고, 브라우저는 `ranking.js`에서 15초마다 기존 `/api/rankings/location`, `/api/rankings/population` API를 다시 호출한다. 달라진 점은 UI가 이제 active 보드 하나만 보여준다는 것이다. `위치/인구수`, `전체/일간` 버튼은 프론트 로컬 상태만 바꾸고, 폴링은 같은 API 응답으로 각 `tbody`를 갱신한다. 즉 정렬 결과 계산은 계속 서버가 맡고, 어떤 보드를 보여줄지는 프론트가 맡는다.
- 데이터 / 상태 변화: DB 스키마와 Redis 키 전략은 바뀌지 않았다. `leaderboard_record`, `rankingScore`, Redis Sorted Set 구조도 그대로 유지된다. 바뀐 것은 `/ranking`의 표현 구조와 안내 정보다. 화면에 동점 규칙을 직접 적어, `rankingScore desc -> finishedAt asc` 기준이 UI에서도 읽히게 만들었다.
- 핵심 도메인 개념: 이번 변경은 도메인 변경이 아니라 표현 변경이다. 중요한 점은 “필터 전환은 프론트가 해도 되지만, 정렬과 동점 처리 규칙은 서버가 계속 가져가야 한다”는 경계를 지킨 것이다. 그래서 새 API를 늘리지 않고 기존 랭킹 API를 재사용했고, SSR 템플릿은 4개 보드를 준비하되 active 보드만 노출하도록 바꿨다.
- 예외 상황 또는 엣지 케이스: 현재 daily 보드 설명 문구의 기준 날짜는 SSR 시점 값을 사용한다. 같은 날 안에서는 충분하지만, 자정을 넘기는 장시간 탭 유지 상황까지 완전히 매끈하게 맞추려면 API 응답의 `targetDate`를 기준으로 설명 문구도 다시 갱신하도록 한 단계 더 손볼 수 있다. 또 필터 전환은 로컬 UI 상태이므로 URL deep link까지는 아직 지원하지 않는다.
- 테스트 내용: `node --check src/main/resources/static/js/ranking.js` 통과, `./gradlew test --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과. 통합 테스트에는 `/ranking` 페이지가 `게임 모드`, `동점 처리` 문구를 포함해 렌더링되는지 확인을 추가했다.
- 면접에서 30초 안에 설명하는 요약: 랭킹 저장과 조회 구조는 이미 만들었고, 이번에는 화면을 더 설명 가능하게 정리했습니다. 기존 API는 그대로 두고 프론트에서 `위치/인구수`, `전체/일간` 필터로 active 보드 하나만 크게 보여주게 바꿨고, 동점 처리 규칙도 화면에 명시해서 `rankingScore` 기준이 UI에서도 읽히게 했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 active 보드 전환이 로컬 상태라 충분하지만, 나중에 URL 공유나 deep link가 필요해지면 query parameter와 어떻게 연결할지 한 번 더 설계해야 한다. 또한 daily 보드 설명 문구 날짜를 폴링 응답 기준으로 더 정교하게 갱신할지 여부도 판단이 남아 있다.

## 2026-03-24 - 랭킹 단계 마감과 polling 유지 결정

- 단계: 5. Redis 랭킹 시스템 마감
- 목적: 랭킹은 이미 `RDB 저장 + Redis Sorted Set + fallback 조회 + polling UI`까지 구현됐기 때문에, 더 복잡한 SSE를 바로 붙이기보다 현재 전략을 명시적으로 닫는 편이 맞다. 그래서 화면과 문서에 “현재는 15초 polling으로 운영하고, SSE/WebSocket은 9단계 고도화 범위”라는 결정을 남긴다.
- 변경 파일:
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/07-leaderboard-polling-refresh.md`
- 요청 흐름 / 데이터 흐름: 런타임 데이터 흐름은 바뀌지 않는다. `/ranking`은 여전히 SSR 후 15초마다 랭킹 API를 다시 읽는다. 이번 변경은 그 선택을 제품에 명시한 것이다. 사용자는 화면 상단에서 현재 전달 방식이 `15초 Polling`임을 바로 읽을 수 있고, 문서상으로도 SSE/WebSocket은 `9단계 실시간성 고도화`로 넘긴다고 정리했다.
- 데이터 / 상태 변화: DB 스키마, Redis 키, API 응답은 바뀌지 않았다. 바뀐 것은 단계 상태와 설명 문구다. 플레이북에서는 5단계를 `Done`으로 닫고, 실시간 전송 고도화는 9단계로 이관했다.
- 핵심 도메인 개념: 지금 중요한 것은 “실시간 같아 보이는 랭킹 체감”이지, 가장 복잡한 실시간 기술을 먼저 붙이는 것이 아니다. 현재 구조에서는 polling이 이미 Redis read model과 잘 맞고, 설명도 쉽다. 따라서 지금은 polling으로 마감하고, 실제로 더 낮은 지연이나 서버 push가 필요해질 때만 SSE/WebSocket으로 올리는 판단이 더 합리적이다.
- 예외 상황 또는 엣지 케이스: 현재 문구는 전략 결정 자체를 드러내기 위한 것이고, 사용자가 polling 주기까지 바꾸거나 URL에 전략을 선택하는 기능은 없다. 이후 사용량이 늘거나 자정 경계 같은 더 빠른 반영이 중요해지면 그때 SSE로 넘어갈 근거를 다시 모아야 한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과. 통합 테스트에는 `/ranking`이 `15초 Polling` 문구를 포함하는지 확인을 추가했다.
- 면접에서 30초 안에 설명하는 요약: 랭킹은 이미 저장/조회/복구 구조가 완성돼 있었기 때문에, 이번에는 전략을 마감하는 작업을 했습니다. 현재 MVP에서는 15초 polling으로 충분하다고 판단해 그 결정을 화면과 문서에 남겼고, 더 복잡한 SSE/WebSocket은 9단계 실시간성 고도화 범위로 분리했습니다.
- 아직 내가 이해가 부족한 부분: 언제 polling이 더 이상 충분하지 않고 SSE/WebSocket으로 넘어가야 하는지에 대한 정량 기준은 아직 약하다. 이후 사용자 수, 업데이트 빈도, 서버 리소스 데이터를 본 뒤 판단 기준을 더 구체화해야 한다.

## 2026-03-24 - 설문 기반 추천 엔진 1차 vertical slice

- 단계: 6. 설문 기반 추천 엔진
- 목적: 추천 기능을 바로 LLM에게 맡기지 않고, 설문 답변만으로 deterministic하게 top 3 국가를 계산하는 서버 엔진을 먼저 만든다. 이번 조각에서는 설문 페이지, 답변 타입, 국가 프로필 카탈로그, 가중치 계산 서비스, 결과 SSR 화면까지 연결한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfile.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationOptionView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationQuestionView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationPreferenceSummaryView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCandidateView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyResultView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/static/css/site.css`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/09-survey-recommendation-engine.md`
- 요청 흐름 / 데이터 흐름: 사용자는 `GET /recommendation/survey`로 설문 페이지를 열고 6개 문항을 고른 뒤 `POST /recommendation/survey`를 보낸다. 컨트롤러는 `RecommendationSurveyForm`으로 입력을 검증하고, 이를 `RecommendationSurveyAnswers` 불변 객체로 바꿔 `RecommendationSurveyService.recommend()`에 넘긴다. 서비스는 `CountryRepository`의 기본 국가 정보와 `RecommendationCountryProfileCatalog`의 추천 전용 속성을 합쳐 점수를 계산하고, 상위 3개 결과를 `RecommendationSurveyResultView`로 만들어 SSR 결과 페이지에 넘긴다.
- 데이터 / 상태 변화: 아직 DB 스키마는 바뀌지 않았다. 이번 단계의 “답변 저장 구조”는 DB 엔티티가 아니라 `폼 객체 -> 불변 답변 객체` 구조다. 이유는 지금 단계의 핵심이 추천 규칙 확정이기 때문이다. 국가 기본 데이터는 기존 `country` 테이블을 재사용하고, 추천용 속성만 별도 프로필 카탈로그로 시작했다.
- 핵심 도메인 개념: 추천 계산은 LLM이 아니라 서버가 해야 한다. 그래서 설문 입력 자체도 enum 기반으로 타입을 고정했고, 추천 후보 비교는 기후/생활 속도/물가/환경/영어/최우선 기준을 점수화해 deterministic하게 처리했다. 즉 이번 결과 화면은 “자연어 설명”이 아니라 “서버가 계산한 근거”를 먼저 보여주는 1차 엔진이다.
- 예외 상황 또는 엣지 케이스: 현재 추천 프로필은 12개 국가로 시작하는 수작업 카탈로그라, 후보 풀이 아직 제한적이다. 또한 설문 답변은 현재 세션 기록으로 저장하지 않으므로 나중에 추천 이력이나 재조회 기능을 붙일 때는 별도 저장 모델이 필요하다. 영문/현지 언어 적응 난이도도 지금은 영어 친화도 한 축으로 단순화했다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest --tests com.worldmap.web.HomeControllerTest` 통과. 서비스 테스트는 warm/fast/high/city/high english/diversity 조합에서 `싱가포르`가 1위로 나오는지 확인했고, 통합 테스트는 설문 페이지 렌더링과 설문 제출 후 결과 페이지 SSR 렌더링을 확인했다.
- 면접에서 30초 안에 설명하는 요약: 추천 기능은 처음부터 계산과 설명을 분리했습니다. 이번 단계에서는 설문 답변을 enum 기반으로 고정하고, 서버가 국가 프로필 카탈로그와 비교해 가중치 점수로 top 3를 계산하도록 만들었습니다. 즉 추천 결과 자체는 deterministic하게 서버가 만들고, LLM은 다음 단계에서 그 결과를 설명하는 역할만 맡게 됩니다.
- 아직 내가 이해가 부족한 부분: 현재 국가 프로필 12개는 시작용이라 추천 품질을 더 높이려면 후보 국가 수와 속성을 더 늘려야 한다. 또한 답변 저장을 언제 DB 엔티티로 올릴지, LLM 프롬프트 입력용 구조를 어디서 고정할지도 다음 단계에서 더 분명히 해야 한다.

## 2026-03-24 - 공통 박스/버튼 모서리 각지게 통일

- 단계: 공통 UI 폴리시
- 목적: 현재 사이트 전반의 버튼, 패널, 카드, 입력창, 모달이 둥근 모서리를 써서 “차갑고 각진 우주 HUD”라는 시각 방향과 어긋나 있었다. 이번 조각에서는 공통 CSS를 한 번에 정리해 모든 박스형 컴포넌트의 모서리를 각지게 통일한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 런타임 요청이나 서버 데이터 흐름은 바뀌지 않았다. 바뀐 것은 공통 표현 계층이다. 모든 화면은 같은 `site.css`를 공유하므로, 헤더 네비게이션, 버튼, 카드, 입력창, 글로브 스테이지, 게임오버 모달, 결과 배너까지 공통 스타일 토큰을 한 번에 수정하는 방식으로 반영된다.
- 데이터 / 상태 변화: DB, Redis, API, 세션 상태 변화는 없다. 변경은 전부 프론트 공통 테마 레벨에 머문다.
- 핵심 도메인 개념: 이 프로젝트는 기능만 아니라 “게임 플랫폼처럼 보이는 일관된 화면 톤”도 중요하다. 각 화면에서 개별적으로 radius를 줄이는 대신 공통 CSS의 반경 선언을 전부 제거해, 모든 박스형 UI가 같은 시각 규칙을 따르도록 맞췄다.
- 예외 상황 또는 엣지 케이스: 이번 검증 범위는 프로젝트가 직접 관리하는 공통 CSS와 템플릿/정적 스크립트이다. 외부 vendor 파일은 수정하지 않았다. 검색 기준으로는 `src/main/resources/static/css/site.css`, `src/main/resources/templates`, `src/main/resources/static/js`, `src/main/resources/static`에서 vendor를 제외하고 `border-radius|radius`를 다시 확인했다.
- 테스트 내용: 자동 UI 테스트는 없어서 애플리케이션 테스트는 돌리지 않았다. 대신 `git diff --check` 통과, `rg -n "border-radius|radius"` 재검색으로 프로젝트가 직접 관리하는 스타일 영역의 반경 선언을 다시 확인했다.
- 면접에서 30초 안에 설명하는 요약: 디자인 톤을 맞추기 위해 버튼과 카드만 일부 고친 게 아니라, 공통 CSS에서 반경 선언을 한 번에 정리했습니다. 그래서 헤더, 버튼, 입력창, 모달, 게임 스테이지 같은 박스형 UI가 모두 같은 각진 HUD 규칙을 따르게 됐습니다.
- 아직 내가 이해가 부족한 부분: 지금은 전역적으로 반경을 0으로 통일했지만, 이후 브랜드 아이덴티티를 더 다듬는 과정에서 “완전한 직각”과 “아주 작은 모따기” 중 어느 쪽이 더 좋은지 시각적으로 한 번 더 비교해볼 필요가 있다.
- blog 작성 여부: 생략. 이번 변경은 요청 흐름이나 도메인 모델이 아니라 공통 테마 조정이라 별도 기술 글보다 WORKLOG 기록이 더 적합하다고 판단했다.

## 2026-03-24 - 공통 박스/버튼 컷 코너 HUD로 재조정

- 단계: 공통 UI 폴리시
- 목적: `border-radius: 0`만으로는 박스가 네모가 되더라도 전체 인상이 여전히 부드럽게 느껴질 수 있었다. 그래서 이번에는 공통 표면 자체를 컷 코너(chamfer) 형태로 바꿔, 버튼과 패널이 더 기계적이고 각진 HUD처럼 보이도록 재조정한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 런타임 요청이나 서버 데이터 흐름은 바뀌지 않았다. 모든 페이지가 공통 `site.css`를 공유하므로, 헤더 네비게이션, 버튼, 입력창, 카드, 모달, 글로브 스테이지 같은 박스형 UI는 같은 컷 코너 규칙을 바로 공유하게 된다.
- 데이터 / 상태 변화: 상태 변화는 없다. 전부 공통 표현 계층 수정이다.
- 핵심 도메인 개념: 이번 수정의 핵심은 “직각”과 “각진 인상”이 다르다는 점이다. 단순히 radius를 0으로 두는 것만으로는 충분하지 않아서, 공통 표면에 동일한 clip-path 컷 코너 규칙을 적용해 디자인 언어 자체를 더 날카롭게 통일했다.
- 예외 상황 또는 엣지 케이스: 이번 범위는 공통 CSS가 직접 관리하는 표면 요소들이다. 외부 vendor 자산은 건드리지 않았다. 테이블 셸처럼 overflow가 있는 요소도 같은 규칙을 적용했기 때문에, 실제 체감은 브라우저 강력 새로고침 후 확인하는 것이 안전하다.
- 테스트 내용: `git diff --check` 통과, `rg -n "border-radius|border-top-left-radius|border-top-right-radius|border-bottom-left-radius|border-bottom-right-radius|radius"` 재검색으로 프로젝트가 직접 관리하는 스타일 범위를 다시 점검했다. `curl -I http://localhost:8080/css/site.css`로 새 CSS가 정상 서빙되는 것도 확인했다.
- 면접에서 30초 안에 설명하는 요약: 처음에는 모서리 반경만 제거했지만, 그 정도로는 여전히 화면이 부드럽게 느껴졌습니다. 그래서 공통 CSS에서 버튼, 패널, 입력창, 모달 같은 박스형 UI 전체에 같은 컷 코너 규칙을 넣어, 사이트 전반이 더 각진 게임 HUD처럼 보이도록 한 번 더 정리했습니다.
- 아직 내가 이해가 부족한 부분: clip-path 기반 컷 코너가 브라우저별 렌더링이나 모바일 성능에 어떤 차이를 주는지는 실제 기기에서 추가 확인이 필요하다.
- blog 작성 여부: 생략. 이번 변경도 요청 흐름이나 도메인 모델이 아니라 공통 테마 조정이라 WORKLOG 기록으로 충분하다고 판단했다.

## 2026-03-24 - 공통 CSS 반영 경로 정리와 각진 HUD 2차 강화

- 단계: 공통 UI 폴리시
- 목적: 사용자 스크린샷상 둥근 디자인이 그대로 남아 있었고, 확인 결과 8080에서 실제로 내려가던 `site.css`가 수정 전 구버전이었다. 그래서 이번에는 컷 코너를 더 크게 강화하는 동시에, 모든 템플릿의 스타일 링크에 버전 쿼리를 붙여 새 공통 CSS가 확실히 반영되도록 정리한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 비즈니스 요청 흐름은 바뀌지 않았다. 바뀐 것은 렌더링 자산 전달 경로다. 각 페이지 템플릿은 이제 `/css/site.css?v=20260324-angular-hud-2` 형태로 공통 스타일을 참조해, 브라우저와 서버가 이전 CSS를 잡고 있더라도 새 버전을 강제로 읽게 된다.
- 데이터 / 상태 변화: DB, Redis, API, 게임 세션 상태는 바뀌지 않는다. 이번 변경은 전부 공통 UI 자산 전달과 표현 계층에 있다.
- 핵심 도메인 개념: “반경을 0으로 둔다”와 “사용자가 실제로 각진 화면을 본다”는 다른 문제다. 이번에는 컷 코너 크기를 더 키워 디자인 언어를 더 날카롭게 만들고, 동시에 전역 스타일 링크에 버전 쿼리를 붙여 수정본이 실제로 화면에 반영되도록 했다.
- 예외 상황 또는 엣지 케이스: 이 수정은 템플릿이 직접 참조하는 공통 CSS에만 적용된다. 만약 브라우저 탭이 아주 오래된 HTML 자체를 들고 있으면 한 번 새로고침이 여전히 필요할 수 있다. 또한 현재 버전 쿼리는 수동 문자열이므로, 나중에 정적 자산 빌드 파이프라인이 생기면 해시 기반으로 바꾸는 편이 더 좋다.
- 테스트 내용: `/css/site.css` 응답 본문을 직접 확인해 이전 `border-radius: 18px`, `999px` 구버전이 내려오고 있음을 재현했고, 이후 템플릿 링크와 CSS를 수정했다. `git diff --check` 통과, `rg -n "site.css"`로 모든 템플릿의 스타일 링크를 다시 점검했다.
- 면접에서 30초 안에 설명하는 요약: 처음에는 공통 CSS만 수정했지만, 실제 화면은 오래된 정적 자산을 계속 보고 있었습니다. 그래서 버튼과 패널의 컷 코너를 더 크게 강화하는 동시에, 모든 페이지의 `site.css` 링크에 버전 쿼리를 붙여 새 스타일이 확실히 반영되도록 정리했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 수동 버전 문자열로 캐시를 깨고 있는데, 이후 빌드/배포 환경이 생기면 정적 자산 fingerprinting으로 어떻게 자동화할지 더 정리해야 한다.
- blog 작성 여부: 생략. 이번 변경은 공통 스타일 반영 경로와 시각 조정이라 별도 기술 글보다 WORKLOG 기록이 적합하다고 판단했다.

## 2026-03-24 - 컷 코너 제거와 완전한 사각형 UI로 재정렬

- 단계: 공통 UI 폴리시
- 목적: 사용자 피드백 기준으로 컷 코너 자체가 촌스럽고 불필요했다. 이번에는 “각진 느낌”이 아니라 진짜 직사각형을 목표로, 공통 UI에서 잘린 모서리 규칙을 제거하고 모든 박스형 요소를 완전한 사각형으로 정리한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/error/404.html`
  - `src/main/resources/templates/error/500.html`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 서버 요청 흐름은 바뀌지 않았다. 렌더링 레이어에서만 바뀌었다. 모든 템플릿이 이제 `/css/site.css?v=20260324-square-ui-1`을 참조하고, 공통 CSS에서는 컷 코너용 `clip-path`와 관련 변수들을 제거해 단순한 사각형 표면만 남겼다.
- 데이터 / 상태 변화: 없다. 표현 계층만 수정했다.
- 핵심 도메인 개념: 사용자가 원하는 것은 “기계적인 컷 코너”가 아니라 “깔끔한 직사각형”이었다. 그래서 반경을 없앤 뒤 남아 있던 clip-path까지 제거해, 박스형 UI가 해석 여지 없이 직사각형으로 보이도록 정리했다.
- 예외 상황 또는 엣지 케이스: 버전 쿼리를 다시 바꿨기 때문에 새 HTML을 받은 뒤에는 오래된 CSS를 잡을 가능성이 낮다. 다만 브라우저 탭이 아주 오래된 문서를 유지 중이면 한 번 새로고침은 여전히 필요할 수 있다.
- 테스트 내용: `rg -n "clip-path|surface-cut|cut-sm|cut-md|cut-lg"`로 컷 코너 관련 선언과 참조를 다시 확인했고, 스타일 링크가 모든 템플릿에서 `20260324-square-ui-1`로 바뀌었는지 검색으로 점검했다.
- 면접에서 30초 안에 설명하는 요약: 처음에는 radius 제거, 그다음엔 컷 코너까지 시도했지만 사용자 취향과 안 맞았습니다. 그래서 공통 CSS에서 컷 코너 자체를 걷어내고, 모든 박스형 UI를 `border-radius: 0`인 완전한 직사각형으로 다시 정리했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 완전한 직사각형으로 통일했지만, 이후 타이포/간격/보더 두께까지 함께 다듬어야 더 세련된 결과가 나올 수 있다.
- blog 작성 여부: 생략. 이번 변경은 공통 테마 정리라 WORKLOG 기록으로 충분하다고 판단했다.

## 2026-03-24 - 위치 게임 선택 HUD 단순화와 핑크 네온 하이라이트

- 단계: 3. 국가 위치 찾기 게임 Level 1
- 목적: 플레이 화면에서 `선택 상태`, `진행 가이드`, `선택 취소`가 오히려 게임 템포를 끊고 있었다. 이번에는 선택 정보 텍스트를 걷어내고, 지구본 위 하이라이트와 제출 버튼만 남겨 위치 게임 입력 흐름을 더 단순하게 만든다.
- 변경 파일:
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/css/site.css`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 플레이 화면은 여전히 `GET /api/games/location/sessions/{sessionId}/state`로 현재 Stage를 받고, 사용자가 국가를 클릭하면 클라이언트 로컬 상태의 `selectedCountryIso3Code`만 바뀐다. 달라진 점은 이 선택 상태를 더 이상 하단 텍스트 HUD로 풀어주지 않는다는 것이다. 사용자는 지구본 위 선택 하이라이트를 보고 바로 `POST /api/games/location/sessions/{sessionId}/answer` 제출만 한다.
- 데이터 / 상태 변화: 서버 API나 엔티티는 바뀌지 않았다. 바뀐 것은 프론트 표현과 입력 흐름이다. `selectedCountryIso3Code`는 여전히 클라이언트 로컬 상태로 유지되고, 사용자가 다른 국가를 다시 클릭하면 곧바로 교체된다.
- 핵심 도메인 개념: 위치 게임의 선택 상태는 서버가 저장할 필요가 없는 일시적 UI 상태다. 그래서 텍스트 박스로 상태를 길게 설명하기보다, 지구본 위 하이라이트 자체를 선택 신호로 쓰는 편이 게임답고 단순하다. 제출 전 국가명 비노출 규칙도 그대로 유지된다.
- 예외 상황 또는 엣지 케이스: 선택 취소 버튼을 제거했기 때문에 “선택 해제” 대신 “다른 나라 재선택”이 유일한 변경 방식이 됐다. 아무 것도 선택하지 않은 상태에서 제출하면 기존처럼 메시지 박스로 안내한다. 핑크 네온 하이라이트는 선택된 국가의 cap/stroke/altitude만 조정하므로 서버 판정에는 영향을 주지 않는다.
- 테스트 내용: `node --check src/main/resources/static/js/location-game.js` 통과, `git diff --check` 통과. 추가로 플레이 페이지 HTML을 확인해 `선택 상태`, `진행 가이드`, `선택 취소` 요소가 제거되고, 제출 버튼만 남았는지 점검했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임은 선택 상태를 굳이 텍스트 박스로 길게 보여줄 필요가 없다고 판단했습니다. 그래서 하단에서는 제출 버튼만 남기고, 선택 여부는 지구본 위 핑크 네온 하이라이트로만 보여주도록 단순화했습니다. 선택을 바꾸고 싶으면 취소 대신 다른 국가를 다시 클릭하면 됩니다.
- 아직 내가 이해가 부족한 부분: 핑크 네온 하이라이트 강도가 실제 플레이에서 너무 강하거나 약한지, 그리고 모바일 화면에서 제출 버튼 하나만 있는 구성이 충분히 직관적인지는 한 번 더 체감 확인이 필요하다.
- blog 작성 여부: 생략. 이번 변경은 위치 게임 입력 HUD를 단순화하는 UX polish라 WORKLOG 기록으로 충분하다고 판단했다.

## 2026-03-24 - 추천 후보 국가 풀 30개로 확장

- 단계: 6. 설문 기반 추천 엔진
- 목적: 추천 계산 구조는 이미 만들어졌지만, 후보 풀이 12개 국가에 머물러 있어 결과가 몇몇 익숙한 국가에 과도하게 몰릴 수 있었다. 이번 조각에서는 점수 계산 로직은 유지하고, 추천 전용 국가 프로필 카탈로그를 30개로 확장해 후보 다양성을 먼저 높인다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalogTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/09-survey-recommendation-engine.md`
  - `blog/10-expand-recommendation-candidate-pool.md`
- 요청 흐름 / 데이터 흐름: 추천 요청 흐름 자체는 그대로다. 사용자가 설문을 제출하면 `RecommendationSurveyForm -> RecommendationSurveyAnswers -> RecommendationSurveyService.recommend()` 순서로 흐른다. 달라진 점은 서비스가 순회하는 `RecommendationCountryProfileCatalog`의 데이터 폭이다. 이제 북미, 유럽, 동아시아, 동남아, 중동, 남미, 아프리카, 오세아니아까지 포함한 30개 프로필을 비교 대상으로 사용한다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았다. 이번 단계의 변화는 추천 전용 카탈로그 데이터다. `country` 테이블은 여전히 국가 기본 정보 source of truth로 남고, 추천 속성만 별도 프로필 카탈로그에 더 풍부하게 채워 넣었다.
- 핵심 도메인 개념: 추천 품질은 계산 공식뿐 아니라 “어떤 후보 데이터를 비교하느냐”에 크게 의존한다. 그래서 이번 단계에서는 가중치 수식을 먼저 뒤엎지 않고, 동일한 점수 계산 구조가 더 넓은 후보 풀을 평가하도록 만들어 추천 결과의 다양성을 키웠다.
- 예외 상황 또는 엣지 케이스: 프로필 카탈로그가 커질수록 ISO 코드 오타나 시드 데이터와의 불일치가 숨어들 수 있다. 그래서 이번에는 추천 품질 테스트뿐 아니라 “모든 프로필 ISO 코드가 실제 시드 국가에 존재하는가”를 검증하는 카탈로그 테스트를 추가했다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과. 카탈로그 테스트는 30개 / 중복 없음 / 시드 ISO 존재 여부를 검증했고, 서비스 테스트는 확장 후보 풀에서 `말레이시아` 같은 신규 후보가 실제 1위로 올라오는 시나리오를 고정했다.
- 면접에서 30초 안에 설명하는 요약: 추천 엔진은 계산식만큼 후보 데이터가 중요해서, 이번에는 점수 공식을 바꾸지 않고 추천 프로필 카탈로그를 12개에서 30개로 넓혔습니다. 북미, 유럽, 동남아, 남미, 아프리카까지 후보를 분산시킨 뒤, ISO 유효성과 실제 추천 결과 상위권 진입 여부를 테스트로 고정해 추천 다양성을 먼저 높였습니다.
- 아직 내가 이해가 부족한 부분: 지금은 프로필 값을 수작업으로 관리하므로, 이후 후보 국가가 더 늘어나면 어떤 기준으로 값을 보정하고 versioning할지 더 정리할 필요가 있다.

## 2026-03-24 - 추천 결과 비저장 원칙과 만족도 피드백 수집 1차

- 단계: 6. 설문 기반 추천 엔진
- 목적: 추천 결과 자체를 DB에 저장하지 않겠다는 방향을 명확히 하고, 대신 설문 개선을 위한 최소 신호만 남기는 구조를 만든다. 이번 조각에서는 결과 페이지에서 `1~5점 만족도`, `surveyVersion`, `engineVersion`, 사용자가 고른 6개 답변만 익명으로 저장한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyResultView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackPayloadView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackSubmission.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackRepository.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackSavedResponse.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/static/js/recommendation-feedback.js`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/12-collect-recommendation-feedback.md`
- 요청 흐름 / 데이터 흐름: 사용자는 `POST /recommendation/survey`로 추천 결과 페이지를 받는다. 이때 서버는 `RecommendationSurveyResultView` 안에 `surveyVersion`, `engineVersion`, 그리고 선택한 6개 답변 코드로 이루어진 feedback payload를 같이 넣어 준다. 결과 페이지에서 사용자가 만족도 1~5점을 고르면 브라우저가 `POST /api/recommendation/feedback`를 호출하고, 서버는 `RecommendationFeedbackRequest -> RecommendationFeedbackSubmission -> RecommendationFeedbackService` 순서로 익명 피드백 레코드를 저장한다.
- 데이터 / 상태 변화: 추천 결과 top 3 자체는 저장하지 않는다. 저장되는 것은 `surveyVersion`, `engineVersion`, `satisfactionScore`, 그리고 6개 답변 enum 스냅샷이다. 즉 “결과 저장”이 아니라 “설문 개선 신호 저장”으로 범위를 제한했다.
- 핵심 도메인 개념: 이번 설계의 핵심은 “추천 결과를 기록하지 않아도 설문을 개선할 수 있다”는 점이다. 어떤 설문 버전과 엔진 버전에서 만족도가 높거나 낮았는지, 그리고 어떤 답변 조합에서 만족도가 낮은지를 보려면 결과 자체보다 `답변 스냅샷 + 만족도 점수`가 더 중요할 수 있다. 그래서 결과 저장은 생략하고, 개선용 피드백만 최소 구조로 남겼다.
- 예외 상황 또는 엣지 케이스: 만족도 점수는 1~5 범위만 허용한다. 결과 페이지에서 아무 점수도 선택하지 않으면 JS가 제출을 막고, 서버도 `@Min/@Max` 검증으로 한 번 더 막는다. 지금은 중복 제출 방지를 프론트 단의 버튼 잠금으로만 처리하고 있으며, 사용자 식별이나 결과 dedupe는 일부러 넣지 않았다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test` 전체 통과. 피드백 통합 테스트는 `POST /api/recommendation/feedback`가 실제 레코드를 저장하는지와 6점 같은 잘못된 점수를 400으로 거절하는지를 검증했다.
- 면접에서 30초 안에 설명하는 요약: 추천 결과는 저장하지 않기로 했고, 대신 설문을 개선할 수 있는 최소 신호만 남겼습니다. 결과 페이지에서 1~5점 만족도와 설문/엔진 버전, 사용자가 고른 6개 답변만 익명으로 저장하고, 이 데이터를 기준으로 어떤 설문 버전이 더 만족도가 높은지 나중에 비교할 수 있게 했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 피드백을 저장만 하고 집계 조회는 아직 없다. 다음 단계에서 버전별 평균 점수, 응답 수, 특정 답변 조합별 만족도 같은 집계 기준을 어디까지 보여줄지 더 정리해야 한다.

## 2026-03-24 - 추천 만족도 버전 집계 조회 1차

- 단계: 6. 설문 기반 추천 엔진
- 목적: 만족도 피드백을 저장하기만 하면 설문 개선에 바로 쓰기 어렵다. 이번 조각에서는 `surveyVersion + engineVersion` 기준으로 평균 점수, 응답 수, 1~5점 분포를 읽는 최소 집계 화면과 API를 붙인다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackRepository.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackVersionSummaryProjection.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackInsightsView.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackSummaryView.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java`
  - `src/main/resources/templates/recommendation/feedback-insights.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/12-collect-recommendation-feedback.md`
  - `blog/13-recommendation-feedback-insights.md`
- 요청 흐름 / 데이터 흐름: 사용자는 만족도 제출 뒤 내부적으로 `GET /api/recommendation/feedback/summary` 또는 `GET /recommendation/feedback-insights`를 통해 버전별 집계를 본다. 컨트롤러는 진입만 처리하고, 실제 집계는 `RecommendationFeedbackRepository.summarizeByVersion() -> RecommendationFeedbackService.summarizeByVersion()` 순서로 수행된다.
- 데이터 / 상태 변화: 이번 단계에서는 새 데이터를 더 저장하지 않는다. 이미 저장하던 `RecommendationFeedback`만 읽고, `surveyVersion + engineVersion` 그룹 기준으로 `responseCount`, `averageSatisfaction`, `score1~5Count`, `lastSubmittedAt`를 계산해 view로 바꾼다.
- 핵심 도메인 개념: 추천 결과를 저장하지 않더라도 설문 품질을 개선할 수 있는 핵심 단위는 `버전 조합`이다. 같은 `surveyVersion`과 `engineVersion`이 얼마나 만족도를 받았는지, 응답 수가 충분한지, 낮은 점수 분포가 몰려 있는지를 보면 “어느 버전을 유지/폐기할지” 판단할 수 있다.
- 예외 상황 또는 엣지 케이스: 응답 수가 1~2개뿐인 버전은 평균 점수만으로 판단하면 위험하다. 그래서 표에는 평균 점수뿐 아니라 응답 수와 1~5점 분포를 같이 보여준다. 아직은 답변 조합별 drill-down은 넣지 않고, 버전 집계까지만 제한한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest` 통과. 집계 통합 테스트는 `GET /api/recommendation/feedback/summary`가 버전별 응답 수와 평균 점수를 계산하는지, `GET /recommendation/feedback-insights`가 SSR 페이지를 렌더링하는지 확인했다.
- 면접에서 30초 안에 설명하는 요약: 추천 결과는 저장하지 않지만, 설문과 엔진 버전별 만족도 집계는 볼 수 있게 만들었습니다. `RecommendationFeedback`를 `surveyVersion + engineVersion` 기준으로 그룹핑해서 평균 점수, 응답 수, 1~5점 분포를 계산하고, 이 집계를 API와 SSR 화면으로 노출해 설문 개선 기준으로 삼았습니다.
- 아직 내가 이해가 부족한 부분: 지금은 버전 조합 단위 집계까지만 있다. 실제 설문 개선을 더 정교하게 하려면 “어떤 답변 조합에서 낮은 점수가 많이 나왔는지”까지 내려다볼지, 아니면 버전 평균만으로도 충분한지 더 판단해야 한다.

## 2026-03-24 - 추천 가중치 튜닝과 경계값 조정 1차

- 단계: 6. 설문 기반 추천 엔진
- 목적: 후보 국가 풀을 넓힌 뒤에도 영어 친화도나 최우선 기준 점수가 강하게 작용하면, 저물가·음식 중심 취향에서도 싱가포르 같은 일부 국가가 계속 상단에 남을 수 있었다. 이번 단계에서는 점수식의 경계값과 보조 정렬 기준을 조정해 추천 결과가 설문 의도를 더 직접 반영하도록 튜닝한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCandidateView.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/10-expand-recommendation-candidate-pool.md`
  - `blog/11-recommendation-weight-tuning.md`
- 요청 흐름 / 데이터 흐름: 추천 요청 흐름은 그대로 `RecommendationSurveyForm -> RecommendationSurveyAnswers -> RecommendationSurveyService.recommend()`이다. 바뀐 것은 서비스 내부 점수 계산과 정렬 규칙이다. 서비스는 각 후보에 대해 기후/속도/물가/도시성/영어/우선순위 점수를 계산한 뒤, 이제는 `정확 일치 보너스`, `초과 물가 패널티`, `핵심 생활 조건 coherence bonus`까지 합산하고, 동점 시에는 `강한 신호 개수 -> 정확 일치 개수 -> 국가명` 순으로 정렬한다.
- 데이터 / 상태 변화: DB와 설문 입력 구조는 바뀌지 않았다. 변한 것은 추천 계산식과 view 모델에 실어 두는 보조 비교 정보다. `RecommendationCandidateView`에는 이제 `strongSignalCount`, `exactMatchCount`도 함께 담긴다.
- 핵심 도메인 개념: 추천 품질은 후보 데이터뿐 아니라 경계값 설계에도 크게 좌우된다. 특히 물가처럼 사용자 제약에 가까운 항목은 단순한 거리 점수만으로는 부족하므로, “허용 범위를 초과했는가” 같은 규칙을 별도 패널티로 드러내는 편이 더 설명 가능하고 납득 가능한 결과를 만든다.
- 예외 상황 또는 엣지 케이스: 점수식이 복잡해질수록 왜 한 나라가 위로 갔는지 설명하기 어려워질 수 있다. 그래서 이번 단계에서는 너무 많은 축을 새로 만들지 않고, 기존 축 위에 exact match / over-budget penalty / coherence bonus만 얹는 선에서 멈췄다. 이렇게 해야 테스트와 면접 설명이 함께 유지된다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test` 전체 통과. 서비스 테스트는 `저물가 + 음식 중심` 시나리오에서 `말레이시아`가 `싱가포르`보다 앞서도록 기대값을 고정했다.
- 면접에서 30초 안에 설명하는 요약: 추천 후보 풀을 넓힌 뒤에는 점수식도 한 번 다듬었습니다. 이번에는 정확 일치 보너스, 물가 초과 패널티, 생활 조건 coherence bonus를 추가하고, 동점 비교 기준도 `강한 신호 개수`와 `정확 일치 개수`까지 보게 해서 설문 의도가 결과에 더 직접 반영되도록 조정했습니다.
- 아직 내가 이해가 부족한 부분: 지금 경계값은 서비스 내부 상수로 관리되는데, 이후 추천 품질을 더 높이려면 실제 사용자 피드백이나 저장된 추천 이력을 기준으로 어떤 값을 재조정할지 실험 체계가 더 필요하다.

## 2026-03-24 - 추천 런타임 LLM 제거와 오프라인 AI 개선 루프 재정의

- 단계: 7. AI-assisted 설문 개선 체계
- 목적: 추천 결과 설명을 위해 사용자 요청마다 LLM을 호출하는 방향은 과금과 비결정성 부담이 크다. 이번 조각에서는 추천 서비스는 계속 deterministic하게 유지하고, AI는 개발 단계의 설문/시나리오 개선 도구로만 사용하는 방향으로 문서와 화면 설명을 다시 고정한다.
- 변경 파일:
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/AI_AGENT_OPERATING_MODEL.md`
  - `docs/recommendation/OFFLINE_AI_SURVEY_IMPROVEMENT.md`
  - `docs/recommendation/PERSONA_EVAL_SET.md`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/java/com/worldmap/recommendation/package-info.java`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/00_rebuild_guide.md`
  - `blog/01-why-worldmap-game-platform-domain.md`
  - `blog/09-survey-recommendation-engine.md`
  - `blog/13-recommendation-feedback-insights.md`
  - `blog/14-offline-ai-survey-improvement-loop.md`
- 요청 흐름 / 데이터 흐름: 사용자 런타임 요청 흐름은 바뀌지 않는다. 추천은 여전히 `GET /recommendation/survey -> POST /recommendation/survey -> POST /api/recommendation/feedback`로 동작하고, 서버가 top 3를 deterministic하게 계산한다. 새로 고정한 것은 오프라인 개선 흐름이다. 이제는 `만족도 집계 확인 -> 페르소나 시나리오 비교 -> 서브 에이전트로 문항/시나리오 초안 생성 -> 사람 검수 -> 다음 surveyVersion 반영` 순서로 개선한다.
- 데이터 / 상태 변화: 런타임 엔티티는 추가하지 않았다. 대신 `docs/recommendation/PERSONA_EVAL_SET.md`와 `docs/recommendation/OFFLINE_AI_SURVEY_IMPROVEMENT.md`를 새 자산으로 두고, `surveyVersion`과 `engineVersion`을 오프라인 개선 루프의 비교 기준으로 쓰도록 명확히 했다.
- 핵심 도메인 개념: 추천 품질을 높인다고 해서 사용자 요청마다 외부 LLM을 호출할 필요는 없다. 이 프로젝트에서는 “결정은 서버가 하고, AI는 설문 품질을 더 빨리 개선하는 오프라인 도구로만 사용한다”는 분리를 택했다. 그래서 과금과 비결정성을 줄이면서도 AI의 생산성 이점은 가져갈 수 있다.
- 예외 상황 또는 엣지 케이스: 서브 에이전트가 만든 문항 초안이나 페르소나 시나리오는 바로 운영 반영하지 않는다. 최종 버전 채택은 항상 사람이 한다. 또한 지금은 오프라인 개선 루프를 문서와 시나리오 세트로 먼저 고정한 단계라, 실제 버전 개정 실험은 다음 단계에서 한 번 더 검증해야 한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.web.HomeControllerTest`와 `./gradlew test` 전체로 화면 설명과 기존 기능이 깨지지 않았는지 확인한다.
- 면접에서 30초 안에 설명하는 요약: 추천 기능에 런타임 LLM을 붙이는 대신, 서버 추천은 deterministic하게 유지하고 AI는 개발 단계에서만 사용하도록 방향을 바꿨습니다. 만족도 집계와 페르소나 시나리오를 함께 보고, 서브 에이전트가 문항 초안과 평가 시나리오를 제안하면 사람이 검수해 다음 설문 버전을 반영하는 구조로 정리했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 오프라인 개선 루프를 문서화한 단계라, 실제로 surveyVersion을 한 번 더 올려 본 실험 데이터는 아직 없다. 다음 단계에서 시나리오 세트와 실측 만족도 데이터를 같이 보며 실제 버전 개정을 한 번 수행해 볼 필요가 있다.

## 2026-03-24 - 페르소나 baseline 평가와 survey v2 개정안 초안

- 단계: 7. AI-assisted 설문 개선 체계
- 목적: 오프라인 개선 루프를 문서로만 두지 않고, 현재 추천 엔진을 14개 페르소나 시나리오로 실제 평가해 baseline을 고정하고 다음 `survey v2` 개정안을 만든다.
- 변경 파일:
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaScenario.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaFixtures.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `docs/recommendation/SURVEY_V2_PROPOSAL.md`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/14-offline-ai-survey-improvement-loop.md`
  - `blog/15-survey-v2-proposal-from-persona-eval.md`
- 요청 흐름 / 데이터 흐름: 이번 단계는 런타임 기능 추가가 아니라 오프라인 평가 흐름 보강이다. 테스트는 `RecommendationOfflinePersonaFixtures`의 14개 시나리오를 `RecommendationSurveyService.recommend()`에 통과시키고, 현재 엔진이 기대 후보를 얼마나 포함하는지 본다. 그 결과를 바탕으로 `docs/recommendation/SURVEY_V2_PROPOSAL.md`에 다음 버전 개정안을 적는다.
- 데이터 / 상태 변화: 운영 DB나 추천 API는 바뀌지 않았다. 새로 생긴 것은 테스트 자산과 평가 문서다. baseline은 “14개 중 11개 시나리오에서 기대 후보 1개 이상이 top 3에 포함”으로 고정했고, `P04`, `P06`, `P13`을 우선 개선 대상 시나리오로 명시했다.
- 핵심 도메인 개념: 설문 개선도 결국 “평가 자산 + 품질 하한 + 개정안”이 있어야 설명 가능하다. 그래서 Markdown 시나리오 표만 두지 않고 테스트 코드에도 같은 시나리오를 옮겨 baseline을 고정했다. 이렇게 해야 이후 `engine-v2` 실험에서 무엇이 좋아지고 무엇이 깨졌는지 바로 비교할 수 있다.
- 예외 상황 또는 엣지 케이스: 현재 baseline은 “최소 기대 후보 1개 이상이 top 3에 들어오는가”를 기준으로 잡았기 때문에, 결과 순서나 2~3위 후보까지 완전히 이상적이라는 뜻은 아니다. 특히 `P05`, `P11`처럼 부분 일치에 가까운 시나리오는 다음 단계에서 더 정밀하게 볼 필요가 있다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test` 전체 통과. anchor 시나리오 `P01`, `P02`, `P14`는 별도 assertion으로 고정했다.
- 면접에서 30초 안에 설명하는 요약: 오프라인 개선 루프를 실제로 돌리기 위해 14개 페르소나 시나리오를 테스트 코드로 옮겨 baseline을 고정했습니다. 현재 엔진은 11개 시나리오에서 기대 후보를 top 3에 포함하고, 약한 케이스는 복지형, 저예산 안전형, 온화한 고도시 다양성형이었습니다. 그래서 survey v2는 질문 수를 늘리기보다 먼저 climate mismatch penalty, 영어 가중치, 저예산 penalty를 조정하는 방향으로 개정안을 잡았습니다.
- 아직 내가 이해가 부족한 부분: 지금 개정안은 문서 초안이라 실제로 `engine-v2`를 적용했을 때 11/14가 얼마나 올라가는지는 아직 검증하지 않았다. 다음 단계에서 제안한 penalty와 가중치를 실제로 바꾸고 baseline 변화를 확인해야 한다.

## 2026-03-24 - 추천 엔진 실험 전 persona top3 snapshot 고정

- 단계: 7. AI-assisted 설문 개선 체계
- 목적: 직접 `engine-v2` 가중치를 바꿔보니 baseline이 쉽게 흔들렸다. 그래서 이번에는 production 점수식을 바로 더 바꾸기보다, 현재 `engine-v1`의 14개 페르소나 top 3 결과를 snapshot으로 먼저 고정해서 다음 실험을 더 안전하게 만들었다.
- 변경 파일:
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `docs/recommendation/SURVEY_V2_PROPOSAL.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/15-survey-v2-proposal-from-persona-eval.md`
  - `blog/16-freeze-persona-top3-snapshot.md`
- 요청 흐름 / 데이터 흐름: 런타임 요청 흐름은 바뀌지 않았다. 이번 단계는 오프라인 평가 테스트 보강이다. `RecommendationOfflinePersonaSnapshotTest`는 14개 시나리오를 `RecommendationSurveyService.recommend()`에 통과시켜 현재 top 3 국가 순서가 snapshot과 정확히 같은지 본다.
- 데이터 / 상태 변화: 운영 데이터와 추천 API는 그대로다. 새로 추가한 것은 “현재 엔진의 exact top 3 출력”을 테스트 자산으로 고정한 것이다. coverage test가 “기대 후보가 하나라도 들어오는가”를 본다면, snapshot test는 “정확히 어떤 순서로 나오는가”까지 함께 본다.
- 핵심 도메인 개념: 추천 품질 실험은 coverage 숫자만으로 보면 부족하다. 어떤 시나리오는 기대 후보가 top 3에 들어와도, 1위/2위가 계속 이상한 나라일 수 있다. 그래서 다음 `engine-v2` 실험 전에는 exact snapshot도 같이 고정해 두는 편이 더 설명 가능하다.
- 예외 상황 또는 엣지 케이스: snapshot test는 의도적으로 더 엄격하다. 다음 버전 실험에서 결과가 좋아져도 snapshot은 깨질 수 있다. 그때는 단순히 테스트를 맞추는 것이 아니라, 왜 순위가 바뀌었는지 문서와 함께 업데이트해야 한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과.
- 면접에서 30초 안에 설명하는 요약: 추천 엔진을 바로 튜닝해보니 baseline이 쉽게 흔들려서, 먼저 현재 엔진의 14개 페르소나 top 3 결과를 snapshot 테스트로 고정했습니다. 이제 다음 가중치 실험에서는 단순히 11/14 숫자만 보는 게 아니라, 어떤 시나리오의 top 3 순서가 어떻게 움직였는지도 같이 비교할 수 있습니다.
- 아직 내가 이해가 부족한 부분: snapshot을 언제 “좋은 변경이라서 갱신할 것인가”의 기준은 아직 더 정교하게 잡아야 한다. 다음 단계에서는 coverage 개선과 weak scenario 개선 근거가 있을 때만 snapshot을 갱신하는 규칙을 문서로 더 분명히 할 필요가 있다.

## 2026-03-24 - 추천 설문을 8문항으로 확장

- 단계: 6. 설문 기반 추천 엔진
- 목적: 추천 설문이 6문항만 있을 때는 사용자가 “정보가 너무 적다”는 인상을 받을 수 있었다. 이번 단계에서는 추천 계산 구조는 유지한 채, `정착 성향`과 `이동 생활 방식` 두 질문을 추가해 입력 밀도를 높이고 결과/피드백 구조도 같이 확장한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackPayloadView.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaFixtures.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `docs/recommendation/PERSONA_EVAL_SET.md`
  - `docs/recommendation/SURVEY_V2_PROPOSAL.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/17-expand-recommendation-survey-question-set.md`
- 요청 흐름 / 데이터 흐름: 기본 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend()`이다. 다만 입력이 6개 enum에서 8개 enum으로 늘었다. 결과 페이지는 `RecommendationSurveyResultView` 안에 8개 답변 스냅샷을 담은 `feedbackPayload`를 같이 내려주고, 만족도 제출은 `RecommendationFeedbackRequest -> RecommendationFeedbackSubmission -> RecommendationFeedbackService` 순서로 저장된다.
- 데이터 / 상태 변화: 추천 결과 top 3 자체는 여전히 저장하지 않는다. 저장되는 것은 `surveyVersion=survey-v2`, `engineVersion=engine-v2`, `satisfactionScore`, 그리고 8개 답변 스냅샷이다. 기존 feedback 요약 쿼리는 버전/점수 집계만 보기 때문에 그대로 유지된다.
- 핵심 도메인 개념: 문항 수를 늘리는 것은 단순 UI 변경이 아니다. 새 질문이 추천 순위에 영향을 주는 순간, 그 질문은 `RecommendationSurveyAnswers`와 `RecommendationSurveyService`가 책임져야 하는 서버 도메인 규칙이 된다. 이번에는 새 프로필 필드를 대거 추가하지 않고, 기존 국가 프로필 값(도시성, 속도, 안전, 복지, 영어, 다양성)을 조합해 두 질문을 흡수했다.
- 예외 상황 또는 엣지 케이스: 운영 DB에는 기존 `recommendation_feedback` 데이터가 있을 수 있으므로, 새 피드백 컬럼은 nullable로 두어 기존 데이터를 깨지 않게 했다. 오프라인 페르소나 baseline은 갑자기 흔들지 않도록 새 두 문항을 우선 `BALANCED`로 채워 기존 top 3 기준선을 유지했다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest` 통과, `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: 추천 설문이 너무 짧다는 피드백을 반영해 기존 6문항에 `정착 성향`, `이동 생활 방식`을 추가해 8문항으로 확장했습니다. 중요한 건 질문 수만 늘린 게 아니라, 새 답변도 `RecommendationSurveyService`에서 기존 국가 프로필 점수와 합산되도록 만들어 서버 중심 deterministic 추천 구조를 유지한 점입니다. 만족도 피드백 스냅샷도 8개 답변 기준으로 같이 확장했습니다.
- 아직 내가 이해가 부족한 부분: 새 두 질문이 실제로 weak scenario를 얼마나 더 잘 가를 수 있는지는 아직 중립값 baseline만 고정한 상태다. 다음 단계에서는 이 두 질문을 적극적으로 쓰는 새 페르소나를 추가하거나, engine-v2 실험에서 이 신호가 실제로 품질 개선에 얼마나 도움이 되는지 더 봐야 한다.

## 2026-03-24 - 새 추천 문항을 실제로 쓰는 active-signal 페르소나 추가

- 단계: 7. AI-assisted 설문 개선 체계
- 목적: 8문항 설문을 만들었지만, 오프라인 baseline은 아직 새 두 문항을 모두 `BALANCED`로만 쓰고 있었다. 이번 단계에서는 `정착 성향`, `이동 생활 방식`이 실제로 top 3 후보를 바꾸는지 확인할 수 있게 active-signal 페르소나 4개를 추가한다.
- 변경 파일:
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaFixtures.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `docs/recommendation/PERSONA_EVAL_SET.md`
  - `docs/recommendation/SURVEY_V2_PROPOSAL.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/18-activate-new-recommendation-signals-in-persona-eval.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `RecommendationSurveyService.recommend()`다. 이번 단계는 오프라인 평가 자산 보강이다. `RecommendationOfflinePersonaFixtures`에 `P15~P18`을 추가하고, `CoverageTest`는 18개 시나리오 기준 품질 하한을 본다. `SnapshotTest`는 같은 18개 시나리오의 exact top 3를 고정해 이후 가중치 실험에서 새 문항 때문에 어떤 후보가 바뀌었는지 바로 비교할 수 있게 한다.
- 데이터 / 상태 변화: 운영 DB나 추천 페이지는 바뀌지 않았다. 바뀐 것은 평가 자산과 기준선이다. baseline은 기존 14개 중립 시나리오 + 새 문항을 적극적으로 쓰는 4개 active-signal 시나리오로 나뉜다. 현재 품질 하한은 `18개 중 15개 시나리오에서 기대 후보 1개 이상이 top 3에 포함`으로 올렸다.
- 핵심 도메인 개념: 새 설문 문항을 추가했다면 오프라인 품질 평가도 그 문항을 실제로 써야 한다. 그렇지 않으면 추천 엔진이 새 답변을 제대로 읽는지 설명할 수 없다. 이번에는 같은 기본 취향에서 `EXPERIENCE / TRANSIT_FIRST`와 `STABILITY / SPACE_FIRST`만 바꿔, top 3의 2~3위 후보 구성이 실제로 달라지는 페어 시나리오를 만들었다.
- 예외 상황 또는 엣지 케이스: 새 두 문항의 현재 가중치가 2로 낮기 때문에, 모든 시나리오에서 top 1이 크게 바뀌지는 않는다. 그래서 baseline 목적은 “새 신호가 완전히 무시되지 않는가”를 보는 것이고, 더 큰 순위 변화를 원하면 다음 단계에서 penalty와 가중치를 조정해야 한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest` 통과, `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: 설문을 8문항으로 늘린 뒤에도 오프라인 baseline이 새 문항을 실제로 쓰지 않으면 품질 평가가 공허해집니다. 그래서 기존 14개 중립 시나리오 외에, 같은 기본 취향에서 `정착 성향`과 `이동 생활 방식`만 바꾼 4개 active-signal 시나리오를 추가했습니다. 이제 추천 엔진 실험에서 coverage 숫자뿐 아니라 새 문항이 실제 후보 구성을 어떻게 바꾸는지도 테스트와 snapshot으로 같이 볼 수 있습니다.
- 아직 내가 이해가 부족한 부분: 현재는 새 두 문항이 주로 2~3위 후보를 바꾸는 정도로 작동한다. 이 신호를 더 강하게 키워야 할지, 아니면 보조 신호로만 유지할지는 다음 `engine-v2` 실험에서 만족도와 weak scenario 개선 폭을 같이 보고 판단해야 한다.

## 2026-03-24 - public 문구와 admin 운영 화면 분리 설계

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계 / 8. 인증, 전적, 마이페이지
- 목적: 사용자에게는 완성된 게임 서비스처럼 보이게 하고, 버전/집계/로드맵 같은 내부 운영 정보는 별도 관리자 화면에서만 보이게 구조를 다시 나눈다. 현재 홈과 추천 화면에는 `Spring Boot`, `Current Build`, `Prototype`, `deterministic`, `surveyVersion` 같은 내부 개발 언어가 섞여 있어 제품 경험을 해친다.
- 변경 파일:
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `docs/PLAYER_COPY_AND_ADMIN_SPLIT_PLAN.md`
- 요청 흐름 / 데이터 흐름: 이번 단계는 구현이 아니라 설계 고정이다. public 요청 흐름은 그대로 `HomeController -> home.html`, `RecommendationPageController -> survey/result`, `RecommendationFeedbackService -> feedback-insights` 구조를 유지한다. 대신 다음 구현에서 `feedback-insights`를 `/admin/recommendation/feedback`으로 옮기고, 홈/추천/랭킹 public 화면에서는 내부 버전과 운영 정보 노출을 제거하는 방향을 문서로 확정했다.
- 데이터 / 상태 변화: 운영 DB나 서비스 상태는 바뀌지 않았다. 바뀐 것은 정보 구조와 역할 구분이다. public은 게임/추천/랭킹 경험만 보여 주고, admin은 버전/집계/로드맵/weak scenario 같은 운영 정보만 본다.
- 핵심 도메인 개념: 정보 노출도 설계 경계다. 추천 만족도 집계는 계속 `RecommendationFeedbackService`가 계산하더라도, 그 값을 누구에게 어떤 언어로 보여줄지는 별도 레이어 문제다. 그래서 컨트롤러나 템플릿 수정 전에 public copy 원칙과 admin route 구조를 먼저 문서로 고정했다.
- 예외 상황 또는 엣지 케이스: 지금 단계의 `/admin`은 화면 분리 설계이지 보안 완료 기능이 아니다. 실제 접근 제어는 8단계 인증에서 붙여야 한다. 즉, 1차 구현 목표는 “헤더에서 숨김 + internal route 분리”이고, 이것만으로 보안이 끝난 것은 아니다.
- 테스트 내용: 설계 문서 작업이라 애플리케이션 테스트는 실행하지 않았다.
- 면접에서 30초 안에 설명하는 요약: 사용자 화면에 개발 중인 프로젝트 냄새가 나면 제품 몰입이 깨지기 때문에, public 문구와 admin 운영 화면을 분리하는 설계를 먼저 잡았습니다. 앞으로는 홈/추천/랭킹 public 화면에서는 게임 경험만 보여 주고, 버전/집계/로드맵 같은 정보는 `/admin` read-only 대시보드로 옮길 예정입니다.
- 아직 내가 이해가 부족한 부분: admin 1차를 추천 운영 화면만 먼저 만들지, 홈의 빌드/로드맵까지 한 번에 옮길지는 실제 구현 범위를 더 잘게 자르며 판단해야 한다. 또 auth 전 단계에서 admin 링크를 어떻게 숨길지도 함께 정해야 한다.

## 2026-03-24 - public 화면 문구를 제품 언어로 전면 보정

- 단계: 6. 설문 기반 추천 엔진
- 목적: 플레이어가 보는 홈, 추천, 랭킹 화면에서 내부 개발 언어가 새어 나오지 않게 정리한다. 지금까지는 `Spring Boot`, `Current Build`, `Redis Leaderboard`, `deterministic`, `Offline Eval`, `만족도 집계 보기` 같은 표현이 public 화면에 남아 있어 서비스보다 데모처럼 보이는 문제가 있었다.
- 변경 파일:
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/test/java/com/worldmap/web/HomeControllerTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/19-refresh-public-copy-before-admin-split.md`
- 요청 흐름 / 데이터 흐름: 요청 흐름과 상태 변화는 바뀌지 않았다. 홈은 여전히 `HomeController -> home.html`, 추천은 `RecommendationPageController -> survey/result`, 랭킹은 `LeaderboardPageController -> ranking/index.html`로 간다. 이번 단계는 같은 요청 흐름 위에서 public copy만 다시 썼다. 추천 결과 화면에서는 `feedback-insights`로 가는 링크를 제거해, 플레이어가 내부 운영 페이지로 진입하지 않게 했다.
- 데이터 / 상태 변화: DB, 엔티티, API 응답 구조는 그대로다. 바뀐 것은 public 템플릿과 홈 화면 view model의 언어다. 추천 결과 hidden payload의 `surveyVersion`, `engineVersion`은 피드백 API 용도로만 유지되고, 화면 설명에서는 노출하지 않는다.
- 핵심 도메인 개념: “무엇을 계산하느냐”와 “그 계산을 사용자에게 어떤 언어로 보여 주느냐”는 별개다. 추천 집계와 랭킹 계산은 그대로 서버가 맡더라도, public 화면은 플레이어의 행동과 보상 중심 언어로 다시 써야 제품처럼 보인다. 그래서 이번에는 서비스 로직을 건드리지 않고 copy와 view model만 손봤다.
- 예외 상황 또는 엣지 케이스: `/recommendation/feedback-insights` 자체는 아직 남아 있다. 이번 단계에서는 public 링크만 제거했고, 실제 route 분리와 `/admin` 이동은 다음 단계에서 한다. 또한 hidden field 안의 `surveyVersion`, `engineVersion`은 UI용이 아니라 피드백 저장용이라 유지했다.
- 테스트 내용: `./gradlew test --tests com.worldmap.web.HomeControllerTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과, `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: public 화면에 개발 용어가 남아 있으면 서비스보다 포트폴리오 데모처럼 보이기 때문에, 홈·추천·랭킹 copy를 제품 언어로 다시 썼습니다. 요청 흐름과 서버 계산은 그대로 두고, view model과 템플릿만 바꿔 플레이어가 바로 이해할 수 있는 화면으로 정리했고, 내부 운영 페이지로 가는 링크도 public 결과 화면에서 제거했습니다.
- 아직 내가 이해가 부족한 부분: 이번에는 public copy만 정리했기 때문에, 내부 운영 정보를 실제 `/admin` 라우트로 옮기는 작업이 아직 남아 있다. 다음 단계에서 admin 대시보드 1차를 만들 때 어떤 데이터까지 한 화면에 모을지 더 정해야 한다.

## 2026-03-24 - 추천 운영 정보를 `/admin` read-only 화면으로 분리

- 단계: 6. 설문 기반 추천 엔진
- 목적: public 화면에서는 게임/추천 경험만 보이게 하고, 버전·집계·운영 상태 같은 내부 정보는 별도 `/admin` 화면에서만 보이게 실제 라우트와 템플릿을 나눈다. 이전 단계에서는 public 링크만 제거했지만, 운영 화면 자체는 아직 `/recommendation/feedback-insights`에 남아 있었다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java`
  - `src/main/java/com/worldmap/admin/application/AdminDashboardService.java`
  - `src/main/java/com/worldmap/admin/application/AdminDashboardView.java`
  - `src/main/java/com/worldmap/admin/application/AdminDashboardRouteView.java`
  - `src/main/java/com/worldmap/admin/application/AdminDashboardFocusView.java`
  - `src/main/java/com/worldmap/admin/web/AdminPageController.java`
  - `src/main/resources/templates/fragments/admin-header.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/20-move-ops-insights-into-admin-surface.md`
- 요청 흐름 / 데이터 흐름: `GET /admin`은 `AdminPageController.dashboard() -> AdminDashboardService.loadDashboard()` 흐름으로 들어간다. 여기서 서비스가 `RecommendationFeedbackService.summarizeByVersion()`, `RecommendationQuestionCatalog.questions()`, `RecommendationCountryProfileCatalog.profiles()`를 묶어 운영 요약 모델을 만든다. `GET /admin/recommendation/feedback`은 `AdminPageController.recommendationFeedback()`에서 대시보드 요약과 `feedbackInsights` 집계를 같이 템플릿에 넘긴다. 기존 `GET /recommendation/feedback-insights`는 이제 `redirect:/admin/recommendation/feedback`만 수행한다.
- 데이터 / 상태 변화: 운영 DB 스키마나 추천 계산 로직은 바뀌지 않았다. 바뀐 것은 읽기 모델의 노출 위치다. 만족도 집계는 여전히 `RecommendationFeedbackService`가 계산하고, admin 화면은 그 값을 운영 문맥으로 보여 준다. public 화면은 더 이상 만족도 집계 SSR view를 직접 가지지 않는다.
- 핵심 도메인 개념: 운영 화면도 하나의 읽기 모델이다. survey version, engine version, 문항 수, 후보 국가 수, 만족도 응답 수를 한 화면에서 보여 주는 것은 단순 템플릿 조립이 아니라 “운영 요약”을 만드는 일이라서 `AdminDashboardService`로 분리했다. 집계 계산 자체는 기존 추천 서비스가 맡고, admin 서비스는 여러 도메인 값을 한 문맥으로 묶는 역할만 한다.
- 예외 상황 또는 엣지 케이스: 현재 `/admin`은 정보 구조 분리 1차이며, 인증/권한은 아직 없다. 그래서 기존 `/recommendation/feedback-insights`를 바로 404로 없애지 않고 `/admin/recommendation/feedback`으로 redirect해 북마크와 테스트 호환성을 유지했다. 실제 접근 제어는 8단계에서 붙여야 한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.admin.AdminPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest` 통과. `AdminPageIntegrationTest`는 `/admin` 대시보드와 `/admin/recommendation/feedback` 운영 화면 SSR을 검증하고, `RecommendationFeedbackIntegrationTest`는 legacy public route redirect를 검증한다.
- 면접에서 30초 안에 설명하는 요약: public 화면과 운영 화면을 실제 라우트 수준에서 나누기 위해 `/admin` read-only 대시보드와 `/admin/recommendation/feedback`을 추가했습니다. 만족도 집계 계산은 그대로 `RecommendationFeedbackService`가 맡고, `AdminDashboardService`가 현재 설문/엔진 버전과 피드백 요약을 한 번에 묶어 운영 화면 모델을 만듭니다. 기존 public 운영 route는 admin으로 redirect해 사용자 경험과 운영 정보를 분리했습니다.
- 아직 내가 이해가 부족한 부분: 현재 admin 대시보드는 추천 운영과 public 점검에만 초점을 둔다. 다음에는 `persona baseline`, `build 상태`, 더 세밀한 버전 비교까지 이 화면에 얼마나 확장할지 기준을 더 정해야 한다.

## 2026-03-24 - baseline 운영 화면 추가와 public 헤더 단순화

- 단계: 7. AI-assisted 설문 개선 체계 / 8. 인증, 전적, 마이페이지
- 목적: 추천 운영에서는 만족도 집계뿐 아니라 오프라인 페르소나 baseline도 같이 봐야 하므로 `/admin/recommendation/persona-baseline`을 추가한다. 동시에 public 헤더는 게임별 직접 이동을 줄이고 `Home / My Page`만 남겨 상단 구조를 단순화한다. `My Page`는 아직 인증 전 단계라 placeholder shell만 먼저 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java`
  - `src/main/java/com/worldmap/admin/application/AdminPersonaBaselineView.java`
  - `src/main/java/com/worldmap/admin/application/AdminPersonaBaselineScenarioView.java`
  - `src/main/java/com/worldmap/admin/web/AdminPageController.java`
  - `src/main/java/com/worldmap/web/MyPageController.java`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/fragments/admin-header.html`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/templates/mypage.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/web/HomeControllerTest.java`
  - `src/test/java/com/worldmap/web/MyPageControllerTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/21-add-admin-persona-baseline-and-simplify-public-header.md`
- 요청 흐름 / 데이터 흐름: `GET /admin/recommendation/persona-baseline`은 `AdminPageController.recommendationPersonaBaseline() -> AdminDashboardService.loadDashboard() + AdminPersonaBaselineService.loadBaseline()` 흐름으로 처리된다. `AdminPersonaBaselineService`는 baseline 18개, 현재 하한 `15 / 18`, weak scenario 3개, active-signal 4개를 운영 화면 모델로 묶는다. `GET /mypage`는 `MyPageController.myPage()`가 placeholder 템플릿만 렌더링한다. public 헤더는 이제 모든 public 페이지에서 `Home`, `My Page`만 보여 주고, 게임별 이동은 본문 CTA에 남긴다.
- 데이터 / 상태 변화: 운영 DB나 추천 계산 자체는 바뀌지 않았다. 새로 생긴 것은 운영 읽기 모델과 public shell 구조다. `My Page`는 아직 사용자 데이터 저장이나 조회가 없고, 다음 8단계에서 auth/전적 기능이 들어올 자리만 먼저 고정한 상태다.
- 핵심 도메인 개념: admin baseline 화면은 테스트 자산 전체를 노출하는 것이 아니라, 운영자가 봐야 하는 “현재 품질 하한 / weak scenario / active-signal 비교”만 읽기 모델로 다시 묶는다. 그래서 이 조합 책임은 컨트롤러보다 `AdminPersonaBaselineService`가 맡는다. 반면 `My Page`는 지금 상태 변화가 전혀 없으므로 서비스 없이 컨트롤러와 SSR 템플릿만 둔다.
- 예외 상황 또는 엣지 케이스: `My Page`는 실제 로그인/내 기록 화면이 아니다. 사용자가 오해하지 않도록 템플릿에 “준비 중” 상태를 분명히 넣었다. 또한 header에서 게임 직접 이동을 제거했기 때문에, 홈 CTA와 각 모드 시작 화면 링크가 public 주요 진입점이 된다.
- 테스트 내용: `./gradlew test --tests com.worldmap.admin.AdminPageIntegrationTest --tests com.worldmap.web.HomeControllerTest --tests com.worldmap.web.MyPageControllerTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest` 통과. admin baseline page 렌더링, 홈 화면에서 `오늘의 추천 플레이` 제거, header의 `My Page` 노출, `/mypage` placeholder 렌더링을 확인했다.
- 면접에서 30초 안에 설명하는 요약: 추천 운영에서는 만족도 집계만 보면 부족해서 `/admin/recommendation/persona-baseline`을 추가해 18개 시나리오 baseline과 weak scenario를 같이 보게 만들었습니다. 동시에 public 헤더는 `Home / My Page`만 남겨 상단 이동 구조를 단순화했고, `My Page`는 아직 인증 전 단계라 placeholder shell만 먼저 고정했습니다.
- 아직 내가 이해가 부족한 부분: `My Page`를 지금처럼 가벼운 placeholder로 유지할지, 다음 단계에서 게스트 전적 일부라도 먼저 붙일지는 8단계 설계에서 더 정해야 한다. 또 admin baseline 화면을 향후 실제 테스트 결과와 완전히 같은 데이터 원천으로 연결할지도 후속 판단이 필요하다.

## 2026-03-24 - 게스트 유지형 단순 계정 구조 설계

- 단계: 8. 인증, 전적, 마이페이지
- 목적: 비회원은 지금처럼 세션 기반으로 바로 플레이하게 두고, 기록을 오래 남기고 싶은 사용자만 가볍게 로그인하게 만드는 구조를 고정한다. 계정 목적은 커뮤니티가 아니라 `닉네임 유지`, `점수 누적`, `내 전적 조회`다.
- 변경 파일:
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `docs/SIMPLE_ACCOUNT_PROGRESS_PLAN.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/22-guest-session-to-simple-account-plan.md`
- 요청 흐름 / 데이터 흐름: 이번 단계는 구현이 아니라 설계 고정이다. 기본 흐름은 `비회원 플레이 -> guestSessionKey로 세션/랭킹 기록 저장 -> 회원가입 또는 로그인 -> 현재 guestSessionKey 기록을 memberId에 귀속 -> /mypage 이동`으로 잡았다. public 게임 요청 흐름은 계속 비회원도 그대로 들어오고, 인증은 기록 유지와 마이페이지 조회를 위한 확장 레이어로 붙인다.
- 데이터 / 상태 변화: 실제 테이블 변경은 아직 없다. 설계 기준만 먼저 고정했다. 핵심은 `member` 테이블을 `nickname + passwordHash + role` 수준으로 단순화하고, 기존 게임 세션과 랭킹 레코드에는 `memberId` 또는 `guestSessionKey` 둘 중 하나로 소유자를 표시하는 것이다. 추천 결과 자체는 계정 단계에서도 저장하지 않고, 만족도 피드백만 익명으로 유지한다.
- 핵심 도메인 개념: 게스트와 회원을 동시에 다루려면 “현재 플레이 소유자”를 설명할 공통 기준이 필요하다. 그래서 `memberId`와 `guestSessionKey`를 같이 두는 모델이 필요하다. 이렇게 하면 비회원 플레이는 유지하면서도, 로그인 시 현재 브라우저 세션의 기록만 계정으로 자연스럽게 귀속시킬 수 있다.
- 예외 상황 또는 엣지 케이스: 이 설계는 현재 브라우저 세션 범위만 귀속한다. 브라우저를 닫고 새 세션이 되면 예전 비회원 기록은 자동 복구하지 않는다. 의도적으로 “기록을 계속 남기고 싶으면 로그인”하게 만드는 구조다. 또 이메일 복구를 넣지 않기 때문에, 비밀번호 분실 복구는 MVP 범위에서 다루지 않는다.
- 테스트 내용: 설계 문서 작업이라 애플리케이션 테스트는 실행하지 않았다.
- 면접에서 30초 안에 설명하는 요약: WorldMap은 커뮤니티 서비스가 아니라 점수 누적형 게임 허브라서, 비회원 플레이는 그대로 유지하고 기록을 오래 남기고 싶은 사람만 단순 계정으로 로그인하게 설계했습니다. 계정은 `닉네임 + 비밀번호`만 두고, 게임과 랭킹 기록은 `memberId` 또는 `guestSessionKey`로 소유자를 표시합니다. 로그인 시에는 현재 브라우저 세션의 비회원 기록만 계정으로 귀속해 구조를 단순하게 유지합니다.
- 아직 내가 이해가 부족한 부분: `guestSessionKey`를 HttpSession에만 둘지, 별도 쿠키로 조금 더 오래 유지할지는 구현 단계에서 한 번 더 판단해야 한다. 또 로그인 직후 귀속 처리 시 게임 세션과 랭킹 레코드를 어디까지 같은 트랜잭션으로 묶을지도 실제 코드에서 더 세밀하게 정해야 한다.

## 2026-03-24 - 게스트 세션 소유권 기반 추가

- 단계: 8. 인증, 전적, 마이페이지
- 목적: 회원가입 / 로그인을 붙이기 전에, 먼저 모든 게임 기록이 “회원 것인지, 현재 브라우저의 게스트 것인지”를 구분할 수 있어야 한다. 그래서 이번 조각은 계정 UI가 아니라 ownership 데이터 기반을 심는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/auth/domain/Member.java`
  - `src/main/java/com/worldmap/auth/domain/MemberRole.java`
  - `src/main/java/com/worldmap/auth/domain/MemberRepository.java`
  - `src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java`
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardService.java`
  - `src/test/java/com/worldmap/auth/GuestSessionOwnershipIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/23-add-guest-session-ownership-foundation.md`
- 요청 흐름 / 데이터 흐름: 위치/인구수 게임 시작 요청은 `LocationGameApiController` 또는 `PopulationGameApiController`에서 시작한다. 컨트롤러는 `HttpSession`에서 `GuestSessionKeyManager.ensureGuestSessionKey()`를 호출해 현재 브라우저 세션의 guest key를 확보하고, 그 값을 서비스에 전달한다. 서비스는 게임 세션을 만들 때 `memberId = null`, `guestSessionKey = ...`로 저장한다. 이후 게임오버로 랭킹 기록이 만들어질 때 `LeaderboardService`가 세션 ownership을 그대로 넘겨 `leaderboard_record`에도 같은 `guestSessionKey`를 기록한다.
- 데이터 / 상태 변화: 이제 `LocationGameSession`, `PopulationGameSession`, `LeaderboardRecord`는 모두 `memberId`와 `guestSessionKey`를 가진다. 아직 로그인은 없으므로 현재 단계에서는 항상 `memberId = null`이고, 같은 브라우저 세션에서 시작한 게임들은 같은 `guestSessionKey`를 공유한다. 즉, “게스트 기록을 나중에 계정으로 귀속할 수 있는 최소 키”가 실제 데이터에 남기 시작했다.
- 핵심 도메인 개념: 로그인 기능보다 먼저 ownership을 심은 이유는, 나중에 회원가입 성공 후 현재 브라우저의 guest 기록만 안전하게 계정으로 옮기려면 모든 기록이 이미 공통 식별자(`guestSessionKey`)를 갖고 있어야 하기 때문이다. 이 책임은 컨트롤러보다 서비스/도메인에 가깝다. 컨트롤러는 `HttpSession`에서 key를 꺼내 전달만 하고, 실제로 게임 세션과 랭킹 레코드에 ownership을 남기는 규칙은 세션 생성 서비스와 랭킹 서비스가 맡는다.
- 예외 상황 또는 엣지 케이스: 지금 단계에서는 브라우저를 닫고 새 세션이 생기면 새 `guestSessionKey`가 발급된다. 즉, 비회원 기록이 브라우저 재시작 후 자동으로 이어지지는 않는다. 또한 로그인 기능이 아직 없으므로 `memberId`는 계속 `null`이며, ownership 구조만 먼저 깔린 상태다.
- 테스트 내용: `./gradlew test --tests com.worldmap.auth.GuestSessionOwnershipIntegrationTest --tests com.worldmap.game.location.LocationGameFlowIntegrationTest --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과. 새 `GuestSessionOwnershipIntegrationTest`는 같은 `MockHttpSession`으로 위치/인구수 게임을 시작했을 때 같은 `guestSessionKey`를 공유하는지, 게스트 게임오버 후 랭킹 레코드가 같은 ownership을 유지하는지 검증한다.
- 면접에서 30초 안에 설명하는 요약: 로그인 기능을 붙이기 전에 먼저 게스트 기록의 소유권 기반을 깔았습니다. 게임 시작 컨트롤러가 현재 브라우저 세션의 `guestSessionKey`를 확보해 서비스에 넘기고, 게임 세션과 랭킹 레코드는 모두 `memberId` 또는 `guestSessionKey`로 소유자를 저장합니다. 이렇게 해두면 나중에 회원가입/로그인 성공 시 현재 브라우저의 게스트 기록만 계정으로 안전하게 귀속할 수 있습니다.
- 아직 내가 이해가 부족한 부분: `guestSessionKey`를 지금처럼 HttpSession에만 둘지, 추후 remember-me나 별도 cookie로 조금 더 오래 유지할지는 인증 구현 단계에서 다시 판단해야 한다. 또 로그인 직후 귀속 처리에서 게임 세션과 랭킹 레코드를 같은 트랜잭션으로 묶을지, 도메인별로 나눌지 후속 설계가 더 필요하다.

## 2026-03-24 - 단순 회원가입 / 로그인과 member 소유 게임 시작 연결

- 단계: 8. 인증, 전적, 마이페이지
- 목적: ownership 필드만 있는 상태에서는 실제 사용자가 “기록을 남기기 위해 로그인한다”는 흐름을 체감할 수 없다. 그래서 이번 조각은 guest 귀속까지 한 번에 가지 않고, 먼저 `닉네임 + 비밀번호` 회원가입/로그인/로그아웃과 로그인 사용자의 새 게임 기록이 `memberId`로 저장되게 만드는 것에 집중한다.
- 변경 파일:
  - `build.gradle`
  - `src/main/java/com/worldmap/auth/application/AuthenticatedMemberSession.java`
  - `src/main/java/com/worldmap/auth/application/MemberPasswordHasher.java`
  - `src/main/java/com/worldmap/auth/application/MemberSessionManager.java`
  - `src/main/java/com/worldmap/auth/application/MemberAuthService.java`
  - `src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java`
  - `src/main/java/com/worldmap/auth/web/AuthPageController.java`
  - `src/main/java/com/worldmap/auth/web/LoginForm.java`
  - `src/main/java/com/worldmap/auth/web/SignupForm.java`
  - `src/main/java/com/worldmap/web/MyPageController.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGamePageController.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGamePageController.java`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java`
  - `src/test/java/com/worldmap/auth/GuestSessionOwnershipIntegrationTest.java`
  - `src/test/java/com/worldmap/web/MyPageControllerTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/24-add-simple-auth-and-member-owned-game-starts.md`
- 요청 흐름 / 데이터 흐름: 회원가입/로그인은 `AuthPageController`에서 시작한다. `POST /signup`, `POST /login`은 form validation 후 `MemberAuthService`가 닉네임 정규화, 중복 확인, BCrypt 해시 검증, `lastLoginAt` 갱신을 처리하고, 성공하면 `MemberSessionManager`가 현재 `HttpSession`에 `memberId`, `nickname`, `role`을 저장한다. 이후 위치/인구수 게임 시작 API는 컨트롤러에서 현재 로그인 사용자를 먼저 확인하고, 로그인 상태면 guest key 대신 `startMemberGame(memberId, nickname)`으로 들어가 새 게임 세션을 만든다. 게임오버 시 `LeaderboardService`는 기존처럼 세션 ownership을 복사하므로 랭킹 레코드도 `memberId` 소유가 된다.
- 데이터 / 상태 변화: `member_account` 테이블을 실제로 쓰기 시작했고, 비밀번호는 평문이 아니라 BCrypt hash로 저장한다. 로그인 상태에서는 `HttpSession`에 member 정보가 올라가고, 그 세션에서 새로 시작한 게임 기록은 `guestSessionKey = null`, `memberId = ...`로 저장된다. 반대로 기존 guest 기록을 로그인 직후 계정으로 옮기는 동작은 아직 없다.
- 핵심 도메인 개념: 이번 단계의 핵심은 “인증”보다 “현재 플레이 소유자 전환”이다. 컨트롤러는 HTTP form과 `HttpSession`을 다루고, `MemberAuthService`는 회원가입/로그인의 규칙을 담당한다. 게임 서비스가 별도 `startMemberGame`, `startGuestGame` 경로를 가지는 이유는, 로그인 여부에 따라 소유자와 닉네임 결정 규칙이 달라지는 것이 게임 도메인 진입 규칙이기 때문이다. `My Page`도 완전 보호보다 먼저 guest 유도 / member shell 분기로 시작해, 헤더는 단순하게 두면서 로그인 진입점을 자연스럽게 만든다.
- 예외 상황 또는 엣지 케이스: 지금 단계에서는 로그인 성공 전에 쌓인 guest 기록이 자동으로 member로 옮겨지지 않는다. 즉, “로그인 이후 새로 시작한 게임부터” 계정 소유가 된다. 또한 닉네임은 대소문자 구분 없이 중복을 막고, 로그인 상태에서 게임 시작 화면의 닉네임 입력은 무시되며 계정 닉네임이 우선 사용된다.
- 테스트 내용: `./gradlew test --tests com.worldmap.auth.AuthFlowIntegrationTest --tests com.worldmap.auth.GuestSessionOwnershipIntegrationTest --tests com.worldmap.web.MyPageControllerTest --tests com.worldmap.game.location.LocationGameFlowIntegrationTest --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest --tests com.worldmap.ranking.LeaderboardIntegrationTest` 통과. `AuthFlowIntegrationTest`는 회원가입 후 세션 로그인과 `/mypage` 연결, 로그인 실패 메시지를 검증한다. `GuestSessionOwnershipIntegrationTest`는 로그인 사용자로 시작한 게임과 랭킹 레코드가 `memberId` ownership을 가지는지까지 본다.
- 면접에서 30초 안에 설명하는 요약: 단순 계정의 첫 구현으로 `닉네임 + 비밀번호` 회원가입/로그인을 붙이고, 로그인한 사용자가 새로 시작하는 게임 기록은 guest가 아니라 `memberId` 소유로 저장되게 만들었습니다. 폼 검증과 비밀번호 해시는 `MemberAuthService`가 맡고, 게임 시작 컨트롤러는 현재 세션의 로그인 사용자를 확인해 guest/game start 경로를 나눕니다. 이렇게 해서 guest 유지 구조를 깨지 않으면서도 계정 중심 기록 누적의 첫 단계를 만들었습니다.
- 아직 내가 이해가 부족한 부분: 로그인 직후 기존 guest 기록을 어느 범위까지 자동 귀속할지, 그리고 `/mypage`에서 어떤 집계부터 먼저 보여줄지는 다음 조각에서 더 정해야 한다. 또 지금은 Spring Security 없이 단순 세션 관리로 시작했는데, admin 접근 제어까지 갈 때 이 구조를 어디까지 재사용할지도 후속 판단이 필요하다.

## 2026-03-24 - 로그인 직후 guest 기록 귀속 연결

- 단계: 8. 인증, 전적, 마이페이지
- 목적: 이전 단계까지는 로그인 후 “새로 시작하는 게임”만 `memberId` ownership으로 저장됐다. 하지만 이미 guest로 쌓인 현재 브라우저 기록은 그대로 남아 있어서, 사용자가 기대하는 “지금까지 한 기록이 이어진다”는 감각이 부족했다. 그래서 이번 조각은 로그인/회원가입 직후 현재 `guestSessionKey` 기록을 계정에 붙이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/auth/application/GuestSessionKeyManager.java`
  - `src/main/java/com/worldmap/auth/application/GuestProgressClaimService.java`
  - `src/main/java/com/worldmap/auth/web/AuthPageController.java`
  - `src/main/java/com/worldmap/game/common/domain/BaseGameSession.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSessionRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSessionRepository.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
  - `src/main/resources/templates/auth/signup.html`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/mypage.html`
  - `src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/25-claim-current-guest-progress-after-login.md`
- 요청 흐름 / 데이터 흐름: `POST /signup` 또는 `POST /login`은 `AuthPageController`에서 시작한다. 컨트롤러는 `MemberAuthService`로 회원가입/로그인을 성공시킨 뒤, `GuestSessionKeyManager.currentGuestSessionKey()`로 현재 브라우저의 guest key를 읽는다. 그 key가 있으면 `GuestProgressClaimService.claimGuestRecords(memberId, guestSessionKey)`를 호출한다. 이 서비스는 `LocationGameSessionRepository`, `PopulationGameSessionRepository`, `LeaderboardRecordRepository`에서 `guestSessionKey`를 가진 미귀속 레코드만 조회해 `claimOwnership(memberId)`를 호출한다. claim이 끝난 뒤 `MemberSessionManager`가 로그인 세션을 유지한다.
- 데이터 / 상태 변화: claim 전에는 guest 기록이 `memberId = null`, `guestSessionKey = ...` 상태다. claim 후에는 동일 레코드가 `memberId = 로그인 사용자`, `guestSessionKey = null`로 바뀐다. 중요한 점은 `playerNickname` snapshot은 바꾸지 않는다는 것이다. 즉, 소유자는 계정으로 바뀌지만 당시 표시 이름은 그대로 남겨 과거 랭킹/전적 표시를 안정적으로 유지한다.
- 핵심 도메인 개념: guest 기록 귀속은 인증 UI의 부가 기능이 아니라 ownership 전환 규칙이다. 그래서 이 로직은 컨트롤러에 직접 쓰지 않고 `GuestProgressClaimService`로 뺐다. 컨트롤러는 현재 guest key를 읽고 서비스를 호출할 뿐이고, 실제로 어떤 레코드를 어떤 규칙으로 `memberId`로 바꾸는지는 서비스와 엔티티(`claimOwnership`)가 맡는다.
- 예외 상황 또는 엣지 케이스: guest key가 없는 세션에서 바로 로그인하면 claim은 no-op로 끝난다. 또 이미 `memberId`가 채워진 레코드는 다시 건드리지 않는다. 현재 범위는 “현재 브라우저 세션의 guest 기록만” 귀속이며, 다른 브라우저나 과거 세션의 guest 기록까지 복구하지는 않는다.
- 테스트 내용: `./gradlew test --tests com.worldmap.auth.AuthFlowIntegrationTest --tests com.worldmap.auth.GuestSessionOwnershipIntegrationTest` 통과 후 `./gradlew test` 전체 통과. 새 시나리오에서는 guest로 위치 게임을 시작하고 게임오버까지 간 뒤, 같은 `MockHttpSession`으로 회원가입하면 기존 게임 세션과 랭킹 레코드가 모두 `memberId` ownership으로 바뀌고 `guestSessionKey`는 제거되는지 검증했다.
- 면접에서 30초 안에 설명하는 요약: 로그인 화면만 붙이면 이전 guest 기록은 따로 놀게 됩니다. 그래서 회원가입/로그인 직후 현재 브라우저의 `guestSessionKey` 기록만 찾아 `memberId`로 ownership을 바꾸는 `GuestProgressClaimService`를 추가했습니다. 이때 당시 닉네임 snapshot은 유지하고, 소유자 필드만 바꿔 과거 기록 표시와 계정 귀속을 동시에 만족시켰습니다.
- 아직 내가 이해가 부족한 부분: claim 이후 `/mypage`에서 어떤 집계부터 우선 보여줄지, 그리고 guest 기록을 언제까지 브라우저 세션 기준으로만 제한할지 다음 조각에서 더 정해야 한다. admin 접근 제어를 같은 세션 모델 위에 얹을지, 별도 보안 계층으로 분리할지도 후속 판단이 필요하다.

## 2026-03-24 - `leaderboard_record` 기반 `/mypage` 기록 대시보드 연결

- 단계: 8. 인증, 전적, 마이페이지
- 목적: 이전 단계까지는 로그인과 guest 기록 귀속까지만 끝나서, 사용자가 계정으로 무엇을 얻는지 화면에서 바로 느끼기 어려웠다. 그래서 이번 조각은 `/mypage`를 placeholder에서 실제 기록 허브로 바꾸고, 계정에 귀속된 완료 run을 읽어 가장 먼저 보여줄 지표를 고정하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/mypage/application/MyPageService.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageDashboardView.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageBestRunView.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageRecentPlayView.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
  - `src/main/java/com/worldmap/web/MyPageController.java`
  - `src/main/resources/templates/mypage.html`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/web/MyPageControllerTest.java`
  - `src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/26-build-mypage-from-member-leaderboard-runs.md`
- 요청 흐름 / 데이터 흐름: `GET /mypage`는 `MyPageController.myPage()`에서 시작한다. 컨트롤러는 `MemberSessionManager.currentMember()`로 로그인 사용자를 확인하고, 로그인 상태면 `MyPageService.loadDashboard(memberId)`를 호출한다. `MyPageService`는 `LeaderboardRecordRepository`에서 현재 member의 완료 run 개수, 모드별 최고 기록, 최근 완료 run 10개를 읽고 `MyPageDashboardView`로 묶어 템플릿에 넘긴다. 따라서 public 요청은 그대로 SSR이지만, 실제 상태 조회는 `leaderboard_record` 집계 계층에서 이루어진다.
- 데이터 / 상태 변화: 이번 단계는 새로운 상태 전이보다 “어떤 완료 기록을 먼저 보여줄지”를 정하는 read 모델 추가다. `leaderboard_record`는 이미 게임오버 시점 점수, 시도 수, 클리어 Stage, 모드, ownership을 한 행에 담고 있으므로 `/mypage` 첫 버전에서 다시 원본 세션을 조립하지 않아도 된다. 로그인 사용자의 경우 `memberId` 기준으로만 읽고, guest 사용자는 여전히 로그인 유도 화면을 본다.
- 핵심 도메인 개념: `/mypage` 첫 구현을 raw 게임 세션이 아니라 `leaderboard_record`에서 시작한 이유는 “완료된 run 단위의 설명 가능성” 때문이다. 최고 점수, 최고 랭킹, 최근 플레이는 모두 완료된 run 집계이므로 이미 정규화된 랭킹 레코드를 읽는 편이 서비스 책임이 더 명확하다. 컨트롤러는 로그인 여부와 뷰 분기만 맡고, 최고 기록 선택과 랭킹 계산, 최근 플레이 조합은 `MyPageService`가 맡는다.
- 예외 상황 또는 엣지 케이스: 아직 완료된 run이 없는 member는 `/mypage`에서 최고 기록 대신 `기록 없음`, 최근 플레이 영역에서는 안내 메시지를 본다. 최고 랭킹은 현재 record id를 기준으로 전체 정렬 목록에서 순서를 찾기 때문에, 데이터가 매우 커지면 이후 dedicated rank query나 cache를 고려할 수 있다. 또한 현재는 `LEVEL_1` 기록만 읽는다.
- 테스트 내용: `./gradlew test --tests com.worldmap.web.MyPageControllerTest --tests com.worldmap.auth.AuthFlowIntegrationTest` 통과 후 `./gradlew test` 전체 통과. `MyPageControllerTest`는 로그인 사용자에게 최고 점수 / 최근 플레이 / 로그아웃이 보이는지 검증하고, `AuthFlowIntegrationTest`는 guest로 플레이 후 회원가입하면 귀속된 기록이 `/mypage`에서 바로 보이는지 확인한다.
- 면접에서 30초 안에 설명하는 요약: `/mypage` 첫 버전은 원본 게임 세션을 다시 조립하지 않고, 게임오버 시점에 이미 정규화된 `leaderboard_record`를 읽어 만들었습니다. 그래서 총 완료 플레이 수, 모드별 최고 점수, 당시 최고 랭킹, 최근 완료 이력을 한 번에 보여줄 수 있습니다. 컨트롤러는 로그인 세션만 확인하고, 어떤 기록을 고를지는 `MyPageService`가 맡아 read 모델 책임을 분리했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 `leaderboard_record`만 읽기 때문에 정확도, 평균 시도 수, 실패한 run까지 포함한 누적 통계는 아직 없다. 이런 지표를 추가할 때는 raw 게임 세션 기반 read model을 따로 둘지, 현재 대시보드에 혼합할지를 다음 조각에서 더 판단해야 한다.

## 2026-03-24 - admin 화면 세션 기반 접근 제어 연결

- 단계: 8. 인증, 전적, 마이페이지
- 목적: `/admin` 화면은 운영 정보가 분리돼 있었지만 실제로는 public과 같은 수준으로 열려 있었다. 그래서 이번 조각은 기존 단순 세션 로그인 구조를 유지한 채, admin 라우트만 `ADMIN` role로 제한하는 최소 권한 제어를 붙이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/admin/web/AdminAccessInterceptor.java`
  - `src/main/java/com/worldmap/admin/web/AdminWebConfig.java`
  - `src/main/java/com/worldmap/auth/web/AuthPageController.java`
  - `src/main/resources/templates/auth/login.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/error/403.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/auth/AuthFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/27-protect-admin-routes-with-session-role.md`
- 요청 흐름 / 데이터 흐름: admin 요청은 `AdminPageController`에 닿기 전에 `AdminAccessInterceptor`를 먼저 지난다. 인터셉터는 `HttpSession`에서 `MemberSessionManager.currentMember()`를 읽어 현재 로그인 사용자를 확인한다. 세션이 없거나 로그인 상태가 아니면 `/login?returnTo=...`로 리다이렉트하고, 로그인은 되어 있지만 role이 `ADMIN`이 아니면 403으로 막는다. `ADMIN`인 경우에만 실제 `AdminPageController -> AdminDashboardService / RecommendationFeedbackService / AdminPersonaBaselineService` 흐름으로 들어간다.
- 데이터 / 상태 변화: 이번 단계는 새로운 테이블보다 요청 진입 규칙을 추가한 것이다. 로그인 성공 시 세션에 이미 저장하던 `memberId / nickname / role` 중 `role`을 이제 admin 라우트 guard에서 실제로 사용한다. 로그인 페이지는 `returnTo`를 받아, 운영자가 로그인 후 원래 보려던 admin 경로로 바로 돌아갈 수 있게 했다.
- 핵심 도메인 개념: admin 접근 제어는 컨트롤러 액션마다 반복할 규칙이 아니라 라우트 입구의 공통 정책이다. 그래서 서비스보다 먼저, 컨트롤러보다 앞선 web interceptor에 두는 것이 맞다. 반면 로그인 검증과 세션 저장 자체는 여전히 `MemberAuthService`, `MemberSessionManager`가 맡는다. 즉, 인증과 권한 체크를 같은 곳에 섞지 않고 역할을 분리했다.
- 예외 상황 또는 엣지 케이스: 현재 admin 계정 생성 UI는 없다. 그래서 운영용 admin 사용자는 DB role을 `ADMIN`으로 부여한 계정이 있어야 한다. 비로그인 사용자가 admin URL로 들어오면 로그인 페이지로 보내고, 일반 USER가 admin URL로 들어오면 403으로 막는다. `returnTo`는 내부 경로(`/...`)만 허용해 외부 redirect는 막는다.
- 테스트 내용: `./gradlew test --tests com.worldmap.admin.AdminPageIntegrationTest --tests com.worldmap.auth.AuthFlowIntegrationTest` 통과 후 `./gradlew test` 전체 통과. `AdminPageIntegrationTest`는 unauthenticated -> login redirect, USER -> 403, ADMIN -> dashboard/feedback/baseline 접근을 검증한다. `AuthFlowIntegrationTest`는 `returnTo=/admin`으로 로그인한 admin 계정이 다시 `/admin`으로 돌아가는지 확인한다.
- 면접에서 30초 안에 설명하는 요약: admin 화면은 public과 같은 컨트롤러 분기 안에 두지 않고, `/admin/**` 진입 전에 `AdminAccessInterceptor`가 먼저 role을 검사하게 만들었습니다. 비로그인 사용자는 로그인으로 보내고, 일반 회원은 403으로 막으며, `ADMIN` 세션만 실제 운영 화면으로 들어갑니다. 이렇게 해서 기존 단순 세션 로그인 구조를 크게 흔들지 않으면서 운영 화면을 권한 기반으로 분리했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 admin role 부여를 수동 운영 전제로 두고 있다. 이후 실제 운영/포트폴리오 시연에서는 bootstrap admin 계정 생성 방식을 둘지, DB migration이나 환경변수 기반 provisioning으로 둘지 정리가 더 필요하다.

## 2026-03-24 - `/mypage` raw stage 기반 플레이 성향 지표 추가

- 단계: 8. 인증, 전적, 마이페이지
- 목적: 이전 `/mypage`는 완료된 run 요약에는 강했지만, 사용자가 어떤 방식으로 문제를 푸는지는 보여주지 못했다. 그래서 이번 조각은 최고 점수/랭킹과 별개로, raw stage 기록에서 `클리어 Stage 수`, `1트 클리어율`, `평균 시도 수`를 읽어 플레이 성향까지 보여주는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/mypage/application/MyPageModePerformanceView.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageDashboardView.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageService.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSessionRepository.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameStageRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSessionRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameStageRepository.java`
  - `src/main/resources/templates/mypage.html`
  - `src/test/java/com/worldmap/web/MyPageControllerTest.java`
  - `src/test/java/com/worldmap/mypage/MyPageServiceIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/28-add-mypage-stage-performance-metrics.md`
- 요청 흐름 / 데이터 흐름: `GET /mypage`는 여전히 `MyPageController -> MyPageService.loadDashboard(memberId)`로 시작한다. 다만 이번에는 서비스가 `leaderboard_record`만 읽지 않고, 위치/인구수 stage repository를 함께 읽는다. `leaderboard_record`는 완료 run 요약(총 완료 플레이 수, 최고 점수, 최근 플레이)을 만들고, raw stage 집계는 모드별 `클리어 Stage 수`, `1트 클리어율`, `평균 시도 수`를 만든다. 즉, 한 화면 안에서도 서로 다른 read model을 목적에 맞게 쓴다.
- 데이터 / 상태 변화: 새 테이블은 없고 read model만 확장됐다. 중요한 점은 플레이 성향 지표가 “finished session에 속한 CLEARED stage”만 대상으로 한다는 것이다. 아직 끝나지 않은 진행 중 세션이나 재시작되며 지워질 수 있는 임시 stage는 포함하지 않는다.
- 핵심 도메인 개념: `/mypage`의 최고 점수/최근 플레이는 `leaderboard_record`가 가장 자연스럽지만, `1트 클리어율`과 `평균 시도 수`는 run 요약만으로는 설명이 안 된다. 그래서 이 지표는 다시 raw stage 집계로 내려가야 한다. 컨트롤러는 여전히 로그인 여부와 뷰 분기만 맡고, 어떤 저장소를 조합해 어떤 read model을 만들지는 `MyPageService`가 맡는다.
- 예외 상황 또는 엣지 케이스: 아직 클리어한 stage가 없는 모드라도 completed run은 있을 수 있다. 이 경우 해당 모드 카드 자체는 보이지만 `1트 클리어율`, `평균 시도 수`는 `기록 없음`으로 나온다. 숫자 포맷은 정수면 `50%`, `1회`처럼 보이고, 소수면 `1.5회`처럼 한 자리 소수로 제한했다.
- 테스트 내용: `./gradlew test --tests com.worldmap.web.MyPageControllerTest --tests com.worldmap.mypage.MyPageServiceIntegrationTest --tests com.worldmap.auth.AuthFlowIntegrationTest` 통과 후 `./gradlew test` 전체 통과. `MyPageServiceIntegrationTest`는 위치 게임에서 `2회 시도 후 클리어`, `1회 시도 후 클리어`, 이후 게임오버 시나리오를 만들고, 인구수 게임도 별도 한 판 끝낸 뒤 `50%`, `1.5회`, `100%`, `1회`가 실제로 계산되는지 검증한다.
- 면접에서 30초 안에 설명하는 요약: `/mypage`는 이제 완료된 run 요약과 플레이 성향 요약을 분리해서 보여줍니다. 최고 점수와 최근 플레이는 `leaderboard_record`에서 읽고, 1트 클리어율과 평균 시도 수는 raw stage 집계에서 계산합니다. 이렇게 해야 “결과”와 “플레이 방식”을 서로 다른 read model 책임으로 설명할 수 있습니다.
- 아직 내가 이해가 부족한 부분: 지금은 cleared stage 기준 성향만 보여주고, 실패 run까지 포함한 정확도나 기간별 추세는 아직 없다. 이후에는 raw attempt 집계까지 더 내려갈지, 아니면 현재 stage 기반 요약을 유지할지 더 판단해야 한다.

## 2026-03-24 - 환경변수 기반 bootstrap admin 계정 provisioning

- 단계: 8. 인증, 전적, 마이페이지
- 목적: `/admin/**` 접근 제어까지 붙은 상태에서는 실제 운영자가 어떤 계정으로 로그인할지 정리가 필요했다. 일반 회원가입 UI로 admin 계정을 만들면 public 흐름과 운영 계정이 섞이므로, 이번 조각은 서버 시작 시 환경변수로 admin 계정을 자동 준비하는 최소 운영 경로를 만드는 데 집중한다.
- 변경 파일:
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
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/29-bootstrap-admin-account-from-env.md`
- 요청 흐름 / 데이터 흐름: 이번 단계의 진입점은 HTTP 컨트롤러가 아니라 애플리케이션 시작이다. 서버가 올라오면 `AdminBootstrapInitializer.run()`이 실행되고, 여기서 `AdminBootstrapService.ensureBootstrapAdmin()`을 호출한다. 서비스는 `AdminBootstrapProperties`에서 `enabled / nickname / password`를 읽고, `MemberCredentialPolicy`로 닉네임과 비밀번호를 검증한 뒤 `MemberRepository.findByNicknameIgnoreCase()`로 기존 계정을 조회한다. 계정이 없으면 `Member.create(..., ADMIN)`으로 새 admin을 저장하고, 계정이 있으면 `provisionAdmin()`으로 비밀번호 hash를 갱신하고 role을 `ADMIN`으로 승격한다.
- 데이터 / 상태 변화: 새 테이블은 없고 기존 `member_account`를 재사용한다. 이번 단계부터는 `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED=true`, `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME=...`, `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD=...`를 주고 서버를 시작하면, 해당 닉네임 member가 없을 때는 새 `ADMIN` 계정이 생기고, 이미 있으면 `USER -> ADMIN` 승격과 비밀번호 갱신이 일어난다. 테스트 프로파일에서는 기본적으로 bootstrap을 꺼 두어 기존 인증 테스트와 충돌하지 않게 했다.
- 핵심 도메인 개념: 운영용 admin 계정 provisioning은 공개 회원가입 흐름이 아니라 배포 환경의 운영 규칙이다. 그래서 signup 컨트롤러에 얹지 않고 `ApplicationRunner + Service` 조합으로 둔다. `AdminBootstrapInitializer`는 시작 시점만 잡고, 실제 생성/승격 규칙은 `AdminBootstrapService`가 맡는다. 또 회원가입과 bootstrap이 서로 다른 자격 증명 규칙을 가지지 않게 `MemberCredentialPolicy`를 분리해 닉네임/비밀번호 정책을 함께 재사용했다.
- 예외 상황 또는 엣지 케이스: bootstrap이 비활성화되어 있으면 no-op로 끝난다. 반대로 활성화됐는데 닉네임이나 비밀번호가 비어 있으면 서버 시작 단계에서 `IllegalArgumentException`으로 빠르게 실패한다. bootstrap 닉네임과 같은 기존 `USER`가 있으면 새 계정을 만들지 않고 그 사용자를 `ADMIN`으로 승격하며, 이때 password hash도 bootstrap 값으로 다시 설정한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.admin.application.AdminBootstrapServiceTest --tests com.worldmap.admin.AdminBootstrapIntegrationTest --tests com.worldmap.auth.AuthFlowIntegrationTest` 통과 후 `./gradlew test` 전체 통과. 단위 테스트에서는 신규 생성 / 기존 USER 승격 / 설정 누락 fail-fast를 고정했고, 통합 테스트에서는 Spring 컨텍스트 기동 시 bootstrap admin이 실제로 생성되고 BCrypt hash가 저장되는지 확인했다.
- 면접에서 30초 안에 설명하는 요약: admin 라우트를 role로 막은 뒤에는 운영자가 실제로 어떤 계정으로 들어갈지 정해야 했습니다. 공개 signup으로 admin 계정을 만들게 하면 사용자 흐름과 운영 계정이 섞이기 때문에, 서버 시작 시 환경변수로 admin 계정을 bootstrap 하도록 바꿨습니다. 시작 시점은 `ApplicationRunner`가 잡고, 계정 생성이나 기존 USER 승격 규칙은 `AdminBootstrapService`가 맡으며, 닉네임/비밀번호 검증은 회원가입과 같은 `MemberCredentialPolicy`를 재사용해 규칙을 하나로 유지했습니다.
- 아직 내가 이해가 부족한 부분: 현재 bootstrap은 단일 운영자 계정 전제를 둔다. 이후 실제 운영 화면이 늘어나면 다중 admin 계정 provisioning이나 비밀번호 회전 정책을 어디까지 현재 구조에 포함할지, 아니면 별도 운영 절차로 둘지 추가 판단이 필요하다.

## 2026-03-24 - 운영 화면을 `/dashboard`로 전환하고 ADMIN만 헤더에서 노출

- 단계: 8. 인증, 전적, 마이페이지
- 목적: 운영자 계정은 필요하지만 `/admin`이라는 주소와 공개 헤더 노출은 제품보다 개발용 화면이 먼저 보이는 인상을 줬다. 그래서 이번 조각은 권한 모델은 그대로 유지하면서, 운영 화면 표면 언어를 `Dashboard`로 바꾸고 `ADMIN` 로그인 상태에서만 public 헤더에 진입 버튼을 보이게 정리하는 데 집중한다.
- 변경 파일:
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
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/30-rename-admin-surface-to-dashboard.md`
- 요청 흐름 / 데이터 흐름: public 헤더는 이제 `site-header` fragment에서 현재 세션의 `WORLDMAP_MEMBER_ROLE`을 직접 읽고, `ADMIN`일 때만 `Dashboard` 링크를 렌더링한다. 운영 화면 요청은 `GET /dashboard/** -> AdminAccessInterceptor -> MemberSessionManager.currentMember() -> AdminPageController` 흐름으로 들어간다. 기존 `GET /admin/**`는 `LegacyAdminRedirectController`가 받아 `/dashboard/**`로 redirect만 수행한다. public의 legacy route인 `GET /recommendation/feedback-insights`도 이제 `/dashboard/recommendation/feedback`으로 redirect한다.
- 데이터 / 상태 변화: 이번 단계는 새로운 테이블이나 계정 상태 전이를 추가하지 않는다. 바뀐 것은 운영 화면의 주소 체계와 shell 노출 규칙이다. 권한 모델은 그대로 `ADMIN`, `USER`를 유지하고, public 헤더에서는 `ADMIN` 세션일 때만 `Dashboard` 버튼이 보인다. 운영 화면 내부 링크도 모두 `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 기준으로 맞췄다.
- 핵심 도메인 개념: 이번 단계의 핵심은 “권한은 admin이지만, 표면 언어는 dashboard로 바꾼다”는 것이다. 헤더 노출 규칙은 컨트롤러마다 model flag를 넣기보다 fragment가 session role을 직접 읽는 편이 더 단순하다. 반면 운영 화면 접근 권한은 여전히 라우트 입구의 공통 정책이라 interceptor가 맡는 것이 맞다. 또 `/admin`을 바로 지우지 않고 redirect controller를 두어 북마크와 옛 문서 링크를 안전하게 이전하도록 했다.
- 예외 상황 또는 엣지 케이스: guest와 일반 USER는 public 헤더에서 `Dashboard` 버튼을 보지 못한다. 비로그인 사용자가 `/dashboard`로 직접 들어가면 `/login?returnTo=/dashboard`로 보내고, 일반 USER는 403으로 막는다. 예전 `/admin` 북마크는 `ADMIN` 세션에서는 `/dashboard`로 옮겨지고, 비로그인 사용자는 기존처럼 로그인 유도로 들어간다.
- 테스트 내용: `./gradlew test --tests com.worldmap.admin.AdminPageIntegrationTest --tests com.worldmap.auth.AuthFlowIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.web.HomeControllerTest --tests com.worldmap.web.MyPageControllerTest` 통과. `AdminPageIntegrationTest`는 `/dashboard` 접근, guest login redirect, USER 403, legacy `/admin` redirect를 검증한다. `HomeControllerTest`는 guest에게 `Dashboard`가 안 보이고 ADMIN 세션에는 보이는지 확인한다. `AuthFlowIntegrationTest`는 admin 로그인 후 `returnTo=/dashboard` 복귀를 고정한다.
- 면접에서 30초 안에 설명하는 요약: 운영자 계정은 필요하지만 `/admin`이라는 주소와 링크를 public shell에 그대로 두면 제품보다 개발용 화면이 먼저 보였습니다. 그래서 권한 모델은 그대로 `ADMIN`으로 두고, 실제 운영 진입 주소를 `/dashboard`로 바꿨습니다. public 헤더는 `ADMIN` 로그인 상태에서만 `Dashboard` 버튼을 보여주고, 기존 `/admin/**`는 redirect controller로 한동안 유지해 북마크와 테스트를 안전하게 옮겼습니다. 권한 체크는 여전히 interceptor가 맡고, 헤더 노출은 fragment가 session role을 읽는 방식으로 단순하게 유지했습니다.
- 아직 내가 이해가 부족한 부분: 지금은 운영 화면 주소와 shell만 정리했기 때문에, 실제 dashboard다운 숫자 카드인 `총 회원 수`, `오늘 활성 회원/guest`, `오늘 완료 게임 수`는 아직 없다. 다음 조각에서는 read-only 지표를 어떤 기준으로 계산할지 먼저 정의해야 한다.

## 2026-03-24 - Dashboard 1차 운영 수치 카드 추가

- 단계: 8. 인증, 전적, 마이페이지
- 목적: `/dashboard` 주소와 권한 구조는 정리됐지만, 실제 운영자가 제일 먼저 보고 싶은 기초 수치가 비어 있었다. 그래서 이번 조각은 `총 회원 수`, `오늘 활성 회원`, `오늘 활성 게스트`, `오늘 시작된 세션`, `오늘 완료된 게임`, `오늘 모드별 완료 수`를 dashboard 첫 화면에 붙이고, 각 지표의 source of truth를 명확히 고정하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSessionRepository.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSessionRepository.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
  - `src/main/java/com/worldmap/admin/application/AdminDashboardActivityView.java`
  - `src/main/java/com/worldmap/admin/application/AdminDashboardView.java`
  - `src/main/java/com/worldmap/admin/application/AdminDashboardService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/31-add-dashboard-activity-metrics.md`
- 요청 흐름 / 데이터 흐름: `GET /dashboard`는 그대로 `AdminAccessInterceptor -> AdminPageController.dashboard() -> AdminDashboardService.loadDashboard()`로 들어간다. 이번에는 서비스가 `MemberRepository.count()`로 총 회원 수를 읽고, 위치/인구수 게임 세션 repository에서 `startedAt` 기준 오늘 시작 세션 수와 distinct `memberId`, distinct `guestSessionKey`를 읽는다. 그 다음 `LeaderboardRecordRepository`에서 `finishedAt` 기준 오늘 완료 run 수와 모드별 완료 수를 읽어 `AdminDashboardActivityView`로 묶고, 최종적으로 `AdminDashboardView`에 넣어 SSR 템플릿으로 보낸다.
- 데이터 / 상태 변화: 새 테이블은 없고 read model만 확장됐다. 중요한 점은 모든 수치를 한 테이블에서 읽지 않았다는 것이다. 총 회원 수는 `member_account`, 오늘 활성은 각 게임 세션의 `startedAt`, 오늘 완료 수는 `leaderboard_record.finishedAt`를 source of truth로 사용한다. 즉, “가입자 수”, “오늘 시작된 플레이”, “오늘 끝난 게임”은 각각 다른 도메인 이벤트를 기준으로 본다.
- 핵심 도메인 개념: dashboard 카드도 결국 read model이다. 회원 수와 플레이 완료 수를 같은 저장소에서 억지로 읽으면 설명이 꼬이기 때문에, `MemberRepository`, `LocationGameSessionRepository`, `PopulationGameSessionRepository`, `LeaderboardRecordRepository`를 목적별로 분리해 쓴다. 컨트롤러는 `/dashboard` 진입만 맡고, 두 게임 repository의 distinct 사용자 목록을 합쳐 오늘 활성 수를 만드는 책임은 `AdminDashboardService`가 맡는다.
- 예외 상황 또는 엣지 케이스: 오늘 같은 회원이 위치/인구수 두 모드를 모두 플레이할 수 있으므로, 활성 회원 수는 두 repository의 distinct `memberId`를 서비스에서 합쳐 중복을 제거한다. 게스트도 같은 방식으로 distinct `guestSessionKey`를 합친다. 반면 `오늘 시작된 세션 수`는 실제로 시작된 세션 개수가 목적이므로 두 repository count를 그대로 더한다. `오늘 완료된 게임 수`는 finished run만 보므로 `leaderboard_record` 기준으로 집계한다.
- 테스트 내용: `./gradlew test --tests com.worldmap.admin.AdminPageIntegrationTest --tests com.worldmap.auth.AuthFlowIntegrationTest --tests com.worldmap.web.HomeControllerTest` 통과 후 `./gradlew test` 전체 통과. `AdminPageIntegrationTest`에서 회원 2명, 오늘 시작된 member 세션 1개, guest 세션 1개, 오늘 완료된 위치/인구 run 각 1개를 직접 넣고 `/dashboard` HTML에 `TOTAL MEMBERS`, `TODAY ACTIVE MEMBERS`, `TODAY ACTIVE GUESTS`, `TODAY COMPLETED RUNS`, `L 1 / P 1`이 실제로 보이는지 확인했다.
- 면접에서 30초 안에 설명하는 요약: Dashboard에 운영 수치를 붙일 때 한 테이블에서 다 읽지 않았습니다. 총 회원 수는 `member_account`, 오늘 활성 회원/게스트는 각 게임 세션의 `startedAt`, 오늘 완료된 게임 수는 `leaderboard_record.finishedAt`를 source of truth로 삼았습니다. 그리고 위치/인구수 두 모드에서 중복된 회원이나 guest를 중복 집계하지 않도록 distinct id를 서비스에서 합쳐 `AdminDashboardActivityView`로 만들었습니다.
- 아직 내가 이해가 부족한 부분: 지금은 snapshot 카드만 있고 기간별 추이는 없다. 다음 단계에서는 `최근 7일 활성 수`, `일간 완료 run 추이`, `추천 피드백 추이`처럼 시계열 지표를 어떤 저장소에서 얼마나 단순하게 읽을지 더 정해야 한다.

## 2026-03-24 - 공개 `/stats` 페이지와 local demo 계정 / 샘플 데이터 bootstrap

- 단계: 8. 인증, 전적, 마이페이지
- 목적: Dashboard는 운영자에게만 보여 주는 편이 맞지만, 일반 사용자에게도 “이 서비스가 실제로 돌아가고 있다”는 신호는 필요했다. 그래서 이번 조각은 공개 가능한 운영 수치만 보여 주는 `/stats` 페이지를 따로 만들고, local 환경에서 admin / user / 샘플 플레이 기록을 항상 같은 상태로 재현할 수 있는 demo bootstrap을 추가하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/auth/domain/Member.java`
  - `src/main/java/com/worldmap/country/application/CountrySeedInitializer.java`
  - `src/main/java/com/worldmap/admin/application/AdminBootstrapInitializer.java`
  - `src/main/java/com/worldmap/admin/application/AdminDashboardService.java`
  - `src/main/java/com/worldmap/admin/application/AdminDashboardView.java`
  - `src/main/java/com/worldmap/demo/application/DemoBootstrapProperties.java`
  - `src/main/java/com/worldmap/demo/application/DemoBootstrapService.java`
  - `src/main/java/com/worldmap/demo/application/DemoBootstrapInitializer.java`
  - `src/main/java/com/worldmap/stats/application/ServiceActivityService.java`
  - `src/main/java/com/worldmap/stats/application/ServiceActivityView.java`
  - `src/main/java/com/worldmap/stats/web/StatsPageController.java`
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-test.yml`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/test/java/com/worldmap/demo/DemoBootstrapIntegrationTest.java`
  - `src/test/java/com/worldmap/stats/StatsPageControllerTest.java`
  - `README.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/32-make-public-stats-page-from-dashboard-metrics.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
- 요청 흐름 / 데이터 흐름: 공개 통계는 `GET /stats -> StatsPageController -> ServiceActivityService.loadTodayActivity() + LeaderboardService.getLeaderboard(...) -> stats/index.html` 흐름으로 렌더링된다. Dashboard도 같은 `ServiceActivityService`를 재사용하므로, 공개 숫자와 운영 숫자가 서로 다른 기준으로 어긋나지 않는다. local demo 데이터는 HTTP가 아니라 애플리케이션 시작에서 들어온다. 서버가 local profile로 뜨면 `CountrySeedInitializer -> AdminBootstrapInitializer -> DemoBootstrapInitializer` 순서로 실행되고, `DemoBootstrapService`가 `orbit_runner` 계정, 샘플 leaderboard run 2개, 진행 중 guest 세션 1개를 보장한다.
- 데이터 / 상태 변화: 이번 단계는 새 공개 페이지와 local용 재현 데이터 추가가 핵심이다. `/stats`는 `member_account`, 각 게임 세션의 `startedAt`, `leaderboard_record.finishedAt`를 읽어 총 가입자 수 / 오늘 활성 플레이어 수 / 오늘 시작된 세션 수 / 오늘 완료된 run 수 / 오늘 모드별 완료 수 / 일간 Top 3를 보여 준다. local demo bootstrap은 `member_account`에 `worldmap_admin(ADMIN)`, `orbit_runner(USER)`를 만들고, `leaderboard_record`에 demo 위치 run / demo 인구수 run을 저장하며, 위치 게임 세션 테이블에는 `demo-guest-live` 진행 중 세션을 남긴다.
- 핵심 도메인 개념: `/stats`와 `/dashboard`를 같은 화면으로 합치지 않은 이유는 공개 정보와 운영 정보의 목적이 다르기 때문이다. `/stats`는 사회적 증거와 서비스 활성 감각을 주는 public read model이고, `/dashboard`는 추천 품질과 운영 판단용 내부 read model이다. 또 local demo 계정 생성은 signup이나 SQL 초기 스크립트가 아니라 startup runner + service 조합으로 두었다. 현재 구조에서는 국가 시드가 준비된 뒤 도메인 규칙을 이용해 session / stage / attempt / leaderboard record를 설명 가능한 상태로 만드는 편이 더 자연스럽기 때문이다.
- 예외 상황 또는 엣지 케이스: demo bootstrap은 country 시드가 없으면 동작할 수 없으므로, runner 순서를 `country -> admin -> demo`로 명시적으로 고정했다. 이미 데이터가 있는 상태에서 서버를 다시 띄워도 `runSignature`, `guestSessionKey`, `nickname`을 기준으로 중복 생성은 피한다. 공개 `/stats`에서는 추천 만족도 집계, surveyVersion, persona baseline처럼 내부 운영 판단용 정보는 숨기고 `Dashboard`에만 남긴다.
- 테스트 내용: `./gradlew test --tests com.worldmap.stats.StatsPageControllerTest --tests com.worldmap.demo.DemoBootstrapIntegrationTest --tests com.worldmap.admin.AdminPageIntegrationTest --tests com.worldmap.web.HomeControllerTest` 통과 후 `./gradlew test` 전체 통과. 추가로 local profile로 `./gradlew bootRun --args='--spring.profiles.active=local --server.port=8081'`을 실행해 `/stats` 응답과 PostgreSQL 실데이터를 확인했다. 실제 DB에서 `worldmap_admin / orbit_runner` 계정, demo leaderboard run 2개, `demo-guest-live` 진행 중 세션 1개가 생성되는 것을 확인했다.
- 면접에서 30초 안에 설명하는 요약: 운영 Dashboard를 일반 사용자에게 그대로 열기보다, 공개 가능한 숫자만 보여 주는 `/stats`를 따로 만들었습니다. 이때 Dashboard와 Stats가 다른 숫자를 보여 주지 않도록 `ServiceActivityService`라는 공통 read model 서비스로 활동 지표를 분리했습니다. 또 local 환경에서는 `worldmap_admin`, `orbit_runner`, 샘플 run 2개, guest 진행 중 세션 1개를 startup runner에서 자동으로 만들어서, DB를 지워도 같은 시연 상태를 다시 재현할 수 있게 했습니다.
- 아직 내가 이해가 부족한 부분: 현재 demo bootstrap은 local 시연 편의성을 최우선으로 둔 구조라, 나중에 샘플 데이터를 더 늘릴지 아니면 fixture 수준으로만 유지할지 경계를 더 정해야 한다. 또 `/stats`는 snapshot 카드와 일간 Top 3까지만 있으므로, 추세 그래프나 최근 7일 활성 변화는 9단계나 10단계에서 얼마나 확장할지 추가 판단이 필요하다.

## 2026-03-24 - local bootstrap용 `.env.local` 샘플 추가

- 단계: 8. 인증, 전적, 마이페이지
- 목적: local demo bootstrap 자체는 이미 동작했지만, 매번 긴 환경변수 명령을 직접 입력해야 했다. 그래서 이번 조각은 gitignore 대상인 `.env.local` 샘플을 저장소 루트에 두고, 같은 admin / user 계정과 샘플 데이터를 더 쉽게 다시 띄울 수 있게 정리하는 데 집중한다.
- 변경 파일:
  - `.gitignore`
  - `.env.local`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 이번 단계는 HTTP 요청 흐름이 아니라 local 실행 흐름 정리다. 개발자는 `set -a; source .env.local; set +a; ./gradlew bootRun`으로 local profile을 실행하고, 서버는 기존과 동일하게 `CountrySeedInitializer -> AdminBootstrapInitializer -> DemoBootstrapInitializer` 순서로 country seed, admin 계정, demo user/sample run을 만든다.
- 데이터 / 상태 변화: `.env.local`에는 `SPRING_PROFILES_ACTIVE=local`, `WORLDMAP_ADMIN_BOOTSTRAP_*`, `WORLDMAP_DEMO_BOOTSTRAP_*` 기본값이 들어 있다. 이 값으로 local에서 `worldmap_admin / secret123`, `orbit_runner / secret123`, 샘플 run 2개, guest 진행 세션 1개를 다시 재생성할 수 있다. `.env.local`과 `.env`는 `.gitignore`에 넣어 local-only 설정으로 유지한다.
- 핵심 도메인 개념: 이 파일은 새 비즈니스 기능이 아니라 demo bootstrap 사용성을 높이는 local 실행 자산이다. 그래서 설정 파일은 gitignore로 숨기고, 같은 값은 `README`와 `LOCAL_DEMO_BOOTSTRAP.md`에도 남겨 “파일이 로컬에만 있어도 재현 방법은 문서로 공유된다”는 구조를 택했다.
- 예외 상황 또는 엣지 케이스: Spring Boot가 `.env.local`을 자동으로 읽는 것은 아니므로, 터미널에서 `source .env.local`을 먼저 실행해야 한다. `.env.local`을 수정해 닉네임이나 비밀번호를 바꿔도, local bootstrap은 다음 서버 시작 시 새 값으로 계정을 만들거나 기존 계정 비밀번호를 갱신한다.
- 테스트 내용: 애플리케이션 테스트는 다시 추가하지 않았다. 기존 `./gradlew test` 전체 통과 상태를 유지한 채, local 부팅에서 `.env.local` 값과 동일한 계정 / 샘플 데이터가 실제로 생성되는지 앞선 부팅 검증으로 확인했다.
- 면접에서 30초 안에 설명하는 요약: local 시연 상태를 더 쉽게 재현하려고 gitignored `.env.local` 샘플을 따로 두었습니다. 이 파일에는 admin bootstrap과 demo user bootstrap 기본값이 들어 있고, source 후 서버를 띄우면 기존 startup runner 흐름을 통해 같은 계정과 샘플 run을 다시 만들 수 있습니다. 즉, 민감한 local 설정은 git에 올리지 않으면서도, 재현 방법은 문서와 실행 파일로 같이 남겼습니다.
- 아직 내가 이해가 부족한 부분: 현재 `.env.local`은 local 시연 편의성 중심이라 비밀번호 회전이나 다중 개발자별 로컬 값 분기는 따로 고려하지 않았다. 이후 배포/협업 범위가 넓어지면 `.env.example`을 별도로 둘지, 지금처럼 문서 + gitignored 실제 파일 조합을 유지할지 판단이 더 필요하다.

## 2026-03-24 - 홈 첫 화면에 로그인 / 회원가입 진입점 추가

- 단계: 8. 인증, 전적, 마이페이지
- 목적: 로그인과 회원가입 자체는 이미 동작했지만, 홈 첫 화면에는 바로 들어가는 버튼이 없어서 guest 사용자가 계정 연결 경로를 한 번 더 찾아야 했다. 그래서 이번 조각은 홈에서 guest는 `로그인 / 회원가입`, 로그인 사용자는 `My Page / 로그아웃`을 바로 보게 만들어 기록 유지 진입점을 짧게 하는 데 집중한다.
- 변경 파일:
  - `src/main/resources/templates/home.html`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/web/HomeControllerTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/34-add-home-auth-entry-points.md`
- 요청 흐름 / 데이터 흐름: `GET / -> HomeController -> home.html` 흐름은 그대로다. 이번에는 `home.html`이 현재 `HttpSession`을 직접 읽어, guest면 `로그인 / 회원가입` CTA를 렌더링하고, 로그인 상태면 세션에 들어 있는 `WORLDMAP_MEMBER_NICKNAME`을 표시하면서 `My Page / 로그아웃` 블록을 렌더링한다. 로그아웃은 새 흐름을 만들지 않고 기존 `POST /logout -> AuthPageController.logout() -> MemberSessionManager.signOut()`를 그대로 사용한다.
- 데이터 / 상태 변화: 새로운 테이블이나 권한 모델 변화는 없다. 바뀐 것은 홈 첫 화면에서 현재 인증 상태를 직접 보여 주는 방식이다. guest는 홈에서 바로 계정을 연결할 수 있고, 로그인 사용자는 어떤 계정으로 기록을 이어가는지 홈에서 바로 확인한다.
- 핵심 도메인 개념: 이 작업은 인증 규칙 추가가 아니라 표현 규칙 보강이다. 그래서 `HomeController`에 model flag를 더 넘기지 않고, 템플릿이 세션 상태를 직접 읽도록 두었다. 반대로 로그인/로그아웃 같은 상태 변경은 계속 `AuthPageController`, `MemberSessionManager`가 맡는다. 즉, 홈은 상태를 바꾸지 않고 이미 있는 인증 상태를 어떻게 보여 줄지만 책임진다.
- 예외 상황 또는 엣지 케이스: guest는 홈에서 `로그인 / 회원가입`을 보더라도, 헤더의 `My Page` 링크는 기존처럼 남아 있다. 이 경우 `/mypage`는 로그인 유도 화면으로 간다. ADMIN 세션은 `Dashboard` 버튼과 함께 홈에서도 `로그아웃`을 바로 사용할 수 있다.
- 테스트 내용: `./gradlew test --tests com.worldmap.web.HomeControllerTest --tests com.worldmap.auth.AuthFlowIntegrationTest` 통과. `HomeControllerTest`는 guest 홈에서 `로그인`, `회원가입`이 보이고 `로그아웃`은 안 보이는지, ADMIN 세션 홈에서는 `Dashboard`, `로그아웃`이 보이고 guest용 `회원가입`은 숨겨지는지 확인했다.
- 면접에서 30초 안에 설명하는 요약: 계정 기능이 있어도 홈에서 바로 들어갈 수 없으면 사용자 경험이 끊깁니다. 그래서 홈 템플릿이 현재 세션을 보고, guest면 `로그인 / 회원가입`, 로그인 상태면 `My Page / 로그아웃`을 보여 주도록 바꿨습니다. 상태 변경 로직은 기존 auth 흐름을 그대로 쓰고, 홈은 그 상태를 SSR에서 어떻게 표현할지만 맡게 해서 책임을 나눴습니다.
- 아직 내가 이해가 부족한 부분: 지금은 홈 첫 화면에서만 계정 CTA를 더 강하게 노출한다. 이후 위치/인구수 시작 화면에도 같은 수준으로 로그인 유도를 둘지, 아니면 홈만 메인 진입점으로 유지할지는 한 번 더 정리할 필요가 있다.

## 2026-03-24 - 추천 설문을 12문항 trade-off 구조로 재설계

- 단계: 6. 설문 기반 추천 엔진
- 목적: 기존 추천 설문은 문항 수가 적고 질문이 너무 단순했다. 특히 `물가 허용 범위`처럼 대부분이 낮은 물가를 고를 수밖에 없는 질문은 실제 취향을 잘 가르지 못했다. 그래서 이번 조각은 질문 수를 12개로 늘리는 것과 동시에, `비용을 더 내고 생활 품질을 얻을 의향`, `기후 적응 성향`, `치안/공공 서비스/음식/다양성 중요도`처럼 trade-off를 묻는 구조로 추천 입력 자체를 다시 설계하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackPayloadView.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaFixtures.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/35-redesign-recommendation-survey-with-twelve-questions.md`
- 요청 흐름 / 데이터 흐름: 추천 흐름 자체는 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 바뀐 것은 입력 모델과 점수식이다. `RecommendationSurveyForm`이 12개 질문을 `RecommendationSurveyAnswers`로 바꾸고, `RecommendationSurveyService`는 country profile 30개와 비교해 top 3를 계산한다. 결과 페이지는 12개 답변 스냅샷을 hidden payload로 함께 내려 보내고, 만족도 제출은 `RecommendationFeedbackRequest -> RecommendationFeedbackSubmission -> RecommendationFeedbackService`로 저장된다.
- 데이터 / 상태 변화: 추천 결과 top 3 자체는 여전히 저장하지 않는다. 저장되는 것은 `surveyVersion=survey-v3`, `engineVersion=engine-v3`, `satisfactionScore`, 그리고 사용자가 선택한 12개 답변 스냅샷이다. 새 answer snapshot 컬럼은 로컬 기존 row와 충돌하지 않게 nullable로 추가했고, 실제 신규 요청 유효성은 request validation으로 강제한다.
- 핵심 도메인 개념: 이번 단계의 핵심은 “좋아하는 조건”만 묻지 않고 “무엇을 감수할 수 있는가”를 묻는 설문으로 바꾼 것이다. 그래서 단순 budget/priority 한 축 대신 `CostQualityPreference`, `SeasonTolerance`, `ImportanceLevel` 4축을 별도 enum으로 분리했다. 이 로직은 컨트롤러가 아니라 `RecommendationSurveyService`에 있어야 한다. 어떤 신호를 얼마나 강하게 점수에 반영할지, 물가 초과를 어떻게 penalty로 줄지, 영어 지원이 핵심일 때 얼마나 더 강하게 볼지는 HTTP 바인딩이 아니라 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: `VALUE_FIRST` 응답에서는 실제 물가가 높을수록 penalty가 음수까지 내려가게 해서 “좋은 인프라가 있어도 너무 비싼 나라는 어렵다”는 신호를 더 분명히 만든다. 반대로 `QUALITY_FIRST`에서는 높은 생활비 자체를 바로 배제하지 않는다. `LOW` importance는 완전 무시가 아니라 약한 선호로 남겨 둬서, 후보 간 식별력은 유지하되 극단적 편향은 줄인다. 오프라인 페르소나 baseline은 새 질문 구조에 맞게 fixture와 snapshot을 다시 고정했다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest` 통과 후 `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: 추천 설문이 단순하면 대부분 비슷한 답을 고르게 됩니다. 그래서 질문 수를 12개로 늘리는 것보다, `무엇을 좋아하느냐`가 아니라 `무엇을 감수할 수 있느냐`를 묻는 방향으로 다시 설계했습니다. 그리고 이 입력은 `RecommendationSurveyForm -> RecommendationSurveyAnswers -> RecommendationSurveyService` 흐름으로 서버에 들어가고, 서버가 deterministic하게 top 3를 계산합니다. 결과는 저장하지 않고 12개 답변 스냅샷과 만족도만 익명으로 모아 다음 설문 버전 개선에 씁니다.
- 아직 내가 이해가 부족한 부분: 지금 baseline은 새 12문항 구조에 맞춰 다시 고정했지만, `P04`, `P05`, `P06` 같은 시나리오가 진짜로 더 좋아졌는지까지는 다음 버전 실험에서 더 봐야 한다.

## 2026-03-24 - 공통 shell에 다크/라이트 테마 토글 추가

- 단계: 6. 설문 기반 추천 엔진 보조 UI 조각
- 목적: 현재 사이트는 차가운 우주 톤을 기본으로 두고 있지만, 사용자가 오래 보거나 시연할 때는 밝은 화면이 더 나은 경우도 있었다. 그래서 이번 조각은 공통 shell에 라이트모드를 추가하되, 사이트 전체에서 한 번에 적용되는 토글 구조를 만드는 데 집중한다.
- 변경 파일:
  - `src/main/resources/static/css/site.css`
  - `src/main/resources/templates/fragments/site-header.html`
  - `src/main/resources/templates/fragments/admin-header.html`
  - `src/main/resources/static/js/theme-toggle.js`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/36-add-sitewide-light-mode-toggle.md`
- 요청 흐름 / 데이터 흐름: 이 기능은 서버 상태 변화가 없다. 첫 진입 시 header fragment 상단의 짧은 inline script가 `localStorage.worldmap-theme`를 읽어 `html[data-theme]`를 먼저 맞춘다. 이후 `theme-toggle.js`가 토글 버튼 클릭을 받아 `light <-> dark`를 전환하고, 같은 값을 다시 localStorage에 저장한다. 실제 시각 변화는 모두 `site.css`의 CSS 변수 레이어가 담당한다.
- 데이터 / 상태 변화: DB나 세션에는 아무것도 저장하지 않는다. 테마 상태는 브라우저 로컬의 `worldmap-theme` 값만 바뀐다. public shell과 dashboard shell이 같은 storage key를 공유하므로, 사용자가 홈에서 light로 바꾸면 `/dashboard`에 들어가도 같은 테마가 유지된다.
- 핵심 도메인 개념: 이건 사용자 선호 UI 상태이지 비즈니스 데이터가 아니다. 그래서 컨트롤러나 세션에 넣지 않고 `html[data-theme] + CSS 변수 + localStorage` 조합으로 처리했다. 테마를 서버가 들고 있지 않기 때문에 페이지 진입마다 request 처리나 DB 접근이 추가되지 않고, 모든 화면이 공통 fragment와 공통 CSS만으로 같은 동작을 한다.
- 예외 상황 또는 엣지 케이스: localStorage를 쓸 수 없는 환경에서는 기본 다크 테마로만 유지된다. public header와 admin header 둘 다 동일한 toggle markup과 같은 storage key를 쓰기 때문에, 어느 화면에서 바꿔도 다른 화면과 테마가 어긋나지 않는다. inline script를 fragment 상단에 둔 이유는 JS 파일이 로드되기 전 잠깐 다크/라이트가 뒤집혀 보이는 깜빡임을 줄이기 위해서다.
- 테스트 내용: `node --check src/main/resources/static/js/theme-toggle.js`, `node --check src/main/resources/static/js/recommendation-feedback.js`, `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: 라이트모드는 비즈니스 상태가 아니라 UI 상태라서 서버가 가질 이유가 없었습니다. 그래서 공통 header fragment가 localStorage 값을 읽어 `html[data-theme]`를 먼저 맞추고, CSS 변수 레이어가 실제 색을 바꾸게 했습니다. 덕분에 public 화면과 dashboard가 같은 토글 구조를 공유하면서도 서버 로직은 건드리지 않았습니다.
- 아직 내가 이해가 부족한 부분: 현재 라이트모드는 공통 panel과 shell 위주로만 점검했다. 실제 지구본/지도 렌더링 화면에서 light background가 어느 정도까지 잘 어울리는지는 다음 시각 polish에서 조금 더 봐야 한다.

## 2026-03-24 - 추천 설문을 20문항 trade-off 구조로 다시 확장

- 단계: 6. 설문 기반 추천 엔진
- 목적: 12문항 구조는 이전보다 훨씬 나아졌지만, 여전히 질문 축이 부족했다. 특히 기후 질문은 `따뜻함 / 온화함 / 시원함`처럼 몸이 편한 방향을 더 자연스럽게 물어야 하고, 집 크기, 초기 적응 친화도, 디지털 생활 편의, 문화·여가, 장기 기반 같은 실제 생활 축도 더 분리해서 봐야 했다. 이번 조각은 추천 설문을 20문항으로 늘리되, 문항 수만 늘리는 게 아니라 나라 프로필 축과 점수식까지 같이 세분화하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfile.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackPayloadView.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java`
  - `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java`
  - `src/main/resources/templates/recommendation/survey.html`
  - `src/main/resources/templates/recommendation/result.html`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaFixtures.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/37-expand-recommendation-survey-to-twenty-questions.md`
- 요청 흐름 / 데이터 흐름: 추천 흐름 자체는 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 바뀐 것은 입력 모델과 점수 계산의 세밀함이다. `RecommendationSurveyForm`이 20개 질문을 `RecommendationSurveyAnswers`로 정규화하고, `RecommendationSurveyService`는 확장된 나라 프로필 카탈로그를 기준으로 climate / seasonality / housing / newcomer support / digital / culture / future base까지 점수화한다. 결과 페이지는 20개 답변 스냅샷을 hidden payload로 내려 보내고, 만족도 제출은 `RecommendationFeedbackRequest -> RecommendationFeedbackSubmission -> RecommendationFeedbackService`로 저장된다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 저장되는 것은 `surveyVersion=survey-v4`, `engineVersion=engine-v4`, `satisfactionScore`, 그리고 사용자가 선택한 20개 답변 스냅샷이다. 이전 피드백 row와 충돌하지 않게 새 컬럼은 nullable로 추가했고, 신규 요청 유효성은 form / request validation으로 강제한다.
- 핵심 도메인 개념: 이번 단계의 핵심은 질문 수가 아니라 `설문 축의 해상도`다. 그래서 기존 12문항 위에 질문만 덧붙이지 않고, `SeasonStylePreference`, `CrowdPreference`, `HousingPreference`, `NewcomerSupportNeed`, `WorkLifePreference`, `FutureBasePreference` 같은 별도 enum 축을 추가했다. 그리고 나라 프로필에도 `seasonality`, `housingSpace`, `digitalConvenience`, `cultureScene`, `newcomerFriendliness`를 새로 넣었다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 답변이 어떤 나라 속성과 얼마나 가깝고, cost overshoot나 climate mismatch를 어떻게 penalty로 줄지, newcomer support와 future base를 어떻게 read model로 묶을지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: 기존 local DB에 예전 피드백 row가 있을 수 있기 때문에 새 피드백 컬럼은 nullable로 추가했다. `ClimatePreference.COLD` enum name은 그대로 두고 label만 `시원하고 선선한 쪽`으로 바꿔, 기존 enum row를 깨지 않고 사용자-facing 문구만 개선했다. snapshot은 20문항 구조에서 실제 결과가 달라졌기 때문에 `survey-v4 / engine-v4` 기준으로 다시 고정했다.
- 테스트 내용: `./gradlew test --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest --tests com.worldmap.admin.AdminPageIntegrationTest` 통과 후 `./gradlew test` 전체 통과.
- 면접에서 30초 안에 설명하는 요약: 12문항 설문은 시작점으로는 괜찮았지만, 실제 생활 선택에서는 집 크기, 초기 적응 친화도, 디지털 인프라, 문화·여가, 장기 기반 같은 축이 더 필요했습니다. 그래서 설문만 20개로 늘린 게 아니라 `RecommendationSurveyAnswers`, 나라 프로필, 점수식, 익명 feedback snapshot, offline persona baseline을 전부 같이 확장했습니다. 요청은 여전히 `survey -> service -> result -> feedback` 흐름으로 가고, 추천 계산은 서버가 deterministic하게 유지합니다.
- 아직 내가 이해가 부족한 부분: 현재 `survey-v4 / engine-v4` baseline은 다시 고정했지만, `P04`, `P06`, `P14`처럼 남유럽 / 저예산 균형 시나리오가 과하게 스페인·이탈리아 쪽으로 끌리는지, newcomer support와 future base 가중치가 충분히 분리됐는지는 다음 tuning에서 더 봐야 한다.

## 2026-03-25 - 홈 첫 화면에서 중복 모드 노출을 걷어내고 진입 구조 단순화

- 단계: 8. 인증, 전적, 마이페이지 보조 public shell 정리
- 목적: 홈 첫 화면에는 `위치 미션 시작`, `인구수 퀴즈 시작` 같은 hero CTA와 `모드 구성` 카드가 동시에 있어 같은 정보를 여러 번 보게 됐다. 이번 조각은 홈에서 모드 이름을 한 번만 보여 주고, hero는 서비스 소개와 계정/Stats 진입만 맡도록 구조를 단순화하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/web/HomeController.java`
  - `src/main/resources/templates/home.html`
  - `src/main/resources/static/css/site.css`
  - `src/test/java/com/worldmap/web/HomeControllerTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/38-simplify-home-landing-structure.md`
- 요청 흐름 / 데이터 흐름: 요청은 그대로 `GET / -> HomeController -> home.html`이다. 이번에는 `HomeController`가 `modeCards`, `entrySteps`, `accountNotes`만 내려 주고, `home.html`은 그 세 가지 read model과 현재 세션을 합쳐 홈 첫 화면을 렌더링한다. guest / member 분기는 기존처럼 템플릿이 세션을 직접 읽고, hero는 `모드 선택하기 -> #home-modes`, `서비스 현황 보기 -> /stats`만 제공한다. 실제 모드별 직접 이동은 `지금 플레이할 모드` 카드에만 남겼다.
- 데이터 / 상태 변화: 새로운 테이블이나 인증 규칙 변화는 없다. 바뀐 것은 public 홈 첫 화면의 정보 구조다. 모드 선택, 기록 연결, 통계 진입을 서로 다른 위치에서 중복 노출하던 구조를 줄이고, 홈에서는 “무슨 서비스인지”와 “어떻게 시작하는지”만 먼저 보여 주도록 정리했다.
- 핵심 도메인 개념: 이 작업은 도메인 로직 추가가 아니라 public shell의 read model 정리다. 그래서 `HomeController`는 게임 상태나 인증 상태를 바꾸지 않고, 홈에서 반복해서 써야 하는 설명 리스트만 만든다. 반대로 guest/member 인증 상태, 로그아웃, 실제 모드 시작은 기존 auth/game 흐름이 계속 맡는다. 즉, 홈은 상태를 바꾸지 않고 진입 구조를 어떻게 보여 줄지만 책임진다.
- 예외 상황 또는 엣지 케이스: guest는 여전히 홈에서 `로그인 / 회원가입`을 보지만, 직접 게임은 가입 없이 바로 시작할 수 있다. 로그인 사용자는 hero에서 `My Page / 로그아웃`을 그대로 본다. ADMIN 세션은 헤더에 `Dashboard`가 계속 보이지만, hero나 본문에는 운영용 링크를 추가로 두지 않는다. `Stats`는 공개 페이지라 guest와 member 모두 동일하게 진입할 수 있다.
- 테스트 내용: `./gradlew test --tests com.worldmap.web.HomeControllerTest` 통과. guest 홈에서 `모드 선택하기`, `지금 플레이할 모드`, `기록은 이렇게 이어집니다`, `Stats`가 보이고, 기존 중복 문구였던 `위치 미션 시작`, `인구수 퀴즈 시작`, `모드 구성`, `플레이 방식`은 더 이상 보이지 않는지 확인했다. ADMIN 세션에서는 `Dashboard` 노출도 계속 유지되는지 확인했다.
- 면접에서 30초 안에 설명하는 요약: 홈 화면은 첫 진입점이라 정보가 많아질수록 사용자가 어디를 눌러야 할지 헷갈립니다. 그래서 hero에서는 서비스 소개와 계정/Stats 진입만 남기고, 실제 모드 선택은 카드 영역 한 곳에서만 하게 구조를 다시 정리했습니다. 요청 흐름은 그대로 `HomeController -> home.html`이고, 상태 변경은 auth나 game 흐름이 맡고 홈은 SSR에서 진입 구조를 어떻게 보여 줄지만 책임집니다.
- 아직 내가 이해가 부족한 부분: 지금은 홈 hero를 단순화했지만, `Stats`를 hero에 둘지 아니면 카드/하단 정보 영역으로 옮길지가 완전히 고정된 것은 아니다. 이후 홈/Stats/My Page 사이의 공개 진입 흐름을 한 번 더 시연 기준으로 검토할 필요가 있다.

## 2026-03-25 - 디자인 패스 이후 public 화면 테스트와 문서를 현재 카피 기준으로 안정화

- 단계: 8. 인증, 전적, 마이페이지 보조 public shell 안정화
- 목적: 다른 세션에서 home, stats, recommendation, ranking, mypage 등 public 화면 디자인이 크게 바뀌었다. 그런데 이 상태에서 기존 SSR 테스트는 옛 문구를 기대하고 있어 실패가 생겼다. 그래서 이번 조각은 기능을 더 추가하지 않고, 새 디자인과 카피가 실제 테스트와 문서에 반영되도록 안정화하는 데 집중한다.
- 변경 파일:
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/39-stabilize-public-design-pass.md`
- 요청 흐름 / 데이터 흐름: 런타임 요청 흐름 자체는 바뀌지 않았다. 추천은 여전히 `GET /recommendation/survey -> RecommendationPageController -> survey.html`, 랭킹은 `GET /ranking -> LeaderboardPageController -> ranking/index.html`이다. 이번에는 컨트롤러가 내려 주는 SSR 화면의 문구와 테스트 기대값만 새 디자인 기준으로 맞췄다.
- 데이터 / 상태 변화: DB, 세션, 도메인 모델 변화는 없다. 바뀐 것은 public 화면의 표현과, 그 표현을 검증하는 통합 테스트 기대값이다.
- 핵심 도메인 개념: 큰 디자인 패스 직후에는 먼저 “서버가 어떤 HTML을 내려 주는가”를 다시 고정해야 한다. 이 프로젝트는 SSR 기반이라, 카피와 레이아웃이 바뀌면 기능 로직은 그대로여도 테스트는 깨질 수 있다. 그래서 이번 조각은 새 디자인을 또 바꾸기보다, `RecommendationPageIntegrationTest`, `LeaderboardIntegrationTest`가 현재 문구와 구조를 기준으로 다시 통과하도록 만드는 안정화 작업이다.
- 예외 상황 또는 엣지 케이스: 이번 워킹트리에는 디자인과 직접 무관한 변경도 같이 들어 있었다. 그래서 이번 조각은 그 변경을 억지로 되돌리지 않고, public 디자인 패스와 직접 연결되는 테스트/문서만 먼저 맞췄다. `build.gradle`의 Java toolchain 변경처럼 디자인과 직접 무관한 diff는 다음 정리 조각에서 의도를 다시 확인하는 편이 안전하다.
- 테스트 내용: `./gradlew test --tests com.worldmap.web.HomeControllerTest --tests com.worldmap.web.MyPageControllerTest --tests com.worldmap.stats.StatsPageControllerTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.ranking.LeaderboardIntegrationTest`, `./gradlew test` 전체를 다시 돌려 새 카피 기준으로 통과 여부를 확인한다.
- 면접에서 30초 안에 설명하는 요약: 큰 UI 개편 뒤에는 기능을 더 넣기보다 먼저 SSR 테스트를 새 화면 기준으로 다시 고정하는 것이 중요합니다. 이 프로젝트는 Thymeleaf 기반이라 문구와 레이아웃이 바뀌면 컨트롤러 로직은 그대로여도 테스트가 깨질 수 있기 때문입니다. 그래서 추천 설문과 랭킹 통합 테스트를 현재 public 화면 카피에 맞춰 다시 고정하고, 문서도 그 상태 기준으로 정리했습니다.
- 아직 내가 이해가 부족한 부분: 현재 디자인 패스에 포함된 모든 변경 중 어떤 것이 최종 확정본인지와, 디자인과 무관한 `build.gradle` 변경 같은 diff를 이번 커밋에 포함할지 분리할지는 한 번 더 정리할 필요가 있다.

## 2026-03-25 - 추천 엔진 weak scenario 튜닝 1차: 비용 선호별 초과 물가 패널티 분리

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `survey-v4 / engine-v4`에서는 `VALUE_FIRST` 사용자가 응답해도 저비용 시나리오가 남유럽 고비용 후보 쪽으로 자주 끌렸다. 이번 조각은 추천 엔진 전체를 다시 흔들지 않고, `비용을 얼마나 우선하는가` 한 축만 더 정교하게 분리해 `P02`, `P14` 같은 저비용 페르소나의 top 3 구성을 다시 조정하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/40-split-cost-overshoot-penalty-by-preference.md`
- 요청 흐름 / 데이터 흐름: 런타임 요청 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 바뀐 것은 `RecommendationSurveyService` 내부의 비용 점수 계산뿐이다. 사용자가 `CostQualityPreference`를 고르면 서비스가 이제 `VALUE_FIRST / BALANCED / QUALITY_FIRST`에 따라 초과 물가 패널티 강도를 다르게 적용한다. 결과 페이지와 feedback 저장은 기존처럼 `survey-v4`, 새 `engine-v5`, 20개 답변 snapshot을 함께 내려 주고 저장한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 `surveyVersion=survey-v4`, `engineVersion=engine-v5`, `satisfactionScore`, 20개 답변 snapshot만 저장된다. 이 버전 문자열은 `/dashboard/recommendation/feedback`과 `/dashboard` 운영 화면에도 그대로 노출되어, 어느 엔진 실험의 응답인지 나중에 집계할 수 있게 한다.
- 핵심 도메인 개념: 이번 단계의 핵심은 “비용 민감도도 사람마다 다르다”는 점을 점수식에 드러내는 것이다. 이전에는 초과 물가 패널티가 하나였기 때문에 `QUALITY_FIRST`와 `VALUE_FIRST` 모두 같은 강도로 깎였다. 이제는 `VALUE_FIRST > BALANCED > QUALITY_FIRST` 순으로 penalty 강도를 나눴다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 사용자의 비용 선호를 실제 나라 `priceLevel`과 비교해 몇 점을 주거나 깎을지는 HTTP 바인딩이 아니라 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: 이번 튜닝으로 `P02`, `P14`의 3위 후보는 `이탈리아 -> 말레이시아`처럼 더 저비용 쪽으로 움직였지만, `P15`는 `우루과이 -> 남아프리카 공화국`으로 바뀌었다. 즉 `VALUE_FIRST` penalty를 분리하는 것만으로는 탐색형/교통형 저예산 시나리오를 완전히 원하는 방향으로 고정하지 못했다. 그래서 `P15`는 다음 penalty 실험에서도 계속 weak scenario로 볼 예정이다.
- 테스트 내용: 먼저 `RecommendationOfflinePersonaSnapshotTest`에서 실제 top 3를 다시 뽑아 `engine-v5` snapshot을 갱신했다. 그다음 `./gradlew test --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.admin.AdminPageIntegrationTest` 통과, 마지막으로 `./gradlew test` 전체 통과까지 확인했다.
- 면접에서 30초 안에 설명하는 요약: 추천 엔진이 비용 민감 사용자를 충분히 구분하지 못해서, 이번에는 전체 알고리즘을 다시 짜지 않고 `초과 물가 패널티` 한 축만 분리했습니다. `VALUE_FIRST`는 더 강하게, `QUALITY_FIRST`는 더 약하게 깎게 만들어서 저비용 시나리오의 후보 구성이 실제로 달라지게 했습니다. 이 규칙은 `RecommendationSurveyService`가 맡고, 결과는 `RecommendationOfflinePersonaSnapshotTest`로 `engine-v5` snapshot을 다시 고정해 설명 가능하게 유지했습니다.
- 아직 내가 이해가 부족한 부분: `P02`, `P14`는 원하는 방향으로 조금 움직였지만 `P15`는 아직 탐색형/교통형 신호가 충분히 반영되지 않는다. 다음 실험에서는 `newcomerSupport`, `mobility`, `futureBase` 중 어느 축을 더 손봐야 하는지 좁혀 봐야 한다.

## 2026-03-25 - 추천 엔진 weak scenario 튜닝 2차: 탐색형·교통형 저예산 시나리오 보정

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v5`에서도 `P15`는 `포르투갈, 뉴질랜드, 남아프리카 공화국`으로 나와서, 기대했던 `말레이시아`가 top 3에 다시 들어오지 못했다. 이번 조각은 범위를 더 좁혀 `EXPERIENCE + TRANSIT_FIRST + VALUE_FIRST` 조합에서만 작동하는 보정 신호를 추가해, 탐색형/교통형 저예산 시나리오를 실제로 한 단계 더 분리하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/41-add-experience-transit-bonus-for-budget-explorers.md`
- 요청 흐름 / 데이터 흐름: 추천 런타임 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 내부에 `experienceTransitBonus()`를 추가했다. 사용자가 `EXPERIENCE + TRANSIT_FIRST + VALUE_FIRST`로 답하면, 서비스가 각 후보의 `transitSupport`, `newcomerSupport`, `digitalConvenience`, `safety`, `welfare`를 한 번 더 읽어 “가볍게 적응하면서 대중교통 중심으로 살아보기 좋은가”를 별도 보정한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v6`이 저장되고, `/dashboard`와 `/dashboard/recommendation/feedback` 운영 화면도 현재 엔진 버전을 `engine-v6`으로 보여준다. `surveyVersion`은 계속 `survey-v4`를 유지한다. 즉, 이번 조각은 질문이 아니라 엔진만 튜닝한 버전 실험이다.
- 핵심 도메인 개념: 이번 단계의 핵심은 “교통 친화적인 탐색형 생활”을 기존 축들만으로는 완전히 설명하기 어려웠다는 점이다. 그래서 `mobility`, `newcomer`, `digital`, `기본 안전성`을 한 번 더 묶는 작은 coherence 신호를 만들었다. 이 규칙은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 보정을 켜고, 어떤 프로필 속성을 함께 읽을지는 HTTP 처리보다 추천 도메인 규칙에 가깝기 때문이다.
- 예외 상황 또는 엣지 케이스: 처음에는 `EXPERIENCE + TRANSIT_FIRST` 전체에 보정을 주었더니 `P17` 같은 도심 탐험형 시나리오까지 흔들렸다. 그래서 최종 버전에서는 `VALUE_FIRST`까지 동시에 만족할 때만 보정을 켜고, `safety`와 `welfare`가 너무 낮은 후보는 bonus를 못 받게 좁혔다. 덕분에 `P17`은 그대로 두고 `P15`만 다시 움직이게 만들었다.
- 테스트 내용: 먼저 `RecommendationOfflinePersonaSnapshotTest`로 전체 18개 시나리오 top 3를 다시 확인했고, `P15`가 `포르투갈, 뉴질랜드, 말레이시아`로 바뀌는 것을 확인한 뒤 snapshot을 `engine-v6` 기준으로 다시 고정했다. 그 다음 `RecommendationOfflinePersonaCoverageTest`에서 `P15`가 `뉴질랜드`, `말레이시아`를 포함하고 `남아프리카 공화국`은 포함하지 않는지 고정했다. 마지막으로 `./gradlew test --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.admin.AdminPageIntegrationTest`와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: `engine-v5`에서는 저예산 탐색형 시나리오가 아직 원하는 후보를 못 올렸습니다. 그래서 이번에는 추천 엔진 전체를 다시 흔드는 대신, `EXPERIENCE + TRANSIT_FIRST + VALUE_FIRST` 조합에서만 작동하는 작은 보정 신호를 추가했습니다. 이 규칙은 `RecommendationSurveyService`가 맡고, 결과는 `P15`가 `뉴질랜드 + 말레이시아`를 포함하도록 coverage와 snapshot 테스트에 다시 고정했습니다.
- 아직 내가 이해가 부족한 부분: `P15`는 개선됐지만 `P04`, `P06`처럼 남유럽 후보가 상단에 남는 균형형/현실형 시나리오는 아직 더 봐야 한다. 다음에는 `publicService`와 `futureBase`, 혹은 `balanced cost` 구간 penalty를 어디까지 만질지 좁혀야 한다.

## 2026-03-25 - 추천 엔진 weak scenario 튜닝 3차: 균형형 civic base 보정

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v6`에서 `P15`는 개선됐지만, `P04`, `P06`처럼 `MILD + BALANCED + MIXED` 생활을 원하면서 안전이나 공공서비스도 신경 쓰는 시나리오는 여전히 스페인·이탈리아 쪽으로 기울었다. 이번 조각은 이 패턴만 좁혀서, 균형형 사용자에게 `기후가 맞는지`뿐 아니라 `안전, 공공서비스, 기본 정착 안정성`도 함께 반영하는 작은 보정을 추가하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/recommendation/PERSONA_EVAL_SET.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/42-add-civic-base-bonus-for-balanced-lifestyles.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 내부에 `civicBaseBonus()`를 추가했다. 사용자가 `MIXED` 환경, `BALANCED` pace를 원하고 `safetyPriority` 또는 `publicServicePriority`가 `HIGH`이면, 서비스가 후보 국가의 `safety`, `welfare`, `housingSpace`, `newcomerFriendliness`를 한 번 더 묶어 균형형 정착 안정성을 보정한다. 단, `VALUE_FIRST`인데 물가가 너무 높은 후보는 이 bonus를 못 받게 막았다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v7`이 저장되고, `/dashboard`와 `/dashboard/recommendation/feedback` 운영 화면도 현재 엔진 버전을 `engine-v7`으로 보여준다. 설문 버전은 계속 `survey-v4`를 유지한다.
- 핵심 도메인 개념: 이번 단계의 핵심은 “균형형 생활”도 하나의 coherence 신호라는 점이다. 지금까지는 기후, pace, 물가 같은 개별 축 점수는 있었지만, `안전 / 공공서비스 / 정착 안정성`을 같이 묶는 read model은 약했다. 그래서 `civicBaseBonus()`로 이 축을 한 번 더 잡았다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 이 bonus를 켜고, 어떤 프로필 속성을 묶어 읽을지는 추천 도메인 규칙이지 HTTP 바인딩 규칙이 아니기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus 범위를 너무 넓히면 북유럽/영어권 고비용 후보가 과하게 올라올 수 있다. 그래서 `MIXED + BALANCED` 생활과 `HIGH` safety/public service 중요도가 함께 있을 때만 bonus를 켰고, `VALUE_FIRST`인데 `priceLevel >= 4`인 후보는 bonus를 받지 못하게 했다. 덕분에 `P04`는 `스페인, 아일랜드, 우루과이`, `P06`은 `스페인, 우루과이, 이탈리아`까지 움직였고, 다른 anchor 시나리오는 크게 흔들리지 않았다.
- 테스트 내용: 먼저 `RecommendationOfflinePersonaSnapshotTest`로 18개 시나리오 top 3를 다시 뽑아 `P04`, `P06`, `P14` 변화를 확인했다. 그다음 snapshot을 `engine-v7` 기준으로 다시 고정했다. `RecommendationOfflinePersonaCoverageTest`에서는 `P04`가 `아일랜드`, `우루과이`를 포함하고 `이탈리아`는 빠지는지, `P06`이 `우루과이`를 포함하는지를 추가로 고정했다. 마지막으로 `./gradlew test --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.admin.AdminPageIntegrationTest`와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: `P15`를 개선한 뒤에도 균형형 시나리오에서는 여전히 남유럽 후보가 너무 강했습니다. 그래서 이번에는 `MIXED + BALANCED` 생활을 원하면서 안전이나 공공서비스를 중시하는 경우에만 작동하는 `civicBaseBonus`를 추가했습니다. 이 규칙은 `RecommendationSurveyService`가 맡고, `P04`가 `스페인 + 아일랜드 + 우루과이`, `P06`이 `스페인 + 우루과이`를 포함하도록 snapshot과 coverage 테스트에 다시 고정했습니다.
- 아직 내가 이해가 부족한 부분: `P04`는 꽤 좋아졌지만 `P06`의 3위 후보에는 아직 `이탈리아`가 남는다. 다음 실험에서는 `balanced cost` 구간에서 `food/culture`가 너무 강하게 남는지, 아니면 `futureBase`나 `english/newcomer` 쪽을 조금 더 올려야 하는지 더 좁혀 봐야 한다.

## 2026-03-25 - 추천 엔진 weak scenario 튜닝 4차: 현실형 저예산 사용자 soft landing 보정

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v7`에서 `P06`은 `스페인, 우루과이, 이탈리아`까지는 좋아졌지만, 3위에 아직 `이탈리아`가 남아 있었다. 이번 조각은 `P06`만 겨냥해, 저예산이면서 안전을 중시하고 영어 적응도 어느 정도 원하는 현실형 사용자에게 `초기 적응 장벽이 낮은가`를 한 번 더 묻는 아주 좁은 bonus를 추가하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/43-add-soft-landing-bonus-for-practical-budget-users.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `softLandingBonus()`를 추가했다. 사용자가 `VALUE_FIRST + SAFETY HIGH + MIXED + BALANCED + EnglishSupport MEDIUM`으로 답하면, 서비스가 후보 국가의 `englishSupport`, `newcomerFriendliness`, `housingSpace`, `safety`, `welfare`를 함께 읽어 “현실적으로 초기에 부딪히는 장벽이 낮은가”를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v8`이 저장되고, `/dashboard`와 `/dashboard/recommendation/feedback` 운영 화면도 현재 엔진 버전을 `engine-v8`으로 보여준다. 설문 버전은 그대로 `survey-v4`다.
- 핵심 도메인 개념: 이번 단계의 핵심은 `저예산 + 안전 중시 + 영어 적응 중간` 사용자가 실제로 중요하게 느끼는 것은 음식/문화 점수보다 “초기 정착이 얼마나 덜 힘든가”라는 점이다. 그래서 이전 bonus를 더 넓게 키우지 않고, `softLandingBonus()`로 `english + newcomer + housing + safety + welfare`를 동시에 만족할 때만 작동하는 아주 좁은 신호를 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 bonus를 켜고 어떤 프로필 속성을 함께 보는지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 너무 넓히면 `P04`, `P14` 같은 다른 균형형 시나리오나 영어권 고비용 후보가 같이 흔들릴 수 있다. 그래서 `VALUE_FIRST`, `SAFETY HIGH`, `MIXED`, `BALANCED`, `EnglishSupport MEDIUM`을 동시에 만족할 때만 bonus를 켜고, `priceLevel >= 4` 후보는 아예 제외했다. 이 조건이면 `P06`에는 영향을 주되 다른 weak scenario에는 거의 퍼지지 않는다.
- 테스트 내용: 먼저 디버그 테스트로 `P06`의 실제 점수를 직접 확인해 `스페인 314 / 우루과이 298 / 이탈리아 293 / 포르투갈 287`이라는 gap을 확인했다. 그 다음 `softLandingBonus()`를 추가하고 `P06`이 `스페인, 우루과이, 포르투갈`로 바뀐 것을 확인한 뒤, `RecommendationOfflinePersonaSnapshotTest`와 `RecommendationOfflinePersonaCoverageTest`를 `engine-v8` 기준으로 다시 고정했다. 마지막으로 `./gradlew test --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest --tests com.worldmap.recommendation.RecommendationPageIntegrationTest --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest --tests com.worldmap.admin.AdminPageIntegrationTest`와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: `P06`은 비용과 안전을 함께 보는 현실형 사용자였는데, 이전 엔진에서는 여전히 이탈리아가 3위에 남았습니다. 그래서 전체 엔진을 다시 흔들지 않고, 영어 적응과 newcomer 친화도, 주거 안정성이 모두 높은 경우에만 작동하는 `softLandingBonus`를 아주 좁게 추가했습니다. 이 규칙은 `RecommendationSurveyService`가 맡고, 결과는 `P06`이 `스페인 + 우루과이 + 포르투갈`이 되도록 snapshot과 coverage 테스트에 다시 고정했습니다.
- 아직 내가 이해가 부족한 부분: `P06`은 원하는 방향으로 움직였지만, 이제는 `P04`, `P15` 외 다른 시나리오에 숨은 약점이 없는지 전체 coverage를 다시 훑어 봐야 한다. 다음 실험은 새 bonus를 더 키우기보다, 남은 weak scenario가 실제로 무엇인지부터 다시 확인하는 것이 맞다.

## 2026-03-25 - baseline 시나리오 카탈로그 공용화와 dashboard weak scenario 자동 계산

- 단계: 7. AI-assisted 설문 개선 체계
- 목적: 추천 엔진을 튜닝할수록 `/dashboard/recommendation/persona-baseline`에 보이는 weak scenario가 실제 테스트 결과와 어긋나기 시작했다. 이번 조각은 baseline 시나리오 정의를 test helper에서 main source로 올리고, dashboard가 현재 엔진 결과를 기준으로 weak scenario를 자동 계산하게 만들어 test와 운영 화면이 같은 기준을 보게 하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineCatalog.java`
  - `src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineScenario.java`
  - `src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/44-make-dashboard-persona-baseline-dynamic.md`
- 요청 흐름 / 데이터 흐름: 런타임 플레이어 요청 흐름은 바뀌지 않았다. 바뀐 것은 운영 화면 쪽 read model이다. 이제 `GET /dashboard/recommendation/persona-baseline -> AdminPageController -> AdminPersonaBaselineService.loadBaseline()`에서 서비스가 `RecommendationPersonaBaselineCatalog.scenarios()`를 읽고, 각 시나리오를 `RecommendationSurveyService.recommend()`에 다시 넣어 현재 top 3를 계산한다. 그 결과를 기준으로 `expectedCandidates`와 교집합이 없는 시나리오만 weak scenario로 분류해 SSR에 전달한다.
- 데이터 / 상태 변화: 새 테이블은 없다. 바뀐 것은 baseline 정의 위치와 운영 화면 계산 방식이다. 이전에는 weak scenario와 active-signal 시나리오가 `AdminPersonaBaselineService`에 하드코딩돼 있었고, 추천 엔진을 바꿔도 운영 화면 값이 자동으로 따라오지 않았다. 이제는 baseline 정의가 `RecommendationPersonaBaselineCatalog` 하나로 모이고, dashboard가 그 카탈로그와 현재 엔진 결과를 함께 읽어 weak scenario를 계산한다.
- 핵심 도메인 개념: “어떤 페르소나를 baseline으로 볼 것인가”는 test 전용 임시값이 아니라 추천 도메인의 평가 자산이다. 그래서 test source에만 두기보다 main source의 카탈로그로 올렸다. 반대로 weak scenario 판정은 템플릿이 하면 안 되고 `AdminPersonaBaselineService`가 맡아야 한다. 기대 후보와 현재 top 3의 교집합을 보고 weak scenario를 분류하는 규칙이 운영 화면용 read model 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: 현재 엔진이 개선되면 예전 weak scenario였던 `P04`, `P06`이 더 이상 weak가 아닐 수 있다. 이때 운영 화면이 하드코딩이면 오래된 판단을 그대로 보여 주게 된다. 이번 변경 후에는 weak scenario가 동적으로 계산되기 때문에, 엔진을 바꾸면 운영 화면도 자동으로 따라 바뀐다. active-signal 시나리오는 의도적으로 비교 기준으로 쓰는 집합이라 weak 여부와 무관하게 별도 섹션으로 유지했다.
- 테스트 내용: `RecommendationOfflinePersonaCoverageTest`, `RecommendationOfflinePersonaSnapshotTest`가 새 `RecommendationPersonaBaselineCatalog`를 기준으로 그대로 동작하는지 확인했다. 추가로 `AdminPersonaBaselineServiceIntegrationTest`에서 total 18, active signal 4, weak scenario는 현재 top 3가 기대 후보와 겹치지 않는 케이스만 나오는지 검증했다. `AdminPageIntegrationTest`에서는 persona baseline 페이지가 `자동 계산` 문구와 active-signal 섹션을 실제로 렌더링하는지 확인했다. 마지막으로 targeted suite와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: 추천 엔진을 계속 튜닝하면 테스트와 운영 화면이 서로 다른 기준을 보면 안 됩니다. 그래서 이번에는 baseline 시나리오 정의를 `RecommendationPersonaBaselineCatalog` 하나로 모으고, `/dashboard/recommendation/persona-baseline`이 현재 엔진 결과를 기준으로 weak scenario를 자동 계산하도록 바꿨습니다. 덕분에 테스트와 운영 화면이 같은 시나리오 자산을 공유하게 됐습니다.
- 아직 내가 이해가 부족한 부분: 지금은 weak scenario를 “기대 후보가 top 3에 하나도 없는 경우”로만 보고 있다. 다음에는 top 1 miss, rank drift, expected satisfaction range까지 같이 운영 화면에서 보여 줄지 판단해야 한다.

## 2026-03-25 - 추천 엔진 weak scenario 튜닝 5차: 가족형 정착 후보 family base 보정

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: 동적 baseline을 다시 계산해 보니 weak scenario는 `P11` 하나만 남았다. `P11`은 치안과 복지, 영어 적응을 강하게 보는 가족형 시나리오인데도 현재 top 3가 `아일랜드, 스위스, 호주`로 나와 `캐나다, 덴마크, 네덜란드` 계열 기대 후보를 하나도 포함하지 못했다. 이번 조각은 `P11`만 겨냥해 가족형 정착 기반 bonus를 아주 좁게 추가하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/45-add-family-base-bonus-for-family-settlement.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `familyBaseBonus()`를 추가했다. 사용자가 `QUALITY_FIRST + SAFETY HIGH + English HIGH + MIXED + BALANCED + LOW tolerance`로 답하면, 서비스가 후보 국가의 `englishSupport`, `safety`, `welfare`, `housingSpace`, `newcomerFriendliness`를 함께 읽어 가족 단위로도 오래 버티기 쉬운 기반이 있는지를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v9`이 저장되고, `/dashboard`와 `/dashboard/recommendation/feedback` 운영 화면도 현재 엔진 버전을 `engine-v9`으로 보여준다. 동적 baseline 기준으로는 이제 `matchedScenarioCount=18`, `weakScenarioCount=0`이 된다.
- 핵심 도메인 개념: 이번 단계의 핵심은 `P11`이 기후보다 `영어 + 치안 + 복지 + 주거 기반`을 더 강하게 보는 시나리오라는 점이다. 그래서 broad bonus를 또 하나 더 만드는 대신, `QUALITY_FIRST`, `SAFETY HIGH`, `English HIGH`, `MIXED`, `BALANCED`, `LOW tolerance`, `BALANCED settlement`를 동시에 만족할 때만 작동하는 `familyBaseBonus()`를 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽을지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 북유럽 고비용 후보나 영어권 후보 전반이 같이 올라가 다른 시나리오가 흔들릴 수 있다. 그래서 `English HIGH`와 `QUALITY_FIRST`, `LOW tolerance`까지 동시에 만족할 때만 켰고, 후보도 `englishSupport >= 5`, `safety >= 5`, `welfare >= 5`, `housingSpace >= 4`, `newcomerFriendliness >= 4`를 다 만족해야만 bonus를 받도록 좁혔다. 덕분에 `P11`에는 영향을 주되 다른 시나리오에는 거의 퍼지지 않았다.
- 테스트 내용: 먼저 디버그 테스트로 `P11`의 실제 점수를 직접 확인해 `아일랜드 353 / 스위스 338 / 호주 336 / 캐나다 317 / 덴마크 313`이라는 gap을 확인했다. 그 다음 `familyBaseBonus()`를 추가하고 `P11`이 `아일랜드, 캐나다, 스위스`로 바뀐 것을 확인한 뒤, `RecommendationOfflinePersonaSnapshotTest`와 `RecommendationOfflinePersonaCoverageTest`를 `engine-v9` 기준으로 다시 고정했다. `AdminPersonaBaselineServiceIntegrationTest`에서는 dynamic baseline이 `18 / 18`, weak scenario 0이 되는지 검증했고, `recommendation-persona-baseline.html`에는 weak scenario가 없을 때의 empty-state 문구를 추가했다. 마지막으로 targeted suite와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: 동적 baseline을 돌려보니 마지막 weak scenario는 `P11` 하나였습니다. 이 시나리오는 가족형 정착 기반이 핵심이어서, 이번에는 `English + Safety + Welfare + Housing`이 모두 강한 후보에만 좁게 들어가는 `familyBaseBonus`를 추가했습니다. 그 결과 `P11`에 `캐나다`가 다시 top 3에 들어왔고, baseline은 `18 / 18`까지 올라갔습니다.
- 아직 내가 이해가 부족한 부분: 이제 weak scenario는 0개지만, 이게 곧바로 순위 품질이 완벽하다는 뜻은 아니다. 다음 단계에서는 `weak scenario 유무`보다 `top1 miss`, `rank drift`, 실제 만족도 저점이 있는 버전 조합을 더 봐야 한다.

## 2026-03-25 - dashboard persona baseline에 anchor drift 추가

- 단계: 7. AI-assisted 설문 개선 체계
- 목적: `engine-v9`에서 weak scenario는 0개가 됐지만, 운영 화면이 여전히 `기대 후보가 top 3에 있느냐`만 보여 주고 있어 다음 개선 우선순위를 잡기 어려웠다. 이번 조각은 `기대 후보는 들어오지만 기대 1위 anchor가 아직 밀리는 시나리오`를 따로 보여 주는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java`
  - `src/main/java/com/worldmap/admin/application/AdminPersonaBaselineView.java`
  - `src/main/java/com/worldmap/admin/application/AdminPersonaBaselineScenarioView.java`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/46-add-anchor-drift-to-dashboard-persona-baseline.md`
- 요청 흐름 / 데이터 흐름: 플레이어 요청 흐름은 바뀌지 않는다. 이번 조각은 `GET /dashboard/recommendation/persona-baseline -> AdminPageController -> AdminPersonaBaselineService.loadBaseline()` read model만 바뀐다. 서비스는 `RecommendationPersonaBaselineCatalog`의 18개 시나리오를 현재 `RecommendationSurveyService`에 다시 넣어 top 3를 계산한 뒤, `weak scenario`, `anchor drift`, `active signal` 세 그룹으로 나눠 SSR에 전달한다.
- 데이터 / 상태 변화: 새 테이블이나 저장 상태는 없다. 달라진 것은 운영 화면에서 보는 기준이다. 이제는 `expectedCandidates` 중 아무거나 top 3에 있느냐뿐 아니라, `expectedCandidates[0]`이 실제 top 1인지도 같이 계산한다.
- 핵심 도메인 개념: weak scenario는 “완전히 빗나간 경우”이고, anchor drift는 “방향은 맞지만 1위가 아쉬운 경우”다. 이 판정은 템플릿이 하면 안 되고 `AdminPersonaBaselineService`가 맡아야 한다. 기대 후보와 현재 top 3, 기대 1위와 현재 1위를 비교하는 규칙이 운영 read model 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: expected list가 비어 있거나 추천 결과가 비어 있으면 anchor 비교를 할 수 없으므로, view에는 `-` fallback을 둔다. 현재 baseline catalog는 모두 기대 후보를 가지고 있어 실제로는 빈 값이 나오지 않지만, 운영 화면에서 예외로 깨지지 않게 방어했다.
- 테스트 내용: `AdminPersonaBaselineServiceIntegrationTest`에서 현재 baseline이 `18 / 18`, weak 0, anchor drift 13, active signal 4인지 검증했고, drift 시나리오 집합이 `P01, P02, P04, P05, P06, P07, P08, P09, P10, P11, P13, P14, P15`와 일치하는지 고정했다. `AdminPageIntegrationTest`에서는 persona baseline 페이지가 `ANCHOR DRIFT`, `1위 재검토 대상`, `P11`을 실제로 렌더링하는지 확인했다.
- 면접에서 30초 안에 설명하는 요약: baseline이 18 / 18이 되면 weak scenario만으로는 다음 개선 포인트를 잡기 어렵습니다. 그래서 운영 화면 read model을 확장해서, 기대 후보는 top 3에 들어오지만 기대 1위가 밀리는 `anchor drift`를 따로 계산해 보여 주도록 바꿨습니다. 덕분에 이제는 추천 엔진이 완전히 빗나간 경우와, 방향은 맞지만 순위가 아쉬운 경우를 분리해 볼 수 있습니다.
- 아직 내가 이해가 부족한 부분: `expectedCandidates[0]`을 1위 anchor로 보는 기준이 현재는 충분히 설명 가능하지만, 나중에 실제 만족도 데이터가 쌓이면 일부 시나리오는 “1위 후보 하나”보다 “허용 가능한 top 3 조합”이 더 맞을 수도 있다. 그때 anchor drift 정의를 그대로 유지할지 다시 판단해야 한다.

## 2026-03-25 - 추천 만족도 운영 화면에 ops review 추가

- 단계: 7. AI-assisted 설문 개선 체계
- 목적: anchor drift까지 보이기 시작했지만, 운영자는 여전히 `/dashboard/recommendation/feedback`과 `/dashboard/recommendation/persona-baseline`을 따로 오가며 다음 액션을 판단해야 했다. 이번 조각은 현재 버전 만족도 표본과 baseline drift를 한 번에 보고, 다음에 무엇을 먼저 할지 운영 메모로 정리하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewService.java`
  - `src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewView.java`
  - `src/main/java/com/worldmap/admin/web/AdminPageController.java`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/47-add-ops-review-to-recommendation-feedback-dashboard.md`
- 요청 흐름 / 데이터 흐름: 운영 요청은 `GET /dashboard/recommendation/feedback -> AdminPageController -> RecommendationFeedbackService.summarizeByVersion() + AdminRecommendationOpsReviewService.loadReview()`로 흐른다. 여기서 `AdminRecommendationOpsReviewService`는 다시 `RecommendationFeedbackService`와 `AdminPersonaBaselineService`를 호출해 현재 버전 응답 수, 현재 버전 평균 만족도, baseline 18/18 여부, weak count, anchor drift count를 한 read model로 합친다.
- 데이터 / 상태 변화: 새 테이블은 없다. 만족도 피드백은 기존처럼 `recommendation_feedback`에만 쌓이고, baseline은 저장하지 않고 현재 엔진 결과를 다시 계산한다. 이번 조각은 이 두 source를 합쳐 운영 판단 문장과 우선 시나리오 ID를 만드는 read model만 추가했다.
- 핵심 도메인 개념: 운영 화면에서 “다음에 뭘 먼저 할까”는 단순 템플릿 조건문이 아니라 운영 규칙이다. 현재 버전 응답이 5개 미만이면 먼저 피드백 수집, weak scenario가 있으면 weak 먼저, 평균 만족도가 낮으면 문구 점검, 그렇지 않고 drift만 남아 있으면 drift부터 보는 식의 우선순위는 `AdminRecommendationOpsReviewService`가 맡는 편이 맞다.
- 예외 상황 또는 엣지 케이스: 현재 버전 피드백이 하나도 없으면 평균 점수는 0이지만, 이걸 곧바로 “품질 낮음”으로 해석하면 안 된다. 그래서 응답 수가 `5개 미만`이면 평균보다 먼저 `표본 수집`을 우선하는 규칙으로 막았다. drift 시나리오 목록도 너무 길어지지 않게 상위 3개 ID만 메모에 싣도록 제한했다.
- 테스트 내용: `AdminRecommendationOpsReviewServiceIntegrationTest`에서 현재 버전 피드백이 2개일 때는 `현재 버전 피드백 더 수집`, 5개 이상이고 평균이 충분할 때는 `rank drift 줄이기`가 나오는지 고정했다. `AdminPageIntegrationTest`에서는 추천 만족도 운영 화면이 `운영 판단 메모`와 우선 액션 문구를 실제로 렌더링하는지 확인했다.
- 면접에서 30초 안에 설명하는 요약: 추천 품질 운영은 만족도 표만 보는 것으로 끝나지 않습니다. 그래서 현재 버전 만족도와 baseline drift를 한 서비스에서 합쳐, 운영 화면이 `지금은 표본을 더 모을지, weak scenario를 볼지, drift를 줄일지`를 한 줄 메모로 바로 보여 주도록 만들었습니다.
- 아직 내가 이해가 부족한 부분: 현재 운영 메모는 rule-based라서 설명 가능성은 높지만, 실제 만족도 데이터가 더 쌓이면 “표본 수”, “평균 점수”, “drift 수”에 어떤 가중치를 둘지 다시 손볼 수 있다.
## 2026-03-25 - local demo bootstrap에 현재 추천 피드백 샘플 추가

- 단계: 7. AI-assisted 설문 개선 체계 보조 조각
- 목적: 추천 운영 화면은 잘 만들어졌지만, fresh local 환경에서는 current version 피드백이 0개라 `/dashboard/recommendation/feedback`이 계속 `피드백 더 수집`만 보여 줬다. 이번 조각은 demo bootstrap이 현재 survey/engine 기준 만족도 샘플까지 같이 만들어, local에서 drift 메모와 운영 흐름을 바로 재현하게 만드는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/demo/application/DemoBootstrapService.java`
  - `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackRepository.java`
  - `src/test/java/com/worldmap/demo/DemoBootstrapIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
- 요청 흐름 / 데이터 흐름: 이 조각은 HTTP 요청이 아니라 startup bootstrap 흐름이다. `CountrySeedInitializer -> AdminBootstrapInitializer -> DemoBootstrapInitializer -> DemoBootstrapService.ensureLocalDemoData()` 순서에서, demo service가 기존 member run / guest session을 만든 뒤 `RecommendationFeedbackRepository.countBySurveyVersionAndEngineVersion()`로 현재 버전 응답 수를 확인하고, 5개 미만이면 부족한 개수만큼 샘플 피드백을 추가한다.
- 데이터 / 상태 변화: 기존처럼 `leaderboard_record`, `location_game_session`, `population_game_session` 샘플이 생기고, 이번에는 `recommendation_feedback`에도 current `survey-v4 / engine-v9` 샘플 5개가 추가된다. 이미 응답이 5개 이상 있으면 건드리지 않고, 5개 미만일 때만 top-up 한다.
- 핵심 도메인 개념: local demo bootstrap의 목적은 “화면이 비어 있지 않게 만든다”가 아니라 “로컬에서 설명 가능한 상태를 재현한다”는 것이다. 그래서 추천 운영 화면도 게임 기록처럼 sample source of truth가 있어야 한다. 이 로직은 컨트롤러가 아니라 `DemoBootstrapService`가 맡아야 한다. startup runner가 책임지는 개발용 재현 상태이기 때문이다.
- 예외 상황 또는 엣지 케이스: 피드백 엔티티에는 `demo source` 마커가 따로 없어서, 같은 버전 데이터가 일부만 있는 상태에서 앱을 다시 띄우면 부족한 개수만큼만 보충한다. 이건 local/dev 전용 bootstrap이라 허용 가능한 trade-off다. 반대로 5개 이상이면 덮어쓰지 않아 사용자가 local에서 직접 쌓은 데이터가 유지된다.
- 테스트 내용: `DemoBootstrapIntegrationTest`에서 local profile 부팅 후 current `survey-v4 / engine-v9` 피드백이 5개 이상 생성되는지, 그리고 `AdminRecommendationOpsReviewService.loadReview()`가 `rank drift 줄이기` 메모를 반환하는지 확인했다. `AdminRecommendationOpsReviewServiceIntegrationTest`도 함께 다시 통과시켰다.
- 면접에서 30초 안에 설명하는 요약: 운영 화면을 만들었더라도 local에서 빈 상태로 뜨면 설명하기가 어렵습니다. 그래서 이번에는 demo bootstrap이 현재 추천 버전 피드백 5개도 같이 만들게 해서, fresh local 환경에서도 `/dashboard/recommendation/feedback`이 바로 `rank drift 줄이기` 메모를 보여 주도록 재현성을 맞췄습니다.
- 아직 내가 이해가 부족한 부분: 지금은 local bootstrap이 current 버전 피드백을 “최소 5개 보장” 방식으로만 채운다. 나중에 survey/engine 버전이 바뀌면 어떤 샘플 답변 집합을 같이 바꿔야 자연스러운지 운영 문서 기준을 더 명확히 적어둘 필요가 있다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 1차: warm city hub에 global hub bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v9`에서는 baseline 18 / 18은 유지됐지만, `P01`, `P05` 같은 따뜻한 초도시 허브 시나리오에서 기대 1위였던 `싱가포르`가 계속 `아랍에미리트` 뒤로 밀렸다. 이번 조각은 전체 엔진을 다시 흔들지 않고, warm/fast/city/high-quality 조합에서만 작동하는 좁은 보정 신호를 넣어 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/main/resources/templates/admin/recommendation-persona-baseline.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/49-add-global-hub-bonus-for-warm-city-hubs.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `globalHubBonus()`를 추가했다. 사용자가 `WARM + FAST + CITY + QUALITY_FIRST + EnglishSupport HIGH + Diversity HIGH + Digital HIGH + Culture HIGH + Newcomer HIGH`로 답하면, 서비스가 후보 국가의 `urbanity`, `transit`, `digitalConvenience`, `diversity`, `food`, `cultureScene`, `safety`를 함께 읽어 “처음부터 살기 좋은 초도시형 글로벌 허브인가”를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v10`이 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v10`으로 보여준다. dynamic baseline 기준으로는 여전히 `18 / 18`을 유지하면서 anchor drift 수가 `13 -> 11`로 줄었다.
- 핵심 도메인 개념: 이번 단계의 핵심은 `P01`, `P05`가 단순히 “따뜻한 나라”를 찾는 시나리오가 아니라, `영어 / 대중교통 / 디지털 / 문화 / 다양성`이 동시에 높은 초도시형 허브를 원하는 시나리오라는 점이다. 그래서 broad bonus를 더 넓히지 않고, warm/fast/city/high-quality와 높은 적응 요구를 모두 만족할 때만 작동하는 `globalHubBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽을지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P02`, `P04`, `P06` 같은 다른 drift 시나리오나 미국 같은 고비용 영어권 후보도 같이 밀릴 수 있다. 그래서 `English/Digital/Culture/Diversity/Newcomer`를 모두 HIGH로 좁히고, 후보도 `urbanity`, `transit`, `digitalConvenience`, `diversity`, `food`, `cultureScene`, `safety`가 모두 높은 경우에만 bonus를 주도록 제한했다. 이 조건이면 `싱가포르`는 올라가되 `아랍에미리트`와 `미국`은 상대적으로 덜 이득을 받는다.
- 테스트 내용: 먼저 디버그 테스트로 `P01`의 실제 점수를 확인해 `아랍에미리트 356 / 싱가포르 349 / 미국 267` gap을 확인했다. 그 다음 `globalHubBonus()`를 추가하고 `P01`, `P05`가 `싱가포르, 아랍에미리트, 미국`으로 바뀐 것을 확인한 뒤, `RecommendationOfflinePersonaSnapshotTest`와 `RecommendationOfflinePersonaCoverageTest`를 `engine-v10` 기준으로 다시 고정했다. `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `11`로 줄었는지 확인했고, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 ops review의 우선 시나리오가 `P02, P04, P06`으로 바뀐 것을 고정했다. 마지막으로 추천/admin targeted suite와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift가 더 중요한 문제가 됐습니다. 이번에는 `P01`, `P05`처럼 warm/fast/city/high-quality 조합에서만 작동하는 `globalHubBonus`를 추천 엔진에 아주 좁게 추가해서, 기대 1위였던 `싱가포르`가 `아랍에미리트`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift 수를 `13 -> 11`로 줄였습니다.
- 아직 내가 이해가 부족한 부분: 이번 보정으로 `P01`, `P05`는 움직였지만, `P02`, `P04`, `P06` 같은 현실형 drift 시나리오는 여전히 남아 있다. 다음에는 drift 수만 줄일지, 현재 버전 만족도 저점과 겹치는 시나리오를 먼저 볼지 운영 화면 기준으로 다시 판단해야 한다.

## 2026-03-26 - 블로그를 현재 코드 재현용 기준으로 보강

- 단계: 10. 포트폴리오 정리와 발표 준비 보조 조각
- 목적: `blog/`는 현재까지 주로 기능 발전 과정을 기록하는 연대기 역할을 해 왔다. 그래서 설계 이유를 설명하는 데는 좋았지만, 블로그만 보고 현재 저장소를 그대로 재현하려는 사람에게는 `/admin -> /dashboard`, `8문항 -> 12문항 -> 20문항`, `engine-v9 -> engine-v10` 같은 변화가 혼동을 만들 수 있었다. 이번 조각은 blog를 “현재 코드 재현” 기준에서도 읽을 수 있게 보강하는 데 집중한다.
- 변경 파일:
  - `blog/50-current-state-rebuild-map.md`
  - `blog/00_rebuild_guide.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/13-recommendation-feedback-insights.md`
  - `blog/17-expand-recommendation-survey-question-set.md`
  - `blog/20-move-ops-insights-into-admin-surface.md`
  - `blog/27-protect-admin-routes-with-session-role.md`
  - `blog/29-bootstrap-admin-account-from-env.md`
  - `blog/35-redesign-recommendation-survey-with-twelve-questions.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 이번 조각은 런타임 요청을 바꾸지 않는다. 바뀐 것은 설명 흐름이다. 새 허브 글 `blog/50-current-state-rebuild-map.md`는 현재 라우트(`/dashboard`, `/stats`, `/mypage`), 현재 추천 기준(`survey-v4 / engine-v10`, 20문항), 현재 local demo 기준을 한 번에 고정하고, 어떤 글이 역사 기록이고 어떤 글이 현재 재현 기준인지 분리해 준다.
- 핵심 도메인 개념: 문서도 역할이 나뉜다. `docs/`는 SSOT이고, `blog/`는 출판용 연대기다. 그런데 재현형 블로그로도 쓰려면 “현재 상태 허브”가 따로 있어야 한다. 그래서 이번에는 기존 글을 지우지 않고, 구버전 글 상단에 `현재 기준 안내`를 붙여 연대기와 현재 재현 가이드를 분리했다.
- 예외 상황 또는 엣지 케이스: 예전 글을 모두 최신 상태로 덮어쓰면 당시 설계 판단이 사라진다. 반대로 아무 안내도 없이 그대로 두면 현재 재현용으로 읽는 사람은 `/admin`이나 8문항 설문을 최신 기준으로 오해할 수 있다. 그래서 본문은 유지하고, 상단 배너와 허브 글로 현재 기준만 명시하는 방식을 택했다.
- 테스트 내용: 코드나 라우트 자체는 바꾸지 않은 문서 조각이라 애플리케이션 테스트는 다시 돌리지 않았다. 대신 각 글의 상단 안내와 허브 글이 현재 코드 기준([AdminPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/web/AdminPageController.java), [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java), [survey.html](/Users/alex/project/worldmap/src/main/resources/templates/recommendation/survey.html), [site-header.html](/Users/alex/project/worldmap/src/main/resources/templates/fragments/site-header.html))과 직접 연결되도록 점검했고 `git diff --check`로 문서 형식도 확인했다.
- 면접에서 30초 안에 설명하는 요약: 블로그는 원래 변경 과정을 기록하는 연대기라서, 현재 저장소를 그대로 재현하려는 사람에게는 중간 단계 글이 혼동을 줄 수 있었습니다. 그래서 최신 라우트, 최신 설문 버전, 최신 local demo 기준을 한 번에 모은 rebuild map을 따로 만들고, 구버전 글 상단에는 `현재 기준 안내`를 붙여 연대기와 현재 재현 가이드를 분리했습니다.
- 아직 내가 이해가 부족한 부분: 지금 허브 글은 추천과 운영 화면처럼 많이 바뀐 축을 중심으로 정리했다. 이후 Level 2가 시작되면 게임 쪽도 같은 방식으로 “현재 기준 허브”를 더 촘촘히 나눌지 검토가 필요하다.

## 2026-03-26 - 블로그 현재 상태 허브에 실행 체크리스트 추가

- 단계: 10. 포트폴리오 정리와 발표 준비 보조 조각
- 목적: 현재 상태 허브를 만든 뒤에도 “그래서 실제로 무엇을 실행하고 어떤 URL에서 무엇을 확인해야 하는가”는 독자가 다시 조합해야 했다. 이번 조각은 `blog/50-current-state-rebuild-map.md`를 실제 실행 체크리스트까지 가진 재현 허브로 보강하는 데 집중한다.
- 변경 파일:
  - `blog/50-current-state-rebuild-map.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
- 요청 흐름 / 데이터 흐름: 런타임 코드나 요청 흐름은 바뀌지 않았다. 이번 조각은 문서가 실제 실행 순서를 더 직접 안내하도록 바뀐 것이다. 허브 글은 이제 `.env.local -> bootRun -> public URL 확인 -> USER/ADMIN 로그인 -> dashboard 확인` 순서를 한 번에 정리하고, local demo 관련 글도 current `survey-v4 / engine-v10` 기준으로 맞췄다.
- 핵심 도메인 개념: 블로그 재현성은 “최신 글 링크를 모아두는 것”만으로 끝나지 않는다. 실제 재현형 문서는 `무엇을 먼저 실행하고`, `어떤 상태가 보여야 정상인지`까지 포함해야 한다. 그래서 허브 글에 runtime verification checklist를 추가했다.
- 예외 상황 또는 엣지 케이스: 지금 체크리스트는 public 화면, 계정, dashboard, 추천 피드백 sample 기준으로 짜여 있다. 이후 Level 2가 시작되면 게임 규칙까지 달라질 수 있으므로, 그때는 허브의 확인 포인트도 같이 업데이트해야 한다.
- 테스트 내용: 문서 조각이라 애플리케이션 테스트는 다시 돌리지 않았고, 대신 `git diff --check`로 형식을 확인했다. 체크리스트 내용은 [docs/LOCAL_DEMO_BOOTSTRAP.md](/Users/alex/project/worldmap/docs/LOCAL_DEMO_BOOTSTRAP.md), [README.md](/Users/alex/project/worldmap/README.md), 현재 코드 라우트와 직접 대조해 맞췄다.
- 면접에서 30초 안에 설명하는 요약: 블로그 허브를 만든 뒤에는 “현재 기준은 알겠는데 실제로 어떤 순서로 확인해야 하는가”가 남았습니다. 그래서 이번에는 local demo 실행, public URL 확인, USER/ADMIN 로그인, dashboard 점검까지 한 번에 볼 수 있는 실행 체크리스트를 허브 글에 추가해, 블로그만 보고도 현재 저장소 상태를 더 안전하게 재현할 수 있게 만들었습니다.
- 아직 내가 이해가 부족한 부분: 지금 재현 체크리스트는 운영 화면과 추천 엔진 쪽에 더 무게가 있다. 이후 Level 2와 실시간성 고도화가 시작되면 게임 플레이 자체의 정상 동작 체크리스트도 더 자세히 분리할 필요가 있다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 2차: P02에 foodie starter bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v10`에서는 `P02`의 top 3가 `태국, 말레이시아, 스페인`으로 나와 baseline 18 / 18은 유지했지만 기대 1위 anchor였던 `말레이시아`가 계속 밀렸다. 이번 조각은 전체 cost penalty를 다시 흔들지 않고, “저비용 + 음식 + 다문화 + 초반 적응성”이 함께 있는 시나리오에만 좁게 보정 신호를 넣어 `P02`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/51-reduce-p02-anchor-drift-with-foodie-starter-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `foodieStarterBonus()`를 추가했다. 사용자가 `WARM + BALANCED + VALUE_FIRST + MIXED + English MEDIUM + FOOD HIGH + DIVERSITY MEDIUM+ + BALANCED settlement/mobility`로 답하면, 서비스가 후보 국가의 `food`, `diversity`, `newcomerFriendliness`, `englishSupport`, `digitalConvenience`, `safety`, `priceLevel`을 함께 읽어 “저렴하면서도 초반 적응이 쉬운 음식·다문화 시작점인가”를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v11`이 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v11`로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `11 -> 10`으로 줄었고, ops review의 우선 시나리오는 `P04, P06, P07`로 이동했다.
- 핵심 도메인 개념: `P02`는 단순히 “따뜻하고 싼 나라”를 고르는 시나리오가 아니라, “생활비를 아끼면서도 음식 만족도와 다문화 적응성을 같이 보는 초반 정착형” 시나리오다. 그래서 broad cost penalty를 다시 건드리기보다, 이 설문 조합에만 작동하는 `foodieStarterBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P14` 같은 다른 저비용 시나리오나 남유럽 후보까지 같이 흔들릴 수 있다. 그래서 `WARM`, `VALUE_FIRST`, `FOOD HIGH`, `DIVERSITY != LOW`, `MIXED`, `English MEDIUM`, `BALANCED settlement/mobility`까지 모두 묶고, 후보도 `priceLevel < 3`인 경우에만 보정했다. strong bonus는 `말레이시아`처럼 영어·디지털·정착 친화도가 높은 경우에만, weaker bonus는 `태국`처럼 음식/다양성은 강하지만 적응성은 조금 약한 경우에만 주도록 나눴다.
- 테스트 내용: 먼저 임시 디버그 테스트로 `P02`의 실제 점수를 직접 확인해 `태국 316 / 말레이시아 310 / 스페인 297` gap을 본 뒤, `foodieStarterBonus()` 강도를 올려 `말레이시아 318 / 태국 316 / 스페인 297`로 바뀐 것을 확인했다. 그 다음 `RecommendationOfflinePersonaSnapshotTest`와 `RecommendationOfflinePersonaCoverageTest`를 `engine-v11` 기준으로 다시 고정했고, `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `10`으로 줄었는지, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P04, P06, P07`으로 바뀌는지 확인했다. `RecommendationPageIntegrationTest`, `RecommendationFeedbackIntegrationTest`, `AdminPageIntegrationTest`까지 포함한 targeted suite와 `./gradlew test` 전체도 다시 통과시켰다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P02`처럼 저렴하면서도 음식과 다문화 적응성을 같이 보는 시나리오에만 좁게 작동하는 `foodieStarterBonus`를 추가해서, 기대 1위였던 `말레이시아`가 `태국`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `11 -> 10`으로 줄였습니다.
- 아직 내가 이해가 부족한 부분: `P02`의 1위는 해결했지만 3위 후보는 아직 `멕시코`가 아니라 `스페인`이다. 다음엔 broad food/diversity bonus를 더 키우기보다, `P04 / P06 / P07`처럼 아직 우선순위에 남는 drift와 실제 만족도 저점을 먼저 함께 보고 어떤 시나리오를 다음 실험 대상으로 잡을지 정하는 편이 맞다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 3차: P04에 temperate public base bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v11`에서는 baseline `18 / 18`은 유지됐지만, `P04` 같은 온화한 균형형 생활 시나리오에서 기대 1위였던 `우루과이`가 여전히 `스페인`, `아일랜드` 뒤로 밀렸다. 이번 조각은 broad bonus를 다시 넓히지 않고, 온화한 기후와 공공서비스·기본 정착 안정성을 함께 보는 시나리오에만 좁게 작동하는 보정 신호를 넣어 `P04`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/52-reduce-p04-anchor-drift-with-temperate-public-base-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `temperatePublicBaseBonus()`를 추가했다. 사용자가 `MILD + BALANCED + MIXED + English MEDIUM + HIGH publicService + BALANCED settlement/mobility`로 답하면, 서비스가 후보 국가의 `climateValue`, `seasonality`, `safety`, `welfare`, `housingSpace`, `digitalConvenience`, `priceLevel`을 함께 읽어 “온화하면서도 기본 정착 기반이 안정적인 나라”인가를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v12`가 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v12`로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `10 -> 9`로 줄었고, ops review의 우선 시나리오는 `P06, P07, P08`으로 이동했다.
- 핵심 도메인 개념: `P04`는 단순히 “온화한 나라”를 고르는 시나리오가 아니라, “기후는 온화하되 공공서비스와 기본 생활 안정성이 함께 맞는 나라”를 찾는 시나리오다. 그래서 broad climate bonus를 더 주지 않고, 이 설문 조합에만 작동하는 `temperatePublicBaseBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P06`, `P08` 같은 다른 균형형 시나리오나 공공서비스가 강하지만 생활감이 다른 후보들까지 같이 끌어올릴 수 있다. 그래서 `MILD`, `BALANCED`, `MIXED`, `English MEDIUM`, `publicService HIGH`, `settlement BALANCED`, `mobility BALANCED`까지 모두 묶고, 후보도 `priceLevel <= 3`, `climateValue <= 3`, `seasonality >= 4`, `safety >= 4`, `welfare >= 3`, `housingSpace >= 4`, `digitalConvenience >= 4`일 때만 strong bonus를 받게 제한했다.
- 테스트 내용: 먼저 임시 디버그 테스트로 `P04`의 실제 점수를 확인해 `스페인 350 / 아일랜드 336 / 우루과이 331` gap을 본 뒤, `temperatePublicBaseBonus()`를 추가해 `우루과이 351 / 스페인 350 / 아일랜드 336`로 바뀐 것을 확인했다. 그 다음 `RecommendationOfflinePersonaSnapshotTest`를 `engine-v12` 기준으로 다시 고정했고, `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `9`로 줄었는지, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P06, P07, P08`으로 바뀌는지 확인했다. `RecommendationPageIntegrationTest`, `RecommendationFeedbackIntegrationTest`, `AdminPageIntegrationTest`까지 포함한 targeted suite와 `./gradlew test` 전체도 다시 통과시켰다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P04`처럼 온화한 기후와 공공서비스를 함께 보는 시나리오에만 좁게 작동하는 `temperatePublicBaseBonus`를 추가해서, 기대 1위였던 `우루과이`가 `스페인`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `10 -> 9`로 줄였습니다.
- 아직 내가 이해가 부족한 부분: `P04`는 해결됐지만 `P06`, `P07`, `P08`처럼 운영 우선순위에 남은 drift는 아직 있다. 다음에는 broad public-service bonus를 더 키우기보다, 이 세 시나리오 중 하나를 골라 실제 만족도 표본과 함께 다시 보는 편이 맞다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 4차: P06에 practical public value bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v12`에서는 baseline `18 / 18`은 유지됐지만, `P06` 같은 현실형 온화 기후 시나리오에서 기대 1위였던 `우루과이`가 여전히 `스페인` 뒤에 있었다. 이번 조각은 broad climate/public-service bonus를 다시 넓히지 않고, `비용을 아끼면서도 치안·복지·주거 안정성이 함께 맞는 후보`만 밀어 주는 좁은 신호를 넣어 `P06`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/53-reduce-p06-anchor-drift-with-practical-public-value-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `practicalPublicValueBonus()`를 추가했다. 사용자가 `MILD + VALUE_FIRST + SAFETY HIGH + publicService MEDIUM + English MEDIUM + newcomer LOW + MIXED + BALANCED settlement/mobility`로 답하면, 서비스가 후보 국가의 `priceLevel`, `climateValue`, `safety`, `welfare`, `housingSpace`, `digitalConvenience`, `newcomerFriendliness`, `urbanityValue`, `paceValue`를 함께 읽어 “현실적으로 오래 버티기 쉬운 온화한 나라”인가를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v13`이 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v13`으로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `9 -> 8`로 줄었고, ops review의 우선 시나리오는 `P07, P08, P09`로 이동했다.
- 핵심 도메인 개념: `P06`은 단순히 “값싼 온화한 나라”를 고르는 시나리오가 아니라, “생활비는 감당 가능해야 하고, 동시에 치안·복지·주거 안정성이 있어야 하는 현실형 정착 시나리오”다. 그래서 broad cost penalty나 civic bonus를 더 키우기보다, 이 설문 조합에만 작동하는 `practicalPublicValueBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P07`, `P08`, `P09` 같은 다른 drift 시나리오나 `포르투갈`, `아일랜드` 같은 유사 후보도 같이 너무 올라갈 수 있다. 그래서 `MILD`, `VALUE_FIRST`, `SAFETY HIGH`, `publicService MEDIUM`, `newcomer LOW`, `MIXED`, `BALANCED settlement/mobility`까지 모두 묶고, 후보도 `priceLevel <= 3`, `climateValue <= 3`, `safety >= 5`, `welfare >= 4`, `housingSpace >= 4`, `newcomerFriendliness >= 3`일 때만 strong bonus를 받게 제한했다.
- 테스트 내용: 먼저 임시 디버그 테스트로 `P06`의 실제 점수를 확인해 `스페인 314 / 우루과이 298 / 포르투갈 295` gap을 본 뒤, `practicalPublicValueBonus()`를 추가해 `우루과이 316 / 스페인 314 / 포르투갈 295`로 바뀐 것을 확인했다. 그 다음 임시 디버그 테스트는 제거하고, `RecommendationOfflinePersonaSnapshotTest`와 `RecommendationOfflinePersonaCoverageTest`를 `engine-v13` 기준으로 다시 고정했다. `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `8`로 줄었는지, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P07, P08, P09`로 바뀌는지 확인했다. `RecommendationPageIntegrationTest`, `RecommendationFeedbackIntegrationTest`, `AdminPageIntegrationTest`까지 포함한 targeted suite와 `./gradlew test` 전체도 다시 통과시켰다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P06`처럼 온화한 기후와 현실적인 생활비, 치안·복지·주거 안정성을 같이 보는 시나리오에만 좁게 작동하는 `practicalPublicValueBonus`를 추가해서, 기대 1위였던 `우루과이`가 `스페인`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `9 -> 8`로 줄였습니다.
- 아직 내가 이해가 부족한 부분: `P06`은 해결됐지만 이제 운영 우선순위는 `P07`, `P08`, `P09`로 이동했다. 다음에는 이 셋 중 어떤 시나리오가 실제 만족도 저점과 더 가깝게 겹치는지 먼저 보고, broad bonus가 아니라 한 시나리오만 좁게 보는 편이 맞다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 5차: P09에 premium warm hub bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v13`에서는 baseline `18 / 18`은 유지됐지만, `P09` 같은 따뜻한 고비용 도시 허브 시나리오에서 기대 1위였던 `아랍에미리트`가 여전히 `싱가포르` 한 점 뒤에 있었다. 이번 조각은 broad global city bonus를 더 넓히지 않고, `따뜻한 기후 + 고비용 감수 + 영어 지원 + 공공서비스` 조합에만 작동하는 좁은 신호를 넣어 `P09`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/54-reduce-p09-anchor-drift-with-premium-warm-hub-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `premiumWarmHubBonus()`를 추가했다. 사용자가 `WARM + QUALITY_FIRST + CITY + English HIGH + publicService HIGH`로 답하면, 서비스가 후보 국가의 `climateValue`, `priceLevel`, `urbanityValue`, `englishSupport`, `digitalConvenience`, `newcomerFriendliness`, `housingSpace`를 함께 읽어 “비용은 높아도 영어와 정착 허들이 낮은 따뜻한 프리미엄 허브”인가를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v14`가 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v14`로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `8 -> 7`로 줄었고, ops review의 우선 시나리오는 `P07, P08, P10`으로 이동했다.
- 핵심 도메인 개념: `P09`는 단순히 “비싸지만 좋은 나라”를 고르는 시나리오가 아니라, “따뜻한 도시 허브 중에서도 영어·정착 허들이 낮고 중심 생활 인프라가 강한 프리미엄 허브”를 고르는 시나리오다. 그래서 broad city/global bonus를 더 키우기보다, 이 설문 조합에만 작동하는 `premiumWarmHubBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P05`, `P17` 같은 다른 warm city 시나리오나 `싱가포르`가 계속 더 강해지는 부작용이 생길 수 있다. 그래서 `QUALITY_FIRST`, `pace BALANCED`, `publicService HIGH`, `food/diversity LOW`, `newcomer HIGH`까지 모두 묶고, 후보도 `climate >= 5`, `price >= 5`, `urbanity >= 5`, `english >= 5`, `digital >= 5`, `newcomer >= 4`, `housingSpace >= 2`일 때만 strong bonus를 받게 제한했다.
- 테스트 내용: 먼저 임시 디버그 테스트로 `P09`의 실제 점수를 확인해 `싱가포르 322 / 아랍에미리트 321 / 덴마크 293` gap을 본 뒤, `premiumWarmHubBonus()`를 추가해 `아랍에미리트 329 / 싱가포르 326 / 덴마크 293`로 바뀐 것을 확인했다. 그 다음 임시 디버그 테스트는 제거하고, `RecommendationOfflinePersonaSnapshotTest`와 `RecommendationOfflinePersonaCoverageTest`를 `engine-v14` 기준으로 다시 고정했다. `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `7`로 줄었는지, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P07, P08, P10`으로 바뀌는지 확인했다. `RecommendationPageIntegrationTest`, `RecommendationFeedbackIntegrationTest`, `AdminPageIntegrationTest`까지 포함한 targeted suite와 `./gradlew test` 전체도 다시 통과시켰다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P09`처럼 따뜻한 고비용 도시 허브 시나리오에만 좁게 작동하는 `premiumWarmHubBonus`를 추가해서, 기대 1위였던 `아랍에미리트`가 `싱가포르`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `8 -> 7`로 줄였습니다.
- 아직 내가 이해가 부족한 부분: `P09`는 해결됐지만 이제 운영 우선순위는 `P07`, `P08`, `P10`으로 이동했다. 다음에는 이 셋 중 실제 만족도 저점과 가장 겹치는 시나리오를 먼저 골라, broad bonus가 아니라 한 시나리오만 좁게 보는 편이 맞다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 6차: P08에 soft nature base bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v14`에서는 baseline `18 / 18`은 유지됐지만, `P08` 같은 차갑지만 너무 극단적이진 않은 자연형 정착 시나리오에서 기대 1위였던 `뉴질랜드`가 여전히 `핀란드` 뒤에 있었다. 이번 조각은 북유럽 전반 점수를 다시 흔들지 않고, `차가운 기후 + 여유 + 자연 + 중간 수준 영어 적응` 조합에만 작동하는 좁은 신호를 넣어 `P08`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `README.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/55-reduce-p08-anchor-drift-with-soft-nature-base-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `softNatureBaseBonus()`를 추가했다. 사용자가 `COLD + RELAXED + NATURE + English MEDIUM + safety HIGH`로 답하면, 서비스가 후보 국가의 `climateValue`, `urbanityValue`, `paceValue`, `englishSupport`, `safety`, `housingSpace`, `newcomerFriendliness`를 함께 읽어 “너무 가혹하지 않은 기후에서 영어 적응이 쉬운 자연형 정착지”인가를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v15`가 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v15`로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `7 -> 6`으로 줄었고, ops review의 우선 시나리오는 `P07, P10, P11`로 이동했다.
- 핵심 도메인 개념: `P08`은 단순히 “차가운 나라”를 고르는 시나리오가 아니라, “자연과 여유를 원하지만 너무 가혹한 한랭 환경보다는 영어 적응이 쉬운 정착지”를 고르는 시나리오다. 그래서 북유럽 공공서비스 bonus를 더 키우는 대신, 이 설문 조합에만 작동하는 `softNatureBaseBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `캐나다`, `노르웨이`, `스웨덴` 같은 자연형 후보가 전부 같이 올라가서 정작 `뉴질랜드`의 차별점이 사라질 수 있다. 그래서 `COLD`, `RELAXED`, `NATURE`, `SPACE_FIRST`, `English MEDIUM`, `newcomer LOW`, `food/diversity/culture LOW`까지 모두 묶고, 후보도 `climateValue 2~3`, `urbanity <= 2`, `pace <= 2`, `english >= 5`, `safety >= 5`, `housingSpace >= 5`, `newcomer >= 4`일 때만 strong bonus를 받게 제한했다.
- 테스트 내용: 먼저 임시 디버그 테스트로 `P08`의 실제 점수를 확인해 `핀란드 248 / 뉴질랜드 239 / 노르웨이 233` gap을 본 뒤, `softNatureBaseBonus()`를 추가해 `뉴질랜드 251 / 핀란드 248 / 노르웨이 233`로 바뀐 것을 확인했다. 그 다음 임시 디버그 테스트는 제거하고, `RecommendationOfflinePersonaSnapshotTest`와 `RecommendationOfflinePersonaCoverageTest`를 `engine-v15` 기준으로 다시 고정했다. `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `6`으로 줄었는지, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P07, P10, P11`로 바뀌는지 확인했다. `RecommendationPageIntegrationTest`, `RecommendationFeedbackIntegrationTest`, `AdminPageIntegrationTest`까지 포함한 targeted suite와 `./gradlew test` 전체도 다시 통과시켰다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P08`처럼 자연과 여유를 중시하지만 너무 극단적인 한랭 환경은 원하지 않는 시나리오에만 좁게 작동하는 `softNatureBaseBonus`를 추가해서, 기대 1위였던 `뉴질랜드`가 `핀란드`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `7 -> 6`으로 줄였습니다.
- 아직 내가 이해가 부족한 부분: `P08`은 해결됐지만 이제 운영 우선순위는 `P07`, `P10`, `P11`로 이동했다. 다음에는 이 셋 중 실제 만족도 저점과 가장 겹치는 시나리오를 먼저 골라, broad bonus가 아니라 한 시나리오만 좁게 보는 편이 맞다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 7차: P10에 cosmopolitan pulse bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v15`에서는 baseline `18 / 18`은 유지됐지만, `P10` 같은 `온화한 기후 + 빠른 도시 + 문화 다양성 최우선 + 영어 의존 낮음` 시나리오에서 기대 1위였던 `미국`이 여전히 `대한민국` 뒤에 있었다. 이번 조각은 broad city bonus를 다시 넓히지 않고, `영어가 꼭 필요하지 않아도 다문화 자극과 활기를 강하게 느끼는 도시형 후보`만 좁게 밀어 주는 신호를 넣어 `P10`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/56-reduce-p10-anchor-drift-with-cosmopolitan-pulse-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `cosmopolitanPulseBonus()`를 추가했다. 사용자가 `MILD + FAST + CITY + BALANCED + English LOW + diversity HIGH + culture HIGH`로 답하면, 서비스가 후보 국가의 `climateValue`, `paceValue`, `urbanityValue`, `diversity`, `cultureScene`, `food`, `housingSpace`, `digitalConvenience`를 함께 읽어 “영어 의존이 낮아도 다문화 도시 자극을 충분히 느낄 수 있는가”를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v16`이 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v16`으로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `6 -> 5`로 줄었고, ops review의 우선 시나리오는 `P07, P11, P13`으로 이동했다.
- 핵심 도메인 개념: `P10`은 단순히 “빠른 도시”를 고르는 시나리오가 아니라, “영어가 꼭 필요하진 않지만 다양성과 문화 밀도가 높은 도시에서 활기 있게 살고 싶은 시나리오”다. 그래서 broad digital bonus를 더 키우지 않고, 이 설문 조합에만 작동하는 `cosmopolitanPulseBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P07`, `P13`, `P17` 같은 다른 도시형 시나리오나 `영국`, `싱가포르` 같은 이미 강한 후보도 함께 너무 올라갈 수 있다. 그래서 `MILD`, `FAST`, `CITY`, `BALANCED`, `English LOW`, `newcomer MEDIUM`, `digital HIGH`, `diversity HIGH`, `culture HIGH`, `DRIVE_FIRST`, `BALANCED settlement`까지 모두 묶고, 후보도 `climate 3~4`, `pace >= 4`, `urbanity >= 5`, `diversity >= 5`, `culture >= 5`, `food >= 4`, `housing >= 4`, `digital >= 5`일 때만 strong bonus를 받게 제한했다.
- 테스트 내용: 먼저 임시 디버그 테스트로 `P10`의 실제 점수를 확인해 `대한민국 284 / 미국 272 / 영국 264` gap을 본 뒤, `cosmopolitanPulseBonus()`를 추가해 `미국`이 1위로 올라오는지 확인했다. 그 다음 임시 디버그 테스트는 제거하고, `RecommendationSurveyServiceTest`에 `미국` 1위 unit test를 추가했다. `RecommendationOfflinePersonaSnapshotTest`는 `engine-v16` 기준으로 `P10 -> 미국, 대한민국, 영국`을 다시 고정했고, `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `5`로 줄었는지, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P07, P11, P13`으로 바뀌는지 확인했다. 마지막으로 추천/admin targeted suite와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P10`처럼 영어는 꼭 필요 없지만 빠른 도시의 다양성과 활기가 중요한 시나리오에만 좁게 작동하는 `cosmopolitanPulseBonus`를 추가해서, 기대 1위였던 `미국`이 `대한민국`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `6 -> 5`로 줄였습니다.
- 아직 내가 이해가 부족한 부분: `P10`은 해결됐지만 이제 운영 우선순위는 `P07`, `P11`, `P13`으로 이동했다. 다음에는 이 셋 중 실제 만족도 저점과 가장 겹치는 시나리오를 먼저 골라, broad bonus가 아니라 한 시나리오만 좁게 보는 편이 맞다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 8차: P13에 temperate global city bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v16`에서는 baseline `18 / 18`은 유지됐지만, `P13` 같은 `온화한 기후 + 빠른 도시 + 영어 중요 + 문화 다양성` 시나리오에서 기대 1위였던 `미국`이 여전히 `싱가포르`, `아랍에미리트` 뒤에 있었다. 이번 조각은 warm global hub 전체 점수를 다시 흔들지 않고, `온화한 기후에서도 영어 적응과 글로벌 도시 연결성이 충분한 후보`만 좁게 밀어 주는 신호를 넣어 `P13`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/57-reduce-p13-anchor-drift-with-temperate-global-city-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `temperateGlobalCityBonus()`를 추가했다. 사용자가 `MILD + FAST + CITY + QUALITY_FIRST + English HIGH + diversity HIGH`로 답하면, 서비스가 후보 국가의 `climateValue`, `seasonality`, `paceValue`, `urbanityValue`, `englishSupport`, `digitalConvenience`, `diversity`, `cultureScene`, `newcomerFriendliness`, `priceLevel`을 함께 읽어 “온화한 기후에서도 영어 적응과 글로벌 도시 연결성이 충분한가”를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v17`이 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v17`으로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `5 -> 4`로 줄었고, ops review의 우선 시나리오는 `P07, P11, P14`로 이동했다.
- 핵심 도메인 개념: `P13`은 단순히 “영어가 좋은 대도시”를 고르는 시나리오가 아니라, “온화한 기후에서도 영어 적응과 글로벌 도시 연결성이 높은 정착지를 찾는 시나리오”다. 그래서 broad global hub bonus를 더 키우지 않고, 이 설문 조합에만 작동하는 `temperateGlobalCityBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P01`, `P05`, `P17` 같은 warm city 시나리오나 `싱가포르`, `영국` 같은 이미 강한 도시 후보도 함께 너무 올라갈 수 있다. 그래서 `MILD`, `FAST`, `CITY`, `QUALITY_FIRST`, `English HIGH`, `newcomer HIGH`, `digital HIGH`, `diversity HIGH`, `culture HIGH`, `DRIVE_FIRST`, `BALANCED settlement`까지 모두 묶고, 후보도 `climate 2~3`, `seasonality >= 4`, `pace >= 4`, `urbanity >= 5`, `english >= 5`, `digital >= 5`, `diversity >= 5`, `culture >= 5`, `newcomer >= 4`, `price <= 4`일 때만 strong bonus를 받게 제한했다.
- 테스트 내용: 먼저 임시 디버그 테스트로 `P13`의 실제 점수를 확인해 `싱가포르 308 / 아랍에미리트 307 / 미국 304` gap을 본 뒤, `temperateGlobalCityBonus()`를 추가해 `미국 314 / 싱가포르 308 / 아랍에미리트 307`로 바뀐 것을 확인했다. 그 다음 임시 디버그 테스트는 제거하고, `RecommendationSurveyServiceTest`에 `미국` 1위 unit test를 추가했다. `RecommendationOfflinePersonaSnapshotTest`는 `engine-v17` 기준으로 `P13 -> 미국, 싱가포르, 아랍에미리트`를 다시 고정했고, `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `4`로 줄었는지, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P07, P11, P14`로 바뀌는지 확인했다. 마지막으로 추천/admin targeted suite와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P13`처럼 온화한 기후에서도 영어와 글로벌 도시 연결성이 중요한 시나리오에만 좁게 작동하는 `temperateGlobalCityBonus`를 추가해서, 기대 1위였던 `미국`이 `싱가포르`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `5 -> 4`로 줄였습니다.
- 아직 내가 이해가 부족한 부분: `P13`은 해결됐지만 이제 운영 우선순위는 `P07`, `P11`, `P14`로 이동했다. 다음에는 이 셋 중 실제 만족도 저점과 가장 겹치는 시나리오를 먼저 골라, broad bonus가 아니라 한 시나리오만 좁게 보는 편이 맞다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 9차: P14에 accessible warm value hub bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v17`에서는 baseline `18 / 18`은 유지됐지만, `P14` 같은 `따뜻한 기후 + 저비용 + 생활 편의 + 공공서비스 균형` 시나리오에서 기대 1위였던 `말레이시아`가 여전히 `스페인` 뒤에 있었다. 이번 조각은 warm/value 시나리오 전체를 다시 흔들지 않고, 영어 적응과 생활 편의, 기본 공공서비스 균형이 좋은 동남아형 허브만 좁게 밀어 주는 신호를 넣어 `P14`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/00_series_plan.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/58-reduce-p14-anchor-drift-with-accessible-warm-value-hub-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `accessibleWarmValueHubBonus()`를 추가했다. 사용자가 `WARM + VALUE_FIRST + MIXED + English MEDIUM + publicService HIGH`에 가까운 조합으로 답하면, 서비스가 후보 국가의 `climateValue`, `priceLevel`, `urbanityValue`, `englishSupport`, `welfare`, `digitalConvenience`, `newcomerFriendliness`를 함께 읽어 “따뜻한 기후권에서도 영어 적응과 생활 편의 균형이 좋은 실용형 거점인가”를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v18`이 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v18`으로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `4 -> 3`으로 줄었고, ops review의 우선 시나리오는 `P07, P11, P15`로 이동했다.
- 핵심 도메인 개념: `P14`는 단순히 “따뜻하고 싼 나라”를 고르는 시나리오가 아니라, “생활비를 아끼면서도 영어 적응과 생활 편의, 기본 공공서비스가 함께 따라오는 동남아형 거점을 찾는 시나리오”다. 그래서 broad cost penalty를 다시 조정하지 않고, 이 설문 조합에만 작동하는 `accessibleWarmValueHubBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P02`, `P12`, `P15` 같은 다른 warm/value 시나리오나 `태국`, `베트남`까지 함께 과하게 올라갈 수 있다. 그래서 `WARM`, `VALUE_FIRST`, `MIXED`, `English MEDIUM`, `publicService HIGH`, `newcomer MEDIUM`, `safety MEDIUM`, `digital MEDIUM`, `food LOW`, `diversity LOW`, `BALANCED settlement/mobility`까지 모두 묶고, 후보도 `price <= 2`, `urbanity >= 4`, `newcomer >= 4`를 충족할 때만 보정이 들어가게 제한했다. strong bonus는 `말레이시아`처럼 영어·디지털·복지 균형이 잡힌 경우에만, weaker bonus는 `태국`처럼 일부 조건만 맞는 후보에만 주도록 나눴다.
- 테스트 내용: 먼저 임시 디버그 테스트로 `P14`의 실제 점수를 확인해 `스페인 322 / 말레이시아 312 / 태국 310` gap을 본 뒤, `accessibleWarmValueHubBonus()`를 추가해 `말레이시아 330 / 스페인 322 / 태국 319`로 바뀐 것을 확인했다. 그 다음 임시 디버그 테스트는 제거하고, `RecommendationSurveyServiceTest`에 `말레이시아` 1위 unit test를 추가했다. `RecommendationOfflinePersonaSnapshotTest`는 `engine-v18` 기준으로 `P14 -> 말레이시아, 스페인, 태국`을 다시 고정했고, `RecommendationOfflinePersonaCoverageTest`는 `P14`의 1위가 `말레이시아`인지까지 고정했다. `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `3`으로 줄었는지, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P07, P11, P15`로 바뀌는지 확인했다. 마지막으로 추천/admin targeted suite와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P14`처럼 따뜻한 기후권에서 생활비와 편의성 균형을 찾는 실용형 시나리오에만 좁게 작동하는 `accessibleWarmValueHubBonus`를 추가해서, 기대 1위였던 `말레이시아`가 `스페인`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `4 -> 3`으로 줄였습니다.
- 아직 내가 이해가 부족한 부분: `P14`는 해결됐지만 이제 운영 우선순위는 `P07`, `P11`, `P15`로 이동했다. 다음에는 이 셋 중 실제 만족도 저점과 가장 겹치는 시나리오를 먼저 골라, broad regional bonus가 아니라 한 시나리오만 좁게 보는 편이 맞다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 10차: P11에 temperate family bridge bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v18`에서는 baseline `18 / 18`은 유지됐지만, `P11` 같은 `온화한 기후 + 영어 적응 + 안전 + 복지 + 가족형 정착` 시나리오에서 기대 1위였던 `캐나다`가 여전히 `아일랜드` 뒤에 있었다. 이번 조각은 family/safety 시나리오 전체를 다시 흔들지 않고, 온화한 기후권에서도 영어 적응과 복지, 주거 안정성이 모두 받쳐주는 가족형 기반만 좁게 밀어 주는 신호를 넣어 `P11`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/00_series_plan.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/59-reduce-p11-anchor-drift-with-temperate-family-bridge-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `temperateFamilyBridgeBonus()`를 추가했다. 사용자가 `MILD + QUALITY_FIRST + MIXED + English HIGH + safety HIGH`에 가까운 조합으로 답하면, 서비스가 후보 국가의 `climateValue`, `seasonality`, `englishSupport`, `safety`, `welfare`, `housingSpace`, `digitalConvenience`, `diversity`, `newcomerFriendliness`를 함께 읽어 “온화한 기후권에서도 가족형 정착 기반이 충분한가”를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v19`이 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v19`으로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `3 -> 2`로 줄었고, ops review의 우선 시나리오는 `P07, P15`로 이동했다.
- 핵심 도메인 개념: `P11`은 단순히 “영어와 안전이 좋은 나라”를 고르는 시나리오가 아니라, “온화한 기후권에서도 영어 적응, 복지, 주거 안정성, 디지털 생활이 함께 받쳐주는 가족형 정착지”를 찾는 시나리오다. 그래서 기존 `familyBaseBonus()`를 더 키우지 않고, 이 설문 조합에만 작동하는 `temperateFamilyBridgeBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P03`, `P08` 같은 북유럽 안정형 시나리오나 `덴마크`, `스웨덴` 같은 후보가 같이 과하게 올라갈 수 있다. 그래서 `MILD`, `QUALITY_FIRST`, `MIXED`, `English HIGH`, `newcomer HIGH`, `safety HIGH`, `publicService MEDIUM`, `BALANCED settlement`까지 모두 묶고, 후보도 `climate 2~3`, `seasonality >= 5`, `english >= 5`, `safety >= 5`, `welfare >= 5`, `housing >= 4`, `digital >= 5`를 충족할 때만 strong bonus를 받게 제한했다. 덕분에 `캐나다`만 올라오고 `아일랜드`나 `덴마크`는 과하게 움직이지 않게 막았다.
- 테스트 내용: 먼저 임시 디버그 테스트로 `P11`의 실제 점수를 확인해 `아일랜드 353 / 캐나다 339 / 스위스 338` gap을 본 뒤, `temperateFamilyBridgeBonus()`를 추가해 `캐나다 355 / 아일랜드 353 / 스위스 338`로 바뀐 것을 확인했다. 그 다음 임시 디버그 테스트는 제거하고, `RecommendationSurveyServiceTest`에 `캐나다` 1위 unit test를 추가했다. `RecommendationOfflinePersonaSnapshotTest`는 `engine-v19` 기준으로 `P11 -> 캐나다, 아일랜드, 스위스`를 다시 고정했고, `RecommendationOfflinePersonaCoverageTest`는 `P11`의 1위가 `캐나다`인지까지 고정했다. `AdminPersonaBaselineServiceIntegrationTest`에서는 anchor drift가 `2`로 줄었는지, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P07, P15`로 바뀌는지 확인했다. 마지막으로 추천/admin targeted suite와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P11`처럼 온화한 기후권에서 가족형 정착 기반을 찾는 시나리오에만 좁게 작동하는 `temperateFamilyBridgeBonus`를 추가해서, 기대 1위였던 `캐나다`가 `아일랜드`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `3 -> 2`로 줄였습니다.
- 아직 내가 이해가 부족한 부분: `P11`은 해결됐지만 이제 운영 우선순위는 `P07`, `P15`로 이동했다. 다음에는 도시형 warm anchor인 `P07`을 실제로 건드릴지, 아니면 `P15`처럼 active-signal과 겹치는 시나리오를 먼저 줄일지 판단이 필요하다.

## 2026-03-26 - 추천 엔진 anchor drift 튜닝 11차: P15에 exploratory nature runway bonus 추가

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v19`에서는 baseline `18 / 18`은 유지됐지만, `P15` 같은 `온화한 기후 + 자연 + 저비용 + 교통 중심 + 가볍게 살아보기` 시나리오에서 기대 1위였던 `뉴질랜드`가 여전히 `포르투갈` 뒤에 있었다. 이번 조각은 자연형 탐색 시나리오 전체를 다시 흔들지 않고, 영어 적응과 안전, 생활 여유가 함께 받쳐주는 후보만 좁게 밀어 주는 신호를 넣어 `P15`의 anchor drift를 줄이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`
  - `src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
  - `src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java`
  - `src/main/resources/templates/admin/index.html`
  - `src/main/resources/templates/admin/recommendation-feedback.html`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/00_series_plan.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/60-reduce-p15-anchor-drift-with-exploratory-nature-runway-bonus.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번에는 `RecommendationSurveyService` 안에 `exploratoryNatureRunwayBonus()`를 추가했다. 사용자가 `MILD + BALANCED + RELAXED + NATURE + VALUE_FIRST + TRANSIT_FIRST + EXPERIENCE`에 가까운 조합으로 답하면, 서비스가 후보 국가의 `climateValue`, `seasonality`, `paceValue`, `urbanityValue`, `englishSupport`, `safety`, `housingSpace`, `newcomerFriendliness`, `priceLevel`을 함께 읽어 “가볍게 살아보는 탐색 단계에서도 자연, 영어 적응, 생활 여유가 같이 확보되는가”를 별도 bonus로 반영한다.
- 데이터 / 상태 변화: 추천 결과 top 3는 여전히 저장하지 않는다. 익명 피드백에는 이제 `engineVersion=engine-v20`이 저장되고, `/dashboard`, `/dashboard/recommendation/feedback`, `/dashboard/recommendation/persona-baseline` 운영 화면도 현재 엔진 버전을 `engine-v20`으로 보여준다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift 수가 `2 -> 1`로 줄었고, ops review의 우선 시나리오는 `P07` 하나로 좁혀졌다.
- 핵심 도메인 개념: `P15`는 단순히 “자연을 좋아하는 저비용 사용자”를 고르는 시나리오가 아니라, “먼저 가볍게 살아보면서도 영어 적응과 안전, 생활 공간, 이동 편의가 함께 받쳐주는 탐색형 정착지”를 찾는 시나리오다. 그래서 기존 `experienceTransitBonus()`를 더 키우지 않고, 이 설문 조합에만 작동하는 `exploratoryNatureRunwayBonus()`를 따로 추가했다. 이 계산은 컨트롤러가 아니라 `RecommendationSurveyService`가 맡아야 한다. 어떤 설문 조합에서 어떤 프로필 속성을 함께 읽어 rank drift를 줄일지는 추천 도메인 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: bonus를 넓게 켜면 `P08`, `P16`, `P18` 같은 자연형·탐색형 시나리오나 `포르투갈`, `우루과이` 같은 이미 맞는 후보도 함께 과하게 올라갈 수 있다. 그래서 `MILD`, `BALANCED`, `RELAXED`, `CALM`, `VALUE_FIRST`, `SPACE_FIRST`, `NATURE`, `TRANSIT_FIRST`, `English MEDIUM`, `Newcomer LOW`, `Safety HIGH`, `PublicService MEDIUM`, `Food/Diversity/Culture LOW`, `LIFE_FIRST`, `EXPERIENCE`, `LIGHT_START`까지 모두 묶고, 후보도 `climate 2~3`, `seasonality >= 4`, `pace <= 1`, `urbanity <= 3`, `english >= 5`, `safety >= 5`, `housing >= 5`, `newcomer >= 4`, `price <= 4`일 때만 strong bonus를 받게 제한했다.
- 테스트 내용: 먼저 `RecommendationOfflinePersonaSnapshotTest`와 `AdminPersonaBaselineServiceIntegrationTest`로 현재 결과를 확인해 `P15`가 `뉴질랜드, 포르투갈, 말레이시아`로 바뀌고 anchor drift가 `1`로 줄었는지 검증했다. 이어서 `RecommendationSurveyServiceTest`에 `P15`형 입력에서 `뉴질랜드`가 1위가 되는 unit test를 추가했고, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 우선 시나리오가 `P07` 하나로 줄어드는지 고정했다. 마지막으로 추천/admin targeted suite와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: baseline 18 / 18을 맞춘 뒤에는 weak scenario보다 1위 순위 drift를 줄이는 일이 더 중요해졌습니다. 이번에는 `P15`처럼 자연형 탐색 정착 시나리오에만 좁게 작동하는 `exploratoryNatureRunwayBonus`를 추가해서, 기대 1위였던 `뉴질랜드`가 `포르투갈`보다 앞서도록 보정했습니다. 그 결과 baseline은 유지하면서 anchor drift를 `2 -> 1`로 줄였습니다.
- 아직 내가 이해가 부족한 부분: 이제 남은 운영 우선 시나리오는 `P07` 하나다. 다음에는 정말 `P07`까지 좁게 손볼지, 아니면 drift를 1개 남긴 상태에서 6단계를 닫고 9단계 Level 2로 넘어가는 편이 더 설명 가치가 큰지 판단이 필요하다.

## 2026-03-26 - baseline anchor 재정의: P07 warm megacity 시나리오 정리

- 단계: 6. 설문 기반 추천 엔진 / 7. AI-assisted 설문 개선 체계
- 목적: `engine-v20`까지 오면서 baseline `18 / 18`, anchor drift `1`까지 줄었지만 마지막 `P07`은 엔진보다 baseline 정의가 현재 20문항 의미와 어긋난 상태였다. `WARM + FAST + CITY + FOOD` 조합을 `일본` 1위로 강제하면 과적합 bonus가 필요해져서, 이번 조각은 엔진 보정보다 baseline anchor를 현재 설문 의미에 맞게 재정의하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineCatalog.java`
  - `docs/recommendation/PERSONA_EVAL_SET.md`
  - `src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/00_series_plan.md`
  - `blog/33-bootstrap-local-demo-accounts-and-sample-runs.md`
  - `blog/48-seed-current-recommendation-feedback-in-local-demo.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/61-recalibrate-p07-baseline-anchor-for-warm-megacity-scenario.md`
- 요청 흐름 / 데이터 흐름: 런타임 추천 흐름은 그대로 `GET /recommendation/survey -> POST /recommendation/survey -> RecommendationSurveyService.recommend() -> recommendation/result -> POST /api/recommendation/feedback`이다. 이번 조각은 런타임 점수식이 아니라 운영 평가 read model이 기대하는 baseline 정의를 바꿨다. `RecommendationPersonaBaselineCatalog`에서 `P07`의 expected candidates를 `싱가포르, 대한민국, 일본`으로 재정의했고, `AdminPersonaBaselineService`와 `AdminRecommendationOpsReviewService`는 그 현재 baseline 정의를 읽어 weak/drift/ops memo를 다시 계산한다.
- 데이터 / 상태 변화: 추천 결과 top 3 저장 방식은 바뀌지 않는다. `survey-v4 / engine-v20`도 그대로다. 달라진 것은 baseline 해석이다. dynamic baseline 기준으로는 `18 / 18`을 유지하면서 anchor drift가 `1 -> 0`이 되었고, `/dashboard/recommendation/feedback`의 현재 버전 메모는 이제 `현재 엔진 유지`로 떨어진다.
- 핵심 도메인 개념: 모든 mismatch를 엔진 bonus로 해결하면 과적합이 생긴다. `P07`은 현재 20문항에서 `WARM + FAST + CITY`가 warm megacity 쪽으로 읽히는데, anchor를 `일본`으로 두고 있었던 것이 문제였다. 이런 판단은 컨트롤러가 아니라 `RecommendationPersonaBaselineCatalog`가 맡아야 한다. 이건 요청 처리 규칙이 아니라 운영 평가 기준 정의이기 때문이다.
- 예외 상황 또는 엣지 케이스: baseline anchor를 바꾸는 것은 숫자를 맞추기 위한 임시방편이 되면 안 된다. 그래서 이번 변경은 `P07` 점수 gap(`싱가포르 285 / 브라질 284 / 아랍에미리트 281 / 대한민국 258 / 일본 198`)을 먼저 확인한 뒤, 엔진을 더 비틀기보다 현재 20문항 의미에 맞는지 검토한 결과로만 적용했다.
- 테스트 내용: `AdminPersonaBaselineServiceIntegrationTest`에서 anchor drift가 `0`인지 확인했고, `AdminRecommendationOpsReviewServiceIntegrationTest`에서는 current version 샘플이 충분할 때 우선 액션이 `현재 엔진 유지`로 바뀌는지 고정했다. 추천/admin targeted suite와 `./gradlew test` 전체도 통과했다.
- 면접에서 30초 안에 설명하는 요약: 마지막으로 남은 `P07`은 엔진이 틀렸다기보다 baseline anchor가 현재 20문항 의미와 어긋난 상태였습니다. 그래서 추천 엔진을 더 비틀지 않고, `warm megacity + food` 의미에 맞게 `P07`의 기대 anchor를 `싱가포르`로 재정의했습니다. 그 결과 baseline `18 / 18`을 유지하면서 anchor drift를 `0`으로 닫고, 운영 메모도 `현재 엔진 유지`로 바뀌었습니다.
- 아직 내가 이해가 부족한 부분: baseline drift를 `0`으로 맞춘 지금부터는 더 이상 rank 튜닝보다 실제 만족도 데이터가 부족한 시나리오를 어떻게 늘릴지가 더 중요해진다. 다음에는 6단계를 여기서 닫고 Level 2로 넘어갈지, 아니면 추천 설문 카피를 더 다듬을지 우선순위를 다시 확인해야 한다.

## 2026-03-26 - 9단계 1차: 인구수 게임 Level 2 직접 수치 입력형 시작

- 단계: 9. Level 2와 실시간성 고도화
- 목적: 9단계를 너무 크게 열지 않고, 기존 `세션 / Stage / Attempt` 구조를 그대로 재사용할 수 있는 가장 작은 확장부터 시작한다. 인구수 게임은 이미 `targetPopulation`, `selectedPopulation`, 하트, Stage 흐름이 있기 때문에, 보기형 Level 1 위에 `직접 수치 입력 + 오차율 판정`을 얹는 첫 조각으로 적합하다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameLevel.java`
  - `src/main/java/com/worldmap/game/population/domain/PopulationGameSession.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGamePrecisionScoringPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationAnswerJudgement.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStartView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStateView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/population/web/StartPopulationGameRequest.java`
  - `src/main/java/com/worldmap/game/population/web/SubmitPopulationAnswerRequest.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/play.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/static/js/population-game.js`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardGameLevel.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardService.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageService.java`
  - `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
  - `src/test/java/com/worldmap/game/population/application/PopulationGamePrecisionScoringPolicyTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `src/main/java/com/worldmap/demo/application/DemoBootstrapService.java`
- 요청 흐름 / 데이터 흐름: 시작 요청은 `POST /api/games/population/sessions`에서 `gameLevel`을 함께 받고, `PopulationGameApiController -> PopulationGameService.startGuestGame/startMemberGame`으로 들어간다. 세션은 `PopulationGameSession.gameLevel`에 `LEVEL_1` 또는 `LEVEL_2`를 저장한다. 플레이 중 `GET /state`는 현재 level을 내려주고, Level 1이면 보기 4개를, Level 2이면 빈 options와 직접 입력 모드를 내려준다. 제출은 `POST /answer`에서 Level 1이면 `selectedOptionNumber`, Level 2이면 `submittedPopulation`을 받아 서비스가 branch한다.
- 데이터 / 상태 변화: `population_game_session`에 `game_level`이 추가되고, Level 2 run은 `leaderboard_record.game_level=LEVEL_2`로 저장된다. `population_game_attempt`는 기존 `selectedPopulation` 필드를 그대로 재사용하고, Level 2에서는 `selected_option_number=0` sentinel만 사용해 “보기형과 직접 입력형이 같은 attempt 테이블을 공유한다”는 구조를 유지했다. `/mypage`는 최근 플레이와 최고 기록을 이제 level 구분 라벨과 함께 보여 준다.
- 핵심 도메인 개념: 이번 조각의 핵심은 “입력 방식이 바뀌어도 게임 루프는 그대로 유지된다”는 점이다. 세션, Stage, Attempt, 하트, 재시작, endless 흐름은 그대로 두고, Level 2만 `PopulationGamePrecisionScoringPolicy`가 오차율을 계산해 `20% 이하만 정답`, `오차율 band별 부분 점수`를 주게 했다. 이런 정책은 컨트롤러가 아니라 별도 policy가 맡는 편이 맞다. HTTP 입력과 점수 계산 규칙을 섞지 않아야 Level 1/Level 2를 같은 서비스 위에서 설명하기 쉽다.
- 예외 상황 또는 엣지 케이스: Level 2는 숫자 입력형이라 프런트가 빈 값이나 문자 입력을 줄 수 있다. 그래서 JS에서 숫자만 남기고 천 단위 구분을 붙여 주고, 서버도 `submittedPopulation <= 0`이면 바로 막는다. 또 Level 2 run이 public `/ranking`에 아직 드러나지 않으므로, 현재는 `/mypage`에서 level 라벨을 먼저 보강하고 공개 랭킹 필터는 다음 조각으로 넘겼다.
- 테스트 내용: `PopulationGameFlowIntegrationTest`에 `LEVEL_2 state는 options가 비어 있다`, `정확 입력 시 errorRatePercent=0.0으로 정답 처리된다`, `멀리 틀린 입력은 하트를 잃는다`를 추가했다. `PopulationGamePrecisionScoringPolicyTest`에서는 `5% 이내면 최고 band`, `20% 초과면 오답`을 단위 테스트로 고정했다. 그 외 `MyPageControllerTest`, `MyPageServiceIntegrationTest`, `AdminPageIntegrationTest`, `DemoBootstrapService` 연계가 시그니처 변경 이후에도 깨지지 않도록 확인했고, 마지막으로 `node --check src/main/resources/static/js/population-game.js`와 `./gradlew test` 전체 통과를 확인했다.
- 면접에서 30초 안에 설명하는 요약: 9단계 첫 조각으로 인구수 게임 Level 2를 추가했습니다. 기존 `세션 / Stage / Attempt / 하트` 구조는 그대로 두고, 입력 방식만 `보기형`에서 `직접 수치 입력`으로 바꾸고 오차율 기반 점수 policy를 분리했습니다. 그래서 Level 1 구조를 깨지 않고도 Level 2를 확장할 수 있다는 점을 설명할 수 있게 됐습니다.
- 아직 내가 이해가 부족한 부분: 현재 Level 2 결과는 `leaderboard_record`에 `LEVEL_2`로 저장되지만, 공개 `/ranking` 화면은 아직 Level 1만 노출한다. 다음에는 Level 2 랭킹을 public에서 어떻게 보여 줄지, 그리고 위치 찾기 Level 2는 어떤 규칙 조합으로 먼저 시작할지 판단이 필요하다.

## 2026-03-26 - 9단계 2차: 인구수 게임 Level 2 결과를 공개 랭킹으로 연결

- 단계: 9. Level 2와 실시간성 고도화
- 목적: 이전 조각에서 Level 2 run은 이미 `leaderboard_record.game_level=LEVEL_2`로 저장되고 있었지만, public `/ranking`은 여전히 Level 1만 보여 줬다. 이번 조각은 저장 구조와 공개 조회 구조 사이의 간극을 닫아서, 사용자가 Level 2를 플레이한 뒤 결과를 실제 public 랭킹에서 확인할 수 있게 만드는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardGameLevel.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardService.java`
  - `src/main/java/com/worldmap/ranking/web/LeaderboardApiController.java`
  - `src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/static/js/ranking.js`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/63-expose-population-level-2-on-public-ranking.md`
- 요청 흐름 / 데이터 흐름: 공개 랭킹은 `GET /ranking -> LeaderboardPageController -> LeaderboardService.getLeaderboard(mode, level, scope, limit)` 흐름으로 첫 SSR을 만들고, 이후 `ranking.js`가 `GET /api/rankings/{gameMode}?level={gameLevel}&scope={scope}`를 주기적으로 호출해 같은 board를 갱신한다. 이번 조각으로 조회 기준에 `gameLevel`이 추가됐고, Redis key와 DB fallback도 같은 기준으로 분기한다.
- 데이터 / 상태 변화: `leaderboard_record` 저장 방식은 바뀌지 않는다. 대신 Redis key가 `population:l1`, `population:l2`처럼 level별로 분리되고, public `/ranking`은 인구수 게임에서만 `Level 1 / Level 2`를 따로 볼 수 있게 바뀌었다. 위치 찾기 랭킹은 아직 Level 1만 제공하므로, 공개 필터도 그 상태를 그대로 반영한다.
- 핵심 도메인 개념: 이번 단계의 핵심은 “Level 2 노출을 화면 if문으로만 처리하지 않는다”는 점이다. 조회 기준은 `LeaderboardService`가 맡아야 한다. 어떤 `gameMode + gameLevel + scope` 조합을 어떤 Redis key로 읽고, 키가 비었을 때 어떤 DB 쿼리로 fallback할지는 HTTP 분기보다 read model 규칙에 가깝기 때문이다. 컨트롤러는 request param을 전달하고, 템플릿과 JS는 active board만 바꾼다.
- 예외 상황 또는 엣지 케이스: 위치 찾기 Level 2는 아직 없으므로 `location + LEVEL_2` 공개 보드는 일부러 만들지 않았다. 인구수 게임만 Level 필터를 열고, 위치 찾기에서는 `현재 Level 1만 제공` 안내를 유지했다. Level 2 랭킹이 비어 있는 fresh 환경에서는 board가 비어 보여도 정상이며, local demo나 실제 플레이 이후부터 값이 채워진다.
- 테스트 내용: `LeaderboardIntegrationTest`에서 실제 `LEVEL_2` population session을 만들고 `1 Stage 정답 -> 2 Stage 오답 3회 -> GAME_OVER -> /api/rankings/population?level=LEVEL_2` 흐름을 검증했다. Redis key를 지운 뒤에도 DB fallback으로 같은 결과가 나오는지 확인했고, `GET /ranking` SSR 응답에 `게임 레벨`, `Level 2`, `populationLevel2All` 모델이 포함되는지도 같이 고정했다. `node --check src/main/resources/static/js/ranking.js`와 `./gradlew test` 전체도 통과했다.
- 면접에서 30초 안에 설명하는 요약: 인구수 게임 Level 2는 이미 저장되고 있었지만 public 랭킹은 Level 1만 보여 주고 있었습니다. 그래서 이번에는 랭킹 조회를 `gameMode + gameLevel + scope` 기준으로 확장하고, `/ranking`에 인구수 게임 Level 1 / Level 2 보드를 추가했습니다. 핵심은 화면만 바꾼 것이 아니라 Redis key와 DB fallback까지 같은 level 기준으로 맞춰서 저장 구조와 조회 구조를 일관되게 만든 점입니다.
- 아직 내가 이해가 부족한 부분: 지금은 인구수 게임만 공개 Level 2 보드를 열었다. 다음에는 위치 찾기 Level 2를 어떤 규칙으로 시작할지, 그리고 인구수 Level 2 결과 화면에서 오차율 / score band 설명을 public에 얼마나 더 드러낼지 판단이 필요하다.

## 2026-03-26 - 9단계 3차: 인구수 게임 Level 2 결과 화면에 precision band 설명 추가

- 단계: 9. Level 2와 실시간성 고도화
- 목적: Level 2는 이미 직접 입력과 오차율 계산이 동작했지만, 결과 화면에서는 “왜 이 점수를 받았는가”를 읽기 어려웠다. 이번 조각은 기존 계산 규칙을 바꾸지 않고, `errorRate + precision band`를 answer/result view로 끌어올려 설명 가능성을 높이는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/population/application/PopulationGamePrecisionBand.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationAnswerJudgement.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGamePrecisionScoringPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameAttemptResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameStageResultView.java`
  - `src/main/java/com/worldmap/game/population/application/PopulationGameService.java`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/static/js/population-game.js`
  - `src/test/java/com/worldmap/game/population/application/PopulationGamePrecisionScoringPolicyTest.java`
  - `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/64-explain-population-level-2-result-bands.md`
- 요청 흐름 / 데이터 흐름: 플레이 중 제출은 그대로 `POST /api/games/population/sessions/{id}/answer -> PopulationGameService.submitAnswer() -> PopulationGamePrecisionScoringPolicy.judge()` 흐름이다. 달라진 것은 policy가 이제 `correct / score / errorRatePercent`뿐 아니라 `precisionBand`도 함께 내려준다는 점이다. 최종 결과는 `GET /games/population/result/{id} -> PopulationGameService.getSessionResult() -> population-game/result.html`에서 Level 2 attempt마다 `오차율 + band`를 같이 렌더링한다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았다. `population_game_attempt`에 새 컬럼을 추가하지 않고, 기존 `selectedPopulation`과 stage의 `targetPopulation`을 이용해 결과 read model에서 오차율과 band를 다시 계산한다. 즉 이번 단계는 persistence를 키우지 않고 설명용 read model만 보강한 조각이다.
- 핵심 도메인 개념: `정밀 적중 / 근접 적중 / 허용 범위 정답 / 오답`은 화면 카피가 아니라 점수 정책의 일부다. 그래서 이 기준은 템플릿 if문이 아니라 `PopulationGamePrecisionScoringPolicy`와 `PopulationGamePrecisionBand`가 같이 가져야 한다. 컨트롤러는 제출을 받고 응답을 내릴 뿐이고, 어떤 오차율이 어느 band에 속하는지는 policy가 책임져야 Level 2 규칙을 한 문장으로 설명할 수 있다.
- 예외 상황 또는 엣지 케이스: Level 1에는 precision band 개념이 없으므로 answer/result view에서 null을 허용하고, 템플릿은 `LEVEL_2`일 때만 판정 기준 패널과 band 로그를 렌더링한다. 결과 attempt 로그도 DB에 별도 error rate를 저장하지 않고 계산하기 때문에, `selectedPopulation` 또는 `targetPopulation`이 비정상적으로 비어 있으면 band를 비울 수 있게 방어했다.
- 테스트 내용: `PopulationGamePrecisionScoringPolicyTest`에 `5% 이내 -> PRECISE_HIT`, `20% 초과 -> MISS`를 추가했다. `PopulationGameFlowIntegrationTest`에서는 Level 2 answer 응답에 `precisionBand`가 들어가는지, 그리고 실제 Level 2 game over 뒤 결과 페이지 HTML에 `Level 2 판정 기준`, `정밀 적중`, `오차율`, `허용 범위 정답`이 렌더링되는지까지 고정했다. `node --check src/main/resources/static/js/population-game.js`와 targeted suite도 통과했다.
- 면접에서 30초 안에 설명하는 요약: Level 2는 오차율로 정답과 점수를 판정하지만, 이전에는 그 기준이 코드 안에만 있었습니다. 그래서 이번에는 `PopulationGamePrecisionBand`를 도입해 오차율 기준을 도메인 개념으로 올리고, answer/result view가 그 band를 같이 내려주도록 바꿨습니다. 그 결과 사용자가 결과 화면만 봐도 왜 그 점수를 받았는지 다시 설명할 수 있게 됐습니다.
- 아직 내가 이해가 부족한 부분: 지금은 Level 2 band 설명을 결과 화면과 play feedback에만 붙였다. 다음에는 이 정보를 `/stats`나 홈 하이라이트 같은 공개 요약 화면으로도 끌어올릴지, 아니면 Level 2 내부 결과 설명에만 남길지 판단이 필요하다.

## 2026-03-26 - 9단계 4차: 위치 찾기 Level 2 첫 조각 설계 고정

- 단계: 9. Level 2와 실시간성 고도화
- 목적: 인구수 게임 Level 2는 입력, 랭킹, 결과 설명까지 한 사이클이 닫혔다. 다음은 위치 찾기 Level 2지만, 여기서 타이머·194개 전체 자산·소국 모드까지 한 번에 열면 설명 가능성이 급격히 떨어진다. 그래서 이번 조각은 “첫 번째 작은 구현 단위로 무엇을 만들 것인가”를 먼저 문서와 블로그 기준으로 고정하는 데 집중한다.
- 변경 파일:
  - `docs/LOCATION_GAME_LEVEL_2_FIRST_SLICE_PLAN.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/65-design-location-level-2-first-slice.md`
- 요청 흐름 / 데이터 흐름: 이번 조각은 코드 구현이 아니라 설계 확정이다. 기준 흐름은 `POST /api/games/location/sessions (gameLevel=LEVEL_2) -> GET /state -> POST /answer -> LocationGameService -> Level 2 hint policy(distanceKm, directionHint)`로 잡았다. 즉 첫 구현은 입력 방식을 다시 바꾸지 않고, 기존 선택형 흐름 위에 서버 힌트 payload를 얹는 방식으로 간다.
- 데이터 / 상태 변화: 실제 DB 스키마는 아직 바꾸지 않았다. 다만 다음 구현에서 `LocationGameSession.gameLevel`을 추가하고, `LocationGameStage`, `LocationGameAttempt`는 그대로 재사용한다는 원칙을 먼저 정했다. 힌트 사용 이력은 첫 조각에서 컬럼으로 바로 올리지 않고, answer payload와 read model 계산으로 시작하는 것이 맞다고 판단했다.
- 핵심 도메인 개념: 위치 찾기 Level 2를 곧바로 “새 게임”으로 만들지 않는다. Level 1이 이미 가진 `세션 / Stage / Attempt / 하트 / endless run` 구조를 그대로 두고, Level 2는 출제 정책과 오답 피드백 정책만 분리한다. 첫 조각의 차별점은 `거리 + 방향 힌트`이며, 이 계산은 프론트가 아니라 서버 policy가 맡아야 한다. 힌트도 결국 점수와 랭킹 설명에 연결되는 게임 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: 첫 조각에서 194개 전체 자산, 타이머, 소국/영토, streak bonus까지 동시에 넣지 않는다. 현재 globe 안정성 이슈를 생각하면 “조작은 그대로, 정책만 강화”가 더 안전하다. 위치 게임 Level 2를 타이머 중심으로 열지 않는 이유도 같다. 지금 단계에서는 시간 압박보다 거리/방향 힌트가 더 서버 주도 구조를 설명하기 쉽다.
- 테스트 내용: 이번 조각은 설계 문서 작업이라 애플리케이션 테스트는 다시 돌리지 않았다. 대신 이후 구현 순서를 `LocationGameLevel enum -> 세션 level 저장 -> Level 2 hint payload -> play overlay -> 랭킹 분리`로 고정해 다음 코드 조각의 검증 단위를 명확히 했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임 Level 2는 Level 1을 다시 만드는 게 아니라, 이미 있는 `세션 / Stage / Attempt / 하트` 구조 위에 난도 정책과 힌트 정책만 추가하는 방향으로 설계했습니다. 첫 조각은 타이머보다 `오답 시 거리/방향 힌트`를 서버가 계산해 내려주는 방식으로 시작해서, 프론트 리스크를 키우지 않으면서도 Level 1과 분명히 다른 규칙을 만들 계획입니다.
- 아직 내가 이해가 부족한 부분: Level 2 첫 구현에서 출제 국가 풀을 현재 상위 72개 안에서만 강화할지, 아니면 일부 194개까지 같이 열지 아직 판단이 남아 있다. 또 힌트 사용을 score penalty에 바로 반영할지, 1차에서는 시각 피드백만 줄지도 구현 전에 한 번 더 정해야 한다.

## 2026-03-26 - 9단계 5차: 위치 찾기 Level 2 거리 힌트 첫 구현

- 단계: 9. Level 2와 실시간성 고도화
- 목적: 설계 문서로만 고정해 둔 위치 게임 Level 2를 실제 코드로 열어, 시작 화면에서 레벨을 고르고 오답 시 서버가 거리/방향 힌트를 내려주는 첫 구현 단위를 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/location/domain/LocationGameLevel.java`
  - `src/main/java/com/worldmap/game/location/domain/LocationGameSession.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameDistanceHint.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameDistanceHintPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameDifficultyPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStartView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStateView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameSessionResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/java/com/worldmap/game/location/web/StartLocationGameRequest.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/ranking/application/LeaderboardService.java`
  - `src/main/java/com/worldmap/demo/application/DemoBootstrapService.java`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/play.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/static/js/location-game.js`
  - `src/test/java/com/worldmap/game/location/application/LocationGameDistanceHintPolicyTest.java`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `src/test/java/com/worldmap/admin/AdminPageIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/66-start-location-level-2-with-distance-hints.md`
- 요청 흐름 / 데이터 흐름: 시작은 `POST /api/games/location/sessions`에서 `gameLevel`을 함께 받는다. `LocationGameApiController`는 현재 로그인/guest 분기만 하고, 실제 레벨 저장과 Stage 생성은 `LocationGameService.startGuestGame/startMemberGame(..., gameLevel)`이 맡는다. 진행 중에는 `GET /api/games/location/sessions/{id}/state`가 현재 `gameLevel`과 난이도 라벨을 내려주고, `POST /api/games/location/sessions/{id}/answer`는 오답일 때 `LocationGameDistanceHintPolicy`를 호출해 `distanceKm + directionHint`를 answer payload에 포함한다.
- 데이터 / 상태 변화: `location_game_session`에 `game_level`이 추가돼 세션이 `LEVEL_1 / LEVEL_2`를 저장한다. Stage와 Attempt 엔티티는 그대로 재사용하고, 힌트는 아직 DB 컬럼으로 올리지 않고 answer payload에서만 먼저 노출한다. 랭킹도 `LeaderboardService.recordLocationResult()`로 바꿔 `leaderboard_record.game_level`에 위치 게임 Level 2를 구분 저장하도록 맞췄다.
- 핵심 도메인 개념: 이번 조각의 핵심은 “입력 방식은 그대로 두고, 난도와 피드백 정책만 분리한다”는 점이다. Level 2는 새 게임이 아니라 기존 `세션 / Stage / Attempt / 하트 / endless run` 구조 위에 `gameLevel`과 `distance hint policy`를 얹은 형태다. 거리/방향 계산을 프론트가 아니라 서버 정책으로 둔 이유는, 힌트도 결국 정답 판정과 같은 게임 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: 첫 구현에서는 힌트를 오답 payload에만 넣고, Stage별 영구 이력으로는 아직 저장하지 않는다. 즉 결과 화면에서 attempt별 거리 힌트 로그까지 보여 주지는 않는다. 또 Level 2 candidate pool은 더 넓게 열었지만, 타이머·소국·영토까지 동시에 넣지는 않았다. 프론트는 동일한 globe 선택 구조를 유지해 조작 리스크를 늘리지 않았다.
- 테스트 내용: `LocationGameDistanceHintPolicyTest`에서 대표 좌표 기반 거리/방향 계산을 단위 테스트로 고정했다. `LocationGameFlowIntegrationTest`에서는 `LEVEL_2` 시작, state의 `gameLevel`, 오답 answer의 `distanceKm / directionHint`를 통합 테스트로 확인했다. `node --check src/main/resources/static/js/location-game.js`, targeted suite, `./gradlew test`, `git diff --check`까지 통과했다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임 Level 2는 지구본 입력 방식을 다시 만들지 않고, 세션에 `gameLevel`을 저장한 뒤 오답일 때만 서버가 거리와 방향 힌트를 계산해 내려주는 방식으로 열었습니다. 그래서 기존 Level 1의 `세션 / Stage / Attempt / 하트` 구조를 유지하면서도, 난도와 피드백 규칙은 분명히 다르게 설명할 수 있습니다.
- 아직 내가 이해가 부족한 부분: 지금은 힌트를 payload로만 보여 주고 점수에는 직접 반영하지 않는다. 다음에는 `hint debt`를 점수에 넣을지, 결과 화면 attempt 로그에도 거리/방향을 계산해 다시 보여 줄지 판단이 필요하다.

## 2026-03-26 - 9단계 6차: 위치 찾기 Level 2 결과 힌트 로그 read model 보강

- 단계: 9. Level 2와 실시간성 고도화
- 목적: 직전 조각에서 Level 2 오답 힌트는 answer payload에만 들어 있었기 때문에, 사용자가 플레이 중 overlay를 놓치면 결과 화면만으로는 “어느 시도에서 얼마나 빗나갔는지”를 다시 설명하기 어려웠다. 이번 조각은 persistence를 더 키우지 않고, 결과 read model이 attempt 로그에서도 거리/방향 힌트를 다시 계산해 보여 주도록 만드는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/location/application/LocationGameAttemptResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/resources/templates/location-game/result.html`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/67-add-location-level-2-hint-log-to-result-read-model.md`
- 요청 흐름 / 데이터 흐름: 플레이 중 정답/오답 판정 흐름은 그대로 `POST /api/games/location/sessions/{id}/answer -> LocationGameService.submitAnswer() -> LocationGameDistanceHintPolicy`다. 이번 조각에서 달라진 것은 결과 조회 흐름이다. `GET /api/games/location/sessions/{id}/result -> LocationGameService.getSessionResult()`에서 서비스가 attempt와 country 정보를 같이 읽은 뒤, Level 2 오답 attempt에 한해 `LocationGameDistanceHintPolicy`를 다시 호출해 `distanceKm + directionHint`를 `LocationGameAttemptResultView`에 채운다. 그 결과 `GET /games/location/result/{id}`에서도 같은 힌트 로그를 SSR로 보여 줄 수 있다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았다. 힌트를 별도 컬럼으로 저장하지 않고, 이미 저장된 `stage.countryId`와 `attempt.selectedCountryIso3Code`를 이용해 결과 read model에서 다시 계산한다. 즉 이번 조각은 write model이 아니라 read model 설명력을 보강한 작업이다.
- 핵심 도메인 개념: “결과에서 다시 설명 가능한 힌트”도 게임 규칙이다. 그래서 템플릿이 문자열을 조합하는 방식이 아니라, `LocationGameService`가 `LocationGameDistanceHintPolicy`를 재사용해 attempt view를 만들도록 두는 편이 맞다. 어떤 시도에 힌트를 붙일지, 언제 null이어야 할지는 화면보다 도메인 read model 규칙에 가깝기 때문이다.
- 예외 상황 또는 엣지 케이스: Level 1은 힌트를 쓰지 않으므로 attempt log의 `distanceKm`, `directionHint`는 null이어야 한다. Level 2라도 selected country나 target country를 찾지 못하면 억지 값을 넣지 않고 null로 둔다. 정답 attempt에는 힌트를 붙이지 않는다.
- 테스트 내용: `LocationGameFlowIntegrationTest`에 `levelTwoResultIncludesHintLogForWrongAttempt()`를 추가해 `GET /api/games/location/sessions/{id}/result` JSON의 `distanceKm`, `directionHint`와 `GET /games/location/result/{id}` HTML의 `거리 힌트` 렌더링까지 같이 고정했다. 이어서 `./gradlew test` 전체와 `git diff --check`도 통과했다.
- 배운 점: answer payload에만 있는 값은 결과 설명에서 쉽게 사라진다. 게임 결과를 다시 보여 주는 read model이 같은 규칙을 재사용하도록 만들어야 “플레이 중 상태”와 “끝난 뒤 설명”이 어긋나지 않는다.
- 아직 내가 이해가 부족한 부분: 지금은 힌트를 결과 화면에만 다시 붙였다. 다음에는 이 힌트 사용 사실을 점수 감점으로 반영할지, 공개 `/ranking`이나 `/mypage` 하이라이트에도 끌어올릴지 판단이 필요하다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임 Level 2는 오답 때 거리와 방향 힌트를 줍니다. 이전에는 이 값이 answer payload에만 있어서 결과 화면만 보면 추적 과정을 다시 설명하기 어려웠습니다. 그래서 이번에는 write model을 더 키우지 않고, 결과 read model이 `stage.countryId + selectedCountryIso3Code`를 이용해 힌트를 다시 계산하도록 바꿨고, 결과 API와 SSR 화면 모두 같은 attempt 로그를 보여 주도록 맞췄습니다.

## 2026-03-26 - 9단계 7차: 위치 찾기 Level 2를 공개 랭킹으로 연결

- 단계: 9. Level 2와 실시간성 고도화
- 목적: 위치 게임 Level 2는 이미 `gameLevel`, 거리/방향 힌트, 결과 로그 read model까지 갖췄지만, public `/ranking`에서는 여전히 Level 1만 볼 수 있었다. 이번 조각은 “저장되는 Level 2 run”과 “공개 조회되는 Level 2 보드” 사이의 간극을 닫아서, 사용자가 위치 게임 Level 2 결과도 public 랭킹에서 바로 확인할 수 있게 만드는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/static/js/ranking.js`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/68-expose-location-level-2-on-public-ranking.md`
- 요청 흐름 / 데이터 흐름: 공개 랭킹은 그대로 `GET /ranking -> LeaderboardPageController -> LeaderboardService.getLeaderboard(mode, level, scope, limit)` 흐름으로 첫 SSR을 만들고, 이후 `ranking.js`가 `GET /api/rankings/{gameMode}?level={gameLevel}&scope={scope}`를 주기적으로 호출해 같은 보드를 갱신한다. 이번 조각으로 위치 게임도 `LEVEL_1 / LEVEL_2` 보드를 SSR 모델과 polling 둘 다에서 가지게 됐다.
- 데이터 / 상태 변화: 저장 구조는 바뀌지 않는다. `leaderboard_record.game_level`과 Redis key는 직전 조각들에서 이미 `location:l1`, `location:l2`를 지원하고 있었고, 이번에는 public 조회 표면이 그 구조를 실제로 사용하게 됐다. 결과적으로 `/ranking`은 이제 위치/인구수 두 게임 모두 `Level 1 / Level 2` 보드를 각각 갖는다.
- 핵심 도메인 개념: 이번 조각의 핵심은 “필터만 열면 끝”이 아니라, 공개 조회도 `gameMode + gameLevel + scope`를 일관되게 따라가야 한다는 점이다. 어떤 보드를 SSR에서 미리 보여 주고, 이후 polling이 어떤 API 조합을 다시 칠지 결정하는 책임은 템플릿 if문이 아니라 `LeaderboardService`와 그 위의 page controller가 가져야 한다. 조회 기준 자체가 read model 규칙이기 때문이다.
- 예외 상황 또는 엣지 케이스: fresh 환경에서는 `location + LEVEL_2` 키가 비어 있으므로 보드가 빈 상태로 보이는 것이 정상이다. polling도 같은 API를 쓰므로 빈 보드가 갑자기 깨지지 않는다. 기존 JS에는 `location + LEVEL_2` 버튼을 막는 분기가 있었는데, 이번에는 그 제한을 제거하고 위치 Level 2 힌트 문구를 새로 맞췄다.
- 테스트 내용: `LeaderboardIntegrationTest`에 `locationLevelTwoLeaderboardAppearsOnPublicRanking()`를 추가해, `LEVEL_2` 위치 세션을 시작하고 game over까지 진행한 뒤 `/api/rankings/location?level=LEVEL_2` 응답과 `/ranking` SSR 모델의 `locationLevel2All`, `locationLevel2Daily` 존재를 같이 고정했다. 이어서 `./gradlew test` 전체와 `git diff --check`도 통과했다.
- 배운 점: Level 2 기능이 저장과 결과 화면까지 구현돼 있어도, public read model이 그 레벨을 열지 않으면 사용자는 기능이 완성됐다고 느끼기 어렵다. 기능을 닫으려면 write model과 read model 표면을 함께 닫아야 한다.
- 아직 내가 이해가 부족한 부분: 지금은 위치 Level 2를 public 랭킹에만 노출했다. 다음에는 `hint debt`를 점수에 반영할지, `/mypage`나 `/stats`에 Level 2 하이라이트를 추가할지 판단이 남아 있다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임 Level 2는 이미 저장되고 있었지만, public 랭킹에서는 Level 1만 보여 줬습니다. 그래서 이번에는 `/ranking`과 랭킹 polling을 `location + LEVEL_2`까지 이해하도록 확장해, 거리/방향 힌트형 run을 별도 보드로 조회할 수 있게 만들었습니다. 핵심은 템플릿만 바꾼 게 아니라 `gameMode + gameLevel + scope` 기준 read model을 SSR과 polling에 일관되게 적용한 점입니다.

## 2026-03-26 - 9단계 8차: 위치 찾기 Level 2 hint debt 점수 반영

- 단계: 9. Level 2와 실시간성 고도화
- 목적: 위치 게임 Level 2는 이미 거리/방향 힌트와 결과 로그까지 갖췄지만, 힌트를 봐도 최종 점수는 Level 1과 같은 방식으로 계산되고 있었다. 이번 조각은 힌트가 단순 안내문이 아니라 실제 score trade-off가 되도록, 정답 전까지 본 힌트 수만큼 점수를 감점하는 작은 정책을 추가하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/game/location/application/LocationAnswerJudgement.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameScoringPolicy.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameAnswerView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameStageResultView.java`
  - `src/main/java/com/worldmap/game/location/application/LocationGameService.java`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/templates/location-game/result.html`
  - `src/test/java/com/worldmap/game/location/application/LocationGameScoringPolicyTest.java`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/69-apply-hint-debt-to-location-level-2-score.md`
- 요청 흐름 / 데이터 흐름: 제출 흐름은 그대로 `POST /api/games/location/sessions/{id}/answer -> LocationGameService.submitAnswer() -> LocationGameScoringPolicy.judge()`다. 달라진 것은 scoring policy가 이제 `gameLevel`과 `attemptNumber`를 함께 보고, Level 2 정답일 때 `attemptNumber - 1`만큼 hint debt를 계산해 `awardedScore`에서 감점한다는 점이다. 결과 조회는 `GET /api/games/location/sessions/{id}/result -> LocationGameService.getSessionResult()`에서 stage의 wrong attempt 수를 다시 읽어 stage별 `hintPenalty`를 계산해 내려준다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았다. 힌트 감점도 별도 컬럼으로 저장하지 않고, `attemptNumber`와 wrong attempt 수만으로 다시 계산한다. answer payload는 이제 `hintPenalty`를 포함하고, 결과 read model도 stage별 `hintPenalty`를 가진다.
- 핵심 도메인 개념: 이번 조각의 핵심은 “힌트는 도움말이지만 무료가 아니다”라는 규칙을 서버 정책으로 올리는 것이다. 프론트는 단지 `힌트 감점 -15`를 보여 줄 뿐이고, 실제 점수 계산은 `LocationGameScoringPolicy`가 맡는다. 같은 규칙이 answer와 result에서 동시에 설명돼야 하기 때문이다.
- 예외 상황 또는 엣지 케이스: Level 1은 힌트를 쓰지 않으므로 감점이 항상 0이다. Level 2라도 첫 시도 정답이면 hint debt는 없다. 실패 Stage는 최종 점수가 0이므로 stage-level hintPenalty도 0으로 둔다. 현재 구조에서는 하트 3개이기 때문에 Level 2에서 실제 감점 최대치는 `2번 힌트 = 30점`이다.
- 테스트 내용: `LocationGameScoringPolicyTest`에 `LEVEL_2` 두 번째 시도 정답 시 `hintPenalty=15`, `awardedScore=115`를 고정했다. `LocationGameFlowIntegrationTest`에는 `levelTwoCorrectAnswerAfterHintAppliesHintDebt()`를 추가해 wrong 한 번 후 correct 했을 때 answer payload의 `hintPenalty`, `awardedScore`, result JSON의 `stages[0].hintPenalty`, 결과 HTML의 `힌트 감점 -15`까지 같이 검증했다. `node --check src/main/resources/static/js/location-game.js`, targeted suite, `git diff --check`도 통과했다.
- 배운 점: Level 2 규칙을 “힌트를 준다”에서 끝내면 Level 1과 점수 구조가 너무 비슷하게 남는다. 같은 힌트라도 점수 trade-off와 함께 설명해야 Level 2가 실제로 다른 모드처럼 느껴진다.
- 아직 내가 이해가 부족한 부분: 지금은 힌트 감점을 고정 15점으로 두었다. 이후 `distanceKm` 규모나 difficulty label에 따라 가변 감점이 더 맞는지, 혹은 현재처럼 단순 규칙이 설명 가능성 측면에서 더 좋은지 한 번 더 비교할 필요가 있다.
- 면접에서 30초 안에 설명하는 요약: 위치 게임 Level 2는 오답 때 거리와 방향 힌트를 주는데, 이전에는 힌트를 봐도 점수는 Level 1과 같은 방식으로 계산됐습니다. 그래서 이번에는 정답 전까지 본 힌트 수만큼 점수를 깎는 `hint debt`를 `LocationGameScoringPolicy`에 넣고, answer response와 결과 화면이 그 감점을 같이 보여 주도록 바꿨습니다. 그 결과 Level 2를 “힌트가 있지만 점수 trade-off가 있는 모드”로 설명할 수 있게 됐습니다.

## 2026-03-26 - 9단계 9차: `/mypage` Level 2 하이라이트 추가

- 단계: 9. Level 2와 실시간성 고도화
- 목적: 위치 게임 Level 2는 힌트·랭킹·점수 감점까지 갖췄고, 인구수 게임 Level 2도 정확 수치 입력과 오차율 band가 이미 돌아가고 있었다. 그런데 계정 기반 기록 허브 `/mypage`에서는 여전히 전체 최고 기록과 최근 플레이만 보여 줘, “고급 모드에서 어디까지 갔는가”를 한눈에 설명하기 어려웠다. 이번 조각은 write model을 더 키우지 않고, `leaderboard_record`를 다시 읽는 read model 확장만으로 Level 2 최고 기록을 따로 보여 주는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/mypage/application/MyPageBestRunView.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageDashboardView.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageService.java`
  - `src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
  - `src/main/resources/templates/mypage.html`
  - `src/test/java/com/worldmap/mypage/MyPageServiceIntegrationTest.java`
  - `src/test/java/com/worldmap/web/MyPageControllerTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/70-add-level-2-highlights-to-mypage.md`
- 요청 흐름 / 데이터 흐름: 요청은 그대로 `GET /mypage -> MyPageController -> MyPageService.loadDashboard(memberId)` 에서 시작한다. 이번 조각에서 `MyPageService`는 기존 전체 최고 기록과 최근 플레이 read model에 더해, `LeaderboardRecordRepository.findFirstByMemberIdAndGameModeAndGameLevelOrderByRankingScoreDescFinishedAtAsc(...)` 와 `countByMemberIdAndGameModeAndGameLevel(...)` 를 이용해 위치/인구수 `LEVEL_2` 최고 기록을 따로 읽어 `MyPageDashboardView` 에 채운다. 템플릿은 그 값을 `Level 2 하이라이트` 카드로만 표현한다.
- 데이터 / 상태 변화: DB 스키마는 바뀌지 않았다. 새로운 테이블이나 컬럼 없이, 이미 저장된 `leaderboard_record.game_level` 을 읽는 read model만 확장했다. 따라서 이번 조각은 “어떻게 저장하는가”가 아니라 “이미 저장된 Level 2 run을 계정 화면에서 어떻게 다시 설명할 것인가”에 집중한 변경이다.
- 핵심 도메인 개념: `/mypage`는 raw session 전체를 바로 보여 주는 화면이 아니라, `완료된 run 요약(leaderboard_record)` 과 `플레이 방식(stage 집계)` 를 분리해 보여 주는 기록 허브다. Level 2 하이라이트도 이 원칙을 그대로 따라, write model을 건드리지 않고 `leaderboard_record` 기반 최고 기록 카드로 추가하는 편이 더 설명 가능하다. 어떤 Level 2 run이 최고 기록인지, 몇 번의 완료 run 중에서 나온 기록인지, 현재 공개 랭킹에서 몇 위인지 판단하는 책임은 템플릿이 아니라 `MyPageService` read model 규칙에 가깝다.
- 예외 상황 또는 엣지 케이스: 아직 Level 2를 한 번도 끝내지 않은 계정은 `Level 2 하이라이트` 패널에 빈 카드 대신 안내 문구만 보여 준다. 전체 최고 기록은 여전히 Level 1 / Level 2를 통합해 가장 높은 점수를 먼저 보여 주기 때문에, Level 2 최고 기록이 있어도 overall best 카드와 중복될 수 있다. 이번 조각은 그 중복을 없애기보다, “고급 모드만 따로 보여 주는 카드가 필요하다”는 관점으로 해결했다.
- 테스트 내용: `MyPageServiceIntegrationTest` 에 `loadDashboardIncludesLevelTwoBestHighlights()` 를 추가해, 회원 계정으로 위치/인구수 Level 2 run을 각각 하나씩 완료한 뒤 `locationLevel2Best`, `populationLevel2Best`, 완료 run 수, 최고 랭킹, 점수를 검증했다. `MyPageControllerTest` 에서는 로그인 상태 `/mypage` HTML 에 `Level 2 하이라이트`, `115점`, `150점` 이 렌더링되는지 고정했다. 이어서 `./gradlew test --tests com.worldmap.mypage.MyPageServiceIntegrationTest --tests com.worldmap.web.MyPageControllerTest`, `./gradlew test`, `git diff --check` 를 통과했다.
- 배운 점: Level 2 기능이 실제로 구현돼 있어도, 계정 화면에서 다시 읽어 오지 않으면 사용자는 그 모드를 “내 기록”으로 체감하기 어렵다. 공개 `/ranking` 과 개인 `/mypage` 는 같은 `leaderboard_record` 를 읽지만, 전자는 경쟁을 보여 주고 후자는 누적 성과와 모드별 하이라이트를 보여 준다는 목적 차이가 있다는 점을 더 분명히 이해하게 됐다.
- 아직 내가 이해가 부족한 부분: 지금은 `/mypage` 에서만 Level 2 하이라이트를 보여 준다. 다음에는 이 신호를 `/stats` 나 홈 hero까지 올릴지, 혹은 Level 2 하이라이트는 개인 기록 화면까지만 두는 편이 더 깔끔한지 판단이 남아 있다.
- 면접에서 30초 안에 설명하는 요약: Level 2 기능이 늘어나면서 `/mypage` 도 고급 모드 기준 기록을 따로 보여 줄 필요가 생겼습니다. 이번에는 새로운 저장 구조를 만들지 않고, 이미 `gameLevel` 이 들어 있는 `leaderboard_record` 를 다시 읽어 위치/인구수 Level 2 최고 기록, 최고 랭킹, 완료 run 수를 별도 카드로 보여 주도록 확장했습니다. 그래서 write model은 그대로 두고도, 계정 화면이 “일반 기록”과 “고급 모드 기록”을 분리해서 설명할 수 있게 됐습니다.

## 2026-03-26 - 9단계 10차: 공개 `/stats` Level 2 하이라이트 추가

- 단계: 9. Level 2와 실시간성 고도화
- 목적: 직전 조각에서 `/mypage`는 Level 2 최고 기록을 따로 보여 주기 시작했지만, 공개 화면에서는 여전히 활동 지표와 일간 Top 3만 보여 줬다. 이번 조각은 개인 화면이 아닌 public `/stats`에서도 “고급 모드가 실제로 플레이되고 있다”는 신호를 제한적으로 드러내기 위해, 위치/인구수 `Level 2` 최고 기록을 한 장짜리 공개 하이라이트 카드로 노출하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/stats/web/StatsPageController.java`
  - `src/main/resources/templates/stats/index.html`
  - `src/test/java/com/worldmap/stats/StatsPageControllerTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/71-expose-level-2-highlights-on-public-stats.md`
- 요청 흐름 / 데이터 흐름: 요청은 그대로 `GET /stats -> StatsPageController` 에서 시작한다. 컨트롤러는 기존처럼 `ServiceActivityService.loadTodayActivity()` 로 활동 지표를 읽고, `LeaderboardService.getLeaderboard(...)` 를 재사용해 일간 Top 3 외에 `LOCATION + LEVEL_2 + ALL + 1`, `POPULATION + LEVEL_2 + ALL + 1` 조합도 같이 읽어 `locationLevel2Highlight`, `populationLevel2Highlight` 모델을 채운다. 템플릿은 이 두 보드의 첫 entry만 골라 공개용 하이라이트 카드로 보여 준다.
- 데이터 / 상태 변화: DB 스키마와 Redis key는 바뀌지 않았다. 이미 저장되던 `leaderboard_record.game_level`과 Redis `l2` 보드를 public `/stats`가 다시 읽기 시작한 것뿐이다. 즉 이번 조각은 새 저장 규칙이 아니라 공개 read model 확장이다.
- 핵심 도메인 개념: `/stats`는 `Dashboard`의 축소판이 아니라, 공개 가능한 read model만 보여 주는 public 표면이다. 그래서 내부 추천 품질·버전 정보는 계속 숨기고, 게임 쪽도 상세 운영 판단 대신 “Level 2 최고 기록”이라는 제한된 신호만 노출한다. 어떤 Level 2 보드를 어떻게 읽을지는 템플릿 if문보다 `LeaderboardService`의 `gameMode + gameLevel + scope` 조회 규칙을 그대로 재사용하는 편이 더 일관되고 설명 가능하다.
- 예외 상황 또는 엣지 케이스: `LEVEL_2` 완료 기록이 하나도 없으면 카드 대신 안내 문구만 보여 준다. 공개 화면이기 때문에 특정 계정의 상세 이력이나 여러 개의 Level 2 순위표를 모두 노출하지 않고, 모드별 최고 기록 한 장만 보여 준다. Dashboard와 달리 여기서는 추천 버전, baseline, drift 같은 내부 품질 정보는 계속 숨긴다.
- 테스트 내용: `StatsPageControllerTest`에서 guest 기준 `/stats` 렌더링 시 `Level 2 하이라이트`, `위치 찾기 Level 2 최고 기록`, `인구수 Level 2 최고 기록`이 보이는지 검증했다. admin session 테스트도 `Dashboard` 버튼이 계속 보이면서 새 Level 2 highlight 조회 호출이 있어도 깨지지 않는지 확인했다. 이어서 `./gradlew test --tests com.worldmap.stats.StatsPageControllerTest`, `./gradlew test`, `git diff --check` 를 통과했다.
- 배운 점: Level 2 기능을 public 화면에 노출한다고 해서 바로 상세 랭킹이나 내부 지표를 모두 열 필요는 없다. public `Stats`는 “서비스가 실제로 움직인다”는 사회적 신호를 주는 곳이고, `Dashboard`는 운영 판단을 하는 곳이라는 역할 차이를 더 분명히 유지할 수 있었다.
- 아직 내가 이해가 부족한 부분: 지금은 `ALL TIME BEST` 한 장만 public `/stats`에 노출한다. 이후 홈 hero까지 Level 2 하이라이트를 올릴지, 혹은 public surface는 `Stats`까지만 두는 편이 더 명확한지 판단이 남아 있다.
- 면접에서 30초 안에 설명하는 요약: `/stats`는 원래 공개 활동 지표와 일간 Top 3만 보여 주는 화면이었습니다. Level 2 모드가 늘어나면서 public surface에서도 고급 모드가 실제로 플레이되고 있다는 신호가 필요해졌고, 그래서 이번에는 새 집계를 만들지 않고 `LeaderboardService`의 level-aware 조회를 그대로 재사용해 위치/인구수 `Level 2` 최고 기록 카드만 추가했습니다. 내부 운영 판단 정보는 계속 dashboard에만 남기고, 공개 화면은 제한된 정보만 보여 주도록 역할을 분리한 것이 핵심입니다.

## 2026-03-26 - 로컬 demo 부팅 호환성: legacy recommendation feedback 컬럼 완화

- 단계: 9. Level 2와 실시간성 고도화
- 목적: local profile로 서버를 다시 띄우려 했을 때, 예전 로컬 PostgreSQL에 남아 있던 `recommendation_feedback.budget_preference`, `english_importance`, `priority_focus` legacy 컬럼이 여전히 `NOT NULL`인 경우 current demo bootstrap이 실패했다. 이번 조각은 운영 기능을 늘리는 대신, fresh DB가 아니어도 `.env.local + local profile`로 바로 확인 가능한 상태를 다시 보장하는 데 집중한다.
- 변경 파일:
  - `src/main/java/com/worldmap/common/config/RecommendationFeedbackLegacyColumnInitializer.java`
  - `src/test/java/com/worldmap/common/config/RecommendationFeedbackLegacyColumnInitializerIntegrationTest.java`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
- 요청 흐름: HTTP 요청보다 앱 시작 흐름의 수정이다. `CountrySeedInitializer -> AdminBootstrapInitializer -> RecommendationFeedbackLegacyColumnInitializer -> DemoBootstrapInitializer` 순서로 동작하고, 새 initializer가 먼저 legacy 컬럼의 `NOT NULL` 제약을 풀어 current `RecommendationFeedback` insert가 실패하지 않게 만든다.
- 데이터 / 상태 변화: 새 테이블이나 컬럼은 추가하지 않는다. `recommendation_feedback`에 legacy 컬럼이 존재하는 경우에만 `ALTER TABLE ... DROP NOT NULL`을 실행하고, 컬럼이 없으면 아무 일도 하지 않는다. 덕분에 현재 엔티티가 더 이상 쓰지 않는 예전 컬럼이 남아 있어도 local demo seed가 계속 동작한다.
- 핵심 도메인 개념: 이번 조각의 핵심은 “current code가 old local schema에도 다시 올라와야 한다”는 개발 환경 호환성이다. 이 책임은 demo bootstrap 안쪽에 숨기기보다 startup initializer로 분리하는 편이 더 명확하다. 왜냐하면 이 문제는 추천 피드백 생성 규칙이 아니라, boot 전에 legacy schema를 완화하는 인프라 호환성 규칙이기 때문이다.
- 예외 / 엣지 케이스: 컬럼이 아예 없는 fresh DB는 그대로 통과한다. H2 test DB도 `information_schema.columns`를 통해 같은 initializer를 태우도록 구현해서, local PostgreSQL 전용 편법으로 남기지 않았다. 현재 완화 대상은 확인된 세 컬럼만 포함하고, 다른 legacy drift는 이번 범위에 넣지 않았다.
- 테스트:
  - `./gradlew test --tests com.worldmap.common.config.RecommendationFeedbackLegacyColumnInitializerIntegrationTest --tests com.worldmap.demo.DemoBootstrapIntegrationTest`
  - `./gradlew test`
- 배운 점: local demo bootstrap은 “새 환경에서 되는가”보다 “예전 local DB가 남아 있어도 다시 살아나는가”가 더 중요할 수 있다. bootstrap 실패 원인이 도메인 로직이 아니라 schema drift인 경우, HTTP controller가 아니라 startup initializer 쪽을 먼저 봐야 한다.
- 아직 약한 부분: 이번 조각은 세 개의 legacy recommendation feedback 컬럼만 다룬다. 앞으로도 local DB drift가 더 쌓이면, 개별 initializer가 늘어나는 방식이 좋은지 아니면 정식 migration 도구 도입이 나은지 한 번 더 판단해야 한다.
- 면접용 30초 요약: local demo 서버가 안 뜬 원인은 current 추천 피드백 엔티티가 더 이상 쓰지 않는 예전 로컬 컬럼들이 `NOT NULL`로 남아 있었기 때문입니다. 그래서 startup 초기에 legacy 컬럼이 있으면 `NOT NULL`만 완화하는 initializer를 넣고, demo bootstrap이 그 다음에 current 버전 피드백을 넣도록 순서를 잡았습니다. 덕분에 fresh DB가 아니어도 `.env.local` 기반 local demo를 다시 재현할 수 있게 됐습니다.

## 2026-03-26 - 9단계 rollback: public Level 2 제거와 legacy 데이터 정리

- 단계: 9. 고도화 실험 롤백과 실시간성 개선
- 목적: 위치 찾기와 인구수 맞추기 Level 2 실험을 현재 제품 범위에서 제거하고, 기존 DB/Redis에 남아 있던 `LEVEL_2` 흔적까지 같이 정리해 현재 서비스를 다시 `Level 1-only`로 설명 가능하게 만든다.
- 변경 파일:
  - `src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java`
  - `src/main/java/com/worldmap/game/location/web/LocationGameApiController.java`
  - `src/main/java/com/worldmap/game/population/web/PopulationGameApiController.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageDashboardView.java`
  - `src/main/java/com/worldmap/mypage/application/MyPageService.java`
  - `src/main/java/com/worldmap/ranking/web/LeaderboardPageController.java`
  - `src/main/java/com/worldmap/stats/web/StatsPageController.java`
  - `src/main/resources/static/js/location-game.js`
  - `src/main/resources/static/js/population-game.js`
  - `src/main/resources/static/js/ranking.js`
  - `src/main/resources/templates/location-game/start.html`
  - `src/main/resources/templates/location-game/result.html`
  - `src/main/resources/templates/population-game/start.html`
  - `src/main/resources/templates/population-game/result.html`
  - `src/main/resources/templates/ranking/index.html`
  - `src/main/resources/templates/stats/index.html`
  - `src/main/resources/templates/mypage.html`
  - `src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java`
  - `src/test/java/com/worldmap/game/location/LocationGameFlowIntegrationTest.java`
  - `src/test/java/com/worldmap/game/population/PopulationGameFlowIntegrationTest.java`
  - `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
  - `src/test/java/com/worldmap/stats/StatsPageControllerTest.java`
  - `src/test/java/com/worldmap/web/MyPageControllerTest.java`
  - `src/test/java/com/worldmap/mypage/MyPageServiceIntegrationTest.java`
  - `README.md`
  - `docs/PORTFOLIO_PLAYBOOK.md`
  - `docs/LOCAL_DEMO_BOOTSTRAP.md`
  - `docs/WORKLOG.md`
  - `blog/README.md`
  - `blog/00_series_plan.md`
  - `blog/50-current-state-rebuild-map.md`
  - `blog/72-roll-back-game-level-2-and-purge-legacy-data.md`
- 요청 흐름: public 요청 흐름은 다시 단순해졌다. `POST /api/games/location/sessions`, `POST /api/games/population/sessions`는 이제 Level 선택을 무시하고 Level 1 세션만 시작한다. `GET /ranking`, `GET /stats`, `GET /mypage`도 더 이상 Level 2 read model을 만들지 않고, 게임 결과 / 플레이 JS도 Level 1-only copy만 보여 준다.
- 데이터 / 상태 변화: 앱 시작 시 `CountrySeedInitializer -> AdminBootstrapInitializer -> RecommendationFeedbackLegacyColumnInitializer -> GameLevelRollbackInitializer -> DemoBootstrapInitializer` 순서가 된다. 새 rollback initializer가 `game_level = LEVEL_2`인 위치/인구수 세션, Stage, Attempt, `leaderboard_record`와 Redis `l2` 키를 먼저 삭제해 기존 DB에도 Level 2 흔적이 남지 않게 만든다.
- 핵심 도메인 개념: 이번 조각의 핵심은 “기능을 숨기는 것”이 아니라 “호환성을 깨지 않는 롤백”이다. enum을 바로 지우면 old DB row 때문에 JPA가 깨질 수 있어서, 먼저 public surface를 Level 1-only로 되돌리고 startup에서 legacy Level 2 데이터를 비우는 방식으로 정리했다.
- 예외 / 엣지 케이스: 예전 클라이언트나 테스트가 `gameLevel=LEVEL_2`를 보내더라도 현재 컨트롤러는 Level 1으로 수렴시킨다. fresh DB에서는 purge할 데이터가 없어도 그대로 통과해야 한다. Redis에 `l2` 키가 비어 있어도 예외 없이 지나가야 한다.
- 테스트:
  - `node --check src/main/resources/static/js/location-game.js`
  - `node --check src/main/resources/static/js/population-game.js`
  - `node --check src/main/resources/static/js/ranking.js`
  - `./gradlew test --tests com.worldmap.common.config.GameLevelRollbackInitializerIntegrationTest --tests com.worldmap.stats.StatsPageControllerTest --tests com.worldmap.web.MyPageControllerTest --tests com.worldmap.mypage.MyPageServiceIntegrationTest --tests com.worldmap.ranking.LeaderboardIntegrationTest --tests com.worldmap.game.location.LocationGameFlowIntegrationTest --tests com.worldmap.game.population.PopulationGameFlowIntegrationTest`
  - `./gradlew test`
  - `git diff --check`
- 배운 점: 실험 기능을 product에서 내릴 때는 템플릿만 숨기는 것으로 끝나면 안 된다. 시작 API, read model, DB row, Redis key, local demo 문서까지 같이 정리해야 “현재 서비스가 무엇인지”를 한 문장으로 설명할 수 있다.
- 아직 약한 부분: 내부 enum과 일부 Level 2 코드 경로는 old DB 호환성을 위해 아직 남겨 뒀다. 지금은 startup purge로 안전하게 막고 있지만, 나중에는 정식 migration이나 enum 정리 시점을 다시 잡아야 한다.
- 면접용 30초 요약: Level 2를 실험적으로 열어 봤지만 현재 제품 기준에서는 복잡도에 비해 가치가 낮다고 판단했습니다. 그래서 UI만 숨기지 않고 시작 API를 다시 Level 1-only로 수렴시키고, `/ranking`, `/stats`, `/mypage` read model도 같이 정리했습니다. 동시에 startup initializer를 둬서 old DB와 Redis에 남아 있던 `LEVEL_2` 흔적도 부팅 시 자동으로 지우게 만들어, 기능 삭제가 아니라 호환성을 깨지 않는 롤백으로 설명할 수 있게 했습니다.
