# [Spring Boot 포트폴리오] 18. 새 추천 문항을 실제로 쓰는 active-signal 페르소나 추가하기

## 이번 글의 핵심 질문

추천 설문을 8문항으로 늘렸다고 해서, 오프라인 품질 평가도 자동으로 좋아지는 것은 아니다.

이번 단계의 질문은 이것이다.

“새로 추가한 `정착 성향`, `이동 생활 방식` 문항이 실제로 추천 후보를 바꾸는지 어떻게 검증할까?”

이번에는 기존 14개 중립 baseline 위에, 새 두 문항을 적극적으로 쓰는 `P15~P18` 시나리오를 추가했다.

## 왜 이 단계가 필요한가

기존 baseline은 안전했다.

- 새 두 문항을 모두 `BALANCED`로 두고
- 기존 추천 품질 기준선을 흔들지 않으며
- 8문항 구조만 먼저 정착시키는 데 목적이 있었다.

하지만 이 상태로는 중요한 질문에 답하기 어렵다.

- `EXPERIENCE`가 실제로 모험형 후보를 올리는가
- `STABILITY`가 정착형 후보를 올리는가
- `TRANSIT_FIRST`와 `SPACE_FIRST`가 같은 기본 취향에서도 후보 구성을 바꾸는가

즉, 새 문항을 넣었으면 품질 평가 시나리오도 그 문항을 실제로 써야 한다.

## 이번 글에서 다룰 파일

- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaFixtures.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaCoverageTest.java`
- `/Users/alex/project/worldmap/src/test/java/com/worldmap/recommendation/application/RecommendationOfflinePersonaSnapshotTest.java`
- `/Users/alex/project/worldmap/docs/recommendation/PERSONA_EVAL_SET.md`
- `/Users/alex/project/worldmap/docs/recommendation/SURVEY_V2_PROPOSAL.md`

## 어떤 시나리오를 추가했는가

이번에 추가한 건 “완전히 새로운 취향”보다 “같은 기본 취향에서 새 두 문항만 바꾼 비교 페어”다.

### 1. P15 / P16

기본 조건은 같다.

- `MILD`
- `RELAXED`
- `LOW`
- `NATURE`
- `MEDIUM`
- `SAFETY`

여기서 새 두 문항만 바꿨다.

- `P15`: `EXPERIENCE / TRANSIT_FIRST`
- `P16`: `STABILITY / SPACE_FIRST`

의도는 명확하다.

- P15는 “먼저 살아보며 적응”하는 쪽
- P16은 “장기 정착과 넓은 생활 공간”을 보는 쪽

실제 snapshot에서는 두 시나리오가 아래처럼 갈린다.

- `P15`: `뉴질랜드, 말레이시아, 우루과이`
- `P16`: `뉴질랜드, 우루과이, 포르투갈`

즉, 같은 기본 조건이라도 새 문항에 따라 2~3위 후보 구성이 달라진다.

### 2. P17 / P18

이번에는 빠른 고도시 취향을 기준으로 같은 방식으로 짝을 만들었다.

기본 조건:

- `WARM`
- `FAST`
- `MEDIUM`
- `CITY`
- `MEDIUM`
- `DIVERSITY`

변하는 것은 다시 새 두 문항뿐이다.

- `P17`: `EXPERIENCE / TRANSIT_FIRST`
- `P18`: `STABILITY / SPACE_FIRST`

snapshot 결과는 아래처럼 나온다.

- `P17`: `싱가포르, 아랍에미리트, 브라질`
- `P18`: `싱가포르, 아랍에미리트, 대한민국`

1~2위는 유지되지만, 3위권 후보가 “경험형”에서 “정착형”으로 이동한다.

## 왜 이 로직은 컨트롤러가 아니라 테스트/서비스 경계에서 봐야 하는가

이번 단계는 런타임 기능을 늘린 것이 아니다.

사용자 요청 흐름은 그대로다.

- `GET /recommendation/survey`
- `POST /recommendation/survey`
- `RecommendationSurveyService.recommend()`

바뀐 것은 추천 품질을 어떻게 검증하느냐이다.

그래서 핵심은 컨트롤러가 아니라 아래 두 테스트에 있다.

1. `RecommendationOfflinePersonaCoverageTest`
   - 18개 시나리오 중 최소 15개가 기대 후보 1개 이상을 top 3에 포함하는지 본다.
2. `RecommendationOfflinePersonaSnapshotTest`
   - 현재 top 3 순서를 exact snapshot으로 고정한다.

즉, 새 문항이 “있다”가 아니라 “실제로 top 3를 바꾼다”를 코드로 증명하는 단계다.

## 왜 coverage만으로는 부족한가

coverage는 “기대 후보가 하나라도 들어왔는가”만 본다.

하지만 새 문항은 지금 가중치가 크지 않아서, 주로 2~3위 후보를 바꾸는 식으로 작동한다.

이 차이는 coverage 숫자만 보면 잘 드러나지 않는다.

그래서 snapshot이 중요하다.

예를 들어 `P15`와 `P16`은 둘 다 coverage는 통과할 수 있다.
하지만 실제로는 `말레이시아`가 들어오느냐 `포르투갈`이 들어오느냐가 다르다.

이 차이가 바로 새 문항이 살아 있다는 증거다.

## 테스트는 무엇을 확인했는가

이번 단계에서는 세 가지를 같이 봤다.

1. fixture 수 증가
   - 14개 -> 18개
2. baseline 하한 갱신
   - `11/14` -> `15/18`
3. active-signal 페어 차이 확인
   - `P15`에는 `말레이시아`가 있고 `포르투갈`은 없다
   - `P16`에는 `포르투갈`이 있고 `말레이시아`는 없다
   - `P17`에는 `브라질`이 있고 `대한민국`은 없다
   - `P18`에는 `대한민국`이 있고 `브라질`은 없다

## 면접에서는 이렇게 설명하면 된다

“추천 설문을 8문항으로 늘린 뒤에도 오프라인 baseline이 새 문항을 실제로 쓰지 않으면 품질 평가가 공허해집니다. 그래서 기존 14개 중립 시나리오 외에, 같은 기본 취향에서 `정착 성향`과 `이동 생활 방식`만 바꾼 4개 active-signal 시나리오를 추가했습니다. 이제 추천 엔진 실험에서는 coverage 숫자뿐 아니라 새 문항이 실제 후보 구성을 어떻게 바꾸는지도 snapshot 테스트로 같이 볼 수 있습니다.”

## 다음 글

다음 단계는 이 18개 baseline 위에서 실제 `engine-v2` 가중치 실험을 다시 하는 것이다.

이제는 “coverage가 올랐는가”뿐 아니라 “새 문항 신호가 과도하게 세졌는가, 너무 약한가”도 같이 비교할 수 있다.
