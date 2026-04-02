# [Spring Boot 게임 플랫폼 포트폴리오] 15. public scope reset과 신규 게임 lineup을 어떻게 다시 정리했는가

## 1. 이번 글에서 풀 문제

기능을 더 많이 추가한다고 제품이 더 명확해지는 것은 아닙니다.

오히려 public 제품 범위가 흐릿한 상태에서 게임만 늘리면, 사용자는 물론 개발자 자신도 현재 서비스를 한 문장으로 설명하기 어려워집니다.

WorldMap도 같은 문제를 겪었습니다.

초기에는 아래가 섞여 있었습니다.

- 위치/인구수 게임의 예전 `LEVEL_2` 실험 흔적
- public 화면에는 보이지 않지만 DB와 Redis에 남아 있는 legacy row
- 새로 추가할 capital, population-battle, flag의 제품 위치
- 홈, 랭킹, stats, demo bootstrap이 현재 public 범위를 어디까지 반영해야 하는지에 대한 기준 부족

즉 이 단계의 핵심 문제는 "새 게임을 3개 더 만든다"가 아니었습니다.

실제로 닫아야 했던 문제는 아래 다섯 가지였습니다.

1. 지금 public 제품이 무엇을 운영하는지 다시 한 문장으로 닫기
2. public 설명을 방해하는 Level 2 흔적을 실제 데이터와 Redis에서 정리하기
3. 신규 3개 게임도 기존 `Session / Stage / Attempt` 골격 위에서 설명 가능하게 붙이기
4. home, ranking, stats, mypage가 새 라인업과 어긋나지 않게 맞추기
5. "기능을 더 열었다"가 아니라 "public product scope를 다시 선명하게 만들었다"는 이야기를 남기기

이 글을 다 읽으면 현재 저장소 기준으로 아래를 다시 구현할 수 있어야 합니다.

- Level 2 실험 흔적을 public scope에서 걷어내는 기준
- startup 시점 legacy cleanup 전략
- capital, population-battle, flag 세 게임을 같은 endless run contract 위에서 추가하는 방식
- 홈 카드와 공개 surface가 새 라인업과 같이 움직이게 만드는 방법
- 왜 이 단계가 "신규 기능 추가"보다 "제품 범위 재정렬"에 더 가깝다고 말하는지

## 2. 최종 도착 상태

현재 저장소의 최종 도착 상태를 먼저 고정하겠습니다.

### 2-1. public 제품은 이제 명확하게 한 문장으로 설명된다

지금 public에서 설명하는 제품은 아래입니다.

- 위치 게임 Level 1
- 인구수 퀴즈 Level 1
- 수도 맞히기 Level 1
- 인구 비교 퀵 배틀 Level 1
- 국기 보고 나라 맞히기 Level 1
- 국가 추천 설문
- 공개 `/ranking`
- 공개 `/stats`

즉 public 범위는 "`기본 게임 5개 + 추천 + 랭킹 + stats`"로 정리됩니다.

### 2-2. public surface에는 더 이상 `LEVEL_2`라는 축이 보이지 않는다

현재 public 설명에는 위치/인구수 게임의 Level 2 실험이 나오지 않습니다.

그 이유는 단순히 화면에서 감춘 것이 아니라,

- 코드에서 internal Level 2 호환 축을 제거했고
- startup 시점에 legacy row와 Redis key를 정리하며
- README와 플레이북에서도 Level 1-only public scope로 다시 선언했기 때문입니다.

즉 "안 보이게 했다"가 아니라 "현재 제품 기준에서 실제로 정리했다"가 맞습니다.

### 2-3. capital, population-battle, flag는 새로운 별도 시스템이 아니다

세 게임은 모두 기존 endless run contract 위에 추가됐습니다.

공통점은 같습니다.

- 세션 시작
- 현재 stage 조회
- 답안 제출
- 틀리면 같은 stage 재시도
- 하트 3개 소진 시 `GAME_OVER`
- 정답이면 다음 stage 자동 생성
- terminal 상태에서 leaderboard 기록
- 같은 `sessionId` 재시작

즉 interaction만 다를 뿐, 상태 기계는 같고 제품 설명 언어도 같습니다.

### 2-4. public regrouping은 홈 카드까지만의 문제가 아니다

이 단계 이후 public surface는 같이 움직입니다.

- 홈: 새 게임 카드가 올라간다
- 게임 시작/결과: 각 모드가 endless run contract를 그대로 따른다
- 랭킹: terminal write가 늘면서 새 게임 결과가 보드에 들어간다
- `/stats`: 완료 run 집계가 새 게임을 포함한다
- `/mypage`: 로그인 후 best run / recent play가 새 게임까지 포함된다

즉 "게임 package 하나를 추가한다"로는 끝나지 않고, public surface 전체가 같은 product scope를 바라보게 다시 맞춰야 합니다.

### 2-5. rollback initializer는 product cleanup을 위한 호환성 shim이다

[GameLevelRollbackInitializer.java](../src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)는 현재 제품을 운영하는 도메인 코드가 아닙니다.

이 클래스의 역할은 예전 local/test DB나 Redis에 남아 있을 수 있는 흔적을 정리해,

- 현재 엔티티
- 현재 leaderboard game mode
- 현재 demo bootstrap
- 현재 public explanation

이 서로 어긋나지 않게 만드는 데 있습니다.

즉 이 단계에서 initializer는 "기능"이라기보다 **현재 product scope를 깨끗하게 고정하기 위한 호환성 코드**에 가깝습니다.

## 3. 시작 상태

이 글을 쓰기 전 흔한 잘못된 시작 상태는 아래 둘 중 하나입니다.

### 3-1. 새 게임만 더 열면 된다고 생각하는 상태

이 상태에서는 보통 이렇게 생각합니다.

- 위치/인구수는 이미 있으니 capital만 추가하자
- 그다음 battle, flag도 붙이면 되겠지

하지만 이 접근은 금방 아래 문제를 만듭니다.

- 홈이 현재 제품 범위를 제대로 말하지 못한다
- 랭킹과 stats 설명은 여전히 옛 범위에 머문다
- README와 demo가 다른 제품을 가리킨다
- legacy Level 2 흔적이 데이터 레벨에서 계속 남아 있다

즉 기능 수는 늘었는데 제품 설명은 오히려 흐려집니다.

### 3-2. Level 2를 UI에서만 숨기면 충분하다고 생각하는 상태

이것도 자주 하는 실수입니다.

- 화면에서 버튼을 지운다
- 홈에서 Level 2 카피만 없앤다
- README만 바꾼다

하지만 DB와 Redis에 아래가 남아 있으면 문제는 그대로입니다.

- `game_level = LEVEL_2` row
- 예전 `leaderboard_record` 제약
- Redis `l2` key
- local demo bootstrap과 현재 엔티티가 어긋나는 old schema

즉 public copy만 바꾸고 data/history를 그대로 두면, 설명과 실제 상태가 어긋납니다.

## 4. 먼저 알아둘 개념

### 4-1. public scope reset

실험 기능을 안쪽으로 접고, 지금 사용자에게 실제로 보여 줄 public 범위를 다시 고정하는 작업입니다.

이 단계의 핵심은 "더 많은 것을 열기"보다 "무엇만 보여 줄지 다시 결정하는 것"입니다.

### 4-2. legacy cleanup

예전 실험으로 남은

- DB row
- Redis key
- 제약 조건
- bootstrap 전제

를 현재 제품 기준에 맞게 정리하는 작업입니다.

### 4-3. lineup expansion

신규 게임을 추가할 때,

- 새 도메인을 새로 만드는 대신
- 기존 게임 상태 기계를 재사용하고
- interaction과 data source만 다르게 가져가는 방식

입니다.

### 4-4. asset-backed playable pool

flag 게임처럼 정적 자산이 필요한 모드는 country seed만으로는 부족합니다.

아래 세 조건이 모두 맞아야 실제 출제 pool로 인정할 수 있습니다.

1. country seed에 존재
2. manifest에 등재
3. 실제 정적 파일 존재

### 4-5. same loop, different interaction

capital, population-battle, flag는 모두 같은 loop를 따르지만 입력 리듬은 다릅니다.

- capital: 4지선다
- population-battle: 2-choice left/right
- flag: 4지선다 + 정적 이미지 자산

즉 public lineup을 늘릴 때도 상태 관리 언어는 동일하게 유지할 수 있습니다.

### 4-6. public regrouping

새 게임을 추가하면 홈 카드만 바꾸는 게 아닙니다.

다음도 같이 맞춰야 합니다.

- entry copy
- ranking read model
- stats summary
- mypage read model
- demo bootstrap

이 단계는 public surface를 다시 묶는 작업입니다.

## 5. 이번 글에서 다룰 파일

### 5-1. rollback과 scope reset

- [GameLevelRollbackInitializer.java](../src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)
- [GameLevelRollbackInitializerIntegrationTest.java](../src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java)
- [README.md](../README.md)
- [docs/PORTFOLIO_PLAYBOOK.md](../docs/PORTFOLIO_PLAYBOOK.md)

### 5-2. capital vertical slice

- [CapitalGameService.java](../src/main/java/com/worldmap/game/capital/application/CapitalGameService.java)
- [CapitalGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/capital/CapitalGameFlowIntegrationTest.java)
- [sync_capital_city_kr.py](../scripts/sync_capital_city_kr.py)

### 5-3. population battle vertical slice

- [PopulationBattleGameService.java](../src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameService.java)
- [PopulationBattleGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/populationbattle/PopulationBattleGameFlowIntegrationTest.java)

### 5-4. flag vertical slice와 asset pipeline

- [FlagAssetCatalog.java](../src/main/java/com/worldmap/game/flag/application/FlagAssetCatalog.java)
- [FlagQuestionCountryPoolService.java](../src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolService.java)
- [FlagGameService.java](../src/main/java/com/worldmap/game/flag/application/FlagGameService.java)
- [FlagAssetCatalogTest.java](../src/test/java/com/worldmap/game/flag/application/FlagAssetCatalogTest.java)
- [FlagQuestionCountryPoolServiceIntegrationTest.java](../src/test/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolServiceIntegrationTest.java)
- [FlagGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/flag/FlagGameFlowIntegrationTest.java)
- [fetch_flag_assets.py](../scripts/fetch_flag_assets.py)
- [flag-assets.json](../src/main/resources/data/flag-assets.json)

### 5-5. public surface regrouping

- [HomeController.java](../src/main/java/com/worldmap/web/HomeController.java)
- [home.html](../src/main/resources/templates/home.html)
- [HomeControllerTest.java](../src/test/java/com/worldmap/web/HomeControllerTest.java)
- [docs/REALTIME_DELIVERY_DECISION.md](../docs/REALTIME_DELIVERY_DECISION.md)

## 6. 핵심 도메인 모델 / 상태

이 글의 핵심은 새로운 엔티티를 많이 소개하는 것이 아니라, **제품 범위와 공통 게임 상태 기계를 다시 정렬하는 것**입니다.

### 6-1. `public lineup`

현재 public 설명의 핵심 게임 라인업은 다섯 개입니다.

- `location`
- `population`
- `capital`
- `population-battle`
- `flag`

이 다섯 모드가 홈에서 바로 시작 가능한 game lineup입니다.

추천, ranking, stats는 그 위에 놓이는 product feature입니다.

### 6-2. `legacy Level 2 residue`

과거 위치/인구수 Level 2 실험은 현재 제품 범위에서 제거됐습니다.

하지만 local/test 환경의 기존 DB나 Redis에는 아래가 남아 있을 수 있습니다.

- `location_game_session.game_level = 'LEVEL_2'`
- `population_game_session.game_level = 'LEVEL_2'`
- `leaderboard_record.game_level = 'LEVEL_2'`
- `leaderboard:*:*:l2*` Redis key
- 예전 `leaderboard_record_game_mode_check` constraint
- `leaderboard_record.game_level NOT NULL`

이 흔적이 현재 public 설명과 local boot를 방해합니다.

### 6-3. `same endless loop`

세 신규 게임은 모두 아래 공통 상태 흐름을 따릅니다.

```text
READY
-> IN_PROGRESS
-> (정답) 다음 Stage 생성
-> (오답) 같은 Stage 재시도, 하트 감소
-> GAME_OVER 또는 FINISHED
-> terminal result + leaderboard write
```

즉 새로운 interaction을 추가해도 상태 모델을 새로 설명할 필요가 없습니다.

### 6-4. `asset-backed flag pool`

flag는 country seed만으로 출제하면 안 됩니다.

실제로는 아래가 모두 만족되어야 합니다.

- `countries.json`에 존재
- `flag-assets.json` manifest에 존재
- `/static/images/flags/*.svg` 실제 파일 존재

이 세 조건을 통과한 교집합만 출제 가능한 국가 pool이 됩니다.

### 6-5. `public regrouping state`

제품 범위를 재정렬하면, 상태 변화는 game package만이 아니라 public surface 전체에 전파됩니다.

- 홈 카드 제목/설명 변경
- entry step 변경
- account note 변경
- stats / ranking에서 새 게임 결과가 자연스럽게 보임
- demo bootstrap이 새 게임 sample run까지 채움

즉 product scope reset은 UI copy 변경만이 아니라 read model alignment 작업입니다.

## 7. 왜 이런 설계를 택했는가

### 7-1. 왜 신규 게임을 늘리기 전에 scope reset이 필요했는가

public 제품 범위가 흐린 상태에서 게임 수만 늘리면 설명 비용이 폭발합니다.

예를 들어 아래 질문에 답하기 어려워집니다.

- 지금 위치 게임은 Level 1만 운영하나요?
- 인구수 게임의 직접 입력형 실험은 아직 살아 있나요?
- ranking에 보이는 예전 row는 어떤 기준인가요?
- 홈에 없는 모드가 왜 local DB에는 남아 있나요?

그래서 먼저 "지금 public에서 무엇만 운영하는가"를 다시 닫는 편이 맞았습니다.

### 7-2. 왜 UI만 숨기지 않고 데이터도 정리했는가

제품 설명은 화면과 데이터가 함께 맞아야 합니다.

만약 화면만 숨기고 data를 그대로 두면:

- ranking read model에 legacy row가 섞일 수 있고
- stats / mypage 설명이 흔들리고
- local demo bootstrap이 old schema 때문에 깨질 수 있습니다.

즉 rollback initializer는 cosmetic 정리가 아니라 **현재 제품 설명을 보호하는 데이터 정리 코드**입니다.

### 7-3. 왜 신규 게임도 shared game contract 위에 올렸는가

WorldMap은 "게임이 많다"보다 "서버가 상태를 주도한다"를 보여 주는 포트폴리오입니다.

그래서 새 게임을 늘릴 때도 아래를 유지해야 합니다.

- 세션 시작과 ownership
- 서버가 현재 stage를 계산
- 서버가 점수와 하트를 계산
- stale submit 방어
- terminal result에서만 leaderboard 반영

이 공통 골격이 유지되어야 "interaction은 달라도 backend contract는 같다"라고 설명할 수 있습니다.

### 7-4. 왜 capital -> population-battle -> flag 순서로 확장했는가

세 게임은 난이도와 의존성이 다릅니다.

#### capital

- country seed 재사용성이 높다
- 4지선다라는 익숙한 interaction이다
- 한국어 수도명 보강이라는 data task가 핵심이다

즉 설명하기 가장 쉬운 첫 확장입니다.

#### population-battle

- 기존 population data를 그대로 쓴다
- 4-choice가 아니라 left/right two-choice rhythm을 보여 줄 수 있다
- 같은 endless loop인데 interaction이 다르다는 점을 보여 준다

즉 line-up diversity를 늘리기 좋습니다.

#### flag

- 정적 자산 pipeline이 먼저 필요하다
- 출제 가능 국가 pool을 따로 정의해야 한다
- 난이도도 자산 수와 대륙 분포에 크게 영향받는다

즉 가장 무겁지만 public lineup 완성도는 가장 크게 올립니다.

### 7-5. 왜 home, ranking, stats까지 같이 바뀌어야 하는가

새 게임을 추가했는데 홈만 바뀌면 제품은 여전히 어색합니다.

홈은 "무엇을 시작할 수 있는가"를 말하고,
ranking과 stats는 "무엇이 실제로 운영되고 있는가"를 말합니다.

둘이 다르면 product scope 설명이 끊깁니다.

그래서 이 단계는 game package 추가와 public surface regrouping이 같이 가야 했습니다.

## 8. 코드 설명

### 8-1. `GameLevelRollbackInitializer`는 startup cleanup을 맡는다

[GameLevelRollbackInitializer.java](../src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)는 `ApplicationRunner`입니다.

중요한 점은 세 가지입니다.

1. 이 코드는 요청 처리 중에 실행되지 않습니다.
2. `@ConditionalOnProperty(name = "worldmap.legacy.rollback.enabled", havingValue = "true")`일 때만 켜집니다.
3. legacy column/constraint가 실제로 남아 있을 때만 정리합니다.

즉 이 클래스는 runtime domain rule이 아니라 **legacy 호환 정리 작업**입니다.

### 8-2. initializer가 실제로 정리하는 것

이 클래스는 아래 순서로 움직입니다.

1. 정보 스키마에서 column 존재 여부 확인
2. old check constraint 존재 여부 확인
3. `leaderboard_record_game_mode_check` 제거
4. `leaderboard_record.game_level`이 있으면 `NOT NULL` 제거
5. location의 `LEVEL_2` attempt -> stage -> session 삭제
6. population의 `LEVEL_2` attempt -> stage -> session 삭제
7. `leaderboard_record.game_level = 'LEVEL_2'` 삭제
8. `leaderboard:*:*:l2*` Redis key 삭제

여기서 중요한 것은 "column이 실제로 있을 때만" 움직인다는 점입니다.

즉 예전 local DB를 가진 환경은 정리하지만, 현재 clean schema에서는 불필요한 startup 변경을 하지 않습니다.

cleanup 범위도 제한적입니다.

- 위치/인구수의 `LEVEL_2` row
- `leaderboard_record.game_level = 'LEVEL_2'`
- `leaderboard:*:*:l2*`
- 현재 엔티티/게임 모드를 막는 old constraint와 old nullable 규칙

즉 "오래된 데이터를 다 지운다"가 아니라 **현재 public scope를 방해하는 residue만 정리한다**가 정확합니다.

### 8-3. 왜 constraint 정리까지 initializer가 맡는가

[GameLevelRollbackInitializerIntegrationTest.java](../src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java)를 보면,

- 예전 `leaderboard_record_game_mode_check`
- `game_level NOT NULL`

같은 schema residue도 같이 정리합니다.

이유는 현재 엔티티/게임 모드/데모 부트스트랩이 old schema에 막히지 않게 하기 위해서입니다.

즉 이 initializer는 단순 row delete가 아니라 **현재 product scope와 current schema를 맞추는 compatibility shim**입니다.

### 8-4. `CapitalGameService`는 가장 설명하기 쉬운 첫 확장이다

[CapitalGameService.java](../src/main/java/com/worldmap/game/capital/application/CapitalGameService.java)는 구조적으로 기존 quiz contract를 그대로 따릅니다.

핵심은 아래입니다.

- `startGuestGame`, `startMemberGame`
- `getCurrentState`
- `submitAnswer`
- `restartGame`
- `getSessionResult`

즉 API shape가 기존 위치/인구수 게임과 거의 같습니다.

차이는 stage를 만드는 데이터입니다.

- target country를 고르고
- 수도 4개 보기를 만들고
- 정답 option number를 만든다

현재 구현 기준으로 capital의 세부 규칙도 이미 꽤 많이 고정돼 있습니다.

- 보기 번호는 `1..4`만 허용
- same-continent distractor를 먼저 시도하고 부족하면 global fallback
- 결과 payload는 한국어 수도명을 반환
- stale duplicate submit은 `409`
- stage가 이미 넘어간 뒤의 duplicate correct submit도 `409`
- restart는 새 세션 생성이 아니라 같은 `sessionId` 초기화
- terminal 상태에서만 leaderboard write

### 8-5. capital이 새 contract를 만들지 않는 이유

capital도 답안 제출 흐름에서 아래를 그대로 따릅니다.

- `GameSessionAccessContext`
- `getSessionForUpdate(...)`
- `GameSubmissionGuard.assertFreshSubmission(...)`
- 정답/오답 판정
- terminal 상태면 `leaderboardService.recordCapitalResult(...)`

즉 새 게임이지만 hardening과 terminal write 규칙은 기존 contract를 재사용합니다.

이게 중요한 이유는, 포트폴리오 설명이 "capital만 예외적인 게임"이 아니라 "같은 서버 주도 contract를 다른 콘텐츠로 확장했다"가 되기 때문입니다.

### 8-6. capital 데이터에서 실제로 중요한 것은 `capitalCityKr`

capital 게임은 interaction보다 data quality가 중요합니다.

[sync_capital_city_kr.py](../scripts/sync_capital_city_kr.py)는 `countries.json`의 `capitalCityKr`를 보강합니다.

전략은 이렇습니다.

- Wikidata 한국어 label을 기본 소스로 사용
- 한국어 제품 카피에 맞지 않는 표기는 manual override로 보정
- metadata도 함께 갱신

즉 capital vertical slice의 핵심은 controller보다 **시드 보강 품질**입니다.

### 8-7. `PopulationBattleGameService`는 리듬이 다른 endless run이다

[PopulationBattleGameService.java](../src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameService.java)는 quiz 4-choice 대신 좌우 2-choice를 택합니다.

하지만 상태 기계는 같습니다.

- 시작 시 stage 1 생성
- state 조회
- answer 제출
- 오답이면 하트 감소 + 같은 stage 유지
- 정답이면 다음 stage 생성
- terminal 상태면 `leaderboardService.recordPopulationBattleResult(...)`

즉 이 게임의 핵심 가치는 "새로운 상태 기계"가 아니라 **같은 state machine 위에서 interaction rhythm을 바꾸는 것**입니다.

여기서 실제로 다른 것은 아래입니다.

- prompt는 고정 문장이다: `두 나라 중 인구가 더 많은 나라를 고르세요.`
- option 수는 4개가 아니라 2개다
- difficulty는 stage 번호와 population gap 정책을 따른다
- pair generation은 반복 signature를 피하면서 left/right 비교 리듬을 유지한다

### 8-8. population-battle이 왜 중요한가

location과 population quiz만 있으면,

- 지도 기반 하나
- 4지선다 하나

정도로 보입니다.

population-battle은 여기에

- 비교형 질문
- two-choice quick decision

이라는 다른 템포를 추가합니다.

즉 게임 수를 늘린다기보다 lineup의 감각을 넓혀 줍니다.

### 8-9. `FlagAssetCatalog`는 "국기 게임 시작 전"의 계약을 고정한다

flag는 질문 생성보다 먼저 자산 계약이 있어야 합니다.

[FlagAssetCatalog.java](../src/main/java/com/worldmap/game/flag/application/FlagAssetCatalog.java)는 아래를 검증합니다.

- manifest 존재
- manifest가 비어 있지 않음
- ISO3 코드가 3자리 대문자
- ISO3 중복 없음
- 경로가 `/images/flags/` 아래인지
- 확장자가 `.svg`인지
- format이 `svg`인지
- 실제 정적 파일이 존재하는지

즉 이 클래스는 "국기 게임의 런타임"이 아니라 **국기 자산 계약의 source of truth**입니다.

### 8-10. `FlagQuestionCountryPoolService`는 seed와 asset의 교집합만 playable로 인정한다

[FlagQuestionCountryPoolService.java](../src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolService.java)는 seeded country를 전부 믿지 않습니다.

흐름은 이렇습니다.

1. country seed 전체 로드
2. manifest ISO3가 seed에도 있는지 검증
3. 실제 asset가 있는 country만 `FlagQuestionCountryView`로 변환
4. continent별 count 계산

즉 flag question pool은 "나라 데이터가 있다"가 아니라 "시드와 자산이 모두 준비됐다"일 때만 열립니다.

### 8-11. 현재 flag pool이 왜 36개인가

[flag-assets.json](../src/main/resources/data/flag-assets.json)와 관련 테스트를 보면 현재 지원 자산은 36개입니다.

이 숫자는 우연이 아니라,

- 설명 가능한 범위에서
- 자산을 재생성할 수 있고
- 난이도/대륙 분포를 통제할 수 있는

크기로 잡은 첫 public pool입니다.

테스트가 현재 고정하는 분포도 명확합니다.

- 유럽: `15`
- 아시아: `8`
- 북미: `3`
- 남미: `4`
- 아프리카: `4`
- 오세아니아: `2`

즉 flag는 194개 전부를 무조건 여는 것보다 **설명 가능한 snapshot**을 먼저 고른 셈입니다.

### 8-12. `fetch_flag_assets.py`는 build-time snapshot 도구다

[fetch_flag_assets.py](../scripts/fetch_flag_assets.py)는 flagcdn에서 SVG를 내려받아

- `/static/images/flags/*.svg`
- `flag-assets.json`

를 같이 만듭니다.

즉 국기 게임의 자산 준비는 수동 복붙이 아니라 **스크립트로 재생성 가능한 입력 파이프라인**을 갖습니다.

### 8-13. `FlagGameService`는 asset-backed pool 위에서 endless run을 만든다

[FlagGameService.java](../src/main/java/com/worldmap/game/flag/application/FlagGameService.java)는 시작할 때 [FlagQuestionCountryPoolService.java](../src/main/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolService.java)의 결과를 먼저 읽습니다.

즉 시작 자체가 "출제 가능한 국가가 4개 이상 있는가"에 의존합니다.

여기서 중요한 점은:

- stage 생성이 seed 전체가 아니라 playable pool 위에서 이뤄지고
- 답안 제출/재시작/result/leaderboard 흐름은 다른 게임과 동일하다는 점입니다.

즉 asset 제약은 시작 전 준비에 있고, 게임 상태 기계는 공통 contract를 따릅니다.

### 8-14. flag 난이도는 자산 분포를 전제로 한다

flag는 difficulty policy도 자산 분포와 연결됩니다.

이유는 초기 라운드에서 같은 대륙 distractor를 만들려면 대륙별 충분한 playable country 수가 필요하기 때문입니다.

[FlagGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/flag/FlagGameFlowIntegrationTest.java)는 early round target이 충분한 same-continent distractor를 만들 수 있는 대륙에서 먼저 나오는지 검증합니다.

현재 기준으로 초기 라운드는 같은 대륙 distractor를 안정적으로 만들기 어려운 대륙을 피합니다.

즉 flag의 난이도는 단순 stage number가 아니라 **asset-backed pool의 실제 분포**에 묶여 있습니다.

### 8-15. `HomeController`는 현재 public lineup을 문장으로 고정한다

[HomeController.java](../src/main/java/com/worldmap/web/HomeController.java)는 지금 public에서 바로 시작할 수 있는 카드 목록을 직접 만듭니다.

현재 `modeCards()`에는 아래가 순서대로 있습니다.

- 국가 위치 찾기
- 수도 맞히기
- 국기 보고 나라 맞히기
- 인구 비교 퀵 배틀
- 국가 인구수 맞추기
- 나에게 어울리는 국가 찾기

각 카드는 title만이 아니라 label과 실제 route도 같이 고정합니다.

- 위치: `Mission` / `/games/location/start`
- 수도: `Quiz` / `/games/capital/start`
- 국기: `Quiz` / `/games/flag/start`
- 배틀: `Battle` / `/games/population-battle/start`
- 인구수: `Quiz` / `/games/population/start`
- 추천: `Discover` / `/recommendation/survey`

즉 home은 "현재 product lineup의 선언문" 역할을 합니다.

### 8-16. home copy도 scope reset의 일부다

`entrySteps()`와 `accountNotes()`도 같이 중요합니다.

여기에는 현재 public scope와 account model이 드러납니다.

- 게임 하나를 고른다
- 게스트로 바로 플레이한다
- 기록을 남기고 싶으면 로그인한다
- Stats와 My Page에서 전체 흐름과 내 기록을 본다

즉 home copy는 단순 문구가 아니라 현재 제품 경계 설명입니다.

### 8-17. ranking 실시간성 결정도 lineup과 연결된다

[docs/REALTIME_DELIVERY_DECISION.md](../docs/REALTIME_DELIVERY_DECISION.md)는 현재 랭킹 실시간성을 `SSR + 15초 polling`으로 닫습니다.

이게 왜 여기와 연결되냐면, 새 게임을 더 붙였다고 해서 실시간 전달 방식을 복잡하게 바꾸지는 않았기 때문입니다.

즉 lineup expansion의 핵심은

- 모드 수를 늘리되
- backend state contract를 유지하고
- read model 복잡도는 polling으로 통제하는 것

입니다.

## 9. 요청 흐름 / 상태 변화

### 9-1. startup rollback 흐름

```text
애플리케이션 시작
-> GameLevelRollbackInitializer 조건 확인
-> legacy game_level column/constraint 존재 여부 확인
-> location / population LEVEL_2 row 삭제
-> leaderboard LEVEL_2 row 삭제
-> Redis l2 key 삭제
-> 현재 product scope 기준으로 local/test 정리 완료
```

이 흐름은 사용자 요청이 아니라 startup compatibility 흐름입니다.

### 9-2. home 진입 흐름

```text
Browser
-> GET /
-> HomeController
-> modeCards + entrySteps + accountNotes 생성
-> home.html SSR
```

즉 홈은 현재 public lineup을 먼저 선언하는 SSR surface입니다.

### 9-3. capital 시작과 진행 흐름

```text
GET /games/capital/start
-> start page SSR
-> POST /api/games/capital/sessions
-> CapitalGameService.startGuestGame/startMemberGame
-> session 저장 + stage 1 생성 + IN_PROGRESS
-> GET /api/games/capital/sessions/{id}/state
-> POST /answer
-> 정답이면 다음 stage 생성
-> 오답이면 같은 stage 유지 + 하트 감소
-> terminal이면 leaderboard 기록
```

### 9-4. population-battle 시작과 진행 흐름

```text
GET /games/population-battle/start
-> POST /api/games/population-battle/sessions
-> PopulationBattleGameService.start...
-> 좌/우 비교 stage 생성
-> POST /answer
-> same endless run contract
-> terminal이면 leaderboard 기록
```

### 9-5. flag 시작 전 pool 계산 흐름

```text
flag-assets.json + 실제 svg 파일 + countries.json
-> FlagAssetCatalog
-> FlagQuestionCountryPoolService
-> playable pool 확정
-> FlagGameService.start...
-> stage 1 생성
```

즉 flag는 question generation 전에 playable pool이 먼저 확정됩니다.

### 9-6. 새 게임이 read model에 닿는 흐름

```text
capital / population-battle / flag terminal result
-> LeaderboardService.record...Result(...)
-> leaderboard_record 저장 + Redis sync
-> /ranking, /stats, /mypage read model이 새 게임 결과를 읽음
```

즉 public regrouping은 단순한 화면 편집이 아니라 terminal write 확대의 결과이기도 합니다.

새 게임이 같은 leaderboard write contract를 따르기 때문에 ranking과 stats는 별도 예외 분기 없이 current lineup을 수용합니다.

## 10. 실제로 어디서 상태가 바뀌는가

이 단계에서 상태를 바꾸는 곳은 대략 네 군데입니다.

### 10-1. startup 정리

- [GameLevelRollbackInitializer.java](../src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java)

### 10-2. 신규 게임 세션/스테이지/시도 생성

- [CapitalGameService.java](../src/main/java/com/worldmap/game/capital/application/CapitalGameService.java)
- [PopulationBattleGameService.java](../src/main/java/com/worldmap/game/populationbattle/application/PopulationBattleGameService.java)
- [FlagGameService.java](../src/main/java/com/worldmap/game/flag/application/FlagGameService.java)

### 10-3. terminal leaderboard write

- 각 서비스의 `leaderboardService.record...Result(...)` 호출

### 10-4. home SSR model 선언

- [HomeController.java](../src/main/java/com/worldmap/web/HomeController.java)

여기서 중요한 건,

- startup cleanup은 요청 처리와 분리되고
- 게임 상태 변화는 서비스가 맡고
- 홈은 상태를 바꾸지 않고 현재 product lineup만 선언한다는 점입니다.

## 11. 왜 이 로직이 컨트롤러가 아니라 서비스/초기화 컴포넌트에 있어야 하는가

### 11-1. rollback cleanup은 controller에 있으면 안 된다

legacy row 정리는 사용자가 URL을 호출할 때 실행되면 안 됩니다.

이건 제품 호환성 정리라서 startup 시점, 그것도 조건부 initializer에 있어야 합니다.

### 11-2. 신규 게임의 상태 전이는 service가 맡아야 한다

capital, population-battle, flag 모두

- 현재 stage 확인
- stale submit 방어
- 점수 계산
- 하트 감소
- 다음 stage 생성
- terminal leaderboard 기록

을 수행합니다.

이건 화면 조립이 아니라 game rule입니다.

그래서 controller가 아니라 service에 있어야 합니다.

### 11-3. asset contract도 template가 아니라 application service가 맡아야 한다

flag에서 "이 나라를 출제 가능하다고 볼 수 있는가"는 화면 로직이 아닙니다.

- 자산 manifest
- 실제 정적 파일
- country seed

의 교차 검증이기 때문에 `FlagAssetCatalog`와 `FlagQuestionCountryPoolService`가 맡는 편이 맞습니다.

### 11-4. home lineup 선언은 controller에서 충분하다

반대로 home 카드 목록은 복잡한 domain logic이 아닙니다.

현재 product scope를 SSR model에 내려 주는 선언형 정보 구조라서, `HomeController`가 직접 `ModeCardView`를 만드는 현재 구조가 설명 가능성과 단순성 면에서 맞습니다.

## 12. 실패 케이스 / 예외 처리

### 12-1. Level 2 row가 남아 있는 경우

이 경우 public 설명과 실제 DB 상태가 어긋납니다.

- ranking/stats 해석이 흔들리고
- demo bootstrap이 old schema와 충돌할 수 있습니다.

### 12-2. initializer를 prod에서도 무조건 켜는 경우

이건 운영 startup에서 legacy 호환 코드가 실제 데이터를 건드릴 위험이 있습니다.

현재 구조는 conditional property로 local/test 호환성 조각으로 제한하는 쪽이 맞습니다.

### 12-3. capital 데이터가 부정확한 경우

`capitalCityKr` 품질이 떨어지면 게임은 돌아도 제품 카피 품질이 급격히 무너집니다.

즉 capital은 domain rule보다 seed 품질이 먼저입니다.

### 12-4. flag asset가 일부 누락된 경우

manifest만 있고 실제 파일이 없거나, seed에 없는 ISO3가 manifest에 있으면 출제 pool이 신뢰할 수 없게 됩니다.

그래서 `FlagAssetCatalog`와 `FlagQuestionCountryPoolService`가 시작 전에 막습니다.

### 12-5. 새 게임을 홈에만 추가한 경우

이 경우 제품 설명이 어색해집니다.

- home은 새 게임을 말하는데
- stats/ranking/mypage 설명은 옛 범위에 머무를 수 있습니다.

즉 public regrouping을 끝까지 밀지 않으면 half-migrated product가 됩니다.

### 12-6. shared contract를 깨고 새 게임마다 예외 규칙을 넣는 경우

새 게임마다 상태 전이 규칙이 달라지면,

- 테스트 패턴이 깨지고
- ownership/hardening 설명이 흐려지고
- 포트폴리오 메시지도 약해집니다.

이 단계에서는 "새 규칙"보다 "같은 규칙을 다른 콘텐츠에 확장"하는 편이 중요했습니다.

## 13. 테스트로 검증하기

### 13-1. rollback cleanup 증거

[GameLevelRollbackInitializerIntegrationTest.java](../src/test/java/com/worldmap/common/config/GameLevelRollbackInitializerIntegrationTest.java)는 가장 중요한 증거입니다.

이 테스트는 아래를 실제로 확인합니다.

- `LEVEL_2` location/population row 삭제
- `leaderboard_record`의 `LEVEL_2` row 삭제
- old `game_mode` check constraint 제거
- `game_level NOT NULL` 해제
- Redis `l2` key 제거
- `LEVEL_1` row는 남는지 확인

즉 initializer가 "무조건 다 지운다"가 아니라 현재 scope를 위한 정확한 정리만 한다는 점을 검증합니다.

### 13-2. capital flow 증거

[CapitalGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/capital/CapitalGameFlowIntegrationTest.java)는 capital이 현재 contract 위에 제대로 올라갔는지 보여 줍니다.

대표 검증 포인트는 아래입니다.

- 7 stage 이상 계속 진행
- 한국어 수도명 반환
- 오답 시 같은 stage + 하트 감소
- stale duplicate wrong answer `409`
- duplicate correct answer `409`
- 3번 오답 후 `GAME_OVER`
- 같은 `sessionId` restart
- accessible game-over dialog shell

즉 capital은 단순 quiz 추가가 아니라 **shared hardened loop 위의 새 콘텐츠**라는 점을 테스트가 보여 줍니다.

### 13-3. population-battle flow 증거

[PopulationBattleGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/populationbattle/PopulationBattleGameFlowIntegrationTest.java)도 같은 구조를 확인합니다.

대표 포인트는 아래입니다.

- 2-choice interaction이어도 endless run이 유지되는지
- 오답/재시도/하트 감소
- duplicate stale submit 방어
- terminal game over
- restart 후 같은 session 재사용
- accessible modal shell

즉 interaction rhythm이 달라도 contract가 같다는 점을 증명합니다.

### 13-4. flag asset 증거

[FlagAssetCatalogTest.java](../src/test/java/com/worldmap/game/flag/application/FlagAssetCatalogTest.java)는 현재 snapshot 자산이 36개이고, manifest ISO3가 seed에도 존재함을 검증합니다.

[FlagQuestionCountryPoolServiceIntegrationTest.java](../src/test/java/com/worldmap/game/flag/application/FlagQuestionCountryPoolServiceIntegrationTest.java)는 아래를 검증합니다.

- playable pool = 36개
- continent별 count
- 한국어 나라명과 경로 형식
- ISO3 lookup
- continent filtering

즉 flag는 "그럴듯한 자산 폴더"가 아니라 **실제 검증된 playable pool** 위에 서 있습니다.

### 13-5. flag flow 증거

[FlagGameFlowIntegrationTest.java](../src/test/java/com/worldmap/game/flag/FlagGameFlowIntegrationTest.java)는 flag 게임 contract를 고정합니다.

대표 포인트는 아래입니다.

- 7 stage 이상 진행
- 한국어 나라명 반환
- early round target의 대륙 분포 제어
- stale duplicate 방어
- 3번 오답 후 `GAME_OVER`
- accessible modal shell

즉 flag는 자산 게임이지만 runtime contract는 다른 게임과 동일하게 hardened되어 있습니다.

### 13-6. home regrouping 증거

[HomeControllerTest.java](../src/test/java/com/worldmap/web/HomeControllerTest.java)는 홈이 현재 public lineup을 제대로 선언하는지 보여 줍니다.

이 테스트는 아래를 확인합니다.

- 수도 맞히기 노출
- 국기 보고 나라 맞히기 노출
- 인구 비교 퀵 배틀 노출
- "나에게 어울리는 국가 찾기" 노출
- 예전 "게임 선택하기", "어울리는 나라 추천", old build copy 미노출
- admin이 아닐 때 `Dashboard` 비노출

즉 home도 현재 제품 설명을 고정하는 테스트를 가집니다.
이 테스트 묶음은 `public scope reset + 신규 게임 3종 + 홈 regrouping`의 현재 코드 계약을 강하게 고정합니다.
다만 capital/population-battle/flag 각각의 장기 난이도 밸런싱이나 flag asset pool 확장 로드맵까지 자동 증명하는 것은 아닙니다.
지금 고정되는 것은 어디까지나 "현재 public lineup이 무엇인가"와 "새 게임이 shared contract 위에 올라왔는가"입니다.

## 14. 구현 순서로 다시 만들어 보기

이제 실제로 다시 만든다면 어떤 순서가 좋은지 정리하겠습니다.

### 14-1. 1단계: 현재 public scope를 먼저 문장으로 고정한다

가장 먼저 아래를 적습니다.

- public에서 보여 줄 게임은 무엇인가
- 더 이상 보여 주지 않을 실험 축은 무엇인가
- ranking/stats/home가 바라보는 범위는 무엇인가

이걸 안 하고 코드부터 바꾸면 정리가 아니라 확장만 하게 됩니다.

### 14-2. 2단계: legacy residue를 startup에서 정리한다

그 다음 [GameLevelRollbackInitializer.java](../src/main/java/com/worldmap/common/config/GameLevelRollbackInitializer.java) 같은 호환성 컴포넌트를 만듭니다.

포인트는 아래입니다.

- 요청 처리와 분리
- conditional property
- column/constraint 존재 여부 확인
- row + Redis key 정리

### 14-3. 3단계: capital을 첫 확장으로 올린다

이때 핵심은 capitalCityKr입니다.

- seed 보강
- option generator
- stale submit 방어
- terminal leaderboard write

즉 data와 game contract를 같이 닫습니다.

### 14-4. 4단계: population-battle로 interaction rhythm을 넓힌다

여기서는 같은 endless loop를 유지하되,

- 2-choice stage
- 비교형 prompt

를 추가합니다.

즉 상태 기계를 바꾸지 않고 리듬만 바꾸는 것이 핵심입니다.

### 14-5. 5단계: flag는 asset pipeline부터 만든다

flag는 반드시 아래 순서로 갑니다.

1. asset fetch script
2. manifest
3. asset catalog validation
4. playable pool service
5. flag game runtime

이 순서를 뒤집으면 runtime은 돌아도 출제 pool을 설명하기 어려워집니다.

### 14-6. 6단계: home과 public copy를 현재 lineup에 맞춘다

신규 게임 패키지를 올린 뒤에는 바로 home을 맞춥니다.

- modeCards
- entrySteps
- accountNotes

이 세 군데가 현재 제품 선언문입니다.

### 14-7. 7단계: ranking/stats/mypage가 이미 같은 source를 읽는지 확인한다

새 게임이 terminal leaderboard write를 따르기만 하면,

- ranking
- stats
- mypage

는 기존 read model 구조 안에서 자연스럽게 확장됩니다.

즉 새 게임을 추가할 때 read model 설계를 다시 뜯지 않아도 되는지 확인해야 합니다.

### 14-8. 8단계: 테스트를 "제품 범위 설명" 기준으로 묶는다

최소한 아래 증거가 있어야 합니다.

- rollback cleanup test
- capital flow test
- battle flow test
- flag asset/pool test
- flag flow test
- home regrouping test

이 묶음이 있어야 "public scope reset + lineup expansion"이 말로만 남지 않습니다.

## 15. 실행 / 검증 명령

이 글의 핵심 흐름은 아래 테스트 묶음으로 확인할 수 있습니다.

```bash
./gradlew test \
  --tests com.worldmap.common.config.GameLevelRollbackInitializerIntegrationTest \
  --tests com.worldmap.web.HomeControllerTest \
  --tests com.worldmap.game.capital.CapitalGameFlowIntegrationTest \
  --tests com.worldmap.game.populationbattle.PopulationBattleGameFlowIntegrationTest \
  --tests com.worldmap.game.flag.FlagGameFlowIntegrationTest \
  --tests com.worldmap.game.flag.application.FlagAssetCatalogTest \
  --tests com.worldmap.game.flag.application.FlagQuestionCountryPoolServiceIntegrationTest
```

추가로 자산/시드 파이프라인을 다시 만질 때는 아래도 같이 봅니다.

```bash
python3 scripts/sync_capital_city_kr.py
python3 scripts/fetch_flag_assets.py
```

## 16. 이 단계에서 얻는 산출물

이 단계가 끝나면 아래를 말할 수 있어야 합니다.

- public 제품은 5게임 라인업으로 선명하게 설명된다
- Level 2 실험 흔적은 현재 product scope에서 걷어냈다
- 새 게임도 같은 endless run contract 위에 올라간다
- 홈과 public copy가 새 lineup과 맞는다
- ranking/stats/mypage가 새 모드를 자연스럽게 수용한다

## 17. 현재 구현의 한계

이 단계가 끝났다고 해서 모든 확장이 끝난 것은 아닙니다.

### 17-1. flag 자산 수는 여전히 제한적이다

현재 asset snapshot은 36개입니다.

즉 전체 세계 국기를 다 다루는 단계는 아직 아닙니다.

### 17-2. 게임별 세밀한 난이도 확장은 남아 있다

현재는 Level 1-only public lineup을 먼저 선명하게 만드는 것이 목표였습니다.

즉 capital, battle, flag 각각의 더 세밀한 난이도 설계는 다음 단계입니다.

### 17-3. rollback initializer는 영구 기능이 아니라 호환성 조각이다

이 코드는 예전 실험 흔적을 정리하는 단계적 shim입니다.

장기적으로는 운영 migration 전략과 함께 더 정리될 수 있습니다.

### 17-4. 신규 게임 3종은 current public lineup 기준의 첫 vertical slice다

현재 테스트는 shared contract와 public surface 편입을 강하게 고정하지만,
각 게임의 더 세밀한 난이도 단계, 더 큰 자산 풀, 더 공격적인 콘텐츠 확장까지 닫은 것은 아니다.

## 18. 취업 포인트

### 18-1. 1문장 답변

WorldMap은 public scope를 Level 1-only 기준으로 다시 닫은 뒤, capital·population-battle·flag를 기존 endless run contract 위에 올려 5게임 라인업을 설명 가능하게 확장했습니다.

### 18-2. 30초 답변

새 게임 3개를 그냥 추가한 게 아니라, 먼저 위치/인구수의 Level 2 실험 흔적을 현재 public 제품 설명에서 걷어냈습니다. `GameLevelRollbackInitializer`로 legacy DB/Redis 흔적과 old constraint를 startup에서 정리하고, 그 다음 capital, population-battle, flag를 기존 `Session / Stage / Attempt` loop 위에 올렸습니다. 특히 flag는 `FlagAssetCatalog`와 `FlagQuestionCountryPoolService`로 자산과 seed의 교집합만 playable pool로 인정하게 만들어서, 홈과 랭킹, stats까지 포함한 public lineup 전체를 한 문장으로 설명할 수 있게 만들었습니다.

### 18-3. 예상 꼬리 질문

- 왜 기능을 더하기 전에 rollback을 먼저 했나요?
- 왜 Level 2를 화면에서만 숨기면 안 되나요?
- 왜 capital부터 여는 것이 battle이나 flag보다 쉬웠나요?
- 왜 flag는 자산 pipeline이 먼저였나요?
- 새 게임을 추가할 때 home과 ranking/stats는 어떻게 같이 맞췄나요?

## 19. 글 종료 체크포인트

이 글을 다 읽은 뒤 아래 질문에 답할 수 있어야 합니다.

- 왜 product scope reset이 신규 게임 추가만큼 중요한가
- 왜 startup initializer가 현재 제품 설명을 보호하는 역할을 하는가
- 왜 capital, population-battle, flag를 각각 완전히 다른 시스템으로 만들지 않았는가
- 왜 flag는 asset contract를 먼저 고정해야 하는가
- 왜 home, ranking, stats까지 같이 바뀌어야 진짜 lineup expansion이라고 말할 수 있는가

## 20. 자주 막히는 지점

### 20-1. "기능을 더했으니 진전"이라고만 보는 것

이 단계는 오히려 무언가를 걷어내는 작업이 더 중요했습니다.

### 20-2. UI만 바꾸고 데이터는 그대로 두는 것

이러면 README와 실제 DB/Redis가 서로 다른 제품을 설명하게 됩니다.

### 20-3. 신규 게임마다 별도 상태 기계를 만들려는 것

그렇게 하면 lineup은 늘어도 설명 가능성은 오히려 떨어집니다.

### 20-4. flag에서 자산 검증을 뒤로 미루는 것

이 경우 runtime은 돌아도 "무엇을 출제 가능한가"를 설명하지 못합니다.

### 20-5. home 카피를 단순 디자인 문제로 보는 것

home의 카드와 안내 문구는 현재 public product 선언입니다.

즉 이 단계에서 home은 cosmetic surface가 아니라 product scope의 front page입니다.

## 21. 다음 글로 넘어가기 전에

다음 글은 [16-production-runtime-redis-session-and-ecs-deploy-prep.md](./16-production-runtime-redis-session-and-ecs-deploy-prep.md)입니다.

넘어가기 전에 아래를 스스로 설명해 보세요.

1. 지금 public lineup은 몇 개 게임인가
2. `GameLevelRollbackInitializer`는 요청 처리 코드인가, startup 호환성 코드인가
3. capital / battle / flag 중 어떤 것이 data-first, interaction-first, asset-first 확장인가
4. 새 게임이 ranking과 stats에 자연스럽게 들어가려면 어떤 공통 contract를 따라야 하는가
