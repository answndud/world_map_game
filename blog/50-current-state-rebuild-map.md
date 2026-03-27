# 현재 코드 재현용 블로그 허브

## 이 글의 목적

`blog/` 시리즈는 기본적으로 프로젝트가 어떻게 발전했는지 설명하는 연대기다.

그래서 글 번호 순서대로 읽으면 설계 의도와 변화 과정은 잘 보이지만,
현재 저장소의 코드를 그대로 다시 만들려는 사람에게는 두 가지가 헷갈릴 수 있다.

- 예전 라우트와 현재 라우트가 다를 수 있다.
- 예전 설문/엔진 버전과 현재 설문/엔진 버전이 다를 수 있다.

이 글은 그 문제를 줄이기 위한 `현재 코드 재현용 허브`다.

즉, 블로그 전체를 처음부터 다 읽지 않고도

- 지금 기준 라우트가 무엇인지
- 지금 기준 추천 설문이 몇 문항인지
- 지금 기준 운영 화면이 어디인지
- 어떤 글을 따라가면 현재 코드와 가장 가깝게 재현되는지

를 한 번에 확인하도록 만든다.

## 먼저 알아둘 것

이 허브는 **현재 코드 재현 기준**이다.

즉, 아래 글들은 “틀린 글”이 아니라 “과거 단계 기록”이다.

- `17-expand-recommendation-survey-question-set.md`
- `20-move-ops-insights-into-admin-surface.md`
- `27-protect-admin-routes-with-session-role.md`
- `29-bootstrap-admin-account-from-env.md`
- `35-redesign-recommendation-survey-with-twelve-questions.md`
- `48-seed-current-recommendation-feedback-in-local-demo.md`

이 글들은 당시 단계의 설계 판단을 이해하는 데는 유효하지만,
현재 코드 기준 구현 경로는 이 허브에서 다시 정리한다.

## 현재 기준 핵심 상태

### 런타임 기술 기준

- Spring Boot `3.5.12`
- Java toolchain `25`
- SSR + API 혼합 구조
- PostgreSQL + Redis

근거:

- [build.gradle](/Users/alex/project/worldmap/build.gradle)
- [README.md](/Users/alex/project/worldmap/README.md)

### 현재 public 주요 라우트

- `/`
- `/stats`
- `/ranking`
- `/games/capital/start`
- `/games/location/start`
- `/games/population/start`
- `/recommendation/survey`
- `/mypage`
- `/login`
- `/signup`

### 현재 운영 라우트

- `/dashboard`
- `/dashboard/recommendation/feedback`
- `/dashboard/recommendation/persona-baseline`

`/admin`은 현재 기준 구현 경로가 아니라 legacy redirect 성격이다.

근거:

- [AdminPageController.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/admin/web/AdminPageController.java)

### 현재 추천 엔진 기준

- 설문 버전: `survey-v4`
- 엔진 버전: `engine-v20`
- 질문 수: `20`
- 추천 후보 국가 수: `30`
- dynamic baseline: `18 / 18`, `anchor drift 0`
- 현재 운영 우선 시나리오: 없음

근거:

- [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
- [survey.html](/Users/alex/project/worldmap/src/main/resources/templates/recommendation/survey.html)

### 현재 local demo 기준

- admin 계정: `worldmap_admin / secret123`
- user 계정: `orbit_runner / secret123`
- local에서 샘플 run, guest live session, current recommendation feedback sample까지 bootstrap
- legacy DB에 `leaderboard_record.game_level` 컬럼이나 예전 `game_mode` check constraint가 남아 있더라도 startup rollback initializer가 이를 먼저 정리해 current demo bootstrap이 그대로 동작한다.
- 위치/수도/인구수/인구 비교 퀵 배틀 게임이 현재 public 기본 모드이고, 예전 `LEVEL_2` 세션 / 랭킹 row와 Redis `l2` 키는 startup rollback initializer가 먼저 정리
- 국기 게임은 현재 public route `/games/flag/start`가 열려 있고, `FlagAssetCatalog + FlagQuestionCountryPoolService` 기준 36개 출제 가능 국가 pool로 운영한다.
- 국기 distractor 생성은 `same continent -> 인접 대륙 -> 전체 pool` fallback 규칙으로 서버가 관리한다.
- 국기 난이도는 `기본 라운드 -> 확장 라운드 -> 전체 라운드` 3단계이고, 초반 라운드는 same-continent distractor가 충분한 대륙을 우선 target으로 고른다.
- local demo bootstrap에는 `orbit_runner`의 위치 / 인구수 / 수도 / 인구 비교 퀵 배틀 / 국기 sample run이 같이 들어가서 `/stats`와 `/ranking`의 5개 게임 보드를 바로 확인할 수 있다.

근거:

- [LOCAL_DEMO_BOOTSTRAP.md](/Users/alex/project/worldmap/docs/LOCAL_DEMO_BOOTSTRAP.md)
- [.env.local](/Users/alex/project/worldmap/.env.local)

## 현재 상태 재현 체크리스트

현재 저장소를 실제로 다시 올려 확인하려면 아래 순서가 가장 안전하다.

### 1. local demo 상태로 서버 띄우기

```bash
set -a
source .env.local
set +a
./gradlew bootRun
```

또는 profile을 명시해서 띄운다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 2. 브라우저에서 public 화면 확인

아래 URL이 현재 기준 핵심 확인 포인트다.

- `/`
  - `Home`, `Stats`, `Ranking`, `My Page` 헤더가 보이는가
  - `아케이드 러너`, `퀵 퀴즈와 추천` 두 구역으로 모드 카드가 나뉘어 보이는가
- `/stats`
  - `오늘의 활동`, `게임별 완료 수`, `아케이드 상위 기록`, `퀵 퀴즈 상위 기록`이 분리돼 보이는가
  - 위치/수도/인구수/인구 비교 퀵 배틀/국기 퀴즈 활동 지표와 공개 Top 보드가 보이는가
- `/ranking`
  - `위치 / 수도 / 국기 / 배틀 / 인구` 짧은 게임 전환과 전체/일간 필터만 보이고, `게임 레벨` 필터는 더 이상 보이지 않는가
- `/games/capital/start`
  - 닉네임 입력과 게임 시작하기가 보이는가
- `/games/capital/play/{sessionId}`
  - 한국어 수도 보기 4개와 제출 버튼, 하트/점수 HUD가 보이고 정답 뒤에는 `획득 점수`만 잠깐 보인 뒤 자동으로 다음 Stage로 넘어가는가
- `/games/capital/result/{sessionId}`
  - Stage별 시도 로그와 점수 흐름만 보이고, 한국어 정답 수도는 직접 노출되지 않는가
- `/games/population-battle/start`
  - 닉네임 입력과 게임 시작하기가 보이는가
- `/games/population-battle/play/{sessionId}`
  - 두 나라 중 인구가 더 많은 나라를 고르는 좌/우 2-choice Stage가 보이고 정답 뒤에는 `획득 점수`만 잠깐 보인 뒤 자동으로 다음 Stage로 넘어가는가
- `/games/population-battle/result/{sessionId}`
  - Stage별 비교쌍과 점수 흐름만 보이고, 정답 국가와 선택 로그는 직접 노출되지 않는가
- `/games/flag/start`
  - 닉네임 입력과 게임 시작하기가 보이는가
- `/games/flag/play/{sessionId}`
  - 국기 이미지 1개와 나라 보기 4개, 제출 버튼, 하트/점수 HUD가 보이고 정답 뒤에는 `획득 점수`만 잠깐 보인 뒤 자동으로 다음 Stage로 넘어가는가
- `/games/flag/result/{sessionId}`
  - Stage별 시도 로그와 국기 카드가 보이고, 정답 국가/선택 국가는 직접 노출되지 않는가
- `/recommendation/survey`
  - 20문항 설문이 보이는가
- `/games/population/start`
  - 닉네임 입력과 게임 시작하기만 보이고, Level 선택 UI는 더 이상 보이지 않는가
- `/games/population/play/{sessionId}`
  - 보기 4개 구간 선택형 Stage만 보이고 정답 뒤에는 `획득 점수`만 잠깐 보인 뒤 자동으로 다음 Stage로 넘어가는가
- `/games/location/start`
  - 닉네임 입력과 게임 시작하기만 보이고, Level 선택 UI는 더 이상 보이지 않는가
- `/games/location/play/{sessionId}`
  - 제출 버튼 하나만 있는 Level 1 흐름으로 시작하는가
- `/games/location/result/{sessionId}`
  - Level 1 attempt 로그와 점수 요약만 보이고, 거리/방향 힌트 로그나 `힌트 감점` 문구는 더 이상 보이지 않는가

### 3. local demo 계정으로 로그인 확인

- USER
  - `orbit_runner / secret123`
  - 로그인 후 `/mypage`에서 최고 점수, 최근 플레이, 성향 지표가 보이고, `Level 2 하이라이트`는 더 이상 보이지 않는가
- ADMIN
  - `worldmap_admin / secret123`
  - 로그인 후 헤더에 `Dashboard` 버튼이 생기는가

### 4. 운영 화면 확인

- `/dashboard`
  - 현재 survey/engine 버전과 운영 수치 카드가 보이는가
- `/dashboard/recommendation/feedback`
  - current version 응답 수와 `rank drift 줄이기` 메모가 보이는가
- `/dashboard/recommendation/persona-baseline`
  - baseline 18/18, weak/anchor drift 상태가 보이는가

### 5. local demo 데이터가 실제로 들어갔는지 확인

현재 기준으로는 아래 상태가 보여야 한다.

- `worldmap_admin` 계정 존재
- `orbit_runner` 계정 존재
- `orbit_runner` 완료 run 5개
- `demo-guest-live` 진행 중 guest 세션 1개
- current recommendation feedback sample 5개 이상
- `game_level = LEVEL_2`인 위치/인구수 세션과 `leaderboard_record`, Redis `l2` 키는 남아 있지 않은가

여기까지 확인되면 현재 저장소 기준 핵심 흐름은 대체로 재현된 것이다.

## 현재 코드 재현용 추천 읽기 순서

현재 저장소를 기준으로 다시 만들고 싶다면 아래 순서가 가장 안전하다.

### 1. 프로젝트 뼈대와 공통 기반

1. [02-spring-boot-bootstrap.md](./02-spring-boot-bootstrap.md)
2. [03-country-seed-loading.md](./03-country-seed-loading.md)

여기까지 보면

- 프로젝트 실행 기준
- 프로파일 구조
- 국가 시드 적재

를 맞출 수 있다.

### 2. 게임과 랭킹

3. [04-location-game-level-1.md](./04-location-game-level-1.md)
4. [05-population-game-level-1.md](./05-population-game-level-1.md)
5. [06-redis-leaderboard-vertical-slice.md](./06-redis-leaderboard-vertical-slice.md)
6. [07-leaderboard-polling-refresh.md](./07-leaderboard-polling-refresh.md)
7. [08-ranking-filter-and-tie-rule.md](./08-ranking-filter-and-tie-rule.md)
8. [72-roll-back-game-level-2-and-purge-legacy-data.md](./72-roll-back-game-level-2-and-purge-legacy-data.md)
9. [77-add-capital-quiz-level-1-vertical-slice.md](./77-add-capital-quiz-level-1-vertical-slice.md)
10. [78-add-population-battle-level-1-vertical-slice.md](./78-add-population-battle-level-1-vertical-slice.md)
11. [79-add-flag-asset-catalog-before-opening-flag-game.md](./79-add-flag-asset-catalog-before-opening-flag-game.md)
12. [80-add-korean-capital-names-to-country-seed-and-capital-quiz.md](./80-add-korean-capital-names-to-country-seed-and-capital-quiz.md)
13. [81-build-flag-question-country-pool-from-seed-and-assets.md](./81-build-flag-question-country-pool-from-seed-and-assets.md)
14. [82-add-flag-quiz-level-1-vertical-slice.md](./82-add-flag-quiz-level-1-vertical-slice.md)
15. [83-seed-flag-sample-run-in-local-demo-bootstrap.md](./83-seed-flag-sample-run-in-local-demo-bootstrap.md)
16. [84-seed-capital-and-population-battle-sample-runs-in-local-demo-bootstrap.md](./84-seed-capital-and-population-battle-sample-runs-in-local-demo-bootstrap.md)
17. [85-expand-flag-asset-pool-with-regeneratable-snapshots.md](./85-expand-flag-asset-pool-with-regeneratable-snapshots.md)

이 구간은 현재 코드와 비교적 직접 대응된다.

Level 2 실험은 현재 public 제품 범위에서 완전히 제거됐고, internal 호환 코드도 정리됐다. 그래서 현재 코드를 재현할 때는 실험 이력 글을 따라갈 필요 없이, [72-roll-back-game-level-2-and-purge-legacy-data.md](./72-roll-back-game-level-2-and-purge-legacy-data.md)와 [73-remove-internal-level-2-compatibility-code.md](./73-remove-internal-level-2-compatibility-code.md)만 보고 “왜 Level 1-only로 정리됐는가”를 이해하는 편이 안전하다.

### 3. 추천 엔진 현재 상태

추천은 중간 버전 글이 많아서 아래 순서만 따라가는 것이 좋다.

1. [09-survey-recommendation-engine.md](./09-survey-recommendation-engine.md)
2. [10-expand-recommendation-candidate-pool.md](./10-expand-recommendation-candidate-pool.md)
3. [12-collect-recommendation-feedback.md](./12-collect-recommendation-feedback.md)
4. [14-offline-ai-survey-improvement-loop.md](./14-offline-ai-survey-improvement-loop.md)
5. [37-expand-recommendation-survey-to-twenty-questions.md](./37-expand-recommendation-survey-to-twenty-questions.md)
6. [40-split-cost-overshoot-penalty-by-preference.md](./40-split-cost-overshoot-penalty-by-preference.md)
7. [41-add-experience-transit-bonus-for-budget-explorers.md](./41-add-experience-transit-bonus-for-budget-explorers.md)
8. [42-add-civic-base-bonus-for-balanced-lifestyles.md](./42-add-civic-base-bonus-for-balanced-lifestyles.md)
9. [43-add-soft-landing-bonus-for-practical-budget-users.md](./43-add-soft-landing-bonus-for-practical-budget-users.md)
10. [44-make-dashboard-persona-baseline-dynamic.md](./44-make-dashboard-persona-baseline-dynamic.md)
11. [45-add-family-base-bonus-for-family-settlement.md](./45-add-family-base-bonus-for-family-settlement.md)
12. [46-add-anchor-drift-to-dashboard-persona-baseline.md](./46-add-anchor-drift-to-dashboard-persona-baseline.md)
13. [47-add-ops-review-to-recommendation-feedback-dashboard.md](./47-add-ops-review-to-recommendation-feedback-dashboard.md)
14. [49-add-global-hub-bonus-for-warm-city-hubs.md](./49-add-global-hub-bonus-for-warm-city-hubs.md)
15. [51-reduce-p02-anchor-drift-with-foodie-starter-bonus.md](./51-reduce-p02-anchor-drift-with-foodie-starter-bonus.md)
16. [52-reduce-p04-anchor-drift-with-temperate-public-base-bonus.md](./52-reduce-p04-anchor-drift-with-temperate-public-base-bonus.md)
17. [현실형 온화 기후 시나리오의 1위 drift 줄이기](./53-reduce-p06-anchor-drift-with-practical-public-value-bonus.md)
18. [따뜻한 프리미엄 허브 시나리오의 1위 drift 줄이기](./54-reduce-p09-anchor-drift-with-premium-warm-hub-bonus.md)
19. [자연형 저자극 정착 시나리오의 1위 drift 줄이기](./55-reduce-p08-anchor-drift-with-soft-nature-base-bonus.md)

이 순서가 현재 추천 엔진과 가장 가깝다.

### 4. 인증, 마이페이지, 운영 화면 현재 상태

1. [22-guest-session-to-simple-account-plan.md](./22-guest-session-to-simple-account-plan.md)
2. [23-add-guest-session-ownership-foundation.md](./23-add-guest-session-ownership-foundation.md)
3. [24-add-simple-auth-and-member-owned-game-starts.md](./24-add-simple-auth-and-member-owned-game-starts.md)
4. [25-claim-current-guest-progress-after-login.md](./25-claim-current-guest-progress-after-login.md)
5. [26-build-mypage-from-member-leaderboard-runs.md](./26-build-mypage-from-member-leaderboard-runs.md)
6. [28-add-mypage-stage-performance-metrics.md](./28-add-mypage-stage-performance-metrics.md)
7. [30-rename-admin-surface-to-dashboard.md](./30-rename-admin-surface-to-dashboard.md)
8. [31-add-dashboard-activity-metrics.md](./31-add-dashboard-activity-metrics.md)
9. [32-make-public-stats-page-from-dashboard-metrics.md](./32-make-public-stats-page-from-dashboard-metrics.md)
10. [33-bootstrap-local-demo-accounts-and-sample-runs.md](./33-bootstrap-local-demo-accounts-and-sample-runs.md)

### 5. 신규 게임 확장 현재 상태

1. [76-plan-next-country-game-expansion.md](./76-plan-next-country-game-expansion.md)
2. [77-add-capital-quiz-level-1-vertical-slice.md](./77-add-capital-quiz-level-1-vertical-slice.md)
3. [78-add-population-battle-level-1-vertical-slice.md](./78-add-population-battle-level-1-vertical-slice.md)
4. [79-add-flag-asset-catalog-before-opening-flag-game.md](./79-add-flag-asset-catalog-before-opening-flag-game.md)
5. [80-add-korean-capital-names-to-country-seed-and-capital-quiz.md](./80-add-korean-capital-names-to-country-seed-and-capital-quiz.md)
6. [81-build-flag-question-country-pool-from-seed-and-assets.md](./81-build-flag-question-country-pool-from-seed-and-assets.md)
7. [82-add-flag-quiz-level-1-vertical-slice.md](./82-add-flag-quiz-level-1-vertical-slice.md)
8. [83-seed-flag-sample-run-in-local-demo-bootstrap.md](./83-seed-flag-sample-run-in-local-demo-bootstrap.md)
9. [84-seed-capital-and-population-battle-sample-runs-in-local-demo-bootstrap.md](./84-seed-capital-and-population-battle-sample-runs-in-local-demo-bootstrap.md)
10. [85-expand-flag-asset-pool-with-regeneratable-snapshots.md](./85-expand-flag-asset-pool-with-regeneratable-snapshots.md)

이 구간에서 중요한 건 `/admin`보다 `/dashboard`를 기준으로 읽는 것이다.

## 현재 재현 기준에서 건너뛰어도 되는 글

아래 글들은 역사 기록으로는 의미가 있지만, 현재 코드 재현에는 바로 필요하지 않다.

- [13-recommendation-feedback-insights.md](./13-recommendation-feedback-insights.md)
  - 현재 운영 화면은 `/dashboard/recommendation/feedback`
- [17-expand-recommendation-survey-question-set.md](./17-expand-recommendation-survey-question-set.md)
  - 현재 설문은 8문항이 아니라 20문항
- [20-move-ops-insights-into-admin-surface.md](./20-move-ops-insights-into-admin-surface.md)
  - 현재 운영 진입 주소는 `/dashboard`
- [27-protect-admin-routes-with-session-role.md](./27-protect-admin-routes-with-session-role.md)
  - 접근 제어 개념은 유효하지만 경로 기준은 구버전
- [29-bootstrap-admin-account-from-env.md](./29-bootstrap-admin-account-from-env.md)
  - admin bootstrap 개념은 유효하지만 경로 예시는 `/admin`
- [35-redesign-recommendation-survey-with-twelve-questions.md](./35-redesign-recommendation-survey-with-twelve-questions.md)
  - 현재는 12문항이 아니라 20문항
- [48-seed-current-recommendation-feedback-in-local-demo.md](./48-seed-current-recommendation-feedback-in-local-demo.md)
  - local demo 개념은 유효하지만 현재 버전은 `engine-v20`

## 이 허브를 어떻게 써야 하나

만약 목표가

- “프로젝트 흐름을 이해하고 싶다”면  
  처음부터 번호 순서대로 읽으면 된다.

- “현재 코드와 가장 비슷하게 다시 만들고 싶다”면  
  이 글의 순서대로 필요한 글만 좁혀서 읽는 것이 더 안전하다.

## 면접에서는 이렇게 설명할 수 있다

“블로그 시리즈는 원래 기능이 발전하는 과정을 기록한 연대기라서, 현재 저장소 상태를 그대로 재현하려면 중간 단계 글과 최종 기준 글을 구분할 필요가 있었습니다. 그래서 현재 코드 기준 라우트, 추천 버전, local demo 기준, 그리고 실제로 따라 읽어야 하는 글만 묶은 rebuild map을 따로 만들었습니다. 덕분에 설계 스토리와 현재 구현 재현 경로를 분리해서 설명할 수 있습니다.”
