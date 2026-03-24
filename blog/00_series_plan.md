# WorldMap 개발 블로그 시리즈 요약 인덱스

이 문서는 공개용 목차다.

상세 구현 순서와 학습 체크는 [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)를 기준으로 삼고, 이 문서는 독자에게 어떤 순서로 설명할지 정리한다.

## 연재 목표

이 시리즈는 아래 독자를 대상으로 한다.

- Spring Boot 포트폴리오를 처음 만드는 사람
- 게임형 서비스라도 백엔드 중심으로 설명하고 싶은 사람
- AI를 붙이더라도 "왜 이렇게 설계했는가"를 설명하고 싶은 사람

## 공개 순서

### Part A. 문제 정의와 방향

1. [왜 WorldMap 게임 플랫폼을 포트폴리오 주제로 잡았는가](./01-why-worldmap-game-platform-domain.md)

### Part B. 부트스트랩과 공통 기반

2. [Spring Boot, Gradle, Thymeleaf로 프로젝트 시작하기](./02-spring-boot-bootstrap.md)
3. Docker로 PostgreSQL 또는 MySQL, Redis 개발 환경 만들기
4. `application.yml`과 profile 전략 설계하기
5. JPA, Redis, Validation 공통 기반 잡기
6. `common`, `country`, `game`, `ranking`, `recommendation` 패키지 구조 잡기

### Part C. 핵심 게임 도메인

7. [`country` 엔티티와 시드 데이터 설계](./03-country-seed-loading.md)
8. `game_session`, `game_round`로 게임 상태 모델링
9. [위치 찾기 게임 Level 1 구현](./04-location-game-level-1.md)
10. [인구수 맞추기 게임 Level 1 구현](./05-population-game-level-1.md)

### Part D. 랭킹과 추천

11. [Redis Sorted Set으로 랭킹 반영하기](./06-redis-leaderboard-vertical-slice.md)
12. [15초 폴링으로 랭킹 화면 갱신하기](./07-leaderboard-polling-refresh.md)
13. [랭킹 화면 필터와 동점 규칙 정리하기](./08-ranking-filter-and-tie-rule.md)
14. [설문 기반 추천 엔진 만들기](./09-survey-recommendation-engine.md)
15. [추천 후보 국가 풀 넓히기](./10-expand-recommendation-candidate-pool.md)
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
28. [게스트 세션 키와 기록 소유권 기반 심기](./23-add-guest-session-ownership-foundation.md)
29. [단순 회원가입 / 로그인과 member 소유 게임 시작 연결하기](./24-add-simple-auth-and-member-owned-game-starts.md)
30. [로그인 직후 현재 브라우저의 guest 기록을 계정으로 귀속하기](./25-claim-current-guest-progress-after-login.md)
31. [leaderboard_record 기반으로 `/mypage` 기록 대시보드 만들기](./26-build-mypage-from-member-leaderboard-runs.md)
32. [세션 role로 `/admin` 운영 화면 접근 제어 붙이기](./27-protect-admin-routes-with-session-role.md)
33. [raw stage 집계로 `/mypage` 플레이 성향 지표 추가하기](./28-add-mypage-stage-performance-metrics.md)
34. [환경변수로 운영용 admin 계정 bootstrap 하기](./29-bootstrap-admin-account-from-env.md)
35. [운영 화면을 `/dashboard`로 바꾸고 ADMIN만 헤더에서 노출하기](./30-rename-admin-surface-to-dashboard.md)
36. [Dashboard에 회원 수와 오늘 활성 지표 붙이기](./31-add-dashboard-activity-metrics.md)

### Part E. 테스트, 확장, 취업 패키징

37. 왜 핵심 게임 로직을 테스트해야 하는가
38. 인증, 전적, 마이페이지 붙이기
39. Level 2 난이도와 실시간성 고도화
40. README, 아키텍처, 면접 답변 패키지 만들기

## 실제 집필 우선순위

공개는 1번부터 하지만, 실제 작성은 아래 순서가 더 효율적이다.

1. 01. 주제 선정과 방향
2. 02. 프로젝트 뼈대 만들기
3. 07. 국가 데이터와 시드 설계
4. 08. 게임 세션 / 라운드 모델링
5. 09. 위치 찾기 게임 Level 1

이유는 이 다섯 개가 프로젝트의 성격을 가장 빠르게 보여 주기 때문이다.

## 각 글에서 공통으로 다룰 질문

- 왜 이 단계가 필요한가?
- 이전 단계에서 무엇이 준비됐는가?
- 실제로 어떤 파일이 바뀌는가?
- 요청은 어떤 흐름으로 지나가는가?
- 상태는 어디에서 바뀌는가?
- 무엇을 테스트해야 하는가?
- 면접에서는 이걸 어떻게 설명하면 좋은가?
