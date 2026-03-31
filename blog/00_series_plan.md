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
37. [Dashboard 지표 중 공개 가능한 것만 `/stats`로 분리하기](./32-make-public-stats-page-from-dashboard-metrics.md)
38. [local 프로필에서 admin / user 계정과 샘플 run 자동 생성하기](./33-bootstrap-local-demo-accounts-and-sample-runs.md)
39. [홈 첫 화면에 로그인 / 회원가입 진입점 추가하기](./34-add-home-auth-entry-points.md)
40. [추천 설문을 12문항 trade-off 구조로 다시 설계하기](./35-redesign-recommendation-survey-with-twelve-questions.md)
41. [공통 셸에 다크/라이트 테마 토글 붙이기](./36-add-sitewide-light-mode-toggle.md)
42. [추천 설문을 20문항 생활 시나리오형으로 다시 확장하기](./37-expand-recommendation-survey-to-twenty-questions.md)
43. [홈 첫 화면에서 모드 중복 노출을 걷어내고 진입 구조 단순화하기](./38-simplify-home-landing-structure.md)
44. [public 디자인 패스 이후 SSR 화면과 테스트를 안정화하기](./39-stabilize-public-design-pass.md)
45. [비용 선호에 따라 초과 물가 패널티를 다르게 주기](./40-split-cost-overshoot-penalty-by-preference.md)
46. [탐색형·교통형 저예산 시나리오에 보정 신호 하나 더 넣기](./41-add-experience-transit-bonus-for-budget-explorers.md)
47. [균형형 생활 시나리오를 위해 civic base bonus 추가하기](./42-add-civic-base-bonus-for-balanced-lifestyles.md)
48. [현실형 저예산 사용자에게 soft landing bonus 추가하기](./43-add-soft-landing-bonus-for-practical-budget-users.md)
49. [dashboard baseline 화면이 현재 엔진 결과를 직접 읽게 만들기](./44-make-dashboard-persona-baseline-dynamic.md)
50. [가족형 정착 시나리오에 family base bonus 추가하기](./45-add-family-base-bonus-for-family-settlement.md)
51. [dashboard persona baseline에 anchor drift까지 보이게 하기](./46-add-anchor-drift-to-dashboard-persona-baseline.md)
52. [추천 만족도 운영 화면에 다음 액션 메모 붙이기](./47-add-ops-review-to-recommendation-feedback-dashboard.md)
53. [local demo bootstrap에 현재 추천 피드백 샘플 넣기](./48-seed-current-recommendation-feedback-in-local-demo.md)
54. [따뜻한 초도시 허브 시나리오에 global hub bonus 추가하기](./49-add-global-hub-bonus-for-warm-city-hubs.md)
55. [현재 코드 재현용 블로그 허브](./50-current-state-rebuild-map.md)
56. [저비용 음식·다문화 시나리오의 1위 drift 줄이기](./51-reduce-p02-anchor-drift-with-foodie-starter-bonus.md)
57. [온화한 공공서비스형 시나리오의 1위 drift 줄이기](./52-reduce-p04-anchor-drift-with-temperate-public-base-bonus.md)
58. [현실형 온화 기후 시나리오의 1위 drift 줄이기](./53-reduce-p06-anchor-drift-with-practical-public-value-bonus.md)
59. [따뜻한 프리미엄 허브 시나리오의 1위 drift 줄이기](./54-reduce-p09-anchor-drift-with-premium-warm-hub-bonus.md)
60. [자연형 저자극 정착 시나리오의 1위 drift 줄이기](./55-reduce-p08-anchor-drift-with-soft-nature-base-bonus.md)
61. [영어 의존이 낮은 고도시 다양성 시나리오의 1위 drift 줄이기](./56-reduce-p10-anchor-drift-with-cosmopolitan-pulse-bonus.md)
62. [온화한 글로벌 도시 시나리오의 1위 drift 줄이기](./57-reduce-p13-anchor-drift-with-temperate-global-city-bonus.md)
63. [따뜻한 실용형 아시아 시나리오의 1위 drift 줄이기](./58-reduce-p14-anchor-drift-with-accessible-warm-value-hub-bonus.md)
64. [온화한 가족형 정착 시나리오의 1위 drift 줄이기](./59-reduce-p11-anchor-drift-with-temperate-family-bridge-bonus.md)
65. [탐색형 자연 정착 시나리오의 1위 drift 줄이기](./60-reduce-p15-anchor-drift-with-exploratory-nature-runway-bonus.md)
66. [warm megacity 시나리오의 baseline anchor를 다시 정의하기](./61-recalibrate-p07-baseline-anchor-for-warm-megacity-scenario.md)
67. [공개 Level 2 실험을 롤백하고 legacy 데이터를 정리하기](./72-roll-back-game-level-2-and-purge-legacy-data.md)
68. [남아 있던 internal Level 2 호환 코드를 완전히 제거하기](./73-remove-internal-level-2-compatibility-code.md)
69. [polling 유지로 9단계를 닫고 실시간 전달 기준 고정하기](./74-close-stage-9-with-polling-first.md)
70. [아키텍처, ERD, 요청 흐름, 발표 자료를 한 번에 정리하기](./75-package-architecture-and-presentation-kit.md)
71. [다음에 어떤 게임을 더 넣을지 먼저 설계하기](./76-plan-next-country-game-expansion.md)
72. [수도 맞히기 Level 1 vertical slice를 현재 구조에 붙이기](./77-add-capital-quiz-level-1-vertical-slice.md)
73. [인구 비교 퀵 배틀 Level 1 vertical slice를 현재 구조에 붙이기](./78-add-population-battle-level-1-vertical-slice.md)
74. [국기 게임을 열기 전에 FlagAssetCatalog를 먼저 만들기](./79-add-flag-asset-catalog-before-opening-flag-game.md)
75. [수도 맞히기 seed에 한국어 수도명을 따로 넣고 게임 UI를 맞추기](./80-add-korean-capital-names-to-country-seed-and-capital-quiz.md)
76. [국기 자산과 country seed를 합쳐 출제 가능 국가 pool 만들기](./81-build-flag-question-country-pool-from-seed-and-assets.md)
77. [국기 보고 나라 맞히기 Level 1 vertical slice를 현재 구조에 붙이기](./82-add-flag-quiz-level-1-vertical-slice.md)
78. [local demo bootstrap에 국기 퀴즈 sample run을 넣기](./83-seed-flag-sample-run-in-local-demo-bootstrap.md)
79. [local demo bootstrap에 수도/인구 비교 sample run도 넣기](./84-seed-capital-and-population-battle-sample-runs-in-local-demo-bootstrap.md)
80. [국기 자산 pool을 36개 snapshot으로 넓히고 재생성 스크립트를 붙이기](./85-expand-flag-asset-pool-with-regeneratable-snapshots.md)
81. [국기 게임 distractor fallback 순서를 지역 기준으로 다듬기](./86-tune-flag-distractor-fallback-order.md)
82. [국기 게임 난이도 단계와 결과 카피를 플레이어 기준으로 다시 정리하기](./87-polish-flag-difficulty-phases-and-result-copy.md)
83. [신규 게임 3종이 들어온 뒤 public 홈, 랭킹, Stats를 다시 묶기](./88-group-public-surfaces-after-adding-three-new-games.md)
84. [local boot에서 legacy leaderboard game_level 제약 풀기](./89-relax-legacy-leaderboard-game-level-constraint-for-local-boot.md)
85. [모든 게임에서 정답 뒤 자동으로 다음 Stage로 넘기기](./90-auto-advance-to-the-next-stage-after-correct-answers.md)
86. [모든 게임의 오답 피드백 시간을 같은 리듬으로 맞추기](./91-unify-wrong-answer-feedback-rhythm-across-public-games.md)
87. [Java 25 기준 multi-stage Dockerfile로 ECS 배포 준비 시작하기](./92-add-java-25-multi-stage-dockerfile-for-ecs-prep.md)
88. [ECS에서 앱이 어떤 설정으로 떠야 하는지 먼저 분리하기](./93-add-application-prod-profile-for-ecs-runtime-baseline.md)
89. [ECS에서 graceful shutdown과 JVM 옵션 기준 먼저 고정하기](./94-add-graceful-shutdown-and-runtime-jvm-opts-for-ecs.md)
90. [ECS와 ALB가 볼 actuator health probe를 실제로 열기](./95-add-actuator-readiness-and-liveness-for-ecs-health-checks.md)
91. [ECS task definition sample로 secrets 주입 기준 고정하기](./96-add-ecs-task-definition-sample-for-secrets-manager-and-ssm.md)
92. [prod에서만 Spring Session Redis를 켜서 멀티태스크 준비하기](./97-enable-prod-only-spring-session-redis-for-fargate-scale-out.md)
93. [GitHub Actions에서 sample task definition을 렌더링해 ECS에 배포하기](./98-add-github-actions-ecs-deploy-workflow-from-template.md)
94. [게임 write를 직렬화하고 stale submit을 막아 무결성 1차 닫기](./100-serialize-game-session-writes-and-stale-submit-guard.md)
95. [prod 설정을 더 안전하게 만들고 startup rollback 범위를 local/test로 제한하기](./101-harden-prod-config-with-schema-validation-and-safer-startup.md)
96. [guest 기록 귀속 범위를 5개 게임 전체로 확장하기](./102-extend-guest-progress-claim-to-all-five-games.md)
97. [5개 게임 기준으로 `/mypage` read model을 다시 정리하고 현재 순위를 바로잡기](./103-rebuild-mypage-read-model-for-all-five-games.md)
98. [추천 피드백을 session token에 묶고 summary API를 admin 전용으로 닫기](./104-bind-recommendation-feedback-to-session-token-and-lock-summary-api.md)
99. [추천 만족도와 게임오버 모달을 키보드로도 제대로 쓰게 만들기](./105-make-recommendation-feedback-and-game-over-modal-keyboard-accessible.md)
100. [남은 4개 게임에도 같은 game over modal focus 규칙 적용하기](./106-extend-keyboard-game-over-modal-focus-rules-to-all-games.md)
101. [랭킹 화면은 active board만 갱신하고 일간 카피도 같이 맞추기](./107-refresh-only-the-active-ranking-board-and-keep-daily-copy-fresh.md)
102. [`/ranking` 첫 SSR은 기본 보드만 그리고 나머지는 지연 로드하기](./108-defer-non-active-ranking-boards-on-initial-ssr.md)
103. [admin 운영 접근을 session role이 아니라 현재 DB role로 다시 검증하기](./109-revalidate-admin-access-against-current-member-role.md)
104. [public 헤더의 Dashboard 링크도 현재 DB role 기준으로 맞추기](./110-align-public-dashboard-link-visibility-with-current-admin-role.md)
105. [public/auth SSR과 게임 시작도 현재 회원 기준으로 stale 세션 UI를 정리하기](./111-use-current-member-state-for-public-auth-ssr.md)
106. [current member 재검증을 request당 한 번만 하도록 정리하기](./112-cache-current-member-resolution-per-request.md)
107. [Playwright로 public 핵심 흐름 브라우저 스모크 테스트 레일 추가하기](./113-add-a-playwright-browser-smoke-lane-for-public-flows.md)
108. [browser smoke를 local Redis 없이도 뜨는 profile로 분리하기](./114-make-browser-smoke-tests-independent-from-local-redis.md)
109. [Redis가 없어도 `/ranking`, `/stats`는 DB fallback으로 계속 읽히게 만들기](./115-keep-ranking-and-stats-readable-with-db-fallback-when-redis-is-down.md)
110. [capital 게임오버 모달 키보드 흐름을 실제 브라우저 E2E로 고정하기](./116-lock-capital-game-over-modal-keyboard-flow-with-real-browser-e2e.md)

### Part E. 테스트, 확장, 취업 패키징

48. 왜 핵심 게임 로직을 테스트해야 하는가
49. 인증, 전적, 마이페이지 붙이기
50. 실시간 전달 방식 고도화
51. README, 아키텍처, 면접 답변 패키지 만들기

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
