# [Spring Boot 포트폴리오] 10. 설문 추천 결과가 몇 나라에만 몰리지 않게 후보 국가 풀을 넓히기

## 이번 글의 핵심 질문

추천 엔진을 처음 만들면 보통 점수 공식에 먼저 눈이 간다.

하지만 실제로는 그보다 먼저 확인해야 할 것이 있다.

“지금 비교 대상 국가 풀이 너무 좁아서 결과가 몇 나라에만 몰리는 건 아닌가?”

이번 단계에서는 추천 계산 공식을 크게 바꾸지 않고, `RecommendationCountryProfileCatalog`의 후보 국가를 12개에서 30개로 넓혀 추천 결과의 다양성을 먼저 높인다.

## 왜 이 단계가 필요한가

설문 기반 추천은 deterministic하더라도 후보가 너무 적으면 사용자가 느끼는 품질이 금방 한계에 부딪힌다.

예를 들면 이런 문제가 생긴다.

1. 결과가 항상 비슷한 국가 몇 개로 반복될 수 있다.
2. 특정 지역 취향을 가진 사용자에게 선택지가 너무 좁아진다.
3. “추천 품질은 데이터에 달려 있다”는 말을 실제 코드로 보여주기 어렵다.

즉, 추천 엔진 1차를 만든 다음 바로 해야 할 일은 가중치를 뒤엎는 것이 아니라 `비교 대상 데이터 풀을 넓히는 것`이다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/main/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalog.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationCountryProfileCatalogTest.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationSurveyServiceTest.java`

## 이번에 무엇을 바꿨는가

`RecommendationCountryProfileCatalog`를 아래 방향으로 확장했다.

- 북미: 미국, 캐나다
- 유럽: 영국, 아일랜드, 프랑스, 이탈리아, 독일, 네덜란드, 스위스, 오스트리아, 스웨덴, 덴마크, 노르웨이, 핀란드, 스페인, 포르투갈
- 아시아: 대한민국, 일본, 싱가포르, 태국, 말레이시아, 베트남, 아랍에미리트
- 오세아니아: 호주, 뉴질랜드
- 북중미: 미국, 캐나다, 멕시코
- 남미: 브라질, 칠레, 우루과이
- 아프리카: 남아프리카공화국

핵심은 “국가 수를 그냥 늘렸다”가 아니다.

기후, 생활 속도, 물가, 도시성, 영어 친화도, 안전, 복지, 음식, 다양성 값의 분포가 더 넓어지도록 카탈로그를 확장했다.

## 왜 이 로직은 여전히 서비스에 남아 있어야 하는가

후보 국가가 늘어났다고 해서 계산 책임이 컨트롤러로 가면 안 된다.

지금도 추천 흐름은 그대로다.

1. 컨트롤러가 설문 입력을 검증한다.
2. `RecommendationSurveyAnswers` 불변 객체로 변환한다.
3. `RecommendationSurveyService`가 카탈로그를 순회하며 점수를 계산한다.
4. top 3를 정렬해 결과를 만든다.

즉, 바뀐 것은 서비스가 순회하는 데이터 폭이고, 계산 책임은 여전히 서비스가 가진다.

## 왜 DB가 아니라 카탈로그를 먼저 넓혔는가

이번 단계에서도 추천 속성은 DB 테이블로 옮기지 않았다.

이유는 간단하다.

1. 지금 병목은 저장 구조가 아니라 후보 데이터의 폭이다.
2. 추천 속성 값 자체를 더 실험하면서 다듬어야 한다.
3. 구조를 너무 빨리 DB로 고정하면 수정 비용이 커진다.

즉, 아직은 `country` 테이블은 기본 정보 source of truth, `RecommendationCountryProfileCatalog`는 추천 실험 레이어로 분리하는 편이 맞다.

## 테스트는 무엇을 추가했는가

### 1. `RecommendationCountryProfileCatalogTest`

이 테스트는 세 가지를 본다.

1. 프로필이 30개인지
2. ISO 코드가 중복되지 않는지
3. 모든 ISO 코드가 실제 `countries.json` 시드에 존재하는지

즉, 추천 후보를 넓히면서 생길 수 있는 가장 흔한 실수인 `중복`과 `잘못된 코드`를 먼저 막았다.

### 2. `RecommendationSurveyServiceTest`

기존 deterministic 테스트는 그대로 유지하고, 새로 확장된 후보 풀에서 신규 국가가 실제 상위권에 올라오는 시나리오를 추가했다.

예를 들어 `따뜻한 기후 + 균형 잡힌 속도 + 낮은 물가 + 도시 중심 + 음식 우선` 조합에서는 `말레이시아`가 1위로 나오도록 기대값을 고정했다.

즉, 후보가 늘어난 것이 실제 추천 결과에도 반영된다는 점을 테스트로 보여준다.

## 면접에서는 이렇게 설명하면 된다

“추천 엔진은 계산식만이 아니라 어떤 후보 데이터를 비교하느냐에 크게 의존합니다. 그래서 이번 단계에서는 점수 수식을 크게 바꾸지 않고, `RecommendationCountryProfileCatalog`를 12개에서 30개로 확장해 지역과 속성 분포를 넓혔습니다. 그리고 ISO 유효성, 중복 여부, 신규 후보의 실제 상위권 진입까지 테스트로 고정해 추천 다양성을 먼저 끌어올렸습니다.”

## 다음 글

다음 단계는 `가중치 튜닝과 경계값 조정`이다.

이제 후보 데이터 풀은 어느 정도 넓어졌으니, 다음에는 점수식의 섬세함을 다듬어 “저물가 선호”, “영어 중요도”, “정확 일치” 같은 조건이 실제 순위에 어떻게 반영될지 더 직접 조정한다.
