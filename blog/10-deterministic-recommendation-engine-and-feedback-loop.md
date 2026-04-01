# [Spring Boot 게임 플랫폼 포트폴리오] 10. deterministic recommendation engine과 feedback loop를 어떻게 만들었는가

## 1. 이번 글에서 풀 문제

추천 기능은 데모 영상에서는 가장 화려해 보이지만, 포트폴리오에서는 가장 설명하기 어려운 기능이 될 수 있습니다.  
특히 "왜 이 나라가 나왔는가"와 "추천 품질을 어떻게 계속 개선하는가"를 답하지 못하면, 추천은 강점이 아니라 약점이 됩니다.

WorldMap에서 추천 기능은 아래 문제를 먼저 닫아야 했습니다.

- 왜 런타임 LLM 호출이 아니라 deterministic scoring engine을 먼저 만들었는가
- 왜 설문 문항과 국가 프로필을 프런트 상수나 DB row가 아니라 **서버 catalog**로 고정했는가
- 왜 추천 결과 top 3를 영구 저장하지 않고, `surveyVersion + engineVersion + 20개 답변`만 **feedback context**로 잠깐 보관하는가
- 왜 feedback 수집만으로는 부족하고, **offline persona baseline**과 **admin ops review**가 같이 있어야 하는가
- 왜 recommendation도 결국 `/ranking`처럼 **설명 가능한 서버 계약**이어야 하는가

즉, 이 글은 단순히 "나라 추천 페이지를 만들었다"가 아니라 **재현 가능한 scoring engine + 안전한 feedback 저장 + 운영 개선 루프**를 설명합니다.

## 2. 최종 도착 상태

이 글이 끝났을 때 recommendation 파트는 아래 상태여야 합니다.

- `GET /recommendation/survey`가 20문항 설문을 SSR로 렌더링한다
- 설문 입력은 [RecommendationSurveyForm.java](../src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java)의 20개 enum 필드로 서버에 바인딩된다
- 같은 답변을 넣으면 [RecommendationSurveyService.java](../src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)가 항상 같은 top 3 국가를 계산한다
- 현재 버전 상수는 `survey-v4`, `engine-v20`이고, 버전 source of truth는 catalog가 아니라 service다
- 추천 후보는 [RecommendationCountryProfileCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java)의 30개 profile이지만, 실제 결과는 `CountryRepository.findAllByOrderByNameKrAsc()`와 join된 국가만 사용한다
- 결과 페이지는 top 3 국가, headline, top reasons, 20개 선호 요약을 보여주되 내부 버전 문자열은 노출하지 않는다
- 결과 페이지 렌더링 시 session에 UUID feedback token과 `surveyVersion + engineVersion + 20개 답변` context가 저장된다
- `POST /api/recommendation/feedback`는 `feedbackToken + satisfactionScore`만 받고, 실제 survey/engine/answers는 session context에서 복원한다
- feedback token은 **한 번만 consume**되며, 재사용하거나 위조하면 `400`이 난다
- feedback row는 `recommendation_feedback` 테이블에 만족도 점수와 20개 답변 snapshot까지 함께 저장된다
- 운영자는 `/dashboard/recommendation/feedback`과 `/dashboard/recommendation/persona-baseline`에서 현재 버전 응답 수, 평균 만족도, weak scenario, anchor drift, active-signal 시나리오를 같이 본다
- offline test는 `18개 persona`, `coverage >= 15`, `exact top 3 snapshot`, `30개 profile pool seed 연동`을 고정한다

즉, recommendation의 최종 상태는 "추천 결과가 나온다"가 아니라 **추천 계산, 피드백 저장, 품질 회귀 방지, 운영 판단까지 같은 계약으로 설명 가능하다**는 것입니다.

## 3. 먼저 알아둘 개념

### 3-1. deterministic engine

같은 답변이면 항상 같은 결과를 내는 추천 엔진입니다.  
WorldMap은 recommendation을 "생성형 AI 응답"이 아니라 **버전이 있는 scoring function**으로 봅니다.

### 3-2. survey version / engine version

현재 버전 상수는 [RecommendationSurveyService.java](../src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)에 있습니다.

- `SURVEY_VERSION = "survey-v4"`
- `ENGINE_VERSION = "engine-v20"`

즉, version source of truth는 question catalog나 profile catalog가 아니라 **실제 scoring service**입니다.

### 3-3. question catalog

설문 문항과 보기 정의를 [RecommendationQuestionCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java)에서 코드로 고정한 것입니다.  
프런트가 임의로 문항 구조를 들고 있지 않고, 서버가 SSR과 요약 문자열까지 같은 기준으로 생성합니다.

### 3-4. country profile catalog

추천 대상 국가의 성향을 [RecommendationCountryProfileCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java)에 30개 profile로 고정한 것입니다.  
이 profile은 seed country와 join되기 전까지는 **후보 집합**일 뿐입니다.

### 3-5. feedback context

결과 페이지를 본 사용자가 어떤 추천 결과에 대해 만족도 점수를 남기는지 연결하는 서버 측 컨텍스트입니다.  
[RecommendationFeedbackContext.java](../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackContext.java)와 [RecommendationFeedbackSessionStore.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackSessionStore.java)가 이 계약을 담당합니다.

### 3-6. offline persona baseline

실제 사용자 feedback와 별개로, 고정된 18개 persona 시나리오를 돌려 추천 품질 회귀를 막는 테스트 전략입니다.  
운영 표본이 적어도 품질 하한을 유지할 수 있게 해 줍니다.

### 3-7. weak / anchor drift / active signal

admin 운영 화면에서 baseline을 볼 때 쓰는 분류입니다.

- `weak`: 기대 후보가 top 3에 아예 들어오지 않음
- `anchor drift`: 기대 후보는 들어오지만 1위 anchor가 다름
- `active signal`: `settlement / mobility` 같은 후반부 문항이 실제 후보 구성을 바꾸는지 확인하는 시나리오

## 4. 이번 글에서 다룰 파일

- [RecommendationSurveyAnswers.java](../src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java)
- [RecommendationSurveyForm.java](../src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java)
- [RecommendationQuestionCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java)
- [RecommendationQuestionView.java](../src/main/java/com/worldmap/recommendation/application/RecommendationQuestionView.java)
- [RecommendationOptionView.java](../src/main/java/com/worldmap/recommendation/application/RecommendationOptionView.java)
- [RecommendationPreferenceSummaryView.java](../src/main/java/com/worldmap/recommendation/application/RecommendationPreferenceSummaryView.java)
- [RecommendationCountryProfile.java](../src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfile.java)
- [RecommendationCountryProfileCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java)
- [RecommendationCandidateView.java](../src/main/java/com/worldmap/recommendation/application/RecommendationCandidateView.java)
- [RecommendationSurveyResultView.java](../src/main/java/com/worldmap/recommendation/application/RecommendationSurveyResultView.java)
- [RecommendationSurveyService.java](../src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)
- [RecommendationFeedbackContext.java](../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackContext.java)
- [RecommendationFeedbackSubmission.java](../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackSubmission.java)
- [RecommendationFeedbackService.java](../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java)
- [RecommendationFeedbackInsightsView.java](../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackInsightsView.java)
- [RecommendationFeedbackSummaryView.java](../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackSummaryView.java)
- [RecommendationPersonaBaselineCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineCatalog.java)
- [RecommendationPersonaBaselineScenario.java](../src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineScenario.java)
- [RecommendationFeedback.java](../src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java)
- [RecommendationFeedbackRepository.java](../src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackRepository.java)
- [RecommendationFeedbackVersionSummaryProjection.java](../src/main/java/com/worldmap/recommendation/domain/RecommendationFeedbackVersionSummaryProjection.java)
- [RecommendationPageController.java](../src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java)
- [RecommendationFeedbackApiController.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java)
- [RecommendationFeedbackRequest.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java)
- [RecommendationFeedbackSavedResponse.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackSavedResponse.java)
- [RecommendationFeedbackSessionStore.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackSessionStore.java)
- [AdminPageController.java](../src/main/java/com/worldmap/admin/web/AdminPageController.java)
- [AdminPersonaBaselineService.java](../src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java)
- [AdminRecommendationOpsReviewService.java](../src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewService.java)
- [recommendation/survey.html](../src/main/resources/templates/recommendation/survey.html)
- [recommendation/result.html](../src/main/resources/templates/recommendation/result.html)
- [admin/recommendation-feedback.html](../src/main/resources/templates/admin/recommendation-feedback.html)
- [recommendation-feedback.js](../src/main/resources/static/js/recommendation-feedback.js)
- [RecommendationPageIntegrationTest.java](../src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)
- [RecommendationFeedbackIntegrationTest.java](../src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java)
- [RecommendationSurveyServiceTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java)
- [RecommendationCountryProfileCatalogTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalogTest.java)
- [RecommendationOfflinePersonaCoverageTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)
- [RecommendationOfflinePersonaSnapshotTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)
- [AdminPersonaBaselineServiceIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)
- [AdminRecommendationOpsReviewServiceIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)

## 5. 핵심 도메인 모델 / 상태

### 5-1. `RecommendationSurveyAnswers`: 20문항 답변을 enum record로 고정

[RecommendationSurveyAnswers.java](../src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java)는 recommendation의 중심 value object입니다.

현재 필드 수는 20개입니다.

- climatePreference
- seasonStylePreference
- seasonTolerance
- pacePreference
- crowdPreference
- costQualityPreference
- housingPreference
- environmentPreference
- mobilityPreference
- englishSupportNeed
- newcomerSupportNeed
- safetyPriority
- publicServicePriority
- digitalConveniencePriority
- foodImportance
- diversityImportance
- cultureLeisureImportance
- workLifePreference
- settlementPreference
- futureBasePreference

중요한 점은 각 필드가 단순 문자열이 아니라 **의미가 있는 enum**이라는 것입니다.

예:

- `ClimatePreference`는 label/description과 함께 `targetValue()`를 가집니다
- `CostQualityPreference`는 `targetPriceLevel()`을 가집니다
- `ImportanceLevel`은 `weight()`를 가집니다
- `FutureBasePreference`는 장기 기반 선호를 수치로 표현합니다

즉, recommendation은 JSON blob이나 자유 텍스트가 아니라 **typed answer model** 위에서 작동합니다.

### 5-2. `RecommendationSurveyForm`: SSR form binding 상태

[RecommendationSurveyForm.java](../src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java)는 20개 필드를 모두 `@NotNull`로 검증합니다.

역할:

- SSR form submit binding
- validation message 제공
- `toAnswers()`로 domain record 변환
- `isSelected(fieldName, optionValue)`로 validation 실패 시 radio 선택 상태 유지

즉, survey lifecycle의 진짜 entry state는 `RecommendationSurveyAnswers`가 아니라 **`RecommendationSurveyForm`**입니다.

### 5-3. `RecommendationQuestionCatalog`: 문항 source of truth

[RecommendationQuestionCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java)는 20문항 전체를 코드로 고정합니다.

역할:

- 문항 id
- 질문 문구
- helper copy
- option list
- 결과 페이지용 선호 요약 문자열

중요한 점은 catalog가 설문 화면뿐 아니라 결과 요약도 같이 만든다는 것입니다.  
즉, survey와 result는 서로 다른 뷰가 아니라 **같은 catalog의 두 표현**입니다.

### 5-4. `RecommendationCountryProfile`: 30개 후보의 성향 축

[RecommendationCountryProfile.java](../src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfile.java)는 국가별 profile을 아래 축으로 가집니다.

- `climateValue`
- `seasonality`
- `paceValue`
- `priceLevel`
- `urbanityValue`
- `englishSupport`
- `safety`
- `welfare`
- `food`
- `diversity`
- `housingSpace`
- `digitalConvenience`
- `cultureScene`
- `newcomerFriendliness`
- `hookLine`

[RecommendationCountryProfileCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java)는 현재 30개 국가를 하드코딩합니다.

중요한 점:

- 이 30개는 "최종 출력 row"가 아니라 **후보 프로필 집합**입니다
- 실제 추천 결과로 노출되려면 `CountryRepository.findAllByOrderByNameKrAsc()` 결과와 ISO3로 join되어야 합니다
- seed country가 없으면 [RecommendationSurveyService.java](../src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)에서 `null`로 떨어져 조용히 제외됩니다

즉, recommendation candidate pool은 **profile catalog ∩ country seed**입니다.

### 5-5. `RecommendationCandidateView`: 추천 결과 read model

[RecommendationCandidateView.java](../src/main/java/com/worldmap/recommendation/application/RecommendationCandidateView.java)는 결과 페이지에 내려가는 top 3 카드 모델입니다.

필드:

- rank
- iso3Code
- countryNameKr / countryNameEn
- continentLabel
- capitalCity
- populationLabel
- matchScore
- strongSignalCount
- exactMatchCount
- headline
- reasons

즉, 결과 페이지는 "점수 하나"만 보여 주는 게 아니라, **요약 설명을 위한 read model**을 갖고 있습니다.

### 5-6. `RecommendationFeedbackContext`: 서버에 잠깐 보관되는 결과 컨텍스트

[RecommendationFeedbackContext.java](../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackContext.java)는 아래 세 가지를 session에 잠깐 보관합니다.

- surveyVersion
- engineVersion
- RecommendationSurveyAnswers

즉, result 페이지에서 feedback을 받을 때 필요한 핵심 context를 **서버가 보관**합니다.

### 5-7. `RecommendationFeedback`: durable feedback row

[RecommendationFeedback.java](../src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java)는 `recommendation_feedback` 테이블 row입니다.

저장 필드:

- `surveyVersion`
- `engineVersion`
- `satisfactionScore`
- 20개 answer enum snapshot
- `createdAt`

즉, 운영 데이터는 "점수만 저장"이 아니라 **버전 + 답변 스냅샷까지 남기는 방식**입니다.

### 5-8. `RecommendationPersonaBaselineScenario`: offline 품질 기준

[RecommendationPersonaBaselineScenario.java](../src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineScenario.java)는 아래를 묶습니다.

- scenario id (`P01` ~ `P18`)
- 설명
- 실제 `RecommendationSurveyAnswers`
- 기대 후보 3개
- 기대 만족도 범위
- 분석 메모
- activeSignal 여부

즉, baseline도 "테스트 케이스"가 아니라 **운영 설명 자료로도 쓸 수 있는 시나리오 자산**입니다.

## 6. 설계 구상

### 왜 런타임 LLM 호출을 분리했는가

이 프로젝트의 핵심은 "추천이 그럴듯해 보이는가"보다 **왜 이 결과가 나왔는지 설명 가능한가**입니다.

런타임 LLM을 바로 넣으면 장점도 있지만, 아래가 약해집니다.

- 같은 입력에 같은 결과 보장
- 버전 관리
- 회귀 테스트
- 면접 설명 가능성
- 운영 집계 기준

그래서 WorldMap은 순서를 이렇게 잡았습니다.

1. deterministic survey engine을 먼저 만든다
2. feedback과 offline baseline으로 품질 루프를 닫는다
3. 운영 데이터와 baseline drift를 같이 본다
4. 그 뒤에야 AI-assisted tuning을 이야기한다

즉, recommendation에서 AI는 runtime이 아니라 **개발/개선 단계의 보조 수단**입니다.

### 왜 question/profile을 DB가 아니라 code catalog로 두는가

이 추천 엔진은 아직 admin이 실시간으로 후보를 편집하는 제품이 아닙니다.  
오히려 지금 중요한 것은 "현재 버전의 설문과 엔진을 설명할 수 있는가"입니다.

code catalog를 택한 이유:

- git diff로 문항 개정 설명 가능
- enum과 helper text가 한 파일에 모임
- snapshot test와 baseline test에 바로 연결 가능
- seed country와의 join 계약을 코드에서 확인 가능

즉, 현재 단계에서 catalog는 데이터 편집 편의보다 **설명 가능성과 테스트성**이 더 중요합니다.

### 왜 top 3 결과를 저장하지 않는가

추천 결과 자체를 DB에 저장하면 얼핏 좋아 보일 수 있지만, 지금 제품에서는 과한 구조입니다.

문제:

- 결과 row를 장기간 보존할 이유가 약함
- survey/engine version이 바뀌면 과거 결과 해석이 더 어려워짐
- 개인정보는 아니어도 사용자의 취향 응답을 더 오래 쌓게 됨

그래서 현재 구조는 다음만 합니다.

- 결과 페이지를 렌더링한다
- 같은 session 안에 feedback용 context를 잠깐 저장한다
- 피드백이 오면 score + answers snapshot + versions만 저장한다

즉, recommendation의 durable 운영 데이터는 **"추천 결과 자체"가 아니라 "추천에 대한 만족도와 맥락"**입니다.

### 왜 feedback request는 token + score만 받는가

클라이언트가 `surveyVersion`, `engineVersion`, `answers`를 다시 보내게 하면 조작 위험이 큽니다.  
현재 [RecommendationFeedbackRequest.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java)는 아래만 받습니다.

- `feedbackToken`
- `satisfactionScore`

나머지는 모두 session에 저장된 [RecommendationFeedbackContext.java](../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackContext.java)에서 복원합니다.

즉, request body는 최소화하고, 실제 신뢰할 값은 **서버가 result 렌더링 시 보관한 context**에서 가져옵니다.

### 왜 feedback과 offline baseline을 같이 가져가야 하는가

feedback만 있으면:

- 실제 사용자 반응은 보이지만
- 응답 수가 적을 때 tuning 판단이 흔들립니다

offline baseline만 있으면:

- 회귀는 잡지만
- 실제 만족도가 어떤지는 모릅니다

그래서 recommendation 품질 루프는 아래 둘이 동시에 있어야 합니다.

- 운영 표본: 실제 사용자의 만족도
- baseline test: 고정된 persona 시나리오 품질 하한

## 7. 코드 설명

### 7-1. `GET /recommendation/survey`: 서버가 form shell을 만든다

[RecommendationPageController.java](../src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java)의 `survey()`는 아래를 model에 넣습니다.

- `surveyForm`
- `surveyQuestions`

여기서 `surveyQuestions`는 [RecommendationQuestionCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java)의 `questions()` 결과입니다.  
[recommendation/survey.html](../src/main/resources/templates/recommendation/survey.html)은 이 리스트를 그대로 돌며 radio group을 그립니다.

즉, survey SSR의 source of truth는 template가 아니라 **question catalog**입니다.

### 7-2. `POST /recommendation/survey`: 20개 enum validation -> result SSR

`recommend()` 흐름:

1. `RecommendationSurveyForm` 20개 필드 바인딩
2. `@Valid` 검증
3. 실패 시 `surveyQuestions`를 다시 넣고 survey 화면 재렌더
4. 성공 시 `toAnswers()`로 `RecommendationSurveyAnswers` 생성
5. `RecommendationSurveyService.recommend(answers)` 호출
6. 결과 context를 session에 저장
7. result SSR 렌더

중요한 점:

- validation 실패 시 [RecommendationSurveyForm.java](../src/main/java/com/worldmap/recommendation/web/RecommendationSurveyForm.java)의 `isSelected(...)` 덕분에 기존 선택 상태를 다시 보여줄 수 있습니다
- 결과 페이지로 넘어갈 때도 별도 API redirect가 아니라 **SSR render**입니다

[RecommendationPageIntegrationTest.java](../src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)는 이 흐름을 고정합니다.

- survey page가 실제로 20문항을 렌더링하는지
- result page가 deterministic 결과를 보여주는지
- `feedbackToken`이 HTML에 들어가는지
- `surveyVersion`, `engineVersion` 같은 내부 버전 정보가 public result HTML에 노출되지 않는지

### 7-3. `RecommendationSurveyService`: 버전 상수와 scoring engine의 실제 중심

[RecommendationSurveyService.java](../src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)는 recommendation의 실제 중심입니다.

현재 버전 상수:

- `SURVEY_VERSION = "survey-v4"`
- `ENGINE_VERSION = "engine-v20"`

이 service가 하는 일:

1. `countryRepository.findAllByOrderByNameKrAsc()`로 현재 seed country를 읽는다
2. ISO3 기준 map을 만든다
3. 30개 profile 각각에 대해 `scoreCandidate(...)`를 수행한다
4. seed country가 없는 profile은 `null`로 떨어뜨려 제외한다
5. 결과를 아래 tie-break 순서로 정렬한다
   - `matchScore` 내림차순
   - `strongSignalCount` 내림차순
   - `exactMatchCount` 내림차순
   - `countryNameKr` 오름차순
6. top 3만 남긴다
7. rank를 다시 1~3으로 매긴다
8. `RecommendationSurveyResultView`를 만든다

즉, survey version도, engine version도, 최종 top 3 결정도 모두 **service**가 책임집니다.

### 7-4. scoring rule은 "가중치 + derived feature + bonus/penalty" 조합이다

이 글에서 알고리즘 전체를 줄 단위로 복붙할 필요는 없지만, rebuild guide로는 어떤 축이 있는지 알아야 합니다.

현재 base weight 축:

- `CLIMATE_WEIGHT`
- `SEASON_STYLE_WEIGHT`
- `WEATHER_ADAPTATION_WEIGHT`
- `PACE_WEIGHT`
- `CROWD_WEIGHT`
- `COST_QUALITY_WEIGHT`
- `HOUSING_WEIGHT`
- `ENVIRONMENT_WEIGHT`
- `MOBILITY_WEIGHT`
- `ENGLISH_SUPPORT_WEIGHT`
- `NEWCOMER_SUPPORT_WEIGHT`
- `SAFETY_WEIGHT`
- `PUBLIC_SERVICE_WEIGHT`
- `DIGITAL_WEIGHT`
- `FOOD_WEIGHT`
- `DIVERSITY_WEIGHT`
- `CULTURE_WEIGHT`
- `WORK_LIFE_WEIGHT`
- `SETTLEMENT_WEIGHT`
- `FUTURE_BASE_WEIGHT`

현재 derived feature helper:

- `weatherDemand(profile)`
- `crowdEnergy(profile)`
- `transitSupport(profile)`
- `newcomerSupport(profile)`
- `workIntensity(profile)`
- `futureBase(profile)`
- `normalizedAverage(...)`

현재 공통 계산 규칙:

- `closenessScore(distance, weight)`
- `exactMatchBonus(distance)`
- `mismatchPenalty(distance, penaltyWeight)`
- `costQualityPoints(...)`
- `supportPoints(...)`
- `priorityPoints(...)`
- `coherenceBonus(...)`

현재 bonus / penalty family:

- `EXACT_MATCH_BONUS`
- `COHERENCE_BONUS`
- `EXPERIENCE_TRANSIT_BONUS`
- `CIVIC_BASE_BONUS`
- `PRACTICAL_SAFETY_BONUS`
- `SOFT_LANDING_BONUS`
- `FAMILY_BASE_BONUS`
- `GLOBAL_HUB_BONUS`
- `FOODIE_STARTER_BONUS`
- `TEMPERATE_PUBLIC_BASE_BONUS`
- `PRACTICAL_PUBLIC_VALUE_BONUS`
- `PREMIUM_WARM_HUB_BONUS`
- `SOFT_NATURE_BASE_BONUS`
- `COSMOPOLITAN_PULSE_BONUS`
- `TEMPERATE_GLOBAL_CITY_BONUS`
- `ACCESSIBLE_WARM_VALUE_HUB_BONUS`
- `TEMPERATE_FAMILY_BRIDGE_BONUS`
- `EXPLORATORY_NATURE_RUNWAY_BONUS`
- `VALUE_FIRST_COST_OVERSHOOT_PENALTY`
- `BALANCED_COST_OVERSHOOT_PENALTY`
- `QUALITY_FIRST_COST_OVERSHOOT_PENALTY`
- `CLIMATE_MISMATCH_PENALTY`
- `SEASON_STYLE_MISMATCH_PENALTY`

정리하면 이 engine은 "AI 추론"이 아니라 아래 조합입니다.

- 20개 기본 질문에서 나오는 거리/우선도 점수
- profile에서 파생되는 derived feature
- 몇 가지 명시적 scenario bonus/penalty
- tie-break용 strong signal / exact match 카운트

즉, recommendation은 **rule-based weighted scoring engine**입니다.

### 7-5. seed-backed join이 실제 재현성의 핵심이다

추천 결과 카드에는 아래 정보가 보입니다.

- 국가명
- 대륙명
- 수도
- 인구 라벨

이 값들은 profile catalog에 없습니다.  
실제 source는 [Country.java](../src/main/java/com/worldmap/country/domain/Country.java) seed row입니다.

흐름:

```text
RecommendationCountryProfileCatalog.profiles()
-> 각 profile.iso3Code
-> CountryRepository.findAllByOrderByNameKrAsc() 결과와 join
-> Country가 없으면 candidate null
-> 있으면 continent/capital/population label 조립
```

즉, recommendation pool의 진짜 재현 조건은 **30개 profile만 맞추는 것**이 아니라 **30개 profile이 현재 seed countries와 모두 연결되는 것**입니다.

[RecommendationCountryProfileCatalogTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalogTest.java)는 바로 이 점을 고정합니다.

- profile 수는 30개
- ISO3는 모두 유일
- 주요 확장 국가(USA, GBR, FRA, ARE, THA, BRA, MEX, ZAF)가 포함됨
- profile ISO3 집합은 `/data/countries.json` seed에 모두 존재해야 함

### 7-6. result page는 "예쁜 카드"가 아니라 context 생성 지점이다

[recommendation/result.html](../src/main/resources/templates/recommendation/result.html)은 아래를 렌더링합니다.

- 20개 선호 요약
- top 3 국가 카드
- 후보별 headline
- top 3 reasons
- hidden `feedbackToken`
- 접근 가능한 만족도 radio group

중요한 점:

- result HTML에는 `surveyVersion`, `engineVersion`, raw answers가 다시 박히지 않습니다
- 결과 페이지가 렌더링될 때 [RecommendationPageController.java](../src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java)가 session에 context를 저장합니다
- 즉, result page는 단순 output이 아니라 **feedback 시작점**입니다

### 7-7. `RecommendationFeedbackSessionStore`: one-time token store

[RecommendationFeedbackSessionStore.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackSessionStore.java)의 핵심 규칙:

- token은 UUID
- session attribute key는 `WORLDMAP_RECOMMENDATION_FEEDBACK_CONTEXTS`
- session당 최대 10개 context만 유지
- `consume(...)`는 token을 찾으면 **바로 제거**
- 토큰이 없거나 비었거나 이미 소비됐으면 `Optional.empty()`

즉, feedback token은 read-only key가 아니라 **one-time consumption token**입니다.

이 설계가 필요한 이유:

- 같은 결과에 대한 중복 만족도 제출을 줄임
- hidden field로 survey/engine/answers를 다시 보내지 않음
- 오래된 result page 재전송이 운영 데이터를 오염시키지 못하게 함

### 7-8. `POST /api/recommendation/feedback`: request는 최소화하고 context는 서버에서 복원한다

[RecommendationFeedbackRequest.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackRequest.java)는 아래만 받습니다.

- `feedbackToken`
- `satisfactionScore`

유효성:

- token은 `@NotBlank`
- score는 `1~5`

[RecommendationFeedbackApiController.java](../src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java)의 흐름:

1. request validation
2. session store에서 token consume
3. 없으면 `IllegalArgumentException`
4. 있으면 `RecommendationFeedbackSubmission` 생성
5. [RecommendationFeedbackService.java](../src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java)로 저장
6. `feedbackId`, `satisfactionScore`, `surveyVersion`, `engineVersion` 응답

즉, client는 score만 주고, **실제 운영 데이터의 의미는 서버가 알고 있는 context로만 복원**합니다.

### 7-9. `RecommendationFeedback`: 점수만이 아니라 answer snapshot을 저장한다

[RecommendationFeedback.java](../src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java)는 `create(...)` 시점에 아래를 row로 굳힙니다.

- surveyVersion
- engineVersion
- satisfactionScore
- 20개 answer enum
- createdAt

이 구조의 의미:

- 만족도 4점이 "어떤 종류의 사용자"에게서 나왔는지 나중에 볼 수 있음
- 현재 버전별 평균뿐 아니라, 특정 답변 조합의 성향도 뒤에서 분석 가능
- 클라이언트가 answer snapshot을 임의 조작해 넣을 수 없음

즉, feedback row는 단순 별점이 아니라 **versioned recommendation evidence**입니다.

### 7-10. admin 운영 루프는 `/dashboard/recommendation/feedback`과 baseline 평가까지 포함한다

recommendation의 live loop는 feedback 저장에서 끝나지 않습니다.

[AdminPageController.java](../src/main/java/com/worldmap/admin/web/AdminPageController.java)는 아래 두 SSR 페이지를 엽니다.

- `/dashboard/recommendation/feedback`
- `/dashboard/recommendation/persona-baseline`

[admin/recommendation-feedback.html](../src/main/resources/templates/admin/recommendation-feedback.html)은 아래를 보여 줍니다.

- current survey / current engine
- total feedback / tracked versions / overall average
- ops review priority
- version별 score distribution

즉, 운영자는 단순 summary API가 아니라 **SSR 운영 화면**에서 recommendation 상태를 봅니다.

### 7-11. `AdminRecommendationOpsReviewService`: feedback와 baseline을 합쳐 다음 액션을 결정한다

[AdminRecommendationOpsReviewService.java](../src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewService.java)의 현재 기준:

- `MIN_RESPONSES_FOR_TUNING = 5`
- `LOW_SATISFACTION_THRESHOLD = 3.80`

우선순위 결정 순서:

1. 현재 버전 응답 수가 5 미만이면  
   -> "현재 버전 피드백 더 수집"
2. weak scenario가 있으면  
   -> "weak scenario 먼저 정리"
3. 평균 만족도가 3.80 미만이면  
   -> "설문 문구와 helper text 먼저 점검"
4. anchor drift가 있으면  
   -> "rank drift 줄이기"
5. 모두 없으면  
   -> "현재 엔진 유지"

즉, tuning 기준도 감으로 정하지 않고 **운영 규칙**으로 코드에 고정했습니다.

### 7-12. `RecommendationPersonaBaselineCatalog`: 18개 persona는 손으로만 쓰지 않았다

[RecommendationPersonaBaselineCatalog.java](../src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineCatalog.java)는 현재 18개 시나리오를 만듭니다.

특징:

- 핵심 knob 몇 개만 직접 넣고
- 나머지는 helper 메서드로 조합합니다

예:

- `seasonStylePreference(...)`
- `crowdPreference(...)`
- `housingPreference(...)`
- `newcomerSupportNeed(...)`
- `safetyImportance(...)`
- `publicServiceImportance(...)`
- `cultureImportance(...)`

즉, baseline은 18개 답안을 전부 손으로 중복 작성한 게 아니라, **설명 가능한 persona 생성 규칙** 위에서 만든 것입니다.

현재 active-signal scenario는 4개입니다.

- `P15`
- `P16`
- `P17`
- `P18`

이들은 `settlementPreference`, `mobilityPreference` 같은 후반부 문항이 실제로 후보 구성을 바꾸는지 확인합니다.

### 7-13. 현재 offline baseline은 coverage와 exact snapshot을 동시에 잡는다

[RecommendationOfflinePersonaCoverageTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)는 아래를 고정합니다.

- 시나리오 수는 18개
- 현재 engine은 최소 15개 이상에서 기대 후보 중 하나를 top 3에 올려야 함
- 현재 baseline anchor 예시는 `P01 -> 싱가포르`, `P02 -> 말레이시아`, `P09 -> 아랍에미리트`, `P11 -> 캐나다`
- 특정 drift 금지 조건도 함께 본다

[RecommendationOfflinePersonaSnapshotTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)는 더 강합니다.

- 18개 모든 시나리오의 현재 top 3를 exact snapshot으로 고정합니다

즉, recommendation tuning은 "대충 좋아졌다"가 아니라 **snapshot이 바뀌면 설명해야 하는 구조**입니다.

### 7-14. admin baseline view는 현재 엔진 품질을 숫자로 요약한다

[AdminPersonaBaselineService.java](../src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java)는 baseline을 운영용 view로 다시 분류합니다.

현재 코드 기준 핵심 숫자:

- total scenario: 18
- matched scenario: 18
- weak scenario: 0
- active signal scenario: 4
- anchor drift scenario: 0

[AdminPersonaBaselineServiceIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)는 이 현재 상태를 고정합니다.

즉, admin 화면은 "좋아 보인다"가 아니라 **지금 엔진이 18/18을 맞추고 있는지**를 실제로 보여 주는 도구입니다.

## 8. 요청 흐름 / 상태 변화

### 8-1. 설문 SSR 시작

```text
GET /recommendation/survey
-> RecommendationPageController.survey()
-> model["surveyForm"] = new RecommendationSurveyForm()
-> model["surveyQuestions"] = RecommendationQuestionCatalog.questions()
-> recommendation/survey.html
```

### 8-2. 설문 제출과 결과 계산

```text
POST /recommendation/survey
-> RecommendationSurveyForm 20개 enum 바인딩
-> @Valid 검증
-> 실패 시 survey SSR 재렌더 + 선택 상태 유지
-> 성공 시 toAnswers()
-> RecommendationSurveyService.recommend()
-> top 3 RecommendationCandidateView 생성
-> session에 RecommendationFeedbackContext 저장
-> UUID feedbackToken 발급
-> recommendation/result.html SSR
```

### 8-3. feedback 저장

```text
브라우저에서 만족도 radio 선택
-> recommendation-feedback.js
-> POST /api/recommendation/feedback
   body = { feedbackToken, satisfactionScore }
-> RecommendationFeedbackSessionStore.consume()
-> RecommendationFeedbackRequest.toSubmission(context)
-> RecommendationFeedbackService.record()
-> RecommendationFeedback.create(...)
-> recommendation_feedback row 저장
```

### 8-4. admin summary 읽기

```text
GET /dashboard/recommendation/feedback
-> AdminPageController.recommendationFeedback()
-> RecommendationFeedbackService.summarizeByVersion()
-> AdminRecommendationOpsReviewService.loadReview()
-> admin/recommendation-feedback.html SSR
```

### 8-5. offline 품질 회귀 확인

```text
RecommendationPersonaBaselineCatalog.scenarios()
-> RecommendationSurveyService.recommend()를 18번 실행
-> coverage test / exact snapshot test
-> admin baseline view와 같은 기준으로 weak / anchor drift / active signal 분류 가능
```

## 9. 실패 케이스 / 예외 처리

- question 구조를 프런트 상수로만 두면: SSR survey/result, validation, summary label이 분리됩니다
- 20문항 중 일부가 비면: `RecommendationSurveyForm` validation으로 survey를 다시 렌더링합니다
- profile ISO3가 seed에 없으면: 해당 국가는 결과에서 조용히 빠집니다
- client가 `surveyVersion`, `engineVersion`, `answers`를 조작해 보내려 하면: request 모델에 그 필드가 없어서 저장 경로에 들어오지 못합니다
- feedback token이 없거나 재사용되면: `400`
- 만족도 점수가 1~5 범위를 벗어나면: `400`
- non-admin이 summary API를 치면: `403`
- tuning으로 특정 persona anchor가 밀리면: coverage/snapshot/admin baseline에서 바로 드러납니다

즉, recommendation의 핵심 리스크는 "추천이 한 번 나온다"보다 **재현성, 조작 방지, 품질 drift**에 가깝습니다.

## 10. 테스트로 검증하기

### 10-1. [RecommendationPageIntegrationTest.java](../src/test/java/com/worldmap/recommendation/RecommendationPageIntegrationTest.java)

- `/recommendation/survey`가 실제 survey shell을 SSR하는지
- public 화면에 내부 운영 단어가 새지 않는지
- 설문 제출 후 deterministic 결과와 feedback token이 렌더링되는지
- result HTML에 `surveyVersion`, `engineVersion`이 그대로 노출되지 않는지

### 10-2. [RecommendationFeedbackIntegrationTest.java](../src/test/java/com/worldmap/recommendation/RecommendationFeedbackIntegrationTest.java)

- feedback API가 anonymous 만족도와 20문항 답변 snapshot을 저장하는지
- token 재사용/위조가 막히는지
- score range validation이 동작하는지
- summary API가 admin only인지
- revoked admin session이 현재 member role 기준으로 차단되는지
- legacy `/recommendation/feedback-insights`가 `/dashboard/recommendation/feedback`으로 redirect되는지

### 10-3. [RecommendationSurveyServiceTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java)

- deterministic top 3가 유지되는지
- expanded pool candidate가 surface되는지
- settlement / housing / futureBase 문항이 실제 후보를 바꾸는지
- 고도시/영어/다문화 조합에서 기대 anchor가 나오는지

### 10-4. [RecommendationCountryProfileCatalogTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalogTest.java)

- profile 수가 30개인지
- ISO3가 유일한지
- profile pool이 실제 country seed에 모두 연결되는지

### 10-5. [RecommendationOfflinePersonaCoverageTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java)

- 현재 baseline 18개 중 최소 15개 이상이 기대 후보를 top 3에 유지하는지
- 핵심 anchor 시나리오의 현재 후보가 크게 흔들리지 않는지

### 10-6. [RecommendationOfflinePersonaSnapshotTest.java](../src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java)

- 18개 시나리오의 현재 top 3가 exact snapshot과 일치하는지

### 10-7. [AdminPersonaBaselineServiceIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminPersonaBaselineServiceIntegrationTest.java)

- baseline total 18
- matched 18
- weak 0
- active signal 4
- anchor drift 0
- active signal id가 `P15~P18`인지

### 10-8. [AdminRecommendationOpsReviewServiceIntegrationTest.java](../src/test/java/com/worldmap/admin/AdminRecommendationOpsReviewServiceIntegrationTest.java)

- 현재 버전 응답이 5 미만이면 "현재 버전 피드백 더 수집"이 나오는지
- 현재 버전 응답이 충분하고 baseline이 안정적이면 "현재 엔진 유지"가 나오는지

실행 명령:

```bash
./gradlew test \
  --tests com.worldmap.recommendation.RecommendationPageIntegrationTest \
  --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest \
  --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest \
  --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest \
  --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest \
  --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest \
  --tests com.worldmap.admin.AdminPersonaBaselineServiceIntegrationTest \
  --tests com.worldmap.admin.AdminRecommendationOpsReviewServiceIntegrationTest
```

이 테스트 묶음이 자동으로 고정하는 범위는 꽤 넓지만, 그래도 한계는 분명합니다.

- 현재 service 버전의 deterministic top 3
- feedback token/session context 무결성
- current baseline과 admin ops review 계산

반대로 아래는 이 테스트만으로 자동 증명되지 않습니다.

- 실제 운영 사용자 만족도가 정말 올라갔는지
- 문항 helper copy와 scoring change 중 무엇이 효과를 냈는지
- future tuning 이후에도 사람이 기대하는 "좋은 추천" 감각이 그대로 유지되는지

## 11. 회고

recommendation에서 가장 중요한 선택은 "AI처럼 보이게 만들기"가 아니라 **설명 가능한 품질 루프를 갖추기**였습니다.

현재 구조의 장점:

- 같은 입력에 같은 결과를 보장합니다
- 문항/후보/version이 모두 코드로 설명 가능합니다
- result feedback이 위조되기 어렵습니다
- baseline과 실제 운영 feedback를 함께 볼 수 있습니다
- tuning 후에도 snapshot과 admin baseline으로 drift를 추적할 수 있습니다

### 현재 구현의 한계

- profile catalog는 여전히 수작업으로 관리합니다
- 현재 recommendation은 top 3만 보여 주고, "왜 다른 후보는 탈락했는지"까지는 설명하지 않습니다
- tuning rule이 늘어날수록 service가 길어집니다
- 문항 helper text와 점수식이 같이 움직일 때, 둘 중 어느 쪽 효과인지 분리 측정은 아직 약합니다
- offline persona baseline은 회귀 하한을 잡아 주지만, 실제 live 사용자 만족도 전체를 대체하지는 않습니다

즉, 지금 recommendation은 production-grade ML system이 아니라 **포트폴리오용 deterministic decision engine**에 더 가깝습니다.  
하지만 바로 그 점 때문에 설명 가능성과 테스트성이 높습니다.

## 12. 취업 포인트

### 12-1. 1문장 답변

WorldMap 추천은 runtime LLM 대신 versioned deterministic scoring engine으로 구현하고, one-time feedback token과 offline persona baseline으로 품질 루프를 닫았습니다.

### 12-2. 30초 답변

추천은 `RecommendationSurveyAnswers` 20문항 enum 답변과 30개 국가 profile catalog를 기준으로 `RecommendationSurveyService`가 deterministic하게 top 3를 계산합니다. 결과 페이지는 session에 `surveyVersion + engineVersion + answers` context를 저장하고 UUID feedback token만 내려서, 피드백 API는 score만 받고 나머지는 서버 context에서 복원합니다. 품질 관리는 실제 만족도 summary뿐 아니라 18개 persona baseline coverage/snapshot, weak scenario, anchor drift, active signal 분류까지 admin 서비스로 같이 봅니다.

### 12-3. 예상 꼬리 질문

- 왜 런타임 LLM을 안 썼나요?
- 왜 설문 문항과 국가 프로필을 DB가 아니라 코드 catalog로 두나요?
- 왜 feedback request에 answers를 다시 안 받나요?
- 왜 top 3 결과를 저장하지 않나요?
- baseline coverage와 exact snapshot을 둘 다 유지하는 이유는 무엇인가요?
- 현재 tuning 우선순위는 어떻게 결정하나요?

## 13. 시작 상태

- country seed는 있지만 추천 엔진은 없는 상태
- 사용자의 취향을 구조화한 form/value object가 없는 상태
- 결과에 대한 만족도 feedback를 운영 기준으로 읽을 구조가 없는 상태
- tuning 후 품질 회귀를 자동으로 막을 baseline test가 없는 상태

## 14. 이번 글에서 바뀌는 파일

- `src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java`
- `src/main/java/com/worldmap/recommendation/application/RecommendationQuestionCatalog.java`
- `src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java`
- `src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java`
- `src/main/java/com/worldmap/recommendation/web/RecommendationPageController.java`
- `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackSessionStore.java`
- `src/main/java/com/worldmap/recommendation/web/RecommendationFeedbackApiController.java`
- `src/main/java/com/worldmap/recommendation/application/RecommendationFeedbackService.java`
- `src/main/java/com/worldmap/recommendation/domain/RecommendationFeedback.java`
- `src/main/java/com/worldmap/recommendation/application/RecommendationPersonaBaselineCatalog.java`
- `src/main/java/com/worldmap/admin/application/AdminPersonaBaselineService.java`
- `src/main/java/com/worldmap/admin/application/AdminRecommendationOpsReviewService.java`
- `src/main/resources/templates/recommendation/survey.html`
- `src/main/resources/templates/recommendation/result.html`
- `src/main/resources/templates/admin/recommendation-feedback.html`
- `src/main/resources/static/js/recommendation-feedback.js`
- 관련 추천 / admin 테스트

## 15. 구현 체크리스트

1. 20문항 enum answer model을 먼저 정의한다
2. question catalog와 summary label을 같은 서버 source of truth로 묶는다
3. 30개 country profile catalog를 만든다
4. seed country join이 없으면 candidate를 버리는 규칙을 넣는다
5. scoring service에 version 상수를 둔다
6. result SSR과 feedback context 저장을 연결한다
7. feedback request는 token + score만 받게 한다
8. feedback row에는 version + answer snapshot을 저장한다
9. admin summary와 persona baseline view를 만든다
10. coverage / snapshot / ops review 테스트를 고정한다

## 16. 실행 / 검증 명령

```bash
./gradlew test \
  --tests com.worldmap.recommendation.RecommendationPageIntegrationTest \
  --tests com.worldmap.recommendation.RecommendationFeedbackIntegrationTest \
  --tests com.worldmap.recommendation.application.RecommendationSurveyServiceTest \
  --tests com.worldmap.recommendation.application.RecommendationCountryProfileCatalogTest \
  --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaCoverageTest \
  --tests com.worldmap.recommendation.application.RecommendationOfflinePersonaSnapshotTest \
  --tests com.worldmap.admin.AdminPersonaBaselineServiceIntegrationTest \
  --tests com.worldmap.admin.AdminRecommendationOpsReviewServiceIntegrationTest
```

수동 확인:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

그 뒤 아래를 확인합니다.

- `/recommendation/survey`
- `/recommendation/survey` POST 후 result page
- `/dashboard/recommendation/feedback`
- `/dashboard/recommendation/persona-baseline`

## 17. 산출물 체크리스트

- 20문항 survey가 SSR로 렌더링된다
- 같은 입력이면 같은 top 3가 나온다
- 결과 페이지는 feedback token만 client에 노출한다
- feedback는 version + answer snapshot과 함께 저장된다
- admin 화면에서 버전별 만족도와 baseline drift를 함께 볼 수 있다
- baseline 18개 시나리오가 테스트로 고정된다

## 18. 글 종료 체크포인트

- 왜 recommendation version 상수는 service에 있어야 하는가
- 왜 profile catalog만으로는 부족하고 seed-backed join이 필요한가
- 왜 feedback token은 session에서 one-time consume해야 하는가
- 왜 만족도 저장은 score만 받고 answer snapshot은 서버 context에서 복원해야 하는가
- 왜 offline baseline과 운영 feedback를 같이 봐야 하는가
- 왜 current ops priority는 응답 수 5개와 평균 3.80점을 기준으로 갈라지는가

## 19. 자주 막히는 지점

- recommendation을 "LLM 호출"로 바로 시작하려는 것
- question structure를 프런트 하드코딩으로 두는 것
- feedback request에 survey/engine/answers를 다시 받는 것
- profile catalog와 seed country join 계약을 빼먹는 것
- baseline test를 coverage만 두고 exact snapshot을 안 두는 것
- 운영 summary와 baseline drift를 서로 다른 언어로 설명하는 것
