# Portfolio Playbook

## 목적

이 문서는 "무엇을 어떤 순서로 만들지"와 "각 단계를 내가 어디까지 이해해야 하는지"를 같이 관리하는 문서다.

코드를 빨리 늘리는 것보다, 각 단계를 끝낼 때마다 아래를 설명할 수 있어야 한다.

- 왜 이 기능을 지금 만들었는가
- 요청이 어떤 흐름으로 동작하는가
- 어떤 엔티티와 상태 변화가 있는가
- 어떤 테스트로 핵심 리스크를 막았는가

## 사용 방법

- 새 작업을 시작하기 전에 현재 단계를 확인한다.
- 기능을 마칠 때마다 상태를 갱신한다.
- 의미 있는 기능 조각이 끝났다면 `docs/`와 대응 `blog/` 글까지 같은 턴에 남겼는지 확인한다.
- 다음 단계로 넘어가기 전에 `이해 체크`를 반드시 본다.
- 막연하면 기능 구현보다 먼저 이 문서를 세분화한다.

## 단계 현황

| 단계 | 이름 | 상태 |
| --- | --- | --- |
| 0 | 문서와 규칙 정리 | Done |
| 1 | 스프링부트 프로젝트 뼈대 | Done |
| 2 | 국가 데이터와 시드 적재 | Done |
| 3 | 국가 위치 찾기 게임 Level 1 | Reworking |
| 4 | 국가 인구수 맞추기 게임 Level 1 | Reworking |
| 5 | Redis 랭킹 시스템 | Done |
| 6 | 설문 기반 추천 엔진 | In Progress |
| 7 | AI-assisted 설문 개선 체계 | In Progress |
| 8 | 인증, 전적, 마이페이지 | Done |
| 9 | 고도화 실험 롤백과 실시간성 개선 | Done |
| 10 | 포트폴리오 정리와 발표 준비 | Done |
| 11 | 신규 게임 확장 | In Progress |

## 공통 체크리스트

모든 단계에서 아래 질문에 답할 수 있어야 한다.

- 이 단계의 핵심 책임은 무엇인가?
- 컨트롤러, 서비스, 엔티티 중 어디에 어떤 로직이 있어야 하는가?
- 어떤 입력을 받아 어떤 출력과 상태 변화를 만드는가?
- 실패하거나 예외가 나는 경우는 무엇인가?
- 이 단계를 보여주는 가장 중요한 테스트 1개는 무엇인가?

## 단계별 상세 계획

### 0. 문서와 규칙 정리

상태: Done

목표:

- AI와 사람이 같은 기준으로 작업하게 만든다.
- 구현 순서와 기록 방식을 먼저 고정한다.

완료 항목:

- `README.md` 작성
- `AGENTS.md` 작성
- 이 플레이북 작성
- 작업 로그 파일 생성
- `blog/` 공개용 문서 워크스페이스 생성
- AI 에이전트 운영 원칙 문서 작성
- 프로젝트 로컬 커스텀 스킬 `worldmap-doc-sync` 구현

이해 체크:

- 왜 `AGENTS.md`와 플레이북을 분리하는가?
- 왜 코드 작성 전에 개발 순서를 고정해야 하는가?

### 1. 스프링부트 프로젝트 뼈대

상태: Done

목표:

- 실행 가능한 기본 서버 구조를 만든다.
- 패키지 구조와 공통 설정을 너무 늦지 않게 고정한다.

구현 항목:

- Spring Initializr 기반 프로젝트 생성
- 기본 의존성 구성
  - Spring Web
  - Thymeleaf
  - Validation
  - Spring Data JPA
  - Redis
- 기본 패키지 구조 생성
- 메인 페이지 SSR 렌더링
- 공통 예외 처리 구조
- 로컬 개발용 설정 파일 분리
- 필요하면 Docker Compose로 DB/Redis 실행 환경 구성

완료 항목:

- Spring Boot 3.5.12 + Java 25 + Gradle wrapper 기반 프로젝트 생성
- `Spring Web`, `Thymeleaf`, `Validation`, `Spring Data JPA`, `Spring Data Redis` 의존성 구성
- `compose.yaml`로 PostgreSQL / Redis 개발 환경 뼈대 구성
- `application.yml`, `application-local.yml`, `application-test.yml` 분리
- `/` 메인 페이지 SSR 렌더링
- `common`, `auth`, `country`, `game`, `ranking`, `recommendation`, `web` 패키지 구조 고정
- 공통 API 예외 응답 구조 추가
- 컨텍스트 로드 테스트와 홈 컨트롤러 렌더링 테스트 통과

반드시 이해할 것:

- 왜 SSR + API 혼합 구조를 쓰는가
- `Controller -> Service -> Repository` 흐름
- `application.yml`과 프로파일 분리 이유
- 예외 처리를 공통화하는 이유

면접 포인트:

- 왜 React SPA 대신 Thymeleaf SSR을 골랐는가
- 왜 초기에 패키지 구조를 고정했는가

완료 기준:

- 서버가 실행된다.
- 메인 페이지가 렌더링된다.
- 기본 예외 처리 흐름이 있다.
- 패키지 구조를 설명할 수 있다.

### 2. 국가 데이터와 시드 적재

상태: Done

목표:

- 게임과 추천 기능이 공통으로 쓰는 국가 데이터를 신뢰 가능한 형태로 확보한다.

구현 항목:

- `country` 엔티티 설계
- 국가명, ISO 코드, 대륙, 대표 위경도, 인구수 저장 구조
- CSV 또는 JSON 시드 포맷 결정
- 앱 시작 시 시드 적재 또는 초기 데이터 로더 구현
- 데이터 검증 로직 추가

완료 항목:

- `country` 테이블과 `Continent`, `CountryReferenceType` 도메인 추가
- `src/main/resources/data/countries.json` JSON 시드 포맷 확정
- `capitalCityKr`를 포함한 국가 시드 보강과 검증 규칙 추가
- 앱 시작 시 ISO3 기준으로 추가 / 갱신 / 삭제를 동기화하는 `CountrySeedInitializer` 구현
- ISO 코드 중복, 좌표 범위, 필수값을 검사하는 `CountrySeedValidator` 구현
- `GET /api/countries`, `GET /api/countries/{iso3Code}` 조회 API 추가
- `scripts/generate_country_assets.py`로 재생성 가능한 데이터 생성 흐름 추가
- World Bank API + REST Countries 기준 2024 인구수와 대표 좌표를 담은 독립국 시드 194건 동기화
- 시드 적재/조회/예외 응답 통합 테스트 통과

반드시 이해할 것:

- 왜 국가 데이터를 DB에 넣는가
- 대표 좌표와 실제 국경 데이터의 차이
- 시드 데이터와 운영 데이터의 차이
- 왜 `빈 테이블 초기 적재`보다 `ISO3 기준 동기화`가 현재 구조에 더 맞는가

면접 포인트:

- 위치 게임 정답 판정에서 어떤 좌표를 사용했는가
- 인구 데이터 출처와 정규화 전략은 무엇인가
- 왜 처음부터 폴리곤(GeoJSON) 대신 단일 대표 좌표를 택했는가
- 왜 World Bank와 REST Countries를 함께 사용했는가

완료 기준:

- 국가 데이터가 DB에 적재된다.
- 최소한 게임 출제에 필요한 필드를 조회할 수 있다.
- 시드 데이터 형식을 설명할 수 있다.

### 3. 국가 위치 찾기 게임 Level 1

상태: Reworking

목표:

- 동작하는 프로토타입을 `하트 기반 재시도형 아케이드 루프`로 다시 설계한다.
- 위치 게임을 이 프로젝트의 대표 모드로 끌어올린다.

구현 항목:

- 게스트 닉네임 또는 익명 세션 시작
- 게임 세션 시작 API
- 현재 Stage / 하트 / 점수 조회 API
- 선택 상태 확인 후 제출하는 UI 흐름
- 틀리면 같은 Stage 재시도하는 규칙
- 하트 3개와 게임오버 처리
- 맞히면 자동으로 다음 Stage로 진행하는 규칙
- Stage가 올라갈수록 출제 국가 풀이 넓어지는 난이도 정책
- Stage별 점수 계산과 Attempt 기록
- 결과 페이지와 게임오버 화면 정리

현재까지 완료된 리부트 항목:

- `LocationGameSession`에 `livesRemaining`을 추가하고 `LocationGameStage`, `LocationGameAttempt` 도메인으로 재구성
- `POST /api/games/location/sessions`, `GET /state`, `POST /answer`, `GET /result` 중심 인터페이스로 재정리
- 오답 시 같은 Stage 재시도, 하트 3개 소진 시 `GAME_OVER`, 정답 시 자동 다음 Stage 이동 구현
- 고정 5 Stage 종료를 없애고, 정답 시 다음 Stage를 서버가 계속 생성하는 endless 구조 적용
- 현재 Level 1 후보 국가를 인구 기준 상위 72개로 줄여 렌더링 안정성부터 확보
- Level 1 활성 지오데이터를 `Natural Earth 110m` 기반으로 낮춰 초기 로딩과 지글거림을 줄임
- Stage 번호에 따라 이 72개 안에서 `주요 국가 -> 지역 확장` 식으로 후보 풀이 넓어지는 난이도 정책 적용
- Stage 번호, 시도 횟수, 남은 하트에 따라 점수가 달라지는 정책 적용
- `active-countries.geojson`과 `Globe.gl` 기반 3D 지구본 플레이 셸 유지
- 지구본 위 국가 이름 tooltip 제거
- 제출 전 국가명 비노출, 텍스트 선택 HUD 없이 지구본 하이라이트로만 선택 상태를 보여주는 제출 흐름 적용
- 하단 액션 영역은 `선택 취소` 없이 제출 버튼 하나만 유지하고, 다른 국가를 다시 고르면 선택이 곧바로 교체되도록 단순화
- 하트 소진 시 자동 강제 이동 대신 `탈락 모달 -> 홈으로 / 다시 시작` 선택 흐름 적용
- 새 플레이 화면 진입 시 이전 `GAME_OVER` 모달 상태가 남지 않도록 초기화 보강
- `다시 시작`은 새 세션 생성이 아니라 같은 `sessionId`를 초기 상태로 리셋하는 방식으로 변경
- 드래그와 클릭을 분리해 회전 중 오선택을 줄이는 선택 안정화 로직 적용
- `Globe.gl` 폴리곤 클릭만 믿지 않고, 지구본 클릭 좌표를 GeoJSON과 다시 비교하는 직접 hit-test 로직 적용
- GeoJSON 외곽 링 방향을 원본과 맞춰, 국가 내부만 채워지도록 지구본 폴리곤 자산 보정
- 클릭 불안정과 지글거림을 줄이기 위해 위치 게임 폴리곤 자산을 Level 1 전용 `Natural Earth 110m` 기반으로 재구성
- 모든 SSR 페이지에서 `/`로 복귀 가능한 공통 헤더 추가
- 결과 페이지에서 Stage별 Attempt 기록 조회 가능
- 결과 화면에 `총 시도 수 / 1트 클리어 수`를 추가해 디브리프 요약 밀도 보강
- 핵심 점수 정책 단위 테스트와 상태 전이 통합 테스트 통과

리부트에서 남은 일:

- 결과 / 게임오버 화면의 모션과 피드백 완성도 높이기
- 모바일 드래그 감도와 지구본 상호작용 미세 조정
- 시각 연출과 사운드/애니메이션 여부 결정

반드시 이해할 것:

- 왜 세션과 라운드를 분리했는가
- 왜 `세션 / Stage / Attempt` 구조가 필요한가
- 정답 판정을 프론트가 아니라 서버에서 해야 하는 이유
- 왜 하트와 점수를 클라이언트가 아니라 서버가 관리해야 하는가
- 왜 endless 모드에서도 다음 Stage 생성은 서버가 맡아야 하는가
- 왜 지금은 194개 전체보다 72개 주요 국가로 Level 1을 제한했는가
- 왜 난이도 상승을 “국가 풀 확장”으로 설명하는 것이 현재 데이터 구조에 맞는가
- 왜 나라 이름 tooltip을 끄는 것이 게임성에 중요한가
- 왜 `Globe.gl` 기본 polygon click만으로는 안정적인 국가 선택 UX가 안 나올 수 있는가
- 점수 계산은 어떤 기준으로 하는가

면접 포인트:

- 요청 한 번마다 어떤 테이블이 바뀌는가
- 상태 전이: `READY -> IN_PROGRESS -> GAME_OVER`
- 잘못된 세션 ID, 중복 제출, 다른 Stage 제출 같은 예외는 어떻게 막는가
- 왜 지구본 렌더링은 프론트 자산으로 두고, 하트와 점수는 서버가 맡는가
- 왜 `한 문제 = 한 번 제출` 구조를 버리고 재시도형 Stage 구조로 바꾸는가
- 왜 초반에는 주요 국가 위주, 후반에는 전체 국가로 푸는 방식을 택했는가

완료 기준:

- 한 판의 게임을 `하트 3개 규칙`으로 엔드리스하게 진행할 수 있다.
- 틀리면 같은 Stage를 다시 시도한다.
- 하트가 모두 사라지면 게임오버가 된다.
- 세션과 Stage / Attempt 상태가 서버에 남는다.
- 핵심 상태 전이 테스트가 있다.

### 4. 국가 인구수 맞추기 게임 Level 1

상태: Reworking

목표:

- 첫 게임에서 만든 공통 구조를 재사용해 두 번째 모드를 추가한다.

구현 항목:

- 인구수 보기형 문제 출제
- 보기 생성 규칙 정의
- 정답 판정과 점수 계산
- 게임 공통 구조 재사용 여부 정리

현재까지 완료된 항목:

- `game/common`에 `BaseGameSession`, `GameSessionStatus` 공통 구조 추가
- 위치 게임 세션이 공통 세션 구조를 재사용하도록 리팩터링
- `PopulationGameSession`에 하트 3개와 재시작 가능한 공통 세션 흐름 추가
- `PopulationGameStage`, `PopulationGameAttempt` 도메인으로 재구성
- 인구수 근접 국가를 우선 섞는 `PopulationGameOptionGenerator` 구현
- `POST /api/games/population/sessions`, `GET /state`, `POST /answer`, `POST /restart`, `GET /{sessionId}` API로 재정리
- 오답 시 같은 Stage 재시도, 하트 3개 소진 시 `GAME_OVER`, 같은 `sessionId` 재시작 구현
- 고정 5라운드 종료를 없애고 정답 시 다음 Stage를 계속 생성하는 endless 구조 적용
- Stage 번호에 따라 후보 국가 풀이 넓어지는 난이도 정책과 단계형 점수 정책 적용
- Level 1 보기 표현을 `인구 규모 구간 4개` 방식으로 전환
- SSR 시작 페이지, 플레이 HUD, 결과 페이지, 게임오버 모달 1차 구현
- 플레이 HUD에 `선택 상태 / 진행 가이드` 영역을 추가해 현재 입력 상태와 다음 행동을 더 분명하게 노출
- 결과 화면에 `총 시도 수 / 1트 클리어 수`를 추가해 디브리프 요약 밀도를 높임
- 오답 시에는 하단 결과 카드를 띄우지 않고 오버레이만 보여주도록 피드백 강도 조정
- 오답 시 같은 Stage를 다시 그리기 위해 `GET /state`를 재호출하지 않고, 로컬 상태만 갱신하도록 조정
- 정답 시 `획득 점수`만 잠깐 보여 준 뒤 자동으로 다음 Stage를 다시 로드하도록 전환
- 오답 시에도 오버레이와 HUD를 약 `950ms`만 노출한 뒤, 같은 Stage 재시도 상태로 자동 복귀하도록 리듬을 통일
- 홈 / 시작 / 결과 화면에 공통 `cold space` 테마 1차 적용
- 옵션 생성 단위 테스트와 인구수 게임 상태 전이 통합 테스트 통과
- 인구수 게임 아케이드 리부트 설계 문서 초안 작성

리부트에서 새로 해야 할 일:

- 정답/오답 연출, 연속 정답 보너스, 사운드 여부 같은 아케이드 polish 결정
- 현재 구간 경계가 실제 플레이 감각에 맞는지 조정
- 모바일 기준 버튼 간격, HUD 밀도, 결과 테이블 가독성 미세 조정

반드시 이해할 것:

- 위치 게임과 인구수 게임의 공통점 / 차이점
- 어떤 부분을 공통화하고 어떤 부분은 분리해야 하는가
- 왜 현재 인구수 게임을 `완료`가 아니라 `리부트 중`으로 봐야 하는가
- 왜 Level 1은 정확 숫자 4개 보기보다 구간형이 더 적합한가
- 왜 인구수 게임도 `세션 / Stage / Attempt` 구조가 필요한가
- 왜 endless 모드에서도 다음 Stage 생성은 서버가 맡아야 하는가

면접 포인트:

- 공통 세션 구조를 어떻게 재사용했는가
- 전략 패턴 또는 정책 클래스를 도입했는가, 왜 했는가
- 왜 지금은 추상 베이스 세션까지만 공통화했고, 라운드는 모드별로 분리했는가
- 왜 위치 게임과 같은 아케이드 루프를 인구수 게임에도 다시 적용하려 하는가
- 왜 Level 1 구간 경계는 한 번 더 조정할 여지가 있는가

완료 기준:

- 한 판의 인구수 게임을 `하트 3개 규칙`으로 진행할 수 있다.
- 틀리면 같은 Stage를 다시 시도한다.
- 하트가 모두 사라지면 게임오버가 된다.
- 같은 `sessionId`로 다시 시작할 수 있다.
- 점수와 난이도 정책이 설명 가능하다.
- 공통 코드와 모드별 코드의 경계가 설명 가능하다.

### 5. Redis 랭킹 시스템

상태: Done

목표:

- 게임 결과를 빠르게 집계하고 상위 점수를 즉시 조회할 수 있게 만든다.

구현 항목:

- 게임 종료 시 최종 점수 확정
- RDB 결과 저장
- Redis Sorted Set 반영
- 일간 / 전체 키 전략 정의
- 랭킹 조회 API
- 랭킹 화면 표시

현재까지 완료된 항목:

- `leaderboard_record` 테이블로 게임 종료 run을 별도 저장
- 위치 게임 / 인구수 게임 `GAME_OVER` 시점에 랭킹 레코드 생성
- 같은 `sessionId` 재시작 구조와 충돌하지 않도록 `runSignature` 기준 중복 방지
- `LeaderboardRankingPolicy`로 총점, 클리어 수, 총 시도 수를 합친 내부 정렬 점수 정의
- RDB 저장 후 `after commit` 시점에 Redis Sorted Set 전체 / 일간 키 반영
- `GET /api/rankings/{gameMode}` 조회 API 추가
- `/ranking` SSR 화면 추가
- Redis 키가 비어 있을 때 RDB 상위 기록으로 fallback 조회 후 Redis 재구성
- Redis가 내려가 있거나 읽기/재구성에 실패해도 `/api/rankings/*`, `/ranking`, `/stats` public read path는 `LeaderboardService`가 DB top run fallback으로 계속 응답하고, Redis warm/rebuild는 best-effort로만 시도하게 정리
- `/ranking` 화면에서 15초 간격 폴링과 수동 새로고침으로 갱신 체감 추가
- `/ranking` 화면에서 `위치/인구수`, `전체/일간` 필터 전환으로 active 보드만 크게 보는 UI 적용
- `/ranking` 화면 자동 갱신을 "숨겨진 10개 보드 fan-out"이 아니라 "현재 보고 있는 active 보드 1개 갱신"으로 줄이고, 모드/범위 전환 직후에는 새 active 보드만 즉시 fetch하도록 정리
- `GET /ranking` 첫 SSR은 기본 active 보드인 `location:ALL`만 실제로 읽고, 나머지 9개 보드는 placeholder 행과 `data-initial-rendered="false"` 상태로 defer render한 뒤 첫 전환 시 fetch하도록 정리
- 일간 보드 설명 카피의 기준 날짜가 SSR 시점에 멈추지 않도록, polling 응답의 `targetDate`로 `data-copy`를 다시 동기화
- 동점 처리 규칙을 화면에 명시하고, 현재 보드 제목/설명도 필터 상태에 맞게 갱신
- 랭킹 통합 테스트 통과
- `LeaderboardPageControllerTest`로 `/ranking` 첫 진입이 `LeaderboardService.getLeaderboard(LOCATION, ALL, 10)` 한 번만 호출되는지 고정

이 단계에서 남은 일:

이 단계를 마치며 다음 단계로 넘긴 일:

- SSE / WebSocket 업그레이드는 `9단계 실시간성 고도화`에서 판단하고 구현한다.
- 닉네임 외 식별자 정책은 `8단계 인증, 전적, 마이페이지`와 함께 정리한다.
- 지금의 `기본 보드 SSR 1개 + active board polling + defer board first-load` 조합이 실제 TTFB와 사용자 체감 기준으로 충분한지 실측 후 판단한다.

반드시 이해할 것:

- 왜 RDB만으로 랭킹을 만들지 않았는가
- Sorted Set의 score/member 구조
- Redis 장애 시 어떤 문제가 생기는가
- 왜 세션 재시작이 가능한 구조에서는 `게임 세션`과 `랭킹 run 결과`를 분리해야 하는가
- 왜 Redis 반영을 DB 저장 `after commit` 뒤로 미루는가
- 왜 조회 경로를 `Redis 상위 id -> RDB 상세 조회`로 나눴는가
- 왜 첫 실시간 전달 방식으로 SSE 대신 짧은 주기 폴링을 택했는가
- 왜 화면 필터 전환은 프론트가 맡고, 랭킹 정렬 규칙은 서버가 계속 맡아야 하는가

면접 포인트:

- 왜 Redis를 썼는가
- 점수 저장의 진실 공급원(source of truth)은 어디인가
- 동점자 처리 규칙은 무엇인가
- Redis 키가 날아가면 어떻게 복구하는가
- 왜 보드 전환 UI를 위해 API를 새로 쪼개지 않고 기존 API를 재사용했는가

완료 기준:

- 게임 종료 후 랭킹이 반영된다.
- 상위 점수 조회가 가능하다.
- Redis와 RDB의 역할 분담을 설명할 수 있다.
- 현재는 왜 polling으로 닫고, SSE는 왜 다음 단계로 넘겼는지 설명할 수 있다.

### 6. 설문 기반 추천 엔진

상태: Done

목표:

- 추천 결과의 근거를 서버 계산으로 확보한다.

구현 항목:

- 설문 문항 정의
- 답변 저장 구조
- 국가 메타데이터와 가중치 설계
- 상위 3개 국가 계산
- 추천 결과 페이지 표시

현재까지 완료된 항목:

- `RecommendationSurveyAnswers`로 설문 답변 타입과 enum 선택지 구조 정의
- `RecommendationQuestionCatalog`로 20개 문항과 SSR 렌더링용 옵션 카탈로그 구성
- `RecommendationCountryProfileCatalog`로 추천 계산에 쓰는 국가 프로필 30개 정의
- `RecommendationSurveyForm`으로 요청 바인딩 및 답변 유효성 검증 추가
- `RecommendationSurveyService.recommend()`에서 가중치 기반 상위 3개 국가 계산 구현
- `RecommendationSurveyService` 점수식을 trade-off 중심으로 조정해 정확 일치 보너스, 비용 선호별 초과 물가 패널티, 영어 지원 필요도 가중치, 극단 기후 mismatch penalty, coherence bonus, 동점 보조 비교 기준을 추가
- 단순 취향 체크 대신 `기후 방향`, `사계절 변화`, `기후 적응 성향`, `집 크기 vs 중심 접근성`, `초기 적응 친화도`, `디지털 생활 편의`, `문화·여가`, `장기 기반`까지 묻는 20문항 구조로 재설계하고, 피드백 스냅샷도 20개 답변 기준으로 확장
- `/recommendation/survey` 설문 페이지와 `/recommendation/survey` POST 결과 페이지 SSR 흐름 추가
- 결과 페이지에서 설문 입력 요약, top 3 국가, 서버 계산 이유 3개 노출
- 추천 결과 top 3 자체는 저장하지 않고, 결과 페이지마다 서버 세션에 `feedbackToken -> 추천 문맥(surveyVersion + engineVersion + 선택한 20개 답변)`을 잠깐 저장한 뒤 `1~5점 만족도`만 익명 피드백으로 수집
- 추천 결과 만족도 입력은 커스텀 `aria-pressed` 버튼 묶음 대신 `fieldset + radio + live region`으로 다시 정리해, 키보드와 스크린리더에서도 실제 단일 선택 점수 입력으로 동작하게 맞춤
- `/dashboard/recommendation/feedback` SSR 페이지와 admin session으로 보호된 `/api/recommendation/feedback/summary` API로 버전 조합별 평균 점수, 응답 수, 점수 분포 조회 추가
- 추천 기능을 홈 화면과 공통 헤더 내비게이션에 연결
- 공통 CSS에서 버튼, 패널, 입력창, 모달, 테이블 셸, 배지의 모서리를 완전한 사각형으로 통일하고, 스타일 버전 쿼리까지 적용해 실제 반영 경로를 함께 정리
- 공통 shell에 dark/light theme toggle을 추가하고, `html[data-theme] + CSS 변수 + localStorage` 조합으로 테마 상태를 사이트 전체에서 유지
- light theme에서 primary button 텍스트, secondary button 표면, filter/option 선택 상태가 다크 값으로 남지 않도록 공통 interactive color token을 정리해 버튼 대비를 다시 맞춤
- light theme 홈 화면에서 dark slab처럼 남던 panel, hero callout, mode card, form input 표면을 밝은 blue-tinted layer로 다시 맞춰 섹션 간 색 계층을 정리
- public/admin/game 템플릿 전체를 다시 읽고, light theme용 subtitle/meta/hint/badge/chip/globe/modal/message token을 추가해 화면별 다크 하드코딩과 단일 경고 박스 스타일을 정리
- `message-box`를 neutral / info / error / success tone으로 분리하고, location/population/ranking JS도 로딩/성공/에러 상황에 맞는 tone을 넘기도록 보정
- globe playfield, stage overlay, game-over modal도 light theme에서 검은 slab처럼 보이지 않도록 전용 surface token으로 다시 맞추고, location/population/ranking JS asset version을 함께 올려 캐시 혼선을 줄임
- light theme 기본 진입값을 `light`로 바꾸고, hero account callout / panel / 공통 shell surface를 더 하얗고 덜 흐리게 다시 조정해 사용자가 체감하는 dark 잔재를 한 번 더 제거
- theme toggle은 다음 액션이 아니라 현재 모드를 표시하도록 바꾸고, fragment script와 asset version도 함께 올려 토글 의미와 캐시 혼선을 줄임
- light theme를 dark shell의 완화판이 아니라 `white surface + slate text + cobalt accent` 팔레트로 다시 정의하고, body noise / panel glint를 꺼서 홈과 공통 화면이 처음부터 light 제품처럼 보이도록 리부트
- 홈 화면은 hero 2열, 우측 요약 패널, 2x2 모드 카드, 더 읽기 쉬운 header/nav/button/type scale로 재구성해 라이트 모드 첫 화면의 위계를 다시 세움
- 홈 hero 우측 `바로 시작하는 흐름` 박스를 제거하고, public 홈 copy에서는 `모드` 대신 `게임`을 사용해 제품 언어를 더 단순하게 맞춤
- 홈 hero에서는 `서비스 현황 보기` 옆에 계정 callout을 붙여 보조 행동을 한 줄로 묶고, 실시간 랭킹은 게임 카드가 아니라 공통 header navigation으로 올려 정보 탐색과 게임 시작 경로를 분리
- 홈 hero support row에서는 `서비스 현황 보기` 링크도 callout과 같은 surface/border/shadow 계층으로 맞춰, 같은 수평선에서 보이는 보조 행동 카드의 높이와 덩어리감을 통일
- light theme는 flat white shell에 머무르지 않도록 배경 radial layer, glass-like chrome, panel glint, card shadow, hover lift를 다시 올려 밝은 화면에서도 생동감과 입체감이 느껴지게 보정
- public 화면에서 내부 구현 문구를 제거하고, 버전/집계/로드맵은 별도 admin 화면으로 옮기기 위한 `/Users/alex/project/worldmap/docs/PLAYER_COPY_AND_ADMIN_SPLIT_PLAN.md` 설계 초안 작성
- 홈, 추천 설문/결과, 랭킹 public 화면의 copy를 제품 언어 중심으로 다시 쓰고, 추천 결과 화면에서 내부 운영 페이지 링크를 제거
- `/dashboard` read-only 대시보드와 `AdminDashboardService`, `AdminPageController`를 추가해 public과 내부 운영 화면을 실제 라우트 수준에서 분리
- 기존 `/recommendation/feedback-insights` public route는 `/dashboard/recommendation/feedback`으로 redirect해 북마크 호환성을 유지
- `/dashboard/recommendation/persona-baseline` read-only 화면으로 weak scenario와 active-signal 비교 시나리오를 운영 화면에 노출
- `survey-v4 / engine-v5`에서는 `VALUE_FIRST` 응답의 초과 물가 penalty를 더 강하게 적용해 `P02`, `P14`, `P15` 같은 저비용 시나리오의 후보 구성을 다시 실험
- `engine-v6`에서는 `EXPERIENCE + TRANSIT_FIRST + VALUE_FIRST` 조합에 한해 `transit + newcomer + digital + 기본 안전성`을 함께 보는 보정을 추가해 `P15`에 `말레이시아`를 다시 끌어올림
- `engine-v7`에서는 `MIXED + BALANCED` 생활과 `안전 / 공공서비스` 중요도가 함께 높을 때 `safety + welfare + housing + newcomer`를 함께 보는 civic base bonus를 추가해 `P04`, `P06`의 균형형 후보 구성을 다시 조정
- `engine-v8`에서는 `VALUE_FIRST + SAFETY + MIXED + BALANCED + 영어 적응 MEDIUM` 조합에 한해 `english + newcomer + housing + safety + welfare`를 함께 보는 soft landing bonus를 추가해 `P06`의 3위 후보를 `포르투갈`로 교체
- public 헤더는 `Home / My Page`만 남기고, 게임별 직접 이동은 본문 CTA로만 남겨 진입 구조를 단순화
- `/mypage` placeholder SSR 화면을 먼저 추가해 다음 8단계 인증/전적 확장의 진입점을 미리 고정
- 추천 페이지 통합 테스트와 추천 서비스 단위 테스트 통과

이 단계에서 남은 일:

- 후보 풀이 넓어진 뒤에도 결과가 특정 지역으로 치우치지 않는지 확인
- 낮은 만족도 응답을 답변 조합 단위로 더 내려다볼지 결정
- `/admin/build`처럼 남은 운영 화면을 더 확장
- 다음 단계에서 서브 에이전트 평가에 사용할 설문 버전/시나리오 자산 고정
- 지구본 플레이 화면과 게임오버 모달까지 light theme 시각 톤을 더 다듬을지 결정
- 추천 결과 / auth form / stats 화면까지 light theme 실화면 검증을 더 할지 결정
- 모바일 폭에서 dashboard / ranking / recommendation 결과 카드 밀도가 적절한지 추가 확인

반드시 이해할 것:

- 왜 추천 후보 계산을 LLM에 맡기지 않는가
- 문항별 가중치가 결과에 어떤 영향을 주는가
- 왜 추천 결과는 저장하지 않고, 만족도 피드백만 익명으로 수집하는가
- 왜 버전별 집계를 컨트롤러가 아니라 서비스 집계로 만드는가
- 왜 country 테이블만으로 부족한 속성은 별도 프로필 카탈로그로 시작했는가

면접 포인트:

- 추천 로직의 결정성을 어떻게 확보했는가
- 추천 품질은 어떤 데이터에 의존하는가
- 왜 결과 설명과 결과 계산을 분리했는가

완료 기준:

- 동일한 입력에 동일한 추천 결과가 나온다.
- 추천 근거를 설명할 수 있다.
- 플레이어용 추천 화면과 내부 운영 화면을 분리할 설계 기준이 있다.

### 7. AI-assisted 설문 개선 체계

상태: Done

목표:

- 런타임 LLM 호출 없이, 개발 단계에서 AI/서브 에이전트로 설문 품질을 반복 개선하는 체계를 만든다.

구현 항목:

- 설문 버전 관리 기준 정리
- 페르소나 평가 시나리오 세트 고정
- 서브 에이전트 역할 분담 문서화
- 시나리오별 기대 후보 / 기대 만족도 평가 절차 정리
- 만족도 실측 데이터와 오프라인 시나리오 평가를 함께 보는 개선 루프 문서화

현재까지 완료된 항목:

- `docs/recommendation/OFFLINE_AI_SURVEY_IMPROVEMENT.md`로 오프라인 AI 개선 루프 문서화
- `docs/recommendation/PERSONA_EVAL_SET.md`로 18개 페르소나 평가 시나리오 정리
- `RecommendationOfflinePersonaCoverageTest`로 현재 추천 엔진 baseline 품질 하한 고정
- 현재 baseline에서 18개 중 15개 시나리오가 기대 후보 1개 이상을 top 3에 포함하는지 자동 검증
- `RecommendationOfflinePersonaSnapshotTest`로 현재 추천 엔진의 18개 시나리오 top 3 순서를 snapshot으로 고정
- 기존 14개 중립 baseline 외에, 새 두 문항을 적극적으로 쓰는 `P15~P18` 비교 시나리오를 추가해 `EXPERIENCE / TRANSIT_FIRST`와 `STABILITY / SPACE_FIRST`가 실제 후보 구성을 바꾸는지 검증
- `docs/recommendation/SURVEY_V2_PROPOSAL.md`로 우선 개선 대상 시나리오와 v2 개정안 초안 정리
- `survey-v4 / engine-v4` 20문항 trade-off 설문 반영 후 baseline fixture, coverage, snapshot을 현재 결과 기준으로 다시 고정
- `engine-v5`에서는 `VALUE_FIRST > BALANCED > QUALITY_FIRST` 순으로 초과 물가 penalty 강도를 나눠 비용 민감 시나리오의 top 3 변화를 snapshot으로 다시 고정
- `engine-v6`에서는 탐색형/교통형 저예산 시나리오 전용 보정을 추가하고, `P15`가 `뉴질랜드 + 말레이시아`를 다시 포함하는지 coverage/snapshot으로 고정
- `engine-v7`에서는 `P04`가 `스페인 + 아일랜드 + 우루과이`, `P06`이 `스페인 + 우루과이`를 포함하도록 snapshot/coverage를 다시 고정
- `engine-v8`에서는 `P06`이 `스페인 + 우루과이 + 포르투갈`이 되도록 snapshot/coverage를 다시 고정
- `engine-v9`에서는 `QUALITY_FIRST + SAFETY HIGH + English HIGH + MIXED + BALANCED + LOW tolerance` 조합에 한해 family base bonus를 추가해 `P11`에 `캐나다`를 다시 포함시키고 baseline을 `18 / 18`까지 올림
- `engine-v10`에서는 `WARM + FAST + CITY + QUALITY_FIRST + English/Digital/Culture HIGH` 조합에만 작동하는 global hub bonus를 추가해 `P01`, `P05`의 1위 anchor를 `싱가포르`로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v11`에서는 `WARM + BALANCED + VALUE_FIRST + MIXED + English MEDIUM + FOOD HIGH + DIVERSITY MEDIUM+` 조합에만 작동하는 foodie starter bonus를 추가해 `P02`의 1위 anchor를 `말레이시아`로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v12`에서는 `MILD + BALANCED + MIXED + English MEDIUM + HIGH publicService` 조합에만 작동하는 temperate public base bonus를 추가해 `P04`의 1위 anchor를 `우루과이`로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v13`에서는 `MILD + VALUE_FIRST + SAFETY HIGH + publicService MEDIUM + newcomer LOW` 조합에만 작동하는 practical public value bonus를 추가해 `P06`의 1위 anchor를 `우루과이`로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v14`에서는 `WARM + QUALITY_FIRST + CITY + English HIGH + publicService HIGH` 조합에만 작동하는 premium warm hub bonus를 추가해 `P09`의 1위 anchor를 `아랍에미리트`로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v15`에서는 `COLD + RELAXED + NATURE + English MEDIUM + safety HIGH` 조합에만 작동하는 soft nature base bonus를 추가해 `P08`의 1위 anchor를 `뉴질랜드`로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v16`에서는 `MILD + FAST + CITY + BALANCED + English LOW + diversity HIGH` 조합에만 작동하는 cosmopolitan pulse bonus를 추가해 `P10`의 1위 anchor를 `미국`으로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v17`에서는 `MILD + FAST + CITY + QUALITY_FIRST + English HIGH + diversity HIGH` 조합에만 작동하는 temperate global city bonus를 추가해 `P13`의 1위 anchor를 `미국`으로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v18`에서는 `WARM + VALUE_FIRST + MIXED + English MEDIUM + publicService HIGH` 조합에만 작동하는 accessible warm value hub bonus를 추가해 `P14`의 1위 anchor를 `말레이시아`로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v19`에서는 `MILD + QUALITY_FIRST + MIXED + English HIGH + safety HIGH` 조합에만 작동하는 temperate family bridge bonus를 추가해 `P11`의 1위 anchor를 `캐나다`로 되돌리고 snapshot/coverage를 다시 고정
- `engine-v20`에서는 `MILD + BALANCED + RELAXED + NATURE + VALUE_FIRST + TRANSIT_FIRST + EXPERIENCE` 조합에만 작동하는 exploratory nature runway bonus를 추가해 `P15`의 1위 anchor를 `뉴질랜드`로 되돌리고 snapshot/coverage를 다시 고정
- `P07`은 엔진을 더 비트는 대신 baseline anchor를 `싱가포르`로 재정의해, 현재 20문항이 읽는 `warm megacity + food` 의미와 평가 기준을 맞춤
- `RecommendationPersonaBaselineCatalog`로 baseline 시나리오 정의를 main source로 올리고, `/dashboard/recommendation/persona-baseline`이 현재 엔진 결과를 기준으로 weak scenario를 자동 계산하게 정리
- weak scenario가 0개가 된 뒤에는 `/dashboard/recommendation/persona-baseline`이 기대 후보 top 3 여부뿐 아니라 `기대 1위 anchor`가 실제 top 1인지도 같이 계산하도록 확장
- `/dashboard/recommendation/feedback`는 현재 버전 만족도와 baseline drift를 함께 읽어, 다음 액션을 `피드백 수집 / weak scenario / 문구 점검 / rank drift` 중 하나로 정리하는 운영 메모를 추가
- local demo bootstrap은 current `survey-v4 / engine-v20` 피드백 샘플 5개를 함께 만들어, fresh local 환경에서도 `/dashboard/recommendation/feedback`이 `현재 엔진 유지` 메모를 바로 보여 주도록 정리
- `/Users/alex/project/worldmap/docs/PLAYER_COPY_AND_ADMIN_SPLIT_PLAN.md`로 public copy와 admin 운영 화면 분리 설계 정리
- `/dashboard/recommendation/persona-baseline` read-only 화면으로 현재 baseline 18/18, weak scenario 자동 계산, anchor drift 0개, active-signal 4개를 운영 화면에서 바로 확인
- `/dashboard` read-only 대시보드를 추가해 현재 survey/engine 버전, 질문 수, 후보 국가 수, 만족도 수집 현황을 한 화면에서 조회
- `/dashboard/recommendation/feedback`로 버전별 만족도 집계를 실제 운영 화면으로 이동

반드시 이해할 것:

- 왜 런타임 LLM 호출을 빼는 것이 현재 포트폴리오에 더 맞는가
- 왜 AI를 서비스 기능이 아니라 설문 개선 도구로 돌리는가
- 서브 에이전트가 어떤 산출물을 만들고, 사람은 어디서 최종 판단하는가
- 왜 baseline 평가를 문서가 아니라 테스트로도 같이 고정해야 하는가
- 왜 coverage 하한 테스트만으로는 부족하고 top 3 snapshot도 함께 잡아야 하는가

면접 포인트:

- 왜 AI를 오프라인 설문 품질 개선 루프에만 사용했는가
- 과금, 결정성, 테스트 용이성 관점에서 어떤 trade-off를 택했는가

완료 기준:

- 설문 개선 루프 문서를 보고 버전 실험 절차를 설명할 수 있다.
- 페르소나 시나리오 세트와 실제 만족도 집계를 함께 볼 수 있다.

이 단계에서 남은 일:

- baseline은 18 / 18, anchor drift 0개까지 맞췄으니, 다음엔 6단계를 닫고 실시간성 고도화나 Level 1 polish로 넘어갈지 판단
- 블로그는 연대기 성격이 강하므로, 현재 코드 재현이 목적일 때는 `blog/50-current-state-rebuild-map.md` 기준으로 최신 글과 구버전 글을 구분해 읽는 흐름을 유지
- local demo / blog 재현성은 `blog/50-current-state-rebuild-map.md`의 실행 체크리스트와 `docs/LOCAL_DEMO_BOOTSTRAP.md`를 함께 기준으로 본다
- 현재는 `/dashboard/recommendation/feedback`이 그 판단을 운영 메모로 내려주므로, 다음 실험은 메모가 가리키는 우선순위 하나만 좁게 집행
- 실험 결과를 snapshot과 비교해 어떤 시나리오 순위가 움직였는지 문서화
- rank drift가 큰 시나리오 중 실제 만족도 저점과 겹치는 조합이 있는지 확인
- baseline 18 / 18을 유지하면서 만족도 데이터와의 간극이 있는지 검토

### 8. 인증, 전적, 마이페이지

상태: Done

목표:

- 게스트 기반 MVP를 사용자 계정 기반 서비스로 확장한다.

구현 항목:

- 회원가입 / 로그인
- 사용자별 게임 기록 조회
- 내 랭킹 확인
- admin 라우트 접근 제어
- 운영용 대시보드 권한 분리
- 게스트 플레이 유지 + 로그인 시 현재 브라우저 기록 귀속
- 운영용 admin 계정 provisioning

현재까지 완료된 항목:

- `/mypage` placeholder SSR 화면과 public 헤더 진입점 추가
- `/Users/alex/project/worldmap/docs/SIMPLE_ACCOUNT_PROGRESS_PLAN.md`로 게스트 세션 유지형 단순 계정 설계 고정
- 계정 목적을 `닉네임 유지`, `점수 누적`, `내 전적 조회`로 한정하고 이메일/소셜/커뮤니티 기능은 MVP 범위에서 제외
- `memberId`와 `guestSessionKey`를 같이 두는 소유권 모델 초안 정리
- 회원가입/로그인 시 현재 브라우저 세션의 guest 기록만 계정으로 귀속하는 흐름 설계
- `member`, `MemberRole`, `MemberRepository`를 추가해 단순 계정 도메인 뼈대를 먼저 만들었다
- `GuestSessionKeyManager`가 같은 브라우저 세션 안에서 공통 `guestSessionKey`를 발급/유지하도록 연결했다
- 위치/인구수/수도/국기/인구 비교 퀵 배틀 게임 세션과 `leaderboard_record`가 모두 `memberId` 또는 `guestSessionKey`로 소유자를 저장하도록 ownership 필드를 추가했다
- 같은 브라우저 세션으로 다섯 게임을 시작하면 동일 `guestSessionKey`를 공유하고, 게스트 게임 종료 시 랭킹 레코드도 같은 ownership을 유지하는 테스트를 고정했다
- BCrypt 기반 `MemberPasswordHasher`, `MemberAuthService`, `MemberSessionManager`를 추가해 `닉네임 + 비밀번호` 단순 계정의 세션 로그인 흐름을 만들었다
- `/signup`, `/login`, `/logout` SSR 폼과 `/mypage`의 guest 유도 / 로그인 상태 shell 분기를 추가했다
- 로그인 사용자가 새로 시작하는 다섯 게임은 request nickname 대신 계정 닉네임을 사용하고, 세션/랭킹 기록을 `memberId` ownership으로 저장하도록 연결했다
- `GuestProgressClaimService`를 추가해 회원가입/로그인 직후 현재 브라우저의 `guestSessionKey` 기록을 계정 ownership으로 귀속하도록 연결했다
- claim 범위는 현재 위치/인구수/수도/국기/인구 비교 퀵 배틀 5개 게임 세션과 `leaderboard_record` 전체다. signup/login 통합 테스트로 같은 브라우저에서 시작한 모든 guest 세션이 한 번에 같은 `memberId`로 귀속되는지 고정했다
- guest로 저장됐던 게임 세션 / 랭킹 레코드는 claim 시 `memberId`를 채우고 `guestSessionKey`는 비워 ownership을 단일화한다
- `GameSessionAccessContext`, `GameSessionAccessContextResolver`를 추가해 게임 `play / state / answer / restart / result` 요청이 현재 `memberId` 또는 같은 브라우저의 `guestSessionKey` ownership과 일치하는 세션만 읽도록 묶었다
- access context는 `memberId`와 `guestSessionKey`를 함께 들고 간다. 그래서 로그인 직후 아직 claim되지 않은 same-browser guest 세션도 이어서 접근할 수 있고, 다른 브라우저에서 `sessionId`만 알아도 열 수는 없게 된다
- 게임 결과는 terminal resource로 다시 정의했다. `READY`, `IN_PROGRESS` 상태에서는 `/result` API와 결과 페이지가 404를 돌려 진행 중 정답과 시도 이력이 먼저 노출되지 않는다
- `MemberSessionManager.signIn()`은 로그인 / 회원가입 성공 시 `changeSessionId()`를 호출해 session fixation 위험을 줄인다
- 무결성 1차로 각 게임 session repository에 write 전용 `findByIdForUpdate()`를 추가하고, `submitAnswer` / `restartGame`는 session row를 잠근 뒤 처리하도록 바꿨다. 같은 `sessionId`의 write가 직렬화되므로 attempt 번호 계산, 다음 stage 생성, restart reset이 서로 충돌해 500으로 번지는 위험을 먼저 줄였다
- public 플레이 화면은 state 응답에서 `stageId`, `expectedAttemptNumber`를 함께 받고, 답안 제출 시 그대로 돌려보낸다. 서버는 현재 stage/attempt와 다르면 stale submit으로 보고 `409`를 반환하므로, 같은 오답 payload 재전송이 life를 두 번 깎는 문제를 막을 수 있다
- `LeaderboardService`는 `runSignature` unique 충돌을 no-op로 삼도록 바뀌어, terminal 상태 중복 submit이 들어와도 같은 run의 leaderboard row를 한 번만 남긴다
- `MyPageService`를 추가해 `/mypage`가 로그인 사용자의 `leaderboard_record`를 읽어 총 완료 플레이 수, 모드별 최고 점수, 현재 전체 순위, 최근 플레이 10개를 보여주도록 연결했다
- `/mypage`는 raw game session 전체보다 먼저 `leaderboard_record`를 읽는다. 완료된 run 단위가 이미 정규화돼 있어 최고 기록과 최근 기록을 설명하기 쉽고, 최근 플레이/베스트 카드의 현재 전체 순위도 같은 read model에서 계산할 수 있기 때문이다
- guest로 한 판 끝낸 뒤 회원가입하면, 귀속된 `leaderboard_record`가 즉시 `/mypage` 최근 플레이와 최고 기록에 반영되는 통합 테스트를 고정했다
- `/admin/**`는 `AdminAccessInterceptor`가 보호하고, 비로그인 사용자는 `/login?returnTo=...`로 보내며, 로그인한 일반 사용자(`USER`)는 403으로 막는다
- admin 접근 제어는 각 컨트롤러 메서드가 아니라 인터셉터에 뒀다. 이 규칙은 비즈니스 상태 변경보다 라우트 입구의 공통 진입 정책이기 때문이다
- `AdminAccessGuard`는 세션의 `memberId`로 현재 회원을 다시 조회해 `role`을 재확인하고, 세션 닉네임/role도 현재 DB 값으로 동기화한다. 그래서 admin 권한이 회수된 뒤 기존 세션이 남아 있어도 `/dashboard/**`와 `/api/recommendation/feedback/summary`는 즉시 막힌다
- public `site-header`도 `SiteHeaderModelAdvice`를 통해 같은 `AdminAccessGuard`를 사용한다. 그래서 `Dashboard` 링크 노출 역시 세션 role 문자열이 아니라 현재 DB role 기준으로 맞춰지고, 권한 강등/승격이 홈 / Stats / Ranking / My Page 같은 SSR shell에 바로 반영된다
- `CurrentMemberAccessService`는 admin 전용 guard 밖에서도 공통 current-member source로 쓴다. 홈 hero의 계정 CTA, `/login`·`/signup` GET 진입 분기, `/mypage`, 5개 게임 시작 페이지, 세션 시작 API, `GameSessionAccessContextResolver`는 세션 캐시가 아니라 이 서비스가 확인한 현재 회원 row를 기준으로 움직인다
- 같은 request 안에서는 `CurrentMemberAccessService`가 request attribute cache를 써서 current member를 한 번만 푼다. 그래서 admin interceptor + SSR advice, SSR advice + auth controller, SSR advice + game access context처럼 계층이 겹쳐도 DB 재조회 기준은 유지하면서 중복 read는 줄일 수 있다
- 로그인 폼은 `returnTo`를 받아, admin 사용자가 로그인 후 원래 보려던 `/admin` 경로로 바로 돌아오도록 정리했다
- `/mypage`는 finished session에 속한 stage를 다시 읽어 모드별 `클리어 Stage 수`, `1트 클리어율`, `평균 시도 수`를 추가로 보여준다
- 이 성향 지표는 `leaderboard_record`가 아니라 raw stage 집계에서 나온다. 최고 점수/최근 완료 이력과 달리, 플레이 방식 자체는 stage 시도 수를 봐야 설명할 수 있기 때문이다
- `MyPageServiceIntegrationTest`로 위치/인구수 게임을 실제로 한 판씩 끝낸 뒤, raw stage 기반 성향 지표가 기대값으로 계산되는지 고정했다
- `/mypage` read model은 현재 `bestRuns`, `modePerformances`, `recentPlays` per-mode 리스트 구조로 정리돼, 위치/수도/국기/인구 비교/인구수 5개 게임을 같은 템플릿 iteration으로 렌더링한다
- `MyPageServiceIntegrationTest`, `MyPageControllerTest`, `AuthFlowIntegrationTest`로 5개 게임 노출, 현재 전체 순위 라벨, 회원가입 직후 `/mypage` 연결이 함께 유지되는지 고정했다
- `MemberCredentialPolicy`로 닉네임 / 비밀번호 규칙을 회원가입과 admin bootstrap이 함께 재사용하도록 정리했다
- `AdminBootstrapProperties`, `AdminBootstrapService`, `AdminBootstrapInitializer`를 추가해 서버 시작 시 환경변수 기준 운영용 admin 계정을 자동 생성하거나 기존 계정을 `ADMIN`으로 승격하도록 연결했다
- bootstrap admin은 `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED`, `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME`, `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD`로 제어한다
- 운영용 admin 계정은 signup UI와 분리했다. admin provisioning은 플레이어용 공개 흐름이 아니라 배포 환경의 운영 규칙이기 때문에 startup runner + service 조합으로 두는 편이 현재 구조에 더 맞다
- `AdminBootstrapServiceTest`, `AdminBootstrapIntegrationTest`로 신규 생성 / 기존 USER 승격 / 잘못된 설정 fail-fast / 해시 저장을 고정했다
- 운영 화면 진입 주소를 `/admin`에서 `/dashboard`로 바꾸고, `ADMIN` 로그인일 때만 public 헤더에 `Dashboard` 버튼이 보이게 정리했다
- 기존 `/admin/**`는 즉시 제거하지 않고 `/dashboard/**`로 redirect해 북마크 호환성과 기존 링크 테스트를 유지했다
- 운영 화면 내부 헤더도 `Home -> Dashboard -> My Page -> Recommendation -> Baseline` 순으로 정리해 제품 화면과 운영 화면의 이동 언어를 맞췄다
- `/dashboard` 첫 화면에 `총 회원 수`, `오늘 활성 회원`, `오늘 활성 게스트`, `오늘 시작된 세션`, `오늘 완료된 게임`, `오늘 모드별 완료 수` 카드를 추가했다
- Dashboard 수치는 한 저장소에서 다 읽지 않는다. 회원 수는 `MemberRepository`, 오늘 활성 수는 각 게임 세션 repository의 `startedAt` distinct 집계, 오늘 완료 수는 `LeaderboardRecordRepository.finishedAt` 집계에서 읽는다
- `AdminPageIntegrationTest`로 Dashboard 수치 카드가 실제 데이터 기준으로 렌더링되는지 고정했다
- `ServiceActivityService`와 `ServiceActivityView`로 활동 지표 read model을 분리해 `/dashboard`와 공개 `/stats`가 같은 기준의 숫자를 재사용하도록 정리했다
- `/stats` 공개 페이지를 추가해 일반 사용자도 `총 가입자 수`, `오늘 활성 플레이어 수`, `오늘 시작된 세션 수`, `오늘 완료된 게임 수`, `오늘 위치/인구수 Top 3`를 볼 수 있게 했다
- 공개 `/stats`는 서비스 활성을 보여 주는 숫자만 노출하고, 추천 만족도 집계 / persona baseline / surveyVersion 같은 내부 운영 정보는 계속 `/dashboard`에만 남긴다
- `DemoBootstrapProperties`, `DemoBootstrapService`, `DemoBootstrapInitializer`를 추가해 local profile 시작 시 `worldmap_admin(ADMIN)`, `orbit_runner(USER)`, 샘플 완료 run 5개, 진행 중 guest 세션 1개를 자동 생성하도록 연결했다
- local demo bootstrap은 `country seed -> admin bootstrap -> demo bootstrap` 순서를 `@Order`로 고정해, DB를 비운 뒤 서버를 다시 띄워도 같은 확인용 상태를 재생성하게 했다
- 저장소 루트의 gitignored `.env.local`에 local bootstrap 기본값을 같이 두고, source 후 `bootRun` 하면 같은 확인용 계정 / 샘플 데이터를 바로 불러오게 정리했다
- 홈 첫 화면은 guest면 `로그인 / 회원가입`, 로그인 상태면 `My Page / 로그아웃`을 바로 보여 주도록 바꿔 계정 연결 진입점을 홈에서도 명확하게 만들었다
- 홈 첫 화면은 hero에서 개별 게임 CTA를 반복하지 않고, 실제 모드 선택은 `지금 플레이할 모드` 카드 영역 한 곳에서만 하도록 다시 정리했다
- hero는 서비스 소개, 계정 연결, 공개 `Stats` 진입만 맡고, 각 모드별 직접 이동은 본문 카드와 `My Page`/`Stats` 흐름으로만 연결해 첫 진입 구조를 단순화했다
- 최근 디자인 패스 이후 public 화면 테스트를 새 카피와 레이아웃 기준으로 다시 맞추고, 홈 / 추천 / 랭킹 / Stats / My Page의 공통 shell 안정화 여부를 먼저 확인하는 작은 안정화 조각을 별도로 두었다
- `DemoBootstrapIntegrationTest`, `StatsPageControllerTest`로 local dummy data bootstrap과 public stats 렌더링을 고정했다
- 게임 세션 API와 `play/result` 페이지는 현재 브라우저의 access context로 ownership을 다시 검사한다. 로그인 사용자는 `memberId`, 비회원은 `guestSessionKey`가 맞아야 세션 조회 / 답안 제출 / 재시작 / 결과 확인이 가능하다
- 게임 결과는 terminal resource로 다시 정의했다. `READY`나 `IN_PROGRESS` 상태에서는 `/result` API와 결과 페이지를 404로 막아, 플레이 중간에 정답과 시도 기록을 먼저 읽는 치팅 경로를 닫았다
- 회원가입 / 로그인 성공 시 `MemberSessionManager.signIn()`이 `changeSessionId()`를 호출해 기존 단순 세션 로그인 구조에서도 session fixation을 줄이도록 정리했다

이후 고도화 아이디어:

- `/mypage` 고도화 여부 결정
  - 연속 최고 기록
  - 모드별 누적 플레이 시간
  - 실패 run 포함 정확도
  - 시즌/기간 필터
- restart 후 늦게 도착한 오래된 answer packet까지 완전히 막기 위한 `run generation token` 또는 restart nonce 설계
- prod/local profile, startup initializer, readiness 기준을 운영 안전성 관점에서 재점검
- admin 운영 도구 확장
  - build 상태
  - 랭킹 캐시 점검
  - 추천 버전 롤아웃 메모

반드시 이해할 것:

- 게스트 세션과 회원 계정을 어떻게 연결할지
- 인증이 도메인 로직과 어디서 만나는지
- 왜 이메일 없는 단순 계정으로도 현재 서비스 목적을 충족할 수 있는지
- 로그인 전 guest 기록과 로그인 후 member 기록이 어떻게 달라지는지
- 왜 guest 기록 귀속 시 `playerNickname`은 바꾸지 않고 ownership만 바꾸는지
- 왜 `/mypage` 첫 구현은 raw 세션 테이블보다 `leaderboard_record`에서 시작하는지
- 왜 admin 권한 체크는 컨트롤러가 아니라 인터셉터에 두는 것이 더 맞는지
- 왜 `/mypage` 세부 성향 지표는 다시 raw stage 집계로 내려가야 하는지
- 왜 운영용 admin 계정을 signup 화면이 아니라 startup bootstrap으로 여는 것이 현재 프로젝트 범위에 더 맞는지
- 왜 bootstrap 계정 생성에서도 회원가입과 같은 credential policy를 재사용해야 하는지
- 왜 public에 `/admin` 링크를 노출하지 않고, `ADMIN` 로그인 상태에서만 `Dashboard` 버튼을 보여주는 것이 더 자연스러운지
- 왜 `/admin`을 바로 삭제하지 않고 legacy redirect를 한 번 두는 것이 현재 운영 안정성에 유리한지
- 왜 Dashboard 지표를 한 테이블에서 억지로 읽지 않고, member / game session / leaderboard_record read model을 나눠 쓰는 것이 더 설명 가능한지
- 왜 공개 `/stats`와 내부 `/dashboard`를 같은 화면으로 합치지 않고 분리하는 것이 더 맞는지
- 왜 local demo 계정 / 샘플 run 생성은 signup이나 SQL seed보다 startup runner + service 조합으로 두는 것이 현재 구조에 더 맞는지
- 왜 demo bootstrap은 country seed 이후에만 돌도록 순서를 고정해야 하는지
- 왜 홈 첫 화면에서 guest와 member의 계정 CTA를 다르게 보여 주는 것이 `My Page` 하나만 남기는 것보다 더 명확한지
- 왜 홈 hero에서 개별 모드 CTA를 반복하지 않고, 모드 선택을 카드 영역 한 곳으로 모으는 것이 더 자연스러운지
- 왜 홈 hero는 서비스 소개와 계정/Stats 진입만 맡고, 실제 모드 선택은 본문으로 내리는 편이 구조적으로 더 깔끔한지
- 왜 큰 디자인 패스 직후에는 기능 추가보다 먼저 컨트롤러/SSR 테스트를 새 카피 기준으로 다시 고정해야 하는지
- 왜 게임 세션 접근 권한 검증은 컨트롤러 복붙이 아니라 서비스 진입 시점의 공통 access context 비교로 두는 편이 더 설명 가능한지
- 왜 게임 결과는 “언제든 읽을 수 있는 세션 상태”가 아니라 “종료 후에만 생기는 결과 리소스”로 보는 편이 치팅 방지와 API 의미 모두에 더 맞는지
- 왜 단순 세션 로그인 구조를 유지하더라도 로그인/회원가입 성공 순간에 세션 ID를 회전시켜야 하는지

면접 포인트:

- 인증 추가 전후로 데이터 모델이 어떻게 바뀌는가
- 왜 비회원 플레이를 버리지 않고 유지했는가
- 왜 내 기록 허브 첫 버전은 `leaderboard_record` 기반으로 만드는 것이 설명과 구현 모두에 유리한가
- 왜 admin 접근 제어를 Spring Security 전체 도입 대신 기존 세션 구조 위에 작게 얹었는가
- 왜 `/mypage`는 `run 요약`과 `play style 요약`을 서로 다른 read model에서 읽는가
- 왜 admin 계정은 공개 회원가입이 아니라 환경변수 bootstrap 방식으로 운영하는가
- 왜 운영 화면 URL을 `/admin`보다 `/dashboard`로 바꾸었는가
- 왜 `오늘 활성 수`는 session 시작 기준, `오늘 완료 수`는 leaderboard record 기준으로 분리했는가
- 왜 일반 사용자에게는 `/dashboard` 대신 공개 `/stats`를 별도로 열어 두었는가
- DB가 비어도 local demo 계정과 샘플 run을 어떻게 다시 재현하는가
- 왜 홈 첫 화면에서 정보 카드, 모드 목록, 직접 CTA를 중복 노출하지 않고 한 번씩만 보여 주는 편이 더 나은가
- 왜 guest 플레이를 허용하는 구조에서도 현재 브라우저 ownership과 맞는 세션만 읽게 해야 하는가
- 왜 `/result`를 `IN_PROGRESS` 때도 열어 두면 치팅 경로가 되는가
- 왜 Spring Security 전체 도입 전에도 `changeSessionId()`와 ownership guard만으로 먼저 막아야 할 보안 구멍이 있는가

완료 기준:

- 사용자 단위의 기록 조회가 가능하다.
- 인증 흐름을 설명할 수 있다.
- admin 화면이 public과 분리된 권한으로 보호된다.
- 운영자가 실제로 로그인 가능한 admin 계정을 배포 설정으로 만들 수 있다.
- 일반 사용자도 공개 가능한 서비스 활동 수치를 `/stats`에서 볼 수 있다.

### 9. 고도화 실험 롤백과 실시간성 개선

상태: Done

목표:

- public 제품 범위를 다시 단순하게 정리한다.
- 실험으로 열어 둔 고도화 기능을 정리하고, 실시간성 개선의 다음 기준을 다시 고정한다.

구현 항목:

- public 게임 모드 범위 재정리
- legacy 실험 데이터 정리
- 공개 read model 단순화
- SSE 또는 WebSocket 기반 실시간 랭킹 반영

현재까지 완료된 항목:

- 위치/인구수 게임 모두 public 시작 흐름을 다시 Level 1 전용으로 고정
- 공개 `/ranking`에서 게임 레벨 필터와 모든 Level 2 보드를 제거
- `/stats`, `/mypage`에서 위치/인구수 Level 2 하이라이트를 제거
- `GameLevelRollbackInitializer`를 추가해 앱 시작 시 기존 `LEVEL_2` 위치/인구수 세션, Stage, Attempt, `leaderboard_record`, Redis `l2` 키를 정리
- `GameLevelRollbackInitializerIntegrationTest`로 기존 DB/Redis에 남은 Level 2 흔적이 실제로 제거되는지 통합 테스트로 고정
- public 템플릿, JS, current-state 문서, local demo 문서가 모두 Level 1-only 기준으로 다시 동기화
- 남아 있던 internal Level 2 enum, 점수 정책, read model, public ranking level 축까지 모두 제거
- startup rollback initializer는 이제 legacy `game_level` 컬럼이 실제로 남아 있는 DB에서만 purge를 수행하도록 완화
- 실시간 전달 방식은 현재 제품 범위에서 `15초 polling 유지`로 결정하고, SSE/WebSocket은 다음 확장 후보로만 남김
- `docs/REALTIME_DELIVERY_DECISION.md`에 polling 유지 이유와 재검토 기준 정리

반드시 이해할 것:

- 왜 public 제품에서 기능을 제거할 때 UI만 숨기면 안 되고 기존 데이터와 Redis key까지 같이 정리해야 하는가
- 왜 current JPA 스키마에서 `game_level`을 제거한 뒤에도 startup rollback initializer는 남겨 두는가
- SSE와 WebSocket 중 무엇을 선택했는가
- 왜 `/ranking`, `/stats`, `/mypage`, 시작 화면을 같이 롤백해야 “현재 서비스 범위”를 일관되게 설명할 수 있는가
- 왜 startup initializer 순서가 `legacy schema 완화 -> Level 2 purge -> demo bootstrap` 이어야 하는가

면접 포인트:

- 왜 Level 2 실험을 product scope에서 제거했는가
- 기존 DB와 Redis에 남은 실험 데이터를 어떻게 안전하게 정리했는가
- 왜 public surface rollback과 startup data purge를 함께 했는가

완료 기준:

- public 게임 흐름이 Level 1-only로 다시 단순해졌다.
- 기존 DB와 Redis에 남아 있던 Level 2 데이터가 startup에서 정리된다.
- 실시간 전달 방식의 선택 이유를 설명할 수 있다.

### 10. 포트폴리오 정리와 발표 준비

상태: Done

목표:

- 코드를 만드는 것에서 끝내지 않고, 보여줄 수 있는 자료로 정리한다.

구현 항목:

- 아키텍처 다이어그램 작성
- ERD 정리
- 주요 요청 흐름 시퀀스 정리
- 대표 화면 캡처
- README 보강
- 면접 예상 질문 정리

현재까지 완료된 항목:

- [docs/ARCHITECTURE_OVERVIEW.md](/Users/alex/project/worldmap/docs/ARCHITECTURE_OVERVIEW.md)로 시스템 구성과 책임 경계 정리
- [docs/ERD.md](/Users/alex/project/worldmap/docs/ERD.md)로 핵심 테이블과 관계 고정
- [docs/REQUEST_FLOW_GUIDE.md](/Users/alex/project/worldmap/docs/REQUEST_FLOW_GUIDE.md)로 대표 요청 흐름 3개를 시퀀스로 정리
- [docs/PRESENTATION_PREP.md](/Users/alex/project/worldmap/docs/PRESENTATION_PREP.md)로 3분 소개, 10분 기술 설명, 예상 질문 정리
- [docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md)로 계정 생성, 인프라 선택, 시나리오별 비용, 수동 배포, CI/CD, 운영, 롤백까지 초보자 기준 런북 정리
- 런북 2차 보정으로 `Java 25 배포 이미지 결정`, `forwarded headers`, `JVM 메모리 옵션`, `graceful shutdown`, `Secrets Manager/SSM`, `public IPv4 비용`, `ElastiCache TLS`, `ECR lifecycle policy`를 첫 배포 판단 항목으로 보강
- [Dockerfile](/Users/alex/project/worldmap/Dockerfile)과 [.dockerignore](/Users/alex/project/worldmap/.dockerignore)를 추가해 Java 25 기준 multi-stage 컨테이너 빌드를 실제로 검증
- [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)을 추가해 prod datasource / redis / demo bootstrap off / forwarded header 기준이 어디서 분리되는지 고정
- [Dockerfile](/Users/alex/project/worldmap/Dockerfile)의 JVM 옵션을 `JAVA_RUNTIME_OPTS`로 override 가능하게 바꾸고, [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)에 graceful shutdown 기준을 추가해 ALB 뒤 종료 동작을 코드로 고정
- `spring-boot-starter-actuator`를 추가하고 [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)에 health/liveness/readiness probe group을 구성해 ECS/ALB health check 기준을 실제 endpoint로 열었다
- 운영 안정화 1차로 base [application.yml](/Users/alex/project/worldmap/src/main/resources/application.yml)에서 `local` 기본 프로필을 제거하고, prod [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml)은 `ddl-auto=validate`, readiness `db+redis+ping` 기준으로 다시 고정했다
- 같은 조각에서 [GameLevelRollbackInitializer.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)는 `worldmap.legacy.rollback.enabled=true`일 때만 켜지게 바꿨다. local/test는 true, prod는 false로 나눠, legacy rollback 호환 코드가 운영 startup에서 실제 데이터를 건드리지 않게 했다
- [task-definition.prod.sample.json](/Users/alex/project/worldmap/deploy/ecs/task-definition.prod.sample.json)을 추가해 어떤 값은 `environment`, 어떤 값은 `secrets`로 ECS에 넣는지 샘플을 저장소에 고정했다
- [RedisSessionProdConfiguration.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)로 `prod` 프로필에서만 Redis-backed session을 켜고, [application-local.yml](/Users/alex/project/worldmap/src/main/resources/application-local.yml) / [application-test.yml](/Users/alex/project/worldmap/src/main/resources/application-test.yml)에서는 session auto-configuration을 제외해 기존 servlet session 흐름을 보존했다
- [deploy-prod-ecs.yml](/Users/alex/project/worldmap/.github/workflows/deploy-prod-ecs.yml)로 `workflow_dispatch -> OIDC AWS 인증 -> ./gradlew test -> ECR push -> rendered task definition deploy` 순서를 저장소에 고정했다
- [render_ecs_task_definition.py](/Users/alex/project/worldmap/scripts/render_ecs_task_definition.py)로 sample task definition을 실제 AWS 리소스 값과 image URI로 치환하는 렌더링 규칙을 추가했다
- README에 실시간 전달 결정과 발표용 문서 세트 링크 반영

다음에 이어서 할 일:

- 실제 AWS 계정에서 `workflow_dispatch`에 필요한 repository variables와 OIDC role을 연결한다
- ECS 수동 배포 1회와 GitHub Actions 배포 1회를 모두 성공시켜 smoke test를 남긴다
- 실제 ECS 환경에서 Redis-backed session이 task 재기동 이후에도 유지되는지 smoke test를 한다
- migration 도구(Flyway/Liquibase) 도입 여부와 첫 baseline schema 전략을 결정해 `ddl-auto=validate` 이후 운영 schema 변경 경로를 명시한다

반드시 이해할 것:

- 이 프로젝트의 핵심 기술 포인트 3개
- 내가 직접 설명 가능한 범위와 아직 약한 범위

면접 포인트:

- 가장 어려웠던 설계 선택과 그 이유
- 트러블슈팅 사례
- 테스트로 막은 버그 사례

완료 기준:

- 3분 소개가 가능하다.
- 10분 기술 설명이 가능하다.
- 질문이 들어오면 구현 근거를 파일 단위로 짚어 설명할 수 있다.

### 11. 신규 게임 확장

상태: In Progress

목표:

- 현재 `country` 데이터와 게임 공통 구조를 재사용해 새 게임 모드를 확장한다.
- 무작정 많이 추가하지 않고, 설명 가능한 순서로 연다.

우선순위 메모:

- 원래는 10단계 발표 자료 정리를 먼저 더 밀 수 있었다.
- 하지만 사용자가 실제 제품 확장을 먼저 보고 싶다고 방향을 바꿨기 때문에, 이번에는 10단계보다 11단계를 먼저 연다.

구현 항목:

- 수도 맞히기 게임
- 인구 비교 퀵 배틀
- 국기 보고 나라 맞히기

현재까지 완료된 항목:

- [docs/NEW_GAME_EXPANSION_PLAN.md](/Users/alex/project/worldmap/docs/NEW_GAME_EXPANSION_PLAN.md)로 세 게임의 우선순위와 이유를 고정
- `수도 맞히기 -> 인구 비교 퀵 배틀 -> 국기 맞히기` 순서로 가는 이유를 `기존 데이터 재사용 / 에셋 필요도 / 설명 가능성` 기준으로 정리
- 첫 구현 조각을 `수도 맞히기 Level 1 vertical slice`로 확정
- `capital` game mode와 `capital_game_session / stage / attempt` 저장 구조를 추가해 위치/인구수 게임과 같은 endless run 패턴을 재사용
- `POST /api/games/capital/sessions -> GET /state -> POST /answer -> POST /restart -> GET /result` 흐름을 구현
- same-continent 우선 distractor 생성과 글로벌 fallback 규칙으로 수도 보기 4개를 서버가 생성
- `scripts/sync_capital_city_kr.py`로 `capitalCityKr`를 seed에 재생성 가능하게 추가하고, 수도 게임 옵션/정답/결과를 한국어 수도명 기준으로 전환
- 수도 게임 run을 `leaderboard_record`에 반영하고, 공개 `/ranking`, `/stats`, 홈 모드 카드까지 연결
- `CapitalGameFlowIntegrationTest`, `LeaderboardIntegrationTest`, `StatsPageControllerTest`, `HomeControllerTest`로 첫 vertical slice를 고정
- `population-battle` game mode와 `population_battle_game_session / stage / attempt` 저장 구조를 추가해 기존 endless run 패턴을 네 번째 게임에도 재사용
- `POST /api/games/population-battle/sessions -> GET /state -> POST /answer -> POST /restart -> GET /result` 흐름을 구현
- 인구 rank gap 기반 pair 선택과 left/right 랜덤 배치 규칙으로 2-choice 아케이드 배틀을 서버가 생성
- 인구 비교 퀵 배틀 run을 `leaderboard_record`에 반영하고, 공개 `/ranking`, `/stats`, 홈 모드 카드까지 연결
- `PopulationBattleGameFlowIntegrationTest`, `LeaderboardIntegrationTest`, `StatsPageControllerTest`, `HomeControllerTest`로 두 번째 vertical slice를 고정
- `FLAG_GAME_ASSET_PIPELINE_PLAN.md`로 국기 게임의 source / manifest / 정적 파일 경로 / local 재현 원칙을 먼저 고정
- 국기 자산은 DB 컬럼보다 `static/images/flags + flag-assets.json`을 source of truth로 두기로 결정
- 출제 가능 국가를 `country seed ∩ manifest ∩ 실제 파일 존재` 교집합으로 설명하는 기준을 세움
- `FlagAssetCatalog`를 추가해 manifest를 서버가 읽고, ISO3 / 경로 / format / 실제 파일 존재를 startup 기준으로 검증
- flagcdn snapshot SVG 36개와 `FlagAssetCatalogTest`로 국기 자산 파이프라인을 다시 고정
- `scripts/fetch_flag_assets.py`로 선택된 ISO3 목록의 국기 SVG와 manifest를 재생성 가능하게 정리
- `FlagQuestionCountryPoolService`를 추가해 `country seed ∩ manifest ∩ 실제 파일 존재` 교집합을 실제 서버 read model로 계산
- 출제 가능 국기 국가 목록에 `countryId / ISO3 / 한글명 / 영문명 / 대륙 / 정적 flag 경로`를 묶어, 다음 `flag` game mode skeleton이 바로 재사용할 수 있게 함
- `FlagQuestionCountryPoolServiceIntegrationTest`로 출제 가능 국가 36개, 대륙별 분포(EUROPE 15 / ASIA 8 / NORTH_AMERICA 3 / SOUTH_AMERICA 4 / AFRICA 4 / OCEANIA 2), ISO3 lookup을 고정
- `flag` game mode와 `flag_game_session / stage / attempt` 저장 구조를 추가해 기존 endless run 패턴을 다섯 번째 게임에도 재사용
- `POST /api/games/flag/sessions -> GET /state -> POST /answer -> POST /restart -> GET /result` 흐름을 구현
- `FlagQuestionCountryPoolService`를 문제 source of truth로 사용하고, 같은 대륙 우선 distractor + global fallback으로 나라 보기 4개를 생성
- 국기 게임 run을 `leaderboard_record`에 반영하고, 공개 `/ranking`, `/stats`, 홈 모드 카드까지 연결
- `FlagGameFlowIntegrationTest`, `LeaderboardIntegrationTest`, `StatsPageControllerTest`, `HomeControllerTest`로 세 번째 신규 게임 vertical slice를 고정
- 세 신규 게임(수도 / 인구 비교 퀵 배틀 / 국기)이 모두 public start/state/answer/result, 랭킹, 공개 stats까지 연결된 상태로 닫힘
- `DemoBootstrapService`에 flag sample run을 추가해 local `/stats`, `/ranking`에서 flag 보드가 서버 재기동 직후 바로 보이게 함
- `DemoBootstrapIntegrationTest`로 admin/user 계정 + location/population/flag sample run + recommendation feedback sample bootstrap을 같이 고정
- `DemoBootstrapService`에 capital / population-battle sample run도 추가해 local `/stats`, `/ranking`에서 신규 게임 5종 보드가 서버 재기동 직후 바로 보이게 함
- `DemoBootstrapIntegrationTest`로 admin/user 계정 + location/population/capital/flag/population-battle sample run + recommendation feedback sample bootstrap을 같이 고정
- `GameLevelRollbackInitializer`가 legacy local DB의 `leaderboard_record`에 남아 있을 수 있는 `game_level NOT NULL` 제약과 예전 `game_mode` check constraint를 함께 정리해, 현재 `LeaderboardRecord` insert와 demo bootstrap이 예전 스키마에서도 그대로 동작하게 함
- `FlagGameOptionGenerator`를 same-continent random에서 `same continent -> 인접 대륙 -> 전체 pool` 순 fallback으로 바꿔, 북미/오세아니아처럼 후보가 적은 문제도 더 설명 가능하게 정리
- `FlagGameOptionGeneratorTest`로 `EUROPE same-continent 유지`, `OCEANIA -> ASIA fallback`, `NORTH_AMERICA -> SOUTH_AMERICA fallback`을 고정
- `FlagGameDifficultyPolicy`를 플레이어 관점의 `기본 / 확장 / 전체 라운드`로 다시 정의하고, `FlagGameService`가 초반 라운드에서는 same-continent distractor가 충분한 대륙만 우선 출제하도록 정리
- `FlagGameDifficultyPolicyTest`, `FlagGameFlowIntegrationTest`로 `difficultyGuide`, 초반 라운드 target continent 안정화, 결과 화면의 구간 표시를 고정
- 신규 게임 3종이 public 제품에 모두 붙은 뒤, 홈은 `아케이드 러너 / 퀵 퀴즈와 추천` 두 구역으로 모드 카드를 재그룹핑해 첫 진입 판단 비용을 줄임
- 공개 `/ranking`은 다섯 게임 필터를 `위치 / 수도 / 국기 / 배틀 / 인구` 짧은 버튼으로 바꾸고, 정렬 규칙은 그대로 서버에 둔 채 전환 밀도만 줄임
- 공개 `/stats`는 서비스 전체 지표와 게임별 완료 수를 분리하고, Top 보드를 `아케이드 상위 기록 / 퀵 퀴즈 상위 기록` 두 묶음으로 재정리해 다섯 게임 확장 이후의 읽기 부담을 낮춤
- `HomeControllerTest`, `StatsPageControllerTest`, `LeaderboardIntegrationTest`로 새 grouped public surface를 고정
- 위치/인구수/수도/인구 비교/국기 게임은 정답 시 `획득 점수`만 잠깐 보여 준 뒤 자동으로 다음 Stage를 다시 로드하도록 공통 UX를 맞춤
- 수도/인구수/인구 비교/국기 플레이 화면에서 `다음 Stage` 수동 버튼을 제거하고, 결과 페이지 중심이 아니라 플레이 연속성 중심으로 루프를 정리
- 위치/인구수/수도/인구 비교/국기 게임은 오답 오버레이와 입력 잠금 해제도 약 `950ms` 리듬으로 맞춰, 정답/오답 템포가 게임마다 흔들리지 않게 정리
- 공통 shell에 `focus-visible` 링을 추가하고, 위치/인구수/수도/국기/인구 비교 퀵 배틀의 게임오버 모달은 `aria-describedby + tabindex + inert + focus trap`으로 실제 focus scope를 갖게 정리해 키보드 접근성을 한 번 더 보강
- 국기 플레이/결과 화면의 카드 프레임이 정의되지 않은 CSS 토큰 때문에 무테두리처럼 깨지지 않도록, `flag-display-card`와 `flag-display-image`에 공통 surface/border fallback token을 적용
- `build.gradle`에 `browserSmokeTest` verification task를 추가하고, `BrowserSmokeE2ETest`로 `home` SSR shell, `capital start -> play`, `recommendation survey -> result`를 실제 headless Chromium에서 검증하는 Playwright 브라우저 스모크 레일을 붙였다
- 기본 `test` task는 `browser-smoke` tag를 제외해 빠른 피드백을 유지하고, 브라우저 레일만 별도 실행하게 분리했다
- `application-browser-smoke.yml`로 browser smoke 전용 profile을 추가해 `worldmap.legacy.rollback.enabled=false`를 강제하고, Redis는 의도적으로 빈 `127.0.0.1:6390`으로 돌려 local Redis 6379에 기대지 않는 현재 smoke 범위를 확인하게 했다
- `BrowserSmokeProfileConfigTest`로 이 profile의 rollback off / Redis override를 고정했다
- `LeaderboardService` read path가 Redis `DataAccessException`을 `cache miss`처럼 처리하고 DB top run으로 fallback하도록 보강해, browser smoke가 `/ranking`, `/stats` 같은 public leaderboard read model까지 local Redis 없이 검증할 수 있게 했다
- `RedisUnavailableLeaderboardFallbackIntegrationTest`와 `BrowserSmokeE2ETest`로 `/api/rankings/*`, `/ranking`, `/stats`가 실제 Redis unavailable 조건에서도 계속 뜨는지 고정했다
- `BrowserSmokeE2ETest`에 capital 대표 게임의 game-over modal keyboard flow를 추가해, 실제 Chromium에서 `Tab / Shift+Tab / Escape / restart 후 focus return`이 유지되는지 고정했다
- 이 E2E는 브라우저가 세션을 만든 뒤 서버 도메인 API로 lives를 1개 남은 상태까지 먼저 줄이고, 마지막 오답 1회만 브라우저로 제출해 modal focus 규칙 자체를 안정적으로 검증하도록 정리했다
- 같은 방식으로 `population` 대표 게임에도 game-over modal keyboard E2E를 추가해, endless 4-choice quiz shell에서도 `Tab / Shift+Tab / Escape / restart 후 focus return`이 실제 Chromium에서 유지되는지 고정했다
- 같은 방식으로 `population-battle` 대표 게임에도 game-over modal keyboard E2E를 추가해, 2-choice 게임에서도 `Tab / Shift+Tab / Escape / restart 후 focus return`이 실제 Chromium에서 유지되는지 고정했다
- `.github/workflows/verify.yml`을 추가해 GitHub Actions에서 `test -> browser-smoke` 두 verification job을 분리해 돌리도록 했다. `test` job은 Redis service를 명시적으로 띄우고, `browser-smoke` job은 Playwright Chromium 설치 뒤 `./gradlew browserSmokeTest`를 실행한다
- 이 과정에서 `RedisSessionConfigurationIntegrationTest`가 prod session config만 보도록 schema 생성 override를 추가했고, 여러 게임 flow test의 game-over/restart 기대값도 현재 하트 규칙에 맞게 정리해 CI green baseline을 다시 맞췄다

다음 후속 개선 후보:

- 국기 게임 세부 난이도(동일 대륙 고정 비율, 자산 36개 이후 확장 전략)를 더 넓힐지 결정
- 신규 게임 3종이 모두 열린 상태에서 홈/랭킹/Stats 문구를 더 줄일지, 아니면 현재 그룹 구조로 유지할지 한 번 더 확인
- 같은 modal keyboard E2E를 location / flag까지 더 넓힐지 결정
- 반복된 game-over modal focus 로직을 공용 helper로 올릴지, 지금처럼 게임별 script 안에 유지할지 결정
- verify workflow를 PR required check로 걸지, 일단 optional verification으로 둘지 결정

반드시 이해할 것:

- 왜 수도 맞히기를 첫 확장 대상으로 잡는가
- 왜 수도 보기 생성에서 같은 대륙 우선 fallback 정책을 썼는가
- 왜 `capitalCity`를 덮어쓰지 않고 `capitalCityKr`를 별도 필드로 두는가
- 왜 한국어 수도명은 runtime 번역이 아니라 seed 재생성 스크립트로 고정하는가
- 왜 수도 게임도 위치/인구수와 같은 세션 / Stage / Attempt 구조를 유지하는가
- 왜 인구 비교 퀵 배틀은 인구수 게임과 비슷하지만 여전히 별도 모드 가치가 있는가
- 왜 인구 비교 퀵 배틀은 4지선다보다 2-choice endless 배틀이 더 설명하기 쉬운가
- 왜 population quiz의 정답 구간 비교를 그대로 재사용하지 않고, rank gap pair 생성 규칙을 별도 정책으로 분리했는가
- 왜 국기 게임은 규칙보다 에셋 파이프라인이 먼저인가
- 왜 국기 자산은 1차에서 DB 컬럼보다 정적 파일 + manifest 구조가 더 설명 가능하고 재현성이 좋은가
- 왜 `FlagAssetCatalog`가 단순 유틸이 아니라 startup validation 역할을 같이 맡게 했는가
- 왜 `FlagQuestionCountryPoolService`가 단순 util이 아니라 seed와 정적 자산의 교집합을 계산하는 read model이어야 하는가
- 왜 `flag` game mode도 별도 추상화 없이 session / Stage / Attempt 구조를 그대로 재사용했는가
- 왜 국기 게임은 sample 자산으로 먼저 vertical slice를 열고, 이후 자산 pool을 점진적으로 넓히는 편이 설명 가능성이 높은가
- 왜 새로운 게임도 서버 주도 세션 / Stage / Attempt 구조를 유지해야 하는가

면접 포인트:

- 왜 지금 이 시점에 새 게임을 확장하는가
- 왜 3개를 한 번에 만들지 않고 순차적으로 여는가
- 현재 `country` 데이터가 어떤 새 게임을 바로 지원할 수 있는가

완료 기준:

- 최소 1개 새 게임이 start/state/answer/result까지 동작한다.
- 수도 맞히기 Level 1 vertical slice가 start/state/answer/result, 랭킹, 공개 stats까지 동작한다.
- 인구 비교 퀵 배틀 Level 1 vertical slice가 start/state/answer/result, 랭킹, 공개 stats까지 동작한다.
- 국기 보고 나라 맞히기 Level 1 vertical slice가 start/state/answer/result, 랭킹, 공개 stats까지 동작한다.
- 랭킹과 문서까지 현재 범위에 맞게 연결된다.
- 세 신규 게임의 구현 순서와 자산 제약을 설명할 수 있다.

## 단계 이동 규칙

다음 단계로 넘어가기 전에 아래 항목이 비어 있으면 안 된다.

- 핵심 기능 설명
- 핵심 클래스 설명
- 핵심 테스트 설명
- 실패 케이스 설명
- 왜 지금 이 순서로 개발하는지에 대한 설명

## 기능 완료 후 반드시 남길 내용

모든 기능 작업이 끝나면 [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)에 아래 내용을 남긴다.

- 작업 이름
- 변경 파일
- 요청 흐름
- 도메인 모델 변화
- 테스트
- 배운 점
- 아직 약한 부분
- 면접용 30초 요약
