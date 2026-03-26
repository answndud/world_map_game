# 대표 요청 흐름 가이드

현재 프로젝트를 설명할 때 가장 중요한 요청 흐름 3개만 고정한다.

## 1. 위치 찾기 게임 한 판

```mermaid
sequenceDiagram
    participant Browser
    participant LocationApi as "LocationGameApiController"
    participant LocationService as "LocationGameService"
    participant DB as "PostgreSQL"
    participant Ranking as "LeaderboardService"
    participant Redis

    Browser->>LocationApi: POST /api/games/location/sessions
    LocationApi->>LocationService: startSession(nickname, owner)
    LocationService->>DB: session/stage 저장
    LocationService-->>Browser: sessionId

    Browser->>LocationApi: GET /api/games/location/sessions/{id}/state
    LocationApi->>LocationService: getState(id)
    LocationService->>DB: session + current stage 조회
    LocationService-->>Browser: stage, lives, score

    Browser->>LocationApi: POST /api/games/location/sessions/{id}/answer
    LocationApi->>LocationService: submitAnswer(id, iso3)
    LocationService->>DB: attempt 저장, stage/session 상태 갱신
    alt GAME_OVER
        LocationService->>Ranking: recordFinishedRun(...)
        Ranking->>DB: leaderboard_record 저장
        Ranking->>Redis: 전체/일간 leaderboard 반영
    end
    LocationService-->>Browser: outcome, score, next state
```

### 핵심 설명 포인트

- 프론트는 국가를 고르고 제출만 한다.
- 정답 판정, 하트 감소, 점수 계산, 다음 Stage 생성은 서버가 맡는다.
- 게임 종료 시점에만 랭킹이 반영된다.

## 2. 게스트 플레이 후 로그인해서 기록 귀속

```mermaid
sequenceDiagram
    participant Browser
    participant AuthController as "AuthPageController"
    participant AuthService as "MemberAuthService"
    participant SessionManager as "MemberSessionManager"
    participant ClaimService as "GuestProgressClaimService"
    participant DB as "PostgreSQL"

    Browser->>AuthController: POST /login
    AuthController->>AuthService: authenticate(nickname, password)
    AuthService->>DB: member 조회 / hash 검증
    AuthService-->>AuthController: member
    AuthController->>SessionManager: 로그인 세션 기록
    AuthController->>ClaimService: claimCurrentGuestProgress(memberId, guestSessionKey)
    ClaimService->>DB: game session / leaderboard ownership 이전
    AuthController-->>Browser: redirect /mypage
```

### 핵심 설명 포인트

- 게스트 플레이를 막지 않는다.
- 로그인은 기록 유지 목적이다.
- 같은 브라우저의 guest 기록만 member 계정으로 귀속한다.

## 3. 추천 설문과 운영 피드백 루프

```mermaid
sequenceDiagram
    participant Browser
    participant RecommendationController as "RecommendationPageController"
    participant RecommendationService as "RecommendationSurveyService"
    participant FeedbackApi as "RecommendationFeedbackApiController"
    participant FeedbackService as "RecommendationFeedbackService"
    participant Dashboard as "AdminRecommendationOpsReviewService"
    participant DB as "PostgreSQL"

    Browser->>RecommendationController: POST /recommendation/survey
    RecommendationController->>RecommendationService: recommend(answers)
    RecommendationService->>DB: country/recommendation metadata 조회
    RecommendationService-->>Browser: top 3 + reasons

    Browser->>FeedbackApi: POST /api/recommendation/feedback
    FeedbackApi->>FeedbackService: save(score, surveyVersion, engineVersion, answers)
    FeedbackService->>DB: feedback 저장

    Browser->>Dashboard: GET /dashboard/recommendation/feedback
    Dashboard->>DB: feedback summary + baseline review 조회
    Dashboard-->>Browser: 운영 판단 화면
```

### 핵심 설명 포인트

- 추천 결과는 서버가 deterministic하게 계산한다.
- 결과 자체는 저장하지 않고, 만족도만 저장한다.
- 운영 화면은 현재 버전 만족도와 baseline drift를 함께 본다.

## 발표할 때의 압축 버전

1. 게임은 `세션 시작 -> 상태 조회 -> 답안 제출 -> 종료 시 랭킹 반영`
2. 계정은 `로그인 -> 세션 기록 유지 -> 기존 guest 기록 귀속`
3. 추천은 `설문 계산 -> 만족도 수집 -> dashboard review`
