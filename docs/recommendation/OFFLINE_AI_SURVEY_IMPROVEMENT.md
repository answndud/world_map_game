# 오프라인 AI-assisted 설문 개선 루프

## 목적

이 문서는 WorldMap 추천 기능에서 `런타임 LLM 호출을 쓰지 않고`, 개발 단계에서만 AI와 서브 에이전트로 설문 품질을 개선하는 운영 기준을 정리한다.

핵심 원칙은 두 가지다.

1. 서비스 런타임의 추천 결과 계산은 항상 서버가 deterministic하게 수행한다.
2. AI는 설문 문항, 평가 시나리오, 개선 아이디어를 더 빠르게 만드는 오프라인 도구로만 사용한다.

## 왜 런타임 LLM을 빼는가

- 추천 결과마다 외부 API를 호출하면 과금이 계속 발생한다.
- 같은 입력에 같은 결과가 나온다는 보장이 약해진다.
- 추천 품질 문제를 `서버 로직`이 아니라 `모델 응답` 탓으로 돌리기 쉬워진다.
- 포트폴리오 설명에서 핵심 백엔드 설계보다 외부 모델 의존성이 더 커진다.

그래서 현재 추천 기능은 아래처럼 나눈다.

- 서비스 런타임
  - `RecommendationQuestionCatalog`
  - `RecommendationCountryProfileCatalog`
  - `RecommendationSurveyService`
  - `RecommendationFeedback`
- 오프라인 개선 루프
  - AI가 설문 문항 후보 생성
  - AI가 문항 중복/모호성 비판
  - AI가 페르소나 시나리오 세트 생성
  - 실제 만족도 데이터와 함께 다음 버전 반영

## 기본 산출물

설문 개선 작업을 할 때는 아래 자산을 같이 본다.

- `/Users/alex/project/worldmap/docs/recommendation/PERSONA_EVAL_SET.md`
  - 오프라인 평가 시나리오
- `surveyVersion`
  - 현재 문항/선택지 버전
- `engineVersion`
  - 현재 점수식/보조 정렬 버전
- `/Users/alex/project/worldmap/src/main/resources/templates/recommendation/survey.html`
  - 실제 사용자 입력 화면
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
  - 실제 서버 계산 규칙
- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java`
  - 만족도 집계 기준

## 권장 서브 에이전트 역할

### 1. survey-drafter

- 책임: 새 설문 문항 후보, 선택지 문구, helper text 초안 생성
- 출력: 문항 후보 목록, 중복 제거 전 초안

### 2. survey-critic

- 책임: 모호한 질문, 겹치는 선택지, 지나치게 유도적인 표현 지적
- 출력: 수정 필요 포인트와 이유

### 3. persona-generator

- 책임: 다양한 생활 조건과 기대 국가군을 가진 평가 시나리오 생성
- 출력: `PERAONA_EVAL_SET`에 들어갈 페르소나 표 초안

### 4. eval-reviewer

- 책임: 서버 추천 결과와 기대 후보를 비교하고, 어떤 버전이 더 낫다고 볼지 정리
- 출력: 버전 비교 메모, 낮은 만족도 케이스 분석

중요한 점은, 서브 에이전트가 최종 결정을 내리지 않는다는 것이다.

최종 판단은 항상 사람이 한다.

## 실제 개선 루프

1. 현재 `surveyVersion`, `engineVersion`을 고정한다.
2. 만족도 집계 화면에서 낮은 평균 점수 또는 낮은 점수 분포를 확인한다.
3. 낮은 점수를 자주 만든 답변 조합을 고른다.
4. `persona-generator`가 비슷한 시나리오를 더 만든다.
5. `survey-drafter`가 문항/선택지 개선안을 제안한다.
6. `survey-critic`가 모호성, 중복, 과도한 유도 문구를 비판한다.
7. 사람이 다음 `surveyVersion` 초안을 확정한다.
8. 서버 점수식과 실제 만족도 집계를 다시 비교한다.

## 버전 관리 규칙

- 문항/선택지가 바뀌면 `surveyVersion`을 올린다.
- 점수식/정렬 규칙이 바뀌면 `engineVersion`을 올린다.
- 둘 다 바뀌면 두 버전을 함께 올린다.
- 실험 중인 안은 운영 반영 전에 `PERSONA_EVAL_SET`로 먼저 본다.

## 지금 단계에서 하지 않는 것

- 런타임 LLM 설명 생성
- 추천 결과 자체 저장
- 사용자별 장기 추천 이력 관리
- 자동으로 설문을 바꾸는 self-modifying system

지금은 어디까지나 `사람 검수 + AI 보조 + 서버 결정 로직 유지`가 원칙이다.

## 면접에서 설명하는 방법

“추천 기능은 외부 LLM을 호출해 실시간으로 설명을 만들지 않고, 서버가 설문을 deterministic하게 계산하도록 유지했습니다. 대신 AI는 개발 단계에서만 써서 문항 후보를 만들고, 페르소나 평가 시나리오를 생성하고, 낮은 만족도 케이스를 분석하는 데 활용했습니다. 그래서 과금과 비결정성은 줄이고, 설문 품질 개선 속도만 가져갔습니다.”
