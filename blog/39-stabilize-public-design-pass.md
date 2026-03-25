# [Spring Boot 포트폴리오] 39. public 디자인 패스 이후 SSR 화면과 테스트를 안정화하기

## 왜 이 글을 쓰는가

디자인을 크게 바꾸는 작업은 보통 CSS만의 문제가 아니다.

이 프로젝트처럼 Thymeleaf 기반 SSR 화면이 많은 구조에서는, 문구와 레이아웃이 바뀌면 컨트롤러 로직은 그대로여도 테스트가 쉽게 깨진다.

이번 단계는 새 기능을 넣는 작업이 아니라, 이미 바뀐 public 화면을 기준으로 테스트와 문서를 다시 맞추는 안정화 작업이었다.

## 이번에 확인한 문제

다른 세션에서 home, stats, recommendation, ranking, mypage 화면 디자인이 크게 바뀐 상태였다.

그런데 테스트는 여전히 예전 문구를 기대하고 있었다.

대표적인 예시는 아래였다.

- 추천 설문 테스트는 `어울리는 나라 추천`을 기대
- 실제 화면은 `나에게 어울리는 국가 찾기`
- 랭킹 테스트는 `게임 모드`를 기대
- 실제 화면은 `게임 종류`

즉, 기능 버그라기보다 `SSR 출력과 테스트 기대값의 불일치`였다.

## 어떤 파일을 고쳤는가

- `src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java`
- `src/test/java/com/worldmap/ranking/LeaderboardIntegrationTest.java`
- `README.md`
- `docs/PORTFOLIO_PLAYBOOK.md`
- `docs/WORKLOG.md`

이번 조각의 핵심은 템플릿을 또 바꾸는 것이 아니라, 현재 템플릿 상태를 기준으로 테스트와 문서를 맞추는 것이다.

## 왜 이런 안정화 조각이 필요한가

SSR 프로젝트에서는 화면 변경이 곧 서버 응답 변경이다.

즉, 아래 질문을 항상 같이 확인해야 한다.

- 컨트롤러는 그대로 맞는 view를 내리고 있는가?
- 새 카피가 진짜 의도한 public 언어인가?
- 테스트는 지금 내려가는 HTML을 기준으로 통과하는가?

이 확인을 건너뛰고 다음 기능으로 넘어가면, 나중에 깨진 테스트가 “기능 문제인지 화면 문제인지” 구분하기 어려워진다.

## 요청 흐름은 어떻게 보는가

이번에도 런타임 흐름은 바뀌지 않았다.

### 추천 설문

```text
GET /recommendation/survey
-> RecommendationPageController
-> recommendation/survey.html
```

### 랭킹

```text
GET /ranking
-> LeaderboardPageController
-> ranking/index.html
```

즉, request flow는 그대로고, 그 응답 HTML의 public copy만 최신 상태에 맞춰 검증한 것이다.

## 왜 controller가 아니라 테스트를 먼저 맞췄는가

이번 단계에서는 controller 책임이 잘못된 것이 아니었다.

문제는 “controller가 이미 맞는 화면을 내리고 있는데, 테스트가 옛 화면을 기대하고 있는 상태”였다.

이럴 때 controller를 억지로 되돌리면 새 디자인과 다시 어긋난다.

그래서 이번 조각에서는:

- controller는 그대로 둔다
- 현재 템플릿을 기준으로 테스트를 업데이트한다
- 문서도 새 public copy 기준으로 정리한다

이 순서가 맞다.

## 무엇을 테스트했는가

아래 범위를 같이 돌려 현재 디자인 패스가 기능 회귀를 만들지 않았는지 확인했다.

- `HomeControllerTest`
- `MyPageControllerTest`
- `StatsPageControllerTest`
- `RecommendationPageIntegrationTest`
- `LeaderboardIntegrationTest`
- 전체 `./gradlew test`

핵심은 화면이 예쁘게 바뀌었는지보다, “새 카피와 레이아웃 기준에서도 SSR 화면과 테스트가 함께 살아 있는가”를 보는 것이다.

## 이번 단계에서 얻은 것

- public copy와 테스트가 다시 같은 기준을 보게 됐다
- 디자인 패스 이후 다음 기능 작업으로 넘어갈 준비가 됐다
- 이후 추천 엔진 tuning이나 Level 2 작업을 할 때, 화면 변경 때문에 생기는 노이즈를 줄일 수 있다

## 면접에서는 이렇게 설명할 수 있다

“큰 디자인 변경 뒤에는 바로 다음 기능으로 넘어가지 않고, 먼저 SSR 테스트를 새 화면 기준으로 다시 고정했습니다. 이 프로젝트는 Thymeleaf 기반이라 화면 문구와 구조가 바뀌면 기능 로직은 그대로여도 테스트가 깨질 수 있기 때문입니다. 그래서 추천 설문과 랭킹 통합 테스트를 현재 public copy에 맞춰 정리하고, 문서도 그 기준으로 다시 맞췄습니다.”

## 다음 단계

다음은 추천 엔진 `survey-v4 / engine-v4`의 weak scenario를 다시 튜닝해서 6단계를 닫는 것이다.
