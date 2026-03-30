# WorldMap Portfolio Project

## 1. 프로젝트 한 줄 소개

하나의 웹사이트에서 사용자가 게임 모드를 선택해 국가 위치 찾기, 수도 맞히기, 국가 인구수 맞추기, 인구 비교 퀵 배틀, 국기 보고 나라 맞히기, AI 기반 나라 추천, 실시간 랭킹을 이용하는
`백엔드 중심 게임 플랫폼`을 만든다.

핵심은 화면이 아니라 `Spring Boot 서버가 문제 생성, 정답 판정, 점수 계산, 시도 횟수, 게임 진행 상태를 주도적으로 관리`한다는 점이다.

## 2. 이 프로젝트를 포트폴리오로 가져갈 때의 포지셔닝

이 서비스는 단순 CRUD 포트폴리오보다 아래 강점을 보여줄 수 있다.

- 게임 세션과 라운드 상태를 관리하는 서버 중심 도메인 설계
- 문제 생성, 정답 판정, 점수 계산 같은 비즈니스 로직 구현
- Redis 기반 실시간 랭킹과 캐시 전략
- SSR(Thymeleaf) 기반 페이지 렌더링과 API 혼합 구조
- 런타임 AI 호출 없이도 추천과 게임 핵심 로직을 설명할 수 있는 설계

즉, "지도 데이터를 다루는 웹게임"이 아니라 `상태 관리가 있는 백엔드 서비스`로 보여주는 것이 중요하다.

## 3. 서비스 컨셉

### 메인 컨셉

사용자는 메인 페이지에서 원하는 모드를 선택한다.

1. 국가 위치 찾기 게임
2. 수도 맞히기 게임
3. 국가 인구수 맞추기 게임
4. 인구 비교 퀵 배틀
5. 국기 보고 나라 맞히기
6. 어울리는 나라 추천
7. 실시간 랭킹 확인

### 핵심 UX 흐름

1. 사용자가 모드를 선택한다.
2. 서버가 문제 또는 설문을 생성한다.
3. 사용자가 답변한다.
4. 서버가 정답 여부, 점수, 진행 상태를 계산한다.
5. 게임 종료 후 결과와 랭킹 반영 결과를 보여준다.

## 4. MVP 범위

처음에는 `게임 2개 + 랭킹 + 추천 1개`를 안정적으로 만드는 것이 맞았고, 현재는 수도 맞히기, 인구 비교 퀵 배틀, 국기 보고 나라 맞히기까지 포함한 `기본 게임 5개 + 랭킹 + 추천 1개` 범위로 확장했다.

### MVP 1차

- 국가 위치 찾기 게임 Level 1
- 국가 인구수 맞추기 게임 Level 1
- Redis 기반 실시간 랭킹
- 나라 추천 설문 + 추천 결과 페이지
- 게스트 닉네임 기반 플레이

### MVP 2차

- 닉네임 기반 회원가입 / 로그인
- 내 전적 조회
- 일간 / 전체 랭킹 분리
- 공개 `/stats`와 운영 `/dashboard`

회원 기능은 커뮤니티가 아니라 `기록 유지`가 목적이다.

- 비회원은 지금처럼 세션 기반으로 바로 플레이
- 로그인하면 내 계정에 기록과 랭킹 이력이 누적
- 계정 정보는 `닉네임 + 비밀번호` 수준으로 단순하게 유지
- 게임 세션 조회 / 답안 제출 / 재시작 / 결과 확인은 현재 브라우저 세션의 ownership(`memberId` 또는 `guestSessionKey`)과 맞는 경우에만 허용
- 게임 결과는 `GAME_OVER` 또는 `FINISHED`가 된 뒤에만 열리는 terminal resource로 취급
- 현재는 9단계 rollback 기준으로 `member`, `guestSessionKey`, 게임 세션 / 랭킹 레코드 ownership 필드, 닉네임 + 비밀번호 기반 회원가입 / 로그인 / 로그아웃, 로그인 직후 현재 브라우저의 guest 기록 귀속, `/mypage` 기록 허브, raw stage 기반 플레이 성향 요약, `/dashboard/**` 접근 제어, 환경변수 기반 bootstrap admin provisioning, Dashboard 운영 수치 카드, 공개 `/stats` 화면, local demo 계정 / 샘플 run bootstrap, 현재 survey/engine 버전 추천 피드백 샘플 bootstrap, 홈 첫 화면 계정 진입 CTA, 수도 맞히기 Level 1 vertical slice, 인구 비교 퀵 배틀 Level 1 vertical slice, 국기 보고 나라 맞히기 Level 1 vertical slice와 공개 `/ranking` / `/stats` / 홈 연동까지 연결했다. 위치/인구수 Level 2 실험은 현재 product scope에서 제거했고, internal Level 2 enum/정책/read model도 함께 걷어냈다. startup `GameLevelRollbackInitializer`는 legacy DB에 `game_level` 컬럼이 실제로 남아 있을 때만 예전 `LEVEL_2` 세션 / Stage / Attempt / 랭킹 row와 Redis `l2` 키를 정리한다.

### 이후 확장

- 국기 게임 난이도 세분화와 결과 카피 polish
- 업적 시스템
- 문제 풀이 히스토리
- 관리자용 국가 데이터 관리 화면
- admin 권한 제어와 운영 화면 확장

## 5. 게임 규칙 설계

기획 초기에 가장 중요한 것은 `문제를 어떻게 내고, 무엇을 정답으로 판정할지`를 명확히 하는 것이다.

### 5.1 국가 위치 찾기 게임

#### Level 1

- 서버가 국가 1개를 출제한다.
- 프론트는 3D 지구본에서 국가 폴리곤 클릭 입력을 받는다.
- 플레이 중 지구본 위 국가 이름은 노출하지 않는다.
- 클릭 후 선택 상태는 지구본 위 하이라이트로만 보여주고, 하단에는 제출 버튼만 유지한다.
- 다른 국가를 다시 클릭하면 현재 선택이 바로 교체된다.
- 선택한 국가 이름은 제출 전에는 노출하지 않고, 판정 단계 이후에만 공개한다.
- 서버는 `현재 Stage`, `하트 3개`, `점수`, `시도 횟수`를 관리한다.
- 오답이면 하트가 1개 줄고 같은 Stage를 다시 시도한다.
- 하트가 모두 사라지면 `GAME OVER`로 종료하고, 현재 세션을 같은 `sessionId`로 다시 시작하거나 홈으로 복귀할 수 있다.
- 정답이면 점수를 부여하고 다음 Stage로 자동 진행한다.
- 정답/오답 피드백은 약 1초만 노출하고, 이후 다음 Stage 또는 같은 Stage 재시도 상태로 자동 정리한다.
- 현재 Level 1은 고정 5스테이지가 아니라 `하트가 남아 있는 동안 계속 진행되는 endless run`이다.
- 현재 Level 1은 성능과 클릭 안정성을 위해 인구 기준 상위 72개 주요 국가로 먼저 운영한다.
- 현재 Level 1의 지구본 자산은 `Natural Earth 110m` 기반으로 다시 생성해 초기 로딩과 폴리곤 흔들림을 줄였다.
- 현재 Level 1의 국가 선택은 `Globe.gl polygon click`에만 의존하지 않고, 지구본 클릭 좌표를 기준으로 GeoJSON 내부 포함 판정을 한 번 더 수행한다.
- 현재 public 서비스는 위치 게임 Level 1만 운영한다.
- 이전 거리/방향 힌트 실험으로 남아 있던 legacy `LEVEL_2` 세션 / 시도 / 랭킹 레코드와 Redis `l2` 키는 startup `GameLevelRollbackInitializer`가 정리한다.
- 이후 고도화 후보는 `소국/영토`, `타이머`, `streak` 같은 난도 실험을 다시 설계하는 일이다.

#### 서버 책임

- 출제 국가 선택
- Stage 진행 관리
- 목숨(하트) 감소 처리
- 시도 이력 저장
- 선택 국가 코드 검증
- 정답 국가 코드 비교
- 라운드별 점수 계산
- 총점과 게임오버 상태 관리

### 5.2 국가 인구수 맞추기 게임

#### Level 1

- 서버가 국가를 1개 제시한다.
- 사용자는 보기 4개 중 가장 맞는 인구 규모를 선택한다.
- 서버는 `현재 Stage`, `하트 3개`, `점수`, `시도 횟수`를 관리한다.
- 오답이면 하트가 1개 줄고 같은 Stage를 다시 시도한다.
- 하트가 모두 사라지면 `GAME OVER`로 종료하고, 현재 세션을 같은 `sessionId`로 다시 시작하거나 홈으로 복귀할 수 있다.
- 정답이면 점수를 부여하고 다음 Stage로 자동 진행한다.
- 정답/오답 피드백은 약 1초만 노출하고, 이후 다음 Stage 또는 같은 Stage 재시도 상태로 자동 정리한다.
- 현재 구현은 `인구 규모 구간 4개 보기형` 위에서 아케이드 루프를 붙인 상태다.
- 남은 고도화 방향은 `구간 경계 / 난이도 세분화`다.
- 예: `1천만 미만`, `1천만~5천만`, `5천만~1억`, `1억 이상`
- 현재 public 서비스는 인구수 게임 Level 1만 운영한다.
- 이전 직접 입력형 실험으로 남아 있던 legacy `LEVEL_2` 세션 / 시도 / 랭킹 레코드와 Redis `l2` 키도 startup `GameLevelRollbackInitializer`가 정리한다.

#### 서버 책임

- 국가별 인구 데이터 제공
- 보기 생성과 정답 구간 비교
- 난이도별 점수 차등

### 5.3 수도 맞히기 게임

#### Level 1

- 서버가 국가 1개를 출제한다.
- 사용자는 수도 보기 4개 중 정답 1개를 고른다.
- 서버는 `현재 Stage`, `하트 3개`, `점수`, `시도 횟수`를 관리한다.
- 오답이면 하트가 1개 줄고 같은 Stage를 다시 시도한다.
- 하트가 모두 사라지면 `GAME OVER`로 종료하고, 현재 세션을 같은 `sessionId`로 다시 시작하거나 홈으로 복귀할 수 있다.
- 정답이면 점수를 부여하고 다음 Stage로 자동 진행한다.
- 정답/오답 피드백은 약 1초만 노출하고, 이후 다음 Stage 또는 같은 Stage 재시도 상태로 자동 정리한다.
- 보기는 같은 대륙 수도를 먼저 우선해 만들고, 부족하면 전체 국가 수도에서 보충한다.
- 수도 표기는 `country.capitalCityKr`를 사용해 한국어 수도명 4개 보기로 노출한다.
- `capitalCityKr`는 seed 갱신 스크립트가 Wikidata 한국어 label을 기본으로 채우고, 일부 국어사전식 표기와 legacy seed 예외는 manual override로 보정한다.
- 현재 public 서비스는 수도 맞히기 Level 1만 운영한다.

#### 서버 책임

- 출제 국가 선택
- 수도 보기 4개 생성
- 같은 대륙 우선 distractor 생성과 글로벌 fallback
- 정답 판정
- Stage / 하트 / 점수 / 다음 Stage 관리

### 5.4 인구 비교 퀵 배틀

#### Level 1

- 서버가 인구 비교용 국가 2개를 제시한다.
- 사용자는 두 나라 중 인구가 더 많은 나라를 고른다.
- 서버는 `현재 Stage`, `하트 3개`, `점수`, `시도 횟수`를 관리한다.
- 오답이면 하트가 1개 줄고 같은 Stage를 다시 시도한다.
- 하트가 모두 사라지면 `GAME OVER`로 종료하고, 현재 세션을 같은 `sessionId`로 다시 시작하거나 홈으로 복귀할 수 있다.
- 정답이면 점수를 부여하고 다음 Stage로 자동 진행한다.
- 정답/오답 피드백은 약 1초만 노출하고, 이후 다음 Stage 또는 같은 Stage 재시도 상태로 자동 정리한다.
- Stage가 올라갈수록 비교 쌍이 `큰 격차 -> 근접 비교 -> 글로벌 고난도` 순으로 어려워진다.
- 현재 public 서비스는 인구 비교 퀵 배틀 Level 1만 운영한다.

#### 서버 책임

- 비교할 국가 쌍 선택
- left / right 보기 배치 랜덤화
- 정답 판정
- Stage / 하트 / 점수 / 다음 Stage 관리
- 난이도별 rank gap 규칙 적용

### 5.5 국기 보고 나라 맞히기

#### Level 1

- 서버가 출제 가능한 국기 국가 pool에서 문제 국가 1개를 고른다.
- 사용자는 국기 이미지 1개를 보고 나라 보기 4개 중 정답 1개를 고른다.
- 서버는 `현재 Stage`, `하트 3개`, `점수`, `시도 횟수`를 관리한다.
- 오답이면 하트가 1개 줄고 같은 Stage를 다시 시도한다.
- 하트가 모두 사라지면 `GAME OVER`로 종료하고, 현재 세션을 같은 `sessionId`로 다시 시작하거나 홈으로 복귀할 수 있다.
- 정답이면 점수를 부여하고 다음 Stage로 자동 진행한다.
- 정답/오답 피드백은 약 1초만 노출하고, 이후 다음 Stage 또는 같은 Stage 재시도 상태로 자동 정리한다.
- 출제 가능 국가는 `country seed ∩ flag manifest ∩ 실제 파일 존재` 교집합만 사용한다.
- 현재 public 서비스는 flagcdn snapshot 기반 SVG 36개를 기준으로 국기 게임 Level 1을 운영한다.
- distractor는 같은 대륙 국가를 먼저 우선하고, 부족하면 인접 대륙 순서로 보충한 뒤 마지막에만 전체 출제 가능 pool으로 fallback한다.
- 난이도는 `기본 라운드 -> 확장 라운드 -> 전체 라운드` 3단계로 진행되고, 초반 라운드는 같은 대륙 distractor가 충분한 대륙에서만 먼저 출제한다.

#### 서버 책임

- 출제 가능한 국기 국가 pool 계산
- 정적 국기 경로 검증과 Stage 문제 생성
- 나라 보기 4개 생성
- 정답 판정
- Stage / 하트 / 점수 / 다음 Stage 관리

## 6. AI 추천 기능 설계

이 기능은 `런타임 LLM 호출 없이도 추천 품질을 개선할 수 있는 구조`가 중요하다.
그래서 추천 결과 계산은 서버가 맡고, AI는 서비스 런타임이 아니라 `개발 단계의 설문 개선 루프`에만 사용한다.

### 추천 방식

1. 사용자가 설문에 답한다.
2. 서버가 설문 점수를 기반으로 국가 후보를 계산한다.
3. 상위 3개 국가를 추린다.
4. 결과 페이지에서 만족도 피드백을 수집한다.
5. 개발 단계에서 AI/서브 에이전트가 설문과 평가 시나리오를 개선한다.

### 설문 예시

- 선호 기후: 더운 / 온화한 / 추운
- 생활 속도: 빠름 / 보통 / 여유로움
- 물가 허용 범위: 낮음 / 중간 / 높음
- 자연 vs 도시 선호
- 영어 친화도 중요 여부
- 치안 / 복지 / 음식 / 문화다양성 선호

### 추천 기능에서 서버가 주도해야 할 것

- 설문 문항 관리
- 문항별 가중치 계산
- 국가 메타데이터 기반 후보 산출
- 설문 / 엔진 버전 관리
- 만족도 피드백 수집과 집계

### 개발 단계에서 AI가 맡는 역할

- 설문 문항 후보 초안 생성
- 모호한 문항이나 중복 선택지 비판
- 페르소나 시나리오 세트 생성
- 설문 버전과 엔진 버전 비교 평가 보조

즉, `추천 결과의 근거와 계산은 서버 데이터`, `AI는 오프라인 설문 개선 도구`로 분리한다.

### 현재 1차 구현 상태

- `/recommendation/survey`에서 SSR 설문 20문항을 제공한다.
- 서버는 `RecommendationQuestionCatalog`로 문항과 선택지를 관리한다.
- 제출된 답변은 `RecommendationSurveyForm -> RecommendationSurveyAnswers` 구조로 검증하고 변환한다.
- `RecommendationSurveyService`가 30개 국가 프로필 카탈로그와 비교해 가중치 점수를 계산하고 상위 3개 국가를 반환한다.
- 현재 설문은 단순 선호보다 `따뜻함/시원함`, `사계절 변화`, `기후 적응 성향`, `집 크기 vs 중심 접근성`, `초기 적응 친화도`, `디지털 생활 편의`, `문화·여가`, `장기 기반`까지 함께 묻는 20문항 trade-off 구조로 다시 설계했다.
- 추천 후보 풀은 북미, 유럽, 동아시아, 동남아, 중동, 남미, 아프리카, 오세아니아까지 분산해 한 지역에만 결과가 몰리지 않도록 넓혔다.
- 현재 점수식은 `정확 일치 보너스`, `비용 선호별 초과 물가 패널티`, `영어 지원 필요도 가중치`, `극단 기후 mismatch penalty`, `핵심 생활 조건 coherence bonus`를 포함하도록 다시 조정했다.
- 정렬은 총점 우선이지만, 동점 구간에서는 `강한 신호 개수 -> 정확 일치 개수 -> 국가명` 순으로 보조 비교한다.
- 결과 페이지는 서버가 계산한 매칭 점수와 핵심 이유 3개를 deterministic하게 보여준다.
- 추천 결과 자체는 저장하지 않고, 결과 페이지에서 `1~5점 만족도 + surveyVersion + engineVersion + 사용자가 선택한 20개 답변`만 익명 피드백으로 수집한다.
- `/dashboard/recommendation/feedback`와 `/api/recommendation/feedback/summary`에서 `surveyVersion + engineVersion` 기준 평균 점수, 응답 수, 1~5점 분포를 읽어 설문 개선 기준으로 사용한다.
- `/dashboard/recommendation/feedback`는 현재 버전 만족도와 baseline anchor drift를 함께 읽어, 지금은 `피드백을 더 모을지`, `문구를 손볼지`, `rank drift를 줄일지`를 운영 메모로 바로 보여 준다.
- `/dashboard/recommendation/persona-baseline`에서 18개 페르소나 baseline을 현재 엔진 결과로 다시 계산해 weak scenario, 1위 anchor drift, active-signal 비교 시나리오를 운영 화면으로 확인한다.
- 오프라인 baseline과 snapshot은 현재 `survey-v4 / engine-v20` 기준으로 다시 고정했다.
- `engine-v7`에서는 `MIXED + BALANCED` 생활을 원하면서 `안전 / 공공서비스`를 중시하는 경우, `safety + welfare + housing + newcomer`를 함께 보는 작은 보정을 추가해 `P04`, `P06` 같은 균형형 시나리오의 남유럽 쏠림을 한 단계 더 줄였다.
- `engine-v8`에서는 `VALUE_FIRST + SAFETY + MIXED + BALANCED + 영어 적응 MEDIUM` 조합에서만 작동하는 `soft landing bonus`를 추가해, `P06`의 3위 후보를 `이탈리아`에서 `포르투갈`로 교체했다.
- `engine-v9`에서는 `QUALITY_FIRST + SAFETY HIGH + English HIGH + MIXED + BALANCED + LOW tolerance` 조합에서만 작동하는 `family base bonus`를 추가해, `P11`에 `캐나다`를 다시 올리고 baseline을 `18 / 18`까지 끌어올렸다.
- `engine-v10`에서는 `WARM + FAST + CITY + QUALITY_FIRST + English/Digital/Culture HIGH` 조합에만 작동하는 `global hub bonus`를 추가해, `P01`, `P05`에서 `싱가포르`가 `아랍에미리트`보다 먼저 오도록 좁게 보정했다.
- `engine-v11`에서는 `WARM + BALANCED + VALUE_FIRST + MIXED + English MEDIUM + FOOD HIGH + DIVERSITY MEDIUM+` 조합에만 작동하는 `foodie starter bonus`를 추가해, `P02`에서 `말레이시아`가 `태국`보다 먼저 오도록 좁게 보정했다.
- `engine-v12`에서는 `MILD + BALANCED + MIXED + English MEDIUM + HIGH publicService` 조합에만 작동하는 `temperate public base bonus`를 추가해, `P04`에서 `우루과이`가 `스페인`보다 먼저 오도록 좁게 보정했다.
- `engine-v13`에서는 `MILD + VALUE_FIRST + SAFETY HIGH + publicService MEDIUM + newcomer LOW` 조합에만 작동하는 `practical public value bonus`를 추가해, `P06`에서 `우루과이`가 `스페인`보다 먼저 오도록 좁게 보정했다.
- `engine-v14`에서는 `WARM + QUALITY_FIRST + CITY + English HIGH + publicService HIGH` 조합에만 작동하는 `premium warm hub bonus`를 추가해, `P09`에서 `아랍에미리트`가 `싱가포르`보다 먼저 오도록 좁게 보정했다.
- `engine-v15`에서는 `COLD + RELAXED + NATURE + English MEDIUM + safety HIGH` 조합에만 작동하는 `soft nature base bonus`를 추가해, `P08`에서 `뉴질랜드`가 `핀란드`보다 먼저 오도록 좁게 보정했다.
- `engine-v16`에서는 `MILD + FAST + CITY + BALANCED + English LOW + diversity HIGH` 조합에만 작동하는 `cosmopolitan pulse bonus`를 추가해, `P10`에서 `미국`이 `대한민국`보다 먼저 오도록 좁게 보정했다.
- `engine-v17`에서는 `MILD + FAST + CITY + QUALITY_FIRST + English HIGH + diversity HIGH` 조합에만 작동하는 `temperate global city bonus`를 추가해, `P13`에서 `미국`이 `싱가포르`보다 먼저 오도록 좁게 보정했다.
- `engine-v18`에서는 `WARM + VALUE_FIRST + MIXED + English MEDIUM + publicService HIGH` 조합에만 작동하는 `accessible warm value hub bonus`를 추가해, `P14`에서 `말레이시아`가 `스페인`보다 먼저 오도록 좁게 보정했다.
- `engine-v19`에서는 `MILD + QUALITY_FIRST + MIXED + English HIGH + safety HIGH` 조합에만 작동하는 `temperate family bridge bonus`를 추가해, `P11`에서 `캐나다`가 `아일랜드`보다 먼저 오도록 좁게 보정했다.
- `engine-v20`에서는 `MILD + BALANCED + RELAXED + NATURE + VALUE_FIRST + TRANSIT_FIRST + EXPERIENCE` 조합에만 작동하는 `exploratory nature runway bonus`를 추가해, `P15`에서 `뉴질랜드`가 `포르투갈`보다 먼저 오도록 좁게 보정했다.
- `P07` baseline은 현재 20문항 의미에 맞춰 anchor를 `일본`에서 `싱가포르`로 재정의했다. `WARM + FAST + CITY + FOOD` 조합은 현재 설문에서는 warm megacity 축에 더 가깝고, 이를 엔진 보정보다 baseline 기대값에서 바로잡는 편이 더 설명 가능했다.
- 현재는 weak scenario가 `0개`까지 줄었기 때문에, 다음 추천 품질 판단은 `기대 후보가 top 3에 들어오는가`뿐 아니라 `기대 1위 anchor가 실제 top 1인지`까지 함께 본다.
- 현재 dynamic baseline은 `18 / 18`을 유지하면서 anchor drift를 `0개`까지 줄인 상태이다.
- 공통 shell은 다크/라이트 테마 토글을 제공하고, 사용자가 고른 테마는 `localStorage`의 `worldmap-theme`로 유지한다.
- 홈, 추천, 랭킹 public 화면은 내부 구현 용어보다 플레이어가 바로 이해할 수 있는 제품 언어로 다시 정리했고, 버전/집계/로드맵 같은 내부 정보는 `/dashboard` 운영 화면으로 분리하는 방향으로 간다.
- 공통 shell과 홈, 추천, 랭킹, Stats, My Page는 최근 디자인 패스에서 다크/라이트 공통 톤과 각진 패널 레이아웃 기준으로 다시 정리했고, public 화면 테스트도 새 카피 기준으로 다시 고정했다.
- 신규 게임 3종이 들어온 뒤 public 정보 밀도가 다시 높아져, 홈은 `아케이드 러너 / 퀵 퀴즈와 추천` 두 구역으로 모드 카드를 재그룹핑했다. 위치 찾기, 인구 비교 퀵 배틀, 인구수 맞추기는 빠른 반복 플레이 축으로, 수도 맞히기, 국기 퀴즈, 나라 추천은 짧은 퀴즈/탐색 축으로 묶어 첫 진입 판단 비용을 낮췄다.
- 공개 `/ranking`은 다섯 게임을 긴 라벨 대신 `위치 / 수도 / 국기 / 배틀 / 인구` 짧은 버튼으로 전환하게 바꿨다. 정렬과 집계 규칙은 그대로 서버가 맡고, public 표면에서는 필터 탐색 비용만 줄이는 방향으로 다듬었다.
- 공개 `/stats`는 서비스 전체 지표와 게임별 완료 수를 분리하고, Top 보드도 `아케이드 상위 기록 / 퀵 퀴즈 상위 기록` 두 묶음으로 다시 정리했다. 즉, 운영 숫자를 더 늘리지 않고 “지금 서비스가 어떤 종류의 플레이를 담고 있는가”를 먼저 읽히게 하는 편을 택했다.
- 모든 public 게임은 정답을 맞히면 `획득 점수`만 짧게 보여 준 뒤 자동으로 다음 Stage로 넘어간다. 즉시 피드백과 결과 화면에서 `내 선택`과 `정답`을 과하게 반복하지 않고, 반복 플레이 흐름이 끊기지 않는 쪽을 우선했다.
- 현재는 `/dashboard` read-only 운영 화면과 `/dashboard/recommendation/feedback` 운영 화면을 두고, `ADMIN` role 세션 기반 접근 제어까지 연결했다.
- public 헤더는 기본 `Home`, `My Page`를 유지하고, `ADMIN` 로그인일 때만 `Dashboard` 버튼을 추가로 노출한다.
- Dashboard 첫 화면은 `총 회원 수`, `오늘 활성 회원 수`, `오늘 활성 게스트 수`, `오늘 시작된 세션 수`, `오늘 완료된 게임 수`, `오늘 모드별 완료 수`를 바로 보여준다.
- 이 지표는 모두 같은 테이블에서 읽지 않는다. 회원 수는 `member_account`, 오늘 활성은 각 게임 세션의 `startedAt`, 오늘 완료 수는 `leaderboard_record.finishedAt`를 source of truth로 사용한다.
- local demo bootstrap은 현재 `survey-v4 / engine-v20` 기준 추천 만족도 샘플도 함께 만들어, fresh local 환경에서도 `/dashboard/recommendation/feedback`이 `현재 엔진 유지` 메모까지 보여 주도록 한다.
- public 헤더는 게임별 직접 이동을 제거하고 `Home`, `My Page`만 남겨 이동 구조를 단순화했다.
- `/mypage`는 비회원에게는 로그인 유도 화면, 로그인 사용자에게는 계정별 기록 허브로 동작한다.
- 8단계 계정 구조는 `게스트 세션 유지 + 로그인 시 현재 브라우저 세션 기록 귀속`을 기본 원칙으로 설계한다.
- 8단계 1차 구현으로 `member` 엔티티, `GuestSessionKeyManager`, 게임 세션 / 랭킹 레코드의 `memberId`, `guestSessionKey` 기반 ownership 저장 구조를 먼저 추가했다.
- 8단계 2차 구현으로 `/signup`, `/login`, `/logout`과 BCrypt 기반 비밀번호 해시, 단순 세션 로그인, 로그인 사용자의 새 게임 시작 시 `memberId` ownership 저장을 추가했다.
- 8단계 3차 구현으로 회원가입 / 로그인 직후 현재 브라우저의 `guestSessionKey` 기록을 계정 ownership으로 귀속하는 서비스를 추가했다.
- 8단계 보안 보강으로 게임 `play / state / answer / restart / result` 경로는 현재 `memberId` 또는 같은 브라우저의 `guestSessionKey` ownership이 맞는 세션만 열리게 바꿨다.
- 결과는 terminal resource로 취급해 `READY`, `IN_PROGRESS` 상태에서는 `/result` API와 결과 페이지가 404를 돌리도록 닫았다. 즉 진행 중 세션의 정답과 시도 이력은 더 이상 먼저 노출되지 않는다.
- 로그인 / 회원가입 성공 시 `MemberSessionManager`가 `changeSessionId()`를 호출해 session fixation 위험을 줄였다.
- 8단계 4차 구현으로 `/mypage`가 `leaderboard_record`를 읽어 총 완료 플레이 수, 모드별 최고 점수, 최고 랭킹, 최근 플레이 10개를 보여주는 실제 기록 대시보드로 바뀌었다.
- `/mypage`는 원본 게임 세션 전체가 아니라 완료된 run이 이미 정규화된 `leaderboard_record`를 먼저 읽는다. 그래서 모드별 최고 기록, 최근 완료 이력, 당시 랭킹 위치를 한 번에 설명하기 쉽다.
- 8단계 5차 구현으로 운영 라우트는 `AdminAccessInterceptor`가 보호하고, 비로그인 사용자는 `/login?returnTo=...`로 보내며, 로그인한 일반 사용자는 `403`으로 막는다.
- 운영 화면 접근 제어는 컨트롤러마다 복붙하지 않고 인터셉터로 묶었다. dashboard 진입 정책은 도메인 상태 변경보다 라우트 입구의 공통 규칙에 가깝기 때문이다.
- 8단계 6차 구현으로 `/mypage`는 finished session에 속한 stage를 다시 읽어 모드별 `클리어 Stage 수`, `1트 클리어율`, `평균 시도 수`까지 보여주기 시작했다.
- `/mypage`는 이제 `leaderboard_record` 기반 완료 run 요약과, raw stage 기반 플레이 성향 요약을 함께 가진다. 즉, “무슨 결과를 냈는가”와 “어떤 방식으로 플레이하는가”를 분리해서 보여준다.
- 8단계 7차 구현으로 `AdminBootstrapProperties`, `AdminBootstrapService`, `AdminBootstrapInitializer`를 추가해 서버 시작 시 `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED`, `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME`, `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD` 기준으로 운영용 admin 계정을 자동 생성하거나 기존 계정을 `ADMIN`으로 승격하게 했다.
- bootstrap admin 생성은 signup UI가 아니라 startup runner에서 처리한다. 운영자 계정은 공개 회원가입 흐름이 아니라 배포 환경 설정으로 여는 편이 더 단순하고, 일반 사용자에게 admin 경로를 노출하지 않아도 되기 때문이다.
- 8단계 8차 구현으로 운영 화면 진입 주소를 `/admin`에서 `/dashboard`로 바꿨다. `ADMIN` 로그인 사용자에게만 public 헤더에 `Dashboard` 버튼을 노출하고, 기존 `/admin/**`는 임시 redirect로만 남겨 북마크 호환성을 유지한다.
- 8단계 9차 구현으로 `/dashboard` 첫 화면에 운영 수치 카드를 추가했다. `총 회원 수`는 `member_account`, `오늘 활성 회원 / 게스트`와 `오늘 시작된 세션 수`는 각 게임 세션의 `startedAt`, `오늘 완료된 게임 수`와 `모드별 완료 수`는 `leaderboard_record.finishedAt` 기준으로 계산한다.
- 8단계 10차 구현으로 dashboard 활동 지표를 `ServiceActivityService`로 분리해 `/dashboard`와 공개 `/stats`가 같은 read model을 재사용하게 했다. `Stats`는 전체 사용자에게 공개 가능한 운영 수치와 일간 Top 3만 보여 주고, 추천 품질/버전 정보는 계속 Dashboard에만 남긴다.
- 8단계 10차 구현으로 local profile 시작 시 admin 계정 `worldmap_admin`, 일반 계정 `orbit_runner`, 샘플 leaderboard run 5개, 진행 중 guest 세션 1개를 자동 생성하는 demo bootstrap을 추가했다. DB 데이터를 비워도 local profile로 서버를 다시 띄우면 country seed -> admin bootstrap -> demo bootstrap 순서로 같은 확인용 상태를 다시 만들 수 있다.
- local boot 호환성 조각으로 `GameLevelRollbackInitializer`가 legacy `leaderboard_record`에 남아 있을 수 있는 `game_level NOT NULL` 제약과 예전 `game_mode` check constraint를 함께 정리하게 바꿨다. 그래서 예전 DB 스키마를 가진 local 환경에서도 현재 `LeaderboardRecord` 엔티티와 demo bootstrap insert가 그대로 동작한다.
- 9단계 rollback 구현으로 위치/인구수 Level 2 실험은 product scope에서 제거했고, internal Level 2 호환 코드도 정리됐다. `GameLevelRollbackInitializer`는 앱 시작 시 legacy `game_level` 컬럼이 남아 있는 DB에서만 기존 `LEVEL_2` 세션 / 시도 / 랭킹 row와 Redis `l2` 키를 정리한다. 그래서 public `/ranking`, `/stats`, `/mypage`, 게임 시작 화면은 다시 Level 1-only 기준으로 단순해졌다.
- 8단계 11차 구현으로 홈 첫 화면에서 guest는 `로그인 / 회원가입`, 로그인 사용자는 `My Page / 로그아웃`을 바로 볼 수 있게 정리했다. 계정 기능은 그대로 두고, 홈에서 기록 유지 진입점을 더 짧게 만든 조각이다.
- 8단계 12차 구현으로 홈 첫 화면은 hero에서 개별 게임 CTA를 반복하지 않고, 실제 모드 선택은 `지금 플레이할 모드` 카드 영역 한 곳에서만 하도록 다시 정리했다. hero는 서비스 소개, 계정 연결, 공개 `Stats` 진입만 맡고, 모드 설명 중복을 줄여 첫 진입 구조를 단순화했다.
- 8단계 13차 구현으로 게임 세션 API와 `play/result` 페이지는 현재 브라우저가 소유한 세션만 열 수 있게 바꿨다. 세션 조회 / 답안 제출 / 재시작 / 결과 확인은 `memberId` 또는 `guestSessionKey` ownership을 현재 요청의 access context와 비교해 통과시킨다.
- 같은 조각에서 결과 화면은 종료된 run에만 존재하는 리소스로 재정의했다. 그래서 `READY`나 `IN_PROGRESS` 상태에서는 `/result` API와 페이지가 404를 반환하고, 플레이 중간 치팅으로 정답을 미리 읽을 수 없게 했다.
- 회원가입과 로그인 성공 시에는 `changeSessionId()`로 세션 ID를 회전시켜 기존 단순 세션 구조 안에서도 session fixation을 줄였다.
- 현재 8단계 핵심 범위는 닫혔고, 이후 고도화 포인트는 `/mypage` 기간별 누적 통계, season/기간 필터, 더 세밀한 운영 도구 확장이다.
- 이 피드백은 설문 문항과 가중치를 계속 개선하기 위한 신호로 사용하고, 오프라인 AI-assisted 평가 루프는 `docs/recommendation/OFFLINE_AI_SURVEY_IMPROVEMENT.md`와 `docs/recommendation/PERSONA_EVAL_SET.md`에서 관리한다.
- `RecommendationOfflinePersonaCoverageTest`로 18개 페르소나 baseline을 자동 평가하고, 현재 엔진이 최소 15개 시나리오에서 기대 후보 1개 이상을 top 3에 포함하는지를 품질 하한으로 고정했다.
- baseline은 기존 14개 중립 시나리오와, 새 두 문항을 적극적으로 쓰는 `P15~P18` 비교 시나리오로 나뉜다.
- `RecommendationOfflinePersonaSnapshotTest`로 18개 페르소나의 현재 top 3 추천 순서를 snapshot으로 고정해, 다음 실험에서 coverage뿐 아니라 순위 변화 자체도 비교할 수 있게 했다.
- baseline 시나리오 정의는 이제 test 전용 helper가 아니라 `RecommendationPersonaBaselineCatalog`로 올려, 테스트와 `/dashboard/recommendation/persona-baseline` 운영 화면이 같은 시나리오 집합을 공유한다.
- 현재 baseline 결과를 바탕으로 `docs/recommendation/SURVEY_V2_PROPOSAL.md`에 `복지형`, `저예산 안전형`, `온화한 고도시 다양성형` 시나리오를 우선 개선 대상으로 정리했다.

## 7. 랭킹 시스템 설계

랭킹은 Redis를 쓰는 이유가 분명해야 한다.

### 목표

- 점수 반영이 빠를 것
- 상위 N명 조회가 빠를 것
- 모드별 / 일간 / 전체 랭킹 분리가 가능할 것

### 권장 구조

- 영속 저장: RDB에 게임 결과 저장
- 실시간 조회: Redis Sorted Set 사용

### Redis 키 예시

- `leaderboard:all:location`
- `leaderboard:all:population`
- `leaderboard:daily:location:2026-03-22`

### 랭킹 반영 흐름

1. 게임 종료
2. 서버가 최종 점수를 확정
3. RDB에 결과 저장
4. Redis 랭킹 갱신
5. 랭킹 페이지 또는 실시간 이벤트로 반영

### 현재 1차 구현 상태

- 각 게임 종료 run을 `leaderboard_record`로 별도 저장한다.
- 같은 `sessionId`를 재시작해도 이전 run이 덮어써지지 않도록, 랭킹은 세션 자체가 아니라 `종료된 run 결과`를 기준으로 저장한다.
- RDB 저장이 끝난 뒤 `after commit` 시점에 Redis Sorted Set의 전체 / 일간 키에 반영한다.
- 조회는 `Redis 상위 record id 조회 -> RDB 상세 조회` 순서로 처리한다.
- Redis 키가 비어 있으면 RDB를 source of truth로 사용해 상위 기록을 다시 읽고, Redis 키를 재구성한다.
- `/ranking` 화면은 15초 간격 짧은 주기 polling으로 자동 갱신되고, 사용자가 수동 새로고침도 할 수 있다.
- `/ranking` 화면은 `위치/수도/인구수`, `전체/일간` 필터를 프론트에서 전환하며 같은 랭킹 API를 재사용한다.
- 동점은 총점, 클리어 수, 총 시도 수를 합친 내부 `rankingScore`를 먼저 비교하고, 그 값까지 같으면 더 빨리 종료된 run이 앞선다.
- 현재 MVP의 실시간 전달 방식은 15초 polling으로 닫고, SSE/WebSocket은 다음 확장 후보로만 남겨뒀다.
- 현재 제공 경로:
  - `/api/rankings/location`
  - `/api/rankings/capital`
  - `/api/rankings/population`
  - `/ranking`

### 실시간 전달 방식

- 현재 결정: 짧은 주기 polling 유지
- 재검토 후보: SSE 또는 WebSocket

선택 이유와 재검토 기준은 [docs/REALTIME_DELIVERY_DECISION.md](/Users/alex/project/worldmap/docs/REALTIME_DELIVERY_DECISION.md)에 정리했다.

## 8. 권장 아키텍처

### 프론트

- Thymeleaf SSR
- 바닐라 JavaScript
- 3D 지구본 표현: `Three.js + Globe.gl`

현재 위치 찾기 Level 1은 정적 GeoJSON과 지구 텍스처를 사용해 `Globe.gl`로 렌더링한다.
전체 국가 시드는 `World Bank API + REST Countries` 기준 독립국 194개를 유지한다.
다만 위치 게임 Level 1의 `active-countries.geojson`은 현재 성능과 상호작용 안정화를 위해 인구 기준 상위 72개 국가만 사용한다.
Level 1 활성 지오데이터는 `Natural Earth 110m` 기반으로 따로 유지하고, 선택 판정은 지구본 클릭 좌표를 GeoJSON과 다시 비교하는 방식으로 안정성을 보강했다.
정답 판정은 프론트가 아니라 서버에서 국가 ISO 코드 비교로 처리한다.
현재 프로토타입은 동작하지만, 실제 목표는 `하트 기반 아케이드 게임 루프 + 우주 HUD 디자인`이다.

### 백엔드

- Spring Boot 3.5.x
- Spring MVC
- Thymeleaf
- Spring Data JPA
- Validation
- Spring Security(2차)
- Redis

### 저장소

- RDB: 사용자, 국가, 게임 세션, 라운드 결과, 추천 기록
- Redis: 실시간 랭킹, 캐시, 필요 시 활성 세션 보조 저장

### 현재 구현 상태

- Spring Boot 3.5.12 + Java 25 + Gradle wrapper
- Thymeleaf SSR 메인 페이지와 기본 에러 페이지
- `application.yml`, `application-local.yml`, `application-test.yml` 프로파일 분리
- Docker Compose 기반 PostgreSQL / Redis 개발 환경 뼈대
- 공통 API 예외 응답 구조
- `country` 엔티티, 시드 검증기, 시작 시 ISO3 기준 동기화 로직
- `scripts/generate_country_assets.py`로 재생성 가능한 국가 데이터 파이프라인 추가
- World Bank API + REST Countries 기준 2024 인구수와 대표 좌표를 담은 독립국 시드 194건
- 국가 목록 / 상세 조회 API (`/api/countries`, `/api/countries/{iso3Code}`)
- 위치 찾기 Level 1 `세션 / Stage / Attempt` 저장 구조
- 위치 찾기 하트 3개, 같은 Stage 재시도, 게임오버, 자동 다음 Stage 흐름
- 위치 찾기 endless Stage 생성과 단계별 난이도 확장 정책
- 위치 찾기 Stage별 점수 계산 로직과 Attempt 기록 저장
- 위치 찾기용 `active-countries.geojson`, 지구 텍스처 정적 에셋 추가
- 위치 찾기 Level 1 활성 폴리곤 자산을 Natural Earth 110m 기반으로 재구성
- 위치 찾기 SSR 시작/플레이/결과 페이지와 3D 지구본 국가 선택 셸
- 위치 게임 플레이 중 tooltip 제거, 제출 전 국가명 비노출, 지구본 하이라이트 기반 선택, 오버레이 피드백, 자동 전환 1차 구현
- 드래그와 클릭을 구분하는 선택 안정화 로직, 전역 홈 이동 헤더 추가
- 홈 / 시작 / 결과 화면과 공통 CSS의 `cold space HUD` 테마 1차 적용
- 위치 게임 아케이드 리부트 설계 문서 작성
- `game/common`의 공통 세션 구조 재사용
- 인구수 맞추기 Level 1 `세션 / Stage / Attempt` 저장 구조 1차 리부트
- 인구수 맞추기 하트 3개, 같은 Stage 재시도, 게임오버, 같은 세션 재시작 흐름
- 인구수 맞추기 endless Stage 생성과 단계별 난이도 확장 정책
- 인구수 맞추기 Stage별 점수 계산 로직과 Attempt 기록 저장
- 인구수 맞추기 Level 1 구간형 보기 생성과 라벨 기반 결과 피드백
- 인구수 맞추기 SSR 시작/플레이/결과 페이지의 아케이드 HUD 1차 전환
- 인구수 게임 아케이드 리부트 설계 문서와 1차 구현 반영
- 수도 맞히기 Level 1 `세션 / Stage / Attempt` 저장 구조 추가
- 수도 맞히기 Level 1 하트 3개, 같은 Stage 재시도, 게임오버, 같은 세션 재시작 흐름
- 수도 맞히기 Level 1 same-continent 우선 보기 생성과 글로벌 fallback 정책
- 수도 맞히기 seed에 `capitalCityKr`를 추가하고, 수도 게임 옵션/정답/결과를 한국어 수도명 기준으로 전환
- 수도 맞히기 SSR 시작/플레이/결과 페이지와 answer/state/result API 추가
- 수도 맞히기 run의 랭킹 반영과 공개 `/ranking`, `/stats`, 홈 모드 카드 연동
- 인구 비교 퀵 배틀 Level 1 `세션 / Stage / Attempt` 저장 구조 추가
- 인구 비교 퀵 배틀의 2-choice endless run, 하트 3개, 같은 Stage 재시도, 게임오버, 같은 세션 재시작 흐름
- 인구 rank gap 기반 pair 생성과 left/right 랜덤 배치 정책
- 인구 비교 퀵 배틀 SSR 시작/플레이/결과 페이지와 answer/state/result API 추가
- 인구 비교 퀵 배틀 run의 랭킹 반영과 공개 `/ranking`, `/stats`, 홈 모드 카드 연동
- 국기 게임 1차 준비용 `FlagAssetCatalog`, `flag-assets.json`, flagcdn snapshot SVG 36개와 manifest/file existence 검증 추가
- `scripts/fetch_flag_assets.py`로 선택된 ISO3 목록의 국기 SVG와 manifest를 재생성 가능하게 정리
- `FlagQuestionCountryPoolService`로 출제 가능 국가 pool 36개를 서버 read model로 고정
- 국기 보고 나라 맞히기 Level 1 `세션 / Stage / Attempt` 저장 구조 추가
- 국기 게임의 국기 이미지 + 나라 4지선다 endless run, 하트 3개, 같은 Stage 재시도, 게임오버, 같은 세션 재시작 흐름
- 국기 게임 SSR 시작/플레이/결과 페이지와 answer/state/result API 추가
- 국기 게임 run의 랭킹 반영과 공개 `/ranking`, `/stats`, 홈 모드 카드 연동

현재 위치 게임 리부트 기준 문서:

- `docs/LOCATION_GAME_ARCADE_REBOOT.md`

현재 인구수 게임 리부트 기준 문서:

- `docs/POPULATION_GAME_ARCADE_REBOOT.md`

## 9. 도메인 모델 초안

### 주요 엔티티

- `country`
  - 국가명, ISO 코드, 대륙, 대표 좌표, 대표 좌표 타입, 인구수, 추천 태그 정보
- `member`
  - 사용자 계정 정보
- `guest_player`
  - 게스트 닉네임 기반 플레이어
- `game_session`
  - 모드, 상태, 총점, 시작/종료 시각
- `game_round`
  - 몇 번째 문제인지, 출제 국가, 정답 데이터, 사용자 답변, 정답 여부, 획득 점수
- `location_game_stage`
  - 아케이드형 위치 게임에서 현재 단계, 정답 국가, 단계 상태, 획득 점수
- `location_game_attempt`
  - 위치 게임에서 한 단계 안의 개별 시도 기록, 오답 이력, 남은 하트
- `population_game_stage`
  - 아케이드형 인구수 게임에서 현재 단계, 보기 4개, 정답 인구수, 단계 상태, 획득 점수
- `population_game_attempt`
  - 인구수 게임에서 한 단계 안의 개별 선택 기록, 남은 하트
- `capital_game_stage`
  - 아케이드형 수도 맞히기 게임에서 현재 단계, 출제 국가, 수도 보기 4개, 단계 상태, 획득 점수
- `capital_game_attempt`
  - 수도 게임에서 한 단계 안의 개별 선택 기록, 남은 하트
- `population_battle_game_stage`
  - 인구 비교 퀵 배틀에서 비교 국가 2개, 정답 보기, 단계 상태, 획득 점수
- `population_battle_game_attempt`
  - 인구 비교 퀵 배틀에서 한 단계 안의 좌/우 선택 기록, 남은 하트
- `flag_game_stage`
  - 국기 보고 나라 맞히기에서 국기 이미지 경로, 나라 보기 4개, 단계 상태, 획득 점수
- `flag_game_attempt`
  - 국기 게임에서 한 단계 안의 개별 선택 기록, 남은 하트
- `recommendation_feedback`
  - 설문 버전, 엔진 버전, 만족도 점수, 답변 스냅샷

### 관계 예시

- 한 명의 플레이어는 여러 `game_session`을 가진다.
- 한 개의 `game_session`은 여러 `game_round`를 가진다.
- 한 개의 `location_game_session`은 여러 `location_game_stage`를 가진다.
- 한 개의 `location_game_stage`는 여러 `location_game_attempt`를 가진다.
- 한 개의 `population_game_session`은 여러 `population_game_stage`를 가진다.
- 한 개의 `population_game_stage`는 여러 `population_game_attempt`를 가진다.
- 한 개의 `capital_game_session`은 여러 `capital_game_stage`를 가진다.
- 한 개의 `capital_game_stage`는 여러 `capital_game_attempt`를 가진다.
- 한 개의 `population_battle_game_session`은 여러 `population_battle_game_stage`를 가진다.
- 한 개의 `population_battle_game_stage`는 여러 `population_battle_game_attempt`를 가진다.
- 한 개의 `flag_game_session`은 여러 `flag_game_stage`를 가진다.
- 한 개의 `flag_game_stage`는 여러 `flag_game_attempt`를 가진다.
- 한 개의 `recommendation_feedback`는 설문 / 엔진 버전별 개선 신호가 된다.

## 10. 페이지 / 화면 초안

### 필수 페이지

- `/`
  - 메인 페이지, 모드 선택
- `/games/location/start`
  - 위치 찾기 게임 시작
- `/games/location/play/{sessionId}`
  - 위치 찾기 진행 화면
- `/games/location/result/{sessionId}`
  - 위치 찾기 결과 화면
- `/games/population/start`
  - 인구수 게임 시작
- `/games/population/play/{sessionId}`
  - 인구수 진행 화면
- `/games/population/result/{sessionId}`
  - 인구수 결과 화면
- `/games/capital/start`
  - 수도 맞히기 게임 시작
- `/games/capital/play/{sessionId}`
  - 수도 맞히기 진행 화면
- `/games/capital/result/{sessionId}`
  - 수도 맞히기 결과 화면
- `/games/population-battle/start`
  - 인구 비교 퀵 배틀 시작
- `/games/population-battle/play/{sessionId}`
  - 인구 비교 퀵 배틀 진행 화면
- `/games/population-battle/result/{sessionId}`
  - 인구 비교 퀵 배틀 결과 화면
- `/games/flag/start`
  - 국기 보고 나라 맞히기 시작
- `/games/flag/play/{sessionId}`
  - 국기 보고 나라 맞히기 진행 화면
- `/games/flag/result/{sessionId}`
  - 국기 보고 나라 맞히기 결과 화면
- `/ranking`
  - 실시간 랭킹
- `/stats`
  - 공개 가능한 서비스 활동 수치와 오늘의 Top 3, 수도 퀴즈 / 인구 비교 퀵 배틀 / 국기 퀴즈 하이라이트
- `/recommendation/survey`
  - 나라 추천 설문
- `/mypage`
  - 로그인 사용자 기록 허브, 비회원 로그인 유도 화면

### 운영용 페이지

- `/dashboard`
  - `ADMIN` role 세션 전용 read-only 운영 대시보드
- `/dashboard/recommendation/feedback`
  - `ADMIN` role 세션 전용 설문 / 엔진 버전별 만족도 집계
- `/dashboard/recommendation/persona-baseline`
  - `ADMIN` role 세션 전용 18개 페르소나 baseline 운영 요약
  - weak scenario, 1위 anchor drift, active-signal 시나리오를 현재 엔진 기준으로 자동 계산

기존 `/admin/**` 경로는 임시 redirect로 유지하고, 실제 운영 진입 주소는 `/dashboard/**`를 기준으로 사용한다.

## 11. 발표용 문서 세트

현재 상태를 설명할 때 바로 참고할 문서는 아래 6개다.

- [docs/REALTIME_DELIVERY_DECISION.md](/Users/alex/project/worldmap/docs/REALTIME_DELIVERY_DECISION.md)
- [docs/ARCHITECTURE_OVERVIEW.md](/Users/alex/project/worldmap/docs/ARCHITECTURE_OVERVIEW.md)
- [docs/ERD.md](/Users/alex/project/worldmap/docs/ERD.md)
- [docs/REQUEST_FLOW_GUIDE.md](/Users/alex/project/worldmap/docs/REQUEST_FLOW_GUIDE.md)
- [docs/PRESENTATION_PREP.md](/Users/alex/project/worldmap/docs/PRESENTATION_PREP.md)
- [docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md](/Users/alex/project/worldmap/docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md)

현재 저장소 기준 배포 준비 상태는 아래까지 왔다.

- multi-stage [Dockerfile](/Users/alex/project/worldmap/Dockerfile) 추가
- [.dockerignore](/Users/alex/project/worldmap/.dockerignore) 추가
- [application-prod.yml](/Users/alex/project/worldmap/src/main/resources/application-prod.yml) 추가
- ALB 기준 `forwarded headers`와 graceful shutdown 설정 추가
- Docker runtime `JAVA_RUNTIME_OPTS`로 JVM 메모리 옵션 override 가능하게 정리
- Actuator health, liveness, readiness endpoint 추가
- ECS용 [task-definition.prod.sample.json](/Users/alex/project/worldmap/deploy/ecs/task-definition.prod.sample.json) 추가
- sample task definition을 실제 값으로 치환하는 [render_ecs_task_definition.py](/Users/alex/project/worldmap/scripts/render_ecs_task_definition.py) 추가
- 수동 실행 기준 GitHub Actions workflow [deploy-prod-ecs.yml](/Users/alex/project/worldmap/.github/workflows/deploy-prod-ecs.yml) 추가
- prod 전용 [RedisSessionProdConfiguration.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/common/config/RedisSessionProdConfiguration.java)으로 Redis-backed session과 `WMSESSION` 쿠키 기준 활성화
- `docker build`로 Java 25 기반 컨테이너 이미지 생성 확인

즉 저장소 기준으로는 `Dockerfile -> prod profile -> health probe -> secrets sample -> session externalization -> rendered task definition -> GitHub Actions deploy workflow`까지 이어진 상태다.
다음 배포 준비 코드는 ECS 수동 배포 1회와 실제 AWS 값 연결 smoke test다.

운영용 admin 계정은 공개 회원가입으로 만들지 않는다.

- `WORLDMAP_ADMIN_BOOTSTRAP_ENABLED=true`
- `WORLDMAP_ADMIN_BOOTSTRAP_NICKNAME=...`
- `WORLDMAP_ADMIN_BOOTSTRAP_PASSWORD=...`

를 설정하고 서버를 시작하면 해당 닉네임 계정을 자동 생성하거나 기존 계정을 `ADMIN`으로 승격한다.

### local 확인용 demo 계정 / 샘플 데이터

local 프로필에서는 아래 기본값으로 확인용 계정과 샘플 데이터가 자동 생성된다.

- admin 계정
  - 닉네임: `worldmap_admin`
  - 비밀번호: `secret123`
- 일반 계정
  - 닉네임: `orbit_runner`
  - 비밀번호: `secret123`
- 샘플 데이터
  - `orbit_runner` 완료 run 5개
    - 위치 찾기 420점
    - 인구수 퀴즈 390점
    - 수도 맞히기 285점
    - 국기 퀴즈 285점
    - 인구 비교 퀵 배틀 275점
  - 공개 Stats / Ranking에서 보이는 샘플 랭킹 데이터
  - `demo-guest-live` guest 진행 중 위치 게임 세션 1개

더미 데이터는 local profile 전용 `DemoBootstrapService`가 생성하며, DB를 비운 뒤 서버를 다시 시작하면 재생성된다. 자세한 재생성 절차와 환경변수 오버라이드는 [LOCAL_DEMO_BOOTSTRAP.md](/Users/alex/project/worldmap/docs/LOCAL_DEMO_BOOTSTRAP.md)에 정리했다.

저장소 루트에는 gitignore 대상인 `.env.local` 샘플도 같이 두었다. local에서 아래처럼 한 번 불러오고 서버를 실행하면 같은 계정 / 샘플 데이터를 바로 재현할 수 있다.

```bash
set -a
source .env.local
set +a
./gradlew bootRun
```

### 2차 페이지

- `/login`
- `/signup`
- `/history/games`

## 12. API / 서버 인터페이스 초안

SSR을 쓰더라도 게임 진행 중에는 비동기 API가 필요하다.

### 국가 데이터

- `GET /api/countries`
  - 출제 가능한 국가 목록 조회
- `GET /api/countries/{iso3Code}`
  - 특정 국가 상세 정보 조회

### 위치 찾기

- `POST /api/games/location/sessions`
  - 게임 세션 시작
- `GET /api/games/location/sessions/{sessionId}/state`
  - 현재 Stage, 하트, 점수, 문제 국가 조회
- `POST /api/games/location/sessions/{sessionId}/answer`
  - 선택한 국가의 `ISO3 코드` 제출
- `GET /api/games/location/sessions/{sessionId}/result`
  - 결과 요약과 Stage / Attempt 기록 조회

### 인구수 맞추기

- `POST /api/games/population/sessions`
- `GET /api/games/population/sessions/{sessionId}/state`
- `POST /api/games/population/sessions/{sessionId}/answer`
- `POST /api/games/population/sessions/{sessionId}/restart`
- `GET /api/games/population/sessions/{sessionId}`
  - 결과 요약과 Stage / Attempt 기록 조회

### 수도 맞히기

- `POST /api/games/capital/sessions`
- `GET /api/games/capital/sessions/{sessionId}/state`
- `POST /api/games/capital/sessions/{sessionId}/answer`
- `POST /api/games/capital/sessions/{sessionId}/restart`
- `GET /api/games/capital/sessions/{sessionId}`
  - 결과 요약과 Stage / Attempt 기록 조회

### 인구 비교 퀵 배틀

- `POST /api/games/population-battle/sessions`
- `GET /api/games/population-battle/sessions/{sessionId}/state`
- `POST /api/games/population-battle/sessions/{sessionId}/answer`
- `POST /api/games/population-battle/sessions/{sessionId}/restart`
- `GET /api/games/population-battle/sessions/{sessionId}`
  - 결과 요약과 Stage / Attempt 기록 조회

### 국기 보고 나라 맞히기

- `POST /api/games/flag/sessions`
- `GET /api/games/flag/sessions/{sessionId}/state`
- `POST /api/games/flag/sessions/{sessionId}/answer`
- `POST /api/games/flag/sessions/{sessionId}/restart`
- `GET /api/games/flag/sessions/{sessionId}`
  - 결과 요약과 Stage / Attempt 기록 조회

### 추천

- `GET /recommendation/survey`
- `POST /recommendation/survey`
- `POST /api/recommendation/feedback`
- `GET /api/recommendation/feedback/summary`

### 랭킹

- `GET /api/rankings/location?scope=DAILY`
- `GET /api/rankings/capital?scope=ALL`
- `GET /api/rankings/population-battle?scope=ALL`
- `GET /api/rankings/population?scope=ALL`

## 13. 구현 우선순위

### Step 1

- 기본 SSR 화면 뼈대 작성
- 패키지 구조 초안 고정
- 공통 예외 응답 구조와 프로파일 분리

### Step 2

- 국가 데이터 준비
- 인구수 시드 데이터와 국가 기준 데이터 준비
- 시드 검증과 초기 적재 구조 구현

### Step 3

- 위치 찾기 Level 1 아케이드 리부트
- 서버 주도 세션 / Stage / Attempt 설계
- 하트, 점수, 재시도, 게임오버 규칙 구현
- 3D 지구본 HUD와 선택 확인 UI 구현
- 결과 / 게임오버 화면 완성도 보강

### Step 4

- 인구수 맞추기 Level 1 구현
- 공통 게임 세션 구조 추상화
- 사이트 전반 우주 테마 디자인 통일

### Step 5

- 게임 종료 결과 저장
- Redis 랭킹 연동
- 랭킹 화면 구현

### Step 6

- 설문 기반 추천 로직 구현
- 만족도 수집과 버전 집계 연결

### Step 7

- AI-assisted 설문 개선 체계 정리
- 페르소나 시나리오 기반 오프라인 평가 루프 정리

### Step 8

- 인증 / 마이페이지 / 전적 조회
- 실시간성 고도화

## 14. 패키지 구조 제안

처음부터 기능 기준으로 패키지를 나누는 것이 좋다.

- `common`
  - 공통 예외, 공통 응답, 유틸
- `country`
  - 국가 데이터 조회, 시드 적재
- `game`
  - 게임 세션, 라운드, 점수 정책, 모드별 서비스
- `ranking`
  - Redis 랭킹 처리, 조회 API
- `recommendation`
  - 설문, 추천 계산, 만족도 피드백, 오프라인 개선 자산
- `web`
  - Thymeleaf 컨트롤러, API 컨트롤러
- `auth`
  - 로그인, 회원, 권한 처리

핵심은 `game` 패키지 안에 공통 세션 로직과 모드별 구현을 같이 두는 것이다.

## 15. 테스트 전략

이 프로젝트는 테스트가 있어야 포트폴리오 설득력이 올라간다.

### 꼭 필요한 테스트

- 점수 계산 서비스 단위 테스트
- 위치 게임 국가 코드 판정 테스트
- 인구수 보기 생성 / 점수 계산 테스트
- 게임 세션 상태 전이 테스트
- 랭킹 반영 통합 테스트
- 추천 점수 계산 테스트

### 권장 테스트 구분

- 단위 테스트
  - 점수 정책, 판정 로직, 추천 계산
- 통합 테스트
  - JPA 저장, Redis 연동, 서비스 흐름
- 웹 테스트
  - 주요 페이지 응답, API 요청/응답 검증

면접에서는 "어떤 테스트가 핵심 비즈니스 리스크를 막는가"를 설명할 수 있어야 한다.

## 16. 이 프로젝트에서 면접 때 강조할 포인트

- 프론트가 아니라 서버가 게임 상태를 관리하도록 설계한 이유
- 정답 판정과 점수 계산을 서비스 계층에서 어떻게 캡슐화했는지
- Redis를 단순 캐시가 아니라 랭킹용 자료구조로 사용한 이유
- AI를 서비스 런타임 호출이 아니라 오프라인 설문 개선 도구로 제한한 이유
- SSR + API 혼합 구조를 선택한 이유
- 프로토타입을 아케이드 게임 루프로 다시 설계한 이유

## 17. 지금 바로 시작할 때의 현실적인 개발 순서

가장 좋은 시작 순서는 아래다.

1. 프로젝트 이름 확정
2. 국가 시드 데이터 포맷 결정(JSON/CSV)
3. `위치 찾기 Level 1` 프로토타입 완성
4. 위치 게임을 하트 기반 아케이드 루프로 리부트
5. 게임 세션 공통 구조 재정리
6. 인구수 게임 추가
7. Redis 랭킹 연결
8. 추천 만족도 집계와 오프라인 개선 루프 정리

핵심은 `지도 게임 1개를 끝까지 완성한 뒤, 실제 플레이 감각과 상태 모델을 다시 다듬고 나서 공통 구조를 재사용`하는 것이다.
처음부터 모든 기능을 동시에 만들면 포트폴리오보다 미완성 사이드프로젝트가 되기 쉽다.
