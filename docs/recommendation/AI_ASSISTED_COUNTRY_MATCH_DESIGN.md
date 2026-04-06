# AI와 함께 `나에게 어울리는 국가 찾기`를 설계한 과정

## 왜 이 기능을 만들었나

WorldMap을 처음 구상할 때부터, 이 프로젝트를 단순한 지리 퀴즈 묶음으로 끝내고 싶지는 않았다.

- 위치를 찾는 게임
- 짧게 반복하는 퀴즈
- 기록과 랭킹

여기까지는 비교적 익숙한 흐름이다.

그 다음에 넣고 싶었던 것은, 정답을 맞히는 감각과는 전혀 다른 종류의 몰입이었다.
그래서 “내가 실제로 살아 보고 싶은 나라는 어디일까?”라는 질문을 제품 안으로 끌어왔다.

이 기능의 목표는 여행 MBTI처럼 가볍게 끝나는 테스트가 아니라,

- 사용자가 자기 생활 조건을 다시 정리해 보게 만들고
- 왜 이 나라가 나왔는지 나중에 설명할 수 있고
- 운영자가 버전을 바꾸며 계속 튜닝할 수 있는

`제품 기능`을 만드는 것이었다.

## 처음부터 AI에게 추천을 맡기지 않은 이유

이 기능에서 AI를 가장 많이 썼다고 해서, 런타임 추천 결과를 LLM이 직접 만들게 하지는 않았다.

그 이유는 명확했다.

1. 같은 답변에 같은 결과가 나와야 했다.
2. 추천이 이상할 때, 원인을 문항/프로필/점수식 중 어디에서 찾아야 할지 분명해야 했다.
3. 포트폴리오로 설명할 때 “모델이 그렇게 답했다”가 아니라 “왜 이 규칙을 선택했는가”를 말할 수 있어야 했다.

그래서 현재 서비스 런타임의 추천 결과는 아래 기준으로 고정했다.

- 설문 버전: `survey-v4`
- 엔진 버전: `engine-v20`
- 질문 구조: [RecommendationQuestionCatalog.java](../../src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java)
- 국가 프로필 기준: [RecommendationCountryProfileCatalog.java](../../src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java)
- 서버 계산: [RecommendationSurveyService.java](../../src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
- 만족도 수집: [RecommendationFeedbackService.java](../../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java)

즉, AI는 `서비스 런타임 추천기`가 아니라 `설계와 개선을 가속하는 파트너`로 썼다.

## AI를 어디에 썼는가

### 1. 질문 축을 넓히는 데 썼다

처음의 설문은 아주 얕았다.

- 따뜻한 나라를 좋아하는가
- 도시를 좋아하는가
- 생활비가 중요한가

이 정도로는 결과가 쉽게 뭉개졌다.

그래서 AI를 이용해 아래 질문을 반복했다.

- 이 질문 세트로는 어떤 사람이 서로 구분되지 않는가
- 실제 이주나 장기 체류를 생각할 때 빠진 축이 무엇인가
- 서로 충돌하는 취향을 어떤 질문으로 드러낼 수 있는가
- 질문이 너무 유도적이거나 중복되는 지점은 어디인가

이 과정에서 단순 선호 조사가 아니라, 생활 선택과 정착 기준으로 질문 축을 넓혔다.

지금 설문은 아래처럼 정리돼 있다.

- 기후 방향
- 계절 변화 선호
- 기후 적응 성향
- 생활 리듬
- 인구 밀도 감수성
- 비용 대비 품질 기준
- 주거 공간 vs 중심 접근성
- 도시 편의 vs 자연 접근성
- 이동 방식
- 영어 지원 필요도
- 초기 정착 친화도
- 치안 / 공공서비스 우선도
- 디지털 편의
- 음식 / 다양성 / 문화 생활
- 일과 삶의 균형
- 장기 정착과 미래 기반

결과적으로 지금의 20문항은 “질문 수를 늘렸다”보다,
`생활 조건을 비교 가능한 축으로 정리했다`는 쪽이 더 정확하다.

### 2. 국가 프로필 비교축을 잡는 데 썼다

질문만 늘리면 좋은 추천이 되지 않는다.
각 나라를 어떤 기준으로 비교할지도 같이 정의해야 했다.

여기서 AI는 아래 역할에 특히 유용했다.

- 어떤 속성 조합이 추천 결과 설명에 도움이 되는지 후보를 넓히기
- 비슷해 보이는 나라를 어떤 축으로 구분해야 하는지 가설 세우기
- 지금 프로필 정의로는 왜 특정 나라가 항상 과하게 올라오는지 점검하기

그 뒤 최종적으로는 사람이 직접 정리해서,
현재는 `30개 국가`를 고정된 프로필 집합으로 관리한다.

기준은 대략 다음과 같다.

- 기후
- 생활 속도
- 물가 감각
- 주거 성향
- 이동성
- 영어/정착 친화도
- 치안
- 공공서비스
- 디지털 환경
- 음식
- 다양성
- 문화 생활
- 장기 정착성

이 기준은 [RecommendationCountryProfile.java](../../src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfile.java)와
[RecommendationCountryProfileCatalog.java](../../src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java)에 고정돼 있다.

## AI가 가장 유용했던 지점: “좋은 질문”을 만드는 과정

개인적으로 이 기능에서 가장 큰 어려움은 점수식보다 질문 자체였다.

추천 결과가 애매할 때 문제는 보통 세 가지 중 하나였다.

1. 질문이 너무 넓어서 서로 다른 사람이 같은 답을 고른다.
2. 질문은 괜찮지만 국가 프로필 차이가 너무 약하다.
3. 질문과 프로필은 맞는데, 점수식이 특정 축을 과대평가한다.

AI는 여기서 빠르게 비교안을 만드는 데 강했다.

예를 들어:

- 한 문항을 둘로 나누면 어떤 사용자군이 더 잘 갈리는지
- helper text를 줄이면 답변 의도가 더 분명해지는지
- “도시 선호”와 “생활 속도”가 사실상 같은 질문처럼 겹치지 않는지
- “영어 필요도”와 “정착 친화도”가 어떤 경우에는 다른 결론을 만들어야 하는지

이런 가설을 빠르게 늘리고 줄이는 데 AI가 유용했다.

하지만 최종 판단은 항상 사람이 했다.

- 실제 제품 질문으로 채택할지
- 어떤 문항을 삭제할지
- helper text를 얼마나 노출할지
- 사용자가 부담 없이 끝까지 답할 길이인지

이런 결정은 직접 보면서 정리했다.

## 점수 계산과 결과 문장은 왜 규칙 기반으로 남겼나

AI와 함께 질문과 프로필을 설계했다고 해서, 결과 자체를 생성형으로 만들 필요는 없었다.

오히려 이 프로젝트에서는 그 반대가 중요했다.

- 추천 결과는 반복해서 검증할 수 있어야 하고
- 낮은 만족도 케이스를 다음 버전 개선 근거로 삼을 수 있어야 하고
- 운영자가 “왜 이 나라가 1위인지”를 설명할 수 있어야 한다

그래서 점수 계산과 보정 규칙은 [RecommendationSurveyService.java](../../src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)에 남겼다.

현재 구조는 이렇게 읽는 편이 맞다.

- AI: 질문/프로필/비교축/가설 탐색을 빠르게 넓힘
- 사람: 제품 문항, 국가 집합, 보너스/패널티, 결과 문장을 최종 결정
- 서버: 버전 고정된 규칙으로 결과 계산

## 만족도와 운영 화면을 왜 같이 만들었나

추천 기능은 “한 번 그럴듯하게 보인다”에서 끝나면 금방 설명 가치가 떨어진다.

그래서 이 기능은 처음부터 다음 개선 루프까지 염두에 두고 만들었다.

- 결과 페이지에서 만족도 수집
- feedback token으로 현재 결과와만 연결
- hidden field 신뢰 대신 session store 기준으로 복원
- 운영 화면에서 버전별 만족도와 분포 확인
- persona baseline으로 대표 시나리오를 반복 확인

관련 코드는 아래를 기준으로 보면 된다.

- [RecommendationFeedbackSessionStore.java](../../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackSessionStore.java)
- [RecommendationFeedback.java](../../src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java)
- [RecommendationFeedbackService.java](../../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java)
- [RecommendationPersonaBaselineCatalog.java](../../src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineCatalog.java)

그리고 오프라인 개선 기준은 기존 문서에도 정리해 두었다.

- [OFFLINE_AI_SURVEY_IMPROVEMENT.md](OFFLINE_AI_SURVEY_IMPROVEMENT.md)
- [PERSONA_EVAL_SET.md](PERSONA_EVAL_SET.md)

## 이 기능에서 AI를 “뽐낼 수 있는” 이유

이 기능이 단순히 “AI 써서 문항 좀 만들었다” 수준이면 README에 따로 링크할 가치가 없다.

내가 이 기능을 보여 주고 싶은 이유는, AI를 아래처럼 썼기 때문이다.

- 막연한 아이디어를 질문 구조로 정리했다
- 질문과 국가 프로필을 제품 규칙으로 고정했다
- 페르소나와 만족도 데이터를 통해 다음 버전을 설계하게 만들었다
- 결국 AI 사용을 코드, 테스트, 운영 화면, 문서로 다시 설명 가능한 결과물로 닫았다

즉, AI는 이 프로젝트에서 단순한 보조 작성기가 아니라
`제품 기획 실험의 속도를 올리는 도구`로 사용했다.

하지만 결과는 항상 사람이 책임지는 구조로 마감했다.

## 면접에서 짧게 설명하면

“추천 기능은 런타임 LLM 추천으로 만들지 않았습니다. 대신 AI를 설계 파트너처럼 사용해서 질문 축, 국가 프로필, 페르소나 시나리오를 빠르게 탐색했고, 최종 결과는 사람이 정한 규칙과 서버 코드로 고정했습니다. 그래서 AI의 탐색 속도는 가져가면서도, 추천 결과는 버전과 테스트가 있는 제품 기능으로 설명할 수 있습니다.”
