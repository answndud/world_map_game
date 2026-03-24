# raw stage 집계로 `/mypage` 플레이 성향 지표 추가하기

## 왜 이 글을 쓰는가

`/mypage`에 최고 점수와 최근 플레이가 보이기 시작하면,
다음으로 궁금한 것은 결과보다 플레이 방식이다.

예를 들면 이런 질문이다.

- 이 사용자는 1트에 자주 맞히는가?
- 문제를 안정적으로 푸는 편인가?
- 평균적으로 몇 번 시도해서 Stage를 클리어하는가?

이번 단계는 이 질문에 답하기 위한 것이다.

## 이전 단계의 한계

이전 `/mypage`는 `leaderboard_record` 기반이었다.

이 선택은 맞았다.

왜냐하면 아래 값은 `leaderboard_record`가 가장 잘 설명하기 때문이다.

- 총 완료 플레이 수
- 모드별 최고 점수
- 최고 랭킹
- 최근 완료 플레이

하지만 이런 값은 설명하기 어렵다.

- 1트 클리어율
- 평균 시도 수
- 실제로 몇 개 Stage를 안정적으로 클리어했는가

이 값은 run 요약만으로는 안 나온다.

그래서 이번 단계에서는 `/mypage`를 두 층으로 나눴다.

1. `leaderboard_record` 기반 결과 요약
2. raw stage 기반 플레이 성향 요약

## 이번 단계에서 바꾼 것

`/mypage`에 모드별 플레이 성향 패널을 추가했다.

현재 보여주는 값은 아래와 같다.

- 완료 run 수
- 클리어한 Stage 수
- 1트 클리어율
- 평균 시도 수

## 왜 attempt가 아니라 stage부터 읽었는가

처음에는 attempt를 직접 세면 된다고 생각할 수 있다.

하지만 이번 단계의 핵심은 "클리어된 Stage 기준 성향"이다.

이미 stage에는 이런 값이 있다.

- `status`
- `attemptCount`
- `session`

즉, stage만 읽어도 충분하다.

특히 `attemptCount`가 이미 stage에 누적돼 있으므로
attempt 전체를 다시 순회하지 않아도

- 몇 번 만에 클리어했는가
- 1트 클리어였는가

를 쉽게 계산할 수 있다.

그래서 이번 단계에서는 raw attempt가 아니라 raw stage 집계로 멈췄다.

## 바뀐 파일

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

## 요청 흐름

```text
GET /mypage
-> MyPageController
-> MyPageService.loadDashboard(memberId)
-> leaderboard_record 조회
-> finished session의 cleared stage 조회
-> MyPageDashboardView
-> mypage.html 렌더링
```

## 이번 단계의 핵심 설계

이번 구현의 핵심은 `하나의 화면이지만 read model은 둘로 나눈다`는 점이다.

### 1. 결과 요약

이건 `leaderboard_record`가 맡는다.

- 최고 점수
- 최고 랭킹
- 최근 완료 플레이

### 2. 성향 요약

이건 raw stage가 맡는다.

- 클리어 Stage 수
- 1트 클리어율
- 평균 시도 수

이 분리를 해두면 설명이 쉬워진다.

> 결과는 run 요약에서 읽고, 플레이 방식은 stage 집계에서 읽는다.

## 컨트롤러가 아니라 서비스에 둔 이유

컨트롤러는 여전히 로그인 여부와 뷰 분기만 담당한다.

반면 아래 판단은 비즈니스 로직이다.

- 어떤 저장소에서 어떤 지표를 읽을지
- `leaderboard_record`와 raw stage를 어떻게 합칠지
- `1트 클리어율`을 어떤 기준으로 계산할지
- `1회`, `1.5회`, `50%` 같은 표시 형식을 어떻게 만들지

이건 HTTP와 무관하다.

그래서 `MyPageService`가 맡는 것이 맞다.

## 이번 단계에서 조심한 기준

진행 중 세션은 성향 지표에 넣지 않았다.

이유는 두 가지다.

1. 아직 끝나지 않은 세션은 중간 상태라서 통계가 흔들린다.
2. 재시작하면 stage가 지워질 수 있다.

그래서 `finished session에 속한 CLEARED stage`만 집계했다.

## 테스트

이번 단계에서 중요한 테스트는 두 개다.

### 1. `MyPageControllerTest`

로그인 사용자가 `/mypage`에 들어왔을 때

- 플레이 성향 섹션이 보이는지
- `1트 클리어율` 문구가 보이는지
- 평균 시도 수가 렌더링되는지

를 검증했다.

### 2. `MyPageServiceIntegrationTest`

이 테스트가 더 중요하다.

실제로 두 게임을 한 판씩 끝내면서

- 위치 게임: `2회 시도 후 클리어`, `1회 시도 후 클리어`
- 인구수 게임: `1회 시도 후 클리어`

시나리오를 만든 뒤

- 위치 게임 `50%`, `1.5회`
- 인구수 게임 `100%`, `1회`

가 계산되는지 확인했다.

즉, 이번 단계는 단순 템플릿 변경이 아니라
실제 집계 규칙을 테스트로 고정한 것이다.

## 아직 남은 것

지금은 stage 수준에서 멈췄다.

그래서 아직 없는 값도 있다.

- 실패 시도까지 포함한 정확도
- 모드별 누적 플레이 시간
- 기간별 추세
- 시즌 필터

이런 값은 다음 단계에서 raw attempt나 session 시간을 더 내려다봐야 한다.

## 면접에서 어떻게 설명할까

이렇게 말하면 된다.

> `/mypage`는 한 가지 저장소만 읽지 않고, 결과 요약과 플레이 성향 요약을 서로 다른 read model에서 읽도록 만들었습니다. 최고 점수와 최근 플레이는 `leaderboard_record`에서 가져오고, 1트 클리어율과 평균 시도 수는 finished session에 속한 cleared stage를 다시 집계해서 계산합니다. 이렇게 하면 결과와 플레이 방식을 분리해서 설명할 수 있고, 컨트롤러는 로그인 여부만 확인하고 실제 조합 책임은 `MyPageService`가 맡게 됩니다.

## 다음 글 예고

다음 단계는 두 방향 중 하나다.

1. admin 계정 provisioning 방식 정리
2. `/mypage`에 실패 run 포함 정확도나 누적 시간 같은 더 깊은 지표 추가

현재 흐름상 다음 우선순위는 admin provisioning이다.
