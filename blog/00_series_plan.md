# Series Plan

## 공개 순서

| 글 | 닫는 문제 | 독자가 끝나고 얻어야 하는 것 |
| --- | --- | --- |
| 01 | 왜 WorldMap을 서버 주도 게임 플랫폼으로 정의하는가 | 이 프로젝트를 CRUD가 아니라 상태 관리 서비스로 설명하는 언어 |
| 02 | Spring Boot + SSR baseline을 어떻게 여는가 | 실행 가능한 앱 뼈대와 패키지 구조 |
| 03 | PostgreSQL/Redis 개발 환경을 어떻게 재현 가능하게 만드는가 | compose 기반 로컬 개발 환경 |
| 04 | profile을 어떻게 분리해 local/test/prod를 구분하는가 | 설정 파일 전략과 운영 안전 기준 |
| 05 | 모든 게임이 공통으로 쓰는 국가 데이터를 어떻게 준비하는가 | country seed, validator, initializer |
| 06 | 여러 게임이 같은 언어로 말하게 하려면 공통 계약을 어디까지 올려야 하는가 | `BaseGameSession`, status, access contract |
| 07 | 위치 게임에서 `Session / Stage / Attempt` 루프를 어떻게 구현하는가 | 대표 vertical slice |
| 08 | 퀴즈형 게임도 같은 endless loop로 설명할 수 있는가 | population quiz, option generator, scale band |
| 09 | terminal run을 어떻게 랭킹 페이지로 읽게 만드는가 | Redis leaderboard, DB fallback, `/ranking` |
| 10 | 런타임 LLM 없이 추천 품질을 어떻게 설명 가능한 구조로 만드는가 | deterministic engine, feedback, baseline test |
| 11 | guest 플레이와 계정 귀속을 어떻게 같이 만족시키는가 | guest ownership, claim |
| 12 | simple auth와 member session을 어떻게 최소 구조로 닫는가 | signup/login/session/admin entry |
| 13 | 사용자용 기록 화면과 공개 지표를 어떻게 분리하는가 | `/mypage`, `/stats` read model |
| 14 | 운영자 surface를 어떻게 public 화면과 분리하는가 | `/dashboard`, operations cards, admin review |
| 15 | public product scope를 다시 닫고 게임 라인업을 어떻게 늘리는가 | Level 2 rollback, capital/battle/flag |
| 16 | 운영 런타임과 배포 준비를 어떻게 코드로 남기는가 | Docker.local, prod profile, Redis session, Railway/ECS prep |
| 17 | production-ready hardening을 어떻게 닫는가 | ownership, terminal result, stale submit, role revalidation |
| 18 | 검증과 시연 자료를 어떻게 하나의 포트폴리오 패키지로 묶는가 | browser smoke, verify, demo bootstrap, interview pack |

## production-ready bundle 읽는 법

후반부 `16~18`은 독립된 세 글이면서 동시에 하나의 bundle입니다.

읽는 순서는 아래가 가장 자연스럽습니다.

1. `16`
   - 운영 런타임과 deploy input이 무엇인지 먼저 고정
2. `17`
   - 그 런타임 위에서 어떤 hardening이 server contract를 지키는지 설명
3. `18`
   - 마지막으로 무엇을 어떻게 검증하고 시연하는지 설명

즉 `16` 없이 `18`만 읽으면 verify lane은 이해돼도 prod contract가 흐려지고,
`17` 없이 `18`만 읽으면 왜 ownership/current role hardening이 필요한지 약해집니다.

## 이 시리즈의 재현 범위

이 시리즈는 "문장만 읽고 저장소 없이 구현 끝"을 약속하지 않습니다.
대신 아래 순서를 약속합니다.

1. 글이 문제와 구현 순서를 설명한다
2. 글이 먼저 열 코드, 테스트, 설정 파일을 정확히 가리킨다
3. 독자가 링크된 source of truth를 따라가며 현재 저장소를 다시 세운다

즉 재현의 단위는 `글 단독`이 아니라 `글 + 링크된 저장소 파일`입니다.
후반부 `16~18`은 여기에 더해 `자동 검증 범위`와 `수동 운영 절차`도 분리해서 읽어야 합니다.

## 실제 집필 우선순위

아래 순서로 밀도를 높이면 시리즈 완성도가 가장 빨리 올라갑니다.

1. 허브 문서
   - `README.md`
   - `00_rebuild_guide.md`
   - `00_quality_checklist.md`
2. 핵심 대표 글
   - `07`
   - `09`
   - `10`
   - `17`
   - `18`
3. 사용자/운영 surface 글
   - `11`
   - `12`
   - `13`
   - `14`
4. baseline과 확장 글
   - `02~06`
   - `08`
   - `15`
   - `16`

5. production-ready bundle polish
   - `16`
   - `17`
   - `18`

## 글별 source of truth

### baseline

- `02`: `build.gradle`, `src/main/java/com/worldmap/**`, 기본 controller/test
- `03`: `compose.yaml`, `.env.example`, local 실행 문서
- `04`: `application.yml`, `application-local.yml`, `application-test.yml`, `application-prod.yml`

### data와 game loop

- `05`: `country/**`, `scripts/generate_country_assets.py`, `countries.json`
- `06`: `game/common/**`, `common/exception/**`, `common/response/**`
- `07`: `game/location/**`, `location-game/**`, 관련 test
- `08`: `game/population/**`, `population-game/**`, 관련 test

### ranking과 recommendation

- `09`: `ranking/**`, `ranking/index.html`, `ranking.js`
- `10`: `recommendation/**`, `recommendation/*.html`, feedback 관련 test

### ownership, auth, read model, admin

- `11`: `GuestSessionKeyManager`, `GuestProgressClaimService`, ownership test
- `12`: `MemberAuthService`, `MemberSessionManager`, `AuthPageController`, admin bootstrap
- `13`: `MyPageService`, `ServiceActivityService`, `mypage.html`, `stats/index.html`
- `14`: `AdminDashboardService`, `AdminRecommendationOpsReviewService`, `admin/*.html`

### runtime, hardening, verification

- `15`: `GameLevelRollbackInitializer`, 신규 게임 3종 패키지, 홈/랭킹/stats 연동
- `16`: `Dockerfile.local`, `railway.toml`, `application-prod.yml`, `RedisSessionProdConfiguration`, deploy scripts/workflow
- `17`: `GameSessionAccessContextResolver`, `GameSubmissionGuard`, `CurrentMemberAccessService`, `AdminAccessGuard`
- `18`: `BrowserSmokeE2ETest`, `PublicUrlSmokeE2ETest`, `verify.yml`, `DemoBootstrapService`, 발표 문서

## 번들 단위에서 꼭 맞아야 하는 것

`16~18`은 아래 표현이 서로 어긋나면 안 됩니다.

- prod runtime contract
- current member / current role
- browser smoke vs public URL smoke
- verify workflow vs GitHub required check
- local demo baseline vs interview pack

## 글이 끝날 때 독자가 확인해야 하는 것

모든 본편은 최소한 아래 셋을 남겨야 합니다.

1. 먼저 열 파일
2. 최소 검증 명령
3. 다음 글로 넘어가기 전에 설명할 수 있어야 하는 질문

그리고 가능하면 아래 넷째 항목까지 남겨야 합니다.

4. 무엇이 자동으로 증명됐고, 무엇이 아직 수동 운영이나 후속 검증에 남는가

이 세 가지가 빠진 글은 WorldMap 시리즈의 기준을 통과하지 못한 것입니다.
