# My Page에 Level 2 최고 기록 하이라이트 추가하기

`/mypage`는 이미 `leaderboard_record` 기반 최고 기록과 최근 플레이, raw stage 기반 플레이 성향까지 보여 주고 있었다.  
그런데 9단계에서 위치/인구수 게임 모두 `Level 2`가 열리면서, 계정 화면에서도 “고급 모드에서 어디까지 갔는가”를 따로 보여 줄 필요가 생겼다.

이번 글에서는 새로운 저장 구조를 만들지 않고, 이미 저장된 `leaderboard_record.game_level`을 다시 읽는 방식으로 `/mypage`에 `Level 2 하이라이트`를 붙인 과정을 정리한다.

## 이번 조각에서 바뀐 파일

- `src/main/java/com/worldmap/mypage/application/MyPageBestRunView.java`
- `src/main/java/com/worldmap/mypage/application/MyPageDashboardView.java`
- `src/main/java/com/worldmap/mypage/application/MyPageService.java`
- `src/main/java/com/worldmap/ranking/domain/LeaderboardRecordRepository.java`
- `src/main/resources/templates/mypage.html`
- `src/test/java/com/worldmap/mypage/MyPageServiceIntegrationTest.java`
- `src/test/java/com/worldmap/web/MyPageControllerTest.java`

## 왜 지금 필요한가

현재 구조에서는 다음이 이미 가능했다.

- 위치 게임 `Level 2`는 거리/방향 힌트, 결과 로그, 힌트 감점, 공개 랭킹까지 연결됨
- 인구수 게임 `Level 2`는 직접 수치 입력, precision band, 공개 랭킹까지 연결됨

하지만 로그인 사용자의 `/mypage`는 여전히

- 전체 최고 기록
- 최근 플레이
- 플레이 성향

만 보여 줬다.

즉, “고급 모드도 이미 구현돼 있는데 내 계정 화면에서는 그 성과가 잘 안 보이는 상태”였다.  
그래서 이번 조각의 목표는 `write model을 더 키우지 않고`, `Level 2 최고 기록`만 따로 읽어 카드로 보여 주는 것이었다.

## 요청 흐름

```text
GET /mypage
-> MyPageController
-> MyPageService.loadDashboard(memberId)
-> LeaderboardRecordRepository
-> MyPageDashboardView
-> mypage.html
```

중요한 점은 `/mypage`가 게임 세션 전체를 그대로 뿌리는 화면이 아니라는 것이다.

- 완료된 run 요약은 `leaderboard_record`
- 플레이 성향은 raw stage 집계

로 읽고 있었다.

이번 Level 2 하이라이트도 이 기준을 그대로 따른다.

## 핵심 설계

### 1. `leaderboard_record`만으로 해결한다

Level 2 최고 기록을 위해 새 테이블이나 새 컬럼은 만들지 않았다.  
이미 `leaderboard_record`에 아래 값이 들어 있다.

- `gameMode`
- `gameLevel`
- `totalScore`
- `clearedStageCount`
- `finishedAt`

그래서 `memberId + gameMode + gameLevel=LEVEL_2` 조합만 다시 읽으면 된다.

### 2. read model에만 필드를 추가한다

이번 조각에서 read model은 이렇게 확장됐다.

- `MyPageDashboardView`
  - `locationLevel2Best`
  - `populationLevel2Best`

그리고 카드 한 장에 필요한 값을 명확하게 묶기 위해 `MyPageBestRunView`는 `completedRunCount`도 같이 갖게 했다.

즉, 템플릿이 repository를 직접 만지지 않고

- 최고 점수
- 최고 랭킹
- 최대 Stage
- 완료 run 수

를 한 번에 렌더링할 수 있다.

### 3. 이 책임은 템플릿이 아니라 서비스가 갖는다

Level 2 하이라이트는 단순 문자열 조합이 아니다.

- 어느 run이 최고 기록인지
- 그 run의 현재 공개 랭킹이 몇 위인지
- 같은 모드/레벨 completed run이 몇 개인지

를 같이 판단해야 한다.

이건 화면 포맷보다 `read model 규칙`에 가깝기 때문에 `MyPageService`가 맡는 편이 맞다.

## 구현 포인트

### `LeaderboardRecordRepository`

이번 조각에서 추가한 핵심 쿼리:

- `countByMemberIdAndGameModeAndGameLevel(...)`
- `findFirstByMemberIdAndGameModeAndGameLevelOrderByRankingScoreDescFinishedAtAsc(...)`

첫 번째는 카드 상단의 `완료 run N`을 만들기 위해 필요하고,  
두 번째는 해당 모드/레벨의 최고 기록 한 장을 찾기 위해 필요하다.

### `MyPageService`

`loadDashboard()`는 이제 기존 전체 최고 기록 외에 아래 두 개를 같이 채운다.

- `bestRunView(memberId, LOCATION, LEVEL_2)`
- `bestRunView(memberId, POPULATION, LEVEL_2)`

내부적으로는 `LeaderboardRecord`를 받아 `MyPageBestRunView`로 바꾸는 `toBestRunView()`로 공통화했다.

이렇게 해야

- 전체 최고 기록
- Level 2 최고 기록

을 같은 형식으로 만들 수 있다.

### `mypage.html`

새로운 패널은 `Level 2 하이라이트`다.

- 위치 게임 Level 2 카드
- 인구수 게임 Level 2 카드
- 둘 다 없으면 안내 문구

로 구성했다.

이때 기존 “전체 최고 기록” 카드를 건드리지 않고, 별도 패널을 만든 이유는  
Level 2를 “전체 최고 기록과 경쟁하는 카드”가 아니라 “고급 모드 성과를 따로 보여 주는 카드”로 읽게 하기 위해서다.

## 테스트

### `MyPageServiceIntegrationTest`

새 테스트 `loadDashboardIncludesLevelTwoBestHighlights()`를 추가했다.

시나리오:

1. 회원 계정 생성
2. 위치 게임 `Level 2` run 생성
   - 1 Stage에서 한 번 틀리고 정답
   - 2 Stage에서 오답으로 game over
3. 인구수 게임 `Level 2` run 생성
   - 1 Stage 정답
   - 2 Stage에서 오답으로 game over
4. `/mypage` read model 조회
5. `locationLevel2Best`, `populationLevel2Best`, 점수, 완료 run 수, rank를 검증

이 테스트가 중요한 이유는, `leaderboard_record`가 실제로 생성되는 완료 run이 있어야만 `/mypage` 카드가 채워지기 때문이다.

### `MyPageControllerTest`

로그인 상태 `/mypage` HTML에 아래 문자열이 실제로 있는지 고정했다.

- `Level 2 하이라이트`
- `115점`
- `150점`

즉, read model만 맞고 화면이 빠지는 경우를 막았다.

## 이번 조각에서 얻은 것

이제 `/mypage`는 세 층위로 읽을 수 있다.

1. 전체 최고 기록과 최근 플레이
2. 플레이 성향
3. Level 2 하이라이트

그래서 계정 화면이 단순 점수판이 아니라,

- 내가 어떤 결과를 냈는지
- 어떻게 플레이하는지
- 고급 모드에서는 어디까지 갔는지

를 같이 보여 주는 기록 허브가 됐다.

## 면접에서 이렇게 설명하면 된다

> Level 2 기능이 실제로 구현돼 있어도, 계정 화면에서 따로 읽어 오지 않으면 사용자는 그 성과를 자기 기록으로 체감하기 어렵습니다. 그래서 이번에는 write model을 바꾸지 않고, 이미 `gameLevel`이 저장된 `leaderboard_record`를 다시 읽어 `/mypage`에 위치/인구수 Level 2 최고 기록 카드를 따로 추가했습니다. 핵심은 저장 구조를 늘리지 않고 read model만 확장해서, 일반 기록과 고급 모드 기록을 분리해 설명 가능하게 만든 점입니다.
