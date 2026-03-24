# Blog Workspace

이 폴더는 WorldMap 프로젝트를 기반으로 작성할 `취업용 개발 블로그 시리즈`의 작업 공간입니다.

목표는 세 가지입니다.

1. Spring Boot 초보자가 "이런 순서로 프로젝트를 만들면 되는구나"를 이해하게 만들기
2. 면접 준비생이 "기획 -> 도메인 -> 게임 로직 -> Redis -> AI -> 테스트 -> 포트폴리오 패키징" 흐름을 따라가게 만들기
3. 작성자인 내가 `blog/`만 보고도 구현 이유와 요청 흐름을 다시 설명할 수 있게 만들기

## 이 폴더의 역할

- 연재 전체 순서를 관리한다.
- 각 글을 일정한 템플릿으로 쓴다.
- 초보자 기준으로 개념을 풀어 쓴다.
- 실제 코드가 생기면 파일, 클래스, 메서드 이름까지 연결한다.

## 이 프로젝트에서 문서 역할 분리

- [README.md](/Users/alex/project/worldmap/README.md)
  - 프로젝트 소개, 기능, 아키텍처, 도메인 개요
- [AGENTS.md](/Users/alex/project/worldmap/AGENTS.md)
  - AI 에이전트 작업 규칙
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
  - 개발 순서, 단계별 목표, 이해 체크
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)
  - 실제 작업 기록과 학습 메모
- [blog/00_series_plan.md](/Users/alex/project/worldmap/blog/00_series_plan.md)
  - 공개용 글 순서와 주제
- [blog/_post_template.md](/Users/alex/project/worldmap/blog/_post_template.md)
  - 실제 글 작성 템플릿

즉, `docs/`는 개발용 SSOT이고, `blog/`는 설명용 출판 워크스페이스다.

운영 기준도 하나 더 분명하다.

- 의미 있는 기능 조각이 끝났다면 `docs/`와 함께 `blog/`도 같은 턴에 같이 남긴다.
- 아주 작은 문구 수정이나 미세 CSS 조정처럼 설명 가치가 낮은 변경만 예외로 둔다.

## 집필 원칙

- "무엇을 만들었는가"보다 "왜 그렇게 설계했는가"를 먼저 설명한다.
- 매 글마다 실제 파일 경로, 클래스 이름, 가능하면 메서드 이름까지 적는다.
- 글 구조는 `문제 -> 개념 -> 설계 -> 코드 -> 흐름 -> 테스트 -> 회고 -> 취업 포인트`를 유지한다.
- 초보자를 위해 용어를 먼저 풀어서 설명한다.
- 글 끝에는 반드시 면접에서 어떻게 설명할지 적는다.

## 현재 연재 인덱스

### Part A. 문제 정의와 프로젝트 방향

1. [왜 WorldMap 게임 플랫폼을 포트폴리오 주제로 잡았는가](./01-why-worldmap-game-platform-domain.md)

### Part B. 부트스트랩과 공통 기반

2. [Spring Boot 프로젝트 뼈대 만들기](./02-spring-boot-bootstrap.md)
3. Docker로 DB / Redis 개발 환경 만들기
4. application.yml과 profile 전략 설계하기
5. JPA / Redis / Validation 공통 기반 잡기
6. 패키지 구조와 예외 처리 전략 정리하기

### Part C. 핵심 게임 도메인

7. [국가 데이터 시드와 `country` 모델링](./03-country-seed-loading.md)
8. `game_session`, `game_round` 모델링
9. [국가 위치 찾기 게임 Level 1 만들기](./04-location-game-level-1.md)
10. [국가 인구수 맞추기 게임 Level 1 만들기](./05-population-game-level-1.md)

### Part D. 랭킹과 추천

11. [Redis Sorted Set으로 실시간 랭킹 만들기](./06-redis-leaderboard-vertical-slice.md)
12. [SSE 대신 15초 폴링으로 랭킹 화면을 살아 있게 만들기](./07-leaderboard-polling-refresh.md)
13. [랭킹 화면에 모드/범위 필터와 동점 규칙 정리하기](./08-ranking-filter-and-tie-rule.md)
14. [설문 기반 나라 추천 엔진 만들기](./09-survey-recommendation-engine.md)
15. [추천 후보 국가 풀을 30개로 넓히기](./10-expand-recommendation-candidate-pool.md)
16. [추천 가중치와 경계값 튜닝하기](./11-recommendation-weight-tuning.md)
17. [추천 결과는 저장하지 않고 만족도 피드백만 수집하기](./12-collect-recommendation-feedback.md)
18. [설문 / 엔진 버전별 만족도 집계 기준 정리하기](./13-recommendation-feedback-insights.md)
19. [오프라인 AI-assisted 설문 개선 루프 정리하기](./14-offline-ai-survey-improvement-loop.md)
20. [페르소나 평가로 survey v2 개정안 만들기](./15-survey-v2-proposal-from-persona-eval.md)
21. [추천 엔진 실험 전 persona top3 snapshot 고정하기](./16-freeze-persona-top3-snapshot.md)
22. [추천 설문을 8문항으로 확장하기](./17-expand-recommendation-survey-question-set.md)
23. [새 추천 문항을 실제로 쓰는 active-signal 페르소나 추가하기](./18-activate-new-recommendation-signals-in-persona-eval.md)
24. [public 화면 문구를 제품 언어로 정리하기](./19-refresh-public-copy-before-admin-split.md)
25. [public 운영 정보를 `/admin` read-only 화면으로 옮기기](./20-move-ops-insights-into-admin-surface.md)
26. [추천 baseline 운영 화면과 public 헤더를 정리하기](./21-add-admin-persona-baseline-and-simplify-public-header.md)
27. [게스트 플레이를 유지하면서 단순 계정으로 확장하는 설계](./22-guest-session-to-simple-account-plan.md)
28. [게스트 세션 키와 기록 소유권 기반 먼저 심기](./23-add-guest-session-ownership-foundation.md)
29. [단순 회원가입 / 로그인과 member 소유 게임 시작 연결하기](./24-add-simple-auth-and-member-owned-game-starts.md)
30. [로그인 직후 현재 브라우저의 guest 기록을 계정으로 귀속하기](./25-claim-current-guest-progress-after-login.md)
31. [leaderboard_record 기반으로 `/mypage` 기록 대시보드 만들기](./26-build-mypage-from-member-leaderboard-runs.md)
32. [세션 role로 `/admin` 운영 화면 접근 제어 붙이기](./27-protect-admin-routes-with-session-role.md)
33. [raw stage 집계로 `/mypage` 플레이 성향 지표 추가하기](./28-add-mypage-stage-performance-metrics.md)
34. [환경변수로 운영용 admin 계정 bootstrap 하기](./29-bootstrap-admin-account-from-env.md)
35. [운영 화면을 `/dashboard`로 바꾸고 ADMIN만 헤더에서 노출하기](./30-rename-admin-surface-to-dashboard.md)
36. [Dashboard에 회원 수와 오늘 활성 지표 붙이기](./31-add-dashboard-activity-metrics.md)

### Part E. 확장과 포트폴리오 정리

37. 인증과 내 전적 조회 고도화하기
38. Level 2와 실시간 전달 방식 고도화하기
39. 테스트, 아키텍처, 면접 패키지로 프로젝트 마감하기

## 읽는 순서

1. [blog/00_rebuild_guide.md](/Users/alex/project/worldmap/blog/00_rebuild_guide.md)로 이 시리즈를 어떤 방식으로 읽을지 확인한다.
2. [blog/00_series_plan.md](/Users/alex/project/worldmap/blog/00_series_plan.md)로 전체 순서를 훑는다.
3. 글은 번호 순서대로 읽는다.
4. 구현 글에서는 본문뿐 아니라 "다룰 파일", "요청 흐름", "테스트", "취업 포인트"를 같이 본다.

## 작성자용 규칙

새 글을 쓰기 전에는 아래를 먼저 확인한다.

1. 현재 단계가 [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)에서 어디인지
2. 최근 작업이 [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)에 정리됐는지
3. 글에 넣을 실제 파일/클래스/테스트 근거가 있는지

코드 근거가 없는 글은 추상적으로 길게 쓰지 않는다.
