# 공개 Stats에 Level 2 최고 기록 하이라이트 노출하기

`/mypage`에는 이미 Level 2 최고 기록 카드가 붙어 있었다.  
하지만 공개 화면에서는 여전히 활동 지표와 일간 Top 3만 보여 줘서, “고급 모드도 실제로 돌아가고 있다”는 신호가 잘 드러나지 않았다.

이번 조각의 목표는 간단하다.

- `Dashboard`처럼 내부 판단 정보를 공개하지는 않는다
- 대신 위치/인구수 `Level 2` 최고 기록 한 장씩만 public `/stats`에 보여 준다

즉, 운영 화면을 public에 그대로 복사하는 것이 아니라, `공개 가능한 Level 2 read model`만 얇게 추가하는 작업이다.

## 이번에 바뀐 파일

- `src/main/java/com/worldmap/stats/web/StatsPageController.java`
- `src/main/resources/templates/stats/index.html`
- `src/test/java/com/worldmap/stats/StatsPageControllerTest.java`

## 왜 지금 필요한가

9단계가 진행되면서 public에서 이미 볼 수 있는 것은 많아졌다.

- `/ranking` 에서 위치/인구수 `Level 1 / Level 2` 보드 조회
- `/mypage` 에서 로그인 계정 기준 `Level 2` 최고 기록 하이라이트

그런데 로그인하지 않은 사용자 입장에서는 `/stats` 가 여전히

- 활동 수치
- 일간 Top 3

만 보여 주는 상태였다.

그래서 이번 조각에서는 public `Stats`도 “고급 모드가 실전에서 돌아가고 있다”는 최소 신호를 갖게 만든다.

## 요청 흐름

```text
GET /stats
-> StatsPageController
-> ServiceActivityService.loadTodayActivity()
-> LeaderboardService.getLeaderboard(...)
-> stats/index.html
```

기존 흐름은 그대로 유지하고, `LeaderboardService` 호출 두 개만 더 늘렸다.

- `LOCATION + LEVEL_2 + ALL + 1`
- `POPULATION + LEVEL_2 + ALL + 1`

즉, 새 저장 구조나 새 통계 서비스를 만들지 않고, 이미 있는 `level-aware leaderboard`를 그대로 public 화면에 재사용한 것이다.

## 핵심 설계

### 1. `Stats`는 `Dashboard`의 축소판이 아니다

이 프로젝트에서 둘은 역할이 다르다.

- `/dashboard`
  - 운영자만 본다
  - 추천 버전, 만족도, baseline drift 같은 내부 판단용 정보가 있다
- `/stats`
  - 모든 사용자가 본다
  - 공개 가능한 활동 지표와 사회적 신호만 보여 준다

그래서 이번 조각도 `Dashboard` 정보를 public에 복사하지 않았다.

공개 화면에서는 오직:

- 위치 게임 `Level 2` 최고 기록 1건
- 인구수 게임 `Level 2` 최고 기록 1건

만 노출한다.

### 2. 새 집계를 만들지 않고 `LeaderboardService`를 재사용한다

이미 `LeaderboardService`는

- `gameMode`
- `gameLevel`
- `scope`

조합으로 Redis + DB fallback 조회를 통일하고 있었다.

그래서 `StatsPageController`는 이 규칙을 그대로 쓴다.

이렇게 하면

- `/ranking`
- `/stats`
- `/mypage`

가 모두 같은 Level 2 저장 구조를 읽게 된다.

## 구현 포인트

### `StatsPageController`

기존에 있던 모델:

- `activity`
- `locationDailyTop`
- `populationDailyTop`

이번에 추가된 모델:

- `locationLevel2Highlight`
- `populationLevel2Highlight`

둘 다 `LeaderboardView`로 받고, 템플릿에서는 첫 번째 entry만 사용한다.

### `stats/index.html`

새 섹션 이름은 `Level 2 하이라이트`다.

구성은 단순하다.

- 위치 찾기 Level 2 최고 기록 카드
- 인구수 Level 2 최고 기록 카드
- 둘 다 없으면 안내 문구

여기서 일부러 카드 한 장씩만 두었다.  
public 화면에서는 세부 운영표보다 “이 모드가 실제로 플레이되고 있다”는 신호가 더 중요하기 때문이다.

## 테스트

`StatsPageControllerTest`에서 guest 기준 `/stats` 렌더링에 아래 문자열을 같이 고정했다.

- `Level 2 하이라이트`
- `위치 찾기 Level 2 최고 기록`
- `인구수 Level 2 최고 기록`

그리고 admin session에서도 기존 `Dashboard` 버튼이 계속 보이면서 새 하이라이트 모델 때문에 깨지지 않는지 같이 확인했다.

## 이번 조각에서 얻은 것

이제 public surface는 세 층으로 나뉜다.

- `/ranking`: 경쟁과 순위
- `/stats`: 서비스 활동과 고급 모드 공개 하이라이트
- `/dashboard`: 내부 운영 판단

즉, 모든 정보를 한 화면에 몰아넣지 않고, 공개/비공개 read model 역할을 더 선명하게 나눌 수 있게 됐다.

## 면접에서는 이렇게 설명하면 된다

> public `Stats`는 운영 dashboard를 노출하는 화면이 아니라, 서비스가 실제로 돌아가고 있다는 공개 신호를 보여 주는 화면입니다. 그래서 이번에는 새 집계를 만들지 않고 `LeaderboardService`의 `gameMode + gameLevel + scope` 조회 규칙을 재사용해, 위치/인구수 Level 2 최고 기록만 얇게 추가했습니다. 내부 품질 판단 정보는 계속 dashboard에 남기고, public 화면은 제한된 정보만 보여 주도록 분리한 것이 핵심입니다.
