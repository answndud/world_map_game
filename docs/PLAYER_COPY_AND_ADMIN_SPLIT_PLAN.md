# Player Copy And Admin Split Plan

## 목적

이 문서는 WorldMap 서비스를 `플레이어가 보는 제품 화면`과 `개발자/관리자가 보는 내부 운영 화면`으로 분리하는 설계 기준을 정리한다.

핵심 목표는 두 가지다.

1. 공개 화면에서는 이 서비스가 `포트폴리오용 프로젝트`나 `개발 중 프로토타입`처럼 보이지 않게 만든다.
2. 버전, 피드백 집계, 로드맵, 기술 키워드 같은 내부 정보는 별도 관리자 화면으로 옮긴다.

즉, 사용자는 완성된 게임 서비스처럼 느끼고, 개발자는 관리자 페이지에서 품질과 운영 정보를 확인하는 구조로 바꾼다.

## 현재 문제

현재 공개 화면에는 아래처럼 내부 개발 관점 문구가 섞여 있다.

- 홈:
  - `Spring Boot 3 Game Platform`
  - `Current Build`
  - `ORBIT 0.4`
  - `현재 로드맵`
  - `Prototype`, `Survey + Eval`, `Redis Sorted Set`
- 추천 설문/결과:
  - `deterministic`
  - `Offline Eval`
  - `surveyVersion`, `engineVersion`
  - `만족도 집계 보기`
- 여러 화면 설명:
  - `서버 주도`, `backend`, `포트폴리오`, `오프라인 평가`, `AI-assisted` 같은 표현이 그대로 노출됨

이 정보는 개발자에게는 필요하지만, 플레이어 입장에서는 몰입을 깨고 서비스가 “데모”처럼 보이게 만든다.

## 설계 원칙

### 1. 공개 화면은 제품 언어만 쓴다

플레이어가 보는 화면에서는 다음 표현을 기본적으로 제거한다.

- Spring Boot
- 백엔드
- 포트폴리오
- 프로토타입
- deterministic
- surveyVersion / engineVersion
- Offline Eval
- Redis / Sorted Set / polling
- 현재 빌드 / 로드맵

대신 아래처럼 사용자 가치 중심 표현으로 바꾼다.

- `3D 지구본에서 나라를 찾는 미션`
- `인구 규모를 감으로 맞히는 퀴즈`
- `나와 맞는 나라를 찾는 설문`
- `지금 플레이어들의 상위 점수`

### 2. 내부 운영 정보는 관리자 화면으로 분리한다

버전, 만족도 집계, 페르소나 baseline, 현재 구현 상태, 로드맵 같은 정보는 public 화면이 아니라 `/admin` 아래에서만 본다.

### 3. public과 admin은 정보 구조부터 다르게 가져간다

같은 데이터라도 설명 방식이 달라야 한다.

- public:
  - 무엇을 할 수 있는가
  - 어떻게 플레이하는가
  - 지금 들어가면 어떤 경험을 얻는가
- admin:
  - 어떤 버전이 배포 중인가
  - 만족도는 어느 버전이 높은가
  - 약한 시나리오는 무엇인가
  - 다음 개선 우선순위는 무엇인가

### 4. 지금 단계의 admin은 “운영 도구”이지 “보안 완료 기능”이 아니다

현재 단계에서는 먼저 화면과 라우트를 분리한다.

- 1차: 헤더/공개 링크에서 admin을 숨기고, 내부 운영 페이지를 `/admin` 아래로 분리
- 2차: 인증/권한은 8단계에서 붙인다

즉, 지금 만드는 것은 정보 구조와 화면 분리이고, 보안 경계는 이후 단계에서 강화한다.

## 공개 / 내부 정보 분리 표

| 주제 | 공개 화면 | 관리자 화면 |
| --- | --- | --- |
| 게임 소개 | 가능 | 가능 |
| 게임 규칙 | 가능 | 가능 |
| 랭킹 | 가능 | 가능 |
| 추천 설문 | 가능 | 가능 |
| 만족도 집계 | 숨김 | 가능 |
| surveyVersion / engineVersion | 숨김 | 가능 |
| 현재 빌드 상태 | 숨김 | 가능 |
| 로드맵 / 구현 현황 | 숨김 | 가능 |
| Redis / polling / backend 설명 | 숨김 | 가능 |
| weak scenario / persona baseline | 숨김 | 가능 |

## 목표 정보 구조

### 공개 경로

- `/`
  - 게임 허브 홈
- `/games/location/start`
  - 위치 미션 시작
- `/games/population/start`
  - 인구 퀴즈 시작
- `/recommendation/survey`
  - 나라 추천 설문
- `/recommendation/result`
  - 추천 결과
- `/ranking`
  - 플레이어 랭킹

### 관리자 경로

- `/admin`
  - 운영 대시보드
- `/admin/recommendation/feedback`
  - 만족도 집계
- `/admin/recommendation/persona-baseline`
  - 오프라인 페르소나 baseline 요약
- `/admin/build`
  - 현재 버전 / 구현 상태 / 내부 메모

현재 1차 구현에서는 최소 아래 두 페이지부터 시작한다.

- `/admin`
- `/admin/recommendation/feedback`

## 화면별 문구 리디자인 방향

### 홈 `/`

현재 문제:

- 개발 스택과 빌드 상태가 전면에 보인다.
- `현재 로드맵`이 사용자가 보는 메인 화면에 있다.
- 카드 상태값 `Prototype`, `Survey + Eval`이 제품보다 개발 단계를 먼저 보여 준다.

변경 방향:

- eyebrow:
  - `Spring Boot 3 Game Platform` 제거
  - 제품 톤 문구로 교체
- 우측 패널:
  - `Current Build`, `ORBIT 0.4` 제거
  - 대신 `오늘 플레이할 모드`, `추천 플레이 순서` 같은 유저용 정보로 교체
- `현재 로드맵`:
  - public 홈에서 제거
  - `/admin/build` 또는 `/admin`으로 이동
- 모드 상태값:
  - `Prototype`, `Survey + Eval`, `Redis Sorted Set` 같은 구현 용어 제거
  - `Mission`, `Quiz`, `Discover`, `Live Ranking` 같은 사용자 언어로 교체

### 추천 설문 `/recommendation/survey`

현재 문제:

- `deterministic`, `Offline Eval` 같은 내부 구현 문구가 보인다.

변경 방향:

- 설문 목적을 사용자 가치 중심으로 다시 쓴다.
  - 예: `생활 취향을 고르면 잘 맞는 나라 3곳을 골라드립니다.`
- 내부 품질 루프 언급 제거
- 문항 helper text도 사용자가 답하기 쉬운 생활 표현으로 다듬는다.

### 추천 결과 `/recommendation/result`

현재 문제:

- `서버가 8개 설문 답변을 가중치로 바꿨다`
- `deterministic`
- `만족도 집계 보기`

변경 방향:

- 결과 설명은 사용자 관점으로 바꾼다.
  - 예: `당신의 생활 취향과 가장 잘 맞는 나라를 골랐습니다.`
- 만족도 제출은 유지하되, 운영/집계 화면 링크는 제거
- `version` 관련 값은 hidden payload에만 남기고 화면에는 노출하지 않는다

### 랭킹 `/ranking`

현재 문제:

- 구현 용어가 강하게 드러나는 복사본이 남아 있을 가능성이 높다.

변경 방향:

- 플레이어에게는 `실시간 상위 점수`만 보여 준다
- polling, Redis, fallback 같은 문구는 admin으로 이동

## 관리자 페이지 설계

### `/admin`

역할:

- 내부 운영 허브

노출 정보:

- 현재 공개 모드 상태
- 추천 설문/엔진 현재 버전
- 만족도 총 응답 수
- weak scenario 요약
- 다음 작업 우선순위

데이터 출처:

- 추천 버전: `RecommendationSurveyService`
- 만족도 집계: `RecommendationFeedbackService`
- 나머지 구현 상태: controller/view model에서 조합

### `/admin/recommendation/feedback`

역할:

- 지금의 `/recommendation/feedback-insights`를 public에서 분리한 내부용 운영 화면

노출 정보:

- surveyVersion / engineVersion
- 평균 점수
- 응답 수
- 점수 분포
- 마지막 응답 시각

public과의 차이:

- public 결과 화면에서는 이 링크를 없앤다
- admin에서는 버전 비교와 표본 수 판단을 중심으로 본다

### `/admin/recommendation/persona-baseline`

역할:

- 오프라인 페르소나 coverage와 snapshot 관리 화면의 초석

1차는 정적/SSR 요약으로 충분하다.

- baseline 시나리오 수
- 현재 품질 하한
- weak scenario 목록
- 마지막 수동 업데이트 시점

이 페이지는 나중에 실제 테스트 리포트 연계가 필요하면 확장한다.

## 패키지 / 템플릿 구조 제안

- `com.worldmap.admin.web.AdminPageController`
- `com.worldmap.admin.application.AdminDashboardService`
- `/Users/alex/project/worldmap/src/main/resources/templates/admin/index.html`
- `/Users/alex/project/worldmap/src/main/resources/templates/admin/recommendation-feedback.html`

설계 이유:

- 여러 도메인에서 읽기 전용 운영 데이터를 모아야 하므로, 컨트롤러가 직접 각 서비스에서 값을 끌어오게 하지 않고 `AdminDashboardService`가 조합한다.
- 추천 집계처럼 이미 서비스가 있는 데이터는 기존 `RecommendationFeedbackService`를 재사용한다.

즉,

- 도메인 계산은 기존 서비스가 맡고
- admin 페이지용 조합은 admin application/service가 맡고
- controller는 SSR 진입만 담당한다.

## 구현 순서

### Slice 1. public copy audit 반영

- 홈
- 추천 설문
- 추천 결과
- 랭킹

목표:

- public 화면에서 내부 개발 용어 제거

### Slice 2. admin read-only dashboard 1차

- `/admin`
- `/admin/recommendation/feedback`

목표:

- 현재 public에 노출된 내부 정보를 admin으로 옮긴다

### Slice 3. 링크와 네비게이션 분리

- public 헤더에서는 admin 링크 숨김
- 추천 결과 화면의 `만족도 집계 보기` 제거
- internal 진입은 직접 경로 또는 이후 auth 후 메뉴 노출

### Slice 4. auth 붙이기

- 8단계에서 admin 라우트에 인증/권한 적용

## 테스트 계획

### public 화면 테스트

- 홈 화면에 `Spring Boot`, `Portfolio`, `Prototype`, `Current Build` 같은 단어가 없는지
- 추천 결과 화면에 `survey-v`, `engine-v`, `feedback-insights` 링크가 없는지

### admin 화면 테스트

- `/admin`이 버전/집계 요약을 렌더링하는지
- `/admin/recommendation/feedback`가 버전별 집계를 렌더링하는지

### 경계 테스트

- public 결과 페이지는 만족도 제출만 가능하고, 내부 집계 링크는 노출하지 않는지
- admin 페이지는 기존 서비스 데이터를 읽기 전용으로 조합만 하는지

## 다음 구현 조각

가장 먼저 할 일은 두 가지다.

1. public copy 전면 정리
2. `/recommendation/feedback-insights`를 `/admin/recommendation/feedback`으로 옮기는 read-only admin 1차

이 두 조각이 끝나야 플레이어는 제품만 보고, 개발자는 관리자 화면에서 운영 데이터를 볼 수 있는 구조가 만들어진다.
