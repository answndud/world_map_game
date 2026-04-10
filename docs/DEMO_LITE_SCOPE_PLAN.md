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

## 구현 baseline

현재 저장소는 `demo-lite`의 실제 baseline을 아래처럼 고정한다.

1. sibling 앱 경로는 [demo-lite](/Users/alex/project/worldmap/demo-lite)다.
2. 스택은 `Vite + vanilla JS`다.
3. 라우팅은 free static hosting rewrite 의존을 줄이기 위해 `hash routing`을 먼저 쓴다.
4. 메인 앱의 Spring Boot runtime, JPA, Redis, auth bean graph는 재사용하지 않는다.
5. 대신 아래 정적 데이터와 자산만 직접 읽는다.
   - [countries.json](/Users/alex/project/worldmap/src/main/resources/data/countries.json)
   - [flag-assets.json](/Users/alex/project/worldmap/src/main/resources/data/flag-assets.json)
   - [flags](/Users/alex/project/worldmap/src/main/resources/static/images/flags)

즉 `demo-lite`는 "메인 앱의 profile 하나"가 아니라, 같은 저장소 안의 **별도 공개 앱**이다.

## 추천 범위

### 남길 화면

현재 `demo-lite`에서 남길 화면은 아래다.

1. `#/`
   - 홈
   - 게임 선택과 제품 소개의 최소 shell
2. `#/games/capital`
   - 수도 맞히기
3. `#/games/flag`
   - 국기 보고 나라 맞히기
4. `#/games/population`
   - 국가 인구수 맞추기
5. `#/games/population-battle`
   - 인구 비교 퀵 배틀
6. `#/recommendation`
   - 설문 입력과 추천 결과 표시

### 남기지 않을 화면

아래는 현재 `demo-lite`에서 제외한다.

1. `/games/location/**`
2. `/ranking`
3. `/stats`
4. `/signup`, `/login`, `/logout`
5. `/mypage`
6. `/dashboard/**`
7. `/api/recommendation/feedback`

## 왜 이 범위인가

### 1. 수도 / 국기 / 인구수 / 인구 배틀은 free-demo에 맞다

이 네 게임은 아래 이유로 `demo-lite`에 잘 맞는다.

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

### 4. 인구수 4지선다는 shared country data만으로 안전하게 추가할 수 있다

인구수 맞추기는 `countries.json`만으로 구간형 보기 4개를 만들 수 있고, 현재 `capital / flag / population-battle` local-state loop와 같은 패턴으로 닫을 수 있다.

즉 현재 `demo-lite`는 아래 네 가지 플레이 감각을 함께 보여 준다.

- 텍스트 퀴즈
- 이미지 퀴즈
- 구간 추정 퀴즈
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
2. 홈에서 대표 게임 4종과 추천 설문으로 바로 진입할 수 있다.
3. 수도/국기/인구수/인구 배틀이 DB 없이 한 판 끝까지 동작한다.
4. 추천 설문이 저장 없이 결과 3개를 보여 준다.
5. 로그인, 랭킹, 통계, 운영 화면이 없어도 제품 설명이 어색하지 않다.

## 현재 실제로 열린 첫 조각

현재 저장소에는 아래까지 구현돼 있다.

- [demo-lite](/Users/alex/project/worldmap/demo-lite) 디렉터리 생성
- [package.json](/Users/alex/project/worldmap/demo-lite/package.json), [vite.config.mjs](/Users/alex/project/worldmap/demo-lite/vite.config.mjs) 추가
- [index.html](/Users/alex/project/worldmap/demo-lite/index.html), [main.js](/Users/alex/project/worldmap/demo-lite/src/main.js), [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js), [routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js), [style.css](/Users/alex/project/worldmap/demo-lite/src/style.css)로 별도 app shell 생성
- `#/`, `#/games/capital`, `#/games/flag`, `#/games/population`, `#/games/population-battle`, `#/recommendation` retained route map 고정
- [sync-shared-assets.mjs](/Users/alex/project/worldmap/demo-lite/scripts/sync-shared-assets.mjs)와 [shared-data.js](/Users/alex/project/worldmap/demo-lite/src/lib/shared-data.js)로 메인 저장소의 `countries.json`, `flag-assets.json`, `flags/*`를 `public/generated/`로 복사하고 fetch하는 계약 추가
- [README.md](/Users/alex/project/worldmap/demo-lite/README.md)로 별도 실행 명령 분리

즉 첫 구현 조각의 목적은 실제 게임 이식이 아니라, **free-tier 공개용 sibling 앱의 entrypoint와 navigation을 먼저 고정하는 것**이다.

후속 실제 구현으로는 아래 네 retained game과 20문항 recommendation loop가 먼저 열려 있다.

- [capital-game.js](/Users/alex/project/worldmap/demo-lite/src/features/capital-game.js)
  - `수도 맞히기 5문제 러닝 + 3 lives + 같은 문제 재시도 + localStorage best score`
- [flag-game.js](/Users/alex/project/worldmap/demo-lite/src/features/flag-game.js)
  - `국기 퀴즈 5문제 러닝 + same-continent 우선 distractor + 같은 문제 재시도 + localStorage best score`
- [population-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-game.js)
  - `인구 규모 구간 4선다 + 5문제 러닝 + 같은 문제 재시도 + localStorage best score`
- [population-battle-game.js](/Users/alex/project/worldmap/demo-lite/src/features/population-battle-game.js)
  - `인구 순위 gap 기반 2-choice 배틀 + 5 Stage 구간 + 같은 문제 재시도 + localStorage best score`
- [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)
  - `survey-v4 / engine-v20 + 20문항 설문 + 30국가 deterministic top 3 + 추천 요약/공유 copy + 비교 기준 카드 + feedback 저장 제거`
- [recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)
  - `메인 anchor scenario(USA / CAN / MYS / NZL) top1을 browser-side test로 고정`

즉 현재 `demo-lite v1`은 retained route shell만 있는 상태가 아니라, retained game 4종과 20문항 recommendation surface까지 브라우저 메모리 상태만으로 끝까지 체험할 수 있다.

후속 마감 조각으로는 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)를 추가해,

- 각 게임 종료 기록
- recommendation 최근 TOP1
- mode별 browser summary

를 [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js) 홈에서 다시 읽게 했다.

즉 `demo-lite`는 이제 retained surface만 playable한 것이 아니라, **브라우저 단위 recent play와 cross-mode summary까지 가진 체험판**이 됐다.

추가 마감 조각으로는 같은 [browser-history.js](/Users/alex/project/worldmap/demo-lite/src/lib/browser-history.js)가

- 최근 게임 mode streak
- 최근 클리어 streak
- 복사 가능한 one-line share summary

까지 계산하고, [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js) 홈이 이를 별도 panel로 노출한다.

즉 현재 `demo-lite` 홈은 단순 route index가 아니라, **playable surface 4개 위에 얇은 browser-side read model을 한 번 더 올린 요약 대시보드**에 가깝다.

추가로 [style.css](/Users/alex/project/worldmap/demo-lite/src/style.css), [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js), [routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)를 다시 잡아 `demo-lite` 전체 visual shell도 한 번 더 리디자인했다.

- 기준은 `Coinbase-inspired blue / white / near-black` visual system이다.
- 홈은 `dark hero + white card grid + browser recent summary` 위계로 재배치했다.
- feature route hero도 같은 토큰 안에서 dark section과 pill CTA를 공유하게 맞췄다.
- [public/_headers](/Users/alex/project/worldmap/demo-lite/public/_headers)는 Google Fonts를 쓰되 Cloudflare Pages CSP를 최소 허용만 열도록 같이 조정했다.

즉 이번 조각의 의도는 기능 추가가 아니라, 이미 열어 둔 retained surfaces가 `촌스러운 정적 샘플`이 아니라 `공개 가능한 제품 체험판`으로 읽히게 만드는 것이었다.

## v1 이후 후보

`demo-lite v1`을 닫은 뒤에만 아래를 검토한다.

1. 위치 게임 추가
2. 질문 프리셋 / 대표 페르소나 quick start
3. localStorage 기반 "내 브라우저 최고 점수"
4. full version 비교 소개 섹션 추가

즉 확장은 **무료 배포가 먼저 안정화된 뒤**에만 연다.
