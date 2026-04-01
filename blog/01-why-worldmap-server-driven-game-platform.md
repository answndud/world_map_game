# [Spring Boot 게임 플랫폼 포트폴리오] 01. 왜 WorldMap을 서버 주도 게임 플랫폼으로 잡았는가

## 1. 이번 글에서 풀 문제

WorldMap은 겉으로 보면 “나라 맞히기 게임 사이트”다.
그런데 포트폴리오로 설명할 때는 왜 `서버 주도 게임 플랫폼`이라고 말해야 할까?

이 질문에 답하지 못하면, 이후의 세션 구조, 랭킹, 추천, 인증, 검증 레일이 전부 산발적인 기능처럼 보인다.

## 2. 먼저 알아둘 개념

### 2-1. 서버 주도 게임

입력은 브라우저가 받아도, 문제 생성, 정답 판정, 점수 계산, 상태 전이는 서버가 맡는 구조다.

### 2-2. read model

게임 세션과 운영 데이터는 그대로 화면에 보여 주기 어렵다.
그래서 `/ranking`, `/mypage`, `/stats`, `/dashboard` 같은 읽기 전용 모델이 별도로 필요하다.

### 2-3. deterministic recommendation

추천은 런타임 AI 호출보다, 같은 입력에 같은 결과를 주는 서버 계산 구조가 더 설명 가능하다.

### 2-4. production-ready verification

기능 구현이 끝났다고 제품이 끝나는 건 아니다.
브라우저 smoke, public URL smoke, verify workflow까지 있어야 “믿을 수 있다”를 말할 수 있다.

## 3. 이번 글에서 다룰 파일

- [README.md](../README.md)
- [HomeController.java](../src/main/java/com/worldmap/web/HomeController.java)
- [BaseGameSession.java](../src/main/java/com/worldmap/game/common/domain/BaseGameSession.java)
- [LeaderboardRecord.java](../src/main/java/com/worldmap/ranking/domain/LeaderboardRecord.java)
- [RecommendationSurveyService.java](../src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
- [MyPageService.java](../src/main/java/com/worldmap/mypage/application/MyPageService.java)

## 4. 설계 구상

### 왜 이 구조가 좋은가

WorldMap은 CRUD 포트폴리오가 보여 주지 못하는 문제를 보여 준다.

- 상태 전이가 있는 세션 설계
- 클라이언트 입력을 신뢰하지 않는 정답 판정
- 결과를 읽기 좋은 read model로 다시 푸는 구조
- deterministic recommendation과 feedback loop
- production-ready 무결성과 verification pipeline

즉, “예쁜 게임 화면”보다 “상태를 다루는 서버 시스템”으로 설명하는 편이 가치가 크다.

## 5. 코드 설명

### 5-1. `HomeController`: 이 프로젝트의 public surface 시작점

홈은 단순한 정적 소개 페이지가 아니다.
현재 public 게임 5종, 추천, 랭킹, 인증 진입점을 한곳에 묶는 허브다.

### 5-2. `BaseGameSession`: 게임을 서비스로 보는 핵심 단서

이 클래스가 있다는 건 WorldMap을 단순 페이지 묶음이 아니라 “상태를 가진 시스템”으로 봐야 한다는 뜻이다.

### 5-3. `LeaderboardRecord`: 결과는 그대로 노출하지 않고 read model로 다시 만든다

게임 세션이 끝나면 별도 row로 요약된다는 사실이, 이 프로젝트의 read model 축을 보여 준다.

### 5-4. `RecommendationSurveyService`: 추천도 서버 계산이다

추천을 runtime LLM이 아니라 deterministic engine으로 처리한다는 점이, 이 프로젝트의 설명 가능성을 크게 올린다.

### 5-5. `MyPageService`: 제품은 게임만으로 끝나지 않는다

유저별 기록 허브가 있다는 것은 session, leaderboard, ownership이 모두 서로 연결된다는 뜻이다.

## 6. 실제 흐름

```text
GET /
-> HomeController
-> public 게임/추천/랭킹/auth 진입

게임 시작
-> 서버가 session/stage/attempt 관리
-> terminal run 생성
-> leaderboard / mypage / stats / dashboard read model로 이어짐
```

즉, 홈은 UI 허브고, 실제 제품 중심축은 서버 쪽 도메인과 read model이다.

## 7. 테스트로 검증하기

이 글 자체는 철학 글에 가깝지만, 아래를 기준으로 제품 축을 빠르게 확인할 수 있다.

```bash
./gradlew test \
  --tests com.worldmap.web.HomeControllerTest \
  --tests com.worldmap.game.location.LocationGameFlowIntegrationTest \
  --tests com.worldmap.ranking.LeaderboardIntegrationTest \
  --tests com.worldmap.recommendation.RecommendationPageIntegrationTest
```

이 네 테스트가 자동으로 고정하는 것은 "WorldMap을 서버 주도 플랫폼으로 설명할 실체가 현재 저장소에 존재한다"는 점입니다.
반대로 이 테스트만으로 `인증`, `운영 화면`, `production-ready verification` 전체가 한 번에 증명되는 것은 아닙니다.
그 범위는 뒤 글에서 각각 더 구체적인 테스트와 운영 절차로 나눠 닫습니다.

## 8. 회고

이 프로젝트는 처음부터 “화면이 먼저”가 아니라 “서버 상태와 결과 설명이 먼저”라는 기준을 잡았을 때 비로소 일관성이 생겼다.

### 현재 구현의 한계

- 실시간 경쟁 게임이나 멀티플레이는 아직 범위 밖이다.
- 추천은 deterministic engine 중심이고, 런타임 생성형 AI 기능은 아직 없다.
- 이 글의 검증은 대표 축을 빠르게 확인하는 수준이지, 전체 제품 범위를 전부 자동 고정하는 테스트 맵은 아니다.

## 9. 취업 포인트

### 9-1. 1문장 답변

WorldMap은 게임 UI를 보여 주는 사이트가 아니라, 세션 상태와 결과를 서버가 끝까지 책임지는 Spring Boot 게임 플랫폼 포트폴리오입니다.

### 9-2. 30초 답변

이 프로젝트는 위치 찾기, 수도 맞히기, 인구수 퀴즈, 인구 비교 배틀, 국기 퀴즈를 제공하는 서버 주도 게임 플랫폼입니다. 핵심은 브라우저가 아니라 서버가 문제 생성, 정답 판정, 점수 계산, 진행 상태를 관리한다는 점입니다. 게임 결과는 Redis leaderboard, `/mypage`, `/stats`, `/dashboard` 같은 read model로 다시 풀고, 추천도 deterministic engine으로 처리해서 구조와 테스트를 설명하기 쉽게 만들었습니다.

### 9-3. 예상 꼬리 질문

- 왜 React SPA보다 SSR + API 혼합 구조를 골랐나요?
- 왜 게임 규칙을 프런트로 보내지 않았나요?
- 왜 추천을 런타임 LLM 호출이 아니라 deterministic engine으로 만들었나요?

## 10. 시작 상태

- Spring Boot 프로젝트가 아직 없거나
- 프로젝트는 있어도 제품 포지셔닝이 불분명한 상태

## 11. 이번 글에서 바뀌는 파일

- `README.md`
- `blog/README.md`
- 이 글 자체

## 12. 구현 체크리스트

- 제품 범위를 1문장으로 설명할 수 있는지 확인
- public 기능과 read model surface를 분리해서 적기
- 서버 책임과 브라우저 책임을 나눠 설명하기
- 이후 시리즈의 순서를 정하기

## 13. 실행 / 검증 명령

```bash
./gradlew test --tests com.worldmap.web.HomeControllerTest
```

## 14. 산출물 체크리스트

- 프로젝트 한 줄 소개가 명확하다
- public 기능 범위가 정리돼 있다
- 이후 글 순서를 왜 그렇게 잡는지 설명할 수 있다

## 15. 글 종료 체크포인트

- 왜 WorldMap을 서버 주도 게임 플랫폼으로 봐야 하는가?
- 이후 글에서 왜 game loop, leaderboard, recommendation, auth, verification이 중요한가?

## 16. 자주 막히는 지점

- “게임이라서 프런트 프로젝트”라고 먼저 생각하는 것
- 추천을 AI 기능 데모 정도로 축소해서 보는 것
- read model과 운영 surface의 존재 이유를 나중 문제로 미루는 것
