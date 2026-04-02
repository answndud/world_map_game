# [Spring Boot 게임 플랫폼 포트폴리오] 105. demo-lite recommendation을 20문항과 30국가로 확장하는 이유

## 1. 이번 글에서 닫을 문제

[104번 글](./104-add-demo-lite-compact-recommendation-loop.md)에서 `demo-lite` recommendation을 먼저 playable하게 닫았습니다.

그 조각은 필요했습니다.

- `demo-lite` 마지막 retained surface를 shell에서 실제 결과 loop로 바꾸고
- free-tier static 앱에서도 추천 결과까지 보여 줄 수 있게 했기 때문입니다.

하지만 compact 6문항 구조에는 한계가 있었습니다.

> 추천 결과는 나오지만, 메인 앱의 20문항 recommendation과 설명 간격이 너무 크다

즉 "free 공개용 체험판"으로는 충분해도,
"현재 제품이 어떤 trade-off를 보고 나라를 고르는가"를 설명하기에는 압축이 과했습니다.

그래서 이번 조각의 목표는 아래처럼 다시 잡았습니다.

- feedback 저장과 ops review는 계속 제거한다
- 대신 질문 breadth와 국가 profile 폭은 메인 앱에 더 가깝게 끌어올린다

핵심은 **runtime persistence는 줄이고, 설명 가능한 scoring breadth는 되살리는 것**입니다.

## 2. 이번에 바뀐 파일

- [demo-lite/src/features/recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)
- [demo-lite/tests/recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)
- [demo-lite/src/routes.js](/Users/alex/project/worldmap/demo-lite/src/routes.js)

문서도 같이 맞췄습니다.

- [demo-lite/README.md](/Users/alex/project/worldmap/demo-lite/README.md)
- [README.md](/Users/alex/project/worldmap/README.md)
- [docs/DEMO_LITE_SCOPE_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_SCOPE_PLAN.md)
- [docs/DEMO_LITE_DECOMPOSITION_PLAN.md](/Users/alex/project/worldmap/docs/DEMO_LITE_DECOMPOSITION_PLAN.md)
- [docs/PORTFOLIO_PLAYBOOK.md](/Users/alex/project/worldmap/docs/PORTFOLIO_PLAYBOOK.md)
- [docs/WORKLOG.md](/Users/alex/project/worldmap/docs/WORKLOG.md)

## 3. 무엇을 유지하고 무엇을 버렸나

### 유지한 것

- 20문항 질문 breadth
- 30개 국가 profile catalog
- deterministic top 3 결과
- 답변 요약
- 나라 metadata join
- 브라우저 안에서 끝나는 local-state 계산

### 버린 것

- feedback token
- 만족도 저장
- `/api/recommendation/feedback`
- dashboard review
- baseline ops view
- `engine-v20` 운영 튜닝 100% parity

즉 이번 조각은 "메인 추천을 다 옮겼다"가 아니라,
**질문 breadth와 profile breadth는 살리고, persistence와 운영 루프만 제거했다**에 가깝습니다.

## 4. 왜 20문항 breadth가 중요했나

compact 6문항 추천은 아래 두 가지를 놓치기 쉬웠습니다.

1. 같은 `도시 선호`라도  
   `빠른 속도`, `군중 밀도`, `디지털 편의`, `영어 지원`, `다양성`, `장기 기반`이 서로 다른 방향으로 갈 수 있다
2. 같은 `안정성 선호`라도  
   `안전`, `공공 서비스`, `정착 친화도`, `future base`, `housing`, `mobility`가 서로 다른 의미를 가진다

메인 recommendation이 설득력을 가지는 이유는 문항 수가 많아서가 아니라,
이런 **trade-off를 축별로 나눠 본다**는 데 있습니다.

그래서 demo-lite도 질문 수를 다시 늘릴 필요가 있었습니다.

## 5. 질문 모델은 어떻게 옮겼나

이번에는 [RecommendationSurveyAnswers.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/domain/RecommendationSurveyAnswers.java) 기준 20개 질문 순서를 그대로 따라갔습니다.

예를 들면:

- `climatePreference`
- `seasonStylePreference`
- `seasonTolerance`
- `pacePreference`
- `crowdPreference`
- `costQualityPreference`
- `housingPreference`
- `environmentPreference`
- `mobilityPreference`
- `englishSupportNeed`
- `newcomerSupportNeed`
- `safetyPriority`
- `publicServicePriority`
- `digitalConveniencePriority`
- `foodImportance`
- `diversityImportance`
- `cultureLeisureImportance`
- `workLifePreference`
- `settlementPreference`
- `futureBasePreference`

그리고 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js) 안에서
각 선택지를 단순 문자열이 아니라 아래처럼 option object로 정규화합니다.

- `value`
- `label`
- `description`
- `targetValue` 또는 `weight`

이렇게 해 두면 브라우저 form은 string을 쓰더라도,
scoring은 숫자/weight 기반으로 안정적으로 계산할 수 있습니다.

## 6. 국가 profile은 어떻게 가져왔나

이번 조각에서는 메인 앱의 [RecommendationCountryProfileCatalog.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java) 기준 30개 profile을
demo-lite 쪽 local table로 옮겼습니다.

즉 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js) 안의 profile은 아래 축을 가집니다.

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

여기에 generated [countries.json](/Users/alex/project/worldmap/src/main/resources/data/countries.json) 를 `iso3Code`로 join해서

- 나라 이름
- 대륙 라벨
- 수도
- 인구

를 결과 카드에 붙입니다.

즉 recommendation surface도 여전히 **정적 국가 데이터 contract** 위에 서 있습니다.

## 7. 어떤 scoring을 브라우저에 남겼나

이번에는 compact scoring을 버리고,
메인 서비스의 broad helper를 브라우저 함수로 다시 옮겼습니다.

기준 파일:

- [RecommendationSurveyService.java](/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationSurveyService.java)

옮긴 핵심은 아래입니다.

- `closenessScore`
- `exactMatchBonus`
- `supportPoints`
- `priorityPoints`
- `cost overshoot penalty`
- `coherenceBonus`
- `weatherDemand`
- `crowdEnergy`
- `transitSupport`
- `newcomerSupport`
- `futureBase`
- 대표 scenario bonus 일부

즉 demo-lite recommendation은 이제 단순 "6축 점수 합"이 아니라,
**메인 앱과 비슷한 방향의 helper 조합**으로 top 3를 계산합니다.

다만 여기서도 경계는 분명히 둡니다.

- 메인 recommendation의 운영용 tuning과 완전 동일하다고 말하지 않는다
- free-tier sibling 앱에서 설명 가능한 breadth까지만 가져온다

## 8. 왜 이 로직을 여전히 `recommendation.js`에 뒀나

[app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)의 책임은 route shell입니다.

- 현재 hash route 결정
- 공통 navigation 렌더링
- feature mount 연결

반면 recommendation의 책임은 다릅니다.

- 20문항 질문 카탈로그
- answer normalization
- 30국가 profile table
- scoring helper
- top 3 정렬
- 결과 렌더링

이건 route shell이 아니라 **추천 규칙**이므로,
이번에도 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)에 남기는 편이 맞았습니다.

## 9. 새 브라우저 흐름

메인 Spring Boot 요청 흐름은 바뀌지 않았습니다.

이번 조각에서 새로 정리한 건 demo-lite recommendation 브라우저 흐름입니다.

1. 브라우저가 [index.html](/Users/alex/project/worldmap/demo-lite/index.html)을 연다
2. [main.js](/Users/alex/project/worldmap/demo-lite/src/main.js)가 hash route를 읽는다
3. [app.js](/Users/alex/project/worldmap/demo-lite/src/app.js)가 `#/recommendation` route를 [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)에 연결한다
4. [recommendation.js](/Users/alex/project/worldmap/demo-lite/src/features/recommendation.js)가 generated countries와 local profile catalog를 join한다
5. 사용자가 20문항에 답하면 브라우저 안에서 answer option을 normalize하고 deterministic top 3를 계산한다
6. 결과 카드에 이유 3개, 수도, 인구, match score를 함께 보여 준다

즉 여전히 API 호출은 없고,
**질문 breadth만 늘어난 브라우저 내부 state machine**입니다.

## 10. 무엇을 테스트로 고정했나

이번에는 [recommendation.test.mjs](/Users/alex/project/worldmap/demo-lite/tests/recommendation.test.mjs)를 같이 키웠습니다.

```bash
cd demo-lite
npm test
```

현재 고정한 것은 아래입니다.

1. 질문 수가 20개인지
2. profile 수가 30개인지
3. 답 요약과 top 3 결과가 나오는지
4. warm premium city 시나리오에서 `싱가포르`가 1위인지
5. cold nature 시나리오에서 `뉴질랜드`가 1위인지
6. warm foodie value 시나리오에서 `말레이시아`가 1위인지

그리고 정적 앱 전체는 다시 빌드로 확인했습니다.

```bash
cd demo-lite
npm run build
```

즉 이번 조각의 핵심 검증은
"질문 breadth를 다시 넓혀도 deterministic top 3가 여전히 설명 가능하게 유지되는가"입니다.

## 11. 지금 남은 것

이제 demo-lite recommendation은 6문항 compact shell이 아니라,
메인 앱에 더 가까운 20문항 surface가 되었습니다.

남은 것은 추천 기능 자체보다 demo-lite 전체 마감도 쪽입니다.

예를 들면:

1. browser 단위 recent play/history
2. cross-mode summary
3. 결과/홈 copy 더 다듬기
4. free static host 실제 배포

즉 다음 조각은 recommendation 기능 확장보다,
**demo-lite를 실제 공개 가능한 앱으로 마감하는 작업**에 더 가깝습니다.

## 12. 30초 답변

demo-lite recommendation을 compact 6문항 버전에서 20문항·30국가 구조로 확장했습니다. 메인 Spring Boot 추천 엔진의 broad scoring helper를 브라우저 로컬 함수로 옮기고, generated countries와 30개 프로필을 `iso3Code`로 join해서 deterministic top 3를 계산합니다. 핵심은 free-tier 공개용 앱에서도 feedback 저장 없이 질문 breadth와 추천 후보 폭을 유지해, 메인 앱과 더 가까운 설명이 가능해졌다는 점입니다.
