# Blog Workspace

이 폴더는 WorldMap을 기반으로 작성하는 **서버 주도 게임 플랫폼 포트폴리오 시리즈**의 작업 공간입니다.

목표는 세 가지입니다.

1. Spring Boot 입문자도 "게임 상태를 서버가 주도하는 프로젝트를 이런 순서로 만들 수 있구나"를 이해하게 만들기
2. 취업 준비생이 "부트스트랩 -> 도메인 -> 랭킹/추천 -> 인증/권한 -> 운영/검증" 흐름으로 성장하게 만들기
3. 독자가 `blog/`와 각 글이 직접 가리키는 코드, 테스트, 설정 파일을 함께 따라가며 현재 WorldMap 저장소를 단계별로 재구현할 수 있게 만들기

이 시리즈는 커밋 로그나 작업 회고 모음이 아닙니다. 각 글은 **문제 하나를 닫고**, **그 문제를 닫기 위해 먼저 열 파일**, **요청 흐름**, **테스트**, **재현 체크포인트**까지 같이 제공합니다.

## 블로그 작업 SSOT

- [README.md](../README.md)
  - 제품 소개, 현재 public 범위, 전체 아키텍처 방향
- [docs/PORTFOLIO_PLAYBOOK.md](../docs/PORTFOLIO_PLAYBOOK.md)
  - 개발 순서, 단계 목표, 다음에 이어갈 주제
- [docs/WORKLOG.md](../docs/WORKLOG.md)
  - 실제 작업 단위, 요청 흐름, 테스트 이력
- [00_rebuild_guide.md](./00_rebuild_guide.md)
  - 이 시리즈를 "재현형 기술 문서"로 쓰기 위한 공통 규칙
- [00_quality_checklist.md](./00_quality_checklist.md)
  - 초보자 이해도, 재현성, 코드-문서 정합성, 면접 대응력 점검표

즉, `docs/`가 개발 SSOT라면 `blog/`는 독자용 재현 허브입니다.
다만 최종 source of truth는 언제나 저장소의 실제 코드, 테스트, 설정 파일입니다.
`blog/`는 그 source of truth를 읽는 순서와 이유를 설명하는 문서 계층이지, 저장소와 분리된 별도 교과서가 아닙니다.

## 링크 정책

- `blog/` 문서는 repo-relative 링크를 사용합니다.
- 모든 글은 실제 클래스, 설정 파일, 테스트 파일을 직접 가리킵니다.
- 설명만 있고 파일 포인터가 없는 글은 실패한 글로 봅니다.
- 외부 플랫폼으로 옮길 때만 절대 URL 또는 permalink 체계로 바꿉니다.

## 이 폴더의 역할

- [00_series_plan.md](./00_series_plan.md)
  - 전체 연재 구조
  - 각 글이 닫는 문제
  - 공개 순서와 집필 우선순위
- [00_rebuild_guide.md](./00_rebuild_guide.md)
  - 각 글에 반드시 있어야 하는 섹션과 재현 규칙
- [00_quality_checklist.md](./00_quality_checklist.md)
  - 문장 품질, 구조 품질, 코드-문서 정합성 기준

## 이 시리즈가 다루는 범위

- 왜 WorldMap을 `서버 주도 게임 플랫폼`으로 정의했는가
- Spring Boot, Thymeleaf SSR, Docker, PostgreSQL, Redis 기반 baseline
- 국가 시드와 공통 reference data 파이프라인
- `Session / Stage / Attempt` 게임 루프
- 위치 게임, 인구수 퀴즈, 수도 퀴즈, 인구 비교 배틀, 국기 게임
- Redis leaderboard와 공개 `/ranking` 페이지
- deterministic recommendation engine과 feedback loop
- guest ownership, simple auth, `/mypage`, `/stats`, `/dashboard`
- public scope reset과 Level 1-only lineup 정리
- production runtime, Redis session, ECS deploy prep
- game integrity, current member/current role revalidation
- browser smoke, public URL smoke, verify pipeline
- demo bootstrap, architecture docs, interview pack

## 집필 원칙

- "무엇을 만들었는가"보다 "왜 지금 이 설계를 택했는가"를 먼저 설명합니다.
- 각 글은 한 문제를 닫아야 합니다. 문제 두세 개를 억지로 합치지 않습니다.
- 각 글은 최소한 아래를 포함해야 합니다.
  - 최종 도착 상태
  - 실제 파일 목록
  - 요청 흐름 또는 상태 변화
  - 실패 케이스
  - 테스트와 검증 명령
- 초심자를 위해 용어를 바로 쓰지 않고, 한 번은 평이한 말로 풀어 씁니다.
- 글 끝에는 항상 `취업 포인트`와 `글 종료 체크포인트`를 둡니다.
- 자동 검증으로 고정된 사실과 수동 운영 절차, 아직 남은 한계를 같은 문장에 섞어 쓰지 않습니다.

## 이 시리즈가 약속하지 않는 것

- `blog/` 문장만 읽고 저장소 밖에서 그대로 복사 구현이 끝난다고 약속하지 않습니다.
- 실제 재현은 각 글이 연결한 클래스, 템플릿, 테스트, 스크립트를 함께 열어야 닫힙니다.
- production runtime, verify, public smoke 같은 후반부 글은 특히 `자동으로 증명된 범위`와 `사람이 수동으로 맞춰야 하는 범위`를 분리해서 설명합니다.
- local fallback timing, fixture 기반 preflight, controller/page test 결과를 production 실측이나 운영 보장으로 과장하지 않습니다.

## 연재 인덱스

### Part A. 문제 정의와 baseline

1. [왜 WorldMap을 서버 주도 게임 플랫폼으로 잡았는가](./01-why-worldmap-server-driven-game-platform.md)
   - 이 프로젝트를 CRUD가 아니라 상태 관리가 있는 서비스로 포지셔닝하는 글
2. [Gradle, Spring Boot, SSR 뼈대 만들기](./02-gradle-spring-boot-ssr-bootstrap.md)
   - 실행 가능한 Spring Boot baseline과 패키지 뼈대를 고정하는 글
3. [Docker로 PostgreSQL, Redis 개발 환경 만들기](./03-docker-postgres-redis-dev-environment.md)
   - 로컬 개발 환경을 문서가 아니라 코드와 compose로 재현 가능하게 만드는 글
4. [application.yml과 profile 전략 설계하기](./04-application-yml-and-profile-strategy.md)
   - local, test, prod의 책임을 분리하고 운영 위험을 줄이는 글

### Part B. 공통 데이터와 게임 골격

5. [country seed와 reference data 파이프라인 만들기](./05-country-seed-and-reference-data-pipeline.md)
   - 모든 게임과 추천이 공통으로 쓰는 국가 기준 데이터를 만드는 글
6. [game 패키지 구조와 shared session contract 잡기](./06-game-package-structure-and-shared-session-contract.md)
   - 여러 게임이 같은 언어로 말하게 만드는 공통 골격 설계 글
7. [위치 게임으로 `Session / Stage / Attempt` 루프 만들기](./07-location-game-session-stage-attempt-loop.md)
   - WorldMap의 대표 vertical slice를 구현하는 글
8. [인구수 퀴즈로 endless arcade loop와 option generation 확장하기](./08-population-quiz-arcade-loop-and-option-generation.md)
   - 퀴즈형 게임도 같은 상태 기계로 설명 가능하게 만드는 글

### Part C. 결과를 읽기 좋은 surface로 바꾸기

9. [Redis leaderboard와 공개 `/ranking` 페이지 만들기](./09-redis-leaderboard-and-ranking-page.md)
   - terminal run을 Redis Sorted Set과 SSR read model로 읽게 만드는 글
10. [deterministic recommendation engine과 feedback loop 만들기](./10-deterministic-recommendation-engine-and-feedback-loop.md)
   - 런타임 LLM 없이도 설명 가능한 추천 엔진과 피드백 루프를 설계하는 글

### Part D. 사용자와 운영 surface 열기

11. [guest session ownership과 progress claim 만들기](./11-guest-session-ownership-and-progress-claim.md)
   - 비회원 즉시 플레이와 기록 귀속을 동시에 잡는 글
12. [simple auth, member session, admin entry 만들기](./12-simple-auth-member-session-and-admin-entry.md)
   - 닉네임/비밀번호 기반 인증과 세션, 관리자 진입 기준을 고정하는 글
13. [`/mypage`와 공개 `/stats` read model 만들기](./13-mypage-and-public-stats-read-models.md)
   - 사용자 중심 read model과 공개 지표 surface를 설계하는 글
14. [`/dashboard` 운영 surface와 operations card 만들기](./14-dashboard-admin-surface-and-operations-cards.md)
   - 관리자용 read model을 일반 public 화면과 분리하는 글

### Part E. public scope를 다시 닫고 게임 라인업 확장하기

15. [public scope reset과 신규 게임 lineup 정리하기](./15-public-scope-reset-and-new-games-lineup.md)
   - Level 2 실험 흔적을 걷고 capital, population-battle, flag를 public lineup에 올리는 글

### Part F. 운영 런타임과 hardening

16. [production runtime, Redis session, ECS deploy prep 묶기](./16-production-runtime-redis-session-and-ecs-deploy-prep.md)
   - Docker image, prod profile, Redis session, ECS 배포 준비를 한 번에 설명하는 글
17. [game integrity와 current member/current role 재검증 닫기](./17-game-integrity-current-member-and-role-revalidation.md)
   - ownership, stale submit, terminal result, admin role 재검증을 production hardening으로 묶는 글

### Part G. production-ready 검증과 마감

18. [production verification과 demo/interview pack으로 프로젝트 마감하기](./18-production-verification-and-demo-interview-pack.md)
   - browser smoke, public URL smoke, verify workflow, demo bootstrap, 발표 자료를 하나의 마감 패키지로 묶는 글

### production-ready bundle만 먼저 읽고 싶다면

후반부만 빠르게 따라갈 때는 아래 세 편을 한 묶음으로 읽는 편이 좋습니다.

1. [16](./16-production-runtime-redis-session-and-ecs-deploy-prep.md)
   - 운영 런타임과 deploy input contract
2. [17](./17-game-integrity-current-member-and-role-revalidation.md)
   - ownership, stale submit, current member/current role hardening
3. [18](./18-production-verification-and-demo-interview-pack.md)
   - verify lane, demo baseline, interview pack

즉 `16 -> 17 -> 18`은 runtime, hardening, verification을 순서대로 닫는 trilogy입니다.

## 독자용 시작 순서

### 1. 프로젝트를 처음 읽는 경우

1. [00_rebuild_guide.md](./00_rebuild_guide.md)
2. [00_quality_checklist.md](./00_quality_checklist.md)
3. [00_series_plan.md](./00_series_plan.md)
4. [01](./01-why-worldmap-server-driven-game-platform.md)부터 [18](./18-production-verification-and-demo-interview-pack.md)까지 순서대로 읽기

### 2. "재구현"이 목적일 때

아래 순서로 들어가는 것이 가장 빠릅니다.

1. [02](./02-gradle-spring-boot-ssr-bootstrap.md)
2. [03](./03-docker-postgres-redis-dev-environment.md)
3. [04](./04-application-yml-and-profile-strategy.md)
4. [05](./05-country-seed-and-reference-data-pipeline.md)
5. [06](./06-game-package-structure-and-shared-session-contract.md)
6. [07](./07-location-game-session-stage-attempt-loop.md)
7. [08](./08-population-quiz-arcade-loop-and-option-generation.md)
8. [09](./09-redis-leaderboard-and-ranking-page.md)
9. [10](./10-deterministic-recommendation-engine-and-feedback-loop.md)
10. [11](./11-guest-session-ownership-and-progress-claim.md)
11. [12](./12-simple-auth-member-session-and-admin-entry.md)
12. [13](./13-mypage-and-public-stats-read-models.md)
13. [14](./14-dashboard-admin-surface-and-operations-cards.md)
14. [15](./15-public-scope-reset-and-new-games-lineup.md)
15. [16](./16-production-runtime-redis-session-and-ecs-deploy-prep.md)
16. [17](./17-game-integrity-current-member-and-role-revalidation.md)
17. [18](./18-production-verification-and-demo-interview-pack.md)

### 3. "면접 준비"가 목적일 때

아래 글만 먼저 읽어도 큰 줄기를 잡을 수 있습니다.

1. [01](./01-why-worldmap-server-driven-game-platform.md)
2. [07](./07-location-game-session-stage-attempt-loop.md)
3. [09](./09-redis-leaderboard-and-ranking-page.md)
4. [10](./10-deterministic-recommendation-engine-and-feedback-loop.md)
5. [17](./17-game-integrity-current-member-and-role-revalidation.md)
6. [18](./18-production-verification-and-demo-interview-pack.md)

추가로 production-ready 답변을 더 안정적으로 준비하려면
[16](./16-production-runtime-redis-session-and-ecs-deploy-prep.md)까지 같이 읽는 것이 좋습니다.

## 작성자용 시작 순서

1. [README.md](../README.md)로 현재 제품 범위와 public scope를 다시 확인합니다.
2. [docs/PORTFOLIO_PLAYBOOK.md](../docs/PORTFOLIO_PLAYBOOK.md)로 현재 단계와 다음 목표를 확인합니다.
3. [docs/WORKLOG.md](../docs/WORKLOG.md)로 최근 작업과 설계 이유를 확인합니다.
4. [00_rebuild_guide.md](./00_rebuild_guide.md)와 [00_quality_checklist.md](./00_quality_checklist.md)로 문서 기준을 맞춥니다.
5. 새 글을 추가하기 전에 "이 글이 실제로 닫는 문제 하나가 무엇인가"를 먼저 적습니다.

## 이 시리즈가 약속하는 것

WorldMap 블로그는 아래 셋을 동시에 만족해야 합니다.

- 현재 저장소를 이해할 수 있어야 한다
- 글이 가리키는 코드/테스트/설정 파일을 함께 따라가며 같은 구조를 다시 만들 순서를 배울 수 있어야 한다
- 면접에서 30초와 1분 답변으로 축약할 수 있어야 한다

이 셋 중 하나라도 빠지면 글을 다시 써야 합니다.
