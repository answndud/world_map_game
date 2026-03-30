# leaderboard_record 기반으로 `/mypage` 기록 대시보드 만들기

## 왜 이 글을 쓰는가

회원가입, 로그인, guest 기록 귀속까지 붙이고 나면 다음 질문이 바로 나온다.

- 로그인하면 무엇이 달라지는가?
- 내 계정에는 어떤 기록이 쌓이는가?
- 그 기록을 어디에서 볼 수 있는가?

이번 단계의 목표는 이 질문에 답하는 것이다.

그래서 `/mypage`를 단순 placeholder 화면에서 실제 기록 대시보드로 바꿨다.

이 글은 위치/인구수 두 게임만 있던 시점의 첫 `/mypage` 버전을 설명한다.
현재 5개 게임 기준 확장과 `현재 전체 순위` 라벨 정리는
[103-rebuild-mypage-read-model-for-all-five-games.md](./103-rebuild-mypage-read-model-for-all-five-games.md)에서 이어진다.

핵심 선택은 하나다.

`/mypage` 첫 버전은 raw 게임 세션을 직접 다시 읽지 않고, 이미 완료된 run이 정리되어 있는 `leaderboard_record`를 읽는다.

이렇게 해야 이유가 분명해진다.

- 최고 점수
- 최고 랭킹
- 최근 완료 플레이

이 세 가지는 모두 "완료된 run" 관점의 읽기 모델이기 때문이다.

## 이번 단계에서 바꾼 것

- 로그인 사용자의 `/mypage`에서 총 완료 플레이 수를 보여준다.
- 위치 게임 / 인구수 게임 각각 최고 점수와 최고 랭킹을 보여준다.
- 최근 완료 플레이 10개를 최신순으로 보여준다.
- 비회원은 여전히 로그인 유도 화면을 본다.

## 왜 `leaderboard_record`부터 읽었는가

처음에는 이렇게 생각하기 쉽다.

> 마이페이지니까 게임 세션 테이블을 직접 읽어야 하는 것 아닌가?

하지만 지금 보여주고 싶은 값은 "완료된 run 요약"이다.

이미 `leaderboard_record`에는 아래 값이 있다.

- 모드
- 총점
- 클리어 Stage 수
- 총 시도 수
- 게임 종료 시각
- ownership(`memberId` 또는 `guestSessionKey`)

즉, `/mypage` 첫 버전에서 필요한 데이터가 이미 정규화되어 있다.

그래서 이번 단계에서는 raw 세션을 다시 조립하는 대신, `leaderboard_record`를 읽는 전용 서비스로 시작했다.

이 선택의 장점은 세 가지다.

1. 설명하기 쉽다.
2. 최고 기록 / 최근 기록 조회가 단순하다.
3. guest -> member 귀속 이후에도 같은 read model을 그대로 쓸 수 있다.

## 바뀐 파일

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

## 요청 흐름

```text
GET /mypage
-> MyPageController.myPage()
-> MemberSessionManager.currentMember()
-> MyPageService.loadDashboard(memberId)
-> LeaderboardRecordRepository
-> MyPageDashboardView
-> mypage.html SSR 렌더링
```

## 컨트롤러가 아니라 서비스에 둔 이유

컨트롤러의 책임은 두 가지뿐이다.

- 로그인 사용자인지 확인
- guest 화면과 member 화면을 분기

반대로 아래 규칙은 비즈니스 로직이다.

- 어떤 run이 위치 게임 최고 기록인가
- 어떤 run이 인구수 게임 최고 기록인가
- 최근 플레이 10개는 어떻게 자를 것인가
- 현재 record의 랭킹 위치를 어떻게 계산할 것인가

이건 HTTP와 무관하다.

그래서 `MyPageService`가 맡아야 한다.

## 화면에서 보여주는 값

현재 `/mypage`는 아래 정보를 보여준다.

- 계정 닉네임
- 총 완료 플레이 수
- 위치 게임 최고 점수 / 최고 랭킹
- 인구수 게임 최고 점수 / 최고 랭킹
- 최근 완료 플레이 10개
  - 모드
  - 종료 시각
  - 총점
  - 현재 전체 순위
  - 클리어 Stage
  - 총 시도 수

## 테스트

이번 단계에서 중요한 테스트는 두 개다.

### 1. `MyPageControllerTest`

로그인 사용자가 `/mypage`에 들어왔을 때

- 최고 점수가 보이는지
- 최근 플레이가 보이는지
- 로그아웃 버튼이 보이는지

를 확인했다.

### 2. `AuthFlowIntegrationTest`

이 테스트가 더 중요하다.

guest로 게임을 끝낸 뒤 같은 브라우저 세션에서 회원가입하면

- 기존 guest 기록이 `memberId` ownership으로 바뀌는지
- 그 귀속된 기록이 `/mypage`에서 바로 보이는지

를 검증했다.

즉, "회원가입 후 이전 기록이 이어지는가?"를 실제로 확인한 것이다.

## 이번 단계에서 아직 남긴 것

지금 `/mypage`는 완료된 run 중심이다.

그래서 아직 없는 값도 있다.

- 정확도
- 평균 시도 수
- 실패한 run까지 포함한 누적 통계
- 모드별 플레이 시간

이런 값은 raw 게임 세션 기반 read model이 필요할 수 있다.

## 면접에서 어떻게 설명할까

이렇게 말하면 된다.

> 마이페이지 첫 버전은 원본 게임 세션을 다시 조립하지 않고, 게임오버 시점에 이미 정규화된 `leaderboard_record`를 읽어 만들었습니다. 그래서 총 완료 플레이 수, 모드별 최고 점수, 최고 랭킹, 최근 완료 이력을 단순한 서비스 로직으로 만들 수 있었습니다. 컨트롤러는 로그인 여부만 확인하고, 어떤 기록을 고를지는 `MyPageService`가 맡아 read model 책임을 분리했습니다.

## 다음 글 예고

다음 단계는 두 갈래 중 하나다.

1. `/mypage`에 더 세밀한 누적 통계를 붙인다.
2. `/admin`과 운영 화면에 실제 접근 제어를 붙인다.

현재 흐름상 다음 우선순위는 admin 접근 제어다.
