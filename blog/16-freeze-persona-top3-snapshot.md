# [Spring Boot 포트폴리오] 16. 추천 엔진 실험 전 persona top3 snapshot 고정하기

## 이번 글의 핵심 질문

페르소나 coverage 하한을 테스트로 고정했다면, 바로 가중치를 바꿔도 될까?

이번 단계의 답은 “아직 아니다”였다.

실제로 추천 점수식을 조금만 바꿔도 `11/14` 숫자는 유지되면서 top 3 순서가 크게 흔들릴 수 있고, 반대로 약한 시나리오 하나를 고치려다 다른 시나리오가 더 쉽게 깨질 수 있다.

그래서 이번에는 production 점수식을 더 바꾸기 전에, 현재 `engine-v1`의 14개 페르소나 top 3 결과를 snapshot으로 먼저 고정했다.

## 왜 coverage 테스트만으로는 부족했는가

`RecommendationOfflinePersonaCoverageTest`는 중요한 테스트다.

하지만 이 테스트는 기준이 비교적 넓다.

- 기대 후보가 top 3 안에 하나라도 들어오면 통과한다.

이 기준만으로는 아래 상황을 잡기 어렵다.

1. 기대 후보는 들어오지만 1위가 계속 이상한 나라로 남는 경우
2. weak scenario를 고치려다 다른 시나리오의 top 3 순서가 크게 흔들리는 경우
3. coverage 숫자는 같지만 체감 품질은 나빠진 경우

즉, 이제는 “들어왔는가”만이 아니라 “어떤 순서로 나왔는가”도 같이 봐야 한다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaFixtures.java`

## snapshot 테스트는 무엇을 고정하는가

이번 테스트는 14개 페르소나 각각에 대해 현재 `RecommendationSurveyService.recommend()`가 반환하는 top 3 국가 이름을 그대로 고정한다.

예를 들면 이런 식이다.

- `P01`: `싱가포르, 아랍에미리트, 미국`
- `P04`: `우루과이, 칠레, 스페인`
- `P13`: `미국, 싱가포르, 아랍에미리트`

즉, coverage 테스트가 “품질 하한”이라면, snapshot 테스트는 “현재 출력 기준선”이다.

## 왜 지금 snapshot을 먼저 잡는가

이번 단계에서 실제로 점수식 조정을 먼저 시도해 보니, 약한 시나리오 하나를 움직이는 동안 다른 시나리오가 더 쉽게 깨졌다.

이 경험이 의미하는 것은 분명하다.

1. 추천 엔진은 생각보다 민감하다.
2. weak scenario만 보고 가중치를 만지면 전체 균형이 흔들릴 수 있다.
3. 그래서 다음 실험 전에는 exact top 3 기준도 함께 고정해야 한다.

즉, 이번 단계는 “개선 적용”보다 “개선 실험을 더 안전하게 만드는 준비”에 가깝다.

## 이 테스트가 주는 장점

1. 다음 `engine-v2` 실험에서 무엇이 바뀌었는지 바로 diff처럼 볼 수 있다.
2. coverage는 유지됐는데 top 3 순서가 이상하게 흔들린 경우를 잡을 수 있다.
3. 면접에서 “추천 품질을 어떻게 관리했는가”를 더 구체적으로 설명할 수 있다.

## 면접에서는 이렇게 설명하면 된다

“추천 엔진 품질을 `기대 후보가 하나라도 들어오는가` 수준에서만 보지 않았습니다. 다음 가중치 실험에서 어떤 나라가 1~3위로 움직였는지도 비교할 수 있도록, 14개 페르소나의 현재 top 3 결과를 snapshot 테스트로 먼저 고정했습니다. 그래서 이후 `engine-v2` 실험은 coverage와 exact ranking 변화를 같이 보면서 더 안전하게 진행할 수 있습니다.”

## 다음 글

다음 단계는 다시 `engine-v2` 실험으로 돌아간다.

다만 이번에는 그냥 점수식을 건드리는 것이 아니라,

1. coverage 하한
2. persona top 3 snapshot
3. weak scenario 개선 목표

이 세 가지를 같이 보면서 조정하는 방식으로 간다.
