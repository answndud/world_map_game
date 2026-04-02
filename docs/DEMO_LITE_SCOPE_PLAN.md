# Demo-Lite Scope Plan

## 목적

이 문서는 현재 `WorldMap` 풀 기능 앱을 유지한 채, **무료 플랜에서도 공개 가능한 `demo-lite` 버전**의 제품 범위를 먼저 고정하기 위한 문서다.

핵심 전제는 아래 두 가지다.

1. 현재 메인 앱은 그대로 유지한다.
2. `demo-lite`는 기능을 억지로 반쪽만 남긴 main 변형이 아니라, **공개 데모용으로 의도적으로 줄인 별도 배포 트랙**으로 본다.

즉 목표는 "현재 앱을 무료에 욱여넣기"가 아니라, **무료 배포가 가능한 범위만 다시 정의하는 것**이다.

## 왜 별도 트랙이 필요한가

현재 앱은 아래를 모두 포함한다.

- Spring Boot always-on 서버
- PostgreSQL source of truth
- Redis leaderboard / prod session
- 회원가입 / 로그인 / guest ownership claim
- `/mypage`
- `/stats`
- `/dashboard`
- recommendation feedback 저장과 운영 review

이 구조는 포트폴리오 설명력은 높지만, 무료 플랜에 맞는 구조는 아니다.

따라서 무료 공개용은 아래처럼 다시 정의해야 한다.

- **보여 주고 싶은 것만 남긴다**
- **저장이 필요한 기능은 뺀다**
- **운영용 화면은 뺀다**
- **항상 켜진 서버를 전제로 하지 않는다**

## hard constraint

`demo-lite`는 아래 제약을 전제로 설계한다.

1. PostgreSQL 없이 동작해야 한다.
2. Redis 없이 동작해야 한다.
3. 로그인/회원 세션 없이 동작해야 한다.
4. 서버 저장형 랭킹/통계/운영 화면 없이 동작해야 한다.
5. free-tier에서 끊기지 않게 `정적 + 가벼운 함수` 또는 매우 얇은 런타임만 가정한다.

## 추천 범위

### 남길 화면

`demo-lite v1`에서 남길 화면은 아래다.

1. `/`
   - 홈
   - 게임 선택과 제품 소개의 최소 shell
2. `/games/capital/start`
   - 수도 맞히기
3. `/games/flag/start`
   - 국기 보고 나라 맞히기
4. `/games/population-battle/start`
   - 인구 비교 퀵 배틀
5. `/recommendation/survey`
   - 설문 입력
6. `/recommendation/result`
   - 추천 결과 표시

### 남기지 않을 화면

아래는 `demo-lite v1`에서 제외한다.

1. `/games/location/**`
2. `/games/population/**`
3. `/ranking`
4. `/stats`
5. `/signup`, `/login`, `/logout`
6. `/mypage`
7. `/dashboard/**`
8. `/api/recommendation/feedback`

## 왜 이 범위인가

### 1. 수도 / 국기 / 인구 배틀은 free-demo에 맞다

이 세 게임은 아래 이유로 `demo-lite`에 잘 맞는다.

- 문제/보기 구조가 단순하다
- 정적 국가 데이터로 충분히 설명할 수 있다
- local state나 client-side loop로 옮기기 쉽다
- DB-backed endless run을 강하게 재현하지 않아도 제품 성격이 살아 있다

### 2. 추천 설문은 유지 가치가 높다

추천 설문은 현재 제품의 폭을 보여 주는 중요한 surface다.

다만 `demo-lite`에서는 아래까지만 남긴다.

- 설문 질문
- deterministic scoring
- 결과 3개

아래는 제거한다.

- feedback token
- 만족도 저장
- admin feedback review
- persona baseline ops view

### 3. 위치 게임은 v1에서 뺀다

위치 게임은 대표성은 높지만, `demo-lite` 관점에서는 extraction cost가 가장 높다.

이유:

- WebGL 지구본 렌더링
- 국가 hit-test
- Stage/Attempt 루프
- 서버 판정 중심 계약

즉 위치 게임은 "보여 주면 좋다"보다 "무료 demo-lite를 빠르고 안정적으로 닫는가" 관점에서 후순위다.

### 4. 인구수 4지선다도 v1에서는 뺀다

인구수 맞추기는 구조상 옮길 수는 있지만, `capital / flag / population-battle`보다 free demo에서 덜 차별적이다.

즉 `demo-lite v1`은 아래 세 가지를 보여 주는 편이 낫다.

- 텍스트 퀴즈
- 이미지 퀴즈
- 비교형 퀴즈

## demo-lite에서 유지할 제품 메시지

`demo-lite`는 현재 풀 제품의 축소판이지, 다른 제품이 아니다.

홈에서는 아래 메시지까지만 말한다.

- 세계/국가 주제의 게임과 추천을 한 곳에서 체험할 수 있다
- 몇 가지 대표 게임을 바로 해 볼 수 있다
- 추천 설문 결과를 바로 볼 수 있다
- full version에는 랭킹, 전적, 운영 화면이 있다

즉 `demo-lite`는 **체험판**이고, **풀 제품 기능 소개 페이지**가 아니다.

## demo-lite 상태 전략

### 저장 방식

`demo-lite`는 서버 저장 대신 아래만 허용한다.

- 현재 브라우저 메모리 state
- `localStorage` 기반 last-play / best-score

### 저장하지 않는 것

- 회원 계정
- guest ownership
- session claim
- 서버 랭킹
- 공개 통계
- 추천 만족도
- 운영 로그

## data source 전략

`demo-lite`는 아래 정적 자산만 재사용한다.

- `src/main/resources/data/countries.json`
- `src/main/resources/data/flag-assets.json`
- `src/main/resources/static/images/flags/*`

즉 유지할 것은 **국가 기준 데이터와 국기 자산**이고, 버릴 것은 **DB에 쌓이는 플레이 기록**이다.

## non-goal

아래는 `demo-lite v1`의 목표가 아니다.

- main app와 기능 parity 맞추기
- 로그인/전적/운영 흐름 보존
- Redis fallback 구조 재현
- browser smoke / public URL smoke 전체 재사용
- "Spring Boot backend 포트폴리오" 전체를 무료 호스팅에 그대로 올리기

## v1 성공 기준

아래를 만족하면 `demo-lite v1` 범위가 닫힌다.

1. 무료 플랜에서 공개 URL이 뜬다.
2. 홈에서 대표 게임 3종과 추천 설문으로 바로 진입할 수 있다.
3. 수도/국기/인구 배틀이 DB 없이 한 판 끝까지 동작한다.
4. 추천 설문이 저장 없이 결과 3개를 보여 준다.
5. 로그인, 랭킹, 통계, 운영 화면이 없어도 제품 설명이 어색하지 않다.

## v1 이후 후보

`demo-lite v1`을 닫은 뒤에만 아래를 검토한다.

1. 위치 게임 추가
2. 인구수 4지선다 추가
3. localStorage 기반 "내 브라우저 최고 점수"
4. full version 비교 소개 섹션 추가

즉 확장은 **무료 배포가 먼저 안정화된 뒤**에만 연다.
