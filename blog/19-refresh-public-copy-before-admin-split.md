# [Spring Boot 포트폴리오] 19. public 화면 문구를 제품 언어로 정리하기

## 이번 글의 핵심 질문

기능이 잘 돌아가도 홈과 추천 화면에 `Spring Boot`, `Current Build`, `deterministic`, `Redis Leaderboard` 같은 말이 남아 있으면 사용자는 서비스를 “제품”보다 “개발 중 데모”로 느끼기 쉽다.

이번 단계의 질문은 이것이다.

“서버 구조와 운영 정보는 그대로 유지하면서, 플레이어가 보는 화면만 제품 언어로 다시 쓸 수 있을까?”

이번에는 홈, 추천, 랭킹 public 화면의 문구를 전면 정리하고, 추천 결과 화면에서 내부 운영 페이지 링크도 제거했다.

## 왜 이 단계가 필요한가

이 프로젝트는 포트폴리오로 만들고 있지만, 사용자 화면까지 포트폴리오처럼 보여 줄 필요는 없다.

오히려 public 화면에 개발자 언어가 남아 있으면 아래 문제가 생긴다.

1. 사용자 몰입이 깨진다.
2. 서비스보다 구현 과정을 먼저 보게 된다.
3. 추천 기능이나 랭킹 기능이 “내가 얻는 경험”보다 “어떻게 만들었는가”로 읽힌다.

즉, 제품 언어와 내부 운영 언어는 분리해야 한다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/main/java/com/worldmap/web/HomeController.java`
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java`
- `/Users/alex/project/worldmap/src/main/resources/templates/home.html`
- `/Users/alex/project/worldmap/src/main/resources/templates/recommendation/survey.html`
- `/Users/alex/project/worldmap/src/main/resources/templates/recommendation/result.html`
- `/Users/alex/project/worldmap/src/main/resources/templates/ranking/index.html`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/web/HomeControllerTest.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`

## 홈 화면에서 무엇을 바꿨는가

홈은 가장 먼저 눈에 들어오는 화면이라, 내부 개발 언어를 제일 먼저 걷어냈다.

### 제거한 것

- `Spring Boot 3 Game Platform`
- `Current Build`
- `ORBIT 0.4`
- `Arcade Reboot`
- `Redis Online`
- `현재 로드맵`

### 바꾼 것

- eyebrow: `Explore The World`
- 우측 패널: `오늘의 추천 플레이`
- 하단 리스트: `플레이 방식`, `처음이라면`

중요한 건 구조는 그대로 두고 언어만 바꿨다는 점이다.

`HomeController`는 여전히 `modeCards`, `principles`, `roadmap`를 모델에 담아 렌더링한다. 다만 이제 그 값이 “개발 현황”이 아니라 “플레이어가 무엇을 할 수 있는가”를 설명하도록 바뀌었다.

## 추천 설문과 결과 화면은 어떻게 바꿨는가

### 설문 화면

이전에는 아래 같은 문구가 있었다.

- `Survey Recommendation Engine`
- `deterministic`
- `Offline Eval`

이건 개발자에겐 정확하지만, 설문을 푸는 사람에게는 필요 없다.

그래서 다음처럼 바꿨다.

- eyebrow: `Find Your Match`
- 핵심 설명:
  - `생활 방식과 취향을 고르면 지금 나와 잘 맞는 나라 3곳을 바로 골라드립니다.`
- rule card:
  - `8 Questions`
  - `Top 3 Match`
  - `Quick Result`

### 결과 화면

이전에는 추천 계산 구조를 직접 설명하는 문장이 있었다.

- `서버는 8개 설문 답변을 가중치로 바꾼 뒤...`
- `deterministic`
- `만족도 집계 보기`

이걸 아래처럼 제품 언어로 바꿨다.

- `8개 답변을 바탕으로 지금 생활 취향과 잘 맞는 나라 3곳을 골랐습니다.`
- `내가 고른 취향`
- `잘 맞는 나라 3곳`
- `잘 맞는 정도`

그리고 가장 중요한 변화 하나가 더 있다.

추천 결과 화면에서 `만족도 집계 보기` 링크를 제거했다.

이 링크는 운영자에게는 필요하지만, 플레이어가 바로 눌러야 할 정보는 아니다.

## 랭킹 화면은 무엇이 달라졌는가

랭킹 페이지는 특히 기술 설명이 강했다.

이전에는 아래 문구가 있었다.

- `Redis Leaderboard`
- `Redis Sorted Set`
- `15초 Polling`

지금은 이렇게 바꿨다.

- eyebrow: `Live Scores`
- 설명:
  - `플레이가 끝나면 점수가 바로 반영되고, 지금 가장 높은 기록과 오늘의 상위 기록을 바로 확인할 수 있습니다.`
- 갱신 안내:
  - `15초마다 갱신`

즉, 구현 방식을 설명하는 대신 사용자가 체감하는 결과만 남겼다.

## 왜 이 로직은 컨트롤러보다 view model과 템플릿에서 해결했는가

이번 단계는 도메인 규칙 변경이 아니라 표현 레이어 정리다.

요청 흐름은 그대로다.

- 홈: `HomeController -> home.html`
- 추천: `RecommendationPageController -> survey/result`
- 랭킹: `LeaderboardPageController -> ranking/index.html`

바뀐 것은 “어떤 데이터를 보여 줄까”보다 “그 데이터를 어떤 언어로 말할까”이다.

그래서 핵심 수정 위치는 아래다.

- `HomeController`
  - 홈 화면 카드/문구용 view data 변경
- `RecommendationQuestionCatalog`
  - 설문 helper text를 생활 언어로 정리
- 각 `templates/*.html`
  - 헤더, 설명 문구, 카드 제목 변경

즉, 도메인 로직을 건드리지 않고도 제품 인상은 크게 바꿀 수 있다.

## 테스트는 무엇을 확인했는가

이번 단계에서는 “있어야 하는 문구”와 “없어야 하는 문구”를 같이 테스트했다.

### 1. `HomeControllerTest`

- `오늘의 추천 플레이`
- `플레이 방식`
- 아래 문구는 없어야 함
  - `Spring Boot 3 Game Platform`
  - `Current Build`
  - `ORBIT 0.4`
  - `현재 로드맵`

### 2. `RecommendationPageIntegrationTest`

- 설문 페이지에 `Find Your Match`가 보이는지
- `deterministic`, `Offline Eval`이 없는지
- 결과 페이지에 `잘 맞는 나라 3곳`이 보이는지
- `만족도 집계 보기` 링크가 없는지

### 3. `LeaderboardIntegrationTest`

- 랭킹 페이지에 `15초마다 갱신`이 보이는지
- `Redis Leaderboard`가 없는지

## 면접에서는 이렇게 설명하면 된다

“서비스를 포트폴리오로 만들더라도 사용자 화면까지 개발 용어로 채울 필요는 없다고 봤습니다. 그래서 홈, 추천, 랭킹의 public copy를 제품 언어로 다시 쓰고, 내부 운영 페이지로 가는 링크는 결과 화면에서 제거했습니다. 요청 흐름과 서버 계산은 그대로 유지하고, view model과 템플릿만 바꿔 제품처럼 보이는 인상을 먼저 정리한 단계입니다.”

## 다음 글

다음 단계는 여기서 한 걸음 더 나아가, 현재 public에 남아 있는 운영 화면을 `/admin` 아래 read-only 페이지로 실제로 옮기는 것이다.
