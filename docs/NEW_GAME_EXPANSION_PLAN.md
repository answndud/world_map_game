# 새 게임 확장 계획

## 목적

현재 public 제품은

- 국가 위치 찾기
- 수도 맞히기
- 국가 인구수 맞추기
- 나라 추천
- 랭킹

까지 안정화돼 있다.

다음 확장은 무작정 모드를 늘리는 것이 아니라,
`현재 country/session/ranking 구조를 재사용하면서 설명 가능한 게임`을 추가하는 방향이어야 한다.

이번 계획의 목표는 아래 세 가지를 순서대로 여는 것이다.

1. 수도 맞히기
2. 인구 비교 퀵 배틀
3. 국기 보고 나라 맞히기

## 왜 이 세 가지를 고르는가

### 1. 수도 맞히기

가장 먼저 해야 한다.

이유:

- `country.capitalCity`가 이미 있다.
- 4지선다 보기형 구조가 현재 인구수 게임과 유사해 reuse가 쉽다.
- 서버가 `출제 국가 선택 -> 보기 생성 -> 정답 판정 -> 점수/하트`를 그대로 가져갈 수 있다.

즉, 가장 작고 설명 가능한 다음 게임이다.

### 2. 인구 비교 퀵 배틀

두 번째가 맞다.

이유:

- `country.population`이 이미 있다.
- 현재 인구수 게임의 보기형/점수/난이도 정책을 재사용할 수 있다.
- “정확한 인구 규모 구간 고르기”와 달리, 체감상 더 빠르고 아케이드스럽다.

즉, 새 데이터 없이도 추가 가능한 고속 비교형 모드다.

### 3. 국기 보고 나라 맞히기

세 번째가 맞다.

이유:

- 사용자 체감은 좋다.
- 하지만 국기 정적 자산 파이프라인이 먼저 필요하다.
- 즉 게임 규칙보다 데이터/에셋 파이프라인 준비가 선행된다.

## 왜 이 순서가 설명 가능한가

현재 도메인 구조는 이미

- 세션
- Stage
- Attempt
- 랭킹 run

기준으로 설명 가능하다.

새 게임도 이 구조를 최대한 재사용해야 한다.

그래서 우선순위는

- 새 데이터 없이 바로 열 수 있는 게임 먼저
- 그 다음 기존 데이터 재사용 게임
- 마지막에 자산 파이프라인이 필요한 게임

순서가 된다.

## 공통 설계 원칙

### 서버 주도 유지

새 게임도 모두

- 문제 생성
- 보기 생성
- 정답 판정
- 점수 계산
- 하트 감소
- 다음 Stage 생성

을 서버가 맡는다.

### 세션 / Stage / Attempt 구조 재사용

완전히 새로운 추상화보다,
현재 위치/인구수 게임에서 이미 설명 가능한 구조를 재사용하는 쪽이 낫다.

### 랭킹은 완료 run 기준 유지

게임 종류가 늘어나도 `leaderboard_record`는 여전히 “끝난 한 판” 기준으로 저장하는 것이 맞다.

## 수도 맞히기 첫 조각

첫 구현 조각인 `수도 맞히기 Level 1 vertical slice`는 현재 완료됐다.

### 입력 방식

- 나라 1개 제시
- 수도 보기 4개 중 1개 선택

### 서버 책임

- 출제 국가 선택
- 오답 보기를 다른 수도 3개로 생성
- 정답 판정
- 하트/점수/다음 Stage 관리

### 필요한 데이터

- `country.id`
- `country.nameKr`
- `country.nameEn`
- `country.capitalCity`
- `country.continent`

### 첫 구현 범위

1. `capital` game mode 정의
2. start/state/answer/result 흐름 추가
3. SSR 시작/플레이/결과 화면 추가
4. 최소 1개 핵심 통합 테스트 추가
5. 랭킹 반영은 첫 조각에서는 붙이되, UI polish는 다음 턴로 넘김

### 현재 완료 상태

- `capital` game mode 추가
- `capital_game_session / stage / attempt` 저장 구조 추가
- start/state/answer/restart/result API 및 SSR 화면 추가
- same-continent 우선 distractor 생성과 글로벌 fallback 구현
- 공개 홈, `/ranking`, `/stats`까지 capital 모드 연결
- 핵심 통합 테스트와 ranking/stats/home 회귀 테스트 추가

다음 작은 조각은 `인구 비교 퀵 배틀 Level 1 설계`였다.

## 인구 비교 퀵 배틀 초안

### 입력 방식

- 두 나라 제시
- “인구가 더 많은 나라” 고르기

### 장점

- 읽기 속도가 빠르다.
- 현재 `population` 데이터를 그대로 재사용한다.

### 리스크

- 현재 인구수 게임과 너무 비슷하게 느껴질 수 있다.

그래서 수도 맞히기 다음 순서가 맞다.

### 현재 완료 상태

- `population-battle` game mode 추가
- `population_battle_game_session / stage / attempt` 저장 구조 추가
- start/state/answer/restart/result API 및 SSR 화면 추가
- 인구 rank gap 기반 pair 생성과 left/right 랜덤 배치 구현
- 공개 홈, `/ranking`, `/stats`까지 population-battle 모드 연결
- 핵심 통합 테스트와 ranking/stats/home 회귀 테스트 추가

## 국기 게임 초안

### 입력 방식

- 국기 이미지 1개
- 나라 4지선다

### 선행 작업

- 국기 자산 source 정리
- 정적 파일 경로 정책
- local/dev/demo에 포함 가능한 크기와 형식 결정

즉, 이 게임은 규칙 설계보다 에셋 파이프라인이 먼저다.

### 현재 결정

- [FLAG_GAME_ASSET_PIPELINE_PLAN.md](/Users/alex/project/worldmap/docs/FLAG_GAME_ASSET_PIPELINE_PLAN.md)로 국기 게임 1차 자산 기준을 고정
- 실제 파일은 `src/main/resources/static/images/flags/{iso3}.svg`
- manifest는 `src/main/resources/data/flag-assets.json`
- 출제 가능 국가는 `country seed ∩ manifest ∩ 실제 파일 존재` 교집합
- `FlagAssetCatalog`가 manifest를 읽고, ISO3 / 경로 / format / 파일 존재를 startup 기준으로 검증하게 했다.
- sample SVG 12개를 먼저 저장소에 포함해 local/demo에서도 같은 자산을 재현 가능하게 했다.

다음 작은 코드 조각은 `flag` game mode vertical slice가 아니라 `출제 가능 flag country pool 계산 + game mode 첫 skeleton`이다.

## 이번 우선순위 변경 이유

원래는 10단계 발표 자료 정리를 더 밀어도 됐다.

하지만 사용자가 실제로 서비스 확장 방향을 먼저 보고 싶어 했고,
현재 구조상 새 게임 1개를 추가하는 편이 슬라이드보다 더 큰 제품 가치가 있다.

그래서 지금은 발표 자료보다 `신규 게임 확장`을 먼저 여는 것으로 우선순위를 바꾼다.

## 면접에서 어떻게 설명할 것인가

> 기존 두 게임이 안정화된 뒤에는 데이터를 더 억지로 늘리기보다, 현재 `country` 모델과 `session/stage/attempt` 구조를 재사용할 수 있는 새 게임을 확장하는 쪽이 더 자연스럽다고 판단했습니다. 그래서 capitalCity가 이미 있는 수도 맞히기를 첫 확장 대상으로 잡고, 그 다음 population 재사용이 쉬운 인구 비교 배틀, 마지막으로 자산 파이프라인이 필요한 국기 게임 순으로 열기로 설계했습니다.
