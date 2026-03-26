# 아키텍처, ERD, 요청 흐름, 발표 자료를 한 번에 정리하기

## 왜 이 조각이 필요했는가

기능이 거의 다 닫힌 뒤에는 "무엇을 더 만들까"보다
"지금 있는 것을 어떻게 설명할까"가 더 중요해진다.

그래서 이번 조각의 목표는

1. 현재 시스템 구성을 한 장으로 설명할 수 있게 만들고
2. 핵심 테이블 관계를 ERD로 고정하고
3. 대표 요청 흐름을 시퀀스로 정리하고
4. 3분 소개 / 10분 발표 / 예상 질문까지 한 번에 묶는 것이었다.

## 이번에 만든 문서

### 1. 아키텍처 개요

[docs/ARCHITECTURE_OVERVIEW.md](/Users/alex/project/worldmap/docs/ARCHITECTURE_OVERVIEW.md)

이 문서는

- 브라우저
- Spring Boot
- PostgreSQL
- Redis
- 추천 / 랭킹 / 계정 / dashboard read model

구성이 어떻게 나뉘는지 한 장으로 보여 준다.

### 2. ERD

[docs/ERD.md](/Users/alex/project/worldmap/docs/ERD.md)

여기서는 현재 설명에 꼭 필요한 핵심 테이블만 남겼다.

- `country`
- `member_account`
- 위치/인구수 `session -> stage -> attempt`
- `leaderboard_record`
- `recommendation_feedback`

핵심은 "게임 한 판", "문제 하나", "시도 하나"를 분리한 이유를 설명할 수 있게 만드는 것이다.

### 3. 대표 요청 흐름

[docs/REQUEST_FLOW_GUIDE.md](/Users/alex/project/worldmap/docs/REQUEST_FLOW_GUIDE.md)

현재 프로젝트를 설명할 때 가장 중요한 3개 흐름만 골랐다.

- 위치 찾기 게임 한 판
- guest -> login -> 기록 귀속
- 추천 설문 -> 만족도 피드백 -> dashboard review

### 4. 발표 준비 노트

[docs/PRESENTATION_PREP.md](/Users/alex/project/worldmap/docs/PRESENTATION_PREP.md)

이 문서에는

- 3분 소개 스크립트
- 10분 기술 설명 아웃라인
- 예상 질문과 답변
- 발표 직전 체크리스트

를 넣었다.

## 왜 이 문서들이 중요한가

포트폴리오는 코드만으로 끝나지 않는다.
면접에서는 결국 아래를 짧게 설명해야 한다.

- 이 프로젝트는 무엇인가
- 왜 이런 구조를 골랐는가
- 상태는 어디서 바뀌는가
- 어떤 저장소가 어떤 책임을 가지는가
- 추천과 랭킹은 왜 이렇게 설계했는가

그래서 이번 조각은 기능 추가가 아니라 `설명 가능한 구조를 문서로 고정하는 작업`이었다.

## README와 플레이북도 같이 바꾼 이유

문서가 흩어져 있으면 오히려 더 설명이 어려워진다.

그래서 이번에는

- [README.md](/Users/alex/project/worldmap/README.md)에 발표용 문서 세트 링크를 추가하고
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)에서 9단계를 닫고 10단계를 `In Progress`로 열었다.

즉, 문서도 현재 단계 상태를 같이 반영하도록 맞췄다.

## 면접에서는 어떻게 설명할 것인가

> 기능 구현이 어느 정도 닫힌 뒤에는, 현재 시스템을 설명할 수 있는 문서 세트를 별도로 만들었습니다. 아키텍처 개요, ERD, 대표 요청 흐름, 발표 스크립트를 분리해서 정리해 두면, 코드와 문서를 오가며 설명하기 쉬워지고 실제 면접에서도 답변 구조가 안정됩니다.
