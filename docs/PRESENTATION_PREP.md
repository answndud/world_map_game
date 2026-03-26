# 발표 / 면접 준비 노트

## 3분 소개 스크립트

안녕하세요. WorldMap은 국가 위치 찾기 게임, 인구수 맞추기 게임, 나라 추천, 실시간 랭킹을 한 사이트 안에 묶은 백엔드 중심 게임 플랫폼입니다.

이 프로젝트에서 가장 중요하게 잡은 기준은 프론트보다 서버가 상태를 주도해야 한다는 점입니다. 그래서 게임 시작, 정답 판정, 점수 계산, 하트 감소, 다음 Stage 생성은 모두 Spring Boot 서비스가 맡고, 프론트는 입력과 표현만 담당합니다.

저는 이 구조를 두 개의 게임에 공통으로 적용했습니다. 위치 게임과 인구수 게임 모두 `session -> stage -> attempt` 구조를 사용하고, 같은 Stage 재시도와 endless run을 서버가 관리합니다. 게임이 끝나면 완료된 run을 `leaderboard_record`로 별도 저장하고, Redis Sorted Set에는 빠른 상위 랭킹 조회용 read model만 유지합니다.

추천 기능은 런타임 LLM 호출 없이 설문 기반으로 계산했습니다. 추천 결과는 서버가 deterministic하게 만들고, 사용자의 1~5점 만족도만 저장해서 `/dashboard`에서 버전별 만족도와 baseline drift를 함께 보는 구조로 정리했습니다.

회원 기능도 커뮤니티 목적이 아니라 기록 유지 목적에 맞춰 단순하게 설계했습니다. 게스트는 바로 플레이하고, 로그인하면 현재 브라우저의 guest 기록이 계정으로 귀속됩니다.

결과적으로 이 프로젝트는 게임형 서비스이지만, 실제로 보여주고 싶은 것은 서버 상태 관리, read model 분리, 추천 품질 개선 루프, 그리고 설명 가능한 아키텍처입니다.

## 10분 기술 설명 아웃라인

### 1. 왜 이 프로젝트를 골랐는가

- CRUD보다 상태 관리가 있는 백엔드 서비스를 보여주고 싶었다.
- 게임은 상태 전이와 점수 계산을 설명하기 좋다.

### 2. 전체 구조

- SSR + API 혼합
- Spring Boot + JPA + Redis
- PostgreSQL은 truth, Redis는 leaderboard read model

### 3. 게임 도메인

- 위치/인구수 모두 `session -> stage -> attempt`
- 왜 session과 leaderboard_record를 분리했는가
- 왜 점수/정답 판정을 서버가 가져가는가

### 4. 랭킹

- 게임 종료 후 RDB 저장
- after-commit Redis Sorted Set 반영
- Redis miss 시 DB fallback
- 왜 지금은 15초 polling으로 닫았는가

### 5. 추천

- 런타임 LLM 호출 없이 deterministic 계산
- 만족도만 저장
- dashboard에서 feedback + baseline drift 함께 보기

### 6. 계정과 기록 귀속

- guestSessionKey
- memberId
- 로그인 직후 current guest progress claim

### 7. 트러블슈팅

- Level 2 실험 rollback
- legacy DB와 Redis 정리
- public surface와 internal code를 같이 단순화

## 면접에서 자주 나올 질문

### 왜 프론트가 아니라 서버에서 점수를 계산했나요?

게임 상태와 점수는 조작 가능성이 있고, 랭킹의 근거가 되기 때문에 서버가 source of truth여야 했습니다.

### Redis를 왜 썼나요?

랭킹 상위 N명 조회를 빠르게 하기 위해서입니다. 점수의 영속 저장은 RDB가 맡고, Redis는 leaderboard read model만 맡습니다.

### 왜 SSE/WebSocket을 안 썼나요?

현재 프로젝트 범위에서는 15초 polling으로도 충분히 살아 있는 랭킹 체감을 줄 수 있었고, SSE/WebSocket에서 생기는 연결 관리 복잡도보다 설명 가능한 구조가 더 중요하다고 판단했습니다.

### 추천에 왜 LLM을 안 붙였나요?

런타임 과금과 비결정성을 피하고 싶었습니다. 추천 계산은 서버가 deterministic하게 하고, AI는 오프라인 문항 개선 루프에만 사용했습니다.

### 왜 guest를 유지했나요?

진입 장벽을 낮추기 위해서입니다. 대신 기록을 유지하고 싶을 때만 단순 계정으로 넘어가게 했고, 그때 같은 브라우저의 guest 기록을 계정에 귀속합니다.

## 약한 부분

- 모바일 지구본 상호작용은 더 다듬을 여지가 있다.
- dashboard 지표는 운영 입문 수준이며, 장기 trend chart는 아직 없다.
- 추천 baseline은 강해졌지만, 현실 데이터셋 기반 검증까지는 가지 않았다.

## 발표 전에 마지막으로 확인할 것

1. local demo 계정 로그인
2. `/stats`, `/ranking`, `/mypage`, `/dashboard` 확인
3. 위치 게임 한 판, 인구수 게임 한 판 직접 플레이
4. 추천 설문 제출 후 `/dashboard/recommendation/feedback` 확인
5. [ARCHITECTURE_OVERVIEW.md](/Users/alex/project/worldmap/docs/ARCHITECTURE_OVERVIEW.md), [ERD.md](/Users/alex/project/worldmap/docs/ERD.md), [REQUEST_FLOW_GUIDE.md](/Users/alex/project/worldmap/docs/REQUEST_FLOW_GUIDE.md) 다시 읽기
